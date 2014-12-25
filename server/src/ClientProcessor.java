package com.sdl.MiroServer;

import java.util.Arrays;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.net.Socket;

public class ClientProcessor {

    public static final String sp = " ";
    public static final String lineEnd= "\r\n";

    /**
     * send hello msg to server and analyze feedback
     * @param h target to send to
     */
    public static void hello(Host h, String hostName) {
        // send hello message
        String msg = "MIRO" + sp + hostName + lineEnd;

        h.ps.print(msg);
        h.ps.flush();
        System.out.println(msg);
    }

    /**
     * send login feedback to client
     * @param receiver
     * @return error code
     */
    public static void sendStatus(Host h, int status) {
        String msg = "CS1.0" + sp + "STATUS" + sp + status + lineEnd;
        msg += "Date" + sp + getCurDate() + lineEnd;
        msg += "Content-Length" + sp + 0 + lineEnd;
        msg += lineEnd;
        msg += lineEnd;

        h.ps.print(msg);
        h.ps.flush();
        System.out.println(msg);
    }

    /**
     * send a list of online user info to a client
     * @param h the receiver
     * @param hostName the sender
     * @return a list of hosts information the server returned
     */
    public static void sendList(Host h, List<Host> hosts) {
        String msg = "CS1.0" + sp + "LIST" + lineEnd;
        msg += "Date" + sp + getCurDate() + lineEnd;

        String data = "";
        for (int i =0; i < hosts.size(); i++) {
            data += hosts.get(i).name + sp + hosts.get(i).ip + sp + hosts.get(i).listeningPort + lineEnd;
        }

        msg += "Content-Length:" + sp + data.length() + lineEnd;
        msg += lineEnd;
        msg += data;
        msg += lineEnd;

        h.ps.print(msg);
        h.ps.flush();
        System.out.println(msg);
    }

    public static void sendUpdate(Host h, List<Host> hosts, int status) {
        String msg = "CS1.0" + sp + "UPDATE" + sp + status + sp + h.name + lineEnd;
        msg += "Date" + sp + getCurDate() + lineEnd; 

        String data =h.name + sp + h.ip + sp + h.listeningPort + lineEnd;

        msg += "Content-Length" + sp + data.length() + lineEnd;
        msg += lineEnd;
        msg += data;
        msg += lineEnd;

        for (int i = 0; i < hosts.size(); i++) {
            if (hosts.get(i).name != h.name) {
                hosts.get(i).ps.print(msg);
                hosts.get(i).ps.flush();
            }
        }
        System.out.println(msg);
    }

    /**
     * send hello msg to client
     * @param h the receiver
     * @param hostName the sender
     * @param msg the text to send
     * @return  msg's send date of string format
     */
    public static void sendMsg(List<Host> hosts, String userName, String text) {
        String msg = "CS1.0" + sp + "CSMESSAGE" + sp + userName + lineEnd;
        msg += "Date" + sp + getCurDate() + lineEnd;
        msg += "Content-Length:" + sp + text.length() + lineEnd;       
        msg += lineEnd;
        msg += text;
        msg += lineEnd;

        for (int i = 0; i < hosts.size(); i++) {
            if (hosts.get(i).name != userName) {
                hosts.get(i).ps.print(msg);
                hosts.get(i).ps.flush();
            }
        }
        System.out.println(msg);
    }

    public static String[] recvAndProcsMsg(Host h) {
        List<String> results = new ArrayList<String>(); // store all results

        List<String> strs = new ArrayList<String>();
        String buff = "";

        //  read header information
        // If it is a hello msg, just return the array and ignore the rest
        // hello msg format: ["MINET"/"MIRO", hostname]
        try {
            buff = h.reader.readLine();
            System.out.println(buff);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        String[] temp = buff.split(sp);
        if ((temp[0].equals("MINET"))|| (temp[0].equals("MIRO")))
            return temp;

        // If it is not a hello msg, then remove the first element:protocol header
        String[] parts = Arrays.copyOfRange(temp, 1, temp.length);
        results.addAll(Arrays.asList(parts));

        // read property info and analyze it
        try {
            while(!(buff = h.reader.readLine()).equals("")) {
                strs.add(buff);
                System.out.println(buff);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 获取首部行
        for (int i = 0; i < strs.size(); i++) {
            results.add(strs.get(i).split(sp)[1]);
        }

        // read data info
        strs.clear();
        try {
            while(!(buff = h.reader.readLine()).equals("")) {
                strs.add(buff);
                System.out.println(buff);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 获取数据主体
        if (results.get(0).equals("MESSAGE")) {
                // msg format: ["MESSAGE", userName, date, length, data...]
                String data = "";
                for (int i = 0; i < strs.size(); i++) {
                    data += strs.get(i) + "\r\n";
                }
                results.add(data);            
        } else {
                // login msg format: ["LOGIN", userName, listeningPort, date, length]
                // getlist msg format: ["GETLIST", date, content-length]
                // leave msg format: ["LEAVE", userName, date, length]
                // beat msg foramt: ["BEAT", userName, date, length]
                results.add("");
        }

        return (String [])results.toArray(new String[results.size()]);
    }

    public static String getCurDate() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");//设置日期格式
        return df.format(new Date());// new Date()为获取当前系统时间
    }
}