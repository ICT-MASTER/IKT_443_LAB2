package no.uia.server;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by per on 24.02.16.
 */
public class ServerGUI extends JFrame{
    private JList clientList;
    private JPanel rootPanel;
    private JPanel imagePane;
    private JLabel serverAddress;
    private JLabel lblImage;

    public ServerGUI(){
        super("Per2Per Server");
        setPreferredSize(new Dimension(700,400));
        pack();

        setContentPane(rootPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setupClientList();
        setupImagePane();


        setVisible(true);

    }

    public void setupImagePane()
    {
        URL url = null;
        try {
            url = new URL("https://scontent-arn2-1.xx.fbcdn.net/hphotos-xft1/v/t1.0-0/p296x100/12743603_1120373211308073_4059319791372263559_n.jpg?oh=e9d38eeffab6f9afa528dddb6b06a534&oe=5753DEEB");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        java.awt.Image image = java.awt.Toolkit.getDefaultToolkit().createImage(url);
        lblImage.setIcon(new ImageIcon(image));
        lblImage.setText("");


    }

    public void setupClientList(){
        DefaultListModel listmodel=new DefaultListModel();
        clientList.setModel(listmodel);
        clientList.setCellRenderer(new DefaultListCellRenderer());

    }

    public JList getClientList() {
        return clientList;
    }

    public JLabel getServerAddress() {
        return serverAddress;
    }
}
