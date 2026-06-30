# 0001. 직렬화 DTO의 IDE opt-in 경고에 `@OptIn` 우회를 쓰지 않는다

- **상태**: Accepted
- **날짜**: 2026-06-29
- **관련**: 이슈 #47

## 맥락

`@Serializable` DTO(`GithubRepoResponse`)에서 IDE(Android Studio/IntelliJ)가 아래 에러를 빨간 줄로 표시할 수 있다.

```
This declaration is opt-in and its usage should be marked with
@kotlinx.serialization.InternalSerializationApi or @OptIn(...)
```

이는 **KSP가 생성한 serializer를 IDE 코드 분석이 인식하지 못해** 생기는 알려진 false-positive다([KTIJ-31549](https://youtrack.jetbrains.com/issue/KTIJ-31549)). 실제 컴파일과 런타임은 정상이다.

## 결정

IDE의 quick-fix가 권하는 `@OptIn(InternalSerializationApi::class)`를 코드에 추가하지 않는다. 실제로는 생성된 serializer를 쓰는데 내부 API 사용을 허용하는 어노테이션을 박는 잘못된 해결이며, 진짜 원인(IDE 인덱싱)을 가리기 때문이다.

## 해소 방법

Android Studio에서 *Sync Project with Gradle Files*. 그래도 남으면 *Invalidate Caches → Restart*.
