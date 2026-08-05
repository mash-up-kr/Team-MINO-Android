# DI 바인딩 규칙

## DI 바인딩 소유

인터페이스 구현을 가진 모듈이 자신의 `di/` 패키지에서 그 바인딩을 소유한다. 구현체는 `internal`로 닫고 `@Module @InstallIn(...) internal abstract class` + `@Binds`로만 노출한다.

`:app`은 `@HiltAndroidApp`으로 그래프를 조립할 뿐 바인딩을 두지 않는다.

스코프는 컴포넌트를 따른다. 앱 전역 싱글턴은 `SingletonComponent`, Activity 수명에 묶인 전환 계약(`Launcher`)은 `ActivityRetainedComponent` + `@ActivityRetainedScoped`.

## 금지 규칙

- `:feature:*`가 `:core:data`를 직접 의존 → domain 인터페이스만 알고, 구현 바인딩은 구현을 소유한 모듈의 DI에서 (위 `DI 바인딩 소유`)
- 구현 바인딩을 `:app`에 모으기 → 구현체를 `public`으로 열게 되어 모듈 경계가 무너진다

> 위 규칙을 검사하는 장치는 아직 없다. `lint.xml`에 모듈 의존 방향 룰이 없고 Gradle 의존 검증도 도입하지 않아, 경계는 문서와 리뷰에만 의존한다.

## 배경

이 규칙이 정해진 배경은 [ADR: Hilt 구현 바인딩은 구현을 소유한 모듈이 갖고, `:app`은 그래프 조립만 한다](../adr/2026-08-02-di-binding-ownership.md) 참조.
