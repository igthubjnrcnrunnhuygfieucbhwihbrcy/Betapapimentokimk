package com.kim.papiamento;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lessen oefenen: kies een les of herhaal alles wat vervalt. Elke sessie is
 * een mix van oefenvormen (zie SessionRunner) met adaptieve herhaling.
 */
public class LessonActivity extends Activity {

    private String currentLesson = null;
    private boolean reviewMode = false;

    private LinearLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Speaker.init(this);
        content = Ui.root(this);
        setContentView(Ui.scroll(this, content));

        if ("review".equals(getIntent().getStringExtra("mode"))) {
            startReview();
        } else {
            buildPicker();
        }
    }

    // ------------------------------------------------------------------

    private void buildPicker() {
        currentLesson = null;
        reviewMode = false;
        content.removeAllViews();

        TextView back = Ui.text(this, "←  Terug naar menu", 15f, true, Ui.BLUE);
        back.setOnClickListener(v -> finish());
        content.addView(back);

        TextView header = Ui.text(this, "📗 Lessen", 26f, true, Ui.TEXT);
        header.setPadding(0, Ui.dp(this, 14), 0, 0);
        content.addView(header);

        List<String> lessons = WordBank.lessons();
        for (int i = 0; i < lessons.size(); i++) {
            final String name = lessons.get(i);
            List<Integer> idx = WordBank.lessonIndexes(name);
            float strength = WordBank.lessonStrength(this, name);

            LinearLayout card = Ui.card(this);
            card.setOnClickListener(v -> startLesson(name));
            card.setClickable(true);

            TextView t1 = Ui.text(this, "Les " + (i + 1) + "  •  " + name, 17f, true, Ui.TEXT);
            card.addView(t1);
            TextView t2 = Ui.text(this, idx.size() + " woorden", 13f, false, Ui.MUTED);
            t2.setPadding(0, Ui.dp(this, 2), 0, Ui.dp(this, 8));
            card.addView(t2);
            LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 14));
            card.addView(Ui.progressBar(this, strength,
                    strength >= 0.8f ? Ui.PRIMARY : strength >= 0.4f ? Ui.YELLOW : Ui.BORDER), barLp);
            content.addView(card, Ui.cardParams(this));
        }

        TextView hint = Ui.text(this,
                "De balk laat zien hoe stevig elke les zit (niveau 5 = volledig in je lange termijn).",
                13f, false, Ui.MUTED);
        hint.setPadding(0, Ui.dp(this, 16), 0, 0);
        content.addView(hint);
    }

    // ------------------------------------------------------------------

    private void startLesson(String lesson) {
        currentLesson = lesson;
        List<Integer> all = WordBank.lessonIndexes(lesson);
        List<Integer> newWords = new ArrayList<>();
        List<Integer> dueWords = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (int i : all) {
            if (WordBank.isNew(this, i)) newWords.add(i);
            else if (WordBank.dueAt(this, i) <= now) dueWords.add(i);
        }
        Collections.shuffle(newWords);
        Collections.shuffle(dueWords);
        if (newWords.size() > 4) newWords = new ArrayList<>(newWords.subList(0, 4));
        if (dueWords.size() > 6) dueWords = new ArrayList<>(dueWords.subList(0, 6));

        if (newWords.isEmpty() && dueWords.isEmpty()) {
            buildAllDone("Deze les zit al helemaal vast! 🎉\n\nDe app haalt je terug zodra er weer een herhaling vervalt.");
            return;
        }
        runSession("Les  •  " + lesson, newWords, dueWords);
    }

    private void startReview() {
        reviewMode = true;
        List<Integer> dueWords = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (int i = 0; i < WordBank.WORDS.length; i++) {
            if (!WordBank.isNew(this, i) && WordBank.dueAt(this, i) <= now) dueWords.add(i);
        }
        Collections.shuffle(dueWords);
        if (dueWords.size() > 12) dueWords = new ArrayList<>(dueWords.subList(0, 12));

        if (dueWords.isEmpty()) {
            buildAllDone("Niets te herhalen! 🎉\n\nJe brein heeft alles vastgezet. Nieuwe woorden leer je via de lessen.");
            return;
        }
        runSession("Herhaling  •  " + dueWords.size() + " woorden", new ArrayList<Integer>(), dueWords);
    }

    // ------------------------------------------------------------------

    private void runSession(String title, List<Integer> newWords, List<Integer> dueWords) {
        content.removeAllViews();

        TextView t = Ui.text(this, title, 16f, true, Ui.MUTED);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
        content.addView(t);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        content.addView(box);

        new SessionRunner(this, box, new Runnable() {
            @Override
            public void run() {
                if (reviewMode) startReview();
                else buildPicker();
            }
        }, new SessionRunner.Callback() {
            @Override
            public void onDone(int xp, int asked, int correct, List<Integer> missed) {
                Progress.addXp(LessonActivity.this, xp);
                Progress.touchStreak(LessonActivity.this);
                buildResult(xp, asked, correct, missed);
            }
        }).start(newWords, dueWords);
    }

    private void buildResult(int xp, int asked, int correct, List<Integer> missed) {
        content.removeAllViews();

        int pct = asked == 0 ? 100 : Math.round(correct * 100f / asked);
        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 24), Ui.dp(this, 32), Ui.dp(this, 24), Ui.dp(this, 32));
        card.addView(Ui.bigCardText(this, pct >= 80 ? "🎉 Uitstekend!" : pct >= 50 ? "👍 Goed bezig!" : "💪 Blijven oefenen!", 24f, Ui.PRIMARY));

        TextView stats = Ui.text(this,
                "⚡ +" + xp + " XP\n🎯 " + correct + " van " + asked + " goed (" + pct + "%)\n🔥 Streak: "
                        + Progress.streak(this) + " dag(en)",
                17f, false, Ui.TEXT);
        stats.setGravity(Gravity.CENTER);
        stats.setPadding(0, Ui.dp(this, 16), 0, 0);
        card.addView(stats);

        TextView note = Ui.text(this,
                missed.isEmpty()
                        ? "Geen fouten — deze woorden schuiven door naar de volgende herhalingsronde (1, 3, 7, 14, 30 dagen)."
                        : "De woorden die je miste komen vanzelf terug, binnen deze sessie én in je herhalingsschema.",
                14f, false, Ui.MUTED);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, Ui.dp(this, 14), 0, 0);
        card.addView(note);
        content.addView(card, Ui.cardParams(this));

        content.addView(Ui.button(this, "Nog een sessie", Ui.PRIMARY, 0xFFFFFFFF, v -> {
            if (reviewMode) startReview();
            else startLesson(currentLesson);
        }));
        content.addView(Ui.button(this, "Terug naar menu", 0xFFFFFFFF, Ui.BLUE, v -> finish()));
    }

    private void buildAllDone(String message) {
        content.removeAllViews();

        TextView back = Ui.text(this, "←  Terug", 15f, true, Ui.BLUE);
        back.setOnClickListener(v -> buildPicker());
        content.addView(back);

        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 24), Ui.dp(this, 32), Ui.dp(this, 24), Ui.dp(this, 32));
        TextView m = Ui.bigCardText(this, message, 17f, Ui.TEXT);
        card.addView(m);
        content.addView(card, Ui.cardParams(this));

        content.addView(Ui.button(this, "Terug naar menu", Ui.PRIMARY, 0xFFFFFFFF, v -> finish()));
    }
}
