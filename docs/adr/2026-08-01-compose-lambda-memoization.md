# 컴포저블 안의 람다는 수동 `remember`로 감싸지 않는다

- **상태**: Accepted
- **작성일**: 2026-08-01
- **작성자**: Jaesung Lee

## 컨텍스트

`MainActivity`가 `MainShell`에 넘기는 전환 콜백을 `remember`로 감싸 내려보낸 코드를 두고 PR #108 리뷰에서 논의가 있었다.

감싼 쪽의 근거는 이랬다 — 콜백이 Activity와 `ActivityResultLauncher`를 캡처하는데 둘 다 unstable 타입이라 컴파일러가 memoize하지 못하고, 그 콜백을 캡처한 `NavHost`의 그래프 빌더 람다까지 identity가 리컴포지션마다 바뀌어 `createGraph`가 다시 돌 것이다.

리뷰에서 이 전제가 strong skipping 도입 이전의 동작이라는 지적이 나왔다. 확인 결과 프로젝트는 Kotlin `2.2.10`을 쓰고 Compose 컴파일러 플러그인이 그 버전에 연동되어 있으며, 저장소 어디에도 `composeCompiler { }` 블록이나 strong skipping을 끄는 설정이 없다. 기본값인 활성 상태다.

같은 판단이 반복될 여지가 있어 기록한다. "unstable 캡처 = memoize 안 됨"은 널리 퍼진 오래된 모델이고, 이번처럼 근거가 그럴듯해 보이면 리뷰에서 걸러지지 않을 수 있다.

## 결정

**컴포저블 함수 안에서 정의되는 람다를 수동 `remember`로 감싸지 않는다.** 람다 memoization은 컴파일러에 맡기고, 콜백은 파라미터에 직접 전달한다.

```kotlin
// 이렇게 쓴다
MainShell(
    onNavigateToSample = {
        sampleLauncher.launch(this) { putExtra(EXTRA_SAMPLE_FROM_HOME, true) }
    },
)
```

이 결정은 **람다에만** 적용된다. 상태 홀더(`SnackbarHostState`·`MutableInteractionSource`·`mutableStateOf`)나 비용이 큰 객체를 `remember`로 붙잡는 것은 그대로 유지한다 — 그쪽은 컴파일러가 대신 해주지 않는다.

## 근거

- **strong skipping이 켜져 있으면 컴포저블 안의 모든 람다가 자동으로 memoize된다.** 캡처가 unstable이어도 예외가 아니고, 비교 규칙이 인스턴스 동등성(`===`)으로 바뀔 뿐이다. Activity·`ActivityResultLauncher`처럼 컴포지션 동안 인스턴스가 고정인 캡처는 항상 같다고 판정되므로 같은 람다 인스턴스가 재사용된다.
- **키 없는 `remember { }`는 자동 memoization과 동작이 다르고, 더 위험하다.** 자동 wrap은 캡처를 키로 쓰지만 키 없는 `remember`는 최초 1회 생성 후 캡처가 바뀌어도 재생성하지 않는다. 리컴포지션마다 바뀌는 값을 캡처하는 람다에 같은 패턴을 복사하면 stale 콜백이 남는다. 불필요한 것을 넘어 잘못된 선례가 된다.
- 수동 `remember`는 지역 변수와 들여쓰기를 늘려 호출부를 읽기 어렵게 만든다. 얻는 것이 없는 노이즈다.

## 결과

- `/review` 커맨드의 Compose 리뷰 기준에서 "`remember` 누락"을 지적할 때 람다는 제외한다.
- 컴포저블을 쓰는 사람이 닿는 자리에도 규칙을 둔다 — `core/design-system/README.md`(Compose 작성 규칙)와 `docs/architecture/feature-navigation.md`(전환 콜백). 세 곳 모두 규칙 한 줄 + 이 문서 링크로 두고 근거를 복사하지 않는다.
- 실제 동작을 확인해야 할 상황이 오면 Compose 컴파일러 안정성 리포트(`reportsDestination`)로 skippable·stability를 보고, 필요하면 생성 코드까지 확인한다. 리포트가 "이 람다가 memoize됐다"를 직접 알려주지는 않는다. 이번에는 설정 확인과 공식 문서 근거로 판단했고 리포트는 생성하지 않았다.
- strong skipping을 끄게 되면 이 결정의 전제가 무너진다. 끄는 설정을 추가할 때 이 문서를 함께 갱신한다.

## 고려한 대안

- **수동 `remember`로 감싸기** — PR #108의 원안. 전제가 성립하지 않아 폐기. 게다가 키 없는 형태라 자동 memoization보다 나쁘게 동작할 수 있다.
- **콜백을 Activity의 멤버 함수 참조(`::navigateToSample`)로 전달** — 호출부는 짧아지지만 바운드 참조도 호출마다 새 인스턴스가 만들어질 수 있어 보장이 되지 않는다. 자동 memoization이 이미 처리하는 문제를 다른 문법으로 우회하는 것에 가깝다. 폐기.
- **람다를 Activity의 프로퍼티로 hoist** — memoization 문제 자체가 사라지지만, UI 콜백이 컴포지션 밖 필드로 나가 화면 구성과 Activity 상태가 뒤섞인다. 폐기.
- **컴파일러 리포트를 상시 켜두고 확인** — 빌드에 상시 비용이 붙고, 리포트가 답하는 것(skippable·stability)과 알고 싶은 것(이 람다가 memoize됐는가)이 정확히 겹치지도 않는다. 필요할 때만 켜는 쪽으로 두고 폐기.

## 참고

- [Strong skipping mode — 람다 메모이제이션](https://developer.android.com/develop/ui/compose/performance/stability/strongskipping?hl=ko#lambda-memoization)
