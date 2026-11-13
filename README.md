# Papiamento Blitz 🇨🇼🇦🇼🇧🇶

Een **gratis** Android-app om supersnel Papiamento te leren, mét anti-stottertraining.
Geen advertenties, geen internet nodig, geen accounts.

## 📲 Installeren

1. Download **[PapiamentoBlitz.apk](PapiamentoBlitz.apk)** (of via de [Releases](../../releases)-pagina).
2. Open het bestand op je telefoon. Android vraagt om "Onbekende bronnen / installeren via browser" toe te staan — dat is normaal bij apps buiten de Play Store.
3. Klaar! Het app-icoon staat tussen je andere apps.

Werkt op Android 5.0 (Lollipop) en nieuwer.

## 🧠 Hoe de app je brein helpt (korte → lange termijn)

- **Werkgeheugen (korte termijn):** houdt nieuwe woorden maar 15–30 seconden vast, ±4–7 items tegelijk. Daarom oefen je in blokjes van 8 woorden.
- **Micro-herhaling:** binnen een sessie komen woorden na ±2 en ±10 minuten terug — net vóór het spoor vervaagt.
- **Spaced repetition (lange termijn):** wist je een woord? Dan komt het terug na **1 → 3 → 7 → 14 → 30 dagen**, precies op het moment dat je brein het bijna vergeet.
- **Actief ophalen (testegeffect):** eerst zelf hardop proberen, dan pas omdraaien. Quizzen versterken het geheugen 2–3× sterker dan herlezen.
- **Slaap:** tijdens de slaap wordt alles van korte naar lange termijn verplaatst. Daarom staat er in de app "herhaal morgen 5 minuten".

## 📅 Kies je eigen tempo (allemaal even effectief)

| Schema | Hoe | Tijd |
|---|---|---|
| ⚡ Snelle 1-dags cursus | 4 blokken van 10 min + 1 pauze van 5 min, eindtest | ±1 uur |
| 🗓️ 2× per week | 2 sessies van 20 min, app onthoudt wat vervalt | 20 min/sessie |
| 🐢 1× per week | 1 sessie van 30 min | 30 min/sessie |

## 🗣️ Anti-stottertraining

Vijf bewezen technieken (fluency shaping), meteen geoefend in het Papiamento:

1. Buikademhaling (getimede oefening in de app)
2. Zachte aanloop (easy onset)
3. Lichte articulatie-contacten
4. Gelijkmatig spreektempo (lettergreep voor lettergreep — de meest bewezen techniek)
5. Vermijding doorbreken

*Let op: oefenmateriaal, geen medische behandeling. Bij hardnekkig stotteren helpt een logopedist het meest.*

## 📚 Inhoud

8 lessen, 74 woorden en zinnen: groeten, kleine praatjes, familie, getallen, eten & drinken, werkwoorden, tijd & weer en handige zinnen (Curaçaos Papiamento).

## 🛠️ Zelf bouwen

Geen Gradle nodig:

```sh
export ANDROID_SDK=/pad/naar/sdk    # met build-tools;34.0.0 en platforms;android-34
export JAVA_HOME=/pad/naar/jdk-17
./build.sh
```

Resultaat: `PapiamentoBlitz.apk` (getekend met een debug-sleutel).
