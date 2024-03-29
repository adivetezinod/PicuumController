package com.etezinod.picuumcontroller.wrapper

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@SuppressLint("MissingPermission")
class BluetoothLeChannelScanner(
    private val bluetoothLeFlowScanner: BluetoothLeFlowScanner
) {
    suspend fun startGattClient(
        address: String,
        sender: Sender,
        receiver: Receiver,
        onConnect: () -> Unit
    ) = withContext(Dispatchers.IO) {
        val jobs = arrayListOf<Job>()
        val writeLock = Channel<Boolean>(Channel.BUFFERED)

        bluetoothLeFlowScanner.connectGatt(
            address,
            onCharacteristicChanged = { _, value ->
                receiver.send(value)
            },
            onCharacteristicWrite = {
                writeLock.trySend(true)
            },
            onCharacteristicRead = { _, _ ->
                // nothing
            }
        ).collect { bluetoothGatt ->
            onConnect()
            jobs += launch {
                receiver.start(bluetoothGatt)
                sender.start(bluetoothGatt, writeLock)
            }
        }

        jobs.forEach { it.cancelAndJoin() }
    }

    class Sender(
        private val channel: ReceiveChannel<ByteArray>,
        private val service: UUID,
        private val characteristic: UUID
    ) {
        suspend fun start(
            bluetoothGatt: BluetoothGatt,
            writeLock: ReceiveChannel<Boolean>
        ) {
            val service = bluetoothGatt.getService(service)
            val characteristic = service.getCharacteristic(characteristic)

            for (value in channel) {
                BluetoothGattCompat.writeCharacteristic(
                    bluetoothGatt,
                    characteristic,
                    value,
                    BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                ) && writeLock.receive()
            }
        }
    }

    class Receiver(
        private val channel: SendChannel<ByteArray>,
        private val service: UUID,
        private val characteristic: UUID,
        private val descriptor: UUID? = null,
    ) {
        fun start(
            bluetoothGatt: BluetoothGatt,
        ) {
            val service = bluetoothGatt.getService(
                service
            ) ?: throw IllegalStateException(
                "UUID $service not found"
            )
            val characteristic = service.getCharacteristic(
                characteristic
            ) ?: throw IllegalStateException(
                "Characteristic $characteristic not found for service $service"
            )
            bluetoothGatt.setCharacteristicNotification(
                characteristic,
                true
            )
            val descriptors = descriptor
                ?.let(characteristic::getDescriptor)
                ?.let(::listOf)
                ?: characteristic.descriptors ?: emptyList()

            descriptors.forEach { descriptor ->
                BluetoothGattCompat.writeDescriptor(
                    bluetoothGatt,
                    descriptor
                )
            }
        }

        fun send(value: ByteArray) {
            channel.trySend(value)
        }
    }
}