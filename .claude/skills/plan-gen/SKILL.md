---
name: plan-gen
description: Mino-Android SDD plan.md 생성. 컨펌된 spec.md를 안드로이드 MVI 설계(plan.md)로 번역한다. Mino-Android 레포 체크아웃 안에서 docs·모듈 구조를 직접 읽어 작성하고, plan-reviewer 서브에이전트로 자가검수까지 수행한다. 컨펌된 spec 본문이 주어졌을 때 사용.
---

# plan-gen — Mino-Android SDD plan.md 생성

Mino-Android의 SDD **plan.md**를 생성하는 스킬이다. plan.md는 "어떻게 만들까"로,
**컨펌된 spec.md를 안드로이드 MVI 설계로 번역**한 문서다.
이 작업은 **Mino-Android 레포 체크아웃 안에서** 실행한다 — docs·모듈 구조를 직접 읽어 참조하라.
출력 규약([`plan-format.md`](plan-format.md))을 **정확히 그대로** 따라 작성한 뒤, **반드시 자가검수 단계까지 수행**한다.
실행 구조(오케스트레이터 + 서브에이전트)의 결정 배경은 `docs/adr/2026-07-15-plan-gen-subagent-orchestration.md` 참조.

## 입력 (스킬 실행 시 받는다)

- **컨펌된 spec 본문** — 디자이너 컨펌이 끝난 spec.md.

> spec 본문이 비어 있으면 사용자에게 컨펌된 spec.md를 요청한다.

## 작업 순서

> **산출물 위치**: `{작업 디렉터리}/plan.md` (컨펌된 spec.md가 들어 있는 `docs/specs/{NNN}-{슬러그}/`, spec.md와 형제).
>
> **실행 구조**: 메인이 **Phase 1(번역 계약)** 을 직렬로 끝낸 뒤, **Phase 2** 에서 컨텍스트 수집을 병렬로, **Phase 3** 에서 설계 생성을 조건부 팬아웃으로 실행한다. 갈래 간 유일한 공유 지점은 *네이밍*(화면 설계가 쓰는 UseCase 이름 = 데이터 설계가 정의하는 UseCase 이름)이며, 이를 Phase 1의 **번역 계약**으로 고정해 갈래들이 서로 대기하지 않게 한다.

### Phase 1 — 번역 계약 확정 (메인, 직렬·1회)

1. **입력 확인**: 컨펌된 spec 본문. 비어 있으면 사용자에게 요청한다.
2. **슬러그·작업 디렉터리·spec 버전 확정**: spec 첫 줄 `<!-- feature: {슬러그} -->`에서 슬러그를 읽어 `docs/specs/{NNN}-{슬러그}/`를 찾는다(없으면 사용자에게 위치 확인). spec `변경 이력` 최신 행에서 버전을 파싱한다.
3. **기능 행 인벤토리 작성**: spec 5.x의 **모든** 기능 행을 1:1로 옮긴 표. 이 표가 섹션 6 기계 생성과 결정적 체크 C1의 기준 집합이다.

   | spec ID | interactionType | 확정 | 담당 화면 | 데이터 관여 | 구현 매핑 요약 |
   |---|---|---|---|---|---|

   - `담당 화면` — Phase 3 팬아웃 시 각 `plan-screen-designer`의 행 배정 키.
   - `데이터 관여` — 관련 UseCase 이름. `plan-data-designer`에 넘길 부분집합 필터.
   - `확정` — spec 값을 이월해 `needs_policy`/`partial` 행의 섹션 7 이월을 계약 단계에서 고정.
4. **네이밍 계약 확정**: 모든 갈래가 공유할 이름을 표 2개로 사전 확정한다. **서브에이전트는 여기에 없는 이름을 만들 수 없다.**

   | 화면 | Route | 패키지 | UiState | Intent | SideEffect |
   |---|---|---|---|---|---|

   | 구성요소 | 이름 | 레이어 | 모듈 | 구분 |
   |---|---|---|---|---|

   - 구성요소: 모델 / UseCase / Repository 인터페이스 / RepositoryImpl / DataSource·DTO.
   - `구분`(new/modify)은 이 시점엔 미정 — Phase 2 코드 인벤토리로 확정한다.
