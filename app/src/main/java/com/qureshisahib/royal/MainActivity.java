package com.qureshisahib.royal;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.*;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import org.json.JSONObject;

public class MainActivity extends Activity {
    static final int BG = Color.parseColor("#0B0B12"), GOLD = Color.parseColor("#FFD700"),
        SURF = Color.parseColor("#1A1A26"), GREEN = Color.parseColor("#00FF9C");

    private FrameLayout content;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private Runnable poll;
    private CommandCenter center;
    private Button[] tabs;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (Build.VERSION.SDK_INT >= 33)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        if (!CommandCenter.isRemote(this)) startForegroundService(new Intent(this, BotService.class));
        center = new CommandCenter(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(0, dp(36), 0, 0);

        content = new FrameLayout(this);
        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1f));

        LinearLayout nav = new LinearLayout(this);
        nav.setBackgroundColor(SURF);
        String[] names = {"Home", "WhatsApp", "Custom", "Terminal"};
        tabs = new Button[names.length];
        for (int i = 0; i < names.length; i++) {
            final int idx = i;
            tabs[i] = btn(names[i], v -> show(idx));
            nav.addView(tabs[i], new LinearLayout.LayoutParams(0, -2, 1f));
        }
        root.addView(nav);
        setContentView(root);
        show(0);
    }

    @Override protected void onDestroy() { super.onDestroy(); if (poll != null) ui.removeCallbacks(poll); }

    // ---------- navigation ----------
    private void show(int i) {
        if (poll != null) ui.removeCallbacks(poll);
        content.removeAllViews();
        View v = i == 0 ? home() : i == 1 ? whatsapp() : i == 2 ? custom() : terminal();
        content.addView(v);
        for (int k = 0; k < tabs.length; k++) tabs[k].setTextColor(k == i ? GOLD : Color.LTGRAY);
    }

    // ---------- Home ----------
    private View home() {
        LinearLayout l = col();
        l.addView(h1("♛ QURESHI ROYAL"));
        TextView st = txt("Loading...");
        st.setTextSize(16);
        l.addView(st);

        LinearLayout row = new LinearLayout(this);
        String[] acts = {"start", "stop", "restart"};
        for (String a : acts)
            row.addView(btn(a.substring(0, 1).toUpperCase() + a.substring(1), v -> bg(() -> {
                String r = center.run("bot " + a);
                runOnUiThread(() -> Toast.makeText(this, r, Toast.LENGTH_LONG).show());
            })), new LinearLayout.LayoutParams(0, -2, 1f));
        l.addView(row);
        l.addView(btn("Allow background (battery)", v -> center.run("battery")));

        poll = new Runnable() {
            @Override public void run() {
                bg(() -> {
                    String r = Api.call(MainActivity.this, "GET", "/api/bot/status", null);
                    String t;
                    if (r == null) t = "Engine: starting...\n(first run takes ~30 seconds)";
                    else try {
                        JSONObject j = new JSONObject(r);
                        t = "Engine: online\nWhatsApp: " + j.optString("status") + "\nBot: "
                            + (j.optBoolean("enabled") ? "ON" : "OFF") + "\nLinked: " + (j.optBoolean("linked") ? "yes" : "no");
                    } catch (Exception e) { t = "Status error"; }
                    final String f = t;
                    runOnUiThread(() -> st.setText(f));
                });
                ui.postDelayed(this, 3000);
            }
        };
        ui.post(poll);
        return l;
    }

    // ---------- WhatsApp ----------
    private View whatsapp() {
        LinearLayout l = col();
        l.addView(h1("WhatsApp"));
        l.addView(txt("1. Type the bot's number with country code (digits only).\n2. Tap 'Get code'.\n3. WhatsApp > Linked devices > Link with phone number > enter the code."));
        EditText num = edit("93700000000");
        num.setInputType(InputType.TYPE_CLASS_NUMBER);
        l.addView(num);
        TextView res = txt("");
        res.setTextSize(22); res.setTextColor(GOLD); res.setTextIsSelectable(true);
        l.addView(btn("Get code", v -> bg(() -> {
            String r = center.run("whatsapp pairing " + num.getText().toString().trim());
            runOnUiThread(() -> res.setText(r));
        })));
        l.addView(res);
        l.addView(btn("Disconnect / unlink", v -> bg(() -> {
            String r = center.run("whatsapp disconnect");
            runOnUiThread(() -> res.setText(r));
        })));
        return l;
    }

    // ---------- Customization ----------
    private View custom() {
        ScrollView sv = new ScrollView(this);
        LinearLayout l = col();
        sv.addView(l);
        l.addView(h1("Customization"));

        EditText name = edit("Bot name"), prefix = edit("Prefix (e.g. !)"),
            welcome = edit("Welcome (@user, @group)"), goodbye = edit("Goodbye (@user)");
        Switch link = sw("Anti-link"), spam = sw("Anti-spam"), flood = sw("Anti-flood");
        for (View v : new View[]{name, prefix, welcome, goodbye, link, spam, flood}) l.addView(v);

        bg(() -> {
            String r = Api.call(this, "GET", "/api/settings", null);
            if (r == null) return;
            try {
                JSONObject j = new JSONObject(r), p = j.optJSONObject("protection");
                runOnUiThread(() -> {
                    name.setText(j.optString("botName")); prefix.setText(j.optString("prefix"));
                    welcome.setText(j.optString("welcome")); goodbye.setText(j.optString("goodbye"));
                    if (p != null) { link.setChecked(p.optBoolean("antilink")); spam.setChecked(p.optBoolean("antispam")); flood.setChecked(p.optBoolean("antiflood")); }
                });
            } catch (Exception ignored) {}
        });

        l.addView(btn("Save", v -> bg(() -> {
            try {
                JSONObject body = new JSONObject()
                    .put("botName", name.getText().toString()).put("prefix", prefix.getText().toString())
                    .put("welcome", welcome.getText().toString()).put("goodbye", goodbye.getText().toString())
                    .put("protection", new JSONObject().put("antilink", link.isChecked())
                        .put("antispam", spam.isChecked()).put("antiflood", flood.isChecked()));
                String r = Api.call(this, "PUT", "/api/settings", body.toString());
                runOnUiThread(() -> Toast.makeText(this, r == null ? "Engine not ready" : "Saved", Toast.LENGTH_SHORT).show());
            } catch (Exception ignored) {}
        })));

        l.addView(h1("Custom commands"));
        EditText cn = edit("Command name (e.g. rules)"), cr = edit("Reply text");
        TextView list = txt("");
        l.addView(cn); l.addView(cr);
        l.addView(btn("Add / update", v -> bg(() -> {
            String r = center.run("commands create " + cn.getText().toString().trim() + " " + cr.getText().toString().trim());
            runOnUiThread(() -> Toast.makeText(this, r, Toast.LENGTH_SHORT).show());
        })));
        l.addView(btn("Show list", v -> bg(() -> {
            String r = center.run("commands list");
            runOnUiThread(() -> list.setText(r));
        })));
        l.addView(list);
        return sv;
    }

    // ---------- Terminal ----------
    private View terminal() {
        LinearLayout l = col();
        l.addView(h1("Command Center"));
        ScrollView scroll = new ScrollView(this);
        TextView out = txt("Type 'help'.\n");
        out.setTextColor(GREEN); out.setTypeface(Typeface.MONOSPACE); out.setTextIsSelectable(true);
        scroll.addView(out);
        l.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        EditText in = edit("royal> ");
        in.setSingleLine(true);
        in.setImeOptions(EditorInfo.IME_ACTION_SEND);
        in.setOnEditorActionListener((v, id, e) -> {
            String line = in.getText().toString();
            in.setText("");
            out.append("royal> " + line + "\n");
            bg(() -> {
                String r = center.run(line);
                runOnUiThread(() -> { if (!r.isEmpty()) out.append(r + "\n"); scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN)); });
            });
            return true;
        });
        l.addView(in);
        return l;
    }

    // ---------- helpers ----------
    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }
    private void bg(Runnable r) { new Thread(r).start(); }
    private LinearLayout col() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(16), dp(8), dp(16), dp(8));
        return l;
    }
    private TextView h1(String s) {
        TextView t = new TextView(this); t.setText(s); t.setTextColor(GOLD); t.setTextSize(20);
        t.setPadding(0, dp(12), 0, dp(8)); return t;
    }
    private TextView txt(String s) {
        TextView t = new TextView(this); t.setText(s); t.setTextColor(Color.WHITE);
        t.setPadding(0, dp(6), 0, dp(6)); return t;
    }
    private Button btn(String s, View.OnClickListener c) {
        Button b = new Button(this); b.setText(s); b.setAllCaps(false);
        b.setTextColor(Color.LTGRAY); b.setBackgroundColor(SURF); b.setOnClickListener(c);
        return b;
    }
    private EditText edit(String hint) {
        EditText e = new EditText(this); e.setHint(hint);
        e.setTextColor(Color.WHITE); e.setHintTextColor(Color.GRAY); return e;
    }
    private Switch sw(String s) {
        Switch w = new Switch(this); w.setText(s); w.setTextColor(Color.WHITE);
        w.setPadding(0, dp(8), 0, dp(8)); return w;
    }
}
