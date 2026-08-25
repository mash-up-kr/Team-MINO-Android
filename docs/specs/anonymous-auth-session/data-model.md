# Phase 1 데이터 모델: 비회원 익명 인증 세션

**대상 스펙 경로**: `docs/specs/anonymous-auth-session`

**소속 문서**: [plan.md](./plan.md) — 이 문서는 plan에 종속된 부속 산출물이며 독자 버전을 갖지 않는다. 현재 상태만 담는다.

이 스펙은 서버 API를 새로 추가하지 않는다. 따라서 DTO·Mapper·DB Entity가 없고, 도메인 모델 1개와 그 모델을 만들어 내는 원천 타입만 존재한다.

---

## 1. 도메인 엔티티

### `AnonymousSession` — 익명 세션

**위치**: `core/domain/src/main/kotlin/team/mino/core/domain/model/AnonymousSession.kt`

spec §2.3의 "익명 세션"과 "사용자 식별자"를 하나의 모델로 표현한다. 세션이 가진 관찰 가능한 상태가 식별자뿐이라 별도 타입으로 나누지 않는다.

| 필드 | 타입 | 필수 | 의미 |
|---|---|---|---|
| `userId` | `String` | O | 인증 제공자가 이 익명 세션에 부여한 고유 값. 서버가 데이터 소유자를 판정하는 키다 |

앱이 검증하는 규칙은 없다. 발급 주체가 인증 제공자이고(FR-009), 값의 형식·고유성은 그쪽 계약이다 — 앱이 다시 판정하면 판정 기준이 둘로 갈린다. 값이 비어 오는 상황은 계약 위반이므로 도메인 예외가 아니라 버그로 다룬다.

**수명**

| 사건 | 결과 | 근거 |
|---|---|---|
| 최초 확보 | 새 `userId` 발급 | FR-001 |
| 앱 재실행 | 같은 `userId` 복원, 네트워크 왕복 0회 | FR-002 · SC-001 |
| 장기 미사용 후 재실행 | 같은 `userId` 유지 | FR-017 |
| 앱 삭제 후 재설치 | 다른 `userId` 발급, 이전 값 복구 불가 | FR-006 |
| 기기 백업·이전 | 이전되지 않음 → 대상 기기는 새 세션 | FR-007 |
| 회원 탈퇴 | 폐기 (이번 범위 밖 — 승격 스펙이 정의) | FR-014 |

**상태 전이**

세션 자체는 상태 기계를 갖지 않는다. `없음 → 확보됨` 단방향이며, 확보 후에는 앱 삭제·회원 탈퇴 외에 `없음`으로 돌아가는 경로가 없다(FR-014·FR-017). 확보 **과정**의 상태(정상·지연·연결 실패·그 밖의 실패)는 진입 화면이 소유하며 도메인 모델이 아니다 — [research.md](./research.md) R-011.

**앱 내 소비자**

`userId`의 앱 내 용도는 spec §2.3이 크래시 리포트·분석 이벤트와 서버 로그의 대조로 한정했고, 그 배선을 요구하는 FR은 없다. 따라서 이번 범위에서 이 값을 소비하는 프로덕션 코드는 만들지 않는다 — 반환값의 역할은 계약 검증과 [quickstart.md](./quickstart.md) §2의 단위 테스트다. 실기기에서 `userId`를 눈으로 확인하는 절차는 호출자가 생긴 뒤로 이연됐다(같은 문서 §3).

---

## 2. 신원 증명

도메인 모델로 두지 않는다. 결정과 근거는 [research.md](./research.md) R-016이 소유한다.

---

## 3. 데이터 원천 타입 (`:core:data` 내부)

도메인 모델이 아니며 모듈 밖으로 나가지 않는다(전부 `internal`). 계약 형태는 [contracts/identity-proof-attachment.md](./contracts/identity-proof-attachment.md) §1이 소유한다.

| 타입 | 역할 | 산출 |
|---|---|---|
| `AnonymousAuthProvider` | 인증 제공자에서 현재 세션을 읽고, 없으면 익명 세션을 발급받는다 | `userId`(`String`) |
| `IdTokenProvider` | 현재 세션의 신원 증명을 얻는다 | 신원 증명 문자열, 세션이 없으면 `null` |

---

## 4. 실패 모델

새 실패 체계를 만들지 않고 `MinoDomainException`(`:core:error-handling`)을 확장한다.

| 리프 | 상태 | 이 스펙에서의 의미 | 근거 |
|---|---|---|---|
| `Network(cause)` | 기존 | 연결 문제로 세션 확보·신원 증명 획득이 실패했다 | FR-018 (연결 문제 갈래) · EC-001 |
| `Auth(cause)` | **신설** | 연결은 됐으나 인증 제공자가 세션·신원 증명을 발급하지 못했다 | FR-018 (그 밖의 실패 갈래) · TS-020 |
| `Http(code, cause)` | 기존 | 이 스펙에서는 쓰지 않는다 (Mino 서버 응답 실패용) | — |

리프의 선언 형태는 [contracts/domain-exception-auth-leaf.md](./contracts/domain-exception-auth-leaf.md), 원천 예외와의 매핑과 열거 밖 예외의 처리는 [contracts/identity-proof-attachment.md](./contracts/identity-proof-attachment.md) §2가 소유한다.
