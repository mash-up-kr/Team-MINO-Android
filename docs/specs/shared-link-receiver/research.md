# 리서치: 외부 공유 수신 방 선택 바텀시트

**대상 스펙 경로**: `docs/specs/shared-link-receiver`

**명세서**: [spec.md](./spec.md) · **계획**: [plan.md](./plan.md)

이 문서는 [plan.md](./plan.md)에 종속된 부속 산출물이며 독자 버전을 갖지 않는다. 각 항목은 **어느 plan 버전에서 결정되었는지**를 함께 적는다. 뒤집힌 결정은 지우지 않고 취소선과 `재검토됨(plan X.Y.Z)` 표시를 남긴 뒤 새 항목을 덧붙인다.

---

## ~~R-001. 저장 API — 서버 계약 확장을 전제하고 mock으로 구현한다~~ *재검토됨(plan 2.0.0)*

> **뒤집힘.** 이 항목이 전제한 "저장 계약이 서버에 없다"가 깨졌다. 2026-08-27 재확인 결과 `POST /api/v1/place/places`는 **삭제됐고**, 인증이 걸린 `POST /api/v1/rooms/{roomId}/pins`가 그 자리를 대신한다. 새 결정은 R-013이며, 아래 본문은 기각 이력으로 남긴다.

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

> **사실 정정(plan 2.0.0)**: 위 본문이 근거로 든 엔드포인트 `POST /api/v1/place/places`는 사라졌고, 그 자리의 `POST /api/v1/rooms/{roomId}/pins`는 `url`에 `format: uri`만 걸 뿐 `pattern: instagram\.com`을 걸지 않는다. 판정을 서버에 두는 결정은 바뀌지 않는다 — 오히려 스키마 수준의 도메인 제약이 사라져 근거가 강해졌다.

---

## R-003. 방 썸네일은 서버 필드 추가를 요청하고, 그때까지 대표 색상으로 폴백한다 *(서버가 필드를 붙였다 — 이름과 의미는 R-022가 소유 · 폴백을 누가 그리는지는 R-019)*

**결정(plan 1.0.0)**: `GET /api/v1/rooms` 응답에 장소 이미지 배열(최대 4장)을 추가해달라고 요청하고 그 계약을 [`contracts/room-list-api.md`](./contracts/room-list-api.md)에 명세한다. 서버 대응 전까지 ~~`MinoRoomThumbnail`은 `color`로 대표 색상 배경을 그리고~~ **폴백이 대표 색상 배경을 그리고**(소유자는 R-019), 이미지 배열이 채워지면 콜라주로 전환한다.

**근거**(2026-08-27 재확인 — 여전히 없다): FR-006이 방 카드에 썸네일을 요구하는데 현행 응답에는 `color`·`pinCount`만 있고 콜라주에 쓸 이미지가 없다. PRD 「방 썸네일」이 정의한 두 형태 중 '장소 0개'(대표 색상+캐릭터)는 `color` 하나로 그릴 수 있으므로, 서버가 늦어도 카드가 비어 보이지 않는다.

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

## R-005. 워커는 네트워크 제약 + 지수 백오프로 재시도하고, 4xx는 즉시 실패로 확정한다 *(재시도 정책은 유효 · 실행 단위는 R-014를 거쳐 R-021에서 다시 바뀜)*

**결정(plan 1.0.0)**: `SharedPlaceSaveWorker`에 `NetworkType.CONNECTED` 제약을 걸고, `MinoDomainException.Network`와 5xx `MinoDomainException.Http`는 `Result.retry()`로 지수 백오프에 맡긴다. 4xx `Http`는 재시도해도 결과가 같으므로 `Result.failure()`로 확정한다.

**근거**: EC-009(저장 요청 시점에 네트워크가 끊겨 있다)가 재시도로 구제된다 — 제약이 걸린 워커는 연결이 돌아올 때까지 실행되지 않는다. 반대로 `400 INVALID_INSTAGRAM_URL`·`VALIDATION_ERROR`는 같은 입력으로 몇 번을 보내도 같은 응답이므로 재시도가 배터리만 쓴다. 실패 확정은 FR-014의 오류 알림으로 전달되며 이는 서버가 소유한다(R-006 아래 주석 참고).

**Alternatives considered**:
- *제약 없이 1회 전송* — 구현이 단순하지만 EC-009가 그대로 실패로 확정되어, WorkManager를 도입한 이유의 절반이 사라진다. 기각.
- ~~*방별로 워커 분리* — spec §4 가정("저장은 선택한 방마다 독립적으로 성립한다")을 실행 단위까지 관철한다. 다만 R-001에서 요청할 계약이 `roomIds` 배열 1회 전송이라 방 단위 분해는 서버가 맡는다. 클라이언트가 다시 쪼개면 계약과 어긋나고 요청 수만 늘어난다. 기각.~~ **뒤집힘(plan 2.0.0)** — 기각 근거였던 `roomIds` 배열 계약이 존재하지 않는 것으로 확정됐다. 새 결정은 R-014.

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

## R-012. 세션은 `currentUserId()`로만 복원하고 `ensureSession()`을 호출하지 않는다 *(`ensureSession()`을 부르지 않는다는 결정은 유효 · 호출 경로는 R-020에서 바뀜)*

**결정(plan 1.0.0)**: 이 진입점은 `EnsureAnonymousSessionUseCase`를 호출하지 않는다. 기존 `AnonymousAuthProvider.currentUserId()`가 이미 네트워크 왕복 없이 로컬 캐시에서 uid를 복원하므로, 그 값이 있으면 방 목록을 조회하고 없으면 R-006의 경로로 넘긴다.

