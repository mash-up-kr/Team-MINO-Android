# 리서치: 외부 공유 수신 방 선택 바텀시트

**대상 스펙 경로**: `docs/specs/shared-link-receiver`

**명세서**: [spec.md](./spec.md) · **계획**: [plan.md](./plan.md)

이 문서는 [plan.md](./plan.md)에 종속된 부속 산출물이며 독자 버전을 갖지 않는다. 각 항목은 **어느 plan 버전에서 결정되었는지**를 함께 적는다. 뒤집힌 결정은 지우지 않고 취소선과 `재검토됨(plan X.Y.Z)` 표시를 남긴 뒤 새 항목을 덧붙인다.

---

## R-001. 저장 API — 서버 계약 확장을 전제하고 mock으로 구현한다

**결정(plan 1.0.0)**: 클라이언트가 필요로 하는 계약을 [`contracts/shared-place-save-api.md`](./contracts/shared-place-save-api.md)에 명세해 서버 협의의 근거로 삼고, 그 계약이 붙기 전까지는 mock `SharedPlaceRemoteDataSource` 구현으로 개발한다. `SharedPlaceRemoteDataSource` 인터페이스는 실서버가 붙어도 바뀌지 않으며, 바뀌는 것은 Hilt 바인딩 대상뿐이다.

**근거**: 현행 `POST /api/v1/place/places`는 요청 본문이 `{ url }` 하나뿐이고 `security`가 비어 있다(2026-08-26 `https://api.gguk.org/api-docs-json` 확인). 즉 **어느 방에 저장할지도, 누가 저장하는지도 서버에 전달할 방법이 없다.** FR-007(복수 선택)·FR-010(선택된 모든 방에 저장)이 이 계약 위에서는 성립하지 않는다. `roomIds` 배열을 받는 곳은 `POST /api/v1/pins/{pinId}/duplicate` 하나뿐인데, 이쪽은 이미 존재하는 핀을 복제하는 [SYS-003] 경로다.

`:core:data`에는 이미 `RoomMockRemoteDataSourceImpl`로 같은 방식을 쓴 선례가 있고(`group-room-form`), Ktor `HttpClient`·`MinoIdentityProofPlugin`·`convertDomainException` 인프라가 모두 갖춰져 있어 실계약이 확정되는 순간 `Impl` 하나를 추가하고 바인딩만 바꾸면 된다.

**Alternatives considered**:
- *현행 계약 그대로 설계* — `url`만 보내고 방 선택 결과를 버린다. 시트에서 고른 방이 저장에 반영되지 않아 FR-007·FR-010과 정면으로 어긋나고, 시트 자체의 존재 이유가 사라진다. 기각.
- *2단계 우회(place 생성 → pinId 수신 → duplicate로 배포)* — `POST /api/v1/place/places`가 202 enqueue만 반환하고 `pinId`를 돌려주지 않아 **성립 자체가 불가능하다.** 조회로 pinId를 되찾을 수단도 없다(`GET /api/v1/pins`는 `roomId` 기준). 기각.

---

## R-002. 공유 URL의 종류 판정은 서버에 위임한다

**결정(plan 1.0.0)**: 클라이언트는 추출한 URL의 도메인을 검사하지 않고 그대로 전송한다. 서버가 지원하지 않는 URL이면 4xx로 응답하고, 그 실패는 FR-014의 저장 오류 알림 경로로 흘린다.

**근거**: 현행 `POST /api/v1/place/places`는 `url`에 `pattern: instagram\.com`을 걸고 위반 시 `400 INVALID_INSTAGRAM_URL`을 낸다. 반면 spec §4 가정은 "인스타그램은 대표 사례일 뿐이며 … 인스타그램 전용 예외 처리는 두지 않는다"이고, §3.2 비목표는 "공유 링크에서 장소를 뽑아내는 분석 규칙(지원 도메인 …)은 서버가 정의한다"이다. 판정을 서버에 두면 두 문장이 그대로 성립하고, 서버가 지원 도메인을 넓혀도 앱을 고치지 않아도 된다.

**Alternatives considered**:
- *클라이언트에서 사전 차단* — 불필요한 왕복이 줄지만 지원 도메인 목록이 클라이언트에 복제된다. 서버가 확장할 때마다 앱 배포가 따라와야 하고, 배포되지 않은 사용자에게는 서버가 지원하는 링크가 거부된다. 기각.

---

## R-003. 방 썸네일은 서버 필드 추가를 요청하고, 그때까지 대표 색상으로 폴백한다

