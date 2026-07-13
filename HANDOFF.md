HANDOFF. Shared logbook between the coding sessions and the Cowork sessions. Read this at the start of every session, update it before you finish, newest entry on top.

Entry 2026-07-13 later, written by a Cowork session through the GitHub website

STATUS: RED. CI run 13 for commit 4da25e3, the Veo switch build, failed in task :app:compileDebugKotlin. Unit tests and the APK never ran. Fix these before anything else.

Two Kotlin compile errors, both in app/src/main/java/com/adsmaker/app/data/video/VideoWatermarker.kt line 53:
At column 42: Java type mismatch. The call expects a Kotlin List of androidx.media3.common.Effect but receives a Guava ImmutableList of androidx.media3.effect.OverlayEffect. Use an explicit cast or pass a plain Kotlin list.
At column 73: same problem. Expects a Kotlin List of androidx.media3.effect.TextureOverlay but receives a Guava ImmutableList of androidx.media3.effect.TextOverlay. Same fix.

Next: coding session fixes VideoWatermarker.kt line 53, also lands any confirmed adversarial review findings, runs testDebugUnitTest, pushes, confirms Android CI green, and notes here when the new app-debug artifact is ready. Cowork then delivers the APK to Luke through the Claude chat and Luke runs the first real Veo generation.

---

Entry 2026-07-13, written by a Claude Code coding session

STATUS: implemented Luke's four decisions (provider switch, no house style, watermark freemium, future list). Pushed; CI result and the new app-debug artifact will be noted here once the run finishes.

DECISION 1 — PROVIDER SWITCHED TO VEO 3.1 FAST (Seedance parked, dormant).
- Active endpoint: fal-ai/veo3.1/fast/image-to-video (text-to-video fallback: fal-ai/veo3.1/fast). Contract verified live: duration enum "4s"/"6s"/"8s" (so max clip = 8s, as expected), resolution 720p/1080p, aspect auto/16:9/9:16, generate_audio, $0.15/sec WITH audio at both resolutions.
- New VeoVideoGenerator is active via one line in ServiceLocator; SeedanceVideoGenerator stays in the tree, compiling, dormant (fal.ai gates Seedance behind early access + business-only terms).
- New costs: Final = 8s at 720p = about $1.20. Draft = 4s = about $0.60 (the earlier "~$0.75 per 5s" note assumed 5s; Veo only does 4/6/8). CostEstimator now uses flat $0.15/sec.
- Durations: platform clip lengths are capped at 8s (Veo max). The old 15s TikTok target and the ~60s YouTube target need multi-clip stitching later — spec updated with honest budget math (a full 60s YouTube ad would be ~$9 on its own; the $10/3-platform budget needs re-deciding at Week 2+).

DECISION 2 — HOUSE STYLE REMOVED, USER DECIDES.
- The fixed "sharp foreground, fade-to-blur, narration takeover" style is gone from the prompt builder, UI copy, and tests. AdStyle.kt deleted.
- New optional "Ad style" text field on the create screen; whatever the user writes is passed to the model verbatim. Empty = the AI picks something platform-appropriate. Ad STRUCTURE (hook, one benefit, CTA) stays — that's effectiveness, not style.
- Reference-ad imitation ("make it look like this ad") — honest scoping, PROPOSAL ONLY, not built:
  * Needs a vision-capable model to analyze the reference (extra per-analysis cost); the video model can't watch a clip for us.
  * Pasted TikTok/IG links are the hard version — those platforms block programmatic fetching; a paste box that usually fails is worse than none.
  * Smallest honest version to build next: user SHARES A VIDEO FILE into Ads Maker -> sample a few frames on-device -> vision model writes a style recipe (pacing, hook, tone, text treatment — never cloning footage) -> recipe lands in the style field for the user to review/edit before generating. Link pasting only after that works.

DECISION 3 — WATERMARK FREEMIUM BUILT.
- FreemiumPolicy (pure, unit-tested): free tier = ONE watermarked generation per install, then generation pauses; owner mode = clean and uncapped. Blocked attempts never reach the paid API.
- Watermark is burned on-device AFTER download (Media3 Transformer, centered translucent "ADS MAKER · FREE TRIAL" text): the provider generates once, clean; for free users the clean copy is deleted the moment the watermarked copy exists (also deleted if watermarking fails — a free user can never end up with a clean file).
- Owner toggle in Settings ("Owner mode") for Luke's devices. Create screen shows the trial state; preview labels watermarked videos.
- Economics recorded in the spec: every video INCLUDING free trials bills Luke's fal account (~$0.60/draft, ~$1.20/final), hence the hard cap. Free counter is per-install until accounts/payments exist.

DECISION 4 — FUTURE LIST RECORDED (nothing built): "home factory" — Luke's high-end GPU laptop could later run a free open-source video model to serve free watermarked trials at zero per-video cost, paid finals staying on Veo. Parked behind the payments milestone. Windows desktop stays parked behind the phone test.

