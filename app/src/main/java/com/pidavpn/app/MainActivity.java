package com.pidavpn.app;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class MainActivity extends AppCompatActivity {
    private Button connectButton, disconnectButton, addConfigButton;
    private TextView statusText;
    private boolean isConnected = false;
    
    // نمونه کانفیگ‌های V2Ray (می‌توانید اینها را با کانفیگ‌های واقعی خود جایگزین کنید)
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
        
        // اضافه کردن دکمه جدید برای افزودن کانفیگ
        addConfigButton = new Button(this);
        addConfigButton.setText("افزودن کانفیگ");
        addConfigButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewConfig();
            }
        });
        
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
                
                // اینجا می‌توانید Intent برای باز کردن برنامه VPN دیگر اضافه کنید
                // openVpnApp(config);
                
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
    
    private void addNewConfig() {
        // این متد می‌تواند یک Dialog برای وارد کردن کانفیگ جدید نمایش دهد
        Toast.makeText(this, "قابلیت افزودن کانفیغ به زودی اضافه خواهد شد", Toast.LENGTH_SHORT).show();
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
    
    // متد برای باز کردن برنامه‌های VPN دیگر (اختیاری)
    private void openVpnApp(JsonObject config) {
        try {
            // نمونه: باز کردن OpenVPN با config
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("content://path/to/your/config.ovpn"), "application/x-openvpn-profile");
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "برنامه VPN یافت نشد", Toast.LENGTH_SHORT).show();
        }
    }
}
