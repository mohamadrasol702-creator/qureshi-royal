package com.qureshisahib.royal;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;
import org.json.JSONObject;

public class MainActivity extends Activity {

    final int BG = Color.rgb(10, 6, 24);
    final int CARD = Color.rgb(22, 14, 42);
    final int PURPLE = Color.rgb(124, 58, 237);
    final int GOLD = Color.rgb(224, 184, 91);
    final int GREEN = Color.rgb(76, 175, 80);
    final int RED = Color.rgb(229, 57, 53);
    final int GRAY = Color.LTGRAY;

    LinearLayout content, nav;
    ScrollView scroll;
    SharedPreferences sp;
    String current = "splash";

    // ================= ابزارها =================
    int dp(int n) { return (int) (n * getResources().getDisplayMetrics().density + .5f); }

    GradientDrawable bg(int color, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        if (stroke != 0) d.setStroke(dp(1), stroke);
        return d;
    }

    TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        t.setPadding(dp(5), dp(4), dp(5), dp(4));
        return t;
    }

    LinearLayout.LayoutParams params(int top) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, dp(top), 0, dp(top));
        return p;
    }

    void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    void title(String t) { content.addView(text(t, 21, GOLD, true), params(10)); }

    void say(String t) {
        TextView v = text(t, 13, Color.WHITE, false);
        v.setGravity(Gravity.START);
        v.setBackground(bg(CARD, PURPLE, 12));
        v.setPadding(dp(12), dp(10), dp(12), dp(10));
        v.setTextIsSelectable(true);
        content.addView(v, params(5));
    }

    LinearLayout box(int radius) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setGravity(Gravity.CENTER);
        c.setPadding(dp(12), dp(12), dp(12), dp(12));
        c.setBackground(bg(CARD, PURPLE, radius));
        return c;
    }

    void card(String icon, String name, String sub, View.OnClickListener click) {
        LinearLayout c = box(16);
        c.addView(text(icon, 24, GOLD, true));
        c.addView(text(name, 15, Color.WHITE, true));
        if (sub != null) c.addView(text(sub, 12, GRAY, false));
        if (click != null) c.setOnClickListener(click);
        content.addView(c, params(5));
    }

    LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        content.addView(r, params(2));
        return r;
    }

    void tile(LinearLayout r, String icon, String name, final Runnable act) {
        LinearLayout c = box(15);
        c.setPadding(dp(4), dp(14), dp(4), dp(14));
        c.addView(text(icon, 24, PURPLE, true));
        c.addView(text(name, 12, Color.WHITE, false));
        c.setOnClickListener(v -> act.run());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1);
        p.setMargins(dp(3), dp(3), dp(3), dp(3));
        r.addView(c, p);
    }

    void button(String name, boolean primary, final Runnable act) {
        Button b = new Button(this);
        b.setText(name);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setTextColor(primary ? Color.BLACK : GOLD);
        b.setBackground(primary ? bg(GOLD, 0, 28) : bg(CARD, GOLD, 28));
        b.setOnClickListener(v -> act.run());
        content.addView(b, params(5));
    }

    EditText input(String hint, String value, boolean multi) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setText(value);
        e.setTextColor(Color.WHITE);
        e.setHintTextColor(Color.GRAY);
        e.setTextSize(14);
        e.setPadding(dp(14), dp(10), dp(14), dp(10));
        e.setBackground(bg(CARD, PURPLE, 14));
        if (multi) { e.setMinLines(2); e.setGravity(Gravity.TOP | Gravity.START); }
        return e;
    }

    EditText field(String label, String key, String def, boolean multi) {
        TextView l = text(label, 13, GOLD, true);
        l.setGravity(Gravity.START);
        content.addView(l, params(2));
        EditText e = input(label, sp.getString(key, def), multi);
        content.addView(e, params(3));
        return e;
    }

    // ================= گزارش داخلی =================
    void log(String msg) {
        String t = new SimpleDateFormat("MM/dd HH:mm", Locale.US).format(new Date());
        String old = sp.getString("logs", "");
        String[] l = (t + "  " + msg + (old.isEmpty() ? "" : "\n" + old)).split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(l.length, 50); i++) { if (i > 0) sb.append("\n"); sb.append(l[i]); }
        sp.edit().putString("logs", sb.toString()).apply();
    }

    // ================= ارتباط با سرور =================
    void net(final String method, final String path, final String body, final Consumer<String> cb) {
        new Thread(() -> {
            final String r = Api.call(MainActivity.this, method, path, body);
            runOnUiThread(() -> cb.accept(r));
        }).start();
    }

    String j(String line) {
        try { return new JSONObject().put("line", line).toString(); } catch (Exception e) { return "{}"; }
    }

    String outOf(String r) {
        if (r == null) return "سرور در دسترس نیست. مطمئن شو سرور روشن است.";
        try { return new JSONObject(r).optString("output", r); } catch (Exception e) { return r; }
    }

    void cmd(String line, Consumer<String> cb) { net("POST", "/api/command", j(line), r -> cb.accept(outOf(r))); }

    void cmdMany(final String[] lines, final Runnable done) {
        new Thread(() -> {
            for (String l : lines) Api.call(MainActivity.this, "POST", "/api/command", j(l));
            runOnUiThread(done);
        }).start();
    }

    void refreshStatus() {
        net("GET", "/api/bot/status", null, r -> {
            boolean on = false, linked = false;
            if (r != null) {
                try {
                    JSONObject o = new JSONObject(r);
                    on = "connected".equals(o.optString("status"));
                    linked = o.optBoolean("linked");
                } catch (Exception e) { }
            }
            boolean changed = on != sp.getBoolean("online", false);
            sp.edit().putBoolean("online", on).putBoolean("linked", linked).putBoolean("reach", r != null).apply();
            if (on) cmd("group list", o -> {
                int n = o.startsWith("No groups") || o.startsWith("Error") ? 0 : o.split("\n").length;
                sp.edit().putInt("groups_n", n).apply();
            });
            if (changed && current.equals("home")) render("home");
        });
    }

    // ================= صفحه‌ها =================
    void pageSplash() {
        content.addView(text("♛", 90, GOLD, true), params(60));
        content.addView(text("QURESHI ROYAL", 32, Color.WHITE, true), params(4));
        content.addView(text("WhatsApp Bot", 16, GOLD, false), params(2));
        content.addView(text("♛ در خدمتم شهزاده ارسلان ♛", 14, GOLD, false), params(14));
        content.addView(text("Loading...", 13, GRAY, false), params(20));
        new Handler().postDelayed(() -> { if (current.equals("splash")) show("home"); }, 1500);
    }

    void pageHome() {
        content.addView(text("♛ QURESHI ROYAL ♛", 24, Color.WHITE, true), params(2));
        content.addView(text("King of WhatsApp Bots", 13, GOLD, false), params(0));

        boolean on = sp.getBoolean("online", false), reach = sp.getBoolean("reach", false);
        LinearLayout st = box(18);
        st.addView(text("♛", 50, GOLD, true));
        st.addView(text(on ? "🟢  ربات آنلاین است" : "🔴  ربات آفلاین است", 16, on ? GREEN : RED, true));
        st.addView(text(!reach ? "سرور در دسترس نیست" : (sp.getBoolean("linked", false) ? (on ? "Connected" : "متصل نیست") : "هنوز شماره‌ای وصل نشده"), 12, GRAY, false));
        content.addView(st, params(8));

        LinearLayout stats = new LinearLayout(this);
        stats.setBackground(bg(CARD, PURPLE, 14));
        stats.setPadding(dp(4), dp(8), dp(4), dp(8));
        String[][] s = {{"👥", "گروه‌ها", "" + sp.getInt("groups_n", 0)}, {"💬", "چت خصوصی", "-"}, {"⏱", "فعالیت", on ? "24h" : "-"}};
        for (String[] x : s) {
            LinearLayout b = new LinearLayout(this);
            b.setOrientation(LinearLayout.VERTICAL);
            b.setGravity(Gravity.CENTER);
            b.addView(text(x[0], 20, PURPLE, true));
            b.addView(text(x[1], 12, GRAY, false));
            b.addView(text(x[2], 16, Color.WHITE, true));
            stats.addView(b, new LinearLayout.LayoutParams(0, -2, 1));
        }
        content.addView(stats, params(6));

        button(on ? "⏻  خاموش کردن ربات" : "⏻  روشن کردن ربات", true, () -> {
            if (!on && !sp.getBoolean("linked", false)) { toast("اول از صفحهٔ اتصال شماره را وصل کن"); show("connect"); return; }
            net("POST", on ? "/api/bot/stop" : "/api/bot/start", "{}", r -> {
                toast(r == null ? "سرور در دسترس نیست" : (on ? "خاموش شد" : "در حال اتصال..."));
                log(on ? "ربات خاموش شد" : "درخواست روشن‌کردن ربات");
                new Handler().postDelayed(this::refreshStatus, 2500);
            });
        });
        button("⚙  تنظیمات", false, () -> show("settings"));

        LinearLayout r1 = row();
        tile(r1, "👥", "مدیریت گروه", () -> show("groups"));
        tile(r1, "📊", "گزارش‌ها", () -> show("logs"));
        tile(r1, "✉", "پیام‌ها", () -> show("messages"));
        LinearLayout r2 = row();
        tile(r2, "◈", "اتصال", () -> show("connect"));
        tile(r2, "⌨", "مرکز فرمان", () -> show("commands"));
        tile(r2, "ⓘ", "درباره", () -> show("about"));
        LinearLayout r3 = row();
        tile(r3, "❓", "راهنما", () -> show("help"));
        tile(r3, "⏻", "خروج", () -> finish());
    }

    void pageConnect() {
        title("◈ اتصال به واتساپ");
        boolean linked = sp.getBoolean("linked", false);
        card(linked ? "✅" : "📱", linked ? "شماره وصل است" : "شماره هنوز وصل نیست",
                "اتصال با کد جفت‌سازی انجام می‌شود (QR در سرور پشتیبانی نمی‌شود)", null);
        content.addView(text("شمارهٔ ربات با کد کشور و بدون + (مثال: 93700000000)", 12, GRAY, false), params(4));
        final EditText phone = input("شمارهٔ واتساپ", sp.getString("phone", ""), false);
        phone.setInputType(InputType.TYPE_CLASS_PHONE);
        content.addView(phone, params(4));
        button("دریافت کد جفت‌سازی", true, () -> {
            String p = phone.getText().toString().replaceAll("[^0-9]", "");
            if (p.length() < 8) { toast("شماره درست نیست"); return; }
            sp.edit().putString("phone", p).apply();
            toast("چند ثانیه صبر کن...");
            cmd("whatsapp pairing " + p, o -> {
                log("درخواست کد جفت‌سازی");
                say(o);
                say("واتساپ ← دستگاه‌های متصل ← اتصال با شماره ← کد را وارد کن. بعد دکمهٔ روشن‌کردن را در خانه بزن.");
            });
        });
        button("قطع اتصال و حذف نشست", false, () ->
            cmd("whatsapp disconnect", o -> { log("اتصال قطع شد"); toast(o); refreshStatus(); }));
    }

    void toggle(final String key, final String name, boolean def, final String serverKey) {
        LinearLayout r = new LinearLayout(this);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(dp(14), dp(8), dp(14), dp(8));
        r.setBackground(bg(CARD, PURPLE, 14));
        TextView t = text(name, 15, Color.WHITE, false);
        t.setGravity(Gravity.START);
        r.addView(t, new LinearLayout.LayoutParams(0, -2, 1));
        Switch sw = new Switch(this);
        sw.setChecked(sp.getBoolean(key, def));
        sw.setOnCheckedChangeListener((v, on) -> {
            sp.edit().putBoolean(key, on).apply();
            log((on ? "فعال شد: " : "غیرفعال شد: ") + name);
            if (serverKey != null) cmd("security " + serverKey + (on ? " on" : " off"), o -> { });
        });
        r.addView(sw);
        content.addView(r, params(4));
    }

    void pageSettings() {
        title("⚙ تنظیمات");
        toggle("antilink", "ضد لینک (حذف خودکار)", false, "antilink");
        toggle("antiflood", "ضد سیل و اسپم", false, "antiflood");
        toggle("antirepeat", "ضد تکرار پیام", false, "antispam");
        toggle("antiads", "ضد تبلیغات (در اپ)", true, null);
        toggle("onlyadmin", "فقط مدیران (در اپ)", false, null);
        content.addView(text("سه گزینهٔ اول مستقیم روی سرور اثر می‌گذارند.", 11, GRAY, false), params(4));
        button("✉  ویرایش پیام‌ها و پیشوند", false, () -> show("messages"));
    }

    void pageMessages() {
        title("✉ پیام‌ها و متن‌ها");
        final EditText bn = field("نام ربات", "botname", "Qureshi Royal", false);
        final EditText pre = field("پیشوند دستورها (۱ یا ۲ حرف)", "prefix", "!", false);
        final EditText w = field("خوش‌آمدگویی (@user و @group)", "welcome_text", "به @group خوش آمدی @user 👑", true);
        final EditText f = field("خداحافظی", "farewell_text", "خداحافظ @user", true);
        button("💾  ذخیره روی سرور", true, () -> {
            String p = pre.getText().toString().trim();
            if (p.isEmpty() || p.length() > 2) { toast("پیشوند باید ۱ یا ۲ حرف باشد"); return; }
            sp.edit().putString("botname", bn.getText().toString()).putString("prefix", p)
                .putString("welcome_text", w.getText().toString()).putString("farewell_text", f.getText().toString()).apply();
            String[] lines = {"settings set botname " + bn.getText(), "settings set prefix " + p,
                "settings set welcome " + w.getText().toString().replace("\n", " "),
                "settings set goodbye " + f.getText().toString().replace("\n", " ")};
            cmdMany(lines, () -> { log("متن‌ها ذخیره شد"); toast("ذخیره شد"); });
        });
    }

    void pageGroups() {
        title("♜ مدیریت گروه");
        final int sel = sp.getInt("sel", 0);
        LinearLayout r1 = row();
        tile(r1, "🔒", "بستن گروه", () -> groupAct("close"));
        tile(r1, "🔓", "باز کردن گروه", () -> groupAct("open"));
        LinearLayout r2 = row();
        tile(r2, "ℹ", "اعضای گروه", () -> groupAct("members"));
        tile(r2, "🔄", "تازه‌سازی لیست", () -> show("groups"));
        say("افزودن، حذف، ارتقا و تنزل عضو را داخل گروه بنویس:\n" + sp.getString("prefix", "!") + "add 937...   " + sp.getString("prefix", "!") + "kick @عضو\n" + sp.getString("prefix", "!") + "promote @عضو   " + sp.getString("prefix", "!") + "demote @عضو");
        content.addView(text(sel == 0 ? "یک گروه انتخاب کن:" : "گروه انتخاب‌شده: شمارهٔ " + sel, 13, GOLD, true), params(6));
        final LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        content.addView(list);
        cmd("group list", o -> {
            if (o.startsWith("No groups") || o.startsWith("Error") || o.startsWith("سرور")) {
                TextView t = text(o.startsWith("Error") ? "ربات هنوز به واتساپ وصل نیست" : o, 13, GRAY, false);
                list.addView(t);
                return;
            }
            for (final String line : o.split("\n")) {
                LinearLayout c = box(12);
                c.addView(text(line, 14, Color.WHITE, false));
                final int n = parseNum(line);
                if (n == sel) c.setBackground(bg(CARD, GOLD, 12));
                c.setOnClickListener(v -> { sp.edit().putInt("sel", n).apply(); show("groups"); });
                list.addView(c, params(3));
            }
        });
    }

    int parseNum(String line) {
        try { return Integer.parseInt(line.split("\\.")[0].trim()); } catch (Exception e) { return 0; }
    }

    void groupAct(final String act) {
        int n = sp.getInt("sel", 0);
        if (n == 0) { toast("اول یک گروه را انتخاب کن"); return; }
        cmd("group " + act + " " + n, o -> { log("گروه " + n + ": " + act); say(o); });
    }

    void pageLogs() {
        title("▤ گزارش‌ها");
        String l = sp.getString("logs", "");
        if (!l.isEmpty()) for (String line : l.split("\n")) {
            LinearLayout r = new LinearLayout(this);
            r.setGravity(Gravity.CENTER_VERTICAL);
            r.setPadding(dp(10), dp(6), dp(10), dp(6));
            r.setBackground(bg(CARD, PURPLE, 12));
            r.addView(text("●", 16, GREEN, true));
            TextView t = text(line, 12, Color.WHITE, false);
            t.setGravity(Gravity.START);
            r.addView(t, new LinearLayout.LayoutParams(0, -2, 1));
            content.addView(r, params(3));
        }
        content.addView(text("گزارش سرور", 14, GOLD, true), params(8));
        cmd("logs", o -> say(o));
        button("پاک‌کردن گزارش‌های اپ", false, () -> { sp.edit().remove("logs").apply(); show("logs"); });
    }

    void pageCommands() {
        title("⌨ مرکز فرمان");
        content.addView(text("هر فرمان سرور را اینجا بنویس. برای لیست: help", 12, GRAY, false), params(3));
        final EditText c = input("مثلاً: help یا system", "", false);
        content.addView(c, params(4));
        button("اجرا", true, () -> {
            String line = c.getText().toString().trim();
            if (line.isEmpty()) { toast("فرمان را بنویس"); return; }
            cmd(line, o -> say(o));
        });
    }

    void pageAbout() {
        title("ⓘ درباره");
        card("👑", "QURESHI ROYAL", "King of WhatsApp Bots", null);
        card("♛", "طراحی و سفارش", "شهزاده ارسلان", null);
        card("ⓘ", "نسخه", "1.0", null);
    }

    void pageHelp() {
        title("❓ راهنما");
        card("1", "اتصال", "صفحهٔ اتصال: شماره را بنویس، کد را بگیر و در واتساپ وارد کن", null);
        card("2", "روشن‌کردن", "در خانه دکمهٔ روشن‌کردن ربات را بزن", null);
        card("3", "دستورها در گروه", "ربات را ادمین گروه کن و بنویس: " + sp.getString("prefix", "!") + "menu", null);
    }

    // ================= ناوبری =================
    void render(String page) {
        current = page;
        content.removeAllViews();
        switch (page) {
            case "splash": pageSplash(); break;
            case "connect": pageConnect(); break;
            case "settings": pageSettings(); break;
            case "messages": pageMessages(); break;
            case "groups": pageGroups(); break;
            case "logs": pageLogs(); break;
            case "commands": pageCommands(); break;
            case "about": pageAbout(); break;
            case "help": pageHelp(); break;
            default: pageHome();
        }
        buildNav();
        scroll.post(() -> scroll.scrollTo(0, 0));
    }

    void show(String page) {
        render(page);
        if (page.equals("home")) refreshStatus();
    }

    void navItem(final String page, String icon, String name) {
        LinearLayout it = new LinearLayout(this);
        it.setOrientation(LinearLayout.VERTICAL);
        it.setGravity(Gravity.CENTER);
        int c = current.equals(page) ? GOLD : Color.GRAY;
        it.addView(text(icon, 18, c, true));
        it.addView(text(name, 10, c, current.equals(page)));
        it.setOnClickListener(v -> show(page));
        nav.addView(it, new LinearLayout.LayoutParams(0, dp(52), 1));
    }

    void buildNav() {
        nav.removeAllViews();
        nav.setVisibility(current.equals("splash") ? View.GONE : View.VISIBLE);
        navItem("home", "♛", "خانه");
        navItem("connect", "◈", "اتصال");
        navItem("groups", "👥", "گروه‌ها");
        navItem("settings", "⚙", "تنظیمات");
        navItem("logs", "▤", "گزارش");
    }

    @Override
    public void onBackPressed() {
        if (!current.equals("home") && !current.equals("splash")) show("home");
        else super.onBackPressed();
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        sp = getSharedPreferences("royal", MODE_PRIVATE);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(10), dp(8), dp(10), dp(4));

        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(3), dp(3), dp(3), dp(8));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setBackground(bg(CARD, PURPLE, 16));
        root.addView(nav, params(4));

        setContentView(root);
        render("splash");
    }
}
