package team.mino.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import team.mino.core.data.datasource.RoomRemoteDataSource
import team.mino.core.data.repository.mapper.toDomain
import team.mino.core.domain.model.Room
import team.mino.core.domain.repository.RoomRepository
import javax.inject.Inject

internal class RoomRepositoryImpl @Inject constructor(
    private val dataSource: RoomRemoteDataSource,
) : RoomRepository {
    override fun observeMyRooms(): Flow<List<Room>> =
        flow {
            emit(dataSource.getRooms().map { it.toDomain() })
        }

    override suspend fun getRoom(roomId: String): Room = dataSource.getRooms().first { it.id == roomId }.toDomain()
}
