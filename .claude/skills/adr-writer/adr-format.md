# ADR 출력 포맷 (SSOT)

ADR 문서의 **파일명·통제 어휘·템플릿** 형식을 정의하는 단일 출처다. ADR 작성은 [`adr-writer` 스킬](SKILL.md)이 전담하며 이 형식을 따른다(작성 시점 판단·항목 추출·인덱스 갱신 같은 정책은 그쪽에 있다). ADR을 읽는 사람에게는 상태 값의 의미 참조처다. 규칙을 다른 곳에서 다시 풀어쓰지 않고 이 파일을 참조한다.

## 파일명

- `NNNN-kebab-제목.md` (4자리 일련번호, 예: `0001-serialization-optin-ide-warning.md`)
- 번호는 **한 번 부여하면 재사용하지 않는다**. 결정이 뒤집히면 새 번호로 ADR을 쓰고, 기존 ADR은 본문은 그대로 두고 상태만 `Superseded by NNNN`으로 바꾼다

## 통제 어휘 — 상태 값

| 상태 | 의미 |
|---|---|
| `Proposed` | 제안됨, 합의 전 |
| `Accepted` | 채택됨, 현재 유효 |
| `Superseded` | 다른 ADR로 대체됨 (대체 ADR 번호 명시) |
| `Deprecated` | 더 이상 유효하지 않음 |

## 문서 템플릿

언어는 한국어. 아래 헤더·순서를 그대로 따르되, 서술 내용은 매 사례마다 다르므로 예시 문장은 넣지 않는다.

```markdown
# NNNN. {제목}

- **상태**: Proposed | Accepted | Superseded by NNNN | Deprecated
- **작성일**: YYYY-MM-DD
- **작성자**:

## 컨텍스트

## 결정

## 근거

## 결과

## 고려한 대안
```