5. **팬아웃 분기 결정**: 화면 수 **N ≥ 3이면 Phase 3-A(팬아웃), N < 3이면 Phase 3-B(메인 직접 작성)**. 분기는 계약 확정 시점인 여기서 결정한다 — Phase 2 수집 범위 지시가 흔들리지 않게.

### Phase 2 — 컨텍스트 수집 (내장 Explore 에이전트 2개, 병렬)

메인이 `Agent` 툴 **2개를 한 메시지에서 동시 호출**한다(`subagent_type: Explore` — 읽기 전용 수집이므로 커스텀 에이전트 정의 없이 내장 Explore를 쓴다, spec-gen과의 의도적 차이). 주입 프롬프트에 다음을 반드시 포함한다:

- **Explore A (docs 컨벤션 수집)**:
  - 읽기 화이트리스트: `docs/architecture/feature-module.md`·`feature-navigation.md`·`modularization.md` + spec이 건드리는 **`core/{module}/README.md`만** (메인이 Phase 1에서 모듈 목록을 정해 주입). 필요 시 `core/common/android`의 MVI 베이스(`UiState`/`Intent`/`SideEffect`/`MviContainer`) 확인 포함.
  - 무시 목록: `docs/diagrams/`, `docs/operations/`, 다른 feature 내부.
  - 반환 형식: designer 주입용 컨벤션 요약 bullet + 참고한 파일 경로 목록(섹션 2 작성 재료).
- **Explore B (코드 인벤토리)**:
  - 네이밍 계약의 이름 목록 자체를 주입하고 **각 이름의 레포 실존 여부만** 조사한다(계약 밖 탐색으로 범위가 표류하지 않게 폐쇄).
  - 반환 형식: `| 이름 | 존재 여부 | 경로 | new/modify 판정 |` 표.

**배리어**: 두 결과를 받은 뒤 네이밍 계약의 `구분` 컬럼을 확정한다. 수집 결과 계약에 없는 이름이 필요하다고 드러나면 **메인이 계약을 갱신**한 뒤 진행한다(피어 협상 없음 — 서브에이전트 오케스트레이션 ADR의 예외 처리 원칙).

### Phase 3 — 설계 생성 (조건부 팬아웃 + 메인 조립)

#### 3-A. 팬아웃 (화면 ≥ 3)

[`plan-screen-designer`](../../agents/plan/plan-screen-designer.md) **× N**(섹션 4.x 화면당 1개) + [`plan-data-designer`](../../agents/plan/plan-data-designer.md) **1개**를 한 메시지에서 동시 호출한다. Phase 1 네이밍 계약 덕에 서로 대기하지 않는다. 각 에이전트에 주입할 계약 항목은 각 정의의 `## 입력` 참조.

> 각 에이전트는 파일을 쓰지 않고 **섹션 마크다운 블록을 텍스트로 반환**한다 — plan.md는 단일 파일인데 작성자가 N+1개라, Write 지점을 메인 1곳으로 고정해 조립 충돌·순서 비결정성을 막는다(spec-generator가 파일을 직접 쓰는 것과의 의도적 차이).

#### 3-B. 메인 직접 작성 (화면 < 3)

팬아웃 오버헤드가 이득이 없으므로 메인이 [`plan-format.md`](plan-format.md)를 따라 섹션 4·5를 직접 작성한다.

#### 조립 (메인, 공통)

- 섹션 1·2·3·7: 계약 + Phase 2 수집 결과로 메인이 직접 작성 (섹션 3의 new/modify는 확정된 계약·designer 반환 재사용).
- 섹션 4: 화면 designer 반환 블록을 계약의 화면 순서대로 결합.
- 섹션 5: data designer 반환 블록.
- 섹션 6: **기능 행 인벤토리에서 기계 생성** (행 = 인벤토리 행 1:1).
- 변경 이력: spec 버전 연동 + 오늘 날짜.
- → `{작업 디렉터리}/plan.md`를 **한 번에 Write**한다.

