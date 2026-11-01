package com.qureshisahib.royal;

import android.content.Context;
import java.io.*;
import java.net.*;

/** Tiny HTTP helper to talk to the engine inside the app. Returns null if engine is not reachable. */
public class Api {
    static String call(Context c, String method, String path, String body) {
        try {
            HttpURLConnection h = (HttpURLConnection) new URL("http://127.0.0.1:3000" + path).openConnection();
            h.setConnectTimeout(3000); h.setReadTimeout(40000);
            h.setRequestMethod(method);
            h.setRequestProperty("Authorization", "Bearer " + CommandCenter.token(c));
            if (body != null) {
                h.setRequestProperty("Content-Type", "application/json");
                h.setDoOutput(true);
                try (OutputStream o = h.getOutputStream()) { o.write(body.getBytes("UTF-8")); }
            }
            int code = h.getResponseCode();
            InputStream is = code >= 400 ? h.getErrorStream() : h.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder(); String l;
            while ((l = r.readLine()) != null) sb.append(l);
            return sb.toString();
        } catch (Exception e) { return null; }
    }
}
