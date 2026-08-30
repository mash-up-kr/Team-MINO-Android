# 실패 응답의 `errorCode`를 도메인 예외로 올리지 않고, 에러 본문 타입은 공용으로 하나만 둔다

- **상태**: Accepted
- **작성일**: 2026-08-28
- **작성자**: Jaesung Lee

## 컨텍스트

꾹 서버(`Team MINO API` 1.0.0)는 성공 응답만 `{ data }`로 감싸고, 실패 응답은 봉투 없이 `{ "errorCode", "message" }`로 내려준다. 봉투 쪽은 [응답 봉투 ADR](2026-08-27-response-envelope-unwrapped-in-apiservice.md)이 이미 확정했고, 그 ADR은 "에러 본문은 이 봉투가 아니며 이 타입이 다루지 않는다"로 실패 본문을 **명시적으로 범위 밖에 두었다.** 그 자리가 비어 있었다.

비어 있어도 문제가 없었던 것은 **실패 본문을 읽는 코드가 한 줄도 없었기 때문**이다. 실패 판정은 `expectSuccess = true`가 만든 예외를 매핑 지점이 상태 코드만으로 `MinoDomainException`에 옮겨 끝났다([원천별 매핑 지점 ADR](2026-08-22-domain-exception-mapping-per-source.md) · [`docs/conventions/error_handling.md`](../conventions/error_handling.md) §3). `PinApiService`·`SharedPlaceSaveWorker`는 그 사실을 KDoc에 "실패 판정은 `errorCode`가 아니라 HTTP 상태 코드만 본다"로 못 박아 두기까지 했다.

`GET /api/v1/users/me`가 붙으면서 그 전제가 처음으로 깨졌다([profile](../specs/profile/plan.md)이 설계했고, splash-screen이 먼저 구현했다). 서버가 **인증 실패와 미등록을 같은 `401`로** 내려주어([API 계약](../specs/profile/contracts/profile-api-contract.md) §2 협의 항목 ⑤), 상태 코드만으로는 "로그인 문제"와 "아직 가입 안 함"을 가를 수 없고, 가르지 못하면 온보딩 분기가 성립하지 않는다.

정할 것은 둘이었다. **에러 본문 타입을 누가 소유하는가**, 그리고 **`errorCode`를 도메인 예외까지 올릴 것인가**. 두 번째는 프로필만의 문제가 아니다 — 서버를 소비하는 모든 feature가 언젠가 같은 갈림길에 선다.

## 결정

**하나.** 실패 본문 DTO는 `:core:data`의 `network/dto/response/ErrorResponse.kt` **하나만** 둔다. 서버 전역의 형식이므로 타입 이름에 feature를 담지 않고, 성공 봉투(`MinoResponse<T>`)에 감싸지 않는다.

```kotlin
@Serializable
internal data class ErrorResponse(
    val errorCode: String,
    val message: String? = null,
)
```

**둘.** `errorCode`를 `MinoDomainException`의 리프로 승격하지 않는다. 실패 판정의 기본은 여전히 상태 코드이며, 본문 코드를 봐야 하는 지점만 **그 자리에서 지역 처리**한다. 지역 catch는 [`docs/conventions/error_handling.md`](../conventions/error_handling.md) §3이 "엔드포인트별 특수 정책"으로 이미 열어 둔 자리를 쓰는 것이고, 그 밖으로 나가지 않는다.

```kotlin
// UserApiService — 이 저장소에서 에러 본문을 읽는 유일한 지점
} catch (failure: MinoDomainException.Http) {
    if (failure.code == HttpStatusCode.Unauthorized.value && failure.isUserNotRegistered()) {
        null
    } else {
        throw failure
    }
}
```

소유 서비스가 `UserApiService`인 것은 세 유저 오퍼레이션이 OpenAPI `user` 태그에 속하기 때문이다 — [`ApiService` 소유 단위 ADR](2026-08-28-api-service-owned-per-server-tag.md).

