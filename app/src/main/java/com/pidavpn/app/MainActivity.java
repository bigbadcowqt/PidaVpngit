package com.pidavpn.app;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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
    private V2RayVpnService vpnService;
    private boolean isBound = false;
    
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
    
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            V2RayVpnService.LocalBinder binder = (V2RayVpnService.LocalBinder) service;
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
        configSpinner = findViewById(R.id.configSpinner);
        
        // تنظیمات Spinner
        setupConfigSpinner();
        
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
        Intent intent = new Intent(this, V2RayVpnService.class);
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
        if (isBound && vpnService != null && !vpnService.isRunning()) {
            String configJson = vpnConfigs[currentConfigIndex];
            
            try {
                Gson gson = new Gson();
                JsonObject config = gson.fromJson(configJson, JsonObject.class);
                
                String name = config.get("name").getAsString();
                
                Intent intent = new Intent(this, V2RayVpnService.class);
                intent.setAction("connect");
                intent.putExtra("config", configJson);
                startService(intent);
                
                statusText.setText("در حال اتصال به: " + name);
                Toast.makeText(this, "در حال اتصال به " + name, Toast.LENGTH_SHORT).show();
                
            } catch (Exception e) {
                Toast.makeText(this, "خطا در پردازش کانفیگ", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void disconnectFromVpn() {
        if (isBound && vpnService != null && vpnService.isRunning()) {
            Intent intent = new Intent(this, V2RayVpnService.class);
            intent.setAction("disconnect");
            startService(intent);
            
            statusText.setText("آماده برای اتصال");
            Toast.makeText(this, "در حال قطع اتصال", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateUI() {
        if (isBound && vpnService != null) {
            if (vpnService.isRunning()) {
                connectButton.setEnabled(false);
                disconnectButton.setEnabled(true);
                configSpinner.setEnabled(false);
                statusText.setText("متصل شده");
            } else {
                connectButton.setEnabled(true);
                disconnectButton.setEnabled(false);
                configSpinner.setEnabled(true);
                statusText.setText("آماده برای اتصال");
            }
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
