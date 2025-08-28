package com.pidavpn.app;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Button connectButton, disconnectButton;
    private TextView statusText;
    private Spinner configSpinner;
    private boolean isConnected = false;
    
    // کانفیگ‌های واقعی شما
    private final String[] vpnConfigs = {
        // کانفیگ شماره 1 (ساده‌شده)
        "{\"name\": \"کانفیگ ۱ - yy8443\", \"protocol\": \"vless\", \"server\": \"104.21.33.70\", \"port\": 8443, \"id\": \"d1563ff5-b87c-41a2-ab52-315a5251783b\", \"host\": \"white-tooth-0914.motilew530.workers.dev\", \"path\": \"/SfCL44HuPKPyhBBf?ed=2560\", \"security\": \"tls\"}",
        
        // کانفیگ شماره 2 (از لینک VLESS)
        "{\"name\": \"کانفیگ ۲ - Ali\", \"protocol\": \"vless\", \"server\": \"104.24.72.159\", \"port\": 8443, \"id\": \"d1563ff5-b87c-41a2-ab52-315a5251783b\", \"host\": \"white-tooth-0914.motilew530.workers.dev\", \"path\": \"/SfCL44HuPKPyhBBf?ed\", \"security\": \"tls\"}",
        
        // کانفیگ شماره 3 (از لینک VLESS)
        "{\"name\": \"کانفیگ ۳ - Domain\", \"protocol\": \"vless\", \"server\": \"www.speedtest.net\", \"port\": 443, \"id\": \"d29debb4-5df3-424e-85c5-e2dffa095ec0\", \"host\": \"amoozesh-enl.pages.dev\", \"path\": \"/V0cJso6c0RXNrfEp\", \"security\": \"tls\"}"
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
        configSpinner = findViewById(R.id.configSpinner);
        
        // تنظیمات Spinner
        setupConfigSpinner();
        
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
    
    private void setupConfigSpinner() {
        // ایجاد لیست نام‌های کانفیگ‌ها
        String[] configNames = new String[vpnConfigs.length];
        for (int i = 0; i < vpnConfigs.length; i++) {
            try {
                Gson gson = new Gson();
                JsonObject config = gson.fromJson(vpnConfigs[i], JsonObject.class);
                configNames[i] = config.get("name").getAsString();
            } catch (Exception e) {
                configNames[i] = "کانفیگ " + (i + 1);
            }
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, configNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        configSpinner.setAdapter(adapter);
        configSpinner.setOnItemSelectedListener(this);
    }
    
    private void connectToVpn() {
        String configJson = vpnConfigs[currentConfigIndex];
        
        try {
            Gson gson = new Gson();
            JsonObject config = gson.fromJson(configJson, JsonObject.class);
            
            String server = config.get("server").getAsString();
            int port = config.get("port").getAsInt();
            String protocol = config.get("protocol").getAsString();
            String id = config.get("id").getAsString();
            String name = config.get("name").getAsString();
            
            // شبیه‌سازی اتصال
            isConnected = true;
            updateUI();
            
            // نمایش اطلاعات اتصال
            String statusMessage = String.format("متصل به: %s\\nسرور: %s:%d\\nپروتکل: %s\\nID: %s", 
                name, server, port, protocol, id.substring(0, 8) + "...");
            statusText.setText(statusMessage);
            
            Toast.makeText(this, "اتصال به " + name + " برقرار شد", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "خطا در پردازش کانفیگ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            configSpinner.setEnabled(false);
        } else {
            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
            configSpinner.setEnabled(true);
        }
    }
    
    // روش‌های interface OnItemSelectedListener
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        currentConfigIndex = position;
        Toast.makeText(this, "کانفیگ انتخاب شد: " + (position + 1), Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // هیچ کاری لازم نیست انجام شود
    }
}
