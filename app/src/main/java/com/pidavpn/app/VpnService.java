package com.pidavpn.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

public class VpnService extends Service {
    private static final String TAG = "PidaVPN";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "vpn_service_channel";
    
    private final IBinder binder = new LocalBinder();
    private boolean isRunning = false;
    
    public class LocalBinder extends Binder {
        VpnService getService() {
            return VpnService.this;
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals("connect")) {
                String config = intent.getStringExtra("config");
                startVpn(config);
            } else if (intent.getAction().equals("disconnect")) {
                stopVpn();
            }
        }
        return START_STICKY;
    }
    
    private void startVpn(String config) {
        if (isRunning) return;
        
        // ایجاد notification
        createNotificationChannel();
        Notification notification = createNotification("در حال اتصال...");
        startForeground(NOTIFICATION_ID, notification);
        
        // اینجا کد اتصال واقعی به VPN قرار می‌گیرد
        // برای نسخه اول، فقط وضعیت را تغییر می‌دهیم
        isRunning = true;
        
        // آپدیت notification
        notification = createNotification("متصل شده");
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, notification);
        
        Log.d(TAG, "VPN started with config: " + config);
    }
    
    private void stopVpn() {
        if (!isRunning) return;
        
        isRunning = false;
        stopForeground(true);
        stopSelf();
        
        Log.d(TAG, "VPN stopped");
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "سرویس VPN",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("سرویس اتصال VPN");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    private Notification createNotification(String text) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PidaVPN")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build();
    }
    
    public boolean isRunning() {
        return isRunning;
    }
}
