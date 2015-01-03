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

public class PrivateChatFrame extends JFrame implements ActionListener{
    public static final int PASSIVE = 0;
    public static final int INITIATIVE = 1;

    // public properties
    public PeerHost ph;

    // GUI component
    public TextField tfEdit = new TextField(45);
    public JButton btSend = new JButton("send");
    public JButton btSendFile = new JButton("File");
    public TextArea taChat = new TextArea(30, 65);
    public JMenuItem quitMenuItem;

    public PrivateChatFrame(Socket s_, String selfName, int mode_) {
        this.setUpGUI();

        // 利用socket进行hello
         if (mode_ == PASSIVE) {
            // 查看对方的hello信息
            String[] contents = null;
            try {
                contents =  PeerProcessor.recvAndProcsMsg(s_);
            } catch (java.io.IOException ex) {

            }
            if (contents[0].equals("MINET")) {
                // 新建一个PeerHost, 并回应对方的hello信息
                ph = new PeerHost(this, selfName, contents[1], s_.getInetAddress().getHostAddress(), s_.getPort());
                ph.setSocket(s_);
                PeerProcessor.hello(ph);
            }
         } else if(mode_ == INITIATIVE){
            // 首先尝试发送hello信息
            PeerProcessor.hello(s_, selfName);
            // 分析返回的信息
            String[] contents = null;
            try {
                contents =  PeerProcessor.recvAndProcsMsg(s_);
            } catch (java.io.IOException ex) {

            }
            if (contents[0].equals("MINET")) {
                // 新建一个PeerHost, 并回应对方的hello信息
                ph = new PeerHost(this, selfName, contents[1], s_.getInetAddress().getHostAddress(), s_.getPort());
                ph.setSocket(s_);
            }
         }

         ph.start();
    }

    public PrivateChatFrame(PeerHost ph_) {
        this.setUpGUI();
        // 调用peer host的setFrame(this)
        ph = ph_;
        ph.setFrame(this);

        //  isHello 决定是否hello
        while (!ph.isHello) {
            String[] contents;
            // 首先尝试发送hello信息
            PeerProcessor.hello(ph);
            // 分析返回的信息
            try {
                contents =  PeerProcessor.recvAndProcsMsg(ph);
            } catch (java.io.IOException ex) {
                // DO NOTHING
                continue;
            }
            if (contents[0].equals("MINET")) {
                ph.isHello = true;
            }            
        }
    }
    private void setUpGUI() {

        // SET UP GUI
        this.setLayout(new BorderLayout());

        JPanel panelM = new JPanel();
        JPanel panelL = new JPanel();

        // set up CENTER
        panelM.add(taChat);

        // set up SOUTH
        Label lEdit = new Label("New Msg:");
        panelL.add(lEdit);
        panelL.add(tfEdit);
        panelL.add(btSend);
        panelL.add(btSendFile);
        btSendFile.addActionListener(new OpenHandler());
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
        quitMenuItem= new JMenuItem("Quit");
        quitMenuItem.addActionListener(this);
        JMenuBar mb = new JMenuBar();
        cMenu.add(quitMenuItem);
        mb.add(cMenu);
        this.setJMenuBar(mb);

        // 注册窗口事件监听器
        this.addWindowListener(new MyWinAdapter());

        // set visible
        this.setVisible(true);
        this.pack();
        this.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

        // 默认获取焦点
        tfEdit.requestFocus();
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == quitMenuItem) {
                // stop listening thread
                this.ph.close();

                this.dispose();
            } else if (e.getSource() == btSend) {
                // 发消息给服务器
                String dat = PeerProcessor.sendMsg(this.ph, tfEdit.getText());
                taChat.append("Yourself(" +  dat + "): "+ tfEdit.getText() + "\n");
                tfEdit.setText("");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.pack();
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

    class OpenHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser jc = new JFileChooser();
            int rVal = jc.showOpenDialog(PrivateChatFrame.this);  //显示打开文件的对话框
            if(rVal == JFileChooser.APPROVE_OPTION) {
                String path = jc.getCurrentDirectory().toString() +  System.getProperty("file.separator")  + jc.getSelectedFile().getName();
                printSystemMsg("你尝试发送文件: " + jc.getSelectedFile().getName());
                PeerProcessor.sendFile(PrivateChatFrame.this.ph, path);
            }
        }
    }

    class MyWinAdapter extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
                // stop listening thread
                ph.close();
        }
    }
}
