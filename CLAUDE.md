# MinoAndroid

Android 프로젝트. Git Flow 브랜치 전략 사용.

## 문서 네비게이션

상세 규칙은 이 파일에 두지 않는다. 필요한 상황에 맞춰 아래 문서를 먼저 Read해 최신 규칙을 확인한다.

| 상황 | 참조할 문서 |
|---|---|
| Git 브랜치 생성, 이름 짓기, Git Flow 관련 작업 | `docs/conventions/branch-naming.md` |
| Git Worktree 병렬 작업 환경, 워크트리 생성/정리, `claude -w` 사용 | `docs/conventions/worktree.md` |
| 커밋 메시지 작성, 커밋 쪼개기 | `docs/conventions/commit-message.md` |
| Pull Request 생성, 본문 작성, 제목·연결 키워드 | `docs/conventions/pull-request.md` + `.github/PULL_REQUEST_TEMPLATE.md` |
| Compose Lint 룰·severity 조정, 위반 처리 | `docs/conventions/compose-lint.md` + `lint.xml` |
| 모듈 구성·의존성 규칙, 레이어 경계, 새 모듈 추가 절차 | `docs/architecture/modularization.md` |
| Domain Repository 인터페이스, UseCase 사용 기준, ViewModel 책임 분리 | `core/domain/README.md` |
| feature 모듈 추가, api/impl 구조, 패키지·역할, Route↔Screen 작성 | `docs/architecture/feature-module.md` |
| feature 화면 전환 — feature 간 Activity(Launcher), feature 내부 Route(NavHost·인자 전달) | `docs/architecture/feature-navigation.md` |
| 디자인 시스템 사용·토큰 추가, `core:design-system` 모듈 작업 | `core/design-system/README.md` |
| `core:common` 공용 기반 모듈 사용·확장 (Kotlin 유틸·MVI·공통 UI) | `core/common/kotlin/README.md` · `core/common/android/README.md` · `core/common/ui/README.md` |
| `core:navigation` 화면 전환 인프라 API 사용·확장 (Activity Launcher / type-safe Route) | `core/navigation/README.md` |
| `core:map` 지도 모듈 사용·확장 (`MinoMap`·`GeoPoint` 좌표 변환·폴리곤) | `core/map/README.md` |
| CD 배포, Play Store 자동화, 시크릿·트리거 | `docs/operations/cd-pipeline.md` |
| `core:data` 데이터 레이어 작업 (DataSource·Repository·Mapper·네트워크 추가) | `core/data/README.md` |
| 과거 설계 결정의 배경·이유 확인, 새 설계 결정 기록 (ADR) | `docs/adr/README.md` + `docs/adr/*.md` |
| 머지 후 잘못된 것으로 판명된 결정·구현 확인, 새 실패 기록 작성 | `docs/failures/README.md` + `docs/failures/*.md` |

> 새 문서가 추가되면 성격에 맞는 `docs/` 하위 디렉터리(`conventions/` 규약, `architecture/` 구조, `operations/` 배포, `adr/` 결정 기록, `failures/` 실패 기록)에 파일을 만들고 위 표에 줄을 추가할 것.
