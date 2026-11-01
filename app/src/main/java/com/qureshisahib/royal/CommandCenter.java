package com.qureshisahib.royal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.Settings;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.security.SecureRandom;

/** CLI client. The bot engine runs inside this app (127.0.0.1:3000). */
public class CommandCenter {
    private final Context ctx;
    private static final String URL = "http://127.0.0.1:3000";

    public CommandCenter(Context c) { ctx = c; }

    static synchronized String token(Context c) {
        SharedPreferences sp = c.getSharedPreferences("royal", Context.MODE_PRIVATE);
        String t = sp.getString("token", null);
        if (t == null) {
            byte[] b = new byte[24];
            new SecureRandom().nextBytes(b);
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            t = sb.toString();
            sp.edit().putString("token", t).apply();
        }
        return t;
    }

    public String run(String line) {
        String t = line.trim();
        if (t.isEmpty()) return "";
        String c = t.toLowerCase();
        if (c.equals("battery")) {
            Intent i = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:" + ctx.getPackageName())).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(i);
            return "Allow 'unrestricted' battery so Android does not kill the bot.";
        }
        if (c.equals("help")) return "battery   (keep bot alive in background)\n---\n" + post("help");
        return post(t);
    }

    private String post(String line) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(URL + "/api/command").openConnection();
            c.setConnectTimeout(3000); c.setReadTimeout(40000);
            c.setRequestMethod("POST");
            c.setRequestProperty("Content-Type", "application/json");
            c.setRequestProperty("Authorization", "Bearer " + token(ctx));
            c.setDoOutput(true);
            try (OutputStream os = c.getOutputStream()) {
                os.write(new JSONObject().put("line", line).toString().getBytes("UTF-8"));
            }
            int code = c.getResponseCode();
            if (code == 401) return "Auth error (token mismatch). Restart the app.";
            if (code == 429) return "Too many requests. Wait a bit.";
            InputStream is = code >= 400 ? c.getErrorStream() : c.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder(); String l;
            while ((l = r.readLine()) != null) sb.append(l);
            return new JSONObject(sb.toString()).optString("output", "(empty)");
        } catch (java.net.ConnectException e) {
            return "Engine is starting (first run takes ~30s). Try again shortly.";
        } catch (Exception e) { return "Error: " + e.getMessage(); }
    }
}
