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

        // Initialize values
        if (serverThread.isServerRunning().get()) {
            serverStatusView.setText(Constants.Active);
        } else {
            serverStatusView.setText(Constants.Closed);
        }

        // Start server event listener
        startServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!serverThread.isServerRunning().get()) {
                    serverThread.startServer();
                    serverStatusView.setText(new String("Server online"));
                }
            }
        });

        // Stop server event listener
        stopServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serverThread.isServerRunning().get()) {
                    serverThread.stopServer();
                    serverStatusView.setText(new String("Server offline"));
                }
            }
        });
    }
}
