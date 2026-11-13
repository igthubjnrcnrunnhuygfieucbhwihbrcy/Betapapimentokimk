package com.kim.papiamento;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BrainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = Ui.root(this);

        TextView back = Ui.text(this, "←  Terug naar menu", 15f, true, Ui.PRIMARY);
        back.setOnClickListener(v -> finish());
        root.addView(back);

        TextView header = Ui.text(this, "🧠 Zo onthoudt je brein het", 24f, true, Ui.TEXT);
        header.setPadding(0, Ui.dp(this, 14), 0, 0);
        root.addView(header);

        section(root,
                "Korte termijn: het werkgeheugen (15–30 seconden)",
                "Je werkgeheugen houdt nieuwe informatie maar zo'n 15 tot 30 seconden vast, "
                        + "en er passen ongeveer 4 tot 7 onderdelen in. Zonder herhaling is een nieuw "
                        + "woord na een halve minuut alweer weg.\n\n"
                        + "Daarom werkt deze app met blokjes van 8 woorden (een 'chunk' die in je "
                        + "werkgeheugen past) en herhaalt hij binnen één sessie al na 2 en 10 minuten: "
                        + "net op tijd, vóór het spoor vervaagt.");

        section(root,
                "Naar de lange termijn: herhalen met tussenpozen",
                "Iets wordt pas langetermijngeheugen als je brein het meerdere keren hoeft op te "
                        + "halen, met rust ertussen. Dat heet het spacing-effect (Ebbinghaus): 5 korte "
                        + "herhalingen verspreid over dagen werken beter dan uren achter elkaar.\n\n"
                        + "Het schema van deze app: 2 minuten → 1 dag → 3 dagen → 7 dagen → 14 dagen → "
                        + "30 dagen. Elke herhaling komt precies op het moment dat je het woord bijna "
                        + "vergeet — dat is het moment waarop je brein het 'belangrijk' gaat vinden en "
                        + "het definitief vastzet.");

        section(root,
                "Ophalen = leren (het testeffect)",
                "Je geheugen wordt niet sterker van opnieuw lezen, maar van proberen te herinneren. "
                        + "Eerst zelf hardop proberen en dan pas omdraaien, of een quiz maken, versterkt "
                        + "het geheugenspoor twee tot drie keer sterker dan passief herlezen.\n\n"
                        + "Daarom vraag je in deze app altijd eerst zelf te denken, en is de quiz een "
                        + "vast onderdeel van elk blok.");

        section(root,
                "Slaap: de stille verwerker",
                "Tijdens de diepe slaap en de REM-slaap speelt je brein de dag opnieuw en verplaatst "
                        + "het herinneringen van de hippocampus (korte termijn) naar de cortex (lange "
                        + "termijn). Dit heet consolidatie.\n\n"
                        + "Praktisch: leer 's avonds, herhaal de volgende ochtend even, en ga op tijd "
                        + "slapen. Dan doet je brein het zware werk terwijl jij slaapt.");

        section(root,
                "Welk schema past bij jou?",
                "• Snelle cursist: de 1-uurs cursus vandaag, morgen 5 minuten herhalen, daarna volgt "
                        + "de app automatisch het 1-3-7-14-30 schema.\n"
                        + "• 2× per week: twee sessies van 20 minuten. Elke sessie begint met wat er "
                        + "vervalt — de app houdt dat bij.\n"
                        + "• 1× per week: één sessie van 30 minuten. Iets langzamer, maar met hetzelfde "
                        + "schema kom je net zo goed op de lange termijn uit.\n\n"
                        + "Het maakt dus niet uit welk tempo je kiest: de tussenpozen doen het werk, "
                        + "niet de tijd die je achter elkaar doorbrengt.");

        setContentView(Ui.scroll(this, root));
    }

    private void section(LinearLayout root, String title, String body) {
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, title, 17f, true, Ui.PRIMARY));
        TextView b = Ui.text(this, body, 15f, false, Ui.TEXT);
        b.setPadding(0, Ui.dp(this, 8), 0, 0);
        card.addView(b);
        root.addView(card, Ui.cardParams(this));
    }
}
