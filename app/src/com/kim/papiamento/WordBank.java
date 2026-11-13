package com.kim.papiamento;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Woordenbank + slim herhalings-systeem (spaced repetition).
 *
 * Niveau's en tussenpozen (gebaseerd op het spacing-effect en de
 * vergeetcurve van Ebbinghaus):
 *   niveau 0 -> na 2 minuten (werkgeheugen / korte termijn)
 *   niveau 1 -> na 1 dag
 *   niveau 2 -> na 3 dagen
 *   niveau 3 -> na 7 dagen
 *   niveau 4 -> na 14 dagen
 *   niveau 5 -> na 30 dagen (lange termijn)
 *
 * Fout maken demoot één niveau (in plaats van alles te vergeten): zo blijft
 * oefenen motiverend en volgt de herhaling je echte niveau.
 */
public class WordBank {

    public static class Word {
        public final String lesson;
        public final String pa;
        public final String nl;
        public Word(String lesson, String pa, String nl) {
            this.lesson = lesson;
            this.pa = pa;
            this.nl = nl;
        }
    }

    public static final Word[] WORDS = {
            // ---- Les 1: Groeten & beleefdheid ----
            new Word("Groeten & beleefdheid", "Bon dia", "Goedemorgen"),
            new Word("Groeten & beleefdheid", "Bon tardi", "Goedemiddag"),
            new Word("Groeten & beleefdheid", "Bon nochi", "Goedenavond"),
            new Word("Groeten & beleefdheid", "Bon bini", "Welkom"),
            new Word("Groeten & beleefdheid", "Ayo", "Tot ziens"),
            new Word("Groeten & beleefdheid", "Te otro biaha", "Tot de volgende keer"),
            new Word("Groeten & beleefdheid", "Masha danki", "Heel erg bedankt"),
            new Word("Groeten & beleefdheid", "Por fabor", "Alsjeblieft"),
            new Word("Groeten & beleefdheid", "Dispensa", "Sorry / excuus"),
            new Word("Groeten & beleefdheid", "Kon ta bai?", "Hoe gaat het?"),
            // ---- Les 2: Kleine praatjes ----
            new Word("Kleine praatjes", "Mi ta bon", "Het gaat goed"),
            new Word("Kleine praatjes", "Kon ta ku bo?", "En met jou?"),
            new Word("Kleine praatjes", "Masha bon", "Heel goed"),
            new Word("Kleine praatjes", "Tur kos ta bon", "Alles is goed"),
            new Word("Kleine praatjes", "Mi nòmber ta ...", "Ik heet ..."),
            new Word("Kleine praatjes", "Kende ta bo nòmber?", "Hoe heet jij?"),
            new Word("Kleine praatjes", "Mi ta bin di Hulanda", "Ik kom uit Nederland"),
            new Word("Kleine praatjes", "Mi ta siña Papiamento", "Ik leer Papiamento"),
            new Word("Kleine praatjes", "Dushi", "Lekker / schat"),
            new Word("Kleine praatjes", "Si / No", "Ja / Nee"),
            // ---- Les 3: Mensen & familie ----
            new Word("Mensen & familie", "Amigu", "Vriend"),
            new Word("Mensen & familie", "Famia", "Familie"),
            new Word("Mensen & familie", "Mama", "Mama"),
            new Word("Mensen & familie", "Papa", "Papa"),
            new Word("Mensen & familie", "Yu", "Kind"),
            new Word("Mensen & familie", "Hòmber", "Man"),
            new Word("Mensen & familie", "Muhé", "Vrouw"),
            new Word("Mensen & familie", "Mucha", "Meisje / jong kind"),
            // ---- Les 4: Getallen ----
            new Word("Getallen", "Un", "1 (één)"),
            new Word("Getallen", "Dos", "2 (twee)"),
            new Word("Getallen", "Tres", "3 (drie)"),
            new Word("Getallen", "Kuater", "4 (vier)"),
            new Word("Getallen", "Sinku", "5 (vijf)"),
            new Word("Getallen", "Sesh", "6 (zes)"),
            new Word("Getallen", "Shete", "7 (zeven)"),
            new Word("Getallen", "Ocho", "8 (acht)"),
            new Word("Getallen", "Nuebe", "9 (negen)"),
            new Word("Getallen", "Dies", "10 (tien)"),
            // ---- Les 5: Eten & drinken ----
            new Word("Eten & drinken", "Awa", "Water"),
            new Word("Eten & drinken", "Kòfi", "Koffie"),
            new Word("Eten & drinken", "Serbes", "Bier"),
            new Word("Eten & drinken", "Pan", "Brood"),
            new Word("Eten & drinken", "Keshi", "Kaas"),
            new Word("Eten & drinken", "Fruita", "Fruit"),
            new Word("Eten & drinken", "Galiña", "Kip"),
            new Word("Eten & drinken", "Sòpi", "Soep"),
            new Word("Eten & drinken", "Mi tin sed", "Ik heb dorst"),
            new Word("Eten & drinken", "Mi tin hamber", "Ik heb honger"),
            // ---- Les 6: Werkwoorden ----
            new Word("Werkwoorden", "Bai", "Gaan"),
            new Word("Werkwoorden", "Bini", "Komen"),
            new Word("Werkwoorden", "Kome", "Eten"),
            new Word("Werkwoorden", "Bebe", "Drinken"),
            new Word("Werkwoorden", "Mira", "Kijken"),
            new Word("Werkwoorden", "Tuma", "Nemen"),
            new Word("Werkwoorden", "Duna", "Geven"),
            new Word("Werkwoorden", "Papia", "Praten"),
            new Word("Werkwoorden", "Siña", "Leren"),
            // ---- Les 7: Tijd & weer ----
            new Word("Tijd & weer", "Awor", "Nu"),
            new Word("Tijd & weer", "Ayera", "Gisteren"),
            new Word("Tijd & weer", "Mañan", "Morgen"),
            new Word("Tijd & weer", "Dia", "Dag"),
            new Word("Tijd & weer", "Siman", "Week"),
            new Word("Tijd & weer", "Ora", "Uur"),
            new Word("Tijd & weer", "Calor", "Warm / heet"),
            new Word("Tijd & weer", "Friu", "Koud"),
            new Word("Tijd & weer", "Solo", "Zon"),
            new Word("Tijd & weer", "Shùlu", "Regen"),
            // ---- Les 8: Handige zinnen ----
            new Word("Handige zinnen", "Bo ta papia Ingles?", "Spreek je Engels?"),
            new Word("Handige zinnen", "Mi no ta komprondè", "Ik begrijp het niet"),
            new Word("Handige zinnen", "Papia mas despacio, por fabor", "Praat alsjeblieft langzamer"),
            new Word("Handige zinnen", "Ki ora ta?", "Hoe laat is het?"),
            new Word("Handige zinnen", "Unda ta e banyo?", "Waar is het toilet?"),
            new Word("Handige zinnen", "Kuanto ta koste?", "Hoeveel kost het?"),
            new Word("Handige zinnen", "Mi ta busca ...", "Ik zoek ..."),
            new Word("Handige zinnen", "Por fabor yudami", "Help me alsjeblieft"),
            new Word("Handige zinnen", "Mi ta sta bo", "Ik hou van jou"),
            new Word("Handige zinnen", "Bon viaje", "Goede reis"),
    };

