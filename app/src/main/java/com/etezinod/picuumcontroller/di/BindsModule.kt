package com.etezinod.picuumcontroller.di

import com.etezinod.picuumcontroller.data.repository.PicuumDeviceRepositoryImpl
import com.etezinod.picuumcontroller.domain.repository.PicuumDeviceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface BindsModule {
    @Binds
    fun bindsPicuumDeviceRepositoryImpl(
        impl: PicuumDeviceRepositoryImpl
    ): PicuumDeviceRepository
}