Verified this session (no Android SDK here, so JVM-verifiable parts only): domain tests pass (cost 8s=$1.20/4s=$0.60, prompt builder passes user style verbatim + no blur/house-style text, freemium policy allow/watermark/block); the whole remote layer + BOTH generators compile against real Retrofit/OkHttp artifacts; Veo duration snapping (4/6/8) unit-tested. An adversarial multi-agent review ran over the full diff before push. The Media3 Transformer watermark code compiles only on CI (Android artifact) and its runtime behaviour still needs the real-phone test.

NEXT: (1) CI green + new app-debug artifact for Luke — note below when ready. (2) Luke's first real generation, now ~$0.60 in draft mode. (3) His verdict on the shared-video-file style-analysis proposal above. (4) Multi-clip stitching + platform re-budget at Week 2+.

---

Entry 2026-07-12, written by a Claude Code coding session

STATUS: GREEN. Priority-one (cheap Draft mode) is DONE and CI run #11 (commit 89b1469) passed end to end. A new app-debug APK is ready.

IMPORTANT PRICING CORRECTION for Luke: the old "about 1.35 USD per 15s clip" was WRONG. fal.ai bills Seedance 2.0 Fast by resolution-scaled tokens, not a flat 0.09 USD/sec. Verified formula: tokens = width*height*duration*24/1024, at 0.0112 USD per 1000 tokens. That works out to 0.2419 USD/sec at 720p and about 0.108 USD/sec at 480p. So:
- Final (15s, 720p) is about 3.63 USD per clip, not 1.35.
- Draft (5s, 480p) is about 0.54 USD per clip.
CostEstimator now implements this real formula (was a flat 0.09/sec guess). The create screen, confirm dialog, and preview all show the corrected, mode-aware cost. README updated. (Note: fal.ai's own rate is higher than some third-party hosts like Atlas Cloud; we use fal.ai, so fal's rate is what applies.)

Priority-one delivered (Draft mode):
- Clearly-labeled "Draft mode" toggle on the create screen. Draft = 5s at 480p, audio on. Final = 15s at 720p (unchanged). GenerationMode + OutputResolution in the domain layer.
- Cost estimate, Generate button, confirm dialog, and preview screen all reflect the selected mode and resolution.
- Last-used mode is remembered across restarts (SharedPrefsAppPreferences).
- Usage log records draft vs final on every attempt (GenerationAttempt.mode; logcat tag AdsMakerUsage).
- Verified: domain unit tests (token math: 0.24192/sec at 720p, draft << final) pass; CI green including assembleDebug.

FOR LUKE, phone-only: download the "app-debug" artifact from the newest green Android CI run (run #11, commit 89b1469; ~20 MB, expires 2026-10-10). Install, open Settings (gear) and paste your fal.ai key if not already saved, then flip Draft mode ON for your first tries — about 0.54 USD each instead of 3.63. When a draft looks right, turn Draft off for the full-quality 15s/720p final.

Still unverified (needs a real phone run): whether Seedance returns a usable video, the model slug, the data-URI image path. Draft mode makes verifying this ~7x cheaper per attempt.

---

Entry 2026-07-12 afternoon, written by a Cowork session through the GitHub website

STATUS: green. The run 8 app-debug APK was delivered to Luke through the Claude chat, checksum verified against the artifact digest. Phone install and first real generation are up next on his side. New feature request from Luke before his test runs.

FEATURE REQUEST, priority one: cheap draft mode for generation.
Why: Luke wants to experiment several times without paying about 1.35 USD per try. Cost is fal.ai per-second pricing, so shorter and lower-resolution test clips are the lever.
What to build: a clearly labeled Draft toggle on the create screen. Draft generates about 5 seconds at the lowest resolution Seedance 2.0 Fast handles well, audio kept on, and shows its own lower cost estimate before the confirm tap. Final mode stays exactly as today, 15 seconds, 9:16, 720p. The usage log should record draft or final on every attempt. While in there, verify live fal.ai per-second pricing for both resolutions and correct CostEstimator if the 0.09 USD per second assumption is off. Nice to have if quick: remember the last used mode.
Also noted: BlueStacks on the laptop is deferred, Luke chose phone and tablet first. The Windows desktop project stays queued behind everything above.

---

Entry 2026-07-12, written by a Cowork session through the GitHub website

STATUS: green, unchanged. Recording a future project from Luke. Do not start it before the first real phone generation test passes.

FUTURE PROJECT, Week 2 or later: a true Windows desktop version of Ads Maker.
Why: Luke wants the app installable on his Windows laptop without helper programs. The Android APK already covers his phone and his Android tablet, so this is desktop only.
Direction: the app is Jetpack Compose with a clean domain and data split behind the VideoGenerator interface, so the natural route is Compose Multiplatform, sharing domain, data and most UI, with desktop replacements for the Android-only parts: media picking, gallery export and share, encrypted key storage, and BuildConfig. Scope it properly first; it is a real project, not a tweak.
Priority order stays: first the real end-to-end generation on the phone, then big-screen layout polish if wanted, then this desktop project.

---

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
