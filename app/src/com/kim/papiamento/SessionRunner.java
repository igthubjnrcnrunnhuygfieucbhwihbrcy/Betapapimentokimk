package com.kim.papiamento;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Oefenmotor in Duolingo-stijl. Mixt per woord verschillende oefenvormen:
 *
 *  - INTRO:    nieuwe woorden eerst rustig kennismaken (met uitspraak 🔊)
 *  - MC:       multiple choice (NL→PA en PA→NL)
 *  - TILES:    woord bouwen uit losse letters of woorden
 *  - TYPE_IN:  het woord zelf typen (strengste test van het geheugen)
 *
 * Fout beantwoorde woorden komen binnen dezelfde sessie terug tot je ze goed
 * hebt (adaptief), en worden in het herhalingsschema teruggezet.
 */
public class SessionRunner {

    public interface Callback {
        void onDone(int xp, int asked, int correct, List<Integer> missed);
    }

    private static final int INTRO = 0, MC_NL_PA = 1, MC_PA_NL = 2, TILES = 3, TYPE_IN = 4;

    private final Activity act;
    private final LinearLayout box;
    private final Runnable onExit;
    private final Callback callback;

    private final List<int[]> queue = new ArrayList<>();
    private final List<Integer> requeue = new ArrayList<>();
    private final List<Integer> missed = new ArrayList<>();
    private int pos = 0, xp = 0, asked = 0, correctFirst = 0;
    private boolean answered = false;

    // status voor de woord-bouw-oefening
    private List<String> tilePool = new ArrayList<>();
    private final List<String> tileAnswer = new ArrayList<>();
    private String tileTarget = "";
    private boolean tileWordMode = false;
    private LinearLayout answerRow, poolRow;
    private Button tileCheck;

    public SessionRunner(Activity act, LinearLayout box, Runnable onExit, Callback callback) {
        this.act = act;
        this.box = box;
        this.onExit = onExit;
        this.callback = callback;
    }

    public void start(List<Integer> newWords, List<Integer> dueWords) {
        List<Integer> order = new ArrayList<>();
        order.addAll(newWords);
        order.addAll(dueWords);
        Collections.shuffle(order);
        for (int idx : order) {
            if (newWords.contains(idx)) {
                queue.add(new int[]{idx, INTRO});
                queue.add(new int[]{idx, MC_NL_PA});
            } else {
                queue.add(new int[]{idx, pickType(idx)});
            }
        }
        renderNext();
    }

    private int pickType(int idx) {
        String pa = WordBank.WORDS[idx].pa;
        boolean shortWord = pa.length() <= 12
                && !pa.contains(".") && !pa.contains("?") && !pa.contains(",") && !pa.contains("/");
        double r = Math.random();
        if (shortWord) {
            if (r < 0.35) return TILES;
            if (r < 0.65) return MC_NL_PA;
            if (r < 0.85) return MC_PA_NL;
            return TYPE_IN;
        }
        if (pa.contains("...")) return MC_NL_PA;
        return r < 0.55 ? MC_NL_PA : MC_PA_NL;
    }

    // ------------------------------------------------------------------

    private void renderNext() {
        if (pos >= queue.size()) {
            if (!requeue.isEmpty()) {
                for (int idx : requeue) queue.add(new int[]{idx, pickType(idx)});
                requeue.clear();
            } else {
                callback.onDone(xp, asked, correctFirst, missed);
                return;
            }
        }
        answered = false;
        box.removeAllViews();
        box.addView(header());
        int[] step = queue.get(pos);
        int idx = step[0], type = step[1];
        if (type == INTRO) renderIntro(idx);
        else if (type == MC_NL_PA) renderMc(idx, true);
        else if (type == MC_PA_NL) renderMc(idx, false);
        else if (type == TILES) renderTiles(idx);
        else renderTypeIn(idx);
    }

    private LinearLayout header() {
        LinearLayout h = new LinearLayout(act);
        h.setOrientation(LinearLayout.HORIZONTAL);
        h.setGravity(Gravity.CENTER_VERTICAL);
        TextView x = Ui.text(act, "✕", 18f, true, Ui.MUTED);
        x.setPadding(0, 0, Ui.dp(act, 14), 0);
        x.setOnClickListener(v -> onExit.run());
        h.addView(x);
        float f = queue.isEmpty() ? 0f : (float) pos / queue.size();
        h.addView(Ui.progressBar(act, f, Ui.PRIMARY),
                new LinearLayout.LayoutParams(0, Ui.dp(act, 16), 1f));
        h.setPadding(0, 0, 0, Ui.dp(act, 14));
        return h;
    }