### Phase 4 — 이중 검증 (메인 결정적 체크 + plan-reviewer, 필수)

> **수정 예산**: 수정→재검증 사이클은 4-1·4-2 **합산 최대 2회** (카운트 단위 = 수정 후 재검증 1회, 결함 종류가 바뀌어도 리셋하지 않는다). 4-2의 수정이 본문을 바꾸면 같은 사이클 안에서 C1~C5를 재확인한다(새 카운트 아님). 결함이 해소되면 남은 횟수와 무관하게 즉시 진행하고, 예산을 초과하면 남은 치명 결함을 사용자에게 보고하고 중단한다 — 수정으로 해소되지 않는 결함은 대개 spec 쪽 문제라 사용자 판단이 필요하다.

**4-1. 메인 결정적 체크** — 집합 비교·문자열 매칭으로 기계 검증한다:

- **C1 (전수 매핑)**: spec 5.x ID 집합 vs plan 섹션 6 `spec ID` 컬럼 집합 — **양방향 차집합 = 0** (spec에만 있으면 누락, plan에만 있으면 유령 — 둘 다 결함).
- **C2 (슬러그)**: plan 첫 줄 `<!-- feature: {slug} -->` = spec 첫 줄과 문자열 동일.
- **C3 (버전 연동)**: plan `변경 이력` 최신 행 버전 = Phase 1에서 파싱한 spec 버전, 날짜 = 오늘(YYYY-MM-DD).
- **C4 (필수 섹션)**: H2 8개가 `plan-format.md`의 `필수 섹션`과 순서·제목 일치.
- **C5 (계약-본문 정합)**: 섹션 4가 참조하는 UseCase·모델 이름 집합 ⊆ 섹션 5 정의 집합 ⊆ 네이밍 계약 (designer 반환의 "참조 이름 목록"으로 대조).

실패 처리: C1·C5 → 해당 화면/데이터 블록만 수정(3-A였다면 해당 designer만 재디스패치), C2~C4 → 메인이 직접 수정. 수정 후 C1~C5 재실행(수정 예산 소모).

**4-2. plan-reviewer 자가검수** — [`plan-reviewer`](../../agents/plan/plan-reviewer.md) 서브에이전트를 호출한다.

- 입력: ① `{작업 디렉터리}/plan.md` 경로 ② spec.md 경로 ③ 기능 행 인벤토리 ④ 네이밍 계약.
- **치명 결함(매핑·아키텍처 규칙 위반, 추측 삽입, TBD/needs_policy 이월 누락, 전수 매핑 누락)** 이 보고되면 plan.md를 수정하고 재검수한다(수정 예산 소모).

### Phase 5 — 핸드오프 (메인)

검수 통과 요약을 사용자에게 보고한다. 산출물 구성은 `## 산출물 핸드오프` 참조.

## 작성 규약

plan.md의 **출력 템플릿·통제 어휘·interactionType→MVI 매핑·아키텍처 규칙·작성 규칙**은 [`plan-format.md`](plan-format.md)를 **단일 출처**로 한다. `plan-screen-designer`·`plan-data-designer`(작성)·`plan-reviewer`(검수)도 이 문서를 따른다. 규칙을 여기서 다시 풀어쓰지 않는다.

## 산출물 핸드오프

- 산출물은 `{작업 디렉터리}/plan.md`다 (컨펌된 spec.md와 형제, 예: `docs/specs/001-mypage/plan.md`). 로컬 산출용일 뿐 레포에 직접 커밋하지 않는다 — 사용자가 **spec-center 대시보드에 업로드**하고 (spec_approved 이후), PR 단계에서 커밋된다. plan은 별도 컨펌 게이트 없이 PR 리뷰(얼라인)에서 검증된다.
