package com.alexanderglueck.urlpusher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.alexanderglueck.urlpusher.ui.SessionState
import com.alexanderglueck.urlpusher.ui.SessionViewModel
import com.alexanderglueck.urlpusher.ui.navigation.UrlPusherNavHost
import com.alexanderglueck.urlpusher.ui.theme.UrlPusherTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val sessionViewModel: SessionViewModel by viewModels()

    // Holds the URL shared into the app. Because the activity is singleTask, a
    // share while the app is already running arrives via onNewIntent (not a fresh
    // onCreate), so this state is updated from both paths and read in setContent.
    private val sharedUrl = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition {
            sessionViewModel.state.value == SessionState.Loading
        }
        super.onCreate(savedInstanceState)
        intent?.extractSharedUrl()?.let { sharedUrl.value = it }

        setContent {
            UrlPusherTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NotificationPermissionGate()

                    val navController = rememberNavController()

                    UrlPusherNavHost(
                        navController = navController,
                        sessionViewModel = sessionViewModel,
                        sharedUrl = sharedUrl.value,
                        onSharedUrlConsumed = { sharedUrl.value = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.extractSharedUrl()?.let { sharedUrl.value = it }
    }

    private fun Intent.extractSharedUrl(): String? {
        if (action != Intent.ACTION_SEND || type != "text/plain") return null
        val text = getStringExtra(Intent.EXTRA_TEXT)?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        // The payload may be a bare URL or a title followed by a URL (common from
        // browsers), so fall back to extracting the first URL found within the text.
        if (Patterns.WEB_URL.matcher(text).matches()) return text
        val matcher = Patterns.WEB_URL.matcher(text)
        return if (matcher.find()) text.substring(matcher.start(), matcher.end()) else null
    }
}

@androidx.compose.runtime.Composable
private fun NotificationPermissionGate() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { /* result ignored — the user's choice is reflected by the next FCM delivery */ }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
