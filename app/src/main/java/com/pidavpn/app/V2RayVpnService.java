package com.pidavpn.app;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.io.IOException;

public class V2RayVpnService extends VpnService {
    private static final String TAG = "V2RayVpnService";
    private ParcelFileDescriptor vpnInterface;
    private boolean isRunning = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ("connect".equals(action)) {
                startVpn();
            } else if ("disconnect".equals(action)) {
                stopVpn();
            }
        }
        return START_STICKY;
    }

    private void startVpn() {
        if (isRunning) return;
        
        try {
            // ایجاد رابط VPN ساده
            Builder builder = new Builder();
            builder.setSession("PidaVPN")
                   .addAddress("10.8.0.2", 24)
                   .addDnsServer("8.8.8.8")
                   .addDnsServer("8.8.4.4")
                   .addRoute("0.0.0.0", 0)
                   .setConfigureIntent(PendingIntent.getActivity(this, 0, 
                       new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE));

            vpnInterface = builder.establish();
            
            isRunning = true;
            sendStatusUpdate("connected", "اتصال VPN برقرار شد");
            
            // اینجا می‌توانید ترافیک را مدیریت کنید
            // برای شروع، یک اتصال ساده ایجاد می‌کنیم
            
        } catch (Exception e) {
            sendStatusUpdate("error", "خطا در ایجاد VPN: " + e.getMessage());
        }
    }

    private void stopVpn() {
        try {
            isRunning = false;
            
            if (vpnInterface != null) {
                vpnInterface.close();
                vpnInterface = null;
            }
            
            sendStatusUpdate("disconnected", "اتصال VPN قطع شد");
            
        } catch (Exception e) {
            sendStatusUpdate("error", "خطا در قطع VPN: " + e.getMessage());
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
        stopVpn();
        super.onDestroy();
    }
}
