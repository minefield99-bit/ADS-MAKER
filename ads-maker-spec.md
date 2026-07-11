# Ads Maker (AM) — Project Spec

**Status:** Decisions locked in. Scope adjusted for a realistic 1-week first build.
This document is the full background + future scope. For what Week 1 actually
builds, see the "Scope Split" section and the repo `README.md`.

## What This App Does
Ads Maker is an Android app that automatically creates marketing videos for other
apps. A user gives it some materials (a picture, a video, or a text file
describing the app), and AM turns that into a ready-to-post video ad.

## Who It's For
Anyone who wants to make an ad — app developers, small businesses, or indie
creators. This will be sold publicly on the Play Store, so it needs to work well
for people who don't know video editing.

## What the User Gives AM
- A picture
- A video (optional — AM can make a video even without one)
- A text file (like notes or a description of the app/product)

## How AM Makes the Video
AM connects to an outside AI video-generation service (an API). AM doesn't build
video from scratch — it sends the pictures/video/text to that service and gets a
finished video back.

**Chosen service: Seedance 2.0 Fast** (by ByteDance)
- Good quality (up to 720p on the Fast tier, sharp enough for social)
- ~**$0.09 per second** of generated video — one of the cheapest options that
  still looks good (re-verify live pricing before launch)
- Official API access is via **fal.ai** (`queue.fal.run/bytedance/seedance-2.0-fast/*`);
  ByteDance's own **BytePlus ModelArk** is an alternative route
- Supports image-to-video, text-to-video, 9:16 vertical framing, durations 4–15s,
  and native audio synthesis (used for voice-over narration)

**Budget check — $10 per ad, across 3 platforms:**
| Platform | Length | Estimated Cost |
|---|---|---|
| TikTok | ~15 sec | ~$1.35 |
| Instagram Reels | ~15–20 sec | ~$1.35–$1.80 |
| YouTube | ~60 sec | ~$5.40 |
| **Total** | | **~$8–9** |

Each *attempt* costs money, so the app has a "review/confirm before you commit"
step so users don't accidentally burn budget on bad generations.

## Style Rules AM Must Follow

**1. Platform-specific formats**
| Platform | Length | Style |
|---|---|---|
| TikTok | ~15 sec | Fast, casual |
| Instagram Reels | ~15–20 sec | Fast, casual, visually punchy |
| YouTube | ~60 sec | Slower, more serious/professional |

**2. The signature visual style**
- Video starts in the foreground, sharp and clear
- After a moment, it fades back and gets blurry
- While that happens, text and a voice narration take over, to convince the viewer

**3. Follow "what works"**
AM studies patterns from top-performing app ads (hooks in the first few seconds,
clear call-to-action, etc.) and applies them automatically.

## Scope Split: Week 1 vs. Later

### Week 1 — What Gets Built
- Upload picture, video, and/or text file
- AI reads the text file to understand the product/selling points
- Auto-generate one video using Seedance 2.0 Fast
- Apply the "clear video → fade to blurred background + text/narration" style
- Auto-adjust output for **TikTok** first
- Preview the video before saving
- Manual export/download (share to socials manually — no auto-posting yet)
- Cost estimate + confirmation before each generation
- Usage logging of every attempt (for later billing)

### Added in Week 2+
- Instagram + YouTube versions (same ad, resized/re-paced per platform)
- Auto text overlay + narration polish
- Direct share/export to each platform
- Accounts, payments, and usage tracking UI
- Deeper study/application of top-ad patterns

## Business Notes (before selling publicly — not Week 1)
- **Pricing model** — per video? Subscription? Free with limits?
- **Cost control** — track usage per user so video costs don't outpace revenue
- **Play Store requirements** — privacy policy, content ratings, permissions

## Decisions Locked In
1. AI video service → **Seedance 2.0 Fast** (via fal.ai)
2. Platforms → **TikTok, Instagram, YouTube** (TikTok first)
3. Budget/timeline → **~$10/ad, 1 week for a working prototype**
