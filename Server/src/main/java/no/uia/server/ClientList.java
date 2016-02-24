package no.uia.server;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Created by per on 24.02.16.
 */
public class ClientList {


    private ArrayList<Client> list;

    private int maxClients;

    public ClientList(){
        list = new ArrayList<Client>();

    }

    public void addClient(Client client, JList clientList){
        if(this.list.size() >= maxClients)
            return;

        this.list.add(client);

        // Set client ID
        client.setID(this.list.size());

        DefaultListModel model = (DefaultListModel)clientList.getModel();
        model.removeAllElements();
        for(Client c : this.list){
            model.addElement(c);
        }


    }

    public void setMaxClients(int maxClients) {
        this.maxClients = maxClients;
    }

    public ArrayList<Client> toList() {
        return this.list;
    }
}
