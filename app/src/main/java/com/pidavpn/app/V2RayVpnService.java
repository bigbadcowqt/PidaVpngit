package com.pidavpn.app;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.system.OsConstants;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class V2RayVpnService extends VpnService {
    private static final String TAG = "V2RayVpnService";
    private ParcelFileDescriptor vpnInterface;
    private Thread v2rayThread;
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
            
            // شروع V2Ray در thread جداگانه
            v2rayThread = new Thread(() -> {
                try {
                    isRunning = true;
                    
                    // ذخیره کانفیگ در فایل
                    File configDir = getFilesDir();
                    File configFile = new File(configDir, "config.json");
                    FileOutputStream fos = new FileOutputStream(configFile);
                    fos.write(config.getBytes(StandardCharsets.UTF_8));
                    fos.close();
                    
                    // اجرای V2Ray (این بخش نیاز به پیاده‌سازی کامل دارد)
                    runV2RayCore(configFile.getAbsolutePath());
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error in V2Ray thread: " + e.getMessage());
                    sendStatusUpdate("error", e.getMessage());
                }
            });
            
            v2rayThread.start();
            sendStatusUpdate("connected", "");
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting VPN: " + e.getMessage());
            sendStatusUpdate("error", e.getMessage());
        }
    }

    private native void runV2RayCore(String configPath);

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
            Log.e(TAG, "Error stopping VPN: " + e.getMessage());
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

    static {
        System.loadLibrary("v2ray");
    }
}
