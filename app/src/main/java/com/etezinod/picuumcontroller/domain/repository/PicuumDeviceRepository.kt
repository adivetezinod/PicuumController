package com.etezinod.picuumcontroller.domain.repository

import com.etezinod.picuumcontroller.domain.model.PicuumDevice
import kotlinx.coroutines.channels.ReceiveChannel

interface PicuumDeviceRepository {
    suspend fun getDevices(): List<PicuumDevice>
}