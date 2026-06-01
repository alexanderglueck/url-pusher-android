package com.alexanderglueck.urlpusher.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
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
    sharedUrl: String?,
    onSharedUrlConsumed: () -> Unit,
) {
    val sessionViewModel: SessionViewModel = hiltViewModel()
    val session by sessionViewModel.state.collectAsState()

    LaunchedEffect(session) {
        val current = navController.currentDestination?.route
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
        startDestination = Routes.WELCOME,
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
