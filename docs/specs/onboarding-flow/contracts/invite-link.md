# 계약: 초대 링크 (`GetInviteLinkUseCase` · `InviteLinkBuilder`)

**대상 스펙 경로**: `docs/specs/onboarding-flow` · **부속 문서**: [plan.md](../plan.md)

친구 초대 스텝이 **방금 만든 공동방의 초대 링크를 확보하는** 경로다(FR-008). 링크의 형식·유효기간·수신 처리는 이 스펙이 정의하지 않는다(spec §3.2 — [SYS-010] 소관).

---

## 1. 서버 계약 대조

[swagger `0.1.0-draft`](https://raw.githubusercontent.com/mash-up-kr/Team-MINO-Node/refs/heads/KKardy/GM-111-outline-prd/docs/swagger.yaml)에서 이 스텝에 닿는 것은 하나다.

| 엔드포인트 | 이 스텝과의 관계 |
|---|---|
| `GET /api/v1/rooms/{roomId}` | **쓴다.** 응답 `RoomDetail`(= `Room` + 메타)의 `inviteCode`가 링크의 원천이다 |
| `POST /api/v1/rooms` | **쓰지 않는다.** 방 생성은 공동방 폼의 몫이고, 온보딩은 그 결과의 `roomId`만 받는다 |
| `GET /api/v1/invitations/{code}` | **쓰지 않는다.** 초대를 **받는** 쪽의 미리보기 API다 — [SYS-010] 소관 |

**swagger가 적은 것**

- `Room.inviteCode` — `string`, `maxLength: 16`. 설명: *"초대 링크 `gguk.org/r/{code}`의 code. 개인방 코드는 초대에 사용 불가. invitation 테이블/리소스로 분리되면 이 필드는 응답에서 빠질 수 있다."*
- 방 생성 설명: *"생성자가 방장(`owner_id`)이 되고 `invite_code`가 발급된다."* → 공동방 폼이 만든 방에는 코드가 이미 붙어 있다.

**어긋나는 것 · 미확정인 것**

| # | 무엇 | 이 계약의 처리 |
|---|---|---|
| 1 | 링크의 **호스트와 경로**가 스키마가 아니라 설명 문장에만 있다. flavor별 호스트도 정해진 바 없다 | 조립을 `InviteLinkBuilder` 구현 한 파일에 가둔다(§3). 값 확정은 [열린 항목 D](../research.md#열린-항목) |
| 2 | `inviteCode`가 **응답에서 빠질 수 있다**고 swagger가 예고한다 | 빠지면 고칠 자리가 `RoomMapper`와 조회 경로 하나다. 도메인 모델의 필드 이름은 그대로 둘 수 있다 |
| 3 | 개인방 코드는 초대에 쓸 수 없다 | 이 스텝이 조회하는 것은 **공동방** id뿐이다(`createdRoomId`). 개인방 id가 이 경로에 들어올 통로가 없다 |

---

## 2. `GetInviteLinkUseCase`

`:core:domain/usecase/GetInviteLinkUseCase.kt`

```
class GetInviteLinkUseCase @Inject constructor(
    private val roomRepository: RoomRepository,
    private val inviteLinkBuilder: InviteLinkBuilder,
) {
    suspend operator fun invoke(roomId: String): String
}
```

**하는 일**: `roomRepository.getRoom(roomId)`로 방을 조회하고, 그 `inviteCode`를 `inviteLinkBuilder.build(code)`에 넘겨 링크 문자열을 돌려준다.

| 항목 | 내용 | 근거 |
|---|---|---|
| 성공 | 완성된 링크 문자열 | FR-008·TS-020 |
| 실패 | `MinoDomainException`(`Network`·`Http`) throw. **`null`이나 빈 문자열로 뭉개지 않는다** | EC-008 |
| 캐시 | 하지 않는다. 화면이 한 번 받은 값을 `UiState`에 든다 | EC-012 — 백그라운드에서 돌아와도 다시 확보하지 않는다 |

**UseCase로 분리한 이유**: [`core/domain/README.md`](../../../../core/domain/README.md) §4의 네 조건 중 "단순 표시"와 "재사용 없음"을 만족하지 않는다 — 응답 필드를 그대로 보여주는 것이 아니라 다른 협력자(`InviteLinkBuilder`)와 조합하고, 방 상세의 초대 시트([SYS-006] Flow B)가 같은 행위를 쓴다.

**`RoomRepository`를 새로 만들지 않는다.** `getRoom(roomId): Room`은 [공동방 폼 계획](https://github.com/mash-up-kr/Team-MINO-Android/issues/146)의 `contracts/room-repository.md`가 소유한다. 이번 범위가 그 인터페이스에 더하는 함수는 없다.

---

## 3. `InviteLinkBuilder`

`:core:domain/InviteLinkBuilder.kt` (인터페이스) · `:core:data`의 구현

```
interface InviteLinkBuilder {
    fun build(inviteCode: String): String
}
```

| 계층 | 무엇을 아는가 |
|---|---|
| `:core:domain` — 인터페이스 | "코드로부터 링크가 만들어진다"는 사실만 안다. 호스트도 경로도 모른다 |
| `:core:data` — `InviteLinkBuilderImpl` | 호스트와 경로 형식을 안다. 호스트는 flavor 빌드 설정에서 온다 |

**이렇게 가른 이유**([초대 링크 조립 ADR](../../../adr/2026-08-24-invite-link-assembly-domain-interface.md) · 경위는 [research.md R-011](../research.md)): 코드는 서버가 주고 호스트는 빌드 설정이 안다. 도메인이 URL 상수를 들면 `:core:domain`이 배포 환경을 알게 되고, 화면이 조립하면 서버 소유의 형식이 UI로 샌다. 이 저장소는 이미 `HttpClient`의 baseUrl을 flavor BuildConfig로 다루고 있어 같은 자리가 있다.

**호스트 값은 이 계획이 정하지 않는다.** swagger가 설명 문장에 적은 `gguk.org/r/{code}`가 현재 알려진 전부다 — [열린 항목 D](../research.md#열린-항목). 구현 착수 시 확정한다.

**단위 테스트 대상**: 주어진 코드로 만들어진 문자열이 기대 형식과 일치하는지 1건(구현 모듈). `GetInviteLinkUseCase`는 Fake `RoomRepository` + Fake `InviteLinkBuilder`로 성공·실패 2건.

---

## 4. `Room` 모델과 DTO의 확장

| 파일 | 모듈 | 변경 |
|---|---|---|
| `model/Room.kt` | `:core:domain` | `inviteCode: String` 필드 추가 |
| `network/dto/response/RoomResponse.kt` | `:core:data` | `inviteCode` 필드 추가 |
| `repository/mapper/RoomMapper.kt` | `:core:data` | 1:1 매핑 추가 |

세 파일 모두 공동방 폼 계획이 만드는 것이고, 그 계획의 `data-model.md` §2가 `inviteCode`를 **"다른 feature가 필요로 할 때 필드를 더한다"**로 열어 두었다. 이번 범위가 그 다른 feature다([research.md R-010](../research.md)).

**나머지 서버 필드는 여전히 넣지 않는다** — `type`·`createdAt`·`pinCount`·`memberCount`. [`core/domain/README.md`](../../../../core/domain/README.md) §5의 "서버 전용 필드는 도메인 모델에 포함하지 않는다".

---

## 5. 실패의 성격

| 상황 | 통로 | UI | 근거 |
|---|---|---|---|
| 진입 시 링크 확보 실패 | `UiState.inviteLink = null`(State) | 화면은 그대로. 에러 화면으로 갈아 끼우지 않는다 | EC-008·UX-002 |
| 링크가 없는 상태에서 두 액션 중 하나가 눌림 | `DomainErrorEmitter` → Route가 스낵바 | 실패를 알리고 재확보를 시도한다. 공유 시트를 열거나 클립보드에 쓰지 않는다 | EC-008 |
| 우상단 [X] | — | 링크 상태와 무관하게 언제나 튜토리얼로 나아간다 | FR-013·EC-011 |

이것은 [`error_handling.md`](../../../conventions/error_handling.md) §5의 두 통로 중 어느 쪽에도 그대로 맞지 않는 **경계 사례**이며, 같은 문서 §8이 "첫 적용 화면 구현 시 결정한다"로 열어 둔 자리다. 판단 근거는 [research.md R-012](../research.md).

리프는 `MinoDomainException.Network` · `Http(code)` 둘뿐이다. 문구 매핑의 소유자는 `error_handling.md` §5·§8이 정한다.

---

## 6. 이 계약이 지켜지는지 보는 법

| 확인 | 방법 |
|---|---|
| 도메인이 URL을 모른다 | `:core:domain` 어디에도 `http`·`gguk` 문자열이 없다 |
| 화면이 링크를 조립하지 않는다 | `:feature:onboarding`에 문자열 연결로 URL을 만드는 코드가 없다 |
| 잘못된 링크가 나가지 않는다 | 공유·복사 실행 지점이 `inviteLink != null` 가드 안에 있다 |
| 방을 두 번 만들지 않는다 | `:feature:onboarding`에 `createRoom`·`RoomRepository` 직접 호출이 없다 |
