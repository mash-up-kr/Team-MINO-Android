# 리서치: 알림함 (Notifications)

**대상 스펙**: [spec.md](./spec.md) 7.0.0 · **계획**: [plan.md](./plan.md)

각 항목의 `(plan X.Y.Z)`는 그 결정이 확정된 plan 버전이다. 결정이 뒤집히면 항목을 지우지 않고 취소선과 함께 `재검토됨`으로 표시한 뒤 새 항목을 덧붙인다.

---

## D1. 탭 feature 모듈 `:feature:notifications`를 신설한다 (plan 1.0.0)

- **Decision**: 알림 탭을 `:feature:notifications` 단일 모듈로 만든다. 밖으로 여는 표면은 진입 Route `NotificationGraph`와 등록 함수 `NavGraphBuilder.notificationGraph(...)` 둘뿐이며, `:feature:main`의 placeholder Route `Notification`과 그 `screen<Notification>` 등록을 걷어낸다.
- **Rationale**: [`feature-module.md`](../../architecture/feature-module.md)와 [`feature-navigation.md`](../../architecture/feature-navigation.md)가 정한 탭 feature 골격이고, `:feature:home`의 `HomeGraph`/`homeGraph()`가 같은 모양의 선례다. `MainNavHost`의 주석도 "모듈이 생기면 홈처럼 그 모듈의 등록 함수 호출로 교체하고 Route 소유도 그쪽으로 옮긴다"로 이 경로를 지목한다.
- **Alternatives considered**: `:feature:main` 안에 화면을 두기 — 기각. 헌법 원칙 II의 모듈 경계와 위 두 규약을 정면으로 어긴다. 알림함을 `:feature:home`에 얹기 — 기각. 서로 다른 탭이고 공유하는 상태가 없다.

## D2. 저장 오류 안내 화면은 같은 그래프의 두 번째 목적지로 둔다 (plan 1.0.0)

- **Decision**: `NotificationGraph` 안에 `NotificationMain`·`SaveErrorGuide` 두 목적지를 두고, 저장 오류 알림 클릭은 그래프 **내부** 전환으로 처리한다. 모듈 밖으로 콜백을 내보내지 않는다.
- **Rationale**: FR-011·UX-008·TS-030이 요구하는 "바텀 네비게이션이 계속 보이고 `알림` 탭이 선택 상태"가 **배선 없이 성립한다.** `MainShell`은 바텀 바를 항상 그리고 `currentTab()`이 `hierarchy`를 훑어 상위 탭을 찾으므로(`MainTabNavigation.kt`), 같은 그래프 하위 목적지면 탭 선택 상태가 유지된다. TS-031(다른 탭 다녀오기)도 탭 상태 저장·복원(`navigateToTab`의 `saveState`/`restoreState`)이 그대로 덮는다.
- **Alternatives considered**: 별도 Activity로 띄우기 — 기각. Activity로 나가면 바텀 네비게이션이 사라져 FR-011과 정반대가 된다. 목록 화면 안의 오버레이 상태로 두기 — 기각. UX-008이 "목록을 대체하지 않고 그 위에 쌓이는 화면"이라 뒤로가기가 목적지 단위로 동작해야 하고(EC-014), 상태로 두면 시스템 뒤로가기를 직접 가로채야 한다.

## D3. 페이징은 Paging3 없이 ViewModel이 직접 이어 붙인다 (plan 1.0.0)

- **Decision**: `page`를 `NotificationViewModel`이 들고, 목록 끝 도달 Intent에서 다음 페이지를 요청해 기존 목록 뒤에 이어 붙인다. Paging 라이브러리를 도입하지 않는다.
- **Rationale**: ① 서버가 `hasNext`를 주므로 종료 판정에 라이브러리가 필요 없다. ② UX-012·EC-016이 요구하는 "추가 로드 실패는 이미 그린 목록을 지우지 않고 **목록 끝에서만** 알린다"가 직접 제어에서 그대로 표현된다 — `LoadState.append`를 화면 상태로 번역하는 층이 없다. ③ 저장소에 Paging 의존성 선례가 없어(`libs.versions.toml` 미포함) 도입 자체가 헌법 원칙 III의 ADR 대상이 되는데, 이 화면 하나를 위해 치를 비용이 아니다.
- **Alternatives considered**: `androidx.paging` 도입 — 기각. 위 ③. 전체 일괄 로드 — 기각. FR-018이 20건 단위 페이징을 명시한다.

