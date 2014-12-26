package com.sdl.MinetClient.network;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.text.SimpleDateFormat;

public class ServerProcessor {

    public static final String sp = " ";
    public static final String lineEnd= "\r\n";

    public static void hello(ServerHost h) {
        // send hello message
        String msg = "MINET" + sp + h.selfName + lineEnd;

        System.out.println(msg);

        h.ps.print(msg);
        h.ps.flush();
    }

    public static void logIn(ServerHost h) {
        String msg = "CS1.0" + sp + "LOGIN" + sp + h.selfName + lineEnd;
        msg += "Port" + sp + h.listeningPort + lineEnd;
        msg += "Date" + sp + getCurDate() + lineEnd;
        msg += "Content-Length:" + sp + 0 + lineEnd;
        msg += lineEnd;
        msg += lineEnd;

        h.ps.print(msg);
        h.ps.flush();
        System.out.println(msg);
    }

    public static void getList(ServerHost h) {
        // fisrt send user list queries
        String msg = "CS1.0" + sp + "GETLIST" + sp + h.selfName + lineEnd;
        msg += "Date" + sp + getCurDate() + lineEnd;
        msg += "Content-Length:" + sp + 0 + lineEnd;
        msg += lineEnd;
        msg += lineEnd;

        h.ps.print(msg);
        h.ps.flush();
        System.out.println(msg);
    }

    public static String sendMsg(ServerHost h, String text) {
        String dat = getCurDate();
        String msg = "CS1.0" + sp + "MESSAGE" + sp + h.selfName + lineEnd;
        msg += "Date" + sp + dat + lineEnd;
        msg += "Content-Length:" + sp + text.length() + lineEnd;
        msg += lineEnd;
        msg += text;
        msg += lineEnd;
        msg += lineEnd;

        h.ps.print(msg);
        h.ps.flush();
        System.out.println(msg);

        return dat;
    }

    public static String sendMail(ServerHost h, String receiver, String title, String text) {
        String dat = getCurDate();
        String msg = "CS1.0" + sp + "EMAIL" + sp + h.selfName + lineEnd;
        msg += "Date" + sp + dat + lineEnd;
        msg += "Content-Length" + sp + text.length() + lineEnd;
        msg += "Receiver" + sp + receiver + lineEnd;
        msg += "Title" + sp + title + lineEnd;
        msg += lineEnd;

        msg += text + lineEnd + lineEnd;
        h.ps.print(msg);
        h.ps.flush();
        return dat;
    }

    public static void leave(ServerHost h) {
        // send leave msg to server
        String msg = "CS1.0" + sp + "LEAVE" + sp + h.selfName + lineEnd;
        msg += "Date" + sp + getCurDate() + lineEnd;
        msg += "Content-Length:" + sp + 0 + lineEnd;
        msg += lineEnd;
        msg += lineEnd;

        h.ps.print(msg);
        h.ps.flush();
        System.out.println(msg);
    }

    public static void sendBeat(ServerHost h) {
        // send leave msg to server
        String msg = "CS1.0" + sp + "BEAT" + sp + h.selfName + lineEnd;
        msg += "Date" + sp + getCurDate() + lineEnd;
        msg += "Content-Length:" + sp + 0 + lineEnd;
        msg += lineEnd;
        msg += lineEnd;

        h.ps.print(msg);
        h.ps.flush();
        System.out.println(msg);
    }

    public static String[] recvAndProcsMsg(ServerHost h) throws java.io.IOException{
        List<String> results = new ArrayList<String>(); // store all results

        List<String> strs = new ArrayList<String>();
        String buff = "";

        //  read header information
        // If it is a hello msg, just return the array and ignore the rest
        // hello msg format: ["MINET"/"MIRO", hostname]
        buff = h.reader.readLine();
        System.out.println(buff);

        String[] temp = buff.split(sp);
        if ((temp[0].equals("MINET"))|| (temp[0].equals("MIRO")))
            return temp;

        // If it is not a hello msg, then remove the first element:protocol header
        String[] parts = Arrays.copyOfRange(temp, 1, temp.length);
        results.addAll(Arrays.asList(parts));
        // read property info and analyze it
        while(!(buff = h.reader.readLine()).equals("")) {
            strs.add(buff);
            System.out.println(buff);
        }

        // 获取首部行
        for (int i = 0; i < strs.size(); i++) {
            results.add(strs.get(i).split(sp)[1]);
        }

        // read data info
        strs.clear();
        while(!(buff = h.reader.readLine()).equals("")) {
            strs.add(buff);
            System.out.println(buff);
        }

        System.out.println("Data read complete");

        // 获取数据主体
        if ((results.get(0).equals("CSMESSAGE")) || (results.get(0).equals("EMAIL"))) {
            // msg format: ["MESSAGE", userName, date, length, data...]
            // mail format:["EMAIL", sender, date, length, title, data]
            String data = "";
            for (int i = 0; i < strs.size(); i++) {
                data += strs.get(i) + "\r\n";
            }
            results.add(data);
        } else if (results.get(0).equals("LIST")) {
             // list msg format: ["LIST", date, content-length, u1, i1, p1, u2, i2, p2...]
            for (int i = 0; i < strs.size(); i++) {
                results.addAll(Arrays.asList(strs.get(i).split(sp)));
            }
        } else if (results.get(0).equals("UPDATE")) {
            // update msg format: ["UPDATE", status, date, length, i1,p1]
            results.addAll(Arrays.asList(strs.get(0).split(sp)));
        } else {
                // status msg format: ["STATUS", status, date, length]
                results.add("");
        }
        return (String [])results.toArray(new String[results.size()]);
    }

    public static String getCurDate() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");//设置日期格式
        return df.format(new Date());// new Date()为获取当前系统时间
    }
}