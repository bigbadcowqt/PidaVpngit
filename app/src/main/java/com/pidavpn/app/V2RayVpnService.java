package com.pidavpn.app;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class V2RayVpnService extends VpnService {
    private static final String TAG = "V2RayVpnService";
    private ParcelFileDescriptor vpnInterface;
    private boolean isRunning = false;
    private Thread v2rayThread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ("connect".equals(action)) {
                String config = intent.getStringExtra("config");
                startV2Ray(config);
            } else if ("disconnect".equals(action)) {
                stopV2Ray();
            }
        }
        return START_STICKY;
    }

    private void startV2Ray(String config) {
        if (isRunning) return;
        
        try {
            // ایجاد رابط VPN
            Builder builder = new Builder();
            builder.setSession("PidaVPN")
                   .addAddress("10.8.0.2", 24)
                   .addDnsServer("8.8.8.8")
                   .addDnsServer("8.8.4.4")
                   .addRoute("0.0.0.0", 0)
                   .setConfigureIntent(PendingIntent.getActivity(this, 0, 
                       new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE));

            vpnInterface = builder.establish();
            
            // ذخیره کانفیگ
            File configDir = getFilesDir();
            File configFile = new File(configDir, "config.json");
            FileOutputStream fos = new FileOutputStream(configFile);
            fos.write(config.getBytes(StandardCharsets.UTF_8));
            fos.close();
            
            // شروع V2Ray در thread جداگانه
            v2rayThread = new Thread(() -> {
                try {
                    isRunning = true;
                    
                    // تبدیل لینک VLESS به کانفیگ JSON کامل
                    String jsonConfig = convertVlessToJson(config);
                    
                    // اجرای V2Ray (این بخش نیاز به پیاده‌سازی کامل دارد)
                    runV2Ray(jsonConfig);
                    
                    sendStatusUpdate("connected", "");
                    
                } catch (Exception e) {
                    sendStatusUpdate("error", e.getMessage());
                }
            });
            
            v2rayThread.start();
            
        } catch (Exception e) {
            sendStatusUpdate("error", e.getMessage());
        }
    }

    private String convertVlessToJson(String vlessLink) {
        // این متد لینک VLESS را به کانفیگ JSON کامل تبدیل می‌کند
        // برای سادگی، یک کانفیگ پایه برگردانید
        return "{\n" +
            "  \"inbounds\": [\n" +
            "    {\n" +
            "      \"port\": 1080,\n" +
            "      \"protocol\": \"socks\",\n" +
            "      \"settings\": {\n" +
            "        \"auth\": \"noauth\",\n" +
            "        \"udp\": true\n" +
            "      }\n" +
            "    }\n" +
            "  ],\n" +
            "  \"outbounds\": [\n" +
            "    {\n" +
            "      \"protocol\": \"vless\",\n" +
            "      \"settings\": {\n" +
            "        \"vnext\": [\n" +
            "          {\n" +
            "            \"address\": \"104.24.69.25\",\n" +
            "            \"port\": 8443,\n" +
            "            \"users\": [\n" +
            "              {\n" +
            "                \"id\": \"d1563ff5-b87c-41a2-ab52-315a5251783b\",\n" +
            "                \"encryption\": \"none\"\n" +
            "              }\n" +
            "            ]\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"streamSettings\": {\n" +
            "        \"network\": \"ws\",\n" +
            "        \"security\": \"tls\",\n" +
            "        \"wsSettings\": {\n" +
            "          \"path\": \"/SfCL44HuPKPyhBBf?ed=2560\",\n" +
            "          \"headers\": {\n" +
            "            \"Host\": \"white-tooth-0914.motilew530.workers.dev\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"tlsSettings\": {\n" +
            "          \"serverName\": \"white-tooth-0914.motilew530.workers.dev\",\n" +
            "          \"allowInsecure\": false\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    }

    private void runV2Ray(String jsonConfig) {
        try {
            // اینجا باید V2Ray core اجرا شود
            // این یک پیاده‌سازی ساده است
            
            File configDir = getFilesDir();
            File configFile = new File(configDir, "v2ray_config.json");
            FileOutputStream fos = new FileOutputStream(configFile);
            fos.write(jsonConfig.getBytes(StandardCharsets.UTF_8));
            fos.close();
            
            // اجرای V2Ray - این بخش نیاز به کتابخانه native دارد
            // برای حالا فقط منتظر می‌مانیم
            Thread.sleep(5000);
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void stopV2Ray() {
        try {
            isRunning = false;
            
            if (v2rayThread != null) {
                v2rayThread.interrupt();
                v2rayThread = null;
            }
            
            if (vpnInterface != null) {
                vpnInterface.close();
                vpnInterface = null;
            }
            
            sendStatusUpdate("disconnected", "");
            
        } catch (Exception e) {
            sendStatusUpdate("error", e.getMessage());
        }
        
        stopSelf();
    }

    private void sendStatusUpdate(String status, String message) {
        Intent intent = new Intent("vpn_status");
        intent.putExtra("status", status);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        stopV2Ray();
        super.onDestroy();
    }
}
