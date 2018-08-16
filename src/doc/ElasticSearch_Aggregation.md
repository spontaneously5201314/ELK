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

### Pipeline聚合分析
> 针对聚合分析的结果再次进行聚合分析，而且支持链式调拥    
> 所有的Pipeline都有buckets_path关键词      
> Pipeline的分析结果会输出到原结果中，分局输出位置的不同，分为两类  
> 1.Parent：结果内嵌到现有的聚合分析结果中
```
- Derivative
GET test_search_index/_search
{
  "size": 0,
  "aggs": {
    "birth": {
      "date_histogram": {
        "field": "birth",
        "interval": "year",
        "min_doc_count": 0
      },
      "aggs": {
        "avg_salary": {
          "avg": {
            "field": "salary"
          }
        },
        "derivative_avg_salary": {
          "derivative": {
            "buckets_path": "avg_salary"
          }
        }
      }
    }
  }
}
- Moving Average
- Cumulative Sum
```
> 2.Sibling：结果与现有聚合分析结果同级
```
- Max Bucket/Min Bucket/Avg Bucket/Sum Bucket
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
        "avg_salary": {
          "avg": {
            "field": "salary"
          }
        }
      }
    },
    "min_salary_by_job": {
      "min_bucket": {
        "buckets_path": "jobs>avg_salary"
      }
    }
  }
}
- Stats/Extended Stats Bucket
- Percentiles Bucket
```

### 聚合分析的作用范围
> ES聚合分析默认的作用范围是query的结果集，可以通过如下的方式改变其作用范围
- filter：为某个聚合分析设定过滤条件，从而在不更改整体query语句的情况下修改作用范围
```
GET test_search_index/_search
{
  "size": 0,
  "aggs": {
    "jobs_salary_small": {
      "filter": {
        "range": {
          "salary": {
            "gte": 0,
            "lte": 10000
          }
        }
      },
      "aggs": {
        "jobs": {
          "terms": {
            "field": "job.keyword",
            "size": 10
          }
        }
      }
    },
    "jobs": {
      "terms": {
        "field": "job.keyword",
        "size": 10
      }
    }
  }
}
```
- post_filter：作用于文档过滤，但在聚合分析后生效
```
GET test_search_index/_search
{
  "aggs": {
    "jobs": {
      "terms": {
        "field": "job.keyword",
        "size": 10
      }
    }
  },
  "post_filter": {
    "match": {
      "job.keyword": "java engineer"
    }
  }
}
```
- global：无视query过滤条件，基于全部文档进行分析 
```
GET test_search_index/_search
{
  "query": {
    "match": {
      "job.keyword": "java engineer"
    }
  },
  "aggs": {
    "java_arg_salary": {
      "avg": {
        "field": "salary"
      }
    },
    "all": {
      "global": {},
      "aggs": {
        "avg_salary": {
          "avg": {
            "field": "salary"
          }
        }
      }
    }
  }
}
```

### 排序
> 可以使用自带的关键数据进行排序，比如：
- _count文档数
- _key按照key值排序
```
GET test_search_index/_search
{
  "size": 0,
  "aggs": {
    "jobs": {
      "terms": {
        "field": "job.keyword",
        "size": 10,
        "order": [
          {
            "_count": "asc"
          },
          {
            "_key": "desc"
          }
        ]
      }
    }
  }
}
--------------------------------------------------
GET test_search_index/_search
{
  "size": 0,
  "aggs": {
    "salary_hist": {
      "histogram": {
        "field": "salary",
        "interval": 5000,
        "order": {
          "age>avg_age": "desc"
        }
      },
      "aggs": {
        "age": {
          "filter": {
            "range": {
              "age": {
                "gte": 10
              }
            }
          },
          "aggs": {
            "avg_age": {
              "avg": {
                "field": "age"
              }
            }
          }
        }
      }
    }
  }
}
```

### Terms桶查询中的精准度问题
> 出现问题的原因是因为数据是分散在多个Shard上，Coordinating Node无法得悉数据的全貌
![term精准度问题](https://github.com/spontaneously5201314/ELK/blob/master/src/doc/img/aggregation/01_terms_problem.png)

### 精准度问题的解决方案
> 设置Shard数为1，从根本上消除数据分散的问题，但无法承载大数据量
> 合理设置_search中Shard_Size的大小，即每次从Shard上额外多获取数据，以提升精度
```
GET test_search_index/_search
{
  "size": 0,
  "aggs": {
    "jobs": {
      "terms": {
        "field": "job.keyword",
        "size": 1,
        "shard_size": 10
      }
    }
  }
}
```
> 如何确定Shard_Size的大小？
```
terms聚合返回结果中有如下两个统计值：
- doc_count_error_upper_bound：被遗漏的term可能的最大值
- sum_other_doc_count：返回结果bucket的term外其他term的文档总数
设定show_term_doc_count_error可以查看每个bucket误算的最大值
----------------------------------------------------------------
GET test_search_index/_search
{
  "size": 0,
  "aggs": {
    "jobs": {
      "terms": {
        "field": "job.keyword",
        "size": 2,
        "show_term_doc_count_error": true
      }
    }
  }
}
----------------------------------------------------------------
Shard_Size默认大小是：shard_size=(size*1.5)+10
通过调整Shard_Size的大小降低doc_count_error_upper_bound来提升准确度，增大了整体的计算量，从而降低了响应时间
```

### 近似统计算法需要权衡的三个问题
> 在ES的聚合分析中，Cardinality和Percentile分析使用的是近似统计计算
- 结果是近似准确的，但不一定精准
- 可以通过参数的调整使其结果精准，但同时也以为着更多的计算时间和更大的性能消耗
![近似统计算法](https://github.com/spontaneously5201314/ELK/blob/master/src/doc/img/aggregation/02_approximate_statistical_algorithm.png)
