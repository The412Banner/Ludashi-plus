# Ludashi-plus Master Map — v3.0 Base APK

Generated: 2026-04-30  
Base APK: `Winlator-Ludashi 3.0 bionic` (`ludashi-bionic.apk` from StevenMXZ/Winlator-Ludashi tag `3.0`)  
Decompiled at: `/data/data/com.termux/files/home/ludashi_decoded_3.0/`  
Build status: **✅ All 5 APK variants built successfully on 3.0 branch** (CI run 25165864621)

---

## 1. APK Metadata

| Field | Value |
|---|---|
| Package name | `com.ludashi.benchmark` |
| Version name | `7.1.4x-cmod` |
| Version code | `20` |
| minSdkVersion | `26` |
| targetSdkVersion | `28` |
| compileSdkVersion | `34` (Android 14) |
| apktool version used | `2.12.1` |
| DEX count | 15 (smali + smali_classes2 through smali_classes15) |
| Apktool forced package ID | `127` (0x7f) |

---

## 2. AndroidManifest Summary

### Activities (base v3.0)

| Class | Notes |
|---|---|
| `com.winlator.cmod.MainActivity` | Main nav drawer activity |
| `com.winlator.cmod.XServerDisplayActivity` | Wine/X11 display; `sensorLandscape`, PiP, `singleTask` |
| `com.winlator.cmod.BigPictureActivity` | Big Picture mode |
| `com.winlator.cmod.ControlsEditorActivity` | Input controls editor; `sensorLandscape`, fullscreen |
| `com.winlator.cmod.ExternalControllerBindingsActivity` | Controller bindings |
| `com.winlator.cmod.XrActivity` | XR/VR activity; `vr_process`, landscape |

**New in v3.0:** `XrActivity` (not observed in earlier maps).  
**No services** declared in v3.0 base manifest (WinlatorService/ForegroundService absent from base).

### Our store additions (patches/AndroidManifest.xml — 15 store entries)

Activities added by our patches:
- `com.winlator.cmod.store.GogMainActivity` / `GogLoginActivity` / `GogGamesActivity`
- `com.winlator.cmod.store.EpicMainActivity` / `EpicLoginActivity` / `EpicGamesActivity`
- `com.winlator.cmod.store.AmazonMainActivity` / `AmazonLoginActivity` / `AmazonGamesActivity`
- `com.winlator.cmod.store.SteamMainActivity` / `SteamLoginActivity` / `QrLoginActivity` / `SteamGamesActivity` / `SteamGameDetailActivity`
- `com.winlator.cmod.store.SteamForegroundService` (foregroundServiceType=dataSync)

### Permissions (base v3.0)

```
ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE, FOREGROUND_SERVICE,
HIGH_SAMPLING_RATE_SENSORS, INTERNET, MANAGE_EXTERNAL_STORAGE,
MODIFY_AUDIO_SETTINGS, POST_NOTIFICATIONS, READ_EXTERNAL_STORAGE,
VIBRATE, WRITE_EXTERNAL_STORAGE, WRITE_SECURE_SETTINGS,
com.android.launcher.permission.INSTALL_SHORTCUT
```

---

## 3. Key Class Locations (smali)

### smali_classes8/com/winlator/cmod/ — Main UI

| Class | Notes |
|---|---|
| `MainActivity` | Nav drawer, menu dispatch, fragment host |
| `XServerDisplayActivity` | Wine/Vulkan display, 44 lambda synthetics |
| `XrActivity` | XR support (new in v3.0) |
| `BigPictureActivity` | Big picture mode |
| `ControlsEditorActivity` | Input controls UI |
| `ExternalControllerBindingsActivity` | Controller key bindings |
| `AdrenotoolsFragment` | GPU driver management UI |
| `ContainerDetailFragment` | Per-container settings UI (largest fragment, 47+ lambdas) |
| `ContainersFragment` | Container list |
| `ContentsFragment` | Contents tab (still exists in smali, just removed from menu) |
| `FileManagerFragment` | File manager |
| `InputControlsFragment` | Input controls list |
| `SettingsFragment` | App settings |
| `ShortcutsFragment` | Shortcuts list |
| `ShortcutBroadcastReceiver` | Launcher shortcut handler |

#### MainActivity — public methods

