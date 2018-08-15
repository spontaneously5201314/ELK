### 启动一个节点
```
bin/elasticsearch -Ecluster.name=test -Enode.name=node1 -Epath.data=test_node1 -Ehttp.port=5200 -d
bin/elasticsearch -Ecluster.name=test -Enode.name=node2 -Epath.data=test_node2 -Ehttp.port=5300 -d
```

### 提高系统可用性
> 服务可用性
```
2个节点的情况下，允许其中１个节点停止服务
```
> 数据可用性
```
- 引入副本(Replication)解决
- 每个节点上都有完备的数据
```

### 增大系统容量
> 如何将数据分布于所有节点上？
```
引入分片(Shard)解决问题
```
> 分片是ES支持PB级数据的基石
```
- 分片存储了部分数据，可以分布于任意节点上
- 分片数在索引创建时指定且后续不允许再更改，默认为5个
- 分片有主分片和副本分片之分，以实现数据的高可用
- 副本分片的数据由主分片同步，可以有多个副本分片，从而提高读取的吞吐量
```

### 两个问题
> 在创建完索引之后，增加节点是否能提高索引的数据容量？
````
不能，因为在创建索引的时候，就指定了分片的数量，所以新增的节点无法利用。
````
> 在创建完索引之后，增加副本数是否可以提高索引的读取吞吐量？
```
不能，因为创建索引的时候已经指定节点数量。新增的副本也是分布在原来的节点上，还是利用了同样的资源。如果要增加吞吐量，还需要新增节点。
```

### 分片的设置
```
- 分片过小会导致后续无法通过增加节点来实现水平扩容
- 分片过大会导致一个节点上分布过多分片，造成资源浪费，同时会影响查询性能
```

### 文档分布式存储
> 文档是如何存储到分片上的？选择分片的依据是什么？
```
需要文档到分片的映射算法 
```
> 目的
```
使得文档均匀分布在所有分片上，以充分利用资源
```
> 映射算法
```
- shard = hash(routing) % number_of_primary_shards
- hash算法保证可以将数据均匀的分散在分片中
- routing是一个关键参数，默认是文档id，也可以自行指定
- number_of_primary_shards主分片数
```

### ES中文档的创建和读取流程
![单个文档创建流程](https://github.com/spontaneously5201314/ELK/blob/master/src/doc/img/cluster/01_create_document.png)
![单个文档读取流程](https://github.com/spontaneously5201314/ELK/blob/master/src/doc/img/cluster/02_read_document.png)
![文档批量创建流程](https://github.com/spontaneously5201314/ELK/blob/master/src/doc/img/cluster/03_bulk_create_document.png)
![文档批量读取流程](https://github.com/spontaneously5201314/ELK/blob/master/src/doc/img/cluster/04_bulk_read_document.png)
![脑裂问题](https://github.com/spontaneously5201314/ELK/blob/master/src/doc/img/cluster/05_split_brain.png)
![脑裂问题的解决方案](https://github.com/spontaneously5201314/ELK/blob/master/src/doc/img/cluster/06_solution_of_split_brain.png)
![倒排索引的不可变更](https://github.com/spontaneously5201314/ELK/blob/master/src/doc/img/cluster/07_Invariability%20of%20inverted%20index.png)