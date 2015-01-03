package com.sdl.MinetClient.gui;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FileOutputStream;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.URL;
import com.sdl.MinetClient.network.*;

public class GroupChatFrame extends JFrame implements ActionListener{

    // public properties
    public String name;
    public int listeningPort;
    public List<PeerHost> peers;
    public ServerHost sh;
    public PeerListener pl;

    // GUI component
    public JTextField tfEdit = new JTextField(55);
    public JButton btSend = new JButton("send");
    public JTextArea taChat = new JTextArea(30, 50);
    public java.awt.List list1 = new java.awt.List(27);
    public JMenuItem quitMenuItem;
    public JMenuItem mailMenuItem;
    public JMenuItem saveMenuItem;

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
        // 设置背景图片
        URL url = getClass().getResource("/resource/GroupChatBG.jpg");
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
        panelL.setOpaque(false);
        // set up CENTER
        JScrollPane jsp = new JScrollPane(taChat);
        jsp.setOpaque(false);
        jsp.getViewport().setOpaque(false);
        jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        taChat.setOpaque(false);
        panelM.add(jsp);

        panelM.add(list1);
        list1.addActionListener(this);

        // set up SOUTH
        JLabel lEdit = new JLabel("New Msg:");
        lEdit.setOpaque(false);
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
        saveMenuItem= new JMenuItem("Save Chat");
        mailMenuItem= new JMenuItem("Send Mail");
        quitMenuItem= new JMenuItem("Quit");
        saveMenuItem.addActionListener(this);
        mailMenuItem.addActionListener(this);
        quitMenuItem.addActionListener(this);

        cMenu.add(mailMenuItem);
        cMenu.add(saveMenuItem);
        cMenu.add(quitMenuItem);
        JMenuBar mb = new JMenuBar();
        mb.add(cMenu);
        this.setJMenuBar(mb);

        // 注册窗口事件监听器
        this.addWindowListener(new MyWinAdapter());

        // set visible
        this.setVisible(true);
        this.pack();
        this.setSize(760,570);
        this.setResizable(false);
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
