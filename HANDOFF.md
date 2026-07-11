HANDOFF. Shared logbook between the coding sessions and the Cowork sessions. Read this at the start of every session, update it before you finish, newest entry on top.

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
