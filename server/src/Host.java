package com.sdl.MiroServer;

import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class Host extends Thread {
    Socket socket;
    String name;
    String ip;
    int listeningPort;
    MiroServer ms;

    BufferedReader reader;
    PrintStream ps;

    boolean isRun = true;
    boolean isHello = false;
    boolean isLogin = false;
    public Host(Socket s, MiroServer ms_) {
        ms = ms_;
        socket = s;
        ip = socket.getInetAddress().getHostAddress();
        try {
            // get input and output stream
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ps = new PrintStream(socket.getOutputStream());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        String[] contents;
        while(isRun) {
            contents = ClientProcessor.recvAndProcsMsg(this);
            System.out.println("Processing new string arrays\n");
            for (int i =0;  i < contents.length; i++){
                System.out.println(contents[i]);
            }
            if (contents[0].equals("MINET")) {
                if (!isHello) {
                    System.out.println(contents[1] + "says hello!");
                    isHello = true;

                    // send back hello msg
                    ClientProcessor.hello(this, "MiroServer");
                }
            } else if (contents[0].equals("LOGIN")) {
                if (isHello) {
                    // 检查用户名是否重复
                    boolean isSame = false;
                    for (int i = 0; i < ms.hosts.size(); i++) {
                        if (ms.hosts.get(i).name.equals(contents[1])) {
                            isSame = true;
                            break;
                        }
                    }
                    if (isSame) {
                        // 通知该客户端登录失败
                        ClientProcessor.sendStatus(this, 0);
                    } else {
                        name = contents[1];
                        listeningPort = Integer.parseInt(contents[2]);

                        // 通知该客户端登录成功
                        ClientProcessor.sendStatus(this, 1);
                        // 通知其他客户端新用户上线。
                        ClientProcessor.sendUpdate(this, ms.hosts, 1);

                        // 在用户列表中添加该客户端
                        ms.hosts.add(this);
                        isLogin = true;
                    }
                }
            } else if (contents[0].equals("GETLIST")) {
                if (isLogin) {
                    // 发送在线用户列表
                    ClientProcessor.sendList(this, ms.hosts);
                }
            } else if (contents[0].equals("LEAVE")) {
                if(isLogin) {
                    // 从用户列表中删除它
                    ms.hosts.remove(this);

                    // 想其他所有用户发送update信息
                    ClientProcessor.sendUpdate(this, ms.hosts, 0);
                    // 结束这个监听线程
                    isRun = false;
                }
            } else if (contents[0].equals("MESSAGE")) {
                if(isLogin) {
                    // 将信息发送给其他客户端
                    ClientProcessor.sendMsg(ms.hosts, name, contents[contents.length-1]);
                }
            } else if (contents[0].equals("BEAT")) {
                // TO DO
            }
        }
    }
}