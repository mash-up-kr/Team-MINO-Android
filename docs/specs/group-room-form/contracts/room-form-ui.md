# 계약: 화면 (`RoomFormUiState` · `Intent` · `SideEffect`)

**대상 스펙 경로**: `docs/specs/group-room-form` · **부속 문서**: [plan.md](../plan.md)

`:feature:roomform`의 화면 계약이다. 타입의 필드는 [data-model.md](../data-model.md) §4가 소유하고, 여기서는 **무엇이 무엇을 부르는가**를 정한다.

> Route↔Screen 분리·셸/그래프 분리는 [`feature-module.md`](../../../architecture/feature-module.md) 4장, MVI 타입은 [`core/common/android/README.md`](../../../../core/common/android/README.md) §2가 소유한다.

---

## 1. 화면 구성

```
RoomFormActivity          extra 복원 → RoomFormShell(startDestination) · 결과 setResult·finish
└── RoomFormShell         MinoScaffold + navController + TrackScreenViews
    └── RoomFormNavHost   screen<RoomForm> { RoomFormRoute(onFinish) }
        └── RoomFormRoute (stateful)  VM·state 구독 · SideEffect 수집 · CollectDomainError
            └── RoomFormScreen (stateless)
                ├── MinoTopNavigation      뒤로가기 / 타이틀 / [건너뛰기]
                │                          생성 `공동방 만들기` · 편집 `방 편집` (FR-025)
                ├── (스크롤 영역)
                │   ├── RoomPreviewCard    썸네일 + 이름·설명 (FR-008)
                │   ├── MinoTextField      방 이름 (FR-002·FR-004) — 카운터 없음 (FR-003·TS-045)
                │   ├── MinoTextArea       방 설명 (FR-005) — state는 Route가 소유
                │   └── RoomColorPalette   3×4 칩 그리드 (FR-006)
                ├── MinoActionArea         하단 고정 CTA (UX-005)
                └── RoomFormConfirmDialog  dialog != null 일 때 (UX-008)
```

- **`RoomFormScreen`은 `Scaffold`를 열지 않는다.** 셸이 소유한다.
- **모달은 Route가 아니라 `RoomFormScreen` 안의 오버레이다** — [research.md](../research.md) R-011.
- `MinoTopNavigation`은 화면 고유 chrome이라 셸의 슬롯이 아니라 화면이 직접 배치한다([`feature-module.md`](../../../architecture/feature-module.md) 4장).
- **두 입력 필드의 상한을 자르는 주체가 다르다** — 방 이름은 ViewModel의 `NameChanged`, 방 설명은 `MinoTextArea`가 자른다. 근거와 대가는 [research.md](../research.md) R-019가 소유한다. 이 문서가 그 규칙의 계약이며, 다른 산출물은 여기를 지목한다.
- **방 설명 30자는 UI 차단이 유일한 강제 지점이다.** 도메인·Repository는 길이를 재검증하지 않는다 — 방 이름의 15자를 `ValidateRoomNameUseCase`가 판정하지 않는 것과 같은 이유다([contracts/room-repository.md](./room-repository.md) §2).
- `[TBD]` **방 설명의 글자 수 세는 단위가 spec 가정과 어긋난다.** spec §4 가정은 "사용자가 보는 문자 단위"를 요구하는데 `MinoTextArea`는 `state.text.length`(코드 유닛)로 세고 자른다. 방 설명에는 문자 종류 제한이 없어(EC-006) 이모지가 들어올 수 있고, 그때 `n/30` 카운터와 실제 차단 지점이 사용자가 보는 글자 수와 갈린다. 디자인 시스템 컴포넌트를 고칠지 편차를 받아들일지는 설계가 임의로 정하지 않는다.
- `RoomFormScreen`은 상태와 콜백만 받는다. `descriptionState`는 Route가 소유하므로 stateless는 유지된다.

```
@Composable
internal fun RoomFormScreen(
    state: RoomFormUiState,
    descriptionState: TextFieldState,
    onIntent: (RoomFormIntent) -> Unit,
    modifier: Modifier = Modifier,
)
```

---

## 2. `RoomFormIntent`

