package com.sdl.MiroServer;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MiroServer {
    public static int listeningPort = 3456;
    public List<Host> hosts; // store clients
    public ServerSocket server = null; // server socket
    public Socket socket = null; // client socket

    public MiroServer() {
        hosts = new ArrayList<Host>();

        try {
            System.out.println("Miro Server starting: Listing port num:" + listeningPort);
            server = new ServerSocket(listeningPort);
            Host newHost;

            // 不停地监听新的连接
            while (true) {
                socket = server.accept();
                // set time out for socket
                try {
                    socket.setSoTimeout(1000);
                } catch (Exception e) {
                    // Do nothing
                }
                System.out.println(socket.getInetAddress() + "connections");
                newHost = new Host(socket, this);
                newHost.start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MiroServer();
    }
}