**근거**: FR-019는 "저장된 익명 세션을 네트워크 요청 없이 로컬에서 복원한 뒤 시트를 표출한다. 복원할 세션이 없으면 세션을 새로 확보하지 않고 FR-013의 경로로 넘긴다"이다. `AnonymousAuthProviderImpl.currentUserId()`의 주석이 "`currentUser`는 SDK가 초기화 때 복원해 메모리에 들고 있는 값이라 조회에 네트워크 왕복이 없다"고 이 성질을 이미 보증한다. 반면 `ensureSession()`은 세션이 없으면 `signInAnonymously()`로 네트워크 왕복을 일으켜 UX-010·SC-001을 되돌린다.

`MinoIdentityProofPlugin`이 `IdTokenProvider.getIdToken()`이 `null`이면 `checkNotNull`로 즉시 실패하므로, **세션 확인은 방 목록 요청 전에 끝나야 한다.** 이 순서가 어긋나면 도메인 예외가 아니라 프로그래머 버그로 전파된다.

**Alternatives considered**:
- *`EnsureAnonymousSessionUseCase` 호출* — 앱의 다른 진입점과 코드가 같아지지만 FR-019·UX-010을 정면으로 위반한다. 기각.

> **경로 정정(plan 2.2.0)**: 위 본문이 지목한 `AnonymousAuthProvider`는 `:core:data`의 `internal` 인터페이스라 feature 모듈에서 보이지 않는다. 복원 수단을 `currentUserId()`로 삼는 결정은 유지하되, **feature가 그것에 닿는 경로**는 R-020이 새로 정한다.

---

## R-013. 저장 계약이 확정됐다 — ~~`POST /api/v1/rooms/{roomId}/pins`~~, mock을 만들지 않는다 *(엔드포인트는 재검토됨(plan 3.0.0) — 새 결정은 R-021 · mock을 두지 않는다는 결정은 유효)*

**결정(plan 2.0.0)**: R-001이 요청서로 남겨 둔 저장 계약이 서버에 배포됐다. `SharedPlaceRemoteDataSource` mock을 만들지 않고, 처음부터 Ktor 실구현으로 붙인다. 계약은 [`contracts/shared-place-save-api.md`](./contracts/shared-place-save-api.md)가 소유한다.

**근거**: 2026-08-27 `https://api.gguk.org/api-docs-json`(`Team MINO API` 1.0.0) 재확인 결과 R-001이 근거로 삼은 사실이 셋 다 바뀌었다.

| R-001 시점 (2026-08-26) | 지금 (2026-08-27) |
|---|---|
| `POST /api/v1/place/places` — 본문 `{ url }`, `security` 없음 | **경로 자체가 사라졌다**(`place/places` 검색 0건) |
| 저장 대상 방을 전달할 방법이 없다 | `POST /api/v1/rooms/{roomId}/pins` — 대상 방이 **경로에 있다** |
| 저장 주체를 특정할 수 없다 | `security: bearer`(Firebase ID 토큰). `MinoIdentityProofPlugin`이 이미 붙인다 |

mock을 두는 이유였던 "계약이 없다"가 사라졌고, `HttpClient`·`MinoIdentityProofPlugin`·`convertDomainException`은 이미 갖춰져 있다([`core/data/README.md`](../../../core/data/README.md) §4). 지금 mock을 만들면 곧바로 버릴 코드를 쓰는 셈이다.

**Alternatives considered**:
- *mock을 유지하고 실구현을 뒤로 미룬다* — 서버 상태와 무관하게 개발할 수 있으나, 계약이 확정된 상태에서 mock은 우리가 쓴 가정을 우리가 검증하는 코드가 된다. `Flavor.apiBaseUrl`이 이미 실서버(`https://api.gguk.org/`)를 가리키고 있어 미룰 이유가 없다. 기각.
- *`POST /api/v1/pins/{pinId}/duplicate`로 복수 방을 한 번에 처리* — `roomIds` 배열을 받는 유일한 경로지만 **이미 존재하는 핀**을 복제하는 [SYS-003] 경로다. 공유 링크는 아직 핀이 아니라 `pinId`가 없고, "대상 방 중 하나라도 중복이면 409로 전체 거절"이라 FR-016(중복 방도 구분 없이 선택 가능)과 정면으로 어긋난다. 기각.

---

## ~~R-014. 저장 요청은 방마다 워커 하나다~~ *재검토됨(plan 3.0.0)*

> **뒤집힘.** 이 항목이 근거로 삼은 "`roomIds` 배열 계약은 존재하지 않는다"(R-013)가 깨졌다. 2026-08-28 재확인 결과 서버가 `POST /api/v1/rooms/pins`로 **`roomIds` 배열을 받는다.** 새 결정은 R-021이며, 아래 본문은 기각 이력으로 남긴다.

**결정(plan 2.0.0)**: `[저장하기]`로 방 N개가 확정되면 `SharedPlaceSaveWorker`를 **N개 예약**한다. 워커 하나가 `POST /api/v1/rooms/{roomId}/pins`를 한 번 호출하며, 재시도·백오프·네트워크 제약(R-005)은 방 단위로 독립한다. R-005가 기각했던 "방별로 워커 분리"를 뒤집는다.

**근거**: 새 계약은 대상 방이 경로에 있어 **한 요청이 곧 한 방**이다. 기각 근거였던 `roomIds` 배열 계약은 존재하지 않으므로, 방 단위 분해를 서버에 맡길 방법이 없어졌다. 분해는 클라이언트 책임으로 넘어온다.

