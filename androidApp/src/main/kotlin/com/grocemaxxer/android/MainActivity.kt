package com.grocemaxxer.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.grocemaxxer.app.App
import com.grocemaxxer.app.GrocemaxxerAndroidContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Draw behind the system bars. Android 15+ enforces this for apps
        // targeting SDK 35 and above regardless; calling it explicitly gives
        // older releases the same layout. Compose applies the resulting insets
        // once, at the top of App().
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        GrocemaxxerAndroidContext.appContext = applicationContext
        setContent {
            App()
        }
    }
}