    // ------------------------------------------------------------------

    private void renderIntro(int idx) {
        WordBank.Word w = WordBank.WORDS[idx];
        Speaker.speak(w.pa);

        LinearLayout card = Ui.card(act);
        card.setPadding(Ui.dp(act, 24), Ui.dp(act, 32), Ui.dp(act, 24), Ui.dp(act, 32));
        card.addView(Ui.bigCardText(act, "NIEUW WOORD", 13f, Ui.MUTED));

        TextView pa = Ui.bigCardText(act, w.pa, 32f, Ui.TEXT);
        pa.setPadding(0, Ui.dp(act, 16), 0, 0);
        card.addView(pa);

        Button sound = Ui.button(act, "🔊 Luister", Ui.BLUE, 0xFFFFFFFF, v -> Speaker.speak(w.pa));
        sound.setLayoutParams(soundParams(act));
        card.addView(sound);

        TextView nl = Ui.bigCardText(act, w.nl, 18f, Ui.MUTED);
        nl.setPadding(0, Ui.dp(act, 14), 0, 0);
        card.addView(nl);

        TextView tip = Ui.bigCardText(act, "Zeg het 2× hardop — praten helpt je geheugen het meest", 13f, Ui.MUTED);
        tip.setPadding(0, Ui.dp(act, 12), 0, 0);
        card.addView(tip);
        box.addView(card, Ui.cardParams(act));

        box.addView(Ui.button(act, "DOORGAAN ▶", Ui.PRIMARY, 0xFFFFFFFF, v -> {
            pos++;
            renderNext();
        }));
    }