이 분해를 워커 경계에 맞추면 spec §4 가정("저장은 선택한 방마다 독립적으로 성립한다. 한 방의 실패가 다른 방의 저장을 되돌리지 않는다")과 TS-019(부분 실패)가 **실행 단위에서 그대로 성립한다.** 한 방이 5xx로 백오프하는 동안 다른 방은 이미 접수돼 있고, 한 방의 4xx 확정이 다른 방의 재시도를 끊지 않는다.

**Alternatives considered**:
- *워커 하나가 N개 방을 순회한다* — 예약이 1건이라 단순해 보이지만 `Result.retry()`가 **워커 전체를 처음부터 다시 실행**한다. 이미 202로 접수된 방에 같은 요청이 다시 나가거나, 어디까지 성공했는지를 별도 상태로 들고 다녀야 한다. WorkManager는 재시도 시 같은 `inputData`로 재실행하므로 진행 상태를 담을 자리가 없다. 부분 실패를 관철하려고 도입한 구조가 부분 실패를 못 다루게 된다. 기각.
- *`WorkContinuation`으로 직렬 연결* — 순서 보장을 얻지만 앞 방의 실패가 뒤 방의 실행을 막아 §4 가정을 정면으로 위반한다. 방 사이에 순서 요구도 없다. 기각.

---

## R-015. 이 feature가 쓰는 두 엔드포인트만 실서버로 붙이고, 전용 DataSource를 신설한다

**결정(plan 2.0.0)**: `GET /api/v1/rooms`와 `POST /api/v1/rooms/{roomId}/pins`를 위한 `ApiService`·`DataSource` 쌍을 새로 만든다. 기존 `RoomRemoteDataSource`(방 생성·편집·상세, mock 바인딩)는 **건드리지 않는다.**

| 신설 | 대상 | 비고 |
|---|---|---|
| `RoomListRemoteDataSource` + `RoomApiService` | `GET /api/v1/rooms` | 실서버 |
| `PinRemoteDataSource` + `PinApiService` | `POST /api/v1/rooms/{roomId}/pins` | 실서버 |

`RoomRepositoryImpl`은 과도기 동안 두 출처를 함께 주입받는다 — `getRooms()`는 실서버, `getRoom`·`createRoom`·`updateRoom`은 mock이다.

**근거**: `RoomRemoteDataSource`의 바인딩을 실구현으로 갈아 끼우면 `group-room-form`이 확정한 mock 설계([그 feature의 research.md R-002](../group-room-form/research.md))가 함께 뒤집히고, 방 생성·편집 화면의 재검증이 이 작업에 딸려 온다. 헌법 원칙 IV가 명세를 구현에 선행시키는데, 그 전환을 요구하는 spec은 이 feature의 것이 아니다.

반대로 방 목록을 mock으로 두면 mock이 만든 방 `id`를 실서버에 `POST` 하게 되어 실기기 검증이 성립하지 않는다. 두 엔드포인트를 함께 실서버로 붙여야 [quickstart.md](./quickstart.md)의 엔드투엔드 시나리오가 닫힌다.

DataSource를 리소스가 아니라 **바인딩 대상이 다른 단위**로 나누는 것은 [`core/data/README.md`](../../../core/data/README.md) §5의 인터페이스·구현체 쌍 규칙 안에 있다. 같은 `room` 리소스에 DataSource가 둘인 상태는 `group-room-form`이 실서버로 전환하는 시점에 하나로 합쳐지며, 그때 지워지는 것은 `RoomListRemoteDataSource`다.

**Alternatives considered**:
- *`RoomRemoteDataSource`에 `listRooms()`를 더하고 바인딩을 실구현으로 전환* — DataSource가 하나로 유지되지만 위 이유로 다른 feature의 확정 설계를 흔든다. 기각.
- *저장만 실서버, 목록은 mock 유지* — 변경 범위가 가장 작지만 실기기에서 mock 방 `id`가 `404`·`403`을 받아 엔드투엔드 검증이 불가능하다. 기각.

---

## R-016. 워커는 `MinoDomainException`만 재시도 판정에 쓰고, 그 밖의 예외는 전파한다

**결정(plan 2.0.0)**: `SharedPlaceSaveWorker`는 `MinoDomainException`을 잡아 R-005의 재시도 판정을 내리고, 그 밖의 예외는 잡지 않는다.

**근거**: 실서버가 붙으면서 도메인 예외가 아닌 실패 경로가 실제로 생긴다 — `MinoIdentityProofPlugin`은 신원 증명이 없으면 `checkNotNull`로 `IllegalStateException`을 던지고, 이는 `convertDomainException`이 매핑하지 않는다(R-012). [`error_handling.md`](../../conventions/error_handling.md)가 프로그래머 버그를 삼키지 말고 전파하라고 정하므로 워커도 예외가 아니다. 전파된 예외는 WorkManager가 그 실행을 `FAILED`로 확정하며, 앱이 죽지 않는다.

세션 유무를 워커가 미리 검사하지 않는다. spec §4가 "설치가 살아 있는 동안 세션은 만료되지 않는다"를 확정했고 세션이 없으면 방 목록이 비어 `[저장하기]`가 비활성이므로(FR-013·FR-019), 워커가 예약되는 경로에는 항상 세션이 있다. 도달하지 않는 경로에 방어 코드를 두지 않는다.

**Alternatives considered**:
- *모든 예외를 `Result.failure()`로 흡수* — 워커가 절대 던지지 않아 안전해 보이지만 프로그래머 버그가 조용한 저장 실패로 위장된다. 규약 위반. 기각.
- *워커가 요청 전에 `currentUserId()`로 세션을 확인* — 방어적이지만 위 근거대로 도달하지 않는 분기이며, 검사가 통과한 뒤에도 토큰 갱신 실패는 여전히 남아 문제를 닫지 못한다. 기각.

---

