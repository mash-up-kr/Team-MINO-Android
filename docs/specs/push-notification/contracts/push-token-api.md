# 계약: 푸시 토큰 등록 API

**대상**: FR-002·FR-003 — 등록 토큰을 서버에 등록·재등록한다.

**근거**: 배포된 OpenAPI(`https://api.gguk.org/api-docs-json`, 조회 2026-09-03) — 대응 오퍼레이션 있음.

---

## 1. 엔드포인트

```
PUT /api/v1/users/me/push-token
```

OpenAPI 태그 `user`, operationId `uH_updatePushToken` — 같은 태그의 `POST /api/v1/users`·`GET|PATCH /api/v1/users/me`와 소유자를 공유한다([ADR 2026-08-28](../../../adr/2026-08-28-api-service-owned-per-server-tag.md)).

인증: `Authorization: Bearer <신원 증명>` — 기존 `MinoIdentityProofPlugin`이 Mino 서버로 나가는 모든 요청에 자동으로 붙인다(`core/data/network/plugin/MinoIdentityProofPlugin.kt`). 이 계약은 별도 인증 배선을 필요로 하지 않는다.

**요청 본문**

```json
{ "token": "<FCM 등록 토큰>" }
```

| 필드 | 타입 | 제약 |
|---|---|---|
| `token` | string | `minLength: 1` |

**응답 (200)**

```json
{ "data": { "ok": true } }
```

응답 본문을 역직렬화해 쓰지 않는다 — 호출자가 필요로 하는 것은 2xx 여부뿐이다(`UserApiService.hasProfile`이 같은 이유로 상태 코드만 본다).

**실패 (401)**

```json
{ "errorCode": "UNAUTHORIZED" | "TOKEN_EXPIRED" | "USER_NOT_REGISTERED", "message": "..." }
```

`USER_NOT_REGISTERED`가 가능하다는 것은 이 엔드포인트가 **프로필 등록을 마친 사용자**를 전제한다는 뜻이다. 온보딩 도중(프로필 등록 전) 앱 시작이 토큰 등록을 시도하면 이 401을 받을 수 있다 — spec FR-004·EC-002가 이미 "실패는 삼키고 다음 앱 시작에서 재시도"를 요구하므로 이 문서는 별도 분기를 두지 않는다. 사용자가 온보딩을 마친 뒤 앱을 재시작하면 그 시점 등록이 성공한다.

---

## 2. 데이터 흐름

```
PushRegistrationRepositoryImpl.registerCurrentToken()
  1. PushTokenProvider.currentToken()          # FirebaseMessaging.getInstance().token.await()
  2. UserRemoteDataSource.registerPushToken(token) # UserApiService.updatePushToken(PushTokenRequest(token))
  실패(1·2 어느 쪽이든) → 삼키고 Unit 반환. CancellationException만 rethrow.
```

`updatePushToken`은 **기존 `UserApiService`에 더한다** — 이 오퍼레이션이 `user` 태그라 그 태그의 소유자가 갖는다(위 ADR, [research.md D5](../research.md#d5-토큰-조회등록은-coredata에-기존-인증-제공자-패턴을-그대로-재사용한다) 1.0.1 보정). 클라이언트 주입·지역 catch 없음(비2xx는 `convertDomainException`이 던진다)은 그 서비스가 이미 따르는 규칙이다. `UserRemoteDataSource`도 같은 단위이므로 `registerPushToken` 함수를 더하고, DI 바인딩은 기존 `UserDataSourceModule`이 그대로 맡는다.

## 3. 서버팀 협의가 필요 없는 이유

요청·응답 스키마가 spec FR-002·FR-003의 요구("등록 토큰을 서버에 등록한다")와 완전히 일치하고, 어긋나는 지점이 없다. spec §4 가정에 남아 있던 "서버의 토큰 등록 API는 아직 존재하지 않는다"(2026-08-31 조회 기준)는 이번 조회(2026-09-03)로 해소되었다 — 완료 보고에서 spec.md 갱신을 제안한다.
