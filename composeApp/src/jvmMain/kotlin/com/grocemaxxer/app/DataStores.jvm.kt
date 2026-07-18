package com.grocemaxxer.app

import java.io.File

actual fun groceryDataStorePath(): String {
    val dir = File(System.getProperty("user.home"), ".grocemaxxer")
    if (!dir.exists()) dir.mkdirs()
    return File(dir, "grocemaxxer.preferences_pb").absolutePath
}
