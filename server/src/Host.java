package com.sdl.MiroServer;

import java.util.List;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class Host extends Thread {
    Socket socket = null;
    String name;
    String ip;
    int listeningPort;
    MiroServer ms;
    LinkedList<Mail> mailBox = new LinkedList<Mail>();

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

    public Host() {}

    public void run() {
        String[] contents;
        while(isRun) {
            try {
                contents = ClientProcessor.recvAndProcsMsg(this);
            } catch (java.io.IOException ex) {
                // do nothing
                System.out.println("Client Time out");
                continue;
            }
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
                if ((isHello)&&(!isLogin)) {
                    // 检查用户名是否重复
                    boolean isSame = false;
                    for (int i = 0; i < ms.hosts.size(); i++) {
                        if (ms.hosts.get(i).name.equals(contents[1])) {
                            if (ms.hosts.get(i).socket == null) {
                                // 唤醒offline的监听器
                                Host nh = new Host();

                                nh.socket = this.socket;
                                nh.ps =  this.ps;
                                nh.reader =  this.reader;
                                this.socket = null;
                                this.reader = null;
                                this.ps = null;

                                nh.name = ms.hosts.get(i).name;
                                nh.ip = nh.socket.getInetAddress().getHostAddress();
                                nh.listeningPort = Integer.parseInt(contents[2]);
                                nh.mailBox = ms.hosts.get(i).mailBox;
                                nh.ms = ms.hosts.get(i).ms;
                                nh.isHello = true;
                                nh.isLogin = true;
                                nh.isRun = true;

                                ms.hosts.remove(i);
                                ms.hosts.add(nh);

                                // 通知该客户端登录成功
                                ClientProcessor.sendStatus(nh, 1);
                                // 通知其他客户端新用户上线。
                                ClientProcessor.sendUpdate(nh, ms.hosts, 1);

                                nh.start();
                                return;
                            }
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
                // 通知该客户端登录成功
                ClientProcessor.sendStatus(this, 1);
            } else if (contents[0].equals("GETLIST")) {
                if (isLogin) {
                    // 发送在线用户列表
                    ClientProcessor.sendList(this, ms.hosts);
                    ClientProcessor.clearMailBox(this);
                }
            } else if (contents[0].equals("LEAVE")) {
                if(isLogin) {
                    try {
                        this.ps.close();
                        this.reader.close();
                        this.socket.close();
                        this.ps = null;
                        this.reader = null;
                        this.socket = null;
                    } catch (Exception e) {
                        //DO NOTHING
                    }
                    // 想其他所有用户发送update信息
                    ClientProcessor.sendUpdate(this, ms.hosts, 0);
                    // 结束这个监听线程
                    this.isHello = false;
                    this.isLogin = false;
                    this.isRun = false;
                }
            } else if (contents[0].equals("MESSAGE")) {
                if(isLogin) {
                    // 将信息发送给其他客户端
                    ClientProcessor.sendMsg(ms.hosts, name, contents[contents.length-1]);
                }
            } else if (contents[0].equals("BEAT")) {
                // TO DO
            } else if (contents[0].equals("EMAIL")) {
                // put it in receiver's mail box and try to clear it
               // mail format from server to client : ["EMAIL", sender, date, length, receriver, title, data..]
                for (int i = 0; i < ms.hosts.size(); i++) {
                    if (ms.hosts.get(i).name.equals(contents[4])) {
                        ms.hosts.get(i).mailBox.add(new Mail(contents[1], contents[2], contents[5], contents[contents.length-1]));
                        ClientProcessor.clearMailBox(ms.hosts.get(i));
                        break;
                    }
                }                
            }
        }
    }
}