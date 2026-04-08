# Ludashi-plus — Master Reverse Engineering Report
**Base APK:** Winlator Ludashi v2.9 bionic (`ludashi-bionic.apk`)  
**Source repo:** StevenMXZ/Winlator-Ludashi  
**Decompiled:** apktool 2.9.3 + jadx (4507 classes, 36 errors)  
**Date:** 2026-04-08 (38× verification passes — **CONVERGED** ✓ — 3 consecutive clean passes (Pass 36, 37, 38). Final counter=3. (Pass 26: 0 — FIRST CLEAN PASS, Pass 27: 11+, Pass 28: 4, Pass 29: 5, Pass 30: 2, Pass 31: 0 — counter=1, Pass 32: 1 — reset, Pass 33: 1 — reset, Pass 34: 1 — reset, Pass 35: 3 — reset, Pass 36: 0 — counter=1, Pass 37: 0 — counter=2, Pass 38: 0 — counter=3 ✓ CONVERGED). layout XML, all source packages, assets, manifest, Container/Shortcut/ImageFs/ContainerManager fields fully traced, storage paths, every menu file, env var pipeline fully documented, vkbasalt functional confirmed (was incorrectly dead-code), Box64/FEX preset names, xserver DEX distribution, UnixSocketConfig paths, xenvironment component DEX, AdrenotoolsManager meta.json, ContentProfile enum suite, MANGOHUD strip, HODLL, GUEST_PROGRAM_LAUNCHER_COMMAND, BOX64_ constants, launch command format x86_64 vs ARM64EC, Container .container config file and internal dirs, extraData hidden+cache keys, dead layouts, Termux:X11 orphaned XMLs, SharedPreferences defaults, BigPictureAdapter DEX fix (classes12 not 14), BigPicture playtime_stats prefs, music/animation prefs, cover art cache, EnvVars wire format, WineThemeManager constants+wallpaper BMP path, WineUtils DLL override list+dosdevices, FrameRating HUD element indices+GPU sysfs paths, all 8 nav menu XMLs verified, all sidebar button IDs verified, smali counts verified per-DEX, XrActivity 23 axes+19 buttons confirmed, VirGLRendererComponent dead-code confirmed+VIRGL_SERVER_PATH added, ARM64EC command slash corrected, launch component add order corrected, full WRAPPER_* env var batch added from smali, MESA_VK_WSI_DEBUG forcesync case added, launch pipeline background thread sequence corrected, GuestProgramLauncherComponent constructor args corrected, extraData 6 missing cache keys added, audio driver registry write documented)

---

## 1. App Identity

| Field | Value |
|---|---|
| Package | `com.ludashi.benchmark` |
| App Label | `Winlator CMOD` |
| Version Name | `7.1.4x-cmod` |
| Version Code | `20` |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 28 |
| Compile SDK | 34 |
| Debuggable | **true** |
| Main source namespace | `com.winlator.cmod` |

> Note: The package ID is `com.ludashi.benchmark` (Ludashi AI Benchmark disguise) but all actual app code is under `com.winlator.cmod`. This is StevenMXZ's Ludashi-spoofed fork of Winlator CMOD.

---

## 2. AndroidManifest Components

### Activities (6)

| Class | Role | Notes |
|---|---|---|
| `com.winlator.cmod.MainActivity` | **LAUNCHER** — main app UI | DrawerLayout nav, hosts all Fragments |
| `com.winlator.cmod.XServerDisplayActivity` | **Game display** — where games run | singleTask, sensorLandscape, PiP support |
| `com.winlator.cmod.BigPictureActivity` | Steam-like TV/couch UI | exported=false |
| `com.winlator.cmod.XrActivity` | VR/Oculus mode | `:vr_process`, OpenXR LAUNCHER |
| `com.winlator.cmod.ControlsEditorActivity` | Virtual input controls editor | sensorLandscape |
| `com.winlator.cmod.ExternalControllerBindingsActivity` | Physical controller button mapping | sensor |

### Receivers (1)

| Class | Intent Action |
|---|---|
| `com.winlator.cmod.ShortcutBroadcastReceiver` | `com.winlator.cmod.SHORTCUT_ADDED` |

### Providers (3)

| Class | Authority | Role |
|---|---|---|
| `androidx.core.content.FileProvider` | `com.ludashi.benchmark.tileprovider` | File sharing |
| `com.winlator.cmod.core.WinlatorFilesProvider` | `com.ludashi.benchmark.core.WinlatorFilesProvider` | Documents provider (exported, MANAGE_DOCUMENTS permission) |
| `androidx.startup.InitializationProvider` | `com.ludashi.benchmark.androidx-startup` | EmojiCompat + ProcessLifecycle |

### Hardware Features

```xml
<uses-feature android:glEsVersion="0x00020000" android:required="true"/>
<uses-feature android:name="android.hardware.vr.headtracking" android:required="false"/>
<uses-feature android:name="com.oculus.feature.PASSTHROUGH" android:required="false"/>
<uses-feature android:name="oculus.software.handtracking" android:required="false"/>
<uses-feature android:name="oculus.software.overlay_keyboard" android:required="false"/>
```

### Application Meta-data

```
com.samsung.android.multidisplay.keep_process_alive = false
android.allow_multiple_resumed_activities = true
```

### Permissions

```
INTERNET, ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE, VIBRATE,
WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, MODIFY_AUDIO_SETTINGS,
MANAGE_EXTERNAL_STORAGE, INSTALL_SHORTCUT, HIGH_SAMPLING_RATE_SENSORS,
POST_NOTIFICATIONS (declared twice — harmless duplicate), FOREGROUND_SERVICE,
WRITE_SECURE_SETTINGS
```

### `res/xml/` Files

| File | Purpose |
|---|---|
| `file_paths.xml` | FileProvider paths — `<external-path name="external_files" path="." />` (external storage root) |
| `preferences.xml` | **Orphaned** — **identical to `preferences_x11.xml`**. Both files contain the exact same 50+ Termux:X11 preference keys. Winlator's `SettingsFragment` defines all 20 global settings **programmatically** in Java (via `getDefaultSharedPreferences()`), not via any XML preference file. This file is a dead duplicate. |
| `preferences_x11.xml` | **Orphaned** — 50+ Termux:X11 preference keys (output: displayResolutionMode/displayScale/displayStretch/fullscreen/etc.; pointer: touchMode/scaleTouchpad/stylusIsMouse/pointerCapture/etc.; kbd: showAdditionalKbd/preferScancodes/etc.; other: clipboardEnable/etc.). `preferences.xml` is a byte-for-byte duplicate of this file. No Winlator Java code uses either. Remnant of Termux:X11 import. |
| `shortcuts.xml` | **Orphaned** — Launcher shortcut pointing at `com.termux.x11.LoriePreferences` (another package). Dead artifact. |
| `accessibility_service_config.xml` | **Orphaned** — Accessibility service config with `android:settingsActivity=".LoriePreferences"`. No such class in this app. Dead artifact. |
| `standalone_badge*.xml` | MaterialComponents library badge layout variants (5 files) — not app-specific |

---

## 3. DEX Map (16 DEX files)

| DEX | Size | Smali Files | Contents |
|---|---|---|---|
| `classes.dex` | 11.1 MB | 7,021 | android stubs, androidx, cn (sherlock/javax/media), com.bumptech.glide, com.github.luben.zstd, com.google, jp.kshoji (MIDI), kotlin, kotlinx, okhttp3, okio |
| `classes2.dex` | 491 KB | 295 | androidx (cont.), com.bumptech.glide (cont.), **com.winlator.cmod R$\* resource classes** (R, R$anim/array/attr/color/dimen/drawable/id/integer/interpolator/layout/menu/mipmap/string/style/styleable/xml — 17 total) |
| `classes3.dex` | 18 KB | 8 | `com.winlator.cmod.fexcore` |
| `classes4.dex` | 134 KB | 104 | `com.winlator.cmod.contentdialog`, `com.winlator.cmod.xserver` (partial) |
| `classes5.dex` | 37 KB | 10 | `com.winlator.cmod.midi` |
| `classes6.dex` | 37 KB | 35 | `com.winlator.cmod.box64`, `com.winlator.cmod.xserver` (partial) |
| `classes7.dex` | 8 KB | 5 | `com.winlator.cmod.alsaserver` |
| `classes8.dex` | 6.1 MB | **322** | **`com.winlator.cmod` root** — all main Activity/Fragment classes + D8 synthetic `-IA` stubs |
| `classes9.dex` | 31 KB | 22 | `com.winlator.cmod.renderer.effects` — all 10 effect classes + inner Material classes + `-IA` stubs (CRTEffect, ColorEffect, Effect, FSREffect, FXAAEffect, FrameGenerationEffect, HDREffect, NTSCCombinedEffect, NaturalEffect, ToonEffect) |
| `classes10.dex` | 171 KB | 80 | `com.winlator.cmod.core`, `com.winlator.cmod.xenvironment/components/` (all 6 component classes) |
| `classes11.dex` | 141 KB | 89 | `com.winlator.cmod.math`, `com.winlator.cmod.sysvshm`, `com.winlator.cmod.xserver` (core + managers + data objects + extensions) |
| `classes12.dex` | 87 KB | 58 | `com.winlator.cmod.bigpicture` (BigPictureAdapter/CarouselItemDecoration/TiledBackgroundView), `com.winlator.cmod.renderer` (GLRenderer/EffectComposer/core), `com.winlator.cmod.winhandler`, `com.winlator.cmod.xenvironment` (base: XEnvironment/EnvironmentComponent/ImageFs/ImageFsInstaller) |
| `classes13.dex` | 21 KB | 27 | `com.winlator.cmod.xconnector`, `com.winlator.cmod.xserver` (partial) |
| `classes14.dex` | 191 KB | 82 | `com.winlator.cmod.bigpicture.steamgrid` (SteamGridDBApi/SteamGridGridsResponse/SteamGridGridsResponseDeserializer/SteamGridSearchResponse), `com.winlator.cmod.container`, `com.winlator.cmod.inputcontrols`, `com.winlator.cmod.widget` |
| `classes15.dex` | 32 KB | 14 | `com.winlator.cmod.contents`, `com.winlator.cmod.renderer` (partial) |
| `classes16.dex` | 6.1 MB | 5,474 | `org.*` (Apache Commons, BouncyCastle, Conscrypt, intellij/jetbrains annotations, newsclub AFUNIXSocket, OpenJSSE, tukaani XZ), `retrofit2.*` — **⚠️ likely at DEX method limit** |

**Total smali files: 13,646** (7021+295+8+104+10+35+5+322+22+80+89+58+27+82+14+5474)

> **classes2 correction:** The 17 "root classes" in classes2 are all `R$*` resource identifier classes (generated), not runtime logic classes. The actual runtime root classes for `com.winlator.cmod` live in `classes8`.

> **classes8 `-IA` files:** D8 compiler generates `ClassName-IA.smali` as synthetic interface adapter stubs. Not real classes — do not patch these.

> **classes16 `org.newsclub`:** This is `org.newsclub.net.unix` — AFUNIXSocket library providing Unix domain socket support in pure Java. Used by `XConnector` for Wine→Android IPC on older Android versions.

---

## 4. Source Package Map (`com.winlator.cmod`)

### Root Package (classes8)
All main Activities and Fragments. **Primary patch target.**

| Class | DEX | Role |
|---|---|---|
| `MainActivity` | classes8 | Main app shell, DrawerLayout + NavigationView |
| `XServerDisplayActivity` | classes8 | **Game window** — XServer + Wine display + sidebar |
| `BigPictureActivity` | classes8 | Steam Big Picture TV mode |
| `XrActivity` | classes8 | VR/Oculus display — extends XServerDisplayActivity; OpenXR JNI bridge with 8 native methods (`beginFrame`, `bindFramebuffer`, `endFrame`, `getAxes`, `getButtons`, `getWidth`, `getHeight`, `init`); static state: `isImmersive`, `isSBS`; static helpers: `isEnabled(context)`, `isSupported()`, `getInstance()`, `getSBS()`, `getImmersive()`, `updateControllers()`; supports 23 controller axes (L/R/HMD) and 19 controller buttons |
| `ControlsEditorActivity` | classes8 | Input controls layout editor |
| `ExternalControllerBindingsActivity` | classes8 | Physical gamepad config |
| `AdrenotoolsFragment` | classes8 | Adreno GPU driver management UI |
| `ContainerDetailFragment` | classes8 | Container settings detail UI |
| `ContainersFragment` | classes8 | Container list (main screen) |
| `ContentsFragment` | classes8 | Content/component list UI |
| `FileManagerFragment` | classes8 | Built-in file manager |
| `InputControlsFragment` | classes8 | Virtual controls list |
| `SettingsFragment` | classes8 | App global settings |
| `ShortcutsFragment` | classes8 | Game shortcuts list |
| `ShortcutBroadcastReceiver` | classes8 | Shortcut install receiver |

### `alsaserver` (classes7)
ALSA audio server Unix socket protocol.

| Class | Role |
|---|---|
| `ALSAClient` | ALSA client connection |
| `ALSAClientConnectionHandler` | Handles client socket |
| `ALSARequestHandler` | Processes ALSA requests |
| `RequestCodes` | ALSA op codes |

### `bigpicture` (classes12 + classes14)
Big Picture / Steam Grid TV UI.

| Class | Role |
|---|---|
| `BigPictureAdapter` | RecyclerView adapter for game carousel |
| `CarouselItemDecoration` | Carousel scroll decoration |
| `TiledBackgroundView` | Tiled background view |
| `steamgrid/SteamGridDBApi` | SteamGridDB cover art API (BASE_URL = `https://www.steamgriddb.com/api/v2/`) |
| `steamgrid/SteamGridGridsResponse` | Grid images response model |
| `steamgrid/SteamGridGridsResponseDeserializer` | JSON deserializer |
| `steamgrid/SteamGridSearchResponse` | Search response model |

**BigPictureActivity key fields:**
- Hardcoded default API key: `0324c52513634547a7b32d6d323635d0` — overridden by `custom_api_key` pref if `enable_custom_api_key=true`
- Supports: custom wallpaper (image/solid color via RadioGroup), custom background music (MP3/YouTube), custom cover art upload (PNG), game info panel (audio driver, DX wrapper, graphics driver, box64 preset, play count, playtime), YouTube WebView embedding
- Uses OkHttp3 + Retrofit2 + Gson for SteamGridDB API calls
- **SharedPreferences `"playtime_stats"`**: `{shortcut.name}_playtime` (long, ms), `{shortcut.name}_play_count` (int)
- **Cover art cache**: `getCacheDir()/coverArtCache/{shortcut.name}.png`; custom override stored in `shortcut.getCustomCoverArtPath()` (absolute path)
- **Background animation options** (`selected_animation`): `"ab"` (default), `"ab_gear"`, `"ab_quilt"`, `"folder"` (user PNG dir), `"none"`
- **Parallax modes** (`parallax_mode`): `"default"`, `"fast"`, `"slow"`, `"off"`
- **Music source** (`music_source`): `"mp3"` or `"youtube"`; `"bg_music_enabled"` (boolean, default true); `"saved_youtube_url"` (String); `"selected_mp3_path"` (String)
- **Default YouTube video ID**: `"yNwKYgM6SkM"` (used when no saved URL)
- **runFromShortcut** → starts `XServerDisplayActivity` with extras: `container_id`, `shortcut_path`, `shortcut_name`, `disableXinput` (String, from shortcut extra "disableXinput", default "0") — **note:** BigPicture does NOT include `native_rendering` in Intent (always defaults to `false`); BigPicture also does NOT check `XrActivity.isEnabled()` (always routes to XServerDisplayActivity); ShortcutsFragment includes `native_rendering` (`shortcut.getNativeRendering()`) and checks `XrActivity.isEnabled()` first. ⚠️ **XServerDisplayActivity only reads `container_id`, `shortcut_path`, and `native_rendering` from Intent extras** — `shortcut_name` is NEVER read (possibly used by Android OS for task display only); `disableXinput` is NEVER read from Intent (XServerDisplayActivity reads it directly from the Shortcut file at `this.shortcut.getExtra("disableXinput", "false")` — the Intent extra is redundant).
- **Typo in code**: shortcut extra read as `"graphicsDroverConfig"` (not `"graphicsDriverConfig"`) on line 535 of BigPictureActivity — this is a bug in the source, the field should map to graphicsDriverConfig

