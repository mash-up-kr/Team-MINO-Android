# 구현 계획: 외부 공유 수신 방 선택 바텀시트 (Shared Link Receiver)

**대상 스펙 경로**: `docs/specs/shared-link-receiver`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: 2.4.0

**최초 작성일**: 2026-08-26

**최종 수정일**: 2026-08-28

**버전**: 3.1.0

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

## 요약 (Summary)

인스타그램 등 외부 앱이 OS 공유 시트로 넘긴 링크를 받아, 앱 화면으로 전환하지 않고 딤 배경 위에 방 선택 바텀시트만 띄운다. 사용자가 방을 복수 선택하고 `[저장하기]`를 누르면 저장 요청을 접수하고 곧바로 외부 앱으로 물러난다. 링크 분석은 저장 이후 서버가 수행하며, 결과는 알림함으로 사후 전달된다.

기술적 접근은 넷으로 요약된다.

1. **투명 Activity + 자체 시트** — `ACTION_SEND`를 받는 `ShareReceiverActivity`가 투명 테마로 뜨고, `AnchoredDraggable` 기반 2단 고정 높이 시트를 직접 그린다. 스플래시·셸·NavHost를 거치지 않는다. **이 Activity는 `taskAffinity=""`로 앱의 태스크와 분리돼, 앱이 실행 중이든 종료돼 있든 꾹의 화면을 전면으로 끌어올리지 않는다**([research.md R-023](./research.md)).
2. **로컬 세션 복원만** — 도메인에 조회 함수 `AnonymousAuthRepository.currentSession()`을 열어 네트워크 왕복 없이 세션을 복원한다. 세션이 없으면 새로 확보하지 않고 빈 목록 경로로 넘긴다([research.md R-020](./research.md)).
3. **WorkManager로 요청 생존 보장** — `[저장하기]` 이후의 요청을 워커로 넘겨 Activity 종료·프로세스 종료 후에도 살아남게 한다. 워커는 **방 개수와 무관하게 하나**이고 요청도 하나다 — 서버가 `roomIds` 배열을 받으므로 방 단위 분해는 서버가 한다([research.md R-021](./research.md)).
4. **디자인 시스템 이관** — `:feature:sample`에 이미 있는 방 카드 일가를 `:core:design-system`으로 옮기고, 체크박스·썸네일·스크롤 바를 분리·신설한다. 썸네일의 **색상 폴백만** 도메인 값과 캐릭터 이미지를 요구해 `:core:common:ui`가 갖고, 슬롯으로 잇는다([research.md R-019](./research.md)).

저장 계약이 서버에 배포되어 mock 없이 실서버로 붙인다 — 이 feature가 저장소에서 **처음으로 실제 응답을 파싱하는** 경로다. 2026-08-28 재확인 시점에 서버 공백은 없다 — 썸네일 이미지도 `thumbnailList`로 내려온다([research.md R-022](./research.md)).

## 기술 컨텍스트 (Technical Context)

**언어/버전**: Kotlin 2.2.10 · JDK 17 · AGP 9.1.1

**주요 의존성**: Jetpack Compose (BOM 2026.04.01) · Hilt 2.59.2 · Ktor Client 3.3.0 · kotlinx.serialization · Coil 3.3.0 · Firebase Auth · **WorkManager (신규 — [research.md R-004](./research.md))**

**저장소**: 없음. 이 feature는 로컬 영속 데이터를 두지 않는다. 방 목록은 매 진입 시 조회하고, 공유받은 링크는 보관하지 않는다(FR-013)

**테스트**: JUnit + `kotlinx-coroutines-test` (UseCase·ViewModel) · Compose Preview (컴포넌트) · `androidx.work:work-testing` + `ktor-client-mock` (워커 재시도 정책 — [quickstart.md §5.5](./quickstart.md))

**대상 플랫폼**: Android (minSdk는 프로젝트 설정을 따른다)

**프로젝트 유형**: mobile-app — 다중 Gradle 모듈

