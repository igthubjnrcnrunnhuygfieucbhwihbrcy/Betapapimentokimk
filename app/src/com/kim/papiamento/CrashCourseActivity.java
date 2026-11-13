package com.kim.papiamento;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Snelle 1-dags cursus: ±60 minuten, 4 blokken van ±10 minuten met 1 pauze
 * van 5 minuten. Elk blok: nieuwe woorden kennismaken + gemixte oefeningen +
 * micro-herhaling van het vorige blok. Daarna een eindtest over alles.
 */
public class CrashCourseActivity extends Activity {

    private static final int BLOCK_WORDS = 8;

    private LinearLayout content;
    private int block = 0;
    private int totalXp = 0, totalAsked = 0, totalCorrect = 0;
    private final List<Integer> allLearned = new ArrayList<>();

    private final Handler handler = new Handler();
    private Runnable pauseTicker;
    private long pauseEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Speaker.init(this);
        content = Ui.root(this);
        setContentView(Ui.scroll(this, content));
        buildIntro();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pauseTicker != null) handler.removeCallbacks(pauseTicker);
    }

    private List<Integer> blockIndexes(int b) {
        List<Integer> idx = new ArrayList<>();
        for (int i = b * BLOCK_WORDS; i < Math.min((b + 1) * BLOCK_WORDS, WordBank.WORDS.length); i++) {
            idx.add(i);
        }
        return idx;
    }

    // ------------------------------------------------------------------

    private void buildIntro() {
        if (pauseTicker != null) handler.removeCallbacks(pauseTicker);
        content.removeAllViews();

        TextView back = Ui.text(this, "←  Terug naar menu", 15f, true, Ui.BLUE);
        back.setOnClickListener(v -> finish());
        content.addView(back);

        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 20), Ui.dp(this, 24), Ui.dp(this, 20), Ui.dp(this, 24));
        card.addView(Ui.text(this, "⚡ Snelle 1-dags cursus", 24f, true, Ui.TEXT));

        TextView body = Ui.text(this,
                "In ±60 minuten praat je de basis van het Papiamento.\n\n"
                        + "1️⃣  Blok 1 — 8 nieuwe woorden + oefeningen\n"
                        + "2️⃣  Blok 2 — 8 nieuw + herhaling van blok 1\n"
                        + "☕  Pauze — 5 minuten (je brein verwerkt alles)\n"
                        + "3️⃣  Blok 3 — 8 nieuw + herhaling van blok 2\n"
                        + "4️⃣  Blok 4 — 8 nieuw + herhaling van blok 3\n"
                        + "🏁  Eindtest — alle 32 woorden door elkaar\n\n"
                        + "Waarom dit werkt: je werkgeheugen houdt iets maar 15–30 seconden vast. "
                        + "Door net op tijd te herhalen (2 min, 10 min) en daarna met tussenpozen "
                        + "(1, 3, 7, 14, 30 dagen) schuift het van korte naar lange termijn.",
                15f, false, Ui.TEXT);
        body.setPadding(0, Ui.dp(this, 12), 0, 0);
        card.addView(body);
        content.addView(card, Ui.cardParams(this));

        content.addView(Ui.button(this, "START CURSUS ▶", Ui.PRIMARY, 0xFFFFFFFF, v -> {
            block = 0;
            totalXp = 0;
            totalAsked = 0;
            totalCorrect = 0;
            allLearned.clear();
            startBlock();
        }));
    }

    // ------------------------------------------------------------------

    private void startBlock() {
        if (pauseTicker != null) handler.removeCallbacks(pauseTicker);
        content.removeAllViews();

        TextView t = Ui.text(this, "Blok " + (block + 1) + " van 4  •  ±10 minuten", 16f, true, Ui.MUTED);
        t.setGravity(Gravity.CENTER);
        content.addView(t);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        content.addView(box);

        List<Integer> newWords = blockIndexes(block);
        allLearned.addAll(newWords);

        List<Integer> review = new ArrayList<>();
        if (block > 0) {
            List<Integer> prev = blockIndexes(block - 1);
            Collections.shuffle(prev);
            review.addAll(prev.subList(0, Math.min(4, prev.size())));
        }

        new SessionRunner(this, box, new Runnable() {
            @Override
            public void run() {
                buildIntro();
            }
        }, new SessionRunner.Callback() {
            @Override
            public void onDone(int xp, int asked, int correct, List<Integer> missed) {
                totalXp += xp;
                totalAsked += asked;
                totalCorrect += correct;
                Progress.addXp(CrashCourseActivity.this, xp);
                Progress.touchStreak(CrashCourseActivity.this);
                block++;
                if (block == 2) {
                    showPause();
                } else if (block >= 4) {
                    startFinal();
                } else {
                    showBlockDone();
                }
            }
        }).start(newWords, review);
    }

    private void showBlockDone() {
        content.removeAllViews();

        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 24), Ui.dp(this, 32), Ui.dp(this, 24), Ui.dp(this, 32));
        card.addView(Ui.bigCardText(this, "✅ Blok " + block + " klaar!", 26f, Ui.PRIMARY));
        TextView body = Ui.text(this,
                "⚡ +" + totalXp + " XP tot nu toe\n\nEven doorlopen? Elk blok begint met een "
                        + "mini-herhaling — dat is precies wat je brein nodig heeft om alles vast te houden.",
                16f, false, Ui.TEXT);
        body.setGravity(Gravity.CENTER);
        body.setPadding(0, Ui.dp(this, 14), 0, 0);
        card.addView(body);
        content.addView(card, Ui.cardParams(this));

        content.addView(Ui.button(this, "VOLGENDE BLOK ▶", Ui.PRIMARY, 0xFFFFFFFF, v -> startBlock()));
    }

    // ------------------------------------------------------------------

    private void showPause() {
        if (pauseTicker != null) handler.removeCallbacks(pauseTicker);
        content.removeAllViews();

        TextView t = Ui.text(this, "Pauze", 16f, true, Ui.MUTED);
        t.setGravity(Gravity.CENTER);
        content.addView(t);

        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 24), Ui.dp(this, 32), Ui.dp(this, 24), Ui.dp(this, 32));
        card.addView(Ui.bigCardText(this, "☕ Pauze van 5 minuten", 24f, Ui.TEXT));

        final TextView countdown = Ui.bigCardText(this, "5:00", 44f, Ui.BLUE);
        countdown.setPadding(0, Ui.dp(this, 18), 0, 0);
        card.addView(countdown);

        TextView body = Ui.text(this,
                "Sta op, loop even, kijk uit het raam. Doe NIETS met leren.\n\n"
                        + "Terwijl jij pauzeert, rangschikt je brein net geleerde woorden. Zo'n korte "
                        + "rust is bewezen een van de beste manieren om iets vast te zetten.",
                15f, false, Ui.MUTED);
        body.setGravity(Gravity.CENTER);
        body.setPadding(0, Ui.dp(this, 14), 0, 0);
        card.addView(body);
        content.addView(card, Ui.cardParams(this));

        content.addView(Ui.button(this, "Pauze overslaan ▶", 0xFFFFFFFF, Ui.BLUE, v -> startBlock()));

        pauseEnd = System.currentTimeMillis() + 5L * 60 * 1000;
        pauseTicker = new Runnable() {
            @Override
            public void run() {
                long left = pauseEnd - System.currentTimeMillis();
                if (left <= 0) {
                    startBlock();
                    return;
                }
                long min = left / 60000;
                long sec = (left / 1000) % 60;
                countdown.setText(String.format("%d:%02d", min, sec));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(pauseTicker);
    }

    // ------------------------------------------------------------------

    private void startFinal() {
        content.removeAllViews();

        TextView t = Ui.text(this, "🏁 Eindtest  •  alle 32 woorden", 16f, true, Ui.MUTED);
        t.setGravity(Gravity.CENTER);
        content.addView(t);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        content.addView(box);

        List<Integer> all = new ArrayList<>(allLearned);
        Collections.shuffle(all);

        new SessionRunner(this, box, new Runnable() {
            @Override
            public void run() {
                buildIntro();
            }
        }, new SessionRunner.Callback() {
            @Override
            public void onDone(int xp, int asked, int correct, List<Integer> missed) {
                totalXp += xp;
                totalAsked += asked;
                totalCorrect += correct;
                Progress.addXp(CrashCourseActivity.this, xp);
                Progress.touchStreak(CrashCourseActivity.this);
                buildDone(missed);
            }
        }).start(new ArrayList<Integer>(), all);
    }

    private void buildDone(List<Integer> missed) {
        if (pauseTicker != null) handler.removeCallbacks(pauseTicker);
        content.removeAllViews();

        int pct = totalAsked == 0 ? 100 : Math.round(totalCorrect * 100f / totalAsked);
        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 24), Ui.dp(this, 32), Ui.dp(this, 24), Ui.dp(this, 32));
        card.addView(Ui.bigCardText(this, "🏁 Cursus afgerond!", 26f, Ui.PRIMARY));

        TextView stats = Ui.text(this,
                "⚡ Totaal: +" + totalXp + " XP\n🎯 Eindscore: " + pct + "%\n🔥 Streak: "
                        + Progress.streak(this) + " dag(en)",
                17f, false, Ui.TEXT);
        stats.setGravity(Gravity.CENTER);
        stats.setPadding(0, Ui.dp(this, 16), 0, 0);
        card.addView(stats);

        TextView body = Ui.text(this,
                missed.isEmpty()
                        ? "Alles goed bij de eindtest — sterk! 🔒"
                        : "De " + missed.size() + " woord(en) van de eindtest die lastig waren, komen vanzelf terug.",
                14f, false, Ui.MUTED);
        body.setGravity(Gravity.CENTER);
        body.setPadding(0, Ui.dp(this, 12), 0, 0);
        card.addView(body);
        content.addView(card, Ui.cardParams(this));

        LinearLayout next = Ui.card(this);
        next.addView(Ui.text(this, "Belangrijk voor de lange termijn:", 15f, true, Ui.TEXT));
        next.addView(Ui.text(this,
                "• Herhaal morgen 5 minuten via 🔁 HERHALEN\n"
                        + "• Daarna over 3, 7, 14 en 30 dagen — de app zet het klaar\n"
                        + "• Ga vannacht op tijd slapen: tijdens de slaap wordt alles vastgezet",
                14f, false, Ui.TEXT));
        content.addView(next, Ui.cardParams(this));

        content.addView(Ui.button(this, "Terug naar menu", Ui.PRIMARY, 0xFFFFFFFF, v -> finish()));
    }
}
