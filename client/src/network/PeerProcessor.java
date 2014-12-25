package com.sdl.MinetClient.network;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.net.Socket;


/**
 * A <code>PeerSocket</code> is a class that manage connections with Minet peers.
 */
public class PeerProcessor {
    public static final String sp = " ";
    public static final String lineEnd= "\r\n";

    public static void hello(PeerHost h) {
        // send hello message
        String msg = "MINET" + sp + h.selfName + lineEnd;

        h.ps.print(msg);
        h.ps.flush();
        System.out.println(msg);
    }

    public static void hello(Socket s, String selfName) {
        PrintStream ps = null;
        try {
            ps = new PrintStream(s.getOutputStream());
        }  catch (Exception ex) {
            ex.printStackTrace();
        }

        String msg = "MINET" + sp + selfName + lineEnd;

        ps.print(msg);
        ps.flush();
        System.out.println(msg);
    }

    public static String sendMsg(PeerHost h, String text) {
        String dat = getCurDate();
        String msg = "P2P1.0" + sp + "P2PMESSAGE" + sp + h.selfName + lineEnd;
        msg += "Date" + sp + dat + lineEnd;
        msg += "Content-Length:" + sp + text.length() + lineEnd;
        msg += lineEnd;
        msg += text + lineEnd;
        msg += lineEnd;

        h.ps.print(msg);
        h.ps.flush();
        System.out.println(msg);

        return dat;
    }

    public static void leave(PeerHost h) {
        // send leave msg to server
        String msg = "P2P1.0" + sp + "LEAVE" + sp + h.selfName + lineEnd;
        msg += "Date" + sp + getCurDate() + lineEnd;
        msg += "Content-Length:" + sp + 0 + lineEnd;
        msg += lineEnd;
        msg += lineEnd;

        h.ps.print(msg);
        h.ps.flush();
        System.out.println(msg);
    }

    public static void sendBeat(PeerHost h) {
        // send leave msg to server
        String msg = "P2P1.0" + sp + "BEAT" + sp + h.selfName + lineEnd;
        msg += "Date" + sp + getCurDate() + lineEnd;
        msg += "Content-Length:" + sp + 0 + lineEnd;
        msg += lineEnd;
        msg += lineEnd;

        h.ps.print(msg);
        h.ps.flush();

        System.out.println(msg);
    }

    public static String[] recvAndProcsMsg(PeerHost h)  throws java.io.IOException{
        return recvAndProcsMsg(h.reader);
    }

    public static String[] recvAndProcsMsg(Socket s)  throws java.io.IOException{
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        }  catch (Exception ex) {
            ex.printStackTrace();
        }

        return recvAndProcsMsg(reader);
    }

    public static String[] recvAndProcsMsg(BufferedReader reader)  throws java.io.IOException{

        // read all lines
        List<String> results = new ArrayList<String>();
        List<String> strs = new ArrayList<String>();
        String buff = "";

        // process it depending on the first line's information
        // If it is a hello msg, just return the array and ignore the rest
        // hello msg format: ["MINET"/"MIRO", hostname]
        buff = reader.readLine();
        System.out.println(buff);

        String[] temp = buff.split(sp);
        if ((temp[0].equals("MINET"))|| (temp[0].equals("MIRO")))
            return temp;

        // If it is not a hello msg, then remove the first element:protocol header
        String[] parts = Arrays.copyOfRange(temp, 1, temp.length);
        results.addAll(Arrays.asList(parts));

        // read property info and analyze it
        while(!(buff = reader.readLine()).equals("")) {
            strs.add(buff);
            System.out.println(buff);
        }


        // 获取首部行
        for (int i = 0; i < strs.size(); i++) {
            results.add(strs.get(i).split(sp)[1]);
        }

        // read data info
        strs.clear();
        while(!(buff = reader.readLine()).equals("")) {
            strs.add(buff);
            System.out.println(buff);
        }

        System.out.println("Data read complete");

        // 获取数据主体
        if (results.get(0).equals("P2PMESSAGE")) {
                // p2p msg format: ["P2PMESSAGE", userName, date, content-length, data]
                String data = "";
                for (int i = 0; i < strs.size(); i++) {
                    data += strs.get(i) + "\r\n";
                }
                results.add(data);
        } else {
                // beat msg format: ["BEAT", userName, date, content-length, data]
                // leave msg format: ["LEAVE", userName, date, content-length,data]
                results.add("");            
        }

        return (String [])results.toArray(new String[results.size()]);
    }

    public static String getCurDate() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");//设置日期格式
        return df.format(new Date());// new Date()为获取当前系统时间
    }
}