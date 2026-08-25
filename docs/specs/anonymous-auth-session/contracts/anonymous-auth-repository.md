# 계약: 익명 세션 확보 (`:core:domain` 공개 API)

**소속 문서**: [plan.md](../plan.md) — 부속 산출물이며 독자 버전을 갖지 않는다.

이 스펙이 앱의 나머지 부분에 노출하는 **유일한** 계약이다. 호출자(진입 화면)는 이 파일만 읽고 세션 확보를 배선할 수 있어야 한다.

---

## 1. 도메인 모델

```kotlin
// core/domain/src/main/kotlin/team/mino/core/domain/model/AnonymousSession.kt
package team.mino.core.domain.model

data class AnonymousSession(
    val userId: String,
)
```

필드 의미·수명은 [data-model.md](../data-model.md) §1이 소유한다.

---

## 2. Repository 인터페이스

```kotlin
// core/domain/src/main/kotlin/team/mino/core/domain/repository/AnonymousAuthRepository.kt
package team.mino.core.domain.repository

import team.mino.core.domain.model.AnonymousSession

interface AnonymousAuthRepository {
    suspend fun ensureSession(): AnonymousSession
}
```

### `ensureSession()` 동작 계약

| 항목 | 계약 | 근거 |
|---|---|---|
| **사후 조건** | 정상 반환했다면 익명 세션이 확보되어 있고, 반환된 `userId`가 그 세션의 식별자다 | FR-001 |
| **멱등성** | 몇 번을 호출해도 세션은 하나만 만들어진다. 동시 호출도 마찬가지다 | FR-004 · TS-003 · SC-004 |
| **재호출 비용** | 이미 확보된 상태의 호출은 인증 제공자와의 왕복 없이 완료된다 | FR-004 · TS-004 · SC-001 |
| **재실행 복원** | 이전 실행에서 확보한 세션이 있으면 네트워크 없이 같은 `userId`를 반환한다 | FR-002 · TS-008 · TS-009 |
| **영속성** | 확보된 세션은 이 함수의 어떤 호출로도 폐기되지 않는다 | FR-014 · FR-017 · TS-012 |
| **실패·취소** | 실패는 예외로 던지고 취소는 그대로 전파한다 — 저장소 공통 규약을 따른다 | [`error_handling.md`](../../../conventions/error_handling.md) §3 |
| **타임아웃** | 이 함수는 스스로 상한을 두지 않는다 | FR-019 · [research.md](../research.md) R-011 |

### 실패 계약

| 던지는 예외 | 언제 |
|---|---|
| `MinoDomainException.Network` | 연결 문제로 확보에 실패했다 |
| `MinoDomainException.Auth` | 연결은 됐으나 인증 제공자가 발급하지 못했다 |
| 그 밖의 `Throwable` | 계약 위반·버그. 도메인 예외가 아니므로 `runCatchingDomain`에 잡히지 않고 CEH로 간다 |

두 리프의 구분이 FR-018·SC-012가 요구하는 두 안내의 유일한 근거다. 리프 정의는 [domain-exception-auth-leaf.md](./domain-exception-auth-leaf.md), 원천 예외와의 매핑은 [identity-proof-attachment.md](./identity-proof-attachment.md) §2가 소유한다.

---

## 3. UseCase

```kotlin
// core/domain/src/main/kotlin/team/mino/core/domain/usecase/EnsureAnonymousSessionUseCase.kt
package team.mino.core.domain.usecase

class EnsureAnonymousSessionUseCase @Inject constructor(
    private val anonymousAuthRepository: AnonymousAuthRepository,
) {
    suspend operator fun invoke(): AnonymousSession
}
```

- 호출자는 Repository가 아니라 이 UseCase를 주입받는다. 작성 근거는 [research.md](../research.md) R-005.
- 계약은 §2와 동일하다 — 규칙을 더하지 않는다.

---

## 4. 호출자 계약 (진입 화면)

이 스펙은 화면을 만들지 않는다. 아래는 진입 화면 스펙·구현이 지켜야 하는 조건이며, 구체 표현(문구·임계 시간·표시 위치)은 spec §3.2에 따라 진입 화면 스펙이 소유한다.

| # | 조건 | 근거 |
|---|---|---|
| C-1 | 앱 시작 후 **첫 Mino 서버 요청보다 먼저** 이 UseCase를 호출한다 | FR-003 · TS-002 |
| C-2 | 정상 반환 전에는 다음 화면으로 전환하지 않는다 | FR-016 · TS-018 · SC-009 |
| C-3 | 실패하면 진입 화면에 머물고, 사용자 조작 없이 다시 호출한다 | FR-005 · UX-002 · EC-002 · TS-021 · SC-011 |
| C-4 | 재시도 횟수에 상한을 두지 않는다 | spec §4 가정 |
| C-5 | 재시도 루프를 도메인 예외 수신에만 종속시키지 않는다 — CEH로 가는 실패에도 화면이 안내·재시도 없이 멈춘 채 남지 않아야 한다 | FR-005 · FR-016 · [plan.md](../plan.md) §복잡도 추적 V-4 |
| C-6 | `Network`와 `Auth`를 서로 다른 안내로 표현한다 | FR-018 · UX-001 · TS-005 · TS-020 |
| C-7 | 확보가 지연되면 진행 중임을 알리고, 지연이 임계를 넘으면 **그 밖의 실패(`Auth` 안내)로 합류**시킨다. 임계 시간은 진입 화면 스펙이 정한다 | FR-019 · EC-009 · TS-022 |
| C-8 | 정상 속도로 끝나는 경로에서는 세션 확보를 알리는 표현을 노출하지 않는다 | UX-003 · TS-022 |

> **구현 주의 (계약이 아니라 함정 안내)**: C-7의 임계를 `withTimeout`으로 걸면 `TimeoutCancellationException`이 `CancellationException`이라 도메인 예외 경로를 타지 않고 CEH로 샌다. `withTimeoutOrNull`로 받아 화면 상태 전이로 처리한다.
>
> ViewModel의 코루틴 시작·실패 소비 방식은 이 계약이 정하지 않는다 — [`error_handling.md`](../../../conventions/error_handling.md) §7 리뷰 규약을 따른다.

---

## 5. 폐기되는 계약

FR-015에 따라 아래 공개 API가 사라진다. 대체는 이 문서의 계약이다.

| 폐기 | 대체 |
|---|---|
| `team.mino.core.domain.repository.DeviceRepository` | `AnonymousAuthRepository` |
| `team.mino.core.domain.usecase.EnsureDeviceIdUseCase` | `EnsureAnonymousSessionUseCase` |

제거 대상 파일 전체 목록은 [research.md](../research.md) R-013이 소유한다.