## D4. 유형 문구는 서버 `typeLabel`을 그대로 쓴다 (plan 1.0.0)

- **Decision**: FR-004의 6종 문구를 클라이언트가 갖지 않고 응답의 `typeLabel`을 그대로 렌더링한다. 응답의 `type` enum은 **도착지 판별에만** 쓴다.
- **Rationale**: 공동방 참가 ①의 `{참가자 이름}님이 들어왔어요`는 가변 문구인데 `payload`에 참가자 이름이 없다. 클라이언트가 조립할 재료가 없으므로 서버가 완성해 주는 문자열을 쓰는 것 외에 선택지가 없다. 응답 스키마의 `typeLabel` example이 `이미 저장해둔 곳이에요`로 spec FR-004 표와 일치해 서버가 그 표를 구현하고 있음이 확인된다.
- **파급**: SC-003(6종 문구가 시안과 100% 일치)의 검증이 **서버 응답에 의존한다.** 클라이언트 단위 테스트로는 지킬 수 없고 quickstart의 실기기 확인으로만 판정된다 — [quickstart.md](./quickstart.md) §3.1.
- **Alternatives considered**: `type` enum으로 클라이언트가 문구를 결정 — 기각. 참가자 이름을 채울 수 없다. `typeLabel`을 받되 클라이언트 문구와 대조해 어긋나면 클라이언트 것을 쓰기 — 기각. 두 벌의 문구가 생겨 헌법 원칙 I(SSOT)을 어기고, 어긋남을 조용히 덮는다.

## D5. ~~썸네일은 유형으로 세 갈래를 가르고, 방 썸네일은 방 목록에서 합성한다~~ (plan 1.0.0 / **1.2.0 폐기**)

- **Decision (1.2.0)**: **합성하지 않는다.** 썸네일은 두 갈래다 — 저장 오류만 고정 오류 아이콘이고, 나머지 5종은 응답의 `thumbnailUrl`을 그대로 그리며 `null`이면 플레이스홀더다. 방 목록 조회(`GET /api/v1/rooms`)를 하지 않는다.
- **Rationale**: spec 6.0.0이 FR-012를 「서버가 준 이미지 한 장」으로 바꿨다(§5 Q13). 1.0.0의 합성은 「방 썸네일」(콜라주 / 대표 색상+캐릭터)을 단일 필드로 표현할 수 없다는 제약을 클라이언트가 떠안은 것이었는데, 그 요구 자체가 사라졌다.
- **얻는 것**: **알림 탭이 부르는 API가 하나로 줄었다.** 목록 진입 시 병렬 두 요청이 한 요청이 되고, `roomId → RoomSummary` 맵과 그 생애주기 관리(페이지를 이어 붙일 때의 재사용)가 통째로 없어진다. SC-001(2초)·SC-011(첫 화면에 필요한 만큼만)도 그만큼 여유가 생긴다.
- **잃는 것**: 장소가 0개인 공동방을 가리키는 알림 행에 그 방의 대표 색상+캐릭터가 나오지 않는다. 서버가 그 방의 이미지를 주지 못하면 플레이스홀더가 선다 — spec §5 Q13이 이 대가를 명시적으로 감수했다.
- **알림함은 방의 대표 색상을 읽지 않는다.** `RoomSummary.color`도, `thumbnailList`의 색상 키 폴백도 이 화면과 무관해졌다.

## D6. ~~표시 기준 방 저장소를 이번 범위에서 세운다~~ (plan 1.0.0 / **1.1.0 폐기**)

