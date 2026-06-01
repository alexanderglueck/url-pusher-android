package com.alexanderglueck.urlpusher.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alexanderglueck.urlpusher.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    sharedUrl: String?,
    onSharedUrlConsumed: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val pushedMessage = stringResource(R.string.share_pushed)
    val failedMessage = stringResource(R.string.share_failed)

    LaunchedEffect(sharedUrl) {
        val url = sharedUrl
        if (!url.isNullOrBlank()) {
            viewModel.pushSharedUrl(url)
            onSharedUrlConsumed()
        }
    }

    LaunchedEffect(state.toast) {
        when (state.toast) {
            HomeViewModel.TOAST_PUSHED -> {
                snackbarHostState.showSnackbar(pushedMessage)
                viewModel.consumeToast()
            }
            HomeViewModel.TOAST_FAILED -> {
                snackbarHostState.showSnackbar(failedMessage)
                viewModel.consumeToast()
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.home_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.home_welcome, state.userName),
                style = MaterialTheme.typography.titleMedium,
            )
            val deviceText = state.activeDeviceName?.let { stringResource(R.string.home_current_device, it) }
                ?: stringResource(R.string.home_no_device)
            Text(text = deviceText, style = MaterialTheme.typography.bodyLarge)

            if (state.pushing) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator()
                Text(stringResource(R.string.share_pushing))
            }

            Text(
                text = stringResource(R.string.home_share_hint),
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = viewModel::changeDevice,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.home_change_device)) }

            Button(
                onClick = viewModel::signOut,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.home_sign_out)) }
        }
    }
}
