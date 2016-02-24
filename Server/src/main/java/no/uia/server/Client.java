package no.uia.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by per on 24.02.16.
 */
public class Client extends Thread implements Runnable {

    private int ID = -1;
    private final Socket socket;
    private final Server serverInstance;
    private static final Logger logger = LogManager.getLogger(Client.class);

    public Client(Socket socket, Server serverInstance){
        this.socket = socket;
        this.serverInstance = serverInstance;
    }

    public void run(){


        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader( socket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                logger.info("Client {" + this.ID + "} => " + inputLine);
                String[] answer = inputLine.split("=");

                if(answer[0].equalsIgnoreCase("get:userlist")){
                    // Iterate over all clients connected to server, and send them to the client
                    for(Client c : serverInstance.getClientList().toList()) {
                        out.println("get:userlist=" + c.getHost());
                    }

                }
                else if(answer[0].equalsIgnoreCase("get:ping")){
                    out.println("get:ping="+System.currentTimeMillis());
                } else {
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
}
