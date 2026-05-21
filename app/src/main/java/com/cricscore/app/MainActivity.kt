package com.cricscore.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cricscore.app.ui.navigation.CricScoreNavHost
import com.cricscore.app.ui.theme.CricScoreTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CricScoreTheme {
                CricScoreNavHost()
            }
        }
    }
}
