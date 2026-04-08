# Ludashi-plus Progress Log

Build and modification history for Ludashi-plus — Winlator Ludashi v2.9 bionic with GOG, Epic Games, and Amazon Games store integration.

---

## Session: 2026-04-08 — Game Stores Integration

### Research & Reverse Engineering (Passes 1–38)
- Full reverse engineering of Winlator Ludashi v2.9 bionic APK documented in `LUDASHI_MASTER_MAP.md`
- Mapped all 38+ passes of analysis covering: Activities, Fragments, Container system, XServerDisplayActivity launch pipeline, SharedPreferences schema, DefaultVersion constants, FEXCore/Box64 preset tables, FrameRating update intervals, env var build order
- Confirmed 3 consecutive clean passes → convergence declared

### Game Stores Feature Design
- Decision: Port BannerHub GOG/Epic/Amazon store Activities to Ludashi-plus
- Source: `~/bannerhub/extension/*.java` (29 Java files)
- Launch bridge: `LudashiLaunchBridge` uses reflection to call `ContainerManager.getContainers()` and start `XServerDisplayActivity` — no compile-time dependency on Ludashi classes

### Files Changed

#### New: `extension/` (29 Java files)
All files adapted from BannerHub: package changed `app.revanced.extension.gamehub` → `com.winlator.cmod.store`

| File | Role |
|---|---|
| `LudashiLaunchBridge.java` | New — reflection-based Wine launch bridge |
| `GogLaunchHelper.java` | Adapted — delegates to LudashiLaunchBridge |
| `GogMainActivity.java` | Adapted — removed BH pending-exe finish() |
| `GogLoginActivity.java` | Ported as-is |
| `GogGamesActivity.java` | Ported as-is |
| `GogGame.java` | Ported as-is |
| `GogDownloadManager.java` | Ported as-is |
| `GogTokenRefresh.java` | Ported as-is |
| `GogInstallPath.java` | Ported as-is |
| `EpicMainActivity.java` | Ported as-is |
| `EpicLoginActivity.java` | Ported as-is |
| `EpicGamesActivity.java` | Adapted — pendingLaunchExe uses LudashiLaunchBridge |
| `EpicGame.java` | Ported as-is |
| `EpicDownloadManager.java` | Ported as-is |
| `EpicAuthClient.java` | Ported as-is |
| `EpicApiClient.java` | Ported as-is |
| `EpicCredentialStore.java` | Ported as-is |
| `AmazonMainActivity.java` | Ported as-is |
| `AmazonLoginActivity.java` | Ported as-is |
| `AmazonGamesActivity.java` | Adapted — pendingLaunchExe uses LudashiLaunchBridge |
| `AmazonGame.java` | Ported as-is |
| `AmazonDownloadManager.java` | Ported as-is |
| `AmazonApiClient.java` | Ported as-is |
| `AmazonAuthClient.java` | Ported as-is |
| `AmazonCredentialStore.java` | Ported as-is |
| `AmazonManifest.java` | Ported as-is |
| `AmazonPKCEGenerator.java` | Ported as-is |
| `AmazonLaunchHelper.java` | Ported as-is |
| `AmazonSdkManager.java` | Ported as-is |

#### Modified: `patches/res/menu/main_menu.xml`
- Added `group_game_stores` group (checkableBehavior=none) with 3 items:
  - `main_menu_gog` — "GOG"
  - `main_menu_epic` — "Epic Games"
  - `main_menu_amazon` — "Amazon Games"

#### Modified: `patches/res/values/public.xml`
- Added 4 new resource IDs:
  - `group_game_stores` = 0x7f090387
  - `main_menu_amazon` = 0x7f090388
  - `main_menu_epic` = 0x7f090389
  - `main_menu_gog` = 0x7f09038a

#### Modified: `patches/AndroidManifest.xml`
- Registered 9 new Activities:
  - `com.winlator.cmod.store.GogMainActivity`
  - `com.winlator.cmod.store.GogLoginActivity`
  - `com.winlator.cmod.store.GogGamesActivity`
  - `com.winlator.cmod.store.EpicMainActivity`
  - `com.winlator.cmod.store.EpicLoginActivity`
  - `com.winlator.cmod.store.EpicGamesActivity`
  - `com.winlator.cmod.store.AmazonMainActivity`
  - `com.winlator.cmod.store.AmazonLoginActivity`
  - `com.winlator.cmod.store.AmazonGamesActivity`

#### Modified: `patches/smali_classes8/com/winlator/cmod/MainActivity.smali`
- Added 3 `if-eq` branches before packed-switch in `onNavigationItemSelected()`:
  - `0x7f09038a` (GOG) → `startActivity(GogMainActivity)`
  - `0x7f090389` (Epic) → `startActivity(EpicMainActivity)`
  - `0x7f090388` (Amazon) → `startActivity(AmazonMainActivity)`

#### New: `.github/workflows/build.yml`
- Triggers on `v*` tags and `workflow_dispatch`
- Downloads Winlator-Ludashi v2.9 bionic APK from StevenMXZ/Winlator-Ludashi
- Decodes with apktool 2.9.3
- Applies patches/
- Compiles extension/*.java with android.jar + org.json → d8 → classes17.dex
- Injects classes17.dex into rebuilt APK
- Signs with AOSP testkey, uploads to GitHub release

### Build Process (Compilation Chain)
- `javac -source 8 -target 8 -cp android.jar:org-json.jar → ext_classes/`
- `d8 --release --min-api 26 → ext_dex/classes.dex`
- This is injected as `classes17.dex` into the APK zip after apktool build

### Launch Behavior (v1)
After game downloads in store Activity:
- `LudashiLaunchBridge.triggerLaunch(activity, exePath)` called
- Reflectively loads `ContainerManager`, gets first container
- Toast shows install path
- Starts `XServerDisplayActivity` with `container_id`
- Wine desktop opens — user navigates to exe manually

---

## Commits

| Commit | Tag | Description |
|---|---|---|
| (pending first build) | — | — |

---

*Updated automatically after every commit and build.*