```
onCreate(Bundle)
onActivityResult(int, int, Intent)
onBackPressed()
onNavigationItemSelected(MenuItem) → boolean
onOptionsItemSelected(MenuItem) → boolean
onRequestPermissionsResult(int, String[], int[])
toggleDrawer()
```

#### XServerDisplayActivity — public methods

```
onCreate(Bundle), onDestroy(), onStop(), onResume(), onPause()
onActivityResult(int, int, Intent)
onBackPressed(), onConfigurationChanged(Configuration), onWindowFocusChanged(boolean)
onTrimMemory(int)
dispatchGenericMotionEvent(MotionEvent) → boolean
dispatchKeyEvent(KeyEvent) → boolean
getContainer() → Container
getInputControlsView() → InputControlsView
getOverrideEnvVars() → EnvVars
getWinHandler() → WinHandler
getXServer() → XServer
getXServerView() → XServerView
setDXWrapper(String)
```

### smali_classes11/com/winlator/cmod/renderer/ — Renderer (FULLY REWRITTEN in v3.0)

| Class | Notes |
|---|---|
| `VulkanRenderer` | Main renderer — replaces GLRenderer entirely |
| `VulkanRenderer$RenderableWindow` | Per-window render state |
| `GPUImage` | Hardware buffer image; now uses AHB → VkImage |
| `Texture` | Texture wrapper |
| `ViewTransformation` | View/surface transform math |

**GLRenderer and EffectComposer: completely absent from v3.0.** Not found anywhere in the decompiled APK.

#### VulkanRenderer — public methods

```
constructor(XServerView, XServer)
dumpRendererInfo()
getEffectId() → int
getFpsLimit() → int
getMagnifierZoom() → float
getRefreshRateLimit() → int
getSharpness() → float
isCursorVisible() → boolean
isFullscreen() → boolean
isNativeMode() → boolean
isScreenOffsetYRelativeToCursor() → boolean
onChangeWindowZOrder(Window)
onDestroyWindow(Window)
onMapWindow(Window)
onPointerMove(short, short)
onSurfaceChanged(int, int)
onSurfaceCreated(Surface)
onSurfaceDestroyed()
onUnmapWindow(Window)
onUpdateWindowAttributes(Window, Bitmask)
onUpdateWindowContent(Window)
onUpdateWindowContentDirect(Window, Drawable, short, short)
onUpdateWindowGeometry(Window, boolean)
queueSceneUpdate()
setCursorVisible(boolean)
setDriverInfo(String, String, String)
setEffect(int, float)
setFilterMode(int)          ← new: Bilinear/Nearest Neighbor
setFpsLimit(int)
setFrameRating(Object)
setMagnifierZoom(float)
setNativeMode(boolean)
setRefreshRateLimit(int)
setScreenOffsetYRelativeToCursor(boolean)
setSwapRB(boolean)
setUnviewableWMClasses(String...)
setVerboseLog(boolean)
setVkPresentMode(int)       ← new: FIFO / Mailbox
toggleFullscreen()
updateScene()
updateVisualCursorPosition(int, int)
```

#### GPUImage — public methods

```
constructor(int)
constructor(short, short)
allocateTexture(short, short, ByteBuffer)
checkIsSupported()   [static]
destroy()
getHardwareBufferPtr() → long
getStride() → short
getVirtualData() → ByteBuffer
isSupported() → boolean  [static]
lock()
unlock()
updateFromDrawable(Drawable)
```

### smali_classes13/com/winlator/cmod/ — Container + HUD

| Class | Location | Notes |
|---|---|---|
| `ContainerManager` | `smali_classes13/container/` | Container CRUD + async ops |
| `WinlatorHUD` | `smali_classes13/widget/` | HUD rewritten in v3.0 |

#### ContainerManager — public methods

```
constructor(Context)
activateContainer(Container)
createContainerAsync(JSONObject, ContentsManager, Callback)
duplicateContainerAsync(Container, Runnable)
extractContainerPatternFile(Container, String, ContentsManager, File, OnExtractFileListener) → boolean
getContainerById(int) → Container
getContainerForShortcut(Shortcut) → Container
getContainers() → ArrayList
getContext() → Context
getNextContainerId() → int
isInitialized() → boolean
loadShortcuts() → ArrayList
removeContainerAsync(Container, Runnable)
```

