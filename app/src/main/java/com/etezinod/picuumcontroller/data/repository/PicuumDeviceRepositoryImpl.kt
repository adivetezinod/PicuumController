package com.etezinod.picuumcontroller.data.repository

import android.annotation.SuppressLint
import com.etezinod.picuumcontroller.domain.model.PicuumDevice
import com.etezinod.picuumcontroller.domain.repository.PicuumDeviceRepository
import com.etezinod.picuumcontroller.wrapper.BluetoothLeFlowScanner
import kotlinx.coroutines.flow.toSet
import javax.inject.Inject

@SuppressLint("MissingPermission")
class PicuumDeviceRepositoryImpl @Inject constructor(
    private val bluetoothLeFlowScanner: BluetoothLeFlowScanner
) : PicuumDeviceRepository {
    override suspend fun getDevices(): List<PicuumDevice> {
        if (!bluetoothLeFlowScanner.isScanEnabled) return emptyList()
        return bluetoothLeFlowScanner.startScan().toSet().toList().map {
            PicuumDevice(it.name ?: "", it.address ?: "")
        }
    }
}