package com.etezinod.picuumcontroller.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.etezinod.picuumcontroller.domain.model.PicuumDevice
import com.etezinod.picuumcontroller.domain.repository.PicuumDeviceRepository
import com.etezinod.picuumcontroller.ui.theme.PicuumControllerTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun PicuumDevicesScreen(
    onClick: (PicuumDevice) -> Unit
) {
    val vm: PicuumDevicesScreenViewModel = hiltViewModel()
    val state by vm.state.collectAsState()
    PicuumDevicesComposable(
        state,
        vm::startScan,
        onClick
    )
}

@Composable
private fun PicuumDevicesComposable(
    state: PicuumDevicesScreenState,
    startScan: () -> Unit = {},
    onClick: (PicuumDevice) -> Unit = {}
) {
    if (!state.isScanning) {
        LazyColumn {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = startScan) {
                        Text("Start scan")
                    }
                }
            }
            items(state.devices) {
                PicuumDeviceCard(it, onClick)
            }
        }
    } else {
        CircularProgressIndicator()
    }
}

@Composable
private fun PicuumDeviceCard(
    picuumDevice: PicuumDevice,
    onClick: (PicuumDevice) -> Unit
) {
    Card(
        Modifier
            .padding(8.dp)
            .clickable { onClick(picuumDevice) }
    ) {
        Row(
            Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(.9F)) {
                Text(picuumDevice.name)
                HorizontalDivider()
                Text(picuumDevice.address)
            }
            Icon(
                Icons.Default.Share,
                "Connect to",
                Modifier.weight(.1F),
            )
        }
    }
}

data class PicuumDevicesScreenState(
    val isScanning: Boolean = false,
    val devices: List<PicuumDevice> = emptyList()
)

@HiltViewModel
class PicuumDevicesScreenViewModel @Inject constructor(
    private val picuumDeviceRepository: PicuumDeviceRepository
) : ViewModel() {
    val state = MutableStateFlow(PicuumDevicesScreenState())

    fun startScan() {
        viewModelScope.launch {
            state.update { it.copy(isScanning = true) }
            val devices = picuumDeviceRepository.getDevices()
            state.update { it.copy(isScanning = false, devices = devices) }
        }
    }
}

private class StateProvider : CollectionPreviewParameterProvider<PicuumDevicesScreenState>(
    listOf(
        PicuumDevicesScreenState(
            isScanning = true
        ),
        PicuumDevicesScreenState(
            isScanning = false,
            devices = listOf(
                PicuumDevice(
                    "Device 1",
                    "1234"
                ),
                PicuumDevice(
                    "Device 2",
                    "5678"
                )
            )
        )
    )
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PicuumDevicesScreenPreview(
    @PreviewParameter(StateProvider::class)
    data: PicuumDevicesScreenState
) {
    PicuumControllerTheme {
        PicuumDevicesComposable(data)
    }
}