# 계약: `MinoDomainException.Auth` 리프 추가 (`:core:error-handling` 공개 API)

**소속 문서**: [plan.md](../plan.md) — 부속 산출물이며 독자 버전을 갖지 않는다.

FR-018이 요구하는 실패 2종 구분을 기존 도메인 예외 체계 안에서 표현하기 위해 리프 하나를 추가한다. 새 실패 체계를 만들지 않는다(FR-013 · TS-006). 판단 근거는 [research.md](../research.md) R-007.

---

## 1. 추가되는 리프

기존 `sealed class MinoDomainException`(`Network`·`Http`)에 리프 하나를 더한다. 나머지 선언은 그대로다.

```kotlin
// core/error-handling/src/main/kotlin/team/mino/core/errorhandling/MinoDomainException.kt
/** 연결은 됐으나 인증 제공자가 세션·신원 증명을 발급하지 못했다. */
class Auth(cause: Throwable) : MinoDomainException(cause = cause)
```

리프의 형태 규칙(생성자 형태·문구 금지·탈출구 금지·매핑과 짝 추가)은 [`core/error-handling/README.md`](../../../../core/error-handling/README.md) §4가 소유한다. `Auth`가 그 규칙을 만족하는 방식은 다음과 같다.

| §4의 규칙 | `Auth`의 준수 방식 |
|---|---|
| `class` · 필수 `cause` · 문구 없음 | 위 선언 그대로 |
| 매핑 지점과 짝으로 추가 | 짝은 [identity-proof-attachment.md](./identity-proof-attachment.md) §2의 화이트리스트다. §4가 상정한 짝(Ktor validator)이 아니라는 점은 [plan.md](../plan.md) §복잡도 추적 V-1이 다룬다 |
| 탈출구가 아님 | 열거된 인증 제공자 예외에만 매핑한다 — 열거 밖 처리는 §2가 소유 |

---

## 2. 소비 측 영향

- `MinoDomainException`은 `sealed`이며 소비 `when`에 `else`가 허용되므로([`core/error-handling/README.md`](../../../../core/error-handling/README.md) §6), 리프 추가가 기존 소비 코드를 깨뜨리지 않는다.
- 이 리프를 실제로 구분해 표현하는 곳은 진입 화면 하나다(FR-016 — 실패 표현이 한 곳에만 존재한다). 표현 조건은 [anonymous-auth-repository.md](./anonymous-auth-repository.md) §4 C-6·C-7이 소유한다.
