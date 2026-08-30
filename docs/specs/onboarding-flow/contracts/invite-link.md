# 계약: 초대 링크 (`RoomInvitationRepository` · `GetInviteLinkUseCase` · `InviteLinkBuilder`)

**대상 스펙 경로**: `docs/specs/onboarding-flow` · **부속 문서**: [plan.md](../plan.md)

친구 초대 스텝이 **방금 만든 공동방의 초대 링크를 확보하는** 경로다(FR-008). 링크의 유효기간·수신 처리는 이 스펙이 정의하지 않는다(spec §3.2 — [SYS-010] 소관).

> **plan 2.0.0에서 이 문서가 가장 크게 바뀌었다.** 1.0.x는 `GET /api/v1/rooms/{roomId}` 응답의 `inviteCode`를 원천으로 삼았는데, 서버가 초대를 별도 리소스로 분리해 그 필드가 사라졌다. 경위는 [research.md R-009·R-010 (재검토)](../research.md)·[R-021](../research.md).

---

## 1. 서버 계약 대조

**출처**: <https://api.gguk.org/api-docs-json> — `Team MINO API 1.0.0`, 오퍼레이션 25개
**조회 시점**: 2026-08-29T01:09:27+09:00

이 스텝에 닿는 오퍼레이션은 셋이고, 그중 **하나만 쓴다.**

| 엔드포인트 | 태그 | 이 스텝과의 관계 |
|---|---|---|
| `POST /api/v1/rooms/{roomId}/invitations` | `invitation` | **쓴다.** 응답 `data.code`가 링크의 원천이다 |
| `GET /api/v1/rooms/{roomId}` | `room` | **쓰지 않는다.** 응답에 초대 코드가 없다(§1.2) |
| `GET /api/v1/invitations/{code}` | `invitation` | **쓰지 않는다.** 초대를 **받는** 쪽의 미리보기 API다 — [SYS-010] 소관 |

### 1.1 발급 오퍼레이션이 적은 것

- **설명**: *"멤버당 초대 1개다. 이미 발급했다면 같은 code를 돌려준다(재발급·만료 없음). 클라이언트가 `gguk.org/r/{code}` 형태로 링크를 조립한다. 개인방은 초대할 수 없다."*
- **요청**: 경로 파라미터 `roomId`(`string`)만. 본문 없음. `security: bearer`.
- **200 응답**: `data.code` — `type: string`, `minLength: 6`, `maxLength: 6`, `pattern: ^[ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789]{6}$`. 예시 `K7Q2MZ`.
- **실패**: `401`(`UNAUTHORIZED`·`TOKEN_EXPIRED`·`USER_NOT_REGISTERED`) · `403`(`NOT_ROOM_MEMBER`·`PERSONAL_ROOM_NOT_ALLOWED`) · `404`(`ROOM_NOT_FOUND`).

**세 가지가 이 계약을 단순하게 만든다.**

| 서버가 보장하는 것 | 이 계약이 하지 않아도 되는 일 |
|---|---|
| 멱등 — 이미 발급했으면 **같은 code** | 코드를 저장하지 않는다. 재개 경로(EC-021)에서 다시 불러도 같은 링크다 |
| 만료 없음 | 갱신·재발급 경로를 두지 않는다 |
| 조립은 클라이언트 몫이라고 명시 | 서버가 완성 URL을 줄 것을 기대하지 않는다 — [조립 위치 ADR](../../../adr/2026-08-24-invite-link-assembly-domain-interface.md)이 그대로 성립한다 |

### 1.2 1.0.x의 전제가 깨진 지점

`GET /api/v1/rooms/{roomId}`의 200 응답 스키마는 `id` · `type`(`personal`/`shared`) · `name` · `description` · `color` · `ownerId` · `createdAt` · `pinCount` · `memberCount`다. **`inviteCode`가 없다.** 1.0.0이 근거로 삼은 swagger 초안이 스스로 예고한 대로(*"invitation 테이블/리소스로 분리되면 이 필드는 응답에서 빠질 수 있다"*) 분리가 실제로 일어났다.

이로써 `Room` 도메인 모델·`RoomResponse`·`RoomMapper`의 변경이 이번 범위에서 빠진다.

### 1.3 어긋나는 것 · 미확정인 것

