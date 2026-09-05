# 계약: 도메인 레이어 (`:core:domain`)

**대상 스펙**: [spec.md](../spec.md) 7.0.0 · **계획**: [plan.md](../plan.md)

알림함이 세우는 도메인 계약이다. 기존 `RoomRepository`·`PlaceRepository`와 같은 규약을 따른다 — **`Flow`를 흘리지 않고 실패를 `Result`로 감싸지 않으며**, `MinoDomainException`으로 던지고 취소는 그대로 전파한다.

---

## 1. `NotificationRepository`

```
interface NotificationRepository {
    suspend fun getNotifications(page: Int): NotificationPage
}
```

### `getNotifications`

FR-018의 한 묶음을 가져온다.

- **`pageSize`를 받지 않는다.** 서버 기본값을 따르는 것이 spec §4의 전제이고, 인자로 열어 두면 호출부가 20을 박게 된다([contracts/notification-api.md §1](./notification-api.md)).
- **정렬 책임을 갖지 않는다.** 서버가 최신순으로 주고 받은 순서를 그대로 돌려준다(FR-001) — `RoomRepository.getRooms`와 같은 규약이다.
- **알 수 없는 `type`의 항목은 버린다.** 문구도 도착지도 정할 수 없어 행으로 그릴 수 없기 때문이며, 목록 전체를 실패로 만들지는 않는다. 이 판정은 Mapper가 집행한다 — `RoomSummaryMapper`가 "방 하나의 값이 어긋났다는 이유로 목록 전체가 실패하면 안 된다"로 세운 규칙과 같다.
- **대상 식별자가 없는 항목도 버린다.** 유형은 알아도 `payload`에 그 유형이 요구하는 식별자가 없으면(장소 대상인데 `pinId`가 없거나 공동방 참가인데 `roomId`가 없으면) 그 항목을 버린다. 남길 수 없는 이유는 `NotificationTarget.None`이 저장 오류의 자리이고([data-model.md §1.3](../data-model.md)) [§2](#2-resolvenotificationdestinationusecase)가 그것을 `SaveErrorGuide`로 해석하기 때문이다 — `None`으로 메우면 장소·방 알림이 저장 오류 안내로 열린다. 앞 줄과 같이 목록 전체를 실패로 만들지 않으며, 집행 위치도 Mapper로 같다.

> **버림이 둘이므로 받은 건수와 `hasNext`를 대조해 끝을 판정하면 안 된다.** 위 두 조건 중 하나라도 걸리면 한 묶음의 항목 수가 서버가 보낸 수보다 적어진다. 다음 묶음이 있는지는 `NotificationPage.hasNext`만 말한다.

- 실패는 던진다. 빈 목록으로 수렴시키는 것은 화면의 몫이 아니다 — UX-002가 "알림이 없다"와 "못 불러왔다"를 구분하라고 요구하므로 둘을 같은 값으로 만들면 안 된다.

읽음 처리·삭제·수신 설정 함수를 두지 않는다. spec §3.2가 셋 모두 범위 밖으로 뺐고, FR-016이 읽음이라는 상태 자체를 없앴다.

---

## 2. `ResolveNotificationDestinationUseCase`

```
class ResolveNotificationDestinationUseCase @Inject constructor() {
    operator fun invoke(notification: Notification): NotificationDestination
}
```

알림 하나를 실제로 열 화면으로 바꾼다. FR-005·FR-022가 이 안에서 판정된다.

**`suspend`가 아니고 저장소를 주입받지 않는다.** spec 5.0.0에서 `payload`가 도착지 핀을 직접 주므로 조회할 것이 없다 — 유형과 대상만 보는 순수 매핑이다([research.md D8](../research.md)). `ResolvePushDestinationUseCase`(이슈 #275)가 푸시 쪽에서 같은 모양으로 서 있다.

### 판정

`Notification.target` 하나로 전부 갈린다. **조회도 분기 조건도 더 없다.**

| 대상 | 결과 |
|---|---|
| `None`(저장 오류) | `SaveErrorGuide` — 그래프 내부 전환(FR-010) |
| `Room(roomId)` | `RoomDetail(roomId)` — 호출부가 `RoomDetailRequestHolder`로 배선한다([research.md D10](../research.md)) |
| `Pin(pinId)` | `PlaceDetail(pinId)` — 호출부가 `PlaceDetailRequestHolder`로 배선한다([research.md D14](../research.md)) |

`Unreachable`을 두지 않는다. **spec 7.0.0이 대상 소멸 판정을 도착지 화면의 몫으로 옮겼기 때문이다**(spec UX-006·EC-009·EC-010·§3.2). 알림함은 이동 전에 대상이 살아 있는지 되묻지 않으므로, 이 UseCase가 낼 수 있는 갈래는 위 셋뿐이다. push-notification spec EC-010도 같은 결론이다.

> **4.1.1까지 여기 있던 절차가 통째로 사라졌다.** `showHasPlaceId` 조회, 후보 방 필터링, 표시 기준 방 조회, 핀 상세 N+1 병렬 호출, 기본값 판정 다섯 단계가 `payload.pinId` 하나로 대체됐다([research.md D6·D7](../research.md)).

### 이 UseCase가 하지 않는 것

- **`recordAccess`를 부르지 않는다.** 「경과일 초기화 확인」은 [SCR-006]의 몫이다([research.md D9](../research.md)).
- **표시 기준 방을 읽지 않는다.** spec 5.0.0이 그 판정을 서버로 옮겼다(spec §3.2).
- **방 선택을 사용자에게 묻지 않는다.** FR-022가 방을 고르는 단계를 금지한다(SC-014).
- **네트워크를 타지 않는다.** 그래서 EC-011의 중복 탭 방어는 "조회 중 재진입"이 아니라 **이동 요청이 두 번 나가는 것**만 막으면 된다([contracts/notification-ui.md §2](./notification-ui.md)).
- **대상이 아직 있는지 확인하지 않는다.** spec 7.0.0 UX-006이 그 판정을 도착지 화면으로 옮겼다.

---

## 3. 기존 계약의 변경

| 대상 | 변경 | 근거 |
|---|---|---|
| — | — | — |

**이 표는 비었다.** 1.0.0은 `RoomRepository.getRooms()` 확장과 `MinoRoomThumbnail`의 `size` 파라미터 둘을 요구했으나, spec 5.0.0(도착지 역산 제거)과 6.0.0(썸네일 합성 제거)이 두 근거를 차례로 없앴다([research.md D5·D6·D7·D13](../research.md)). **이 spec은 기존 모듈의 공개 계약을 바꾸지 않는다.**

---

## 4. DI 바인딩 소유

[`dependency-injection.md`](../../../conventions/dependency-injection.md)와 헌법 원칙 II에 따라 **구현을 가진 모듈이 자신의 `di/`에서 바인딩을 소유한다.**

| 인터페이스 | 구현 | 바인딩 위치 |
|---|---|---|
| `NotificationRepository` | `NotificationRepositoryImpl` | `:core:data`의 `repository/di` |
| `NotificationRemoteDataSource` | `NotificationRemoteDataSourceImpl` | `:core:data`의 `datasource/di` |

`ResolveNotificationDestinationUseCase`는 `@Inject` 생성자를 가진 클래스라 바인딩이 필요 없다 — 기존 UseCase들과 같다.

`:app`은 그래프를 조립할 뿐 바인딩을 두지 않는다.
