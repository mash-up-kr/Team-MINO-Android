---
name: spec-gen
description: Mino-Android SDD spec.md 생성. Figma 화면 + 기획서로부터 "무엇을 만들까"(화면 동작 정의) 기준 문서 spec.md와 화면 PNG(assets/)를 만들고, spec-reviewer 서브에이전트로 Figma·기획서 대조 자가검수까지 수행한다. Figma URL과 기획서가 주어졌을 때 사용.
---

# spec-gen — Mino-Android SDD spec.md 생성

Mino-Android 프로젝트의 SDD(Spec Driven Development) **spec.md**를 생성하는 스킬이다.
spec.md는 "무엇을 만들까"(화면 동작 정의)를 기술하는 **기준 문서**이며, 유일한 원천은 **Figma**다.
입력(Figma + 기획서)을 토대로 출력 규약([`spec-format.md`](spec-format.md))을 **정확히 그대로** 따라 작성한 뒤, **반드시 자가검수 단계까지 수행**한다.

## 입력 (스킬 실행 시 받는다)

- **Figma 화면 URL(들)** — Dev Mode. 복수 가능.
- **기획서** — 텍스트.

> 입력이 비어 있으면 사용자에게 Figma URL과 기획서를 요청한다.

## 작업 순서

> **산출물 위치**: 모든 산출물은 `docs/specs/{NNN}-{슬러그}/` 아래에 둔다. 이하 이 디렉터리를 `{작업 디렉터리}`라 부른다.
>
> **실행 구조**: 메인이 **Phase 1(공통 선행)** 을 직렬로 끝낸 뒤, **Phase 2** 에서 spec 작성과 에셋 추출을 **서브에이전트 2개로 병렬** 실행한다(`Agent` 툴 동시 호출). 두 갈래의 유일한 공통 의존은 *Figma 읽기 + 파일명 규칙* 이며, 이를 Phase 1의 **노드→파일명 매핑표(계약)** 로 고정해 두 갈래가 서로 대기하지 않게 한다.

### Phase 1 — 공통 선행 (메인, 직렬·1회)

1. **입력 확인**: Figma URL(들) + 기획서. 비어 있으면 사용자에게 요청한다.
2. **Figma Dev Mode MCP로 구조 1회 조회** — 레이아웃·컴포넌트·텍스트·dev resources(주석/측정/상태)를 읽어 화면 상태(기본/로딩/빈/에러/모달 등)를 식별한다. 중복 읽기를 막기 위해 여기서 한 번에 파악한다.
3. **슬러그 + 작업 디렉터리 결정**:
   - 슬러그는 kebab-case(영문 소문자/숫자/하이픈만), 기능을 대표하는 유니크한 이름. Figma·기획서에서 **자동 판단**한다.
   - `docs/specs/` 하위 기존 `NNN-*` 디렉터리 중 최대 번호 + 1을 3자리 zero-pad(`001`, `002` …)로 매긴다. 없으면 `001`.
   - `{작업 디렉터리} = docs/specs/{NNN}-{슬러그}/` (예: `docs/specs/001-mypage/`). 이 디렉터리와 그 하위 `assets/`를 만든다.
4. **노드→파일명 매핑표 확정 (계약)**: 식별한 각 화면 상태에 export 파일명을 1:1로 배정한다. 파일명은 `{화면식별자}.png`(예: 노드 ID 기반 `2001-0001.png`), 공백·한글 금지·영문 소문자/숫자/하이픈만. **이 표가 Phase 2 두 갈래의 단일 계약**이다.
5. **사실 요약본 작성**: Figma 텍스트·구조·상태별 동작 근거와 **기획서 교차검증** 결과를 정리해, Phase 2의 spec 작성 에이전트에 넘길 컨텍스트로 만든다.

### Phase 2 — 팬아웃 (서브에이전트 2개, 병렬)

메인이 `Agent` 툴 **2개를 한 메시지에서 동시 호출**한다(`subagent_type`으로 아래 커스텀 에이전트 지정). 둘 다 Phase 1의 매핑표를 입력으로 받으며, 서로의 산출물을 기다리지 않는다.

- **`spec-generator`** — `{작업 디렉터리}` · 매핑표 · 사실 요약본 · 슬러그 · 원본 Figma URL을 주입한다. 에이전트는 출력 규약([`spec-format.md`](spec-format.md))대로 `{작업 디렉터리}/spec.md`를 작성하고, 이미지 링크는 매핑표 파일명으로 경로만 박는다(PNG 실물 불필요).
- **`figma-asset-exporter`** — `{작업 디렉터리}` · 매핑표 · Figma fileKey/URL을 주입한다. 에이전트는 각 노드를 PNG export해 `{작업 디렉터리}/assets/{파일명}`에 저장하고, 접근 권한이 없으면 수동 export 안내로 폴백한다.

> 두 에이전트의 역할·입출력 상세는 각 정의([`spec-generator`](../../agents/spec-generator.md) · [`figma-asset-exporter`](../../agents/figma-asset-exporter.md)) 참조.

### Phase 3 — 배리어 + 정합성 체크 (메인)

두 에이전트가 모두 끝나면, `spec.md`가 참조하는 파일명 집합과 `{작업 디렉터리}/assets/`의 실제 파일 집합이 일치하는지(깨진 링크 0) 검증한다. 누락이 있으면 해당 노드만 `figma-asset-exporter`를 재시도하거나 수동 export 항목으로 표기한다(spec 본문은 경로가 이미 박혀 있어 영향 없음).

### Phase 4 — 자가검수 (메인, 필수)

`spec-reviewer` 서브에이전트를 호출해 `{작업 디렉터리}/spec.md`를 입력 Figma URL·기획서와 대조 검수한다.
- 입력: ① `{작업 디렉터리}/spec.md` 경로 ② 원본 Figma URL ③ 기획서.
- **치명 결함(사실 오류·누락·통제 어휘 위반·깨진 이미지 링크)** 이 보고되면 spec.md를 수정하고 재검수한다.

### Phase 5 — 핸드오프 (메인)

검수 통과 요약을 사용자에게 보고한다. 산출물 구성은 `## 산출물 핸드오프` 참조.

> **결정성 옵션**: 기본은 위 `Agent` 팬아웃이다. 더 엄격한 결정적 실행이 필요하면 Phase 2를 `Workflow`의 `parallel([A, B])` 배리어로 대체할 수 있다(사용자 opt-in 필요).

## 작성 규약

spec.md의 **출력 템플릿·통제 어휘·작성 규칙**은 [`spec-format.md`](spec-format.md)를 **단일 출처**로 한다. `spec-generator`(작성)·`spec-reviewer`(검수)도 이 문서를 따른다. 규칙을 여기서 다시 풀어쓰지 않는다.

## 산출물 핸드오프

- 산출물은 `{작업 디렉터리}/spec.md` + `{작업 디렉터리}/assets/*.png`다 (예: `docs/specs/001-mypage/`). 사용자는 이를 **spec-center 대시보드에 drag-drop 업로드**한다 (대시보드의 붙여넣기 구조 검증 → 디자이너 컨펌 게이트로 이어짐). 스킬은 대시보드/Firestore에 직접 쓰지 않는다.
