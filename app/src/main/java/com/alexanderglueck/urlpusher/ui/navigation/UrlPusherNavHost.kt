package com.alexanderglueck.urlpusher.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.alexanderglueck.urlpusher.ui.SessionState
import com.alexanderglueck.urlpusher.ui.SessionViewModel
import com.alexanderglueck.urlpusher.ui.auth.SignInScreen
import com.alexanderglueck.urlpusher.ui.auth.SignUpScreen
import com.alexanderglueck.urlpusher.ui.auth.WelcomeScreen
import com.alexanderglueck.urlpusher.ui.devices.DevicePickerScreen
import com.alexanderglueck.urlpusher.ui.home.HomeScreen
import com.alexanderglueck.urlpusher.ui.pair.QrPairScreen

private val AUTH_ROUTES = setOf(Routes.WELCOME, Routes.SIGN_IN, Routes.SIGN_UP, Routes.QR_PAIR)
private val DEVICE_ROUTES = setOf(Routes.DEVICE_PICKER, Routes.QR_PAIR)
private val HOME_ROUTES = setOf(Routes.HOME)

@Composable
fun UrlPusherNavHost(
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    sharedUrl: String?,
    onSharedUrlConsumed: () -> Unit,
) {
    val session by sessionViewModel.state.collectAsState()

    // Lock in the start destination only after the persisted session resolves.
    // The system splash (set up in MainActivity) stays on top until then, so the
    // user never sees the welcome/sign-in screen flash for one frame on launch.
    var startDestination by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(session) {
        if (startDestination == null) {
            startDestination = when (session) {
                SessionState.Loading -> null
                SessionState.SignedOut -> Routes.WELCOME
                SessionState.NeedsDevice -> Routes.DEVICE_PICKER
                SessionState.Ready -> Routes.HOME
            }
        }
    }
    val resolved = startDestination ?: return

    // React to subsequent session changes (sign in/out, device pairing).
    LaunchedEffect(session) {
        val current = navController.currentDestination?.route ?: return@LaunchedEffect
        val (allowed, fallback) = when (session) {
            SessionState.Loading -> return@LaunchedEffect
            SessionState.SignedOut -> AUTH_ROUTES to Routes.WELCOME
            SessionState.NeedsDevice -> DEVICE_ROUTES to Routes.DEVICE_PICKER
            SessionState.Ready -> HOME_ROUTES to Routes.HOME
        }
        if (current !in allowed) {
            navController.navigate(fallback) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = resolved,
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onSignIn = { navController.navigate(Routes.SIGN_IN) },
                onSignUp = { navController.navigate(Routes.SIGN_UP) },
                onScanQr = { navController.navigate(Routes.QR_PAIR) },
            )
        }
        composable(Routes.SIGN_IN) {
            SignInScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SIGN_UP) {
            SignUpScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.QR_PAIR) {
            QrPairScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.DEVICE_PICKER) {
            DevicePickerScreen(
                onScanQrInstead = { navController.navigate(Routes.QR_PAIR) },
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                sharedUrl = sharedUrl,
                onSharedUrlConsumed = onSharedUrlConsumed,
            )
        }
    }
}