- **Decision (1.1.0)**: **세우지 않는다.** `PlaceRoomContextRepository`·`PlaceRoomContextLocalDataSource`와 그 DataStore 구현, 두 DI 바인딩을 전부 걷어낸다. 알림함은 장소별 표시 기준 방을 읽지 않는다.
- **Rationale**: spec 5.0.0이 FR-022의 도착지 방 판정을 서버로 옮겼다 — 알림 `payload`가 `pinId`를 실어 주므로 앱이 방을 정할 일이 없다([contracts/notification-api.md §1](./contracts/notification-api.md)). 세울 근거였던 요구사항이 사라졌다.
- **1.0.0의 판단이 틀렸던 것은 아니다.** 당시 조회한 스키마에는 `pinId`가 없었고, 그 스키마 위에서는 앱이 방을 역산하는 수밖에 없었다. 서버가 필드를 더하면서 전제가 바뀐 것이다. 다만 1.0.1이 발견한 사실(쓰기 주체인 [SCR-006]이 전환을 구현하고도 값을 영속하지 않는다)은 이 저장소가 애초에 성립하기 어려웠음을 함께 말해 준다 — 세웠다면 아무도 쓰지 않는 죽은 바인딩이 됐을 것이다.
- **Alternatives considered**: 계약만 남기고 구현을 비우기 — 기각. 읽는 곳도 쓰는 곳도 없는 계약이다. 나중을 위해 세워 두기 — 기각. 필요해지는 시점에 그 요구사항이 근거를 갖고 세우면 된다.

## D7. ~~「가장 최근에 저장한 방」은 매칭 핀의 `createdAt`으로 판정한다~~ (plan 1.0.0 / **1.1.0 폐기**)

- **Decision (1.1.0)**: **판정하지 않는다.** `GET /api/v1/rooms?showHasPlaceId={placeId}` 조회도, 후보 방마다 `GET /api/v1/pins/{pinId}`를 병렬 호출하는 N+1도 두지 않는다.
- **Rationale**: D6과 같다. 이 절차는 `placeId`에서 방을 역산하기 위한 것이었고, `payload.pinId`가 그 결과를 직접 준다. 서버 협의 항목으로 올려 두었던 `matchedPinSavedAt` 요청도 함께 소멸한다.
- **얻는 것**: 도착지 판정에서 **네트워크 조회가 완전히 사라진다.** 알림 하나를 눌렀을 때 최대 1+N회 나가던 요청이 0회가 되고, SC-004(1회 탭으로 도달)와 EC-011(빠른 두 번 탭)의 위험도 함께 줄어든다.

## D8. 도착지 해석은 UseCase가 소유하되 순수 매핑이다 (plan 1.0.0 / 1.1.0 축소)