## R-017. 화면은 예약만 알고, 전송은 `:core:data` 안에서 끝난다 *(경계는 유효 · 방 단위 분해 문장은 R-021에서 바뀜)*

**결정(plan 2.0.0)**: 도메인 `SharedPlaceRepository`가 노출하는 함수는 예약 하나뿐이다.

```
fun scheduleSave(request: SharedPlaceSaveRequest)
```

`suspend`가 아니다 — 예약은 즉시 반환하고, 결과를 기다리지 않는다. 구현(`SharedPlaceRepositoryImpl`)이 `roomIds`를 방 단위로 쪼개 워커 N개를 `WorkManager`에 넣고(R-014), 워커는 같은 모듈 안의 `PinRemoteDataSource`를 직접 호출한다. **전송용 함수를 도메인 표면에 두지 않는다.**

**근거**: plan 1.0.0은 "feature는 도메인 계약 뒤에 감춰진 예약 함수를 호출한다"고 적었으나 `data-model.md`는 `requestSave(request)` 하나만 정의해 예약과 전송이 한 함수에 겹쳐 있었다. 계약이 방 단위로 바뀌면서 이 경계를 확정해야 한다.

워커는 `:core:data/work/`에 있으므로 `PinRemoteDataSource`(`internal`)에 모듈 안에서 닿는다. 전송을 도메인 인터페이스로 한 번 더 감싸면, 아무 화면도 호출하지 않는 함수가 `:core:domain`의 공개 표면에 남는다.

호출 흐름에서 `[저장하기]`가 네트워크를 기다리지 않는다는 점도 같은 결론을 가리킨다 — FR-011·UX-006이 요구하는 "토스트 후 즉시 물러남"은 예약이 non-suspend일 때 자연스럽게 성립한다.

**Alternatives considered**:
- *`SharedPlaceRepository`에 `scheduleSave`와 `savePlace(url, roomId)`를 함께 둔다* — 워커가 도메인 계약을 통해 내려가 레이어 그림이 균일해지지만, `savePlace`의 유일한 호출자가 같은 모듈 안에 있는데도 도메인에 공개 함수가 하나 늘어난다. 기각.
- *ViewModel이 `WorkManager`를 직접 호출* — 예약 함수가 사라지지만 feature 모듈이 WorkManager(안드로이드 인프라)와 워커 클래스(`:core:data`)를 알아야 한다. 헌법 원칙 II 위반. 기각.

---

## R-018. `{ "data": ... }` 봉투는 제네릭 DTO 하나로 벗기고, `ApiService` 밖으로 내보내지 않는다

**결정(plan 2.0.0)**: `:core:data/network/dto/response/MinoResponse.kt`에 `internal data class MinoResponse<T>(val data: T)` 하나를 두고, `ApiService`가 `body<MinoResponse<...>>().data`로 봉투를 벗겨 알맹이만 반환한다. `DataSource`·`RepositoryImpl`·`Mapper`는 봉투를 알지 못한다.

**근거**: Mino API는 성공 응답을 예외 없이 `{ "data": ... }`로 감싼다(`GET /api/v1/rooms`·`POST /api/v1/users`·`GET /api/v1/users/me` 모두 동일). 지금까지는 `RoomMockRemoteDataSourceImpl`이 JSON을 거치지 않아 이 봉투를 다룰 자리가 없었고, 이 feature가 **저장소에서 처음으로 실제 응답을 파싱한다.**

봉투를 벗기는 자리를 `ApiService`로 정한 것은 [`core/data/README.md`](../../../core/data/README.md) §2의 레이어 그림을 따른 것이다 — `DataSource`의 반환 타입은 DTO이고, 봉투는 서버 전송 형식일 뿐 데이터가 아니다. 엔드포인트마다 `XxxListResponse(val data: List<...>)` 같은 래퍼를 손으로 쓰면 엔드포인트 수만큼 의미 없는 타입이 늘어난다.

**Alternatives considered**:
- *엔드포인트마다 전용 래퍼 DTO* — 제네릭이 없어 단순하지만 같은 모양의 타입이 무한히 늘어난다. 기각.
- *`DataSource`가 봉투째 반환하고 `RepositoryImpl`이 벗긴다* — `RepositoryImpl`이 전송 형식을 알게 되어 README §6의 "DataSource 호출 + Mapper 적용" 책임을 넘는다. 기각.

> **ADR로 승격됨(2026-08-27).** 이 봉투는 이 feature의 두 엔드포인트만의 규칙이 아니라 Mino API 전체의 응답 형식이고, `group-room-form`이 실서버로 전환할 때도 같은 타입을 쓴다. 결정의 배경·근거·기각한 대안은 이제 [서버 응답의 `{ data }` 봉투는 제네릭 DTO 하나로 `ApiService`에서 벗긴다](../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)가 소유한다.

---

## R-019. 썸네일의 색상 폴백은 `:core:common:ui`가 갖고, 디자인 시스템 컴포넌트는 슬롯으로 받는다

**결정(plan 2.1.0)**: `MinoRoomThumbnail`은 콜라주 배치만 소유하고 `fallback: @Composable () -> Unit` 슬롯을 받는다. 폴백 컴포넌트 `RoomThumbnailFallback(color: MinoRoomColor?)`와 캐릭터 이미지 13종은 `:core:common:ui`가 갖는다. 도메인 `RoomColor` → `MinoRoomColor?` 매핑은 feature(`RoomPickerItem` 변환)가 소유한다. 계약은 [`contracts/room-picker-sheet-ui.md`](./contracts/room-picker-sheet-ui.md) §1·§2.2·§2.2.1이 갖는다.