| # | 무엇 | 이 계약의 처리 |
|---|---|---|
| 1 | **링크 호스트가 프로덕션 기준으로만 적혀 있다.** `gguk.org/r/{code}`는 설명 문장의 값이고, dev·qa flavor가 같은 호스트를 쓰는지 스키마에 없다 | 조립을 `InviteLinkBuilder` 구현 한 파일에 가둔다(§4). 값 확정은 [열린 항목 D](../research.md#열린-항목)·서버팀 협의 항목 S-1 |
| 2 | **`POST`인데 호출자에게는 조회처럼 쓰인다.** 화면 진입마다 부르게 되고, 멱등이라 문제는 없지만 재시도 정책이 서버 설명 문장에만 근거한다 | 화면 진입 시 1회만 부르고, 실패해도 자동 재시도 루프를 돌지 않는다(§5). 설명 문장이 계약에서 빠지면 이 판단을 다시 본다 |
| 3 | **`403` 두 갈래(개인방·비멤버)가 온보딩 경로에서는 도달 불가다** | 도달하면 저장된 진행 상태가 손상된 것이다. 다른 HTTP 실패와 같이 다루고 별도 리프를 만들지 않는다(§5 · [ADR](../../../adr/2026-08-28-error-body-type-and-no-error-code-leaf.md)) |

**대응 API가 없는 요구사항: 없다.** 이 스텝의 모든 요구(FR-008~FR-013)가 위 오퍼레이션 하나와 클라이언트 로컬 동작으로 덮인다.

---

## 2. `RoomInvitationRepository`

`:core:domain/repository/RoomInvitationRepository.kt`

```
interface RoomInvitationRepository {
    suspend fun issueInviteCode(roomId: String): String
}
```

| 항목 | 내용 | 근거 |
|---|---|---|
| 성공 | 6자 초대 코드. **링크가 아니라 코드다** — 조립은 도메인 위쪽이 한다 | FR-008 |
| 실패 | `MinoDomainException`(`Network`·`Http`·`Auth`) throw. `null`이나 빈 문자열로 뭉개지 않는다 | EC-008 · [`error_handling.md`](../../../conventions/error_handling.md) §3 |
| 멱등 | 서버가 보장한다. 이 계약은 캐시하지 않는다 | §1.1 |

- **`RoomRepository`에 얹지 않는다.** 도메인 Repository의 단위는 서버 태그가 아니라 관심사이고, "초대 코드 발급"은 방의 CRUD와 다른 관심사다([research.md R-022](../research.md) · [ADR](../../../adr/2026-08-28-api-service-owned-per-server-tag.md)).
- 함수가 하나뿐인 Repository인 것은 의도다. [SYS-006] Flow B(방 상세 초대 시트)와 [SYS-010](초대 수신)이 이 계약에 함수를 더할 자리다.

### 구현 (`:core:data`)

| 파일 | 역할 |
|---|---|
| `network/service/InvitationApiService.kt` | `invitation` 태그의 오퍼레이션. 경로 문자열과 서버 코드 상수를 **이 파일 안에** 둔다 |
| `network/dto/response/InvitationResponse.kt` | `@Serializable data class InvitationResponse(val code: String)` |
| `datasource/InvitationRemoteDataSource.kt`(+`Impl`) | 서비스를 감싼다 |
| `repository/RoomInvitationRepositoryImpl.kt` | DTO → `String`. **DTO가 이 클래스 밖으로 나가지 않는다** |
| `datasource/di/`·`repository/di/` | 각 `@Binds` |

- 봉투(`MinoResponse<T>`) 해제는 ApiService가 `body<MinoResponse<InvitationResponse>>().data`로 한다([ADR](../../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)).
- `Authorization: Bearer`는 `MinoIdentityProofPlugin`이 싣고, 비2xx는 `convertDomainException`이 `MinoDomainException`으로 바꿔 던지므로 서비스가 잡지 않는다.
- **Mapper를 두지 않는다.** DTO 필드 하나를 그대로 꺼내는 것이라 변환 규칙이 없다.

**단위 테스트 대상**: `RoomInvitationRepositoryImpl` — 성공 시 코드 전달 1건, DataSource가 던진 예외의 전파 1건.

---

## 3. `GetInviteLinkUseCase`

`:core:domain/usecase/GetInviteLinkUseCase.kt`

```
class GetInviteLinkUseCase @Inject constructor(
    private val roomInvitationRepository: RoomInvitationRepository,
    private val inviteLinkBuilder: InviteLinkBuilder,
) {
    suspend operator fun invoke(roomId: String): String
}
```

**하는 일**: `issueInviteCode(roomId)`로 코드를 받아 `inviteLinkBuilder.build(code)`에 넘겨 링크 문자열을 돌려준다.

| 항목 | 내용 | 근거 |
|---|---|---|
| 성공 | 완성된 링크 문자열 | FR-008·TS-020 |
| 실패 | 아래 Repository의 예외를 그대로 전파한다 | EC-008 |
| 캐시 | 하지 않는다. 화면이 한 번 받은 값을 `UiState`에 든다 | EC-012 — 백그라운드에서 돌아와도 다시 확보하지 않는다 |

**UseCase로 분리한 이유**: [`core/domain/README.md`](../../../../core/domain/README.md) §4의 네 조건 중 "단순 표시"와 "재사용 없음"을 만족하지 않는다 — 응답 필드를 그대로 보여주는 것이 아니라 다른 협력자(`InviteLinkBuilder`)와 조합하고, 방 상세의 초대 시트([SYS-006] Flow B)가 같은 행위를 쓴다.

**단위 테스트 대상**: Fake `RoomInvitationRepository` + Fake `InviteLinkBuilder`로 성공 1건 · 실패 전파 1건.

---

## 4. `InviteLinkBuilder`

`:core:domain/invite/InviteLinkBuilder.kt` (인터페이스) · `:core:data`의 구현

```
interface InviteLinkBuilder {
    fun build(inviteCode: String): String
}
```

| 계층 | 무엇을 아는가 |
|---|---|
| `:core:domain` — 인터페이스 | "코드로부터 링크가 만들어진다"는 사실만 안다. 호스트도 경로도 모른다 |
| `:core:data` — `InviteLinkBuilderImpl` | 호스트와 경로 형식을 안다. 호스트는 flavor 빌드 설정에서 온다 |

**이렇게 가른 이유**의 소유자는 [초대 링크 조립 ADR](../../../adr/2026-08-24-invite-link-assembly-domain-interface.md)이다. 경위는 [research.md R-011](../research.md).

**2.0.0 재확인**: 서버가 코드 발급을 별도 리소스로 분리한 뒤에도 이 결정이 유효하다. 오히려 API 문서가 *"클라이언트가 `gguk.org/r/{code}` 형태로 링크를 조립한다"* 고 명시해 근거가 강해졌다 — 조립이 클라이언트 몫임이 서버 계약으로 확인됐다.

**경로 형식은 `/r/{code}`로 좁혀졌고 호스트만 미정이다** — [열린 항목 D](../research.md#열린-항목)·협의 항목 S-1.

**단위 테스트 대상**: 주어진 코드로 만들어진 문자열이 기대 형식과 일치하는지 1건(구현 모듈).

---

## 5. 실패의 성격

| 상황 | 통로 | UI | 근거 |
|---|---|---|---|
| 진입 시 링크 확보 실패 | `UiState.inviteLink = null`(State) | 화면은 그대로. 에러 화면으로 갈아 끼우지 않는다 | EC-008·UX-002 |
| 링크가 없는 상태에서 두 액션 중 하나가 눌림 | `DomainErrorEmitter` → Route가 스낵바 | 실패를 알리고 재확보를 시도한다. 공유 시트를 열거나 클립보드에 쓰지 않는다 | EC-008 |
| 우상단 [X] | — | 링크 상태와 무관하게 언제나 튜토리얼로 나아간다 | FR-013·EC-011 |

이것은 [`error_handling.md`](../../../conventions/error_handling.md) §5의 두 통로 중 어느 쪽에도 그대로 맞지 않는 **경계 사례**이며, 같은 문서 §8이 "첫 적용 화면 구현 시 결정한다"로 열어 둔 자리다. 판단 근거는 [research.md R-012](../research.md).

### 리프와 문구

발급 API가 내는 실패는 `MinoDomainException.Network` · `Http(code)` · `Auth` 셋이다. 서버 `errorCode`를 도메인 리프로 세우지 않는다([ADR](../../../adr/2026-08-28-error-body-type-and-no-error-code-leaf.md)).

| 서버 응답 | 리프 | 이 화면의 취급 |
|---|---|---|
| 네트워크 단절 | `Network` | 연결 안내 |
| `401` | `Auth` | 나머지와 같은 안내 — 세션은 스플래시가 보장하므로 정상 경로에서 오지 않는다 |
| `403`·`404`·그 밖 | `Http(code)` | 나머지와 같은 안내. **온보딩 경로에서는 도달 불가**(§1.3 #3) |

사용자가 취할 행동이 셋 다 같으므로(다시 누르거나 [X]로 나아간다) 문구를 가르지 않는다. 매핑은 `InviteRoute`의 `messageResOf`가 갖는다 — 공통 매퍼를 두지 않는 이유는 `error_handling.md` §8이 소유한다.

---

## 6. 이 계약이 지켜지는지 보는 법

| 확인 | 방법 |
|---|---|
| 도메인이 URL을 모른다 | `:core:domain` 어디에도 `http`·`gguk` 문자열이 없다 |
| 화면이 링크를 조립하지 않는다 | `:feature:onboarding`에 문자열 연결로 URL을 만드는 코드가 없다 |
| 잘못된 링크가 나가지 않는다 | 공유·복사 실행 지점이 `inviteLink != null` 가드 안에 있다 |
| 방을 두 번 만들지 않는다 | `:feature:onboarding`에 `createRoom`·`RoomRepository` 직접 호출이 없다 |
| 태그 소유 규칙을 지켰다 | `api/v1/rooms/{roomId}/invitations` 문자열이 `InvitationApiService` 한 곳에만 있고, `RoomApiService`는 이번 변경에서 손대지 않았다 |
| 코드를 저장하지 않는다 | `OnboardingProgress`에 코드 필드가 없고 DataStore 키도 3개 그대로다 |
