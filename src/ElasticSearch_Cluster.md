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