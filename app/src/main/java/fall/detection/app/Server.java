package fall.detection.app;

import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fall.detection.general.Constants;
import fall.detection.general.Utilities;

public class Server extends Thread {

    // Single instance
    private static Server serverInstance;

    // Server variables
    private final AtomicBoolean isRunning;
    private ServerSocket serverSocket;

    // Connection variables

    // Constructor
    public Server() {
        isRunning = new AtomicBoolean(false);
    }

    public AtomicBoolean isServerRunning() {
        return isRunning;
    }

    public static Server instance() {
        // Singleton instance
        if (serverInstance == null) {
            serverInstance = new Server();
        }
        return serverInstance;
    }

    public void startServer() {
        if (!isRunning.get()) {
            isRunning.set(true);
            start();
            Log.i(Constants.TAG, "Server started!");
        }
    }

    public void stopServer() {
        isRunning.set(false);
        serverInstance = new Server();
        try {
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred on stopServer(): " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
        Log.i(Constants.TAG, "Server stopped!");
    }

    private void upgradeToWebSocket(InputStream in, OutputStream out) {
        Scanner s = new Scanner(in, "UTF-8");
        try {
            String data = s.useDelimiter("\\r\\n\\r\\n").next();
            Log.v(Constants.TAG, "Message from client:\n" + data);
            Log.v(Constants.TAG, "\n============================\n");
            Matcher get = Pattern.compile("^GET").matcher(data);
            if (get.find()) {
                Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                match.find();
                byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                        + "Connection: Upgrade\r\n"
                        + "Upgrade: websocket\r\n"
                        + "Sec-WebSocket-Accept: "
                        + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
                        + "\r\n\r\n").getBytes("UTF-8");
                out.write(response, 0, response.length);
                Log.i(Constants.TAG, "Handshake complete!");
            }
        } catch (NoSuchAlgorithmException | IOException exception) {
            Log.e(Constants.TAG, "An exception has occurred on upgradeToWebSocket(): " + exception.getMessage());
            if (Constants.DEBUG) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            while (isRunning.get()) {
                // While server is ACTIVE
                if (serverSocket == null) {
                    // The server was activated now
                    Log.i(Constants.TAG, "ServerSocket initialized!");
                    serverSocket = new ServerSocket(Constants.SERVER_PORT);
                }
                // Waiting for connection
                Socket client = serverSocket.accept();
                Log.i(Constants.TAG, "Connection opened with " + client.getInetAddress() + ":" + client.getLocalPort());
                // Get input/output streams with client
                InputStream in = client.getInputStream();
                OutputStream out = client.getOutputStream();
                // Upgrade connection to WebSocket
                upgradeToWebSocket(in, out);

                // Communicate with the client
                while (!serverSocket.isClosed()) {
//                  byte[] decoded = new byte[6];
//                  byte[] encoded = new byte[] { (byte) 198, (byte) 131, (byte) 130, (byte) 182, (byte) 194, (byte) 135 };
//                  byte[] key = new byte[] { (byte) 167, (byte) 225, (byte) 225, (byte) 210 };
//                  for (int i = 0; i < encoded.length; i++) {
//                      decoded[i] = (byte) (encoded[i] ^ key[i & 0x3]);
//                  }

//                  PrintWriter printWriter = Utilities.getWriter(client);
//                  printWriter.println("Serverul te saluta!");
//                  client.close();
//                  Log.v(Constants.TAG, "Connection closed");
                }
            }
        } catch (SocketException socketException) {
            // It is normal if you close the server while socketServer waits for connection in .accept()
            isRunning.set(false);
            Log.i(Constants.TAG, "Server closed while waiting for socket connection [" + socketException.getMessage() + "]");
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }
}

