package com.kim.papiamento;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/**
 * Uitspraak via de ingebouwde spraakmotor van Android. Papiamento kent geen
 * eigen TTS-stem; Spaans komt het dichtst bij de uitspraak (Papiamento is
 * grotendeels Spaans/Portugees-gebaseerd). Als er geen TTS is, doet de app
 * gewoon niets in plaats van te crashen.
 */
public class Speaker {

    private static TextToSpeech tts;
    private static boolean ready = false;

    public static void init(Context c) {
        if (tts != null) return;
        try {
            tts = new TextToSpeech(c.getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    ready = status == TextToSpeech.SUCCESS;
                    if (ready) {
                        try {
                            tts.setLanguage(new Locale("es", "ES"));
                            tts.setSpeechRate(0.85f);
                        } catch (Exception ignored) {
                        }
                    }
                }
            });
        } catch (Exception e) {
            tts = null;
        }
    }

    public static boolean available() {
        return tts != null && ready;
    }

    public static void speak(String s) {
        if (!available()) return;
        try {
            tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, "papiamento");
        } catch (Exception ignored) {
        }
    }
}
