package com.pidavpn.app;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button connectButton, disconnectButton;
    private TextView statusText;
    private boolean isConnected = false;

    // لیست کانفیگ‌های از پیش تعریف شده
    private final String[] vpnConfigs = {
        "server1.example.com:1194:udp:username:password",
        "server2.example.com:443:tcp:username:password",
        "server3.example.com:1194:udp:username:password"
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
                if (!isConnected) {
                    connectToVpn();
                }
            }
        });

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    disconnectFromVpn();
                }
            }
        });
    }

    private void connectToVpn() {
        // استفاده از اولین کانفیگ
        String config = vpnConfigs[0];
        String[] parts = config.split(":");
        String server = parts[0];
        String port = parts[1];
        
        // شبیه‌سازی اتصال
        isConnected = true;
        connectButton.setEnabled(false);
        disconnectButton.setEnabled(true);
        statusText.setText("متصل به: " + server);
        
        Toast.makeText(this, "در حال اتصال به سرور...", Toast.LENGTH_SHORT).show();
    }

    private void disconnectFromVpn() {
        // شبیه‌سازی قطع اتصال
        isConnected = false;
        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);
        statusText.setText("آماده برای اتصال");
        
        Toast.makeText(this, "اتصال قطع شد", Toast.LENGTH_SHORT).show();
    }
}
