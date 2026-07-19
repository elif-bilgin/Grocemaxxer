package com.grocemaxxer.app

import android.app.Activity
import android.content.Context
import java.io.File

/**
 * Set from MainActivity before the first composition, so expect/actual
 * platform code can reach the app's files directory (and, for Credential
 * Manager sign-in, the current Activity) without threading a Context
 * through common code.
 */
object GrocemaxxerAndroidContext {
    lateinit var appContext: Context
    var currentActivity: Activity? = null
}

actual fun groceryDataStorePath(): String =
    File(GrocemaxxerAndroidContext.appContext.filesDir, "grocemaxxer.preferences_pb").absolutePath
