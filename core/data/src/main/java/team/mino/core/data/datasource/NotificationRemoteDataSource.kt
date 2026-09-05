package team.mino.core.data.datasource

import team.mino.core.data.network.dto.response.NotificationPageResponse

/**
 * 알림함의 원격 출처. 계약은 `docs/specs/notifications/contracts/notification-api.md`가 소유한다.
 *
 * 함수가 하나뿐이다 — 알림 탭이 부르는 서버 API는 목록 조회 하나이고, 목록을 그리는 데도 알림을 눌러
 * 도착지를 정하는 데도 다른 요청이 없다(같은 계약 §2 · `docs/specs/notifications/research.md` D5).
 * 읽음 처리·삭제·수신 설정은 spec §3.2가 범위 밖으로 뺐다.
 *
 * FCM 토큰 등록은 서버 리소스가 달라 `UserRemoteDataSource` 경로에 있다 — `NotificationApiService`
 * KDoc이 그 사정을 든다.
 *
 * 소비자는 같은 모듈의 `NotificationRepositoryImpl` 하나뿐이며, `internal`로 닫혀 있어 전송용 DTO가
 * 도메인 표면에 올라가지 않는다.
 */
internal interface NotificationRemoteDataSource {
    /**
     * 알림 목록의 [page]쪽을 가져온다.
     *
     * **[CommentRemoteDataSource.getComments]와 같은 이유로 봉투를 벗긴 알맹이가 아니라
     * [NotificationPageResponse] 전체를 돌려준다.** 서버가 `data`와 나란히 `pagination`을 싣고, 도메인이
     * 그 `hasNext`를 읽기 때문이다 — 사정은 그 DTO의 KDoc에 있다.
     *
     * [page] 0이 최신 묶음이다. 한 묶음의 크기는 인자로 열지 않고 서버 기본값을 따른다(같은 계약 §1).
     */
    suspend fun getNotifications(page: Int): NotificationPageResponse
}
