package com.kim.papiamento;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = Ui.root(this);

        TextView title = Ui.text(this, "Papiamento Blitz", 32f, true, Ui.PRIMARY);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = Ui.text(this,
                "Snel Papiamento leren met je brein als bondgenoot — gratis, offline, geen advertenties",
                14f, false, Ui.MUTED);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, Ui.dp(this, 4), 0, 0);
        root.addView(sub);

        LinearLayout stats = Ui.card(this);
        int due = WordBank.dueCount(this);
        int mastered = WordBank.masteredCount(this);
        int fresh = WordBank.newCount(this);
        TextView statText = Ui.text(this,
                "📊 Vandaag te herhalen: " + due + " woorden\n"
                        + "✅ Vastgezeten (lange termijn): " + mastered + " van " + WordBank.WORDS.length + "\n"
                        + "🌱 Nog nieuw: " + fresh,
                15f, false, Ui.TEXT);
        stats.addView(statText);
        root.addView(stats, Ui.cardParams(this));

        root.addView(menuCard("⚡", "Snelle 1-dags cursus",
                "±1 uur in 4 blokken met 1 pauze — vandaag nog de basis praten", new Intent(this, CrashCourseActivity.class)));

        root.addView(menuCard("📗", "Lessen oefenen",
                "Flashcards en quiz per onderwerp, met slimme herhaling", new Intent(this, LessonActivity.class)));

        root.addView(menuCard("🗓️", "Schema 1× of 2× per week",
                "De app onthoudt wat aan herhaling toe is — 20 à 30 minuten per sessie is genoeg", new Intent(this, LessonActivity.class)));

        root.addView(menuCard("🗣️", "Anti-stotter training",
                "Ademhaling, zachte aanloop en gelijkmatig tempo — oefen meteen in het Papiamento", new Intent(this, StutterActivity.class)));

        root.addView(menuCard("🧠", "Zo onthoudt je brein het",
                "Korte en lange termijn geheugen in 2 minuten uitgelegd", new Intent(this, BrainActivity.class)));

        TextView foot = Ui.text(this,
                "Tip: kies één schema en blijf erbij — kleine beetjes winnen van lange dagen.",
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
