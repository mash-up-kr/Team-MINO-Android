package team.mino.feature.placedetail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import dagger.hilt.android.AndroidEntryPoint
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.navigation.activity.launcher.EXTRA_PLACE_DETAIL_PIN_ID

/**
 * 장소 상세의 진입 Activity. 이 feature가 밖으로 여는 유일한 표면이다
 * (`contracts/place-detail-launcher.md` §1).
 *
 * 진입 인자는 `pinId` 하나이고 해석하지 않은 채 시작 라우트에 싣는다 — 「지금 보고 있는 방」은 호출자가 어느
 * 핀을 지목했느냐로 이미 결정돼 있다(계약 §2).
 *
 * 화면 밖으로 나가는 셋을 실행하는 것은 이 Activity다. 나가기는 [finish]까지만 한다 — 「지금 보고 있는 방의
 * 방 상세로 보낸다」(FR-009)는 목적지 배선이 `[TBD]`로 남아 있어서이며, 그 근거는
 * `docs/specs/place-detail/research.md` D2가 소유한다.
 */
@AndroidEntryPoint
class PlaceDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* pinId가 없으면 아무것도 열지 않고 물러난다(계약 §2가 [TBD]로 둔 자리).
         * 이 값 하나가 조회할 대상 전부라 빈 값으로는 띄울 화면도 보낼 요청도 없고, 화면에는 재시도가 붙는
         * 자리가 없어 오류 상태로 열어 두면 사용자가 할 수 있는 일이 없다. 값을 싣지 않은 것은 호출자의
         * 결함이므로 예외로 앱을 죽이지 않고 조용히 닫는다 — 결과를 돌려주지 않는 것은 계약 §3 그대로다.
         * `ShareReceiverActivity`가 공유 URL이 없을 때 취하는 처리와 같은 형태다. */
        val pinId = intent.getStringExtra(EXTRA_PLACE_DETAIL_PIN_ID).orEmpty()
        if (pinId.isBlank()) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            MinoAndroidAppTheme {
                PlaceDetailShell(
                    startDestination = PlaceDetailMain(pinId),
                    onExit = ::finish,
                    onOpenExternalMap = ::openExternalMap,
                    onOpenSourceLink = ::openSourceLink,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    /**
     * 외부 지도로 장소를 연다(spec FR-016).
     *
     * **`geo:` 후보를 맨 앞에 둔다.** FR-016이 갈래를 가르는 조건은 「외부 지도 앱이 있는가」 하나인데, 설치된
     * 지도 앱만 받고 브라우저는 받지 않는 표준 스킴이 `geo:`뿐이다. 서버가 준 [mapUrl]을 먼저 열면 그 링크를
     * 어느 앱이 검증(App Links)해 뒀느냐에 따라 지도 앱이 있는데도 브라우저로 새어 TS-028과 어긋난다.
     * 지도 앱의 패키지명을 박지 않으므로 매니페스트 `<queries>`도 필요 없다.
     *
     * 지도 앱이 없으면 브라우저로 대체한다(spec TS-029) — [mapUrl]이 있으면 그것을, 없으면 장소명으로 만든
     * 지도 검색 URL을 연다. 두 후보를 차례로 시도하므로 아무 반응 없이 끝나지 않는다(spec SC-004).
     */
    private fun openExternalMap(
        mapUrl: String?,
        query: String,
    ) {
        val encodedQuery = Uri.encode(query)
        if (startViewIntent(GEO_SEARCH_URI_PREFIX + encodedQuery)) return
        startViewIntent(mapUrl?.takeIf { it.isNotBlank() } ?: (WEB_MAP_SEARCH_URL_PREFIX + encodedQuery))
    }

    /**
     * 장소의 원문 링크를 연다(spec FR-017).
     *
     * 후보를 하나만 두는 것은 원문이 [SYS-002] 링크 분석이 수집한 그 주소여야 하기 때문이다 — 대신 열 만한
     * 다른 주소가 없다. 열린 뒤 게시글이 삭제돼 오류 화면이 뜨는 것은 앱이 처리하지 않는다(spec EC-018).
     */
    private fun openSourceLink(url: String) {
        startViewIntent(url)
    }

    /**
     * 후보 하나를 외부에 넘긴다. 받을 앱이 없으면 `ActivityNotFoundException`이 나므로, 그것을 「이 후보로는
     * 열리지 않는다」는 신호로 바꿔 호출부가 다음 후보로 넘어가게 한다.
     */
    private fun startViewIntent(uri: String): Boolean =
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
            true
        } catch (notFound: ActivityNotFoundException) {
            false
        }

    private companion object {
        /** 좌표가 아니라 검색어로 여는 형태다. 어느 지도 앱이 받을지는 사용자의 기본 설정이 정한다. */
        const val GEO_SEARCH_URI_PREFIX = "geo:0,0?q="

        /** 지도 앱이 없을 때의 웹 검색. `:core:map`이 Google Maps를 쓰므로 브라우저 쪽도 같은 서비스로 맞춘다. */
        const val WEB_MAP_SEARCH_URL_PREFIX = "https://www.google.com/maps/search/?api=1&query="
    }
}
