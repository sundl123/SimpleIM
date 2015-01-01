package com.sdl.MinetClient.gui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import com.sdl.MinetClient.network.ServerProcessor;


public class MailFrame extends JFrame implements ActionListener{
    public static final int READ_MODE = 0;
    public static final int EDIT_MODE = 1;

    public GroupChatFrame gcf;

    // GUI component
    public TextField tfFrom = new TextField(30);;
    public TextField tfTo = new TextField(30);
    public TextField tfTitle = new TextField(30);
    public TextArea taMsg = new TextArea(20, 20);
    public JButton btSend = new JButton("Send");
    public JButton btClear = new JButton("Clear");
    public JButton btQuit = new JButton("Quit");

    public MailFrame(GroupChatFrame gcf_, int mode, String[] contents){
        gcf = gcf_;

        // set up gui
        this.setLayout(new BorderLayout());

        JPanel panelN = new JPanel(new GridLayout(4, 1));
        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        JPanel panel3 = new JPanel();

        Label label = new Label("     From:");
        panel1.add(label);
        panel1.add(tfFrom);
        label = new Label("         To:");
        panel2.add(label);
        panel2.add(tfTo);        
        label = new Label("Subject:");
        panel3.add(label);
        panel3.add(tfTitle);
        label = new Label("Message: ");

        panelN.add(panel1);
        panelN.add(panel2);
        panelN.add(panel3);
        panelN.add(label);

        JPanel panelS = new JPanel();
        panelS.add(btSend);
        panelS.add(btClear);
        panelS.add(btQuit);
        btSend.addActionListener(this);
        btClear.addActionListener(this);
        btQuit.addActionListener(this);

        this.add(panelN, BorderLayout.NORTH);
        this.add(taMsg, BorderLayout.CENTER);
        this.add(panelS, BorderLayout.SOUTH);

        if (mode == READ_MODE) {
            // mail format:["EMAIL", sender, date, length, title, data]
            this.tfFrom.setText(contents[1]);
            this.tfTo.setText("Me");
            this.tfTitle.setText(contents[4]);
            this.taMsg.setText(contents[contents.length-1]);
            this.tfFrom.setEnabled(false);
            this.tfTo.setEnabled(false);
            this.tfTitle.setEnabled(false);
            this.taMsg.setEnabled(false);
            this.btSend.setEnabled(false);
            this.btClear.setEnabled(false);
        } else {
            this.tfFrom.setText("Me");
            this.tfFrom.setEnabled(false);
        }

        // set visible
        this.setVisible(true);
        this.pack();
        this.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btSend) {
            boolean find;
            for (int i = 0; i < gcf.peers.size(); i++) {
                if (gcf.peers.get(i).hostName.equals(tfTo.getText())){
                    ServerProcessor.sendMail(gcf.sh, tfTo.getText(), tfTitle.getText(), taMsg.getText());
                    JOptionPane.showMessageDialog(null, "邮件" + tfTitle.getText() + "已发给" + tfTo.getText(), "发送成功",JOptionPane.INFORMATION_MESSAGE);  
                    return;
                }
            }
            JOptionPane.showMessageDialog(null, "收件人" + tfTo.getText() + "不在列表中", "发送失败",JOptionPane.ERROR_MESSAGE);  
        } else if (e.getSource() == btClear) {
            this.taMsg.setText("");
        } else if (e.getSource() == btQuit) {
            this.dispose();
        }
    }
}