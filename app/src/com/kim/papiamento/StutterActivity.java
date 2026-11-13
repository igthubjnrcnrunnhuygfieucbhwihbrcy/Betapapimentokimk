package com.kim.papiamento;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Anti-stotter training: bewezen technieken (zachte aanloop, lichte contacten,
 * gelijkmatig tempo, ademhaling) meteen geoefend in het Papiamento.
 *
 * Let op: dit is oefenmateriaal, geen medische behandeling. Bij hardnekkig
 * stotteren helpt een logopedist het meest.
 */
public class StutterActivity extends Activity {

    private final Handler handler = new Handler();
    private Runnable step;
    private LinearLayout content;
    private boolean breathingActive = false;

    private static final String[] TECHNIQUES = {
            "1. Buikademhaling\n"
                    + "Adem 4 tellen in door je neus (buik gaat uit), 4 tellen vasthouden, "
                    + "6 tellen rustig uitblazen. Spanning in keel en borst omlaag = ruimte om vlot te starten.",

            "2. Zachte aanloop (easy onset)\n"
                    + "Begin een woord zacht en glijdend: eerst lucht, dan pas stem. "
                    + "Bijvoorbeeld 'hh-bon dia' in plaats van het woord met een klap te starten. "
                    + "Dit voorkomt blokkeren op de eerste letter.",

            "3. Lichte contacten\n"
                    + "Raak de eerste lettergreep zacht aan: lippen en tong 'strijken' in plaats van "
                    + "drukker tegen elkaar. Druk is de brandstof van stotteren.",

            "4. Gelijkmatig tempo (lettergreep voor lettergreep)\n"
                    + "Spreek elke lettergreep even lang: 'bon- -di- -a'. Dit is de meest bewezen "
                    + "techniek uit de stottertherapie: je hersenen krijgen per lettergreep de tijd, "
                    + "en de spraakmotoriek stopt met haasten. Later bouw je het tempo weer op.",

            "5. Vermijding doorbreken\n"
                    + "Woorden vermijden en anderen laten praten voelt veilig, maar houdt de angst "
                    + "in stand. Spreek juist eerder, houd oogcontact, en accepteer dat een hapering "
                    + "mag. Hoe minder je vecht tegen spreken, hoe vloeiender het wordt.",
    };

    private static final String[] PRACTICE_SENTENCES = {
            "Bon dia, kon ta bai?",
            "Mi ta bon, masha danki.",
            "Mi nòmber ta Kim.",
            "Mi ta siña Papiamento.",
            "Por fabor papia mas despacio.",
            "Mi no ta komprondè, dispensa.",
            "Bon bini na Kòrsou!",
            "Masha bon, tur kos ta bon.",
            "Mi ta bin di Hulanda.",
            "Ayo, te otro biaha!",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = Ui.root(this);
        setContentView(Ui.scroll(this, content));
        buildMain();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (step != null) handler.removeCallbacks(step);
    }

    private void buildMain() {
        breathingActive = false;
        if (step != null) handler.removeCallbacks(step);
        content.removeAllViews();

        TextView back = Ui.text(this, "←  Terug naar menu", 15f, true, Ui.PRIMARY);
        back.setOnClickListener(v -> finish());
        content.addView(back);

        TextView header = Ui.text(this, "🗣️ Anti-stotter training", 24f, true, Ui.TEXT);
        header.setPadding(0, Ui.dp(this, 14), 0, 0);
        content.addView(header);

        TextView intro = Ui.text(this,
                "Stotteren is geen fout van jou: het is een anderheid in de spraakaansturing van "
                        + "de hersenen. Wat wél helpt: technieken die de spanning uit je spraak halen. "
                        + "Oefen elke techniek 1 à 2 minuten, en combineer ze met de Papiamento-zinnen "
                        + "onderaan — zo train je taal én vloeiendheid tegelijk.",
                15f, false, Ui.TEXT);
        intro.setPadding(0, Ui.dp(this, 8), 0, 0);
        content.addView(intro);

        for (String t : TECHNIQUES) {
            LinearLayout card = Ui.card(this);
            card.addView(Ui.text(this, t, 15f, false, Ui.TEXT));
            content.addView(card, Ui.cardParams(this));
        }

        content.addView(Ui.button(this, "🫁 Start ademhalingsoefening (60 sec)", Ui.PRIMARY, 0xFFFFFFFF,
                v -> buildBreathing()));

        LinearLayout practice = Ui.card(this);
        practice.addView(Ui.text(this, "Oefenzinnen: lees hardop met zachte aanloop en "
                + "gelijkmatig tempo. Tik op 'Volgende zin' als je klaar bent.", 15f, false, Ui.TEXT));
        content.addView(practice, Ui.cardParams(this));

        showPracticeSentence(0);

        TextView disc = Ui.text(this,
                "Deze oefeningen zijn gebaseerd op gangbare logopedische technieken (fluency shaping). "
                        + "Ze vervangen geen behandeling: een logopedist kan ze persoonlijk afstemmen.",
                13f, false, Ui.MUTED);
        disc.setPadding(0, Ui.dp(this, 16), 0, 0);
        content.addView(disc);
    }

