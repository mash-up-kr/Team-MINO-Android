package team.mino.core.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 방 리스트의 영속 값을 공유 `DataStore<Preferences>`에 보관한다.
 *
 * `HomePreferencesLocalDataSourceImpl`과 같은 패턴 — 흐름을 구독하지 않고 현재 값을 한 번만 본다.
 * epoch milliseconds로 저장하고, 도메인 경계에서는 [Instant]로 변환해 오간다.
 *
 * 저장 실패(디스크 이상 등)는 잡지 않고 그대로 전파한다.
 */
@OptIn(ExperimentalTime::class)
internal class RoomPreferencesLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : RoomPreferencesLocalDataSource {
    override suspend fun getNudgeDismissedAt(): Instant? =
        dataStore.data.first()[NUDGE_DISMISSED_AT_KEY]?.let { Instant.fromEpochMilliseconds(it) }

    override suspend fun setNudgeDismissedAt(dismissedAt: Instant) {
        dataStore.edit { preferences -> preferences[NUDGE_DISMISSED_AT_KEY] = dismissedAt.toEpochMilliseconds() }
    }

    private companion object {
        val NUDGE_DISMISSED_AT_KEY = longPreferencesKey("room_nudge_dismissed_at")
    }
}
