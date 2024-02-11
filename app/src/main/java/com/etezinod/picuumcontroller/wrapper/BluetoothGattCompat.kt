package com.etezinod.picuumcontroller.wrapper

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothStatusCodes
import android.os.Build

@SuppressLint("MissingPermission")
object BluetoothGattCompat {
    fun writeCharacteristic(
        bluetoothGatt: BluetoothGatt,
        bluetoothGattCharacteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
    ) = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        bluetoothGatt.writeCharacteristic(
            bluetoothGattCharacteristic,
            value,
            writeType
        ) == BluetoothStatusCodes.SUCCESS
    } else {
        bluetoothGatt.writeCharacteristic(
            bluetoothGattCharacteristic.apply {
                this.value = value
                this.writeType = writeType
            },
        )
    }) && writeType != BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE

    fun writeDescriptor(
        bluetoothGatt: BluetoothGatt,
        bluetoothGattDescriptor: BluetoothGattDescriptor,
        value: ByteArray = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
    ) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        bluetoothGatt.writeDescriptor(
            bluetoothGattDescriptor,
            value
        ) == BluetoothStatusCodes.SUCCESS
    } else {
        bluetoothGatt.writeDescriptor(
            bluetoothGattDescriptor.apply {
                this.value = value
            }
        )
    }
}