    private void showPracticeSentence(int i) {
        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 24), Ui.dp(this, 28), Ui.dp(this, 24), Ui.dp(this, 28));
        TextView label = Ui.text(this, "Zin " + (i + 1) + " van " + PRACTICE_SENTENCES.length, 13f, false, Ui.MUTED);
        label.setGravity(Gravity.CENTER);
        card.addView(label);
        TextView sentence = Ui.bigCardText(this, PRACTICE_SENTENCES[i], 24f, Ui.PRIMARY);
        sentence.setPadding(0, Ui.dp(this, 14), 0, 0);
        card.addView(sentence);
        content.addView(card, Ui.cardParams(this));

        int next = (i + 1) % PRACTICE_SENTENCES.length;
        content.addView(Ui.button(this, "Volgende zin ▶", Ui.ACCENT, 0xFFFFFFFF, v -> {
            content.removeView(card);
            content.removeViewAt(content.getChildCount() - 1);
            showPracticeSentence(next);
        }));
    }

    // ------------------------------------------------------------------
    // Ademhalingsoefening: 4 sec in - 4 vast - 6 uit, 5 rondes
    // ------------------------------------------------------------------

    private void buildBreathing() {
        if (step != null) handler.removeCallbacks(step);
        content.removeAllViews();

        TextView back = Ui.text(this, "←  Stop oefening", 15f, true, Ui.PRIMARY);
        back.setOnClickListener(v -> buildMain());
        content.addView(back);

        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 24), Ui.dp(this, 48), Ui.dp(this, 24), Ui.dp(this, 48));
        TextView roundLabel = Ui.text(this, "Ronde 1 van 5", 15f, false, Ui.MUTED);
        roundLabel.setGravity(Gravity.CENTER);
        card.addView(roundLabel);
        TextView phaseLabel = Ui.bigCardText(this, "Klaar?", 40f, Ui.PRIMARY);
        phaseLabel.setPadding(0, Ui.dp(this, 30), 0, 0);
        card.addView(phaseLabel);
        TextView countLabel = Ui.bigCardText(this, " ", 54f, Ui.TEXT);
        card.addView(countLabel);
        content.addView(card, Ui.cardParams(this));

        breathingActive = true;
        runBreathingRound(1, phaseLabel, countLabel, roundLabel);
    }

    private void runBreathingRound(int round, TextView phaseLabel, TextView countLabel, TextView roundLabel) {
        if (!breathingActive) return;
        roundLabel.setText("Ronde " + round + " van 5");
        phaseLabel.setText("Adem in…");
        countDown(4, 4, countLabel, () -> {
            if (!breathingActive) return;
            phaseLabel.setText("Vasthouden…");
            countDown(4, 4, countLabel, () -> {
                if (!breathingActive) return;
                phaseLabel.setText("Adem uit…");
                countDown(6, 6, countLabel, () -> {
                    if (!breathingActive) return;
                    if (round < 5) {
                        runBreathingRound(round + 1, phaseLabel, countLabel, roundLabel);
                    } else {
                        phaseLabel.setText("Klaar! 🌿");
                        countLabel.setText(" ");
                        content.addView(Ui.button(this, "Opnieuw", Ui.PRIMARY, 0xFFFFFFFF,
                                v -> buildBreathing()));
                        content.addView(Ui.button(this, "Terug", Ui.CARD, Ui.PRIMARY, v -> buildMain()));
                    }
                });
            });
        });
    }

    private void countDown(int from, int total, TextView label, Runnable done) {
        label.setText(String.valueOf(from));
        step = new Runnable() {
            int n = from;

            @Override
            public void run() {
                if (!breathingActive) return;
                n--;
                if (n <= 0) {
                    done.run();
                } else {
                    label.setText(String.valueOf(n));
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.postDelayed(step, 1000);
    }
}
