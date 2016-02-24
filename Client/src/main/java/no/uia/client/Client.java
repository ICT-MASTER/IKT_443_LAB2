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
import java.net.Socket;

/**
 * Created by per on 24.02.16.
 */

public class Client extends Thread implements Runnable{


    private Socket socket = null;
    private String address;
    private int port;
    PrintWriter out;
    private static final Logger logger = LogManager.getLogger(Client.class);

    private ClientGUI gui;
    private Stopwatch stopwatch;


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



                // Request new jlist data
                out.println("get:userlist=");
            }
        });
        t.setInitialDelay(0);
        t.start();
    }

    public void update_ping(){
        Timer t = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopwatch.record();
                out.println("get:ping=");
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
    public void input_userlist(String item){
        DefaultListModel model = (DefaultListModel) gui.getClientList().getModel();

        if(!model.contains(item))
            model.addElement(item);
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
                    input_chat("System: No host selected!");
                else
                    output_connect((String) model.getElementAt(index));
            }
        });
    }


    public void run() {
        try {
            socket = new Socket(address, port);

            gui.lblConnectedTo.setText("Connected: " + socket.getRemoteSocketAddress());

            // Socket io streams
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

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

                if(answer[0].equalsIgnoreCase("get:unhandled")){
                    input_chat("Server: " + answer[1]);
                }


            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
