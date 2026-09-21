# CampusHire – Setup Guide (do this once)

Your package name must match everywhere. This code uses `com.example.campushire`.
If Android Studio gave your project a different package, change the `package` line at the top of every .kt file
(and `applicationId` / `namespace`) – or right-click the package in Android Studio > Refactor > Rename.

## 1. Gradle

### Project-level `build.gradle.kts` (root) – add to the `plugins { }` block
```kotlin
id("com.google.gms.google-services") version "4.5.0" apply false
```

### `app/build.gradle.kts`
Add to `plugins { }`:
```kotlin
id("com.google.gms.google-services")
```
Add to `dependencies { }`:
```kotlin
// Navigation, ViewModel, icons
implementation("androidx.navigation:navigation-compose:2.8.5")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
implementation("androidx.compose.material:material-icons-extended")

// REST API (Retrofit + Gson + logging) and image loading (Coil)
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
implementation("io.coil-kt:coil-compose:2.7.0")

// Firebase (Authentication + Firestore)
implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
implementation("com.google.firebase:firebase-auth")
implementation("com.google.firebase:firebase-firestore")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

// Unit tests
testImplementation("junit:junit:4.13.2")
```
Use **Empty Activity (Compose)** as the project template so Compose is already configured. minSdk 24+ is fine.
Then **File > Sync Project with Gradle Files**.

## 2. Manifest
Keep your generated `AndroidManifest.xml` and just add, above `<application>`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```
Make sure the `<activity android:name=".MainActivity">` still exists.

## 3. Firebase (this is the "online-hosted authentication service + database" for the video)
1. https://console.firebase.google.com > **Add project** > `CampusHire`.
2. **Add app > Android**, enter your package name, download **google-services.json** and put it in the `app/` folder.
3. **Build > Authentication > Get started > Sign-in method > Email/Password > Enable.**
4. **Build > Firestore Database > Create database** (production mode), then open **Rules** and paste `firestore.rules`.
5. Run the app. Register a user, then show in the video:
   - Firebase Console > Authentication > Users (password is never visible – Firebase stores a salted hash)
   - Firebase Console > Firestore > `users/{uid}` with `saved`, `applications`, `notifications`

## 4. Copy the files
Replace/copy the `app/src/main/java/...`, `app/src/test/java/...` and `.github/workflows/build.yml` files from this
folder into your project. Delete the template's default `Greeting`/theme Kotlin files if they clash
(the old `ui/theme/Theme.kt`, `Color.kt`, `Type.kt` – my `Theme.kt` replaces them).

## 5. GitHub
```
git init
git add .
git commit -m "Initial CampusHire prototype"
git branch -M main
git remote add origin https://github.com/<you>/CampusHire.git
git push -u origin main
```
Add `app/google-services.json` to `.gitignore`. In GitHub: Settings > Secrets > Actions > New secret named
`GOOGLE_SERVICES_JSON`, value = output of `base64 -w0 app/google-services.json` (Windows PowerShell:
`[Convert]::ToBase64String([IO.File]::ReadAllBytes("app\google-services.json"))`). If you skip the secret the
workflow uses a dummy file and still builds + runs tests.
Commit often (one commit per feature) – the rubric checks for multiple commits.