#### WinlatorHUD — public methods (v3.0 rewrite)

```
constructor(Context), constructor(Context, AttributeSet)
onAttachedToWindow(), onDetachedFromWindow()
onDraw(Canvas), onMeasure(int,int)
onVisibilityChanged(View, int), onWindowVisibilityChanged(int)
onFrame(), onTouchEvent(MotionEvent) → boolean
onRendererDetected(String), onRendererGone()
disableByUser(), disableByUser(boolean), enableByUser()
forceReset(), reset(), resetFromContainer()
hasSavedPref() → boolean
isSavedVisible() → boolean
setDataSource(HudDataSource)
setGpuName(String)
setHudAlpha(float)
setHudScale(float)
setIsNative(boolean)
setRenderer(String)
syncCheckboxes(CheckBox×6)   ← v3.0: 6-checkbox sync (expanded HUD options)
toggleElement(int, boolean)
update()
```

### smali_classes14/com/winlator/cmod/contents/ — ContentManager (v3.0 rewrite)

| Class | Notes |
|---|---|
| `ContentsManager` | Per-component install/download manager |
| `ContentProfile` | Component metadata |
| `ContentProfile$ContentType` | Enum of component types |
| `ContentProfile$ContentFile` | Individual file within a content profile |
| `AdrenotoolsManager` | GPU driver management |
| `Downloader` | HTTP download with progress |

#### ContentsManager — public methods

```
constructor(Context)
applyContent(ContentProfile) → boolean
extraContentFile(Uri, OnInstallFinishedCallback)
finishInstallContent(ContentProfile, OnInstallFinishedCallback)
getProfileByEntryName(String) → ContentProfile
getProfiles(ContentProfile.ContentType) → List
getUnTrustedContentFiles(ContentProfile) → List
readProfile(File) → ContentProfile
removeContent(ContentProfile)
setGraphicsDriverInstalled(String, boolean)
setRemoteProfiles(String)
syncContents()
cleanTmpDir(Context)  [static]
getContentDir(Context) → File  [static]
getContentTypeDir(Context, ContentType) → File  [static]
getEntryName(ContentProfile) → String  [static]
getInstallDir(Context, ContentProfile) → File  [static]
getSourceFile(Context, ContentProfile, String) → File  [static]
getTmpDir(Context) → File  [static]
```

### smali_classes4/com/winlator/cmod/contentdialog/ — New per-component download UI

New in v3.0 — replaces the old Contents tab. Components (FEX, Box64, DXVK, drivers, etc.) are now downloaded from dialogs within Container/Shortcut settings rather than a dedicated menu tab.

| Class | Notes |
|---|---|
| `ContentDialog` | Base download/install dialog |
| `ContentInfoDialog` | Shows component info + file list |
| `ContentUntrustedDialog` | Warns on untrusted content |
| `DriverDownloadDialog` | Downloads GPU drivers (Turnip etc.) |
| `DriverRepo` | Remote driver repo handler |
| `GraphicsDriverConfigDialog` | Configure installed GPU driver |
| `DXVKConfigDialog` | DXVK config + install |
| `RendererOptionsDialog` | Renderer settings (VkPresentMode, filter mode, native mode) |
| `DebugDialog` | Debug/log options |
| `AddEnvVarDialog` | Add environment variable |

#### RendererOptionsDialog — public methods
```
constructor(View, Config, boolean)
toVkPresentMode(String) → int  [static]  ← maps "FIFO"/"Mailbox" string to int
```

### smali_classes10/com/winlator/cmod/xserver/ — XServer

Notable: `GraphicsContext`, `GraphicsContextManager` present. This is unchanged from v2.9 functionally.

---

## 4. Resource Structure

### Menu — main_menu.xml (v3.0 base)

v3.0 base has **7 items** (Contents tab removed):

```xml
<group checkableBehavior="single">
  main_menu_shortcuts          @id/main_menu_shortcuts
  main_menu_file_manager       @id/main_menu_file_manager
  main_menu_containers         @id/main_menu_containers
  main_menu_input_controls     @id/main_menu_input_controls
  main_menu_adrenotools_gpu_drivers  @id/main_menu_adrenotools_gpu_drivers
  main_menu_settings           @id/main_menu_settings
  main_menu_about              @id/main_menu_about
</group>
```

