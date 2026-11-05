package com.grocemaxxer.app

actual fun appVersionCode(): Int {
    val context = GrocemaxxerAndroidContext.appContext
    return runCatching {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0).versionCode
    }.getOrDefault(0)
}
