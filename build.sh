#!/bin/sh
# Bouwt PapiamentoBlitz-debug.apk zonder Gradle.
# Vereist: JDK 17+, Android build-tools 34 en platforms;android-34.
#
#   export ANDROID_SDK=/pad/naar/sdk      (met build-tools/34.0.0 en platforms/android-34)
#   export JAVA_HOME=/pad/naar/jdk-17
#   ./build.sh
set -e

SDK="${ANDROID_SDK:?Zet ANDROID_SDK naar je Android SDK map}"
JAVA_HOME="${JAVA_HOME:?Zet JAVA_HOME naar JDK 17+}"
BT="$SDK/build-tools/34.0.0"
PLATFORM="$SDK/platforms/android-34/android.jar"

rm -rf build
mkdir -p build/gen build/obj build/apk

"$BT/aapt2" compile --dir app/res -o build/res.zip
"$BT/aapt2" link -o build/unsigned.apk -I "$PLATFORM" \
    --manifest app/AndroidManifest.xml -R build/res.zip \
    --java build/gen --auto-add-overlay

find app/src -name '*.java' > build/sources.txt
"$JAVA_HOME/bin/javac" -source 17 -target 17 \
    -classpath "$PLATFORM" \
    -d build/obj \
    @build/sources.txt build/gen/com/kim/papiamento/R.java

"$BT/d8" --release --min-api 21 --lib "$PLATFORM" \
    --output build/apk $(find build/obj -name '*.class')

cd build/apk
zip -q -X ../unsigned.apk classes.dex
cd ../..

if [ ! -f debug.keystore ]; then
    "$JAVA_HOME/bin/keytool" -genkeypair -keystore debug.keystore \
        -storepass android -keypass android -alias androiddebugkey \
        -dname "CN=Android Debug,O=Android,C=US" \
        -keyalg RSA -keysize 2048 -validity 10000
fi

"$JAVA_HOME/bin/jarsigner" -keystore debug.keystore -storepass android \
    -keypass android -signedjar build/signed-raw.apk build/unsigned.apk androiddebugkey
"$BT/zipalign" -f 4 build/signed-raw.apk PapiamentoBlitz.apk
"$BT/apksigner" sign --ks debug.keystore --ks-pass pass:android \
    --key-pass pass:android PapiamentoBlitz.apk

echo "Klaar: $(ls -la PapiamentoBlitz.apk | awk '{print $5}') bytes -> PapiamentoBlitz.apk"
