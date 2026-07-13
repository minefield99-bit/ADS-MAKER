# Ads Maker — Android

Turn a picture (plus optional video/notes) into a short, TikTok-ready AI video ad.
Generation is powered by **Google Veo 3.1 Fast** via the **fal.ai** API.
(The original provider, Seedance 2.0 Fast, is parked — early-access gated on
fal.ai — and its implementation stays dormant behind the `VideoGenerator`
interface.)

> Background, decisions, and future scope live in
> [`ads-maker-spec.md`](./ads-maker-spec.md). Session-to-session status lives
> in [`HANDOFF.md`](./HANDOFF.md).

## What it does
1. **Input** — pick a product picture (required for image-to-video), an optional
   video clip, and an optional `.txt` notes file describing the product.
2. **Style is yours** — an optional free-text field drives the look and feel
   (there is no built-in house style). Leave it empty and the AI picks a style
   that fits the platform.
3. **Cost confirmation** — Veo 3.1 Fast bills **$0.15/sec with audio** at
   720p/1080p, so a **Final** (8s, 720p) ≈ **$1.20** and a **Draft** (4s) ≈
   **$0.60**. The estimate is shown and must be confirmed before any paid call.
   Toggle **Draft mode** to iterate cheaply; the last-used mode is remembered.
4. **Generate** — builds a prompt from your notes + your style + proven ad
   structure (hook, one benefit, call-to-action) and calls Veo 3.1 Fast
   (9:16, up to 8s — the provider's max clip length, audio on).
5. **Preview / Export** — loops the result in-app; save to the gallery
   (Movies/AdsMaker) or share. **Regenerate** re-runs a new paid generation
   (with the cost warning repeated).
6. **Freemium** — free-tier videos get an on-device watermark and are capped at
   **one per install**; the **owner-mode** toggle in Settings produces clean,
   uncapped videos (for the app owner). Payments/accounts come later.
7. **Errors & logging** — API/network failures surface clear messages; every
   attempt (success or failure, draft or final) is logged for cost tracking
   (logcat tag `AdsMakerUsage`).

## Setup

You need a fal.ai API key (free to create): <https://fal.ai/dashboard/keys>.

### Option A — phone only, no Android Studio (easiest)
1. Download the **`app-debug`** artifact from the latest green [Android CI run](../../actions).
2. Install the APK on an Android phone (API 26+; allow "install unknown apps").
3. Open the app → tap the **settings (gear) icon** (or the **Enter API key**
   button) → paste your fal.ai key → **Save**. The key is encrypted with the
   Android Keystore and stored only on that phone.
4. Owner of the app? Flip **Owner mode** in the same Settings screen for clean,
   uncapped videos.

### Option B — Android Studio (for development)
1. Open the project in **Android Studio** (Ladybug or newer) with **Android SDK 35**.
2. Copy `local.properties.example` → `local.properties` and set:
   ```properties
   FAL_API_KEY=your_fal_api_key_here
   ```
   (`local.properties` is git-ignored; the key is injected via `BuildConfig`.)
3. Run on a device/emulator with **API 26+**.

**Key precedence:** a key entered in the app always overrides the build-time
`BuildConfig` key. The build-time key stays as a dev-only fallback, so CI-built
APKs (which have no baked-in key) work once you paste a key in the app.

## Architecture
Single-module app, MVVM, Jetpack Compose. No DI framework — a small manual
`ServiceLocator` wires everything.

```
domain/     Pure business logic (no Android deps, unit-tested)
            PlatformFormat · GenerationMode · OutputResolution · AdPromptBuilder
            CostEstimator · FreemiumPolicy · AdInputs · GeneratedAd
data/
  remote/   fal.ai queue API: Retrofit service, DTOs (Veo + dormant Seedance)
  video/    VideoGenerator (interface) · VeoVideoGenerator (ACTIVE)
            SeedanceVideoGenerator (DORMANT) · VideoDownloader · VideoWatermarker
  repository/ AdRepository — freemium gate → prompt → generate → download →
            watermark (free tier) → usage log
  usage/    UsageLogger — every attempt, incl. failures, for future billing
  settings/ Encrypted API key store · app preferences (mode, owner, trial count)
  session/  UserSession placeholder for future accounts
ui/         Compose screens (create, preview, settings), theme, ViewModels
util/       MediaUtils (read text, image→data URI, export/share)
di/         ServiceLocator
```

### Provider abstraction (swap-ability, proven in practice)
The app depends only on the `VideoGenerator` interface. The Seedance→Veo switch
was exactly the advertised change: one new implementation + one line in
`ServiceLocator`. Seedance remains in the tree, compiling, ready to reactivate.

## Veo 3.1 Fast API (fal.ai queue)
- `POST https://queue.fal.run/fal-ai/veo3.1/fast/image-to-video`
  (falls back to `.../veo3.1/fast` text-to-video when no image is supplied)
- Header: `Authorization: Key <FAL_KEY>`
- Body: `{ prompt, image_url (data URI), duration: "4s"|"6s"|"8s",
  aspect_ratio: "9:16", resolution: "720p", generate_audio: true }`
- Poll the returned `status_url`, then fetch `response_url` →
  `{ video: { url } }`.

## Tests
Pure-domain unit tests (prompt building, cost math, freemium policy, Veo
duration mapping):
```bash
./gradlew testDebugUnitTest
```

## Known limitations (by design, for now)
- One clip per generation, max 8s (Veo's limit). Longer ads need multi-clip
  stitching (planned).
- The optional video clip is accepted and carried through the input model, but
  image-to-video uses the picture; reference-video style analysis is a planned
  feature (see the spec for the honest scoping).
- The free-trial counter is per-install (server-side enforcement arrives with
  accounts/payments).
- The picked image is sent as a base64 data URI; large images are downscaled
  first.
