1.文件传输
2.邮件服务器
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

今天晚上的作业：
3.实现拓展功能;
    2.离线邮件
    3...端口探测
    2.界面优化
4.

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



