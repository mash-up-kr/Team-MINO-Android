---
name: spec-gen
description: Mino-Android SDD spec.md 생성. Figma 화면 + 기획서로부터 "무엇을 만들까"(화면 동작 정의) 기준 문서 spec.md와 화면 PNG(assets/)를 만들고, spec-reviewer 서브에이전트로 Figma·기획서 대조 자가검수까지 수행한다. Figma URL과 기획서가 주어졌을 때 사용.
---

# spec-gen — Mino-Android SDD spec.md 생성

Mino-Android 프로젝트의 SDD(Spec Driven Development) **spec.md**를 생성하는 스킬이다.
spec.md는 "무엇을 만들까"(화면 동작 정의)를 기술하는 **기준 문서**이며, 유일한 원천은 **Figma**다.
입력(Figma + 기획서)을 토대로 아래 출력 템플릿을 **정확히 그대로** 따라 작성한 뒤, **반드시 자가검수 단계까지 수행**한다.

## 입력 (스킬 실행 시 받는다)

- **Figma 화면 URL(들)** — Dev Mode. 복수 가능.
- **기획서** — 텍스트.

> 입력이 비어 있으면 사용자에게 Figma URL과 기획서를 요청한다.

## 작업 순서

1. **Figma Dev Mode MCP로 각 화면을 조회**한다 — 레이아웃·컴포넌트 구조·텍스트·dev resources(주석/측정/상태)를 읽는다.
2. **각 화면 상태를 PNG로 export**해 현재 디렉터리의 `./assets/`에 저장한다.
   - 파일명: `{화면식별자}.png` (예: Figma 노드 ID 기반 `2001-0001.png`). 공백·한글 금지, 영문 소문자/숫자/하이픈만.
3. Figma에서 읽은 사실과 **기획서를 교차 검증**해 화면 동작을 정의한다.
4. 아래 **출력 템플릿 구조 그대로** `./spec.md`를 작성한다 (섹션 순서·제목·표 컬럼 변경 금지).
   - **맨 첫 줄에 슬러그 주석을 넣는다**: `<!-- feature: {feature-슬러그} -->`
     - 슬러그는 kebab-case(영문 소문자/숫자/하이픈만), 기능을 대표하는 유니크한 이름. 기능명·슬러그는 Figma·기획서에서 **자동 판단**한다.
5. `./spec.md`와 `./assets/`를 형제 파일/폴더로 남긴다.
6. **자가검수(필수)**: `spec-reviewer` 서브에이전트를 호출해, 작성한 `./spec.md`를 입력 Figma URL·기획서와 대조해 정확성을 검수한다.
   - 입력으로 ① `./spec.md` 경로 ② 원본 Figma URL ③ 기획서를 전달한다.
   - 검수에서 **치명 결함(사실 오류·누락·통제 어휘 위반·깨진 이미지 링크)** 이 보고되면 spec.md를 수정하고 재검수한다.
   - 최종적으로 검수 통과 요약을 사용자에게 보고한다.

## 작성 규칙

- **사실 기반**: Figma·기획서에 없는 내용을 추측해 채우지 마라. 불확실하거나 결정이 필요한 항목은:
  - 기능 행이면 `확정` 컬럼을 `partial` 또는 `needs_policy`로 표기.
  - 정책 미정이면 `## Open Questions` 표에 행으로 추가하고 `결정 주체`(기획/서버/디자인)를 명시.
- **`2. 화면 상태별 읽기`에는 화면별 실제 Figma 이미지가 반드시 포함**되어야 한다 — `![](assets/파일명.png)` 상대경로로 삽입. 상태(기본/로딩/빈/에러 등)마다 한 행.
- **상세 기능 명세(5.x)** 각 행에도 해당 동작 이미지가 있으면 `assets/` 상대경로로 삽입.
- **통제 어휘 (반드시 이 값만 사용)**:
  - `interactionType`: `display_state` / `user_action` / `navigation` / `async_process` / `validation` / `modal_dialog`
  - `확정`: `confirmed` / `partial` / `needs_policy`
- 기능 행 ID는 `UPPER_SNAKE_CASE`로, 기능 그룹별(5.1, 5.2 …)로 묶는다.
- **변경 이력**: 최초 작성이면 `v0.1.0`. 날짜는 **오늘 날짜(YYYY-MM-DD)를 자동 기입**. 버전명은 이후 PR 브랜치 suffix로 쓰이므로 정확히 기입.

## 산출물 핸드오프

- 산출물은 `./spec.md` + `./assets/*.png`다. 사용자는 이를 **spec-center 대시보드에 drag-drop 업로드**한다 (대시보드의 붙여넣기 구조 검증 → 디자이너 컨펌 게이트로 이어짐). 스킬은 대시보드/Firestore에 직접 쓰지 않는다.

## 출력 템플릿 (이 구조를 그대로 따른다)

```markdown
<!-- feature: {feature-슬러그} -->
# {기능명}

## 1. 한눈에 보기
| 항목 | 내용 |
|---|---|
| 목적 | … |
| 진입점 | … |
| 대상 | … |
| 핵심 규칙 | … |

## 2. 화면 상태별 읽기
| 상태 | 설명 | 이미지 |
|---|---|---|
| 기본 진입 | … | ![](assets/xxxx.png) |
| 로딩 | … | ![](assets/xxxx.png) |
| 빈 상태 | … | ![](assets/xxxx.png) |
| 에러 | … | ![](assets/xxxx.png) |

## 3. 핵심 UX 규칙
| 규칙 | 내용 |
|---|---|
| … | … |

## 4. 사용자 흐름
| 단계 | 동작 | 결과 |
|---|---|---|
| 1 | … | … |

## 5. 상세 기능 명세
### 5.1 {그룹명}
| ID | 기능 | Trigger | 화면 반응 | interactionType | 확정 | 이미지 |
|---|---|---|---|---|---|---|
| GROUP_ACTION | … | … | … | display_state | confirmed | ![](assets/xxxx.png) |

## 비목표
| 제외 항목 | 사유/연결 |
|---|---|
| … | … |

## Open Questions
| ID | 결정 주체 | 질문 |
|---|---|---|
| TBD-1 | 기획 | … |

## 변경 이력
| 버전 | 날짜 | 변경 | 근거 |
|---|---|---|---|
| v0.1.0 | {오늘 YYYY-MM-DD} | 최초 작성 | {Figma 노드 + 화면 텍스트 근거} |
```
