package no.uia.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by per on 24.02.16.
 */
public class ClientList {


    private final int timeout;
    private final JList clientList;
    private ArrayList<Client> list;

    private int maxClients;
    private static final Logger logger = LogManager.getLogger(ClientList.class);

    public ClientList(JList clientList){
        list = new ArrayList<Client>();
        this.clientList = clientList;
        this.timeout = 10 * 1000;


        timeoutListener();
    }

    /**
     * Create a client list, Timeout is in seconds
     * @param timeout
     */
    public ClientList(int timeout, JList clientList){
        this.timeout = timeout * 1000;
        this.clientList = clientList;
        timeoutListener();
    }

    public void timeoutListener(){
        Timer t = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                // Create a list which shall contain all clients subject for removal
                ArrayList<Client> removedClients = new ArrayList<Client>();

                // Iterate over all connected clients
                // Calculate last ping update
                // If ping update is older then "timeout"
                // Add the client to removedClients list
                for(Client c : list){
                    long lastPing = c.getLastPing();
                    long now = System.currentTimeMillis();

                    if (now - lastPing > timeout){
                        logger.info("Disconnecting: " + c.getHost() + " (Connection Timeout)");
                        removedClients.add(c);
                    }
                }

                // Iterate over the removed clients
                // Close the socket
                // Remove them from the primary client list
                for(Client c: removedClients)
                {
                    System.out.println(list.size());
                    list.remove(c);
                    System.out.println(list.size());
                    try {
                        c.getSocket().close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

                // Allow java to garbage collect old clients
                removedClients.clear();

                // Update GUI
                update_client_list_gui();

            }
        });
        t.setInitialDelay(0);
        t.start();

    }

    public void update_client_list_gui()
    {
        DefaultListModel model = (DefaultListModel)clientList.getModel();
        model.removeAllElements();
        for(Client c : this.list){
            model.addElement(c);
        }
    }

    public void addClient(Client client){
        if(this.list.size() >= maxClients)
            return;

        this.list.add(client);

        // Set client ID
        client.setID(this.list.size());

        update_client_list_gui();




    }

    public void setMaxClients(int maxClients) {
        this.maxClients = maxClients;
    }

    public ArrayList<Client> toList() {
        return this.list;
    }

    public Client findByPort(String port) {
        for (Client c: this.list)
        {
            if(c.getSocket().getPort() == Integer.parseInt(port))
                return c;
        }
        return null;

    }
}