**성능 목표**: OS 공유 시트에서 꾹을 고른 뒤 시트가 조작 가능해지기까지 1초 이내 (SC-001). 시트 표출이 방 목록 조회·링크 분석을 기다리지 않는 것이 이 목표의 설계적 근거다

**제약 조건**: 스플래시·세션 확보 대기를 끼워 넣지 않는다 (UX-010) · 시트 표출과 `[저장하기]` 사이에 로딩 표현을 두지 않는다 (UX-009) · `[저장하기]` 이후 요청은 앱을 떠나도 취소되지 않는다 (spec §4 가정)

**규모/범위**: 화면 1개(상태 2종: 방 목록 / 빈 목록) · 신규 feature 모듈 1개 · 신규·이관 디자인 시스템 컴포넌트 4종 · `:core:common:ui` 공용 컴포넌트 1종과 에셋 승격 13종 · 도메인 모델 3종 · 도메인 Repository 함수 추가 2건(방 목록·세션 조회) · 실서버로 붙이는 엔드포인트 2개(`ApiService`·`DataSource` 2쌍 신설) · 서버 확장 요청 0건

## 헌법 준수 확인 게이트 (Constitution Check)

*게이트: Phase 0 리서치 전에 반드시 통과해야 한다. Phase 1 설계 후 재확인한다.*

[헌법](../../constitution.md) 2.1.0 기준.

