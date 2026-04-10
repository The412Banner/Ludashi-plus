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

### Device Test Results (2026-04-08, v1.0.0-pre2)
- [x] Container picker shows on "Add to Launcher" ✓ (dialog appeared)
- [~] Container list empty — bug: `setMessage()` + `setItems()` are mutually exclusive on Android; message view took over, items ListView never rendered → fixed in pre3
- [ ] Correct containers listed by name
- [ ] After picking, .desktop file written to container's desktop dir
- [ ] Shortcut appears in Ludashi side menu → Shortcuts
- [ ] Shortcut launches game correctly from Shortcuts list
- [ ] ShortcutSettingsDialog lets user customize Wine setup

---

## Session: 2026-04-08 — Container picker fix (v1.0.0-pre3)

### Bug
`AlertDialog.Builder.setMessage()` and `.setItems()` are mutually exclusive on Android.
The message TextView takes over and the items ListView is never rendered, so the
container picker dialog appeared empty even though containers loaded correctly.

### Fix
- `extension/LudashiLaunchBridge.java`: removed `.setMessage("Select a Wine container:")`
- Instruction folded into title: `"Select container for \"{gameName}\""`

### Commits & Builds
| Commit | Tag | Description | CI Run | Result |
|---|---|---|---|---|
| `8378d02` | v1.0.0-pre3 | fix: remove conflicting setMessage() so container list renders in picker dialog | [24158700638](https://github.com/The412Banner/Ludashi-plus/actions/runs/24158700638) | ✅ success |

---

## Session: 2026-04-08 — Steam Integration Phase 0 (CI Upgrade)

### Goal
Add Steam as a 4th game store tab. Phase 0 upgrades the CI to support Kotlin compilation and
JavaSteam JAR injection — the prerequisite for all subsequent Steam phases.

### What Changed (Phase 0)

**CI (`build.yml`) — complete rewrite:**
- Kotlin compiler auto-detection: detects Kotlin version bundled in base APK, downloads matching `kotlinc`
- JavaSteam download: fetches `javasteam-1.8.1-SNAPSHOT.jar` from JitPack
- `d8(javasteam.jar)` → classes18.dex (JavaSteam bundled separately)
- `kotlinc -no-stdlib extension/steam/*.kt` → d8 → classes19.dex+ (Steam Kotlin code)
- Java stores (GOG/Epic/Amazon) remain in classes17.dex (unchanged)
- Dynamic DEX index assignment: JavaSteam fills 18+, Kotlin Steam follows contiguously

**Resource patches:**
- `patches/res/menu/main_menu.xml` — added `main_menu_steam` item to `group_game_stores`
- `patches/res/values/public.xml` — added `main_menu_steam` = `0x7f09038b`
- `patches/res/values/ludashi_plus_ids.xml` — added `main_menu_steam` declaration

**Manifest patches:**
- Added `FOREGROUND_SERVICE_DATA_SYNC` permission
- Registered `SteamMainActivity`, `SteamLoginActivity`, `QrLoginActivity`, `SteamGamesActivity`
- Registered `SteamForegroundService` with `foregroundServiceType="dataSync"`

**Smali patch:**
- `patches/smali_classes8/.../MainActivity.smali` — added `const v4, 0x7f09038b` + `if-eq v1, v4, :start_steam` branch + `:start_steam` label block (starts `SteamMainActivity`)

**New files:**
- `extension/steam/SteamPlaceholder.kt` — smoke-test Kotlin file confirming CI compiles Kotlin

### Commits & Builds
| Commit | Tag | Description | CI Run | Result |
|---|---|---|---|---|
| `980737a` | v1.0.0-pre4 | feat: Phase 0 — Steam CI upgrade, menu wiring, manifest entries | [24165337471](https://github.com/The412Banner/Ludashi-plus/actions/runs/24165337471) | ❌ JitPack SNAPSHOT 0 bytes |
| `980737a` | v1.0.0-pre4 | (same — Gradle JitPack resolution also failed) | [24165396302](https://github.com/The412Banner/Ludashi-plus/actions/runs/24165396302) | ❌ javasteam.jar 0 bytes → ZipException |
| `c5450fc` | v1.0.0-pre4 | fix: build JavaSteam from source instead of JitPack download | [24165531623](https://github.com/The412Banner/Ludashi-plus/actions/runs/24165531623) | ✅ **success** |

### Root Cause Fixed (JavaSteam JAR)
- JitPack SNAPSHOT artifacts are build-on-demand; all direct wget and Gradle+JitPack resolution attempts returned 404 or 0 bytes on a cold CI runner
- Fix: clone LossyDragon/JavaSteam at depth 1 and run `./gradlew shadowJar` (fat JAR with all JavaSteam classes) directly in CI — fully reliable, no external artifact cache dependency
- Steam menu item, manifest entries, and SteamPlaceholder.kt Kotlin compile all pass ✅

---

## Session: 2026-04-09 — Steam Integration Phase 1 (Core Infrastructure)

### Goal
Implement Phase 1: SteamRepository, SteamForegroundService, and stub Activities.

### What Changed

**New files (`extension/steam/`):**
- `SteamRepository.java` — self-contained Java singleton wrapping JavaSteam SteamClient. Java (not Kotlin) to avoid Kotlin 2.2.0 metadata incompatibility. Owns SharedPreferences directly (no Kotlin dependencies). Handles connect/disconnect, callback pump on HandlerThread, auto-login with refresh token, login/logout, license list.
- `SteamPrefs.kt` — SharedPreferences singleton for Steam credentials (username, refreshToken, steamId64, displayName, etc.)
- `SteamEvent.kt` — sealed class event hierarchy: Connected, Disconnected, LoggedIn, LoggedOut, LoginFailed, SteamGuardEmailRequired, SteamGuardTwoFactorRequired, QrChallengeReceived, QrExpired, LibraryProgress, LibrarySynced, DownloadProgress, DownloadComplete, DownloadFailed
- `SteamGame.kt` — data class: appId, name, installDir, iconHash, sizeBytes, depotIds, type, isInstalled; computed headerUrl and iconUrl
- `SteamForegroundService.kt` — foreground service using `Notification.Builder` (not NotificationCompat); starts/stops SteamRepository
- `SteamMainActivity.kt`, `SteamLoginActivity.kt`, `QrLoginActivity.kt`, `SteamGamesActivity.kt` — Phase 1 stubs

**CI (`build.yml`) changes:**
- Java step: now compiles `extension/steam/*.java` alongside `extension/*.java`, with `javasteam.jar` on classpath
- Kotlin step: strips `META-INF/*.kotlin_module` from javasteam.jar (avoids "Kotlin metadata 2.2.0, expected 1.9.0" error), adds `ext_java_classes` to classpath so SteamRepository (Java-compiled) resolves

### Commits & Builds
| Commit | Tag | Description | CI Run | Result |
|---|---|---|---|---|
| `?` | v1.0.0-pre5 | feat: Phase 1 — SteamRepository, SteamForegroundService, stub Activities | [24166218839](https://github.com/The412Banner/Ludashi-plus/actions/runs/24166218839) | ❌ -no-stdlib strips stdlib from classpath |
| `?` | v1.0.0-pre5 | fix: add kotlin-stdlib.jar explicitly to Kotlin classpath | [24166440326](https://github.com/The412Banner/Ludashi-plus/actions/runs/24166440326) | ❌ `in` keyword in import |
| `?` | v1.0.0-pre5 | fix: remove Kotlin SteamRepository (backtick `in` import) | [24166798629](https://github.com/The412Banner/Ludashi-plus/actions/runs/24166798629) | ❌ wrong JAR submodule |
| `?` | v1.0.0-pre5 | fix: find correct JavaSteam library JAR from multi-module build | [24167377317](https://github.com/The412Banner/Ludashi-plus/actions/runs/24167377317) | ❌ Kotlin 2.2.0 metadata error |
| `?` | v1.0.0-pre5 | fix: port SteamRepository to Java to resolve Kotlin 2.2.0 metadata | [24167724171](https://github.com/The412Banner/Ludashi-plus/actions/runs/24167724171) | ❌ SteamPrefs.INSTANCE not on Java classpath |
| `55de913` | v1.0.0-pre5 | fix: SteamRepository self-contained (no SteamPrefs.INSTANCE dependency) | [24168048088](https://github.com/The412Banner/Ludashi-plus/actions/runs/24168048088) | ❌ .toLong() on primitive long |
| `f38465d` | v1.0.0-pre5 | fix: remove .toLong() — convertToUInt64() returns primitive long | [24168181587](https://github.com/The412Banner/Ludashi-plus/actions/runs/24168181587) | ❌ SteamRepository not on Kotlin classpath |
| `f93691a` | v1.0.0-pre5 | fix: strip javasteam Kotlin metadata; add ext_java_classes to Kotlin classpath | [24168366926](https://github.com/The412Banner/Ludashi-plus/actions/runs/24168366926) | ❌ initialize() missing Context arg |
| `d1e2496` | v1.0.0-pre5 | fix: pass Context to SteamRepository.initialize() | [24168527418](https://github.com/The412Banner/Ludashi-plus/actions/runs/24168527418) | ✅ **success** |

### Root Causes Fixed (Phase 1 debugging chain)
1. `-no-stdlib` removed stdlib from Kotlin compile classpath → must add `$HOME/kotlinc/lib/kotlin-stdlib.jar` explicitly
2. `in` is a Kotlin hard keyword → `import in.dragonbra.*` syntax error in Kotlin files → solution: write SteamRepository in Java instead
3. `./gradlew shadowJar` at repo root only produced the protobuf submodule JAR (no SteamClient) → fix: `./gradlew jar` all subprojects, then search for JAR containing `SteamClient.class`
4. JavaSteam latest master compiled with Kotlin 2.2.0; kotlinc 1.9.x rejects its metadata → Java bytecode interop has no metadata version check; writing SteamRepository in Java is the permanent fix
5. SteamRepository.java referenced `SteamPrefs.INSTANCE` (Kotlin class compiled in a LATER step) → must be fully self-contained with its own SharedPreferences helpers
6. `convertToUInt64()` returns primitive `long` in Java; calling `.toLong()` = dereferencing a value type
7. `javasteam.jar` still carries Kotlin metadata headers → strip with `zip -d javasteam.jar 'META-INF/*.kotlin_module'`; `ext_java_classes` not on Kotlin classpath → SteamRepository.class invisible to kotlinc
8. `SteamForegroundService.kt` called `initialize()` with no args; Java method requires `Context` → pass `this`

### Architecture Notes
- **SteamRepository.java is in Java, not Kotlin.** JavaSteam is compiled with Kotlin 2.2.0; kotlinc 1.9.x (matching base APK) cannot read Kotlin 2.2.0 metadata. Java bytecode interop bypasses this. This is the permanent architecture.
- **Kotlin metadata stripped from javasteam.jar before Kotlin compile step.** Class files remain functional as Java bytecode; only the Kotlin module metadata file is removed.
- **ext_java_classes on Kotlin classpath.** Java step runs first → Kotlin files can reference SteamRepository.

---

## Session: 2026-04-09 — Steam Integration Phase 2 (Credential Login UI)

### What Changed

**New: `extension/steam/SteamAuthManager.java`**
- Java singleton wrapping JavaSteam's `SteamAuthentication` API
- Implements `IAuthenticator` via anonymous inner class
- `getEmailCode` and `getDeviceCode` return `CompletableFuture<String>` that resolve when `submitGuardCode()` is called
- `acceptDeviceConfirmation()` returns `CompletableFuture<Boolean>` (informs UI, immediately resolves true)
- Main auth loop runs on dedicated thread; uses `CompletableFuture.get()` on `beginAuthSessionViaCredentials()` and `pollingWaitForResult()`

**Updated: `extension/steam/SteamLoginActivity.kt`**
- Full programmatic login UI: username/password EditTexts, Sign In button, QR button, progress spinner, status text
- `SteamAuthManager.AuthListener` callbacks: `onSteamGuardEmailRequired`, `onSteamGuardTotpRequired`, `onDeviceConfirmationRequired`, `onSuccess`, `onFailure`
- Steam Guard dialog with AlertDialog (email = text input, TOTP = numeric input)
- On success: `SteamRepository.loginWithToken()` + navigate to `SteamGamesActivity`
- Cancels auth in `onDestroy()`

### IAuthenticator API (discovered via javap)
```
CompletableFuture<String>  getDeviceCode(boolean)           // TOTP/mobile auth
CompletableFuture<String>  getEmailCode(String, boolean)    // email Steam Guard
CompletableFuture<Boolean> acceptDeviceConfirmation()       // mobile approval prompt
```
Note: method is `getDeviceCode`, NOT `getTotpCode` — only discovered via `javap -p`.

### Commits & Builds
| Commit | Tag | Description | CI Run | Result |
|---|---|---|---|---|
| `2fb3595` | v1.0.0-pre6 | feat: Phase 2 — Steam credential login UI + auth flow | [24168973466](https://github.com/The412Banner/Ludashi-plus/actions/runs/24168973466) | ❌ IAuthenticator returns CompletableFuture<T> |
| `b1ae266` | v1.0.0-pre6 | ci: add JavaSteam API inspection step | [24169130046](https://github.com/The412Banner/Ludashi-plus/actions/runs/24169130046) | ❌ (inspection only) |
| `4fc9569` | v1.0.0-pre6 | fix: CompletableFuture<T> return types; clientOSType field name | [24169252892](https://github.com/The412Banner/Ludashi-plus/actions/runs/24169252892) | ❌ getTotpCode → getDeviceCode; Boolean not Void |
| `8349367` | v1.0.0-pre6 | ci: inspect IAuthenticator + UserConsoleAuthenticator | [24169413540](https://github.com/The412Banner/Ludashi-plus/actions/runs/24169413540) | ❌ (inspection only) |
| `2939c58` | v1.0.0-pre6 | fix: getDeviceCode + CompletableFuture<Boolean> for acceptDeviceConfirmation | [~24169552000](https://github.com/The412Banner/Ludashi-plus/actions/runs/) | ✅ **success** |

### Root Causes Fixed (Phase 2 debugging chain)
1. `IAuthenticator` is a CompletableFuture-based API (not blocking, not Kotlin coroutines from Java's perspective)
2. `AuthSessionDetails.clientOSType` not `clientOsType` (field name)
3. `acceptDeviceConfirmation()` returns `CompletableFuture<Boolean>` not `Void`
4. TOTP method is `getDeviceCode(boolean)` not `getTotpCode(boolean)` — only visible via `javap -p`

---

---

## Session: 2026-04-09 — Steam Integration Phase 7 (Launch Bridge)

### Goal
Wire up the Launch button in `SteamGameDetailActivity` to `LudashiLaunchBridge`.

### What Changed

**Modified: `extension/steam/SteamGameDetailActivity.kt`**
- `onLaunchClicked()` now:
  1. Scans `game.installDir` tree for `.exe` files using `File.walkTopDown()`
  2. Skips paths containing redist/redistribut/vcredist/directx/etc.
  3. Picks the largest `.exe` found (heuristic: main executable is usually the biggest)
  4. Calls `LudashiLaunchBridge.addToLauncher(activity, gameName, exePath)`
     - Shows container picker dialog
     - Writes `.desktop` shortcut to selected Wine container desktop dir
     - Shortcut appears in Ludashi's Shortcuts nav item for launch + Wine config
- If no `.exe` found: Toast "No executable found in install directory"
- Helper method `findExe(File)` encapsulates the scan logic

### Architecture
Same pattern as GOG fallback exe scan (`GogDownloadManager` beta40). LudashiLaunchBridge handles all the Winlator reflection work — Steam detail activity just provides game name + exe path.

### Commits & Builds
| Commit | Tag | Description | CI Run | Result |
|---|---|---|---|---|
| `b4cf47b` | v1.0.0-pre12 | feat: Phase 7 — Steam game launch via LudashiLaunchBridge | [24183563783](https://github.com/The412Banner/Ludashi-plus/actions/runs/24183563783) | ✅ **success** |

---

**STOP — Phase 7 complete. All 7 phases implemented. Ready for device testing.**

### What needs testing (v1.0.0-pre12)
1. Steam login (credential flow + Steam Guard)
2. Library sync — games appear in list
3. Game detail screen — header art loads, metadata shows
4. Install — depot download starts, progress updates, completes
5. Launch — exe scan finds game exe, container picker appears, shortcut written, game accessible from Shortcuts

---

## Session: 2026-04-09 — QR Code Login

### Goal
Implement QR code Steam login (was a stub Toast since Phase 1).

### What Changed

**New: `extension/steam/SteamQrAuthManager.java`**
- Java singleton wrapping `SteamAuthentication.beginAuthSessionViaQR()`
- `startQrLogin(QrAuthListener)` — starts auth on background thread
- `QrAuthListener` interface: `onQrReady(url)`, `onQrRefreshed(url)`, `onSuccess()`, `onFailure(reason)`
- URL rotation watcher: polls `getChallengeUrl()` every 3s, notifies UI when Steam rotates the QR (~30s cycle)
- `cancel()` — stops watcher and clears listener

**Updated: `extension/steam/QrLoginActivity.kt`** (replaced Phase 1 stub)
- 260dp QR bitmap generated via ZXing `QRCodeWriter` → `BitMatrix` → `Bitmap`
- Auto-refreshes QR when `onQrRefreshed()` fires (URL rotation)
- Retry button on failure
- Cancel/back button
- Instruction text: "Open the Steam app → ☰ → Sign in via QR code"

**Updated: `.github/workflows/build.yml`**
- Added `zxing-core-3.5.2.jar` download from Maven Central
- Added to javac `-cp`, d8 (bundled into classes17.dex), and kotlinc `-classpath`

### API Discovery
- `QRAuthSession` → actually `QrAuthSession` (lowercase r) — caught by CI javap inspection
- `getChallengeUrl()` ✓
- `setChallengeUrlChanged(IChallengeUrlChanged)` exists (not used — polling approach is sufficient)
- `beginAuthSessionViaQR(AuthSessionDetails)` ✓ (one-arg variant, no CoroutineScope needed)

### Commits & Builds
| Commit | Tag | Description | CI Run | Result |
|---|---|---|---|---|
| `aadfdf8` | v1.0.0-pre13 | feat: QR login + ZXing | [24192276381](https://github.com/The412Banner/Ludashi-plus/actions/runs/24192276381) | ❌ QRAuthSession wrong class name |
| `e218eee` | v1.0.0-pre13 | ci: inspect QR auth classes | [24192519373](https://github.com/The412Banner/Ludashi-plus/actions/runs/24192519373) | ❌ (still compiling wrong name) |
| `7d2e998` | v1.0.0-pre13 | fix: QrAuthSession + inspect methods | [24192764293](https://github.com/The412Banner/Ludashi-plus/actions/runs/24192764293) | ✅ success |
| `97c179f` | v1.0.0-pre13 | ci: remove inspection steps | [24193131642](https://github.com/The412Banner/Ludashi-plus/actions/runs/24193131642) | ✅ **success** |

*Updated automatically after every commit and build.*

---

## Session: 2026-04-09 — Steam Integration Phase 4 (Library Sync via PICS)

### Goal
Implement PICS-based library sync: after login, resolve owned games from license list → PICS package info → PICS app info → SQLite.

### What Changed

**Modified: `extension/steam/SteamRepository.java`**
- New imports: `License`, `PICSRequest`, `PICSProductInfo`, `PICSProductInfoCallback`, `KeyValue`, `Collections`, `ConcurrentHashMap`, `AtomicInteger`
- `licenses` field type changed from `List<Object>` to `List<License>`
- Sync state constants: `SYNC_IDLE=0`, `SYNC_PACKAGES=1`, `SYNC_APPS=2`
- `pendingPackages` / `pendingApps` — `ConcurrentHashMap<Integer, PICSProductInfo>` accumulators
- Registered `PICSProductInfoCallback.class` subscriber in `registerCallbacks()`
- `onLicenseList()` — now persists licenses to DB (`upsertLicense`) and calls `syncPackages()`
- `syncPackages(List<License>)` — builds `PICSRequest(packageId, accessToken)` list, calls `steamApps.picsGetProductInfo(emptyList, pkgRequests, false)`
- `onPICSProductInfo(PICSProductInfoCallback)` — two-phase switch:
  - `SYNC_PACKAGES`: accumulates packages; on `!isResponsePending()` extracts appIds from `kv.get("appids").getChildren()`, persists `linkLicenseApp`, emits `LibraryProgress:1:<n>`, calls `syncApps()`
  - `SYNC_APPS`: accumulates apps; on `!isResponsePending()` parses `common.name/type/icon` + depot IDs, skips non-game/non-DLC types, `upsertGame()`, emits `LibrarySynced:<count>`
- `syncApps(List<Integer>)` — builds `PICSRequest(appId)` list, calls `picsGetProductInfo(appRequests, emptyList, false)`
- `syncLibrary()` — public method for pull-to-refresh; re-runs `syncPackages()` from cached license list

**Modified: `extension/steam/SteamGamesActivity.kt`**
- Full programmatic ListView UI replacing Phase 1 stub
- `SteamRepository.SteamEventListener`: handles `LibraryProgress:*` (updates status bar), `LibrarySynced:*` (reloads game list), `LoggedOut`/`Disconnected` (finishes activity)
- Header: ← Back, "Steam Library" title, ↻ Refresh button (calls `syncLibrary()`)
- Status bar: shows sync phase + count
- ListView: game name + type badge (GREEN for game, ORANGE for DLC)
- Empty state text with guidance

**Modified: `.github/workflows/build.yml`**
- Removed "Inspect PICS API (temp)" step — no longer needed

### Commits & Builds
| Commit | Tag | Description | CI Run | Result |
|---|---|---|---|---|
| `8f05b6d` | v1.0.0-pre9 | feat: Phase 4 — Steam library sync via PICS | 24181414708 | ✅ success |

---

## Session: 2026-04-09 — Steam Integration Phase 5 (Game Detail Screen)

### Goal
Implement game detail screen (header art, metadata, Install/Launch buttons) and wire up item clicks in the library list.

### What Changed

**New: `extension/steam/SteamGameDetailActivity.kt`**
- Receives `EXTRA_APP_ID` Intent extra; loads `SteamGame` from `SteamDatabase`
- Header image loaded async via `java.net.URL` + `BitmapFactory` (Steam CDN header.jpg)
- Shows: game name (22sp), type badge, size (Phase 6 will populate), install status
- Install button: "Install" (blue) when not installed, "Uninstall" (red) when installed
- Launch button: grayed/disabled until `isInstalled=true` (Phase 7 wires up LudashiLaunchBridge)
- `SteamEventListener`: handles `DownloadProgress:appId:done:total`, `DownloadComplete:appId`, `DownloadFailed:appId:reason` — updates progress bar + button states
- Phase 6 stub: Install button shows Toast "Download engine coming in Phase 6"
- Phase 7 stub: Launch button shows Toast "Launch coming in Phase 7"

**Modified: `extension/steam/SteamGamesActivity.kt`**
- Added `setOnItemClickListener` — tapping a row launches `SteamGameDetailActivity` with `EXTRA_APP_ID`

**Modified: `patches/AndroidManifest.xml`**
- Registered `SteamGameDetailActivity`

**Modified: `.github/workflows/build.yml`**
- Added "Inspect CDN/depot download API (temp)" step — discovers CDN/depot classes in JavaSteam JAR for Phase 6 implementation

### Commits & Builds
| Commit | Tag | Description | CI Run | Result |
|---|---|---|---|---|
| `0457423` | v1.0.0-pre10 | feat: Phase 5 — game detail screen + click wiring | [24182002486](https://github.com/The412Banner/Ludashi-plus/actions/runs/24182002486) | ✅ success |

---

## Session: 2026-04-09 — Steam Integration Phase 6 (Depot Download Engine)

### Goal
Implement full depot download: manifest IDs from PICS, depot key fetching, CDN download, VZip/LZMA decompression, file writing.

### What Changed

**Modified: `extension/steam/SteamDatabase.java`**
- DB version bumped 1 → 2 (triggers onUpgrade on existing installs)
- New `depot_manifests` table: (app_id, depot_id, manifest_id, size_bytes)
- `DepotManifestRow` inner class
- `upsertDepotManifest()`, `getDepotManifests(appId)` methods

**Modified: `extension/steam/SteamRepository.java`**
- Import `DepotKeyCallback`
- `depotKeys: ConcurrentHashMap<Integer, byte[]>` — depot decryption keys
- `getDepotKey(depotId)`, `requestDepotKey(depotId, appId)` methods
- `onDepotKey()` callback handler — stores key, emits `DepotKeyReady:<id>` or `DepotKeyFailed:<id>:<reason>`
- PICS SYNC_APPS: now also parses `depots/{id}/manifests/public/gid` + `maxsize` → `upsertDepotManifest()`; total size summed from depots and stored in `upsertGame()`
- `emit()` promoted to public (needed by SteamDepotDownloader)

**New: `extension/steam/SteamDepotDownloader.java`**
- Singleton download engine, 3-thread executor
- `installApp(appId, ctx)`: queues download, starts worker thread
- `pickCdnServer()`: GET Steam CDN list API, picks first CDN/SteamCache server with HTTPS; fallback = `lancache.steamcontent.com`
- `downloadAllDepots()`: for each depot: wait for key (30s poll), download manifest, parse files, download+decrypt+decompress each chunk, write at offset
- `downloadManifest()`: HTTPS GET `{cdn}/depot/{depotId}/manifest/{manifestId}/5`
- `decompressManifestBlob()`: auto-detects VZip / PKZIP / gzip / raw protobuf
- `parseManifest()`: `ContentManifest.ContentManifestPayload.parseFrom()` → list of `FileEntry` + `ChunkEntry`
- `downloadChunk()`: HTTPS GET `{cdn}/depot/{depotId}/chunk/{chunkGidHex}`
- `decryptChunk()`: AES-256-ECB via `javax.crypto.Cipher` (null key = pass-through)
- `decompressChunk()`: VZip → `decompressVZip()` → `lzmaDecompress()` via reflection (`org.tukaani.xz.LZMAInputStream`)
- Emits `DownloadProgress:<appId>:<done>:<total>`, `DownloadComplete:<appId>`, `DownloadFailed:<appId>:<reason>`

**Modified: `extension/steam/SteamGameDetailActivity.kt`**
- `onInstallClicked()` now calls `SteamDepotDownloader.getInstance().installApp(appId, ctx)`
- Uninstall also calls `File.deleteRecursively()` in background thread

**Modified: `.github/workflows/build.yml`**
- Removed CDN inspection step (no longer needed)

### Commits & Builds
| Commit | Tag | Description | CI Run | Result |
|---|---|---|---|---|
| `12430c3` | v1.0.0-pre11 | feat: Phase 6 — depot download engine | [24182600708](https://github.com/The412Banner/Ludashi-plus/actions/runs/24182600708) | ❌ emit() private; protobuf-java missing |
| `521a419` | v1.0.0-pre11 | fix: add protobuf-java.jar to javac classpath | [24182977284](https://github.com/The412Banner/Ludashi-plus/actions/runs/24182977284) | ❌ emit() fix not committed |
| `2f3c2e8` | v1.0.0-pre11 | fix: commit emit() public + protobuf-java.jar | [24183211903](https://github.com/The412Banner/Ludashi-plus/actions/runs/24183211903) | ✅ **success** |

### Root Causes Fixed (Phase 6 debugging chain)
1. `emit()` was edited locally but never committed — pushed `private` to GitHub; fix: commit local change
2. `ContentManifest.parseFrom()` requires `com.google.protobuf.*` at compile time; plain `./gradlew jar` does not bundle transitive deps → download `protobuf-java-3.25.4.jar` and add to javac `-cp`

---

## Session: 2026-04-09 — QR Login + Runtime fixes

### Phase QR: QR Code Login (pre13)
- `SteamQrAuthManager.java` — singleton wrapping `beginAuthSessionViaQR()`; polls `getChallengeUrl()` every 3s; `QrAuthListener` interface; callbacks on main thread
- `QrLoginActivity.kt` — ZXing `QRCodeWriter` → 260dp Bitmap; auto-refreshes on URL rotation; retry on failure
- `build.yml` — added `zxing-core-3.25.4.jar` from Maven Central; bundled into classes17.dex
- Key: class is `QrAuthSession` (lowercase r)

### Runtime Fixes from logcat (log_2026_04_09_10_11_21)

**Fix 1: `NoClassDefFoundError: org.apache.commons.lang3.SystemUtils`**
- Root cause: `AuthSessionDetails.<init>` calls `HardwareUtils.getMachineName()` which needs `commons-lang3` at runtime. Same missing-transitive-dep pattern as protobuf-java.
- Fix: download `commons-lang3-3.14.0.jar`, add to javac `-cp` and d8 (bundled into classes17.dex)
- Affects both credential AND QR login paths

**Fix 2: `POST_NOTIFICATIONS` prompted at wrong time**
- Root cause: system auto-prompts when foreground service starts — poor UX (fires mid-first-run setup)
- Fix: `SteamMainActivity` now requests `POST_NOTIFICATIONS` explicitly on Android 13+ (SDK >= 33) before starting `SteamForegroundService`; proceeds regardless of grant/deny

### Commits & Builds
| Commit | Tag | Description | CI Run | Result |
|---|---|---|---|---|
| `97c179f` | v1.0.0-pre13 | ci: remove QR inspection steps | [24193131642](https://github.com/The412Banner/Ludashi-plus/actions/runs/24193131642) | ✅ **success** |
| `07fc9f8` | v1.0.0-pre13 | fix: bundle commons-lang3 + POST_NOTIFICATIONS before service | [24194970921](https://github.com/The412Banner/Ludashi-plus/actions/runs/24194970921) | ✅ **success** |
| `bb72197` | v1.0.0-pre13 | fix: patch HardwareUtils.java to remove SystemUtils.IS_OS_ANDROID | [24195535199](https://github.com/The412Banner/Ludashi-plus/actions/runs/24195535199) | ✅ **success** |

**Fix 3: `NoSuchFieldError: IS_OS_ANDROID`**
- Root cause: `HardwareUtils.getMachineName()` (line 206) reads `SystemUtils.IS_OS_ANDROID` — this field does NOT exist in commons-lang3 3.14.0 (added in a later release). Even though we bundled commons-lang3, the field is missing.
- Fix: patch `HardwareUtils.java` in the JavaSteam source clone (before `./gradlew jar`) with `sed -i 's/SystemUtils.IS_OS_ANDROID/false/g'`. The Android branch is skipped; method falls through to hostname — fine for Steam auth.
- No need to upgrade commons-lang3 version.

**Installation clarification (release description updated)**
- Cannot install over top of original Ludashi 2.9 — different signing key (AOSP testkey vs original). Android rejects with signature mismatch.
- Correct flow: uninstall original → install Ludashi-plus. Winlator containers in external storage survive the uninstall.

**Fix 4: `NoClassDefFoundError: com.google.protobuf.ProtocolMessageEnum`** (logcat log_2026_04_09_10_37_24)
- Root cause: `protobuf-java.jar` was added to javac `-cp` (compile-time) but NOT to d8, so protobuf classes were absent from all DEX files at runtime. Same missing-transitive-dep pattern as commons-lang3.
- Stack: `SteamQrAuthManager.lambda$startQrLogin$4` → `ProtocolMessageEnum` → ClassNotFoundException
- Fix: add `protobuf-java.jar` to the d8 bundling step → lands in classes17.dex
- Affects both QR and credential login (both hit protobuf internally via JavaSteam auth)

### Commits & Builds (continued)
| `bb72197` | v1.0.0-pre13 | fix: patch HardwareUtils.java IS_OS_ANDROID | [24195535199](https://github.com/The412Banner/Ludashi-plus/actions/runs/24195535199) | ✅ **success** |
| `44c9c3f` | v1.0.0-pre13 | fix: bundle protobuf-java.jar into classes17.dex | [24196163292](https://github.com/The412Banner/Ludashi-plus/actions/runs/24196163292) | ✅ **success** |
| `66206c5` | v1.0.0-pre13 | fix: use JavaSteam's own protobuf from Gradle cache | [24197307442](https://github.com/The412Banner/Ludashi-plus/actions/runs/24197307442) | ✅ **success** |

**Fix 5: `NoClassDefFoundError: com.google.protobuf.RuntimeVersion$RuntimeDomain`** (logcat log_2026_04_09_11_00_01)
- Root cause: hardcoded `protobuf-java-3.25.4.jar` (3.x) doesn't have `RuntimeVersion` — that class only exists in protobuf 4.x. JavaSteam's generated protobufs (`SteammessagesAuthSteamclient`) reference it at static init.
- Fix: after `./gradlew jar`, find the exact protobuf JAR Gradle resolved from `~/.gradle/caches` and copy it as `protobuf-java.jar`. Removes the hardcoded 3.25.4 wget. Guarantees version alignment with JavaSteam.
- Rule: always pull transitive deps from the Gradle cache after building JavaSteam, never hardcode versions.

**Fix 6: `NoSuchFieldError: IS_OS_MAC_OSX_SONOMA`** (logcat log_2026_04_09_11_18_27)
- Root cause: The base APK (ludashi-bionic.apk) already bundles an **older** commons-lang3 in its own DEX files (classes.dex–classes16.dex). Android classloader finds that version first — the newer commons-lang3-3.14.0 in our classes17.dex is shadowed. `IS_OS_MAC_OSX_SONOMA` was added in 3.14.0 but the base APK's version predates it. `Utils.<clinit>` (line 67) crashes → entire Utils class fails to init → `steamClient` stays null → both QR and credential login show "Parameter ... null: steamClient".
- Fix: add sed patch in the JavaSteam build step for `Utils.java` (same approach as HardwareUtils.java): replace `IS_OS_MAC_OSX_SONOMA/VENTURA/MONTEREY/BIG_SUR` and `IS_OS_WINDOWS_11` with `false` before `./gradlew jar`. These are OS detection fields — hardcoding `false` means JavaSteam skips those OS branches (irrelevant on Android).
- Tag: v1.0.0-pre14

### Commits & Builds (continued)
| `f8c4c42` | v1.0.0-pre14 | fix: patch Utils.java to replace missing SystemUtils OS fields | [24198322130](https://github.com/The412Banner/Ludashi-plus/actions/runs/24198322130) | ✅ success |

**Fix 7: `NoClassDefFoundError: Lkotlinx/coroutines/future/FutureKt;`** (logcat log_2026_04_09_11_38_03)
- Root cause: `SteamAuthentication.beginAuthSessionViaQR()` (line 110) calls `kotlinx.coroutines.future.await()` from `kotlinx-coroutines-jdk8`. The base APK has `kotlinx-coroutines-core` but NOT the `jdk8` extension — that artifact bridges Kotlin coroutines to `CompletableFuture`, which is irrelevant for a pure-Android app like Ludashi.
- Same missing-transitive-dep pattern as protobuf-java and commons-lang3.
- Fix: pull `kotlinx-coroutines-jdk8-*.jar` from Gradle cache after `./gradlew jar` (same pattern as protobuf-java), bundle into classes17.dex via d8.
- Tag: v1.0.0-pre15

| `6426037` | v1.0.0-pre15 | fix: bundle kotlinx-coroutines-jdk8 into classes17.dex | [24199127825](https://github.com/The412Banner/Ludashi-plus/actions/runs/24199127825) | ❌ jdk8 not in Gradle cache (merged into core in 1.7+) |
| `5dd51aa` | v1.0.0-pre16 | fix: extract kotlinx/coroutines/future/ classes from Gradle cache | [24199867249](https://github.com/The412Banner/Ludashi-plus/actions/runs/24199867249) | ✅ **success** |

**Fix 8: "the steam client instance must be connected"** (logcat log_2026_04_09_12_09_19)
- Root cause: `SteamForegroundService.start()` and `SteamLoginActivity` are launched back-to-back synchronously. `SteamClient.connect()` is async — `ConnectedCallback` has not fired by the time the user taps QR Login (or even types credentials). `SteamAuthentication(steamClient)` enforces that the client is connected, so auth throws immediately.
- Fix: both `QrLoginActivity` and `SteamLoginActivity` now check `SteamRepository.isConnected` before starting auth. If not yet connected, register a `SteamEventListener`, wait for `"Connected"` event, then proceed. Listener removed in `onDestroy()` to prevent leaks.

| `15b66ff` | v1.0.0-pre17 | fix: wait for SteamClient connection before starting QR/credential auth | [24200643204](https://github.com/The412Banner/Ludashi-plus/actions/runs/24200643204) | ✅ **success** |

**Fix 9: QR screen spins indefinitely on "Connecting to Steam…"** (logcat log_2026_04_09_12_30_06)
- Root cause 1: `SteamRepository` was configured with `WEB_SOCKET` only. On networks where WebSocket to Steam's CM servers hangs, no `ConnectedCallback` ever fires. Fix: allow both `WEB_SOCKET` and `TCP` so JavaSteam can fall back.
- Root cause 2: No timeout on the connection wait — spinner ran forever. Fix: 30-second `Handler.postDelayed` in `QrLoginActivity` and `SteamLoginActivity`; shows error + Retry if not connected in 30s.

| `0a6cf33` | v1.0.0-pre18 | fix: TCP fallback + 30s connection timeout | [24201702454](https://github.com/The412Banner/Ludashi-plus/actions/runs/24201702454) | ✅ **success** |

**Fix 10: connection timeout too long + listener leak + no network error message** (logcat log_2026_04_09_12_30_06)
- Reduced CM connection timeout 30s → 10s; fixed Retry accumulating duplicate listeners; added reachability pre-check (HEAD store.steampowered.com); changed catch(Exception) → catch(Throwable) in pump to survive NoClassDefFoundError.

**Fix 11: three-state network diagnosis** (no internet / Steam blocked / CM blocked)
- Reachability check now pings connectivitycheck.gstatic.com first, then api.steampowered.com, then CM port. Shows actionable message per case.

**Fix 12: CM connection never established** — withDirectoryFetch(true) + auto-reconnect
- `SteamConfiguration` was missing `withDirectoryFetch(true)` → CM server list never fetched → `getNextServerCandidate()` returned null immediately → DisconnectedCallback fired with no network attempt. Fixed. Also added up to 5 auto-reconnect retries with backoff.

**Fix 13: crash on Steam connect — use TCP-only**
- WebSocket in JavaSteam requires `io.ktor.client.engine.cio.CIO` which is not in our DEX → hard crash in WebSocketConnection.connect(). Switched to TCP-only (`ProtocolTypes.TCP`).

**Fix 14: `NoClassDefFoundError: kotlin.enums.EnumEntriesKt`** (logcat log_2026_04_09_13_xx)
- `ServerQuality.kt` in JavaSteam uses `enumEntries()` (Kotlin 1.9+ API) which references `EnumEntriesKt`. Base APK's Kotlin stdlib predates 1.9.
- Note: TCP WAS connecting and receiving Steam packets by this point; crash happened during packet processing.
- Fix: extract `kotlin/enums/` package from our kotlinc stdlib JAR and bundle into classes17.dex.

**Steam grayed out (pre24)** — remaining JavaSteam compat issues deferred
- After fixing EnumEntriesKt, further compat issues remained. Steam menu item temporarily disabled (`android:enabled="false"`, title = "Steam (Coming Soon)") to prevent crashes while other stores are tested.

### Commits & Builds (pre19–pre24)
| Commit | Tag | Description | CI Run | Result |
|---|---|---|---|---|
| `946b467` | v1.0.0-pre19 | fix: 10s connection timeout, reachability check, listener leak fix | [24203451631](https://github.com/The412Banner/Ludashi-plus/actions/runs/24203451631) | ✅ **success** |
| `34ed2b3` | v1.0.0-pre20 | fix: three-state network diagnosis | [24204392065](https://github.com/The412Banner/Ludashi-plus/actions/runs/24204392065) | ✅ **success** |
| `53b5f31` | v1.0.0-pre21 | fix: withDirectoryFetch(true) + auto-reconnect | [24204982124](https://github.com/The412Banner/Ludashi-plus/actions/runs/24204982124) | ✅ **success** |
| `dd4c046` | v1.0.0-pre22 | fix: TCP-only, remove WebSocket | [24205610294](https://github.com/The412Banner/Ludashi-plus/actions/runs/24205610294) | ✅ **success** |
| `131a1eb` | v1.0.0-pre23 | fix: bundle kotlin.enums classes (EnumEntriesKt) | [24206186631](https://github.com/The412Banner/Ludashi-plus/actions/runs/24206186631) | ✅ **success** |
| `dc1e712` | v1.0.0-pre24 | chore: gray out Steam menu item (coming soon) | [24206186631](https://github.com/The412Banner/Ludashi-plus/actions/runs/24206186631) | ✅ **success** |

---

## Session: 2026-04-09 — GOG Launch Fix

**Fix: GOG shortcut "file not found" when launching from Shortcuts** (logcat log_2026_04_09_14_42_04)
- Root cause: `Shortcut.java:85` parses `Exec=` by calling `execArgs.substring(execArgs.lastIndexOf("wine ") + 4)`. `LudashiLaunchBridge` was writing `Exec=/data/user/0/.../game.exe` — no "wine " prefix → `lastIndexOf()` returns -1 → `substring(3)` = garbage path → Wine reports "file not found".
- Fix: convert Android absolute path to Wine Z: drive path (`/foo/bar` → `Z:\foo\bar`), escape each `\` as `\\\\` for `.desktop` file format (so `Shortcut.unescape()` restores single `\`), write `Exec=wine Z:\\\\data\\\\...\\\\game.exe`.
- File changed: `extension/LudashiLaunchBridge.java` — `writeShortcut()` content generation.

**Fix 2: Z: drive maps imagefs, not /** (logcat log_2026_04_09_15_02_33)
- Winlator maps Z: to `container.getRootDir()/../..` = `imagefs/` (WineUtils.java:22), NOT to `/`. Games at `filesDir/gog_games/` are outside imagefs and unreachable by Wine.
- Fix: `GogInstallPath.getInstallDir()` now installs to `filesDir/imagefs/gog_games/<name>/`. Added `GogInstallPath.toWinePath()` to strip imagefs prefix and return `Z:\gog_games\...\game.exe`. `LudashiLaunchBridge` uses `toWinePath()`.
- Games previously downloaded to the old path must be re-downloaded.

**Diagnosis: launch confirmed working** (logcat log_2026_04_09_15_38_52)
- `winhandler.exe` correctly finds and executes `ELDERBORN.exe` at `imagefs/gog_games/ELDERBORN/` — path fix verified working.
- Remaining "not found" was a container mismatch: first launch used arm64ec native Wine (xuser-2) which can't run x86 Unity games → `UnityCrashHandler` fires in 1 second. Second launch used Proton 9.0 x86_64 via box64 (xuser-1) and loaded DLLs cleanly. Not a code bug — user must select the x86/Proton container when adding the shortcut.

### Commits & Builds
| Commit | Tag | Description | CI Run | Result |
|---|---|---|---|---|
| `9922837` | v1.0.0-pre25 | fix: GOG shortcut launch — write Exec=wine Z:\\ path | [24207623589](https://github.com/The412Banner/Ludashi-plus/actions/runs/24207623589) | ✅ **success** |
| `a12cb04` | v1.0.0-pre25 | fix: install GOG games under imagefs/ — Z: maps imagefs not / | [24208299494](https://github.com/The412Banner/Ludashi-plus/actions/runs/24208299494) | ✅ **success** |


### pre1 fix — 2026-04-10 — Null-safe PICS + disconnect fix (`7e6b122`)
- `kvStr()` helper: all `asString()` calls in PICS handler are now null-safe (NPE was crashing sync)
- `SteamGamesActivity`: only `finish()` on LoggedOut, not Disconnected; sync kickstart on empty DB
- Type filter broadened (tool/hardware/music/video/advertising excluded; everything else shown)

### pre1 update — 2026-04-10 — Games filter + cover art (`b8099da`)
- Only type="game" shown in library list
- Status count from games.size (not sync event count)
- Portrait cover art per row (async, LruCache)
