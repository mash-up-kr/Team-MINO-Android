# 구현 계획: 알림함 (Notifications)

**대상 스펙 경로**: `docs/specs/notifications`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: 7.0.0

**최초 작성일**: 2026-09-01

**최종 수정일**: 2026-09-05

**버전**: 1.2.2

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

## 요약 (Summary)

바텀 네비게이션 세 번째 탭에 알림 목록을 놓고, 알림을 눌러 그 알림이 가리키는 화면으로 보낸다. 화면 상태는 목록·빈 상태·오류 셋뿐이며 읽음 여부도 권한 유도도 두지 않는다(SC-009).

새 탭 feature 모듈 `:feature:notifications`를 만들고, `:core:domain`에 알림 조회·도착지 해석 계약을, `:core:data`에 그 구현을 세운다. `:feature:main`의 placeholder 탭 자리를 이 모듈의 그래프 등록으로 교체한다.

설계에서 갈린 지점 셋이 이 계획의 뼈대다.

- **저장 오류 안내 화면은 같은 그래프의 두 번째 목적지다.** 그래야 바텀 네비게이션이 배선 없이 유지된다(FR-011).
- **도착지 해석은 UseCase가 소유하되 조회를 하지 않는다.** 서버가 `payload.pinId`로 도착지 핀을 지목해 주므로(spec 5.0.0 FR-022), 유형과 대상만 보는 순수 매핑이다.
- **장소 상세·방 상세로 가는 두 전환은 홀더를 지난다.** 둘 다 저장 탭 안의 화면이라 Activity 전환이 아니고, 알림 모듈은 `pinId`·`roomId`를 콜백으로 올리는 데까지만 관여한다([research.md D10·D14](./research.md)).

판단 근거는 [research.md](./research.md)가 소유한다.

## 기술 컨텍스트 (Technical Context)

**언어/버전**: Kotlin · Jetpack Compose · Hilt (버전은 `gradle/libs.versions.toml`이 소유)

**주요 의존성**: 기존 스택만 쓴다 — Ktor(`:core:data`), Hilt, Compose Navigation(`:core:navigation`), DataStore, kotlinx-collections-immutable. **새로 더하는 라이브러리가 없다**([research.md D3](./research.md)에서 Paging 도입을 기각했다).

**저장소**: 없다. 알림 목록도 도착지도 기기에 저장하지 않는다(spec §4의 온라인 전제). 1.0.x가 두었던 DataStore(장소별 표시 기준 방)는 [research.md D6](./research.md) 폐기로 사라졌다.

**테스트**: JUnit + `kotlinx-coroutines-test`. ViewModel 테스트는 Fake Repository를 쓰는 `:feature:home`의 방식을 따른다.

**대상 플랫폼**: Android (`:app`의 `minSdk`/`targetSdk`를 따른다)

**프로젝트 유형**: mobile-app — 다중 Gradle 모듈

**성능 목표**: 알림 탭 진입 후 2초 이내 첫 화면(SC-001) · 스크롤 중 프레임 드랍 없음(SC-002) · 첫 화면을 위해 20건을 넘는 알림을 기다리지 않음(SC-011)

**제약 조건**: 알림 → 대상 화면까지 탭 1회, 방을 고르는 단계 0회(SC-004·SC-014) · 추가 로드 실패가 이미 그린 목록을 지우지 않음(UX-012) · 화면 상태는 목록·빈 상태·오류 셋(SC-009)

**규모/범위**: 화면 2개(알림 목록·저장 오류 안내) · 알림 유형 6종 · 페이지당 20건, 총량 상한 없음

**참조 API 문서**: `https://api.gguk.org/api-docs-json` (Team MINO API 1.0.0, 오퍼레이션 29개) — **조회 시점 2026-09-04T16:54:22+09:00**. 쓰는 오퍼레이션과 스키마 인용은 [contracts/notification-api.md](./contracts/notification-api.md)가 소유한다.

## 헌법 준수 확인 게이트 (Constitution Check)

[`docs/constitution.md`](../../constitution.md) 2.1.0 기준. **Phase 0 전 판정과 Phase 1 설계 후 재판정이 같다.**