| 게이트 | Phase 0 전 | Phase 1 후 | 판정 근거 |
|---|---|---|---|
| **I. 단일 출처 문서화** | PASS | PASS | 규약·README·PRD의 규칙 본문을 복제하지 않고 링크로 지목했다. 이 feature 안에서만 유효한 선택은 `research.md`가, 서버·OS 계약은 `contracts/`가 소유한다. 3.0.0까지 `contracts/share-intent.md`가 spec에 근거 없는 판단("공유가 연달아 들어와도 시트가 겹치지 않는다")을 혼자 쥐고 있었고, spec 2.4.0의 EC-013이 그 출처를 되찾았다 — 계약은 이제 그 결정을 **참조**한다 |
| **II. 레이어 경계와 의존 방향** | PASS | PASS | `:feature:sharereceiver` → `:core:domain`·`:core:design-system`·`:core:common:*`. `:core:data`를 직접 의존하지 않는다. `:core:design-system`은 `:core:domain`을 의존하지 않는 상태를 유지한다 — 도메인 값을 요구하는 썸네일 폴백을 컴포넌트 밖으로 밀어냈다([research.md R-019](./research.md)). 세션 조회도 같은 이유로 도메인 표면을 넓혀 해결했다 — plan 2.1.0까지는 feature가 `:core:data`의 `internal` 제공자를 부르는 설계여서 이 게이트가 실제로는 성립하지 않았고, [research.md R-020](./research.md)이 그 경로를 바로잡았다. 다른 feature 모듈을 의존하지 않는다 — 방 카드는 `:feature:sample`에서 **참조**하지 않고 `:core:design-system`으로 **이관**한다([research.md R-010](./research.md)). DI 바인딩은 구현을 가진 모듈이 소유한다 |
| **III. 결정과 실패는 기록으로 남는다** | PASS | PASS | WorkManager 채택은 라이브러리 신규 도입이자 다른 feature에도 구속력을 갖는 결정이라 ADR 대상이었고, [ADR로 기록했다](../../adr/2026-08-26-workmanager-for-detached-requests.md)(2026-08-26). 뒤집힌 결정(R-001·R-005 일부·**R-014**)은 지우지 않고 취소선과 재검토 표시로 남겼다. R-014는 서버가 `roomIds` 배열 계약을 열면서 근거를 잃었고 R-021이 대신한다 — 그 경위를 기록에 남겼다. 응답 봉투 규칙(R-018)도 다른 feature에 구속력을 가져 [ADR로 기록했다](../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)(2026-08-27) |
| **IV. 명세가 구현에 선행한다** | PASS | PASS | spec 2.4.0을 입력으로 삼았고 plan에만 있는 요구사항을 만들지 않았다. plan 3.1.0이 다루는 태스크 분리와 `onNewIntent`는 **spec이 먼저 FR-003을 보강하고 TS-027·TS-028·EC-013을 신설한 뒤** 그 요구를 설계로 옮긴 것이다 — 구현에서 결함을 발견했더라도 spec을 건너뛰고 계약부터 고치지 않았다. 저장 계약이 배열로 바뀌어 **TS-019(부분 실패)를 지키는 주체가 클라이언트에서 서버로 옮겨졌으나 spec 개정은 필요하지 않다** — spec §4 가정과 TS-019가 요구하는 것은 "방마다 독립적으로 성립한다"는 결과이지 그것을 누가 실행하느냐가 아니고, 클라이언트 요구사항은 줄어드는 방향이다([research.md R-021](./research.md)). spec이 정의하지 않은 조회 실패 상태는 새 상태를 만들지 않고 FR-013으로 수렴시켰다([research.md R-006](./research.md)). 실서버 전환 범위를 이 feature가 쓰는 두 엔드포인트로 묶은 것도 같은 원칙이다 — `group-room-form`의 mock 전환을 요구하는 spec은 없다([research.md R-015](./research.md)) |
| **V. 컨벤션은 권고가 아니라 게이트** | PASS | PASS | 브랜치는 `feature/158-instagram-share-receive/plan`으로 base에서 분기했다. 에러는 `MinoDomainException`으로 매핑해 소비하고 프로그래머 버그는 전파한다 — `MinoIdentityProofPlugin`의 `checkNotNull`에 도달하지 않도록 세션 확인을 요청보다 앞에 둔다([research.md R-012](./research.md)). 그 확인은 도메인 계약 `AnonymousAuthRepository.currentSession()`으로 한다 — `:core:data`의 `internal` 제공자에 feature가 직접 닿지 않는다([research.md R-020](./research.md)) |
| **기술 표준 — 디자인 토큰** | PASS | PASS | 값이 일치하는 토큰이 있으면 토큰, 없으면 Figma 실측값을 쓴다. 판정·대조는 [`figma-design-fidelity.md`](../../conventions/figma-design-fidelity.md)를 따르며 토큰 신설을 구현의 선행 조건으로 삼지 않는다 |
| **기술 표준 — 컴포넌트 배치** | PASS | PASS | Figma `013-1-2` 노드 트리를 열어 인스턴스/로컬 프레임을 판정했다([contracts/room-picker-sheet-ui.md §1](./contracts/room-picker-sheet-ui.md)). 인스턴스 4종은 `:core:design-system`, 로컬 프레임 4종은 feature가 소유한다. 썸네일만 폴백이 갈려 `:core:common:ui`가 받는다 — 이미지 에셋을 design-system에 두지 않는다는 [`component-asset-placement.md`](../../conventions/component-asset-placement.md) §1과 두 번째 사용처에서 승격한다는 §2.1을 따른 것이다 |
| **기술 표준 — M3 컴포넌트 패턴** | PASS | PASS | 신설·이관 컴포넌트 모두 `Defaults`·`Colors`·컴포넌트 토큰 구성을 따른다 |

**정당화가 필요한 이탈 1건** — 진입형 feature 골격에서 `Shell`·`NavHost`·`Launcher`를 뺀다. §복잡도 추적에 기록했다.

## 프로젝트 구조 (Project Structure)

### 문서 (이번 Feature)

