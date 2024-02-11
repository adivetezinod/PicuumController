package com.etezinod.picuumcontroller.di

import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.core.content.getSystemService
import com.etezinod.picuumcontroller.wrapper.BluetoothCoroutineScanner
import com.etezinod.picuumcontroller.wrapper.BluetoothLeChannelScanner
import com.etezinod.picuumcontroller.wrapper.BluetoothLeFlowScanner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AndroidModule {
    @Provides
    fun providesBluetoothManager(
        @ApplicationContext context: Context
    ) = context.getSystemService<BluetoothManager>()!!

    @Provides
    fun providesBluetoothCoroutineScanner(
        @ApplicationContext context: Context,
        bluetoothManager: BluetoothManager
    ) = BluetoothCoroutineScanner(
        context,
        bluetoothManager
    )

    @Provides
    fun providesBluetoothLeCoroutineScanner(
        @ApplicationContext context: Context,
        bluetoothManager: BluetoothManager
    ) = BluetoothLeFlowScanner(
        context,
        bluetoothManager
    )

    @Provides
    fun providesBluetoothLeChannelScanner(
        bluetoothLeFlowScanner: BluetoothLeFlowScanner
    ) = BluetoothLeChannelScanner(
        bluetoothLeFlowScanner
    )
}