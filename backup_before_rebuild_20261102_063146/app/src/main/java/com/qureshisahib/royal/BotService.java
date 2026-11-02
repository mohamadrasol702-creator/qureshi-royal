package com.qureshisahib.royal;

import android.app.*;
import android.content.*;
import android.os.IBinder;
import android.system.Os;
import java.io.*;
import java.util.zip.*;

public class BotService extends Service {
    static volatile boolean started = false;

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationChannel ch = new NotificationChannel("royal", "Qureshi Royal", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
        Notification n = new Notification.Builder(this, "royal")
            .setContentTitle("Qureshi Royal")
            .setContentText("Bot engine is running")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentIntent(PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE))
            .build();
        startForeground(1, n);

        if (!started) {
            started = true; // Node can start only once per process
            new Thread(null, this::runNode, "node", 2 * 1024 * 1024).start();
        }
        return START_STICKY;
    }

    private void runNode() {
        try {
            File dir = new File(getFilesDir(), "nodejs-project");
            SharedPreferences sp = getSharedPreferences("royal", MODE_PRIVATE);
            long upd = getPackageManager().getPackageInfo(getPackageName(), 0).lastUpdateTime;
            File boot = new File(dir, "boot.js");
            if (sp.getLong("installed", 0) != upd || !boot.exists()) {
                try (InputStream in = getAssets().open("nodejs-project.zip")) { unzip(in, dir); }
                sp.edit().putLong("installed", upd).apply();
            }
            Os.setenv("API_TOKEN", CommandCenter.token(this), true);
            Os.setenv("HOST", "127.0.0.1", true);
            Os.setenv("PORT", "3000", true);
            Os.setenv("HOME", getFilesDir().getAbsolutePath(), true);
            Os.setenv("TMPDIR", getCacheDir().getAbsolutePath(), true);
            NodeBridge.startNodeWithArguments(new String[]{"node", boot.getAbsolutePath()});
        } catch (Throwable e) {
            started = false;
            e.printStackTrace();
        }
    }

    static void unzip(InputStream in, File dest) throws IOException {
        String root = dest.getCanonicalPath() + File.separator;
        try (ZipInputStream z = new ZipInputStream(in)) {
            ZipEntry e;
            byte[] buf = new byte[16384];
            while ((e = z.getNextEntry()) != null) {
                File f = new File(dest, e.getName());
                if (!f.getCanonicalPath().startsWith(root)) continue; // zip-slip guard
                if (e.isDirectory()) { f.mkdirs(); continue; }
                f.getParentFile().mkdirs();
                try (FileOutputStream o = new FileOutputStream(f)) {
                    int r;
                    while ((r = z.read(buf)) > 0) o.write(buf, 0, r);
                }
            }
        }
    }

    @Override public IBinder onBind(Intent i) { return null; }
}
