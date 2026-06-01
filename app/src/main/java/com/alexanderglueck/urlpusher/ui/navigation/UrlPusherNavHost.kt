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
import com.alexanderglueck.urlpusher.ui.auth.LoginScreen
import com.alexanderglueck.urlpusher.ui.devices.DevicePickerScreen
import com.alexanderglueck.urlpusher.ui.home.HomeScreen

@Composable
fun UrlPusherNavHost(
    navController: NavHostController,
    sharedUrl: String?,
    onSharedUrlConsumed: () -> Unit,
) {
    val sessionViewModel: SessionViewModel = hiltViewModel()
    val session by sessionViewModel.state.collectAsState()

    LaunchedEffect(session) {
        val target = when (session) {
            SessionState.Loading -> null
            SessionState.SignedOut -> Routes.LOGIN
            SessionState.NeedsDevice -> Routes.DEVICE_PICKER
            SessionState.Ready -> Routes.HOME
        }
        if (target != null && navController.currentDestination?.route != target) {
            navController.navigate(target) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
    ) {
        composable(Routes.LOGIN) {
            LoginScreen()
        }
        composable(Routes.DEVICE_PICKER) {
            DevicePickerScreen()
        }
        composable(Routes.HOME) {
            HomeScreen(
                sharedUrl = sharedUrl,
                onSharedUrlConsumed = onSharedUrlConsumed,
            )
        }
    }
}