- **Decision**: `ResolveNotificationDestinationUseCase`를 `:core:domain`에 두고 알림 하나를 받아 `NotificationDestination`을 돌려준다. **1.1.0에서 이 UseCase는 `suspend`가 아니고 저장소를 주입받지 않는다** — 유형과 `payload`만 보고 갈래를 정하는 순수 함수다.
- **Rationale**: 1.0.0은 두 저장소 조회와 네 갈래 분기를 엮느라 이 자리가 필요했다. 5.0.0에서 조회가 모두 사라졌지만 자리는 그대로 둔다 — 유형 6종을 도착지 세 갈래로 가르는 판정은 여전히 화면 상태 관리와 섞이면 안 되는 도메인 규칙이고, `ResolvePushDestinationUseCase`(`:core:domain`, 이슈 #275)가 푸시 쪽에서 정확히 같은 모양으로 이미 서 있다.
- **두 UseCase를 합치지 않는다.** 입력이 다르다 — 푸시는 `PushMessage`(FCM data), 알림함은 `Notification`(REST 응답)이고, 도착지도 갈린다(저장 오류가 푸시는 알림 탭, 알림함은 안내 화면). 같은 규칙이 아니라 **같은 모양의 다른 규칙**이다.
- **Alternatives considered**: ViewModel이 직접 조립 — 기각. 도메인 규칙이 화면에 샌다. UseCase를 없애고 매퍼로 흡수 — 기각. 매퍼는 응답을 모델로 바꾸는 자리이고, 어느 화면으로 가는지는 그와 다른 판단이다.

## D9. 「경과일 초기화 확인」은 알림함이 호출하지 않는다 (plan 1.0.0)

- **Decision**: 알림을 눌러 장소 상세로 보낼 때 `PlaceRepository.recordAccess`를 부르지 않는다.
- **Rationale**: spec §3.2가 이 이벤트를 [SCR-006] 장소 상세의 몫으로 명시했고, 실제로 `PlaceRepository.recordAccess`가 이미 존재해 장소 상세 진입 시 호출된다. 알림함이 한 번 더 부르면 append-only 로그에 중복 기록이 쌓인다.
- **Alternatives considered**: 알림함에서도 호출 — 기각. 위와 같음.

## D10. 방 대상 알림은 `RoomDetailRequestHolder`로 저장 탭에 요청한다 (plan 1.0.0 보류 → 1.0.1 배선)

- **Decision (1.0.1)**: 공동방 참가 ①② 알림의 클릭을 `RoomDetailRequestHolder.request(roomId)` + 저장 탭 전환으로 배선한다(FR-005). 알림 모듈은 `onNavigateToRoomDetail: (roomId) -> Unit` 콜백만 올리고, 홀더 적재와 탭 전환은 셸이 한다 — 탭 목록을 아는 것은 셸뿐이다([`feature-navigation.md`](../../architecture/feature-navigation.md) 3장).
- **Rationale**: 1.0.0의 보류 근거("`RoomDetail` 화면·Route·Launcher가 저장소 어디에도 없다")가 사라졌다. [SCR-005] 방 상세는 `feature/room/.../detail/`에 있고, 푸시 알림 딥링크(이슈 #275)가 같은 도착지를 열려고 `:core:navigation`에 `RoomDetailRequestHolder`를 세워 `MainActivity`까지 배선해 두었다. 알림함은 그 홀더를 그대로 나눠 쓰면 되고 새로 만들 계약이 없다.
- **방 상세는 별도 목적지가 아니다.** `RoomListRoute`가 `selectedRoomId` 로컬 상태로 여닫으므로(`RoomNavigation.kt`), 탭 밖에서 여는 경로는 Route 인자가 아니라 홀더를 지난다 — `navigateToTab`의 `restoreState`가 복원된 항목의 옛 인자를 되살리기 때문이며, 장소 상세가 홀더를 쓰는 이유와 같다([ADR 2026-09-02](../../adr/2026-09-02-immersive-map-screen-shares-one-map-in-tab-feature.md)).
- **파급**: TS-023·TS-024·TS-044·EC-010이 이번 라운드의 검증 대상으로 **들어온다** — [quickstart.md](./quickstart.md) §5에서 빠진다.
- **Alternatives considered**: 보류 유지 — 기각. 배선할 대상이 생겼는데 FR-005의 절반을 비워 두면 목록에 그려진 행이 눌러도 아무 일도 하지 않는다. 알림 모듈이 홀더를 직접 주입받기 — 기각. feature가 탭 목록을 몰라 전환을 못 하므로, 콜백 하나를 올리는 편이 홈·푸시가 이미 쓰는 형태와 같다.

## D11. 실패의 성격이 통로를 가른다 (plan 1.0.0)

- **Decision**: [`error_handling.md`](../../conventions/error_handling.md)를 따라 세 갈래로 가른다 — ① 첫 페이지 로드 실패는 `NotificationUiState.loadError`(UX-002·EC-001) ② 추가 페이지 로드 실패는 `NotificationUiState.appendError`(UX-012·EC-016) ③ 이동 대상 소멸(EC-009·EC-010)은 `DomainErrorEmitter`로 흘려 Route가 수집한다(UX-006).
- **Rationale**: `HomeViewModel`이 이미 "주 데이터 로드 실패는 상태에, 사용자 액션의 일회성 실패는 `DomainErrorEmitter`로"라는 같은 갈림을 쓴다. ①과 ②를 한 필드로 합치면 UX-012가 요구하는 "이미 본 알림을 지우지 않는다"를 상태로 표현할 수 없다.
- **Alternatives considered**: 모두 `DomainErrorEmitter` — 기각. 재시도 가능한 오류 화면(UX-002)이 상태로 남아야 하는데 일회성 신호로는 못 그린다.

## D12. 경과 시간은 목록을 받은 시점에 한 번만 계산한다 (plan 1.0.0)

- **Decision**: `createdAt`을 FR-003의 네 구간 문자열로 바꾸는 계산을 응답 → UI 모델 변환 시점에 한 번 수행하고, 화면에 머무는 동안 다시 계산하지 않는다. 계산은 `:feature:notifications` 안의 포맷터가 갖는다.
- **Rationale**: EC-005가 "화면에 머무는 동안 다시 계산하지 않고, 목록을 다시 불러오는 시점에 갱신한다"를 명시한다. 표기 규칙은 이 화면 전용(코멘트는 [SCR-006]의 다른 규칙을 쓴다 — PRD [SCR-006] Flow F)이라 공용 모듈로 올리지 않는다([`component-asset-placement.md`](../../conventions/component-asset-placement.md)).
- **Alternatives considered**: 1분마다 재계산 — 기각. EC-005와 정반대다. 서버가 표기 문자열을 주기 — 기각. 기기 로컬 시간대 기준이라는 §4 가정과 어긋나고, 목록을 오래 열어 두면 서버 계산도 낡는다.

## D13. ~~`MinoRoomThumbnail`에 크기 파라미터를 넓힌다~~ (plan 1.0.0 / **1.2.0 폐기**)

- **Decision (1.2.0)**: **넓히지 않는다.** 알림 행이 `MinoRoomThumbnail`을 쓰지 않으므로 `:core:design-system`에 손댈 것이 없다.
- **Rationale**: D5 폐기로 방 썸네일 컴포넌트를 부를 일이 사라졌다. 알림 행의 썸네일은 이미지 한 장(`MinoAsyncImage`)이거나 고정 오류 아이콘이다.
- **파급**: 이 spec이 `:core:design-system`에 남기는 변경이 **0건**이 되었다. 56dp가 필요해지는 다른 화면이 생기면 그때 그 spec이 근거를 갖고 넓히면 된다.

## D14. 장소 상세 진입은 Activity가 아니라 저장 탭 홀더를 지난다 (plan 1.0.1)

- **Decision**: 장소 대상 알림의 클릭을 `PlaceDetailRequestHolder.request(pinId, PlaceDetailEntryOrigin.NOTIFICATION)` + 저장 탭 전환으로 배선한다. D10과 같은 형태이며 `origin`을 함께 싣는 것만 다르다.
- **Rationale**: plan 1.0.0이 전제한 「장소 상세 = 별도 Activity, `PlaceDetailLauncher`로 연다」는 **2026-09-02에 폐기됐다**([실패 기록](../../failures/2026-09-02-entry-feature-for-place-detail.md) · [ADR](../../adr/2026-09-02-immersive-map-screen-shares-one-map-in-tab-feature.md)). `:feature:placedetail`과 `PlaceDetailLauncher`·`EXTRA_PLACE_DETAIL_PIN_ID`는 저장소에서 사라졌고 장소 상세는 저장 탭 그래프 안의 화면이 되었다. 이 spec의 설계가 그 하루 전(2026-09-01)에 서서 낡은 구조를 인용하고 있었다.
- **`PlaceDetailEntryOrigin.NOTIFICATION`은 이미 있다.** 장소 상세 [나가기]의 목적지가 진입 경로에 따라 갈리므로(place-detail spec FR-009) 홀더가 출처를 함께 싣는데, `HOME`만 홈 탭으로 되돌리고 `NOTIFICATION`은 방 상세로 나간다 — FR-020이 요구하는 그대로다.
- **파급**: 바텀 네비게이션이 감춰지는 것도 「별도 Activity 위라서」가 아니라 `ImmersiveRouteRegistry`·`LocalBottomNavVisibility`가 하는 일이다. 어느 쪽이든 알림 모듈이 코드를 더하지 않는다는 결론은 같다(FR-020·TS-042).
- **Alternatives considered**: 없다. 폐기된 구조를 되살릴 수 없다.

---

## 미해소 항목

| 항목 | 상태 | 처리 |
|---|---|---|
| ~~알림 응답 `thumbnailUrl`로 방 썸네일을 실을 수 없음~~ | **해소(spec 6.0.0)** | FR-012가 「서버가 준 이미지 한 장」으로 바뀌어 요구 자체가 사라졌다(D5 폐기) |
| ~~방 목록 `showHasPlaceId` 응답에 핀 저장 시각 없음~~ | **해소(1.1.0)** | D7 폐기 — 방을 역산하지 않으므로 이 조회 자체를 안 쓴다 |
| ~~표시 기준 방이 항상 비어 있음~~ | **해소(1.1.0)** | D6 폐기 — 알림함이 이 값을 읽지 않는다 |
| ~~도착지 방을 앱이 정할지 서버가 정할지~~ | **확정(spec 5.0.0)** | 서버가 정한다. `payload.pinId`가 도착지 방을 지목한다 |
| FR-021의 저장 시도 단위 묶음을 서버가 수행하는지 | **미확인(협의 대상에서 내림)** | 응답 스키마로는 판별할 수 없고 묶음을 나타내는 필드도 없다. **사용자 판단으로 spec §4 가정("묶는 일은 서버가 한다")을 신뢰하고 진행한다**(spec 6.0.0 §5 Q13). 실제 데이터 확인만 [quickstart.md](./quickstart.md) §4.3에 남는다 |

## 인접 spec에 전할 것

- **~~[place-detail] D10의 차단 사유가 해소되었다.~~ (1.0.1 — 전달 완료, 그쪽이 이미 구현했다)** `GET /api/v1/rooms?showHasPlaceId={placeId}`가 방마다 `matchedPinId`·`hasPlace`를 돌려준다는 사실 위에서 place-detail이 [저장된 방] 전환(FR-025)과 「이미 저장된 방」 표시를 구현했고, `RoomRepository.getRooms(placeId)`·`RoomSummary.hasPlace`·`matchedPinId` 확장도 함께 들어왔다. 알림함이 세울 것이 아니라 **이미 있는 것을 쓴다**([contracts/notification-repository.md §4](./contracts/notification-repository.md)).
- **[place-detail]·[room-detail] 「없는 대상」 처리를 재시도 불가로 구분해야 한다.** spec 7.0.0이 대상 소멸 판정을 도착지 화면의 몫으로 옮겼는데(§5 Q14), 지금 `PlaceDetailViewModel`은 `getPlaceDetail(pinId)` 실패를 `loadError`에 담아 **재시도 가능한 오류**로 그린다 — 삭제된 핀은 재시도해도 영원히 실패하고 사용자가 그 화면에 갇힌다. **두 spec이 「없어진 대상」과 「일시적 실패」를 갈라 안내하고 나가기를 주어야 이 결정이 닫힌다.** 알림뿐 아니라 지도 마커·방 상세 목록·[SCR-003] 홈 카드로 들어와도 같은 상황이라, 네 진입점이 함께 낫는다.
- **~~[push-notification] 도착지 방 판정이 두 스펙에서 갈린다.~~ (1.1.0 — 해소)** spec 5.0.0이 FR-022를 「알림이 실어 온 핀의 방」으로 바꾸면서 그쪽 FR-013과 규칙이 같아졌다. 두 경로 모두 서버가 지목한 `pinId`로 같은 화면을 연다. **push-notification spec은 이제 EC-012의 「표시 기준 방과 달라도」라는 단서를 손볼 수 있다** — 앱 안 경로에도 표시 기준 방 판정이 없어졌으므로 그 문장이 비교하는 대상이 사라졌고, SC-005는 성립한다.
