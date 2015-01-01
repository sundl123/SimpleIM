package com.sdl.MinetClient.network;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.*;
import com.sdl.MinetClient.gui.GroupChatFrame;
import com.sdl.MinetClient.gui.MailFrame;

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

        mf.printSystemMsg("你加入了聊天室");

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
                // list format:["LIST", date, length, u1, i1, p1, status, u2, i2, p2, status2]

                PeerHost ph;
                for (int i = 3; i < contents.length; i += 4) {
                    // contents[i]: user name, contents[i+1]: ip addr, contents[i+2]: listenning port
                    if (contents[i].equals(selfName)) {
                        mf.list1.add("Yourself-Online");
                        continue;
                    }
                    if (contents[i+3].equals("0")) {
                        mf.list1.add(contents[i]+"-Offline");
                    } else {
                        mf.list1.add(contents[i]+"-Online");
                    }

                    ph = new PeerHost(null, selfName, contents[i], contents[i+1], Integer.parseInt(contents[i+2]));
                    mf.peers.add(ph);
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

            // 检测流末尾
            if (contents == null) {
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
                    // 先查找该用户是否有记录
                    int idx;
                    for (idx = 0; idx < mf.peers.size(); idx++) {
                        if (mf.peers.get(idx).hostName.equals(contents[2]))
                            break;
                    }

                    mf.printSystemMsg(contents[2]+ "加入了聊天室");

                    if (idx != mf.peers.size()) {
                        mf.peers.get(idx).status = true;
                        mf.peers.get(idx).selfName = selfName;
                        mf.peers.get(idx).ip = contents[contents.length-2];
                        mf.peers.get(idx).listeningPort = Integer.parseInt(contents[contents.length-1]);
                        mf.list1.remove(mf.peers.get(idx).hostName+ "-Offline");
                        mf.list1.add(mf.peers.get(idx).hostName+ "-Online");
                        continue;
                    }
                    // 如果没有记录则新建一条记录
                    PeerHost ph = new PeerHost(null, selfName, contents[2], contents[contents.length-2], Integer.parseInt(contents[contents.length-1]));
                    mf.peers.add(ph);
                    mf.list1.add(contents[2] + "-Online");
                } else if (contents[1].equals("0")) {
                    // user offline
                    int idx;
                    for (idx = 0; idx < mf.peers.size(); idx++) {
                        if (mf.peers.get(idx).hostName.equals(contents[2]))
                            break;
                    }
                    mf.printSystemMsg(contents[2]+ "离开了聊天室");
                    if (idx != mf.peers.size()) {
                        //修改状态和标签
                        mf.peers.get(idx).status = false;
                        mf.list1.remove(mf.peers.get(idx).hostName + "-Online");
                        mf.list1.add(mf.peers.get(idx).hostName + "-Offline");
                        continue;
                    }
                }
            } else if (contents[0].equals("CSMESSAGE")) {
                // csmessage format: ["CSMESSAGE", user name, date, length, data]
                String data = contents[1] + "(" + contents[2] + "): " + contents[contents.length -1];
                mf.taChat.append(data);
            } else if (contents[0].equals("EMAIL")) {
                Object[] options = {"查看", "忽略"};
                int sel = JOptionPane.showOptionDialog(null, "收到" + contents[1] + "的邮件" + contents[4], "新邮件", 
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

                if (sel == 0) {
                    new MailFrame(this.mf, MailFrame.READ_MODE, contents);
                }
            }
            mf.pack();
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