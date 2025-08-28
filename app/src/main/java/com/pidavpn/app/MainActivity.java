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
    
    // کانفیگ‌های واقعی شما
    private final String[] vpnConfigs = {
        // کانفیگ شماره 1 (ساده‌شده)
        "{\"protocol\": \"vless\", \"server\": \"104.21.33.70\", \"port\": 8443, \"id\": \"d1563ff5-b87c-41a2-ab52-315a5251783b\", \"host\": \"white-tooth-0914.motilew530.workers.dev\", \"path\": \"/SfCL44HuPKPyhBBf?ed=2560\", \"security\": \"tls\"}",
        
        // کانفیگ شماره 2 (از لینک VLESS)
        "{\"protocol\": \"vless\", \"server\": \"104.24.72.159\", \"port\": 8443, \"id\": \"d1563ff5-b87c-41a2-ab52-315a5251783b\", \"host\": \"white-tooth-0914.motilew530.workers.dev\", \"path\": \"/SfCL44HuPKPyhBBf?ed\", \"security\": \"tls\"}",
        
        // کانفیگ شماره 3 (از لینک VLESS)
        "{\"protocol\": \"vless\", \"server\": \"www.speedtest.net\", \"port\": 443, \"id\": \"d29debb4-5df3-424e-85c5-e2dffa095ec0\", \"host\": \"amoozesh-enl.pages.dev\", \"path\": \"/V0cJso6c0RXNrfEp\", \"security\": \"tls\"}"
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
                String id = config.get("id").getAsString();
                
                // شبیه‌سازی اتصال
                isConnected = true;
                updateUI();
                
                // نمایش اطلاعات اتصال
                String statusMessage = String.format("متصل به: %s:%d\\nپروتکل: %s\\nID: %s", 
                    server, port, protocol, id.substring(0, 8) + "...");
                statusText.setText(statusMessage);
                
                Toast.makeText(this, "اتصال برقرار شد", Toast.LENGTH_SHORT).show();
                
            } catch (Exception e) {
                Toast.makeText(this, "خطا در پردازش کانفیگ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
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
