package com.alexanderglueck.urlpusher.ui.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alexanderglueck.urlpusher.R
import com.alexanderglueck.urlpusher.domain.model.Device

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicePickerScreen(
    onScanQrInstead: () -> Unit,
    viewModel: DevicePickerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.devices_title)) }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.devices_subtitle),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(16.dp))
            when {
                state.loading -> LoadingBlock()
                state.devices.isEmpty() -> Text(stringResource(R.string.devices_empty))
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.devices, key = { it.id }) { device ->
                        DeviceRow(
                            device = device,
                            isSelecting = state.selecting == device.id,
                            onClick = { viewModel.select(device) },
                        )
                    }
                }
            }
            state.error?.let {
                Spacer(Modifier.height(12.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = onScanQrInstead,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.devices_scan_qr_instead)) }
        }
    }
}

@Composable
private fun LoadingBlock() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.devices_loading))
    }
}

@Composable
private fun DeviceRow(device: Device, isSelecting: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        enabled = device.canPush && !isSelecting,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = device.name, style = MaterialTheme.typography.titleMedium)
            if (!device.canPush) {
                Text(
                    text = stringResource(R.string.devices_cannot_push),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            if (isSelecting) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.height(20.dp))
            }
        }
    }
}
