package fall.detection.server;

import android.content.res.AssetManager;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import fall.detection.classifier.DataClassifier;
import fall.detection.general.Constants;

public class WSServer extends WebSocketServer {

    private MutableLiveData<String> debugPanel;
    public WebSocket clientSocket = null;
    // Asset Manager for assets folder
    private AssetManager assetManager;

    public WSServer(int port, MutableLiveData<String> debugPanel, MutableLiveData<String> serverStatus, AssetManager assetManager) throws UnknownHostException {
        super( new InetSocketAddress( port ) );
        this.debugPanel = debugPanel;
        this.assetManager = assetManager;
    }

    public WSServer(int port) {
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

        // Save client connection
        clientSocket = conn;
        // Send a message to the new client
        JSONObject message = new JSONObject();
        try {
            message.put("title", "Welcome to Andrei's server!");
            conn.send(message.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
        try {
            // Create JSON form received String message
            JSONObject messageJson = new JSONObject(message);
            // Parse the title
            String messageTitle = messageJson.getString("title");

            // Received accelerometer batch
            if (messageTitle.equals("accBatch")) {
                // Parse received JSON
                parseJson(messageJson);
            } else {
                // Debug
                if (Constants.DEBUG) {
                    debugPanel.postValue("[" + conn.getRemoteSocketAddress().getAddress().getHostAddress() + "] sent:\n"+ message +"\n");
                    Log.i(Constants.MESSAGE, "[" + conn.getRemoteSocketAddress().getAddress().getHostAddress() + "] sent:\n"+ message);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onMessage( WebSocket conn, ByteBuffer message ) {

        // Debug
        if (Constants.DEBUG) {
            debugPanel.postValue("[" + conn.getRemoteSocketAddress().getAddress().getHostAddress() + "] sent:\n"+ message.toString() +"\n");
            Log.i(Constants.MESSAGE, "[" + conn.getRemoteSocketAddress().getAddress().getHostAddress() + "] sent:\n"+ message.toString());
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

    private void parseJson(JSONObject messageJson) {
        try {
            // Array for X values
            JSONArray jsonArray = messageJson.getJSONArray("xArray");
            ArrayList<Double> xArray = new ArrayList<>(jsonArray.length());
            // Extract numbers from JSON array.
            for (int i = 0; i < jsonArray.length(); i++) {
                xArray.add(i, jsonArray.getDouble(i));
            }
            // Debug
            System.out.println("xArray[" + xArray.size() + "] =" + xArray);

            // Array for Y values
            jsonArray = messageJson.getJSONArray("yArray");
            ArrayList<Double> yArray = new ArrayList<>(jsonArray.length());
            // Extract numbers from JSON array.
            for (int i = 0; i < jsonArray.length(); i++) {
                yArray.add(i, jsonArray.getDouble(i));
            }
            // Debug
            System.out.println("yArray[" + yArray.size() + "] =" + yArray);

            // Array for Z values
            jsonArray = messageJson.getJSONArray("zArray");
            ArrayList<Double> zArray = new ArrayList<>(jsonArray.length());
            // Extract numbers from JSON array.
            for (int i = 0; i < jsonArray.length(); i++) {
                zArray.add(i, jsonArray.getDouble(i));
            }
            // Debug
            System.out.println("zArray[" + zArray.size() + "] =" + zArray);

            // DEBUG
            if (Constants.DEBUG) {
                debugPanel.postValue("[WSS] Accelerometer JSON parsed and sent to classification!\n");
                Log.i(Constants.WSS,  "Accelerometer JSON parsed and sent to classification!");
            }

            // Start classification of given data
            String classificationResult = DataClassifier.classifyData(xArray, yArray, zArray, assetManager);
            // Sent back to the client the result
            if (classificationResult.equals("FALL") || classificationResult.equals("ADL") ) {
                // Debug
                if (Constants.DEBUG) {
                    debugPanel.postValue("Classification result = " + classificationResult + "\n");
                    Log.i(Constants.WSS, "Classification result = " + classificationResult);
                }

                // Send a message to the client
                JSONObject message = new JSONObject();
                try {
                    if (clientSocket.isOpen()) {
                        message.put("title", "classificationResult");
                        message.put("result", classificationResult);
                        clientSocket.send(message.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void main( String[] args ) throws InterruptedException , IOException {
//        int port = 8887; // 843 flash policy port
//        try {
//            port = Integer.parseInt( args[ 0 ] );
//        } catch ( Exception ex ) {
//        }
//
//        WSServer s = new WSServer( port );
//        s.start();
//        Log.i(Constants.SERVER, "WebSocketServer started on port: " + s.getPort() );
//
//        BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
//        while ( true ) {
//            String in = sysin.readLine();
//            s.broadcast( in );
//            if( in.equals( "exit" ) ) {
//                s.stop(1000);
//                break;
//            }
//        }
    }
}