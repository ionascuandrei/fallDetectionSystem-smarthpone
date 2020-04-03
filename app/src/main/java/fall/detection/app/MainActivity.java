package fall.detection.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import fall.detection.general.Constants;

public class MainActivity extends AppCompatActivity {
    // Instances
    private Server serverThread;

    // Displayed components
    private TextView serverStatusView;
    private Button startServerButton;
    private Button stopServerButton;
    private Button stopThreadButton;
    private TextView threadStatusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize server instance
        serverThread = Server.instance();

        // Initialize displayed components
        serverStatusView = findViewById(R.id.serverStatusView);
        startServerButton = findViewById(R.id.startServerButton);
        stopServerButton = findViewById(R.id.stopServerButton);
        stopThreadButton = findViewById(R.id.stopThread);
        threadStatusView = findViewById(R.id.threadView);

        // Initialize values
        serverStatusView.setText(Constants.serverStatus);
        if (serverThread.isThreadActive().get()) {
            threadStatusView.setText(Constants.Active);
        } else {
            threadStatusView.setText(Constants.Closed);
        }

        // Start server event listener
        startServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!serverThread.isServerRunning()) {
                    serverThread.startServer();
                    if (serverThread.isThreadActive().get()) {
                        threadStatusView.setText(Constants.Active);
                    }
                    serverStatusView.setText(new String("Server online"));
                }
            }
        });

        // Stop server event listener
        stopServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serverThread.isServerRunning()) {
                    serverStatusView.setText(new String("Server offline"));
                    serverThread.stopServer();
                }
            }
        });

        // Stop thread event listener
        stopThreadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serverThread.isThreadActive().get()) {
                    serverThread.stopThread();
                    threadStatusView.setText(Constants.Closed);
                    serverStatusView.setText(new String("Server offline"));
                    serverThread = Server.instance();
                }
            }
        });
    }
}