| 게이트 | 판정 | 근거 |
|---|---|---|
| **I. SSOT** | 통과 | 규약을 본문으로 옮기지 않고 링크로 지목했다. FR-004 유형 문구를 클라이언트가 갖지 않는 것도 같은 이유다([research.md D4](./research.md)) — 서버와 두 벌이 되면 어느 쪽이 최신인지 알 수 없다 |
| **II. 레이어 경계와 의존 방향** | 통과 | `:feature:notifications` → `:core:domain`·`:core:design-system`·`:core:common:*` 단방향. **다른 feature를 의존하지 않는다** — 셸(`:feature:main`) → 탭 feature는 허용된 예외다. 바인딩은 구현을 가진 `:core:data`가 소유한다([contracts/notification-repository.md §5](./contracts/notification-repository.md)) |
| **III. 결정과 실패 기록** | 통과 | 되돌리기 어려운 결정이 이 feature 안에서만 유효해 `research.md`가 담는다. **ADR 승격 후보 1건**은 아래 §ADR 판정에 적었다 |
| **IV. Spec-First** | 통과 | spec 7.0.0을 입력으로 삼았고 **plan에만 있고 spec에 근거가 없는 요구사항을 두지 않았다.** 각 설계 판단이 FR·UX·EC·SC를 지목한다. 템플릿을 먼저 복사한 뒤 제자리 편집했다 |
| **V. 컨벤션 게이트** | 통과 | 에러 처리는 [`error_handling.md`](../../conventions/error_handling.md)의 통로 분리를 따른다([research.md D11](./research.md)). Compose Lint·브랜치·커밋은 구현 단계의 게이트다 |
| **디자인 토큰 판정** | 통과 | 값이 일치하는 토큰이 있으면 토큰, 없으면 실측값. 판정 절차는 [`figma-design-fidelity.md`](../../conventions/figma-design-fidelity.md)를 따르고 구현 착수 시 Figma 원본과 대조한다 |
| **컴포넌트·에셋 소속** | 통과 | `Room Thumbnail`은 Figma 디자인 시스템 컴포넌트라 `:core:design-system`이 계속 소유하며, **이 spec은 그 모듈에 아무 변경도 남기지 않는다** — spec 6.0.0이 방 썸네일 합성을 걷어내 컴포넌트를 부를 일이 없어졌다([research.md D5·D13](./research.md) 폐기). 알림 행·경과 시간 포맷터는 이 화면 전용이라 feature에 둔다([`component-asset-placement.md`](../../conventions/component-asset-placement.md)) |

**정당화가 필요한 위반 없음.** 복잡도 추적 표를 비워 둔다.

### ADR 판정

| 결정 | 구속력 | 처리 |
|---|---|---|
| D1~D14 | 이 feature 안에서만 유효 | `research.md`가 소유 |

**ADR 승격 후보가 없다.** 1.0.0은 D6(장소별 표시 기준 방의 소유 규칙 — "여러 화면이 공유하는 기기 로컬 상태"의 선례가 될 것으로 봤다)을 후보로 올렸으나, spec 5.0.0이 도착지 판정을 서버로 옮기면서 그 저장소 자체가 사라졌다(1.1.0에서 D6 폐기). 승격할 결정이 없으므로 이 feature는 ADR을 낳지 않는다.

## 프로젝트 구조 (Project Structure)

### 문서 (이번 Feature)

```text
docs/specs/notifications/
├── plan.md              # 이 파일 (/mino-plan 산출물)
├── research.md          # Phase 0 산출물 (/mino-plan)
├── data-model.md        # Phase 1 산출물 (/mino-plan)
├── quickstart.md        # Phase 1 산출물 (/mino-plan)
├── contracts/           # Phase 1 산출물 (/mino-plan)
│   ├── notification-api.md          # 서버 API 대조 결과와 협의 항목
│   ├── notification-ui.md           # 모듈 표면 · Intent/SideEffect · 화면 구성
│   └── notification-repository.md   # 도메인 계약 · UseCase · DI 소유
└── tasks.md             # /mino-task 산출물 (/mino-plan 이 생성하지 않음)
```

### 소스 코드 (Repository Root 기준)

