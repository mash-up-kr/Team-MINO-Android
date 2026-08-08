# Pull Request 컨벤션

본문 스켈레톤은 [`.github/PULL_REQUEST_TEMPLATE.md`](../../.github/PULL_REQUEST_TEMPLATE.md). 이 문서는 작성 규칙·섹션 주체·메타 설정만 정의한다.

## 제목 포맷

**이슈 제목 그대로** (prefix·번호 없음). 예: ✅ `로그인 API 연동`, ❌ `feat: 로그인 API 연동`. 이슈 번호는 본문(`Closes #N`)에서 연결.

## base 브랜치

- **기본**: `develop`
- **예외**: `release`/`hotfix` 브랜치는 [`branch-naming.md`](branch-naming.md)의 머지 대상 표 참조
- **워크플로우 하위 작업**: `/pr`이 git 조상 관계로 자동 판단해 상위 브랜치를 base로 쓴다(질문 없음). 판단 기준은 [`.claude/commands/pr.md`](../../.claude/commands/pr.md) 0-4가 단일 출처, 개념은 [`branch-naming.md`의 "base 브랜치"](branch-naming.md#base-브랜치-워크플로우-통합) 참조
- `/pr`은 기본값 `develop`. 다른 base가 더 맞으면 사용자에게 확인 후 진행

## 이슈 연결

- **모든 PR은 `Closes #N`으로 연결.** 머지 시 이슈 자동 닫힘
- 이슈 번호는 브랜치 이름에서 자동 추출 (포맷은 [`branch-naming.md`](branch-naming.md) 참조)
- 여러 이슈: `Closes #12`, `Closes #15` 여러 줄로

## 섹션별 작성 주체

| 섹션 | 작성 주체 | 내용 |
|---|---|---|
| 요약 | **Claude 자동** | 전체 변경 2~3줄 요약 |
| 관련 이슈 | **Claude 자동** | `Closes #N` 삽입 |
| 변경 내용 | **Claude 자동** | 커밋 로그 기반. 필요 시 mermaid 자동 삽입(아래) |
| 주요 스크린샷 | **사용자** | UI 변경 시 최대 3개. 없으면 "해당 없음" |
| 리뷰 포인트 | **사용자** | 꼼꼼히 리뷰받고 싶은 영역 |
| 관련 레퍼런스 자료 | **사용자** | Figma·디자인 시스템·외부 문서 링크 |

"사용자" 섹션은 헤더와 힌트 주석만 유지, Claude가 내용을 채우지 않는다.

## Mermaid 다이어그램

"변경 내용" 섹션 내부 하단에 삽입해 변경의 구조·흐름·상태를 시각화. `/pr`이 diff를 분석해 유용하면 자동 생성한다.

| 타입 | 자동 생성 기준 |
|---|---|
| `flowchart` | 조건 분기 3갈래 이상 / UI 네비게이션 변화 |
| `sequenceDiagram` | API 호출 흐름·비동기 시퀀스 2단계 이상 |
| `classDiagram` | 새 클래스/인터페이스 2개 이상 / 상속·의존 관계 변경 |
| `stateDiagram-v2` | sealed class/enum 상태 머신 신규·변경 |

위 4종만 사용. 단순 버그 수정·리네이밍·포맷팅·문서만 변경에는 삽입하지 않음.

**작성 가이드**: 라벨은 간결하게(한국어 가능), 노드 ≤ 15·엣지 ≤ 20, 한 PR당 최대 2개. 초과 시 PR 쪼개기를 권고.

## draft 정책

`/pr` 실행 시 매번 선택. **draft** = 정리 중·셀프 체크 필요, **ready** = 리뷰 요청 준비 완료.

## Assignee / 레이블 / 리뷰어

- **Assignee**: 현재 `gh` 사용자(`@me`) 자동
- **D-n 라벨**: `/pr`이 PR 생성 직후 선택받아 부착 (아래 정책 참조). 그 외 라벨은 자동 부여 안 함
- **리뷰어**: [`.github/CODEOWNERS`](../../.github/CODEOWNERS) 기반 GitHub 자동 요청. `/pr`은 `--reviewer` 지정 안 함

## D-n 라벨 정책

리뷰 마감까지 남은 일수.

| 라벨 | 의미 | 색상 |
|---|---|---|
| `D-1` | 긴급 / 1일 내 | 빨강 |
| `D-2` | 2일 내 (**기본값**) | 주황 |
| `D-3` | 3일 내 | 노랑 |
| `D-4` | 4일 내 | 연녹 |

`/pr` 실행 시 부착하거나 웹 UI/`gh pr edit`으로 수동 부착. "붙이지 않음"은 카운트다운 제외. [`.github/workflows/d-day-labeler.yml`](../../.github/workflows/d-day-labeler.yml)이 매일 자정(KST) [`naver/d-day-labeler`](https://github.com/naver/d-day-labeler)로 1씩 감소 (Draft 제외).

## 운영 규칙

- **보호 브랜치(`main`/`develop`)**: `/pr`은 경고만 후 진행 (차단 안 함)
- **원격 푸시**: 푸시 안 됐거나 뒤처졌으면 `/pr`이 `git push -u origin <branch>` 자동 실행
- **중복 PR**: 같은 브랜치로 열린 PR 있으면 생성 안 하고 기존 URL만 출력
