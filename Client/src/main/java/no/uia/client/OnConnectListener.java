package no.uia.client;

import java.net.Socket;

/**
 * Created by Per-Arne on 24.02.2016.
 */
public interface OnConnectListener {

    public void onConnected(Socket s);

}
