# Architecture Decision Records (ADR)

설계 과정에서 내린 **중요한 결정과 그 이유**를 기록하는 문서 모음이다. 코드만 보면 "무엇을" 했는지는 알 수 있지만 "왜" 그렇게 했는지는 사라진다. ADR은 그 맥락을 남겨, 나중에 합류한 사람(과 에이전트)이 같은 고민을 반복하거나 이미 폐기된 방식으로 되돌리는 것을 막는다.

## 언제 ADR이 남는가

- 라이브러리·프레임워크 선택 또는 교체
- 모듈 경계·레이어 구조에 영향을 주는 결정
- 되돌리기 어렵거나, 나중에 "왜 이렇게 했지?"라는 의문이 생길 만한 결정
- 의도적으로 **하지 않기로** 한 것 (예: 특정 우회책을 금지)

자명하거나 쉽게 되돌릴 수 있는 결정은 남기지 않는다. 구체적인 판단 기준(트리거·안티-트리거)은 [`adr-writer` 스킬](../../.claude/skills/adr-writer/SKILL.md)의 입력 섹션이 단일 출처다.

## 작성 규칙

ADR은 [`adr-writer` 스킬](../../.claude/skills/adr-writer/SKILL.md)이 자동으로 작성하고 아래 인덱스까지 갱신한다. 문서 형식(파일명·상태 값·템플릿)은 [`adr-format.md`](../../.claude/skills/adr-writer/adr-format.md)를 단일 출처로 따른다.

## 인덱스

| # | 제목 | 상태 | 작성일 | 작성자 |
|---|---|---|---|---|
| [0001](0001-serialization-optin-ide-warning.md) | 직렬화 DTO의 IDE opt-in 경고에 `@OptIn` 우회를 쓰지 않는다 | Accepted | 2026-06-29 | full_avocado |
| [0002](0002-plan-gen-subagent-orchestration.md) | plan-gen 고도화는 에이전트 팀이 아닌 서브에이전트 오케스트레이션으로 한다 | Accepted | 2026-07-15 | Jaesung Lee |
