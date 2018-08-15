### 聚合分析的分类
> Bucket,分桶类型，类似SQL中的GROUP BY语法    
> Metric，指标分析类型，比如计算最大值，最小值，平均值等    
> Pipeline，管道分析类型，基于上一级的聚合分析的结果进行再分析    
> Matrix，矩阵分析类型

### Metric聚合分析的分类
```
准备数据：
POST test_search_index/doc/_bulk
{
  "index": {
    "_id": "1"
  }
}
{
  "username": "alfred way",
  "job": "java engineer",
  "age": 18,
  "birth": "1990-01-02",
  "isMarried": false,
  "salary": 10000
}
{
  "index": {
    "_id": "2"
  }
}
{
  "username": "tom",
  "job": "java senior engineer",
  "age": 28,
  "birth": "1980-05-07",
  "isMarried": true,
  "salary": 30000
}
{
  "index": {
    "_id": "3"
  }
}
{
  "username": "lee",
  "job": "ruby engineer",
  "age": 22,
  "birth": "1985-08-07",
  "isMarried": false,
  "salary": 15000
}
{
  "index": {
    "_id": "4"
  }
}
{
  "username": "Nick",
  "job": "web engineer",
  "age": 23,
  "birth": "1989-08-07",
  "isMarried": false,
  "salary": 8000
}
{
  "index": {
    "_id": "5"
  }
}
{
  "username": "Niko",
  "job": "web engineer",
  "age": 18,
  "birth": "1994-08-07",
  "isMarried": false,
  "salary": 5000
}
{
  "index": {
    "_id": "6"
  }
}
{
  "username": "Michell",
  "job": "ruby engineer",
  "age": 26,
  "birth": "1987-08-07",
  "isMarried": false,
  "salary": 12000
}
```
> 单值分析，只输出一个分析结果
```
- min/max/avg/sum
GET test_search_index/_search
{
  "size": 0,
  "aggs": {
    "min_age": {
      "min": {
        "field": "age"
      }
    },
    "max_age": {
      "max": {
        "field": "age"
      }
    },
    "avg_age": {
      "avg": {
        "field": "age"
      }
    },
    "sum_age": {
      "sum": {
        "field": "age"
      }
    }
  }
}
```
```
- cardinality
GET test_search_index/_search
{
  "size": 0,
  "aggs": {
    "count_of_job": {
      "cardinality": {
        "field": "job.keyword"
      }
    }
  }
}
```
> 多值分析，输出多个分析结果
```
- stats(包含min/max/avg/sum/count)
GET test_search_index/_search
{
  "size": 0,
  "aggs": {
    "stats_age": {
      "stats": {
        "field": "age"
      }
    }
  }
}
```
```
- extended stats
GET test_search_index/_search
{
  "size": 0,
  "aggs": {
    "exstats_salary": {
      "extended_stats": {
        "field": "salary"
      }
    }
  }
}
```
```
- percentile
GET test_search_index/_search
{
  "size": 0,
  "aggs": {
    "per_salary": {
      "percentiles": {
        "field": "salary",
        "percents": [
          1,
          5,
          25,
          50,
          75,
          95,
          99
        ]
      }
    }
  }
}
```
```
- percentile rank
用于查找11000和30000在当前的排名中所处的位置
GET test_search_index/_search
{
  "size": 0,
  "aggs": {
    "per_salary": {
      "percentile_ranks": {
        "field": "salary",
        "values": [
          11000,
          30000
        ]
      }
    }
  }
}
```
```
- top hits，一般用于分桶后获取该桶内最匹配的顶部文档列表，即详情数据
GET test_search_index/_search
{
  "size": 0,
  "aggs": {
    "jobs": {
      "terms": {
        "field": "job.keyword",
        "size": 10
      },
      "aggs": {
        "top_employee": {
          "top_hits": {
            "size": 10,
            "sort": [
              {
                "age": {
                  "order": "desc"
                }
              }
            ]
          }
        }
      }
    }
  }
}
```

### Bucket聚合分析的分类
> Terms，直接按照term来分桶，如果是text类型，则按照分词后的结果来分桶
```
GET test_search_index/_search
{
  # size是用来指定返回的数据中带不带原文档的，如果是0就是不带
  "size": 20,   
  "aggs": {
    "jobs": {
      "terms": {
        "field": "job.keyword",
        "size": 10
      }
    }
  }
}
```
> Range，通过指定数值的范围来设定分桶规则    
```
GET test_search_index/_search
{
  "size": 0,
  "aggs": {
    "salary_range": {
      "range": {
        "field": "salary",
        "ranges": [
          {
            "to": 10000
          },
          {
            "from": 10000,
            "to": 20000
          },
          {
            "from": 20000
          }
        ]
      }
    }
  }
}
```
> Data Range，通过指定日期的范围来设定分桶规则   
```
GET test_search_index/_search
{
  "size": 0,
  "aggs": {
    "date_range": {
      "date_range": {
        "field": "birth",
        "format": "yyyy", 
        "ranges": [
          {
            "from": "1980",
            "to": "1990"
          },
          {
            "from": "1990",
            "to": "2000"
          },
          {
            "from": "2000"
          }
        ]
      }
    }
  }
}
```
> Histogram，直方图，以固定间隔的策略来分割数据
```
GET test_search_index/_search
{
  "size": 0,
  "aggs": {
    "salary_hist": {
      "histogram": {
        "field": "salary",
        "interval": 5000,
        "extended_bounds": {
          "min": 0,
          "max": 40000
        }
      }
    }
  }
}
```   
> Date Histogram，针对日期的直方图或者柱状图，是时序数据分析中常用的聚合分析类型
```
GET test_search_index/_search
{
  "size": 0,
  "aggs": {
    "by_year": {
      "date_histogram": {
        "field": "birth",
        "interval": "year",
        "format": "yyyy"
      }
    }
  }
}
```

### Bucket+Metric聚合分析
> Bucket聚合分析允许通过添加子分析来进一步进行分析，该子分析可以死Bucket也可以是Metric   
```
GET test_search_index/_search
{
  "size": 0,
  "aggs": {
    "jobs": {
      "terms": {
        "field": "job.keyword",
        "size": 10
      },
      "aggs": {
        "age_range": {
          "range": {
            "field": "age",
            "ranges": [
              {
                "to": 20
              },
              {
                "from": 20,
                "to": 30
              },
              {
                "from": 30
              }
            ]
          }
        }
      }
    }
  }
}
---------------------------------------------------
GET test_search_index/_search
{
  "size": 0,
  "aggs": {
    "jobs": {
      "terms": {
        "field": "job.keyword",
        "size": 10
      },
      "aggs": {
        "salary": {
          "stats": {
            "field": "salary"
          }
        }
      }
    }
  }
}
```