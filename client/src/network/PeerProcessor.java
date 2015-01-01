package com.sdl.MinetClient.network;

import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Random;

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

    public static void sendFile(PeerHost h, String filePath) {
        // test
        System.out.println("I am trying to send a file: " + filePath + " to : " + h.hostName);
        // test

        DataOutputStream os = null;
        DataInputStream is = null;
        ServerSocket server = null;
        Socket socket = null;

        try {
            File file = new File(filePath);
            int progress = 0;

            int listeningPort = -1;
            while(true) {
                // 产生随机端口号（3000～4000之间）
                Random r = new Random();
                listeningPort = r.nextInt(4000) % (4000-3000+1) + 3000;     

                // 测试该端口是否可用
                try {
                    server = new ServerSocket(listeningPort);
                } catch (Exception e) {
                    continue;
                }

                // 发送文件头,用户名, IP, port, filename
                String msg = "P2PFILE" + sp + h.selfName + sp + h.socket.getLocalAddress().getHostAddress() + sp + listeningPort + sp + file.getName()+ lineEnd;

                h.ps.print(msg);
                h.ps.flush();

                // 等待连接
                 socket = server.accept();

                break;
            }

             os = new DataOutputStream(socket.getOutputStream());
             is = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));  

            // 将文件长度传给服务器端。
            os.writeLong((long) file.length());  
            os.flush();
  
            // 缓冲区大小  
            int bufferSize = 8192;  
            // 缓冲区  
            byte[] buf = new byte[bufferSize]; 

            // 传输文件  
            while (true) {  
                int read = 0;  
                if (is != null) {  
                    read = is.read(buf);  
                }  
                progress += Math.abs(read);
                if (read == -1) {  
                    break;  
                }  
                os.write(buf, 0, read); 
            }
            os.flush();
        }catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
            // 关闭所有连接  
            try {  
                if (os != null)  
                    os.close();  
            } catch (IOException e) {  
                }  
            try {  
                if (is != null)  
                    is.close();  
            } catch (IOException e) {  
                }  
            try {  
                if (socket != null)  {
                    server.close();
                    socket.close();  
                }
            } catch (IOException e) {  
                }  
            } 
    }

    public static void receiveFile(String ip, int port, String savePath) { 
        int progress = 0;
        Socket socket;

        // test
        System.out.println("I am trying to receiveFile: " + savePath + " from: " + ip + ":" + port);
        // test

        try {
            // 建立socket连接
            socket = new Socket(ip, port);

            // test
            System.out.println("connections success!");
            // test

            // 建立socket输入流  
            DataInputStream inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));  
            // 缓冲区大小  
            int bufferSize = 8192;  
            // 缓冲区  
            byte[] buf = new byte[bufferSize];  
            int passedlen = 0;  
            long len = 0;  
            // 获取文件名称
            DataOutputStream fileOut = new DataOutputStream(  
                    new BufferedOutputStream(new BufferedOutputStream(  
                            new FileOutputStream(savePath))));  
            // 获取文件长度  
            len = inputStream.readLong();  

            System.out.println("文件的长度为:" + len + "  KB");  
            System.out.println("开始接收文件!");  

            // 获取文件，下边是进度条。
            System.out.print("#>>>>>>>>#>>>>>>>>>#>>>>>>>>>#>>>>>>>>>#>>>>>>>>>#");
            System.out.println(">>>>>>>>>#>>>>>>>>>#>>>>>>>>>#>>>>>>>>>#>>>>>>>>>#");  
            while (true) {
                int read = 0;  
                if (inputStream != null) {  
                    read = inputStream.read(buf);  
                }  
                passedlen += read;
                if (read == -1) {  
                    break;  
                }
                if((int)(passedlen * 100.0 / len)-progress > 0){
                            progress = (int)(passedlen * 100.0 / len);
                              System.out.println("文件接收了" + progress + "%"); 
                            System.out.print(">");
                            }

                fileOut.write(buf, 0, read);  
            }
            System.out.println();
            System.out.println("接收完成，文件存为: " + savePath);  
            
            fileOut.close();
            inputStream.close();
            socket.close();
        } catch (Exception e) {
            System.err.println("File Server Excetption: " + e);
            e.printStackTrace();
        }
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
        if (buff == null) {
            return null;
        }
        System.out.println(buff);

        String[] temp = buff.split(sp);

        if ((temp[0].equals("MINET"))|| (temp[0].equals("MIRO")) || (temp[0].equals("P2PFILE")))
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