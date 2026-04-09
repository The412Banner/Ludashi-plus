.class public Lcom/winlator/cmod/MainActivity;
.super Landroidx/appcompat/app/AppCompatActivity;
.source "MainActivity.java"

# interfaces
.implements Lcom/google/android/material/navigation/NavigationView$OnNavigationItemSelectedListener;


# static fields
.field public static final CONTAINER_PATTERN_COMPRESSION_LEVEL:B = 0x9t

.field public static final EDIT_INPUT_CONTROLS_REQUEST_CODE:B = 0x3t

.field public static final OPEN_DIRECTORY_REQUEST_CODE:B = 0x4t

.field public static final OPEN_FILE_REQUEST_CODE:B = 0x2t

.field public static final OPEN_IMAGE_REQUEST_CODE:B = 0x5t

.field public static final PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE:B = 0x1t


# instance fields
.field private containerManager:Lcom/winlator/cmod/container/ContainerManager;

.field private drawerLayout:Landroidx/drawerlayout/widget/DrawerLayout;

.field private editInputControls:Z

.field private isDarkMode:Z

.field public final preloaderDialog:Lcom/winlator/cmod/core/PreloaderDialog;

.field private selectedProfileId:I

.field private sharedPreferences:Landroid/content/SharedPreferences;


# direct methods
.method public static synthetic $r8$lambda$PQ_2cL5jQKUmZTkYlIV_MmAyFeo(Lcom/winlator/cmod/MainActivity;Landroid/content/DialogInterface;I)V
    .locals 0

    invoke-direct {p0, p1, p2}, Lcom/winlator/cmod/MainActivity;->lambda$showAllFilesAccessDialog$0(Landroid/content/DialogInterface;I)V

    return-void
.end method

.method public constructor <init>()V
    .locals 1

    .line 51
    invoke-direct {p0}, Landroidx/appcompat/app/AppCompatActivity;-><init>()V

    .line 59
    new-instance v0, Lcom/winlator/cmod/core/PreloaderDialog;

    invoke-direct {v0, p0}, Lcom/winlator/cmod/core/PreloaderDialog;-><init>(Landroid/app/Activity;)V

    iput-object v0, p0, Lcom/winlator/cmod/MainActivity;->preloaderDialog:Lcom/winlator/cmod/core/PreloaderDialog;

    .line 60
    const/4 v0, 0x0

    iput-boolean v0, p0, Lcom/winlator/cmod/MainActivity;->editInputControls:Z

    return-void
.end method

.method private synthetic lambda$showAllFilesAccessDialog$0(Landroid/content/DialogInterface;I)V
    .locals 3
    .param p1, "dialog"    # Landroid/content/DialogInterface;
    .param p2, "which"    # I

    .line 146
    new-instance v0, Landroid/content/Intent;

    const-string v1, "android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION"

    invoke-direct {v0, v1}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V

    .line 147
    .local v0, "intent":Landroid/content/Intent;
    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "package:"

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {p0}, Lcom/winlator/cmod/MainActivity;->getPackageName()Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v1}, Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;

    move-result-object v1

    invoke-virtual {v0, v1}, Landroid/content/Intent;->setData(Landroid/net/Uri;)Landroid/content/Intent;

    .line 148
    invoke-virtual {p0, v0}, Lcom/winlator/cmod/MainActivity;->startActivity(Landroid/content/Intent;)V

    .line 149
    return-void
.end method

