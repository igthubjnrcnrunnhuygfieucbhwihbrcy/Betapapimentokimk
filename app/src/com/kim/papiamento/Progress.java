package com.kim.papiamento;

import android.content.Context;
import android.content.SharedPreferences;

/** XP en dag-streak (zoals Duolingo): beloning voor elke oefensessie. */
public class Progress {

    private static SharedPreferences p(Context c) {
        return c.getSharedPreferences("progress", Context.MODE_PRIVATE);
    }

    public static int xp(Context c) {
        return p(c).getInt("xp", 0);
    }

    public static void addXp(Context c, int n) {
        p(c).edit().putInt("xp", xp(c) + n).apply();
    }

    public static int streak(Context c) {
        return p(c).getInt("streak", 0);
    }

    /** Bijwerken na een sessie: streak +1 bij oefenen op opeenvolgende dagen. */
    public static void touchStreak(Context c) {
        int today = (int) (System.currentTimeMillis() / 86400000L);
        int last = p(c).getInt("lastDay", -1);
        if (last == today) return;
        int next = (last == today - 1) ? streak(c) + 1 : 1;
        p(c).edit().putInt("streak", next).putInt("lastDay", today).apply();
    }
}
