
代码解释：
1.服务端
MiroServer.java：该类代表一个服务器，它负责不停地监听客户端的连接，并且将连接实例成Host对象。

Host.java：代表一个客户端，当服务器监听到一个客户端的连接之后，就会实例化一个Host对象，通过Host对象实现与客户端的交互管理。Host对象中几乎包含了所有客户端的相关信息。如果socket对象，IP地址，监听端口等。Host类型嗯继承了Thread类，所以每一个Host对象都运行在单独了线程中，不会相互影响，服务器可以继续等待新的链接，而无须等待。

Mail.java：该类是保存邮件内容的一个数据结构，服务器收到用户的邮件之后，会使用收到的邮件信息生成一个Mail类的实例对象，并保存在收信用户的邮箱中。其中保存的内容有发信人，时间戳，标题，内容。

ClientProcessor.java：用于实现MINET/MIRO交互协议的主要内容。
它是一个工具类，所有方法都是静态方法，每个方法都用来实现协议的一个方面。比如说
 public static void hello(Host h, String hostName)
，这个方法是用来实现服务端向客户端回复hello信息的。需要的参数是
Host h 代表了hello信息的接收方
String hostName 代表服务器自己的名字
其他的函数功能类似， sendStatus是用来发送Status信息的，sendList用来发送用户列表，sendUpdate用于发送Update信息...
以上这些函数都是用来发送信息的，在ClientProcessor中还有一个重要的方法：
 public static String[] recvAndProcsMsg(Host h) throws java.io.IOException
这个方法是用来接受并解析客户端的信息。该方法会等待客户端的信息，并将得到报文解析成可以识别的信息并且以字符串数组的方式返回。



2.客户端
分为gui和network两个部分
gui：负责实现软件的图形界面，用户交互的响应等。
LoginFrame.java:代表登录窗口
GroupChatFrame.java：代表群聊天窗口
PrivateChatFrame.java：代表私人聊天窗口
MailFrame.java：代表邮件的编辑和阅读窗口

network：负责实现软件的网络连接处理与响应。

PeerHost.java:代表一个P2P的连接，当该客户端与另外一个客户端成功建立链接之后，就会实例化一个PeerHost对象来处理与其他客户端的信息交互。PeerHost对象中几乎包含了其他客户端的所有信息，包括名称，IP地址,socket对象等。

ServerHost.java：代表与服务器的连接，当程序与服务器连接成功，是实例化一个ServerHost对象来处理与服务器的信息交互。ServerHost对象中几乎包含了服务器的所有信息，包括名称，IP地址,socket对象等。


PeerProcessor.java：用于实现与其他客户端的交互协议。功能类似于服务器中的ClientProcessor。
ServerProcessor.java：用于实现与服务端的交互协议。功能类似于服务器中的ClientProcessor。





待实现的功能：
    格式化信息
    图片显示/截图
    5.对讲机功能
    声音元素
    某人加入了房间
    某人离开了房间
    文件单独发送和群发
    聊天加密功能
    聊天记录保存
    @语法
    ip地址,端口扫描
    聊天头像

协议补充
文件传输功能
客户端发到客户端
CS1.0 sp P2PMESSAGE sp 用户名 cr lf
Date
Content-Length
Content-Type
FileName
FileLength
data...


客户端到服务端
CS1.0 sp MESSAGE sp 用户名 cr lf

Date
Content-Length
Content-Type
FileName
FileLength
userNum
name1
name2
name3

data...


服务端到客户端
CS1.0 sp CSMESSAGE sp 用户名 cr lf
Date
Content-Length
Content-Type
FileName
FileLength
usrName
data...
data...

CS1.0 sp EMAIL sp 用户名 cr lf
Date
Content-Length
receiver
Title:
data
data



