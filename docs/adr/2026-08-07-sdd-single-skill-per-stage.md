# SDD 파이프라인은 단계별 단일 스킬로 구성하고 spec-gen·plan-gen 서브에이전트 오케스트레이션을 폐기한다

- **상태**: Accepted
- **작성일**: 2026-08-07
- **작성자**: Jaesung Lee

## 컨텍스트

이슈 #124에서 SDD 파이프라인을 백지에서 다시 설계해 헌법 → PRD → Spec → Plan 네 단계를 각각 `mino-constitution`·`mino-prd`·`mino-spec`·`mino-plan` 스킬과 `mino-sdd/template/`의 템플릿으로 재구성했다.

기존 `spec-gen`·`plan-gen`은 이 중 Spec·Plan과 같은 일을 하고, **같은 경로**(`docs/specs/{name}/spec.md`, `plan.md`)에 **구조가 호환되지 않는** 문서를 쓴다. 구 규약(`spec-format.md`·`plan-format.md`)은 고정 H2 8개 + 슬러그 주석 + 변경 이력 표를 요구하는데 새 템플릿은 다른 섹션 구성을 쓰고, `plan-gen`은 입력 spec의 슬러그 주석과 변경 이력 버전을 파싱하므로 새 spec을 받으면 실패한다. 두 스킬은 `description` 트리거도 겹친다 — `mino-spec`이 "자연어 기능 설명 또는 Figma URL", `spec-gen`이 "Figma URL과 기획서"라 후자가 전자의 부분집합이고, 호출 시점에 고를 근거가 없다.

`plan-gen`의 실행 구조는 [plan-gen 고도화는 에이전트 팀이 아닌 서브에이전트 오케스트레이션으로 한다](2026-07-15-plan-gen-subagent-orchestration.md)에서 결정된 것이다. 그 기록을 읽고 아래 근거로 되돌린다.

## 결정

`spec-gen`·`plan-gen` 스킬과 두 스킬만 참조하던 서브에이전트 6개(`spec-generator`·`spec-reviewer`·`figma-asset-exporter`·`plan-screen-designer`·`plan-data-designer`·`plan-reviewer`)를 제거하고 `.claude/agents/` 디렉터리를 없앤다. SDD 각 단계는 **스킬 1개 + 템플릿 1개**로 구성하고, 계약 기반 팬아웃과 서브에이전트 자가검수 게이트는 두지 않는다.

범위는 Spec·Plan **생성** 단계다. 구 ADR이 남긴 "실제 구현 단계 스킬을 만들 때 에이전트 팀 구조를 재검토한다"는 유보는 그대로 유효하다.

## 근거

- **한 산출물에 규약이 둘일 수 없다.** 두 스킬이 같은 파일 경로에 다른 구조를 쓰는 이상 공존은 선택지가 아니다.
- **2026-07-15 결정의 전제가 사라졌다.** 그 ADR은 "`spec-gen`이 이미 Phase + 계약 + 팬아웃 패턴으로 굳어 있으니 `plan-gen`도 같은 패턴을 쓰면 유지보수 멘탈 모델이 하나로 유지된다"를 근거의 하나로 들었다. #124가 `spec-gen`을 포함해 파이프라인 전체를 교체하면서 이 대칭 근거가 성립하지 않는다.
- **단계가 넷으로 늘었다.** 결정 당시 파이프라인은 spec·plan 둘이었지만 지금은 헌법·PRD·Spec·Plan 넷이다. 단계마다 오케스트레이터 + 서브에이전트 + 포맷 SSOT 3종 세트를 두면 유지 대상이 스킬 4개에서 12개 이상으로 늘어난다.
- 제거 시점에 `docs/specs/`에 커밋된 산출물이 없다. 마이그레이션 부채 없이 끊을 수 있는 유일한 시점이다.

## 결과

- SDD 스킬은 `mino-constitution`·`mino-prd`·`mino-spec`·`mino-plan` 넷만 남고, 세션마다 로드되던 서브에이전트 정의 6개의 컨텍스트 비용이 사라진다.
- **자가검수 게이트가 없어진다.** `spec-reviewer`·`plan-reviewer`가 하던 원본 대조·전수 매핑 검사는 사람 컨펌(원칙 IV)만 남는다.
- **화면 단위 병렬 설계가 없어진다.** 구 `plan-gen`은 네이밍 계약을 먼저 고정한 뒤 화면당 설계자 + 데이터 설계자를 동시 실행했다. `mino-plan`에 남은 팬아웃은 Phase 0 리서치 작업뿐이고 계약도 배리어도 없다.
- **`plan-format.md`가 갖고 있던 이 레포의 MVI 계약이 대응물 없이 사라진다** — `interactionType`→MVI 매핑 표, 아키텍처 규칙 표, 화면 설계(UiState/Intent/SideEffect)·데이터 설계 섹션, spec↔구현 전수 매핑 섹션. 새 `plan-template.md`는 상위 도구의 범용 구조라 이 중 어느 것도 없다. 원문은 이 커밋의 부모 리비전 `.claude/skills/plan-gen/plan-format.md`에 있다.
  단순 이식으로는 안 된다 — 매핑 표의 키인 `interactionType`은 구 `spec-format.md`의 통제 어휘였고, 새 `spec-template.md`는 그 축을 산출하지 않는다. 요구사항 유형 축을 새로 정의해야 한다.
- **`plan-gen`의 읽기 화이트리스트가 없어져** 규약 문서 수집이 Explore 서브에이전트가 아니라 메인 컨텍스트에서 일어난다. `feature-module.md`·`feature-navigation.md`·`modularization.md` 등 40KB대 문서가 요약 없이 그대로 쌓인다.
- **Plan 산출물이 `plan.md` 1개에서 `research.md`·`data-model.md`·`contracts/`·`quickstart.md`를 더한 5개로 늘어난다.** 후속 단계의 읽기 비용이 그만큼 커진다.
- 헌법의 `TODO(SDD_PIPELINE_SOURCE)`는 이 변경으로 닫히지 않는다. `docs/architecture/sdd-pipeline.md` 신설 + `CLAUDE.md` 네비게이션 표 한 줄 + 원칙 IV 출처 MINOR 개정이 후속으로 남는다(Governance상 헌법 개정은 별도 변경으로 한다).
- [Hilt 구현 바인딩은 구현을 소유한 모듈이 갖고, `:app`은 그래프 조립만 한다](2026-08-02-di-binding-ownership.md)가 교정 대상으로 언급한 `plan-format.md`는 더 이상 존재하지 않는다. 해당 ADR 본문은 당시 기록이므로 고치지 않는다.

## 고려한 대안

- **구 스킬을 이름만 바꿔 공존시킨다**: 산출물 경로와 포맷 충돌이 그대로 남고, `description`이 겹쳐 호출 시점의 모호함도 남는다. 기각.
- **새 스킬을 구 오케스트레이션 구조 위에 얹는다**: 팬아웃·자가검수를 유지할 수 있지만 #124가 백지 설계로 시작한 전제와 어긋난다. 기각.
- **`plan-format.md`만 남겨 새 `mino-plan`의 출력 규약으로 쓴다**: 그 문서는 구 plan.md의 H2 8개 구조를 전제로 쓰여 새 템플릿과 섹션이 어긋난다. 기각.
