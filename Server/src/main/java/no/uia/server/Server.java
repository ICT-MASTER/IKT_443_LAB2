package no.uia.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by per on 24.02.16.
 */
public class Server extends Thread implements Runnable {

    public static final int PORT = 25000;
    private final ServerGUI gui;

    public boolean isRunning = true;

    private final ClientList clientList;
    public ClientList getClientList(){
        return clientList;
    }



    private ServerSocket serverSocket;


    private static final Logger logger = LogManager.getLogger(Server.class);

    public Server(){
        clientList = new ClientList();
        clientList.setMaxClients(5);

        // Create Server GUI
        gui = new ServerGUI();
        gui.setVisible(true);
    }


    public void run(){
        logger.info("Staring server on port " + Server.PORT);

        // Create new ServerSocket instance
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress("localhost", Server.PORT));
            logger.info("The server is listening on " + Server.PORT + ".");

            gui.getServerAddress().setText("localhost" + Server.PORT);


            while (isRunning) {

                Socket clientSocket = serverSocket.accept();

                // Create new client instance and start the thread
                Client client = new Client(clientSocket, this);
                client.start();

                this.clientList.addClient(client, this.gui.getClientList());


                logger.info("New connection accepted!");

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    
}
