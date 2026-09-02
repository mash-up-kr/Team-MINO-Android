package team.mino.feature.room

import android.content.res.Resources
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * [SYS-003] 방 선택 시트의 공유 완료 토스트.
 *
 * **두 진입점이 나눠 쓴다** — 방 상세의 장소 카드 메뉴와 장소 상세의 [다른방에 공유]가 같은 시트를
 * 부르므로, 같은 동작의 문구와 노출 시간이 진입점에 따라 갈리지 않는다
 * (`docs/specs/place-detail/contracts/place-detail-main-contract.md` §3.4.4).
 *
 * **시간을 여기서 잰다.** `SnackbarDuration.Short`가 4초라 Figma 주석 3번이 정한 3초를 그 값으로
 * 표현할 수 없다 — 띄우는 것을 따로 띄우고, 3초 뒤 거둔다.
 */
internal suspend fun SnackbarHostState.showShareCompleted(resources: Resources) {
    coroutineScope {
        val showing = launch { showSnackbar(resources.getString(R.string.roomshare_completed)) }
        delay(SHARE_COMPLETED_TOAST_DURATION_MS)
        showing.cancel()
    }
}

/** Figma 주석 3번(`완료 토스트 / 3초 노출`, 섹션 `3225-88512`). */
private const val SHARE_COMPLETED_TOAST_DURATION_MS = 3000L
