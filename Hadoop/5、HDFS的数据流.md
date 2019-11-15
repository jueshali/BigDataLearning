# HDFS的数据流

HDFS的数据流包括上传和下载的数据流

## HDFS的数据上传（写流程）

1. 客户端创建一个`Distributed File System` 对象，这个对象向配置中的`NameNode`发起请求上传的请求
2. `NameNode`处理请求，检查权限，是否可以上传等，如果可以上传回复一个消息。
3. 客户端根据配置文件按块上传，读取一个128M的数据开始上传一个`Block`,请求返回配置中的副本数量的`Datanode`。
4. `NameNode`根据请求信息根据空间和距离等信息，返回三个按照某特定算法选择的`DataNode`, DN1,DN2,DN3.
5. 客户端创建一个`FSDataOutputStream`将数据写出。
6. 在数据传输时采取`pipeLine`的方式传播，在三个DN中 ，客户端只与DN1进行连接，与DN1连接后，DN1再与DN2连接，DN2再与DN3连接。应答成功的信号也是从DN3-->DN2-->DN1-->client.
7. 在收到DN1的应答成功信号以后，`FSDataOutputStream`就将数据写出到DN1，以Packet的方式进行传输。
8. 一次传输一个块的数据，在传输时一个块由一个一个的64k的packet传输，packet由512 + 4 B的chunk组成。传输时有两个队列，`dataQueue` 和`ackQueue`，dataQueue中为封装好的待发送的packet,ackQueue中为正在发送的packet。发送时，输出流将首先从dataQueue中选一个packet，把他传送到第一个节点，并且将dataQueue中得数据移动到ackQueue中。如果传送成功，客户端将会接收到一个datanodes得ack消息，当收到所有datanode得ack消息后，客户端将会把这个包从dataQueue中移除。（一旦发生异常，会移动走ackQueue中的packet,并建立一个新的通道进行传输，这个通道在建立时会忽略那个坏的node。重新建立连接时要是第一个到第二个节点依旧损害，那忽略第二个节点，客户端只在第一个和第三个节点间建立连接，也就是说数据只会存储两份。）
9. 当datanode收到一个块大小的数据后会向namenode上报信息。
10. 接下来的块重复到第四步。
11. 当所有数据发送完后，发送一个数据传输完成得消息。

![hdfs写数据流程](upload.jpg)

## HDFS的数据下载（读流程）

1. 客户端建立一个Distributed FileSystem对象向NN请求下载，NN中存储有元数据，NN查看权限自己有没有等信息后回复
2. NN返回目标文件的元数据
3. 客户端就建立一个InputStream去读取，从元数据指示的的DataNode中去请求读取。
4. 传输数据写入目标文件之中。

![hdfs读数据流程](download.png)
