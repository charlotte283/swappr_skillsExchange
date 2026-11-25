package com.skillexchange.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.skillexchange.app.ui.SkillExchangeApp
import com.skillexchange.app.ui.SkillExchangeTheme
import com.skillexchange.app.ui.SplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkillExchangeTheme {
                var showSplash by remember { mutableStateOf(true) }
                if (showSplash) {
                    SplashScreen(onTimeout = { showSplash = false })
                } else {
                    SkillExchangeApp()
                }
            }
        }
    }
}