본문을 읽지 못하면 `false`로 떨어뜨려 원래의 `401`을 그대로 전파한다.

## 근거

**에러 본문은 feature의 것이 아니라 서버의 형식이다.** 프로필은 그것을 처음 읽은 소비자일 뿐이고, 형식 자체는 모든 엔드포인트가 공유한다. 이름에 feature를 담으면 두 번째 소비자가 같은 모양의 타입을 다시 만들게 된다 — [응답 봉투 ADR](2026-08-27-response-envelope-unwrapped-in-apiservice.md)이 성공 쪽에서 타입 하나를 택한 이유와 같다.

**리프로 올리면 예외 계층이 서버 에러 코드 표를 베끼기 시작한다.** 리프 추가는 매핑 지점의 화이트리스트와 짝이어야 하는데([`core/error-handling/README.md`](../../core/error-handling/README.md) §4), 한 상태 코드에 여러 `errorCode`가 달리는 순간 그 화이트리스트는 서버가 정의한 코드 수만큼 늘어난다. 게다가 리프는 저장소 전체가 `when`으로 소비하는 **공용 어휘**인데, `USER_NOT_REGISTERED`는 엔드포인트 하나의 사정이다. 공용 어휘에 한 엔드포인트의 사정을 새기면 그 다음 feature도 자기 사정을 새길 근거를 갖는다.

**매핑 지점이 본문을 읽게 되는 것이 더 큰 대가다.** 상태 코드 판정은 동기적이고 실패할 수 없지만 본문 파싱은 suspend이고 실패할 수 있다. 그 부담을 모든 요청이 통과하는 한 지점에 지우면, 본문을 볼 필요가 없는 절대다수의 실패 경로까지 파싱 실패라는 새 실패 모드를 안게 된다.

**관례를 어기는 것이 아니라 관례가 다루지 않는 경우다.** 다른 곳이 상태 코드만 보는 것은 실패를 **가를 필요가 없기 때문**이다(실패는 전부 같은 실패다). 프로필의 `401`은 성격이 전혀 다른 두 상태를 겸하고 있어 가르지 않으면 화면이 성립하지 않는다. 선례 둘과 이 한 건은 충돌하지 않는다.

**본문을 못 읽으면 미등록이 아니라고 본다.** 미등록이라 결론 낼 수 없는 상황에서 파싱 예외를 던지면, 진짜 원인인 `401`이 직렬화 실패에 덮여 사라진다. 직렬화 실패를 도메인 예외로 매핑하지 않는 §3의 판단과 방향이 같다 — 실패 경로에서 파싱은 원인을 바꿔치기할 수 있으므로 조용히 포기하고 원래 실패를 전파한다.

## 결과

