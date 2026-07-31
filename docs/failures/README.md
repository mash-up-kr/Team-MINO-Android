# Failure Records (실패 기록)

머지된 이후 잘못된 결정·구현으로 판명되어 되돌리거나 다시 고친 사례를 기록하는 문서 모음이다. [ADR](../adr/README.md)이 "왜 이렇게 하기로 했는지"를 남긴다면, 실패 기록은 "왜 그 결정이 틀렸는지, 무엇으로 대체했는지"를 남긴다. 목적은 같은 실패를 반복하며 재조사·재논의에 대화 토큰을 다시 쓰는 것을 막는 것이다.

## 언제 실패 기록이 남는가

- 머지된 코드가 이후(대개 다른 세션에서) 잘못된 설계·구현으로 판명되어 되돌리거나 크게 고친 경우
- 기존 [ADR](../adr/README.md)의 결정이 실무에서 틀렸다고 판명된 경우 — 기존 ADR의 상태도 함께 갱신된다
- 시도했지만 채택하지 않기로 한 접근 (같은 시도를 다시 반복하지 않기 위해)

사소한 오타·단순 버그 수정처럼 "왜 틀렸는지"를 남길 필요가 없는 것은 기록하지 않는다. 구체적인 판단 기준(트리거·안티-트리거)은 [`failure-writer` 스킬](../../.claude/skills/failure-writer/SKILL.md)의 입력 섹션이 단일 출처다.

## 작성 규칙

실패 기록은 [`failure-writer` 스킬](../../.claude/skills/failure-writer/SKILL.md)이 자동으로 작성하고 아래 인덱스까지 갱신한다. 문서 형식(파일명·상태 값·템플릿)은 [`failure-format.md`](../../.claude/skills/failure-writer/failure-format.md)를 단일 출처로 따른다.

## 인덱스

| 발생일자 | 제목 | 관련 ADR | 상태 | 작성자 |
|---|---|---|---|---|
| 2026-07-30 | [feature를 `api`/`impl` 두 모듈로 분리한 규약이 탭 feature에서 성립하지 않았다](2026-07-30-feature-api-impl-split.md) | [단일 feature 모듈](../adr/2026-07-30-single-feature-module.md) | Resolved | Jaesung Lee |
