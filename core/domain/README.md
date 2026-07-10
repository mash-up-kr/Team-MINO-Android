# core:domain

MinoAndroid의 **비즈니스 로직 중심** 모듈. 도메인 모델·Repository 인터페이스·UseCase를 여기 두고, ViewModel과 데이터 계층 사이의 경계를 명확히 한다.

> 모듈 책임·경계·의존 방향(레이어 그래프)은 [`docs/architecture/modularization.md`](../../docs/architecture/modularization.md)를 단일 출처로 한다. 이 문서는 이 모듈의 **API·사용법·확장 규칙**만 다룬다.

---

## 1. 개요

| | |
|---|---|
| **무엇을** | 도메인 모델(Entity·VO)·Repository 인터페이스·UseCase를 제공한다. |
| **빌드 타입** | Kotlin JVM (`mino.kotlin.jvm`) |

> [!IMPORTANT]
> 이 모듈은 Kotlin JVM이라 **Android SDK에 의존할 수 없다.** Android 타입(`Context`·`Uri`·Compose 등)을 둘 수 없으며, 덕분에 JVM 단위 테스트를 Android 환경 없이 실행할 수 있다. 모듈 경계 판단은 [`modularization.md`](../../docs/architecture/modularization.md) 참조.

---

## 2. 레이어 흐름

```mermaid
flowchart TD
    VM["ViewModel\n(feature:x:impl)"]
    UC["UseCase\n(core:domain/usecase)"]
    RI["Repository 인터페이스\n(core:domain/repository)"]
    Impl["RepositoryImpl\n(core:data)"]
    DS["DataSource · DTO · Mapper\n(core:data)"]

    VM -->|"비즈니스 규칙 있을 때"| UC
    VM -->|"단순 조회 시 직접"| RI
    UC --> RI
    RI -.->|"Hilt 주입"| Impl
    Impl --> DS
```

ViewModel은 UseCase 또는 Repository 인터페이스만 알고, 구현체(`RepositoryImpl`)는 알지 못한다. RepositoryImpl과 Hilt 바인딩은 `:core:data`에 두고, `:app`은 최종 DI 그래프를 조립한다.

---

## 3. 디렉토리 구조

```
core/domain/src/main/kotlin/team/mino/core/domain/
├── model/          # 도메인 모델 (Entity, VO)
├── repository/     # Repository 인터페이스
└── usecase/        # UseCase (필요한 경우에만)
```

| 디렉터리 | 배치 기준 |
|---|---|
| `model/` | 비즈니스 개념을 표현하는 순수 Kotlin 데이터 타입. DTO·Room Entity 아님. `enum`·`sealed class`도 비즈니스 개념이면 함께 둔다. 파일이 늘어나면 `model/enum`·`model/state` 등으로 하위 분리 가능 |
| `repository/` | 데이터 접근의 계약(인터페이스). 구현체는 `:core:data`에 위치 |
| `usecase/` | 여러 Repository 조합·비즈니스 규칙·재사용 행위만 위치. 기준은 4장 참조 |

---

## 4. UseCase 생성·생략 기준

비즈니스 규칙이 존재한다면 UseCase로 분리한다. ViewModel이 Repository를 직접 호출하는 것은 아래 조건을 **모두** 만족하는 단순 조회로 한정한다.

### ViewModel의 Repository 직접 호출 예시

| 조건 | 설명 |
|---|---|
| 단일 API | 화면에 필요한 데이터를 API 하나로 모두 받는다 |
| 단순 표시 | 응답 데이터를 그대로(또는 단순 변환만) 화면에 보여준다 |
| 재사용 없음 | 여러 화면에서 공통으로 재사용할 행위가 아니다 |
| 비즈니스 규칙 없음 | 정렬·필터링·권한 판단·상태 전이 등 어떤 비즈니스 판단도 없다 |

> `MviContainer` · `UiState` · `SideEffect` 등 MVI 기반 타입은 [`core/common/android/README.md — 2. 핵심 API`](../../core/common/android/README.md)가 단일 출처다.

### UseCase를 작성하는 경우

위 조건을 하나라도 만족하지 않으면 UseCase를 `core:domain/usecase`에 작성한다. 비즈니스 규칙의 종류와 무관하게 ViewModel에 두지 않는다.

> UseCase가 주입받는 Repository는 `core:domain/repository`의 인터페이스이며, 실제 구현체(`RepositoryImpl`)는 `core:data`에 있고 Hilt가 런타임에 바인딩한다.

아래는 참고용 예시다. 이 목록에 없는 비즈니스 규칙도 UseCase로 분리한다.

| 예시 유형 | 구체적 예 |
|---|---|
| 여러 Repository/API 조합 | 프로필 + 팔로워 수 + 게시물 수를 합쳐 대시보드 구성 |
| 정렬·필터링 | 공지사항을 고정 → 최신순으로 정렬 |
| 권한 판단 | 로그인 상태·역할에 따라 메뉴 노출 여부 결정 |
| 상태 전이 | 주문 상태가 특정 단계일 때만 취소 가능 판단 |
| 여러 화면에서 재사용하는 행위 | 알림 읽음 처리를 피드·마이페이지·상세 세 곳에서 공통 사용 |

