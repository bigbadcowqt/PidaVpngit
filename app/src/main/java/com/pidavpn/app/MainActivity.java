package com.pidavpn.app;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class MainActivity extends AppCompatActivity {
    private Button connectButton, disconnectButton;
    private TextView statusText;
    private boolean isConnected = false;
    
    // نمونه کانفیگ‌های V2Ray (موقت - بعداً با کانفیگ‌های واقعی جایگزین می‌شود)
    private final String[] vpnConfigs = {
        "{\"protocol\": \"vmess\", \"server\": \"server1.example.com\", \"port\": 443, \"id\": \"your-uuid-here\"}",
        "{\"protocol\": \"vless\", \"server\": \"server2.example.com\", \"port\": 80, \"id\": \"your-uuid-here\"}",
        "{\"protocol\": \"trojan\", \"server\": \"server3.example.com\", \"port\": 443, \"password\": \"your-password\"}"
    };
    
    private int currentConfigIndex = 0;

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
        
        updateUI();
    }
    
    private void connectToVpn() {
        if (currentConfigIndex < vpnConfigs.length) {
            String configJson = vpnConfigs[currentConfigIndex];
            
            try {
                Gson gson = new Gson();
                JsonObject config = gson.fromJson(configJson, JsonObject.class);
                
                String server = config.get("server").getAsString();
                int port = config.get("port").getAsInt();
                String protocol = config.get("protocol").getAsString();
                
                // شبیه‌سازی اتصال
                isConnected = true;
                updateUI();
                
                // نمایش اطلاعات اتصال
                statusText.setText("متصل به: " + server + ":" + port + " (" + protocol + ")");
                Toast.makeText(this, "اتصال برقرار شد", Toast.LENGTH_SHORT).show();
                
            } catch (Exception e) {
                Toast.makeText(this, "خطا در پردازش کانفیگ", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "لطفاً ابتدا کانفیگ اضافه کنید", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void disconnectFromVpn() {
        isConnected = false;
        updateUI();
        statusText.setText("آماده برای اتصال");
        Toast.makeText(this, "اتصال قطع شد", Toast.LENGTH_SHORT).show();
    }
    
    private void updateUI() {
        if (isConnected) {
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
        } else {
            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
        }
    }
}
