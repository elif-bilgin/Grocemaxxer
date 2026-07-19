package com.grocemaxxer.app

import android.content.Intent

actual fun shareListText(text: String) {
    val context = GrocemaxxerAndroidContext.appContext
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(send, "Share grocery list").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
}
