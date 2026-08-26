# 구현 계획: 외부 공유 수신 방 선택 바텀시트 (Shared Link Receiver)

**대상 스펙 경로**: `docs/specs/shared-link-receiver`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: 2.3.1

**최초 작성일**: 2026-08-26

**최종 수정일**: 2026-08-26

**버전**: 1.0.0

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

## 요약 (Summary)

인스타그램 등 외부 앱이 OS 공유 시트로 넘긴 링크를 받아, 앱 화면으로 전환하지 않고 딤 배경 위에 방 선택 바텀시트만 띄운다. 사용자가 방을 복수 선택하고 `[저장하기]`를 누르면 저장 요청을 접수하고 곧바로 외부 앱으로 물러난다. 링크 분석은 저장 이후 서버가 수행하며, 결과는 알림함으로 사후 전달된다.

기술적 접근은 넷으로 요약된다.

1. **투명 Activity + 자체 시트** — `ACTION_SEND`를 받는 `ShareReceiverActivity`가 투명 테마로 뜨고, `AnchoredDraggable` 기반 2단 고정 높이 시트를 직접 그린다. 스플래시·셸·NavHost를 거치지 않는다.
2. **로컬 세션 복원만** — 기존 `AnonymousAuthProvider.currentUserId()`로 네트워크 왕복 없이 uid를 복원한다. 세션이 없으면 새로 확보하지 않고 빈 목록 경로로 넘긴다.
3. **WorkManager로 요청 생존 보장** — `[저장하기]` 이후의 요청을 워커로 넘겨 Activity 종료·프로세스 종료 후에도 살아남게 한다.
4. **디자인 시스템 이관** — `:feature:sample`에 이미 있는 방 카드 일가를 `:core:design-system`으로 옮기고, 체크박스·썸네일·스크롤 바를 분리·신설한다.

서버 계약 두 건(저장 API의 `roomIds`·인증, 방 목록의 썸네일 이미지)이 아직 없어 `contracts/`에 요청 명세를 남기고 mock으로 개발한다.

## 기술 컨텍스트 (Technical Context)

**언어/버전**: Kotlin 2.2.10 · JDK 17 · AGP 9.1.1

**주요 의존성**: Jetpack Compose (BOM 2026.04.01) · Hilt 2.59.2 · Ktor Client 3.3.0 · kotlinx.serialization · Coil 3.3.0 · Firebase Auth · **WorkManager (신규 — [research.md R-004](./research.md))**

**저장소**: 없음. 이 feature는 로컬 영속 데이터를 두지 않는다. 방 목록은 매 진입 시 조회하고, 공유받은 링크는 보관하지 않는다(FR-013)

**테스트**: JUnit + `kotlinx-coroutines-test` (UseCase·ViewModel) · Compose Preview (컴포넌트) · `androidx.work:work-testing` (워커)

**대상 플랫폼**: Android (minSdk는 프로젝트 설정을 따른다)

**프로젝트 유형**: mobile-app — 다중 Gradle 모듈

**성능 목표**: OS 공유 시트에서 꾹을 고른 뒤 시트가 조작 가능해지기까지 1초 이내 (SC-001). 시트 표출이 방 목록 조회·링크 분석을 기다리지 않는 것이 이 목표의 설계적 근거다

**제약 조건**: 스플래시·세션 확보 대기를 끼워 넣지 않는다 (UX-010) · 시트 표출과 `[저장하기]` 사이에 로딩 표현을 두지 않는다 (UX-009) · `[저장하기]` 이후 요청은 앱을 떠나도 취소되지 않는다 (spec §4 가정)

**규모/범위**: 화면 1개(상태 2종: 방 목록 / 빈 목록) · 신규 feature 모듈 1개 · 신규·이관 디자인 시스템 컴포넌트 4종 · 도메인 모델 3종 · 신규 API 계약 2건

## 헌법 준수 확인 게이트 (Constitution Check)

*게이트: Phase 0 리서치 전에 반드시 통과해야 한다. Phase 1 설계 후 재확인한다.*

[헌법](../../constitution.md) 2.1.0 기준.