### 판단 흐름

```
위 조건 4가지를 모두 만족하는가?
    YES → ViewModel이 Repository 직접 호출
    NO  → UseCase 작성 (core:domain/usecase)
```

---

## 5. 확장 규칙 — 어디에 둘지 결정

| 패키지 | 두는 것 | 작성 규칙 |
|---|---|---|
| `model/` | 비즈니스 개념을 표현하는 순수 Kotlin 타입. `data class`·`enum`·`sealed class` | 파일명은 비즈니스 개념 이름 그대로 (`Profile.kt`, `PostStatus.kt`) |
| `repository/` | 데이터 접근 계약 인터페이스. 반환 타입은 도메인 모델만, `suspend`·`Flow` 기반 | 이름은 `XxxRepository`. 구현체(`RepositoryImpl`)·DTO·Mapper는 `core:data`에 위치 |
| `usecase/` | 비즈니스 규칙·Repository 조합·재사용 행위 | 이름은 `XxxUseCase`. public 함수는 `operator fun invoke(...)` 하나. 행위 동사로 시작 (`GetDashboardUseCase`, `MarkNotificationReadUseCase`) |

### Mapper (DTO → 도메인 모델 변환)

Mapper는 **`core:data`에만 위치**한다. `core:domain`은 자신의 모델이 어디서 오는지 알지 못한다.

| 항목 | 위치 |
|---|---|
| 도메인 모델 | `core:domain/model/` |
| DTO (서버·DB 스펙) | `core:data/network/dto/` |
| Mapper 함수 (`toDomain()`) | `core:data/repository/mapper/` — DTO의 확장 함수로 작성 |

- DTO의 확장 함수(`fun XxxResponse.toDomain()`)로 작성한다.
- 파일명은 `XxxMapper.kt`, 변환 책임의 소유자인 `RepositoryImpl`이 속한 `repository/` 하위의 `mapper/` 서브패키지에 둔다. DTO 패키지(`network/dto/`)는 서버 계약만 표현하며 도메인 모델을 의존하지 않는다.
- 서버 스펙에만 존재하는 필드는 도메인 모델에 포함하지 않는다.
- 변환 책임은 `RepositoryImpl` 내부에서 끝내고, ViewModel·UseCase까지 DTO가 노출되지 않게 한다.

---

## 6. 의존성 추가 가이드

`build.gradle.kts`에 추가:

```kotlin
dependencies {
    implementation(project(":core:domain"))
}
```

이 모듈이 끌어오는 주요 라이브러리: `kotlinx-coroutines-core`·`javax-inject` (`implementation`이라 전이되지 않음).

> [!NOTE]
> `core:domain`은 Kotlin JVM 모듈이다. Android Library 모듈에서도 의존할 수 있으나, 반대(domain → Android)는 금지다.

---

## 7. 금지 규칙 (안티패턴)

### 의존 방향

모듈 간 의존 방향 금지 규칙은 [`docs/architecture/modularization.md — 금지 규칙`](../../docs/architecture/modularization.md)이 단일 출처다.

### domain 고유 계약 위반

| 금지 | 이유 |
|---|---|
| Repository 인터페이스가 DTO·Room Entity 반환 | 데이터 계층 구현 상세가 도메인으로 노출됨 |
| UseCase·Repository가 UI 상태·`Context`·navigation을 의존 | domain은 Android·UI 레이어를 알아서는 안 됨 |
| ViewModel이 `RepositoryImpl`을 직접 의존 | 인터페이스에만 의존, 구현 바인딩은 Hilt |
| ViewModel 안에 비즈니스 규칙 작성 | 재사용·테스트 불가, UseCase로 분리 |

---

## 8. 컨벤션

| 항목 | 규칙 |
|---|---|
| **의존** | `:core:common:kotlin`만 허용. Android SDK·`:core:data` 의존 금지 |
| **Repository 반환 타입** | 도메인 모델(`core:domain/model/`)만 반환. DTO·Room Entity 반환 금지 |
| **Mapper 위치** | `core:data`에만 위치. `fun XxxResponse.toDomain()` 확장 함수로 작성 |
| **비즈니스 규칙** | ViewModel에 두지 않는다. UseCase(`core:domain/usecase/`)로 분리 |
| **ViewModel 직접 호출** | 단일 API·단순 표시·재사용 없음·비즈니스 규칙 없음 4가지를 모두 만족할 때만 허용 |
| **UseCase 함수** | `operator fun invoke(...)` 하나. UI 상태·navigation·`Context` 의존 금지 |
| **테스트** | Fake 구현체로 JVM 단위 테스트. Android 환경 불필요 |
