package com.grocemaxxer.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.grocemaxxer.app.App
import com.grocemaxxer.app.GrocemaxxerAndroidContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GrocemaxxerAndroidContext.appContext = applicationContext
        setContent {
            App()
        }
    }
}