| 게이트 | Phase 0 전 | Phase 1 후 | 판정 근거 |
|---|---|---|---|
| **I. 단일 출처 문서화** | PASS | PASS | 규약·README·PRD의 규칙 본문을 복제하지 않고 링크로 지목했다. 이 feature 안에서만 유효한 선택은 `research.md`가, 서버·OS 계약은 `contracts/`가 소유한다 |
| **II. 레이어 경계와 의존 방향** | PASS | PASS | `:feature:sharereceiver` → `:core:domain`·`:core:design-system`·`:core:common:*`. `:core:data`를 직접 의존하지 않는다. 다른 feature 모듈을 의존하지 않는다 — 방 카드는 `:feature:sample`에서 **참조**하지 않고 `:core:design-system`으로 **이관**한다([research.md R-010](./research.md)). DI 바인딩은 구현을 가진 모듈이 소유한다 |
| **III. 결정과 실패는 기록으로 남는다** | PASS | PASS | WorkManager 채택은 라이브러리 신규 도입이자 다른 feature에도 구속력을 갖는 결정이라 ADR 대상이었고, [ADR로 기록했다](../../adr/2026-08-26-workmanager-for-detached-requests.md)(2026-08-26) |
| **IV. 명세가 구현에 선행한다** | PASS | PASS | spec 2.3.1을 입력으로 삼았고 plan에만 있는 요구사항을 만들지 않았다. spec이 정의하지 않은 조회 실패 상태는 새 상태를 만들지 않고 FR-013으로 수렴시켰다([research.md R-006](./research.md)). 템플릿을 복사한 뒤 제자리 편집했다 |
| **V. 컨벤션은 권고가 아니라 게이트** | PASS | PASS | 브랜치는 `feature/158-instagram-share-receive/plan`으로 base에서 분기했다. 에러는 `MinoDomainException`으로 매핑해 소비하고 프로그래머 버그는 전파한다 — `MinoIdentityProofPlugin`의 `checkNotNull`에 도달하지 않도록 세션 확인을 요청보다 앞에 둔다([research.md R-012](./research.md)) |
| **기술 표준 — 디자인 토큰** | PASS | PASS | 값이 일치하는 토큰이 있으면 토큰, 없으면 Figma 실측값을 쓴다. 판정·대조는 [`figma-design-fidelity.md`](../../conventions/figma-design-fidelity.md)를 따르며 토큰 신설을 구현의 선행 조건으로 삼지 않는다 |
| **기술 표준 — 컴포넌트 배치** | PASS | PASS | Figma `013-1-2` 노드 트리를 열어 인스턴스/로컬 프레임을 판정했다([contracts/room-picker-sheet-ui.md §1](./contracts/room-picker-sheet-ui.md)). 인스턴스 4종은 `:core:design-system`, 로컬 프레임 4종은 feature가 소유한다 |
| **기술 표준 — M3 컴포넌트 패턴** | PASS | PASS | 신설·이관 컴포넌트 모두 `Defaults`·`Colors`·컴포넌트 토큰 구성을 따른다 |

**정당화가 필요한 이탈 1건** — 진입형 feature 골격에서 `Shell`·`NavHost`·`Launcher`를 뺀다. §복잡도 추적에 기록했다.

## 프로젝트 구조 (Project Structure)

### 문서 (이번 Feature)

```text
docs/specs/shared-link-receiver/
├── spec.md              # 입력 (/mino-spec 산출물, 이 단계에서 수정하지 않음)
├── plan.md              # 이 파일 (/mino-plan 산출물)
├── research.md          # Phase 0 산출물 — 설계 결정 12건
├── data-model.md        # Phase 1 산출물 — 도메인·UI 타입
├── quickstart.md        # Phase 1 산출물 — 검증 시나리오
├── contracts/           # Phase 1 산출물
│   ├── shared-place-save-api.md    # 저장 API (서버 확장 요청)
│   ├── room-list-api.md            # 방 목록 API
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
    ├── AndroidManifest.xml                             # ACTION_SEND intent-filter (contracts/share-intent.md)
    └── java/team/mino/feature/sharereceiver/
        ├── ShareReceiverActivity.kt                    # public. 투명 테마 + Route 직접 호스팅
        └── picker/
            ├── screen/    ShareReceiverRoute.kt · ShareReceiverScreen.kt
            ├── vm/        ShareReceiverViewModel · UiState · Intent · SideEffect
            ├── model/     RoomPickerItem.kt · SheetStep.kt
            └── component/ RoomPickerSheet · RoomPickerHeader · RoomPickerList · RoomPickerEmpty

core/design-system/src/main/java/team/mino/core/designsystem/component/
├── roomcard/          # :feature:sample에서 이관 (research.md R-010)
├── checkbox/          # 신설 — 이관 중 분리
├── roomthumbnail/     # 신설 — 이관 중 분리
└── scrollbar/         # 신설

core/domain/src/main/kotlin/team/mino/core/domain/
├── model/         RoomSummary.kt · RoomType.kt · SharedPlaceSaveRequest.kt
├── repository/    RoomRepository.kt (함수 추가) · SharedPlaceRepository.kt (신설)
└── usecase/       ExtractSharedUrlUseCase.kt · GetRoomPickerRoomsUseCase.kt

core/data/src/main/java/team/mino/core/data/
├── datasource/    RoomRemoteDataSource.kt (함수 추가) · RoomMockRemoteDataSourceImpl.kt (대응)
│                  SharedPlaceRemoteDataSource.kt · SharedPlaceMockRemoteDataSourceImpl.kt
├── network/dto/   response/RoomSummaryResponse.kt · request/SharedPlaceSaveRequestBody.kt
├── repository/    SharedPlaceRepositoryImpl.kt · RoomRepositoryImpl.kt (함수 추가)
│                  mapper/RoomSummaryMapper.kt
└── work/          SharedPlaceSaveWorker.kt · di/WorkerModule.kt          # 신규 패키지

feature/sample/…/main/component/                        # 이관 후 삭제되는 파일들
```

