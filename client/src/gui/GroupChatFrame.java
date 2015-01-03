package com.sdl.MinetClient.gui;

import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.util.Random;
import com.sdl.MinetClient.network.*;

public class GroupChatFrame extends JFrame implements ActionListener{

    // public properties
    public String name;
    public int listeningPort;
    public List<PeerHost> peers;
    public ServerHost sh;
    public PeerListener pl;

    // GUI component
    public TextField tfEdit = new TextField(55);
    public JButton btSend = new JButton("send");
    public TextArea taChat = new TextArea(30, 50);
    public java.awt.List list1 = new java.awt.List(27);
    public JMenuItem quitMenuItem;
    public JMenuItem mailMenuItem;

    public GroupChatFrame(Socket s, int listeningPort_, String name_) {
        // SET UP INTERNET CONNECTION
        peers = new ArrayList<PeerHost>();
        this.name = name_;
        this.listeningPort = listeningPort_;
        // start to listen to server
        sh = new ServerHost(this, s, this.name, this.listeningPort);
        sh.start();

        // start a thread that accepts other peers's connection;
        pl = new PeerListener(this, this.listeningPort);
        pl.start();

        // SET UP GUI
        this.setLayout(new BorderLayout());

        JPanel panelM = new JPanel();
        JPanel panelL = new JPanel();

        // set up CENTER
        panelM.add(taChat);
        panelM.add(list1);
        list1.addActionListener(this);

        // set up SOUTH
        Label lEdit = new Label("New Msg:");
        panelL.add(lEdit);
        panelL.add(tfEdit);
        panelL.add(btSend);
        btSend.addActionListener(this);
        // 按下Enter之后自动触发start键
        tfEdit.addKeyListener(new KeyAdapter(){
           public void keyPressed(KeyEvent ke){
            if(ke.getKeyChar() == ke.VK_ENTER){
                btSend.doClick();
            }
           }
          }
        ) ;

        this.add(panelM, BorderLayout.CENTER);
        this.add(panelL, BorderLayout.SOUTH);

        // set up menu item
        JMenu cMenu = new JMenu("Connection");
        mailMenuItem= new JMenuItem("Send Mail");
        quitMenuItem= new JMenuItem("Quit");
        mailMenuItem.addActionListener(this);
        quitMenuItem.addActionListener(this);
        cMenu.add(mailMenuItem);
        cMenu.add(quitMenuItem);
        JMenuBar mb = new JMenuBar();
        mb.add(cMenu);
        this.setJMenuBar(mb);

        // 注册窗口事件监听器
        this.addWindowListener(new MyWinAdapter());

        // set visible
        this.setVisible(true);
        this.pack();
        this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        // 默认获取焦点
        tfEdit.requestFocus();
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == quitMenuItem) {
                // stop listening thread
                this.sh.close();

                // stop accepting peers' connections
                this.pl.close();
                this.dispose();
                System.exit(0);
            } else if (e.getSource() == btSend) {
                // 发消息给服务器
                String dat = ServerProcessor.sendMsg(this.sh, tfEdit.getText());
                taChat.append( "Yourself(" +  dat + "): "+ tfEdit.getText() + "\n");
                tfEdit.setText("");
            } else if (e.getSource() == list1) {
                System.out.println("Clicked, start to connect peer!");
                // 连接上一个peer host
                if (this.list1.getSelectedItem() == this.name+"-Online")
                    return;
                for (int i =0; i < this.peers.size(); i++) {
                    System.out.println(this.peers.get(i).hostName);
                    if ((this.peers.get(i).hostName+"-Online").equals(this.list1.getSelectedItem())) {
                        // 新建一个相同的PeerHost处理业务
                        PeerHost newPh = new PeerHost();
                        newPh.hostName = peers.get(i).hostName;
                        newPh.selfName = peers.get(i).selfName;
                        newPh.ip = peers.get(i).ip;
                        newPh.chatText = peers.get(i).chatText;
                        newPh.listeningPort = peers.get(i).listeningPort;
                        newPh.status = peers.get(i).status;
                        newPh.isRun = true;
                        newPh.isHello = false;

                        peers.remove(i);
                        peers.add(newPh);

                        newPh.connectPeer();
                        newPh.start();
                    }
                }
            } else if (e.getSource() == mailMenuItem) {
                new MailFrame(this, MailFrame.EDIT_MODE, null);
            }
            this.pack();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void printSystemMsg(String msg) {
            // 居中打印提示信息
            int c = this.taChat.getColumns();

            if (c > msg.length()) {
                char[] sp = new char[(c - msg.length())/2];
                for (int i =0; i < sp.length; i++) {
                    sp[i] = ' ';
                }
                msg = "\n" + new String(sp) + msg + "\n\n";
            }

            this.taChat.append(msg);
            this.pack();
    }

    class PeerListener extends Thread {
        boolean isRun = true;
        int port;
        GroupChatFrame mf;

        public PeerListener(GroupChatFrame mf_, int port_) {
            mf = mf_;
            port = port_;
        }
        public void close() {
            this.isRun = false;
        }
        public void run() {
            try {
                ServerSocket ss = new ServerSocket(port);
                ss.setSoTimeout(1000);
                while(isRun) {
                    Socket newSocket;
                    try {
                        newSocket = ss.accept();
                    } catch (java.net.SocketTimeoutException e) {
                        continue;
                    }
                    System.out.println(newSocket.getInetAddress() + " peer connections");
                    // 尝试获取信息
                    String[] contents =  PeerProcessor.recvAndProcsMsg(newSocket);
                    if (contents[0].equals("MINET")) {
                        for (int i = 0; i < peers.size(); i++) {
                            if (peers.get(i).hostName.equals(contents[1])) {
                                PeerHost newPh = new PeerHost();
                                newPh.setSocket(newSocket);

                                newPh.hostName = peers.get(i).hostName;
                                newPh.selfName = peers.get(i).selfName;
                                newPh.ip = peers.get(i).ip;
                                newPh.chatText = peers.get(i).chatText;
                                newPh.listeningPort = peers.get(i).listeningPort;
                                newPh.status = peers.get(i).status;
                                newPh.isRun = true;
                                newPh.isHello = true;

                                PeerProcessor.hello(newPh);

                                peers.remove(i);
                                peers.add(newPh);
                                newPh.start();
                                break;
                            }
                        }
                    } else {
                        newSocket.close();
                    }
                }
                ss.close();
            }  catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    class MyWinAdapter extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            // stop listening thread
            sh.close();

            // stop accepting peers' connections
            pl.close();
            System.exit(0);
        }
    }
}
