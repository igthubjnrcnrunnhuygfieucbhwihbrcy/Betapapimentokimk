package com.kim.papiamento;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Speaker.init(this);
        buildUi();
        maybeShowWhatsNew();
        UpdateChecker.checkAsync(new UpdateChecker.Callback() {
            @Override
            public void onResult(boolean updateAvailable, String notes, String apkUrl) {
                if (updateAvailable) UpdateChecker.showUpdateDialog(MainActivity.this, notes, apkUrl);
            }
        });
    }

    /** Eenmalige pop-up na het installeren van een nieuwe versie. */
    private void maybeShowWhatsNew() {
        android.content.SharedPreferences p = getSharedPreferences("progress", MODE_PRIVATE);
        if (p.getInt("seenVersion", 0) < UpdateChecker.CURRENT_VERSION) {
            p.edit().putInt("seenVersion", UpdateChecker.CURRENT_VERSION).apply();
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Nieuw in versie 1.2 \uD83C\uDF89")
                    .setMessage("\u2022 Update-pop-up: de app laat voortaan automatisch weten als er een nieuwe versie is\n"
                            + "\u2022 Alles uit v1.1: Duolingo-stijl, gemixte oefeningen, XP & streak, uitspraak \uD83D\uDD0A")
                    .setPositiveButton("Leuk!", null)
                    .show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        buildUi(); // ververs streak/XP bij terugkeer
    }

    private void buildUi() {
        LinearLayout root = Ui.root(this);

        root.addView(Ui.topBar(this, Progress.streak(this), Progress.xp(this)));

        TextView title = Ui.text(this, "Papiamento Blitz", 30f, true, Ui.PRIMARY);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, Ui.dp(this, 18), 0, 0);
        root.addView(title);

        LinearLayout bubble = Ui.card(this);
        bubble.addView(Ui.text(this,
                "🦜  Bon bini! Elke dag een klein beetje — dan praat je binnenkort Papiamento!",
                15f, false, Ui.TEXT));
        root.addView(bubble, Ui.cardParams(this));

        int due = WordBank.dueCount(this);
        int mastered = WordBank.masteredCount(this);
        int fresh = WordBank.newCount(this);

        LinearLayout stats = Ui.card(this);
        stats.addView(Ui.text(this,
                "✅ Vast in je lange termijn: " + mastered + " van " + WordBank.WORDS.length,
                14f, true, Ui.TEXT));
        stats.addView(Ui.text(this, "🌱 Nieuw: " + fresh + "   •   🔁 Te herhalen: " + due,
                14f, false, Ui.MUTED));
        LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 16));
        barLp.topMargin = Ui.dp(this, 10);
        stats.addView(Ui.progressBar(this, mastered / (float) WordBank.WORDS.length, Ui.PRIMARY), barLp);
        root.addView(stats, Ui.cardParams(this));

        Button hero = Ui.button(this, "⚡ SNELLE CURSUS — ±1 UUR", Ui.PRIMARY, 0xFFFFFFFF,
                v -> startActivity(new Intent(this, CrashCourseActivity.class)));
        LinearLayout.LayoutParams heroParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 62));
        heroParams.topMargin = Ui.dp(this, 20);
        root.addView(hero, heroParams);

        Button review = Ui.button(this,
                due > 0 ? "🔁 HERHALEN — " + due + " woorden klaar" : "🔁 HERHALEN — niets vervalt, top!",
                Ui.BLUE, 0xFFFFFFFF,
                v -> {
                    Intent i = new Intent(this, LessonActivity.class);
                    i.putExtra("mode", "review");
                    startActivity(i);
                });
        root.addView(review);

        root.addView(menuCard("📗", "Lessen oefenen",
                "8 lessen met een mix van oefeningen: kiezen, bouwen, typen", new Intent(this, LessonActivity.class)));

        root.addView(menuCard("🗣️", "Anti-stotter training",
                "Ademhaling, zachte aanloop en gelijkmatig tempo", new Intent(this, StutterActivity.class)));

        root.addView(menuCard("🧠", "Zo onthoudt je brein het",
                "Korte en lange termijn geheugen in 2 minuten uitgelegd", new Intent(this, BrainActivity.class)));

        TextView foot = Ui.text(this,
                "Elke sessie telt voor je 🔥 streak. 20 minuten, 2× per week is genoeg — de tussenpozen doen het werk.",
                13f, false, Ui.MUTED);
        foot.setGravity(Gravity.CENTER);
        foot.setPadding(0, Ui.dp(this, 20), 0, 0);
        root.addView(foot);

        setContentView(Ui.scroll(this, root));
    }

    private LinearLayout menuCard(String emoji, String title, String desc, Intent target) {
        LinearLayout card = Ui.card(this);
        card.setOnClickListener(v -> startActivity(target));
        card.setClickable(true);

        TextView t1 = Ui.text(this, emoji + "  " + title, 18f, true, Ui.TEXT);
        card.addView(t1);
        TextView t2 = Ui.text(this, desc, 14f, false, Ui.MUTED);
        t2.setPadding(0, Ui.dp(this, 4), 0, 0);
        card.addView(t2);
        return card;
    }
}
