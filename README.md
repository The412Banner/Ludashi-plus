<p align="center">
  <img src="patches/res/mipmap-xxxhdpi-v4/ic_launcher.png" alt="Ludashi+" width="120">
</p>

<h1 align="center">Ludashi+</h1>

<p align="center">
  Winlator Ludashi 3.0 Bionic — with GOG, Epic Games, Amazon Games, and Steam stores built in.
</p>

---

Ludashi+ is a feature-rich, enhanced build of [Winlator-Ludashi v3.0](https://github.com/StevenMXZ/Winlator-Ludashi) by StevenMXZ. All credit for the core app goes to him — this project layers additional store support, a polished install experience, and community-friendly defaults on top of his work without changing the underlying emulation engine.

It is signed with the same key as StevenMXZ's original release, meaning it can update directly over his release and vice versa. Whether you do a fresh install or update over an existing installation is entirely your decision. **As always, use at your own risk.**

---

## What's new in v3.1.1 (hotfix)

- **Fixes the cyan/orange color bug when LSFG-VK is enabled** — in v3.1, enabling LSFG-VK caused a clean R↔B channel swap in games (cyan skin, orange wallpaper) and on the Wine desktop. Root cause was two upstream Pipetto Winlator commits that reworked the swapchain channel handling (BGRA8 swapchain + per-window RGBA8 texture allocation); the LSFG framegen layer reads swapchain bytes raw, so it saw mismatched semantics. Both commits have been reverted on the base APK; everything else from v3.1 is unchanged.

## What's new in v3.1

- **LSFG-VK frame generation** — the base APK now bundles the Lossless Scaling Vulkan implicit layer (from [`The412Banner/Winlator-Ludashi/lsfg-vk`](https://github.com/The412Banner/Winlator-Ludashi/tree/lsfg-vk), originally ported from ref4ik bionic). Toggle per-container in container settings (multiplier / flow scale / performance mode) or via the in-game quick menu — runtime, settings, launcher hook, and UI all ride along from the base
- **Unified download manager** — all GOG / Epic / Amazon downloads now flow through a single queue with Android progress notifications and a cancel action button
- **Downloads screen** — a new entry in the side menu (also reachable from a ⬇ button in each store header) shows every active and finished download in one place
- **Per-game detail screens** for GOG, Epic, and Amazon — tap a card to open the full game page; in-progress downloads now restore correctly across list, grid, and detail views
- **Reliable cancel** across all four stores — fixes cases where cancel left buttons stuck or kept downloading in the background
- **Refreshed base APK** — built on a slightly newer Ludashi 3.0 bionic with rebased resource IDs

---

## What Ludashi+ adds

### Game Stores

- **GOG** — log in with your GOG account, browse your library, download and launch games. Games install into the Wine prefix (Z: drive / imagefs).
- **Epic Games Store** — OAuth login via in-app browser, library sync, chunked manifest download (Fastly/Akamai CDN, no token required), launch via Wine shortcut.
- **Amazon Games** — PKCE OAuth login, GetEntitlements library sync, protobuf manifest download (LZMA/XZ), fuel.json launch with FuelSDK env vars.
- **Steam** — full Steam integration:
  - Log in with username + password, or scan a QR code (verified working)
  - Library sync — your full owned game list from Steam
  - Download with speed picker (Safe / Normal / Fast)
  - Pause and resume downloads — state persists across app restarts
  - Cancel downloads — stops immediately and removes all downloaded files
  - Launch installed games via Wine shortcut
  - Logout support
  - Auto-rotate on library and game detail screens

### First-Install Experience

- Full-screen branded splash screen on first launch — replaces the plain progress dialog
- Progress bar and percentage counter during asset extraction
- Proceed button appears at 100%; tapping requests storage permission then opens the app
- Already-installed fast-path: no splash shown on subsequent launches

### UI / App
- App label: **Ludashi+**
- Custom icon — distinct from base Ludashi so both can coexist on the same device
- Store section in the side menu (GOG / Epic / Amazon / Steam tabs)
- Banners Turnip Drivers included as the first driver repository entry

---

## What is the Ludashi package name?

The Ludashi build uses the package name `com.winlator.cmod` to mimic Ludashi, a popular benchmark app. Some Android phones — especially Xiaomi devices — automatically enable performance mode when such apps are detected, potentially reducing throttling and boosting performance.

---

## Installation

1. Download the latest APK from the [Releases](https://github.com/The412Banner/Ludashi-plus/releases) page.
2. Install directly over StevenMXZ's original Winlator-Ludashi or as a fresh install — both work. Your Wine containers in external storage are preserved either way.
3. Launch the app and wait for the first-run setup to complete.
4. To use a game store: open the side menu → tap GOG, Epic, Amazon, or Steam → log in.

---

## Useful Tips

- Here is a tutorial from the ZeroKimchi channel on how to use Winlator Bionic:
  https://youtu.be/EJDWZUGF9sk?si=e3Z-DdmMJSYKduWz
- If you are using an `x86_64` container and experiencing performance issues, try changing the Box86/Box64 preset to **Performance** in Container Settings → Advanced Tab.
- If you are using an `Arm64EC` container, try swapping between different FEXCore versions (2505, 2507, etc.) in the container settings for better compatibility or performance.
- For applications that use .NET Framework, try installing Wine Mono from Start Menu → System Tools.
- If some older games don't open, try adding the environment variable `MESA_EXTENSION_MAX_YEAR=2003` in Container Settings → Environment Variables.
- Try running games using a shortcut on the Winlator home screen — you can define individual settings per game there.
- To speed up installers, try changing the Box86/Box64 preset to **Intermediate** in Container Settings → Advanced Tab.

---

## Additional Components & Updates

Updated components (`wcps`) for improved compatibility and performance, plus new drivers:

- **Winlator Components (FEXCore, Box64/Box86, DXVK, etc.):**
  - Recommended: [Xnick417x's community content list](https://raw.githubusercontent.com/Xnick417x/Winlator-Bionic-Nightly-wcp/refs/heads/main/content.json) — add this URL under **App Settings** in the app
  - [StevenMXZ's Winlator-Contents Repository](https://github.com/StevenMXZ/Winlator-Contents)
- **Adreno GPU Drivers (Turnip):**
  - [Banners Turnip Drivers](https://github.com/The412Banner/Banners-Turnip) — included in the app's driver repository list
  - [Kimchi's AdrenoToolsDrivers Releases](https://github.com/K11MCH1/AdrenoToolsDrivers/releases)

---

## Credits and Third-Party

- **Winlator-Ludashi** by [StevenMXZ](https://github.com/StevenMXZ/Winlator-Ludashi) — base APK this project is built on
- **Original Winlator Bionic** by [Pipetto-crypto](https://github.com/Pipetto-crypto/winlator)
- **Original Winlator** by [brunodev85](https://github.com/brunodev85/winlator)
- **Winlator (coffincolors fork)** by [coffincolors](https://github.com/coffincolors/winlator)
- **JavaSteam** by [LossyDragon](https://github.com/LossyDragon/JavaSteam) — Steam client library
- Ubuntu RootFs (Bionic Beaver): [releases.ubuntu.com/bionic](https://releases.ubuntu.com/bionic)
- Wine: [winehq.org](https://www.winehq.org/)
- Box86/Box64 by [ptitSeb](https://github.com/ptitSeb)
- FEX-Emu by [FEX-Emu](https://github.com/FEX-Emu/FEX)
- PRoot: [proot-me.github.io](https://proot-me.github.io)
- Mesa (Turnip/Zink/VirGL): [mesa3d.org](https://www.mesa3d.org)
- DXVK: [github.com/doitsujin/dxvk](https://github.com/doitsujin/dxvk)
- VKD3D: [gitlab.winehq.org/wine/vkd3d](https://gitlab.winehq.org/wine/vkd3d)
- D8VK: [github.com/AlpyneDreams/d8vk](https://github.com/AlpyneDreams/d8vk)
- CNC DDraw: [github.com/FunkyFr3sh/cnc-ddraw](https://github.com/FunkyFr3sh/cnc-ddraw)
- [ptitSeb](https://github.com/ptitSeb) (Box86/Box64), [Danylo](https://blogs.igalia.com/dpiliaiev/tags/mesa/) (Turnip), [alexvorxx](https://github.com/alexvorxx) (Mods/Tips) and others