```text
docs/specs/shared-link-receiver/
├── spec.md              # 입력 (/mino-spec 산출물, 이 단계에서 수정하지 않음)
├── plan.md              # 이 파일 (/mino-plan 산출물)
├── research.md          # Phase 0 산출물 — 설계 결정 24건 (뒤집힌 항목은 이력으로 보존)
├── data-model.md        # Phase 1 산출물 — 도메인·UI 타입
├── quickstart.md        # Phase 1 산출물 — 검증 시나리오
├── contracts/           # Phase 1 산출물
│   ├── shared-place-save-api.md    # 저장 API (배포됨)
│   ├── room-list-api.md            # 방 목록 API (배포됨 · 썸네일만 확장 요청)
│   ├── share-intent.md             # OS 공유 인텐트 수신
│   └── room-picker-sheet-ui.md     # 시트 UI 컴포넌트 배치·표면
├── quality/
│   └── spec-checklist.md
└── tasks.md             # /mino-task 산출물 (이 단계가 생성하지 않음)
```

### 소스 코드 (Repository Root 기준)

```text
feature/sharereceiver/                                  # 신규 모듈 (진입형)
└── src/main/
    ├── AndroidManifest.xml                             # ACTION_SEND intent-filter + taskAffinity="" (contracts/share-intent.md)
    └── java/team/mino/feature/sharereceiver/
        ├── ShareReceiverActivity.kt                    # public. 투명 테마 + Route 직접 호스팅 + onNewIntent (R-024)
        └── picker/
            ├── screen/    ShareReceiverRoute.kt · ShareReceiverScreen.kt
            ├── vm/        ShareReceiverViewModel · UiState · Intent · SideEffect
            ├── model/     RoomPickerItem.kt · SheetStep.kt
            └── component/ RoomPickerSheet · RoomPickerHeader · RoomPickerList · RoomPickerEmpty

core/design-system/src/main/java/team/mino/core/designsystem/component/
├── roomcard/          # :feature:sample에서 이관 (research.md R-010)
├── checkbox/          # 신설 — 이관 중 분리
├── roomthumbnail/     # 신설 — 콜라주만. 폴백은 슬롯으로 받는다 (research.md R-019)
└── scrollbar/         # 신설

core/common/ui/src/main/
├── java/team/mino/core/common/ui/component/RoomThumbnailFallback.kt   # 신설 — 색 배경 + 캐릭터
└── res/drawable-{mdpi,xhdpi,xxhdpi}/room_thumbnail_*.webp             # :feature:roomform에서 승격 (13종 × 3벌)

core/domain/src/main/kotlin/team/mino/core/domain/
├── model/         RoomSummary.kt · RoomType.kt · SharedPlaceSaveRequest.kt
├── repository/    RoomRepository.kt (함수 추가) · SharedPlaceRepository.kt (신설)
│                  AnonymousAuthRepository.kt (조회 함수 추가 — research.md R-020)
└── usecase/       ExtractSharedUrlUseCase.kt · GetRoomPickerRoomsUseCase.kt

core/data/src/main/java/team/mino/core/data/
├── network/
│   ├── dto/       response/MinoResponse.kt · response/RoomSummaryResponse.kt
│   │              request/PinCreateRequest.kt
│   └── service/   RoomApiService.kt · PinApiService.kt                   # 신규 패키지 — 첫 실서버 호출
├── datasource/    RoomListRemoteDataSource.kt(+Impl) · PinRemoteDataSource.kt(+Impl)
│                  di/RoomListDataSourceModule.kt · di/PinDataSourceModule.kt
├── repository/    SharedPlaceRepositoryImpl.kt(신설 — 워커 예약) · RoomRepositoryImpl.kt(함수 추가)
│                  AnonymousAuthRepositoryImpl.kt(조회 함수 추가)
│                  mapper/RoomSummaryMapper.kt · di/SharedPlaceRepositoryModule.kt
└── work/          SharedPlaceSaveWorker.kt · di/WorkManagerModule.kt     # 신규 패키지

# 기존 RoomRemoteDataSource·RoomMockRemoteDataSourceImpl·RoomDataSourceModule은 건드리지 않는다 (research.md R-015)

feature/sample/…/main/component/                        # 이관 후 삭제되는 파일들
feature/roomform/…/res/drawable-*/room_thumbnail_*.webp # 승격 후 삭제 · RoomColorUiModel.kt의 thumbnailRes도 함께
```

