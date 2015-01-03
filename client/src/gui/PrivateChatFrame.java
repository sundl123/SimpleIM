package com.sdl.MinetClient.gui;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.URL;
import com.sdl.MinetClient.network.*;

public class PrivateChatFrame extends JFrame implements ActionListener{
    public static final int PASSIVE = 0;
    public static final int INITIATIVE = 1;

    // public properties
    public PeerHost ph;

    // GUI component
    public JTextField tfEdit = new JTextField(45);
    public JButton btSend = new JButton("send");
    public JButton btSendFile = new JButton("File");
    public JTextArea taChat = new JTextArea(30, 65);
    public JMenuItem quitMenuItem;
    public JMenuItem saveMenuItem;

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
        // 设置背景图片
        URL url = getClass().getResource("/resource/PrivateChatBG.jpg");
        ImageIcon img = new ImageIcon(url);
        JLabel imgLabel = new JLabel(img);
        this.getLayeredPane().add(imgLabel, new Integer(Integer.MIN_VALUE));
        imgLabel.setBounds(0, 0, img.getIconWidth(), img.getIconHeight());
        JPanel jp = (JPanel)getContentPane();
        jp.setOpaque(false);

        this.setLayout(new BorderLayout());

        JPanel panelM = new JPanel();
        JPanel panelL = new JPanel();
        panelM.setOpaque(false);
        panelM.setOpaque(false);

        // set up CENTER
        JScrollPane jsp = new JScrollPane(taChat);
        jsp.setOpaque(false);
        jsp.getViewport().setOpaque(false);
        jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        taChat.setOpaque(false);
        panelM.add(jsp);

        // set up SOUTH
        JLabel lEdit = new JLabel("New Msg:");
        lEdit.setOpaque(false);
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
        saveMenuItem= new JMenuItem("Save Chat");
        quitMenuItem= new JMenuItem("Quit");
        quitMenuItem.addActionListener(this);
        saveMenuItem.addActionListener(this);

        JMenuBar mb = new JMenuBar();
        cMenu.add(saveMenuItem);
        cMenu.add(quitMenuItem);
        mb.add(cMenu);
        this.setJMenuBar(mb);

        // 注册窗口事件监听器
        this.addWindowListener(new MyWinAdapter());

        // set visible
        this.setVisible(true);
        this.pack();
        this.setSize(760,570);
        //this.setResizable(false);
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
            } else if (e.getSource() == saveMenuItem) {
                // 询问保存地址
                JFileChooser jc = new JFileChooser();
                // 设置默认文件名
                JTextField text;
                text=getTextField(jc);
                text.setText("Minet "+ getCurDate()+ ".txt");
                int rVal = jc.showSaveDialog(this);  ////显示保存文件的对话框
                String path;
                if(rVal == JFileChooser.APPROVE_OPTION) {
                    path = jc.getCurrentDirectory().toString() +  System.getProperty("file.separator")  + jc.getSelectedFile().getName();

                    // 保存文件
                    File file = new File(path);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileOutputStream out = new FileOutputStream(file, true);
                    out.write(taChat.getText().getBytes("utf-8"));
                    out.close();
                }
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

    public static String getCurDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return df.format(new Date());// new Date()为获取当前系统时间
    }

    public JTextField getTextField(Container c) {
            JTextField textField = null;
            for (int i = 0; i < c.getComponentCount(); i++) {
                Component cnt = c.getComponent(i);
                if (cnt instanceof JTextField) {
                    return (JTextField) cnt;
                }
                if (cnt instanceof Container) {
                    textField = getTextField((Container) cnt);
                    if (textField != null) {
                        return textField;
                    }
                }
            }
            return textField;
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
