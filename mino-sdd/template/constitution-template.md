# [PROJECT_NAME] 헌법(Constitution)

| 항목 | 내용 |
|---|---|
| **버전** | [CONSTITUTION_VERSION] |
| **제정일** | [RATIFICATION_DATE] |
| **최종 개정일** | [LAST_AMENDED_DATE] |
| **최초 제정자** | [RATIFICATION_AUTHOR] |
| **최종 개정자** | [LAST_AMENDED_AUTHOR] |

## 핵심 원칙

### [PRINCIPLE_1_NAME]
<!-- 예시: I. 라이브러리 우선(Library-First) -->
[PRINCIPLE_1_DESCRIPTION]
<!-- 예시: 모든 기능은 독립 실행 가능한 라이브러리로 시작한다; 라이브러리는 자족적(self-contained)이고, 독립적으로 테스트 가능하며, 문서화되어야 한다; 명확한 목적이 필요하다 - 단순히 코드를 묶기 위한 라이브러리는 금지 -->

### [PRINCIPLE_2_NAME]
<!-- 예시: II. CLI 인터페이스 -->
[PRINCIPLE_2_DESCRIPTION]
<!-- 예시: 모든 라이브러리는 CLI로 기능을 노출한다; 텍스트 입출력 프로토콜: stdin/args → stdout, 오류 → stderr; JSON 형식과 사람이 읽을 수 있는 형식을 모두 지원한다 -->

### [PRINCIPLE_3_NAME]
<!-- 예시: III. 테스트 우선(Test-First) (타협 불가) -->
[PRINCIPLE_3_DESCRIPTION]
<!-- 예시: TDD 필수: 테스트 작성 → 사용자 승인 → 테스트 실패 확인 → 그다음 구현; Red-Green-Refactor 사이클을 엄격히 준수한다 -->

### [PRINCIPLE_4_NAME]
<!-- 예시: IV. 통합 테스트 -->
[PRINCIPLE_4_DESCRIPTION]
<!-- 예시: 통합 테스트가 필요한 영역: 새 라이브러리의 계약(contract) 테스트, 계약 변경, 서비스 간 통신, 공유 스키마 -->

### [PRINCIPLE_5_NAME]
<!-- 예시: V. 관측 가능성(Observability), VI. 버저닝 & 파괴적 변경, VII. 단순성 -->
[PRINCIPLE_5_DESCRIPTION]
<!-- 예시: 텍스트 I/O로 디버깅 가능성을 보장한다; 구조화된 로깅 필수; 또는: MAJOR.MINOR.BUILD 형식; 또는: 단순하게 시작하고 YAGNI 원칙을 따른다 -->

## [SECTION_2_NAME]
<!-- 예시: 추가 제약사항, 보안 요구사항, 성능 기준 등 -->

[SECTION_2_CONTENT]
<!-- 예시: 기술 스택 요구사항, 컴플라이언스 기준, 배포 정책 등 -->

## [SECTION_3_NAME]
<!-- 예시: 개발 워크플로, 리뷰 프로세스, 품질 게이트 등 -->

[SECTION_3_CONTENT]
<!-- 예시: 코드 리뷰 요구사항, 테스트 게이트, 배포 승인 프로세스 등 -->

## Governance
<!-- 예시: 헌장은 다른 모든 관행에 우선한다; 개정에는 문서화, 승인, 마이그레이션 계획이 필요하다 -->

[GOVERNANCE_RULES]
<!-- 예시: 모든 PR/리뷰는 헌장 준수 여부를 확인해야 한다; 복잡도는 반드시 정당화되어야 한다; 런타임 개발 가이드는 [GUIDANCE_FILE]을 참고한다 -->
