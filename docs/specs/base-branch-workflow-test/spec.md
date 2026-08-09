<!-- feature: base-branch-workflow-test -->
# base 브랜치 워크플로우 테스트 스펙

이슈 #130에서 구현한 base 브랜치 자동판단(git 조상 관계 기반)이 실제 spec 하위 작업 PR 흐름에서 동작하는지 검증하기 위한 더미 spec 문서. 실제 화면·기능 정의는 아니다.

## 검증 항목
- `feature/130-base-branch-workflow`에서 분기한 이 브랜치가 `/pr` 실행 시 `develop`이 아니라 `feature/130-base-branch-workflow`를 base로 자동 타겟하는지 확인한다.
