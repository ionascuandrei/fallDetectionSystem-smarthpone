package fall.detection.server;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import fall.detection.general.Constants;

public class WSServer extends WebSocketServer {

    private MutableLiveData<String> debugPanel;

    public WSServer(int port, MutableLiveData<String> debugPanel, MutableLiveData<String> serverStatus) throws UnknownHostException {
        super( new InetSocketAddress( port ) );
        this.debugPanel = debugPanel;
    }

    public WSServer(int port) throws UnknownHostException {
        super( new InetSocketAddress( port ) );
    }

    public WSServer(InetSocketAddress address ) {
        super( address );
    }

    @Override
    public void onOpen( WebSocket conn, ClientHandshake handshake ) {
        // Debug
        if (Constants.DEBUG) {
            debugPanel.postValue("Client [ "+ conn.getRemoteSocketAddress().getAddress().getHostAddress()+  " ] connected\n");
            Log.i(Constants.WSS, "Client [ "+ conn.getRemoteSocketAddress().getAddress().getHostAddress()+  " ] connected");
        }

        // Send a message to the new client
        conn.send("Welcome to Andrei's server!");
    }

    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
        // Debug
        if (Constants.DEBUG) {
            debugPanel.postValue("Client [ " + conn.getRemoteSocketAddress().getAddress().getHostAddress() + " ] has left the room!\n");
            Log.i(Constants.WSS, "Client [ " + conn.getRemoteSocketAddress().getAddress().getHostAddress() + " ] has left the room!");
        }
    }

    @Override
    public void onMessage( WebSocket conn, String message ) {
        // Debug
        if (Constants.DEBUG) {
            debugPanel.postValue("[" + conn.getRemoteSocketAddress().getAddress().getHostAddress() + "] sent:\n"+ message +"\n");
            Log.i(Constants.MESSAGE, "[" + conn.getRemoteSocketAddress().getAddress().getHostAddress() + "] sent:\n"+ message);
        }
    }
    @Override
    public void onMessage( WebSocket conn, ByteBuffer message ) {
        // Debug
        if (Constants.DEBUG) {
            debugPanel.postValue("[" + conn.getRemoteSocketAddress().getAddress().getHostAddress() + "] sent:\n"+ message +"\n");
            Log.i(Constants.MESSAGE, "[" + conn.getRemoteSocketAddress().getAddress().getHostAddress() + "] sent:\n"+ message);
        }
    }

    @Override
    public void onError( WebSocket conn, Exception ex ) {
        // Debug
        if (Constants.DEBUG) {
            debugPanel.postValue("[WSS] OnError:\n"+ ex +"\n");
            Log.e(Constants.WSS, ex.toString());
        }

        if( conn != null ) {
            // some errors like port binding failed may not be assignable to a specific websocket
            debugPanel.postValue("Some errors like port binding failed may not be assignable to a specific websocket\n");
        }
    }

    @Override
    public void onStart() {
        // Debug
        if (Constants.DEBUG) {
            debugPanel.postValue("[WSS] Server started!\n");
            Log.i(Constants.WSS, "Server started!");
        }
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    public static void main( String[] args ) throws InterruptedException , IOException {
        int port = 8887; // 843 flash policy port
        try {
            port = Integer.parseInt( args[ 0 ] );
        } catch ( Exception ex ) {
        }

        WSServer s = new WSServer( port );
        s.start();
        Log.i(Constants.SERVER, "WebSocketServer started on port: " + s.getPort() );
//        System.out.println( "ChatServer started on port: " + s.getPort() );

        BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
        while ( true ) {
            String in = sysin.readLine();
            s.broadcast( in );
            if( in.equals( "exit" ) ) {
                s.stop(1000);
                break;
            }
        }
    }
}