**근거**: plan 2.0.0의 계약은 `MinoRoomThumbnail(imageUrls, color: RoomColor)`였는데, 그 시그니처는 `:core:design-system`에 두 가지를 함께 요구한다. 둘 다 규범이 막는다.

| 요구 | 막는 규범 |
|---|---|
| `:core:domain`의 `RoomColor`를 파라미터로 받는다 | [방 색상 팔레트 ADR](../../adr/2026-08-14-room-color-palette-in-design-system.md)이 「고려한 대안」에서 이 선택지를 레이어 역행으로 명시 기각했다. [modularization.md](../../architecture/modularization.md)의 의존 그래프에도 그 엣지가 없다 |
| 폴백의 캐릭터 이미지를 품는다 | [`component-asset-placement.md`](../../conventions/component-asset-placement.md) §1 — "`:core:design-system`은 이미지 에셋을 받지 않는다. 여러 feature가 공유하는 이미지의 자리는 `:core:common:ui`다" |

두 제약이 같은 곳을 가리킨다. **색과 캐릭터를 아는 쪽으로 폴백을 밀어내면 둘 다 풀린다.** `Room Thumbnail`이 Figma 디자인 시스템 컴포넌트인 사실은 변하지 않으므로([`component-asset-placement.md`](../../conventions/component-asset-placement.md) §1.2) 콜라주 컴포넌트 자체는 `:core:design-system`에 남고, R-010이 `MinoRoomCheckBoxCard`에 `thumbnail` 슬롯을 열어 둔 것과 같은 수법으로 잇는다.

폴백을 `:core:common:ui`에 두는 것은 승격 규칙과도 맞는다. 캐릭터 에셋 13종은 지금 `:feature:roomform`에만 있고 이 시트가 **두 번째 사용처**이므로 §2.1의 승격 시점이 성립하며, 승격 방향은 §2에 따라 `:core:common:ui` 하나뿐이다. `:core:common:ui`는 `:core:design-system`을 의존하므로 `MinoRoomColor`가 보이고 `:core:domain`은 의존하지 않아 경계가 그대로 유지된다.

이 결정은 규범을 새로 만들지 않는다. 기존 ADR·컨벤션이 이미 정해 둔 자리를 이 feature의 계약이 잘못 적었던 것을 되돌리는 것이므로 ADR 대상이 아니다.

**Alternatives considered**:
- *`:core:design-system`에 `:core:domain` 의존을 추가한다* — 계약을 글자 그대로 구현할 수 있지만 방 색상 팔레트 ADR이 기각한 바로 그 안이고, 뒤집으려면 ADR·modularization·헌법 원칙 II를 함께 고쳐야 한다. 팔레트 파라미터 하나 때문에 치를 값이 아니다. 기각.
- *에셋을 `:feature:sharereceiver`에도 복사하고 폴백을 feature가 그린다* — 다른 모듈을 건드리지 않아 이 이슈의 범위가 가장 작다. 다만 같은 웹피 13종 × 밀도 3벌이 두 모듈에 남아 [`component-asset-placement.md`](../../conventions/component-asset-placement.md) §2.1을 알면서 어기게 된다. 기각.
- *썸네일 전체를 `:core:common:ui`로 옮긴다* — 폴백과 콜라주가 한 컴포넌트에 남아 슬롯이 필요 없지만, Figma 디자인 시스템 컴포넌트를 design-system 밖에 두게 되어 §1.2와 어긋난다. 기각.

---

## R-020. 로컬 세션 조회를 도메인 계약으로 노출한다

**결정(plan 2.2.0)**: `AnonymousAuthRepository`에 조회 함수 하나를 더한다.

```
suspend fun currentSession(): AnonymousSession?
```

로컬에 유지된 세션을 네트워크 왕복 없이 돌려주고, 없으면 `null`이다. 던지지 않는다. 구현은 `AnonymousAuthRepositoryImpl`이 `AnonymousAuthProvider.currentUserId()`에 위임하되 **발급으로 넘어가지 않는다.** 기존 `ensureSession()`은 손대지 않으므로 R-012가 확정한 "이 진입점은 세션을 새로 확보하지 않는다"가 그대로 유지된다.

**근거**: R-012가 복원 수단으로 지목한 `AnonymousAuthProvider`는 `:core:data`의 **`internal` 인터페이스**다. feature 모듈은 두 겹으로 막혀 있다 — `:core:data`를 의존하지 않고([`modularization.md`](../../architecture/modularization.md)의 의존 그래프·헌법 원칙 II), 의존하더라도 `internal`이라 보이지 않는다. 반면 도메인이 노출하는 세션 API는 `ensureSession()` 하나뿐이고 R-012가 그것을 부르지 말라고 확정했으므로, **feature가 세션을 확인할 합법적 경로가 존재하지 않았다.** plan 2.1.0까지의 설계는 이 지점에서 컴파일되지 않는다.

FR-019가 요구하는 것은 "저장된 익명 세션을 네트워크 요청 없이 로컬에서 복원"이고, 그 성질은 조회 함수의 계약으로 그대로 옮겨진다. `AnonymousAuthRepositoryImpl.ensureSession()`이 이미 같은 호출을 잠금 밖 빠른 경로로 쓰고 있어, 구현은 그 경로를 발급 없이 반환하는 것이 전부다.

`ensureSession()`(확보)과 `currentSession()`(조회)이 짝을 이루며, 어느 진입점이 어느 쪽을 쓰는지는 호출자가 정한다 — 스플래시는 확보를, 이 진입점은 조회를 쓴다. 반환 타입을 `String?`이 아니라 `AnonymousSession?`으로 둔 것은 기존 함수와 같은 도메인 모델을 쓰기 위해서다.

