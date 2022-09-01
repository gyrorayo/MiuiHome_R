package com.yuk.miuiHomeR

import android.content.Context
import android.os.Process
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.Log.logexIfThrow
import com.yuk.miuiHomeR.hook.*
import com.yuk.miuiHomeR.utils.Helpers
import com.yuk.miuiHomeR.utils.PrefsMap
import com.yuk.miuiHomeR.utils.PrefsUtils
import com.yuk.miuiHomeR.utils.ktx.hookBeforeMethod
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

private const val PACKAGE_NAME_HOOKED = "com.miui.home"
private const val TAG = "MiuiHomeR"
var mPrefsMap = PrefsMap<String, Any>()

class MainHook : IXposedHookLoadPackage, IXposedHookZygoteInit /* Optional */ {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            PACKAGE_NAME_HOOKED -> {
                EzXHelperInit.initHandleLoadPackage(lpparam)
                EzXHelperInit.setLogTag(TAG)
                EzXHelperInit.setToastTag(TAG)
                "com.miui.home.launcher.Application".hookBeforeMethod(
                    "attachBaseContext", Context::class.java
                ) {
                    EzXHelperInit.initAppContext(it.args[0] as Context)
                    initHooks(
                        ResourcesHook,
                    )
                }
                "com.miui.home.launcher.Application".hookBeforeMethod(
                    "attachBaseContext", Context::class.java
                ) {
                    initHooks(
                        AllowMoveAllWidgetToMinus,
                        AlwaysBlurWallpaper,
                        AlwaysShowMiuiWidget,
                        AlwaysShowStatusClock,
                        DisableRecentViewWallpaperDarken,
                        HideStatusBarWhenEnterRecent,
                        AnimDurationRatio,
                        DoubleTapToSleep,
                        HideWidgetTitles,
                        BlurLevel,
                        BlurRadius,
                        FolderColumnsCount,
                        EnableBlurWhenOpenFolder,
                        EnableFolderIconBlur,
                        SetDeviceLevel,
                        DockBlur,
                        ShortcutBlur,
                        UnlockHotseatIcon,
                        TaskViewHorizontal,
                        TaskViewVertical,
                        IconTitleSize
                    )
                }
            }
            else -> return
        }
    }

    // Optional
    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelperInit.initZygote(startupParam)

        if (mPrefsMap.size == 0) {
            var mXSharedPreferences: XSharedPreferences? = null
            try {
                mXSharedPreferences =
                    XSharedPreferences(Helpers.mAppModulePkg, PrefsUtils.mPrefsName)
                mXSharedPreferences.makeWorldReadable()
            } catch (t: Throwable) {
                XposedBridge.log(t)
            }
            val allPrefs = mXSharedPreferences?.all
            if (allPrefs == null || allPrefs.isEmpty()) {
                XposedBridge.log("[UID " + Process.myUid() + "] Cannot read module's SharedPreferences, some mods might not work!")
            } else {
                mPrefsMap.putAll(allPrefs)
            }
        }
    }

    private fun initHooks(vararg hook: BaseHook) {
        hook.forEach {
            runCatching {
                if (it.isInit) return@forEach
                it.init()
                it.isInit = true
                Log.i("Inited hook: ${it.javaClass.simpleName}")
            }.logexIfThrow("Failed init hook: ${it.javaClass.simpleName}")
        }
    }


}
