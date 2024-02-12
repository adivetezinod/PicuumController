package com.etezinod.picuumcontroller.screen

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.etezinod.picuumcontroller.ui.theme.PicuumControllerTheme
import com.etezinod.picuumcontroller.wrapper.BluetoothLeChannelScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@Composable
fun PicuumControllerScreen() {
    val vm: PicuumControllerViewModel = hiltViewModel()
    val state by vm.state.collectAsState()
    PicuumControllerScreenComposable(state, vm::onCommand)
}

@Composable
private fun PicuumControllerScreenComposable(
    state: PicuumControllerScreenState,
    onCommand: (PicuumControllerCommand) -> Unit = {}
) {
    if (!state.isConnected && !state.hasError) {
        CircularProgressIndicator()
    } else if (state.hasError) {
        Text("We're dead")
    } else {
        Row(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PicuumControllerCommand.entries.forEach { command ->
                Box(Modifier.weight(1F)) {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()

                    LaunchedEffect(isPressed) {
                        if (isPressed) {
                            while (coroutineContext.isActive) {
                                delay(75)
                                onCommand(command)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            onCommand(command)
                        },
                        interactionSource = interactionSource
                    ) {
                        Text("$command")
                    }
                }
            }
        }
    }
}

data class PicuumControllerScreenState(
    val isConnected: Boolean = false,
    val hasError: Boolean = false,
)

enum class PicuumControllerCommand {
    FORWARD,
    BACKWARD,
    LEFT,
    RIGHT
}

@HiltViewModel
class PicuumControllerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bluetoothLeChannelScanner: BluetoothLeChannelScanner
) : ViewModel() {
    val state = MutableStateFlow(PicuumControllerScreenState())

    private val service = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
    private val characteristic = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
    private val descriptor = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private val sender = Channel<ByteArray>(Channel.UNLIMITED)
    private val receiver = Channel<ByteArray>(Channel.UNLIMITED)

    init {
        val address = savedStateHandle["address"] ?: ""
        viewModelScope.launch {
            bluetoothLeChannelScanner.startGattClient(
                address,
                BluetoothLeChannelScanner.Sender(
                    sender, service, characteristic,
                ),
                BluetoothLeChannelScanner.Receiver(
                    receiver, service, characteristic, descriptor,
                )
            ) {
                state.update { it.copy(isConnected = true) }
            }
            state.update { it.copy(isConnected = false, hasError = true) }
        }
    }

    fun onCommand(command: PicuumControllerCommand) {
        sender.trySend(byteArrayOf(command.ordinal.toByte()))
    }
}

private class PicuumControllerScreenStateProvider :
    CollectionPreviewParameterProvider<PicuumControllerScreenState>(
        listOf(
            PicuumControllerScreenState(
                isConnected = false,
                hasError = false
            ),
            PicuumControllerScreenState(
                isConnected = true,
                hasError = false
            ),
            PicuumControllerScreenState(
                isConnected = false,
                hasError = true
            )
        )
    )

@Preview(
    showBackground = false, showSystemUi = true,
    device = "spec:parent=pixel_5,orientation=landscape"
)
@Composable
private fun PicuumControllerScreenPreview(
    @PreviewParameter(PicuumControllerScreenStateProvider::class)
    data: PicuumControllerScreenState
) {
    PicuumControllerTheme {
        PicuumControllerScreenComposable(data)
    }
}