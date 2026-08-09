# 구현 계획: [FEATURE NAME]

**대상 스펙 경로**: `{SPEC_DIR}`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: [1.0.0]

**최초 작성일**: [날짜]

**최종 수정일**: [날짜]

**버전**: [1.0.0]

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

## 요약 (Summary)

[기능 명세서에서 추출: 핵심 요구사항 + 리서치에서 도출한 기술적 접근 방식]

## 기술 컨텍스트 (Technical Context)

<!--
  작업 필요: 이 섹션의 내용을 프로젝트의 기술 세부사항으로 교체하세요.
  여기 제시된 구조는 반복 과정을 안내하기 위한 참고 사항입니다.
-->

**언어/버전**: [예: Kotlin 2.0.0, Swift 5.9 또는 NEEDS CLARIFICATION]

**주요 의존성**: [예: FastAPI, UIKit, LLVM 또는 NEEDS CLARIFICATION]

**저장소**: [해당되는 경우, 예: PostgreSQL, CoreData, 파일 또는 N/A]

**테스트**: [예: pytest, XCTest, cargo test 또는 NEEDS CLARIFICATION]

**대상 플랫폼**: [예: Linux 서버, iOS 15+, WASM 또는 NEEDS CLARIFICATION]

**프로젝트 유형**: [예: library/cli/web-service/mobile-app/compiler/desktop-app 또는 NEEDS CLARIFICATION]

**성능 목표**: [도메인별로 작성, 예: 1000 req/s, 10k lines/sec, 60 fps 또는 NEEDS CLARIFICATION]

**제약 조건**: [도메인별로 작성, 예: p95 200ms 미만, 메모리 100MB 미만, 오프라인 지원 또는 NEEDS CLARIFICATION]

**규모/범위**: [도메인별로 작성, 예: 사용자 1만 명, 100만 LOC, 화면 50개 또는 NEEDS CLARIFICATION]

## 헌법 준수 확인 게이트 (Constitution Check)

*게이트: Phase 0 리서치 전에 반드시 통과해야 한다. Phase 1 설계 후 재확인한다.*

[헌법(constitution) 파일을 기준으로 결정된 게이트]

## 프로젝트 구조 (Project Structure)

### 문서 (이번 Feature)

```text
docs/specs/{feature-name}/
├── plan.md              # 이 파일 (/mino-plan 산출물)
├── research.md          # Phase 0 산출물 (/mino-plan)
├── data-model.md        # Phase 1 산출물 (/mino-plan)
├── quickstart.md        # Phase 1 산출물 (/mino-plan)
├── contracts/           # Phase 1 산출물 (/mino-plan)
└── tasks.md             # /mino-task 산출물 (/mino-plan 이 생성하지 않음)
```

### 소스 코드 (Repository Root 기준)
<!--
  작업 필요: 아래 자리표시자 트리를 이 기능에 맞는 구체적인 레이아웃으로
  교체하세요. 사용하지 않는 옵션은 삭제하고, 선택한 구조를 실제 경로
  (예: apps/admin, packages/something)로 확장하세요. 최종 계획에는
  Option 라벨이 남아 있으면 안 됩니다.
-->

```text
# [사용하지 않으면 삭제] Option 1: 단일 프로젝트 (기본값)
src/
├── models/
├── services/
├── cli/
└── lib/

tests/
├── contract/
├── integration/
└── unit/

# [사용하지 않으면 삭제] Option 2: 웹 애플리케이션 ("frontend" + "backend" 가 감지된 경우)
backend/
├── src/
│   ├── models/
│   ├── services/
│   └── api/
└── tests/

frontend/
├── src/
│   ├── components/
│   ├── pages/
│   └── services/
└── tests/

# [사용하지 않으면 삭제] Option 3: 모바일 + API ("iOS/Android" 가 감지된 경우)
api/
└── [위 backend 와 동일]

ios/ 또는 android/
└── [플랫폼별 구조: feature 모듈, UI 플로우, 플랫폼 테스트]
```

**구조 결정**: [선택한 구조를 기록하고 위에 정리한 실제 디렉터리를 참조할 것]

## 복잡도 추적 (Complexity Tracking)

> **헌장 준수 확인에서 정당화가 필요한 위반이 있는 경우에만 작성**

| 위반 사항 | 필요한 이유 | 더 단순한 대안을 기각한 이유 |
|-----------|-------------|------------------------------|
| [예: 4번째 프로젝트] | [현재 필요성] | [3개 프로젝트로 부족한 이유] |
| [예: Repository 패턴] | [구체적인 문제] | [DB 직접 접근으로 부족한 이유] |
