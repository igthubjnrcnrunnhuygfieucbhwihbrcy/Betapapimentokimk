package com.kim.papiamento;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class Ui {

    public static final int BG = Color.parseColor("#FFF7EE");
    public static final int CARD = Color.parseColor("#FFFFFF");
    public static final int PRIMARY = Color.parseColor("#0E7C7B");
    public static final int ACCENT = Color.parseColor("#F28C28");
    public static final int TEXT = Color.parseColor("#22313F");
    public static final int MUTED = Color.parseColor("#6B7A8A");
    public static final int GOOD = Color.parseColor("#2E7D32");
    public static final int BAD = Color.parseColor("#C62828");

    public static LinearLayout root(Context c) {
        LinearLayout root = new LinearLayout(c);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(c, 20), dp(c, 24), dp(c, 20), dp(c, 24));
        return root;
    }

    public static ScrollView scroll(Context c, LinearLayout content) {
        ScrollView sv = new ScrollView(c);
        sv.addView(content);
        sv.setFillViewport(true);
        return sv;
    }

    public static TextView text(Context c, String s, float sp, boolean bold, int color) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(sp);
        t.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        t.setTextColor(color);
        t.setLineSpacing(dp(c, 3), 1f);
        return t;
    }

    public static LinearLayout card(Context c) {
        LinearLayout card = new LinearLayout(c);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(c, 18), dp(c, 18), dp(c, 18), dp(c, 18));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(CARD);
        bg.setCornerRadius(dp(c, 16));
        card.setBackground(bg);
        return card;
    }

    public static LinearLayout.LayoutParams cardParams(Context c) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(c, 14);
        return lp;
    }

    public static Button button(Context c, String label, int bgColor, int textColor, View.OnClickListener onClick) {
        Button b = new Button(c);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(17f);
        b.setTextColor(textColor);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(rounded(bgColor, dp(c, 14)));
        b.setOnClickListener(onClick);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(c, 54));
        lp.topMargin = dp(c, 10);
        b.setLayoutParams(lp);
        return b;
    }

    public static GradientDrawable rounded(int color, float radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radiusDp);
        return g;
    }

    public static TextView bigCardText(Context c, String s, float sp, int color) {
        TextView t = text(c, s, sp, true, color);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    public static int dp(Context c, int v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }
}
