package no.uia.client;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Per-Arne on 24.02.2016.
 */
public class P2PServer extends Thread implements Runnable {


    private final int port;
    private final JTextPane chatWindow;
    private Client client;
    private OnConnectListener onConnectListener;
    private ServerSocket p2pSocket;

    public void setOnConnectListener(OnConnectListener listener){
        this.onConnectListener = listener;
    }

    public P2PServer(int port, JTextPane chatWindow, Client client)
    {
        this.client = client;
        this.port = port;
        this.onConnectListener = null;
        this.chatWindow = chatWindow;
    }

    public void run()
    {
        try {
            p2pSocket = new ServerSocket();
            p2pSocket.bind(new InetSocketAddress("localhost", port));
            Socket s = p2pSocket.accept();
            this.onConnectListener.onConnected(s);

            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            out.println(System.nanoTime());

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                //chatWindow.setText(chatWindow.getText() + "\n" + inputLine);
                String[] answer = inputLine.split("=");


                if (answer[0].equalsIgnoreCase("get:connect")) {

                    // Attempt to find host with specified port
                    String host = answer[1].split(":")[0];
                    String port = answer[1].split(":")[1];

                    Socket old_client = client.socket;
                    client.socket = new Socket();
                    old_client.close();



                    client.socket.connect(new InetSocketAddress(host, Integer.parseInt(port)));

                    // NOTE this = Client.out/in
                    client.out = new PrintWriter(client.socket.getOutputStream(), true);
                    client.in = new BufferedReader(new InputStreamReader(client.socket.getInputStream()));


                    chatWindow.setText(chatWindow.getText() + "\n" + "[SYSTEM]: Connected to central server at " + client.socket.getRemoteSocketAddress());

                    p2pSocket.close();
                    System.out.println(":OOOO");
                    break;



                }
                else if(answer[0].equalsIgnoreCase("get:msg"))
                {
                    String[] split = answer[1].split(",");

                    chatWindow.setText(chatWindow.getText() + "\n[" + split[0] + "]: " + split[1]);

                }
            }



        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
