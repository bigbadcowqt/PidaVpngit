package com.pidavpn.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
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
import java.net.URLDecoder;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Button connectButton, exportButton;
    private TextView statusText;
    private Spinner configSpinner;
    
    // کانفیگ‌های واقعی شما - دقیقاً مطابق با فرمت ارسالی
    private final String[] vpnConfigs = {
        // کانفیگ اصلی شما به صورت لینک VLESS
        "vless://d1563ff5-b87c-41a2-ab52-315a5251783b@104.24.69.25:8443?path=%2FSfCL44HuPKPyhBBf%3Fed%3D2560&security=tls&alpn=http%2F1.1&encryption=none&host=white-tooth-0914.motilew530.workers.dev&type=ws&sni=white-tooth-0914.motilew530.workers.dev#proxy",
        
        // کانفیگ‌های اضافی (در صورت نیاز می‌توانید اضافه کنید)
        "vless://d29debb4-5df3-424e-85c5-e2dffa095ec0@www.speedtest.net:443?path=%2FV0cJso6c0RXNrfEp%2FMTA0LjI0LjU0Ljc1LDEwNC4yNC4zOC4yNTMsMTg4LjI0NC4xMjIuNTMsMTYyLjE1OC4xNDguNzQsMTYyLjE1OC4xNjcuMjYsMTA0LjI0LjYyLjM0LDE2Mi4xNTguMTc5LjEwMSwxMDQuMjQuMjkuMjQ2LDE4OC4xMTQuMTExLjk3LDEwNC4yNC4zOC41MCwxMDQuMjQuMzguMjUzLDEwNC4yNC4yOS40LDEwNC4yNC41NC4xMjMsMTcyLjY0LjQxLjIxOSwxMDQuMjQuNTQuMjcsMTcyLjY0LjQxLjIyMSwxODUuMjIxLjE2MC43MiwwLjAsMC4wLDEwNC4yNC41NC4xNDE%3Ed%3D2560&security=tls&alpn=http%2F1.1&encryption=none&host=amoozesh-enl.pages.dev&type=ws&sni=amoozesh-enl.pages.dev#%F0%9F%92%A6%202%20-%20VLESS%20-%20Domain%20%3A%20443"
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
                if (checkStoragePermission()) {
                    exportConfig();
                } else {
                    requestStoragePermission();
                }
            }
        });
    }
    
    private void setupConfigSpinner() {
        // ایجاد لیست نام‌های کانفیگ‌ها
        String[] configNames = new String[vpnConfigs.length];
        for (int i = 0; i < vpnConfigs.length; i++) {
            try {
                // اگر لینک VLESS است، نام را از fragment بردارید
                if (vpnConfigs[i].startsWith("vless://")) {
                    Uri uri = Uri.parse(vpnConfigs[i]);
                    String fragment = uri.getFragment();
                    configNames[i] = fragment != null ? fragment : "کانفیگ " + (i + 1);
                } else {
                    // اگر JSON است، نام را از فیلد name بردارید
                    Gson gson = new Gson();
                    JsonObject config = gson.fromJson(vpnConfigs[i], JsonObject.class);
                    configNames[i] = config.get("name").getAsString();
                }
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
        String config = vpnConfigs[currentConfigIndex];
        
        try {
            // اگر لینک VLESS است، مستقیماً از آن استفاده کنید
            if (config.startsWith("vless://")) {
                //尝试用Intent打开VPN程序
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(config));
                
                // اضافه کردن intentهای جایگزین برای برنامه‌های محبوب
                Intent v2rayNGIntent = new Intent(Intent.ACTION_VIEW);
                v2rayNGIntent.setData(Uri.parse("v2rayng://install-config/" + Uri.encode(config)));
                
                // ایجاد chooser برای انتخاب برنامه
                Intent chooser = Intent.createChooser(intent, "اتصال با برنامه VPN");
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{v2rayNGIntent});
                
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(chooser);
                    statusText.setText("در حال باز کردن برنامه VPN...");
                    Toast.makeText(this, "در حال باز کردن برنامه VPN", Toast.LENGTH_SHORT).show();
                } else {
                    // اگر برنامه VPN یافت نشد، کانفیگ را export کنیم
                    Toast.makeText(this, "برنامه VPN یافت نشد. لطفاً از گزینه 'خروجی کانفیگ' استفاده کنید", Toast.LENGTH_LONG).show();
                    statusText.setText("برنامه VPN یافت نشد. از گزینه خروجی استفاده کنید");
                }
            } else {
                // اگر JSON است، مانند قبل رفتار کنید
                Gson gson = new Gson();
                JsonObject configJson = gson.fromJson(config, JsonObject.class);
                
                String name = configJson.get("name").getAsString();
                String server = configJson.get("server").getAsString();
                int port = configJson.get("port").getAsInt();
                String id = configJson.get("id").getAsString();
                
                // ایجاد لینک v2ray برای باز کردن برنامه‌های VPN
                String v2rayLink = String.format("vless://%s@%s:%d?type=ws&security=tls&path=%s&host=%s#%s",
                    id, server, port, 
                    configJson.get("path").getAsString().split("\\?")[0],
                    configJson.get("host").getAsString(),
                    name.replace(" ", "-"));
                
                //尝试用Intent打开VPN程序
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(v2rayLink));
                
                // اضافه کردن intentهای جایگزین برای برنامه‌های محبوب
                Intent v2rayNGIntent = new Intent(Intent.ACTION_VIEW);
                v2rayNGIntent.setData(Uri.parse("v2rayng://install-config/" + Uri.encode(v2rayLink)));
                
                // ایجاد chooser برای انتخاب برنامه
                Intent chooser = Intent.createChooser(intent, "اتصال با برنامه VPN");
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{v2rayNGIntent});
                
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(chooser);
                    statusText.setText("در حال باز کردن برنامه VPN...");
                    Toast.makeText(this, "باز کردن " + name + " در برنامه VPN", Toast.LENGTH_SHORT).show();
                } else {
                    // اگر برنامه VPN یافت نشد، کانفیگ را export کنیم
                    Toast.makeText(this, "برنامه VPN یافت نشد. لطفاً از گزینه 'خروجی کانفیگ' استفاده کنید", Toast.LENGTH_LONG).show();
                    statusText.setText("برنامه VPN یافت نشد. از گزینه خروجی استفاده کنید");
                }
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "خطا در پردازش کانفیگ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
    
    private void exportConfig() {
        try {
            String config = vpnConfigs[currentConfigIndex];
            String fileName;
            String content;
            
            if (config.startsWith("vless://")) {
                // برای لینک VLESS
                Uri uri = Uri.parse(config);
                String fragment = uri.getFragment();
                fileName = (fragment != null ? fragment : "config") + ".vless";
                content = config;
            } else {
                // برای JSON
                Gson gson = new Gson();
                JsonObject configJson = gson.fromJson(config, JsonObject.class);
                fileName = configJson.get("name").getAsString().replace(" ", "_") + ".json";
                content = config;
            }
            
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File configFile = new File(downloadsDir, fileName);
            
            // ذخیره کانفیگ در فایل
            FileOutputStream fos = new FileOutputStream(configFile);
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.close();
            
            statusText.setText("کانفیگ ذخیره شد: " + fileName);
            Toast.makeText(this, "کانفیگ در پوشه Downloads ذخیره شد", Toast.LENGTH_LONG).show();
            
            // اشتراک‌گذاری فایل
            shareFile(configFile);
            
        } catch (IOException e) {
            Toast.makeText(this, "خطا در ذخیره کانفیگ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void shareFile(File file) {
        Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری کانفیگ"));
    }
    
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // نمایش توضیح برای کاربر
                Toast.makeText(this, "برای ذخیره کانفیگ نیاز به مجوز ذخیره‌سازی داریم", Toast.LENGTH_LONG).show();
            }
            
            ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_CODE);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "مجوز ذخیره‌سازی داده شد", Toast.LENGTH_SHORT).show();
                exportConfig();
            } else {
                Toast.makeText(this, "مجوز ذخیره‌سازی رد شد. می‌توانید از طریق اشتراک‌گذاری استفاده کنید", Toast.LENGTH_LONG).show();
                
                // اشتراک‌گذاری مستقیم محتوا بدون نیاز به ذخیره فایل
                shareConfigContent();
            }
        }
    }
    
    private void shareConfigContent() {
        try {
            String config = vpnConfigs[currentConfigIndex];
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, config);
            
            String subject;
            if (config.startsWith("vless://")) {
                Uri uri = Uri.parse(config);
                String fragment = uri.getFragment();
                subject = fragment != null ? fragment : "VLESS Configuration";
            } else {
                Gson gson = new Gson();
                JsonObject configJson = gson.fromJson(config, JsonObject.class);
                subject = configJson.get("name").getAsString() + " Configuration";
            }
            
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            
            startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری کانفیگ"));
            
        } catch (Exception e) {
            Toast.makeText(this, "خطا در اشتراک‌گذاری کانفیگ", Toast.LENGTH_SHORT).show();
        }
    }
    
    // روش‌های interface OnItemSelectedListener
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        currentConfigIndex = position;
        
        try {
            String config = vpnConfigs[position];
            
            if (config.startsWith("vless://")) {
                // برای لینک VLESS
                Uri uri = Uri.parse(config);
                String fragment = uri.getFragment();
                String host = uri.getHost();
                int port = uri.getPort();
                
                statusText.setText(String.format("انتخاب شده: %s\nسرور: %s:%d", 
                    fragment != null ? fragment : "کانفیگ VLESS", 
                    host, port));
            } else {
                // برای JSON
                Gson gson = new Gson();
                JsonObject configJson = gson.fromJson(config, JsonObject.class);
                String name = configJson.get("name").getAsString();
                String server = configJson.get("server").getAsString();
                int port = configJson.get("port").getAsInt();
                
                statusText.setText(String.format("انتخاب شده: %s\nسرور: %s:%d", name, server, port));
            }
        } catch (Exception e) {
            statusText.setText("کانفیگ انتخاب شد: " + (position + 1));
        }
    }
    
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // هیچ کاری لازم نیست انجام شود
    }
}