| Intent | 화면 조작 | ViewModel이 하는 일 | 근거 |
|---|---|---|---|
| `NameChanged(value)` | 방 이름 입력 | 15자로 자른 값을 `values.name`에 반영하고 `ValidateRoomNameUseCase` 재실행. **카운터는 그리지 않는다** | FR-003·FR-004·TS-045 |
| `DescriptionChanged(value)` | `descriptionState`의 텍스트 변화 | `values.description`에 **그대로** 반영한다 | FR-005·UX-007 |
| `ColorSelected(color)` | 칩 선택 | `values.color`를 교체. 같은 칩 재선택으로 해제하지 않는다 | FR-006·TS-006 |
| `SubmitClicked` | CTA | 생성이면 `dialog = Save`, 편집이면 곧바로 제출. `!canSubmit`이면 아무 일도 하지 않는다 | FR-020·UX-004·TS-038 |
| `SaveConfirmed` | 저장 확인 모달 [저장하기] | `dialog = null`로 모달을 닫고 `CreateRoomUseCase` 실행. **실패해도 모달을 다시 열지 않는다** | FR-010·TS-012·EC-009 |
| `BackClicked` | 뒤로가기 · OS 뒤로 제스처 — **`dialog == null`일 때만** | `needsExitConfirm`이면 이탈 모달, 아니면 즉시 종료 | FR-021·FR-024·EC-015 |
| `ExitConfirmed` | 이탈 모달 [나가기] | 입력값을 버리고 종료 | FR-018 |
| `DialogDismissed` | 모달 [취소] · 딤 바깥 탭 · **모달이 떠 있을 때의 뒤로가기** | `dialog = null`. 다른 상태를 건드리지 않는다 | UX-009·EC-017·EC-022 |
| `SkipClicked` | [건너뛰기] | 확인 없이 `skipped`로 종료 | FR-017·TS-024 |
| `RetryLoad` | 로드 실패 화면의 재시도 | 편집 진입 조회 재시도 | error_handling §5 |

**`SubmitClicked`·`SaveConfirmed`는 `isSubmitting`이 `true`면 무시된다** — UX-001·SC-005·EC-008.

**뒤로가기의 우선순위는 모달이 먼저다.** `dialog != null`이면 뒤로가기(OS 제스처 포함)는 `BackClicked`가 아니라 `DialogDismissed`로 들어온다. 이 순서를 정하지 않으면 저장 확인 모달 위의 뒤로가기가 이탈 확인 모달을 띄워 **EC-017을 정면으로 어긴다**. 세 모달 모두 같다.

**모달이 떠 있는 동안 입력 intent가 도달하지 않는다** — UX-008·EC-018. 이것은 그리기의 부산물이 아니라 **오버레이의 계약**이다: 딤 레이어가 하위 터치를 소비하고, 바깥 탭과 뒤로가기는 `DialogDismissed`로 올라온다. 이 계약을 만족하는 한 M3 `Dialog`로 띄우든 화면 안 오버레이로 그리든 구현이 정한다.

**미리보기 카드는 방 이름이 오류 상태여도 현재 입력값을 그대로 반영한다** — EC-007. `nameValidation`은 필드의 오류 표시와 CTA만 가르고 `RoomPreviewCard`가 읽는 `values`에는 관여하지 않는다.

---

## 3. `RoomFormSideEffect`

```
sealed interface RoomFormSideEffect : SideEffect {
    data class Finish(val outcome: RoomFormOutcome) : RoomFormSideEffect
}
```

```
sealed interface RoomFormOutcome {
    data class Created(val roomId: String) : RoomFormOutcome
    data class Updated(val roomId: String) : RoomFormOutcome
    data object Skipped : RoomFormOutcome
    data object Cancelled : RoomFormOutcome
}
```

- **SideEffect는 이것 하나다.** 스낵바·화면 전환은 이 feature가 하지 않는다([research.md](../research.md) R-004).
- `RoomFormRoute`가 `CollectSideEffect`로 받아 `onFinish(outcome)` 콜백을 올리고, `RoomFormActivity`가 [room-form-launcher.md](./room-form-launcher.md) §3의 Intent 표현으로 옮겨 `setResult` 후 `finish()`한다.
- **콜백이 Activity까지 올라가는 이유**: 전환·종료는 Activity의 몫이고 컴포저블이 시작하지 않는다([`feature-navigation.md`](../../../architecture/feature-navigation.md) 1장).

