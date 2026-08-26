# 도메인 예외 매핑 지점은 원천마다 하나씩 두고, 인증 실패용 `Auth` 리프를 추가한다

- **상태**: Accepted
- **작성일**: 2026-08-22
- **작성자**: Jaesung Lee

## 컨텍스트

[`error_handling.md`](../conventions/error_handling.md) §2·§3과 [`core/error-handling/README.md`](../../core/error-handling/README.md) §4는 `:core:data`의 도메인 예외 매핑 지점을 **Ktor `HttpResponseValidator` 하나**로 전제하고 쓰여 있었다. "지점이 하나여서 누락이 구조적으로 불가능하다"가 그 규약이 매핑에 요구하는 성질이었고, 새 리프를 추가할 때도 그 validator의 화이트리스트와 짝으로 추가하도록 규정했다.

이슈 #176이 Firebase Authentication을 데이터 출처로 들이면서 이 전제가 깨졌다. Firebase Auth는 `HttpClient`를 거치지 않으므로 validator가 닿지 않는다. 그런데 요구사항은 두 가지였다 — 세션 확보·신원 증명 발급 실패를 **기존 도메인 예외 체계로** 전파할 것(별도 에러 처리 경로를 만들지 말 것), 그리고 실패를 **연결 문제로 인한 실패**와 **그 밖의 실패** 두 갈래로 구분해 서로 다른 안내를 노출할 것.

기존 리프는 `Network`와 `Http(code)` 둘뿐이었다. "그 밖의 실패"는 HTTP 응답이 아니라서 `Http(code)`로 표현하면 코드 자리에 넣을 값이 없다.

## 결정

**매핑 지점의 개수를 하나로 고정하지 않고, 원천마다 하나씩 둔다.** 규약이 요구하는 성질은 "지점이 하나일 것"이 아니라 **"그 원천의 모든 호출이 통과하는 지점이 하나이고, 그 지점이 화이트리스트 열거일 것"**으로 다시 읽는다.

- Firebase 예외 → `MinoDomainException` 매핑은 **`Task` → suspend 변환 지점 한 곳**에서 수행한다(`:core:data`의 `auth/extension/`). 모든 Firebase 호출이 이 변환을 통과하므로, 이 지점이 그 원천에 대해 Ktor validator와 같은 역할을 한다.
- 매핑은 화이트리스트 열거다. 연결 실패는 `Network`로, 인증 제공자가 발급에 실패한 경우(호출 한도 초과·인증 구성 오류 등)는 신설 리프 `Auth`로 보낸다. **열거 밖 예외는 매핑하지 않고 rethrow**해 CEH로 보내며, `CancellationException`도 매핑하지 않는다. 열거를 넓히는 상위 타입 분기를 추가하지 않는다.
- `MinoDomainException`에 `class Auth(cause: Throwable)` 리프를 추가한다. 연결 실패 갈래는 기존 `Network`를 재사용한다.
- 데이터소스별 지역 `try/catch`는 계속 금지한다. 이 결정이 늘리는 것은 **원천의 수만큼의 관문**이지, 매핑을 흩뿌릴 자유가 아니다.

## 근거

Firebase Auth 호출은 반드시 `Task`를 반환하고, 데이터 레이어 계약은 전부 `suspend`다. 따라서 변환 지점이 구조적으로 강제되고, 그 지점을 통과하지 않고 Firebase를 호출하는 경로가 존재할 수 없다. 규약이 validator에 기대했던 "누락이 구조적으로 불가능하다"는 성질이 그대로 성립한다.

대안이었던 `AnonymousAuthRepositoryImpl`의 지역 `try/catch`는 Firebase 호출 지점이 늘어날 때마다 매핑을 따라 붙여야 하고, 빠뜨려도 컴파일이 통과한다. 규약이 데이터소스별 catch를 기각한 이유가 그대로 적용된다.

