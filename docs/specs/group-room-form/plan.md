# 구현 계획: 공동방 생성 및 편집 폼 (Group Room Form)

**대상 스펙 경로**: `docs/specs/group-room-form`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: 4.0.0

**최초 작성일**: 2026-08-21

**최종 수정일**: 2026-08-31

**버전**: 3.1.0

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

> **Figma 노드 표기**: 이 문서의 `NNNN-NNNNN`은 [MU_디자인](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8) 파일의 노드 ID다. 디자인 시스템 라이브러리 노드는 [MU_Wanted Design System](https://www.figma.com/design/hkSOCt4kOfyaVWdxybTicF/MU_Wanted-Design-System--Community-) 파일 소속임을 그 자리에 밝힌다. 표기 규칙은 [`figma-design-fidelity.md`](../../conventions/figma-design-fidelity.md) §5.

## 요약 (Summary)

생성과 편집이 하나의 폼을 공유하고(FR-001), 그 폼을 여는 자리가 온보딩·방 리스트 탭·홈 탭·장소 복제 시트·방 상세로 흩어져 있다. 서로 다른 feature에 흩어진 호출자가 한 화면을 열어야 하므로 **진입형 feature 모듈 `:feature:roomform`** 하나를 만들고, 진입 계약 `RoomFormLauncher`를 [`:core:navigation`](../../../core/navigation/README.md)에 둔다.

핵심 설계 판단은 **폼이 도착점을 모른다**는 것이다. FR-011이 진입점별로 다른 도착점을 요구하지만, 그 도착점(온보딩 친구 초대 스텝·방 상세·복제 시트)은 모두 다른 feature의 화면이다. 폼이 도착점을 알면 feature 간 의존이 생겨 [헌법 원칙 II](../../constitution.md)를 어긴다. 그래서 폼은 **무슨 일이 일어났는지**(생성됨·편집됨·건너뜀·이탈)만 Activity 결과로 돌려주고, 어디로 갈지와 완료 스낵바(FR-012·FR-015)는 폼을 연 진입점 feature가 정한다.

데이터는 배포된 [Team MINO API](https://api.gguk.org/api-docs-json) `1.0.0`의 `POST /api/v1/rooms` · `PATCH /api/v1/rooms/{roomId}` · `GET /api/v1/rooms/{roomId}`를 **실제로 호출한다.** DTO·Mapper 체인은 그대로 두고 이미 있는 `RoomApiService`를 확장해 `RoomRemoteDataSourceImpl`을 세우며, 같은 리소스에 둘로 갈려 있던 DataSource를 하나로 합친다. 계약은 [contracts/room-api.md](./contracts/room-api.md)가 소유한다.

디자인 자산 실사 결과 세 갈래가 갈렸다 — 상단 내비게이션은 Figma 디자인 시스템 컴포넌트셋이라 `:core:design-system`이 신설하고(`MinoTopNavigation`), 대표 색상 칩은 [ADR](../../adr/2026-08-14-room-color-palette-in-design-system.md)이 이미 그 모듈로 정해 두었으며, 확인 모달 3종과 미리보기 카드는 디자인 시스템 컴포넌트가 아니라서 `:feature:roomform`이 갖는다. 근거는 [research.md](./research.md) R-006·R-007·R-008.

1.1.0에서 남아 있던 미확정 4건을 Figma 대조로 모두 닫았고(R-015~R-018), spec 3.0.0이 그중 둘을 FR-003·FR-025로 추인했다.

1.2.0은 **비어 있던 설계 공백 두 곳을 메운다.** 방 설명 필드의 편집 상태를 `RoomFormRoute`가 소유하기로 정했고(R-019), 이 feature가 읽지 않는 `Room.type`을 도메인 모델에서 뺐다(R-020). 앞의 결정은 두 입력 필드의 상한을 자르는 주체를 갈라 놓는다 — 방 이름은 ViewModel, 방 설명은 컴포넌트다. 함께 spec 3.1.0이 확정한 자모 허용을 검증 계약에 반영했다(R-021).

**2.0.0은 데이터 출처를 mock에서 실서버로 바꾼다.** 서버가 배포되어 1.0.0이 mock을 택한 전제가 사라졌다(R-024). 걷어내는 비용이 파일 2개 삭제·2개 신규·바인딩 1줄로 끝난 것은 R-002가 노렸던 그대로다 — DTO·Mapper·`RoomRepositoryImpl`·`RoomRemoteDataSource` 인터페이스와 `:core:domain`·`:feature:roomform` 전체가 **한 글자도 바뀌지 않는다.** 함께 세 가지가 새로 드러났다. 응답 봉투는 [응답 봉투 ADR](../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)이 정한 `MinoResponse<T>`를 **그대로 따르고**(R-025), 실제 스키마 대조가 색 계약의 어긋남을 하나 더 드러냈으며(R-026), 직렬화가 처음 개입하면서 **편집에서 지운 설명이 요청 본문에서 빠지는** 결함을 미리 막았다(R-027). 이 계획이 만들지 않는 선행 조건 하나도 새로 드러났다 — 세션·유저 등록이 배선되기 전에는 실기기 검증이 전부 401로 막힌다(R-028 · §열린 항목 H).

**2.1.0은 그 어긋남 네 건을 서버팀 협의로 닫는다**(R-029). 방 설명 30자와 색 식별자 길이 상한은 **서버가 고치기로 했고**, 갈리던 색 표현은 스키마의 모순이 아니라 낡은 예시였으며, 색 어휘가 12색으로 확정됐다. 확정 어휘가 R-018의 소문자 스네이크 표기와 일치해 ~~이 계획이 고치는 것은 `RoomColor.GRAY`의 서버 식별자 하나(`"gray"` → `"grey"`)뿐이다.~~ **— 이 문장은 2.2.0에서 뒤집혔다. 배포된 `enum`의 회색이 `"gray"`라 고칠 것이 없었다.** 설계 경계는 움직이지 않는다. 2.1.1은 `/mino-analyze`가 짚은 것을 받아 [quickstart.md](./quickstart.md) §5에 **실서버에서 처음 도달 가능해진 `403`(방장 아님)을 검증하지 않는 항목으로** 명시했다 — 설계는 그대로이고 검증 범위의 공백을 드러낸 것이다.

**3.1.0은 설계를 하나도 바꾸지 않고 열린 항목 D를 닫는다.** spec이 4.0.0으로 오르며 FR-011의 홈 방 시트 분기를 **방 상세 → 홈 덱 전환**으로 뒤집었으나, 이 계획은 그 도착점을 처음부터 §범위 경계 밖(호출자 몫)에 두었고 결과 계약이 홈이 필요로 하는 값(`created` + `roomId`)을 이미 싣고 있어 **계약 표면이 한 글자도 바뀌지 않는다** — 도착점을 폼 밖에 둔 R-004가 그 대가를 치르지 않게 했다(R-036). 함께 서버 문서를 재조회해 **마지막 어긋남이 닫힌 것을 확인했다** — `description.maxLength: 20`이 제거되면서 21~30자 설명이 더는 거절되지 않고, `name.maxLength: 15`도 사라진 대신 `pattern`이 생겨 **서버가 FR-004와 같은 문자 집합(자모 포함)을 거른다.** 15자 상한의 유일한 수문장이 클라이언트가 됐다는 사실만 계약 문서에 남기고 코드는 더하지 않는다(R-035).

**3.0.1은 설계를 바꾸지 않고 3.0.0이 잘못 적은 위험의 성격 하나를 고친다.** 3.0.0은 `RoomApiService`를 덮어쓰면 방 선택 시트가 **"조용히 깨진다"**고 적었으나, `listRooms()`를 참조하는 곳이 프로덕션 1곳과 테스트 3건이라 그 함수를 지우면 **컴파일이 먼저 깨진다.** 결정(확장으로 다룬다)도 작업 목록도 그대로이고, 바뀌는 것은 이 위험을 얼마나 무겁게 읽어야 하는가다 — 착수자가 파일을 열어보지 않아도 빌드가 막아 준다. 함께 확인한 것은 **합병 구간에 가드가 비는 순간이 없다**는 사실이다(`T072` → `T073` → `T082` 순서가 프로덕션 참조를 끊기 전에 새 참조를 세운다).

**3.0.0은 `develop`을 실측해 이 계획이 백지에서 세운다고 적어 둔 것 중 이미 서 있는 것을 걷어내고, 다른 spec이 이 계획에 배정해 둔 합병을 받는다.** 2.0.0~2.2.1이 쓰인 뒤 `shared-link-receiver`가 머지되면서 방 데이터 레이어의 지형이 바뀌었다. `RoomApiService`는 **이미 있고**(`listRooms()` 하나), 이 계획이 그것을 "신규"로 다루면 작업자가 덮어써 방 선택 시트의 유일한 데이터 경로를 지운다 — 다만 그 실패는 **컴파일에서 드러난다**(R-031의 3.0.1 정정). 기다리던 선행 조건도 닫혔다 — `MinoResponse<T>`와 [응답 봉투 ADR](../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)이 둘 다 `develop`에 있어, 2.0.0이 적었던 "이 브랜치에서 링크가 열리지 않는다"는 끝났다.

**설계 경계를 움직이는 것은 DataSource 합병이다**(R-032). `shared-link-receiver`의 research R-015가 *"두 DataSource는 `group-room-form`이 실서버로 전환하는 시점에 합쳐지고, 그때 지워지는 것은 `RoomListRemoteDataSource`다"*라고 이 계획에 배정해 두었고, 같은 문장이 [`RoomListRemoteDataSource.kt`](../../../core/data/src/main/java/team/mino/core/data/datasource/RoomListRemoteDataSource.kt)의 KDoc에도 남아 있다. 둘로 갈라 놓았던 유일한 근거가 *"바인딩 대상이 다르다 — 그쪽은 mock, 이쪽은 실서버"*인데, **mock을 걷어내는 순간 그 근거가 함께 사라진다.** 그래서 2.0.0이 "`RoomRemoteDataSource` 인터페이스는 한 글자도 바뀌지 않는다"고 적었던 판정이 뒤집힌다 — 인터페이스가 `listRooms()`를 흡수하고, `RoomRepositoryImpl`의 생성자 인자가 둘에서 하나로 줄며, `RoomList*` 3파일이 사라진다. 함께 서버 문서를 재조회해 **어긋남 한 건(`description.maxLength: 20`)이 그대로임을 확인했다**(R-033).

**2.2.1은 [contracts/design-system-additions.md](./contracts/design-system-additions.md) §1의 `MinoTopNavigation` API 초안을 `develop`에 들어가 있는 실제 시그니처로 맞췄다** — 초안의 `onNavigateBack`·`colors`가 실제와 갈린 채 남아 있어 계약 문서를 먼저 읽는 사람이 틀린 표면을 보게 돼 있었다. 어느 쪽을 따를지는 2026-08-25에 이미 현 구현으로 확정된 것이고, 이번 개정은 그 결정을 계약 문서에 반영한 것이라 설계 변경이 아니다.

**2.2.0은 색 계약을 배포된 `enum`으로 확정하고, 2.1.0의 오판 하나를 되돌린다.** 서버가 `color`에 13색 `enum`을 배포해 어긋남 2·4가 해소됐고 3도 방 응답 쪽은 정리됐다. 그런데 그 `enum`의 회색이 **`"gray"`**여서, 구두 협의를 근거로 `"grey"`로 확정했던 2.1.0의 판정이 뒤집혔다 — [R-018](./research.md#r-018-mock의-색상-식별자-표기-plan-110)의 원래 표기가 13색 전부에서 맞았으므로 **`RoomMapper`는 고칠 것이 없다**(R-030). 남은 어긋남은 `description.maxLength: 20` 하나다. 설계 경계는 움직이지 않고, 없어지는 것은 하지 말았어야 할 작업 하나다.

**2.1.2는 설계를 하나도 바꾸지 않는다.** spec이 3.2.1로 오르며 기준 PRD를 9.0.0으로 맞췄으나 **요구사항 diff가 0건**이라(PRD 5.0.0 → 9.0.0의 네 번의 개정이 전부 [SCR-003] 홈 카드 덱·[SCR-006] 장소 상세에 국한됐고 `[SYS-001]`은 한 글자도 바뀌지 않았다) 이 계획이 따라갈 것이 없다. 함께 서버 문서를 재조회했고 **어긋남 세 지점의 수정이 아직 배포되지 않았음을 확인했다**(§열린 항목 D).

1.4.0은 **설계를 바꾸지 않고 열린 항목 하나를 닫는다.** 편집 명세 보드가 방 이름 필드를 생성 보드와 같은 `Textinput/Textfield`로 갈아 끼워, 구현 착수를 막고 있던 디자인 불일치(열린 항목 C)가 해소됐다(R-023). 대조하면서 FR-013의 Figma 근거가 낡은 User Flow 보드에만 남는다는 사실이 드러났고, **spec 3.2.0이 그것을 §4 가정 3건으로 받아 닫았다** — 요구사항은 하나도 바뀌지 않고 FR-013의 근거가 Figma에서 PRD [SYS-001] Flow D로 옮겨졌다. 1.4.1은 그 추인을 반영해 기준 spec 버전을 맞춘 것이다.

## 기술 컨텍스트 (Technical Context)

**언어/버전**: Kotlin 2.2.10 / JVM 17. `compileSdk` 36 · `minSdk` 29 · `targetSdk` 36.

**주요 의존성**: Jetpack Compose · Hilt · AndroidX Navigation(type-safe Route) · kotlinx-serialization · kotlinx-collections-immutable. 새 외부 라이브러리는 도입하지 않는다. 모듈이 자동으로 얻는 의존은 `mino.android.feature` 컨벤션 플러그인이 정한다(`build-logic/convention/src/main/kotlin/AndroidFeatureConventionPlugin.kt`).

**저장소**: **원격 서버뿐**이다. DataStore·DB·인메모리 캐시를 쓰지 않는다 — 폼은 열 때 읽고 닫을 때 쓰는 1회성 왕복만 한다. 레이어 구성은 [contracts/room-api.md](./contracts/room-api.md) §4가 소유하며, **그 절은 이미 `develop`에 서 있는 것을 기준으로 `[확장]`·`[삭제]`를 표기한다**(R-031·R-032).

**참조 API 문서**: [Team MINO API](https://api.gguk.org/api-docs-json) `1.0.0` · **2026-08-31T12:51:29+09:00 조회 기준.** 이 계획의 서버 계약 판단은 그 시점의 문서를 근거로 하며, 인용된 스키마 제약의 소유자는 [contracts/room-api.md](./contracts/room-api.md) §1이다. **이 조회에서 요청 스키마가 바뀌었다** — `name`·`description`의 길이 상한이 사라지고 `name`에 `pattern`이 생겨 어긋남이 0건이 됐다([research.md](./research.md) R-035).

조회 이력 — 08-27T21:45:27 최초 대조 · 08-27T23:33:29 재조회(변동 없음) · 08-28T00:55:30 재조회에서 `color`의 `enum` 배포를 확인(R-030) · **08-28T11:39:53 재조회에서 변동 없음 — `description.maxLength`는 여전히 `20`**(R-033).

**테스트**: JVM 단위 테스트(JUnit4 + `kotlinx-coroutines-test`). 대상 목록은 [contracts/room-repository.md](./contracts/room-repository.md) §2와 [contracts/room-form-ui.md](./contracts/room-form-ui.md) §5가 소유한다. **실서버 전환이 `:core:data`에 검증 대상을 하나 더한다** — 요청 본문 직렬화가 `description`을 빠뜨리지 않는지는 실기기가 아니라 단위 테스트가 잡아야 한다(R-027). `:feature:roomform`·`:core:domain`의 `build.gradle.kts`에 `testImplementation(libs.kotlinx.coroutines.test)`를 더한다 — feature 컨벤션 플러그인은 `junit`만 붙인다. Compose UI 테스트는 이번 범위에 넣지 않는다.

**대상 플랫폼**: Android (`minSdk` 29).

**프로젝트 유형**: mobile-app — 다중 Gradle 모듈. 이번 작업이 손대는 모듈은 아래 §프로젝트 구조 참조.

**성능 목표**: SC-002가 요구하는 "한 글자도 뒤처지지 않는 반영"은 프레임 예산이 아니라 **동기 상태 갱신**으로 만족시킨다 — 입력·검증·미리보기 반영에 비동기 경계를 두지 않고, `updateState`가 같은 프레임에서 끝난다. 전달 경로에 지연 연산자(`debounce`·`sample` 등)를 두지 않는다 — 메커니즘 자체를 금지하는 것이 아니라 **지연을 넣지 않는다**는 뜻이다.

**제약 조건**:
- 폼은 다른 feature 모듈을 의존하지 않는다. 진입·복귀는 `:core:navigation` 계약 한 겹으로만 이뤄진다([헌법 원칙 II](../../constitution.md)).
- 도착점 feature(온보딩·방 리스트 탭·홈 탭·장소 복제 시트·방 상세)가 **아직 하나도 존재하지 않는다.** FR-011·FR-012·FR-015의 이동·스낵바는 이 범위에서 구현되지 않고, 결과 계약과 임시 검증 진입점까지가 이번 몫이다(§범위 경계).
- 대표 색상 원시값은 `AtomicColorToken`(`internal`)에 있어 feature에서 보이지 않는다 — 팔레트를 쓰는 컴포넌트는 `:core:design-system` 안에서만 만들 수 있다([ADR](../../adr/2026-08-14-room-color-palette-in-design-system.md)).
- 서버 계약과 어긋났던 네 지점 중 **둘이 해소되고 하나가 부분 해소됐다.** 색은 배포된 `enum` 13색으로 확정됐고(회색은 **`"gray"`**), 남은 것은 `description.maxLength: 20`뿐이다([research.md](./research.md) R-030, 표는 [contracts/room-api.md](./contracts/room-api.md) §2). **21~30자 설명은 지금도 서버가 거절한다.**
- **응답 봉투 처리는 이 계획이 만들지 않는다.** `MinoResponse<T>`와 [그 ADR](../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)은 **이제 `develop`에 있다** — 2.0.0이 기다리던 선행 조건이 닫혔고, 기존 `RoomApiService.listRooms()`가 이미 그 봉투를 벗기고 있다. 이 계획은 같은 형태를 잇기만 한다([research.md](./research.md) R-025·R-031).
- **방 데이터 레이어는 백지가 아니다.** `RoomApiService`와 방 목록용 DataSource 한 벌이 `shared-link-receiver`의 손으로 이미 서 있다. 이 계획은 앞의 것을 **확장**하고 뒤의 것을 **흡수해 지운다.** 덮어쓰면 `:core:data`가 컴파일되지 않는다 — **은밀한 회귀가 아니라 빌드 실패**이므로, 확장으로 다루는 이유는 위험의 은밀함이 아니라 **덮어쓸 이유가 없다는 것**이다([research.md](./research.md) R-031·R-032).
- **세션 확보와 유저 등록이 배선되어 있지 않아 실기기 검증이 막힌다.** 이 계획이 고칠 것이 아니다([research.md](./research.md) R-028 · §열린 항목 H).

**규모/범위**: 화면 1개(모달 3종 오버레이 포함) · 신규 feature 모듈 1개 · 신규 Repository 1개 · 신규 UseCase 2개 · 디자인 시스템 신규 컴포넌트 2개 · 신규 `ApiService` 1개. 도메인 모델의 목록은 [data-model.md](./data-model.md) §2가 소유한다.

## 헌법 준수 확인 게이트 (Constitution Check)

*게이트: Phase 0 리서치 전에 반드시 통과해야 한다. Phase 1 설계 후 재확인한다.*

기준은 [`docs/constitution.md`](../../constitution.md) 2.1.0이다.

| # | 게이트 | 판정(Phase 0 전) | 판정(Phase 1 후) | 근거 |
|---|---|---|---|---|
| G1 | **원칙 I — SSOT.** 이 계획이 규약 본문을 복제하지 않고 링크로 지목하는가 | PASS | PASS | 모듈 골격·에러 처리·토큰 판정 절차를 본문에 옮기지 않고 소유 문서를 링크한다. 새 규칙을 만들지 않는다 |
| G2 | **원칙 II — 레이어 경계.** feature→feature 의존이 없고, feature가 `:core:data`를 직접 의존하지 않으며, DI 바인딩을 구현 소유 모듈이 갖는가 | PASS | PASS | 진입·복귀는 `RoomFormLauncher` 계약 한 겹. `:feature:roomform`은 `:core:domain`만 안다. `@Binds`는 `:core:data`와 `:feature:roomform`의 각 `di/`가 소유한다([contracts/room-form-launcher.md](./contracts/room-form-launcher.md) §4). **3.0.0의 DataSource 합병은 이 경계를 넘지 않는다** — 사라지는 `RoomListDataSourceModule`도 `:core:data` 안쪽이고, 도메인 `RoomRepository`의 시그니처는 그대로다(R-032) |
| G3 | **원칙 II — Android 의존 방향.** `:core:domain`이 Android를 알지 않는가 | PASS | PASS | 도메인 모델·Repository·UseCase 모두 순수 Kotlin. `RoomColor`는 `Color`가 아니라 enum이다([data-model.md](./data-model.md) §2) |
| G4 | **원칙 III — 기록.** 되돌리기 어려운 결정이 ADR 후보로 식별되었는가 | PASS | PASS | R-006(디자인 시스템 컴포넌트 판정)·R-022(DS 컴포넌트의 글자 수 단위)가 다른 feature를 구속한다. **R-025는 승격 제안이 아니라 준수다** — [응답 봉투 ADR](../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)이 이미 정했고 이 feature를 직접 지목한다. **R-002는 후보에서 내렸다** — 전제가 사라져 다른 feature를 구속할 수 없다(R-024). **3.0.0이 더한 후보는 없다** — R-032는 이 리소스 한정의 배선 정리이고, R-034는 [래스터 이미지 ADR](../../adr/2026-08-19-raster-image-placement-and-format.md) §결정 1과 [팔레트 ADR](../../adr/2026-08-14-room-color-palette-in-design-system.md) §적용 범위가 **이미 정해 둔 대로 한 것**이라 새 결정이 아니다 |
| G5 | **원칙 IV — Spec-First.** plan에만 있고 spec에 근거가 없는 요구사항이 없는가 | PASS | PASS | 모든 설계 항목이 FR/UX/EC/TS 번호로 역추적된다. **1.1.0에서 유일하게 spec을 벗어나 있던 지점(FR-003 카운터)이 spec 3.0.0으로 해소돼 이제 어긋남이 0건이다** |
| G6 | **원칙 V — 에러 처리 규약.** 실패가 2단 분류를 따르는가 | PASS | PASS | 편집 진입 로드 실패는 State(주 데이터), 생성·편집 요청 실패는 `DomainErrorEmitter`(액션 일회성). `launchSafely`·`runCatchingDomain`·`onDomainFailure`만 쓴다([error_handling.md](../../conventions/error_handling.md) §5). **실서버가 401·403·`Network`를 도달 가능하게 만들지만 분류는 그대로다** — 이 feature는 `errorCode`로 분기하지 않으며 새 매핑도 더하지 않는다([contracts/room-api.md](./contracts/room-api.md) §6) |
| G7 | **기술 표준 — 디자인 토큰 판정.** 값이 일치하는 토큰이 있으면 토큰, 없으면 실측값 규칙을 따르는가 | PASS | PASS | 판정은 구현 착수 시 노드 대조로 수행한다([figma-design-fidelity.md](../../conventions/figma-design-fidelity.md) §2). 계획은 토큰 신설을 선행 조건으로 삼지 않는다 |
| G8 | **기술 표준 — 컴포넌트·에셋 배치.** 각 UI 자산의 소속 모듈이 판정 규칙대로 정해졌는가 | PASS | PASS | 세 자산을 각각 판정했다 — 근거는 R-006·R-007·R-008. 판정 근거가 Figma 실사다([component-asset-placement.md](../../conventions/component-asset-placement.md) §1.2). **3.0.0에서 썸네일 13종과 폴백이 `:core:common:ui`로 승격된 것을 확인했다** — 두 번째 사용처가 생겨 같은 규약 §1.1의 승격 조건을 충족한 결과이고, 이 계획이 더 배치할 에셋은 없다(R-034) |
| G9 | **기술 표준 — 검증 장치의 한계.** "CI가 잡아 줄 것"을 전제하지 않는가 | PASS | PASS | 빌드 확인의 최소선을 `./gradlew :app:assembleQaDebug`로 두고, 경계 위반은 리뷰가 잡는다는 전제로 계획했다([quickstart.md](./quickstart.md) §4). **2.0.0이 이 게이트를 한 번 더 쓴다** — 세션 미배선으로 시나리오가 막히는 것을 "통과"로 적지 않도록 [quickstart.md](./quickstart.md) §1·§4가 미검증을 명시적 판정으로 두었다 |

**정당화가 필요한 위반**: 1건이다 — 1.3.0이 plan 단계에서 `:core:design-system`의 프로덕션 코드를 냈다(원칙 IV의 단계 순서). 정당화와 그 대가는 §복잡도 추적이 소유한다. 위 게이트 9개의 판정은 그 위반에 걸리지 않는다.

**3.0.0 재평가 결과 9개 모두 PASS 유지.** 이번 개정이 더한 것은 새 설계가 아니라 `develop` 실측에 따른 경계 정정이라, 게이트 판정을 뒤집는 항목이 없다.

**3.1.0 재평가 결과도 9개 모두 PASS 유지.** spec 4.0.0이 바꾼 것은 이 계획이 §범위 경계 밖에 둔 도착점 하나이고 서버 스키마 변경은 제약이 느슨해진 쪽이라, 모듈 경계(G2)·레이어 방향(G3)·에러 분류(G6)·자산 배치(G8) 어느 것도 움직이지 않는다. G5도 그대로다 — 새로 더한 서술이 전부 spec FR-004·FR-005·FR-011로 역추적된다.

## 프로젝트 구조 (Project Structure)

### 문서 (이번 Feature)

```text
docs/specs/group-room-form/
├── plan.md              # 이 파일 (/mino-plan 산출물)
├── research.md          # Phase 0 산출물 (/mino-plan)
├── data-model.md        # Phase 1 산출물 (/mino-plan)
├── quickstart.md        # Phase 1 산출물 (/mino-plan)
├── contracts/           # Phase 1 산출물 (/mino-plan)
│   ├── room-form-launcher.md        # feature 간 진입·결과 계약
│   ├── room-repository.md           # 도메인 계약 (Repository·UseCase)
│   ├── room-api.md                  # 서버 API 계약 (배포 OpenAPI 대조)
│   ├── room-form-ui.md              # 화면 계약 (UiState·Intent·SideEffect)
│   └── design-system-additions.md   # 디자인 시스템 신설·확장 컴포넌트 계약
└── tasks.md             # /mino-task 산출물 (/mino-plan 이 생성하지 않음)
```

### 소스 코드 (Repository Root 기준)

> [!IMPORTANT]
> **표기 기준은 2026-08-28 `develop` 실측이다.** 데이터 레이어의 실서버 전환을 뺀 나머지는 **이미 구현되어 있다.** `[완료]`는 `develop`에 있어 다시 만들지 않는다는 뜻이고, 남은 일은 `[신규]`·`[확장]`·`[수정]`·`[삭제]`뿐이다. 이 구분이 곧 `/mino-task`가 다시 짤 작업 목록의 경계다.

```text
core/navigation/src/main/java/team/mino/core/navigation/activity/launcher/
├── ExtraTag.kt                       # [수정] EXTRA_ROOM_FORM_* 키 추가
└── RoomFormLauncher.kt               # [신규] interface RoomFormLauncher : ActivityLauncher

core/domain/src/main/kotlin/team/mino/core/domain/
├── model/
│   ├── Room.kt                       # [완료] 공동방 도메인 모델 — type 없음 (R-020)
│   ├── RoomColor.kt                  # [완료] 대표 색상 12종 + GRAY
│   ├── RoomDraft.kt                  # [완료] 생성·편집 입력값
│   └── RoomNameValidation.kt         # [완료] 방 이름 판정 결과
├── repository/RoomRepository.kt      # [완료] 목록·조회·생성·편집 계약 (getRooms는 shared-link-receiver가 더했다)
└── usecase/
    ├── ValidateRoomNameUseCase.kt    # [완료] FR-002·FR-004·EC-001·EC-005 (길이는 판정하지 않는다)
    └── CreateRoomUseCase.kt          # [완료] FR-006 회색 기본값 적용 + FR-010

core/data/src/main/java/team/mino/core/data/     # 백지가 아니다 — 표기 기준은 develop (R-031·R-032)
├── network/
│   ├── dto/
│   │   ├── request/RoomRequest.kt        # [수정] description의 기본값 제거 — R-027
│   │   ├── response/RoomResponse.kt      # [기존] Room 응답 DTO (봉투 안쪽)
│   │   └── response/MinoResponse.kt      # [기존] 봉투 ADR 소유 — 이 계획이 만들지 않는다
│   └── service/RoomApiService.kt         # [확장] listRooms()가 이미 있다. 세 함수를 더한다 — R-031
├── datasource/
│   ├── RoomRemoteDataSource.kt           # [확장] listRooms() 흡수 — R-032
│   ├── RoomRemoteDataSourceImpl.kt       # [신규] RoomApiService 위임 (네 함수)
│   ├── RoomMockRemoteDataSourceImpl.kt   # [삭제] R-024
│   ├── mock/RoomMockStore.kt             # [삭제] mock/ 디렉터리째 — R-024
│   ├── RoomListRemoteDataSource.kt       # [삭제] 합병 — R-032
│   ├── RoomListRemoteDataSourceImpl.kt   # [삭제] 합병 — R-032
│   └── di/
│       ├── RoomDataSourceModule.kt       # [수정] @Binds 대상을 실구현으로
│       └── RoomListDataSourceModule.kt   # [삭제] 바인딩이 하나로 줄었다 — R-032
└── repository/
    ├── RoomRepositoryImpl.kt             # [수정] 생성자 인자 2개 → 1개 — R-032
    ├── mapper/RoomMapper.kt              # [무변경] 색 식별자 표가 배포된 enum과 일치 (R-030)
    ├── mapper/RoomSummaryMapper.kt       # [무변경] shared-link-receiver 소유 — 건드리지 않는다
    └── di/RoomRepositoryModule.kt        # [기존] @Binds

core/design-system/src/main/java/team/mino/core/designsystem/component/
├── topnavigation/                    # [완료] MinoTopNavigation — Figma 컴포넌트셋 · FR-025
│   ├── MinoTopNavigation.kt          #   실제 시그니처의 소유자는 contracts/design-system-additions.md §1
│   ├── MinoTopNavigationDefaults.kt
│   ├── TopNavigationPreview.kt
│   └── token/TopNavigationTokens.kt
├── textinput/                        # [완료] R-022 — 상한·카운터를 grapheme 기준으로
│   ├── MinoTextArea.kt               # [수정] 카운터·InputTransformation 교체
│   └── MaxGraphemeLengthTransformation.kt  # [신규]
└── roomcolorchip/                    # [완료] ADR 2026-08-14 지정 위치
    ├── MinoRoomColor.kt              # 팔레트 12항목 enum (도메인 규칙 없음)
    ├── MinoRoomColorChip.kt
    ├── MinoRoomColorChipDefaults.kt
    ├── RoomColorChipPreview.kt
    └── token/RoomColorChipTokens.kt

core/design-system/src/main/java/team/mino/core/designsystem/util/
└── text/GraphemeLength.kt            # [완료] grapheme 단위 글자 수 (R-022) — 열린 항목 G

core/common/ui/src/main/                                 # [완료] 승격 — R-034
├── res/drawable-{mdpi,xhdpi,xxhdpi}/room_thumbnail_*.webp   # 썸네일 13종(12색 + 회색)
└── java/.../component/RoomThumbnailFallback.kt              # public — 방 선택 시트도 함께 쓴다

feature/roomform/                     # [완료] 진입형 feature 모듈 — 에셋은 :core:common:ui로 갔다 (R-034)
├── build.gradle.kts
└── src/main/
    └── java/team/mino/feature/roomform/
        ├── RoomFormActivity.kt       # (public) 진입점 — extra 복원·결과 반환
        ├── RoomFormDestinations.kt   # @Serializable RoomForm Route
        ├── RoomFormShell.kt          # MinoScaffold + navController + TrackScreenViews
        ├── RoomFormNavHost.kt        # screen<RoomForm> 등록
        ├── di/
        │   ├── RoomFormLauncherImpl.kt
        │   └── RoomFormNavigationModule.kt
        └── form/
            ├── screen/  RoomFormRoute.kt · RoomFormScreen.kt
            ├── vm/      RoomFormViewModel · RoomFormUiState · RoomFormIntent · RoomFormSideEffect
            ├── model/   RoomFormMode.kt · RoomFormDialog.kt · RoomColorUiModel.kt
            └── component/
                ├── RoomPreviewCard.kt        # 상단 미리보기 카드 (FR-008) — 썸네일은 공용 폴백 호출 (R-034)
                ├── RoomColorPalette.kt       # 3×4 칩 그리드 배치 (FR-006)
                └── RoomFormConfirmDialog.kt  # 확인 모달 3종 공통 (UX-008·UX-009)

feature/main/src/main/java/team/mino/feature/main/   # [수정] 임시 검증 진입점의 편집 조작 (§범위 경계)
app/build.gradle.kts                                 # [완료] implementation(project(":feature:roomform"))
settings.gradle.kts                                  # [완료] include(":feature:roomform")
```

**구조 결정**: **진입형 feature 모듈 `:feature:roomform` 단일 모듈**이다. 근거는 [`feature-module.md`](../../architecture/feature-module.md) 1장의 구분 기준 — 폼은 탭 셸의 그래프에 편입되는 화면이 아니라 **Activity로 독립 진입**하고, 호출자가 여러 feature에 흩어져 있으며(FR-001의 진입점 8개), 결과를 호출자에게 돌려줘야 한다(FR-011·FR-019). 화면이 하나여도 `XShell`·`XNavHost`를 유지한다 — 진입 인자 복원(`toRoute`)과 화면 조회 로깅이 NavHost에 딸려 오기 때문이다(같은 문서 4장).

### 범위 경계 — 이번 계획이 만들지 않는 것

spec §3.2가 이미 범위 밖으로 둔 것 외에, **도착점 feature를 다른 spec이 소유해** 이번에 완결되지 않는 것을 명시한다.

> [!NOTE]
> **3.1.0에서 근거가 바뀌었다.** 2.2.1까지는 *"도착점 역할을 하는 화면이 하나도 없다"*가 근거였으나, 실측하면 **`:feature:home`이 이미 홈 방 시트를 그리고 있다** — [`HomeRoomSheet.kt`](../../../feature/home/src/main/java/team/mino/feature/home/main/component/HomeRoomSheet.kt)의 첫 칸 `방 만들기`가 `HomeSideEffect.NavigateToRoomForm`으로 나가고, 그 신호를 어디로 배선할지는 [`HomeNavigation.kt`](../../../feature/home/src/main/java/team/mino/feature/home/HomeNavigation.kt)의 `onNavigateToRoomForm`을 받는 셸이 정한다. **아래 표는 그대로 유효하고, 유효한 이유만 "화면이 없다"에서 "그 화면을 다른 spec이 소유한다"로 바뀐다**([research.md](./research.md) R-036).
>
> **spec 4.0.0이 뒤집은 홈 분기가 실제로 닿는 곳이 그 모듈이다.** 지금 `onNavigateToRoomForm: () -> Unit`은 결과를 되받는 표면이 없어, 폼이 돌려주는 `roomId`로 보는 방을 바꾸려면 그 콜백이 넓어져야 한다. 그것은 [`home-deck-exploration`](../home-deck-exploration/spec.md)의 몫이며 **이 계획은 코드를 하나도 더하지 않는다** — 폼 쪽 계약은 이미 필요한 값을 싣고 있다([contracts/room-form-launcher.md](./contracts/room-form-launcher.md) §3).
>
> 폼을 실제로 여는 유일한 코드는 여전히 `MainActivity`의 임시 검증 배선이다(`RoomFormLauncher` 참조처 실측).

| spec 항목 | 이번 범위 | 남는 몫 |
|---|---|---|
| FR-011 진입점별 도착점 이동 | 폼이 결과(`created`·`updated`·`skipped`)와 `roomId`를 돌려주는 데까지 | 각 진입점 feature가 결과를 받아 자기 도착점으로 이동. **홈 방 시트만 방 상세가 아니라 홈 덱 전환이다**(spec 4.0.0 FR-011) — [`home-deck-exploration`](../home-deck-exploration/spec.md) |
| FR-012 `방 생성 완료!` · FR-015 `방 편집이 완료되었어요` | 스낵바를 표출하지 않는다 — 표출 자리가 도착 화면이므로(UX-006) | 도착점 feature |
| FR-019 복제 시트 목록 두 번째 배치 | `roomId` 반환까지 | 장소 복제 시트(PRD [SYS-003]) |
| FR-014 더보기 [편집] 노출 제어 | 편집 진입 계약(`roomId` extra)까지 | 방 상세(PRD [SCR-005]) |
| FR-016 편집 결과의 다른 화면 반영 | 수정된 `Room`을 결과로 돌려주는 데까지 | 방 목록·지도 마커·방 뱃지를 그리는 feature |
| FR-017 건너뛰기 후 튜토리얼 스텝 이동 | `skipped` 결과 반환까지 | 온보딩(PRD [SCR-002]) |

**임시 검증 진입점을 `:feature:main`에 둔다.** 이 모듈은 이미 전환 검증용 배선(`onNavigateToSample`·`onRequestSampleResult`)과 placeholder 탭을 갖고 있어, 같은 자리에 폼 진입·결과 수신·스낵바 표출을 붙이면 위 표의 미구현분을 **한 진입점에 한해** 실제로 눌러 볼 수 있다. 실제 진입점 feature가 생기면 걷어낸다. `:feature:sample`을 쓰지 않는 이유는 그 모듈이 제거 예정이라 새 의존을 더하지 않기 위해서다.

**2.0.0에서 이 진입점의 편집 조작이 바뀐다.** mock 시드가 사라져 고정된 `roomId`가 더는 없으므로, `onEditSeedRoom`은 **직전에 생성한 방의 `roomId`로 여는 조작**이 된다. 방을 한 번도 만들지 않았으면 조작이 비활성이다. 임시 배선이므로 계약 문서를 두지 않는다 — 진입 계약 자체는 [contracts/room-form-launcher.md](./contracts/room-form-launcher.md)가 그대로 소유한다.

**이 진입점에 세션·유저 등록을 붙이지 않는다.** 걷어낼 배선에 인증 부트스트랩을 넣으면 걷어내는 순간 그 책임이 사라진다([research.md](./research.md) R-028).

## 복잡도 추적 (Complexity Tracking)

> **헌장 준수 확인에서 정당화가 필요한 위반이 있는 경우에만 작성**

| 위반 사항 | 필요한 이유 | 더 단순한 대안을 기각한 이유 |
|---|---|---|
| **plan 단계에서 프로덕션 코드를 냈다.** `:core:design-system`의 `MinoTextArea` 동작을 고쳤다(R-022). 헌법 원칙 IV의 단계 순서(명세 → 설계 → 구현)와 열린 항목 F가 적었던 "구현 착수 전에 닫는다"를 함께 어긴다 | 2026-08-23 사용자 지시. F는 **어느 쪽을 고를지**의 문제였고(편차 수용 vs 컴포넌트 수정), 고르는 순간 그 결정의 실체가 코드 몇 줄이라 문서로만 남기면 다음 단계가 같은 판단을 다시 해야 했다 | **`/mino-task`로 미루기** — 이 변경은 `:feature:roomform`이 아니라 `:core:design-system`의 것이라 이 feature의 작업 목록에 자연스럽게 들어가지 않는다. 그 모듈의 별도 이슈로 떼는 편이 정석이나, 그러면 F가 이 계획의 열린 항목으로 무기한 남는다 |

**이 위반의 대가**: 이 델타의 코드는 `tasks.md`를 거치지 않았으므로 `/mino-task`가 만들 작업 목록에 **다시 등장하면 안 된다.** 트리에 `[완료]`로 표기한 이유다.

### 1.0.0이 남긴 미확정 4건 — 전부 해소 *(1.1.0)*

| # | 남겼던 것 | 확정 | 근거 |
|---|---|---|---|
| TBD-1 | 방 이름 필드의 `n/15` 카운터를 `MinoTextField` 확장으로 얻을지 | **카운터가 없다.** `MinoTextField`를 확장하지 않는다 | [research.md](./research.md) R-015 |
| TBD-2 | 편집 폼 상단 타이틀 문구 | 생성 `공동방 만들기` · 편집 **`방 편집`** | R-016 |
| TBD-3 | 회색(미선택) 방의 썸네일 에셋 | `Room Thumbnail_Empty`의 **`my room` variant가 곧 회색**이다. 별도 에셋이 없다 | R-017 |
| TBD-4 | 대표 색상의 서버 식별자 문자열 | 소문자 스네이크 식별자로 진행 | R-018 |

### 1.1.0이 남긴 "다른 문서의 몫" 3건 — 전부 해소 *(A·B는 1.1.1 · C는 1.4.0)*

| # | 무엇 | 상태 |
|---|---|---|
| A | FR-003·TS-003이 요구하는 방 이름 카운터가 디자인에 없다 | **해소.** spec 3.0.0이 FR-003에서 카운터 요구를 걷어냈다. 함께 TS-003·TS-018·UX-007·SC-002가 정정되고 TS-045가 신설됐다 |
| B | spec §4 가정("편집 타이틀을 확정하지 않는다")이 낡았다 | **해소.** spec 3.0.0이 **FR-025**(생성 `공동방 만들기` · 편집 `방 편집`)로 승격하고 TS-044를 신설했다. §4 가정에서는 제거됐다 |
| ~~C~~ | ~~편집 보드가 방 이름을 생성 보드와 다른 컴포넌트로 그렸다~~ | **해소(1.4.0).** 편집 명세 보드 `2542-125922`가 방 이름을 `Textinput/Textfield`로 갈아 끼워 생성 보드 `2314-95301`과 같아졌다 — 두 갈래 중 "생성 보드가 의도" 쪽이다. 이 계획의 `MinoTextField` 결정은 무변경 → [research.md](./research.md) R-023. 사실 서술의 소유자는 [contracts/design-system-additions.md](./contracts/design-system-additions.md) §3 말미다 |

### 이 계획 밖에서 닫히는 것

열린 것은 **G·H 둘이다.** **3.1.0에서 D가 닫혔다** — 서버가 마지막 어긋남(`description.maxLength`)의 상한을 걷어냈다(R-035). H(세션 선행 조건)는 이 계획이 닫을 수 없고, G는 조건이 올 때까지 닫지 않는다. C·D·E·F는 닫힌 이력을 남겨 둔다. **3.1.0이 연 항목은 없다** — R-035·R-036은 둘 다 실측으로 확정된 것이라 미결로 남기지 않는다.

**H는 구현이 아니라 검증을 막는다.** 이 계획의 코드는 H와 무관하게 전부 만들어지고 단위 테스트로 검증되지만, 실기기 시나리오는 H가 닫히기 전까지 돌지 않는다([quickstart.md](./quickstart.md) §1).

| # | 무엇 | 닫는 조건 |
|---|---|---|
| ~~D~~ | ~~어긋남 `description.maxLength: 20` vs FR-005의 30자~~ | **해소(3.1.0).** 2026-08-31T12:51:29 재조회에서 서버가 **상한 자체를 걷어냈다** — 협의한 `30`을 넣는 대신 제약을 없앴으므로 spec과 부딪힐 값이 남지 않았다. 함께 `name.maxLength: 15`도 사라지고 `pattern`이 생겼다 → [research.md](./research.md) R-035 · [contracts/room-api.md](./contracts/room-api.md) §1의 인용을 재대조하며, 반영 여부는 [quickstart.md](./quickstart.md) S-9의 4번이 확인한다. **이 계획의 코드는 더 바뀌지 않는다** |
| H | 익명 세션 확보·유저 등록(`POST /api/v1/users`)이 배선되지 않아 모든 요청이 401로 막힌다([research.md](./research.md) R-028). **2026-08-28 재확인** — `EnsureAnonymousSessionUseCase`는 있으나 호출처가 없고, `ProfileRepositoryImpl`은 여전히 로컬 저장뿐이다 | `docs/specs/anonymous-auth-session`과 `docs/specs/profile`이 각각 배선하면 닫힌다. **이 계획은 코드를 하나도 더하지 않는다** |
| ~~E~~ | ~~방 이름의 자모 단독 허용 여부~~ | **해소(1.2.0).** spec 3.1.0이 FR-004를 `한글(완성형·자모)`로 고치고 EC-025를 신설했다 → [research.md](./research.md) R-021 |
| ~~F~~ | ~~방 설명의 글자 수 세는 단위가 spec 가정과 어긋난다~~ | **해소(1.3.0).** `MinoTextArea`의 상한·카운터를 grapheme 기준으로 고쳤다 → [research.md](./research.md) R-022 |
| G | `graphemeLength`가 `:core:design-system`의 `internal`이라 feature에서 보이지 않는다 | 방 이름은 FR-004의 허용 문자가 전부 코드 유닛 1개라 지금은 승격이 필요 없다. **FR-004의 허용 문자가 넓어지거나 두 번째 사용처가 생길 때** `:core:common:kotlin`으로 올린다 — 그때 `java.text.BreakIterator`의 JVM/Android 규칙 차이를 함께 판정한다(R-022) |

**규약 충돌은 열린 항목이 아니다.** 1.1.0이 R-008에서 "규약 충돌"로 보고한 것(Figma 컴포넌트셋 vs 이미지 에셋)은 [래스터 이미지 배치·포맷 ADR](../../adr/2026-08-19-raster-image-placement-and-format.md)이 이미 닫아 둔 문제이므로, 그 ADR을 근거로 지목하는 것으로 끝난다.
