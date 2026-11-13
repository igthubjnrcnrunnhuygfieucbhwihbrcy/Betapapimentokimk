package com.kim.papiamento;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Duolingo-achtige stijl: witte achtergrond, dikke ronde knoppen met een
 * donkere onderrand (3D-effect), ronde kaarten met randje, groene
 * voortgangsbalkjes.
 */
public class Ui {

    public static final int BG = Color.parseColor("#FFFFFF");
    public static final int CARD = Color.parseColor("#F7F7F7");
    public static final int BORDER = Color.parseColor("#E5E5E5");
    public static final int PRIMARY = Color.parseColor("#58CC02");
    public static final int PRIMARY_DARK = Color.parseColor("#46A302");
    public static final int BLUE = Color.parseColor("#1CB0F6");
    public static final int BLUE_DARK = Color.parseColor("#1899D6");
    public static final int YELLOW = Color.parseColor("#FFC800");
    public static final int ORANGE = Color.parseColor("#FF9600");
    public static final int RED = Color.parseColor("#FF4B4B");
    public static final int RED_DARK = Color.parseColor("#EA2B2B");
    public static final int GOOD = Color.parseColor("#58CC02");
    public static final int BAD = Color.parseColor("#FF4B4B");
    public static final int TEXT = Color.parseColor("#4B4B4B");
    public static final int MUTED = Color.parseColor("#AFAFAF");
    public static final int GREEN_SOFT = Color.parseColor("#D7FFB8");
    public static final int RED_SOFT = Color.parseColor("#FFDFE0");

    public static LinearLayout root(Context c) {
        LinearLayout root = new LinearLayout(c);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(c, 20), dp(c, 20), dp(c, 20), dp(c, 24));
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
        return card(c, CARD);
    }

    public static LinearLayout card(Context c, int color) {
        LinearLayout card = new LinearLayout(c);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(c, 18), dp(c, 18), dp(c, 18), dp(c, 18));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(c, 16));
        bg.setStroke(dp(c, 2), BORDER);
        card.setBackground(bg);
        return card;
    }

    public static LinearLayout.LayoutParams cardParams(Context c) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(c, 14);
        return lp;
    }

    public static int darken(int color, float factor) {
        int r = Math.round(((color >> 16) & 0xFF) * factor);
        int g = Math.round(((color >> 8) & 0xFF) * factor);
        int b = Math.round((color & 0xFF) * factor);
        return Color.rgb(r, g, b);
    }

    public static GradientDrawable rounded(int color, float radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radiusDp);
        return g;
    }

    public static GradientDrawable outlined(Context c, int color, int strokeColor) {
        GradientDrawable g = rounded(color, dp(c, 12));
        g.setStroke(dp(c, 2), strokeColor);
        return g;
    }

    /** Dikke 3D-knop zoals Duolingo: kleur bovenop een donkere onderrand. */
    public static Button button(Context c, String label, int bgColor, int textColor, View.OnClickListener onClick) {
        Button b = new Button(c);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(16f);
        b.setTextColor(textColor);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(chunky(bgColor, dp(c, 16), dp(c, 5)));
        b.setPadding(dp(c, 12), 0, dp(c, 12), dp(c, 5));
        b.setOnClickListener(onClick);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(c, 56));
        lp.topMargin = dp(c, 10);
        b.setLayoutParams(lp);
        return b;
    }

    public static LayerDrawable chunky(int color, float radiusDp, int bottomDp) {
        GradientDrawable bottom = new GradientDrawable();
        bottom.setColor(darken(color, 0.78f));
        bottom.setCornerRadius(radiusDp);
        GradientDrawable top = new GradientDrawable();
        top.setColor(color);
        top.setCornerRadius(radiusDp);
        LayerDrawable ld = new LayerDrawable(new android.graphics.drawable.Drawable[]{bottom, top});
        ld.setLayerInset(1, 0, 0, 0, bottomDp);
        return ld;
    }

    /** Horizontale voortgangsbalk (0..1) in Duolingo-stijl. */
    public static LinearLayout progressBar(Context c, float fraction, int color) {
        LinearLayout bar = new LinearLayout(c);
        bar.setBackground(rounded(BORDER, dp(c, 8)));
        bar.setPadding(dp(c, 3), dp(c, 3), dp(c, 3), dp(c, 3));
        float f = Math.max(0f, Math.min(1f, fraction));
        if (f > 0.01f) {
            LinearLayout fill = new LinearLayout(c);
            fill.setBackground(rounded(color, dp(c, 6)));
            bar.addView(fill, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, f));
        }
        LinearLayout rest = new LinearLayout(c);
        bar.addView(rest, new LinearLayout.LayoutParams(0, 1, Math.max(0.001f, 1 - f)));
        return bar;
    }

    /** Bovenbalk met streak en XP. */
    public static LinearLayout topBar(Context c, int streak, int xp) {
        LinearLayout h = new LinearLayout(c);
        h.setOrientation(LinearLayout.HORIZONTAL);
        TextView left = text(c, "🔥 " + streak, 16f, true, ORANGE);
        TextView right = text(c, "⚡ " + xp + " XP", 16f, true, YELLOW);
        right.setGravity(Gravity.END);
        h.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        h.addView(right, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        return h;
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