**결정(plan 1.0.0)**: `GET /api/v1/rooms` 응답에 장소 이미지 배열(최대 4장)을 추가해달라고 요청하고 그 계약을 [`contracts/room-list-api.md`](./contracts/room-list-api.md)에 명세한다. 서버 대응 전까지 `MinoRoomThumbnail`은 `color`로 대표 색상 배경을 그리고, 이미지 배열이 채워지면 콜라주로 전환한다.

**근거**: FR-006이 방 카드에 썸네일을 요구하는데 현행 응답에는 `color`·`pinCount`만 있고 콜라주에 쓸 이미지가 없다. PRD 「방 썸네일」이 정의한 두 형태 중 '장소 0개'(대표 색상+캐릭터)는 `color` 하나로 그릴 수 있으므로, 서버가 늦어도 카드가 비어 보이지 않는다.

**Alternatives considered**:
- *방마다 `GET /api/v1/pins` 호출* — 서버 변경이 필요 없지만 방 개수만큼의 N+1 요청이 시트 표출 경로에 들어가 SC-001(1초 이내 조작 가능)을 직접 위협한다. 기각.
- *대표 색상만 쓰고 콜라주를 포기* — 서버 협의가 필요 없지만 장소가 쌓인 방과 빈 방이 영영 같아 보인다. R-001에서 이미 같은 서버 협의가 열리므로 함께 올리는 편이 비용이 낮다. 기각.

---

## R-004. 저장 요청의 생존은 WorkManager가 보장한다

**결정(plan 1.0.0)**: `[저장하기]` 이후의 저장 요청을 `SharedPlaceSaveWorker`(WorkManager)로 넘긴다. 시트를 닫고 Activity가 종료된 뒤에도, 프로세스가 죽은 뒤에도 요청이 살아남는다. `androidx.work:work-runtime-ktx`와 `androidx.hilt:hilt-work`를 버전 카탈로그에 새로 추가한다.

**근거**: spec §4 가정이 "`저장하기`를 누른 뒤 저장 결과가 확정되기 전에 사용자가 앱을 떠나도 저장 요청은 취소되지 않는다"를 못박았다. FR-011은 토스트가 사라지면 꾹의 화면을 남기지 않고 물러난다고 정하므로, 요청이 살아 있어야 하는 구간과 Activity가 살아 있는 구간이 어긋난다. `viewModelScope`는 Activity 종료와 함께 취소되므로 이 가정을 지킬 수 없다.

> **ADR로 승격됨(2026-08-26).** 새 라이브러리 채택이며 다른 feature(알림 예약·백그라운드 동기화)에도 구속력을 갖는다. 결정의 배경·근거·기각한 대안은 이제 [화면보다 오래 살아야 하는 요청은 WorkManager가 소유하고, 재시도 정책을 워커가 든다](../../adr/2026-08-26-workmanager-for-detached-requests.md)가 소유한다. 특히 [익명 세션 확보의 재시도는 호출 화면이 소유한다](../../adr/2026-08-22-session-retry-owned-by-caller.md)와의 경계는 그 ADR이 다룬다.

**Alternatives considered**:
- *Application scope 코루틴(`@Singleton CoroutineScope`)* — 신규 의존성이 없고 구현이 가볍다. 다만 프로세스가 죽으면 요청이 유실되고, 재시도·네트워크 제약을 직접 구현해야 한다. 공유 수신은 프로세스가 방금 뜬 콜드 스타트가 잦아 유실 위험이 상시적이다. 사용자가 WorkManager를 선택해 기각.
- *`viewModelScope` 유지* — §4 가정을 지키지 못한다. 기각.

---

## R-005. 워커는 네트워크 제약 + 지수 백오프로 재시도하고, 4xx는 즉시 실패로 확정한다

**결정(plan 1.0.0)**: `SharedPlaceSaveWorker`에 `NetworkType.CONNECTED` 제약을 걸고, `MinoDomainException.Network`와 5xx `MinoDomainException.Http`는 `Result.retry()`로 지수 백오프에 맡긴다. 4xx `Http`는 재시도해도 결과가 같으므로 `Result.failure()`로 확정한다.

**근거**: EC-009(저장 요청 시점에 네트워크가 끊겨 있다)가 재시도로 구제된다 — 제약이 걸린 워커는 연결이 돌아올 때까지 실행되지 않는다. 반대로 `400 INVALID_INSTAGRAM_URL`·`VALIDATION_ERROR`는 같은 입력으로 몇 번을 보내도 같은 응답이므로 재시도가 배터리만 쓴다. 실패 확정은 FR-014의 오류 알림으로 전달되며 이는 서버가 소유한다(R-006 아래 주석 참고).

