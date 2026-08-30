# 서버 응답의 `{ data }` 봉투는 제네릭 DTO 하나로 `ApiService`에서 벗긴다

- **상태**: Accepted
- **작성일**: 2026-08-27
- **작성자**: Jaesung Lee

## 컨텍스트

꾹 서버(`Team MINO API` 1.0.0)는 성공 응답을 예외 없이 한 겹 감싼다.

```json
GET /api/v1/rooms
{ "data": [ { "id": "...", "name": "맛집 탐방", ... } ] }
```

`GET /api/v1/rooms` · `POST /api/v1/users` · `GET /api/v1/users/me` 등 조회·생성 계열이 모두 같은 모양이고, 실패 응답만 봉투 없이 `{ "errorCode", "message" }`로 온다.

그런데 이 저장소에는 **실제 JSON을 파싱하는 코드가 한 줄도 없었다.** `:core:data`의 유일한 원격 DataSource 구현이 `RoomMockRemoteDataSourceImpl`이고, 이쪽은 인메모리 맵(`RoomMockStore`)을 그대로 돌려주므로 봉투를 만날 일이 없었다. `HttpClient`·`MinoIdentityProofPlugin`·`convertDomainException` 인프라는 갖춰져 있었지만 그 host로 요청을 보내는 `ApiService`가 아직 없는 상태였다.

[shared-link-receiver](../specs/shared-link-receiver/plan.md)가 `GET /api/v1/rooms`와 `POST /api/v1/rooms/{roomId}/pins`를 실서버로 붙이면서, 이 봉투를 **어느 레이어에서 벗길지**와 **타입을 몇 개 둘지**를 처음으로 정해야 했다.

## 결정

봉투 전용 제네릭 DTO를 하나만 두고, `ApiService`가 벗겨 알맹이만 반환한다.

```kotlin
// core/data/network/dto/response/MinoResponse.kt
@Serializable
internal data class MinoResponse<T>(val data: T)
```

```kotlin
// core/data/network/service/RoomApiService.kt
internal class RoomApiService @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun listRooms(): List<RoomSummaryResponse> =
        client.get("api/v1/rooms").body<MinoResponse<List<RoomSummaryResponse>>>().data
}
```

`DataSource`·`RepositoryImpl`·`Mapper`는 봉투의 존재를 알지 못한다.

## 근거

**봉투는 데이터가 아니라 전송 형식이다.** [`core/data/README.md`](../../core/data/README.md) §2의 레이어 그림에서 `DataSource`의 반환 타입은 DTO이고, DTO는 서버가 표현하는 **데이터**를 담는다. `data` 키는 그 데이터를 실어 나르는 봉투이므로 HTTP를 직접 다루는 가장 아래 레이어에서 끝나는 것이 맞다.

**엔드포인트마다 래퍼를 쓰면 같은 모양의 타입이 무한히 늘어난다.** 서버가 응답을 예외 없이 감싸므로, 래퍼를 손으로 쓰는 방식은 엔드포인트 수만큼 `XxxListResponse(val data: List<Xxx>)`를 만들어 낸다. 이 타입들은 서로 구별되는 의미가 없고 이름만 다르다.

**벗기는 지점이 하나면 새 엔드포인트를 붙이는 규칙도 하나다.** [`core/data/README.md`](../../core/data/README.md) §8의 "새 API 엔드포인트 추가 절차" 2번(`ApiService`)에서 봉투가 처리되고 끝나므로, 3번 이후 단계는 봉투를 고려하지 않는다.

**실패 경로와 겹치지 않는다.** `expectSuccess = true`가 비2xx를 예외로 바꾸고 `convertDomainException`이 `MinoDomainException`으로 매핑하므로([도메인 예외 매핑 ADR](2026-08-22-domain-exception-mapping-per-source.md)), 이 타입은 성공 경로에만 관여한다. 에러 본문(`errorCode`·`message`)은 이 봉투가 아니며 이 타입이 다루지 않는다.

## 결과

- 새 엔드포인트를 붙일 때 `ApiService`는 `body<MinoResponse<...>>().data` 형태를 따른다. 반환 타입에 `MinoResponse`가 드러나면 안 된다.
- `202`처럼 본문 스키마가 없는 응답에는 쓰지 않는다. 그런 함수의 반환은 `Unit`이다.
- `group-room-form`이 mock을 걷어내고 실서버로 전환할 때 같은 타입을 쓴다. 이 결정이 그 feature까지 구속하는 이유가 여기에 있다.
- 서버가 봉투 형태를 바꾸면 고칠 곳은 이 타입 하나와 `ApiService`들이다. `DataSource` 위 레이어는 영향을 받지 않는다.
- 이름은 `MinoDomainException`·`MinoIdentityProofPlugin`과 같은 접두어를 따랐다. 전송 형식이라는 성격을 더 드러내는 `ApiEnvelope<T>` 쪽이 나을 여지는 있으나, 하는 일이 같으므로 지금 바꿀 이유로 보지 않았다.

## 고려한 대안

**엔드포인트마다 전용 래퍼 DTO를 쓴다.** 제네릭이 없어 타입을 읽기 쉽고 kotlinx.serialization의 제네릭 처리도 신경 쓸 필요가 없다. 다만 서버가 모든 응답을 감싸므로 엔드포인트가 늘어나는 만큼 의미 없는 타입이 정확히 같은 수로 늘어난다. 기각.

**`DataSource`가 봉투째 반환하고 `RepositoryImpl`이 벗긴다.** `ApiService`가 Ktor 호출만 하는 얇은 계층으로 유지된다. 그러나 `RepositoryImpl`이 서버의 전송 형식을 알게 되어 [`core/data/README.md`](../../core/data/README.md) §6이 정한 "DataSource 호출 + Mapper 적용" 책임을 넘고, 봉투를 벗기는 코드가 Repository 수만큼 흩어진다. 기각.