Our patched menu adds a second group with GOG / Epic Games / Amazon Games / Steam.

### Menu ID shifts — v2.9 → v3.0

| Name | v2.9 ID | v3.0 ID | Our patch pins to |
|---|---|---|---|
| main_menu_about | 0x7f09026c | 0x7f090275 | **0x7f09026c** (pinned via public.xml) |
| main_menu_adrenotools_gpu_drivers | 0x7f09026d | 0x7f090276 | **0x7f09026d** |
| main_menu_containers | 0x7f09026e | 0x7f090277 | **0x7f09026e** |
| main_menu_contents | 0x7f09026f | **REMOVED** | **REMOVED from public.xml** |
| main_menu_file_manager | 0x7f090270 | 0x7f090278 | **0x7f090270** |
| main_menu_input_controls | 0x7f090271 | 0x7f090279 | **0x7f090271** |
| main_menu_settings | 0x7f090272 | 0x7f09027a | **0x7f090272** |
| main_menu_shortcuts | 0x7f090273 | 0x7f09027b | **0x7f090273** |

**Our `patches/res/values/public.xml` pins all base menu IDs to v2.9 values**, so our `MainActivity.smali` patch's `packed-switch` at `0x7f09026c` remains valid. The v3.0 native IDs are overridden at build time.

### Our custom store IDs (ludashi_plus_ids.xml — dynamic @+id/)

```xml
group_game_stores    (dynamic — assigned by aapt at build time)
main_menu_gog        (dynamic — 0x7f09038a in smali patch)
main_menu_epic       (dynamic — 0x7f090389)
main_menu_amazon     (dynamic — 0x7f090388)
main_menu_steam      (dynamic — 0x7f09038b)
```

These are `@+id/` declarations so they get fresh IDs above the v2.9 range — no conflict with v3.0's new assignments.

### String keys

| Key | In v3.0 base | Status |
|---|---|---|
| `contents` | ✅ present | Used internally, not needed by us |
| `shortcuts` | ✅ | Used in menu |
| `containers` | ✅ | Used in menu |
| `input_controls` | ✅ | Used in menu |
| `adrenotools_gpu_drivers` | ✅ | Used in menu |
| `settings` | ✅ | Used in menu |
| `about` | ✅ | Used in menu |
| `main_menu_gog/epic/amazon/steam` | ❌ not in base | Added by us as literal strings in menu XML |
| `help_steamgrid` | ✅ present | SteamGrid API key string — pre-existing |

### Key layouts

| Layout | Present in v3.0 | Notes |
|---|---|---|
| `main_activity.xml` | ✅ | Nav drawer + fragment container |
| `main_menu_header.xml` | ✅ | Drawer header |
| `xserver_display_activity.xml` | ✅ | Wine display; major changes expected (Vulkan) |
| `renderer_options_dialog.xml` | ✅ | **New in v3.0** — VkPresentMode, filter mode |
| `contents_fragment.xml` | ✅ | Still exists even though menu item removed |
| `container_detail_fragment.xml` | ✅ | Per-container settings |
| `frame_rating` layout | ❌ **REMOVED** | Was in v2.9; removed in our public.xml fix |
| `screen_effect_dialog` layout | ❌ **REMOVED** | Was in v2.9 (shader effects UI); Vulkan rebuild removed it |

---

## 5. Renderer Changes (v3.0)

### What was removed

| Class | Status |
|---|---|
| `GLRenderer` | **GONE** — completely removed |
| `EffectComposer` | **GONE** — completely removed |
| `screen_effect_dialog` layout | **GONE** |
| `frame_rating` layout | **GONE** |
| Shader effect IDs (`CBEnableCRTShader`, `CBEnableFXAA`, `CBEnableNTSCEffect`, `CBEnableToonShader`) | **GONE** |
| Render mode spinner (`SPRenderMode`) | **GONE** |
| Color adjustment controls (`LBLColorAdjustment`, `SBBrightness`, `SBContrast`, `SBGamma`) | **GONE** |
| Native options layout (`LLNativeOptions`, `CBNativeRendering`) | **GONE** (concept persists via `VulkanRenderer.isNativeMode()`) |
| Old HUD IDs (`TVFpsBig`, `TVGpuLoad`, `TVHardwareStats`, `TVRenderer`, `TVWattsTemp`) | **GONE** — HUD rewritten |
| Graph container (`FLGraphContainer`) | **GONE** |
| Separator IDs (`Sep0-3`) | **GONE** |

