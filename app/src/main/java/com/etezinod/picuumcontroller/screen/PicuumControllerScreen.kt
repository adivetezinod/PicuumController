package com.etezinod.picuumcontroller.screen

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    PicuumControllerScreenComposable(state)
}

@Composable
private fun PicuumControllerScreenComposable(
    state: PicuumControllerScreenState
) {
    if (!state.isConnected && !state.hasError) {
        CircularProgressIndicator()
    } else if (state.hasError) {
        Text("We're dead")
    } else {
        Text("Big day")
    }
}

data class PicuumControllerScreenState(
    val isConnected: Boolean = false,
    val hasError: Boolean = false,
)

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
            state.update { it.copy(isConnected = false,  hasError = true) }
        }
        viewModelScope.launch {
            var counter = 0
            while (coroutineContext.isActive) {
                sender.trySend("Hello World: $counter".toByteArray())
                counter++
                delay(1000)
            }
        }
        viewModelScope.launch {
            for (value in receiver) {
                val str = String(value)
                println(str)
            }
        }
    }
}