package fall.detection.app;

import android.content.res.AssetManager;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import fall.detection.general.Constants;
import fall.detection.server.WSServer;

import static fall.detection.general.Constants.SERVER;

public class Server extends Thread {

    // Single instance
    private static Server serverInstance;

    // Classifier model file
    private static AssetManager assetManager;

    // Thread variables
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    String debugData;

    // LiveData variables for UI
    MutableLiveData<String> debugPanel = new MutableLiveData<>();
    MutableLiveData<String> serverStatus = new MutableLiveData<>();

    // Connection variables
    private WSServer webSocketServer;

    // Constructor
    Server(AssetManager assetManager) {
        serverInstance = this;
        debugData = "";
        Server.assetManager = assetManager;
    }

    static Server instance(AssetManager assetManager) {
        // Singleton instance
        if (serverInstance == null) {
            serverInstance = new Server(assetManager);
        }
        return serverInstance;
    }

    static boolean isServerInstance() {
        return serverInstance != null;
    }

    AtomicBoolean isServerRunning() {
        return isRunning;
    }

    void startServer() {
        if (!isRunning.get()) {
            serverStatus.postValue(Constants.serverOnline);

            isRunning.set(true);
            start();
        }
    }

    void stopServer() {
        try {
            if (webSocketServer.clientSocket != null && webSocketServer.clientSocket.isOpen()) {
                JSONObject message = new JSONObject();
                message.put("title", "serverClosed");
                webSocketServer.clientSocket.send(message.toString());
            }
            webSocketServer.stop();
            isRunning.set(false);
            serverStatus.setValue(Constants.serverOffline);
            // Debug
            if (Constants.DEBUG) {
                debugPanel.postValue("Server stopped!\n");
                Log.i(SERVER, "Server stopped!");
            }
        } catch (IOException | InterruptedException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // Start the server
            webSocketServer = new WSServer(Constants.SERVER_PORT, debugPanel, serverStatus, assetManager);
            webSocketServer.start();
            while (isRunning.get()) {
                // Handle messages
            }
        } catch (IOException ioException) {
            // Debug info
            if (Constants.DEBUG) {
                debugPanel.postValue("An exception has occurred: \n" + ioException.getMessage() + "\n");
                Log.e(SERVER, "An exception has occurred: " + ioException.getMessage());
                ioException.printStackTrace();
            }
        }
    }
}
