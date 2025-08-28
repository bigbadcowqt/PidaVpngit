package com.pidavpn.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Button connectButton, exportButton;
    private TextView statusText;
    private Spinner configSpinner;
    
    // کانفیگ‌های واقعی شما
    private final String[] vpnConfigs = {
        // کانفیگ شماره 1
        "{\"name\": \"کانفیگ ۱ - yy8443\", \"protocol\": \"vless\", \"server\": \"104.21.33.70\", \"port\": 8443, \"id\": \"d1563ff5-b87c-41a2-ab52-315a5251783b\", \"host\": \"white-tooth-0914.motilew530.workers.dev\", \"path\": \"/SfCL44HuPKPyhBBf?ed=2560\", \"security\": \"tls\"}",
        
        // کانفیگ شماره 2
        "{\"name\": \"کانفیگ ۲ - Ali\", \"protocol\": \"vless\", \"server\": \"104.24.72.159\", \"port\": 8443, \"id\": \"d1563ff5-b87c-41a2-ab52-315a5251783b\", \"host\": \"white-tooth-0914.motilew530.workers.dev\", \"path\": \"/SfCL44HuPKPyhBBf?ed\", \"security\": \"tls\"}",
        
        // کانفیگ شماره 3
        "{\"name\": \"کانفیگ ۳ - Domain\", \"protocol\": \"vless\", \"server\": \"www.speedtest.net\", \"port\": 443, \"id\": \"d29debb4-5df3-424e-85c5-e2dffa095ec0\", \"host\": \"amoozesh-enl.pages.dev\", \"path\": \"/V0cJso6c0RXNrfEp\", \"security\": \"tls\"}"
    };
    
    private int currentConfigIndex = 0;
    private static final int STORAGE_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize views
        connectButton = findViewById(R.id.connectButton);
        exportButton = findViewById(R.id.disconnectButton);
        statusText = findViewById(R.id.statusText);
        configSpinner = findViewById(R.id.configSpinner);
        
        // تغییر متن دکمه
        exportButton.setText("خروجی کانفیگ");
        
        // تنظیمات Spinner
        setupConfigSpinner();
        
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectWithVpnApp();
            }
        });
        
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportConfig();
            }
        });
        
        // درخواست مجوزهای لازم
        requestStoragePermission();
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
    
    private void connectWithVpnApp() {
        String configJson = vpnConfigs[currentConfigIndex];
        
        try {
            Gson gson = new Gson();
            JsonObject config = gson.fromJson(configJson, JsonObject.class);
            
            String name = config.get("name").getAsString();
            String server = config.get("server").getAsString();
            int port = config.get("port").getAsInt();
            
            // ایجاد لینک v2ray برای باز کردن برنامه‌های VPN
            String v2rayLink = String.format("vless://%s@%s:%d", 
                config.get("id").getAsString(), server, port);
            
            //尝试用Intent打开VPN程序
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(v2rayLink));
            
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                statusText.setText("در حال باز کردن برنامه VPN...");
                Toast.makeText(this, "باز کردن " + name + " در برنامه VPN", Toast.LENGTH_SHORT).show();
            } else {
                // اگر برنامه VPN یافت نشد، کانفیگ را export کنیم
                Toast.makeText(this, "برنامه VPN یافت نشد. در حال خروجی گرفتن از کانفیگ", Toast.LENGTH_SHORT).show();
                exportConfig();
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "خطا در پردازش کانفیگ", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void exportConfig() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                requestStoragePermission();
                return;
            }
        }
        
        try {
            String configJson = vpnConfigs[currentConfigIndex];
            Gson gson = new Gson();
            JsonObject config = gson.fromJson(configJson, JsonObject.class);
            
            String fileName = config.get("name").getAsString().replace(" ", "_") + ".json";
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File configFile = new File(downloadsDir, fileName);
            
            // ذخیره کانفیگ در فایل
            FileOutputStream fos = new FileOutputStream(configFile);
            fos.write(configJson.getBytes(StandardCharsets.UTF_8));
            fos.close();
            
            statusText.setText("کانفیگ ذخیره شد: " + fileName);
            Toast.makeText(this, "کانفیگ در پوشه Downloads ذخیره شد", Toast.LENGTH_LONG).show();
            
        } catch (IOException e) {
            Toast.makeText(this, "خطا در ذخیره کانفیگ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "مجوز ذخیره‌سازی داده شد", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "مجوز ذخیره‌سازی رد شد", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    // روش‌های interface OnItemSelectedListener
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        currentConfigIndex = position;
        
        try {
            Gson gson = new Gson();
            JsonObject config = gson.fromJson(vpnConfigs[position], JsonObject.class);
            String name = config.get("name").getAsString();
            String server = config.get("server").getAsString();
            int port = config.get("port").getAsInt();
            
            statusText.setText(String.format("انتخاب شده: %s\\nسرور: %s:%d", name, server, port));
        } catch (Exception e) {
            statusText.setText("کانفیگ انتخاب شد: " + (position + 1));
        }
    }
    
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // هیچ کاری لازم نیست انجام شود
    }
}
