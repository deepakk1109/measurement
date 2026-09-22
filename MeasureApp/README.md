# Measure App (Android, AR)

iPhone Measure app madhiri, ARCore vechu distance measure panra Android app. Rendu points tap panna, adhukkula distance meters/cm la காட்டும்.

## 1. Project ah Android Studio la open pannunga
- Android Studio (latest) install pannunga → **Open** → indha `MeasureApp` folder select pannunga.
- First open la Gradle sync automatic ah nadakum (wrapper missing na, **File → Sync Project with Gradle Files** click pannunga, adhu wrapper generate pannikum).
- Run pannurathukku: real Android phone (ARCore support irukanum — mostly ella modern phone-layum irukum), USB debugging on pannitu **Run ▶** click pannunga. Emulator la ARCore work aagadhu, so real device use pannunga.

## 2. Enna irukku indha code la
- `MainActivity.kt` — camera permission, ARCore session setup, tap → hitTest → anchor create, rendu anchors distance calculate.
- `ArRenderer.kt` — camera feed OES texture bind panra basic GL renderer. (Camera background draw panna exact shader boilerplate Google oda official `hello_ar_kotlin` sample la irukku — adha copy pannitu idhula plug pannikalam, measuring logic already ready.)
- `activity_main.xml` — AR surface + bottom la result text.

**Adhu missing** (extend pannikalam): connecting line/tape visual between points, multi-segment measure, area mode, save/share measurement. Base structure ready pannitten, feature add panna easy ah irukum.

## 3. Play Store la podanum na
1. `./gradlew bundleRelease` run pannunga (APK illa, **AAB** venum Play Store ku).
2. Signing key create pannunga: `keytool -genkey -v -keystore measureapp.jks -keyalg RSA -keysize 2048 -validity 10000 -alias measureapp`
3. `app/build.gradle.kts` la `signingConfigs` add pannunga (release build ku key attach panna).
4. [Google Play Console](https://play.google.com/console) la account create pannunga (one-time $25 fee), app create pannunga, AAB upload pannunga, store listing (screenshots, description, icon) fill pannunga, submit for review.

## 4. CI/CD (rendu option kudutrukken)

**Option A — GitHub Actions** (`.github/workflows/android-build.yml`): every push ku automatic ah debug APK build aagum, Actions tab la download pannikalam. Simple, free, quick setup.

**Option B — Ungal existing Jenkins→Docker→Quay.io→OpenShift pipeline** (`Jenkinsfile` + `docker-serve/`): Android SDK Docker image la APK build pannitu, adha oru chinna Nginx image ku pack pannitu, Quay.io ku push pannitu, OpenShift la deploy pannitum — so oru download page (`measureapp-download` route) vandhurum, adhula irundhu APK download pannikalam. Idhu ungal calculator app pipeline pola exact same pattern.

`Jenkinsfile` la `<your-username>` and Quay creds ID unga setup ku match aaga update pannikonga.
