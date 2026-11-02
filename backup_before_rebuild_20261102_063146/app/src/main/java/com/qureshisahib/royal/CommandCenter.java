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

/**
 * CLI client. Two modes:
 *  - local  (default): engine runs inside this app at 127.0.0.1:3000
 *  - remote: engine runs on your VPS ("config url ..." + "config token ...")
 */
public class CommandCenter {
    private final Context ctx;
    public CommandCenter(Context c) { ctx = c; }

    private static SharedPreferences sp(Context c) { return c.getSharedPreferences("royal", Context.MODE_PRIVATE); }

    static boolean isRemote(Context c) { return sp(c).contains("url"); }
    static String baseUrl(Context c) { return sp(c).getString("url", "http://127.0.0.1:3000"); }
    static String activeToken(Context c) { return isRemote(c) ? sp(c).getString("rtoken", "") : token(c); }

    /** Token of the engine inside the app (auto-generated). */
    static synchronized String token(Context c) {
        String t = sp(c).getString("token", null);
        if (t == null) {
            byte[] b = new byte[24];
            new SecureRandom().nextBytes(b);
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            t = sb.toString();
            sp(c).edit().putString("token", t).apply();
        }
        return t;
    }

    public String run(String line) {
        String t = line.trim();
        if (t.isEmpty()) return "";
        String c = t.toLowerCase();
        if (c.startsWith("config")) return config(t.split("\\s+", 3));
        if (c.equals("battery")) {
            ctx.startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:" + ctx.getPackageName())).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            return "Allow 'unrestricted' battery so Android does not kill the bot.";
        }
        if (c.equals("help"))
            return "config url <http://IP:3000> | config token <TOKEN> | config local | config show\nbattery\n---\n" + post("help");
        return post(t);
    }

    private String config(String[] p) {
        String sub = p.length > 1 ? p[1].toLowerCase() : "";
        SharedPreferences s = sp(ctx);
        if (sub.equals("url") && p.length > 2) {
            String u = p[2].trim().replaceAll("/+$", "");
            if (!u.startsWith("http://") && !u.startsWith("https://")) return "URL must start with http:// or https://";
            s.edit().putString("url", u).apply();
            return "Server URL saved (VPS mode). Now: config token <TOKEN>. Restart the app after.";
        }
        if (sub.equals("token") && p.length > 2) { s.edit().putString("rtoken", p[2].trim()).apply(); return "Token saved."; }
        if (sub.equals("local")) { s.edit().remove("url").remove("rtoken").apply(); return "Back to local mode (engine inside the app). Restart the app."; }
        if (sub.equals("show")) {
            String tk = s.getString("rtoken", "");
            return "mode: " + (isRemote(ctx) ? "VPS" : "local") + "\nurl: " + baseUrl(ctx)
                + "\ntoken: " + (isRemote(ctx) ? (tk.isEmpty() ? "(not set)" : tk.substring(0, Math.min(4, tk.length())) + "****") : "(automatic)");
        }
        return "Usage: config url|token|local|show";
    }

    private String post(String line) {
        if (isRemote(ctx) && activeToken(ctx).isEmpty()) return "Set the token: config token <TOKEN>";
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(baseUrl(ctx) + "/api/command").openConnection();
            c.setConnectTimeout(8000); c.setReadTimeout(40000);
            c.setRequestMethod("POST");
            c.setRequestProperty("Content-Type", "application/json");
            c.setRequestProperty("Authorization", "Bearer " + activeToken(ctx));
            c.setDoOutput(true);
            try (OutputStream os = c.getOutputStream()) {
                os.write(new JSONObject().put("line", line).toString().getBytes("UTF-8"));
            }
            int code = c.getResponseCode();
            if (code == 401) return "Wrong token.";
            if (code == 429) return "Too many requests or locked. Wait a bit.";
            InputStream is = code >= 400 ? c.getErrorStream() : c.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder(); String l;
            while ((l = r.readLine()) != null) sb.append(l);
            return new JSONObject(sb.toString()).optString("output", "(empty)");
        } catch (java.net.ConnectException e) {
            return isRemote(ctx) ? "Cannot reach the server. Check URL / server is running." : "Engine is starting (first run ~30s). Try again shortly.";
        } catch (Exception e) { return "Error: " + e.getMessage(); }
    }
}
