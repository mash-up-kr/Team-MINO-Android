# core:common:kotlin

MinoAndroid의 **순수 Kotlin(JVM) 공용 유틸리티** 모듈. 값 객체·확장·헬퍼를 제공한다.

> 모듈 책임·경계·의존 방향(레이어 그래프)은 [`docs/architecture/modularization.md`](../../../docs/architecture/modularization.md)를 단일 출처로 한다. 이 문서는 이 모듈의 **API·사용법·확장 규칙**만 다룬다.

---

## 1. 개요

| | |
|---|---|
| **무엇을** | 프레임워크 무관 값 객체(`GeoPoint` 등)와 공용 Kotlin 확장·유틸을 제공한다. |
| **빌드 타입** | Kotlin JVM (`mino.kotlin.jvm`) |

> [!IMPORTANT]
> 이 모듈은 Kotlin JVM이라 **Android 타입(Context, Uri, Compose 등)을 둘 수 없다.** Android가 필요한 코드의 행선지 등 모듈 경계 판단은 [`modularization.md`](../../../docs/architecture/modularization.md) 참조.

---

## 2. 핵심 API

### `GeoPoint`

위경도 좌표를 표현하는 **SDK 무관 값 객체**. maps-compose의 `LatLng` 같은 특정 SDK 타입에 의존하지 않으므로 도메인·UI 어디서든 쓸 수 있다.

```kotlin
import team.mino.core.common.kotlin.geo.GeoPoint

val seoulCityHall = GeoPoint(latitude = 37.5663, longitude = 126.9779)

// SDK 타입과의 변환은 그 SDK를 아는 쪽(예: core:map)에서 책임진다.
// fun GeoPoint.toLatLng() = LatLng(latitude, longitude)   // core:map 등에서 정의
```

`data class`이므로 `==`/`copy`/구조 분해를 그대로 사용한다.

---

## 3. 디렉토리 구조

```
core/common/kotlin/src/main/kotlin/team/mino/core/common/kotlin/
├── geo/
│   └── GeoPoint.kt        # 위경도 값 객체
├── extension/             # (예약 — 공용 Kotlin 확장 함수)
└── util/
    └── ElapsedTime.kt     # 경과 시간 네 구간 판정
```

`extension`은 아직 비어 있는 **예약 패키지**다. 새 코드를 추가할 때 아래 분류 기준을 따른다.

---

## 4. 확장 규칙 — 어디에 둘지 결정

| 패키지 | 두는 것 | 예시 |
|---|---|---|
| `geo` | 좌표·위치 관련 값 객체 | `GeoPoint` |
| `extension` | **기존 Kotlin/stdlib 타입에 붙는** 확장 함수·프로퍼티 | `String.toSlug()`, `List<T>.second()` |
| `util` | **타입에 종속되지 않는** 독립 헬퍼·상수 | 계산 유틸, 포맷터, 검증기 |

### 파일명 컨벤션

- **`geo` 등 값 객체** — 타입 이름 그대로 (`GeoPoint.kt`).
- **`extension`** — 파일명은 **리시버(수신자) 타입 이름**으로 짓고, 같은 타입의 확장은 그 파일에 모은다. Android와 달리 대상은 순수 Kotlin/stdlib 타입이다.

  | 확장 대상 | 파일명 |
  |---|---|
  | `String` 확장 | `String.kt` |
  | `Collection`/`List` 확장 | `Collection.kt` |
  | `Flow` 확장 | `Flow.kt` |

- **`util`** — 파일/타입명은 **행동을 나타내는 접미사**(`Util`, `Formatter`, `Validator` 등)로 짓는다. 객체가 무엇을 하는지에 따라 고른다. (Android 상태를 들고 있는 `Manager`류는 순수 Kotlin 모듈에 두지 않는다 — 그런 건 [`core:common:android`](../android/README.md))

  | 역할 | 접미사 | 예시 |
  |---|---|---|
  | 정적 헬퍼 함수 모음 | `Util` | `DistanceUtil.kt` |
  | 포맷 변환 | `Formatter` | `DurationFormatter.kt` |
  | 값 검증 | `Validator` | `EmailValidator.kt` |

위 표는 **이 모듈 안에서** 패키지를 고르는 기준이다. 이 모듈에 둘지, 다른 모듈(`core:common:android`·feature 등)에 둘지 — 즉 모듈 경계 판단은 [`modularization.md`](../../../docs/architecture/modularization.md)를 따른다.

---

## 5. 의존성 추가 가이드

`build.gradle.kts`에 추가:

```kotlin
dependencies {
    implementation(project(":core:common:kotlin"))
}
```

이 모듈이 끌어오는 주요 라이브러리: `kotlinx-coroutines-core`, `kotlinx-datetime`, `kotlinx-collections-immutable` (모두 `implementation`이라 전이되지 않음).

---

## 6. 컨벤션

| 항목 | 규칙 |
|---|---|
| **의존성** | Android SDK 의존 **금지**. 순수 Kotlin/코루틴/datetime/immutable 컬렉션까지만. |
| **불변성** | 값 객체는 `data class` + `val` 프로퍼티로 불변 유지. |