이 결정은 `:core:data`의 기존 가시성 규칙을 바꾸지 않고 도메인 표면만 넓히므로 ADR 대상이 아니다.

**Alternatives considered**:
- *feature가 `:core:data`를 의존한다* — 코드는 가장 적게 바뀌지만 헌법 원칙 II와 [`modularization.md`](../../architecture/modularization.md)의 의존 그래프를 정면으로 위반한다. `internal` 가시성 때문에 그래도 컴파일되지 않는다. 기각.
- *`AnonymousAuthProvider`를 `public`으로 연다* — 데이터 레이어의 내부 계약이 앱 전체 표면으로 새어 나가고, `IdTokenProvider`까지 같은 압력을 받는다. 기각.
- *`EnsureAnonymousSessionUseCase`를 그대로 쓴다* — R-012가 이미 기각했다. FR-019·UX-010 위반.
- *`RoomRepository.getRooms()`가 세션 없음을 빈 목록으로 흡수하게 한다* — 조회 함수 추가 없이 R-006의 수렴만으로 닫으려는 안이다. 다만 `MinoIdentityProofPlugin`이 신원 증명 없이 요청이 나가면 `checkNotNull`로 **프로그래머 버그를 던지므로**(R-012·R-016), 세션 확인이 요청보다 앞서야 한다는 제약을 없앨 수 없다. 기각.

---

## R-021. 저장은 요청 하나로 여러 방에 보내고, 워커도 하나다

**결정(plan 3.0.0)**: 서버가 `roomIds` 배열을 받는 계약을 열었다. `SharedPlaceSaveWorker`를 **방 개수와 무관하게 하나만** 예약하고, 그 워커가 `POST /api/v1/rooms/pins`를 **한 번** 호출한다. R-014가 채택했던 방 단위 분해(방 N개 → 워커 N개)를 되돌린다. 계약은 [`contracts/shared-place-save-api.md`](./contracts/shared-place-save-api.md)가 소유한다.

**근거**: 2026-08-28 `https://api.gguk.org/api-docs-json` 재확인 결과 R-013·R-014가 딛고 선 사실이 바뀌었다.

| R-013·R-014 시점 (2026-08-27) | 지금 (2026-08-28) |
|---|---|
| `POST /api/v1/rooms/{roomId}/pins` — 대상 방이 **경로에** 하나 | **경로가 사라졌다** |
| `roomIds` 배열을 받는 계약이 없다 | `POST /api/v1/rooms/pins` — 본문에 **`roomIds: uuid[]`(minItems 1)** |
| 요약: "…방에 핀을 추가한다" | 요약: "…**여러 방에** 핀을 추가한다" |

R-014의 채택 근거는 "한 요청이 곧 한 방이라 분해를 서버에 맡길 방법이 없다"였다. 그 전제가 사라졌으므로 분해를 다시 서버가 가져간다. **클라이언트가 배열 계약을 두고 굳이 N번 쪼개 보내면 계약과 어긋나고 요청 수만 늘어난다.**

**spec의 보장은 그대로 성립한다 — 지키는 주체만 바뀐다.** spec §4 가정("저장은 선택한 방마다 독립적으로 성립하고, 한 방의 실패가 다른 방의 저장을 되돌리지 않는다")과 TS-019(부분 실패)는 이제 **서버가** 방마다 갈라 처리하고 방 단위로 알림을 남기는 것으로 충족된다. 클라이언트 요구사항이 줄어드는 방향이라 spec 개정이 필요하지 않다.

**재시도가 이미 접수된 방을 다시 보내는 문제는 생기지 않는다.** `202`는 접수만 확정하고 중복 판정은 서버가 저장 시점에 한다(FR-015). 재시도로 같은 `url`+`roomIds`가 다시 가도 서버가 중복으로 흡수하며, 그것이 사용자에게 닿는 형태는 알림함의 중복 알림이다.

**Alternatives considered**:
- *R-014를 유지하고 배열 계약에 방 하나씩 N번 보낸다* — 코드 변경이 가장 작다. 다만 `minItems: 1`인 배열에 원소 하나씩 넣어 N번 부르는 것은 계약의 의도를 거스르고, 서버가 한 번에 처리할 일을 N번의 왕복과 N번의 링크 분석으로 늘린다. 기각.
- *워커 없이 요청 하나를 그 자리에서 보낸다* — 요청이 하나뿐이니 WorkManager가 과해 보인다. 다만 [ADR 2026-08-26](../../adr/2026-08-26-workmanager-for-detached-requests.md)이 정한 생존 구간(Activity·프로세스 종료 후에도 살아남는다)은 요청 개수와 무관하게 그대로 필요하다(spec §4 가정·FR-011). 기각.
- *방별 워커를 유지하되 각 워커가 배열에 자기 방 하나만 싣는다* — 위 첫 대안과 같은 문제에 워커 관리 비용까지 더한다. 기각.

> **ADR과의 관계.** [ADR 2026-08-26](../../adr/2026-08-26-workmanager-for-detached-requests.md) 74줄이 "방마다 워커를 분리"를 기각하며 든 근거("서버 계약이 `roomIds` 배열 1회 전송이라 방 단위 분해는 서버가 맡는다")가 **다시 사실이 됐다.** R-014 기간 동안 그 ADR이 낡았던 것이 해소된다. 다만 같은 ADR의 21줄(`POST /api/v1/place/places`)과 27줄("워커는 Repository를 호출해")은 여전히 현행과 어긋나므로 ADR 개정은 그대로 필요하다.

---

## R-022. 서버 썸네일 필드는 `thumbnailList`이고, 색상 키가 섞여 온다