- **새 엔드포인트의 기본은 상태 코드 판정이다.** 본문 `errorCode`를 읽으려면 "같은 상태 코드가 성격이 다른 상태를 겸한다"는 근거가 있어야 하고, 처리는 그 `ApiService`의 지역 catch 안에 가둔다. 근거 없이 읽기 시작하면 이 ADR을 먼저 고친다.
- **`MinoDomainException`의 리프는 늘지 않는다.** 서버 에러 코드 때문에 리프를 추가하자는 제안은 이 ADR이 기각 사유가 된다.
- **`ErrorResponse`에 코드 상수 표를 모으지 않는다.** 코드 문자열은 그것을 읽는 지점의 상수로 둔다(`UserApiService`의 `USER_NOT_REGISTERED`). 표를 모으면 아무도 읽지 않는 코드까지 타입이 알게 되고, 그것이 리프 승격의 다음 근거가 된다.
- **현재 소비 지점은 `UserApiService`의 `401` 판정 헬퍼 하나다.** 엔드포인트는 `GET /api/v1/users/me` 하나이지만 그것을 부르는 함수는 둘이고(`hasProfile()`은 splash-screen의 진입 판정, `getMe()`는 profile의 프리필), **둘이 같은 헬퍼를 공유한다.** 읽는 지점을 하나로 유지하는 것이 이 ADR이 지키려는 것이지, 호출자가 하나라는 뜻이 아니다.
- 서버가 미등록을 `404`로 바꾸면 그 헬퍼는 통째로 사라지고 `ErrorResponse`의 소비자는 0이 된다. 그 자리는 [API 계약](../specs/profile/contracts/profile-api-contract.md) §4가 지목해 두었다.
- 실패 본문 형식 자체가 바뀌면 고칠 곳은 이 타입 하나다. `DataSource` 위 레이어는 이 타입의 존재를 모른다.
- **`core/data/README.md` §4의 문구가 이 결정과 어긋나 보인다.** §4는 엔드포인트별 특수 정책의 지역 catch를 "해당 **DataSource**에서 병용한다"고 적었고, splash-screen의 `UserRemoteDataSourceImpl`이 그것을 따라 `ResponseException`·`bodyAsText()`를 직접 만지고 있다 — 아래 §고려한 대안이 기각한 바로 그 배치다. **이 ADR이 옳고 그 코드가 뒤따라야 한다**는 것이 [profile plan 5.0.0](../specs/profile/plan.md)의 판정이며, 판정 로직을 `UserApiService`로 옮기면서 정리된다.
  - **§4 문구는 2026-08-28에 정리됐다.** 단순히 `ApiService`로 바꾼 것이 아니라 **자리를 무엇을 보는지로 갈랐다** — 상태 코드만 보는 정책(§4의 원래 예시인 "404를 빈 결과로")은 `DataSource`에 그대로 두고, **실패 본문을 읽어야 하는 정책만** `ApiService`로 보낸다. 두 경우가 다른 것을 요구하므로 한쪽으로 몰지 않았다.

## 고려한 대안

**`errorCode`마다 `MinoDomainException` 리프를 추가한다** (`UserNotRegistered` 또는 코드를 담는 `Business(code)`). 화면이 `when` 한 번으로 분기할 수 있고 `ApiService`의 지역 catch가 사라진다. 그러나 매핑 지점이 모든 실패 응답의 본문을 읽어야 하고, 리프 집합이 서버 에러 코드 표를 따라 자라며, 코드를 문자열로 담는 형태는 사실상 탈출구 리프여서 [`core/error-handling/README.md`](../../core/error-handling/README.md) §4가 금지한다. 기각.

**feature 전용 타입(`ProfileErrorResponse`)으로 둔다.** 다른 feature가 프로필 이름이 붙은 타입에 의존하는 모양을 피할 수 있다. 그러나 실패 본문은 서버 전역의 형식이므로, 두 번째 소비자가 나타나는 순간 같은 모양의 타입이 이름만 바꿔 복제된다. 기각.

**지역 catch를 `DataSource`나 `RepositoryImpl`로 올린다.** `ApiService`를 Ktor 호출만 하는 얇은 계층으로 유지할 수 있다. 그러나 본문을 다시 읽으려면 원본 `ResponseException`이 들고 있는 응답이 필요하고, 그것을 `DataSource`로 넘기면 Ktor 타입을 다루는 일이 [`core/data/README.md`](../../core/data/README.md) §5의 "데이터 출처 호출만"을 넘어선다. HTTP 세부는 HTTP를 다루는 레이어에서 끝나야 한다. 기각.

> 이 문단은 처음에 `§5·§9`를 근거로 들었으나 **§9는 이 건과 관계가 없어 인용에서 뺐다.** §9가 금지하는 것은 `DataSourceImpl`이 DTO를 **노출**하는 것이고, 여기서 Ktor 타입은 반환되지 않고 안에서만 쓰인다. 기각 사유를 지탱하는 것은 §5의 **책임 범위** 하나다 — 결정은 바뀌지 않는다.
