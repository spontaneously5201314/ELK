# ELK
Demo For Spring Boot And ELK Stack

### Logstash需要注意的点  
>Pipeline
```
- input-filter-output的3阶段处理流程
- 队列管理
- 插件生命周期管理
```         
>Logstash Event
```
- 内部流转的数据表现形式
- 原始数据在input被转换成Event，在output event被转换为目标格式数据
- 在配置文件中可以对Event中的属性进行增删改查
```

### Logstash优化
> Logstash默认使用memory queue，可以使用persisted queue替换，效率上会慢5%左右，但是容灾，配置参数如下。     
参考文档：https://www.elastic.co/guide/en/logstash/current/persistent-queues.html
```
queue.type:persisted
    - 默认是memory
queue.max_bytes:4gb
    - 队列存储最大数据量
```

### Logstash线程调优(| 后面为Logstash命令行参数)
> pipeline.workers | -w
```
pipeline线程数，即filter_output的处理线程数，默认是cpu核数
```
> pipeline.batch.size | -b
```
Batcher一次批量获取的待处理文档数，默认125，可以根据输出进行调整，越大会占用越多的heap空间，可以通过jvm.options调整
```
> pipeline.batch.delay | -u 
```
Batcher等待的时长，单位为ms
```

----------------------------------  


### Logstash File Input插件需要注意的几个点
> 文件内容如何只被读取一次？即重启Logstash时，从上次读取到位置继续
```
sincedb
```
> 如何及时读取到文件的新内容？
```
定时检查文件是否有更新
```
> 如何发现性文件并进行读取？
```
可以，定时检查新文件
```
> 如果文件发生了归档操作，是否影响当前的内容的读取？
```
不影响，被归档的文件内容可以继续被读取
```

### glob匹配语法
   字符   |含义
   ----- | -----
   *     |      匹配任意字符，但不匹配以.开头的隐藏文件，匹配这类文件时使用.*来匹配           
   **    |      递归匹配子目录     
   ?     |      匹配单一字符       
   []    |      匹配多个字符，比如[a-z]、[^a-z]       
   {}    |      匹配多个单词，比如{foo,bar,hello}        
   \     |      转义符号        



### File Input的一个坑
> 如果是使用了start_position为begining，那么只有在文件第一次读取的时候，才会从头开始读取，如果是已经读取过，那么就算重启Logstash，也会从该文件记录在sincedb中的已读取位置接着往下读取，
这种不方便调试，在调试的过程中可以配置上sincedb_path来解决这个问题，如下配置：
```
input {
    file {
        path => "/var/log/*.log"
        sincedb_path => "/dev/null"  #其中这个配置就是为了让Logstash不去记录文件的读取位置
        start_position => "begining"
        ignore_older => 0
        close_older => 5
        discover_interval => 1
    }
}
output {
    stdout{
        codec => rubydebug
    }
}
```

### Kafka Input的配置案例
```
input {
    kafka {
        zk_connect => "kafka:2181"
        group_id => "logstash"
        topic_id => "apache_logs"
        consumer_threads => 16
    }
}
```

### Codec Input/Output
> 常见的codec有：
- plain     读取原始内容
- dots      将内容简化为点进行输出
- rubydebug 将Logstash Event按照ruby格式输出，方便调试
- line      处理带有换行符的内容
- json      处理json格式的内容
- multiline 处理多行数据的内容，常用于堆栈日志信息的处理，比如java的Exception的栈信息等等

### 利用multiline收集java的异常堆栈信息
```
input {
    stdin {
        codec => multiline {
            pattern => "^\s" # 表示以空格开头
            what => "previous"
        }
    }
}
```


### Filter Plugin
> 常见的有：
- date 日期解析
- grok 正则匹配解析
- dissect 分隔符解析
- mutate 对字段做处理，比如重命名，删除，替换等
- json 按照json解析字段内容到指定字段中
- geoip 增加地址位置数据
- ruby 利用ruby代码来动态修改Logstash Event


