# Hilt 구현 바인딩은 구현을 소유한 모듈이 갖고, `:app`은 그래프 조립만 한다

- **상태**: Accepted
- **작성일**: 2026-08-02
- **작성자**: Jaesung Lee

## 컨텍스트

`docs/architecture/modularization.md`와 `.claude/skills/plan-gen/plan-format.md`가 "구현 바인딩은 `:app`의 DI에서"라고 적고 있었다. 이 서술을 근거로 plan-gen이 만드는 plan.md도 새 기능의 바인딩 위치를 `:app`으로 지정할 참이었다.

실제 코드를 확인하니 `:app`에는 Hilt 모듈이 하나도 없었다. `MinoApplication`의 `@HiltAndroidApp`이 전부이고, 바인딩은 전부 구현을 소유한 모듈이 자신의 `di/` 패키지에서 갖고 있었다. 게다가 구현체(`RepositoryImpl`·`DataSourceImpl`·`LauncherImpl`)와 DI 모듈이 모두 `internal`이라 `:app`에서 바인딩하는 것은 문법적으로 불가능했다.

즉 문서만 낡은 상태였고, 어느 쪽을 규칙으로 확정할지 정해야 했다.

## 결정

인터페이스 구현을 가진 모듈이 자신의 `di/` 패키지에서 그 바인딩을 소유한다. 구현체는 `internal`로 닫고 `@Module @InstallIn(...) internal abstract class` + `@Binds`로만 노출한다.

`:app`은 `@HiltAndroidApp`으로 그래프를 조립할 뿐 바인딩을 두지 않는다.

스코프는 컴포넌트를 따른다. 앱 전역 싱글턴은 `SingletonComponent`, Activity 수명에 묶인 전환 계약(`Launcher`)은 `ActivityRetainedComponent` + `@ActivityRetainedScoped`.

## 근거

`:app`이 바인딩을 소유하려면 모든 구현체를 `public`으로 열어야 한다. 그 순간 `:core:data`가 "외부 진입점은 `core:domain` 인터페이스뿐"이라는 전제를 잃고, feature가 구현체를 직접 참조할 수 있게 되어 모듈 경계가 무너진다. 구현체를 `internal`로 유지하는 것과 `:app` 바인딩은 양립할 수 없다.

바인딩을 구현 옆에 두면 구현을 바꿀 때 수정 범위가 그 모듈 안에서 닫힌다. 분석 SDK 교체가 `core:analytics` 안에서 끝나는 것이 그 예다.

`:app`에 모든 바인딩이 모이면 모듈이 늘어날수록 `:app`이 모든 구현 모듈을 의존해야 하고, 빌드 그래프 상 `:app`이 변경 전파의 병목이 된다.

## 결과

- 새 인터페이스와 구현을 추가할 때 바인딩 모듈은 구현이 있는 모듈의 `di/` 패키지에 만든다. 구현체를 `public`으로 여는 방식은 금지한다.
- 규칙의 단일 출처는 [`dependency-injection.md`](../conventions/dependency-injection.md)이며, "구현 바인딩을 `:app`에 모으기" 금지 규칙도 함께 옮겨졌다.
- `plan-format.md`의 `:app DI` 서술 2곳이 교정되어, plan.md가 새 기능의 바인딩 위치를 잘못 지정하지 않는다.
- 코드 변경은 없다. 기존 구조가 이미 이 결정과 일치했고, 이번 작업은 문서를 코드에 맞춘 것이다.
- 이 건이 "문서가 코드보다 낡아 설계 문서가 틀린 값을 퍼뜨릴 뻔한" 사례였으므로, `plan-format.md` 작성 규칙에 "문서와 코드가 충돌하면 코드를 사실로 삼고, 어긋난 문서 위치를 plan의 미해결 섹션에 이월한다"를 함께 추가했다.
- 이 경계를 검사하는 자동 장치는 여전히 없다. 모듈 의존 검증 도입은 `modularization.md`가 이미 밝힌 대로 별도 과제다.

## 고려한 대안

**모든 구현 바인딩을 `:app`의 DI로 모은다** (문서가 주장하던 방식). DI 그래프 전체를 한곳에서 조망할 수 있다는 장점이 있으나, 구현체를 전부 `public`으로 열어야 해서 모듈 경계와 정면으로 충돌한다. `:app`이 모든 구현 모듈을 의존하게 되는 비용도 크다. 배제했다.