**Alternatives considered**:
- *제약 없이 1회 전송* — 구현이 단순하지만 EC-009가 그대로 실패로 확정되어, WorkManager를 도입한 이유의 절반이 사라진다. 기각.
- *방별로 워커 분리* — spec §4 가정("저장은 선택한 방마다 독립적으로 성립한다")을 실행 단위까지 관철한다. 다만 R-001에서 요청할 계약이 `roomIds` 배열 1회 전송이라 방 단위 분해는 서버가 맡는다. 클라이언트가 다시 쪼개면 계약과 어긋나고 요청 수만 늘어난다. 기각.

---

## R-006. 방 목록 조회 실패는 FR-013의 방 0개 경로로 수렴시킨다

**결정(plan 1.0.0)**: `GET /api/v1/rooms`가 실패하면(오프라인·5xx·세션 미복원) 별도 오류 상태를 만들지 않고 FR-013이 정의한 빈 목록 시트 — 저장할 방이 없다는 안내 + `[저장하기]` 비활성 — 를 그대로 표출한다.

**근거**: spec은 조회 실패 상태를 정의하지 않았고, plan이 요구사항을 새로 만들 수는 없다(헌법 원칙 IV). FR-013은 이미 "저장할 방이 하나도 없는 사용자"를 시트의 한 상태로 다루고 있으며, spec §4 가정도 "복원할 세션이 없어 방 목록을 조회할 수조차 없는 경우(EC-011)와 세션은 있으나 방이 0개인 경우를 사용자에게 구분해 보이지 않는다"고 이미 같은 수렴을 확정해 두었다. 조회 실패는 그 문장이 덮는 범위의 자연스러운 확장이다.

로컬 방 캐시가 없어(`core/data/database/entity`는 비어 있다) 오프라인에서 목록을 그릴 방법이 애초에 없다는 점도 같은 결론을 가리킨다.

> **알림의 소유자.** FR-014·FR-015의 알림은 저장이 비동기로 확정된 뒤 발생하므로 **서버가 만든다.** 현재 API에 알림 관련 엔드포인트가 하나도 없으나(`notification` 검색 결과 0건), 알림함 화면은 spec §3.2가 [SCR-007]로 넘긴 비목표다. 이 feature의 클라이언트 책임은 저장 요청을 확실히 전달하는 데서 끝난다.

**Alternatives considered**:
- *재시도 액션이 달린 오류 상태 신설* — 사용자에게 더 정확하지만 spec에 근거가 없는 요구사항이 plan에만 생기고, UX-008("저장 조작은 방 선택과 `[저장하기]` 두 종류뿐")과도 충돌한다. 기각.

---

## R-007. 2단 고정 높이 시트는 `AnchoredDraggable`로 직접 구현한다

**결정(plan 1.0.0)**: Material3 `ModalBottomSheet`를 쓰지 않고 `androidx.compose.foundation.gestures.AnchoredDraggableState`로 `Peek`/`Full` 두 앵커를 dp 값으로 직접 정의한다. 딤 배경은 시트 뒤에 `Box`로 깔고 탭 시 닫는다(EC-001).

**근거**: FR-008이 요구하는 높이는 **콘텐츠와 무관한 고정 dp**다 — `Peek` 436dp / `Full` 612dp(방 4개) / `Full` 644dp(방 5개 이상). M3 `ModalBottomSheet`의 `PartiallyExpanded`는 콘텐츠 높이의 비율로 결정되어 임의의 dp 앵커를 지정할 수 없고, `Full` 높이가 방 개수에 따라 612/644로 갈리는 규칙도 표현할 수 없다. `AnchoredDraggable`은 Compose BOM 2026.04.01(foundation 1.7+)에서 안정 API이며 앵커를 픽셀 단위로 직접 준다.

**Alternatives considered**:
- *M3 `ModalBottomSheet` + `SheetState` 커스터마이즈* — 표준 컴포넌트를 쓰는 이점이 있으나 위 세 값을 표현할 방법이 없다. 콘텐츠에 고정 높이를 강제해 우회하면 `Peek`이 콘텐츠 높이의 50%로 고정되어 436dp와 어긋난다. 기각.

---

## R-008. 진입형 feature이나 `Shell`·`NavHost`·`Launcher`를 두지 않는다