### `box64` (classes6)
Box64 x86_64 CPU emulator preset management.

| Class | Role |
|---|---|
| `Box64EditPresetDialog` | Edit Box64 preset dialog |
| `Box64Preset` | Preset data model |
| `Box64PresetManager` | Load/save Box64 presets |

### `container` (classes14)
Wine container data model and management.

| Class | Role |
|---|---|
| `Container` | **Core data model** — all per-container settings |
| `ContainerManager` | Create/load/save containers (JSON in imagefs/home/) |
| `Shortcut` | Per-game shortcut with extra overrides |

**Container storage:** Each container lives at `imagefs/home/xuser-{id}/` inside the imagefs rootfs (internal storage), loaded by `ContainerManager` which reads `imagefs/home/` and looks for `xuser-*` directories.

**Container internal paths:**
- Config file: `xuser-{id}/.container` (JSON) — serialized by `Container.saveData()`
- Shortcuts/desktop files: `xuser-{id}/.wine/drive_c/users/xuser/Desktop/*.desktop` (and `.lnk` for Windows shortcuts)
- Start menu: `xuser-{id}/.wine/drive_c/ProgramData/Microsoft/Windows/Start Menu/`
- Icons: `xuser-{id}/.local/share/icons/hicolor/{size}x{size}/apps/`
- Cover art: `xuser-{id}/app_data/cover_arts/{name}.png`

**Container constants:**
- `DEFAULT_SCREEN_SIZE = "1280x720"`
- `DEFAULT_GRAPHICS_DRIVER = "wrapper"`
- `DEFAULT_DXWRAPPER = "dxvk+vkd3d"`
- `DEFAULT_DDRAWRAPPER = "none"`
- `DEFAULT_WINCOMPONENTS = "direct3d=1,directsound=0,directmusic=0,directshow=0,directplay=0,xaudio=0,vcrun2010=1"`
- `FALLBACK_WINCOMPONENTS = "direct3d=1,directsound=1,directmusic=1,directshow=1,directplay=1,xaudio=1,vcrun2010=1"`
- `DEFAULT_AUDIO_DRIVER = "alsa"`
- `DEFAULT_EMULATOR = "FEXCore"`
- `DEFAULT_ENV_VARS` = `"WRAPPER_MAX_IMAGE_COUNT=0 VKD3D_SHADER_MODEL=6_6 ZINK_DESCRIPTORS=lazy ZINK_DEBUG=compact MESA_SHADER_CACHE_DISABLE=false MESA_SHADER_CACHE_MAX_SIZE=512MB mesa_glthread=true WINEESYNC=1 TU_DEBUG=noconform,sysmem DXVK_HUD=0"`
- `DEFAULT_GRAPHICSDRIVERCONFIG` = `"vulkanVersion=1.3;version=;blacklistedExtensions=;maxDeviceMemory=0;presentMode=mailbox;syncFrame=0;disablePresentWait=0;resourceType=auto;bcnEmulation=auto;bcnEmulationType=compute;bcnEmulationCache=0;gpuName=Device"` — field meanings: `version` = **adrenotools driver ID** (empty = use System driver; smali 2043-2052 confirms `graphicsDriverConfig.get("version")` → `adrenoToolsDriverId`); `vulkanVersion` = major.minor for WRAPPER_VK_VERSION base; `gpuName` = GPU display name for WRAPPER_DEVICE_NAME/ID/VENDOR_ID; all others: WRAPPER_* env vars as documented above
- `DEFAULT_DXWRAPPERCONFIG` = `"version={DXVK},framerate=0,async=0,asyncCache=0,vkd3dVersion=None,vkd3dLevel=12_1,ddrawrapper=none,csmt=3,gpuName=NVIDIA GeForce GTX 480,videoMemorySize=2048,strict_shader_math=1,OffscreenRenderingMode=fbo,renderer=gl"` — ⚠️ this constant exists but **constructors set `dxwrapperConfig = ""` (empty string)**; the constant is used for resets, not initial construction
- `DEFAULT_DRIVES = "F:{ExternalStoragePath}D:{DownloadsPath}"`
- `STARTUP_SELECTION_NORMAL=0`, `STARTUP_SELECTION_ESSENTIAL=1`, `STARTUP_SELECTION_AGGRESSIVE=2`

**Key Container fields (all private, accessed via getters/setters):**
- `id` (int, public final) — container ID
- `name` (String, default: `"Container-{id}"`)
- `screenSize` (init: `DEFAULT_SCREEN_SIZE="1280x720"`), `graphicsDriver` (init: `DEFAULT_GRAPHICS_DRIVER="wrapper"`), `graphicsDriverConfig` (init: `DEFAULT_GRAPHICSDRIVERCONFIG`), `dxwrapper` (init: `DEFAULT_DXWRAPPER="dxvk+vkd3d"`), `dxwrapperConfig` (init: `""`)
- `wincomponents` (init: `DEFAULT_WINCOMPONENTS`), `audioDriver` (init: `DEFAULT_AUDIO_DRIVER="alsa"`), `emulator` (init: **null** — DEFAULT_EMULATOR defined but unused in constructor; set via setter or JSON load), `envVars` (init: `DEFAULT_ENV_VARS`), `drives` (init: `DEFAULT_DRIVES`)
- `wineVersion` (init: `WineInfo.MAIN_WINE_VERSION.identifier()` = `"proton-9.0-x86_64"`)
- `box64Version`, `box64Preset` (init: set from JSON on load; default `"0.4.1"` / `"COMPATIBILITY"`)
- `fexcoreVersion`, `fexcorePreset` (init: set from JSON on load; default `"2601"` / `"INTERMEDIATE"`)
- `desktopTheme` (init: `WineThemeManager.DEFAULT_DESKTOP_THEME = "LIGHT,IMAGE,#0277bd"`), `lc_all` (init: `""`), `midiSoundFont` (init: `""`)
- `cpuList`, `cpuListWoW64` — CPU affinity lists
- `inputType` (int, init: `4` = `FLAG_INPUT_TYPE_XINPUT`), `primaryController` (int, init: `1`=Right), `controllerMapping` (String, init: zeroed char array), `exclusiveXInput` (boolean, init: `true`)
- `fullscreenStretched` (boolean), `showFPS` (boolean), `startupSelection` (byte, init: `1` = `STARTUP_SELECTION_ESSENTIAL` — **default is Essential, not Normal**)
- `extraData` (JSONObject) — per-container arbitrary extras; known keys:
  - `appVersion` (int string) — **APK version code** (`AppUtils.getVersionCode()` = `20` currently); if < 16 on load, missing DEFAULT_ENV_VARS keys are backfilled into envVars; combined with imgVersion to detect app/image upgrades → trigger `applyGeneralPatches()`; also detects first boot (`appVersion.isEmpty()`)
  - `imgVersion` (string) — **ImageFS version integer** (`imageFs.getVersion()`); combined with appVersion to detect changes; also legacy: read by ImageFsInstaller at install time (checks ≤ 5 → set wineprefixNeedsUpdate), then cleared (set null) after use
  - `wineprefixNeedsUpdate` (string `"t"`) — set by ImageFsInstaller when imgVersion ≤ 5; triggers wineprefix rebuild
  - `box64Version` (string) — last installed box64/wowbox64 version; compared before each launch to avoid re-extracting
  - `fexcoreVersion` (string) — last installed FEXCore version; same cache-check mechanism
  - `dxwrapper` (string) — **change-detection cache**: last extracted DXWrapper combo string (`"dxvk-{ver};vkd3d-{ver};{ddrawrapper}"`); `setupWineSystemFiles()` compares and re-runs `extractDXWrapperFiles()` if changed
  - `wincomponents` (string) — **change-detection cache**: last extracted wincomponents string; `setupWineSystemFiles()` re-runs `extractWinComponentFiles()` if changed
  - `desktopTheme` (string) — **change-detection cache**: last applied desktop theme + screen info (`"{desktopTheme},{screenInfo}"`); `setupWineSystemFiles()` re-applies `WineThemeManager.apply()` if changed; cleared to null by reset method to force reapplication
  - `startupSelection` (string) — **change-detection cache**: last applied startup selection byte; `setupWineSystemFiles()` re-runs `changeServicesStatus()` if changed
  - `audioDriver` (string) — **change-detection cache**: last applied audio driver; `changeWineAudioDriver()` writes Wine registry `Software\\Wine\\Drivers\\Audio` = `"alsa"` (ALSA) or `"pulse"` (PulseAudio — note: registry value is `"pulse"`, not `"pulseaudio"`) when this changes
  - `graphicsDriver` (string) — **change-detection cache**: last applied graphics driver; cleared to null by reset method to force driver reapplication

**Shortcut fields (all from `Shortcut.java`):**
- `container` (Container, final) — owning container
- `file` (File, final) — `.desktop` file on disk
- `name` (String, final) — display name
- `path` (String, final) — exe path
- `wmClass` (String, final) — WM class (window title matcher)
- `icon` (Bitmap) — icon image
- `iconFile` (File) — icon file on disk
- `coverArt` (Bitmap) — cover art image (lazy-loaded from `customCoverArtPath` or default)
- `customCoverArtPath` (String) — custom cover art path (first-class field)
- `extraData` (JSONObject) — all other extras

