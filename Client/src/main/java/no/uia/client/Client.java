package no.uia.client;

import javafx.scene.paint.Stop;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

/**
 * Created by per on 24.02.16.
 */

public class Client extends Thread implements Runnable{


    private ServerSocket p2pSocket = null;

    public Socket socket = null;
    private String address;
    private int port;
    public PrintWriter out;
    private static final Logger logger = LogManager.getLogger(Client.class);

    private ClientGUI gui;
    private Stopwatch stopwatch;
    private boolean chatMode = false;
    public BufferedReader in;


    public Client(String address, int port){
        this.address = address;
        this.port = port;
        this.gui = new ClientGUI();
        this.stopwatch = new Stopwatch();
        this.setupGUI();

    }

    /*############################################################
    ##
    ##
    ## UPDATES
    ##
    ##
     ############################################################*/
    public void update_ClientList(){
        Timer t = new Timer(5000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {


                if(!chatMode){

                    // Request new jlist data
                    out.println("get:userlist=");
                }

            }
        });
        t.setInitialDelay(0);
        t.start();
    }

    public void update_ping(){
        Timer t = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!chatMode) {
                    stopwatch.record();
                    out.println("get:ping=");
                }
            }
        });
        t.setInitialDelay(0);
        t.start();
    }

    /*############################################################
    ##
    ##
    ## INPUT HANDLING
    ##
    ##
     ############################################################*/
    public void input_userlist(String items){
        DefaultListModel model = (DefaultListModel) gui.getClientList().getModel();
        int index = gui.getClientList().getSelectedIndex();
        model.clear();
        for(String item : items.split(","))
            model.addElement(item);
        gui.getClientList().setSelectedIndex(index);
    }

    private void input_ping(long half_rtt, long rtt) {
        gui.lblPing.setText("HRTT: " + half_rtt + "ms | RTT: " + rtt + "ms");
    }

    private void input_chat(String msg){
        gui.txtChat.setText(gui.txtChat.getText() + "\n" + msg);
    }

    /*############################################################
    ##
    ##
    ## Output Handling
    ##
    ##
     ############################################################*/

    private void output_connect(String address){
        out.println("get:connect="+address);
    }



    /*############################################################
    ##
    ##
    ## GUI Handling
    ##
    ##
     ############################################################*/
    public void setupGUI()
    {
        gui.btnConnect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                DefaultListModel model = (DefaultListModel) gui.getClientList().getModel();
                int index = gui.getClientList().getSelectedIndex();
                if(index == -1)
                    input_chat("[SYSTEM]: No host selected.");
                else {
                    output_connect((String) model.getElementAt(index));
                }
            }
        });

        gui.btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                // Retrieve text
                String text = gui.txtInput.getText();
                gui.txtInput.setText("");

                // Send to server
                out.println(text);

            }
        });

        gui.btnConnectCentral.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                output_connect(address+":"+port);

                chatMode = false;
                gui.lblPing.setEnabled(true);
                gui.btnSend.setEnabled(false);
                gui.txtInput.setEnabled(false);
                gui.btnConnectCentral.setEnabled(false);
                gui.btnConnect.setEnabled(true);

            }
        });
    }


    public void run() {
        try {
            socket = new Socket(address, port);
            socket.setReuseAddress(true);
        }catch (SocketException e1) {
            e1.printStackTrace();
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        input_chat("[SYSTEM]: Connected to central server at " +  socket.getRemoteSocketAddress());

        gui.lblMe.setText(socket.getLocalSocketAddress().toString());
        gui.lblConnectedTo.setText("Connected: " + socket.getRemoteSocketAddress());


        int max_fail_count = 10;
        int fail_count = 0;

        while(fail_count++ < max_fail_count) {

            try {
                // Socket io streams
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Frequently updates
                this.update_ClientList();
                this.update_ping();


                String serverAnswer;
                while ((serverAnswer = in.readLine()) != null) {
                    logger.info("Server => " + serverAnswer);


                    String[] answer = serverAnswer.split("=");

                    // get:userlist reply
                    if (answer[0].equalsIgnoreCase("get:userlist")) {
                        input_userlist(answer[1]);
                    }

                    if (answer[0].equalsIgnoreCase("get:ping")) {
                        long half_rtt = stopwatch.diff(Long.parseLong(answer[1]));
                        long rtt = stopwatch.record();
                        input_ping(half_rtt, rtt);
                    }

                    if (answer[0].equalsIgnoreCase("get:p2p")) {
                        //input_chat(answer[1]);


                        // Close connection to Central Server
                        socket.close();

                        input_chat("[SYSTEM]: Disconnected from " + socket.getRemoteSocketAddress());

                        // Enter chatMode
                        this.chatMode = true;

                        // Resolve endpoints for the p2p connection
                        // Remote p2p = The remote clients opened port
                        // Local p2p = The local clients (me) opened port
                        int remote_p2p_port = Integer.parseInt(answer[1].split(",")[1].split(":")[1]);
                        String remote_p2p_address = answer[1].split(",")[1].split(":")[0];
                        int local_p2p = socket.getLocalPort();

                        // Create a P2PServer
                        P2PServer p2pServer = new P2PServer(local_p2p, gui.txtChat, this);
                        p2pServer.start();

                        input_chat("[SYSTEM]: Setting up local endpoint at port " + local_p2p);

                        // Callback for when a client is connected
                        final Socket[] remote_socket = {null};
                        p2pServer.setOnConnectListener(new OnConnectListener() {
                            public void onConnected(Socket s) {
                                logger.info("Client Connected!");
                                remote_socket[0] = s;

                            }
                        });

                        input_chat("[SYSTEM]: Connecting to per at " + remote_p2p_address + ":" + remote_p2p_port);
                        Socket newSock = new Socket();
                        newSock.connect(new InetSocketAddress(remote_p2p_address, remote_p2p_port));
                        socket = newSock;

                        // Update connectedToText and lblMe
                        gui.lblMe.setText(socket.getLocalSocketAddress().toString());
                        gui.lblConnectedTo.setText("Connected: " + socket.getRemoteSocketAddress());

                        // Update Socket IO to the remote host
                        out = new PrintWriter(socket.getOutputStream(), true);
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                        gui.btnConnect.setEnabled(false);
                        gui.btnConnectCentral.setEnabled(true);
                        gui.btnSend.setEnabled(true);
                        gui.txtInput.setEnabled(true);
                        gui.lblPing.setText("N/A");
                        gui.lblPing.setEnabled(false);
                    }

                    if (answer[0].equalsIgnoreCase("get:unhandled")) {
                        input_chat("Server: " + answer[1]);
                    }


                }


            } catch (IOException e) {
                e.printStackTrace();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }

        }

    }
}
