HANDOFF. Shared logbook between the coding sessions and the Cowork sessions. Read this at the start of every session, update it before you finish, newest entry on top.

Entry 2026-07-11 night, written by a Claude Code coding session

STATUS: GREEN. Priority-one feature (in-app API key entry) is DONE and CI is passing. A new app-debug APK with the feature is ready to download.

Priority-one delivered (commit 10b3dfa, CI run #7 all green: SDK, unit tests, assembleDebug, artifact upload):
- Settings screen for the fal.ai key, reached two ways: the gear icon in the top bar, or the "Enter API key" button on the "API key needed" card. Paste / Replace / Remove. Shown password-style while typing (with a show/hide eye), masked to the last 4 chars after saving.
- Stored ENCRYPTED on-device: AES-256/GCM in the hardware-backed Android Keystore (SecureApiKeyStore + KeystoreCrypto). Only ciphertext is persisted. No third-party crypto dependency added.
- Never logged: HTTP logging is BASIC level (no headers), and the auth header is omitted when the key is blank.
- Precedence: a key entered in the app overrides the build-time BuildConfig.FAL_API_KEY; the BuildConfig key stays as a dev fallback. Logic is in ApiKeyResolver and unit-tested.
- Takes effect immediately: the OkHttp auth interceptor reads the key via a provider on every request, so no app restart is needed after saving. The create screen re-checks key state when you return from Settings.
- README updated with a phone-only setup path.

FOR LUKE, phone-only (no Android Studio) — the moment of truth (step 2 below):
1. Open the repo Actions tab, latest green "Android CI" run (run #7, commit 10b3dfa), download the "app-debug" artifact (a zip containing app-debug.apk). Artifact is ~20 MB, expires 2026-10-09.
2. Unzip, transfer app-debug.apk to your Android phone (API 26+), install it (allow "install unknown apps").
3. Open Ads Maker, tap the gear icon, paste your fal.ai key (from fal.ai/dashboard/keys), Save.
4. Pick a product picture, tap Generate, confirm the ~1.35 USD cost, and see if it produces a usable ad. This is the first real end-to-end test against live fal.ai.

Still unverified (needs that live run): whether the Seedance call actually returns a good video, the model slug bytedance/seedance-2.0-fast/image-to-video, the data-URI image path, and live pricing. If generation fails, the in-app error message plus logcat tag "AdsMakerUsage" will show what happened.

---

Entry 2026-07-11 evening, written by a Cowork session through the GitHub website

STATUS: still green. New top priority from Luke: make the app testable phone-only, without Android Studio.

FEATURE REQUEST, priority one: in-app API key entry.
Why: Luke has no Android Studio. The app-debug APK that CI builds installs fine but cannot generate, because FAL_API_KEY is baked in at build time and CI has no key.
What to build: a settings screen or first-run prompt where the user pastes the fal.ai key on the phone. Store it on-device, encrypted if practical. A key entered in the app overrides the BuildConfig one, and the BuildConfig fallback stays for dev builds. The existing API key needed notice should offer a button to that screen. Mask the key after saving and never log it. Push, confirm CI green, and note here when the new app-debug artifact is ready.
Then Luke, phone-only: download app-debug from the newest green run, install it, paste the key inside the app, and run one real generation, about 1.35 USD for a 15 second clip.

---

Entry 2026-07-11, written by a Claude Code coding session

STATUS: GREEN. All 3 compile errors fixed. Android CI run #4 for commit 15c46e9 passed end to end: Android SDK setup, unit tests (testDebugUnitTest), and the debug APK build (assembleDebug) all succeeded. The full app now compiles into an APK, not just the domain layer. Debug APK is uploaded as the run's "app-debug" artifact.

The riskiest UNTESTED thing is now step 2 below: no real end-to-end generation has run against a live fal.ai account yet. Green CI proves it compiles and unit tests pass; it does NOT prove the Seedance call actually returns a usable video.

Done this session:
- Root-caused the two converter errors: the app depended on the Jake Wharton converter (com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0), whose package is com.jakewharton.retrofit2.converter... and which has no asConverterFactory extension, while the import used the Square package retrofit2.converter.kotlinx.serialization.asConverterFactory. Mismatch = unresolved references.
  Fix: switched the dependency to the official Square artifact com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0 (version-matched to Retrofit). Import + json.asConverterFactory(...) call now resolve.
  Verified for real: compiled the full data/remote layer (NetworkFactory, FalApiService, FalDtos) against the real artifacts on a plain JVM here -> BUILD SUCCESSFUL. (The Android UI layer still can't be compiled locally, no Android SDK in this environment; that's what CI checks.)
- Fixed error 3 in CreateAdUiState.kt: changed `phase != Phase.Generating` to `phase !is Phase.Generating` (Generating is a data class, not a value).
- Scanned for other sealed-class == / != misuse: none found.

Next steps, in order:
1. Confirm the Android CI workflow turns green on this push (a coding session is watching / will re-check).
2. Luke: open the project in Android Studio with a real FAL_API_KEY in local.properties and run one real end-to-end generation on a phone or emulator. This answers the riskiest question, whether the AI produces a usable ad.
3. Verify the fal.ai model slug bytedance/seedance-2.0-fast/image-to-video and the data-URI image upload against a live fal.ai account.
4. Re-check Seedance pricing (about 0.09 USD/sec in CostEstimator.kt; endpoints in FalConfig.kt).

---

Entry 2026-07-11, written by a Cowork session through the GitHub website

STATUS: the automated build check is red. The pushed app code does not compile yet.

Note on pull requests: none is possible right now, because claude/ads-maker-android-spec-os78iw is the only branch and also the default branch. If a main branch is wanted later, create main first, then work through pull requests into it.

Done this session:
- Reviewed Android CI run 1 for commit 2a683d1. It failed in task :app:compileDebugKotlin, so unit tests and the APK build never ran.
- Added CLAUDE.md and this HANDOFF.md so the coding sessions and Cowork share one status file.

The build check failure, 3 Kotlin compile errors to fix:
1. app/src/main/java/com/adsmaker/app/data/remote/NetworkFactory.kt line 3 col 18: unresolved reference converter. The Retrofit converter import does not resolve.
2. Same file, line 48 col 39: unresolved reference asConverterFactory. Likely the Retrofit kotlinx-serialization converter dependency is missing from the app build file, or the import path is wrong.
3. app/src/main/java/com/adsmaker/app/ui/create/CreateAdUiState.kt line 28 col 28: Generating is a data class used as a plain value. Construct it with its arguments, or make it an object if it carries no data.

Next steps, in order:
1. Coding session: fix the 3 compile errors, run ./gradlew testDebugUnitTest, push, confirm the Android CI workflow turns green.
2. Luke: open the project in Android Studio with a real FAL_API_KEY in local.properties and run one real end-to-end generation on a phone or emulator. This answers the riskiest question, whether the AI produces a usable ad.
3. Verify the fal.ai model slug bytedance/seedance-2.0-fast/image-to-video and the data-URI image upload against a live fal.ai account. Both were built from docs only and never tested live.
4. Re-check Seedance pricing. The app assumes about 0.09 USD per second, in CostEstimator.kt, and the endpoint config is in FalConfig.kt.

Known Week 1 limits, by design: the optional video clip is accepted but not used by generation yet. Text overlay and narration come from the prompt and Seedance native audio. Instagram, YouTube, auto-posting, accounts and payments are deferred.
