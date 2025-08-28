package com.pidavpn.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Button connectButton, disconnectButton;
    private TextView statusText;
    private Spinner configSpinner;
    
    private static final int VPN_REQUEST_CODE = 1;
    private boolean isConnected = false;
    
    // کانفیگ‌های شما
    private final String[] vpnConfigs = {
        "vless://d1563ff5-b87c-41a2-ab52-315a5251783b@104.24.69.25:8443?path=%2FSfCL44HuPKPyhBBf%3Fed%3D2560&security=tls&alpn=http%2F1.1&encryption=none&host=white-tooth-0914.motilew530.workers.dev&type=ws&sni=white-tooth-0914.motilew530.workers.dev#proxy"
    };
    
    private int currentConfigIndex = 0;
    private BroadcastReceiver vpnStatusReceiver;

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
        
        // ثبت receiver برای وضعیت VPN
        vpnStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String status = intent.getStringExtra("status");
                String message = intent.getStringExtra("message");
                
                if ("connected".equals(status)) {
                    isConnected = true;
                    updateUI();
                    statusText.setText("متصل شده");
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                } else if ("disconnected".equals(status)) {
                    isConnected = false;
                    updateUI();
                    statusText.setText("قطع ارتباط");
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                } else if ("error".equals(status)) {
                    statusText.setText("خطا: " + message);
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }
        };
        
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(vpnStatusReceiver, new IntentFilter("vpn_status"));
        
        updateUI();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(vpnStatusReceiver);
    }
    
    private void setupConfigSpinner() {
        String[] configNames = new String[vpnConfigs.length];
        for (int i = 0; i < vpnConfigs.length; i++) {
            configNames[i] = "کانفیگ " + (i + 1);
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, configNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        configSpinner.setAdapter(adapter);
        configSpinner.setOnItemSelectedListener(this);
    }
    
    private void connectToVpn() {
        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            // درخواست مجوز از کاربر
            startActivityForResult(intent, VPN_REQUEST_CODE);
        } else {
            // مجوز已经有了، شروع سرویس
            startVpnService();
        }
    }
    
    private void disconnectFromVpn() {
        Intent intent = new Intent(this, V2RayVpnService.class);
        intent.setAction("disconnect");
        startService(intent);
    }
    
    private void startVpnService() {
        Intent intent = new Intent(this, V2RayVpnService.class);
        intent.setAction("connect");
        startService(intent);
        statusText.setText("در حال اتصال...");
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            startVpnService();
        } else if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "اجازه VPN داده نشد", Toast.LENGTH_SHORT).show();
            statusText.setText("اجازه VPN داده نشد");
        }
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
    
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        currentConfigIndex = position;
        statusText.setText("کانفیگ انتخاب شد: " + (position + 1));
    }
    
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
