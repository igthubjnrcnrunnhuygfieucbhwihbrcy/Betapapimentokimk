package com.kim.papiamento;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lessen oefenen: flashcards met slimme herhaling (SRS) en een multiple-choice quiz.
 * Werkt voor het wekelijkse schema (1x of 2x per week): de kaarten die vervallen
 * staan altijd vooraan.
 */
public class LessonActivity extends Activity {

    private int lessonIndex = 0;
    private boolean quizMode = false;

    // Flashcard-status
    private List<Integer> queue = new ArrayList<>();
    private int queuePos = 0;
    private boolean showingAnswer = false;
    private int sessionCorrect = 0;
    private int sessionTotal = 0;

    // Quiz-status
    private List<Integer> quizQueue = new ArrayList<>();
    private int quizPos = 0;
    private int quizCorrect = 0;
    private String quizAnswer = null;

    private LinearLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = Ui.root(this);
        setContentView(Ui.scroll(this, content));
        buildLessonScreen();
    }

    private List<Integer> lessonWordIndexes() {
        String lesson = WordBank.WORDS[0].lesson;
        List<String> names = new ArrayList<>();
        for (WordBank.Word w : WordBank.WORDS) {
            if (!names.contains(w.lesson)) names.add(w.lesson);
        }
        lesson = names.get(lessonIndex);
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < WordBank.WORDS.length; i++) {
            if (WordBank.WORDS[i].lesson.equals(lesson)) idx.add(i);
        }
        return idx;
    }

    private void buildLessonScreen() {
        content.removeAllViews();

        List<String> names = new ArrayList<>();
        for (WordBank.Word w : WordBank.WORDS) {
            if (!names.contains(w.lesson)) names.add(w.lesson);
        }
        String lessonName = names.get(lessonIndex);

        TextView back = Ui.text(this, "←  Terug naar menu", 15f, true, Ui.PRIMARY);
        back.setOnClickListener(v -> finish());
        content.addView(back);

        TextView header = Ui.text(this, "Les " + (lessonIndex + 1) + " van " + names.size()
                + "  •  " + lessonName, 20f, true, Ui.TEXT);
        header.setPadding(0, Ui.dp(this, 16), 0, 0);
        content.addView(header);

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        Button prev = Ui.button(this, "◀ Vorige les", Ui.CARD, Ui.PRIMARY, v -> {
            lessonIndex = (lessonIndex + names.size() - 1) % names.size();
            buildLessonScreen();
        });
        Button next = Ui.button(this, "Volgende les ▶", Ui.CARD, Ui.PRIMARY, v -> {
            lessonIndex = (lessonIndex + 1) % names.size();
            buildLessonScreen();
        });
        LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(0, Ui.dp(this, 50), 1f);
        half.rightMargin = Ui.dp(this, 5);
        nav.addView(prev, half);
        LinearLayout.LayoutParams half2 = new LinearLayout.LayoutParams(0, Ui.dp(this, 50), 1f);
        half2.leftMargin = Ui.dp(this, 5);
        nav.addView(next, half2);
        nav.setPadding(0, Ui.dp(this, 6), 0, 0);
        content.addView(nav);

        content.addView(Ui.button(this, "🃏  Flashcards (slimme herhaling)", Ui.PRIMARY, 0xFFFFFFFF,
                v -> startFlashcards()));
        content.addView(Ui.button(this, "❓  Quiz (actief ophalen)", Ui.ACCENT, 0xFFFFFFFF,
                v -> startQuiz()));

        TextView hint = Ui.text(this,
                "Hoe het werkt:\n"
                        + "• Flashcards: eerst zelf hardop proberen, dan omdraaien. Wist je hem? "
                        + "dan komt hij pas na 1, 3, 7, 14 en 30 dagen terug.\n"
                        + "• Quiz: ophalen uit je geheugen is dé manier om iets vast te zetten.",
                14f, false, Ui.MUTED);
        hint.setPadding(0, Ui.dp(this, 18), 0, 0);
        content.addView(hint);
    }

    // ------------------------------------------------------------------
    // Flashcards
    // ------------------------------------------------------------------

    private void startFlashcards() {
        List<Integer> lesson = lessonWordIndexes();
        List<Integer> due = new ArrayList<>();
        List<Integer> fresh = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (int i : lesson) {
            if (WordBank.dueAt(this, i) == 0L) fresh.add(i);
            else if (WordBank.dueAt(this, i) <= now) due.add(i);
        }
        Collections.shuffle(due);
        Collections.shuffle(fresh);
        queue = new ArrayList<>();
        queue.addAll(due);
        queue.addAll(fresh);
        if (queue.size() > 12) queue = new ArrayList<>(queue.subList(0, 12));
        queuePos = 0;
        sessionCorrect = 0;
        sessionTotal = 0;
        if (queue.isEmpty()) {
            buildDoneScreen("Alles uit deze les zit al vast! 🎉\n\nKom terug als de herhaling vervalt — de app onthoudt dat voor je.");
            return;
        }
        showCard();
    }

    private void showCard() {
        if (queuePos >= queue.size()) {
            buildDoneScreen("Klaar! " + sessionCorrect + " van " + sessionTotal
                    + " goed.\n\nDe woorden die je wist komen pas over 1, 3, 7, 14 of 30 dagen "
                    + "terug — precies op het moment dat je brein ze bijna vergeet. Dat is het moment "
                    + "waarop ze naar de lange termijn schuiven. 💪");
            return;
        }
        showingAnswer = false;
        content.removeAllViews();

        int idx = queue.get(queuePos);
        WordBank.Word w = WordBank.WORDS[idx];

        TextView back = Ui.text(this, "←  Stoppen", 15f, true, Ui.PRIMARY);
        back.setOnClickListener(v -> buildLessonScreen());
        content.addView(back);

        TextView progress = Ui.text(this, "Kaart " + (queuePos + 1) + " van " + queue.size()
                + "  •  " + w.lesson, 14f, false, Ui.MUTED);
        progress.setPadding(0, Ui.dp(this, 10), 0, 0);
        content.addView(progress);

        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 24), Ui.dp(this, 40), Ui.dp(this, 24), Ui.dp(this, 40));

        TextView nl = Ui.bigCardText(this, w.nl, 26f, Ui.TEXT);
        card.addView(nl);

        TextView pa = Ui.bigCardText(this, showingAnswer ? w.pa : "…", 34f, Ui.PRIMARY);
        pa.setPadding(0, Ui.dp(this, 24), 0, 0);
        card.addView(pa);

        if (showingAnswer) {
            TextView tip = Ui.bigCardText(this, "Zeg het hardop voor je doortikt!", 13f, Ui.MUTED);
            tip.setPadding(0, Ui.dp(this, 10), 0, 0);
            card.addView(tip);
        }
        content.addView(card, Ui.cardParams(this));

        if (!showingAnswer) {
            content.addView(Ui.button(this, "Toon antwoord", Ui.PRIMARY, 0xFFFFFFFF,
                    v -> { showingAnswer = true; showCard(); }));
        } else {
            content.addView(Ui.button(this, "✓  Wist ik", Ui.GOOD, 0xFFFFFFFF, v -> {
                WordBank.record(this, idx, true);
                sessionCorrect++;
                sessionTotal++;
                queuePos++;
                showCard();
            }));
            content.addView(Ui.button(this, "✗  Nog lastig", Ui.BAD, 0xFFFFFFFF, v -> {
                WordBank.record(this, idx, false);
                sessionTotal++;
                queue.add(idx); // komt binnen 2 minuten terug
                queuePos++;
                showCard();
            }));
        }
    }

    private void buildDoneScreen(String message) {
        content.removeAllViews();
        TextView back = Ui.text(this, "←  Terug naar lessen", 15f, true, Ui.PRIMARY);
        back.setOnClickListener(v -> buildLessonScreen());
        content.addView(back);

        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 24), Ui.dp(this, 32), Ui.dp(this, 24), Ui.dp(this, 32));
        TextView m = Ui.bigCardText(this, message, 17f, Ui.TEXT);
        card.addView(m);
        content.addView(card, Ui.cardParams(this));

        content.addView(Ui.button(this, "Opnieuw oefenen", Ui.PRIMARY, 0xFFFFFFFF, v -> startFlashcards()));
        content.addView(Ui.button(this, "Terug naar menu", Ui.CARD, Ui.PRIMARY, v -> finish()));
    }

    // ------------------------------------------------------------------
    // Quiz
    // ------------------------------------------------------------------

    private void startQuiz() {
        List<Integer> lesson = lessonWordIndexes();
        quizQueue = new ArrayList<>(lesson);
        Collections.shuffle(quizQueue);
        quizPos = 0;
        quizCorrect = 0;
        showQuizQuestion();
    }

    private void showQuizQuestion() {
        if (quizPos >= quizQueue.size()) {
            int total = quizQueue.size();
            buildDoneScreen("Quiz klaar! Score: " + quizCorrect + " / " + total + "\n\n"
                    + (quizCorrect == total
                    ? "Perfect! Je brein heeft deze les vastgezet. 🔒"
                    : "De gemiste woorden komen automatisch terug in je herhaling."));
            return;
        }
        content.removeAllViews();

        int idx = quizQueue.get(quizPos);
        WordBank.Word w = WordBank.WORDS[idx];
        quizAnswer = w.pa;

        TextView back = Ui.text(this, "←  Stoppen", 15f, true, Ui.PRIMARY);
        back.setOnClickListener(v -> buildLessonScreen());
        content.addView(back);

        TextView progress = Ui.text(this, "Vraag " + (quizPos + 1) + " van " + quizQueue.size()
                + "  •  goed: " + quizCorrect, 14f, false, Ui.MUTED);
        progress.setPadding(0, Ui.dp(this, 10), 0, 0);
        content.addView(progress);

        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 24), Ui.dp(this, 32), Ui.dp(this, 24), Ui.dp(this, 32));
        TextView q = Ui.bigCardText(this, w.nl, 24f, Ui.TEXT);
        card.addView(q);
        TextView sub = Ui.bigCardText(this, "Wat is dit in het Papiamento?", 14f, Ui.MUTED);
        sub.setPadding(0, Ui.dp(this, 8), 0, 0);
        card.addView(sub);
        content.addView(card, Ui.cardParams(this));

        List<Integer> options = new ArrayList<>();
        options.add(idx);
        while (options.size() < Math.min(4, WordBank.WORDS.length)) {
            int r = (int) (Math.random() * WordBank.WORDS.length);
            if (!options.contains(r)) options.add(r);
        }
        Collections.shuffle(options);

        for (final int optIdx : options) {
            final String optPa = WordBank.WORDS[optIdx].pa;
            Button b = Ui.button(this, optPa, Ui.CARD, Ui.TEXT, null);

            b.setOnClickListener(v -> {

                                boolean correct = optPa.equals(quizAnswer);
                WordBank.record(this, idx, correct);
                if (correct) quizCorrect++;
                b.setBackground(Ui.rounded(correct ? Ui.GOOD : Ui.BAD, Ui.dp(this, 14)));
                b.setTextColor(0xFFFFFFFF);
                content.postDelayed(this::showQuizQuestion, 700);

            });
            content.addView(b);
        }
    }
}
