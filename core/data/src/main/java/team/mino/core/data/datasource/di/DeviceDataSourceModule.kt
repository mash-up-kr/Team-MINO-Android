package team.mino.core.data.datasource.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.mino.core.data.datasource.DeviceIdLocalDataSource
import team.mino.core.data.datasource.DeviceIdLocalDataSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DeviceDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindDeviceIdLocalDataSource(impl: DeviceIdLocalDataSourceImpl): DeviceIdLocalDataSource
}
