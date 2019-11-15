# HDFS的数据流

HDFS的数据流包括上传和下载的数据流

## HDFS的数据上传

1. 客户端创建一个`Distributed File System` 对象，这个对象向配置中的`NameNode`发起请求上传的请求
2. `NameNode`处理请求，检查权限，是否可以上传等，如果可以上传回复一个消息。
3. 客户端根据配置文件按块上传，读取一个128M的数据开始上传一个`Block`,请求返回配置中的副本数量的`Datanode`。
4. `NameNode`根据请求信息根据空间和距离等信息，返回三个`DataNode`, DN1,DN2,DN3.
5. 客户端创建一个`FSDataOutputStream`将数据写出。
6. 在数据传输时采取pipeLine的方式传播，在三个DN中 ，客户端只与DN1进行连接，与DN1连接后，DN1再与DN2连接，DN2再与DN3连接。应答成功的信号也是从DN3-->DN2-->DN1-->client.(发生错误的处理：)
7. 再收到DN1的应答成功信号以后，`FSDataOutputStream`就将数据写出到DN1，以Packet的方式进行传输。

