# 초대 링크 문자열 조립은 도메인 인터페이스로 계약하고, 호스트를 아는 구현은 `:core:data`가 갖는다

- **상태**: Accepted
- **작성일**: 2026-08-24
- **작성자**: Jaesung Lee

## 컨텍스트

온보딩의 친구 초대 스텝(`docs/specs/onboarding-flow` FR-008)이 방금 만든 공동방의 **초대 링크**를 OS 공유 시트와 클립보드로 내보내야 한다. 같은 링크를 방 상세의 초대 바텀시트(PRD [SYS-006] Flow B)도 쓰게 된다.

링크를 만드는 재료가 **두 곳에서 온다**는 것이 이 결정의 출발점이다.

- **코드**는 서버가 준다. [swagger 초안](https://raw.githubusercontent.com/mash-up-kr/Team-MINO-Node/refs/heads/KKardy/GM-111-outline-prd/docs/swagger.yaml)의 `Room.inviteCode`(최대 16자)이며, `GET /api/v1/rooms/{roomId}` 응답에 실려 온다.
- **호스트와 경로**는 빌드 설정이 안다. swagger는 이것을 스키마가 아니라 `inviteCode` 필드의 **설명 문장**에만 적어 두었다 — *"초대 링크 `gguk.org/r/{code}`의 code"*. 값이 확정된 것이 아니고, 이 저장소는 이미 `HttpClient`의 baseUrl을 flavor BuildConfig로 다루고 있어 호스트가 flavor마다 갈릴 여지가 있다.

여기에 제약이 셋 겹친다.

- `:core:domain`은 Android도 배포 환경도 모른다([헌법 원칙 II](../constitution.md), [`core/domain/README.md`](../../core/domain/README.md) §7).
- 링크의 **형식·유효기간·수신 처리**는 온보딩 스펙이 정의하지 않는다 — [SYS-010]의 몫이다(`spec.md` §3.2). 즉 형식은 **나중에 바뀔 수 있는 값**이다.
- swagger가 `inviteCode` 필드에 *"invitation 테이블/리소스로 분리되면 이 필드는 응답에서 빠질 수 있다"*고 스스로 예고했다.

## 결정

초대 링크 조립을 **도메인의 계약**과 **데이터의 구현**으로 가른다.

```kotlin
// :core:domain — InviteLinkBuilder.kt
interface InviteLinkBuilder {
    fun build(inviteCode: String): String
}

// :core:domain/usecase — GetInviteLinkUseCase.kt
class GetInviteLinkUseCase @Inject constructor(
    private val roomRepository: RoomRepository,
    private val inviteLinkBuilder: InviteLinkBuilder,
) {
    suspend operator fun invoke(roomId: String): String
}
```

- **`:core:domain`은 "코드로부터 링크가 만들어진다"는 사실만 안다.** 호스트도 경로도 모른다.
- **`:core:data`의 `InviteLinkBuilderImpl`이 호스트와 경로 형식을 안다.** 호스트는 flavor 빌드 설정에서 온다. 바인딩은 구현을 소유한 모듈이 갖는다([Hilt 바인딩 소유 ADR](2026-08-02-di-binding-ownership.md)).
- **화면은 완성된 문자열만 받는다.** feature에서 문자열을 이어 붙여 URL을 만들지 않는다.
- `Repository`에 `getInviteLink(roomId): String` 같은 함수를 두지 않는다. 데이터 접근 계약에 표현 형식을 섞지 않는다.

## 근거

**형식이 바뀔 것을 알고 시작한다.** 호스트 값도 경로도 아직 확정이 아니고([SYS-010] 미작성), 서버는 `inviteCode` 필드 자체가 응답에서 빠질 수 있다고 예고했다. 그렇다면 설계의 목표는 "지금 맞는 값을 넣는 것"이 아니라 **바뀔 때 고칠 자리를 한 곳으로 모으는 것**이다. 이 구조에서 형식이 바뀌면 `InviteLinkBuilderImpl` 한 파일, 응답 모양이 바뀌면 `RoomMapper` 한 파일이 고칠 대상의 전부다.

**두 극단이 각각 다른 규칙을 어긴다.**

- 도메인이 URL 상수를 들면 `:core:domain`이 배포 환경(flavor·호스트)을 알게 된다. 순수 Kotlin 모듈이 빌드 변형을 아는 순간 그 모듈은 더 이상 어디서든 쓸 수 있는 계층이 아니다.
- feature가 조립하면 서버 소유의 링크 형식이 UI 코드로 샌다. 사용처가 둘(온보딩 친구 초대·방 상세 초대 시트) 이상이므로, 형식이 확정될 때 고칠 자리가 화면 수만큼 흩어진다.

**중간 지점이 인터페이스다.** 도메인은 "링크가 존재한다"는 개념만 갖고, 그 개념을 값으로 만드는 방법은 바깥 계층이 채운다. 이것은 이 저장소가 이미 `Repository`에 쓰고 있는 패턴과 같은 모양이라 새 개념을 도입하지 않는다.

**`GetInviteLinkUseCase`를 둔 것은 UseCase 생성 기준의 결과다.** [`core/domain/README.md`](../../core/domain/README.md) §4의 네 조건 중 둘을 만족하지 않는다 — 응답 필드를 그대로 보여주는 것이 아니라 다른 협력자와 조합하고(단순 표시 ✗), 방 상세의 초대 시트가 같은 행위를 쓴다(재사용 없음 ✗).

## 결과

- `:core:domain`에 `InviteLinkBuilder`(인터페이스)와 `GetInviteLinkUseCase`가 생긴다. 둘 다 순수 Kotlin이며 Fake로 JVM 테스트가 가능하다.
- `:core:data`에 `InviteLinkBuilderImpl`과 그 `@Binds`가 생긴다. **호스트 값을 아는 유일한 자리**다.
- 공동방 도메인 모델 `Room`에 `inviteCode: String`이 더해진다. 그 모델을 소유한 `docs/specs/group-room-form`의 데이터 모델이 이 필드를 *"다른 feature가 필요로 할 때 더한다"*로 열어 두었던 자리이며, 온보딩이 그 첫 소비자다. `RoomResponse`·`RoomMapper`도 함께 넓어진다.
- **초대 링크가 필요한 다음 화면은 이 UseCase를 주입받는다.** 방 상세 초대 시트가 자체 조립 코드를 만들면 이 ADR 위반이다.
- **호스트와 경로 값은 이 ADR이 정하지 않는다.** [SYS-010] 또는 서버팀이 확정하면 `InviteLinkBuilderImpl`에 반영한다. 그때까지 알려진 것은 swagger 설명 문장의 `gguk.org/r/{code}`뿐이다.
- 리뷰 확인점: `:core:domain` 어디에도 `http`·`gguk` 문자열이 없고, feature에 URL을 문자열 연결로 만드는 코드가 없다.

## 고려한 대안

**1. 도메인 모델이나 상수가 완성된 링크를 든다** — `Room.inviteLink: String`처럼 서버가 준 코드가 아니라 링크를 도메인에 둔다. 기각: 링크를 만들려면 호스트가 필요하고, 그러면 `:core:domain`이 배포 환경을 알게 된다. 헌법 원칙 II의 "`:core:domain`은 Android에 의존하지 않는다"가 지키려는 것이 바로 이 격리다.

**2. `RoomRepository`가 `getInviteLink(roomId): String`을 노출한다** — 조회와 조립을 한 함수로 합친다. 기각: 데이터 접근 계약에 표현 형식이 섞이고, 링크가 필요 없는 호출자(방 목록·방 편집)도 그 함수를 보게 된다. Repository는 도메인 모델을 돌려주는 자리이지 문자열을 가공하는 자리가 아니다.

**3. feature가 `"https://gguk.org/r/" + code`로 조립한다** — 가장 적은 코드로 지금 동작한다. 기각: 사용처가 둘 이상이라 형식이 확정될 때 고칠 자리가 화면마다 흩어진다. 서버가 소유한 형식이 UI 코드에 박히는 것 자체가 SSOT 위반이다([헌법 원칙 I](../constitution.md)).

**4. 공동방 생성 폼의 결과 인텐트에 `inviteCode`를 실어 보낸다** — 조회 없이 링크를 얻는다. 기각: 앱을 다시 켜고 친구 초대 스텝을 재개하는 경로(`spec.md` EC-021)에는 결과 인텐트가 없어 조회 경로가 어차피 필요하다. 두 경로를 유지하느니 하나로 닫는 편이 낫고, 다른 feature의 결과 계약을 이쪽 사정으로 넓히지 않아도 된다.