    private LinearLayout.LayoutParams soundParams(Activity a) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(a, 48));
        lp.topMargin = Ui.dp(a, 14);
        return lp;
    }

    private void renderMc(int idx, boolean nlToPa) {
        WordBank.Word w = WordBank.WORDS[idx];
        String prompt = nlToPa ? w.nl : w.pa;
        String right = nlToPa ? w.pa : w.nl;

        LinearLayout card = Ui.card(act);
        TextView q = Ui.bigCardText(act, prompt, 26f, Ui.TEXT);
        card.addView(q);
        TextView sub = Ui.bigCardText(act, nlToPa ? "Wat is dit in het Papiamento?" : "Wat betekent dit?", 14f, Ui.MUTED);
        sub.setPadding(0, Ui.dp(act, 8), 0, 0);
        card.addView(sub);
        box.addView(card, Ui.cardParams(act));

        for (final int optIdx : optionsFor(idx)) {
            final String optText = nlToPa ? WordBank.WORDS[optIdx].pa : WordBank.WORDS[optIdx].nl;
            Button b = Ui.button(act, optText, 0xFFFFFFFF, Ui.TEXT, null);
            b.setOnClickListener(v -> {
                if (answered) return;
                answered = true;
                boolean ok = optText.equals(right);
                grade(idx, ok);
                if (ok) {
                    b.setBackground(Ui.chunky(Ui.GOOD, Ui.dp(act, 16), Ui.dp(act, 4)));
                    b.setTextColor(0xFFFFFFFF);
                    showFeedback(true, null);
                } else {
                    b.setBackground(Ui.chunky(Ui.BAD, Ui.dp(act, 16), Ui.dp(act, 4)));
                    b.setTextColor(0xFFFFFFFF);
                    showFeedback(false, right);
                }
            });
            box.addView(b);
        }
    }

    private List<Integer> optionsFor(int idx) {
        List<Integer> same = new ArrayList<>(), other = new ArrayList<>();
        for (int i = 0; i < WordBank.WORDS.length; i++) {
            if (i == idx) continue;
            if (WordBank.WORDS[i].lesson.equals(WordBank.WORDS[idx].lesson)) same.add(i);
            else other.add(i);
        }
        Collections.shuffle(same);
        Collections.shuffle(other);
        List<Integer> opts = new ArrayList<>();
        opts.add(idx);
        for (int i : same) {
            if (opts.size() >= 4) break;
            opts.add(i);
        }
        for (int i : other) {
            if (opts.size() >= 4) break;
            opts.add(i);
        }
        Collections.shuffle(opts);
        return opts;
    }

    // ------------------------------------------------------------------

    private void renderTiles(int idx) {
        WordBank.Word w = WordBank.WORDS[idx];
        tileTarget = w.pa;
        tileWordMode = w.pa.contains(" ");
        tilePool = new ArrayList<>();
        if (tileWordMode) {
            Collections.addAll(tilePool, w.pa.split(" "));
        } else {
            for (char ch : w.pa.toCharArray()) tilePool.add(String.valueOf(ch));
        }
        Collections.shuffle(tilePool);
        tileAnswer.clear();

        LinearLayout card = Ui.card(act);
        card.addView(Ui.bigCardText(act, w.nl, 24f, Ui.TEXT));
        TextView sub = Ui.bigCardText(act, tileWordMode ? "Bouw de zin" : "Bouw het woord", 14f, Ui.MUTED);
        sub.setPadding(0, Ui.dp(act, 8), 0, 0);
        card.addView(sub);
        box.addView(card, Ui.cardParams(act));

        answerRow = new LinearLayout(act);
        answerRow.setOrientation(LinearLayout.VERTICAL);
        answerRow.setGravity(Gravity.CENTER);
        answerRow.setBackground(Ui.outlined(act, 0xFFFFFFFF, Ui.BORDER));
        answerRow.setMinimumHeight(Ui.dp(act, 64));
        answerRow.setPadding(Ui.dp(act, 8), Ui.dp(act, 8), Ui.dp(act, 8), Ui.dp(act, 8));
        box.addView(answerRow, Ui.cardParams(act));

        poolRow = new LinearLayout(act);
        poolRow.setOrientation(LinearLayout.VERTICAL);
        box.addView(poolRow);

        tileCheck = Ui.button(act, "CONTROLEREN", Ui.BORDER, Ui.MUTED, v -> checkTiles(idx));
        box.addView(tileCheck);
        refreshTiles();
    }

    private void refreshTiles() {
        answerRow.removeAllViews();
        poolRow.removeAllViews();

        LinearLayout aLine = new LinearLayout(act);
        aLine.setOrientation(LinearLayout.HORIZONTAL);
        aLine.setGravity(Gravity.CENTER);
        for (final String tok : new ArrayList<>(tileAnswer)) {
            aLine.addView(tokenBtn(tok, v -> {
                if (answered) return;
                tileAnswer.remove(tok);
                tilePool.add(tok);
                refreshTiles();
            }));
        }
        if (tileAnswer.isEmpty()) {
            aLine.addView(Ui.text(act, "…", 20f, false, Ui.MUTED));
        }
        answerRow.addView(aLine, rowParams());

        for (int i = 0; i < tilePool.size(); i += 6) {
            List<String> row = new ArrayList<>(tilePool.subList(i, Math.min(i + 6, tilePool.size())));
            LinearLayout line = new LinearLayout(act);
            line.setOrientation(LinearLayout.HORIZONTAL);
            line.setGravity(Gravity.CENTER);
            for (final String tok : row) {
                line.addView(tokenBtn(tok, v -> {
                    if (answered) return;
                    tilePool.remove(tok);
                    tileAnswer.add(tok);
                    refreshTiles();
                }));
            }
            poolRow.addView(line, rowParams());
        }

        boolean full = tileAnswer.size() == expectedTokenCount();
        tileCheck.setEnabled(full);
        tileCheck.setBackground(Ui.chunky(full ? Ui.PRIMARY : Ui.BORDER, Ui.dp(act, 16), Ui.dp(act, 5)));
        tileCheck.setTextColor(full ? 0xFFFFFFFF : Ui.MUTED);
    }

    private int expectedTokenCount() {
        return tileWordMode ? tileTarget.split(" ").length : tileTarget.length();
    }

    private Button tokenBtn(String label, View.OnClickListener onClick) {
        Button b = new Button(act);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(16f);
        b.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        b.setTextColor(Ui.TEXT);
        b.setBackground(Ui.chunky(0xFFFFFFFF, Ui.dp(act, 10), Ui.dp(act, 3)));
        b.setPadding(Ui.dp(act, 10), 0, Ui.dp(act, 10), Ui.dp(act, 3));
        b.setOnClickListener(onClick);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, Ui.dp(act, 46));
        lp.setMargins(Ui.dp(act, 3), Ui.dp(act, 3), Ui.dp(act, 3), 0);
        b.setLayoutParams(lp);
        return b;
    }

    private LinearLayout.LayoutParams rowParams() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private void checkTiles(int idx) {
        if (answered) return;
        answered = true;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tileAnswer.size(); i++) {
            if (i > 0 && tileWordMode) sb.append(' ');
            sb.append(tileAnswer.get(i));
        }
        boolean ok = sb.toString().equals(tileTarget);
        grade(idx, ok);
        if (ok) {
            showFeedback(true, null);
        } else {
            showFeedback(false, tileTarget);
        }
    }

    // ------------------------------------------------------------------

    private void renderTypeIn(int idx) {
        final WordBank.Word w = WordBank.WORDS[idx];
        LinearLayout card = Ui.card(act);
        card.addView(Ui.bigCardText(act, w.nl, 24f, Ui.TEXT));
        TextView sub = Ui.bigCardText(act, "Typ het in het Papiamento", 14f, Ui.MUTED);
        sub.setPadding(0, Ui.dp(act, 8), 0, 0);
        card.addView(sub);
        box.addView(card, Ui.cardParams(act));

        final EditText input = new EditText(act);
        input.setHint("Typ hier…");
        input.setTextSize(18f);
        input.setBackground(Ui.outlined(act, 0xFFFFFFFF, Ui.BORDER));
        input.setPadding(Ui.dp(act, 14), Ui.dp(act, 12), Ui.dp(act, 14), Ui.dp(act, 12));
        box.addView(input, Ui.cardParams(act));

        box.addView(Ui.button(act, "CONTROLEREN", Ui.BORDER, Ui.MUTED, v -> {
            if (answered) return;
            String given = input.getText().toString();
            if (norm(given).isEmpty()) return;
            answered = true;
            boolean ok = norm(given).equals(norm(w.pa));
            grade(idx, ok);
            if (ok) {
                showFeedback(true, null);
            } else {
                showFeedback(false, w.pa);
            }
        }));
    }

    static String norm(String s) {
        s = s.toLowerCase(Locale.ROOT);
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        s = s.replaceAll("[^a-z0-9 ]", "").replaceAll(" +", " ").trim();
        return s;
    }

    // ------------------------------------------------------------------

    private void grade(int idx, boolean ok) {
        WordBank.record(act, idx, ok);
        asked++;
        if (ok) {
            xp += 10;
            correctFirst++;
        } else {
            requeue.add(idx);
            if (!missed.contains(idx)) missed.add(idx);
        }
    }

    private void showFeedback(boolean ok, String rightAnswer) {
        LinearLayout fb = new LinearLayout(act);
        fb.setOrientation(LinearLayout.VERTICAL);
        fb.setBackground(Ui.rounded(ok ? Ui.GREEN_SOFT : Ui.RED_SOFT, Ui.dp(act, 16)));
        fb.setPadding(Ui.dp(act, 18), Ui.dp(act, 16), Ui.dp(act, 18), Ui.dp(act, 18));

        TextView title = Ui.text(act, ok ? "Goed gedaan!  +10 XP" : "Nog niet goed…", 20f, true,
                ok ? Ui.PRIMARY_DARK : Ui.RED_DARK);
        fb.addView(title);

        if (!ok && rightAnswer != null) {
            TextView detail = Ui.text(act, "Juist is: " + rightAnswer, 17f, true, Ui.RED_DARK);
            detail.setPadding(0, Ui.dp(act, 6), 0, 0);
            fb.addView(detail);
            Button sound = Ui.button(act, "🔊 Luister nog eens", Ui.BLUE, 0xFFFFFFFF, v -> Speaker.speak(rightAnswer));
            sound.setLayoutParams(soundParams(act));
            fb.addView(sound);
        }
        box.addView(fb, Ui.cardParams(act));
        box.addView(Ui.button(act, "DOORGAAN ▶", ok ? Ui.PRIMARY : Ui.BAD, 0xFFFFFFFF, v -> {
            pos++;
            renderNext();
        }));
    }
}
