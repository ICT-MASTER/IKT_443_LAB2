package no.uia.server;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by per on 24.02.16.
 */
public class Client extends Thread implements Runnable {

    private int ID = -1;
    private final Socket socket;
    private final Server serverInstance;
    private long lastPing = System.currentTimeMillis();
    private static final Logger logger = LogManager.getLogger(Client.class);
    PrintWriter out;
    private BufferedReader in;

    public long getLastPing(){
        return lastPing;
    }

    public Client(Socket socket, Server serverInstance){
        this.socket = socket;
        this.serverInstance = serverInstance;
    }

    public void run(){


        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                logger.info("Client {" + this.ID + "} => " + inputLine);
                String[] answer = inputLine.split("=");

                if(answer[0].equalsIgnoreCase("get:userlist")){
                    // Iterate over all clients connected to server, and send them to the client
                    ArrayList<String> hosts = new ArrayList<String>();
                    for(Client c : serverInstance.getClientList().toList())
                        hosts.add(c.getHost());

                    String clients = StringUtils.join(hosts, ",");
                    out.println("get:userlist=" + clients);


                }
                else if(answer[0].equalsIgnoreCase("get:ping")){
                    lastPing = System.currentTimeMillis();
                    out.println("get:ping="+lastPing);
                }
                else if(answer[0].equalsIgnoreCase("get:connect"))
                {

                    // Attempt to find host with specified port
                    String port = answer[1].split(":")[1];

                    // $S$ replies to $A$ with $B$'s public and private TCP endpoints, and at the same time sends $A$'s public and private endpoints to $B$.
                    Client found_client = serverInstance.getClientList().findByPort(port);
                    found_client.out.println("get:p2p=" +
                                    this.getSocket().getLocalSocketAddress().toString().replace("/", "") + "," +
                                    this.getSocket().getRemoteSocketAddress().toString().replace("/", "")
                    );

                    this.out.println("get:p2p=" +
                                    found_client.getSocket().getLocalSocketAddress().toString().replace("/", "") + "," +
                                    found_client.getSocket().getRemoteSocketAddress().toString().replace("/", "")
                    );






                }
                else {
                    out.println("get:unhandled=Unrecognized " + answer[0] + " with value " + answer[1]);
                }


            }



        } catch (IOException e) {
            e.printStackTrace();
        }



    }


    public void setID(int ID) {
        this.ID = ID;
    }

    public String toString(){
        return this.ID + ": " + this.socket.getRemoteSocketAddress().toString();
    }

    public String getHost(){
        return this.socket.getRemoteSocketAddress().toString().replace("/","");
    }

    public Socket getSocket() {
        return socket;
    }
}