### grok正则表达式查询地址
> https://github.com/logstash-plugins/logstash-patterns-core/blob/master/patterns/grok-patterns     
其他的patterns地址：https://github.com/logstash-plugins/logstash-patterns-core/tree/master/patterns


### grok语法
> 
- %{SYNTAX:SEMANTIC}
- SYNTAX为grok pattern的名称，SEMANTIC为赋值字段的名称
- %{NUMBER:duration} 可以匹配数值类型，但是grok匹配出的内容都是字符串类型，可以通过在最后指定为int或者float来强制转换类型，比如%{NUMBER:duration:float}

### grok调试建议
> 正则表达式
- https://www.debuggex.com/     
- https://regexr.com/
> grok调试
- http://grokdebug.herokuapp.com
- http://grok.elasticsearch.cn
- x-pack grok debugger

### grok的缺点
> 因为grok采用正则表达式来解析，所以解析的时候回消耗过多的cpu资源，所以出现了dissect这种根据分隔符解析数据的插件，更省资源

### dissect语法
> 由一系列字段（field）和分隔符（delimiter）组成
- %{} 字段
- %{} 之间是分隔符
- dissect分割后的字段都是字符串，可以使用convert_datatype属性进行类型转换
```
filter {
    dissect {
        convert_datatype => {
            field => "int"
        }
    }
}
```

### mutate filter plugin
> 主要操作如下：
- convert 类型转换
- gsub    字符串替换
- split/join/merge 字符串切割、数组合并为字符串、数组合并成数组
- rename 字段重命名
- update/replace 字段内容更新或替换
- remove_field 删除字段

### Logstash热加载
> 因为Logstash启动非常慢，不可能每次都重新启动，所以使用-r 命令参数可以热加载配置

### output
> 常见三种output        
- stdout
```
output {
    stdout {
        codec => rubydebug
    }
}
```
- file
```
实际需求：将分散在多地的日志文件收集到一起，方便查看
output {
    file {
        path => "path.to.store.log"
        codec => line {
            format => "%{message}"  #默认输出json格式的数据，通过format可以输出原始格式
        }
    }
}
```
- elasticsearch
```
output {
    elasticsearch {
        hosts => ["127.0.0.1:9200", "127.0.0.2:9200"]  #不要写master node，写salve node
        index => "nginx-%{+YYYY.MM.dd}"
        template => "./nginx_template.json"
        template_name => "nginx_template"
        template_overwrite => true
    }
}
```

### Logstash调试的配置建议
- http做input，方便输入测试数据，并且可以结合reload特性(stdin无法reload)
- stdout做output，codec使用rubydebug，即时查看解析结果
- 测试错误输入情况下的输出，以便对错误情况进行处理
- 配置如下：
```
input {
    http {
        port => 7474
    }
}
filter {
}
output {
    stdout {
        codec => rubydebug
    }
}
```
- @metadata特殊字段，其内容不会输出在output中
- 适合用来存储做条件判断，临时存储的字段，相比remove_field有一定的性能提升
- 配置如下：
```
input {
    stdin {}
}
filter {
    mutate {
        add_field => {
            "[@metadata][debug]" => true #作为调试开关
        }
    }
    mutate {
        add_field => {
            "show" => "This data will be in the output"
        }
    }
    mutate {
        remove_field => "headers"
    }
}
output {
    if [@metadata][debug] {
        stdout {
            codec => rubydebug
        }
    }else {
        stdout {
            codec => json
        }
    }
}
```

### Logstash监控运维API
```
http://localhost:9600
http://localhost:9600/_node
http://localhost:9600/_node/stats
http://localhost:9600/_node/hot_threads
```

### Logstash 结合X-Pack
```
安装X-Pack
bin/logstash install x-pack
在logstash.yml配置文件后面追加：
xpack.monitoring.elasticsearch.url:["http://localhost:9200"]
地址：https://www.elastic.co/guide/en/logstash/current/installing-xpack-log.html
```

### Filebeat
```
- 读取日志文件，但不做数据的解析处理
- 保证数据“At Least Once”至少被读取一次，即数据不会丢
- 处理多行数据
- 解析json格式数据
- 简单的过滤功能
```