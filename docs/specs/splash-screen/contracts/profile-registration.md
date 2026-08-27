# 계약: 프로필 등록 여부 조회

**소유 모듈**: `:core:domain` (`repository/`) · 구현은 `:core:data`

**대응 요구사항**: FR-002, FR-003, FR-004

**서버 API**: `GET /api/v1/users/me` — [Team MINO API 1.0.0](https://api.gguk.org/api-docs-json), 2026-08-27T20:19:22+09:00 조회

---

## 인터페이스

```kotlin
interface ProfileRegistrationRepository {
    /** 현재 익명 세션에 등록된 프로필이 있는지. */
    suspend fun isRegistered(): Boolean
}
```

> 프로필의 **값**을 다루는 계약이 아니다. 값의 저장·수정은 profile 스펙의 [`ProfileRepository`](../../../../core/domain/src/main/kotlin/team/mino/core/domain/repository/ProfileRepository.kt)가 소유한다. 이 계약은 스플래시의 분기에 필요한 **존재 여부**만 노출한다 — 근거는 [research.md R-014·R-015](../research.md).

## 서버 응답 대응

배포 문서에서 인용한 응답 형태다.

| 응답 | 반환·동작 | 근거 |
|---|---|---|
| `200` (`data: { id, nickname, avatar, createdAt }`) | `true` | FR-004 |
| `401` + `errorCode: USER_NOT_REGISTERED` | `false` | FR-003 |
| `401` + `errorCode: UNAUTHORIZED` \| `TOKEN_EXPIRED` | `MinoDomainException.Auth`를 던진다 | FR-009 |
| 연결 실패 | `MinoDomainException.Network`를 던진다 | FR-008 |

- **`401`을 통째로 "프로필 없음"으로 읽지 않는다.** `errorCode`로 갈라야 세션이 깨진 기존 사용자가 온보딩으로 떨어지는 일이 없다(SC-002).
- `200` 응답의 필드는 읽지 않는다. 스플래시는 존재 여부만 쓴다.
- 실패를 `false`로 뭉개지 않는다 — `false`는 "미등록"이라는 정상 결과이고 실패는 예외다. 뭉개면 EC-004가 최초 실행으로 오판된다.

## 호출 순서

**세션이 확보된 뒤에만 호출된다.** 순서는 `anonymous-auth-session`의 호출자 계약 C-1(첫 서버 요청보다 먼저 세션 확보)을 지키는 `SplashViewModel`이 보장한다 — Bearer 첨부는 `:core:data`의 플러그인이 자동으로 처리하므로 이 계약의 호출자가 토큰을 다루지 않는다.

## 미대조 없음

이 계약의 모든 응답 갈래가 위 OpenAPI 문서에 정의되어 있다. 추정으로 채운 항목이 없다.