```text
feature/notifications/                      # 신설
└── src/main/java/team/mino/feature/notifications/
    ├── NotificationNavigation.kt           # NotificationGraph · notificationGraph()
    └── main/
        ├── screen/                         # NotificationRoute · NotificationScreen
        │                                   # SaveErrorGuideScreen
        ├── component/                      # 알림 행 · 빈 상태 · 오류 상태 · 목록 끝 표시
        ├── vm/                             # NotificationViewModel · UiState · Intent · SideEffect
        ├── model/                          # NotificationItemUiModel · NotificationThumbnail
        │                                   # NotificationPhase
        └── util/                           # 경과 시간 포맷터 (FR-003)

core/domain/src/main/kotlin/team/mino/core/domain/
├── model/                                  # Notification · NotificationType · NotificationTarget
│                                           # NotificationPage · NotificationDestination
├── repository/                             # NotificationRepository
└── usecase/                                # ResolveNotificationDestinationUseCase

core/data/src/main/java/team/mino/core/data/
├── network/service/                        # NotificationApiService
├── network/dto/response/                   # 알림 목록 응답 DTO
├── datasource/                             # NotificationRemoteDataSource(+Impl)
├── repository/                             # NotificationRepositoryImpl
├── repository/mapper/                      # NotificationMapper
└── {datasource,repository}/di/             # 바인딩

feature/main/                               # 배선 교체
├── MainDestinations.kt                     # Notification Route 삭제
├── MainTab.kt                              # NOTIFICATION.route → NotificationGraph
├── MainActivity.kt                         # onRequestRoomDetail 전달 (홀더는 이미 주입돼 있다)
├── MainShell.kt                            # onRequestRoomDetail 파라미터 추가
└── MainNavHost.kt                          # placeholder → notificationGraph()
```

테스트는 각 모듈의 `src/test/`에 붙는다 — `:feature:notifications`는 ViewModel, `:core:domain`은 UseCase가 대상이다.

**구조 결정**: 위 트리대로 **탭 feature 단일 모듈 + 공용 도메인·데이터 신설**을 택한다. 화면과 상태는 `:feature:notifications`가 갖고, 두 spec이 공유하는 것(도메인 모델·조회 계약)만 `:core:*`로 올린다. 신설 Gradle 모듈은 `:feature:notifications` 하나이며 `libs.plugins.mino.android.feature` 컨벤션 플러그인을 그대로 쓴다.

## 복잡도 추적 (Complexity Tracking)

> **헌장 준수 확인에서 정당화가 필요한 위반이 있는 경우에만 작성**

위반 없음. 이 표는 비어 있다.

## 범위 경계

이 계획이 **하지 않는 것**과 그 이유다. spec §3.2와 [research.md](./research.md)가 근거를 소유한다.

| 항목 | 사유 |
|---|---|
| 장소별 표시 기준 방을 읽거나 쓰는 것 | spec 5.0.0이 도착지 판정을 서버로 옮겼다(D6·D7 폐기). 알림함은 이 값에 관여하지 않는다 |
| 도착지 대상의 생사 판정(EC-009·EC-010) | spec 7.0.0 UX-006이 이 판정을 [SCR-006]·[SCR-005]의 몫으로 옮겼다. 알림함은 이동 전에 되묻지 않는다. **두 spec에 「없는 대상」을 재시도 불가로 구분하는 과제를 넘겼다**([research.md 「인접 spec에 전할 것」](./research.md)) |
| 「경과일 초기화 확인」 호출 | [SCR-006]이 이미 `PlaceRepository.recordAccess`로 수행한다(D9) |
| 알림 발송 시점·주기 판정 | 서버 소유(spec §3.2) |
| OS 푸시 수신·표시·토큰 | 별도 범위(spec §3.2) |
| 알림 권한 요청 동선 | [SCR-008] 마이페이지(spec §3.2) |

**서버 협의 항목은 없다.** 1.0.x가 올린 네 건 중 셋은 spec 5.0.0·6.0.0이 닫았고, 남은 하나(FR-021 묶음)는 사용자 판단으로 협의 대상에서 내렸다([contracts/notification-api.md §3](./contracts/notification-api.md)). 검증하지 못한 채 남는 것은 [quickstart.md §5](./quickstart.md)가 나열한다.