### What replaced them

| New | Notes |
|---|---|
| `VulkanRenderer` | Full Vulkan pipeline; AHB → VkImage, no CPU copy |
| `VulkanRenderer$RenderableWindow` | Per-window render state |
| `VulkanRenderer.setVkPresentMode(int)` | FIFO (stable) or Mailbox (low latency) |
| `VulkanRenderer.setFilterMode(int)` | Bilinear or Nearest Neighbor filtering |
| `VulkanRenderer.setNativeMode(boolean)` | Native rendering toggle |
| `VulkanRenderer.setRefreshRateLimit(int)` | 60Hz or DeviceRefreshRate |
| `RendererOptionsDialog` | New UI for above options, replaces screen_effect_dialog |
| `GPUImage` (rewritten) | Uses `getHardwareBufferPtr()` (AHB pointer) instead of GL textures |

---

## 6. ContentManager Changes

### What changed

v3.0 did **not** remove `ContentsFragment` from the smali — the class still exists in `smali_classes8`. However, `main_menu_contents` was removed from:
- `res/menu/main_menu.xml` (menu item gone)
- `res/values/public.xml` (ID gone)

The underlying `ContentsManager` (now in `smali_classes14/contents/`) was **restructured into a full package**:
- Moved from `com.winlator.cmod.ContentManager` → `com.winlator.cmod.contents.ContentsManager`
- New `ContentProfile` model with `ContentType` enum and `ContentFile` inner class
- New `Downloader` class for HTTP downloads
- New `contentdialog/` package for per-component UI

### How components are downloaded now

In v3.0, components (FEX, Box64, DXVK, Wine, GPU drivers) are downloaded from dialogs launched within Container Detail or Shortcut settings — not from a top-level Contents tab. The `ContentsManager` API is unchanged in shape but the entry points are now:
- `DriverDownloadDialog` — launched from Container/Shortcut GPU driver setting
- `DXVKConfigDialog` — launched from DXVK setting in container
- `ContentDialog` — generic base for other components

---

## 7. Patch Compatibility Assessment

### patches/res/values/public.xml
**Status: ✅ Compatible (with 3.0 branch fixes applied)**

24 dead entries were removed for the 3.0 branch:
- Shader effect IDs (5 entries)
- Render option IDs (8 entries)  
- HUD IDs (5 entries)
- `main_menu_contents` (1 entry)
- Layout IDs: `frame_rating`, `screen_effect_dialog` (2 entries)
- Separator IDs `Sep0-3` (4 entries) — wait, Sep0-3 = 4

Remaining entries pin v2.9 menu IDs so our smali patch's packed-switch table remains valid.

### patches/res/values/ludashi_plus_ids.xml
**Status: ✅ Compatible — no changes needed**

Uses dynamic `@+id/` declarations; no conflict with v3.0's ID space.

### patches/res/menu/main_menu.xml
**Status: ✅ Compatible (with 3.0 branch fix applied)**

`main_menu_contents` item removed. Our store group (GOG/Epic/Amazon/Steam) is appended as a second group — unaffected by v3.0 changes.

### patches/AndroidManifest.xml
**Status: ✅ Compatible — no changes needed**

Adds store activities and SteamForegroundService. v3.0 base manifest structure unchanged (no new activities we conflict with). `XrActivity` is new in v3.0 but we carry it through our full manifest replacement correctly.

### patches/smali_classes8/com/winlator/cmod/MainActivity.smali
**Status: ✅ Compatible — verified working**

Key analysis:
- `packed-switch` at `0x7f09026c` — matches our pinned v2.9 menu IDs in public.xml ✅
- `ContentsFragment` instantiation still present at `:pswitch_4` — the class still exists in v3.0 smali so this doesn't crash, but the menu item is hidden (no way to navigate there), which is acceptable
- GOG/Epic/Amazon/Steam dispatch at `0x7f09038a-0x7f09038b` — above all v3.0 base IDs, no conflict ✅
- No references to removed renderer classes ✅

