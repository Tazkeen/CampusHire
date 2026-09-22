# CampusHire – Student Jobs and Career Networking

> Android app (Kotlin + Jetpack Compose) that connects students to flexible, part-time, internship and remote work.

[![Android CI](https://github.com/Tazkeen/CampusHire/actions/workflows/build.yml/badge.svg)](https://github.com/Tazkeen/CampusHire/actions)

**Demo video:** <paste your unlisted YouTube link here>

## 1. Purpose of the app
Students struggle to find work that fits around lectures. CampusHire lets a student register, browse live job listings,
filter them, save favourites, apply with a CV and cover letter, and track every application in one place.

## 2. Features (Part 2 prototype)
| Feature | Status |
|---|---|
| Register / Login with Firebase Authentication (password hashed by Firebase, never stored in the app) | ✅ |
| Forgot password email + Change password | ✅ |
| Settings: dark mode, text size, high contrast, data saver, notifications, language, logout | ✅ |
| REST API: live jobs from the Remotive API through Retrofit | ✅ |
| Job search, filters (work type, category, location, availability and minimum pay), job details | ✅ |
| Save / unsave jobs (Cloud Firestore) | ✅ |
| Apply for a job with CV + cover letter, application tracker (Active / Completed / Withdrawn) | ✅ |
| Profile view + edit (Firestore), notifications, resources | ✅ |
| Employer-verification labels, profile-completion badges and peer-recommendation prototype | ✅ |
| Google/Microsoft/LinkedIn sign-in and CV cloud upload | 🔜 Final PoE |

## 3. Design considerations
- Screens follow the Part 1 prototype (Welcome, Register, Home, Jobs, Filters, Job Details, Apply, Applications, Profile, Resources, Notifications, Settings).
- Consistent blue/navy palette, rounded cards and one typography scale (Material 3) on every screen.
- Bottom navigation for the 5 main areas; back arrows everywhere else.
- Invalid input is handled without crashing: e-mail/password/phone validation, friendly Firebase error messages, retry button when the API is offline, empty-state messages.
- Accessibility: text size, high-contrast mode, content descriptions on icons.
- Data Saver setting genuinely stops company logos from downloading.

## 4. Architecture
```
UI (Compose screens) -> AppViewModel -> FirebaseRepository (Auth + Firestore)
                                     -> ApiClient / RemotiveService (Retrofit, REST)
                                     -> SettingsStore (SharedPreferences)
```
Firestore structure: `users/{uid}` (profile) with sub-collections `saved`, `applications`, `notifications`.
Security rules: a user can only read/write their own `users/{uid}` tree (`firestore.rules`).

## 5. REST API
Public **Remotive Jobs API** – `GET https://remotive.com/api/remote-jobs?search=&category=&limit=`.
Responses are mapped from `RemoteJob` to our `Job` model. Job data is provided by Remotive (remotive.com) and linked back to on the detail screen, as their terms require.

## 6. GitHub and GitHub Actions
- Repository initialised with this README; work committed regularly (one commit per feature).
- `.github/workflows/build.yml` runs on every push/pull request: checkout > JDK 17 > Gradle > **unit tests** > **assembleDebug** > uploads the test report and APK as artifacts.
- `google-services.json` is kept out of Git and injected from the `GOOGLE_SERVICES_JSON` repository secret.
- Unit tests: `ValidatorsTest` (email, password, phone, registration rules) and `JobFilterTest` (job type mapping, availability and salary filtering, HTML clean-up).

<!-- Refer to screenshots above: ![Login](docs/Screenshot 1.png) -->

## 7. How to run
1. Clone the repo, open in Android Studio (Ladybug or newer).
2. Add your own `app/google-services.json` (see SETUP_GUIDE.md).
3. Run on an emulator or phone (API 24+).

## 8. Logging
`Log.d/Log.e` with tags `CampusHireVM` and `OkHttp` record logins, API calls, saves, applications and errors (view in Logcat).

## 9. Youtube Link
https://youtu.be/0jDDV01In5I

## 10. AI Usage
AI tools were used only to generate the voice-over narration for the CampusHire demonstration video. The AI voice-over tool converted the prepared demonstration script into spoken audio, which was then used to explain the application’s features and functionality during the video presentation.

The AI tool was used for narration support only. It was not used as a source of academic research, references, or assessment content. The application demonstration, screenshots, functionality, and database evidence shown in the video remain the student’s own assessment work.

AI voice-over generated from the prepared CampusHire demonstration script was acknowledged in the video where applicable.

## 11. References
- Remotive Jobs API. https://github.com/remotive-com/remote-jobs-api
- Firebase Authentication & Cloud Firestore docs. https://firebase.google.com/docs
- Retrofit. https://square.github.io/retrofit/
- Coil. https://coil-kt.github.io/coil/
- Jetpack Compose & Navigation. https://developer.android.com/jetpack/compose
- GitHub Actions for Android. https://github.com/marketplace/actions/automated-build-android-app-with-github-action

