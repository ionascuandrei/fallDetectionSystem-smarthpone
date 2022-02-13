package fall.detection.app;

import static fall.detection.general.Constants.MAPVIEW_BUNDLE_KEY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;

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
    private Button serverButton;
    private Button mapButton;
    private Button springButton;
    private Button profileButton;


    // Map
    private MapView mapView;

    // Layouts
    LinearLayout mainMenuLayout;
    LinearLayout serverLayout;
    ConstraintLayout mapLayout;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set Logo
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.fall_foreground);

        // Set Layouts
        mainMenuLayout = (LinearLayout) findViewById(R.id.main_menu_layout);
        serverLayout = (LinearLayout) findViewById(R.id.server_layout);
        mapLayout = (ConstraintLayout) findViewById(R.id.map_layout);

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
        serverButton = findViewById(R.id.serverButton);
        mapButton = findViewById(R.id.mapButton);
        springButton = findViewById(R.id.springButton);
        profileButton = findViewById(R.id.profileButton);

        // Initialize display values
        if (!serverThread.isServerRunning().get()) {
            serverStatusView.setText(Constants.serverOffline);
            debugPanelView.setText(Constants.empty);
        }

        // Create the observer which updates the <<DEBUG PANEL>> in UI.
        final Observer<String> debugPanelUpdater = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String updates) {
                if (updates != null) {
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

                    case R.id.mapButton:
                        mapLayout.setVisibility(ConstraintLayout.VISIBLE);
                        serverLayout.setVisibility(LinearLayout.GONE);
                        mainMenuLayout.setVisibility(LinearLayout.GONE);
                        break;

                    case R.id.serverButton:
                        serverLayout.setVisibility(LinearLayout.VISIBLE);
                        mapLayout.setVisibility(ConstraintLayout.GONE);
                        mainMenuLayout.setVisibility(LinearLayout.GONE);
                        break;

                    case R.id.springButton:
                        // TODO
                        break;

                    case R.id.profileButton:
                        // TODO
                        break;
                }
            }
        };

        // Allocate onClick handler for buttons
        startServerButton.setOnClickListener(onClickListenerHandler);
        stopServerButton.setOnClickListener(onClickListenerHandler);
        mapButton.setOnClickListener(onClickListenerHandler);
        serverButton.setOnClickListener(onClickListenerHandler);
        profileButton.setOnClickListener(onClickListenerHandler);
        springButton.setOnClickListener(onClickListenerHandler);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map_button:
                mapLayout.setVisibility(ConstraintLayout.VISIBLE);
                serverLayout.setVisibility(LinearLayout.GONE);
                mainMenuLayout.setVisibility(LinearLayout.GONE);
                break;
            case R.id.server_button:
                serverLayout.setVisibility(LinearLayout.VISIBLE);
                mapLayout.setVisibility(ConstraintLayout.GONE);
                mainMenuLayout.setVisibility(LinearLayout.GONE);
                break;
            case R.id.main_menu_button:
                mainMenuLayout.setVisibility(LinearLayout.VISIBLE);
                mapLayout.setVisibility(ConstraintLayout.GONE);
                serverLayout.setVisibility(LinearLayout.GONE);
                break;
            case R.id.exit:
                finish();
                System.exit(0);
                break;
        }

        return super.onOptionsItemSelected(item);
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
