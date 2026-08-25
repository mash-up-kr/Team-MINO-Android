# 계약: 신원 증명 자동 첨부 (`:core:data` 내부)

**소속 문서**: [plan.md](../plan.md) — 부속 산출물이며 독자 버전을 갖지 않는다.

이 계약은 `:core:data` 밖으로 노출되지 않는다. 모든 타입이 `internal`이며, 외부에서 관찰 가능한 것은 "Mino 서버 요청에 신원 증명이 붙어 나간다"는 **행동**뿐이다(FR-008 · SC-006).

---

## 1. 원천 접근자

인증 제공자 SDK를 감싸 SDK 타입이 Repository·네트워크 계층으로 새지 않게 한다. 배치 근거는 [research.md](../research.md) R-014.

```kotlin
// core/data/src/main/java/team/mino/core/data/auth/AnonymousAuthProvider.kt
internal interface AnonymousAuthProvider {
    /** 로컬에 유지된 현재 세션의 사용자 식별자. 없으면 null. 네트워크 왕복이 없다. */
    suspend fun currentUserId(): String?

    /** 인증 제공자에서 새 익명 세션을 발급받고 사용자 식별자를 반환한다. */
    suspend fun signInAnonymously(): String
}
```

```kotlin
// core/data/src/main/java/team/mino/core/data/auth/IdTokenProvider.kt
internal interface IdTokenProvider {
    /**
     * 현재 세션의 신원 증명. 세션이 없으면 null.
     * 유효 기간 관리·갱신은 인증 제공자가 수행하며 강제 갱신을 요청하지 않는다.
     */
    suspend fun getIdToken(): String?
}
```

| 항목 | 계약 | 근거 |
|---|---|---|
| SDK 타입 비노출 | 두 인터페이스의 파라미터·반환·예외 어디에도 인증 제공자 SDK 타입이 없다 | 헌법 원칙 II |
| 실패 표현 | 구현체는 SDK 예외를 §2의 매핑을 거쳐 던진다 | FR-013 · FR-018 |
| 저장 금지 | 신원 증명을 앱 저장소에 쓰지 않는다 | [research.md](../research.md) R-010 |
| 테스트 | 두 인터페이스의 Fake로 JVM 단위 테스트가 가능해야 한다 | R-015 |

---

## 2. 예외 매핑 지점

모든 인증 제공자 호출이 통과하는 단일 지점에서 화이트리스트로 매핑한다. 매핑의 성질(화이트리스트 열거·`CancellationException` 보존·열거 밖 rethrow)은 [`error_handling.md`](../../../conventions/error_handling.md) §3이 소유하며, 여기서는 그 규칙을 **인증 제공자 예외에 적용한 분류 기준**만 정한다.

```kotlin
// core/data/src/main/java/team/mino/core/data/auth/extension/...
internal suspend fun <T> Task<T>.awaitDomain(): T
```

| 원천 예외의 갈래 | 매핑 | 근거 |
|---|---|---|
| 연결 실패 (`FirebaseNetworkException`) | `MinoDomainException.Network` | FR-018 (연결 갈래) |
| 인증 제공자가 발급에 실패 (호출 한도 초과·인증 구성 오류 등) | `MinoDomainException.Auth` | FR-018 (그 밖의 갈래) |
| 그 외 전부 (`CancellationException` 포함) | 매핑하지 않는다 | [`error_handling.md`](../../../conventions/error_handling.md) §3 |

각 갈래에 속하는 SDK 예외 클래스의 정확한 목록은 구현 시 확정한다 — 위 **분류 기준**이 계약이고, 클래스 열거는 그 적용 결과다. 열거를 넓히는 상위 타입 분기를 추가하지 않는다.

---

## 3. Ktor 첨부 플러그인

```kotlin
// core/data/src/main/java/team/mino/core/data/network/plugin/...
internal fun minoIdentityProofPlugin(idTokenProvider: IdTokenProvider): ClientPlugin<Unit>
```

`NetworkModule`의 `HttpClient` 구성에 설치한다. `provideHttpClient`가 `IdTokenProvider`를 주입받는다.

| # | 계약 | 근거 |
|---|---|---|
| A-1 | 요청 URL의 host가 `BuildConfig.API_BASE_URL`의 host와 같을 때만 `Authorization: Bearer <신원 증명>` 헤더를 붙인다 | FR-008 · FR-011 · TS-013 · TS-016 |
| A-2 | host가 다르면 헤더를 붙이지 않는다. 절대 URL 호출·리다이렉트에도 같은 판정이 적용된다 | FR-011 · TS-016 |
| A-3 | Mino host 요청인데 신원 증명이 `null`이면(세션 미확보) **헤더 없이 내보내지 않는다.** 호출자 계약 C-1 위반이므로 프로그래머 오류로 전파한다 — 도메인 예외로 감싸지 않는다 | FR-003 · SC-009 |
| A-4 | 앱이 만든 사용자 식별자를 헤더·본문·쿼리에 싣지 않는다 | FR-009 · TS-014 |
| A-5 | 신원 증명 획득이 실패하면 그 예외가 요청 호출부로 전파된다. 세션은 폐기하지 않는다 | EC-008 · FR-012 |
| A-6 | 호출자(feature·`ApiService`)는 이 동작을 위해 어떤 코드도 작성하지 않는다 | SC-006 |

> A-3의 선택 근거: SC-009는 "세션 없이 나간 Mino 서버 요청 0건"을 성과로 삼는다. 헤더만 빼고 요청을 보내면 그 위반이 앱에서 관찰되지 않아 측정 수단 자체가 사라진다.
>
> A-1의 판정이 실제로 성립하는 시점은 [plan.md](../plan.md) §전제와 이연 항목이 다룬다.

---

## 4. 서버 측 계약 (범위 밖, 전제)

spec §3.2에 따라 서버 구현은 이 스펙의 범위가 아니다. 앱 계약이 성립하기 위한 전제만 적는다.

- 서버는 `Authorization: Bearer` 헤더의 신원 증명을 검증하고, 그 결과에서 사용자 식별자를 꺼내 요청 주체로 삼는다 (FR-009 · FR-010 · TS-015).
- 서버가 기기 식별자에 의존하지 않는 상태로 함께 전환된다 (spec §4 가정).
