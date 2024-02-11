package com.etezinod.picuumcontroller.wrapper

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class BluetoothLeFlowScanner(
    private val context: Context,
    private val bluetoothManager: BluetoothManager
) {
    val isScanEnabled: Boolean
        get() = bluetoothManager.adapter.isEnabled

    fun startScan(
        timeout: Long = 10000
    ) = callbackFlow {
        val callback = object : ScanCallback() {
            override fun onScanResult(
                callbackType: Int,
                result: ScanResult
            ) {
                trySend(result.device)
            }

            override fun onBatchScanResults(
                results: MutableList<ScanResult>
            ) {
                results.forEach {
                    trySend(it.device)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                println(errorCode)
            }
        }
        bluetoothManager.adapter.bluetoothLeScanner.startScan(callback)
        launch {
            delay(timeout)
            close()
        }
        awaitClose {
            bluetoothManager.adapter.bluetoothLeScanner.stopScan(callback)
        }
    }

    fun getBluetoothGatt(
        address: String,
        onCharacteristicWrite: (BluetoothGattCharacteristic) -> Unit,
        onCharacteristicChanged: (BluetoothGattCharacteristic, ByteArray) -> Unit,
        onCharacteristicRead: (BluetoothGattCharacteristic, ByteArray) -> Unit,
    ) = callbackFlow {
        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bluetoothManager.adapter.getRemoteLeDevice(
                address,
                BluetoothDevice.ADDRESS_TYPE_PUBLIC
            )
        } else {
            bluetoothManager.adapter.getRemoteDevice(address)
        }

        val callback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(
                gatt: BluetoothGatt,
                status: Int,
                newState: Int
            ) {
                if (status != BluetoothGatt.GATT_SUCCESS ||
                    newState != BluetoothProfile.STATE_CONNECTED) {
                    close()
                } else {
                    gatt.discoverServices()
                }
            }

            override fun onServicesDiscovered(
                gatt: BluetoothGatt,
                status: Int
            ) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    close()
                } else {
                    trySend(gatt)
                }
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    close()
                    return
                }
                onCharacteristicWrite(characteristic)
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                onCharacteristicChanged(characteristic, value)
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {
                onCharacteristicChanged(characteristic, characteristic.value)
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    close()
                    return
                }
                onCharacteristicRead(characteristic, characteristic.value)
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
                status: Int
            ) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    close()
                    return
                }
                onCharacteristicRead(characteristic, value)
            }
        }

        val gatt = device.connectGatt(
            context,
            false,
            callback
        ).apply(BluetoothGatt::connect)

        awaitClose {
            gatt.disconnect()
            gatt.close()
        }
    }
}