**Shortcut file format** (`.desktop` text file in container's desktop dir):
```
[Desktop Entry]
Name=...
Exec=...
Icon=...
...

[Extra Data]
key=value
...
```

**Hidden extras** (stored in `[Extra Data]` but not in ShortcutSettingsDialog):
- `uuid` — auto-generated UUID for the shortcut
- `customCoverArtPath` — custom cover art path (also has its own field)

**Shortcut extras (per-game overrides, stored in `[Extra Data]`, 28 ShortcutSettingsDialog keys):**
`execArgs`, `screenSize`, `graphicsDriver`, `graphicsDriverConfig`, `dxwrapper`, `dxwrapperConfig`, `audioDriver`, `emulator`, `midiSoundFont`, `lc_all`, `box64Version`, `box64Preset`, `fexcoreVersion`, `fexcorePreset`, `fullscreenStretched`, `inputType`, `exclusiveXInput`, `disableXinput`, `simTouchScreen`, `nativeRendering`, `controlsProfile`, `wincomponents`, `envVars`, `startupSelection`, `sharpnessEffect`, `sharpnessLevel`, `sharpnessDenoise`, `cpuList`
> Note: `customCoverArtPath` is a hidden extra (set via `Shortcut.setCustomCoverArtPath()`, not via ShortcutSettingsDialog) — see "Hidden extras" above.

### `contentdialog` (classes4)
All content/settings dialogs shown on the game card.

| Class | Role |
|---|---|
| `ContentDialog` | Base dialog class |
| `ContentInfoDialog` | Show content info |
| `ContentUntrustedDialog` | Untrusted source warning |
| `DXVKConfigDialog` | DXVK settings |
| `DebugDialog` | Debug log viewer |
| `DriverDownloadDialog` | GPU driver download UI |
| `DriverRepo` | Driver repository model |
| `GraphicsDriverConfigDialog` | Graphics driver settings |
| `RepositoryManagerDialog` | Content repo manager |
| `ShortcutSettingsDialog` | **Per-shortcut settings** (all per-game overrides) |
| `StorageInfoDialog` | Storage usage info |
| `WineD3DConfigDialog` | WineD3D settings |
| `AddEnvVarDialog` | Add environment variable |

### `contents` (classes15)
Content/component download and install system.

| Class | Role |
|---|---|
| `ContentsManager` | **Manages all downloadable content** |
| `ContentProfile` | Content metadata (type, version, files) |
| `AdrenotoolsManager` | Custom Adreno driver management — stores drivers at `getFilesDir()/contents/adrenotools/{driverId}/` |
| `Downloader` | File downloader |

**ContentProfile fields:** `desc`, `fileList` (List<ContentFile>), `remoteUrl`, `type` (ContentType), `verCode` (int), `verName` (String), `wineBinPath`, `wineLibPath`, `winePrefixPack` (latter three: Wine/Proton only)

**ContentProfile.ContentFile inner class:** `source` (String), `target` (String) — relative source path in archive → absolute target path in imagefs

**ContentProfile.ContentType enum:**
`CONTENT_TYPE_WINE("Wine")`, `CONTENT_TYPE_PROTON("Proton")`, `CONTENT_TYPE_DXVK("DXVK")`, `CONTENT_TYPE_VKD3D("VKD3D")`, `CONTENT_TYPE_BOX64("Box64")`, `CONTENT_TYPE_WOWBOX64("WOWBox64")`, `CONTENT_TYPE_FEXCORE("FEXCore")`

**ContentsManager.InstallFailedReason enum:**
`ERROR_NOSPACE`, `ERROR_BADTAR`, `ERROR_NOPROFILE`, `ERROR_BADPROFILE`, `ERROR_MISSINGFILES`, `ERROR_EXIST`, `ERROR_UNTRUSTPROFILE`, `ERROR_UNKNOWN`

**ContentsManager.ContentDirName enum:**
`CONTENT_MAIN_DIR_NAME("contents")`, `CONTENT_WINE_DIR_NAME("wine")`, `CONTENT_DXVK_DIR_NAME("dxvk")`, `CONTENT_VKD3D_DIR_NAME("vkd3d")`, `CONTENT_BOX64_DIR_NAME("box64")`

**ContentsManager trust file arrays:**
- `DXVK_TRUST_FILES` — 14 paths: `${system32}/d3d{8..11}.dll`, `${system32}/dxgi.dll` + syswow64 equivalents
- `VKD3D_TRUST_FILES` — 4 paths: `${system32}/d3d12{core,}.dll` + syswow64
- `BOX64_TRUST_FILES` — `${bindir}/box64`
- `WOWBOX64_TRUST_FILES` — `${system32}/wowbox64.dll`
- `FEXCORE_TRUST_FILES` — `${system32}/libwow64fex.dll`, `${system32}/libarm64ecfex.dll`

**AdrenotoolsManager — `meta.json` fields:** `libraryName` (String — .so filename), `name` (String — display name), `driverVersion` (String)
- Install from APK assets: `extractDriverFromResources(id)` → extracts `graphics_driver/adrenotools-{id}.tzst` → `contents/adrenotools/{id}/`
- Install from file URI: `installDriver(Uri)` → unzips to `contents/adrenotools/{name}/` (requires `meta.json` in zip root)
- `enumerateInstalledDrivers()` — lists dirs with `meta.json` that are NOT from APK resources (user-installed only)
- **Driver download pipeline:** `RepositoryManagerDialog` (pref `custom_driver_repos`) → user taps repo → `DriverDownloadDialog(context, repoApiUrl)` → fetches GitHub releases JSON → lists `.zip`/`.tzst` assets → `Downloader.downloadFile()` → `cacheDir/driver_temp.zip` → `AdrenotoolsManager.installDriver(Uri)` → unpacks to `contents/adrenotools/{name}/`

**Remote contents URL:** `https://raw.githubusercontent.com/StevenMXZ/Winlator-Contents/main/contents.json`

### `core` (classes10)
General utilities — the toolbox of the app.

| Class | Role |
|---|---|
| `AppUtils` | App-level helpers |
| `ArrayUtils` | Array manipulation |
| `CPUStatus` | CPU freq/info reader |
| `Callback<T>` | Generic callback interface |
| `CubicBezierInterpolator` | Bezier animation interpolator |
| `CursorLocker` | Cursor capture/lock helper |
| `DefaultVersion` | Default versions for all components |
| `DownloadProgressDialog` | Download progress dialog |
| `ElfHelper` | ELF binary inspection |
| `EnvVars` | Environment variable parser/builder — backed by `LinkedHashMap<String,String>`; wire format is **space-separated `KEY=VALUE` pairs** (split on `" "`); `toEscapedString()` escapes spaces in values as `\\ `; `get(name)` returns `""` (not null) if missing |
| `EnvironmentManager` | Static `HashMap<String,String>` env snapshot — `setEnvVars(String[] envp)` parses `KEY=VALUE` array into map; `getEnvVars()` returns copy; used by `ProcessHelper` to snapshot env before exec and propagate to child process |
| `FileUtils` | File I/O helpers |
| `GPUInformation` | GPU vendor/renderer detection |
| `GameImageFetcher` | Fetch game artwork |
| `HttpUtils` | HTTP request helpers |
| `ImageUtils` | Bitmap/image helpers |
| `KeyValueSet` | Key=value string parser |
| `MSBitmap` / `MSLink` / `MSLogFont` | Windows format parsers (LNK files, etc.) |
| `NetworkHelper` | Network state checks |
| `OnExtractFileListener` | Callback interface for tar extraction progress |
| `PatchElf` | ELF patcher (fix library rpath/soname) |
| `PreloaderDialog` | Loading spinner dialog |
| `ProcessHelper` | Fork/exec native processes via libwinlator.so |
| `StreamUtils` | Stream I/O helpers |
| `StringUtils` | String manipulation |
| `TarCompressorUtils` | tar.zst / tar.xz extraction |
| `UnitUtils` | Unit conversion |
| `VKD3DVersionItem` | VKD3D version data model |
| `WineInfo` | Wine version info — **implements Parcelable**, passed between Activities via Intent |
| `WineRegistryEditor` | Read/write Wine registry |
| `WineRequestHandler` | Wine IPC requests |
| `WineStartMenuCreator` | Create Wine start menu entries |
| `WineThemeManager` | Wine desktop theme — `DEFAULT_DESKTOP_THEME = "LIGHT,IMAGE,#0277bd"`; ThemeInfo parses `"{Theme},{BackgroundType},{#rrggbb}"` (or legacy `"{Theme},{#rrggbb}"` where BackgroundType defaults to IMAGE); applies to `Control Panel\Colors` + `Control Panel\Desktop` registry; wallpaper BMP written to `/home/xuser/.cache/wallpaper.bmp` when BackgroundType=IMAGE |
| `WineUtils` | Wine setup helpers — `createDosdevicesSymlinks()`: `c:` → `../drive_c`, `z:` → imagefs/home root (`.wine/dosdevices/z:` → two levels up); `applySystemTweaks()`: sets DLL overrides `"native,builtin"` in user.reg for: d3d8, d3d9, d3d10, d3d10_1, d3d10core, d3d11, d3d12, d3d12core, ddraw, dxgi, wined3d; sets file-type icons in system.reg; `overrideWinComponentDlls()`: reads `wincomponents.json` to get DLL names for identifier, sets "native,builtin" or removes from DllOverrides |
| `WinlatorFilesProvider` | Android Documents Provider |

**Key defaults (DefaultVersion class constants — `com.winlator.cmod.core.DefaultVersion`):**
| Constant | Value | Notes |
|---|---|---|
| `BOX64` | `"0.4.1"` | Box64 version |
| `WOWBOX64` | `"0.4.1"` | WoWBox64 version |
| `DXVK` | `"2.3.1"` (Adreno) / `"1.10.3"` (Mali) | Runtime check: `GPUInformation.getRenderer(null, null).contains("Mali")` |
| `VKD3D` | `"None"` | Also used as sharpnessEffect "disabled" sentinel |
| `FEXCORE` | `"2601"` | FEXCore version |
| `D8VK` | `"1.0"` | D8VK version — bundled asset; legacy-only (not exposed in current `dxwrapper_entries` UI which only has WineD3D/DXVK+VKD3D); migration code keeps existing `"d8vk-{ver}"` configs as-is |
| `WRAPPER` | `"System"` | Default graphics driver mode sentinel — "no custom driver"; used as the `version=` field value in `graphicsDriverConfig` when falling back to system driver (when Adreno driver not supported; see WRAPPER_ADRENO) |
| `WRAPPER_ADRENO` | `"turnip26.0.0"` | Default Adreno GPU driver variant — used when `GPUInformation.isDriverSupported("turnip26.0.0", context)` returns true; otherwise falls back to `WRAPPER="System"`. Set as `version=` in graphicsDriverConfig by `AdrenotoolsManager`. |

> ⚠️ The previous report note claiming D8VK and WRAPPER are "NOT DefaultVersion class constants" was **incorrect** — all 8 entries above are real `DefaultVersion` class static fields. `WRAPPER = "System"` and `WRAPPER_ADRENO = "turnip26.0.0"` are the two driver sentinel values (the `graphicsDriverConfig.get("version")` field stores either `"System"` or `"turnip26.0.0"` or a user-picked driver ID). Note: `DEFAULT_GRAPHICSDRIVERCONFIG` initializes `version=` as **empty string** (not "System") — the "System" value is set by `AdrenotoolsManager` at runtime when needed.

**Main Wine version:** `WineInfo.MAIN_WINE_VERSION = new WineInfo("proton", "9.0", "x86_64")`  
Identifier format: `proton-9.0-x86_64` (or `wine-X.Y-arch`)

### `fexcore` (classes3)
FEX CPU emulator (ARM64EC) preset management. Mirrors box64 in structure.

| Class | Role |
|---|---|
| `FEXCoreEditPresetDialog` | Edit FEX preset |
| `FEXCoreManager` | FEX file management |
| `FEXCorePreset` | Preset data model |
| `FEXCorePresetManager` | Load/save presets |

### `inputcontrols` (classes14)
On-screen virtual gamepad/touch controls.

| Class | Role |
|---|---|
| `Binding` | Button → action mapping |
| `ControlElement` | Single control (button/joystick/etc.) |
| `ControlsProfile` | Full controls layout |
| `ExternalController` | Physical gamepad handler |
| `ExternalControllerBinding` | Physical button binding |
| `FakeInputWriter` | Writes fake input events |
| `GamepadState` | Current gamepad state |
| `InputControlsManager` | Manages all profiles |
| `RangeScroller` | Scroll range helper |

### `math` (classes11)
Math utilities.

| Class | Role |
|---|---|
| `Mathf` | Float math helpers |
| `XForm` | 2D transform matrix |

### `midi` (classes5)
MIDI/soundfont playback.

| Class | Role |
|---|---|
| `MidiHandler` | MIDI event handler |
| `MidiManager` | MIDI device management |
| `RequestCodes` | MIDI op codes |

### `renderer` (classes9 + classes12 + classes15)
OpenGL ES rendering pipeline — renders the X server framebuffer to the Android display.

| Class | Role |
|---|---|
| `GLRenderer` | **Main OpenGL renderer** — `setFpsLimit(int)`, holds `EffectComposer`, implements `WindowManager.OnWindowModificationListener` + `Pointer.OnPointerMotionListener`. Direct rendering: detects single fullscreen window and renders it directly (skips EffectComposer) |
| `EffectComposer` | Chains post-process effects — `addEffect(Effect)`, `removeEffect(Effect)`, `getEffect(Class<T>)` |
| `GPUImage` | GPU-backed image buffer |
| `NativeRenderer` | JNI native rendering bridge |
| `RenderTarget` | FBO render target |
| `RenderableWindow` | X11 window → GL surface |
| `Texture` | OpenGL texture wrapper |
| `VertexAttribute` | VAO attribute |
| `ViewTransformation` | Screen transform (zoom/pan) |

**Effects (renderer/effects/):**

| Effect Class | DEX | Purpose | Notes |
|---|---|---|---|
| `Effect` | classes9 | **Abstract base class** for all effects | Extended by all effect classes below |
| `CRTEffect` | classes9 | CRT scanline filter | Enabled via SPColorMode=3 |
| `ColorEffect` | classes9 | Brightness/contrast/gamma | **Not exposed in sidebar UI** — layout `screen_effect_dialog.xml` exists but no code inflates it. Dead/pending feature. |
| `FSREffect` | classes9 | AMD FSR upscaling | Enabled via SWEnableFSR; modes: Super Resolution(0)/DLS(1) via SPUpscalerMode; level via SBSharpness |
| `FXAAEffect` | classes9 | Fast anti-aliasing | — |
| `FrameGenerationEffect` | classes9 | Frame interpolation | Fully implemented; sidebar not wired (see Dead UI); EffectComposer limits to 1 instance |
| `HDREffect` | classes9 | HDR tone mapping | Enabled via SPColorMode=1 |
| `NTSCCombinedEffect` | classes9 | NTSC composite simulation | — |
| `NaturalEffect` | classes9 | Natural color filter | Enabled via SPColorMode=2 |
| `ToonEffect` | classes9 | Cel-shading filter | — |

**Materials (renderer/material/) — classes15.dex:**
`CursorMaterial`, `ScreenMaterial`, `ShaderMaterial`, `WindowMaterial`

### `sysvshm` (classes11)
System V shared memory IPC (between Wine and X server).

| Class | Role |
|---|---|
| `SysVSharedMemory` | Shared memory segment |
| `SysVSHMConnectionHandler` | Handles SHM connections |
| `SysVSHMRequestHandler` | Processes SHM requests |
| `RequestCodes` | SHM op codes |

### `widget` (classes14)
Custom UI widgets.

| Class | Role |
|---|---|
| `CPUListView` | CPU core list selector |
| `ColorPickerView` | Color picker widget |
| `EnvVarsView` | Env var list editor |
| `FrameRating` | **FPS/HUD overlay** (classes14) — draggable, 6 toggle elements: 0=FPS, 1=Renderer, 2=GPU%, 3=CPU+RAM, 4=Batt+Temp, 5=Frametime graph; stats update every 1000ms via HandlerThread; FPS update every 500ms. GPU sysfs paths tried in order: `/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage`, `/sys/class/kgsl/kgsl-3d0/devfreq/gpu_load`, `/sys/class/misc/mali0/device/utilisation`, `/sys/class/kgsl/kgsl-3d0/gpubusy`. Renderer detection: "DXVK"→"DXVK", "Turnip"→"Turnip", "VirGL"→"VirGL", "llvmpipe"→"Software". Frametime graph: 60 samples max, capped at 66.6ms, redrawn every 50ms. ⚠️ **Source bug**: `updateSidebarHud()` calls `toggleElement(6, cbRenderer.isChecked())` — index 6 is out of range (valid: 0-5); `toggleElement()` switch has no `case 6:` so it does nothing. The Renderer checkbox (`CBHudRenderer`) is effectively broken — Renderer display (element 1) cannot be toggled via sidebar and remains permanently enabled. Fix: call `toggleElement(1, ...)` instead. |
| `ImagePickerView` | Image selector |
| `InputControlsView` | Renders virtual controls overlay |
| `LogView` | Scrollable log output |
| `MagnifierView` | Screen magnifier |
| `MultiSelectionComboBox` | Multi-select dropdown |
| `NumberPicker` | Number input |
| `SeekBar` | Custom seekbar (used throughout) |
| `TouchpadView` | Touchpad input area (four-finger tap → sidebar) |
| `XServerView` | **X server SurfaceView** — the actual game display surface |

### `winhandler` (classes12)
Windows process/window management via native IPC.

| Class | Role |
|---|---|
| `WinHandler` | Communicates with Windows processes — `SERVER_PORT=7947`, `CLIENT_PORT=7946`, `MAX_CONTROLLERS=4`; input type flags: `FLAG_INPUT_TYPE_XINPUT=4`, `FLAG_INPUT_TYPE_DINPUT=8`, `DEFAULT_INPUT_TYPE=4` |
| `TaskManagerDialog` | Shows running Windows processes |
| `ProcessInfo` | Process data model |
| `MouseEventFlags` | Mouse event constants |
| `RequestCodes` | WinHandler op codes |
| `OnGetProcessInfoListener` | Callback for process list |

### `xconnector` (classes13)
Unix socket communication layer (Android ↔ Wine IPC).  
Uses `org.newsclub.net.unix` (AFUNIXSocket) from classes16 for cross-version Unix socket support.

| Class | Role |
|---|---|
| `XConnectorEpoll` | epoll-based socket server |
| `Client` | Connected client |
| `ClientSocket` | Socket wrapper |
| `ConnectionHandler` | Accept new connections |
| `RequestHandler` | Handle incoming requests |
| `UnixSocketConfig` | Socket path config — see constants below |
| `XInputStream` / `XOutputStream` | Typed data streams |
| `XStreamLock` | Thread-safe stream access |

**UnixSocketConfig socket path constants:**
- `ALSA_SERVER_PATH = "/usr/tmp/.sound/AS0"` — ALSA audio Unix socket
- `PULSE_SERVER_PATH = "/usr/tmp/.sound/PS0"` — PulseAudio Unix socket
- `SYSVSHM_SERVER_PATH = "/usr/tmp/.sysvshm/SM0"` — SysV shared memory Unix socket
- `XSERVER_PATH = "/usr/tmp/.X11-unix/X0"` — X11 display server Unix socket
- `VIRGL_SERVER_PATH = "/tmp/.virgl/V0"` — VirGL renderer Unix socket (defined but **dead — VirGLRendererComponent is never instantiated**)

> These paths are inside the imagefs rootfs (i.e., `getFilesDir()/imagefs/usr/tmp/...`). They are the Wine-side Unix socket endpoints; Wine connects here to communicate with the Java servers on the Android side.

### `xenvironment` (classes10 + classes12)
The Wine environment — Linux rootfs paths + component orchestration.

| Class | Role |
|---|---|
| `XEnvironment` | **Container for all environment components** |
| `EnvironmentComponent` | Base class for all components |
| `ImageFs` | Wine rootfs paths and version management |
| `ImageFsInstaller` | First-run installer — extracts `imagefs.txz`, installs Wine/Proton from assets, installs GPU drivers, creates `libSDL2-2.0.so` symlink; `LATEST_VERSION = 21` |

**ImageFs paths:**
- Root: `context.getFilesDir()/imagefs/`
- Wine binaries: `<root>/opt/proton-9.0-x86_64/`
- Home: `<root>/home/xuser/`
- Wine prefix: `<root>/home/xuser/.wine/`
- Cache: `<root>/home/xuser/.cache/`
- Config: `<root>/home/xuser/.config/`
- Active container symlink: `<root>/home/xuser` → `./xuser-{id}`

**XEnvironment Components (all in classes10 — xenvironment/components/):**

| Component Class | DEX | Role |
|---|---|---|
| `XServerComponent` | classes10 | Starts the Java X11 server |
| `GuestProgramLauncherComponent` | classes10 | **Launches Wine** — extracts Box64/FEX, builds env, exec's via ProcessHelper/libwinlator.so |
| `ALSAServerComponent` | classes10 | Starts ALSA audio Unix socket server |
| `PulseAudioComponent` | classes10 | Starts PulseAudio daemon |
| `SysVSharedMemoryComponent` | classes10 | Sets up SysV SHM |
| `VirGLRendererComponent` | classes10 | VirGL GPU renderer — **dead code**: class exists + loads `libvirglrenderer`, but is never instantiated anywhere in setupXEnvironment() or elsewhere in the decompiled source |

> **xenvironment DEX split:** Base classes (`XEnvironment`, `EnvironmentComponent`, `ImageFs`, `ImageFsInstaller`) → classes12. All 6 component subclasses in `xenvironment/components/` → classes10.

### `xserver` (classes4 + classes6 + classes11 + classes13)
Full Java X11 server implementation.

**Core:** `XServer`, `XClient`, `XClientConnectionHandler`, `XClientRequestHandler`, `XResource`, `XResourceManager`, `XLock`, `XKeycode`, `DesktopHelper` (abstract — attaches pointer/window listeners to handle focus; in classes11)

**Managers:** `WindowManager`, `CursorManager`, `DrawableManager`, `GraphicsContextManager`, `GrabManager`, `InputDeviceManager`, `PixmapManager`, `SHMSegmentManager`, `SelectionManager`, `IDGenerator`

**Data objects:** `Atom`, `Bitmask`, `ClientOpcodes`, `Cursor`, `Drawable`, `EventListener`, `GraphicsContext`, `Keyboard`, `Pixmap`, `PixmapFormat`, `Pointer`, `Property`, `ResourceIDs`, `ScreenInfo`, `Visual`, `Window`, `WindowAttributes`

**Extensions:** `BigReqExtension`, `DRI3Extension` (Direct Rendering mode hook point), `Extension`, `MITSHMExtension`, `PresentExtension`, `SyncExtension`

**Events (xserver/events/):** `Event` (base class), ButtonPress/Release, ConfigureNotify/Request, CreateNotify, DestroyNotify, EnterNotify/LeaveNotify, Expose, InputDeviceEvent, KeyPress/Release, MapNotify/Request, MappingNotify, MotionNotify, PointerWindowEvent, PresentCompleteNotify, PresentIdleNotify, PropertyNotify, RawEvent, ResizeRequest, SelectionClear, UnmapNotify

**Errors (xserver/errors/):** BadAccess, BadAlloc, BadAtom, BadCursor, BadDrawable, BadFence, BadGraphicsContext, BadIdChoice, BadImplementation, BadLength, BadMatch, BadPixmap, BadSHMSegment, BadValue, BadWindow, XRequestError

**Requests (xserver/requests/):** AtomRequests, CursorRequests, DrawRequests, ExtensionRequests, FontRequests, GrabRequests, GraphicsContextRequests, KeyboardRequests, PixmapRequests, SelectionRequests, WindowRequests

---

## 5. XServerDisplayActivity — Sidebar Map

The in-game sidebar (DrawerLayout, left side) has these buttons/sections. This is the main in-game UI — critical for patch points.

### Sidebar Top-Level Buttons

| Button ID | Action |
|---|---|
| `BTItemPause` | Pause/resume Wine process |
| `BTSubKeyboard` | Show on-screen keyboard |
| `BTItemInput` | Input settings dialog |
| `BTItemMouse` | Toggle Mouse submenu (`LLSubMouse`) |
| `BTItemFPS` | Toggle FPS submenu (`LLSubFPS`) |
| `BTItemGraphics` | Toggle Graphics submenu (`LLSubGraphics`) |
| `BTItemScreen` | Toggle Screen submenu (`LLSubScreen`) |
| `BTItemTaskManager` | Open TaskManagerDialog (Windows process list) |
| `BTItemLogs` | Show Wine/Box64 debug logs (visible if `enable_wine_debug=true` **OR** `enable_box64_logs=true`; hidden if both false — default) |
| `BTItemExit` | Exit game |

### FPS Submenu (`LLSubFPS`) — "Show FPS" button

| Control ID | Type | Purpose |
|---|---|---|
| `SWHudMaster` | Switch | Master HUD enable/disable |
| `CBHudFps` | CheckBox | Show FPS counter (default: checked) |
| `CBHudGpu` | CheckBox | Show GPU info (default: checked) |
| `CBHudCpuRam` | CheckBox | Show CPU/RAM (default: checked) |
| `CBHudBattTemp` | CheckBox | Show battery/temp (default: checked) |
| `CBHudGraph` | CheckBox | Show FPS graph (default: checked) |
| `CBHudRenderer` | CheckBox | Show renderer info (default: checked) — ⚠️ **BUG**: `updateSidebarHud()` calls `toggleElement(6, ...)` (invalid — valid range 0-5); does nothing. Renderer always stays on regardless of this checkbox. |
| `SBHudScale` | SeekBar | HUD size (0–100); **initial value set to 25** in `setupGraphicsSidebar()` |
| `SBHudAlpha` | SeekBar | HUD transparency (0–100); **initial value set to 100** in `setupGraphicsSidebar()` |

> **Note:** `SPNativeFPS` (FPS limiter) is NOT in this submenu — it is in `LLSubGraphics` (Graphics submenu), labeled "GAME FPS LIMIT".

### Mouse Submenu (`LLSubMouse`)

| Control ID | Type | Purpose |
|---|---|---|
| `SWRelativeMouse` | Switch | Relative mouse mode |
| `SWDisableMouse` | Switch | Disable mouse entirely |

### Graphics Submenu (`LLSubGraphics`) — "Graphic Engine" button

| Control ID | Type | Purpose |
|---|---|---|
| `SPRenderMode` | Spinner | `"Standard (Filters)"` (0) / `"Direct Rendering+"` (1) — **always `setEnabled(false)`, alpha 0.6f** (read-only display; actual mode set by `nativeRendering` per-shortcut extra → `native_rendering` Intent extra → `GLRenderer.setNativeMode()`; `use_dri3` pref is SEPARATE and only affects `MESA_VK_WSI_DEBUG` env var via smali) |
| `SPNativeFPS` | Spinner | **GAME FPS LIMIT**: `"Unlimited"` / `"30 FPS"` / `"45 FPS"` / `"60 FPS"` / `"90 FPS"` / `"120 FPS"` — hidden (`GONE`) in Direct Rendering mode; values mapped to `{0, 30, 45, 60, 90, 120}` via `NATIVE_FPS_VALUES[]` |
| `LLStandardOptions` | LinearLayout | Standard mode filter options — `visibility="gone"` by default, shown when Standard mode selected |
| ↳ `SWEnableFSR` | Switch | Enable FSR/DLS super resolution |
| ↳ `SPUpscalerMode` | Spinner | `"Super Resolution"` (FSR, 0) / `"DLS"` (1) — vkbasalt CAS/DLS |
| ↳ `SBSharpness` | SeekBar | Sharpness level 0–100 |
| ↳ `LBLSharpnessHeader` | TextView | "Sharpness Strength" label |
| ↳ `SPColorMode` | Spinner | `"Disabled"`(0) / `"HDR"`(1) / `"Natural"`(2) / `"CRT Effect"`(3) |
| `LLFrameGenOptions` | LinearLayout | **Frame Generation** — `visibility="gone"`, **NOT wired in Java** (see Dead UI section) |
| ↳ `SPFrameGenFPS` | Spinner | Generation strategy spinner — never referenced in Java |
| `LLNativeOptions` | LinearLayout | Direct Rendering+ section — `visibility="gone"` by default, shown in Direct mode; **contains only descriptive text, no interactive controls** |

> **vkbasalt/sharpness extras — FUNCTIONAL** (JADX missed; confirmed smali 2743-2765): `XServerDisplayActivity.extractGraphicsDriverFiles()` reads `vkbasaltConfig` (built from shortcut extras `sharpnessEffect`/`sharpnessLevel`/`sharpnessDenoise`) and injects it: `ENABLE_VKBASALT="1"` + `VKBASALT_CONFIG="{config}"` (e.g. `"effects=cas;casSharpness=0.8;dlsSharpness=0.8;dlsDenoise=1.0;enableOnLaunch=True"`). Only injected when vkbasaltConfig is **not empty** (i.e., sharpnessEffect ≠ `DefaultVersion.VKD3D = "None"`). JADX's Java decompilation omitted these reads because `extractGraphicsDriverFiles()` was not fully decompiled ("Method not decompiled"). The report previously (incorrectly) claimed vkbasalt was dead code.

### Screen Submenu (`LLSubScreen`)

| Button ID | Type | Purpose |
|---|---|---|
| `BTItemPipMode` | Button | Enter Picture-in-Picture mode |
| `BTItemToggleFullscreen` | Button | Toggle fullscreen |
| `BTItemMagnifier` | Button | Toggle MagnifierView (screen zoom) |

### Frame Generation Section (`LLFrameGenOptions`) — **DEAD / NOT WIRED**

Present in `left_sidebar.xml` with `visibility="gone"`, but **no Java code in XServerDisplayActivity references `LLFrameGenOptions` or `SPFrameGenFPS`**. The `FrameGenerationEffect` class is fully implemented but the sidebar control is never instantiated.

| Control ID | Type | Purpose |
|---|---|---|
| `SPFrameGenFPS` | Spinner | Frame generation target FPS — spinner exists in layout, never touched by Java |

**FrameGenerationEffect constants:**
- Modes: `MODE_NATIVE_PLUS = 0`, `MODE_FRAME_GEN = 1`
- FPS targets: `FPS_AUTO=0`, `FPS_15=15`, `FPS_20=20`, `FPS_25=25`, `FPS_30=30`, `FPS_45=45`, `FPS_60=60`, `FPS_90=90`, `FPS_120=120`

> **Patch opportunity:** Wire `LLFrameGenOptions` / `SPFrameGenFPS` into `XServerDisplayActivity` and call `effectComposer.addEffect(new FrameGenerationEffect(fps, mode))` to expose this already-implemented feature.

### Input Controls Dialog
Contains: `SProfile` (controls profile spinner), `CBShowTouchscreenControls`, `CBEnableTimeout`, `CBEnableHaptics`, `BTSettings` button.

---

## 6. Dead / Unused UI

| Layout / Element | Status | Notes |
|---|---|---|
| `screen_effect_dialog.xml` | **Dead** | `ColorEffect` class exists (brightness/contrast/gamma), layout registered in R.java but **no code inflates it**. Future feature or removed. |
| `activity_terminal.xml` | **Dead** | Has `outputTextView`, `commandInput`, `executeButton`. No `TerminalActivity` in manifest. |
| `shortcut_properties_dialog.xml` | **Dead** | In R.java but no Java class inflates it (confirmed: grep of smali and JADX sources returns no references). |
| `activity_file_picker.xml` | **Dead** | In R.java but no `FilePickerActivity` in manifest or Java source. |
| `wine_install_options_dialog.xml` | **Dead** | In R.java but no `WineInstallOptionsDialog` class references it. |
| `LLFrameGenOptions` / `SPFrameGenFPS` | **Dead** | In `left_sidebar.xml` with `visibility="gone"`. `FrameGenerationEffect` is fully implemented but sidebar spinner is never referenced in Java. Ready to wire up. |
| `sharpnessEffect` / `sharpnessLevel` / `sharpnessDenoise` shortcut extras | **Functional** | Defaults: `sharpnessEffect = "None"` (= `DefaultVersion.VKD3D`); `sharpnessLevel = "100"`; `sharpnessDenoise = "100"` (both confirmed ShortcutSettingsDialog.java lines 263, 279). When `sharpnessEffect ≠ "None"`, builds: `vkbasaltConfig = "effects={effect_lower};casSharpness={level/100};dlsSharpness={level/100};dlsDenoise={denoise/100};enableOnLaunch=True"`. Then `extractGraphicsDriverFiles()` (smali 2743-2765; JADX missed) injects `ENABLE_VKBASALT="1"` + `VKBASALT_CONFIG=vkbasaltConfig`. Fully wired. |
| `res/xml/accessibility_service_config.xml` | **Dead / Orphaned** | References `android:settingsActivity=".LoriePreferences"` — no `LoriePreferences` in this package's manifest. Orphaned from Termux:X11 codebase import. |
| `res/xml/preferences_x11.xml` | **Dead / Orphaned** | 30+ Termux:X11 preference keys (displayResolutionMode, touchMode, pointerCapture, scaleTouchpad, stylusIsMouse, clipboardEnable, etc.). None referenced in Winlator Java sources. Imported from Termux:X11 but not integrated. |
| `res/xml/shortcuts.xml` | **Dead / Orphaned** | Defines a launcher shortcut pointing at `com.termux.x11.LoriePreferences` (a different app). Not activated in Winlator manifest. Remnant of Termux:X11 import. |

---

## 6b. Navigation Menus

### Main Drawer Navigation (`main_menu.xml`) — 8 items, inflated by `MainActivity`

| Menu Item ID | Fragment | Default? |
|---|---|---|
| `main_menu_shortcuts` | `ShortcutsFragment` | — |
| `main_menu_file_manager` | `FileManagerFragment` | — |
| `main_menu_containers` | `ContainersFragment` | **Yes** (default on launch) |
| `main_menu_input_controls` | `InputControlsFragment` | — |
| `main_menu_contents` | `ContentsFragment` | — |
| `main_menu_adrenotools_gpu_drivers` | `AdrenotoolsFragment` | — |
| `main_menu_settings` | `SettingsFragment` | — |
| `main_menu_about` | (AlertDialog, not a Fragment) | — |

### Containers Toolbar Menu (`containers_menu.xml`)
| Item ID | Action |
|---|---|
| `action_big_picture_mode` | Launch `BigPictureActivity` |
| `containers_menu_add` | Add new container → `ContainerDetailFragment` |

### Container Long-Press Popup (`container_popup_menu.xml`)
| Item ID | Action |
|---|---|
| `container_edit` | Edit container → `ContainerDetailFragment` |
| `container_duplicate` | Duplicate container |
| `container_remove` | Remove container |
| `container_info` | Storage info → `StorageInfoDialog` |

### Shortcut Long-Press Popup (`shortcut_popup_menu.xml`)
| Item ID | Action |
|---|---|
| `shortcut_settings` | Per-game settings → `ShortcutSettingsDialog` |
| `shortcut_change_icon` | Change shortcut icon (gallery picker) |
| `shortcut_add_to_home_screen` | Pin to Android launcher |
| `shortcut_remove` | Remove shortcut |
| `shortcut_export` | Export shortcut as LNK file |
| `shortcut_clone_to_container` | Clone shortcut to another container |

### Content Long-Press Popup (`content_popup_menu.xml`)
| Item ID | Action |
|---|---|
| `content_info` | `ContentInfoDialog` |
| `remove_content` | Remove content |

### Process Long-Press Popup (`process_popup_menu.xml`) — TaskManagerDialog
| Item ID | Action |
|---|---|
| `process_affinity` | Set CPU affinity |
| `bring_to_front` | Bring window to front |
| `process_end` | Kill process |

### File Manager Popup (`open_file_popup_menu.xml`)
| Item ID | Action |
|---|---|
| `open_file` | Open file in Wine |
| `download_file` | Download file |

### Exec Args Presets (`extra_args_popup_menu.xml`) — `ShortcutSettingsDialog` execArgs field
Quick-insert DirectX launch flag presets:
`-force-gfx-direct`, `-force-d3d11-singlethreaded`, `-force-dx9`, `-force-d3d9`, `-force-d3d11`, `--force-gfx-direct`, `--force-d3d11-singlethreaded`, `--force-dx9`, `--force-d3d9`, `--force-d3d11`, `/d3d9`

---

## 7. Third-Party Libraries

| Library | Location | Purpose |
|---|---|---|
| Glide | classes.dex + classes2 | Image loading/caching |
| OkHttp3 / Okio | classes.dex | HTTP client |
| Retrofit2 | **classes16** (bulk of 5,474 smali) | REST API client |
| BouncyCastle | classes16 (`org.bouncycastle`) | Crypto/TLS |
| Conscrypt | classes16 (`org.conscrypt`) | TLS via OpenSSL JNI |
| OpenJSSE | classes16 (`org.openjsse`) | TLS extension |
| Apache Commons Compress | classes16 (`org.apache.commons`) | Archive extraction (zip/tar/etc.) |
| tukaani XZ/LZMA | classes16 (`org.tukaani`) | XZ/LZMA decompression |
| AFUNIXSocket | classes16 (`org.newsclub.net.unix`) | Unix domain sockets for Wine→Android IPC |
| IntelliJ/JetBrains annotations | classes16 | Kotlin annotations |
| AndroidX (all) | classes.dex + classes2 | Support libraries |
| Kotlin stdlib/coroutines | classes.dex | Kotlin runtime |
| cn.sherlock (SF2/javax.sound) | classes.dex | MIDI soundfont playback |
| jp.kshoji | classes.dex | MIDI USB controller support |
| com.github.luben.zstd | classes.dex | Zstd JNI wrapper (Java bindings) |
| com.google.* | classes.dex | Google libraries |

---

## 8. Native Libraries (arm64-v8a)

| Library | Purpose |
|---|---|
| `libwinlator.so` | **Main JNI** — process launch, hooks, core native bridge |
| `libfakeinput.so` | Fake input injection for Wine |
| `libfile_redirect_hook.so` | Redirect file paths (Wine sandbox) |
| `libgsl_alloc_hook.so` | Memory allocation interception |
| `libhook_impl.so` | Hook framework |
| `libmain_hook.so` | Main process hook |
| `libltdl.so` | Dynamic library loading (libtool) |
| `libpatchelf.so` | ELF patcher (fix Wine library paths/rpath) |
| `libpulse.so` + `libpulseaudio.so` + `libpulsecommon-13.0.so` + `libpulsecore-13.0.so` | PulseAudio daemon |
| `libsndfile.so` | Audio file decoding (PulseAudio dependency) |
| `libconscrypt_jni.so` | TLS/SSL (Conscrypt JNI) |
| `libopenxr_loader.so` | OpenXR VR runtime |
| `libzstd-jni-1.5.2-3.so` | Zstd decompression JNI |

**Cross-platform junixsocket native dirs (runtime-irrelevant on Android):**
`aarch64-MacOSX-clang/jni/libjunixsocket-native-2.6.0.dylib`, `aarch64-Windows10-clang/jni/junixsocket-native-2.6.0.dll`, `amd64-Windows10-clang/jni/junixsocket-native-2.6.0.dll`, `ppc64-AIX-clang/jni/libjunixsocket-native-2.6.0.a`, `ppc64-OS400-clang/jni/libjunixsocket-native-2.6.0.srvpgm`, `x86_64-MacOSX-clang/jni/libjunixsocket-native-2.6.0.dylib` — these are platform-specific natives bundled by the `org.newsclub.net.unix` (junixsocket/AFUNIXSocket) library for all its supported platforms. Only the Android arm64 native is used at runtime; these are dead weight in the APK.

---

## 9. Assets (Complete)

| Asset | Purpose |
|---|---|
| `imagefs.txz` | **Linux rootfs** — extracted to `getFilesDir()/imagefs/` on first run |
| `box64/box64-0.4.1.tzst` | Box64 x86_64 emulator binary |
| `fexcore/fexcore-2601.tzst` | FEX CPU emulator (ARM64EC) |
| `wowbox64/wowbox64-0.4.1.tzst` | WoWBox64 (Wine WoW64 + Box64 combo) |
| `graphics_driver/adrenotools-turnip26.0.0.tzst` | Default Turnip Vulkan driver |
| `graphics_driver/adrenotools-v819.tzst` | Alt Turnip driver variant |
| `graphics_driver/extra_libs.tzst` | Extra GPU libs |
| `graphics_driver/wrapper.tzst` | Mesa Wrapper driver |
| `dxwrapper/d8vk-1.0.tzst` | D8VK (DXVK for DX8) — **automatically extracted alongside any DXVK version < 2.4** in `extractDXWrapperFiles()` (line 1926-1928); since all current bundled DXVK versions (1.10.3, 1.11.1-sarek, 2.3.1) are < 2.4, d8vk is effectively always installed alongside DXVK |
| `dxwrapper/dxvk-1.10.3.tzst` | DXVK 1.10.3 (Mali default) |
| `dxwrapper/dxvk-1.10.3-arm64ec-async.tzst` | DXVK 1.10.3 ARM64EC async |
| `dxwrapper/dxvk-1.11.1-sarek.tzst` | DXVK-Sarek (Mali variant) |
| `dxwrapper/dxvk-2.3.1.tzst` | DXVK 2.3.1 (Adreno default) |
| `dxwrapper/dxvk-2.3.1-arm64ec-gplasync.tzst` | DXVK 2.3.1 GPL async |
| `dxwrapper/vkd3d-2.8.tzst` | VKD3D 2.8 |
| `dxwrapper/vkd3d-2.14.1.tzst` | VKD3D 2.14.1 |
| `ddrawrapper/cnc-ddraw.tzst` | CNC DDraw wrapper |
| `ddrawrapper/dd7to9.tzst` | DD7to9 wrapper |
| `ddrawrapper/nglide.tzst` | nGlide (Glide → OpenGL) |
| `wincomponents/` | `wincomponents.json` (component→DLL name mapping) + individual DLL pack archives: `ddraw.tzst`, `direct3d.tzst`, `directmusic.tzst`, `directplay.tzst`, `directshow.tzst`, `directsound.tzst`, `vcrun2010.tzst`, `xaudio.tzst` |
| `layers.tzst` | Vulkan layers |
| `input_dlls.tzst` | Windows input DLLs |
| `pulseaudio.tzst` | PulseAudio binaries |
| `container_pattern_common.tzst` | Default container/Wine prefix template |
| `proton-9.0-arm64ec.txz` | Proton 9.0 ARM64EC runtime |
| `proton-9.0-x86_64.txz` | Proton 9.0 x86_64 runtime |
| `proton-9.0-arm64ec_container_pattern.tzst` | Proton ARM64EC container template |
| `proton-9.0-x86_64_container_pattern.tzst` | Proton x86_64 container template |
| `soundfonts/wt_210k_G.sf2` | Bundled MIDI soundfont (wt_210k_G — one soundfont only) + `wt_210k_G_LICENSE.txt` |
| `default_music.mp3` | Big Picture mode background music |
| `inputcontrols/icons/` + `inputcontrols/profiles/` | Default virtual control profiles + icons |
| `gpu_cards.json` | GPU card database (NVIDIA/AMD/Intel device+vendor IDs) |
| `box64_env_vars.json` | Box64 env var profiles |
| `fexcore_env_vars.json` | FEX env var profiles |
| `wine_debug_channels.json` | Wine debug channel list |
| `wine_startmenu.json` | Wine start menu template |
| `common_dlls.json` | Common Windows DLLs list |
| `system.reg.LOG1/LOG2` | Wine registry log template |
| `dexopt/baseline.prof` + `baseline.profm` | ART baseline profile |

**wincomponents.json DLL mapping:**
- `direct3d` → d3dcompiler/d3dx DLLs (47 entries)
- `directsound` → dsound
- `directmusic` → dmband/dmcompos/dmime/etc. (10 entries)
- `directshow` → amstream/qasf/qcap/qdvd/qedit/quartz
- `directplay` → dplaysvr.exe/dplayx/dpnet/etc. (8 entries)
- `xaudio` → x3daudio/xactengine/xaudio2 DLLs (37 entries)
- `vcrun2010` → msvcp100/msvcr100/vcomp100/atl100

---

## 10. Key Storage Paths

| Path | Purpose |
|---|---|
| `${ExternalStorage}/Winlator/` | User data directory (DEFAULT_WINLATOR_PATH) |
| `${ExternalStorage}/Winlator/Shortcuts/` | Exported game shortcut LNK files |
| `context.getFilesDir()/imagefs/` | Wine Linux rootfs root (ImageFs.find(context)) |
| `imagefs/.winlator/` | Winlator config dir (getConfigDir()) |
| `imagefs/.winlator/.img_version` | ImageFS version file (integer) |
| `imagefs/opt/proton-9.0-x86_64/` | Default Wine/Proton binaries (`imageFs.winePath`) |
| `imagefs/opt/installed-wine/` | Directory tracking installed Wine versions (getInstalledWineDir()) |
| `imagefs/home/xuser` | Symlink → active container directory (`./xuser-{id}`) |
| `imagefs/home/xuser-{id}/` | Container data directory (ContainerManager) |
| `imagefs/home/xuser-{id}/.container` | Container JSON config file (`Container.getConfigFile()`) |
| `imagefs/home/xuser-{id}/.wine/drive_c/users/xuser/Desktop/` | Shortcut `.desktop` files (`Container.getDesktopDir()`) |
| `imagefs/home/xuser-{id}/.wine/drive_c/ProgramData/Microsoft/Windows/Start Menu/` | Wine start menu (`getStartMenuDir()`) |
| `imagefs/home/xuser-{id}/.local/share/icons/hicolor/{size}x{size}/apps/` | Shortcut icons (`getIconsDir(size)`) |
| `imagefs/dev/input/event0` | Fake evdev input node — **created by XServerDisplayActivity.onCreate()**: deletes `event0`–`event3` if present, creates fresh `event0`; `WinHandler.setFakeInputPath()` gets the directory path. GPLC.execGuestProgram() also calls `devInputDir.mkdirs()` + creates `event0` if absent (fallback only — it already exists from onCreate). |
| `imagefs/home/xuser/.wine/` | Wine prefix (WINEPREFIX = `/home/xuser/.wine`) |
| `imagefs/home/xuser/.cache/` | Wine cache |
| `imagefs/home/xuser/.config/` | Wine/app config |
| `imagefs/usr/tmp/` | Temp dir (getTmpDir()) |
| `imagefs/usr/lib/` | Native library dir used for LD_PRELOAD / libandroid-sysvshm.so |
| `imagefs/usr/bin/` | Wine/Box64 binaries (added to PATH) |
| `imagefs/usr/share/` | Shared data (fonts, Vulkan layers, etc.) |
| `imagefs/usr/etc/` | Config files (ALSA, TLS, XDG) |
| `context.getFilesDir()/contents/` | Downloaded content installs root |
| `context.getFilesDir()/contents/{type}/{verName}-{verCode}/` | Specific content install dir |
| `context.getFilesDir()/contents/adrenotools/{id}/meta.json` | Custom Adreno GPU driver metadata |
| `imagefs/home/xuser-{id}/app_data/cover_arts/{name}.png` | Shortcut cover art — `Shortcut.COVER_ART_DIR = "app_data/cover_arts/"` relative to `container.getRootDir()` (= `imagefs/home/xuser-{id}`); saved via `new File(container.getRootDir(), COVER_ART_DIR)`. Custom cover art: full absolute path stored in shortcut extraData `customCoverArtPath`. |
| `{winlatorPath}/logs/{name}_{timestamp}.txt` | Debug log files written by `DebugDialog` via `LogView.getLogFile(context)` — `{winlatorPath}` = `winlator_path_uri` pref or DEFAULT_WINLATOR_PATH; `{name}` = sanitized (spaces→`_`, lowercased); `{timestamp}` = `"yyyy-MM-dd_HH-mm-ss"` |
| `${ExternalStorage}/Winlator/Presets/{prefix}_{name}.wbp` | Exported custom preset files |

---

## 11. SharedPreferences

### App Global Settings (`PreferenceManager.getDefaultSharedPreferences` — SettingsFragment)

| Key | Type | Purpose |
|---|---|---|
| `dark_mode` | Boolean | Dark theme |
| `enable_big_picture_mode` | Boolean | Enable Big Picture TV mode |
| `cursor_lock` | Boolean | Lock cursor to game window (default: `false`) |
| `touchscreen_timeout_enabled` | Boolean | Auto-hide touchscreen controls on inactivity (default: `true` in timeout setup, `false` in settings dialog — possible SettingsFragment vs runtime discrepancy; runtime reads use `false` default) |
| `touchscreen_haptics_enabled` | Boolean | Haptic feedback for touchscreen controls (default: `false`) |
| `show_touchscreen_controls_enabled` | Boolean | Show touchscreen controls overlay (default: `false`) |
| `overlay_opacity` | Float | Touchscreen controls overlay opacity (default: `0.4f`) |
| `selected_profile_index` | Int | Last selected input controls profile index (default: `-1`) |
| `xinput_toggle` | Boolean | XInput toggle (default: `false`) |
| `winlator_path_uri` | String | Custom Winlator data directory URI (default: null → uses DEFAULT_WINLATOR_PATH) |
| `shortcuts_export_path_uri` | String | Shortcut export directory (default: null → uses DEFAULT_SHORTCUT_EXPORT_PATH) |
| `use_dri3` | Boolean | Enable DRI3 extension / Direct Rendering mode (**default: `true`**) |
| `use_xr` | Boolean | Enable XR/VR mode (**default: `true`**, but XrActivity.isSupported() guards actual activation) |
| `enable_wine_debug` | Boolean | Enable Wine debug output (default: `false`) |
| `wine_debug_channels` | String | Wine debug channel filter (comma-separated; `DEFAULT_WINE_DEBUG_CHANNELS = "warn,err,fixme"`) |
| `enable_box64_logs` | Boolean | Enable Box64 verbose logs (**default: `false`**) |
| `cursor_speed` | Float | Mouse cursor speed (**default: `1.0f`**; stored as float; SeekBar maps progress/100.0f → stored, and stored×100 → progress on load) |
| `enable_file_provider` | Boolean | Enable file sharing provider (**default: `true`**) |
| `open_with_android_browser` | Boolean | Open URLs in Android browser (→ `WINE_OPEN_WITH_ANDROID_BROWSER=1`) |
| `share_android_clipboard` | Boolean | Share clipboard between Android and Wine (→ `WINE_FROM/TO_ANDROID_CLIPBOARD=1`) |
| `downloadable_contents_url` | String | Custom contents.json URL override |
| `enable_custom_api_key` | Boolean | Use custom SteamGridDB API key |
| `custom_api_key` | String | Custom SteamGridDB API key (replaces hardcoded `0324c52513634547a7b32d6d323635d0`) |
| `box64_preset` | String | Default Box64 preset (**default: `"COMPATIBILITY"`**) |
| `fexcore_preset` | String | Default FEXCore preset (**default: `"INTERMEDIATE"`** for new container creation — `ContainerDetailFragment` line 370). ⚠️ **Source inconsistency:** `SettingsFragment`'s spinner update lambda (`lambda$loadFEXCorePresetSpinners$32`, line 756) uses `"COMPATIBILITY"` as fallback. So if the user opens SettingsFragment before creating any container, the spinner shows `"COMPATIBILITY"`; if saved, the pref is written as "COMPATIBILITY" → future new containers also get "COMPATIBILITY". If the user creates a container without visiting SettingsFragment first, `"INTERMEDIATE"` is applied. `box64_preset` has consistent default `"COMPATIBILITY"` in both places. |

### Other Stores
| Store | Key Examples |
|---|---|
| `contents_manager_prefs` | `graphics_driver_installed_{version}` |
| `playtime_stats` | `{shortcut.name}_playtime` (long, ms), `{shortcut.name}_play_count` (int) — game stats displayed in BigPicture |
| Big Picture prefs (DefaultSharedPreferences) | `custom_wallpaper_path`, `wallpaper_display_mode`, `frame_duration_seekbar`, `selected_animation` ("ab"/"ab_gear"/"ab_quilt"/"folder"/"none"), `parallax_mode` ("default"/"fast"/"slow"/"off"), `music_source` ("mp3"/"youtube"), `bg_music_enabled` (bool, default true), `saved_youtube_url`, `selected_mp3_path` |
| Default SharedPreferences (also) | `box64_custom_presets` (String) — custom Box64 presets serialized as CSV `CUSTOM-{N}\|{name}\|{envVars},...` (id field prefixed "CUSTOM-"; `N` auto-incremented) |
| Default SharedPreferences (also) | `fexcore_custom_presets` (String) — custom FEXCore presets serialized as CSV `CUSTOM-{N}\|{name}\|{envVars},...` |
| Default SharedPreferences (also) | `custom_driver_repos` (String, JSON array) — user-added driver repos for `RepositoryManagerDialog`; each entry: `{name, apiUrl}`; **default (when empty):** 4 preset repos: "K11MCH1 Turnip Drivers" (`api.github.com/repos/K11MCH1/AdrenoToolsDrivers/releases`), "StevenMX Turnip Drivers" (`api.github.com/repos/StevenMXZ/freedreno_turnip-CI/releases`), "Snapdragon Elite Drivers" (`api.github.com/repos/StevenMXZ/Adrenotools-Drivers/releases`), "Weab-Chan Turnip Drivers" (`api.github.com/repos/Weab-chan/freedreno_turnip-CI/releases`) |

**Custom preset export:** Saved to `${winlator_path}/Presets/{prefix}_{name}.wbp` — text format: `ID:{id}\n`, `Name:{name}\n`, `EnvVars:{envVars}\n`

---

## 12. Container Detail Settings (ContainerDetailFragment)

Six tabs. All spinners have corresponding `arrays.xml` entries.

**Tab layout IDs:** `LLTabWineConfiguration`, `LLTabWinComponents`, `LLTabEnvVars`, `LLTabDrives`, `LLTabAdvanced`, `LLTabXR`

### Tab 1 — Wine Configuration
| Field | Widget ID | Options (from arrays.xml) |
|---|---|---|
| Name | `ETName` | Free text |
| Screen Size | `SScreenSize` | Custom / 690x360 / 780x360 / 640x480 / 800x600 / 854x480 / 960x544 / 1024x768 / 1280x720 / 1280x800 / 1280x1024 / 1366x768 / 1440x900 / 1560x720 / 1600x900 / 1920x1080 |
| Wine Version | `SWineVersion` | proton-9.0-x86_64 / proton-9.0-arm64ec |
| Graphics Driver | `SGraphicsDriver` | Wrapper (only built-in option; custom drivers add entries) |
| DX Wrapper | `SDXWrapper` | WineD3D / DXVK+VKD3D |
| DX Wrapper Config | `BTDXWrapperConfig` | Opens DXVK or WineD3D config dialog |
| Audio Driver | `SAudioDriver` | ALSA / PulseAudio |
| 64-bit Emulator | `SEmulator64` | FEXCore / Box64 — **display-only** (always `setEnabled(false)` in code); automatically set to FEXCore when ARM64EC wine selected, Box64 otherwise |
| 32-bit DLL Emulator | `SEmulator` | FEXCore / Box64 — selectable only when ARM64EC wine version active; disabled and forced to Box64 for x86_64 wine |
| MIDI Soundfont | `SMIDISoundFont` | (scans soundfonts/ directory) |
| Show FPS | `CBShowFPS` | Checkbox |
| Fullscreen Stretched | `CBFullscreenStretched` | Checkbox |
| XInput | `CBEnableXInput` | Checkbox |
| DInput | `CBEnableDInput` | Checkbox |
| Exclusive XInput | `CBExclusiveXInput` | Checkbox |
| LC_ALL | `ETlcall` | Free text + `BTShowLCALL` picker (ar_EG … zh_TW — 30 locales) |

### Tab 2 — Win Components
Windows DLL component overrides (Builtin/Native per DLL group, based on `wincomponents.json`)

### Tab 3 — Environment Variables
`EnvVarsView` — editable list of key=value pairs + `AddEnvVarDialog`

### Tab 4 — Drives
Drive letter → path mappings (F: = ExternalStorage, D: = Downloads, etc.)

### Tab 5 — Advanced
| Field | Widget ID | Options |
|---|---|---|
| Startup Selection | `SStartupSelection` | Normal / Essential / Aggressive |
| Box64 Preset | `SBox64Preset` | STABILITY / COMPATIBILITY / INTERMEDIATE / PERFORMANCE + custom (Box64PresetManager) |
| Box64 Version | `SBox64Version` | 0.4.1 |
| FEXCore Version | `SFEXCoreVersion` | 2601 |
| FEXCore Preset | `SFEXCorePreset` | STABILITY / COMPATIBILITY / INTERMEDIATE / PERFORMANCE + custom (FEXCorePresetManager) |
| CPU Affinity | `CPUListView` | Per-core toggles |
| CPU Affinity WoW64 | `CPUListViewWoW64` | Per-core toggles (ARM64EC only) |
| Desktop Theme | `SDesktopTheme` | Light / Dark |
| Desktop Background Type | `SDesktopBackgroundType` | Image / Solid Color |
| Desktop Background Image | `IPVDesktopBackgroundImage` | Image picker |
| Desktop Background Color | `CPVDesktopBackgroundColor` | Color picker |
| Mouse Warp Override | `SMouseWarpOverride` | Disable / Enable / Force |

### Tab 6 — XR (VR Controller Mapping)
| Field | Widget ID |
|---|---|
| Primary Controller | `SPrimaryController` (Left / Right) |
| Button A/B/X/Y | `SButtonA/B/X/Y` |
| Grip / Trigger | `SButtonGrip`, `SButtonTrigger` |
| Thumbstick Up/Down/Left/Right | `SThumbstickUp/Down/Left/Right` |

---

## 12b. Wine Launch Environment Variables

Set by `GuestProgramLauncherComponent.execGuestProgram()` before Wine starts:

| Variable | Value |
|---|---|
| `HOME` | `imagefs/home/xuser` |
| `USER` | `xuser` |
| `TMPDIR` | `imagefs/usr/tmp` |
| `XDG_DATA_DIRS` | `imagefs/usr/share` |
| `XDG_CONFIG_DIRS` | `imagefs/usr/etc/xdg` |
| `LD_LIBRARY_PATH` | `imagefs/usr/lib:/system/lib64` |
| `GST_PLUGIN_PATH` | `imagefs/usr/lib/gstreamer-1.0` |
| `FONTCONFIG_PATH` | `imagefs/usr/etc/fonts` |
| `VK_LAYER_PATH` | `imagefs/usr/share/vulkan/implicit_layer.d:…/explicit_layer.d` |
| `WRAPPER_LAYER_PATH` | `imagefs/usr/lib` |
| `WRAPPER_CACHE_PATH` | `imagefs/usr/var/cache` |
| `WINE_NO_DUPLICATE_EXPLORER` | `1` |
| `PREFIX` | `imagefs/usr` |
| `DISPLAY` | `:0` |
| `WINE_DISABLE_FULLSCREEN_HACK` | `1` |
| `GST_PLUGIN_FEATURE_RANK` | `ximagesink:3000` |
| `ALSA_CONFIG_PATH` | `imagefs/usr/share/alsa/alsa.conf:imagefs/usr/etc/alsa/conf.d/android_aserver.conf` |
| `ALSA_PLUGIN_DIR` | `imagefs/usr/lib/alsa-lib` |
| `OPENSSL_CONF` | `imagefs/usr/etc/tls/openssl.cnf` |
| `SSL_CERT_FILE` | `imagefs/usr/etc/tls/cert.pem` |
| `SSL_CERT_DIR` | `imagefs/usr/etc/tls/certs` |
| `WINE_X11FORCEGLX` | `1` |
| `WINE_GST_NO_GL` | `1` |
| `SteamGameId` | `0` |
| `PROTON_AUDIO_CONVERT` | `0` |
| `PROTON_VIDEO_CONVERT` | `0` |
| `PROTON_DEMUX` | `0` |
| `PATH` | `wine/bin:imagefs/usr/bin` |
| `ANDROID_SYSVSHM_SERVER` | `imagefs/usr/tmp/.sysvshm/SM0` (UnixSocketConfig.SYSVSHM_SERVER_PATH) |
| `ANDROID_RESOLV_DNS` | Primary DNS (live network lookup via ConnectivityManager; fallback: `8.8.4.4`) |
| `WINE_NEW_NDIS` | `1` |
| `LD_PRELOAD` | `imagefs/usr/lib/libandroid-sysvshm.so:imagefs/usr/lib/libfakeinput.so` — `libfakeinput.so` is copied from APK nativeLibDir to imagefs lib dir on first launch if missing |
| `FAKE_EVDEV_DIR` | `imagefs/dev/input` (file `event0` created here for fake input) |
| `WINE_OPEN_WITH_ANDROID_BROWSER` | `1` (if `open_with_android_browser` setting enabled) |
| `WINE_FROM_ANDROID_CLIPBOARD` | `1` (if `share_android_clipboard` setting enabled) |
| `WINE_TO_ANDROID_CLIPBOARD` | `1` (if `share_android_clipboard` setting enabled) |
| `BOX64_NOBANNER` | `"1"` always; `"0"` if `enable_box64_logs=true` |
| `BOX64_DYNAREC` | `"1"` always |
| `BOX64_X11GLX` | `"1"` always |
| `BOX64_NORCFILES` | `"1"` always |
| `BOX64_LOG` | `"1"` only if `enable_box64_logs=true` |
| `BOX64_DYNAREC_MISSING` | `"1"` only if `enable_box64_logs=true` |
| `BOX64_*` (preset) | From `Box64PresetManager.getEnvVars("box64", context, preset)` — sets per-preset values for: `BOX64_DYNAREC_SAFEFLAGS`, `BOX64_DYNAREC_FASTNAN`, `BOX64_DYNAREC_FASTROUND`, `BOX64_DYNAREC_X87DOUBLE`, `BOX64_DYNAREC_BIGBLOCK`, `BOX64_DYNAREC_STRONGMEM`, `BOX64_DYNAREC_FORWARD`, `BOX64_DYNAREC_CALLRET`, `BOX64_DYNAREC_WAIT`, `BOX64_AVX`, `BOX64_UNITYPLAYER`, `BOX64_MMAP32` (see table below) |
| `BOX64_MMAP32` | Set by preset (see table); **overridden to `"0"` on Mali GPU** (after preset, before user envVars). If `"1"` after Mali check AND wineInfo is NOT arm64EC → also sets `WRAPPER_DISABLE_PLACED=1`. |
| `FEX_*` | From FEXCorePresetManager preset: `FEX_TSOENABLED`, `FEX_VECTORTSOENABLED`, `FEX_MEMCPYSETTSOENABLED`, `FEX_HALFBARRIERTSOENABLED`, `FEX_X87REDUCEDPRECISION`, `FEX_MULTIBLOCK` |
| `HODLL` | ARM64EC Wine only — `"libwow64fex.dll"` (FEXCore) or `"wowbox64.dll"` (WoWBox64) |
| `GUEST_PROGRAM_LAUNCHER_COMMAND` | **Override** — if set in envVars, replaces the normal launch command entirely (split on `;`, parts joined with space). Useful for custom launch scripts. |
| `EXTRA_EXEC_ARGS` | **Override** — used only when launching WITHOUT a shortcut; replaces default `wfm.exe` with specified args; removed from envVars before launch. |
| `CNC_DDRAW_CONFIG_FILE` | `"C:\\windows\\syswow64\\ddraw.ini"` — only set when `ddrawrapper = "cnc-ddraw"`. **DDrawrapper extraction logic** (XServerDisplayActivity ~1942-1950): `nglide.tzst` is always extracted; then `if (ddrawrapper.contains("None"))` → `restoreOriginalDllFiles("ddraw.dll", "d3dimm.dll")` + return early; else extract `ddrawrapper/{name}.tzst`. ⚠️ **Source bug:** `ddrawrapper` values are stored as **lowercase** via `StringUtils.parseIdentifier()` (e.g. `"none"`), but the condition checks for `"None"` (capital N, from `DefaultVersion.VKD3D`). So `restoreOriginalDllFiles` is **dead code** — never reached. With ddrawrapper=`"none"` (default), `"ddrawrapper/none.tzst"` extraction is attempted (file does not exist; likely fails silently). |
| `ADRENOTOOLS_DRIVER_PATH` | Driver dir path — set by `AdrenotoolsManager.setDriverById(envVars, imageFs, driverId)` in `extractGraphicsDriverFiles()` when `(isFromResources(driverId) \|\| installedDrivers.contains(driverId)) && libraryName ≠ ""`. `driverId = graphicsDriverConfig.get("version")` (empty = System driver → no env vars). `isFromResources(id)` checks if `assets/graphics_driver/adrenotools-{id}.tzst` exists. |
| `ADRENOTOOLS_HOOKS_PATH` | `imagefs.getLibDir()` = `{filesDir}/imagefs/usr/lib` — set alongside ADRENOTOOLS_DRIVER_PATH |
| `ADRENOTOOLS_DRIVER_NAME` | Library name from driver's `meta.json` `libraryName` field — set alongside ADRENOTOOLS_DRIVER_PATH |
| `ADRENOTOOLS_REDIRECT_DIR` | `{DEFAULT_WINLATOR_PATH}/` — set **only** when `${ExternalStorage}/Winlator/qgl_config.txt` exists |
| `DXVK_FRAME_RATE` | Set only when `dxwrapper` contains "dxvk" AND framerate≠0; value = framerate string from dxwrapperConfig. Also sets `DXVK_CONFIG = "dxgi.maxFrameRate = {n}; d3d9.maxFrameRate = {n}"` (spaces around `=`; exact format from DXVKConfigDialog.setEnvVars() line 224). (**Note:** `DXVKConfigDialog.setEnvVars()` call was missed by JADX; confirmed in smali at XServerDisplayActivity.smali:2080) |
| `DXVK_ASYNC` | Set to `"1"` when dxwrapper=dxvk + async=1 in dxwrapperConfig |
| `DXVK_GPLASYNCCACHE` | Set to `"1"` when dxwrapper=dxvk + asyncCache=1 in dxwrapperConfig |
| `VKD3D_FEATURE_LEVEL` | Set to vkd3dLevel from dxwrapperConfig (e.g. `"12_1"`) when dxwrapper=dxvk |
| `DXVK_STATE_CACHE_PATH` | Set to `{filesDir}/imagefs/home/xuser/.cache` when dxwrapper=dxvk |
| `WRAPPER_NO_PATCH_OPCONSTCOMP` | Set to `"1"` only when dxwrapper=dxvk AND DXVK version string = `"1.11.1-sarek"` (in `extractGraphicsDriverFiles()`, confirmed smali:2059–2080; JADX missed) |
| `MESA_VK_WSI_DEBUG` | Two possible values: (1) `"sw"` when SharedPreferences `use_dri3 = false` (smali ~2115+; JADX missed); (2) `"forcesync"` when `graphicsDriverConfig.syncFrame = "1"` (smali 2469-2484 — v7 register reused; JADX missed). |
| `WRAPPER_VK_VERSION` | Composite: `"{graphicsDriverConfig.vulkanVersion}.{patch}"` where `patch = GPUInformation.getVulkanVersion(driverId, context).split("\\.")[2]` (actual device Vulkan patch component). E.g. config says "1.3", device reports "1.3.204" → WRAPPER_VK_VERSION="1.3.204". Smali 2233-2286; JADX missed. |
| `WRAPPER_EXTENSION_BLACKLIST` | Blacklisted Vulkan extension list from `graphicsDriverConfig.blacklistedExtensions` (smali; JADX missed) |
| `WRAPPER_DEVICE_NAME` | GPU name from `graphicsDriverConfig.gpuName` — set only when gpuName ≠ `"Device"` AND dxvkVersion ≠ `"1.11.1-sarek"` (smali 2328-2347; JADX missed). The sarek exclusion prevents device name injection when using the sarek DXVK build. |
| `WRAPPER_DEVICE_ID` | Numeric GPU device ID — from `WineD3DConfigDialog.getDeviceIdFromGPUName(context, gpuName)` — same conditional block as WRAPPER_DEVICE_NAME (smali 2350-2358; JADX missed) |
| `WRAPPER_VENDOR_ID` | Numeric GPU vendor ID — from `WineD3DConfigDialog.getVendorIdFromGPUName(context, gpuName)` — same conditional block as WRAPPER_DEVICE_NAME (smali 2360-2369; JADX missed) |
| `WRAPPER_VMEM_MAX_SIZE` | Max VRAM in MB — from `graphicsDriverConfig.maxDeviceMemory`; set only when `> 0` (smali; JADX missed) |
| `MESA_VK_WSI_PRESENT_MODE` | Set unconditionally to `graphicsDriverConfig.presentMode` (e.g. `"mailbox"`); when presentMode contains `"immediate"` also sets `WRAPPER_MAX_IMAGE_COUNT="1"` (overriding the DEFAULT_ENV_VARS `=0` default). Smali 2430-2435; JADX missed. |
| `WRAPPER_RESOURCE_TYPE` | Resource type string from `graphicsDriverConfig.resourceType` (smali; JADX missed) |
| `WRAPPER_DISABLE_PRESENT_WAIT` | Disable present-wait flag from `graphicsDriverConfig.disablePresentWait` (smali; JADX missed) |
| `WRAPPER_EMULATE_BCN` | Integer-encoded BCN emulation mode — derived from `graphicsDriverConfig.bcnEmulation` + `bcnEmulationType`; vendor-dependent sparse-switch logic at smali 2625-2718 (JADX missed) |
| `WRAPPER_USE_BCN_CACHE` | `graphicsDriverConfig.bcnEmulationCache` value — whether to cache BCN emulation (smali 2738-2740; JADX missed) |
| `ENABLE_VKBASALT` | Set to `"1"` when `vkbasaltConfig` is non-empty — built from shortcut extras `sharpnessEffect`/`sharpnessLevel`/`sharpnessDenoise`; confirmed smali 2754-2756 (JADX missed `extractGraphicsDriverFiles()`) |
| `VKBASALT_CONFIG` | The `vkbasaltConfig` string value — injected alongside `ENABLE_VKBASALT`; confirmed smali 2761-2765 (JADX missed) |
| `WINE_D3D_CONFIG` | Set when dxwrapper does NOT contain "dxvk" (i.e. WineD3D mode) — via `WineD3DConfigDialog.setEnvVars()` (confirmed smali:2115); value = `"csmt=0x{csmt},strict_shader_math=0x{strictShaderMath},OffscreenRenderingMode={mode},VideoMemorySize={size},VideoPciDeviceID={deviceId},VideoPciVendorID={vendorId},renderer={renderer}"`. Config keys: csmt ("3"=enabled=`GPS_MEASUREMENT_3D`, "0"=disabled; ⚠️ default in DEFAULT_DXWRAPPERCONFIG is "3"=enabled), strict_shader_math ("1"/"0"), OffscreenRenderingMode (fbo/backbuffer), videoMemorySize (MB), renderer (gl/vulkan/gdi). GPU IDs via gpu_cards.json. ⚠️ **Source bug:** `getVendorIdFromGPUName(context, config.get("vendorID"))` uses wrong key `"vendorID"` instead of `"gpuName"` — vendorID always empty in output. |

> **MANGOHUD/MANGOHUD_CONFIG** — explicitly stripped from envVars before launch (in GuestProgramLauncherComponent). Any user-set MangoHUD env vars are silently removed.

**Box64 preset env var values** (from `Box64PresetManager`; `ExifInterface.GPS_MEASUREMENT_2D="2"`, `GPS_MEASUREMENT_3D="3"`):

| Var | STABILITY | COMPATIBILITY | INTERMEDIATE | PERFORMANCE |
|---|---|---|---|---|
| `BOX64_DYNAREC_SAFEFLAGS` | 2 | 2 | 2 | 1 |
| `BOX64_DYNAREC_FASTNAN` | 0 | 0 | 1 | 1 |
| `BOX64_DYNAREC_FASTROUND` | 0 | 0 | 0 | 1 |
| `BOX64_DYNAREC_X87DOUBLE` | 1 | 1 | 1 | 0 |
| `BOX64_DYNAREC_BIGBLOCK` | 0 | 0 | 1 | 3 |
| `BOX64_DYNAREC_STRONGMEM` | 2 | 1 | 0 | 0 |
| `BOX64_DYNAREC_FORWARD` | 128 | 128 | 128 | 512 |
| `BOX64_DYNAREC_CALLRET` | 0 | 0 | 1 | 1 |
| `BOX64_DYNAREC_WAIT` | 0 | 1 | 1 | 1 |
| `BOX64_AVX` | 0 | 0 | 0 | 0 |
| `BOX64_UNITYPLAYER` | 1 | 1 | 0 | 0 |
| `BOX64_MMAP32` | 0 | 0 | 1 | 1 |

**FEXCore preset env var values** (from `FEXCorePresetManager`):

| Var | STABILITY | COMPATIBILITY | INTERMEDIATE | PERFORMANCE |
|---|---|---|---|---|
| `FEX_TSOENABLED` | 1 | 1 | 1 | 0 |
| `FEX_VECTORTSOENABLED` | 1 | 1 | 0 | 0 |
| `FEX_MEMCPYSETTSOENABLED` | 1 | 1 | 0 | 0 |
| `FEX_HALFBARRIERTSOENABLED` | 1 | 1 | 1 | 0 |
| `FEX_X87REDUCEDPRECISION` | 0 | 0 | 1 | 1 |
| `FEX_MULTIBLOCK` | 0 | 1 | 1 | 1 |

> **Full env var priority** (last wins): (1) preset vars (BOX64_+FEX_) → (2) Mali GPU override (BOX64_MMAP32→"0") → (3) system vars (HOME/USER/PATH/etc.) → (4) `this.envVars` = container envVars + shortcut envVars + audio vars. **Container/shortcut envVars therefore override system and preset vars.**

**Additional env vars set in XServerDisplayActivity** (merged before passing to GuestProgramLauncherComponent):

| Variable | Value |
|---|---|
| `LC_ALL` | From container/shortcut `lc_all` field |
| `WINEPREFIX` | `imageFs.wineprefix` = `imagefs/home/xuser/.wine` |
| `WINEDEBUG` | **Always set**: `"+channel1,+channel2,..."` (when `enable_wine_debug=true` AND channels not empty) or `"-all"` (otherwise — disables all Wine debug output) |
| `WINEESYNC` | `1` (added if not already present in container envVars) |
| `ANDROID_ALSA_SERVER` | Unix socket path (when `audioDriver = "alsa"`) |
| `ANDROID_ASERVER_USE_SHM` | `"true"` (when ALSA) |
| `PULSE_SERVER` | Unix socket path (when `audioDriver = "pulseaudio"`) |

**XServerDisplayActivity env var build order (later entries override earlier):**
1. `envVars.put("LC_ALL", ...)` + `envVars.put("WINEPREFIX", ...)` + `envVars.put("WINEDEBUG", ...)` — set first (but overridable by container envVars below)
2. `container.getEnvVars()` (DEFAULT_ENV_VARS + user edits) — **overrides step 1**
3. `shortcut.getExtra("envVars")` (per-game override)
4. `WINEESYNC=1` — added only if not already present in envVars
5. `overrideEnvVars` (programmatic override, e.g. from test tools)
6. `ANDROID_ALSA_SERVER` + `ANDROID_ASERVER_USE_SHM` or `PULSE_SERVER` — **always last; cannot be overridden by user**

This merged EnvVars object is then passed to `GuestProgramLauncherComponent` as `this.envVars`. Inside `execGuestProgram()`, it is merged LAST (after preset and system vars), so **container/shortcut envVars win over all system and preset vars**.

**Guest executable format:**
- **x86_64** (Box64): `imagefs/usr/bin/box64 wine explorer /desktop=shell,{screenInfo} winhandler.exe {args}` (`imageFs.getBinDir() + "/box64 " + guestExecutable`)
- **ARM64EC** (FEXCore/WoWBox64): `{winePath}/bin/wine explorer /desktop=shell,{screenInfo} winhandler.exe {args}` (localVar `winePath = imageFs.getWinePath() + "/bin"`; `guestExecutable` starts with `"wine"`, command = `winePath + "/" + guestExecutable`)

**`winhandler.exe` args built by `getWineStartCommand()`:**
- With `.exe` shortcut: `winhandler.exe /dir {escapedExeDir} "{exeFilename}" {execArgs}`
- With `.lnk` shortcut: `winhandler.exe "{lnkPath}" {execArgs}`
- No shortcut + no `EXTRA_EXEC_ARGS`: `winhandler.exe "wfm.exe"` (Wine File Manager — browse mode)
- No shortcut + `EXTRA_EXEC_ARGS` env var set: `winhandler.exe {EXTRA_EXEC_ARGS}` (then var removed)

> `winhandler.exe` is a Windows-side Wine helper executable bundled in the container that actually launches the target game/app within the Wine desktop.

If `GUEST_PROGRAM_LAUNCHER_COMMAND` env var is set, it replaces the entire command (split on `;`, joined with space).

---

## 13. Launch Pipeline

```
MainActivity
  └─ onNavigationItemSelected(containers) → ContainersFragment
       └─ User taps container/shortcut
            ├─ [if XrActivity.isEnabled(context)] → XrActivity.openIntent() → XrActivity (extends XServerDisplayActivity)
            │    isEnabled() = XrActivity.isSupported() [device is Meta/Oculus] AND use_xr pref=true
            │    XrActivity inherits full XServerDisplayActivity pipeline below + adds OpenXR JNI overlay
            └─ [otherwise] → Intent to XServerDisplayActivity
            └─ XServerDisplayActivity.onCreate()
                 ├─ ContainerManager.getContainerById()
                 ├─ ImageFs.find(context) → rootDir = getFilesDir()/imagefs/
                 ├─ ContainerManager.activateContainer() → creates xuser symlink
                 └─ [background thread — Executors.newSingleThreadExecutor()]
                      ├─ setupWineSystemFiles() — extracts DXWrapper files if changed,
                      │    applies WineThemeManager, configures joystick registry,
                      │    sets WineStartMenu; calls extractDXWrapperFiles(dxwrapper) internally
                      ├─ extractGraphicsDriverFiles() — extracts GPU driver tzst,
                      │    injects WRAPPER_*/DXVK_*/MESA_*/vkbasalt env vars into this.envVars
                      ├─ changeWineAudioDriver() — resolves audioDriver for container
                      └─ setupXEnvironment()
                           ├─ this.envVars.put(LC_ALL, WINEPREFIX, WINEDEBUG)
                           ├─ new GuestProgramLauncherComponent(contentsManager, wineProfile, shortcut)
                           │    (wineProfile = contentsManager.getProfileByEntryName(container.getWineVersion()) — may return null for built-in wine;
                           │     ⚠️ wineProfile field is stored but NEVER READ by any method — effectively dead parameter)
                           ├─ guestProgramLauncherComponent.set(Container, WineInfo, guestExecutable, box64Preset, fexcorePreset)
                           ├─ this.envVars.putAll(container.getEnvVars()) [overrides LC_ALL/WINEPREFIX/WINEDEBUG + any WRAPPER_* from extractGraphicsDriverFiles]
                           ├─ this.envVars.putAll(shortcut.getExtra("envVars")) [if shortcut]
                           ├─ this.envVars.put(WINEESYNC=1 if absent), overrideEnvVars
                           ├─ new XEnvironment(this, imageFs)
                           ├─ xenv.addComponent(new SysVSharedMemoryComponent())   ← added first
                           ├─ xenv.addComponent(new XServerComponent())            ← second
                           ├─ xenv.addComponent(new ALSAServerComponent() | PulseAudioComponent()) ← third (also sets audio env vars)
                           ├─ guestProgramLauncherComponent.setEnvVars(this.envVars)
                           └─ xenv.startEnvironmentComponents()
                                └─ GuestProgramLauncherComponent.start()
                                     ├─ extractBox64Files() — extracts box64-{version}.tzst
                                     ├─ extractEmulatorsDlls() — extracts wowbox64/fexcore DLLs
                                     ├─ builds EnvVars (BOX64_*/FEX_* presets → system vars → putAll(this.envVars) → HODLL)
                                     └─ ProcessHelper.exec() via libwinlator.so JNI
                                          └─ Wine runs under Box64/FEX
                                               └─ Wine connects back via XConnector Unix sockets
                                                    └─ XServer.java receives X11 protocol
                                                         └─ GLRenderer renders framebuffer
                                                              └─ EffectComposer applies effects
                                                                   └─ XServerView displays on screen
```

---

## 14. Key Patch Points for Ludashi-plus

| Area | Class(es) / File | Patch Opportunity |
|---|---|---|
| **Navigation menu** | `MainActivity.onNavigationItemSelected()` (classes8) | Add new Fragment tabs to drawer |
| **Container settings** | `Container.java` (classes14), `ContainerDetailFragment` (classes8) | Add new per-container settings fields |
| **Shortcut settings** | `ShortcutSettingsDialog` (classes4), `Shortcut` (classes14) | Add per-game override keys |
| **In-game sidebar** | `XServerDisplayActivity` (classes8), `left_sidebar.xml` | Add new sidebar buttons/submenus |
| **Renderer effects** | `EffectComposer` (classes12), `GLRenderer` (classes12), effects package (classes9) | Hook new GL effects into pipeline |
| **ColorEffect expose** | `XServerDisplayActivity` + `screen_effect_dialog.xml` | Dead brightness/contrast/gamma UI — wire it up |
| **Graphics driver** | `GraphicsDriverConfigDialog` (classes4) | Add driver options |
| **FPS HUD** | `FrameRating` (classes14) | Add HUD fields |
| **Content/components** | `ContentsManager` (classes15) | Add new download sources |
| **App settings** | `SettingsFragment` (classes8) | Add global prefs |
| **Game launch env** | `GuestProgramLauncherComponent.start()` (classes10) | Inject env vars, pre/post-launch hooks |
| **Big Picture mode** | `BigPictureActivity` (classes8), `BigPictureAdapter` (classes12) | Customize TV UI |
| **Package name** | `AndroidManifest.xml` | `com.ludashi.benchmark` → rename for variant builds |
| **App label** | `AndroidManifest.xml` | `"Winlator CMOD"` → project name |

---

## 15. Build Notes for CI

- **raws.xml**: Present at `res/values/raws.xml` — must `rm -f` before rebuild
- **public.xml firebase**: Check before rebuild — `sed -i '/firebase_common_keep\|firebase_crashlytics_keep/d'`
- **classes16**: 6.1MB / 5,474 smali — at or near DEX method limit. **New classes → inject as classes17+ at build time, same pattern as BannerHub classes18.dex**
- **Signing**: AOSP testkey (same as BannerHub)
- **Package rename**: Replace `com.ludashi.benchmark` throughout AndroidManifest for variant builds
- **App label**: Change `android:label="Winlator CMOD"`
- **versionName `7.1.4x-cmod`**: Unknown if server-checked. Treat like BannerHub — **do NOT change** unless confirmed safe
- **Debuggable**: `android:debuggable="true"` — strip for release if desired
- **No classes12 bypass needed** (unlike BannerHub) — classes12 has only 58 smali files, well under limit

---

## 16. Differences vs GameHub/BannerHub

| Aspect | GameHub (BannerHub base) | Winlator Ludashi (this) |
|---|---|---|
| Architecture | Closed-source Chinese Android app | Open-source Winlator fork |
| Source access | Smali reverse engineering only | Full readable Java via JADX |
| Wine integration | Opaque WineActivityData Parcelable | Clean XEnvironment + component system |
| UI framework | Custom Kotlin UI (proprietary) | Standard Android Fragments + DrawerLayout |
| Patching complexity | Hard (obfuscated, proprietary) | Easy (open source, clear class names) |
| DEX count | ~18 + extras | 16 |
| Package name | `com.xiaoji.egggame` → `banner.hub` | `com.ludashi.benchmark` |
| Graphics | Proprietary Wine/D3D bridge | DXVK/VKD3D/Wrapper + GLRenderer |
| X server | Proprietary | Full Java X11 server implementation |
| Renderer effects | None (sidebar toggles only) | CRT, HDR, FSR, FXAA, FrameGen, Natural, Toon, NTSC, ColorEffect (dead) |
| CPU emulator | Box64 | Box64 + FEXCore (ARM64EC) |
| Big Picture | Not present | Present (with SteamGridDB) |
| VR support | Not present | Present (XrActivity, OpenXR) |

---

*Verified: 2026-04-08 — **THIRTY-EIGHT full passes — CONVERGED ✓** (3 consecutive clean passes: Pass 36, 37, 38). (Pass 27: 11+ findings; Pass 28: 4 findings; Pass 29: 5 findings; Pass 30: 2 findings; Pass 31: 0 findings — counter=1; Pass 32: 1 finding — counter reset to 0 (d8vk auto-extracted with DXVK < 2.4); Pass 33: 1 finding — counter reset to 0 (CBHudRenderer bug: toggleElement(6) is out of range, Renderer checkbox does nothing); Pass 34: 1 finding — counter reset to 0 (fexcore_preset inconsistency: SettingsFragment spinner fallback uses "COMPATIBILITY" but ContainerDetailFragment new-container path uses "INTERMEDIATE"); Pass 35: 3 findings — counter reset to 0 (DefaultVersion: D8VK="1.0", WRAPPER="System", WRAPPER_ADRENO="turnip26.0.0" ARE real class constants — prior note was wrong; all 8 DefaultVersion constants now documented); Pass 36: 0 findings — counter=1; Pass 37: 0 findings — counter=2; Pass 38: 0 findings — counter=3 ✓ CONVERGED. Pass 36-38 coverage: ShortcutsFragment routing, SettingsFragment prefs+defaults, Box64/FEXCore preset tables, Container constants+field defaults, EnvVars wire format, ImageFs paths, WineInfo.fromIdentifier/isMainWineVersion, XrActivity.isEnabled/23axes/19buttons, WINEDEBUG/WINEESYNC, DEFAULT_WINE_DEBUG_CHANNELS, ContainerManager activateContainer/createContainer, Shortcut fields, checkObsoleteOrMissingProperties, MANGOHUD strip, HODLL, GUEST_PROGRAM_LAUNCHER_COMMAND, VirGLRendererComponent dead code, BigPictureActivity API_KEY+graphicsDroverConfig typo, 4 custom driver repos, NATIVE_FPS_VALUES, yNwKYgM6SkM, WineThemeManager DEFAULT_DESKTOP_THEME, SMouseWarpOverride Disable/Enable/Force, WineD3DConfigDialog WINE_D3D_CONFIG format+vendorID bug, DXVK_CONFIG exact format, DXVKConfigDialog all env vars, FrameRating 500ms FPS/1000ms stats/50ms graph/MAX_SAMPLES=60/66.6f cap, setupXEnvironment full env var build order confirmed).* Every source package dir-listed, every layout file listed, all menu XMLs read, assets confirmed, manifest cross-checked, Container/Shortcut/ImageFs/ContainerManager fields verified against source, env var pipeline fully traced, smali count corrected to 13,646; Pass 7: XServerComponent+VirGLRendererComponent DEX, Box64/FEXCore presets, UnixSocketConfig paths, Shortcut fields, FEX env vars, AdrenotoolsManager, ContentProfile suite, custom preset prefs, xenvironment DEX split, 3 dead layouts, 3 Termux:X11 orphaned XMLs, res/xml section; Pass 8: MANGOHUD strip, HODLL env var, GUEST_PROGRAM_LAUNCHER_COMMAND override, full BOX64_ constant set, launch command format (box64 for x86_64 / direct for ARM64EC), PROTON_VIDEO_CONVERT+PROTON_DEMUX names corrected, ANDROID_SYSVSHM_SERVER exact path, Container .container config file, container internal dirs (Desktop/StartMenu/Icons/app_data), extraData keys (imgVersion/wineprefixNeedsUpdate/appVersion), dev/input/event0, LD_PRELOAD absolute paths, SharedPreferences defaults (use_dri3=true, use_xr=true, enable_file_provider=true), XrActivity static helpers documented; Pass 9: BigPictureAdapter DEX corrected (classes12 not 14), playtime_stats prefs, music/animation/parallax BigPicture prefs, cover art cache path, EnvVars wire format, WineThemeManager DEFAULT_DESKTOP_THEME+wallpaper BMP path, WineUtils DLL override list+dosdevices symlinks, FrameRating HUD element indices 0-5+GPU sysfs paths+stats interval, typo "graphicsDroverConfig" in BigPictureActivity; Pass 10: GLRenderer DEX corrected (classes9→classes12 in Key Patch Points), classes9 clarified to renderer/effects/ only, classes14 bigpicture partial clarified to steamgrid/ subpackage specifically, renderer/material/ classes15 noted; Pass 11: cross-platform lib dirs corrected (were wrongly described as "libltdl.so libtool artifacts" — are actually junixsocket-native-2.6.0 platform natives for macOS/Windows/AIX/ppc64/x86_64 from org.newsclub.net.unix AFUNIXSocket library); Pass 12: pref defaults added (enable_box64_logs=false, box64_preset="COMPATIBILITY", fexcore_preset="INTERMEDIATE"); Pass 13: no new findings; Pass 14: no new findings; Pass 15: preferences.xml=identical to preferences_x11.xml (BOTH orphaned Termux:X11 XMLs; SettingsFragment is fully programmatic), cursor_speed default=1.0f added, ALSA_CONFIG_PATH second path made explicit (imagefs/usr/etc/alsa/conf.d/android_aserver.conf), SSL_CERT_FILE/DIR exact paths added, ANDROID_ALSA_SERVER+ANDROID_ASERVER_USE_SHM removed from GuestProgramLauncherComponent table (set in XServerDisplayActivity not GPLC), XServerDisplayActivity merge order corrected (LC_ALL/WINEPREFIX/WINEDEBUG set BEFORE container envVars; audio vars always last), full Box64+FEXCore preset value tables added, BOX64_MMAP32 preset-based behavior documented (preset: STABILITY/COMPATIBILITY=0, INTERMEDIATE/PERFORMANCE=1; Mali override to 0), WINEDEBUG always set ("+channels" or "-all"), BTItemLogs visibility corrected (enable_wine_debug OR enable_box64_logs), SMouseWarpOverride options added (Disable/Enable/Force); Pass 16: cover art path corrected (was getExternalFilesDir() — is actually imagefs/home/xuser-{id}/app_data/cover_arts/; confirmed via container.getRootDir() + COVER_ART_DIR in Shortcut.java:134); Pass 17: shortcut extras count corrected (29→28; customCoverArtPath is a hidden extra not in ShortcutSettingsDialog — was listed in both hidden extras AND the 29 keys = contradiction), SPRenderMode noted as always disabled (setEnabled(false)/alpha 0.6f — read-only display), SBHudScale initial=25 and SBHudAlpha initial=100 added; Pass 18: ARM64EC launch command corrected ({winePath}/bin wine→{winePath}/bin/wine), component add order corrected (SysVSHM first, XServer second — was inverted), VIRGL_SERVER_PATH="/tmp/.virgl/V0" added to UnixSocketConfig constants (was missing), VirGLRendererComponent flagged as dead code (class exists+loads libvirglrenderer but is never instantiated anywhere in decompiled source); Pass 19: Container field init values corrected (graphicsDriverConfig→DEFAULT_GRAPHICSDRIVERCONFIG, screenSize→DEFAULT_SCREEN_SIZE, graphicsDriver→DEFAULT_GRAPHICS_DRIVER, dxwrapper→DEFAULT_DXWRAPPER; wincomponents/audioDriver/emulator/envVars/drives init added; desktopTheme→DEFAULT_DESKTOP_THEME; startupSelection default=1=ESSENTIAL not Normal; exclusiveXInput default=true; inputType default=4=FLAG_INPUT_TYPE_XINPUT), WinHandler ports+input flags added (SERVER_PORT=7947 CLIENT_PORT=7946 FLAG_INPUT_TYPE_XINPUT=4 FLAG_INPUT_TYPE_DINPUT=8), EnvironmentManager description improved (static HashMap snapshot), DXVK env vars added to env table (DXVK_FRAME_RATE/DXVK_ASYNC/DXVK_GPLASYNCCACHE/DXVK_CONFIG/VKD3D_FEATURE_LEVEL/DXVK_STATE_CACHE_PATH — JADX missed DXVKConfigDialog.setEnvVars() call; confirmed in smali:2080); Pass 20: three more env vars added (WRAPPER_NO_PATCH_OPCONSTCOMP=1 for dxvk 1.11.1-sarek only; MESA_VK_WSI_DEBUG=sw when use_dri3=false; WINE_D3D_CONFIG when dxwrapper≠dxvk via WineD3DConfigDialog.setEnvVars() — all confirmed smali ~2059–2120, JADX missed all three); Pass 21: driver download pipeline fully documented (RepositoryManagerDialog→DriverDownloadDialog→AdrenotoolsManager.installDriver; 4 default GitHub release repos; pref `custom_driver_repos` JSON; downloads .zip/.tzst to cacheDir/driver_temp.zip), log file path added ({winlatorPath}/logs/{name}_{timestamp}.txt via LogView.getLogFile()), Presets path added to storage table; Pass 22: DXVK_CONFIG format corrected (spaces around `=` in exact format `"dxgi.maxFrameRate = {n}; d3d9.maxFrameRate = {n}"`); WineD3DConfigDialog source bug documented (vendorID always empty due to wrong config key `"vendorID"` instead of `"gpuName"`); Pass 23: 4 ADRENOTOOLS_* env vars added to table (ADRENOTOOLS_DRIVER_PATH/HOOKS_PATH/DRIVER_NAME/REDIRECT_DIR — set by AdrenotoolsManager.setDriverById() in extractGraphicsDriverFiles() when graphicsDriver≠"System"; confirmed smali:2226; JADX missed in Java decompilation); Pass 24: vkbasalt Dead UI table entry corrected to Functional (ENABLE_VKBASALT+VKBASALT_CONFIG are set; smali 2743-2765 — JADX missed); full WRAPPER_* env var batch added (WRAPPER_VK_VERSION/EXTENSION_BLACKLIST/DEVICE_NAME/DEVICE_ID/VENDOR_ID/VMEM_MAX_SIZE/RESOURCE_TYPE/DISABLE_PRESENT_WAIT/EMULATE_BCN/USE_BCN_CACHE + MESA_VK_WSI_PRESENT_MODE — all from graphicsDriverConfig via extractGraphicsDriverFiles(); JADX missed entire batch); MESA_VK_WSI_DEBUG "forcesync" case added (syncFrame="1" in graphicsDriverConfig, smali 2469-2484); Pass 25: WRAPPER_VK_VERSION formula corrected (composite: config major.minor + GPUInformation.getVulkanVersion() patch component); WRAPPER_DEVICE_NAME condition corrected (dxvkVersion ≠ "1.11.1-sarek" not "v5"); MESA_VK_WSI_PRESENT_MODE WRAPPER_MAX_IMAGE_COUNT override value confirmed ("1"); graphicsDriverConfig "version" field documented as adrenotools driver ID (smali 2043-2052); vkbasaltConfig exact format + "None" sentinel documented; ddrawrapper extraction logic documented (nglide always extracted; source bug: restoreOriginalDllFiles unreachable — "None" vs "none" case mismatch); ADRENOTOOLS_DRIVER_PATH condition corrected (isFromResources||installedDrivers.contains, not graphicsDriver≠System); Pass 26: NO NEW FINDINGS — first clean pass (verified: FEXCore/Box64 preset tables, FrameRating HUD indices 0-5, shortcut extras count (28), GuestProgramLauncherComponent env vars, WINEDEBUG always-set with DEFAULT_WINE_DEBUG_CHANNELS="warn,err,fixme", SMouseWarpOverride registry write, Cover art path, BigPictureActivity=classes8, BigPictureAdapter=classes12); **Pass 27: 11+ findings — counter reset to 0**: (1) 5 missing SharedPrefs (touchscreen_timeout_enabled/haptics_enabled/show_touchscreen_controls_enabled, overlay_opacity=0.4f, selected_profile_index=-1); (2) GuestProgramLauncherComponent constructor args corrected (was "(shortcut, contentsManager)" → correct: "(contentsManager, wineProfile, shortcut)" with ContentProfile arg); (3) launch pipeline restructured: pre-setupXEnvironment background thread sequence documented (setupWineSystemFiles→extractGraphicsDriverFiles→changeWineAudioDriver→setupXEnvironment); (4) 6 missing extraData cache keys added (dxwrapper/wincomponents/desktopTheme/startupSelection/audioDriver/graphicsDriver — all used as change-detection caches by setupWineSystemFiles()+changeWineAudioDriver()); (5) appVersion description corrected (APK version code not "config schema version"); (6) changeWineAudioDriver() registry write documented (Software\\Wine\\Drivers\\Audio = "alsa"/"pulse"); **Pass 28: 4 findings — counter stays at 0**: (1) SEmulator64 spinner added to Tab 1 (always disabled, display-only; auto-set to FEXCore/Box64 by wine selection); (2) XR launch branch added to pipeline (both ContainersFragment.runContainer() and ShortcutsFragment.runFromShortcut() check XrActivity.isEnabled() before routing to XServerDisplayActivity); (3) BigPicture runFromShortcut documented as NOT including native_rendering (ShortcutsFragment does; BigPicture does not); (4) wineProfile dead parameter documented (constructor arg stored at line 128, never read in any GPLC method — effectively dead); Pass 28 also verified: audio driver "pulseaudio" stored value vs "pulse" registry value (correct), GPLC addBox64EnvVars() full contents (correct), execGuestProgram() env var merge order (correct), WineInfo.identifier() format + isArm64EC() check (correct), ImageFs.winePath default (correct), setupWineSystemFiles() full flow (correct), activateContainer() symlink behavior (correct). **Pass 29: 5 findings — counter stays at 0**: (1) event0 creation corrected — created by XServerDisplayActivity.onCreate() (deletes event0-3, creates fresh event0; sets WinHandler fake input path to dir); GPLC is fallback only; (2) Intent extras shortcut_name and disableXinput documented as NOT consumed by XServerDisplayActivity (XServerDisplayActivity reads disableXinput from shortcut file, not Intent; shortcut_name unused in XServerDisplayActivity Java); (3) SPRenderMode description corrected — actual mode is nativeRendering per-shortcut extra → native_rendering Intent → GLRenderer.setNativeMode(), NOT use_dri3 pref (use_dri3 only affects MESA_VK_WSI_DEBUG in smali); (4) custom preset ID format corrected to CUSTOM-{N}|{name}|{envVars} (not just {id}|...); (5) BigPicture runFromShortcut does NOT check XrActivity.isEnabled() — always routes to XServerDisplayActivity (only ShortcutsFragment+ContainersFragment have XR routing); Pass 29 verified: WineInfo.identifier() format, ImageFs paths (HOME_PATH/CACHE_PATH/CONFIG_PATH), Container constructors all defaults, XrActivity.isSupported() Meta/Oculus check, DEFAULT_ENV_VARS exact string, REMOTE_PROFILES URL, DEFAULT_WINLATOR_PATH, FrameRating GPU sysfs paths, BigPictureActivity custom API key applied in smali onCreate() (JADX missed), XrActivity.openIntent() with shortcut_path for shortcut launches / null for container launches. **Pass 30: 2 findings — counter stays at 0**: (1) sharpnessLevel and sharpnessDenoise shortcut extra defaults added (both = "100"; from ShortcutSettingsDialog.java lines 263, 279 — were not stated in report); (2) DefaultVersion table corrected: removed D8VK and Wrapper rows (neither is a DefaultVersion class constant); added note that D8VK is legacy-only (not in UI, kept by migration code); Wrapper driver options clarified to 3 entries from wrapper_graphics_driver_version_entries: "System"/"v819"/"turnip26.0.0" (was missing "v819"). Pass 30 also verified: WineThemeManager DEFAULT_DESKTOP_THEME = "LIGHT,IMAGE,#0277bd" ✓ (Theme.LIGHT + "," + BackgroundType.IMAGE + ",#0277bd"); Container.saveData() all JSON keys confirmed present in report (id/name/screenSize/envVars/cpuList/cpuListWoW64/graphicsDriver/graphicsDriverConfig/emulator/dxwrapper/dxwrapperConfig/audioDriver/wincomponents/drives/showFPS/fullscreenStretched/inputType/startupSelection/box64Version/fexcorePreset/fexcoreVersion/box64Preset/desktopTheme/extraData/midiSoundFont/lc_all/primaryController/controllerMapping/exclusiveXInput/wineVersion); STARTUP_SELECTION constants confirmed (NORMAL=0/ESSENTIAL=1/AGGRESSIVE=2, type byte); vkbasalt_sharpness_entries confirmed (None/CAS/DLS); dxwrapper_entries confirmed (WineD3D/DXVK+VKD3D only — 2 options); arrays.xml full scan completed.*
