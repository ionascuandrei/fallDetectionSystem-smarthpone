package fall.detection.app;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
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

import static java.lang.System.out;

public class Server extends Thread {

    // Single instance
    private static Server serverInstance;

    // Variables
    private boolean isRunning;
    private final AtomicBoolean threadActive;
    private ServerSocket serverSocket;

    // Constructor
    public Server() {
        isRunning = false;
        threadActive = new AtomicBoolean(false);
    }

    public boolean isServerRunning() {
        return isRunning;
    }

    public AtomicBoolean isThreadActive () {
        return threadActive;
    }

    public static Server instance() {
        // Singleton instance
        if (serverInstance == null) {
            serverInstance = new Server();
        }
        return serverInstance;
    }

    public void startServer() {
        isRunning = true;
        if (!threadActive.get()) {
            threadActive.set(true);
            start();
            Log.v(Constants.TAG, "Server thread started!");
        }
        Log.v(Constants.TAG, "Server started!");
    }

    public void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
        Log.v(Constants.TAG, "stopServer() method invoked");
    }

    // Stop thread execution
    public void stopThread() {
        stopServer();
        threadActive.set(false);
        serverInstance = new Server();
    }

    @Override
    public void run() {
        try {
            while (threadActive.get()) {
                while (isRunning) {
                    if (serverSocket == null) {
                        Log.v(Constants.TAG, "Am repornit serverSocket");
                        serverSocket = new ServerSocket(Constants.SERVER_PORT);
                    }
                    Socket client = serverSocket.accept();
                    Log.v(Constants.TAG, "Connection opened with " + client.getInetAddress() + ":" + client.getLocalPort());

                    InputStream in = client.getInputStream();
                    Scanner s = new Scanner(in, "UTF-8");
                    try {
                        String data = s.useDelimiter("\\r\\n\\r\\n").next();
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
                            byte[] decoded = new byte[6];
                            byte[] encoded = new byte[] { (byte) 198, (byte) 131, (byte) 130, (byte) 182, (byte) 194, (byte) 135 };
                            byte[] key = new byte[] { (byte) 167, (byte) 225, (byte) 225, (byte) 210 };
                            for (int i = 0; i < encoded.length; i++) {
                                decoded[i] = (byte) (encoded[i] ^ key[i & 0x3]);
                            }
                        }
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } finally {
                        s.close();
                    }
                    PrintWriter printWriter = Utilities.getWriter(client);
                    printWriter.println("Serverul te saluta!");
                    client.close();
                    Log.v(Constants.TAG, "Connection closed");
                }
            }
        } catch (SocketException socketException) {
            // It is normal if you close the server while socketServer waits for connection in .accept()
            Log.e(Constants.TAG, "Server closed while waiting for socket connection [" + socketException.getMessage() + "]");
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }
}






//package fall.detection.app;
//
//import android.util.Log;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.net.SocketException;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//import fall.detection.general.Constants;
//import fall.detection.general.Utilities;
//
//public class Server extends Thread {
//
//    // Single instance
//    private static Server serverInstance;
//
//    // Variables
//    private boolean isRunning;
//    private final AtomicBoolean threadActive;
//    private ServerSocket serverSocket;
//
//    // Constructor
//    public Server() {
//        isRunning = false;
//        threadActive = new AtomicBoolean(false);
//    }
//
//    public boolean isServerRunning() {
//        return isRunning;
//    }
//
//    public AtomicBoolean isThreadActive () {
//        return threadActive;
//    }
//
//    public static Server instance() {
//        // Singleton instance
//        if (serverInstance == null) {
//            serverInstance = new Server();
//        }
//        return serverInstance;
//    }
//
//    public void startServer() {
//        isRunning = true;
//        if (!threadActive.get()) {
//            threadActive.set(true);
//            start();
//            Log.v(Constants.TAG, "Server thread started!");
//        }
//        Log.v(Constants.TAG, "Server started!");
//    }
//
//    public void stopServer() {
//        isRunning = false;
//        try {
//            if (serverSocket != null) {
//                serverSocket.close();
//                serverSocket = null;
//            }
//        } catch (IOException ioException) {
//            Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
//            if (Constants.DEBUG) {
//                ioException.printStackTrace();
//            }
//        }
//        Log.v(Constants.TAG, "stopServer() method invoked");
//    }
//
//    // Stop thread execution
//    public void stopThread() {
//        stopServer();
//        threadActive.set(false);
//        serverInstance = new Server();
//    }
//
//    @Override
//    public void run() {
//        try {
//            while (threadActive.get()) {
//                while (isRunning) {
//                    if (serverSocket == null) {
//                        Log.v(Constants.TAG, "Am repornit serverSocket");
//                        serverSocket = new ServerSocket(Constants.SERVER_PORT);
//                    }
//                    Socket socket = serverSocket.accept();
//                    Log.v(Constants.TAG, "Connection opened with " + socket.getInetAddress() + ":" + socket.getLocalPort());
//
//                    PrintWriter printWriter = Utilities.getWriter(socket);
//                    printWriter.println("Serverul te saluta!");
//                    socket.close();
//                    Log.v(Constants.TAG, "Connection closed");
//                }
//            }
//        } catch (SocketException socketException) {
//            // It is normal if you close the server while socketServer waits for connection in .accept()
//            Log.e(Constants.TAG, "Server closed while waiting for socket connection [" + socketException.getMessage() + "]");
//        } catch (IOException ioException) {
//            Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
//            if (Constants.DEBUG) {
//                ioException.printStackTrace();
//            }
//        }
//    }
//}
//
