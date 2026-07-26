# 에러 처리는 도메인 예외 매핑 + CEH 안전망의 2단 구조로 한다

- **상태**: Accepted
- **작성일**: 2026-07-25
- **작성자**: Jaesung Lee

## 컨텍스트

레포 전체에 에러 처리 인프라가 없었다 (2026-07 조사 기준): `runCatching`·`try/catch`·`CoroutineExceptionHandler`·커스텀 에러 타입 사용처 0건. Ktor `HttpClient`가 `expectSuccess = true`라 non-2xx 응답 시 `ResponseException`을 던지지만 어디서도 잡지 않아 그대로 crash했다. 이슈 #72에서 규약을 설계했다.

규약 전문은 [`docs/conventions/error_handling.md`](../../docs/conventions/error_handling.md)가 단일 출처다. 이 ADR은 결정 배경과 기각한 대안만 기록한다.

## 결정

2단 분류를 골격으로 한다. 분류 기준은 "data 레이어가 예상할 수 있는 실패인가"다.

1. 예상 가능한 실패 → data 레이어(`HttpResponseValidator` 전역)에서 `MinoDomainException`(sealed, 초기 리프 `Network`·`Http(code)`)으로 매핑 → ViewModel이 `runCatchingDomain`/`onDomainFailure`로 소비 → State(주 데이터 로드 실패) 또는 인스턴스별 채널 `DomainErrorEmitter`(액션 일회성 실패)로 UI 전달
2. 그 외(버그) → 잡지 않고 전파 → `launchSafely`의 CEH가 리포팅 + 전역 `UncaughtErrorHandler` → Activity 루트 `CollectUncaughtError` 수집. 화면 상태 복구는 하지 않는다

## 근거

- 매핑 경계를 "API 에러"가 아닌 "예상 가능성"으로 잡아야 wirebarley 함정 ①(네트워크 단절이 버그 취급 → 화면 무반응)이 재발하지 않는다.
- 매핑을 `HttpResponseValidator` 한 곳에서 전역 수행하면 새 API 추가 시 매핑 누락이 구조적으로 불가능하다. 데이터소스별 catch는 감싸기를 잊는 순간 규약이 깨지고 준수가 리뷰에 의존한다.
- `runCatchingDomain`이 `MinoDomainException`만 잡아야 "도메인 예외가 아닌 것은 CEH로 흘러간다"가 성립하고, `CancellationException`을 삼키지 않아 코루틴 취소가 깨지지 않는다.
- CEH의 동작(리포팅 + 사용자 안내)을 규약으로 정의해 wirebarley 함정 ②(버그가 조용히 묻힘)를 막는다.
- sealed 계층으로 wirebarley 함정 ③(매직넘버 분기)을 막고, 소비 `when`이 타입으로 분기한다.

## 결과

- 신설 모듈 `core:error-handling`(Kotlin JVM — `core:domain`이 의존 가능해야 하므로 Android Library가 아님)에 예외 계층과 소비 헬퍼를 둔다. 매핑은 `core:data`, CEH·`launchSafely`는 `core:common:android`, 수집 컴포저블은 `core:common:ui`.
- release에서 버그로 인한 로딩 고착 화면이 발생할 수 있음을 수용한다(복구하지 않음). 구조적 예방책으로 UiState `isLoading` 분리형 표준화가 [UiState isLoading 분리형 ADR](2026-07-25-uistate-isloading-over-sealed-status.md)(`Proposed`)로 제안되어 있다.
- 이연: 경계 사례 분류(첫 적용 화면에서), Crashlytics 도입(별도 이슈), 리뷰 규약의 lint 전환(후속 이슈), 비즈니스 에러 리프 확장(API 스펙 확정 시).

## 고려한 대안

- **Repository `Result` 반환형 / 전용 sealed `DomainResult`**: `Result` 반환형은 현행 "도메인 모델 직접 반환" 시그니처를 깨고, `DomainResult`는 표준 `onFailure` 우회를 구조적으로 막을 수 있으나 표준 `Result` API(`map`·`getOrNull` 등)를 포기하고 연산자를 직접 만들어야 하는 비용 대비 이득이 작아 기각. 대신 `onDomainFailure` 확장 + "표준 `onFailure` 금지" 리뷰 규약으로 타입 복원.
- **UseCase에서 예외 → `Result` 변환** (wirebarley 방식): UseCase 계층이 없으므로 기각. 변환 지점은 `runCatchingDomain` 한 곳.
- **데이터소스별 catch 매핑**: 감싸기를 잊으면 Ktor 예외가 유출되어 예상 가능한 실패가 CEH로 오분류. 전역 validator로 대체하고, 엔드포인트별 특수 정책만 지역 catch 병용.
- **`Unknown` 리프**: 탈출구 리프가 있으면 버그가 도메인 예외로 오분류되어 "알 수 없는 오류"로 조용히 소비됨(wirebarley 함정 ②의 재발). 매핑은 화이트리스트로 닫고 나머지는 rethrow.
- **`NotFound` 리프**: HTTP 상태는 `Http(code)`로 통일해 매핑 결정성(같은 응답 → 항상 같은 리프) 확보. 404 특수 정책은 지역 catch로.
- **`Serialization` 리프**: 2026-07-19 채택(captive portal·서버 선배포 등 재시도 가능 경로 실재) 후 **2026-07-24 번복** — 서버 계약 위반(버그)으로 보고 CEH행. 해당 경로가 "알 수 없는 오류"로 처리되는 트레이드오프를 인지하고 수용했으며, 실사용에서 문제로 확인되면 재논의. 번복으로 `UncaughtErrorReporter`의 non-fatal 경로(직렬화 non-fatal 리포팅용)도 제거되어 fatal만 정의.
- **CEH를 `MviContainer`에 통합** (스코프 전달 + `launch` 헬퍼): Kotlin 수퍼타입 위임식은 `this` 참조가 불가능해 현행 `by mviContainer(...)` 관용구로는 `viewModelScope`를 전달할 수 없고, 컨테이너가 스코프를 자체 소유하는 우회는 "조립을 잊으면 코루틴 누수"라는 더 나쁜 리뷰 의존을 만든다. `ViewModel.launchSafely` 확장으로 대체.
- **`CollectUncaughtError` Route별 수집**: 2026-07-19 채택 후 **2026-07-25 Activity 루트 수집으로 변경** — "모든 Route가 선언" 규약은 리뷰 의존이 크고 누락 시 이벤트가 버퍼에 고인다. resumed Activity 최대 1개 보장으로 이중 수신 없음은 동일하게 성립하며 선언 지점이 feature Activity 수로 줄어든다.
- **도메인 에러 일회성 안내를 기존 MVI SideEffect 채널로**: feature마다 에러용 SideEffect 타입을 중복 정의하게 되고 문구 매핑이 ViewModel로 끌려온다. 전용 `DomainErrorEmitter`(인스턴스별)를 신설해 Route가 리프를 직접 수집·매핑. 전역 버스안은 백그라운드 ViewModel의 실패가 다른 화면에 오배달되어 기각.
- **CEH 도달 시 화면 상태 복구**: 화면별 복구 콜백은 호출부마다 심어야 해 리뷰 의존이 재발하고 sealed 단일 상태 설계에서는 되돌릴 목적지가 없음. feature 재시작(finish → 재실행)은 스낵바로 충분한 상황에도 과잉 대응(`recreate()`는 ViewModelStore를 보존해 불성립). 복구하지 않기로 하고, 운영에서 고착이 실제 문제로 확인되면 feature 재시작을 재검토.
