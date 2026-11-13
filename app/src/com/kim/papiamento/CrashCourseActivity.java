package com.kim.papiamento;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Snelle 1-dags cursus: ±60 minuten, 4 blokken van ±10 minuten met 1 pauze van
 * 5 minuten. Gebruikt micro-herhaling (na ±2 en ±10 minuten) om woorden van het
 * werkgeheugen naar de lange termijn te duwen.
 */
public class CrashCourseActivity extends Activity {

    private static final int BLOCK_WORDS = 8;
    private static final long BLOCK_MILLIS = 10L * 60 * 1000;
    private static final long PAUSE_MILLIS = 5L * 60 * 1000;

    private static final int PHASE_INTRO = 0;
    private static final int PHASE_LEARN = 1;
    private static final int PHASE_REVIEW = 2;
    private static final int PHASE_QUIZ = 3;
    private static final int PHASE_PAUSE = 4;
    private static final int PHASE_FINAL = 5;
    private static final int PHASE_DONE = 6;

    private int phase = PHASE_INTRO;
    private int block = 0;
    private int pos = 0;
    private int correct = 0;
    private List<Integer> blockWords = new ArrayList<>();
    private List<Integer> allLearned = new ArrayList<>();
    private List<Integer> quizQueue = new ArrayList<>();
    private List<Integer> finalQueue = new ArrayList<>();
    private String quizAnswer = null;
    private boolean showingAnswer = false;

    private final Handler handler = new Handler();
    private Runnable ticker;
    private long phaseEnd = 0;