**구조 결정**: 신규 진입형 feature 모듈 `:feature:sharereceiver` 하나를 더하고, 나머지는 기존 모듈에 파일을 더한다.

- **feature 종류**: 진입형. Activity로 독립 진입하며 탭 셸의 그래프에 편입되지 않는다. 다만 `Shell`·`NavHost`·`Launcher`를 두지 않는다(§복잡도 추적).
- **화면 디렉터리 이름**: `picker/`. `main/`을 쓰지 않은 이유는 이 모듈에 화면이 하나뿐이고, 그 화면의 이름이 "첫 화면"이 아니라 "방 선택"이기 때문이다.
- **워커의 자리**: `:core:data/work/`. 워커는 `PinRemoteDataSource`를 호출해 네트워크 요청을 수행하는 데이터 레이어 인프라이며, feature 모듈은 `:core:data`를 의존할 수 없다([`core/data/README.md`](../../../core/data/README.md) §10). feature는 `WorkManager.enqueue`를 호출하는 대신 도메인 계약의 예약 함수 `SharedPlaceRepository.scheduleSave()` 하나만 호출한다.
- **예약과 전송의 경계**: 도메인이 노출하는 것은 예약뿐이고, 전송은 `:core:data` 안에서 끝난다([research.md R-017](./research.md)). **방을 쪼개지 않는다** — 서버가 `roomIds` 배열을 받으므로 예약도 요청도 1건이다([research.md R-021](./research.md)).
- **DataSource를 새로 세우는 이유**: 기존 `RoomRemoteDataSource`는 `group-room-form`이 확정한 mock 바인딩을 물고 있어 함수를 더하면 그 feature까지 실서버로 끌려온다([research.md R-015](./research.md)).
- **썸네일이 두 모듈로 갈리는 이유**: 콜라주는 Figma 디자인 시스템 컴포넌트라 `:core:design-system`이 갖지만, 폴백은 도메인 색 값과 캐릭터 래스터 이미지를 함께 요구해 그 모듈이 받을 수 없다. 폴백을 `:core:common:ui`로 밀어내고 슬롯으로 잇는다([research.md R-019](./research.md)). **이 승격은 `:feature:roomform`을 함께 고친다** — 에셋의 첫 소유자가 그쪽이고, 두 번째 사용처가 생긴 지금이 승격 시점이다.
- **세션 조회의 자리**: `:core:domain`이다. 복원 수단은 `:core:data`의 `AnonymousAuthProvider`이지만 그 인터페이스는 `internal`이고 feature는 `:core:data`를 의존하지 않으므로, 조회를 `AnonymousAuthRepository`의 함수로 올려 feature가 도메인 경로로만 닿게 한다. 확보 함수 `ensureSession()`은 손대지 않아 이 진입점이 세션을 새로 만들지 않는다는 규칙이 유지된다([research.md R-020](./research.md)).
- **`:core:navigation` 변경 없음**: 이 화면을 여는 feature가 없으므로 `XLauncher`·`EXTRA_*`를 추가하지 않는다([contracts/share-intent.md §4](./contracts/share-intent.md)).

### 서버 계약 상태

2026-08-28 `https://api.gguk.org/api-docs-json`(`Team MINO API` 1.0.0) 재확인 기준.

| 계약 | 상태 | 클라이언트 대응 |
|---|---|---|
| `POST /api/v1/rooms/pins` — 저장 | **배포됨** (bearer 인증, `roomIds` 배열) | 실 `PinApiService`. **방 N개 → 요청 1건** |
| ~~`POST /api/v1/rooms/{roomId}/pins`~~ | **삭제됨** | plan 2.x의 방 단위 계약은 무효 — [research.md R-021](./research.md) |
| ~~`POST /api/v1/place/places`~~ | 삭제됨 | plan 1.0.0의 확장 요청은 무효 — [research.md R-013](./research.md) |
| `GET /api/v1/rooms` — 기본 필드 | 배포됨 | 실 `RoomApiService` |
| `GET /api/v1/rooms` — `thumbnailList` | **배포됨** | 이름·의미가 요청과 다르다. URL만 남기고 색상 키는 버린다 — [research.md R-022](./research.md) |
| 알림 생성 (FR-014·FR-015) | 엔드포인트 없음 | 서버 소관. 클라이언트 책임은 저장 요청 전달까지 |

