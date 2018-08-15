### ES中Search的运行机制
> 主要分为两个步骤
- Query阶段   
![Query阶段](https://github.com/spontaneously5201314/ELK/blob/master/src/doc/img/search/01_query_stage.png)
- Fetch阶段   
![Fetch阶段](https://github.com/spontaneously5201314/ELK/blob/master/src/doc/img/search/02_fetch_stage.png)

### 相关性算分问题
> 相关性算分在shard与shard之间是相互独立的，也就意味着同一个term的IDF等值在不同的shard上是不同的。文档的相关性算分和它所处的shard相关   
> 在文档数量不多的时候，会导致相关性算分严重不准的情况发生  
> 案例演示：     
> 1.创建三个文档： 
```
POST test_search_relevance/doc
{
  "name":"hello"
}

POST test_search_relevance/doc
{
  "name":"hello,world"
}

POST test_search_relevance/doc
{
  "name":"hello,world!a beautiful world"
}
```
> 2.执行查找，如果想要查看更加详细的查询过程，可以使用 "explain": true来查看
```
GET test_search_relevance/_search
{
  "explain": false,
  "query": {
    "match":{
      "name":"hello"
    }
  }
}
```
> 3.可见，结果中hello并不是排在第一的，并且三个匹配的结果的_score都是相同的
```
"hits": {
    "total": 3,
    "max_score": 0.2876821,
    "hits": [
      {
        "_index": "test_search_relevance",
        "_type": "doc",
        "_id": "tdNZPGUBWIylKjQWKXOZ",
        "_score": 0.2876821,
        "_source": {
          "name": "hello,world"
        }
      },
      {
        "_index": "test_search_relevance",
        "_type": "doc",
        "_id": "ttNZPGUBWIylKjQWL3M4",
        "_score": 0.2876821,
        "_source": {
          "name": "hello,world!a beautiful world"
        }
      },
      {
        "_index": "test_search_relevance",
        "_type": "doc",
        "_id": "s9NZPGUBWIylKjQWJ3NE",
        "_score": 0.2876821,
        "_source": {
          "name": "hello"
        }
      }
    ]
  }
```
> 4.删除创建的文档和索引，手动创建索引，并制定shards的数量为1，并按照1中的步骤插入数据
```
DELETE test_search_relevance
PUT test_search_relevance
{
  "settings": {
    "index":{
      "number_of_shards":1
    }
  }
}
```
> 5.再次执行查找，可以看到结果中，三个文档的_score就是不同的了。
```
"hits": {
    "total": 3,
    "max_score": 0.17940095,
    "hits": [
      {
        "_index": "test_search_relevance",
        "_type": "doc",
        "_id": "ftNcPGUBWIylKjQWLXTY",
        "_score": 0.17940095,
        "_source": {
          "name": "hello"
        }
      },
      {
        "_index": "test_search_relevance",
        "_type": "doc",
        "_id": "f9NcPGUBWIylKjQWMnRN",
        "_score": 0.14874382,
        "_source": {
          "name": "hello,world"
        }
      },
      {
        "_index": "test_search_relevance",
        "_type": "doc",
        "_id": "gdNcPGUBWIylKjQWN3TO",
        "_score": 0.09833273,
        "_source": {
          "name": "hello,world!a beautiful world"
        }
      }
    ]
  }
```
> 6.解决思路有两个：
```
- 设置分片的数量为1个，从根本上排除问题，在文档数量不多的时候，可以考虑该方案，比如百万到千万级别的文档数量，但是查询效率会下降很多
- 使用DFS Query-then-Fetch查询方式
```
> 7.DFS Query-then-Fetch查询方式
```
该方式是在拿到所有的文档之后再重新完整的计算一次相关性算分，耗费耕读偶读cpu和内存，执行性能也比较底下，一般不建议使用，使用方式如下：
GET test_search_relevance/_search?search_type=dfs_query_then_fetch
{
  "query": {
    "match":{
      "name":"hello"
    }
  }
}
```

### 分页与遍历
> ES提供了三种方式来解决该问题
```
- from/size
- scroll
- search_after
```

### from/size
```
GET test_search_index/_search
{
  "from":4,
  "size":2
}
其中，from指明开始位置，size指明获取总数
```

### from/size引发的深度分页问题
![深度分页](https://github.com/spontaneously5201314/ELK/blob/master/src/doc/img/search/03_devide_search.png)

### scroll
> 需要注意的是，scroll是利用快照机制的，但是不停的创建快照是很浪费空间的
```
GET test_search_index/_search?scroll=5m
{
  "size":1
}


GET _search/scroll
GET test_search_index/_search

# new doc can not be searched
PUT test_search_index/doc/10
{
  "username":"doc10"
}


DELETE test_search_index/doc/10

POST _search/scroll
{
  "scroll" : "5m",
  "scroll_id": "DXF1ZXJ5QW5kRmV0Y2gBAAAAAAAABswWX3FLSTZFOF9URFdqWHlvX3gtYmhtdw=="
}

DELETE _search/scroll/_all
```

### Search_After
> 避免深度分页的性能问题，提供实时的下一页文档获取功能
- 缺点是不能使用from参数，即不能指定页数
- 只能下一页，不能上一页