    private LinearLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = Ui.root(this);
        setContentView(Ui.scroll(this, content));
        buildIntro();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ticker != null) handler.removeCallbacks(ticker);
    }

    private List<Integer> blockIndexes(int b) {
        List<Integer> idx = new ArrayList<>();
        for (int i = b * BLOCK_WORDS; i < Math.min((b + 1) * BLOCK_WORDS, WordBank.WORDS.length); i++) {
            idx.add(i);
        }
        return idx;
    }

    private void buildIntro() {
        stopTicker();
        phase = PHASE_INTRO;
        content.removeAllViews();

        TextView back = Ui.text(this, "←  Terug naar menu", 15f, true, Ui.PRIMARY);
        back.setOnClickListener(v -> finish());
        content.addView(back);

        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "⚡ Snelle 1-dags cursus", 22f, true, Ui.TEXT));
        TextView body = Ui.text(this,
                "In ±60 minuten praat je de basis van het Papiamento.\n\n"
                        + "• 4 blokken van ±10 minuten, elk met 8 nieuwe woorden\n"
                        + "• Na blok 2: pauze van 5 minuten (je brein verwerkt dan alles)\n"
                        + "• Elk blok begint met een mini-herhaling van het vorige blok\n"
                        + "• Aan het eind: test met alle 32 woorden\n\n"
                        + "Waarom dit werkt: je werkgeheugen houdt iets maar 15–30 seconden vast. "
                        + "Door net op tijd te herhalen (2 min, 10 min) en daarna met tussenpozen "
                        + "(1, 3, 7, 14, 30 dagen) schuift het van korte naar lange termijn.",
                15f, false, Ui.TEXT);
        body.setPadding(0, Ui.dp(this, 12), 0, 0);
        card.addView(body);
        content.addView(card, Ui.cardParams(this));

        content.addView(Ui.button(this, "Start cursus ▶", Ui.PRIMARY, 0xFFFFFFFF, v -> {
            block = 0;
            allLearned = new ArrayList<>();
            startBlock();
        }));
    }

    private void startBlock() {
        blockWords = blockIndexes(block);
        allLearned.addAll(blockWords);
        pos = 0;
        showingAnswer = false;
        if (block == 0) {
            phase = PHASE_LEARN;
            showLearnCard();
        } else {
            phase = PHASE_REVIEW;
            pos = 0;
            showReviewCard();
        }
    }

    private void startPhaseTimer(long duration, String label) {
        stopTicker();
        phaseEnd = System.currentTimeMillis() + duration;
        ticker = new Runnable() {
            @Override
            public void run() {
                long left = phaseEnd - System.currentTimeMillis();
                TextView t = findViewById(987654);
                if (t != null) {
                    long min = Math.max(0, left) / 60000;
                    long sec = (Math.max(0, left) / 1000) % 60;
                    t.setText(label + "  " + String.format("%d:%02d", min, sec));
                }
                if (left > 0) handler.postDelayed(this, 1000);
            }
        };
        handler.post(ticker);
    }

    private void stopTicker() {
        if (ticker != null) {
            handler.removeCallbacks(ticker);
            ticker = null;
        }
    }

    private TextView timerView(String label) {
        TextView t = Ui.text(this, label, 14f, true, Ui.ACCENT);
        t.setId(987654);
        return t;
    }

    private LinearLayout headerBar(String label) {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        TextView back = Ui.text(this, "←", 18f, true, Ui.PRIMARY);
        back.setOnClickListener(v -> buildIntro());
        bar.addView(back);
        TextView t = timerView(label);
        t.setGravity(android.view.Gravity.END);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        t.setLayoutParams(lp);
        bar.addView(t);
        bar.setPadding(0, 0, 0, Ui.dp(this, 8));
        return bar;
    }

    // ------------------------------------------------------------------

    private void showLearnCard() {
        if (pos >= blockWords.size()) {
            startBlockQuiz();
            return;
        }
        content.removeAllViews();

        int idx = blockWords.get(pos);
        WordBank.Word w = WordBank.WORDS[idx];

        content.addView(headerBar("Blok " + (block + 1) + "/4  •  " + (BLOCK_MILLIS / 60000) + " min"));
        TextView progress = Ui.text(this, "Nieuw woord " + (pos + 1) + " van " + blockWords.size()
                + "  •  " + w.lesson, 14f, false, Ui.MUTED);
        progress.setPadding(0, Ui.dp(this, 6), 0, 0);
        content.addView(progress);

        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 24), Ui.dp(this, 36), Ui.dp(this, 24), Ui.dp(this, 36));
        TextView nl = Ui.bigCardText(this, w.nl, 24f, Ui.TEXT);
        card.addView(nl);
        TextView pa = Ui.bigCardText(this, showingAnswer ? w.pa : "Denk eerst zelf na…", 30f,
                showingAnswer ? Ui.PRIMARY : Ui.MUTED);
        pa.setPadding(0, Ui.dp(this, 22), 0, 0);
        card.addView(pa);
        content.addView(card, Ui.cardParams(this));

        if (!showingAnswer) {
            content.addView(Ui.button(this, "Toon antwoord", Ui.PRIMARY, 0xFFFFFFFF, v -> {
                showingAnswer = true;
                showLearnCard();
            }));
        } else {
            content.addView(Ui.button(this, "Volgende woord ▶", Ui.PRIMARY, 0xFFFFFFFF, v -> {
                pos++;
                showingAnswer = false;
                showLearnCard();
            }));
        }
        startPhaseTimer(BLOCK_MILLIS, "Blok " + (block + 1) + " tijd:");
    }

    private void showReviewCard() {
        if (pos >= blockWords.size()) {
            phase = PHASE_LEARN;
            pos = 0;
            showingAnswer = false;
            // nieuwe woorden van dit blok
            List<Integer> fresh = new ArrayList<>();
            for (int i = block * BLOCK_WORDS; i < Math.min((block + 1) * BLOCK_WORDS, WordBank.WORDS.length); i++) {
                fresh.add(i);
            }
            blockWords = fresh;
            showLearnCard();
            return;
        }
        content.removeAllViews();

        int prevBlock = block - 1;
        int idx = blockIndexes(prevBlock).get(pos);
        WordBank.Word w = WordBank.WORDS[idx];

        content.addView(headerBar("Mini-herhaling"));
        TextView progress = Ui.text(this, "Herhaal blok " + prevBlock + ": " + (pos + 1) + " van "
                + blockWords.size(), 14f, false, Ui.MUTED);
        progress.setPadding(0, Ui.dp(this, 6), 0, 0);
        content.addView(progress);

        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 24), Ui.dp(this, 36), Ui.dp(this, 24), Ui.dp(this, 36));
        TextView nl = Ui.bigCardText(this, w.nl, 24f, Ui.TEXT);
        card.addView(nl);
        TextView pa = Ui.bigCardText(this, showingAnswer ? w.pa : "…", 30f,
                showingAnswer ? Ui.PRIMARY : Ui.MUTED);
        pa.setPadding(0, Ui.dp(this, 22), 0, 0);
        card.addView(pa);
        content.addView(card, Ui.cardParams(this));

        content.addView(Ui.button(this, showingAnswer ? "Volgende ▶" : "Toon antwoord",
                Ui.PRIMARY, 0xFFFFFFFF, v -> {
                    if (showingAnswer) {
                        pos++;
                        showingAnswer = false;
                    } else {
                        showingAnswer = true;
                    }
                    showReviewCard();
                }));
    }

    // ------------------------------------------------------------------

    private void startBlockQuiz() {
        quizQueue = new ArrayList<>(blockWords);
        Collections.shuffle(quizQueue);
        pos = 0;
        correct = 0;
        phase = PHASE_QUIZ;
        showQuizQuestion();
    }

    private void showQuizQuestion() {
        if (pos >= quizQueue.size()) {
            block++;
            if (block == 2) {
                showPause();
            } else if (block >= 4) {
                startFinal();
            } else {
                startBlock();
            }
            return;
        }
        content.removeAllViews();

        int idx = quizQueue.get(pos);
        WordBank.Word w = WordBank.WORDS[idx];
        quizAnswer = w.pa;

        content.addView(headerBar("Mini-quiz blok " + (block + 1)));
        TextView progress = Ui.text(this, "Vraag " + (pos + 1) + " van " + quizQueue.size()
                + "  •  goed: " + correct, 14f, false, Ui.MUTED);
        progress.setPadding(0, Ui.dp(this, 6), 0, 0);
        content.addView(progress);

        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 24), Ui.dp(this, 30), Ui.dp(this, 24), Ui.dp(this, 30));
        card.addView(Ui.bigCardText(this, w.nl, 22f, Ui.TEXT));
        TextView sub = Ui.bigCardText(this, "Wat is dit in het Papiamento?", 14f, Ui.MUTED);
        sub.setPadding(0, Ui.dp(this, 8), 0, 0);
        card.addView(sub);
        content.addView(card, Ui.cardParams(this));

        List<Integer> options = new ArrayList<>();
        options.add(idx);
        while (options.size() < 4) {
            int r = (int) (Math.random() * WordBank.WORDS.length);
            if (!options.contains(r)) options.add(r);
        }
        Collections.shuffle(options);

        for (final int optIdx : options) {
            final String optPa = WordBank.WORDS[optIdx].pa;
            Button b = Ui.button(this, optPa, Ui.CARD, Ui.TEXT, null);

            b.setOnClickListener(v -> {

                                boolean ok = optPa.equals(quizAnswer);
                WordBank.record(this, idx, ok);
                if (ok) correct++;
                b.setBackground(Ui.rounded(ok ? Ui.GOOD : Ui.BAD, Ui.dp(this, 14)));
                b.setTextColor(0xFFFFFFFF);
                content.postDelayed(this::showQuizQuestion, 650);

            });
            content.addView(b);
        }
    }

    // ------------------------------------------------------------------

    private void showPause() {
        stopTicker();
        phase = PHASE_PAUSE;
        content.removeAllViews();

        content.addView(headerBar("Pauze"));
        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 24), Ui.dp(this, 32), Ui.dp(this, 24), Ui.dp(this, 32));
        card.addView(Ui.bigCardText(this, "☕ Pauze van 5 minuten", 24f, Ui.TEXT));
        TextView body = Ui.text(this,
                "Sta op, loop even, kijk uit het raam. Doe NIETS met leren.\n\n"
                        + "Terwijl jij pauzeert, rangschikt je brein net geleerde woorden. "
                        + "Zo'n korte rust is bewezen een van de beste manieren om iets vast te zetten.",
                15f, false, Ui.MUTED);
        body.setGravity(android.view.Gravity.CENTER);
        body.setPadding(0, Ui.dp(this, 14), 0, 0);
        card.addView(body);
        content.addView(card, Ui.cardParams(this));

        content.addView(Ui.button(this, "Pauze overslaan ▶", Ui.CARD, Ui.PRIMARY, v -> startBlock()));
        startPhaseTimer(PAUSE_MILLIS, "Pauze nog:");
    }

    // ------------------------------------------------------------------

    private void startFinal() {
        finalQueue = new ArrayList<>(allLearned);
        Collections.shuffle(finalQueue);
        pos = 0;
        correct = 0;
        phase = PHASE_FINAL;
        showFinalQuestion();
    }

    private void showFinalQuestion() {
        if (pos >= finalQueue.size()) {
            phase = PHASE_DONE;
            buildDone();
            return;
        }
        content.removeAllViews();

        int idx = finalQueue.get(pos);
        WordBank.Word w = WordBank.WORDS[idx];
        quizAnswer = w.pa;

        content.addView(headerBar("Eindtest"));
        TextView progress = Ui.text(this, "Woord " + (pos + 1) + " van " + finalQueue.size()
                + "  •  goed: " + correct, 14f, false, Ui.MUTED);
        progress.setPadding(0, Ui.dp(this, 6), 0, 0);
        content.addView(progress);

        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 24), Ui.dp(this, 30), Ui.dp(this, 24), Ui.dp(this, 30));
        card.addView(Ui.bigCardText(this, w.nl, 22f, Ui.TEXT));
        content.addView(card, Ui.cardParams(this));

        List<Integer> options = new ArrayList<>();
        options.add(idx);
        while (options.size() < 4) {
            int r = (int) (Math.random() * WordBank.WORDS.length);
            if (!options.contains(r)) options.add(r);
        }
        Collections.shuffle(options);

        for (final int optIdx : options) {
            final String optPa = WordBank.WORDS[optIdx].pa;
            Button b = Ui.button(this, optPa, Ui.CARD, Ui.TEXT, null);

            b.setOnClickListener(v -> {

                                boolean ok = optPa.equals(quizAnswer);
                WordBank.record(this, idx, ok);
                if (ok) correct++;
                b.setBackground(Ui.rounded(ok ? Ui.GOOD : Ui.BAD, Ui.dp(this, 14)));
                b.setTextColor(0xFFFFFFFF);
                content.postDelayed(this::showFinalQuestion, 650);

            });
            content.addView(b);
        }
    }

    private void buildDone() {
        stopTicker();
        content.removeAllViews();

        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 24), Ui.dp(this, 32), Ui.dp(this, 24), Ui.dp(this, 32));
        card.addView(Ui.bigCardText(this, "🏁 Cursus afgerond!", 26f, Ui.PRIMARY));
        TextView body = Ui.text(this,
                "Score eindtest: " + correct + " van " + finalQueue.size() + "\n\n"
                        + "Belangrijk voor de lange termijn:\n"
                        + "• Herhaal morgen 5 minuten (de app zet de woorden klaar)\n"
                        + "• Herhaal daarna over 3, 7, 14 en 30 dagen\n"
                        + "• Ga vannacht op tijd slapen: tijdens de slaap wordt alles vastgezet\n\n"
                        + "Vanaf nu vind je deze woorden bij 'Lessen oefenen' onder je herhaling.",
                16f, false, Ui.TEXT);
        body.setPadding(0, Ui.dp(this, 14), 0, 0);
        card.addView(body);
        content.addView(card, Ui.cardParams(this));

        content.addView(Ui.button(this, "Terug naar menu", Ui.PRIMARY, 0xFFFFFFFF, v -> finish()));
    }
}
