# Ads Maker (AM) — Project Spec

**Status:** Decisions locked in below (updated 2026-07-13). Scope adjusted for a
realistic first build. This document is the full background + future scope. For
what is actually built today, see the repo `README.md` and `HANDOFF.md`.

## What This App Does
Ads Maker is an Android app that automatically creates marketing videos for other
apps and products. A user gives it some materials (a picture, a video, or a text
file describing the product), and AM turns that into a ready-to-post video ad.

## Who It's For
Anyone who wants to make an ad — app developers, small businesses, or indie
creators. This will be sold publicly on the Play Store, so it needs to work well
for people who don't know video editing.

## What the User Gives AM
- A picture
- A video (optional — AM can make a video even without one)
- A text file (notes or a description of the app/product)
- **The style they want** (optional, their own words — see "Style" below)

## How AM Makes the Video
AM connects to an outside AI video-generation service (an API). AM doesn't build
video from scratch — it sends the picture/text to that service and gets a
finished video back.

**ACTIVE service: Google Veo 3.1 Fast, via fal.ai** *(switched 2026-07-13)*
- Open to all fal.ai accounts — no gate, no waitlist
- Native audio (voice, music, sound effects), 720p or 1080p
- Clips of **4, 6 or 8 seconds** per generation (8s is the max — longer ads
  need multi-clip stitching, a later project)
- Public pricing, verified on the model page: **$0.15 per second with audio**
  ($0.10 without) at 720p/1080p

**PARKED service: Seedance 2.0 Fast (ByteDance)** *(previous choice)*
- Parked 2026-07-13: fal.ai gates Seedance 2.0 behind an early-access wall with
  business-only terms (verified live)
- The Seedance implementation stays in the codebase, dormant behind the
  `VideoGenerator` interface — reactivating it is a one-line change if access
  opens up
- Its billing was token-based (≈$0.24/sec at 720p), for reference

**Budget check at Veo prices — the honest math:**
| Clip | Length | Cost |
|---|---|---|
| Draft (test run) | 4 sec | ~$0.60 |
| Final TikTok / Reels clip | 8 sec | ~$1.20 |
| YouTube at the full ~60s target | ~8 stitched clips | **~$9.00** |

A TikTok + Reels pair fits easily in the old $10/ad budget (~$2.40 plus a few
drafts). A full-length 60s YouTube ad at Veo prices eats ~$9 on its own, so the
$10-per-ad-across-3-platforms target **does not survive as originally written**
— either the YouTube cut gets shorter (e.g. a 30s cut ≈ $4.50, or an 8s bumper),
or the per-ad budget rises. Decision deferred until multi-platform work starts
(Week 2+). Prices in this space move fast; re-verify before launch.

## Style: The User Decides *(changed 2026-07-13)*
There is **no built-in house style**. (The earlier fixed recipe — sharp
foreground, fade-to-blur, narration takeover — is removed entirely.)

What drives the look of an ad, in order:
1. **The user's own written style direction** (a free-text field on the create
   screen): pacing, mood, colors, captions, camera feel — anything, in their
   own words. Passed to the model verbatim. *(Shipped.)*
2. **A reference ad the user likes** — AM should imitate its *style and recipe
   only* (pacing, hook type, tone, text treatment), **never clone footage,
   people, or logos**. *(Planned — honest scoping below.)*
3. If the user gives nothing, the AI picks a style that fits the product and
   platform.

What stays built-in is ad **structure** (not style): a hook in the first
seconds, one clear benefit, a call-to-action — the "what works" patterns.

**Reference-ad analysis, scoped honestly (planned, not built):**
- Analyzing an ad requires a **vision-capable model** (extra cost per analysis)
  — the video model itself can't "watch" a reference clip for us.
- **Pasted TikTok/Instagram links are the hard version**: those platforms
  aggressively block programmatic fetching, and terms-of-service limit it.
  A link-paste box that usually fails is worse than none.
- **The realistic first version**: the user *shares a video file* into Ads
  Maker (Android share sheet) → AM samples a few frames on-device → sends them
  to a vision model → gets back a written style recipe → drops it into the
  style field **for the user to review and edit** before generating. Transparent,
  no scraping, and the user stays in control.
- Link pasting can come after that, where fetching is actually possible.

## Business Model: Watermark Freemium *(locked 2026-07-13)*
Ads Maker will not be free for end users:
- **Free users** get a **watermarked** video to try the product
- **Paying users** get clean videos (payments/accounts are a later milestone)

**The economics to keep in mind:** every video — *including free trials* —
bills the owner's fal.ai account (~$0.60 for a 4s draft, ~$1.20 for an 8s
final at Veo prices). Free usage is therefore tightly capped.

What's built today:
- Free tier: **one watermarked generation per install**, then generation pauses
  until payments exist
- The watermark is **burned into the video on-device after download** (Media3
  Transformer) — the provider output is generated once, clean, and the clean
  copy is deleted immediately for free users
- **Owner mode**: a Settings toggle (for Luke) that produces clean, uncapped
  videos on his own devices
- Every attempt is logged with mode + cost for the future billing system

## Scope Split

### Built (Week 1 + follow-ups)
- Upload picture / optional video / optional notes file
- Optional user style direction (free text) — no house style
- Draft (4s, ~$0.60) vs Final (8s, ~$1.20) modes, cost shown and confirmed
  before every paid call
- Generation via Veo 3.1 Fast (9:16, 720p, audio on), async queue with progress
- Preview, save to gallery, share; regenerate with repeat cost warning
- In-app encrypted fal.ai API key entry (phone-only use, no Android Studio)
- Watermark freemium: 1 free watermarked video, owner-mode toggle
- Usage logging of every attempt (mode, cost, outcome)

### Next (Week 2+)
- First real end-to-end generation test on a phone (the moment of truth)
- Reference-ad style analysis (shared-video-file version first — see above)
- Instagram + YouTube formats; multi-clip stitching for longer ads
- Accounts, payments, per-user usage tracking (before selling publicly)
- Play Store requirements: privacy policy, content ratings, permissions

### Future list (parked, do not build yet)
- **"Home factory"**: Luke's laptop has a high-end GPU. A free open-source
  video model running locally could serve the *free watermarked trials* at zero
  per-video cost, while paid finals stay on Veo quality. Parked behind the
  payments milestone — pointless to optimize free-trial cost before there is a
  paid tier funding it.
- **Windows desktop version** (Compose Multiplatform route sketched in
  HANDOFF) — after phone flow is proven.

## Decisions Locked In
1. AI video service → **Veo 3.1 Fast via fal.ai** (Seedance 2.0 Fast parked:
   early-access gated, business-only terms on fal.ai as of 2026-07-13)
2. Platforms → **TikTok first** (9:16, up to 8s/clip); Instagram + YouTube later
3. Style → **user-decided** (written direction now, reference-ad analysis
   later); no built-in house style
4. Business model → **watermark freemium**; payments are a later milestone
5. Budget → ~$1.20 per final clip, ~$0.60 per draft; the old $10/3-platform
   target needs re-deciding when multi-platform lands (see budget table)