---

## 4. `RoomFormRoute`가 하는 것

| 책임 | 방법 | 근거 |
|---|---|---|
| state 구독 | `collectAsStateWithLifecycle()` | feature-module 4장 |
| 종료 신호 수집 | `CollectSideEffect(viewModel.sideEffect)` | common:ui §2 |
| 액션 실패 표시 | `CollectDomainError(viewModel)` → `LocalSnackbarHostState`로 스낵바 | error_handling §5·§6 |
| 리프 → 문구 매핑 | 이 파일의 `messageResOf(error)` `when` | error_handling §8 |
| OS 뒤로 제스처 | `BackHandler`로 가로채 `BackClicked`로 보낸다 | EC-015 |
| 방 설명 편집 버퍼 소유 | `TextFieldState`를 만들어 들고 `RoomFormScreen`에 넘긴다. 텍스트 변화를 `DescriptionChanged`로 ViewModel에 전달하며, **전달에 지연 연산자를 붙이지 않는다**([plan.md](../plan.md) §성능 목표). 전달 수단은 구현이 고른다 | [research.md](../research.md) R-019 |
| 편집 진입 초기값 주입 | `initial`이 `null`에서 **처음** non-null이 될 때만 넣는다. 재시도로 `initial`이 다시 채워져도 재주입하지 않고, 프로세스 사망 복원 시에는 `TextFieldState`가 복원한 값이 이긴다 | FR-013 · R-019 |

**온보딩에서는 `BackHandler`를 항상 켠 채 아무 일도 하지 않는다.** FR-022가 "OS 뒤로 제스처로도 이전 온보딩 스텝으로 되돌아갈 수 없게 한다"를 요구하므로, 온보딩이면 제스처를 삼킨다(TS-026·EC-015).

**`LocalSnackbarHostState`를 `RoomFormScreen`으로 내려보내지 않는다** — Route에서만 읽는다(error_handling §6).

---

## 5. `RoomFormViewModel`

```
@HiltViewModel
class RoomFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val roomRepository: RoomRepository,
    private val createRoom: CreateRoomUseCase,
    private val validateRoomName: ValidateRoomNameUseCase,
) : ViewModel(),
    MviContainer<RoomFormUiState, RoomFormSideEffect> by mviContainer(RoomFormUiState()),
    DomainErrorEmitter by domainErrorEmitter()
```

- `init`에서 `savedStateHandle.toRoute<RoomForm>()`로 `roomId`·`isOnboarding`을 복원한다. `roomId`가 있으면 `mode = Edit`으로 두고 조회를 시작한다.
- 코루틴 시작과 실패 소비는 [`error_handling.md`](../../../conventions/error_handling.md) §7 리뷰 규약을 따른다.

**단위 테스트 대상** (Fake `RoomRepository` 주입):

| 검증 | 대응 |
|---|---|
| 빈 폼 CTA 비활성 / 이름만 입력 시 활성 | TS-001·TS-002 |
| 진입 맥락에 따라 상단 타이틀과 CTA 라벨이 갈림 | TS-044·TS-037 |
| 방 이름 15자 상한에서 초과 입력이 반영되지 않음 | TS-003 |
| 오류 상태에서 다른 항목을 채워도 CTA 비활성 | TS-009 |
| 생성 CTA가 방을 만들지 않고 모달만 띄움 | TS-030 |
| 모달 [취소]가 입력값을 유지 | TS-031·TS-034 |
| 편집이 모달 없이 제출 | TS-019 |
| 편집에서 되돌린 값은 이탈 모달을 띄우지 않음 | TS-043 |
| 색상만 고른 빈 폼이 이탈 모달을 띄움 | EC-020 |
| 제출 실패 시 입력값 유지 + 도메인 에러 방출 | UX-003·EC-014 |
| 제출 중 재클릭이 요청을 늘리지 않음 | UX-001·SC-005 |