.method private requestAppPermissions()Z
    .locals 8

    .line 182
    const-string v0, "android.permission.WRITE_EXTERNAL_STORAGE"

    invoke-static {p0, v0}, Landroidx/core/content/ContextCompat;->checkSelfPermission(Landroid/content/Context;Ljava/lang/String;)I

    move-result v1

    const/4 v2, 0x0

    const/4 v3, 0x1

    if-nez v1, :cond_0

    move v1, v3

    goto :goto_0

    :cond_0
    move v1, v2

    .line 183
    .local v1, "hasWritePermission":Z
    :goto_0
    const-string v4, "android.permission.READ_EXTERNAL_STORAGE"

    invoke-static {p0, v4}, Landroidx/core/content/ContextCompat;->checkSelfPermission(Landroid/content/Context;Ljava/lang/String;)I

    move-result v5

    if-nez v5, :cond_1

    move v5, v3

    goto :goto_1

    :cond_1
    move v5, v2

    .line 184
    .local v5, "hasReadPermission":Z
    :goto_1
    sget v6, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v7, 0x1e

    if-lt v6, v7, :cond_3

    invoke-static {}, Landroid/os/Environment;->isExternalStorageManager()Z

    move-result v6

    if-eqz v6, :cond_2

    goto :goto_2

    :cond_2
    move v6, v2

    goto :goto_3

    :cond_3
    :goto_2
    move v6, v3

    .line 186
    .local v6, "hasManageStoragePermission":Z
    :goto_3
    if-eqz v1, :cond_4

    if-eqz v5, :cond_4

    if-eqz v6, :cond_4

    .line 187
    return v2

    .line 190
    :cond_4
    if-eqz v1, :cond_5

    if-nez v5, :cond_6

    .line 191
    :cond_5
    const/4 v7, 0x2

    new-array v7, v7, [Ljava/lang/String;

    aput-object v0, v7, v2

    aput-object v4, v7, v3

    move-object v0, v7

    .line 192
    .local v0, "permissions":[Ljava/lang/String;
    invoke-static {p0, v0, v3}, Landroidx/core/app/ActivityCompat;->requestPermissions(Landroid/app/Activity;[Ljava/lang/String;I)V

    .line 195
    .end local v0    # "permissions":[Ljava/lang/String;
    :cond_6
    return v3
.end method

.method private setMenuItemTextColor(Landroid/view/MenuItem;I)V
    .locals 4
    .param p1, "menuItem"    # Landroid/view/MenuItem;
    .param p2, "color"    # I

    .line 346
    new-instance v0, Landroid/text/SpannableString;

    invoke-interface {p1}, Landroid/view/MenuItem;->getTitle()Ljava/lang/CharSequence;

    move-result-object v1

    invoke-direct {v0, v1}, Landroid/text/SpannableString;-><init>(Ljava/lang/CharSequence;)V

    .line 347
    .local v0, "spanString":Landroid/text/SpannableString;
    new-instance v1, Landroid/text/style/ForegroundColorSpan;

    invoke-direct {v1, p2}, Landroid/text/style/ForegroundColorSpan;-><init>(I)V

    const/4 v2, 0x0

    invoke-virtual {v0}, Landroid/text/SpannableString;->length()I

    move-result v3

    invoke-virtual {v0, v1, v2, v3, v2}, Landroid/text/SpannableString;->setSpan(Ljava/lang/Object;III)V

    .line 348
    invoke-interface {p1, v0}, Landroid/view/MenuItem;->setTitle(Ljava/lang/CharSequence;)Landroid/view/MenuItem;

    .line 349
    return-void
.end method

.method private setNavigationViewItemTextColor(Lcom/google/android/material/navigation/NavigationView;I)V
    .locals 4
    .param p1, "navigationView"    # Lcom/google/android/material/navigation/NavigationView;
    .param p2, "color"    # I

    .line 332
    const/4 v0, 0x0

    .local v0, "i":I
    :goto_0
    invoke-virtual {p1}, Lcom/google/android/material/navigation/NavigationView;->getMenu()Landroid/view/Menu;

    move-result-object v1

    invoke-interface {v1}, Landroid/view/Menu;->size()I

    move-result v1

    if-ge v0, v1, :cond_1

    .line 333
    invoke-virtual {p1}, Lcom/google/android/material/navigation/NavigationView;->getMenu()Landroid/view/Menu;

    move-result-object v1

    invoke-interface {v1, v0}, Landroid/view/Menu;->getItem(I)Landroid/view/MenuItem;

    move-result-object v1

    .line 334
    .local v1, "menuItem":Landroid/view/MenuItem;
    invoke-direct {p0, v1, p2}, Lcom/winlator/cmod/MainActivity;->setMenuItemTextColor(Landroid/view/MenuItem;I)V

    .line 336
    invoke-interface {v1}, Landroid/view/MenuItem;->hasSubMenu()Z

    move-result v2

    if-eqz v2, :cond_0

    .line 337
    const/4 v2, 0x0

    .local v2, "j":I
    :goto_1
    invoke-interface {v1}, Landroid/view/MenuItem;->getSubMenu()Landroid/view/SubMenu;

    move-result-object v3

    invoke-interface {v3}, Landroid/view/SubMenu;->size()I

    move-result v3

    if-ge v2, v3, :cond_0

    .line 338
    invoke-interface {v1}, Landroid/view/MenuItem;->getSubMenu()Landroid/view/SubMenu;

    move-result-object v3

    invoke-interface {v3, v2}, Landroid/view/SubMenu;->getItem(I)Landroid/view/MenuItem;

    move-result-object v3

    .line 339
    .local v3, "subMenuItem":Landroid/view/MenuItem;
    invoke-direct {p0, v3, p2}, Lcom/winlator/cmod/MainActivity;->setMenuItemTextColor(Landroid/view/MenuItem;I)V

    .line 337
    .end local v3    # "subMenuItem":Landroid/view/MenuItem;
    add-int/lit8 v2, v2, 0x1

    goto :goto_1

    .line 332
    .end local v1    # "menuItem":Landroid/view/MenuItem;
    .end local v2    # "j":I
    :cond_0
    add-int/lit8 v0, v0, 0x1

    goto :goto_0

    .line 343
    .end local v0    # "i":I
    :cond_1
    return-void
.end method

.method private show(Landroidx/fragment/app/Fragment;Z)V
    .locals 5
    .param p1, "fragment"    # Landroidx/fragment/app/Fragment;
    .param p2, "reverse"    # Z

    .line 262
    invoke-virtual {p0}, Lcom/winlator/cmod/MainActivity;->getSupportFragmentManager()Landroidx/fragment/app/FragmentManager;

    move-result-object v0

    .line 263
    .local v0, "fragmentManager":Landroidx/fragment/app/FragmentManager;
    const v1, 0x7f09008c

    if-eqz p2, :cond_0

    .line 264
    invoke-virtual {v0}, Landroidx/fragment/app/FragmentManager;->beginTransaction()Landroidx/fragment/app/FragmentTransaction;

    move-result-object v2

    .line 265
    const v3, 0x7f010022

    const v4, 0x7f010029

    invoke-virtual {v2, v3, v4}, Landroidx/fragment/app/FragmentTransaction;->setCustomAnimations(II)Landroidx/fragment/app/FragmentTransaction;

    move-result-object v2

    .line 266
    invoke-virtual {v2, v1, p1}, Landroidx/fragment/app/FragmentTransaction;->replace(ILandroidx/fragment/app/Fragment;)Landroidx/fragment/app/FragmentTransaction;

    move-result-object v1

    .line 267
    invoke-virtual {v1}, Landroidx/fragment/app/FragmentTransaction;->commit()I

    goto :goto_0

    .line 269
    :cond_0
    invoke-virtual {v0}, Landroidx/fragment/app/FragmentManager;->beginTransaction()Landroidx/fragment/app/FragmentTransaction;

    move-result-object v2

    .line 270
    const v3, 0x7f010025

    const v4, 0x7f010026

    invoke-virtual {v2, v3, v4}, Landroidx/fragment/app/FragmentTransaction;->setCustomAnimations(II)Landroidx/fragment/app/FragmentTransaction;

    move-result-object v2

    .line 271
    invoke-virtual {v2, v1, p1}, Landroidx/fragment/app/FragmentTransaction;->replace(ILandroidx/fragment/app/Fragment;)Landroidx/fragment/app/FragmentTransaction;

    move-result-object v1

    .line 272
    invoke-virtual {v1}, Landroidx/fragment/app/FragmentTransaction;->commit()I

    .line 275
    :goto_0
    iget-object v1, p0, Lcom/winlator/cmod/MainActivity;->drawerLayout:Landroidx/drawerlayout/widget/DrawerLayout;

    const v2, 0x800003

    invoke-virtual {v1, v2}, Landroidx/drawerlayout/widget/DrawerLayout;->closeDrawer(I)V

    .line 276
    return-void
.end method

.method private showAboutDialog()V
    .locals 10

    .line 279
    const-string v0, "<br />"

    new-instance v1, Lcom/winlator/cmod/contentdialog/ContentDialog;

    const v2, 0x7f0c001c

    invoke-direct {v1, p0, v2}, Lcom/winlator/cmod/contentdialog/ContentDialog;-><init>(Landroid/content/Context;I)V

    .line 280
    .local v1, "dialog":Lcom/winlator/cmod/contentdialog/ContentDialog;
    const v2, 0x7f09009c

    invoke-virtual {v1, v2}, Lcom/winlator/cmod/contentdialog/ContentDialog;->findViewById(I)Landroid/view/View;

    move-result-object v2

    const/16 v3, 0x8

    invoke-virtual {v2, v3}, Landroid/view/View;->setVisibility(I)V

    .line 282
    iget-boolean v2, p0, Lcom/winlator/cmod/MainActivity;->isDarkMode:Z

    if-eqz v2, :cond_0

    .line 283
    invoke-virtual {v1}, Lcom/winlator/cmod/contentdialog/ContentDialog;->getWindow()Landroid/view/Window;

    move-result-object v2

    const v4, 0x7f0800f3

    invoke-virtual {v2, v4}, Landroid/view/Window;->setBackgroundDrawableResource(I)V

    goto :goto_0

    .line 285
    :cond_0
    invoke-virtual {v1}, Lcom/winlator/cmod/contentdialog/ContentDialog;->getWindow()Landroid/view/Window;

    move-result-object v2

    const v4, 0x7f0800f2

    invoke-virtual {v2, v4}, Landroid/view/Window;->setBackgroundDrawableResource(I)V

    .line 289
    :goto_0
    :try_start_0
    invoke-virtual {p0}, Lcom/winlator/cmod/MainActivity;->getPackageManager()Landroid/content/pm/PackageManager;

    move-result-object v2

    invoke-virtual {p0}, Lcom/winlator/cmod/MainActivity;->getPackageName()Ljava/lang/String;

    move-result-object v4

    const/4 v5, 0x0

    invoke-virtual {v2, v4, v5}, Landroid/content/pm/PackageManager;->getPackageInfo(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;

    move-result-object v2

    .line 291
    .local v2, "pInfo":Landroid/content/pm/PackageInfo;
    const v4, 0x7f090186

    invoke-virtual {v1, v4}, Lcom/winlator/cmod/contentdialog/ContentDialog;->findViewById(I)Landroid/view/View;

    move-result-object v4

    check-cast v4, Landroid/widget/TextView;

    .line 292
    .local v4, "tvWebpage":Landroid/widget/TextView;
    const-string v6, "<a href=\"https://www.winlator.org\">winlator.org</a>"

    invoke-static {v6, v5}, Landroid/text/Html;->fromHtml(Ljava/lang/String;I)Landroid/text/Spanned;

    move-result-object v6

    invoke-virtual {v4, v6}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 293
    invoke-static {}, Landroid/text/method/LinkMovementMethod;->getInstance()Landroid/text/method/MovementMethod;

    move-result-object v6

    invoke-virtual {v4, v6}, Landroid/widget/TextView;->setMovementMethod(Landroid/text/method/MovementMethod;)V

    .line 295
    const v6, 0x7f090131

    invoke-virtual {v1, v6}, Lcom/winlator/cmod/contentdialog/ContentDialog;->findViewById(I)Landroid/view/View;

    move-result-object v6

    check-cast v6, Landroid/widget/TextView;

    new-instance v7, Ljava/lang/StringBuilder;

    invoke-direct {v7}, Ljava/lang/StringBuilder;-><init>()V

    const v8, 0x7f10026f

    invoke-virtual {p0, v8}, Lcom/winlator/cmod/MainActivity;->getString(I)Ljava/lang/String;

    move-result-object v8

    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v7

    const-string v8, " "

    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v7

    iget-object v8, v2, Landroid/content/pm/PackageInfo;->versionName:Ljava/lang/String;

    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v7

    invoke-virtual {v7}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v7

    invoke-virtual {v6, v7}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 297
    const/16 v6, 0xf

    new-array v6, v6, [Ljava/lang/CharSequence;

    const-string v7, "Winlator Cmod by coffincolors, me (<a href=\"https://github.com/coffincolors/winlator\">Fork</a>, <a href=\"https://github.com/Pipetto-crypto/winlator\">Fork</a>)"

    aput-object v7, v6, v5

    const-string v7, "Big Picture Mode Music by"

    const/4 v8, 0x1

    aput-object v7, v6, v8

    const-string v7, "Dale Melvin Blevens III (Fumer)"

    const/4 v9, 0x2

    aput-object v7, v6, v9

    const-string v7, "---"

    const/4 v9, 0x3

    aput-object v7, v6, v9

    const-string v7, "Termux Package(<a href=\"https://github.com/termux/termux-packages\">github.com/termux/termux-package</a>)"

    const/4 v9, 0x4

    aput-object v7, v6, v9

    const-string v7, "Wine (<a href=\"https://www.winehq.org\">winehq.org</a>)"

    const/4 v9, 0x5

    aput-object v7, v6, v9

    const-string v7, "Box64 (<a href=\"https://github.com/ptitSeb/box64\">github.com/ptitSeb/box64</a>)"

    const/4 v9, 0x6

    aput-object v7, v6, v9

    const-string v7, "Mesa (Turnip/Zink/Wrapper) (<a href=\"https://github.com/xMeM/mesa/tree/wrapper\">github.com/xMeM/mesa</a>)"

    const/4 v9, 0x7

    aput-object v7, v6, v9

    const-string v7, "DXVK (<a href=\"https://github.com/doitsujin/dxvk\">github.com/doitsujin/dxvk</a>)"

    aput-object v7, v6, v3

    const-string v3, "VKD3D (<a href=\"https://gitlab.winehq.org/wine/vkd3d\">gitlab.winehq.org/wine/vkd3d</a>)"

    const/16 v7, 0x9

    aput-object v3, v6, v7

    const-string v3, "D8VK (<a href=\"https://github.com/AlpyneDreams/d8vk\">github.com/AlpyneDreams/d8vk</a>)"

    const/16 v7, 0xa

    aput-object v3, v6, v7

    const-string v3, "CNC DDraw (<a href=\"https://github.com/FunkyFr3sh/cnc-ddraw\">github.com/FunkyFr3sh/cnc-ddraw</a>)"

    const/16 v7, 0xb

    aput-object v3, v6, v7

    const-string v3, "dxwrapper (<a href=\"https://github.com/elishacloud/dxwrapper\">github.com/elishacloud/dxwrapper</a>)"

    const/16 v7, 0xc

    aput-object v3, v6, v7

    const-string v3, "FEX-Emu (<a href=\"https://github.com/FEX-Emu/FEX\">github.com/FEX-Emu/FEX</a>)"

    const/16 v7, 0xd

    aput-object v3, v6, v7

    const-string v3, "libadrenotools (<a href=\"https://github.com/bylaws/libadrenotools\">github.com/bylaws/libadrenotools</a>)"

    const/16 v7, 0xe

    aput-object v3, v6, v7

    invoke-static {v0, v6}, Ljava/lang/String;->join(Ljava/lang/CharSequence;[Ljava/lang/CharSequence;)Ljava/lang/String;

    move-result-object v3

    .line 315
    .local v3, "creditsAndThirdPartyAppsHTML":Ljava/lang/String;
    const v6, 0x7f09013d

    invoke-virtual {v1, v6}, Lcom/winlator/cmod/contentdialog/ContentDialog;->findViewById(I)Landroid/view/View;

    move-result-object v6

    check-cast v6, Landroid/widget/TextView;

    .line 316
    .local v6, "tvCreditsAndThirdPartyApps":Landroid/widget/TextView;
    invoke-static {v3, v5}, Landroid/text/Html;->fromHtml(Ljava/lang/String;I)Landroid/text/Spanned;

    move-result-object v7

    invoke-virtual {v6, v7}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 317
    invoke-static {}, Landroid/text/method/LinkMovementMethod;->getInstance()Landroid/text/method/MovementMethod;

    move-result-object v7

    invoke-virtual {v6, v7}, Landroid/widget/TextView;->setMovementMethod(Landroid/text/method/MovementMethod;)V

    .line 319
    new-array v7, v8, [Ljava/lang/CharSequence;

    const-string v8, "longjunyu2\'s <a href=\"https://github.com/longjunyu2/winlator/tree/use-glibc-instead-of-proot\">(GLIBC Fork)</a>"

    aput-object v8, v7, v5

    invoke-static {v0, v7}, Ljava/lang/String;->join(Ljava/lang/CharSequence;[Ljava/lang/CharSequence;)Ljava/lang/String;

    move-result-object v0

    .line 321
    .local v0, "glibcExpVersionForkHTML":Ljava/lang/String;
    const v7, 0x7f090159

    invoke-virtual {v1, v7}, Lcom/winlator/cmod/contentdialog/ContentDialog;->findViewById(I)Landroid/view/View;

    move-result-object v7

    check-cast v7, Landroid/widget/TextView;

    .line 322
    .local v7, "tvGlibcExpVersionFork":Landroid/widget/TextView;
    invoke-static {v0, v5}, Landroid/text/Html;->fromHtml(Ljava/lang/String;I)Landroid/text/Spanned;

    move-result-object v5

    invoke-virtual {v7, v5}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 323
    invoke-static {}, Landroid/text/method/LinkMovementMethod;->getInstance()Landroid/text/method/MovementMethod;

    move-result-object v5

    invoke-virtual {v7, v5}, Landroid/widget/TextView;->setMovementMethod(Landroid/text/method/MovementMethod;)V
    :try_end_0
    .catch Landroid/content/pm/PackageManager$NameNotFoundException; {:try_start_0 .. :try_end_0} :catch_0

    .line 326
    .end local v0    # "glibcExpVersionForkHTML":Ljava/lang/String;
    .end local v2    # "pInfo":Landroid/content/pm/PackageInfo;
    .end local v3    # "creditsAndThirdPartyAppsHTML":Ljava/lang/String;
    .end local v4    # "tvWebpage":Landroid/widget/TextView;
    .end local v6    # "tvCreditsAndThirdPartyApps":Landroid/widget/TextView;
    .end local v7    # "tvGlibcExpVersionFork":Landroid/widget/TextView;
    goto :goto_1

    .line 324
    :catch_0
    move-exception v0

    .line 325
    .local v0, "e":Landroid/content/pm/PackageManager$NameNotFoundException;
    invoke-virtual {v0}, Landroid/content/pm/PackageManager$NameNotFoundException;->printStackTrace()V

    .line 328
    .end local v0    # "e":Landroid/content/pm/PackageManager$NameNotFoundException;
    :goto_1
    invoke-virtual {v1}, Lcom/winlator/cmod/contentdialog/ContentDialog;->show()V

    .line 329
    return-void
.end method

.method private showAllFilesAccessDialog()V
    .locals 3

    .line 142
    new-instance v0, Landroid/app/AlertDialog$Builder;

    invoke-direct {v0, p0}, Landroid/app/AlertDialog$Builder;-><init>(Landroid/content/Context;)V

    .line 143
    const-string v1, "All Files Access Required"

    invoke-virtual {v0, v1}, Landroid/app/AlertDialog$Builder;->setTitle(Ljava/lang/CharSequence;)Landroid/app/AlertDialog$Builder;

    move-result-object v0

    .line 144
    const-string v1, "In order to grant access to additional storage devices such as USB storage device, the All Files Access permission must be granted. Press Okay to grant All Files Access in your Android Settings."

    invoke-virtual {v0, v1}, Landroid/app/AlertDialog$Builder;->setMessage(Ljava/lang/CharSequence;)Landroid/app/AlertDialog$Builder;

    move-result-object v0

    new-instance v1, Lcom/winlator/cmod/MainActivity$$ExternalSyntheticLambda0;

    invoke-direct {v1, p0}, Lcom/winlator/cmod/MainActivity$$ExternalSyntheticLambda0;-><init>(Lcom/winlator/cmod/MainActivity;)V

    .line 145
    const-string v2, "Okay"

    invoke-virtual {v0, v2, v1}, Landroid/app/AlertDialog$Builder;->setPositiveButton(Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;

    move-result-object v0

    .line 150
    const-string v1, "Cancel"

    const/4 v2, 0x0

    invoke-virtual {v0, v1, v2}, Landroid/app/AlertDialog$Builder;->setNegativeButton(Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;

    move-result-object v0

    .line 151
    invoke-virtual {v0}, Landroid/app/AlertDialog$Builder;->show()Landroid/app/AlertDialog;

    .line 152
    return-void
.end method


# virtual methods
.method public onActivityResult(IILandroid/content/Intent;)V
    .locals 4
    .param p1, "requestCode"    # I
    .param p2, "resultCode"    # I
    .param p3, "data"    # Landroid/content/Intent;

    .line 353
    invoke-super {p0, p1, p2, p3}, Landroidx/appcompat/app/AppCompatActivity;->onActivityResult(IILandroid/content/Intent;)V

    .line 354
    const/4 v0, 0x5

    if-ne p1, v0, :cond_1

    const/4 v0, -0x1

    if-ne p2, v0, :cond_1

    .line 355
    invoke-virtual {p3}, Landroid/content/Intent;->getData()Landroid/net/Uri;

    move-result-object v0

    const/16 v1, 0x500

    invoke-static {p0, v0, v1}, Lcom/winlator/cmod/core/ImageUtils;->getBitmapFromUri(Landroid/content/Context;Landroid/net/Uri;I)Landroid/graphics/Bitmap;

    move-result-object v0

    .line 356
    .local v0, "bitmap":Landroid/graphics/Bitmap;
    if-nez v0, :cond_0

    return-void

    .line 357
    :cond_0
    invoke-static {p0}, Lcom/winlator/cmod/core/WineThemeManager;->getUserWallpaperFile(Landroid/content/Context;)Ljava/io/File;

    move-result-object v1

    .line 358
    .local v1, "userWallpaperFile":Ljava/io/File;
    sget-object v2, Landroid/graphics/Bitmap$CompressFormat;->PNG:Landroid/graphics/Bitmap$CompressFormat;

    const/16 v3, 0x64

    invoke-static {v0, v1, v2, v3}, Lcom/winlator/cmod/core/ImageUtils;->save(Landroid/graphics/Bitmap;Ljava/io/File;Landroid/graphics/Bitmap$CompressFormat;I)Z

    .line 360
    .end local v0    # "bitmap":Landroid/graphics/Bitmap;
    .end local v1    # "userWallpaperFile":Ljava/io/File;
    :cond_1
    return-void
.end method

.method public onBackPressed()V
    .locals 5

    .line 167
    invoke-virtual {p0}, Lcom/winlator/cmod/MainActivity;->getSupportFragmentManager()Landroidx/fragment/app/FragmentManager;

    move-result-object v0

    .line 168
    .local v0, "fragmentManager":Landroidx/fragment/app/FragmentManager;
    invoke-virtual {v0}, Landroidx/fragment/app/FragmentManager;->getFragments()Ljava/util/List;

    move-result-object v1

    .line 169
    .local v1, "fragments":Ljava/util/List;, "Ljava/util/List<Landroidx/fragment/app/Fragment;>;"
    invoke-interface {v1}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v2

    :goto_0
    invoke-interface {v2}, Ljava/util/Iterator;->hasNext()Z

    move-result v3

    if-eqz v3, :cond_1

    invoke-interface {v2}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v3

    check-cast v3, Landroidx/fragment/app/Fragment;

    .line 170
    .local v3, "fragment":Landroidx/fragment/app/Fragment;
    instance-of v4, v3, Lcom/winlator/cmod/ContainersFragment;

    if-eqz v4, :cond_0

    invoke-virtual {v3}, Landroidx/fragment/app/Fragment;->isVisible()Z

    move-result v4

    if-eqz v4, :cond_0

    .line 171
    invoke-virtual {p0}, Lcom/winlator/cmod/MainActivity;->finish()V

    .line 172
    return-void

    .line 174
    .end local v3    # "fragment":Landroidx/fragment/app/Fragment;
    :cond_0
    goto :goto_0

    .line 175
    :cond_1
    iget-boolean v2, p0, Lcom/winlator/cmod/MainActivity;->editInputControls:Z

    if-nez v2, :cond_2

    .line 176
    new-instance v2, Lcom/winlator/cmod/ContainersFragment;

    invoke-direct {v2}, Lcom/winlator/cmod/ContainersFragment;-><init>()V

    const/4 v3, 0x1

    invoke-direct {p0, v2, v3}, Lcom/winlator/cmod/MainActivity;->show(Landroidx/fragment/app/Fragment;Z)V

    goto :goto_1

    .line 178
    :cond_2
    invoke-super {p0}, Landroidx/appcompat/app/AppCompatActivity;->onBackPressed()V

    .line 179
    :goto_1
    return-void
.end method

.method protected onCreate(Landroid/os/Bundle;)V
    .locals 13
    .param p1, "savedInstanceState"    # Landroid/os/Bundle;

    .line 68
    invoke-super {p0, p1}, Landroidx/appcompat/app/AppCompatActivity;->onCreate(Landroid/os/Bundle;)V

    .line 70
    invoke-static {p0}, Landroidx/preference/PreferenceManager;->getDefaultSharedPreferences(Landroid/content/Context;)Landroid/content/SharedPreferences;

    move-result-object v0

    .line 72
    .local v0, "sharedPreferences":Landroid/content/SharedPreferences;
    const-string v1, "enable_big_picture_mode"

    const/4 v2, 0x0

    invoke-interface {v0, v1, v2}, Landroid/content/SharedPreferences;->getBoolean(Ljava/lang/String;Z)Z

    move-result v1

    .line 74
    .local v1, "isBigPictureModeEnabled":Z
    if-eqz v1, :cond_0

    .line 75
    new-instance v3, Landroid/content/Intent;

    const-class v4, Lcom/winlator/cmod/BigPictureActivity;

    invoke-direct {v3, p0, v4}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V

    .line 76
    .local v3, "intent":Landroid/content/Intent;
    invoke-virtual {p0, v3}, Lcom/winlator/cmod/MainActivity;->startActivity(Landroid/content/Intent;)V

    .line 79
    .end local v3    # "intent":Landroid/content/Intent;
    :cond_0
    invoke-static {p0}, Landroidx/preference/PreferenceManager;->getDefaultSharedPreferences(Landroid/content/Context;)Landroid/content/SharedPreferences;

    move-result-object v0

    .line 80
    const-string v3, "dark_mode"

    invoke-interface {v0, v3, v2}, Landroid/content/SharedPreferences;->getBoolean(Ljava/lang/String;Z)Z

    move-result v3

    iput-boolean v3, p0, Lcom/winlator/cmod/MainActivity;->isDarkMode:Z

    .line 82
    iget-boolean v3, p0, Lcom/winlator/cmod/MainActivity;->isDarkMode:Z

    if-eqz v3, :cond_1

    .line 83
    const v3, 0x7f110009

    invoke-virtual {p0, v3}, Lcom/winlator/cmod/MainActivity;->setTheme(I)V

    goto :goto_0

    .line 85
    :cond_1
    const v3, 0x7f110008

    invoke-virtual {p0, v3}, Lcom/winlator/cmod/MainActivity;->setTheme(I)V

    .line 88
    :goto_0
    const v3, 0x7f0c005f

    invoke-virtual {p0, v3}, Lcom/winlator/cmod/MainActivity;->setContentView(I)V

    .line 90
    const v3, 0x7f09007e

    invoke-virtual {p0, v3}, Lcom/winlator/cmod/MainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v3

    check-cast v3, Landroidx/drawerlayout/widget/DrawerLayout;

    iput-object v3, p0, Lcom/winlator/cmod/MainActivity;->drawerLayout:Landroidx/drawerlayout/widget/DrawerLayout;

    .line 91
    const v3, 0x7f0900c7

    invoke-virtual {p0, v3}, Lcom/winlator/cmod/MainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v3

    check-cast v3, Lcom/google/android/material/navigation/NavigationView;

    .line 92
    .local v3, "navigationView":Lcom/google/android/material/navigation/NavigationView;
    invoke-virtual {v3, p0}, Lcom/google/android/material/navigation/NavigationView;->setNavigationItemSelectedListener(Lcom/google/android/material/navigation/NavigationView$OnNavigationItemSelectedListener;)V

    .line 94
    const v4, 0x7f09018d

    invoke-virtual {p0, v4}, Lcom/winlator/cmod/MainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v4

    check-cast v4, Landroidx/appcompat/widget/Toolbar;

    invoke-virtual {p0, v4}, Lcom/winlator/cmod/MainActivity;->setSupportActionBar(Landroidx/appcompat/widget/Toolbar;)V

    .line 95
    invoke-virtual {p0}, Lcom/winlator/cmod/MainActivity;->getSupportActionBar()Landroidx/appcompat/app/ActionBar;

    move-result-object v4

    .line 96
    .local v4, "actionBar":Landroidx/appcompat/app/ActionBar;
    const v5, 0x7f080118

    const/4 v6, 0x1

    if-eqz v4, :cond_2

    .line 97
    invoke-virtual {v4, v6}, Landroidx/appcompat/app/ActionBar;->setDisplayHomeAsUpEnabled(Z)V

    .line 98
    invoke-virtual {v4, v5}, Landroidx/appcompat/app/ActionBar;->setHomeAsUpIndicator(I)V

    .line 101
    :cond_2
    iget-boolean v7, p0, Lcom/winlator/cmod/MainActivity;->isDarkMode:Z

    if-eqz v7, :cond_3

    const/4 v7, -0x1

    goto :goto_1

    :cond_3
    const/high16 v7, -0x1000000

    .line 102
    .local v7, "textColor":I
    :goto_1
    invoke-direct {p0, v3, v7}, Lcom/winlator/cmod/MainActivity;->setNavigationViewItemTextColor(Lcom/google/android/material/navigation/NavigationView;I)V

    .line 104
    new-instance v8, Ljava/io/File;

    sget-object v9, Lcom/winlator/cmod/SettingsFragment;->DEFAULT_WINLATOR_PATH:Ljava/lang/String;

    invoke-direct {v8, v9}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 105
    .local v8, "winlatorDir":Ljava/io/File;
    invoke-virtual {v8}, Ljava/io/File;->exists()Z

    move-result v9

    if-nez v9, :cond_4

    .line 106
    invoke-virtual {v8}, Ljava/io/File;->mkdirs()Z

    .line 108
    :cond_4
    new-instance v9, Lcom/winlator/cmod/container/ContainerManager;

    invoke-direct {v9, p0}, Lcom/winlator/cmod/container/ContainerManager;-><init>(Landroid/content/Context;)V

    iput-object v9, p0, Lcom/winlator/cmod/MainActivity;->containerManager:Lcom/winlator/cmod/container/ContainerManager;

    .line 110
    invoke-virtual {p0}, Lcom/winlator/cmod/MainActivity;->getIntent()Landroid/content/Intent;

    move-result-object v9

    .line 111
    .local v9, "intent":Landroid/content/Intent;
    const-string v10, "edit_input_controls"

    invoke-virtual {v9, v10, v2}, Landroid/content/Intent;->getBooleanExtra(Ljava/lang/String;Z)Z

    move-result v10

    iput-boolean v10, p0, Lcom/winlator/cmod/MainActivity;->editInputControls:Z

    .line 112
    iget-boolean v10, p0, Lcom/winlator/cmod/MainActivity;->editInputControls:Z

    if-eqz v10, :cond_5

    .line 113
    const-string v5, "selected_profile_id"

    invoke-virtual {v9, v5, v2}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result v2

    iput v2, p0, Lcom/winlator/cmod/MainActivity;->selectedProfileId:I

    .line 114
    const v2, 0x7f080116

    invoke-virtual {v4, v2}, Landroidx/appcompat/app/ActionBar;->setHomeAsUpIndicator(I)V

    .line 115
    invoke-virtual {v3}, Lcom/google/android/material/navigation/NavigationView;->getMenu()Landroid/view/Menu;

    move-result-object v2

    const v5, 0x7f090271

    invoke-interface {v2, v5}, Landroid/view/Menu;->findItem(I)Landroid/view/MenuItem;

    move-result-object v2

    invoke-virtual {p0, v2}, Lcom/winlator/cmod/MainActivity;->onNavigationItemSelected(Landroid/view/MenuItem;)Z

    .line 116
    invoke-virtual {v3, v5}, Lcom/google/android/material/navigation/NavigationView;->setCheckedItem(I)V

    goto :goto_3

    .line 118
    :cond_5
    const-string v10, "selected_menu_item_id"

    invoke-virtual {v9, v10, v2}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result v10

    .line 119
    .local v10, "selectedMenuItemId":I
    if-lez v10, :cond_6

    move v11, v10

    goto :goto_2

    :cond_6
    const v11, 0x7f09026e

    .line 121
    .local v11, "menuItemId":I
    :goto_2
    invoke-virtual {v4, v5}, Landroidx/appcompat/app/ActionBar;->setHomeAsUpIndicator(I)V

    .line 122
    invoke-virtual {v3}, Lcom/google/android/material/navigation/NavigationView;->getMenu()Landroid/view/Menu;

    move-result-object v5

    invoke-interface {v5, v11}, Landroid/view/Menu;->findItem(I)Landroid/view/MenuItem;

    move-result-object v5

    invoke-virtual {p0, v5}, Lcom/winlator/cmod/MainActivity;->onNavigationItemSelected(Landroid/view/MenuItem;)Z

    .line 123
    invoke-virtual {v3, v11}, Lcom/google/android/material/navigation/NavigationView;->setCheckedItem(I)V

    .line 125
    invoke-direct {p0}, Lcom/winlator/cmod/MainActivity;->requestAppPermissions()Z

    move-result v5

    if-nez v5, :cond_7

    .line 126
    invoke-static {p0}, Lcom/winlator/cmod/xenvironment/ImageFsInstaller;->installIfNeeded(Lcom/winlator/cmod/MainActivity;)V

    .line 129
    :cond_7
    sget v5, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v12, 0x1e

    if-lt v5, v12, :cond_8

    invoke-static {}, Landroid/os/Environment;->isExternalStorageManager()Z

    move-result v5

    if-nez v5, :cond_8

    .line 130
    invoke-direct {p0}, Lcom/winlator/cmod/MainActivity;->showAllFilesAccessDialog()V

    .line 133
    :cond_8
    sget v5, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v12, 0x21

    if-lt v5, v12, :cond_9

    .line 134
    const-string v5, "android.permission.POST_NOTIFICATIONS"

    invoke-static {p0, v5}, Landroidx/core/app/ActivityCompat;->checkSelfPermission(Landroid/content/Context;Ljava/lang/String;)I

    move-result v12

    if-eqz v12, :cond_9

    .line 135
    new-array v6, v6, [Ljava/lang/String;

    aput-object v5, v6, v2

    invoke-virtual {p0, v6, v2}, Lcom/winlator/cmod/MainActivity;->requestPermissions([Ljava/lang/String;I)V

    .line 139
    .end local v10    # "selectedMenuItemId":I
    .end local v11    # "menuItemId":I
    :cond_9
    :goto_3
    return-void
.end method

.method public onNavigationItemSelected(Landroid/view/MenuItem;)Z
    .locals 5
    .param p1, "item"    # Landroid/view/MenuItem;

    .line 227
    invoke-virtual {p0}, Lcom/winlator/cmod/MainActivity;->getSupportFragmentManager()Landroidx/fragment/app/FragmentManager;

    move-result-object v0

    .line 228
    .local v0, "fragmentManager":Landroidx/fragment/app/FragmentManager;
    invoke-virtual {v0}, Landroidx/fragment/app/FragmentManager;->getBackStackEntryCount()I

    move-result v1

    const/4 v2, 0x1

    if-lez v1, :cond_0

    .line 229
    const/4 v1, 0x0

    invoke-virtual {v0, v1, v2}, Landroidx/fragment/app/FragmentManager;->popBackStack(Ljava/lang/String;I)V

    .line 232
    :cond_0
    invoke-interface {p1}, Landroid/view/MenuItem;->getItemId()I

    move-result v1

    const/4 v3, 0x0

    # Game Stores: GOG (0x7f09038a)
    const v4, 0x7f09038a
    if-eq v1, v4, :start_gog

    # Game Stores: Epic Games (0x7f090389)
    const v4, 0x7f090389
    if-eq v1, v4, :start_epic

    # Game Stores: Amazon Games (0x7f090388)
    const v4, 0x7f090388
    if-eq v1, v4, :start_amazon

    # Game Stores: Steam (0x7f09038b)
    const v4, 0x7f09038b
    if-eq v1, v4, :start_steam

    packed-switch v1, :pswitch_data_0

    goto :goto_0

    :start_gog
    new-instance v1, Landroid/content/Intent;
    const-class v4, Lcom/winlator/cmod/store/GogMainActivity;
    invoke-direct {v1, p0, v4}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V
    invoke-virtual {p0, v1}, Lcom/winlator/cmod/MainActivity;->startActivity(Landroid/content/Intent;)V
    goto :goto_0

    :start_epic
    new-instance v1, Landroid/content/Intent;
    const-class v4, Lcom/winlator/cmod/store/EpicMainActivity;
    invoke-direct {v1, p0, v4}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V
    invoke-virtual {p0, v1}, Lcom/winlator/cmod/MainActivity;->startActivity(Landroid/content/Intent;)V
    goto :goto_0

    :start_amazon
    new-instance v1, Landroid/content/Intent;
    const-class v4, Lcom/winlator/cmod/store/AmazonMainActivity;
    invoke-direct {v1, p0, v4}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V
    invoke-virtual {p0, v1}, Lcom/winlator/cmod/MainActivity;->startActivity(Landroid/content/Intent;)V
    goto :goto_0

    :start_steam
    new-instance v1, Landroid/content/Intent;
    const-class v4, Lcom/winlator/cmod/store/SteamMainActivity;
    invoke-direct {v1, p0, v4}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V
    invoke-virtual {p0, v1}, Lcom/winlator/cmod/MainActivity;->startActivity(Landroid/content/Intent;)V
    goto :goto_0

    .line 234
    :pswitch_0
    new-instance v1, Lcom/winlator/cmod/ShortcutsFragment;

    invoke-direct {v1}, Lcom/winlator/cmod/ShortcutsFragment;-><init>()V

    invoke-direct {p0, v1, v3}, Lcom/winlator/cmod/MainActivity;->show(Landroidx/fragment/app/Fragment;Z)V

    .line 235
    goto :goto_0

    .line 249
    :pswitch_1
    new-instance v1, Lcom/winlator/cmod/SettingsFragment;

    invoke-direct {v1}, Lcom/winlator/cmod/SettingsFragment;-><init>()V

    invoke-direct {p0, v1, v3}, Lcom/winlator/cmod/MainActivity;->show(Landroidx/fragment/app/Fragment;Z)V

    .line 250
    goto :goto_0

    .line 240
    :pswitch_2
    new-instance v1, Lcom/winlator/cmod/InputControlsFragment;

    iget v4, p0, Lcom/winlator/cmod/MainActivity;->selectedProfileId:I

    invoke-direct {v1, v4}, Lcom/winlator/cmod/InputControlsFragment;-><init>(I)V

    invoke-direct {p0, v1, v3}, Lcom/winlator/cmod/MainActivity;->show(Landroidx/fragment/app/Fragment;Z)V

    .line 241
    goto :goto_0

    .line 255
    :pswitch_3
    new-instance v1, Lcom/winlator/cmod/FileManagerFragment;

    invoke-direct {v1}, Lcom/winlator/cmod/FileManagerFragment;-><init>()V

    invoke-direct {p0, v1, v3}, Lcom/winlator/cmod/MainActivity;->show(Landroidx/fragment/app/Fragment;Z)V

    goto :goto_0

    .line 243
    :pswitch_4
    new-instance v1, Lcom/winlator/cmod/ContentsFragment;

    invoke-direct {v1}, Lcom/winlator/cmod/ContentsFragment;-><init>()V

    invoke-direct {p0, v1, v3}, Lcom/winlator/cmod/MainActivity;->show(Landroidx/fragment/app/Fragment;Z)V

    .line 244
    goto :goto_0

    .line 237
    :pswitch_5
    new-instance v1, Lcom/winlator/cmod/ContainersFragment;

    invoke-direct {v1}, Lcom/winlator/cmod/ContainersFragment;-><init>()V

    invoke-direct {p0, v1, v3}, Lcom/winlator/cmod/MainActivity;->show(Landroidx/fragment/app/Fragment;Z)V

    .line 238
    goto :goto_0

    .line 246
    :pswitch_6
    new-instance v1, Lcom/winlator/cmod/AdrenotoolsFragment;

    invoke-direct {v1}, Lcom/winlator/cmod/AdrenotoolsFragment;-><init>()V

    invoke-direct {p0, v1, v3}, Lcom/winlator/cmod/MainActivity;->show(Landroidx/fragment/app/Fragment;Z)V

    .line 247
    goto :goto_0

    .line 252
    :pswitch_7
    invoke-direct {p0}, Lcom/winlator/cmod/MainActivity;->showAboutDialog()V

    .line 253
    nop

    .line 258
    :goto_0
    return v2

    :pswitch_data_0
    .packed-switch 0x7f09026c
        :pswitch_7
        :pswitch_6
        :pswitch_5
        :pswitch_4
        :pswitch_3
        :pswitch_2
        :pswitch_1
        :pswitch_0
    .end packed-switch
.end method

.method public onOptionsItemSelected(Landroid/view/MenuItem;)Z
    .locals 3
    .param p1, "menuItem"    # Landroid/view/MenuItem;

    .line 200
    invoke-interface {p1}, Landroid/view/MenuItem;->getItemId()I

    move-result v0

    const v1, 0x102002c

    if-ne v0, v1, :cond_2

    .line 201
    iget-boolean v0, p0, Lcom/winlator/cmod/MainActivity;->editInputControls:Z

    const/4 v1, 0x1

    if-eqz v0, :cond_0

    .line 202
    invoke-virtual {p0}, Lcom/winlator/cmod/MainActivity;->onBackPressed()V

    .line 203
    return v1

    .line 206
    :cond_0
    iget-object v0, p0, Lcom/winlator/cmod/MainActivity;->drawerLayout:Landroidx/drawerlayout/widget/DrawerLayout;

    const v2, 0x800003

    invoke-virtual {v0, v2}, Landroidx/drawerlayout/widget/DrawerLayout;->isDrawerOpen(I)Z

    move-result v0

    if-eqz v0, :cond_1

    .line 207
    iget-object v0, p0, Lcom/winlator/cmod/MainActivity;->drawerLayout:Landroidx/drawerlayout/widget/DrawerLayout;

    invoke-virtual {v0, v2}, Landroidx/drawerlayout/widget/DrawerLayout;->closeDrawer(I)V

    goto :goto_0

    .line 209
    :cond_1
    iget-object v0, p0, Lcom/winlator/cmod/MainActivity;->drawerLayout:Landroidx/drawerlayout/widget/DrawerLayout;

    invoke-virtual {v0, v2}, Landroidx/drawerlayout/widget/DrawerLayout;->openDrawer(I)V

    .line 211
    :goto_0
    return v1

    .line 213
    :cond_2
    invoke-super {p0, p1}, Landroidx/appcompat/app/AppCompatActivity;->onOptionsItemSelected(Landroid/view/MenuItem;)Z

    move-result v0

    return v0
.end method

.method public onRequestPermissionsResult(I[Ljava/lang/String;[I)V
    .locals 1
    .param p1, "requestCode"    # I
    .param p2, "permissions"    # [Ljava/lang/String;
    .param p3, "grantResults"    # [I

    .line 156
    invoke-super {p0, p1, p2, p3}, Landroidx/appcompat/app/AppCompatActivity;->onRequestPermissionsResult(I[Ljava/lang/String;[I)V

    .line 157
    const/4 v0, 0x1

    if-ne p1, v0, :cond_1

    .line 158
    array-length v0, p3

    if-lez v0, :cond_0

    const/4 v0, 0x0

    aget v0, p3, v0

    if-nez v0, :cond_0

    .line 159
    invoke-static {p0}, Lcom/winlator/cmod/xenvironment/ImageFsInstaller;->installIfNeeded(Lcom/winlator/cmod/MainActivity;)V

    goto :goto_0

    .line 161
    :cond_0
    invoke-virtual {p0}, Lcom/winlator/cmod/MainActivity;->finish()V

    .line 163
    :cond_1
    :goto_0
    return-void
.end method

.method public toggleDrawer()V
    .locals 2

    .line 218
    iget-object v0, p0, Lcom/winlator/cmod/MainActivity;->drawerLayout:Landroidx/drawerlayout/widget/DrawerLayout;

    const v1, 0x800003

    invoke-virtual {v0, v1}, Landroidx/drawerlayout/widget/DrawerLayout;->isDrawerOpen(I)Z

    move-result v0

    if-eqz v0, :cond_0

    .line 219
    iget-object v0, p0, Lcom/winlator/cmod/MainActivity;->drawerLayout:Landroidx/drawerlayout/widget/DrawerLayout;

    invoke-virtual {v0, v1}, Landroidx/drawerlayout/widget/DrawerLayout;->closeDrawer(I)V

    goto :goto_0

    .line 221
    :cond_0
    iget-object v0, p0, Lcom/winlator/cmod/MainActivity;->drawerLayout:Landroidx/drawerlayout/widget/DrawerLayout;

    invoke-virtual {v0, v1}, Landroidx/drawerlayout/widget/DrawerLayout;->openDrawer(I)V

    .line 223
    :goto_0
    return-void
.end method
