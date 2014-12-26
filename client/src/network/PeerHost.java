package com.sdl.MinetClient.network;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import com.sdl.MinetClient.gui.PrivateChatFrame;

public class PeerHost extends Thread{
    public Socket socket;

    public String hostName; // 对方服务器的名字
    public String selfName; // 你自己的名字
    public String ip; //对方服务器的IP
    public String chatText = ""; // 保存服务器传过来的聊天信息
    public int listeningPort; // 对方的监听端口
    public PrivateChatFrame mf;

    public BufferedReader reader;
    public PrintStream ps;

    public boolean isRun = true;
    public boolean isHello = false;
    public boolean status = false;

    public PeerHost(PrivateChatFrame mf_, String selfName_, String hostName_, String ip_, int port_){
        mf = mf_;
        selfName = selfName_;
        hostName = hostName_;
        ip = ip_;
        listeningPort = port_;
    }
    public PeerHost() {}

    public void setFrame(PrivateChatFrame mf_) {
        this.mf = mf_;
    }

    public void setSocket(Socket s) {
        socket = s;
        isHello = true;

        // 设置超时
        try {
            s.setSoTimeout(1000);
        } catch (Exception e) {
            // DO NOTHING
        }

        // 获取输入输出流
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ps = new PrintStream(socket.getOutputStream());
        }  catch (Exception ex) {
                    ex.printStackTrace();
        }        
    }

    public void connectPeer() {
        try {
            // 连接得到socket
            System.out.println("I am trying to connect to " + listeningPort);
            socket = new Socket(InetAddress.getLocalHost(), listeningPort);
            socket.setSoTimeout(1000);
            // 获取输入输出流
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ps = new PrintStream(socket.getOutputStream());
        }  catch (Exception ex) {
                    ex.printStackTrace();
        }

        // 不停地尝试发送hello信息直到成功
        String[] contents;
        while (!isHello) {
            PeerProcessor.hello(this);
            try {
                contents = PeerProcessor.recvAndProcsMsg(this);
            } catch (java.io.IOException e) {
                // Do nothing
                continue;
            }
            if (contents[0].equals("MINET")) {
                isHello = true;
            }
        }
    }
    public void close() {
        // notify server
        PeerProcessor.leave(this);

        // stop the thread
        this.isRun = false;

        // close inputstream, outstream, socket
        try {
                this.reader.close();
                this.ps.close();
                this.socket.close();
        } catch (Exception ex) {
                    ex.printStackTrace();
                }
    }

    public void run() {
        if (mf == null) {
            new PrivateChatFrame(this);
        }
        // 不停地监听信息
        String[] contents;
        while (isRun) {
            try {
                contents = PeerProcessor.recvAndProcsMsg(this);
            } catch (java.io.IOException e) {
                // Do nothing
                continue;
            }

            System.out.println("Processing new arrays");
            for (int i = 0; i < contents.length; i++) {
                            System.out.println(contents[i]);
            }
            if (contents[0].equals("P2PMESSAGE")) {
                // p2p msg format:["P2PMESSAGE", name, date, length, ..., data]
                String newMsg = hostName + "(" + contents[2] + "): " + contents[contents.length -1] ;
                mf.taChat.append(newMsg);
            } else if (contents[0].equals("LEAVE")) {
                try{
                    this.reader.close();
                    this.ps.close();
                    this.socket.close();
                    this.isRun = false;
                } catch (Exception ex) {
                    // Do nothing
                }
                this.mf.dispose();
            } else if (contents[0].equals("BEAT")) {
                // TO DO
            }
        }
    }
}
