package com.grocemaxxer.app

import android.content.Context
import java.io.File

/**
 * Set from MainActivity (or an Application subclass) before the first
 * composition, so the expect/actual path function can reach the app's
 * files directory without threading a Context through common code.
 */
object GrocemaxxerAndroidContext {
    lateinit var appContext: Context
}

actual fun groceryDataStorePath(): String =
    File(GrocemaxxerAndroidContext.appContext.filesDir, "grocemaxxer.preferences_pb").absolutePath
