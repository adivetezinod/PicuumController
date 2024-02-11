package com.etezinod.picuumcontroller.wrapper

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.toList
import java.util.UUID

@SuppressLint("MissingPermission")
class BluetoothCoroutineScanner(
    private val context: Context,
    private val bluetoothManager: BluetoothManager
) {
    val isScanEnabled: Boolean
        get() = bluetoothManager.adapter.isEnabled

    suspend fun startScan() = callbackFlow {
        val actions = listOf(
            BluetoothAdapter.ACTION_DISCOVERY_STARTED,
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED,
            BluetoothDevice.ACTION_FOUND
        )
        val filter = IntentFilter().apply {
            actions.forEach(::addAction)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {
                when (intent.action) {
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                        // nothing
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        close()
                    }
                    BluetoothDevice.ACTION_FOUND -> {
                        IntentCompat.getParcelableExtra(
                            intent,
                            BluetoothDevice.EXTRA_DEVICE,
                            BluetoothDevice::class.java
                        )?.run(::trySend)
                    }
                }
            }
        }
        if (!bluetoothManager.adapter.startDiscovery()) {
            close()
            return@callbackFlow
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
        awaitClose {
            context.unregisterReceiver(receiver)
            bluetoothManager.adapter.cancelDiscovery()
        }
    }

    suspend fun connect(
        address: String
    ) {
        val device = bluetoothManager.adapter.getRemoteDevice(address)
        val socket = device.createRfcommSocketToServiceRecord(
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        )
        runCatching {
            socket.connect()
        }.onFailure {
            println(it)
            return
        }
        socket.outputStream.write("HEllo World".toByteArray())
    }
}