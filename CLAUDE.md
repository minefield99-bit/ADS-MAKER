Ads Maker, or AM. This repo is an Android app that turns a product picture plus optional video and notes into a short AI video ad. Current scope is TikTok only, 9:16, up to 8 seconds per clip, generated with Google Veo 3.1 Fast through the fal.ai API (Seedance 2.0 Fast is parked, dormant behind the VideoGenerator interface — it went early-access-only on fal.ai). Read README.md for setup and architecture, and ads-maker-spec.md for the full product brief.

How Claude Code and Cowork stay in sync

This repo is the shared channel between Claude Code sessions and Cowork sessions. Both must follow this protocol.

1. At the START of every session, read HANDOFF.md. It holds current status, blockers and next steps.
2. At the END of every session, and after any push, update HANDOFF.md. Say what changed, the CI state, and what is next. Keep it short and put the newest entry on top.
3. Never commit secrets. FAL_API_KEY lives only in local.properties, which is git-ignored.
4. Keep CI green. The workflow is .github/workflows/android.yml and it runs unit tests and builds a debug APK on every push.

Key facts

- Default branch: claude/ads-maker-android-spec-os78iw. The first push created it, so it became the default. There is no main branch yet.
- Single-module Android app. MVVM with Jetpack Compose. No DI framework, wiring is done in a small manual ServiceLocator.
- The app depends only on the VideoGenerator interface, so the Seedance implementation can be swapped by changing one line in ServiceLocator.
- Cost guard: every paid generation shows an estimate (Veo 3.1 Fast: 0.15 USD per second with audio; Final 8s ≈ 1.20 USD, Draft 4s ≈ 0.60 USD) and needs an explicit confirm tap.
- Freemium: free tier = one watermarked video per install (watermark burned on-device); owner mode in Settings = clean, uncapped. Every free trial still bills the owner's fal account.
- Unit tests: ./gradlew testDebugUnitTest

Owner

Luke, GitHub user minefield99-bit. Explain things to him in simple terms and without jargon, and ask clarifying questions before generating anything.