**결정(plan 1.0.0)**: `:feature:sharereceiver`는 Activity를 진입점으로 갖는 진입형 feature이지만, `ShareReceiverShell`·`ShareReceiverNavHost`·`ShareReceiverDestinations`와 `:core:navigation`의 `ShareReceiverLauncher`·`EXTRA_*`를 만들지 않는다. `ShareReceiverActivity`가 `ShareReceiverRoute`를 직접 호스팅하고, 화면 조회 로깅은 `AnalyticsTracker`를 직접 호출한다.

**근거**: [`feature-module.md`](../../architecture/feature-module.md) §2의 진입형 골격에서 세 요소를 뺀 이탈이므로 근거를 남긴다.

- **`NavHost`·`Destinations` 없음**: 이 feature의 화면은 방 선택 시트 하나뿐이고 UX-008이 "시트 안에서 방을 새로 만들거나 장소 정보를 편집하는 경로는 제공하지 않는다"고 못박아 내부 전환 대상이 구조적으로 존재하지 않는다. `MinoNavHost`를 두면 목적지가 하나인 빈 그래프가 된다.
- **`Shell` 없음**: `MinoScaffold`는 chrome·insets·불투명 배경을 여는데, FR-003·UX-001은 앱 화면을 그리지 말고 딤 배경 위에 시트만 띄우라고 요구한다. 셸을 두면 그 배경을 다시 투명하게 되돌려야 한다.
- **`Launcher` 없음**: `XLauncher` 계약은 **다른 feature가 이 화면을 열기 위한** 것인데, 이 화면의 유일한 진입은 OS 공유 인텐트다. [SCR-002] 온보딩 튜토리얼은 연습용 가상 화면이라 이 시트를 호출하지 않는다(spec §3.2 비목표). 호출자가 없는 계약을 `:core:navigation`에 두면 죽은 표면이 된다.

이 이탈은 [plan.md](./plan.md) §복잡도 추적에도 기록한다.

**Alternatives considered**:
- *골격을 그대로 따르고 빈 셸·단일 목적지 그래프를 둔다* — 규약과의 형태적 일치는 얻지만 세 파일이 아무 일도 하지 않고, `MinoScaffold`의 배경을 무력화하는 코드가 추가로 필요하다. 기각.

---

## R-009. 방 목록에는 `RoomSummary`를 새로 만들고 기존 `Room`을 건드리지 않는다

**결정(plan 1.0.0)**: `:core:domain`에 `RoomSummary`(+`RoomType`)를 신설하고 `RoomRepository`에 `suspend fun getRooms(): List<RoomSummary>`를 더한다. 기존 `Room`은 그대로 둔다.

**근거**: 현재 `Room`은 `id·name·description·color·ownerId`뿐이고, [`core/domain/README.md`](../../../core/domain/README.md) §5의 "폼이 쓰는 필드만 담는다 … 필요해지는 feature가 생길 때 더한다"에 따라 방 생성·편집 폼 전용으로 좁게 설계돼 있다. 시트가 필요로 하는 것은 `type`(개인방 최상단 고정 — FR-005), `placeCount`(FR-006), 썸네일 이미지(FR-006)이고, 반대로 폼이 쓰는 필드 대부분은 시트가 쓰지 않는다. 같은 타입에 두 화면의 합집합을 담으면 어느 쪽에서도 절반이 의미 없는 값이 된다.

**Alternatives considered**:
- *`Room`에 필드를 추가* — 타입이 하나로 유지되지만 `getRoom`·`createRoom`·`updateRoom`의 반환값에도 목록 전용 필드가 따라붙어 `RoomMapper`와 mock이 채울 수 없는 값을 채워야 한다. `group-room-form`의 확정된 설계를 흔든다. 기각.

---

## R-010. `:feature:sample`의 방 카드 일가를 `:core:design-system`으로 이관한다

**결정(plan 1.0.0)**: 아래 파일을 `:feature:sample`에서 `:core:design-system`의 `component/roomcard/`로 옮기고 원본은 삭제한다. 이관 과정에서 `MinoRoomCheckBoxCard` 안의 `private fun RoomCheckBox`를 독립 컴포넌트 `MinoCheckbox`(`component/checkbox/`)로 분리하고, `private fun RoomCardCover`를 `MinoRoomThumbnail`(`component/roomthumbnail/`)로 분리한다. `MinoScrollBar`(`component/scrollbar/`)는 새로 만든다.