**Watch item:** The `ContentsFragment` dispatch case in `pswitch_4` is now dead code (no menu item leads to it). Harmless, but if a future v3.0+ base removes `ContentsFragment` from smali, this case would need removal.

### patches/res/mipmap-*/ic_launcher.png
**Status: ✅ Compatible — icon files, no dependency on APK internals**

---

## 8. Extension Compatibility

### extension/*.java (GOG, Epic, Amazon store classes)
**Status: ✅ Fully compatible**

All in package `com.winlator.cmod.store`. Zero imports from base APK Winlator classes. Only standard Android SDK + org.json + okhttp3 dependencies. No renderer, ContentManager, or changed class references.

### extension/steam/*.kt + *.java (Steam integration)
**Status: ✅ Fully compatible**

Zero `import com.winlator.*` statements across all Steam Kotlin/Java files. The Steam extension is entirely self-contained using JavaSteam, Ktor, and Android SDK. No base APK internals referenced.

### Java compilation classpath (CI)
**Status: ✅ No changes needed**

Extension is compiled against `android.jar + org-json.jar + javasteam.jar + javasteam-depotdownloader.jar + protobuf-java.jar + zxing-core.jar + commons-lang3.jar`. None of these have v3.0 dependencies.

---

## 9. Known Issues / Future Work for v3.0

### Dead code: ContentsFragment dispatch in MainActivity patch
The `pswitch_4` case in `MainActivity.smali` still routes to `ContentsFragment` even though the menu item is removed. Harmless for now since `ContentsFragment.class` still exists in v3.0 smali. If a future base removes it, this will crash — track it.

### Smali ID pinning dependency
Our build depends on `patches/public.xml` overriding v3.0's native ID assignments for all base menu items. This is stable as long as we ship a `public.xml` patch. If the base APK ever switches to `@+id/` declarations (not fixed IDs), the pinning approach would need revisiting.

### RendererOptionsDialog — new Vulkan options not exposed to users yet
v3.0 adds `setVkPresentMode(FIFO/Mailbox)`, `setFilterMode(Bilinear/Nearest)`, `setRefreshRateLimit(60Hz/DeviceRefreshRate)`. These are surfaced in `RendererOptionsDialog` in container settings. Our store extension does not interfere with this. Future opportunity: expose these from our store game detail page.

### XrActivity (new in v3.0)
`XrActivity` is registered in v3.0 base manifest and **confirmed present** in `patches/AndroidManifest.xml` with correct attributes (`singleTask`, `landscape`, `:vr_process`). No action needed.

---

## 10. DEX Structure (v3.0 base)

| DEX | Main packages |
|---|---|
| `smali` (classes.dex) | `androidx.*`, `com.google.*`, `kotlin.*`, `okhttp3.*` |
| `smali_classes2` | `androidx.*` continuation |
| `smali_classes3` | `androidx.*` continuation |
| `smali_classes4` | `com.winlator.cmod.contentdialog.*` |
| `smali_classes5` | Additional winlator packages |
| `smali_classes6` | Additional winlator packages |
| `smali_classes7` | Additional winlator packages |
| `smali_classes8` | `com.winlator.cmod.*` (main UI, activities, fragments) |
| `smali_classes9` | Additional winlator packages |
| `smali_classes10` | `com.winlator.cmod.xserver.*`, `com.winlator.cmod.math.*` |
| `smali_classes11` | `com.winlator.cmod.renderer.*` (VulkanRenderer, GPUImage) |
| `smali_classes12` | `com.winlator.cmod.xserver.errors.*` |
| `smali_classes13` | `com.winlator.cmod.container.*`, `com.winlator.cmod.widget.*` (WinlatorHUD) |
| `smali_classes14` | `com.winlator.cmod.contents.*` (ContentsManager, ContentProfile) |
| `smali_classes15` | Additional packages |

Our injected DEX files are assigned indices 17+ (classes17.dex through classes22+):
- `classes17.dex` — Java store extension (GOG/Epic/Amazon + Java Steam support)
- `classes18.dex` (+ overflow) — JavaSteam library
- `classes19+.dex` — Kotlin Steam extension
- Later indices — DepotDownloader + deps
