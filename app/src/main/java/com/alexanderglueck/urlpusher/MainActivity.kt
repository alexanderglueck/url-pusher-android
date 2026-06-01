package com.alexanderglueck.urlpusher

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.alexanderglueck.urlpusher.ui.navigation.UrlPusherNavHost
import com.alexanderglueck.urlpusher.ui.theme.UrlPusherTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialShare = intent?.extractSharedUrl()

        setContent {
            UrlPusherTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    var sharedUrl by remember { mutableStateOf(initialShare) }

                    UrlPusherNavHost(
                        navController = navController,
                        sharedUrl = sharedUrl,
                        onSharedUrlConsumed = { sharedUrl = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun Intent.extractSharedUrl(): String? {
        if (action != Intent.ACTION_SEND || type != "text/plain") return null
        val text = getStringExtra(Intent.EXTRA_TEXT)?.trim() ?: return null
        return text.takeIf { Patterns.WEB_URL.matcher(it).matches() }
    }
}
