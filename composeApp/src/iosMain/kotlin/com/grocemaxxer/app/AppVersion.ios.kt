package com.grocemaxxer.app

import platform.Foundation.NSBundle

actual fun appVersionCode(): Int =
    (NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? String)
        ?.toIntOrNull()
        ?: 1
