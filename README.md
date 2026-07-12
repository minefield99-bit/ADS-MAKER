# Ads Maker — Android (Week 1 prototype)

Turn a picture (plus optional video/notes) into a short, TikTok-ready AI video ad.
This is the **Week 1** build: the full input → generate → preview → export loop for
one platform (TikTok), powered by **Seedance 2.0 Fast** via the **fal.ai** API.

> Background and future scope live in [`ads-maker-spec.md`](./ads-maker-spec.md).

## What it does (Week 1)
1. **Input** — pick a product picture (required), an optional video clip, and an
   optional `.txt` notes file describing the product.
2. **Cost confirmation** — shows the estimated cost and requires a confirm tap
   before any paid API call. fal.ai bills Seedance 2.0 Fast by resolution-scaled
   tokens (≈$0.24/sec at 720p, ≈$0.11/sec at 480p), so a **Final** 15s/720p clip
   is ≈**$3.63** and a **Draft** 5s/480p test is ≈**$0.54**.
   Toggle **Draft mode** on the create screen to iterate cheaply.
3. **Generate** — reads the notes, builds a prompt that encodes the signature
   *sharp → fade-to-blur + text/voice-over* style plus proven ad patterns (hook,
   one clear benefit, CTA), and calls Seedance 2.0 Fast (9:16, ~15s, audio on).
4. **Preview** — loops the result in-app.
5. **Export** — save to the gallery (Movies/AdsMaker) or share to any app.
   **Regenerate** re-runs a new paid generation (with the cost warning repeated).
6. **Errors** — API/network failures surface a clear message; the app never crashes.
7. **Usage log** — every attempt (success *or* failure) is logged for future billing.

Out of scope for Week 1 (documented for later): Instagram/YouTube formats,
auto-posting, accounts, and payments.

## Setup

You need a fal.ai API key (free to create): <https://fal.ai/dashboard/keys>.

### Option A — phone only, no Android Studio (easiest)
1. Download the **`app-debug`** artifact from the latest green [Android CI run](../../actions).
2. Install the APK on an Android phone (API 26+; allow "install unknown apps").
3. Open the app → tap the **settings (gear) icon** (or the **Enter API key** button on
   the "API key needed" card) → paste your fal.ai key → **Save**.
   The key is encrypted with the Android Keystore and stored only on that phone.

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
APKs (which have no baked-in key) still work once you paste a key in the app.

> **Re-verify Seedance pricing/access before launch** — rates and access routes in
> this space change fast. Both are isolated to one file each
> (`CostEstimator.kt`, `FalConfig.kt`).

## Architecture
Single-module app, MVVM, Jetpack Compose. No DI framework in Week 1 — a small
manual `ServiceLocator` wires everything.

```
domain/     Pure business logic (no Android deps, unit-tested)
            PlatformFormat · AdStyle · AdPromptBuilder · CostEstimator · AdInputs
data/
  remote/   fal.ai Retrofit service, DTOs, endpoint config, network factory
  video/    VideoGenerator (interface) + SeedanceVideoGenerator (impl) + downloader
  repository/ AdRepository — orchestrates the full pipeline
  usage/    UsageLogger — logs every attempt for future billing
  session/  UserSession placeholder for future accounts
ui/         Compose screens (create, preview), theme, ViewModel, components
util/       MediaUtils (read text, image→data URI, export/share)
di/         ServiceLocator
```

### Provider abstraction (swap-ability)
The app depends only on the `VideoGenerator` interface. Swapping providers (e.g.
if Seedance pricing/access changes) means writing one new implementation and
changing one line in `ServiceLocator` — nothing in the UI or repository changes.

### Built for the future (per the brief)
- **Cost awareness from day 1** — estimate + explicit confirmation before every
  generation; a per-ad budget constant guards spend.
- **Usage logging from day 1** — `UsageLogger` records every attempt (including
  failed ones, which can still cost API time), shaped like the future billing store.
- **User-session placeholder** — everything per-user already routes through
  `SessionProvider`, so adding real accounts later is additive, not a rewrite.

## fal.ai / Seedance API
Async queue pattern:
- `POST https://queue.fal.run/bytedance/seedance-2.0-fast/image-to-video`
  (falls back to `.../text-to-video` when no image is supplied)
- Header: `Authorization: Key <FAL_KEY>`
- Body: `{ prompt, image_url (data URI), duration:"15", aspect_ratio:"9:16",
  resolution:"720p", generate_audio:true }`
- Poll the returned status URL, then fetch the result: `{ video: { url }, seed }`.

## Tests
Pure-domain unit tests (prompt building, cost math, input validation):
```bash
./gradlew testDebugUnitTest
```

## Notes & known Week-1 limitations
- The optional **video clip** is accepted and carried through the input model, but
  Seedance 2.0 **Fast** image-to-video takes a single reference image; multi-input
  (reference-to-video) is a Week 2+ upgrade. The picture drives generation today.
- Text overlay + narration are expressed through the generation prompt and
  Seedance's native audio. Precise, editable overlays/TTS polish are Week 2+.
- The picked image is sent as a base64 data URI to avoid a separate upload step;
  large images are downscaled first.
