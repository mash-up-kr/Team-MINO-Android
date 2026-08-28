package team.mino.core.domain.usecase

import javax.inject.Inject

/**
 * 공유받은 텍스트에서 저장 대상 URL 하나를 뽑는다(FR-002).
 *
 * 문구와 URL이 섞여 있어도 URL만 남기며, URL이 여러 개면 **가장 앞에 등장하는 하나**만 고른다(EC-003).
 * 하나도 없으면 `null`을 반환한다(EC-002) — 공유 텍스트 자체가 없는 경우도 호출자가 이 경로로 합류한다.
 *
 * 추출한 URL이 지원 대상인지는 판정하지 않는다. 지원 도메인은 서버가 정의하며, 클라이언트가 목록을 복제하면
 * 서버가 대상을 넓힐 때마다 앱 배포가 따라와야 한다(research.md R-002).
 */
class ExtractSharedUrlUseCase @Inject constructor() {
    operator fun invoke(sharedText: String): String? = URL_PATTERN.find(sharedText)?.value

    private companion object {
        /** 공백으로 끊기는 `http`·`https` 토큰 하나. `find`가 등장 순서상 첫 번째를 돌려준다. */
        val URL_PATTERN = Regex("""https?://\S+""")
    }
}
