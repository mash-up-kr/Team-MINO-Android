---
name: plan-gen
description: Mino-Android SDD plan.md 생성. 컨펌된 spec.md를 안드로이드 MVI 설계(plan.md)로 번역한다. Mino-Android 레포 체크아웃 안에서 docs·모듈 구조를 직접 읽어 작성한다. 컨펌된 spec 본문이 주어졌을 때 사용.
---

# plan-gen — Mino-Android SDD plan.md 생성

Mino-Android의 SDD **plan.md**를 생성하는 스킬이다. plan.md는 "어떻게 만들까"로,
**컨펌된 spec.md를 안드로이드 MVI 설계로 번역**한 문서다.
이 작업은 **Mino-Android 레포 체크아웃 안에서** 실행한다 — docs·모듈 구조를 직접 읽어 참조하라.

## 입력 (스킬 실행 시 받는다)

- **컨펌된 spec 본문** — 디자이너 컨펌이 끝난 spec.md.

> spec 본문이 비어 있으면 사용자에게 컨펌된 spec.md를 요청한다.

## 작업 순서

1. spec을 읽고 다룰 기능·화면·데이터를 파악한다. spec 첫 줄 `<!-- feature: {슬러그} -->`에서 슬러그를 읽어 **작업 디렉터리** `docs/specs/{NNN}-{슬러그}/`(컨펌된 spec.md가 들어 있는 그 디렉터리)를 찾는다. 같은 슬러그의 디렉터리가 없으면 사용자에게 위치를 확인한다.
2. **레포 docs를 직접 읽어 컨벤션을 확인**한다:
   - **항상 참조**: `docs/architecture/feature-module.md`, `feature-navigation.md`, `modularization.md`
   - **선별 참조**: spec이 건드리는 관련 **`core/{module}/README.md`만**. 다른 feature 내부는 참조하지 않는다.
   - **무시**: `docs/diagrams/`, `docs/operations/` (CD 파이프라인)
   - 필요 시 `core/common/android`의 MVI 베이스(`UiState`/`Intent`/`SideEffect`/`MviContainer`)를 확인.
3. spec의 각 기능 행을 `interactionType` 기준으로 MVI에 번역한다(아래 매핑 강제).
4. 출력 템플릿 그대로 `{작업 디렉터리}/plan.md`를 작성한다(1번에서 찾은 디렉터리, 컨펌된 spec.md와 형제). **참고한 docs를 `## 2. 참고 문서`에 빠짐없이 명시**한다. (로컬 산출용일 뿐, 레포에 직접 커밋하지 않는다 — 대시보드 업로드 후 PR 단계에서 커밋된다.)

## interactionType → MVI 매핑 (강제)

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
- 통제 어휘: 모듈 구분 `new`/`modify`, 데이터 레이어 `domain`/`data`.
- **`6. 기능 명세 ↔ 구현 매핑`에는 spec의 모든 기능 행(5.x ID)을 전수 포함**한다 (1:1 추적성 — 누락 시 검증에서 즉시 드러나도록).
- **버전은 spec과 연동**한다(예: spec v0.1.0 번역 → plan `v0.1.0`). 날짜는 오늘 날짜 자동 기입.
- **맨 첫 줄 슬러그 주석**(`<!-- feature: {slug} -->`)은 입력 spec과 동일하게 유지한다.

## 산출물 핸드오프

- 산출물은 `{작업 디렉터리}/plan.md`다 (컨펌된 spec.md와 형제, 예: `docs/specs/001-mypage/plan.md`). 사용자는 이를 **spec-center 대시보드에 업로드**한다 (spec_approved 이후). plan은 별도 컨펌 게이트 없이 PR 리뷰(얼라인)에서 검증된다.

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
| feature/{name}/impl | XActivity·XNavHost·화면(screen/vm) | new |
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
