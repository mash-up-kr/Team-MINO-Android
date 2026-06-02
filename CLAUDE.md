# MinoAndroid

Android 프로젝트. Git Flow 브랜치 전략 사용.

## 문서 네비게이션

상세 규칙은 이 파일에 두지 않는다. 필요한 상황에 맞춰 아래 문서를 먼저 Read해 최신 규칙을 확인한다.

| 상황 | 참조할 문서 |
|---|---|
| Git 브랜치 생성, 이름 짓기, Git Flow 관련 작업 | `docs/conventions/branch-naming.md` |
| 커밋 메시지 작성, 커밋 쪼개기 | `docs/conventions/commit-message.md` |
| Pull Request 생성, 본문 작성, 제목·연결 키워드 | `docs/conventions/pull-request.md` + `.github/PULL_REQUEST_TEMPLATE.md` |
| Compose Lint 룰·severity 조정, 위반 처리 | `docs/conventions/compose-lint.md` + `lint.xml` |
| 디자인 시스템 사용·토큰 추가, `core:design-system` 모듈 작업 | `core/design-system/README.md` |
| CD 배포, Play Store 자동화, 시크릿·트리거 | `docs/cd-pipeline.md` |

> 새로운 규약이 추가되면 `docs/conventions/` 하위에 파일을 만들고 위 표에 줄을 추가할 것.
