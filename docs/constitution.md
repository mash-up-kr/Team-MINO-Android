<!--
Sync Impact Report

이 파일은 1.0.0에서 2.0.1로 한 번에 올라간다. 아래 두 개정을 순서대로 기록한다.

## 1.0.0 → 2.0.0 (MAJOR)
- 수정된 섹션: 기술 표준과 제약 §스택과 구조 — 디자인 토큰 조항 재정의
- 수정된 원칙: 없음
- 옛 조항: "색·치수·타이포는 `../core/design-system/README.md`의 토큰 체계로만 접근한다. 하드코딩 값을
  쓰지 않으며, 디자인 시스템 컴포넌트 API는 Material3 컴포넌트 패턴을 따른다."
- 새 조항: 축에 그림자를 더하고, 강제 기준을 **존재**에서 **값 일치**로 바꿨다. 값이 일치하는 토큰이
  있으면 반드시 토큰, 없으면 실측값을 그대로 쓴다. 토큰 신설은 구현의 선행 조건이 아니다.
- MAJOR 판정 근거: "하드코딩 값을 쓰지 않으며"가 삭제됐다. 토큰이 없으면 구현을 멈추던 기존 작업
  방식이 무효화되고, 실측 raw가 조건부로 **허용**으로 뒤집힌다. Governance §버저닝 정책의 "기존 작업
  방식을 무효화하는 재정의"에 해당한다.
- 함께 제거된 문장: "디자인 시스템 컴포넌트 API는 Material3 컴포넌트 패턴을 따른다." 토큰 조항과 한
  문장에 묶여 있던 탓에 재작성에 휩쓸린 것으로, 의도한 폐지가 아니다. 2.0.1에서 별도 줄로 복원했다.
- 근거 ADR: `adr/2026-08-13-design-token-when-value-matches.md` ·
  `adr/2026-08-13-no-shape-token-foundation.md`

## 2.0.0 → 2.0.1 (PATCH)
- 수정된 섹션: 기술 표준과 제약 §스택과 구조 — 디자인 토큰 조항에 판정·대조 절차 문서 지목 추가,
  2.0.0이 휩쓸어 지운 Material3 컴포넌트 패턴 조항을 별도 줄로 복원
- 수정된 원칙: 없음
- 추가된 섹션: 없음
- 제거된 섹션: 없음
- 유예된 플레이스홀더: 없음
- PATCH 판정 근거: 2.0.0이 정한 강제 기준("값이 일치하는 토큰이 있으면 토큰, 없으면 실측값")과 검증
  방식("Figma 원본과의 대조로 판정")이 그대로다. 그 판정·대조를 실제로 어떻게 수행하는지를 소유한
  문서로 가는 링크만 덧붙였다. 무효화되는 기존 작업 방식이 없고 규정 범위도 넓어지지 않는다. M3 조항
  복원도 1.0.0에 있던 규칙을 되돌리는 것이라 새 규정이 아니다.
- 개정 경위: 2.0.0이 남긴 TODO(TOKEN_DECISION_PROCEDURE)를 닫는 과정에서, 판정 절차의 소유자를
  `../core/design-system/README.md`가 아니라 새 규약 문서로 정했다. 그 절차를 따르는 주체가 대부분
  `:core:design-system` 밖(feature UI·감사)에 있어, 모듈 README에 두면 지켜야 할 주체가 `../CLAUDE.md`
  네비게이션 표에서 그 문서에 도달하지 못하기 때문이다. 조항의 배경·근거는 계속 ADR이 소유한다 —
  `adr/2026-08-13-no-shape-token-foundation.md` · `adr/2026-08-13-design-token-when-value-matches.md`.
