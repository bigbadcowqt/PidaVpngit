package com.pidavpn.app;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Button connectButton, disconnectButton;
    private TextView statusText;
    private VpnService vpnService;
    private boolean isBound = false;
    
    // کانفیگ‌های از پیش تعریف شده (مقادیر نمونه)
    private final String[] vpnConfigs = {
        "{\"protocol\": \"vmess\", \"server\": \"server1.example.com\", \"port\": 443, \"username\": \"user1\", \"password\": \"pass1\"}",
        "{\"protocol\": \"vless\", \"server\": \"server2.example.com\", \"port\": 80, \"username\": \"user2\", \"password\": \"pass2\"}",
        "{\"protocol\": \"vmess\", \"server\": \"server3.example.com\", \"port\": 443, \"username\": \"user3\", \"password\": \"pass3\"}"
    };
    
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            VpnService.LocalBinder binder = (VpnService.LocalBinder) service;
            vpnService = binder.getService();
            isBound = true;
            updateUI();
        }
        
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize views
        connectButton = findViewById(R.id.connectButton);
        disconnectButton = findViewById(R.id.disconnectButton);
        statusText = findViewById(R.id.statusText);
        
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToVpn();
            }
        });
        
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectFromVpn();
            }
        });
        
        // Start and bind to the VPN service
        Intent intent = new Intent(this, VpnService.class);
        startService(intent);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }
    
    private void connectToVpn() {
        if (isBound && vpnService != null && !vpnService.isRunning()) {
            // استفاده از اولین کانفیگ
            String config = vpnConfigs[0];
            
            Intent intent = new Intent(this, VpnService.class);
            intent.setAction("connect");
            intent.putExtra("config", config);
            startService(intent);
            
            Toast.makeText(this, "در حال اتصال...", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void disconnectFromVpn() {
        if (isBound && vpnService != null && vpnService.isRunning()) {
            Intent intent = new Intent(this, VpnService.class);
            intent.setAction("disconnect");
            startService(intent);
            
            Toast.makeText(this, "در حال قطع اتصال...", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateUI() {
        if (isBound && vpnService != null) {
            if (vpnService.isRunning()) {
                connectButton.setEnabled(false);
                disconnectButton.setEnabled(true);
                statusText.setText("متصل شده");
            } else {
                connectButton.setEnabled(true);
                disconnectButton.setEnabled(false);
                statusText.setText("آماده برای اتصال");
            }
        }
    }
}