리프를 하나 더 두는 것이 "이 기능 전용 에러 타입"을 만드는 것과 다르다는 점이 중요하다. 금지 대상은 `MinoDomainException` **밖**의 별도 실패 모델(전용 sealed 결과 타입·전용 예외 계층)이고, 리프 추가는 [`core/error-handling/README.md`](../../core/error-handling/README.md) §4가 정식 확장으로 규정한 동작이다. `Auth`가 §4가 금지하는 탈출구(`Unknown`류)가 아닌 이유는 매핑이 열거된 예외에만 한정되기 때문이다.

## 결과

- [`error_handling.md`](../conventions/error_handling.md) §2·§3, [`core/error-handling/README.md`](../../core/error-handling/README.md) §4, [`core/data/README.md`](../../core/data/README.md) §3·§5의 서술을 이 결정에 맞춰 갱신해야 한다. 리프를 "validator 화이트리스트와 짝으로 추가한다"는 규칙에서 **짝의 대상이 둘 이상**이 된다.
- 앞으로 `HttpClient`를 거치지 않는 원천(다른 SDK·시스템 API)이 추가되면 같은 형태를 따른다 — 그 원천의 모든 호출이 통과하는 변환·접근 지점을 하나 만들고, 매핑을 그 지점에만 둔다. 원천이 늘 때마다 이 판례를 참조하면 되고, 규약 본문을 다시 논의할 필요가 없다.
- 이 결정은 [에러 처리는 도메인 예외 매핑 + CEH 안전망의 2단 구조로 한다](2026-07-25-error-handling-two-tier-convention.md)를 **뒤집지 않고 확장한다.** 2단 구조도, 화이트리스트 원칙도, 프로그래머 버그를 CEH로 보내는 규칙도 그대로다. 바뀐 것은 "지점이 하나"라는 전제뿐이다.
- `MinoDomainException`은 `sealed`이고 소비 `when`에 `else`가 허용되므로 리프 추가가 기존 소비 코드를 깨뜨리지 않는다.
- `Auth`와 `Network`를 실제로 구분해 표현하는 곳은 진입 화면 하나다. 두 리프의 구분이 "연결 확인 요청"과 "일시적 오류 안내"라는 두 안내의 유일한 근거이므로, 리프를 합치면 요구사항이 무너진다.
- 인증 제공자 예외 중 어느 클래스가 어느 갈래에 속하는지의 **목록**은 구현 시점에 확정한다. 이 ADR이 소유하는 것은 분류 기준이다.

## 고려한 대안

**`AnonymousAuthRepositoryImpl`에서 지역 `try/catch`로 매핑한다.** 매핑 지점을 늘리지 않는다는 규약의 문구는 지키지만 실질을 잃는다. 호출 지점마다 매핑을 반복해야 하고 누락이 컴파일에 걸리지 않는다.

**Firebase 예외를 그대로 도메인까지 전파한다.** `:core:domain`이 Firebase 타입을 알게 되어 레이어 경계가 무너지고, "기존 도메인 예외 체계로 전파"라는 요구도 어긴다.

**`Http(code)`를 재사용해 "그 밖의 실패"를 표현한다.** HTTP 응답이 아니므로 코드 자리에 넣을 값이 없고, 화면이 두 갈래를 가르는 근거도 사라진다.

**리프를 늘리지 않고 `Network` 하나로 통합한다.** 실패 2종 구분이라는 요구사항 자체를 포기하는 선택이다.

**`MinoDomainException` 밖에 전용 결과 타입(`AuthResult` 등)을 만든다.** "이 기능 전용 에러 타입을 새로 만들지 않는다"는 요구를 정면으로 어긴다.

**탈출구 리프(`Unknown`류)를 만들어 열거 밖 예외까지 전부 도메인 예외로 흡수한다.** 진입 화면이 CEH로 새는 실패를 표시할 수 없다는 문제(화면이 안내도 재시도도 없이 멈춘 채 남을 수 있다)를 풀기 위해 검토했으나, 버그가 조용히 소비되어 `core/error-handling/README.md` §4를 위반한다. 대신 호출자 계약이 "재시도 루프를 도메인 예외 수신에만 종속시키지 않는다"를 규정하는 것으로 해결했다 — [익명 세션 확보의 재시도·지연 판정은 호출 화면이 소유한다](2026-08-22-session-retry-owned-by-caller.md) 참조.
