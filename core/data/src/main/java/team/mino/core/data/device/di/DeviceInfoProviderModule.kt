package team.mino.core.data.device.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.device.DeviceInfoProvider
import team.mino.core.data.device.DeviceInfoProviderImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DeviceInfoProviderModule {
    @Binds
    @Singleton
    abstract fun bindDeviceInfoProvider(impl: DeviceInfoProviderImpl): DeviceInfoProvider
}
