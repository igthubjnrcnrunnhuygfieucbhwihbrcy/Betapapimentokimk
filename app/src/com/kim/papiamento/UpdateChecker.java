package com.kim.papiamento;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Update-controle: bij het opstarten leest de app een klein version.json van
 * GitHub. Is daar de versionCode hoger dan die van de geïnstalleerde app, dan
 * verschijnt er een pop-up met een downloadknop. Offline of zonder bestand
 * gebeurt er gewoon niets.
 *
 * Nieuwe versie uitbrengen: versionCode/versie ophogen in AndroidManifest.xml,
 * UpdateChecker.CURRENT_VERSION ophogen, version.json bijwerken en de nieuwe
 * APK in de repository zetten.
 */
public class UpdateChecker {

    public static final int CURRENT_VERSION = 3;
    public static final String VERSION_URL =
            "https://raw.githubusercontent.com/igthubjnrcnrunnhuygfieucbhwihbrcy/Betapapimentokimk/vibe/papiamento-blitz-11a18b/version.json";
    public static final String FALLBACK_APK_URL =
            "https://github.com/igthubjnrcnrunnhuygfieucbhwihbrcy/Betapapimentokimk/raw/vibe/papiamento-blitz-11a18b/PapiamentoBlitz.apk";

    public interface Callback {
        void onResult(boolean updateAvailable, String notes, String apkUrl);
    }

    public static void checkAsync(final Callback cb) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(VERSION_URL).openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) sb.append(line);
                    r.close();
                    JSONObject json = new JSONObject(sb.toString());
                    int code = json.getInt("versionCode");
                    String notes = json.optString("notes", "");
                    String apkUrl = json.optString("apkUrl", FALLBACK_APK_URL);
                    cb.onResult(code > CURRENT_VERSION, notes, apkUrl);
                } catch (Exception e) {
                    cb.onResult(false, "", FALLBACK_APK_URL);
                }
            }
        }).start();
    }

    public static void showUpdateDialog(final Activity act, String notes, final String apkUrl) {
        new AlertDialog.Builder(act)
                .setTitle("🎉 Update beschikbaar!")
                .setMessage("Er is een nieuwere versie van Papiamento Blitz.\n\n"
                        + notes + "\n\nNu downloaden?")
                .setPositiveButton("Nu downloaden", (d, w) -> {
                    try {
                        act.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl)));
                    } catch (Exception ignored) {
                    }
                })
                .setNegativeButton("Later", null)
                .show();
    }
}
