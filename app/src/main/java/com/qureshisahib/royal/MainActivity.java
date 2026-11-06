package com.qureshisahib.royal;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    final int BG = Color.rgb(10, 6, 24);
    final int CARD = Color.rgb(22, 14, 42);
    final int PURPLE = Color.rgb(124, 58, 237);
    final int GOLD = Color.rgb(224, 184, 91);
    final int GREEN = Color.rgb(76, 175, 80);

    // همین کلیدها با DEFAULTS در index.js ربات یکی هستند
    final String[] KEYS = {"antilink", "antiflood", "antirepeat", "antiads",
            "onlyadmin", "welcome", "farewell", "protectsettings"};
    final String[] NAMES = {"ضد لینک", "ضد سیل پیام", "ضد تکرار", "ضد تبلیغات",
            "فقط ادمین", "خوش‌آمدگویی", "خداحافظی", "محافظت تنظیمات"};
    final boolean[] DEFS = {true, true, true, true, false, true, true, true};

    SharedPreferences sp;
    LinearLayout content, nav;
    String current = "home";

    // ---------- ابزارها ----------
    int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    GradientDrawable shape(int color, int radiusDp, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radiusDp));
        if (stroke != 0) d.setStroke(dp(1), stroke);
        return d;
    }

    TextView label(String v, int size, int color, boolean bold, int gravity) {
        TextView t = new TextView(this);
        t.setText(v);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(gravity);
        t.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        t.setPadding(dp(6), dp(4), dp(6), dp(4));
        return t;
    }

    LinearLayout.LayoutParams lp(int top) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, dp(top), 0, dp(top));
        return p;
    }

    void card(String icon, String title, String sub, View.OnClickListener click) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14), dp(12), dp(14), dp(12));
        box.setBackground(shape(CARD, 18, PURPLE));
        box.addView(label(icon + "  " + title, 18, GOLD, true, Gravity.START));
        if (sub != null) box.addView(label(sub, 13, Color.LTGRAY, false, Gravity.START));
        if (click != null) box.setOnClickListener(click);
        content.addView(box, lp(6));
    }

    Button button(String text, boolean primary, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(16);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setTextColor(primary ? Color.BLACK : GOLD);
        b.setBackground(primary ? shape(GOLD, 30, 0) : shape(CARD, 30, GOLD));
        b.setOnClickListener(l);
        return b;
    }

    EditText input(String hint, String value, boolean multiline) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setText(value);
        e.setTextColor(Color.WHITE);
        e.setHintTextColor(Color.GRAY);
        e.setPadding(dp(14), dp(10), dp(14), dp(10));
        e.setBackground(shape(CARD, 14, PURPLE));
        if (multiline) {
            e.setMinLines(3);
            e.setGravity(Gravity.TOP | Gravity.START);
        }
        return e;
    }

    void toggle(final String key, String name, boolean def) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(8), dp(14), dp(8));
        row.setBackground(shape(CARD, 14, PURPLE));
        TextView t = label(name, 16, Color.WHITE, false, Gravity.START);
        row.addView(t, new LinearLayout.LayoutParams(0, -2, 1));
        Switch s = new Switch(this);
        s.setChecked(sp.getBoolean(key, def));
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean on) {
                sp.edit().putBoolean(key, on).apply();
                log((on ? "فعال شد: " : "غیرفعال شد: ") + key);
            }
        });
        row.addView(s);
        content.addView(row, lp(5));
    }

    void title(String t) {
        content.addView(label(t, 22, GOLD, true, Gravity.CENTER), lp(8));
    }

    // ---------- گزارش‌ها ----------
    void log(String msg) {
        String time = new SimpleDateFormat("MM/dd HH:mm", Locale.US).format(new Date());
        String old = sp.getString("logs", "");
        String all = time + "  " + msg + (old.isEmpty() ? "" : "\n" + old);
        String[] lines = all.split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(lines.length, 50); i++) {
            if (i > 0) sb.append("\n");
            sb.append(lines[i]);
        }
        sp.edit().putString("logs", sb.toString()).apply();
    }

    int activeCount() {
        int n = 0;
        for (int i = 0; i < KEYS.length; i++) if (sp.getBoolean(KEYS[i], DEFS[i])) n++;
        return n;
    }

    // ---------- صفحه‌ها ----------
    void statBox(LinearLayout row, String icon, String name, String val) {
        LinearLayout b = new LinearLayout(this);
        b.setOrientation(LinearLayout.VERTICAL);
        b.setGravity(Gravity.CENTER);
        b.addView(label(icon, 20, PURPLE, true, Gravity.CENTER));
        b.addView(label(name, 12, Color.LTGRAY, false, Gravity.CENTER));
        b.addView(label(val, 16, Color.WHITE, true, Gravity.CENTER));
        row.addView(b, new LinearLayout.LayoutParams(0, -2, 1));
    }

    void gridItem(LinearLayout row, String icon, String name, final String page) {
        LinearLayout b = new LinearLayout(this);
        b.setOrientation(LinearLayout.VERTICAL);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(4), dp(10), dp(4), dp(10));
        b.setBackground(shape(CARD, 14, PURPLE));
        b.addView(label(icon, 22, PURPLE, true, Gravity.CENTER));
        b.addView(label(name, 12, Color.WHITE, false, Gravity.CENTER));
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { show(page); }
        });
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1);
        p.setMargins(dp(4), dp(4), dp(4), dp(4));
        row.addView(b, p);
    }

    LinearLayout hrow() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        return r;
    }

    void pageHome() {
        content.addView(label("♛ QURESHI ROYAL ♛", 24, Color.WHITE, true, Gravity.CENTER), lp(2));
        content.addView(label("King of WhatsApp Bots", 13, GOLD, false, Gravity.CENTER));

        LinearLayout st = new LinearLayout(this);
        st.setOrientation(LinearLayout.VERTICAL);
        st.setGravity(Gravity.CENTER);
        st.setPadding(dp(14), dp(14), dp(14), dp(14));
        st.setBackground(shape(CARD, 18, PURPLE));
        st.addView(label("♛", 56, GOLD, true, Gravity.CENTER));
        st.addView(label("● " + sp.getString("status", "ربات آفلاین است"), 16, GREEN, true, Gravity.CENTER));
        content.addView(st, lp(8));

        LinearLayout stats = hrow();
        stats.setBackground(shape(CARD, 14, PURPLE));
        stats.setPadding(dp(4), dp(8), dp(4), dp(8));
        statBox(stats, "👥", "گروه‌ها", "0");
        statBox(stats, "💬", "چت خصوصی", "0");
        statBox(stats, "⏱", "فعالیت", "24h");
        content.addView(stats, lp(6));

        content.addView(button("⏻  خاموش/روشن کردن ربات", true, new View.OnClickListener() {
            public void onClick(View v) { show("connect"); }
        }), lp(6));
        content.addView(button("⚙  تنظیمات", false, new View.OnClickListener() {
            public void onClick(View v) { show("protect"); }
        }), lp(4));

        LinearLayout r1 = hrow();
        gridItem(r1, "👥", "مدیریت گروه", "groups");
        gridItem(r1, "📊", "آمار و گزارش", "logs");
        gridItem(r1, "✉", "پیام‌ها", "messages");
        content.addView(r1, lp(4));
        LinearLayout r2 = hrow();
        gridItem(r2, "◈", "اتصال", "connect");
        gridItem(r2, "🛡", "محافظت", "protect");
        gridItem(r2, "🏠", "خانه", "home");
        content.addView(r2, lp(0));
    }

    void pageConnect() {
        title("◈ اتصال به واتساپ");
        LinearLayout qr = new LinearLayout(this);
        qr.setOrientation(LinearLayout.VERTICAL);
        qr.setGravity(Gravity.CENTER);
        qr.setPadding(dp(14), dp(24), dp(14), dp(24));
        qr.setBackground(shape(CARD, 18, PURPLE));
        qr.addView(label("▦", 90, Color.WHITE, true, Gravity.CENTER));
        qr.addView(label("کد QR را اسکن کنید", 15, Color.WHITE, false, Gravity.CENTER));
        content.addView(qr, lp(6));

        content.addView(label("♛  یا کد جفت‌سازی را وارد کنید  ♛", 14, GOLD, false, Gravity.CENTER), lp(8));
        TextView code = label(sp.getString("pair_code", "--- ---"), 26, Color.WHITE, true, Gravity.CENTER);
        code.setBackground(shape(CARD, 14, PURPLE));
        content.addView(code, lp(4));
        content.addView(label("زمان انقضا: 10:00", 12, Color.LTGRAY, false, Gravity.CENTER));

        content.addView(label("شمارهٔ ربات (با کد کشور، بدون +)", 13, Color.LTGRAY, false, Gravity.START), lp(6));
        final EditText phone = input("93700000000", sp.getString("phone", ""), false);
        phone.setInputType(InputType.TYPE_CLASS_PHONE);
        content.addView(phone, lp(4));
        content.addView(button("ذخیره شماره", true, new View.OnClickListener() {
            public void onClick(View v) {
                String p = phone.getText().toString().replaceAll("[^0-9]", "");
                if (p.length() < 8) {
                    Toast.makeText(MainActivity.this, "شماره درست نیست", Toast.LENGTH_SHORT).show();
                    return;
                }
                sp.edit().putString("phone", p).putString("status", "شماره ثبت شد: " + p).apply();
                log("شماره ثبت شد");
                Toast.makeText(MainActivity.this, "ذخیره شد", Toast.LENGTH_SHORT).show();
            }
        }), lp(6));
        content.addView(button("↻  تجربهٔ دوباره", false, new View.OnClickListener() {
            public void onClick(View v) { show("connect"); }
        }), lp(4));
        content.addView(button("قطع اتصال و پاک‌کردن شماره", false, new View.OnClickListener() {
            public void onClick(View v) {
                sp.edit().remove("phone").putString("status", "ربات هنوز راه‌اندازی نشده است").apply();
                log("اتصال پاک شد");
                show("connect");
            }
        }), lp(4));
    }

    void pageProtect() {
        title("⚙ تنظیمات");
        toggle("antilink", "ضد لینک (Auto Remove)", true);
        toggle("antiads", "ضد تبلیغات", true);
        toggle("antirepeat", "ضد تکرار پیام", true);
        toggle("antiflood", "ضد اسپم و فلود", true);
        toggle("protectsettings", "قفل تنظیمات گروه", false);
        toggle("onlyadmin", "فقط مدیران", true);
        toggle("welcome", "پیام خوش‌آمدگویی", true);
        toggle("farewell", "پیام خداحافظی", true);
        content.addView(button("✉  ویرایش متن پیام‌ها", false, new View.OnClickListener() {
            public void onClick(View v) { show("messages"); }
        }), lp(6));
        content.addView(button("💾  ذخیره تنظیمات", true, new View.OnClickListener() {
            public void onClick(View v) {
                log("تنظیمات ذخیره شد");
                Toast.makeText(MainActivity.this, "ذخیره شد", Toast.LENGTH_SHORT).show();
            }
        }), lp(4));
    }

    void pageMessages() {
        title("✉ پیام‌های خودکار");
        content.addView(label("پیام خوش‌آمدگویی", 14, GOLD, true, Gravity.START));
        final EditText w = input("متن خوش‌آمدگویی",
                sp.getString("welcome_text", "به گروه خوش آمدی 👑"), true);
        content.addView(w, lp(4));
        content.addView(label("پیام خداحافظی", 14, GOLD, true, Gravity.START));
        final EditText f = input("متن خداحافظی",
                sp.getString("farewell_text", "خداحافظ، به امید دیدار 👋"), true);
        content.addView(f, lp(4));
        content.addView(button("ذخیره پیام‌ها", true, new View.OnClickListener() {
            public void onClick(View v) {
                sp.edit().putString("welcome_text", w.getText().toString())
                        .putString("farewell_text", f.getText().toString()).apply();
                log("پیام‌های خودکار ذخیره شد");
                Toast.makeText(MainActivity.this, "ذخیره شد", Toast.LENGTH_SHORT).show();
            }
        }), lp(8));
    }

    void actionItem(LinearLayout row, String icon, String name) {
        final String n = name;
        LinearLayout b = new LinearLayout(this);
        b.setOrientation(LinearLayout.VERTICAL);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(4), dp(16), dp(4), dp(16));
        b.setBackground(shape(CARD, 14, PURPLE));
        b.addView(label(icon, 26, PURPLE, true, Gravity.CENTER));
        b.addView(label(name, 14, Color.WHITE, false, Gravity.CENTER));
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                log("درخواست: " + n);
                Toast.makeText(MainActivity.this, n + " (بعد از اتصال سرور فعال می‌شود)", Toast.LENGTH_SHORT).show();
            }
        });
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1);
        p.setMargins(dp(4), dp(4), dp(4), dp(4));
        row.addView(b, p);
    }

    void pageGroups() {
        title("♜ مدیریت گروه");
        String[][] items = {
            {"🔒", "بستن گروه (Close)"}, {"🔓", "باز کردن گروه (Open)"},
            {"➕", "افزودن عضو"}, {"➖", "حذف عضو"},
            {"♛", "ارتقای مدیر"}, {"⬇", "تنزل مدیر"},
            {"ℹ", "اطلاعات گروه"}, {"✎", "تغییر نام و عکس"}};
        for (int i = 0; i < items.length; i += 2) {
            LinearLayout r = hrow();
            actionItem(r, items[i][0], items[i][1]);
            actionItem(r, items[i + 1][0], items[i + 1][1]);
            content.addView(r, lp(0));
        }
    }

    void pageLogs() {
        title("▤ گزارش‌ها");
        String logs = sp.getString("logs", "");
        if (logs.isEmpty()) {
            card("∅", "گزارشی وجود ندارد", null, null);
        } else {
            for (String line : logs.split("\n")) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setBackground(shape(CARD, 12, PURPLE));
                row.setPadding(dp(10), dp(8), dp(10), dp(8));
                row.addView(label("●", 18, GREEN, true, Gravity.CENTER));
                row.addView(label(line, 13, Color.WHITE, false, Gravity.START),
                        new LinearLayout.LayoutParams(0, -2, 1));
                content.addView(row, lp(3));
            }
            content.addView(button("پاک‌کردن گزارش‌ها", false, new View.OnClickListener() {
                public void onClick(View v) {
                    sp.edit().remove("logs").apply();
                    show("logs");
                }
            }), lp(8));
        }
    }

    // ---------- ناوبری ----------
    void show(String page) {
        current = page;
        content.removeAllViews();
        switch (page) {
            case "connect": pageConnect(); break;
            case "protect": pageProtect(); break;
            case "messages": pageMessages(); break;
            case "groups": pageGroups(); break;
            case "logs": pageLogs(); break;
            default: pageHome();
        }
        buildNav();
    }

    void navItem(final String page, String text) {
        TextView t = label(text, 12, current.equals(page) ? GOLD : Color.GRAY,
                current.equals(page), Gravity.CENTER);
        t.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { show(page); }
        });
        nav.addView(t, new LinearLayout.LayoutParams(0, dp(48), 1));
    }

    void buildNav() {
        nav.removeAllViews();
        navItem("home", "♛\nخانه");
        navItem("connect", "◈\nاتصال");
        navItem("protect", "🛡\nمحافظت");
        navItem("messages", "✉\nپیام");
        navItem("logs", "▤\nگزارش");
    }

    @Override
    public void onBackPressed() {
        if (!current.equals("home")) show("home");
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
        root.setPadding(dp(12), dp(10), dp(12), dp(4));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setBackground(shape(CARD, 16, PURPLE));
        root.addView(nav, lp(4));

        setContentView(root);
        show("home");
    }
}
