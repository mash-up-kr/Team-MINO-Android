# 0001. Github 네트워크 레이어를 Ktorfit에서 Ktor로 전환

- **상태**: Accepted
- **날짜**: 2026-06-29
- **관련**: 이슈 #47, 후속 작업 #40 (DataSource·Repository)

## 맥락

`core:data`의 Github 네트워크 레이어를 처음에는 **Ktorfit**(Retrofit 스타일의 어노테이션 기반 Ktor 래퍼)으로 구현했다. API는 `@GET`/`@Path`/`@Query` 어노테이션이 달린 인터페이스로 정의하고, `Ktorfit.create<GithubApiService>()`로 구현체를 얻는 방식이었다.

런타임에 다음 크래시가 발생했다.

```
java.lang.ClassCastException: ..._GithubApiServiceProvider cannot be cast to de.jensklingenberg.ktorfit.Ktorfit
```

원인은 Ktorfit의 `create<T>()`가 **컴파일러 플러그인**에 의존해 인터페이스의 구현 클래스를 찾는 데 있다. 플러그인이 구현체를 제대로 연결하지 못하면 생성된 `_GithubApiServiceProvider`가 그대로 반환되어 캐스팅에서 터진다. Ktorfit 공식 문서도 이 플러그인 의존을 알려진 한계로 명시하며, 플러그인을 거치지 않는 생성 확장 함수(`createGithubApiService()`)를 우회책으로 제시한다. 즉 빌드·런타임 안정성이 컴파일러 플러그인의 동작에 묶여 있었다.

이 레이어는 이후 DataSource·Repository(#40)가 올라설 토대이므로, 토대 단계에서 의존성을 단순화할 필요가 있었다.

## 결정

Ktorfit을 제거하고 **순수 Ktor `HttpClient` 직접 호출** 방식으로 전환한다.

- API 정의는 어노테이션 인터페이스 대신, `HttpClient`를 주입받는 **일반 클래스**(`GithubApiService`)로 작성한다.
  ```kotlin
  class GithubApiService @Inject constructor(private val client: HttpClient) {
      suspend fun getUserRepos(user: String, perPage: Int = 30): List<GithubRepoResponse> =
          client.get("users/$user/repos") { parameter("per_page", perPage) }.body()
  }
  ```
- `baseUrl`은 서비스마다 박지 않고 `HttpClient`의 `defaultRequest`로 **한 곳에서** 설정한다.
- 직렬화는 기존대로 `kotlinx.serialization` + Ktor `ContentNegotiation`을 유지한다.
- Ktorfit Gradle 플러그인·라이브러리 의존성, 버전 카탈로그 항목을 모두 제거한다.

## 결과

**장점**

- 컴파일러 플러그인 의존이 사라져 빌드·런타임이 안정적이다. `ClassCastException` 재발 여지 없음.
- 요청 흐름이 코드에 명시적으로 드러나 추적이 쉽다.
- 의존성 그래프가 단순해진다(Ktorfit 제거).

**단점 / 트레이드오프**

- Retrofit/Ktorfit식 선언적 API 대비 호출 코드의 보일러플레이트가 약간 늘어난다. 엔드포인트가 많아지면 공통 헬퍼로 정리할 것.

**알려진 이슈 — IDE false-positive (KTIJ-31549)**

`@Serializable` DTO(`GithubRepoResponse`)에서 IDE(Android Studio/IntelliJ)가 아래 에러를 빨간 줄로 표시할 수 있다.

```
This declaration is opt-in and its usage should be marked with
@kotlinx.serialization.InternalSerializationApi or @OptIn(...)
```

이는 **KSP가 생성한 serializer를 IDE 코드 분석이 인식하지 못해** 생기는 알려진 false-positive다([KTIJ-31549](https://youtrack.jetbrains.com/issue/KTIJ-31549)). 실제 컴파일과 런타임은 정상이며(`./gradlew :core:data:compileQaDebugKotlin` 통과로 확인), Ktor 전환 자체와는 무관한 KSP/IDE 인덱싱 문제다.

> **하지 말 것**: IDE의 quick-fix가 권하는 `@OptIn(InternalSerializationApi::class)`를 코드에 추가하지 않는다. 실제로는 생성된 serializer를 쓰는데 내부 API 사용을 허용하는 어노테이션을 박는 잘못된 해결이며, 진짜 원인(IDE 인덱싱)을 가린다.
>
> **해소 방법**: Android Studio에서 *Sync Project with Gradle Files*. 그래도 남으면 *Invalidate Caches → Restart*.
