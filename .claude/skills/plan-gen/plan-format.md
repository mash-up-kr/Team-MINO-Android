# SDD plan.md 작성 규약 (SSOT)

plan.md의 **출력 템플릿·통제 어휘·interactionType→MVI 매핑·아키텍처 규칙·작성 규칙**의 단일 출처다. `plan-gen` 스킬과 `plan-screen-designer`·`plan-data-designer`·`plan-reviewer` 서브에이전트는 모두 이 문서를 따르며, 규칙을 다른 곳에서 다시 풀어쓰지 않고 이 파일을 참조한다.

## 통제 어휘 (반드시 이 값만 사용)

- 모듈 구분: `new` / `modify`
- 데이터 레이어: `domain` / `data`
- (참조) spec의 `interactionType` 6종 — 아래 매핑 표의 키. 값 정의는 [`../spec-gen/spec-format.md`](../spec-gen/spec-format.md)를 따른다.

## interactionType → MVI 매핑 (강제)

> 이 절과 `아키텍처 규칙` 절의 원출처: [`docs/architecture/feature-module.md`](../../../docs/architecture/feature-module.md) · [`modularization.md`](../../../docs/architecture/modularization.md) · [`core/common/android/README.md`](../../../core/common/android/README.md). plan.md 출력에 강제할 항목만 발췌한 검수 기준이다 — 원본 변경 시 함께 갱신한다.

| interactionType | MVI 구현 |
|---|---|
| `display_state` | `UiState` 필드 (+ 파생/정렬 로직) |
| `user_action` | `XIntent`(sealed) → `processIntent` when → `updateState` |
| `async_process` | `XIntent` → `viewModelScope`+UseCase → `UiState.status`(sealed Idle/Loading/Success/Error) |
| `validation` | `XIntent` → `updateState`로 검증 상태 |
| `navigation` | 내부: `Route` 콜백 `onNavigateToX` / feature 간: `XLauncher`(api 모듈) |
| `modal_dialog` | `UiState` dialog 상태 (1회성이면 `SideEffect`) |

## 아키텍처 규칙 (반드시 준수)

- MVI: ViewModel은 `MviContainer<S,E> by mviContainer(초기State)` 위임. **Reducer 클래스 없음.** `updateState { copy() }` / `postSideEffect()` / `processIntent()`.
- **Intent는 사용자 액션이 있을 때만** 정의(없으면 화면에 Intent 없음).
- 비동기 상태는 `UiState` 안 **sealed `Status`(Idle/Loading/Success/Error)** 패턴.
- **feature `impl`은 `core:data`를 직접 의존하지 않는다** — `core:domain` 인터페이스만 알고, 구현 바인딩은 `:app` DI.
- 화면 패키지: `<screen>/{screen, vm, model, args, component}`. feature는 `{api, impl}`.
- 네비게이션: 내부 전환은 `Route` 콜백, feature 간 전환은 `Launcher`(api 모듈).

## 작성 규칙

- **추측 금지.** spec의 `Open Questions(TBD)`·`needs_policy` 항목은 `## 7. 미해결 / 외부 의존`으로 이어받아 명시한다.
- **참고한 docs를 `## 2. 참고 문서`에 빠짐없이 명시**한다.
- **`6. 기능 명세 ↔ 구현 매핑`에는 spec의 모든 기능 행(5.x ID)을 전수 포함**한다 (1:1 추적성 — 누락 시 검증에서 즉시 드러나도록).
- **버전은 spec과 연동**한다(예: spec v0.1.0 번역 → plan `v0.1.0`). 날짜는 오늘 날짜 자동 기입.
- **맨 첫 줄 슬러그 주석**(`<!-- feature: {slug} -->`)은 입력 spec과 동일하게 유지한다.

## 필수 섹션 (H2 8개, 순서·제목 고정)

1. 한눈에 보기
2. 참고 문서
3. 모듈 구성 & 의존
4. 화면 설계
5. 데이터 설계
6. 기능 명세 ↔ 구현 매핑
7. 미해결 / 외부 의존

이어서 **변경 이력**. 총 8개 H2이며 순서·제목을 변경하지 않는다.

## 출력 템플릿 (이 구조를 그대로 따른다)

```markdown
<!-- feature: {feature-슬러그} -->
# {기능명} — 구현 계획 (plan)

## 1. 한눈에 보기
| 항목 | 내용 |
|---|---|
| 연결 spec | docs/specs/{NNN}-{슬러그}/spec.md (v0.1.0) |
| feature 모듈 | feature/{name}:api, feature/{name}:impl |
| 영향 core 모듈 | core:domain, core:data, … |
| 화면 | N개 ({화면 목록}) |
| 한 줄 요약 | … |

## 2. 참고 문서
- architecture/feature-module.md
- architecture/feature-navigation.md
- architecture/modularization.md
- core/{module}/README.md

## 3. 모듈 구성 & 의존
| 모듈 | 역할 | 구분 |
|---|---|---|
| feature/{name}/api  | XLauncher + EXTRA_*              | new |
| feature/{name}/impl | XActivity·XShell·XNavHost·화면(screen/vm) | new |
| core:domain | 모델·UseCase·Repository 인터페이스 | new/modify |
| core:data   | RepositoryImpl·DataSource·DTO      | new/modify |

> 의존 규칙: impl→자신 api(+상대 api), impl→core:domain(인터페이스), core:data 바인딩은 :app DI

## 4. 화면 설계
### 4.1 {화면명}   (Route: XMain · 패키지: feature/{name}/main)
- **UiState**
  | 필드 | 타입 | 설명 |
  |---|---|---|
- **Intent** (없으면 "없음")
  | Intent | 사용자 액션 | 처리 |
  |---|---|---|
- **SideEffect** (없으면 "없음")
  | Effect | 설명 |
  |---|---|
- **네비게이션**: 진입 인자(args) / 이탈(콜백·Launcher)

## 5. 데이터 설계
| 레이어 | 구성요소 | 모듈 | 출처 |
|---|---|---|---|
| domain | XModel, GetXUseCase, XRepository(interface) | core:domain | - |
| data   | XRepositoryImpl, XRemoteDataSource, XDto      | core:data   | server API / local |

## 6. 기능 명세 ↔ 구현 매핑
| spec ID | interactionType | 구현 매핑 | 화면 |
|---|---|---|---|
| GROUP_ACTION | async_process | Intent.X → GetXUseCase → status(Loading/Success/Error) | main |

## 7. 미해결 / 외부 의존
| ID | 내용 | 의존 | spec 연결 |
|---|---|---|---|
| OPEN-1 | … | 서버 | TBD-2 |

## 변경 이력
| 버전 | 날짜 | 변경 | 근거 |
|---|---|---|---|
| v0.1.0 | {오늘 YYYY-MM-DD} | 최초 작성 | spec v0.1.0 번역 |
```
