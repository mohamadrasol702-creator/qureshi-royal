package com.qureshisahib.royal;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {

    final int BG = Color.rgb(8,5,15);
    final int CARD = Color.rgb(24,17,36);
    final int PURPLE = Color.rgb(112,0,255);
    final int GOLD = Color.rgb(238,195,95);
    final int WHITE = Color.WHITE;
    final int GRAY = Color.LTGRAY;

    LinearLayout root, content, nav;
    ScrollView scroll;
    SharedPreferences sp;
    String current = "home";

    int dp(int n) {
        return (int)(n * getResources().getDisplayMetrics().density + .5f);
    }

    GradientDrawable bg(int color, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{color, Color.rgb(17,9,29)}
        );
        d.setCornerRadius(dp(radius));
        if(stroke != 0) d.setStroke(dp(1),stroke);
        return d;
    }

    TextView text(String s,int size,int color,boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setTypeface(Typeface.DEFAULT,bold?1:0);
        t.setPadding(dp(5),dp(5),dp(5),dp(5));
        return t;
    }

    LinearLayout.LayoutParams params(int top) {
        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1,-2);
        p.setMargins(0,dp(top),0,dp(top));
        return p;
    }

    void addTitle(String title) {
        TextView t = text(title,21,GOLD,true);
        content.addView(t,params(12));
    }

    void header() {
        LinearLayout h = new LinearLayout(this);
        h.setOrientation(1);
        h.setGravity(Gravity.CENTER);
        h.setPadding(dp(8),dp(12),dp(8),dp(12));
        h.setBackground(bg(CARD,PURPLE,20));

        h.addView(text("♛  QURESHI ROYAL  ♛",23,GOLD,true));
        h.addView(text("King of WhatsApp Bots",13,GRAY,false));

        content.addView(h,params(5));
    }

    void card(String icon,String title,String subtitle,
              View.OnClickListener click) {

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(1);
        c.setGravity(Gravity.CENTER);
        c.setPadding(dp(12),dp(12),dp(12),dp(12));
        c.setBackground(bg(CARD,PURPLE,17));

        c.addView(text(icon,25,GOLD,true));
        c.addView(text(title,15,WHITE,true));

        if(subtitle != null)
            c.addView(text(subtitle,12,GRAY,false));

        if(click != null) c.setOnClickListener(click);

        content.addView(c,params(5));
    }

    void grid(String icon,String title,String subtitle,
              final String page) {

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(1);
        c.setGravity(Gravity.CENTER);
        c.setPadding(dp(5),dp(12),dp(5),dp(12));
        c.setBackground(bg(CARD,PURPLE,16));

        c.addView(text(icon,25,GOLD,true));
        c.addView(text(title,13,WHITE,true));
        if(subtitle != null)
            c.addView(text(subtitle,11,GRAY,false));

        c.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                show(page);
            }
        });

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(0,-2,1);
        p.setMargins(dp(3),dp(4),dp(3),dp(4));
        gridRow.addView(c,p);
    }

    LinearLayout gridRow;

    void startGrid() {
        gridRow = new LinearLayout(this);
        gridRow.setOrientation(LinearLayout.HORIZONTAL);
        content.addView(gridRow,params(3));
    }

    void endGrid() {
        gridRow = null;
    }

    void stat(String icon,String number,String name) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(1);
        c.setGravity(Gravity.CENTER);
        c.setPadding(dp(5),dp(10),dp(5),dp(10));
        c.setBackground(bg(CARD,PURPLE,15));

        c.addView(text(icon,22,PURPLE,true));
        c.addView(text(number,18,GOLD,true));
        c.addView(text(name,12,WHITE,false));

        stats.addView(c,new LinearLayout.LayoutParams(0,-2,1));
    }

    LinearLayout stats;

    void button(String title,boolean primary,Runnable action) {
        Button b = new Button(this);
        b.setText(title);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setTextColor(primary?WHITE:GOLD);
        b.setTypeface(null,Typeface.BOLD);
        b.setBackground(bg(primary?PURPLE:CARD,GOLD,25));
        b.setOnClickListener(v -> action.run());
        content.addView(b,params(6));
    }

    void pageHome() {
        header();

        LinearLayout status = new LinearLayout(this);
        status.setOrientation(1);
        status.setGravity(Gravity.CENTER);
        status.setPadding(dp(10),dp(12),dp(10),dp(12));
        status.setBackground(bg(CARD,PURPLE,18));

        status.addView(text("♛",32,GOLD,true));
        status.addView(text((sp.getBoolean("online",false)?"🟢  ربات آنلاین است":"🔴  ربات آفلاین است"),16,WHITE,true));
        status.addView(text((sp.getBoolean("online",false)?"Connected":"Offline"),12,GRAY,false));

        content.addView(status,params(8));

        stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        stats.setGravity(Gravity.CENTER);
        content.addView(stats,params(8));

        stat("👥","0","گروه‌ها");
        stat("💬","0","چت خصوصی");
        stat("◷","24h","فعالیت");

        button("⏻   خاموش کردن ربات",true,() -> {
            Toast.makeText(this,"موتور هنوز متصل نشده است",Toast.LENGTH_SHORT).show();
        });

        button("⚙   تنظیمات",false,() -> show("settings"));

        addTitle("مرکز مدیریت سلطنتی");

        startGrid();
        grid("👥","مدیریت گروه","گروه‌ها","groups");
        grid("▥","آمار و گزارش","فعالیت","logs");
        grid("ⓘ","درباره ما","اطلاعات","about");
        endGrid();

        startGrid();
        grid("▣","اتصال واتساپ","QR / Pairing","connect");
        grid("🛡","امنیت","محافظت","protect");
        grid("⚙","تنظیمات","Settings","settings");
        endGrid();

        startGrid();
        grid("♛","مرکز فرمان","Commands","commands");
        grid("✉","پیام‌ها","متن‌ها","messages");
        grid("⏻","خروج","Exit","exit");
        endGrid();
    }

    void pageConnect() {
        addTitle("♛ اتصال به واتساپ");

        card("◈","اتصال با QR",
             "کد QR پس از اتصال موتور نمایش داده می‌شود",null);

        card("▦","QR CODE",
             "فعلاً موتور واتساپ متصل نیست",null);

        final EditText phone = input("شماره واتساپ با کد کشور");
        phone.setInputType(3);
        content.addView(phone,params(8));

        button("ذخیره شماره",true,() -> {
            String p = phone.getText().toString().replaceAll("\\D","");
            if(p.length()<8) {
                toast("شماره صحیح نیست");
                return;
            }
            sp.edit().putString("phone",p).apply(); log("شماره ثبت شد");
            toast("شماره ذخیره شد");
        });

        final EditText code = input("کد جفت‌سازی");
        content.addView(code,params(8));

        button("تأیید کد",false,() ->
            toast("اتصال واقعی در مرحله موتور فعال می‌شود"));
    }

    EditText input(String hint) {
        EditText e = new EditText(this);
        e.setSingleLine(true);
        e.setHint(hint);
        e.setTextColor(WHITE);
        e.setHintTextColor(GRAY);
        e.setTextSize(14);
        e.setPadding(dp(14),dp(12),dp(14),dp(12));
        e.setBackground(bg(CARD,PURPLE,15));
        return e;
    }

    void toggle(String key,String name,boolean def) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(0);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12),dp(9),dp(12),dp(9));
        row.setBackground(bg(CARD,PURPLE,13));

        TextView t = text(name,14,WHITE,true);
        t.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);

        row.addView(t,new LinearLayout.LayoutParams(0,-2,1));

        Switch s = new Switch(this);
        s.setChecked(sp.getBoolean(key,def));

        s.setOnCheckedChangeListener((v,on) -> { sp.edit().putBoolean(key,on).apply(); log((on?"فعال شد: ":"غیرفعال شد: ")+name); });

        row.addView(s);
        content.addView(row,params(5));
    }

    void pageProtect() {
        addTitle("🛡 تنظیمات امنیتی");

        String[] keys = {
            "antilink","antiads","antiflood","antirepeat",
            "onlyadmin","welcome","farewell","protectsettings"
        };

        String[] names = {
            "حذف خودکار لینک","ضد تبلیغات","ضد سیل پیام",
            "ضد پیام تکراری","فقط مدیران","خوش‌آمدگویی",
            "پیام خداحافظی","قفل تنظیمات گروه"
        };

        boolean[] defaults = {true,true,true,true,false,true,true,true};

        for(int i=0;i<keys.length;i++)
            toggle(keys[i],names[i],defaults[i]);

        button("💾 ذخیره تنظیمات",true,() ->
            toast("تنظیمات ذخیره شد"));
    }

    void groupTile(String icon,String name) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(1);
        c.setGravity(Gravity.CENTER);
        c.setPadding(dp(8),dp(15),dp(8),dp(15));
        c.setBackground(bg(CARD,PURPLE,15));

        c.addView(text(icon,26,GOLD,true));
        c.addView(text(name,13,WHITE,true));

        gridRow.addView(c,new LinearLayout.LayoutParams(0,-2,1));
    }

    void pageGroups() {
        addTitle("♛ مدیریت گروه‌ها");

        startGrid();
        groupTile("🔒","بستن گروه");
        groupTile("🔓","باز کردن گروه");
        endGrid();

        startGrid();
        groupTile("➕","افزودن عضو");
        groupTile("➖","حذف عضو");
        endGrid();

        startGrid();
        groupTile("♛","ارتقای مدیر");
        groupTile("⬇","تنزل مدیر");
        endGrid();

        startGrid();
        groupTile("ⓘ","اطلاعات گروه");
        groupTile("✎","تغییر نام و عکس");
        endGrid();

        card("ℹ","هنوز گروهی متصل نیست",
            "پس از اتصال موتور، گروه‌ها اینجا نمایش داده می‌شوند",null);
    }

    void pageSettings() {
        addTitle("♛ تنظیمات QURESHI ROYAL");

        card("🌐","زبان برنامه","فارسی / دری",null);
        card("🔔","اعلان‌ها","تنظیم اعلان‌های برنامه",null);
        card("🔐","امنیت برنامه","حفاظت از دسترسی",null);
        card("♛","ظاهر برنامه","Royal Black & Purple",null);
        card("💾","پشتیبان‌گیری","ذخیره تنظیمات",null);
        card("ⓘ","نسخه برنامه","QURESHI ROYAL v1.0",null);
    }

    void pageLogs() {
        addTitle("♛ گزارش فعالیت‌ها");

        String logs = sp.getString("logs","");
        if(logs.isEmpty()) {
            card("✓","گزارشی وجود ندارد",
                "گزارش‌ها پس از فعال‌شدن سیستم ثبت می‌شوند",null);
        } else {
            for(String line:logs.split("\n"))
                card("●",line,null,null);
        }
    }

    void pageAbout() {
        addTitle("♛ درباره QURESHI ROYAL");
        card("👑","QURESHI ROYAL",
            "King of WhatsApp Bots",null);
        card("♛","طراحی و ترتیب",
            "ارسلان عربزاده",null);
        card("ⓘ","نسخه","1.0",null);
    }

    void pageCommands() {
        addTitle("♛ مرکز فرمان");

        final EditText command = input("فرمان را وارد کنید");
        content.addView(command,params(8));

        button("اجرای فرمان",true,() ->
            toast("مرکز فرمان بعداً به موتور وصل می‌شود"));
    }

    void pageHelp() {
        addTitle("♛ راهنمای برنامه");
        card("◈","اتصال واتساپ","صفحه اتصال و جفت‌سازی",null);
        card("🛡","امنیت","مدیریت تنظیمات محافظت",null);
        card("👥","گروه‌ها","مدیریت اعضا و مدیران",null);
    }

    void log(String msg) {
        String t = new java.text.SimpleDateFormat("MM/dd HH:mm", java.util.Locale.US).format(new java.util.Date());
        String old = sp.getString("logs", "");
        String[] l = (t + "  " + msg + (old.isEmpty() ? "" : "\n" + old)).split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(l.length, 50); i++) { if (i > 0) sb.append("\n"); sb.append(l[i]); }
        sp.edit().putString("logs", sb.toString()).apply();
    }

    EditText field(String label, String key, String def, boolean multi) {
        content.addView(text(label, 13, GOLD, true), params(2));
        EditText e = input(label);
        e.setText(sp.getString(key, def));
        if (multi) { e.setSingleLine(false); e.setMinLines(2); }
        content.addView(e, params(3));
        return e;
    }

    void pageMessages() {
        addTitle("✉ پیام‌ها و متن‌ها");
        final EditText pre = field("پیشوند دستورها", "prefix", "!", false);
        final EditText w = field("متن خوش‌آمدگویی", "welcome_text", "به گروه @group خوش آمدی @user 👑", true);
        final EditText f = field("متن خداحافظی", "farewell_text", "خداحافظ @user، به امید دیدار 👋", true);
        final EditText a = field("پیام اخطار", "warn_text", "⚠️ @user اخطار @n از 3", true);
        button("💾 ذخیره", true, () -> {
            sp.edit().putString("prefix", pre.getText().toString())
              .putString("welcome_text", w.getText().toString())
              .putString("farewell_text", f.getText().toString())
              .putString("warn_text", a.getText().toString()).apply();
            log("متن‌ها ذخیره شد");
            toast("ذخیره شد");
        });
    }

    void toast(String s) {
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
    }

    void show(String page) {
        current=page;
        content.removeAllViews();

        switch(page) {
            case "connect": pageConnect(); break;
            case "protect": pageProtect(); break;
            case "groups": pageGroups(); break;
            case "settings": pageSettings(); break;
            case "logs": pageLogs(); break;
            case "about": pageAbout(); break;
            case "commands": pageCommands(); break;
            case "help": pageHelp(); break;
            case "messages": pageMessages(); break;
            case "exit": finish(); return;
            default: pageHome();
        }

        buildNav();
        scroll.post(() -> scroll.scrollTo(0,0));
    }

    void navItem(String page,String icon,String name) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(1);
        item.setGravity(Gravity.CENTER);

        int color=current.equals(page)?GOLD:GRAY;

        item.addView(text(icon,20,color,true));
        item.addView(text(name,10,color,true));

        item.setOnClickListener(v -> show(page));

        nav.addView(item,new LinearLayout.LayoutParams(0,dp(55),1));
    }

    void buildNav() {
        nav.removeAllViews();

        navItem("home","♛","خانه");
        navItem("groups","👥","گروه‌ها");
        navItem("settings","⚙","تنظیمات");
        navItem("logs","▤","گزارش");
        navItem("about","ⓘ","درباره");
    }

    @Override
    public void onBackPressed() {
        if(!current.equals("home")) show("home");
        else super.onBackPressed();
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        sp=getSharedPreferences("royal",MODE_PRIVATE);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setLayoutDirection(
            View.LAYOUT_DIRECTION_RTL
        );

        root=new LinearLayout(this);
        root.setOrientation(1);
        root.setBackgroundColor(BG);
        root.setPadding(dp(10),dp(8),dp(10),dp(5));

        scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);

        content=new LinearLayout(this);
        content.setOrientation(1);
        content.setPadding(dp(3),dp(3),dp(3),dp(8));

        scroll.addView(content);

        root.addView(scroll,
            new LinearLayout.LayoutParams(-1,0,1));

        nav=new LinearLayout(this);
        nav.setOrientation(0);
        nav.setGravity(Gravity.CENTER);
        nav.setBackground(bg(CARD,PURPLE,18));

        root.addView(nav,params(5));

        setContentView(root);
        show("home");
    }
}
