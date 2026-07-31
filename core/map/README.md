# core:map

MinoAndroid의 **지도 공용 모듈**. Google Maps(maps-compose)를 감싸 표준 지도 컴포저블과 좌표 변환·기하 헬퍼를 제공한다.

> 모듈 책임·경계·의존 방향(레이어 그래프)은 [`docs/architecture/modularization.md`](../../docs/architecture/modularization.md)를 단일 출처로 한다. 이 문서는 이 모듈의 **API·사용법·확장 규칙**만 다룬다.

---

## 1. 개요

| | |
|---|---|
| **무엇을** | `GoogleMap`을 감싼 표준 지도 컴포저블(`MinoMap`)과 카메라 상태·좌표 변환·폴리곤 정렬 헬퍼를 제공한다. |
| **빌드 타입** | Android Library + Compose (`mino.android.library`, `mino.android.compose`) |

> [!IMPORTANT]
> **Google Maps SDK(`LatLng`)를 아는 곳은 이 모듈뿐이다.** feature는 SDK 무관 값 객체 `GeoPoint`로만 좌표를 다루고, `GeoPoint`↔`LatLng` 변환은 이 모듈 경계에서만 한다. 이렇게 두면 지도 SDK 교체가 feature로 새지 않는다. (좌표 값 객체 `GeoPoint` 자체는 `core:common:kotlin` 소유)

> [!NOTE]
> `MinoMap`은 `GoogleMap` 기반이라 **앱 레벨에 Google Maps SDK API 키 설정이 전제**다(매니페스트 `meta-data`). 키 설정은 이 모듈 밖(앱 모듈) 책임이다.

---

## 2. 핵심 API

| API | 역할 |
|---|---|
| `MinoMap(...)` | 프로젝트 표준 지도 컴포저블. `GoogleMap`을 감싼 단일 진입점으로, 마커·폴리곤 등 오버레이는 `content` 슬롯에서 maps-compose 컴포저블로 구성한다. |
| `rememberMinoCameraState(center: GeoPoint, zoom: Float)` | `center`를 초기 위치로 하는 `CameraPositionState` 생성. `GeoPoint`→`LatLng` 변환과 카메라 초기화 보일러플레이트를 흡수한다. |
| `GeoPoint.toLatLng()` | 프레임워크 무관 `GeoPoint`를 maps-compose의 `LatLng`로 변환하는 경계 확장. |
| `List<GeoPoint>.sortedIntoPolygonOrder()` | 임의 순서의 좌표를 무게중심 기준 각도순으로 정렬해 둘레 순서로 재배열한다. |

> [!NOTE]
> `Polygon`은 넘긴 점 **순서대로** 변을 잇기 때문에, 정렬하지 않은 좌표를 그대로 그리면 변이 교차해 나비넥타이 형태가 된다. `sortedIntoPolygonOrder()`로 (볼록 다각형 기준) 교차 없는 둘레 순서를 만든 뒤 그린다.

### 사용 예시 (A) — 기본 지도

`rememberMinoCameraState`로 카메라만 잡고 `MinoMap`을 그리면 끝이다. `content`를 생략하면 오버레이 없는 기본 지도(기본 UI·제스처만)가 된다.

```kotlin
@Composable
fun BasicMap(
    cameraCenter: GeoPoint,
    zoom: Float,
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberMinoCameraState(center = cameraCenter, zoom = zoom)

    MinoMap(
        cameraPositionState = cameraPositionState,
        modifier = modifier.fillMaxSize(),
    )
}
```

### 사용 예시 (B) — 오버레이 추가 (마커·폴리곤)

마커·폴리곤 같은 지도 위 요소는 `MinoMap`의 `content` 슬롯에서 maps-compose 컴포저블로 얹는다. 좌표는 `GeoPoint`로 받아 **그릴 때만** `LatLng`로 변환하고, 면을 그릴 점은 `sortedIntoPolygonOrder()`로 정렬한다.

```kotlin
@Composable
fun AreaMap(
    cameraCenter: GeoPoint,
    zoom: Float,
    areaPoints: List<GeoPoint>,
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberMinoCameraState(center = cameraCenter, zoom = zoom)

    // 임의 순서의 좌표를 둘레 순서로 정렬한 뒤 LatLng로 변환 — areaPoints가 바뀔 때만 재계산
    val orderedPoints =
        remember(areaPoints) {
            areaPoints.sortedIntoPolygonOrder().map { it.toLatLng() }
        }

    MinoMap(
        cameraPositionState = cameraPositionState,
        modifier = modifier.fillMaxSize(),
    ) {
        orderedPoints.forEachIndexed { index, point ->
            Marker(state = rememberUpdatedMarkerState(position = point), title = "지점 ${index + 1}")
        }
        if (orderedPoints.size >= 3) {
            Polygon(points = orderedPoints, strokeWidth = 6f)
        }
    }
}
```

---

## 3. 디렉토리 구조

```
team/mino/core/map/
├── MinoMap.kt                # 표준 지도 컴포저블 (GoogleMap 래퍼)
├── MinoMapCameraState.kt     # rememberMinoCameraState — 카메라 상태 팩토리
├── GeoPointExtensions.kt     # GeoPoint.toLatLng — SDK 좌표 변환(경계)
└── geometry/
    └── PolygonOrdering.kt    # sortedIntoPolygonOrder — 폴리곤 좌표 정렬
```

---

## 4. 확장 규칙 — 어디에 둘지 결정

| 두는 것 | 위치 |
|---|---|
| 지도 컴포저블·지도 상태 헬퍼 | 모듈 루트 (`MinoMap`·`rememberMinoCameraState`처럼) |
| `GeoPoint`↔지도 SDK 타입 변환 | 경계 확장 파일 (`GeoPointExtensions.kt`) — **SDK 타입을 다루는 코드는 이 경계로 모은다** |
| 좌표 기하 계산(정렬·거리·포함 판정 등) | `geometry/` — `GeoPoint`만 입출력으로 쓰는 순수 계산 |

- 새 코드가 **Google Maps SDK 타입(`LatLng` 등)을 만지면** 루트/경계에, **`GeoPoint`만으로 끝나는 순수 기하 계산이면** `geometry/`에 둔다.
- SDK에 의존하지 않는 좌표 값 객체·범용 유틸은 이 모듈이 아니라 `core:common:kotlin`에 둔다(모듈 경계 판단은 [`modularization.md`](../../docs/architecture/modularization.md)).

---

## 5. 의존성 추가 가이드

`build.gradle.kts`에 추가:

```kotlin
dependencies {
    implementation(project(":core:map"))
}
```

이 모듈이 끌어오는 주요 라이브러리: `maps-compose`. 좌표 타입은 `core:common:kotlin`의 `GeoPoint`를 사용한다. 지도를 그리는 feature 모듈이 의존한다.

---

## 6. 컨벤션

| 항목 | 규칙 |
|---|---|
| **좌표 타입** | 공개 API는 좌표를 `GeoPoint`로만 주고받는다. `LatLng` 등 SDK 타입은 이 모듈 내부에서만 변환한다. |
| **지도 진입점** | `GoogleMap`을 직접 쓰지 않고 `MinoMap`으로 그린다. 오버레이는 `content` 슬롯의 maps-compose 컴포저블로 구성. |
| **폴리곤** | 면을 그릴 좌표는 `sortedIntoPolygonOrder()`로 정렬한 뒤 그린다(점 순서 교차 방지). |
| **변환 비용** | `GeoPoint`→`LatLng` 변환·정렬은 입력이 바뀔 때만 하도록 `remember(key)`로 감싼다. |