| 이관 대상 (`feature/sample/main/component/`) | 이관 후 |
|---|---|
| `MinoRoomCard.kt` · `MinoRoomCheckBoxCard.kt` · `RoomCardContent.kt` · `MinoRoomCardDefaults.kt` · `RoomCardPreview.kt` · `token/RoomCardTokens.kt` | `:core:design-system` `component/roomcard/` |

**근거**: Figma `013-1-2`(노드 `2792:176059`)를 열어보면 `Card_Room`·`Room Thumbnail`·`Checkbox`·`Scroll Bar`가 모두 **컴포넌트 인스턴스**이고, 시트 컨테이너·핸들·헤더만 로컬 프레임이다. [`component-asset-placement.md`](../../conventions/component-asset-placement.md) §1.2는 "Figma 디자인 시스템에 컴포넌트로 존재할 때 `:core:design-system`에 만든다. 사용처 개수와 무관하다"고 정하므로 네 컴포넌트의 자리는 `:core:design-system`으로 확정된다.

`MinoRoomCheckBoxCard`는 이미 커버·제목·설명·장소 수·체크박스를 갖춰 FR-006의 카드 구성과 일치하고, 멤버 아바타가 없는 것도 FR-006("멤버 아바타는 넣지 않는다")과 맞는다. `:feature:sample`의 다른 화면이 이 컴포넌트들을 참조하지 않아(사용처는 자기 Preview뿐) 이관에 따른 호출부 파손이 없다.

`MinoAvatarGroup`·`MinoAsyncImage`·`surface`·`rippleSingleClickable`·`rippleSingleSelectable`·`MinoIcons.Check`는 이미 `:core:design-system`에 있어 이관 후 의존이 모듈 안에서 닫힌다.

**Alternatives considered**:
- *`:feature:sharereceiver`에 새로 만든다* — 규약 §1.2 위반이다. Figma 디자인 시스템 컴포넌트는 사용처 수와 무관하게 `:core:design-system`이 소유한다. 기각.
- *`:feature:sample`에 둔 채 참조* — feature 모듈 간 의존이 되어 헌법 원칙 II를 위반한다. 기각.

---

## R-011. 공유 텍스트의 URL 추출은 도메인 UseCase가 소유한다

**결정(plan 1.0.0)**: `ExtractSharedUrlUseCase`를 `:core:domain/usecase`에 두고, 공유받은 텍스트에서 첫 URL 하나를 뽑는다. URL이 없으면 `null`을 반환한다.

**근거**: FR-002("URL이 여러 개면 가장 앞에 등장하는 하나만")와 EC-003은 명백한 비즈니스 규칙이므로 [`core/domain/README.md`](../../../core/domain/README.md) §4의 "비즈니스 규칙 없음" 조건을 만족하지 못한다. ViewModel에 두지 않는다.

**Alternatives considered**:
- *ViewModel에서 정규식으로 처리* — README §4가 금지한다. 기각.

---

## R-012. 세션은 `currentUserId()`로만 복원하고 `ensureSession()`을 호출하지 않는다

**결정(plan 1.0.0)**: 이 진입점은 `EnsureAnonymousSessionUseCase`를 호출하지 않는다. 기존 `AnonymousAuthProvider.currentUserId()`가 이미 네트워크 왕복 없이 로컬 캐시에서 uid를 복원하므로, 그 값이 있으면 방 목록을 조회하고 없으면 R-006의 경로로 넘긴다.

**근거**: FR-019는 "저장된 익명 세션을 네트워크 요청 없이 로컬에서 복원한 뒤 시트를 표출한다. 복원할 세션이 없으면 세션을 새로 확보하지 않고 FR-013의 경로로 넘긴다"이다. `AnonymousAuthProviderImpl.currentUserId()`의 주석이 "`currentUser`는 SDK가 초기화 때 복원해 메모리에 들고 있는 값이라 조회에 네트워크 왕복이 없다"고 이 성질을 이미 보증한다. 반면 `ensureSession()`은 세션이 없으면 `signInAnonymously()`로 네트워크 왕복을 일으켜 UX-010·SC-001을 되돌린다.

`MinoIdentityProofPlugin`이 `IdTokenProvider.getIdToken()`이 `null`이면 `checkNotNull`로 즉시 실패하므로, **세션 확인은 방 목록 요청 전에 끝나야 한다.** 이 순서가 어긋나면 도메인 예외가 아니라 프로그래머 버그로 전파된다.

**Alternatives considered**:
- *`EnsureAnonymousSessionUseCase` 호출* — 앱의 다른 진입점과 코드가 같아지지만 FR-019·UX-010을 정면으로 위반한다. 기각.
