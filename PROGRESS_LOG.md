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

## Commits & Builds

| Commit | Tag | Description | CI Run | Result |
|---|---|---|---|---|
| `80b227e` | v1.0.0-pre1 | feat: Game Stores integration — GOG, Epic Games, Amazon Games | [24152114060](https://github.com/The412Banner/Ludashi-plus/actions/runs/24152114060) | ❌ wrong APK filename |
| `0c60519` | v1.0.0-pre1 | fix: correct APK filename pattern (ludashi-bionic.apk) | [24152162792](https://github.com/The412Banner/Ludashi-plus/actions/runs/24152162792) | ❌ old tag ref |
| `224da83` | main | fix: use aapt1 for apktool rebuild | [24152205082](https://github.com/The412Banner/Ludashi-plus/actions/runs/24152205082) | ❌ ab_*.png not PNGs |
| `d31f636` | main | fix: delete ab_*.png before aapt, re-inject from base APK | [24152532867](https://github.com/The412Banner/Ludashi-plus/actions/runs/24152532867) | ❌ public.xml still declares ab_* |
| `94c8a87` | main | fix: remove ab_* public.xml declarations | [24152642431](https://github.com/The412Banner/Ludashi-plus/actions/runs/24152642431) | ❌ ab_gear_* also broken |
| `5819bb1` | main | fix: remove ab_gear_* from patches/public.xml | [24152754660](https://github.com/The412Banner/Ludashi-plus/actions/runs/24152754660) | ❌ new IDs not defined |
| `4817758` | main | fix: remove ab_gear_* and broaden sed pattern | [24152887419](https://github.com/The412Banner/Ludashi-plus/actions/runs/24152887419) | ❌ menu IDs not defined |
| `6692819` | main | fix: @+id/ in menu for new IDs | [24153007060](https://github.com/The412Banner/Ludashi-plus/actions/runs/24153007060) | ❌ still not defined |
| `e9f1f2f` | main | fix: add ids.xml to declare new menu IDs for aapt1 | [24153146047](https://github.com/The412Banner/Ludashi-plus/actions/runs/24153146047) | ❌ animated_background refs deleted PNGs |
| `15c1ad1` | main | fix: delete animated_background.xml too, re-inject both | [24153266958](https://github.com/The412Banner/Ludashi-plus/actions/runs/24153266958) | ❌ animated_background in patches/public.xml |
| `515019e` | main | fix: remove animated_background from patches/public.xml | [24153409292](https://github.com/The412Banner/Ludashi-plus/actions/runs/24153409292) | ❌ 403 on release upload |
| `f365554` | v1.0.0-pre1 | fix: add contents: write permission | [24153527621](https://github.com/The412Banner/Ludashi-plus/actions/runs/24153527621) | ✅ APK built, 403 fixed |
| `f365554` | v1.0.0-pre1 | (final tagged build) | [24153825677](https://github.com/The412Banner/Ludashi-plus/actions/runs/24153825677) | ✅ **success — release published** |

### Root Causes Fixed (CI debugging chain)
1. Wrong APK filename (`winlator-ludashi-bionic.apk` → `ludashi-bionic.apk`)
2. `ab_*.png` and `ab_gear_*.png` are raw animation frames, not valid PNGs — aapt1/aapt2 both reject them
3. `animated_background.xml` references the deleted pseudo-PNGs — must also be removed
4. `public.xml` in `patches/` retained entries for all deleted files → must remove them
5. New menu IDs (`group_game_stores`, `main_menu_gog/epic/amazon`) need explicit `<item type="id">` declaration in an ids.xml for aapt1
6. `GITHUB_TOKEN` needs `contents: write` permission for release creation in `workflow_dispatch`

### Signing
APK is signed with the AOSP public testkey (`testkey.pk8` / `testkey.x509.pem`, committed to repo). Fine for sideloading; not suitable for signature-verified updates. A private keystore stored as a GitHub Actions secret is the upgrade path if needed.

### Device Test Results (2026-04-08, v1.0.0-pre1)
- [x] Three store entries visible in nav drawer (GOG, Epic Games, Amazon Games) ✓
- [x] Store Activities open correctly from nav drawer ✓
- [x] GOG login/library/install pipeline works ✓
- [x] Download completes, game installed ✓
- [~] "Add to Launcher" booted Wine container directly (v1 behavior — no container picker, no shortcut written) → **fixed in v1.0.0-pre2**

---

## Session: 2026-04-08 — Container Picker + Shortcuts Integration (v1.0.0-pre2)

### Problem
"Add to Launcher" in v1 called `LudashiLaunchBridge.triggerLaunch()` which picked the first container and immediately booted `XServerDisplayActivity`. No shortcut was written, no container choice, user was dropped into a bare Wine desktop.

### Fix
Replaced launch behavior with shortcut creation:

1. **Container picker dialog** — `addToLauncher()` reflectively loads `ContainerManager.getContainers()`, builds a list of container names, shows `AlertDialog` for user to pick
2. **Shortcut file written** — after selection, `writeShortcut()` calls `Container.getDesktopDir()` via reflection, writes `{gameName}.desktop` to the container's Wine desktop directory
3. **Shortcut format** — standard Ludashi `.desktop` file:
   ```
   [Desktop Entry]
   Name={gameName}
   Exec={absLinuxExePath}
   Icon=
   Type=Application
   StartupWMClass=explorer

   [Extra Data]
   ```
4. **Success toast** — "Added to Shortcuts. Open side menu → Shortcuts to launch and configure it."

Shortcut appears in Ludashi's **Shortcuts** nav item. User can launch it and customize Wine setup (graphics driver, DX wrapper, emulator, etc.) via the native `ShortcutSettingsDialog`.

### Files Changed
- `extension/LudashiLaunchBridge.java` — replaced `triggerLaunch()` with `addToLauncher()` + `writeShortcut()`
- `extension/GogLaunchHelper.java` — `triggerLaunch()` → `addToLauncher(activity, name, path)`
- `extension/GogGamesActivity.java` — 4 call sites updated to pass `game.title`
- `extension/EpicGamesActivity.java` — `pendingLaunchExe(path)` → `pendingLaunchExe(name, path)`
- `extension/AmazonGamesActivity.java` — same as Epic

### Commits & Builds
| Commit | Tag | Description | CI Run | Result |
|---|---|---|---|---|
| `f2b737d` | v1.0.0-pre2 | feat: container picker + .desktop shortcut creation | [24157723585](https://github.com/The412Banner/Ludashi-plus/actions/runs/24157723585) | ✅ success |

### Pending: Device Testing (2026-04-08, v1.0.0-pre2)
- [ ] Container picker dialog appears on "Add to Launcher"
- [ ] Correct containers listed by name
- [ ] After picking, .desktop file written to container's desktop dir
- [ ] Shortcut appears in Ludashi side menu → Shortcuts
- [ ] Shortcut launches game correctly from Shortcuts list
- [ ] ShortcutSettingsDialog lets user customize Wine setup

---

*Updated automatically after every commit and build.*
