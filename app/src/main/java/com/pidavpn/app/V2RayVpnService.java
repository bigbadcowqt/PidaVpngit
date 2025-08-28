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
            
            // ذخیره کانفیگ (برای استفاده بعدی)
            File configDir = getFilesDir();
            File configFile = new File(configDir, "config.json");
            FileOutputStream fos = new FileOutputStream(configFile);
            fos.write(config.getBytes(StandardCharsets.UTF_8));
            fos.close();
            
            isRunning = true;
            sendStatusUpdate("connected", "");
            
            // اینجا می‌توانید کد اتصال واقعی به V2Ray را اضافه کنید
            // در حال حاضر فقط یک VPN ساده ایجاد می‌کند
            
        } catch (Exception e) {
            sendStatusUpdate("error", e.getMessage());
        }
    }

    private void stopV2Ray() {
        try {
            isRunning = false;
            
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
