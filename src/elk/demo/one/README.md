### 用来分析ElasticSearch查询语句的案例
#### 方案：
> １．应用Packetbeat+Logstash完成数据收集工作   
> ２．使用Kibana+Elasticsearch完成数据分析工作

#### 实现：
    Production Cluster:
        ElasticSearch http://127.0.0.1:9200
        Kibana http://127.0.0.1:5601
    Monitoring Cluster:
        ElasticSearch http://127.0.0.1:8200
            bin/elasticsearch -Ecluster.name=sniff_search -Ehttp.port=8200 -Epath.data=sniff
        Kibana http://127.0.0.1:8601
            bin/kibana -e http://127.0.0.1:8200 -p 8601
    Production和Monitoring不能是一个集群，否则会进入抓包死循环
    
#### 步骤（其中启动的命令的目录执行修改）：
    １．首先将Monitoring的ElasticSearch启动起来   
        /opt/elk/elasticsearch/elasticsearch-6.3.2/bin/elasticsearch -Ecluster.name=sniff_search -Ehttp.port=8200 -Epath.data=sniff_search -d
    ２．启动kibana
        /opt/elk/kibana/kibana-6.3.2-linux-x86_64/bin/kibana -e http://127.0.0.1:8200 -p 8601
    ３．启动logstash，使用logstash.conf
        /opt/elk/logstash/logstash-6.3.2/bin/logstash -f /opt/idea/workspace/elk/src/elk/demo/one/logstash.conf
    ４．启动Packetbeat
        sudo /opt/elk/beats/packetbeat/packetbeat-6.3.2-linux-x86_64/packetbeat -e -c /opt/idea/workspace/elk/src/elk/demo/one/packetbeat.yml -strict.perms=false