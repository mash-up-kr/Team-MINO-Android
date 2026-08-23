# 데이터 모델: 스플래시 화면

**대상 스펙 경로**: `docs/specs/splash-screen`

**계획서**: [plan.md](./plan.md)

> 현재 상태만 담는다. 과거 형태는 남기지 않는다.

---

## 1. 도메인 모델 (`:core:domain`)

### 1.1 `UserProfile`

`GET /api/v1/users/me`의 `User` 스키마 중 스플래시가 필요로 하는 것만 담는다. 스플래시는 **존재 여부**만 쓰지만, 같은 모델을 온보딩·마이페이지가 공유하므로 프로필의 최소 형태를 그대로 갖는다.

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | `String` | 서버 발급 사용자 식별자 |
| `nickname` | `String` | 닉네임 |
| `avatar` | `String?` | 아바타 식별자. 미선택 가능 |

- 스플래시는 이 모델의 필드를 읽지 않는다. `null` 여부만 FR-003·FR-004의 분기 근거로 쓴다.
- 검증 규칙(닉네임 2~15자 등)은 온보딩·마이페이지의 몫이며 이 스펙의 범위가 아니다.

### 1.2 `SplashEntry`

`ResolveSplashEntryUseCase`의 반환 타입. 스플래시가 다음에 어디로 가야 하는지를 나타내는 봉인 타입이다.

| 리프 | 의미 | 근거 |
|---|---|---|
| `SplashEntry.Onboarding` | 프로필이 없다 → 프로필 설정으로 시작하는 온보딩 | FR-003 |
| `SplashEntry.Main` | 프로필이 있다 → 직전 세션의 메인 탭 | FR-004 |

- **실패는 이 타입으로 표현하지 않는다.** 실패는 `MinoDomainException`으로 던져지고 `runCatchingDomain`이 소비한다(R-011). 세션 미확보 상태에서는 `SplashEntry`가 아예 만들어지지 않아야 FR-010(진입 차단)이 타입으로 보장된다.

---

## 2. 화면 상태 (`:feature:splash`)

### 2.1 `SplashUiState`

| 필드 | 타입 | 초기값 | 설명 | 근거 |
|---|---|---|---|---|
| `isSpinnerVisible` | `Boolean` | `false` | 로딩 스피너 노출 여부 | FR-006, FR-007, UX-005 |

- 브랜드 화면(캐릭터·워드마크·태그라인)은 **항상 노출**되므로 상태로 두지 않는다(UX-004).
- 오류 토스트는 화면에 머무르는 상태가 아니라 **일회성 표출**이므로 `SideEffect`로 다룬다(§2.3).
- 전환 역시 상태가 아니라 `SideEffect`다 — 상태로 두면 재구성마다 전환이 재발화할 수 있다.

**상태 전이**

| 시점 | 조건 | 전이 |
|---|---|---|
| 앱 실행 | — | `isSpinnerVisible = false` |
| 실행 후 3초 | 세션 미확보 | `isSpinnerVisible = true` |
| 실행 후 3초 | 세션 확보 완료 | 전이 없음 → `NavigateTo` 발행 |
| 실행 후 13초 | 세션 미확보 | `isSpinnerVisible = false` + `ShowToast(TemporaryError)` |
| 언제든 | 세션 확보 성공 | `isSpinnerVisible = false` → `NavigateTo` 발행 |

### 2.2 `SplashIntent`

| Intent | 발생 시점 | 근거 |
|---|---|---|
| `SplashIntent.Start` | 화면 최초 진입(콜드 스타트) 1회 | FR-001, FR-002 |

- 사용자 입력에서 비롯되는 Intent가 없다 — 스플래시는 터치를 소비하지 않는다(FR-005).
- 재시도는 사용자 Intent가 아니라 ViewModel 내부 루프다(FR-010, UX-001).

### 2.3 `SplashSideEffect`

| SideEffect | 페이로드 | 근거 |
|---|---|---|
| `NavigateTo` | `SplashEntry` | FR-003, FR-004 |
| `ShowToast` | `SplashToast` | FR-007, FR-008, FR-009 |

### 2.4 `SplashToast`

| 리프 | 문구 | 근거 |
|---|---|---|
| `NetworkError` | `네트워크 연결을 확인해주세요` | FR-008 |
| `TemporaryError` | `일시적인 오류가 발생했어요` | FR-007, FR-009 |

- 문구는 PRD 5.0.0 `[SCR-001]` Flow C·D가 확정한 값이다. 문자열 리소스로 `:feature:splash`가 소유한다.
- 표출 위치(하단 40dp)는 문구가 아니라 배치이므로 Screen 컴포저블이 갖는다(UX-003).
- **반복 표출 억제**: 직전 표출로부터 10초가 지나지 않으면 발행하지 않는다(UX-006). 마지막 표출 시각은 ViewModel 내부 값이며 UI 상태가 아니다.

---

## 3. 데이터 계층 (`:core:data`)

### 3.1 DTO

`UserResponse` — `GET /api/v1/users/me` 응답의 `data` 필드에 대응한다. 스웨거의 공통 래퍼 `{ "data": ... }`는 기존 네트워크 계층의 처리 방식을 따른다.

| 필드 | 타입 |
|---|---|
| `id` | `String` |
| `nickname` | `String` |
| `avatar` | `String?` |

### 3.2 Mapper

`UserResponse.toDomain(): UserProfile` — 필드 1:1 대응.

### 3.3 저장소

| 대상 | 저장 위치 | 설명 |
|---|---|---|
| 익명 세션 | **인증 제공자 SDK의 앱 프라이빗 저장소** | 앱이 직접 저장하지 않는다. 별도 캐싱은 진실 원천을 둘로 가르므로 금지다 — `anonymous-auth-session` 스펙 소유 |
| 프로필 | 캐시하지 않는다 | FR-011의 "네트워크 없이 복원"은 **세션**에 대한 것이지 프로필이 아니다 |

세션의 데이터 모델(`AnonymousSession`)과 그 수명은 이 스펙이 아니라 `anonymous-auth-session` 스펙이 소유한다. 스플래시는 `ensureSession()`의 정상 반환 여부만 쓰고 `userId`를 읽지 않는다.

---

## 4. 미확정 항목이 데이터 모델에 남긴 구멍

| ID | 구멍 | 막힌 요구사항 |
|---|---|---|
| **TBD-P2** | 프로필 미생성을 나타내는 응답이 정의되지 않아 `UserProfile?`의 `null` 조건을 확정할 수 없다 | FR-003 |

이 항목이 닫히기 전에는 `UserRepositoryImpl`을 구현할 수 없다. `:core:domain`의 인터페이스와 `:feature:splash`의 화면은 영향받지 않는다.

> **TBD-P1(세션 발급 수단)은 plan 2.0.0에서 해소되었다.** → [research.md R-010](./research.md)