**결정(plan 3.0.0)**: R-003이 요청한 썸네일 필드가 붙었다. 이름은 `thumbnailImageUrls`가 아니라 **`thumbnailList`**이고, 값에 **URL이 아닌 색상 키가 섞일 수 있다.** `RoomSummaryMapper`가 **URL만 남기고 색상 키를 버린다.** 도메인 `RoomSummary.thumbnailImageUrls`의 이름과 의미(콜라주에 쓸 이미지 URL, 최대 4장)는 그대로 둔다.

**근거**: 서버 필드 설명이 이렇다.

```
thumbnailList: string[]
  "최근 핀 최대 4개의 장소 대표 이미지 URL(최신순).
   저장된 핀이 없으면 방장 아바타 색상 키 1개."
```

즉 이 배열은 **두 가지 다른 것**을 담는다 — 이미지 URL 목록이거나, 이미지가 없다는 신호로서의 색상 키 하나다. 색상 키를 그대로 도메인에 올리면 `MinoRoomThumbnail`이 URL이 아닌 문자열을 이미지로 로드하려 한다.

**색상 키는 버리는 것이 맞다.** 그 정보는 이미 `color` 필드에 있고(R-003이 정한 폴백의 출처), 폴백은 `RoomThumbnailFallback`이 `color`로 그린다(R-019). 색상 키를 살려도 같은 그림을 두 경로로 얻을 뿐이다. 버리면 배열이 비고, 빈 배열은 이미 폴백 경로다 — **기존 설계가 그대로 성립한다.**

판정은 URL 스킴으로 한다. `http://`·`https://`로 시작하지 않는 원소를 버린다. 색상 키는 `red`·`gray` 같은 소문자 단어라 스킴을 갖지 않는다.

**함께 확인된 것**: `color`가 13색 enum(`red`·`red_orange`·…·`gray`)으로 확정됐고 `RoomMapper`의 기존 대응표와 정확히 일치한다. plan 2.2.0에서 "계약 예시의 `black`이 표에 없어 회색으로 떨어진다"고 기록한 우려는 해소됐다 — `black`은 서버 팔레트에 없다.

**Alternatives considered**:
- *`thumbnailList`를 그대로 도메인에 올리고 UI가 색상 키를 걸러낸다* — Mapper를 안 고쳐도 되지만, "이 배열에 URL 아닌 것이 섞인다"는 서버 계약의 특성이 UI까지 새어 나간다. `core/data/README.md` §7이 DTO의 특성을 도메인으로 넘기지 말라고 정한다. 기각.
- *`pinCount == 0`이면 썸네일 없음으로 본다* — 스킴 검사 없이 판정할 수 있다. 다만 서버 설명이 "저장된 핀이 없으면"이라고만 적을 뿐 `pinCount`와의 관계를 보장하지 않아, 두 필드가 어긋나는 순간 조용히 틀린다. 기각.
- *도메인 필드 이름을 `thumbnailList`로 맞춘다* — 서버 이름과 같아지지만, 도메인이 담는 것은 이미 걸러진 URL 목록이라 "list"보다 `thumbnailImageUrls`가 정확하다. DTO와 도메인의 이름이 다른 것은 Mapper가 있는 이유다. 기각.

---

## R-023. 공유 수신 Activity는 앱 태스크에서 분리한다 — `taskAffinity=""`

**결정(plan 3.1.0)**: `ShareReceiverActivity`에 `android:taskAffinity=""`를 선언해 앱의 태스크와 분리한다. `launchMode="singleTask"`·`excludeFromRecents="true"`·투명 테마는 그대로 둔다.

**근거**: plan 3.0.0까지 이 Activity는 `taskAffinity`를 선언하지 않았고, 그래서 기본값인 `applicationId`를 썼다 — `MainActivity`와 **같은 affinity**다. 이것이 FR-003과 TS-027을 깨는 원인이다.

OS 공유 시트는 대상 Activity를 `FLAG_ACTIVITY_NEW_TASK`로 시작하며, 이 플래그도 `singleTask`도 **affinity가 일치하는 기존 태스크를 먼저 찾아** 거기에 Activity를 넣고 그 태스크를 전면으로 올린다. 결과가 앱 상태에 따라 갈린다.

| 앱 상태 | 매칭되는 태스크 | 결과 |
|---|---|---|
| 종료됨 | 없음 → 새 태스크 | 투명 창 뒤에 외부 앱이 남는다. **의도대로 동작** |
| 실행 중 | 꾹의 태스크 | 그 태스크가 통째로 전면으로 나오고, 사용자가 마지막으로 보던 꾹 화면 **위에** 시트가 얹힌다. **FR-003·TS-027 위반** |

같은 코드가 앱 상태에 따라 다르게 보인 이유가 이것이다. 투명 테마는 처음부터 올바르게 잡혀 있었고 — 문제는 창이 아니라 **태스크 소속**이었다.

`android:taskAffinity=""`는 어떤 태스크와도 매칭되지 않는 빈 affinity를 준다. 앱 상태와 무관하게 항상 자기 태스크에서 뜨고, 그 아래에 남는 것은 직전 전면 태스크 — 공유를 보낸 외부 앱이다. 창이 불투명이 아니므로 그 태스크는 계속 그려진다(TS-027). `finish()`하면 이 태스크가 사라지고 아래 태스크가 드러난다(TS-028·TS-006). 꾹의 태스크는 시작부터 끝까지 건드려지지 않아 사용자가 보던 화면이 그대로 남는다(spec §4 가정).

**곁가지로 함께 해소되는 것** — `excludeFromRecents`가 비로소 의미를 갖는다. 종전에는 이 Activity가 앱 태스크에 얹혀 있어 "이 태스크를 최근 앱에서 빼라"가 앱 자신의 태스크를 가리켰고, 실제로 걷어낼 흔적이 따로 없었다. 이제는 자기 태스크에 걸린다.