- 해소된 TODO:
  - TODO(TOKEN_DECISION_PROCEDURE): `conventions/figma-design-fidelity.md`를 판정 절차의 소유자로 정하고
    작성 완료했다. TODO가 포함을 요구한 다크 모드 단서("모드에 따라 달라져야 하는 색은 raw로 표현할 수
    없으므로 디자이너에게 변수 바인딩을 요청한다")는 그 문서 §2.1이 소유한다.
  - TODO(SPEC_RECHECK): `group-room-form` 기능의 Constitution Check를 2.0.0 기준으로 재판정했다. "디자인
    토큰만 사용" 항목이 "토큰·실측값 판정" 게이트로 대체됐다.
  - TODO(AGENT_RULE_DUPLICATION): `../.claude/agents/ui-developer.md`가 복제하던 구 조항 본문을
    `conventions/figma-design-fidelity.md` 링크 참조로 교체했다.
- 후속 TODO:
  - TODO(SDD_PIPELINE_SOURCE): 원칙 IV는 SDD 단계의 순서와 게이트만 규정하고, 단계별 산출물·도구 구성의
    단일 출처를 지목하지 않는다. 이슈 #124 SDD 개편이 파이프라인 문서를 확정하면 원칙 IV의 출처에 그
    문서를 추가하고 MINOR 개정한다. (현재 파이프라인 문서 자체가 없어 `../CLAUDE.md` 네비게이션 표에도
    해당 줄이 없다. 후속 범위는 `adr/2026-08-07-sdd-single-skill-per-stage.md` §결과가 함께 든다.)
-->

# MinoAndroid 헌법(Constitution)

| 항목 | 내용 |
|---|---|
| **버전** | 2.0.1 |
| **제정일** | 2026-08-07 |
| **최종 개정일** | 2026-08-14 |
| **최초 제정자** | Jaesung Lee |
| **최종 개정자** | Jaesung Lee |

이 문서는 MinoAndroid의 모든 작업 — 사람의 작업과 에이전트의 작업 모두 — 이 따라야 하는 최상위 규범이다.
개별 규약 문서보다 상위이며, 세부 규칙의 본문은 담지 않고 그 출처를 지목한다.

## 핵심 원칙

### I. 단일 출처 문서화(SSOT)

하나의 규칙은 정확히 하나의 문서에만 정의된다.

- 다른 문서·스킬·커맨드는 그 문서를 **링크로 참조**할 뿐, 내용을 요약하거나 다시 풀어쓰지 않는다. (MUST)
- [`../CLAUDE.md`](../CLAUDE.md)는 "상황 → 참조할 문서" 네비게이션만 담고 규칙 본문을 담지 않는다. (MUST)
- 새 문서는 성격에 맞는 `docs/` 하위 디렉터리(`conventions/`·`architecture/`·`operations/`·`adr/`·`failures/`)에
  두고, 같은 변경 안에서 `../CLAUDE.md` 표에 한 줄을 추가한다. (MUST)
- 어떤 규칙의 출처가 어디인지 불분명하면, 규칙을 새로 쓰기 전에 출처부터 정한다. (MUST)

**근거**: 같은 규칙이 두 곳에 존재하면 반드시 갈라지고, 이후의 사람과 에이전트는 어느 쪽이 최신인지 알 수 없는
사본을 근거로 판단하게 된다. 부연 설명을 덧붙이는 순간 그 부연이 두 번째 출처가 된다.

**강제력**: MUST
**출처**: [`../CLAUDE.md`](../CLAUDE.md) · [`conventions/compose-lint.md`](conventions/compose-lint.md) §적용 범위 ·
[`conventions/error_handling.md`](conventions/error_handling.md) §2

### II. 레이어 경계와 의존 방향

모듈 경계는 협상 대상이 아니다.

- 의존 방향은 바깥 → 안쪽(`feature`·`data` → `domain`)이며 역방향 의존을 만들지 않는다. (MUST)
- feature 모듈은 다른 feature 모듈을 의존하지 않는다. 전환은 `:core:navigation`의 계약을 통한다.
  탭 셸(`:feature:main`) → 탭 feature만 예외다. (MUST)
- Android 모듈은 Kotlin 모듈을 의존할 수 있고 그 반대는 불가하다. `:core:domain`은 Android에 의존하지 않는다. (MUST)
- 인터페이스 구현을 가진 모듈이 자신의 `di/`에서 그 바인딩을 소유한다. `:app`은 그래프를 조립할 뿐 바인딩을 두지 않는다. (MUST)
- 의존은 `implementation`이 기본이고, 전이 노출이 필요한 경우에만 `api`를 쓴다. (SHOULD)

**근거**: 이 경계를 검사하는 자동 장치가 없다(→ [기술 표준과 제약](#기술-표준과-제약)). 순환 참조를 제외하면
빌드는 위반을 잡아 주지 않으므로, 규칙이 무너지는 것을 막는 것은 문서와 리뷰뿐이다.

**강제력**: MUST
**출처**: [`architecture/modularization.md`](architecture/modularization.md) §의존성 흐름·§금지 규칙 ·
[`../core/domain/README.md`](../core/domain/README.md) §7 ·
[`adr/2026-08-02-di-binding-ownership.md`](adr/2026-08-02-di-binding-ownership.md) ·
[`adr/2026-08-01-single-module-navigation-contract.md`](adr/2026-08-01-single-module-navigation-contract.md)

### III. 결정과 실패는 기록으로 남는다

"왜 그렇게 했는지"는 코드에 남지 않는다.

- 되돌리기 어려운 설계·아키텍처 결정(라이브러리 채택·교체, 모듈 경계 변경, 의도적으로 하지 않기로 한 것)은
  ADR로 남긴다. (MUST)
- 머지된 뒤 잘못된 것으로 판명되어 되돌리거나 크게 고친 결정은 실패 기록으로 남기고, 뒤집힌 ADR의 상태를
  같은 변경에서 갱신한다. (MUST)
- 자명하거나 쉽게 되돌릴 수 있는 결정은 기록하지 않는다. 기록의 트리거·안티트리거 판단은 각 스킬의 입력
  섹션을 단일 출처로 따른다. (MUST)
- 이미 기록된 결정을 근거 없이 되돌리지 않는다. 되돌리려면 그 기록을 먼저 읽고, 무엇이 달라졌는지를
  새 기록에 쓴다. (MUST)

**근거**: 결정과 그 결정이 틀렸다는 판명은 대개 서로 다른 세션에서 일어난다. 기록이 없으면 같은 조사와 같은
논쟁을 반복하게 되고, 이미 폐기한 방식으로 되돌아가게 된다.

**강제력**: MUST
**출처**: [`adr/README.md`](adr/README.md) · [`failures/README.md`](failures/README.md) ·
[`adr/2026-07-27-record-filename-date-prefix.md`](adr/2026-07-27-record-filename-date-prefix.md)

### IV. 명세가 구현에 선행한다(Spec-First)

기능 작업은 **헌법 → 명세 → 설계 → 구현** 순서로 진행한다.

- 각 단계의 산출물은 사람의 컨펌을 받은 뒤에만 다음 단계의 입력이 된다. 컨펌 없이 다음 단계로 넘어가지
  않는다. (MUST)
- 명세는 **무엇을·왜**만 다룬다. 기술 스택·API·코드 구조 같은 **어떻게**는 설계 단계의 몫이다. (MUST)
- 요구사항은 테스트 가능해야 한다 — 그 문장만 읽고 통과·실패를 판정할 수 있어야 한다. (MUST)
- 근거가 없는 빈틈은 지어내지 않는다. 합리적 기본값을 쓸 수 있으면 그 가정을 명세에 기록하고, 선택이 범위나
  사용자 경험을 가르는 지점이면 `[TBD]`로 표시해 사람에게 되묻는다. (MUST)
- 단계 산출물은 `docs/specs/{feature-name}/` 아래에 모으고, feature 이름은 영문 kebab-case로 고정한다. (MUST)
- 템플릿이 있는 산출물은 템플릿을 먼저 복사한 뒤 제자리 편집한다. 기억으로 골격을 재생성하지 않는다. (MUST)

**근거**: 에이전트가 만드는 산출물에서 가장 비싼 실패는 잘못된 코드가 아니라 잘못된 전제 위에 쌓인 코드다.
컨펌 게이트와 `[TBD]`는 그 전제를 사람이 확인하는 유일한 지점이다.

**강제력**: MUST
**출처**: [`../mino-sdd/template/`](../mino-sdd/template/) ·
[`../.claude/skills/mino-spec/SKILL.md`](../.claude/skills/mino-spec/SKILL.md)
(단계별 산출물 구성의 단일 출처는 미확정 — Sync Impact Report의 `TODO(SDD_PIPELINE_SOURCE)` 참조)

### V. 컨벤션은 권고가 아니라 게이트

브랜치·커밋·PR·Lint·에러 처리 규약은 준수 대상이며, 예외는 문서에 기록된 것만 인정한다.

- 작업 브랜치는 항상 `develop`에서 분기하고, 이름·prefix는 브랜치 네이밍 컨벤션을 따른다. (MUST)
- 커밋 메시지는 Conventional Commits 형식을 따르고, 하나의 커밋은 하나의 논리 단위를 담는다. (MUST)
- PR은 템플릿을 채워 `develop`을 대상으로 올리며, 이슈 연결 키워드를 포함한다. (MUST)
- Compose Lint 위반은 코드 수정으로 해소하는 것이 원칙이다. severity 완화와 baseline 동결은 각각 합의와
  최후 수단이며, 사유를 PR 본문에 남긴다. (MUST)
- 예상 가능한 실패는 `MinoDomainException`으로 매핑해 소비하고, 프로그래머 버그는 잡지 않고 전파한다.
  정상 유저 시나리오에서 CEH에 도달해서는 안 된다. (MUST)

**근거**: 규약을 "가능하면 지키는 것"으로 두면 지키지 않은 것이 기본값이 된다. 이 저장소는 자동 검증이
얇으므로(→ [기술 표준과 제약](#기술-표준과-제약)) 규약의 강제력은 선언과 리뷰에서 나온다.

**강제력**: MUST
**출처**: [`conventions/branch-naming.md`](conventions/branch-naming.md) ·
[`conventions/commit-message.md`](conventions/commit-message.md) ·
[`conventions/pull-request.md`](conventions/pull-request.md) ·
[`conventions/compose-lint.md`](conventions/compose-lint.md) ·
[`conventions/error_handling.md`](conventions/error_handling.md)

## 기술 표준과 제약

**스택과 구조**

- Kotlin · Jetpack Compose · Hilt 기반의 다중 Gradle 모듈 프로젝트다. 모듈 목록과 각 모듈의 책임은
  [`architecture/modularization.md`](architecture/modularization.md)를 단일 출처로 한다.
- feature는 단일 모듈이며 진입형·탭 두 종류로 나뉜다. 골격·공개 범위·Route↔Screen 연결은
  [`architecture/feature-module.md`](architecture/feature-module.md), 화면 전환은
  [`architecture/feature-navigation.md`](architecture/feature-navigation.md)를 따른다.
- 색·치수·타이포·그림자는 [`../core/design-system/README.md`](../core/design-system/README.md)의 토큰 체계를
  단일 접근점으로 한다. 디자인 값과 일치하는 토큰이 있으면 반드시 토큰으로 접근하고, 없으면 디자인
  실측값을 직접 쓴다. 토큰이 없다는 이유로 구현을 멈추거나 토큰 신설을 선행 조건으로 삼지 않으며,
  준수 여부는 코드에 적힌 근거가 아니라 Figma 원본과의 대조로 판정한다. 그 판정과 대조를 수행하는
  절차는 [`conventions/figma-design-fidelity.md`](conventions/figma-design-fidelity.md)를 따른다.
- 디자인 시스템 컴포넌트 API는 Material3 컴포넌트 패턴(Defaults·Colors·컴포넌트 토큰)을 따른다. 본문은
  [`../core/design-system/README.md`](../core/design-system/README.md), 근거는
  [`adr/2026-07-25-design-system-component-m3-pattern.md`](adr/2026-07-25-design-system-component-m3-pattern.md)가 소유한다.

**검증 장치의 한계 — 이 저장소가 스스로 잡아 주지 못하는 것**

이 목록은 현재 상태의 정직한 서술이며, 원칙 II·V의 강제력이 왜 리뷰에 의존하는지의 근거다.

- 모듈 의존 방향을 검사하는 Lint 룰이나 Gradle 의존 검증이 없다. 순환 참조 외의 경계 위반은 빌드가 잡지
  못한다.
- PR을 검증하는 CI가 없다. Lint·빌드가 자동으로 돌아가는 지점이 없으므로 "CI가 잡아 줄 것"이라는 전제로
  작업하지 않는다.
- 로컬 `./gradlew lintDebug`는 JBR JIT 이슈로 데몬이 죽을 수 있다. 이는 코드 문제가 아니며, 그렇다고 검증이
  수행된 것도 아니다.
- 따라서 빌드 확인의 최소선은 `./gradlew :app:assembleQaDebug`이고, 경계·규약 위반의 실질적 게이트는 리뷰다.

**검증 장치를 추가하는 변경은 이 섹션의 갱신을 동반한다.** (MUST)

## 개발 워크플로와 품질 게이트

**작업 흐름**

1. 이슈를 만들고 `develop`에서 작업 브랜치를 분기한다. 병렬 작업은 워크트리로 분리한다
   ([`conventions/worktree.md`](conventions/worktree.md)).
2. 기능 작업이면 원칙 IV의 단계 순서를 따른다. 각 단계는 사람의 컨펌으로 닫힌다.
3. 구현 후 `./gradlew :app:assembleQaDebug`로 빌드를 확인한다.
4. 논리 단위로 커밋하고 PR을 올린다. 리뷰에서 헌법 준수를 확인한다.
5. 되돌리기 어려운 결정이 확정되었으면 ADR을, 과거 결정이 뒤집혔으면 실패 기록을 같은 변경에 포함한다.

**에이전트 행동 규칙**

- 에이전트는 `git commit`을 직접 실행하지 않는다. 파일 작성과 검증까지 수행하고, 커밋 분할안은 제안에
  그친다. 커밋은 사용자가 실행한다. (MUST)
- 대화·문서·템플릿은 한국어로 쓴다. 영문을 쓰는 곳은 브랜치 slug, 코드 식별자, feature 디렉터리 이름처럼
  규약이 영문을 요구하는 자리로 한정한다. (MUST)
- 규칙을 추측으로 재구성하지 않는다. 작업 상황에 해당하는 문서를 [`../CLAUDE.md`](../CLAUDE.md) 표에서 찾아
  먼저 읽는다. (MUST)
- 요청 범위를 넘는 파일을 만들거나 고치지 않는다. 범위 밖의 의도는 후속 작업으로 제안한다. (MUST)

**품질 게이트**

| 게이트 | 시점 | 판정 |
|---|---|---|
| 명세 컨펌 | 명세 작성 후 | 사람이 컨펌해야 설계 단계로 넘어간다 |
| 설계 컨펌 | 설계 작성 후 | 사람이 컨펌해야 구현을 시작한다 |
| 빌드 | 구현 후 | `./gradlew :app:assembleQaDebug` 성공 |
| Lint | 구현 후(가능한 환경에서) | 위반 0건 또는 문서화된 예외 |
| 리뷰 | PR | 헌법 준수 확인 — 특히 원칙 II·III·V |

## Governance

**우선순위**

이 헌법은 저장소의 다른 모든 관행에 우선한다. 다만 헌법이 특정 문서를 단일 출처로 지목한 영역에서는 그
문서가 세부의 출처다(원칙 I). 헌법과 개별 문서가 충돌하면 헌법이 이기며, 충돌 자체가 개정 대상이다.

**개정 절차**

1. 개정은 `/mino-constitution`으로 이 파일을 갱신하는 것으로 시작한다. 이 파일 외의 산출물은 같은 작업에서
   건드리지 않는다.
2. 파일 최상단의 Sync Impact Report를 갱신한다 — 버전 변경, 수정·추가·제거된 원칙과 섹션, 유예 항목.
3. 버전, 최종 개정일(ISO `YYYY-MM-DD`), 최종 개정자를 헤더 표에 반영한다.
4. 개정은 PR로 올려 리뷰를 받는다. 헌법 변경만 담은 커밋으로 분리한다.
5. 헌법을 읽어 동작하는 스킬·템플릿은 런타임에 이 파일을 읽으므로 개정과 함께 고치지 않는다. 그것들의
   수정이 필요하다면 별도 작업으로 분리한다.

**버저닝 정책**

시맨틱 버저닝을 따른다.

- **MAJOR** — 원칙의 제거, 또는 기존 작업 방식을 무효화하는 재정의.
- **MINOR** — 원칙·섹션의 추가, 또는 실질적으로 범위가 넓어진 규정.
- **PATCH** — 표현 수정·오타·명확화 등 의미를 바꾸지 않는 개선.

증가 유형이 모호하면 근거를 Sync Impact Report에 남기고 더 큰 쪽을 택한다.

**준수 검토**

- 모든 PR 리뷰는 헌법 준수 여부를 확인한다. 위반은 머지 차단 사유다.
- 위반이 불가피하면 되돌리거나, 예외를 ADR로 기록하고 그 링크를 PR 본문에 남긴다. 기록 없는 예외는 없다.
- 복잡도는 정당화되어야 한다 — 규칙을 우회하는 구조가 필요하다면 그 이유가 먼저 문서로 남는다.
- 기능 작업의 명세 단계에서 이 문서를 다시 읽는다. 원칙과 실제 작업 방식이 어긋나 있으면 둘 중 하나가
  틀린 것이며, 어느 쪽인지 결정해 개정하거나 작업 방식을 고친다.
- 런타임 개발 가이드(상황별 참조 문서)는 [`../CLAUDE.md`](../CLAUDE.md)의 네비게이션 표를 따른다.