    private static final long[] DAY_INTERVALS = {0, 1, 3, 7, 14, 30};
    private static final int MAX_LEVEL = 5;

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("srs", Context.MODE_PRIVATE);
    }

    public static long intervalMillis(int level) {
        if (level <= 0) return 2L * 60 * 1000;
        long days = DAY_INTERVALS[Math.min(level, MAX_LEVEL)];
        return days * 24L * 60 * 60 * 1000;
    }

    // [level, dueAtMillis, seen, correct]
    private static long[] state(Context c, int idx) {
        try {
            JSONObject o = new JSONObject(prefs(c).getString("srs", "{}"));
            if (o.has(String.valueOf(idx))) {
                JSONArray a = o.getJSONArray(String.valueOf(idx));
                int level = a.getInt(0);
                long due = a.getLong(1);
                int seen = a.length() > 2 ? a.getInt(2) : 0;
                int ok = a.length() > 3 ? a.getInt(3) : 0;
                return new long[]{level, due, seen, ok};
            }
        } catch (Exception ignored) {
        }
        return new long[]{0, 0, 0, 0};
    }

    private static void save(Context c, int idx, int level, long dueAt, int seen, int correct) {
        try {
            JSONObject o = new JSONObject(prefs(c).getString("srs", "{}"));
            JSONArray a = new JSONArray();
            a.put(level).put(dueAt).put(seen).put(correct);
            o.put(String.valueOf(idx), a);
            prefs(c).edit().putString("srs", o.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    public static int level(Context c, int idx) {
        return (int) state(c, idx)[0];
    }

    public static long dueAt(Context c, int idx) {
        return state(c, idx)[1];
    }

    public static boolean isNew(Context c, int idx) {
        return state(c, idx)[2] == 0;
    }

    /** 0..1: hoe sterk het woord zit (niveau 5 = 100%). */
    public static float strength(Context c, int idx) {
        return level(c, idx) / (float) MAX_LEVEL;
    }

    public static void record(Context c, int idx, boolean correct) {
        long[] s = state(c, idx);
        int level = (int) s[0];
        int seen = (int) s[2] + 1;
        int ok = (int) s[3] + (correct ? 1 : 0);
        if (correct) {
            level = Math.min(level + 1, MAX_LEVEL);
        } else if (level > 0) {
            level = level - 1;
        }
        long due = System.currentTimeMillis() + intervalMillis(correct ? level : 0);
        save(c, idx, level, due, seen, ok);
    }

    /** Aantal woorden dat nu aan de beurt is (nog niet beheerst of herhaling vervalt). */
    public static int dueCount(Context c) {
        int n = 0;
        long now = System.currentTimeMillis();
        for (int i = 0; i < WORDS.length; i++) {
            if (level(c, i) < 3 || dueAt(c, i) <= now) n++;
        }
        return n;
    }

    /** Aantal woorden op niveau 3+ (d.w.z. minimaal een week vastgezeten). */
    public static int masteredCount(Context c) {
        int n = 0;
        for (int i = 0; i < WORDS.length; i++) {
            if (level(c, i) >= 3) n++;
        }
        return n;
    }

    public static int newCount(Context c) {
        int n = 0;
        for (int i = 0; i < WORDS.length; i++) {
            if (isNew(c, i)) n++;
        }
        return n;
    }

    public static List<String> lessons() {
        List<String> names = new ArrayList<>();
        for (Word w : WORDS) {
            if (!names.contains(w.lesson)) names.add(w.lesson);
        }
        return names;
    }

    public static List<Integer> lessonIndexes(String lesson) {
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < WORDS.length; i++) {
            if (WORDS[i].lesson.equals(lesson)) idx.add(i);
        }
        return idx;
    }

    /** Gemiddelde sterkte van een les (0..1). */
    public static float lessonStrength(Context c, String lesson) {
        List<Integer> idx = lessonIndexes(lesson);
        float sum = 0;
        for (int i : idx) sum += strength(c, i);
        return idx.isEmpty() ? 0 : sum / idx.size();
    }
}
