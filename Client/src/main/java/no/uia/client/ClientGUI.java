package no.uia.client;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

/**
 * Created by per on 24.02.16.
 */
public class ClientGUI extends JFrame{
    private JPanel panel1;
    private JList clientList;
    public JButton btnSend;
    public JButton btnConnect;
    public JTextPane txtChat;
    public JTextField txtInput;
    public JLabel lblConnectedTo;
    public JLabel lblPing;
    public JLabel lblMe;
    public JButton btnConnectCentral;


    public ClientGUI(){
        super("Per2Per Client");
        setPreferredSize(new Dimension(700,400));
        setAlwaysOnTop(true);
        pack();

        setContentPane(panel1);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setLocationRelativeTo(null);

        setupClientList();
        DefaultCaret caret = (DefaultCaret)txtChat.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);


        setVisible(true);

    }

    public void setupClientList(){
        DefaultListModel listmodel=new DefaultListModel();
        clientList.setModel(listmodel);
        clientList.setCellRenderer(new DefaultListCellRenderer());

    }

    public JList getClientList() {
        return clientList;
    }

}