**구조 결정**: 신규 진입형 feature 모듈 `:feature:sharereceiver` 하나를 더하고, 나머지는 기존 모듈에 파일을 더한다.

- **feature 종류**: 진입형. Activity로 독립 진입하며 탭 셸의 그래프에 편입되지 않는다. 다만 `Shell`·`NavHost`·`Launcher`를 두지 않는다(§복잡도 추적).
- **화면 디렉터리 이름**: `picker/`. `main/`을 쓰지 않은 이유는 이 모듈에 화면이 하나뿐이고, 그 화면의 이름이 "첫 화면"이 아니라 "방 선택"이기 때문이다.
- **워커의 자리**: `:core:data/work/`. 워커는 `SharedPlaceRepository`를 호출해 네트워크 요청을 수행하는 데이터 레이어 인프라이며, feature 모듈은 `:core:data`를 의존할 수 없다([`core/data/README.md`](../../../core/data/README.md) §10). feature는 `WorkManager.enqueue`를 호출하는 대신 도메인 계약 뒤에 감춰진 예약 함수를 호출한다.
- **`:core:navigation` 변경 없음**: 이 화면을 여는 feature가 없으므로 `XLauncher`·`EXTRA_*`를 추가하지 않는다([contracts/share-intent.md §4](./contracts/share-intent.md)).

### 서버 계약 상태

| 계약 | 상태 | 클라이언트 대응 |
|---|---|---|
| `POST /api/v1/place/places` — `roomIds` + 인증 | **없음. 서버 협의 필요** | mock DataSource로 개발. 인터페이스는 실계약이 붙어도 불변 |
| `GET /api/v1/rooms` — 기본 필드 | 있음 | 그대로 사용 |
| `GET /api/v1/rooms` — `thumbnailImageUrls` | **없음. 서버 협의 필요** | 빈 목록으로 읽어 대표 색상 폴백 |
| 알림 생성 (FR-014·FR-015) | 엔드포인트 없음 | 서버 소관. 클라이언트 책임은 저장 요청 전달까지 |

## 복잡도 추적 (Complexity Tracking)

> **헌장 준수 확인에서 정당화가 필요한 위반이 있는 경우에만 작성**

| 위반 사항 | 필요한 이유 | 더 단순한 대안을 기각한 이유 |
|---|---|---|
| 진입형 feature인데 `XShell`을 두지 않는다 ([feature-module.md §2](../../architecture/feature-module.md)) | `MinoScaffold`는 chrome·insets·불투명 배경을 여는데, FR-003·UX-001은 앱 화면을 그리지 말고 딤 배경 위에 시트만 띄우라고 요구한다 | 셸을 두고 배경을 투명하게 되돌리면, 셸이 제공하는 것을 셸 사용자가 무력화하는 코드가 남는다. 셸의 유일한 실질 기능인 화면 조회 로깅은 `AnalyticsTracker` 직접 호출로 대체된다 |
| 진입형 feature인데 `XNavHost`·`XDestinations`를 두지 않는다 | 화면이 방 선택 시트 하나뿐이고, UX-008이 "시트 안에서 방을 새로 만들거나 장소 정보를 편집하는 경로는 제공하지 않는다"로 못박아 내부 전환 대상이 구조적으로 존재하지 않는다 | 목적지가 하나인 `MinoNavHost`는 빈 그래프이고, 진입 인자를 시작 라우트에 싣는 규약도 인자가 `Intent` 하나뿐이라 얻는 것이 없다 |
| 진입형 feature인데 `:core:navigation`에 `XLauncher`를 두지 않는다 | 이 화면의 유일한 진입은 OS 공유 인텐트다. 앱 안에서 이 화면을 여는 feature가 없다 | 호출자가 없는 계약을 `:core:navigation`에 두면 죽은 공개 표면이 된다. [SCR-002] 온보딩 튜토리얼은 연습용 가상 화면이라 이 시트를 호출하지 않는다(spec §3.2 비목표) |
| WorkManager 신규 도입 | spec §4 가정("앱을 떠나도 저장 요청은 취소되지 않는다")과 FR-011(토스트 후 화면을 남기지 않고 물러난다)이 요구하는 생존 구간이 Activity 생애주기보다 길다 | `viewModelScope`는 Activity 종료와 함께 취소된다. Application scope 코루틴은 프로세스 종료 시 유실되며, 공유 수신은 프로세스가 방금 뜬 콜드 스타트가 잦아 유실 위험이 상시적이다. 근거는 [research.md R-004](./research.md) |
| M3 `ModalBottomSheet` 대신 시트를 직접 구현 | FR-008이 요구하는 높이가 콘텐츠와 무관한 고정 dp 3종(436 / 612 / 644)이다 | `ModalBottomSheet`의 `PartiallyExpanded`는 콘텐츠 높이의 비율로 결정되어 임의 dp 앵커를 지정할 수 없고, `Full`이 방 개수에 따라 612/644로 갈리는 규칙도 표현하지 못한다. 근거는 [research.md R-007](./research.md) |
