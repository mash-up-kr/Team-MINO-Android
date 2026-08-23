# 계약: `UserRepository` (스플래시가 쓰는 표면)

**소유 모듈**: `:core:domain` (`repository/`) · 구현은 `:core:data`

**대응 요구사항**: FR-002, FR-003, FR-004

**백엔드**: `GET /api/v1/users/me` ([swagger](https://raw.githubusercontent.com/mash-up-kr/Team-MINO-Node/refs/heads/KKardy/GM-111-outline-prd/docs/swagger.yaml))

---

## 인터페이스

```kotlin
interface UserRepository {
    /** 내 프로필을 조회한다. 프로필이 아직 만들어지지 않았으면 null. */
    suspend fun getMyProfile(): UserProfile?
}
```

> 이 파일은 **스플래시가 쓰는 표면만** 정의한다. 프로필 생성·수정(`POST /api/v1/users`, `PATCH /api/v1/users/me`)은 온보딩·마이페이지의 계약이며 이 스펙의 범위가 아니다.

## 동작 계약

| # | 조건 | 동작 | 근거 |
|---|---|---|---|
| 1 | 프로필이 있다 | `UserProfile`을 반환한다 | FR-004 |
| 2 | 프로필이 없다 | `null`을 반환한다 | FR-003 |
| 3 | 네트워크 끊김·연결 오류 | `MinoDomainException.Network`를 던진다 | FR-008, EC-004 |
| 4 | 그 밖의 실패 | `MinoDomainException.Http` 등 `Network`가 아닌 도메인 예외를 던진다 | FR-009, EC-004 |

- **세션이 확보된 뒤에만 호출된다.** 세션 없이 호출하면 `401`이다. 순서는 `anonymous-auth-session`의 호출자 계약 C-1(첫 서버 요청보다 먼저 세션 확보)을 지키는 `SplashViewModel`이 보장한다 — `ResolveSplashEntryUseCase`는 세션을 확보하지 않는다(→ [research.md R-012](../research.md)).
- 실패를 `null`로 뭉개지 않는다 — `null`은 "프로필 없음"이라는 정상 결과이고, 실패는 예외다. 뭉개면 EC-004(프로필 조회 실패)가 최초 실행으로 오판되어 기존 사용자를 온보딩으로 보낸다.

## 미확정

**[TBD-P2]** 위 계약 #2의 판정 근거가 스웨거에 없다. `GET /api/v1/users/me`는 `200 → User`와 `401 → Error`만 정의하며, 프로필 미생성을 나타내는 응답(예: `404`, 또는 `data: null`)이 없다.

- `401`을 "프로필 없음"으로 해석하면 안 된다. `401`은 인증 실패이고, 세션이 확보된 뒤 호출하는 이 경로에서 `401`이 오면 그것은 세션 문제다.
- 백엔드와 합의가 필요하다: **세션은 유효하나 프로필이 없는 사용자**에게 무엇을 돌려주는가.
- 확정 전에는 `UserRepositoryImpl`을 구현할 수 없다. 인터페이스와 `ResolveSplashEntryUseCase`는 영향받지 않는다.