**서버 공백이 없다.** plan 2.x가 남겨 둔 썸네일 확장 요청은 `thumbnailList`로 해소됐고, 저장 계약은 배열을 받는 형태로 바뀌었다.

**과도기 상태 하나** — `room` 리소스에 DataSource가 둘이다. `getRooms()`만 실서버(`RoomListRemoteDataSource`)이고 방 생성·편집·상세는 mock(`RoomRemoteDataSource`)이다. `group-room-form`이 실서버로 전환하는 시점에 하나로 합쳐지며, 그때 지워지는 쪽은 `RoomListRemoteDataSource`다.

## 복잡도 추적 (Complexity Tracking)

> **헌장 준수 확인에서 정당화가 필요한 위반이 있는 경우에만 작성**

| 위반 사항 | 필요한 이유 | 더 단순한 대안을 기각한 이유 |
|---|---|---|
| 진입형 feature인데 `XShell`을 두지 않는다 ([feature-module.md §2](../../architecture/feature-module.md)) | `MinoScaffold`는 chrome·insets·불투명 배경을 여는데, FR-003·UX-001은 앱 화면을 그리지 말고 딤 배경 위에 시트만 띄우라고 요구한다 | 셸을 두고 배경을 투명하게 되돌리면, 셸이 제공하는 것을 셸 사용자가 무력화하는 코드가 남는다. 셸의 유일한 실질 기능인 화면 조회 로깅은 `AnalyticsTracker` 직접 호출로 대체된다 |
| 진입형 feature인데 `XNavHost`·`XDestinations`를 두지 않는다 | 화면이 방 선택 시트 하나뿐이고, UX-008이 "시트 안에서 방을 새로 만들거나 장소 정보를 편집하는 경로는 제공하지 않는다"로 못박아 내부 전환 대상이 구조적으로 존재하지 않는다 | 목적지가 하나인 `MinoNavHost`는 빈 그래프이고, 진입 인자를 시작 라우트에 싣는 규약도 인자가 `Intent` 하나뿐이라 얻는 것이 없다 |
| 진입형 feature인데 `:core:navigation`에 `XLauncher`를 두지 않는다 | 이 화면의 유일한 진입은 OS 공유 인텐트다. 앱 안에서 이 화면을 여는 feature가 없다 | 호출자가 없는 계약을 `:core:navigation`에 두면 죽은 공개 표면이 된다. [SCR-002] 온보딩 튜토리얼은 연습용 가상 화면이라 이 시트를 호출하지 않는다(spec §3.2 비목표) |
| WorkManager 신규 도입 | spec §4 가정("앱을 떠나도 저장 요청은 취소되지 않는다")과 FR-011(토스트 후 화면을 남기지 않고 물러난다)이 요구하는 생존 구간이 Activity 생애주기보다 길다 | `viewModelScope`는 Activity 종료와 함께 취소된다. Application scope 코루틴은 프로세스 종료 시 유실되며, 공유 수신은 프로세스가 방금 뜬 콜드 스타트가 잦아 유실 위험이 상시적이다. 근거는 [research.md R-004](./research.md) |
| M3 `ModalBottomSheet` 대신 시트를 직접 구현 | FR-008이 요구하는 높이가 콘텐츠와 무관한 고정 dp 3종(436 / 612 / 644)이다 | `ModalBottomSheet`의 `PartiallyExpanded`는 콘텐츠 높이의 비율로 결정되어 임의 dp 앵커를 지정할 수 없고, `Full`이 방 개수에 따라 612/644로 갈리는 규칙도 표현하지 못한다. 근거는 [research.md R-007](./research.md) |
