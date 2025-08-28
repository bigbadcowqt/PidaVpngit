package com.pidavpn.app;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class V2RayVpnService extends VpnService {
    private static final String TAG = "V2RayVpnService";
    private ParcelFileDescriptor vpnInterface;
    private boolean isRunning = false;

    // Native methods
    public native String startV2Ray(String configJson);

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ("connect".equals(action)) {
                String config = intent.getStringExtra("config");
                startV2RayConnection(config);
            } else if ("disconnect".equals(action)) {
                stopV2RayConnection();
            }
        }
        return START_STICKY;
    }

    private void startV2RayConnection(String config) {
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
            
            // تبدیل لینک VLESS به کانفیگ JSON
            String jsonConfig = convertVlessToJson(config);
            
            // ذخیره کانفیگ
            File configDir = getFilesDir();
            File configFile = new File(configDir, "config.json");
            FileOutputStream fos = new FileOutputStream(configFile);
            fos.write(jsonConfig.getBytes(StandardCharsets.UTF_8));
            fos.close();
            
            // شروع V2Ray
            String result = startV2Ray(jsonConfig);
            
            isRunning = true;
            sendStatusUpdate("connected", "اتصال VPN برقرار شد: " + result);
            
        } catch (Exception e) {
            sendStatusUpdate("error", "خطا در ایجاد VPN: " + e.getMessage());
        }
    }

    private String convertVlessToJson(String vlessLink) {
        // تبدیل لینک VLESS به کانفیگ JSON کامل
        try {
            // پارامترهای لینک VLESS
            String server = "104.24.69.25";
            int port = 8443;
            String id = "d1563ff5-b87c-41a2-ab52-315a5251783b";
            String path = "/SfCL44HuPKPyhBBf?ed=2560";
            String host = "white-tooth-0914.motilew530.workers.dev";
            
            return "{\n" +
                "  \"inbounds\": [\n" +
                "    {\n" +
                "      \"port\": 1080,\n" +
                "      \"protocol\": \"socks\",\n" +
                "      \"settings\": {\n" +
                "        \"auth\": \"noauth\",\n" +
                "        \"udp\": true,\n" +
                "        \"ip\": \"127.0.0.1\"\n" +
                "      },\n" +
                "      \"tag\": \"socks\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"port\": 1081,\n" +
                "      \"protocol\": \"http\",\n" +
                "      \"settings\": {\n" +
                "        \"timeout\": 300\n" +
                "      },\n" +
                "      \"tag\": \"http\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"outbounds\": [\n" +
                "    {\n" +
                "      \"protocol\": \"vless\",\n" +
                "      \"settings\": {\n" +
                "        \"vnext\": [\n" +
                "          {\n" +
                "            \"address\": \"" + server + "\",\n" +
                "            \"port\": " + port + ",\n" +
                "            \"users\": [\n" +
                "              {\n" +
                "                \"id\": \"" + id + "\",\n" +
                "                \"encryption\": \"none\",\n" +
                "                \"level\": 0\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      \"streamSettings\": {\n" +
                "        \"network\": \"ws\",\n" +
                "        \"security\": \"tls\",\n" +
                "        \"wsSettings\": {\n" +
                "          \"path\": \"" + path + "\",\n" +
                "          \"headers\": {\n" +
                "            \"Host\": \"" + host + "\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"tlsSettings\": {\n" +
                "          \"serverName\": \"" + host + "\",\n" +
                "          \"allowInsecure\": false\n" +
                "        }\n" +
                "      },\n" +
                "      \"tag\": \"proxy\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"protocol\": \"freedom\",\n" +
                "      \"settings\": {},\n" +
                "      \"tag\": \"direct\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"protocol\": \"blackhole\",\n" +
                "      \"settings\": {},\n" +
                "      \"tag\": \"block\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"routing\": {\n" +
                "    \"domainStrategy\": \"IPOnDemand\",\n" +
                "    \"rules\": [\n" +
                "      {\n" +
                "        \"type\": \"field\",\n" +
                "        \"outboundTag\": \"proxy\",\n" +
                "        \"domain\": [\"geosite:google\", \"geosite:facebook\", \"geosite:twitter\"]\n" +
                "      },\n" +
                "      {\n" +
                "        \"type\": \"field\",\n" +
                "        \"outboundTag\": \"direct\",\n" +
                "        \"domain\": [\"geosite:cn\", \"geosite:ir\"]\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";
        } catch (Exception e) {
            return "{\"error\": \"Invalid VLESS link\"}";
        }
    }

    private void stopV2RayConnection() {
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
        stopV2RayConnection();
        super.onDestroy();
    }
}