**남는 위험** — `windowAnimationStyle=@null`은 일부 OEM·버전에서 기본 애니메이션으로 폴백한다. 태스크가 분리되면 태스크 전환 애니메이션 자체가 사라져 대부분 드러나지 않지만, 눈으로 보는 항목을 [quickstart.md §4.1](./quickstart.md)에 둔다.

**Alternatives considered**:
- *`launchMode`를 `standard`로 되돌린다* — `singleTask`가 원인이라고 보는 관점. 그러나 `singleTask`를 지워도 `FLAG_ACTIVITY_NEW_TASK`의 affinity 매칭은 그대로 남아 **앱 태스크가 전면으로 나오는 것은 똑같다.** 원인이 launchMode가 아니라 affinity이므로 해결되지 않고, EC-013이 요구하는 "시트가 겹치지 않는다"만 잃는다. 기각.
- *`launchMode="singleInstance"`* — 자기 태스크를 독점해 affinity를 지정하지 않아도 분리된다. 그러나 이것은 "이 태스크에 다른 Activity가 절대 들어올 수 없다"는 더 강한 제약이고, 필요한 것은 "앱 태스크와 섞이지 않는다" 하나다. 필요보다 넓은 제약을 매니페스트에 박지 않는다. 기각.
- *`android:documentLaunchMode="always"`* — 매 공유마다 새 태스크를 만들어 앱 태스크와도 분리된다. 그러나 인스턴스가 매번 새로 생겨 EC-013이 깨지고, 최근 앱에 문서 항목을 남기는 동작이 `excludeFromRecents`의 의도와 정면으로 부딪힌다. 기각.

---

## R-024. 시트가 떠 있는 동안 도착한 공유는 `onNewIntent`로 갈아끼운다

**결정(plan 3.1.0)**: `launchMode="singleTask"`를 유지해 두 번째 `ACTION_SEND`를 기존 인스턴스의 `onNewIntent`로 받고, 그 자리에서 시트의 링크를 교체한다. 새 Activity를 띄우지도, 기존 것을 `finish()`하지도 않는다(EC-013).

**근거**: R-023이 `taskAffinity=""`를 넣어도 EC-013은 저절로 성립하지 않는다. `singleTask`는 새 인텐트를 기존 인스턴스로 **라우팅**해 주지만, 현재 설계에서 URL을 읽는 곳은 `onCreate` 하나뿐이라 **시트가 옛 링크를 그대로 쥔 채 남는다.** 사용자는 방금 공유한 게시물을 저장했다고 믿지만 실제로 저장되는 것은 이전 링크다 — 조용히 틀리는 경로다.

처리 규칙은 셋이다.

| 새 인텐트 | 처리 | 근거 |
|---|---|---|
| URL이 있다 | 시트의 링크를 새 URL로 교체하고 방 선택을 비운다 | EC-013 |
| URL이 없다 | **무시하고 떠 있는 시트를 유지한다** | EC-002는 "시트를 띄우지 않는다"이지 "떠 있는 시트를 걷는다"가 아니고, 사용자 조작 없이 시트가 사라지는 경로를 FR-012가 두지 않는다 |
| 저장 완료 토스트 중에 도착한다 | 토스트 단계를 되돌리고 새 시트를 띄운다 | 앞선 저장은 이미 워커로 넘어가 있어 취소되지 않는다([R-004](#r-004-저장-요청의-생존은-workmanager가-보장한다)) |

**방 목록은 다시 조회하지 않는다.** 교체되는 것은 링크와 선택 상태뿐이며, 이미 그려진 목록을 버리고 다시 받으면 SC-001·UX-009가 요구하는 즉시성이 두 번째 공유에서만 깨진다.

배선은 이렇다. `ShareReceiverActivity`가 현재 URL을 상태로 들고, `onNewIntent`에서 `setIntent(intent)`로 태스크 레코드의 인텐트를 갈아끼운 뒤 그 상태를 갱신한다. 시트는 URL이 바뀌면 `ShareReceiverIntent.SharedUrlReplaced`를 올리고, `ShareReceiverViewModel`은 `savedStateHandle[KEY_SHARED_URL]`을 새 값으로 덮고 선택을 비운다. `setIntent`는 프로세스가 통째로 재생성될 때를, `SavedStateHandle` 갱신은 ViewModel만 복원될 때를 각각 덮는다.

**Alternatives considered**:
- *`onNewIntent`에서 `recreate()`를 부른다* — `onCreate`가 새 인텐트로 다시 돌아 URL을 읽는 코드가 한 갈래로 유지된다. 그러나 `recreate()`는 구성 변경과 같아 `ViewModelStore`가 살아남고, **ViewModel이 옛 URL을 그대로 쥔다.** 겉보기만 새로 그려지고 저장되는 링크는 이전 것이 되는, 이 결정이 막으려는 바로 그 상태다. 기각.
- *`onNewIntent`에서 `finish()`하고 새 인텐트로 다시 시작한다* — 상태 초기화가 확실하다. 그러나 종료와 시작 사이에 외부 앱이 한 프레임 드러나고, `excludeFromRecents` 태스크가 사라졌다 다시 생기는 전환이 눈에 띈다. 얻는 것에 비해 값이 비싸다. 기각.
- *`launchMode="standard"`로 두고 새 인스턴스를 띄운다* — `onNewIntent`를 다룰 필요가 없다. 그러나 시트가 겹쳐 뜨고 뒤로가기가 앞선 시트를 다시 드러내 EC-013을 정면으로 위반한다. R-023의 기각 사유와 같다. 기각.
