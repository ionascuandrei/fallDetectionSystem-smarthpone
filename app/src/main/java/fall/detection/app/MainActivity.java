package fall.detection.app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import fall.detection.general.Constants;

public class MainActivity extends AppCompatActivity {
    // Instances
    private Server serverThread;
    private MainActivity myContext;

    // Displayed components
    private TextView serverStatusView;
    private Button startServerButton;
    private Button stopServerButton;
    private TextView debugPanelView;

    // LiveData variables for UI


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Save my context
        myContext = this;

        // Initialize server instance
        serverThread = Server.instance();

        // Initialize displayed components
        serverStatusView = findViewById(R.id.serverStatusView);
        startServerButton = findViewById(R.id.startServerButton);
        stopServerButton = findViewById(R.id.stopServerButton);
        debugPanelView = findViewById(R.id.debugPanel);

        // Initialize values
        if (!serverThread.isServerRunning().get()) {
            serverStatusView.setText("No-Text");
            debugPanelView.setText("");
        }

        // Create the observer which updates the debug panel in UI.
        final Observer<String> debugPanelUpdater = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String updates) {
                debugPanelView.append(updates);
            }
        };

        // Create the observer which updates the server status in UI.
        final Observer<String> serverStatusUpdater = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String value) {
                serverStatusView.setText(value);
                if (value.equals(Constants.serverOffline)) {
                    // Create a new Thread
                    serverThread = new Server();
                    serverThread.serverStatus.observe(myContext, this);
                    serverThread.debugPanel.observe(myContext, debugPanelUpdater);
                }
            }
        };

        // Start server event listener
        serverThread.serverStatus.observe(this, serverStatusUpdater);
        serverThread.debugPanel.observe(this, debugPanelUpdater);

        startServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!serverThread.isServerRunning().get()) {
                    serverThread.startServer();
                }
            }
        });

        // Stop server event listener
        stopServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serverThread.isServerRunning().get()) {
                    serverThread.stopServer();
                }
            }
        });
    }
}
