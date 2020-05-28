package fall.detection.app;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import fall.detection.general.Constants;

public class RootActivity extends AppCompatActivity {
    // Instances
    private Server serverThread;
    private RootActivity uiContext;
    // Asset manager for assets folder
    private AssetManager assetManager;

    // Displayed components
    private TextView serverStatusView;
    private TextView debugPanelView;
    private Button startServerButton;
    private Button stopServerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Save my context
        uiContext = this;

        // Initialize AssetManager
        assetManager = getAssets();

        // Initialize server instance
        serverThread = Server.instance(assetManager);

        // Initialize displayed components
        serverStatusView = findViewById(R.id.serverStatusView);
        startServerButton = findViewById(R.id.startServerButton);
        stopServerButton = findViewById(R.id.stopServerButton);
        debugPanelView = findViewById(R.id.debugPanel);

        // Initialize display values
        if (!serverThread.isServerRunning().get()) {
            serverStatusView.setText(Constants.serverOffline);
            debugPanelView.setText(Constants.empty);
        }

        // Create the observer which updates the <<DEBUG PANEL>> in UI.
        final Observer<String> debugPanelUpdater = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String updates) {
                if(updates != null) {
                    debugPanelView.append(updates);
                }
            }
        };

        // Create the observer which updates the <<SERVER STATUS>> in UI.
        final Observer<String> serverStatusUpdater = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String value) {
                if (value != null) {
                    serverStatusView.setText(value);
                    if (value.equals(Constants.serverOffline)) {
                        // Create a new Thread
                        serverThread = new Server(assetManager);
                        serverThread.serverStatus.observe(uiContext, this);
                        serverThread.debugPanel.observe(uiContext, debugPanelUpdater);
                    }
                }
            }
        };

        // Start server event listener
        serverThread.serverStatus.observe(this, serverStatusUpdater);
        serverThread.debugPanel.observe(this, debugPanelUpdater);

        // OnClickListener Handler
        View.OnClickListener onClickListenerHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.startServerButton:
                        if (!serverThread.isServerRunning().get()) {
                            serverThread.startServer();
                        }
                        break;

                    case R.id.stopServerButton:
                        if (serverThread.isServerRunning().get()) {
                            serverThread.stopServer();
                        }
                        break;
                }
            }
        };

        // Allocate onClick handler for buttons
        startServerButton.setOnClickListener(onClickListenerHandler);
        stopServerButton.setOnClickListener(onClickListenerHandler);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (serverThread.isServerRunning().get()) {
            serverThread.debugData = debugPanelView.getText().toString();
        }

        outState.putString(Constants.debugDataTag, debugPanelView.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String oldData = savedInstanceState.getString(Constants.debugDataTag);

        if (Server.isServerInstance()) {
            if (serverThread.debugData.length() > oldData.length()) {
                debugPanelView.setText(serverThread.debugData);
                if (serverThread.isServerRunning().get()) {
                    serverStatusView.setText(Constants.serverOnline);
                } else {
                    serverStatusView.setText(Constants.serverOffline);
                }
            } else {
                serverThread.debugData = oldData;
                debugPanelView.setText(serverThread.debugData);
            }
        } else {
            Log.e("EROARE", "AJUNG SI AIIIIIIIICI LA RESTORE INSTANCE");
            debugPanelView.append("[ERROR] AJUNG SI AIIIIIIIICI LA RESTORE INSTANCE\n");
        }
    }
}
