package com.sdl.MinetClient.network;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import com.sdl.MinetClient.gui.GroupChatFrame;

public class ServerHost extends Thread{
    public Socket socket;

    public String hostName; // 对方服务器的名字
    public String selfName; // 你自己的名字
    public String ip; //对方服务器的IP
    public String chatText = ""; // 保存服务器传过来的聊天信息
    public int listeningPort; // 你自己的监听端口
    public GroupChatFrame mf;

    public BufferedReader reader;
    public PrintStream ps;

    public boolean isRun = true;
    public boolean isHello = false;
    public boolean isLogin = false;

    public ServerHost(GroupChatFrame mf_, Socket s, String selfName_, int listeningPort_) {
        mf = mf_;
        socket = s;
        selfName = selfName_;
        listeningPort = listeningPort_;

        // set time out for socket
        try {
            socket.setSoTimeout(1000);
        } catch (Exception e) {
            // Do nothing
        }

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ps = new PrintStream(socket.getOutputStream());
        }  catch (Exception ex) {
                    ex.printStackTrace();
                }
    }
    public void run() {
        String[] contents;
        // 不停地尝试hello
        while(!isHello) {
            ServerProcessor.hello(this);
            try {
                contents = ServerProcessor.recvAndProcsMsg(this);
            } catch (java.io.IOException ex) {
                // do nothing
                continue;
            }
            if (contents[0].equals("MIRO")) {
                isHello = true;
                hostName = contents[1];
            }
        }

        // 不停地尝试login
        while(!isLogin) {
            ServerProcessor.logIn(this);
            try {
                contents = ServerProcessor.recvAndProcsMsg(this);
            } catch (java.io.IOException ex) {
                // do nothing
                continue;
            }
            if ((contents[0].equals("STATUS")) && (contents[1].equals("1"))) {
                isLogin = true;
            }
        }

        // 获取在线用户清单
        while(true) {
            ServerProcessor.getList(this);
            try {
                contents = ServerProcessor.recvAndProcsMsg(this);
            } catch (java.io.IOException ex) {
                // do nothing
                continue;
            }
            if (contents[0].equals("LIST")) {
                // list format:["LIST", date, length, u1, i1, p1, u2, i2, p2]

                PeerHost ph;
                for (int i = 3; i < contents.length; i += 3) {
                    // contents[i]: user name, contents[i+1]: ip addr, contents[i+2]: listenning port
                    if (contents[i].equals(selfName)) {
                        mf.list1.add(contents[i]);
                        continue;
                    }

                    ph = new PeerHost(null, selfName, contents[i], contents[i+1], Integer.parseInt(contents[i+2]));
                    mf.peers.add(ph);
                    mf.list1.add(contents[i]);
                }
                break;
            }
        }

        // 不停地监听服务器的信息
        while (this.isRun) {
            try {
                contents = ServerProcessor.recvAndProcsMsg(this);
            } catch (java.io.IOException e) {
                System.out.println("ServerHost timeout");
                continue;
            }

            System.out.println("Processing new arrays");
            for (int i = 0; i < contents.length; i++) {
                            System.out.println(contents[i]);
            }
            if (contents[0].equals("UPDATE")) {
                // update format ["UPDATE", status, username, date, length, ip, port]
                if (contents[1].equals("1")) {
                    // user online
                    PeerHost ph = new PeerHost(null, selfName, contents[2], contents[contents.length-2], Integer.parseInt(contents[contents.length-1]));
                    mf.peers.add(ph);
                    mf.list1.add(contents[2]);
                } else if (contents[1].equals("0")) {
                    // user offline
                    int idx;
                    for (idx = 0; idx < mf.peers.size(); idx++) {
                        if (mf.peers.get(idx).hostName.equals(contents[2]))
                            break;
                    }
                    // if not connected in p2p, then remove it from peers list and list1
                    if (mf.peers.get(idx).socket == null) {
                        mf.list1.remove(mf.peers.get(idx).hostName);
                        mf.peers.remove(idx);
                    }
                }
            } else if (contents[0].equals("CSMESSAGE")) {
                // csmessage format: ["CSMESSAGE", user name, date, length, data]
                String data = contents[1] + "(" + contents[2] + "): " + contents[contents.length -1];
                mf.taChat.append(data);
            }
        }
    }
    public void close() {
        // notify server
        ServerProcessor.leave(this);

        // stop the thread
        this.isRun = false;

        // close inputstream, outstream, socket
        try {
                reader.close();
                ps.close();
                socket.close();
        } catch (Exception ex) {
                    ex.printStackTrace();
                }
    }
}