package fall.detection.app;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import fall.detection.general.Constants;
import fall.detection.server.WSServer;


import static fall.detection.general.Constants.SERVER;

public class Server extends Thread {

    // Single instance
    private static Server serverInstance;

    // Thread variables
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    String debugData;

    // LiveData variables for UI
    MutableLiveData<String> debugPanel = new MutableLiveData<>();
    MutableLiveData<String> serverStatus = new MutableLiveData<>();

    // Connection variables
    private WSServer webSocketServer;

    // Constructor
    public Server() {
        serverInstance = this;
        debugData = "";
    }

    public static Server instance() {
        // Singleton instance
        if (serverInstance == null) {
            serverInstance = new Server();
        }
        return serverInstance;
    }

    public static boolean isServerInstance() {
        return serverInstance != null;
    }

    public AtomicBoolean isServerRunning() {
        return isRunning;
    }

    public void startServer() {
        if (!isRunning.get()) {
            serverStatus.postValue(Constants.serverOnline);

            isRunning.set(true);
            start();
        }
    }

    public void stopServer() {
        try {
            webSocketServer.stop();
            isRunning.set(false);
            serverStatus.setValue(Constants.serverOffline);
            // Debug
            if (Constants.DEBUG) {
                debugPanel.postValue("Server stopped!\n");
                Log.i(SERVER, "Server stopped!");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // Start the server
            webSocketServer = new WSServer(Constants.SERVER_PORT, debugPanel, serverStatus);
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
