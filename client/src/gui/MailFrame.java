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
    public TextField tfFrom = new TextField(15);;
    public TextField tfTo = new TextField(15);
    public TextField tfTitle = new TextField(15);
    public TextArea taMsg = new TextArea(30, 50);
    public Button btSend = new Button("Send");
    public Button btClear = new Button("Clear");
    public Button btQuit = new Button("Quit");

    public MailFrame(GroupChatFrame gcf_, int mode, String[] contents){
        gcf = gcf_;

        // set up GUI
        this.setLayout(new GridLayout(6,1,2,5));

        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        JPanel panel3 = new JPanel();
        JPanel panel4 = new JPanel();

        Label label = new Label("From:");
        panel1.add(label);
        panel1.add(tfFrom);

        label = new Label("To:");
        panel2.add(label);
        panel2.add(tfTo);        

        label = new Label("Subject:");
        panel3.add(label);
        panel3.add(tfTitle);

        panel4.add(btSend);
        panel4.add(btClear);
        panel4.add(btQuit);
        btSend.addActionListener(this);
        btClear.addActionListener(this);
        btQuit.addActionListener(this);

        label = new Label("Message:");
        this.add(panel1);
        this.add(panel2);
        this.add(panel3);
        this.add(label);
        this.add(taMsg);
        this.add(panel4);

        if (mode == READ_MODE) {
            // mail format:["EMAIL", sender, date, length, title, data]
            this.btSend.setEnabled(false);
            this.btClear.setEnabled(false);
            this.tfFrom.setText(contents[1]);
            this.tfTo.setText("Me");
            this.tfTitle.setText(contents[4]);
            this.taMsg.setText(contents[contents.length-1]);
        } else {
            this.tfFrom.setText("Me");
        }

        // set visible
        this.setVisible(true);
        this.setSize(400,600);
        this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btSend) {
            boolean find;
            for (int i = 0; i < gcf.peers.size(); i++) {
                if (gcf.peers.get(i).hostName.equals(tfTo.getText())){
                    ServerProcessor.sendMail(gcf.sh, tfTo.getText(), tfTitle.getText(), taMsg.getText());
                    return;
                }
            }
        } else if (e.getSource() == btClear) {
            this.taMsg.setText("");
        } else if (e.getSource() == btQuit) {
            this.dispose();
        }
    }
}