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
    public TextField tfEdit = new TextField(100);
    public Button btSend = new Button("send");
    public TextArea taChat = new TextArea(30, 50);
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
        btSend.addActionListener(this);

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

        // set visible
        this.setVisible(true);
        this.pack();
        this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
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
                taChat.append(ph.selfName + "(" +  dat + "): "+ tfEdit.getText() + "(p2p)\n");
                tfEdit.setText("");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
