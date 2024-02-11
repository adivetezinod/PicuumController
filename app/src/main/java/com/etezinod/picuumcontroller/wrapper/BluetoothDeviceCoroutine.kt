package com.etezinod.picuumcontroller.wrapper

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@SuppressLint("MissingPermission")
object BluetoothDeviceCoroutine {
    suspend fun fetchUuidsWithSdp(
        context: Context,
        device: BluetoothDevice,
        timeout: Long = 10000
    ) = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {
                IntentCompat.getParcelableExtra(
                    intent,
                    BluetoothDevice.EXTRA_UUID,
                    ParcelUuid::class.java
                )?.also(::trySend)
            }
        }
        val filter = IntentFilter(BluetoothDevice.ACTION_UUID)
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
        if (!device.fetchUuidsWithSdp()) {
            context.unregisterReceiver(receiver)
            return@callbackFlow
        }
        launch {
            delay(timeout)
            close()
        }
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }

    suspend fun createBond(
        context: Context,
        device: BluetoothDevice
    ) = (device.bondState == BluetoothDevice.BOND_BONDED) || suspendCoroutine { cont ->
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {
                val state = intent.getIntExtra(
                    BluetoothDevice.EXTRA_BOND_STATE,
                    BluetoothDevice.BOND_NONE
                )
                if (state != BluetoothDevice.BOND_BONDING) {
                    context.unregisterReceiver(this)
                    cont.resume(state == BluetoothDevice.BOND_BONDED)
                }
            }
        }
        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
        if (!device.createBond()) {
            context.unregisterReceiver(receiver)
            cont.resume(false)
        }
    }
}