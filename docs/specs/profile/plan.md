# 구현 계획: 프로필 설정 및 수정 (Profile Setup & Edit)

**대상 스펙 경로**: `docs/specs/profile`

**명세서**: [spec.md](./spec.md)

**기준 spec 버전**: 4.0.0

**최초 작성일**: 2026-08-18

**최종 수정일**: 2026-08-31

**버전**: 6.0.0

**참고**: 이 템플릿은 `/mino-plan` 명령으로 채워지며, 해당 명령의 정의가 실행 워크플로우를 설명한다.

## 요약 (Summary)

온보딩과 마이페이지가 공유하는 프로필 설정 화면을 **진입형 feature 모듈 `:feature:profile`** 로 신설한다. 두 진입점이 서로 다른 feature가 될 예정이고 feature 간 직접 의존이 금지되어 있으므로, 전환은 `:core:navigation`의 `ProfileLauncher` 계약 한 겹으로 받고 진입점은 Intent extra로, 저장 완료는 `RESULT_OK`로 주고받는다 — 다음 목적지는 호출자가 고른다.

**개정 6.0.0 — [spec 4.0.0](spec.md)이 기본 아바타를 목록 밖의 13번째 값으로 확정했다.** 닉네임 정책은 서버 정책표를 대조한 결과 spec 3.0.0과 **이미 같았고**([D55](research.md#d55-서버-문서-재조회2026-08-31-1603--바뀐-것이-없다)), 이번 개정의 실질은 전부 **아바타와 안내 문구** 쪽이다.

| 무엇이 | 5.1.0까지 | 6.0.0 |
|---|---|---|
| 기본 아바타 | 목록 12종의 **첫 항목**(`ProfileAvatar.Default = Person1` → 서버로 `red`) | **12종 밖의 13번째 값.** 미선택 저장이 서버 `enum`의 13번째 색으로 나간다 |
| 도메인 `ProfileAvatar` | 12항목 | **13항목** — 기본 아바타를 마지막에 선언하고 `Default`가 그것을 가리킨다 |
| design-system `MinoProfileAvatar` | 12항목 (**손대지 않는다**) | **12항목 그대로.** 기본 그림은 열거 항목이 아니라 `MinoProfileAvatarImage(avatar: MinoProfileAvatar?)`의 `null` 갈래가 그린다 |
| `ProfileMapper` 색 표 | 12행. `gray`를 **보내지 않는다** | **13행.** `gray`를 보낸다 — `enum`의 13개 값이 전부 쓰인다 |
| 필드 아래 안내 문구 | 상태와 무관하게 한 문구 | **오류 여부로 갈린다** — 평상시 `최대 15자까지 입력할 수 있어요.`, 오류 시 `한글·영문 2글자 이상을 입력해주세요.` |
| 닉네임 필수 표시 | (계약에 없었다) | **FR-001에 명시.** 구현은 이미 `required = true`이므로 작업이 생기지 않는다 |

**저장소가 같은 문제를 이미 풀어 두었고 그 답을 그대로 쓴다.** 방 대표 색이 정확히 이 구조다 — 도메인 [`RoomColor`](../../../core/domain/src/main/kotlin/team/mino/core/domain/model/RoomColor.kt)는 `GRAY`를 포함한 13항목이고, 디자인 시스템 [`MinoRoomColor`](../../../core/design-system/src/main/java/team/mino/core/designsystem/component/roomcolorchip/MinoRoomColor.kt)는 **12종만** 두며(KDoc: "회색 기본값·표시 이름·서버 식별자·그리드 배치는 여기에 없다"), 미선택은 소비처의 `MinoRoomColor?`가 `null`로 들고, [`RoomThumbnailFallback`](../../../core/common/ui/src/main/java/team/mino/core/common/ui/component/RoomThumbnailFallback.kt)이 `null -> room_thumbnail_gray`로 그린다. `RoomColor`의 KDoc이 든 이유가 spec §4의 문장과 같다 — **"'값 없음'이 아니라 색을 고르지 않은 방이 갖게 되는 색이다."** 근거와 기각한 대안은 [D53](research.md#d53-기본-아바타의-자리--도메인은-13항목-디자인-시스템-팔레트는-12종-그대로)에 있다.

**이 형태가 사게 되는 것 셋** — 아바타 그리드가 **한 줄도 바뀌지 않고**(`entries`가 여전히 12개), `MinoProfileAvatar`의 KDoc("'미선택'을 갖지 않는다")이 지켜지며, **프리필이 저절로 맞는다**(서버가 준 `gray`는 `image`가 `null`을 내므로 "고르지 않음"으로 복원돼 `지우기` 활성 조건이 옳게 계산된다). 13항목으로 넓혔다면 셋을 모두 잃고, 그리드 필터를 빠뜨려도 컴파일이 통과해 **13칸짜리 그리드가 조용히 그려진다.**

**이번 개정이 드러낸 것 하나 — 코드가 문서보다 앞서 있었다.** `profile_nickname_helper`가 이미 `최대 15자까지 입력할 수 있어요.`였고(`feature/273-onboarding-branding` 브랜치의 미커밋 작업분), spec 3.0.0 FR-011과 [화면 계약](contracts/profile-screen-contract.md)만 옛 문구를 들고 있었다. 어긋남이 화면에 드러나지 않은 이유는 그 문구가 오류일 때만 달라야 했는데 오류 갈래가 아예 없었기 때문이다. [D45](research.md#d45-프리필과-갱신의-순서--캐시로-먼저-채우고-갱신이-성공하면-조건부로-한-번-더)의 "문서가 코드보다 앞서간" 사례와 방향이 반대인 첫 사례다([D54](research.md#d54-닉네임-안내-문구--평상시와-오류를-다른-문구로-가른다)).

**MAJOR인 이유**: 확정된 계약 셋이 바뀐다 — ① design-system 공개 시그니처(`MinoProfileAvatarImage`)가 바뀌어 5.1.0의 "`:core:design-system`을 손대지 않는다"가 뒤집히고, ② 도메인 `ProfileAvatar`가 13항목이 되며 `ProfileAvatar.image`의 반환 타입이 nullable이 되고, ③ 상태 계약에서 `ProfileUiState.displayedAvatar`가 사라진다. `tasks.md`의 작업 분해를 다시 짜야 한다.

**개정 5.0.0 — develop 반영 후 전수 확인에서 `user` 태그 엔드포인트의 구현이 이미 있었다.** splash-screen이 `d783e03`으로 넣은 [`UserApiService`](../../../core/data/src/main/java/team/mino/core/data/network/service/UserApiService.kt)·`UserRemoteDataSource`가 `GET /api/v1/users/me`를 먼저 쓰고 있었고, 이 plan이 만들려던 `ProfileApiService`·`ProfileRemoteDataSource`는 **같은 서버 리소스의 두 번째 소유자**였다. 저장소의 규칙은 `ApiService`를 feature가 아니라 **OpenAPI 태그 단위**로 두는 것이다 — `RoomApiService` 하나가 `room` 태그 넷을 다 갖는다. 그래서 새 서비스를 만들지 않고 기존 소유자를 넓힌다.

| 무엇이 | 4.4.0까지 | 5.0.0 |
|---|---|---|
| `user` 엔드포인트 소유자 | `ProfileApiService` 신설 | **`UserApiService` 확장** — `hasProfile()`·`getMe()`·`register()`·`updateMe()` |
| 원격 DataSource | `ProfileRemoteDataSource` 신설 | **`UserRemoteDataSource` 확장** — `getMe()`·`register()`·`updateMe()` 추가 |
| `errorCode` 지역 파싱 | `ProfileApiService` | **`UserApiService`의 헬퍼 하나** — 두 함수가 공유. develop이 `UserRemoteDataSourceImpl`에 둔 수동 JSON 파싱을 이리로 옮긴다 |
| 스플래시 진입 판정 | (남의 feature) | **본문 미역직렬화를 보존한다** — `hasProfile()`을 `getMe()`와 합치지 않는다 |

**개정 5.1.0 — spec 3.0.0의 닉네임 15자 상한을 입력 차단으로 받는다.** [spec 3.0.0](spec.md)이 PRD 10.0.0을 따라 상한을 채택하면서 FR-014·UX-007이 생겼다. 설계는 그것을 **`ProfileViewModel`의 절단 한 겹**으로 받는다 — `ValidateNicknameUseCase`에도 `MinoTextField`에도 상한을 심지 않는다([D51](research.md#d51-닉네임-15자-상한의-강제-지점--viewmodel이-자른다)). 같은 저장소의 방 이름 15자가 정확히 이 형태이고, 세는 단위는 [ADR 2026-08-25](../../adr/2026-08-25-grapheme-count-for-text-input.md)가 이미 규율한다.

**같은 조회에서 서버가 먼저 움직인 것도 확인됐다.** 2026-08-31 재조회에서 `nickname.pattern`의 공백이 빠져(`^[\uAC00-\uD7A3A-Za-z ]+$` → `^[\uAC00-\uD7A3A-Za-z]+$`), 4.x부터 들고 있던 닉네임 어긋남 2건이 **양쪽에서 동시에** 닫혔다([D52](research.md#d52-서버-문서-재조회2026-08-31--닉네임-pattern에서-공백이-빠졌다)). 이제 클라이언트 규칙과 서버 스키마가 `2~15자 · 한글·영문 · 공백 불가`로 완전히 같고, **클라이언트가 통과시킨 닉네임을 서버가 길이·문자로 거절하는 경로가 없다.**

| 무엇이 | 5.0.0까지 | 5.1.0 |
|---|---|---|
| 닉네임 상한 | 클라이언트에 없음 — 16자 이상은 서버가 `400`으로 거절 | **`ProfileViewModel.changeNickname()`이 `take(15)`** — 서버에 닿지 않는다 |
| `ValidateNicknameUseCase` | 하한·문자만 판정(상한 없음) | **그대로다** — KDoc의 "상한을 두지 않는다"만 FR-014를 가리키게 고친다 |
| `MinoTextField` | `maxLength` 없음 | **그대로다** — design-system은 손대지 않는다 |
| 닉네임 어긋남 | 2건(상한·공백)을 알고 받아들임 | **0건** — spec이 상한을, 서버가 공백을 각각 좁혔다 |

**합치지 않은 이유가 이 개정에서 가장 중요한 판단이다.** `hasProfile()`을 `getMe(): ProfileResponse?` 하나로 통합하면 코드가 줄지만, **스플래시의 진입 게이트가 프로필 본문 스키마에 의존하게 된다.** develop의 테스트가 `{"data":{"id":1}}`로 성공을 확인하는 것은 픽스처가 느슨해서가 아니라 **판정이 본문에 기대지 않는다는 사실 자체를 지키고 있는 것**이다. 진입 게이트의 실패 허용치를 좁히는 대가가 중복 제거 이득보다 크다. 근거와 기각한 대안은 [D49](research.md#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다)에 있다.

**이 개정은 이 feature가 처음으로 이미 머지된 다른 feature의 코드를 고친다.** `UserApiService`·`UserRemoteDataSource`(+`Impl`)·그 테스트 넷이다. 그래서 [quickstart §4-4](quickstart.md)에 스플래시 회귀 시나리오를 세웠다 — 프로필이 잘 돌아도 이 넷이 깨지면 앱을 켜는 모든 사용자가 영향을 받는다.

**MAJOR인 이유**: 확정됐던 인터페이스 계약(`ProfileRemoteDataSource`)이 통째로 사라지고 다른 타입으로 이관된다. `ProfileApiService`를 만드는 작업이 `UserApiService` 확장 작업으로 대체되고, 커밋된 코드를 고치는 작업이 새로 생긴다. `tasks.md`의 작업 분해를 다시 짜야 한다.

**개정 4.4.0 — 설계는 건드리지 않고 문서의 빈틈 셋을 메운다.** [`/mino-analyze`](tasks.md)가 spec·plan·tasks를 교차 대조해 찾은 것이며, 셋 다 **이 plan이 이미 정한 것을 옮겨 적지 못한 자리**다. 새 결정은 없다.

| # | 무엇이 비어 있었나 | 어떻게 메웠나 |
|---|---|---|
| 1 | §규모/범위가 `DTO 4`로 셌다 | 4.0.0이 `ApiEnvelope`를 셈에 넣고 쓴 값이다. 4.3.0이 그 타입을 걷어냈으므로 **`DTO 3`**이 맞다. 구조 트리·[API 계약 §3](contracts/profile-api-contract.md)·tasks는 이미 3이었다 |
| 2 | §프로젝트 구조의 `core/data` 테스트 트리가 신규 파일 둘을 빠뜨렸다 | `ProfileApiServiceTest.kt`·`ProfileMapperTest.kt`를 트리에 올린다. [D43](research.md#d43-테스트-범위--mockengine-기반-데이터-레이어-테스트를-더한다)과 [repository 계약 §테스트 계약](contracts/profile-repository-contract.md)이 근거를 들고 있는데 트리에만 없어, **정당한 작업이 범위 이탈로 보였다** |
| 3 | 바뀌는 테스트 파일 표에 `ValidateNicknameUseCaseTest.kt`가 없었다 | repository 계약이 검증 대상에 `중간 공백`을 더했는데 이 표가 그 파일을 들지 않아 **작업으로 만들 수 없었다**([D48](research.md#d48-분석이-드러낸-문서의-빈틈--계약이-요구한-검증이-작업이-되지-못했다)). 표를 여섯 줄로 늘린다 |

**셋 다 같은 성격이다** — 근거는 있는데 그것을 실어 나르는 표·트리가 따라오지 못했다. 1·2는 표현 정정이고, **작업이 하나 늘어나는 것은 3뿐이라 이번 개정이 MINOR다.**

**개정 4.3.0 — 4.2.0의 `:core:data` 대조를 바로잡는다.** 4.2.0을 쓰는 동안 워크트리가 새 커밋(`e00563e`까지)으로 움직였고, `:core:data` 쪽 판정이 그 이전 트리를 근거로 삼았다. 프로필 파일 자체의 판정([D45](research.md#d45-프리필과-갱신의-순서--캐시로-먼저-채우고-갱신이-성공하면-조건부로-한-번-더)의 `.first()` 발견과 8+5 파일 표)은 새 트리에서 재확인해 **그대로 유효**하고, 바로잡히는 것은 넷이다.

| 4.2.0이 적은 것 | 실제 |
|---|---|
| `ApiEnvelope<T>` 신설 | **`MinoResponse<T>`가 이미 있다.** [ADR 2026-08-27](../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)이 Accepted로 지배하고, 그 ADR이 `ApiEnvelope<T>`라는 이름까지 검토한 뒤 기각했다. 재사용한다 |
| 봉투·에러 타입을 ADR로 승격 제안 | **봉투는 이미 승격 완료.** 남는 후보는 `ErrorResponse`와 에러 코드 취급 하나 |
| 방은 여전히 mock | **절반만 맞다.** `getRooms()`는 실서버, `getRoom`·`createRoom`·`updateRoom`만 mock |
| 서버가 만든 개인방이 앱에 안 보인다 | **더 이상 사실이 아니다.** 방 목록이 실서버이고 `:feature:sharereceiver`의 방 선택 시트가 `RoomType.PERSONAL`을 안다 |

근거와 새로 드러난 관례 긴장(`errorCode`에 분기하지 않는다)은 [D47](research.md#d47-develop-재대조--420의-대조가-옛-트리를-근거로-했다)에 있다.

**개정 4.2.0** — **이미 develop에 들어간 코드를 다시 읽어 plan의 전제를 대조했다.** 이 워크트리의 HEAD가 곧 develop이고 plan 3.0.0의 59개 작업이 전부 반영돼 있다. 대조에서 셋이 나왔다. ① **`prefill()`이 `observeProfile().first()` 한 번**이라, T081이 진입 갱신을 붙여도 서버 값이 화면에 반영되지 않는다 — data-model이 "그 흐름을 통해 다시 반영된다"고 적은 것은 **틀린 서술**이었다. 갱신 성공 시 조건부 재프리필로 닫는다([D45](research.md#d45-프리필과-갱신의-순서--캐시로-먼저-채우고-갱신이-성공하면-조건부로-한-번-더)). ② **도메인 테스트 fake가 별도 파일이 아니다** — 문서가 가리킨 경로에 파일이 없다([D46](research.md#d46-develop-대조로-드러난-사실--도메인-테스트-fake는-별도-파일이-아니다)). ③ **바뀌는 파일 표가 프로덕션만 담고 테스트 5개를 빠뜨렸다.** 아래 표를 고쳤다.

**대조에서 확인돼 그대로인 전제**: `ProfileLauncher` 호출자 없음(온보딩·마이페이지 feature 미존재) · 프로필을 표기하는 다른 화면 없음(`:feature:home`에 아바타 표기 없음) · 방은 여전히 mock · `NetworkModule`·`MinoIdentityProofPlugin`·`DataStoreModule` 그대로 · `ktor-client-mock`이 이미 `:core:data`의 테스트 의존 · `MinoProfileAvatarImage`가 `rippleSingleSelectable`로 선택 시맨틱을 이미 싣는다(spec TS-019 충족).

**개정 4.1.0** — 설계는 그대로 두고 두 가지를 반영한다. ① [spec 2.0.0](spec.md)이 FR-003·FR-010을 정정해 이 plan이 원칙 IV에 들고 있던 **미충족 2건이 닫혔다**(칸 단위 아바타 선택 표시·온보딩 뒤로가기 표현). ② 서버가 `avatar.color`를 13개 `enum`으로 확정해, 4.0.0의 잠정 문자열을 **에셋 실측으로 얻은 색 대응표**로 교체했다([D44](research.md#d44-아바타-서버-문자열--12종이-방-팔레트-12색에-1대1로-대응한다)). 아래 4.0.0 서술은 그대로 유효하다.

**이번 개정(4.0.0)이 하는 일은 하나다 — 실서버를 연결한다.** plan 2.0.0이 의도적으로 이연했던 원격 계층(D22)을 착수하고, 프로필의 원천을 로컬 DataStore에서 **꾹 서버**로 옮긴다. 로컬은 캐시로 내려간다([D36](research.md#d36-원격-연동-착수--원천은-서버-로컬-datastore는-캐시)). `ProfileRepository`는 `refreshProfile()`을 더해 세 멤버가 되고([D39](research.md#d39-repository-표면--observeprofile--refreshprofile--saveprofile-세-멤버)), 저장은 캐시 유무로 등록(`POST /api/v1/users`)과 수정(`PATCH /api/v1/users/me`)을 가른다([D38](research.md#d38-등록수정-분기--서버에-직접-묻고-캐시가-그-답을-들고-있는다)). 이로써 plan 2.0.0·3.0.0이 원칙 IV에 미충족으로 들고 있던 **spec §4의 서버 반영 가정과 FR-008의 개인방 생성이 닫힌다.**

**배포된 OpenAPI 문서와 대조한 결과, 아바타 계약이 plan이 전제한 것과 달랐다.** plan 1.1.0 이래 `Profile.avatarId: Int`의 유일한 근거였던 `Avatar { id: integer }`가 문서에 없고, 실제 계약은 `avatar: { color }`다(4.1.0 시점에는 13개 `enum`으로 확정됐다). 근거가 사라진 타입을 유지하지 않고 도메인에 `ProfileAvatar` enum을 신설한다 — [`RoomColor`](../../../core/domain/src/main/kotlin/team/mino/core/domain/model/RoomColor.kt)와 동형이며, 서버 문자열 표는 `ProfileMapper` 한 곳에 갇힌다([D37](research.md#d37-아바타-식별자--도메인-profileavatar-enum-서버-표현은-avatarcolor-문자열)). 함께 로컬 캐시 DataSource가 `ProfileEntry` DTO를 반환하게 되어, plan 3.0.0에서 **원칙 V를 FAIL로 만들었던 규약 충돌이 해소된다**([D42](research.md#d42-로컬-캐시-datasource는-profileentry-dto를-반환한다)).

화면은 바뀌지 않는다. `:core:design-system`의 아바타 12종·상단 바·아이콘, `:feature:profile`의 UiState·Intent·SideEffect·화면 구성이 그대로다 — 화면이 저장 경로를 모르게 설계해 둔 덕이다. 바뀌는 것은 아바타 매핑의 타입과 진입 시 갱신 호출 두 줄이다. 설계 근거는 [`research.md`](research.md), 데이터·상태는 [`data-model.md`](data-model.md), 계약 표면은 [`contracts/`](contracts/), 검증 절차와 **여전히 확인할 수 없는 것**은 [`quickstart.md`](quickstart.md)에 있다.

## 기술 컨텍스트 (Technical Context)

**언어/버전**: Kotlin 2.2.10, Jetpack Compose (버전 카탈로그 그대로)

**주요 의존성**: Hilt 2.59.2, Navigation Compose, `androidx-datastore-preferences` 1.2.1, Ktor(client·ContentNegotiation·kotlinx-serialization) — 모두 기존 카탈로그에 있고 **버전 카탈로그에 새 항목을 추가하지 않는다**. `ktor-client-mock`도 이미 `:core:data`의 테스트 의존이다

**저장소**: 원천은 **꾹 서버**다. 공유 `DataStore<Preferences>`(`core:data/storage/DataStoreModule`)는 그 응답의 캐시로 남고, `profile_nickname`·`profile_avatar` 2개 키를 한 트랜잭션에서 쓴다([D36](research.md#d36-원격-연동-착수--원천은-서버-로컬-datastore는-캐시)). 아바타 키는 `profile_avatar_id`(Int)에서 이름이 바뀌었고 마이그레이션은 두지 않는다

**참조 API 문서**: `https://api.gguk.org/api-docs-json` (Team MINO API 1.0.0) · **조회 시점 2026-08-31T16:03:55+09:00** · 조회 성공. **plan 6.0.0의 이번 조회에서 바뀐 것은 없다** — 세 오퍼레이션을 원문으로 펼쳐 같은 날 12:44 조회본과 대조했고 `nickname` 제약·아바타 13종 `enum`·`required`·`errorCode` 열거까지 전부 동일했다([D55](research.md#d55-서버-문서-재조회2026-08-31-1603--바뀐-것이-없다)). **이번에 움직인 것은 서버가 아니라 이 앱이다** — 13번째 값 `gray`를 이제 실제로 보낸다. 아래는 직전(12:44) 조회의 기록이다: **유저 엔드포인트 3종 중 `nickname.pattern`이 바뀌었다** — 문자 클래스 끝의 공백이 빠졌고 `PATCH` description도 `공백 포함`에서 `공백·숫자 불가`로 바뀌었다([D52](research.md#d52-서버-문서-재조회2026-08-31--닉네임-pattern에서-공백이-빠졌다)). 나머지(아바타 13종 `enum`·`id: uuid`·`401`·`409`·`PATCH`의 `required: []`)는 2026-08-28 조회본과 동일했다. 세 오퍼레이션의 태그는 모두 `user`이며, **이번 개정이 소유 서비스를 `UserApiService`로 바꾼 것은 서버 문서가 아니라 저장소 트리를 대조한 결과다**([D49](research.md#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다)). 소비하는 오퍼레이션은 `POST /api/v1/users` · `GET /api/v1/users/me` · `PATCH /api/v1/users/me` 세 개이고, 원문 스키마와 어긋남은 [`contracts/profile-api-contract.md`](contracts/profile-api-contract.md)가 인용해 소유한다. 이 줄이 이번 계약의 유일한 재현 근거다.

> **"다른 날 조회하면 문서가 다를 수 있다"가 세 번째로 일어났다.** ① plan 4.0.0의 조회(2026-08-27T22:15:17+09:00)와 4.0.0 사이 **약 3시간 만에** `avatar.color`가 자유 문자열(`maxLength 20`)에서 **13개 `enum`**으로 좁혀졌다 — 그 사이에 구현이 진행됐다면 저장이 통째로 실패했을 것이다. ② 4.3.0은 낡은 `:core:data` 트리를 근거로 대조했다. ③ **5.1.0의 이번 조회에서 `nickname.pattern`이 좁아졌다.**
>
> 세 번째가 앞의 둘과 다른 점은 **묻지 않은 것이 바뀌었다는 것**이다. 상한을 확인하러 갔다가 공백이 빠진 것을 발견했다. 특정 필드만 확인하는 것으로는 부족하고 오퍼레이션 전체를 펼쳐 대조해야 한다는 뜻이며, `openapi_digest.py show`가 원문을 그대로 내는 이유가 여기 있다.

**외부 계약**: 위 세 엔드포인트와, 앱 밖으로 여는 `:core:navigation`의 `ProfileLauncher` 하나

**테스트**: JUnit4 + Fake 구현체 JVM 단위 테스트에 **`MockEngine` 기반 원격 경로 테스트**를 더한다([D43](research.md#d43-테스트-범위--mockengine-기반-데이터-레이어-테스트를-더한다)) — 봉투 해제·DTO 매핑·`401 USER_NOT_REGISTERED` 처리·등록/수정 분기·원격 실패 시 캐시 불변. 검증 규칙·저장 왕복·ViewModel 상태 전이는 그대로 덮는다. Compose UI 테스트는 저장소에 선례가 없어 도입하지 않는다(research.md D12). `:feature:profile`은 `testOptions { unitTests { isReturnDefaultValues = true } }`가 있어야 ViewModel이 생성되고, 그 대가로 진입점이 테스트에서 통제되지 않는다([D31](research.md#d31-viewmodel-단위-테스트는-isreturndefaultvalues로-열고-진입점은-통제하지-않는다))

**대상 플랫폼**: Android minSdk 29 / targetSdk 36

**프로젝트 유형**: mobile-app, 다중 Gradle 모듈. `:feature:profile`은 plan 3.0.0에서 신설돼 이미 등록돼 있고, 이번 개정은 새 모듈을 만들지 않는다

**성능 목표**: spec SC-001(진입 후 60초 이내 저장 완료)·SC-005(아바타 선택 즉시 썸네일 반영)은 UX 목표이며 별도 계측 인프라를 두지 않는다. SC-003(앱 전체 즉시 반영)은 `observeProfile()` 구독으로 구조적으로 만족시킨다

**제약 조건**: 온보딩 진입에서는 화면을 벗어날 수 없고 저장 후 되돌아올 수도 없다(FR-010, EC-001, TS-018, EC-013). 저장 실패 시 입력값을 보존한다(FR-012, SC-006) — **이제 발화 원천이 실재한다**(네트워크 단절·서버 거절·`409`). 프로필 표기는 앱 전체에서 하나다(FR-007). 오프라인 저장·나중에 동기화는 다루지 않는다(spec §4) — 네트워크가 없으면 저장은 실패한다. 모든 요청은 Bearer 토큰을 요구하며 첨부는 기존 `MinoIdentityProofPlugin`이 이미 한다([D20](research.md#d20-인증-헤더--이번-범위에서-배선하지-않는다) 보정)

**규모/범위(6.0.0 개정분)**: 새 타입이 **0개**, 새 파일이 **에셋 1종(밀도 3벌)** 과 문자열 리소스 1줄뿐이다. 프로덕션 변경은 여덟 파일에 흩어진 국소 수정이고, `ProfileAvatarGrid`·`ProfileIntent`·`ProfileSideEffect`·`ProfileUiState`의 **필드**는 손대지 않는다. **셈이 늘지 않는데 등급이 MAJOR인 이유는 바뀌는 것이 공개 계약이기 때문이다** — 파일 수가 아니라 계약의 경계가 등급을 정한다.

**규모/범위(5.1.0 개정분)**: 5.0.0의 범위에 **입력 차단 한 겹**만 얹는다 — 새 타입·새 파일이 **0개**다. 프로덕션은 `ProfileViewModel`의 절단 한 줄과 상수 하나, `ValidateNicknameUseCase`의 KDoc 정정이고, 테스트는 `ProfileViewModelTest`에 케이스 3건이 붙는다. 새 모듈·새 계약·새 의존·design-system 변경이 없다.

**규모/범위(5.0.0 개정분)**: plan 3.0.0의 산출물은 이미 구현돼 있다. 이번 개정이 더하는 것은 **DTO 3**(`ProfileRequest`·`ProfileResponse`·`ErrorResponse` — 봉투 `MinoResponse<T>`는 이미 있어 세지 않는다)·**`Mapper` 1**·**도메인 enum 1개**(`ProfileAvatar`)다. 신규 모듈·신규 화면·신규 design-system 컴포넌트는 없다.

**5.0.0이 셈에서 뺀 것 둘** — `ApiService` 1과 `RemoteDataSource` 1쌍이다. 4.x가 신설로 세던 것이 **기존 타입의 확장**으로 바뀌었다([D49](research.md#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다)). 신규 파일이 줄고 대신 **이미 머지된 파일을 고치는 작업**이 생긴다.

**이미 구현된 것 중 바뀌는 파일은 열한 곳이다.** 이 목록 밖을 손대게 되면 그 자체가 설계에서 벗어난 신호다. 5.0.0이 아래 셋을 더했고, **그 셋은 이 feature가 아니라 splash-screen이 만든 파일이다.**

| 파일 | 무엇이 바뀌나 |
|---|---|
| `core/domain/.../model/Profile.kt` | `avatarId: Int` → `avatar: ProfileAvatar` |
| `core/domain/.../model/ProfileAvatar.kt` | **6.0.0에서 새로 목록에 든다.** 13번째 항목(기본 아바타)을 **마지막에** 더하고 `Default`가 그것을 가리키게 한다. KDoc의 "고정 12종"을 고친다 ([D53](research.md#d53-기본-아바타의-자리--도메인은-13항목-디자인-시스템-팔레트는-12종-그대로)) |
| `core/data/.../repository/mapper/ProfileMapper.kt` | **6.0.0: 색 표가 13행이 되고** KDoc의 "`gray`를 내보내는 경로는 없다"가 뒤집힌다 |
| `core/design-system/.../profileavatar/MinoProfileAvatarImage.kt` | **6.0.0에서 새로 목록에 든다.** `avatar: MinoProfileAvatar?`로 바꾸고 `null`이면 기본 그림을 그린다. **`MinoProfileAvatar` 열거와 `MinoProfileAvatarSize`·`Defaults`·토큰은 손대지 않는다** |
| `core/design-system/src/main/res/drawable-{m,x,xx}hdpi/` | **신규 에셋 1종 × 밀도 3벌** — 기본 아바타. 원본은 [`2314-95665`](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2314-95665&m=dev)이고 목록 첫 항목과 다른 그림임을 내려받을 때 확인한다 |
| `core/domain/.../repository/ProfileRepository.kt` | `refreshProfile()` 추가, KDoc의 "발화 원천이 없다" 단서 제거 |
| `core/domain/.../usecase/SaveProfileUseCase.kt` | 파라미터 타입 `Int` → `ProfileAvatar` |
| `core/data/.../datasource/ProfileLocalDataSource.kt`(+`Impl`) | 반환 타입 `Profile` → `ProfileEntry`, `clearProfile()` 추가, 아바타 키 변경 |
| `core/data/.../repository/ProfileRepositoryImpl.kt` | 원격을 앞에 두고 로컬을 캐시로. 등록/수정 분기와 캐시 갱신 순서 |
| `core/data/.../datasource/di/ProfileDataSourceModule.kt` | 원격 DataSource 바인딩 추가 |
| `feature/profile/.../main/model/ProfileAvatarId.kt` | 삭제 → `ProfileAvatarMapping.kt`(`ProfileAvatar` ↔ `MinoProfileAvatar` 매핑)로 대체. **6.0.0: `image`의 반환 타입이 `MinoProfileAvatar?`가 되고 기본 아바타만 `null`로 간다. `DefaultProfileAvatar` 상수는 삭제한다** — 기본값의 단일 출처가 도메인 하나로 모인다 |
| `feature/profile/.../main/vm/ProfileUiState.kt` | **6.0.0에서 새로 목록에 든다.** 파생 프로퍼티 `displayedAvatar`를 지운다. **필드는 하나도 바뀌지 않는다** |
| `feature/profile/.../main/screen/ProfileScreen.kt` | **6.0.0에서 새로 목록에 든다.** 썸네일에 `state.selectedAvatar`를 그대로 넘기고, `helperText`를 `isNicknameErrorVisible`로 갈라 끼운다 ([D54](research.md#d54-닉네임-안내-문구--평상시와-오류를-다른-문구로-가른다)) |
| `feature/profile/src/main/res/values/strings.xml` | **6.0.0에서 새로 목록에 든다.** 오류 문구 `한글·영문 2글자 이상을 입력해주세요.` 한 줄 추가. 기존 `profile_nickname_helper`는 그대로다 |
| `core/data/.../network/service/UserApiService.kt` | **(splash 소유)** `hasProfile()`·`getMe()`·`register()`·`updateMe()` 넷으로 확장. 기존 `getMe()`(Unit)는 `hasProfile()`로 이름이 바뀌며 **본문 미역직렬화는 그대로 둔다.** `401` 판정 헬퍼가 여기로 온다 |
| `core/data/.../datasource/UserRemoteDataSource.kt`(+`Impl`) | **(splash 소유)** `getMe()`·`register()`·`updateMe()` 추가. `Impl`의 수동 `Json.parseToJsonElement` 파싱을 걷어내고 위임만 남긴다 |
| `feature/profile/.../main/vm/ProfileViewModel.kt` | 진입 시 `refreshProfile()` 호출, **갱신 성공 시 조건부 재프리필**([D45](research.md#d45-프리필과-갱신의-순서--캐시로-먼저-채우고-갱신이-성공하면-조건부로-한-번-더)), 저장 시 넘기는 아바타 타입. **5.1.0: `changeNickname()`이 `value.take(NICKNAME_MAX_LENGTH)`로 자르고 자른 값으로 판정한다**([D51](research.md#d51-닉네임-15자-상한의-강제-지점--viewmodel이-자른다)). **6.0.0: `save()`가 `current.selectedAvatar?.profileAvatar ?: ProfileAvatar.Default`를 넘긴다**(`displayedAvatar`가 사라진 자리) |
| `core/domain/.../usecase/ValidateNicknameUseCase.kt` | **5.1.0에서 새로 목록에 든다.** 판정 로직은 그대로이고 **KDoc만 고친다** — "길이 상한은 두지 않는다"가 이제 오해를 낳는다. 상한이 없는 것이 아니라 **여기의 몫이 아니다**(FR-014는 `ProfileViewModel`이 지킨다). 시그니처·동작·테스트 통과 여부는 하나도 바뀌지 않는다 |

**테스트도 일곱 곳이 함께 바뀐다.** plan 4.1.0의 표가 프로덕션 파일만 담아 이것들을 빠뜨렸고, `ValidateNicknameUseCaseTest`는 4.4.0이, `UserRemoteDataSourceImplTest`는 5.0.0이 더했다.

| 파일 | 무엇이 바뀌나 |
|---|---|
| `core/domain/src/test/kotlin/.../usecase/ValidateNicknameUseCaseTest.kt` | **중간 공백 케이스 1건 추가**([repository 계약 §테스트 계약](contracts/profile-repository-contract.md)). 구현은 이미 옳다 — 테스트만 비어 있었다([D48](research.md#d48-분석이-드러낸-문서의-빈틈--계약이-요구한-검증이-작업이-되지-못했다)) |
| `core/domain/src/test/kotlin/.../usecase/SaveProfileUseCaseTest.kt` | 아바타 타입. **이 파일 안의 `private class FakeProfileRepository`에 `refreshProfile()`을 더한다** — 별도 파일이 아니다([D46](research.md#d46-develop-대조로-드러난-사실--도메인-테스트-fake는-별도-파일이-아니다)) |
| `core/data/src/test/java/.../repository/mapper/ProfileMapperTest.kt` | **6.0.0: 왕복 케이스가 12종 → 13종.** 기본 아바타 ↔ `gray`가 양방향으로 도는지, 모르는 문자열·`null`이 기본 아바타로 읽히는지 |
| `core/data/src/test/java/.../datasource/ProfileLocalDataSourceImplTest.kt` | `ProfileEntry` 반환·`clearProfile()` |
| `core/data/src/test/java/.../repository/ProfileRepositoryImplTest.kt` | 등록/수정 분기·캐시 불변·미등록 처리 |
| `core/data/src/test/java/.../datasource/UserRemoteDataSourceImplTest.kt` | **(splash 소유)** `401` 판정이 `UserApiService`로 옮겨간 만큼 그쪽 테스트로 이동한다. 여기 남는 것은 위임 확인이다. **`{"data":{"id":1}}` 픽스처가 지키던 "판정이 성공 본문에 기대지 않는다"는 성질을 잃지 않는다** |
| `feature/profile/src/test/java/.../fake/FakeProfileRepository.kt` | `refreshProfile()` 구현·갱신 호출 관측 |
| `feature/profile/src/test/java/.../main/vm/ProfileViewModelTest.kt` | 아바타 타입·갱신 호출·**조건부 재프리필 가드**. **6.0.0: 기본 아바타 케이스 2건 추가** — 미선택 저장이 `ProfileAvatar.Default`를 넘기는지(TS-023), 기본 아바타로 저장된 프로필을 프리필하면 `selectedAvatar`가 `null`이 되어 `지우기`가 비활성인지([D53](research.md#d53-기본-아바타의-자리--도메인은-13항목-디자인-시스템-팔레트는-12종-그대로)). **5.1.0: 상한 케이스 3건 추가** — 16자 입력 시 15자에서 멈추고 유효 유지(TS-017) · 30자를 한 번에 넣으면 앞 15자만 반영(TS-020) · 자른 값으로 판정한다(원본으로 판정하면 화면에 없는 글자가 오류를 만든다) |

**손대지 않는 것(6.0.0 기준)**: `NetworkModule`·`HttpClientConfig`·`MinoIdentityProofPlugin`·`DataStoreModule`·`settings.gradle.kts`·`app/build.gradle.kts`·`ProfileAvatarGrid`·`ProfileIntent`·`ProfileSideEffect`·`ProfileUiState`의 **필드**·`MinoProfileAvatar` 열거·`MinoProfileAvatarSize`·`MinoProfileAvatarDefaults`·`ProfileAvatarTokens`·`MinoTextField`·방(`RoomMock*`)의 목. 버전 카탈로그에도 손대지 않는다. **5.1.0까지 여기 있던 "`:core:design-system` 전체"는 빠졌다** — 이번 개정이 그 모듈의 공개 시그니처 하나와 에셋을 건드린다.

## 헌법 준수 확인 게이트 (Constitution Check)

*게이트: Phase 0 리서치 전에 반드시 통과해야 한다. Phase 1 설계 후 재확인한다.*

| 원칙/기준 | 판정 | 근거 |
|---|---|---|
| I. 단일 출처 문서화 | PASS | 규약을 링크로만 지목하고 본문을 옮기지 않았다. 화면·계약 정의는 문서마다 소유자를 하나로 두고 서로 참조한다 |
| II. 레이어 경계와 의존 방향 | PASS(5.0.0에서 강화됨) | `:feature:profile`은 `:core:domain`만 알고 `:core:data`를 의존하지 않는다. feature 간 결합은 `:core:navigation`의 `ProfileLauncher` 한 겹뿐이다(research.md D1). `:core:design-system`은 도메인을 모른다 — 아바타 enum이 저장 식별자를 갖지 않는다(D4). DI 바인딩은 구현을 소유한 모듈의 `di/`에 둔다 |
| III. 결정과 실패는 기록으로 남는다 | PASS, 승격 대상 2건 | research.md D1~D26은 이 feature 로컬 결정이다. 이번 개정에서 물러난 D13~D16·D21은 지우지 않고 취소선과 함께 남겼고, 대체 결정 D22~D26을 덧붙였다. D22(원격 이연)는 이 feature의 **작업 순서** 결정이라 승격 대상이 아니다 — 원격이 실제로 붙을 때 D13(원천과 캐시)·D15(목 엔진 소스셋 분리)가 되살아나면 그때 승격을 판단한다. D4(아바타 12종 소유)는 승격 후보로 유지한다 |
| IV. 명세가 구현에 선행한다 | PASS, 미충족 2건·어긋남 1건 보고 | 모든 설계 항목이 spec의 FR-·UX-·EC-·SC- 항목에서 도출됐고, plan에만 있고 spec에 근거가 없는 요구사항은 없다. spec이 다른 문서로 넘긴 범위(개인방 생성 규칙·온보딩 나머지 스텝)를 끌어오지 않았다. **이번 범위가 충족하지 않는 것 2건** — spec §4의 "저장은 서버 반영을 포함한다" 가정과 FR-008의 개인방 생성 트리거다. 둘 다 원격 연동이 있어야 성립하며, 설계로 봉합하지 않고 미충족 사실을 여기와 [quickstart.md §5](quickstart.md)에 드러낸다(research.md D22·D17). **어긋남 1건** — 서버 `Nickname` 규칙(공백 허용·15자 상한)이 spec §5의 확정(공백 불가·상한 없음)과 다르다. 이번 범위에는 서버 거절 경로가 없어 어긋남이 드러나지 않으므로, 원격 연동 전에 spec을 정리해야 한다(research.md D19) |
| V. 컨벤션은 게이트다 | PASS | 브랜치는 `feature/159-profile-setup/plan`으로 이미 분기돼 있다. 새 컴포넌트는 M3 패턴(Defaults·token)을 따르고, 에러 소비는 `launchSafely`·`runCatchingDomain`·`onDomainFailure`만 쓴다 |
| 기술 표준 — 디자인 토큰·실측 판정 | PASS(조건부) | `MinoProfileAvatarImage`·`MinoTopNavigation`의 값 판정은 구현 단계에서 Figma 원본과 대조해 정한다([figma-design-fidelity.md](../../conventions/figma-design-fidelity.md)). 이 plan은 컴포넌트의 존재·역할·공개 API까지만 결정했다 |
| 기술 표준 — 빌드 검증 | PASS | 확인 최소선은 `./gradlew :app:assembleQaDebug`이며 quickstart가 그것을 절차로 담고 있다. 이번 범위는 flavor별로 코드가 갈리지 않아 qa·prod가 같게 동작한다 |

**Phase 1 설계 후 재평가(1.1.0)**: 원격 계층이 들어오면서 `:core:data`의 표면이 늘었지만 경계는 그대로다 — `:feature:profile`은 여전히 `:core:domain`만 알고, DTO는 데이터 레이어 밖으로 나가지 않는다. 1.0.0이 남긴 "spec 가정과 구현의 시점 차이"는 원격 연동을 실제로 설계하면서 닫혔다. 남은 판정 변화는 원칙 IV의 어긋남 1건(닉네임 규칙)뿐이며 설계로 봉합하지 않고 보고한다. `Complexity Tracking`에 올릴 항목은 없다.

**Phase 1 설계 후 재평가(2.0.0)**: 원격 계층을 들어내면서 `:core:data`의 표면이 줄었고, 경계 판정이 뒤집힌 게이트는 없다 — 오히려 이번 범위가 손대는 기존 파일이 세 곳으로 줄어 원칙 II의 위험이 낮아졌다. 판정이 실질적으로 바뀐 곳은 원칙 IV 하나다: 1.1.0이 "닫혔다"고 적은 **spec 가정과 구현의 시점 차이가 다시 열렸다.** 1.1.0과 다른 점은 이번엔 그 차이가 계약 부재 때문이 아니라 **의도적으로 고른 작업 순서** 때문이라는 것이고, 그래서 미충족 항목(spec §4 서버 반영 가정·FR-008 개인방 생성)과 그것이 닫히는 조건을 문서가 직접 든다(research.md D22·D24, quickstart.md §4). 헌법 원칙 IV의 "근거가 없는 빈틈은 지어내지 않는다"에 따라 없는 것을 있는 것처럼 설계하지 않았으므로 게이트는 PASS다. `Complexity Tracking`에 올릴 항목은 없다.

**Phase 1 설계 후 재평가(5.0.0)**: 판정이 뒤집힌 게이트는 없고, **원칙 I과 II는 오히려 강해졌다.**

- **원칙 I(단일 출처)** — 이번 개정이 닫은 것이 정확히 이 원칙의 위반이다. `GET /api/v1/users/me`의 경로·미등록 판정 규칙·`USER_NOT_REGISTERED` 상수가 **두 벌로 갈라져 있었다.** 4.4.0까지의 설계는 그 사실을 몰랐기에 위반을 예정하고 있었고, 5.0.0이 소유자를 `UserApiService` 하나로 모아 닫는다([D49](research.md#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다)).
- **원칙 II(레이어 경계)** — `401` 본문 파싱이 `UserRemoteDataSourceImpl`에서 `UserApiService`로 옮겨가면서 Ktor 타입(`ResponseException`·`bodyAsText()`)이 `network/` 밖으로 새지 않게 된다. [`core/data/README.md`](../../../core/data/README.md) §5가 요구하는 "DataSource는 출처 호출만"에 develop의 현재 코드보다 가까워진다.
- **원칙 III(기록)** — 승격 후보가 둘이다. ① `ApiService`의 단위는 feature가 아니라 **OpenAPI 태그**라는 규칙 — `RoomApiService`·`PinApiService`·`UserApiService`가 이미 따르고 있으나 **어느 문서도 적어 두지 않아** 이 feature가 어길 뻔했다. 다른 feature가 같은 실수를 할 자리다. ② D4(아바타 12종 소유)는 4.x부터의 후보로 유지한다.
- **새로 드러난 규약 긴장 1건(보고 대상)** — [`core/data/README.md`](../../../core/data/README.md) §4는 지역 catch를 "해당 **DataSource**에서 병용한다"고 적고 develop이 그대로 따랐는데, [ADR 2026-08-28](../../adr/2026-08-28-error-body-type-and-no-error-code-leaf.md)은 `ApiService`를 골랐다. **이 plan이 규약 문서를 고쳐 봉합하지 않는다**(스킬 범위 밖). 설계는 ADR을 따르고, 문구 정리는 완료 보고로 넘긴다.
- **범위가 넓어진 사실은 숨기지 않는다** — 이 개정으로 이 feature는 **이미 머지된 splash-screen의 파일 넷을 고친다.** spec에 없는 요구사항을 더한 것이 아니라 같은 요구사항(FR-006·FR-007)을 구현하는 자리가 옮겨간 것이지만, 남의 feature를 건드리는 이상 회귀 확인이 따라야 한다. [quickstart §1·§4-4](quickstart.md)가 그것을 절차로 담는다.

`Complexity Tracking`에 올릴 항목은 없다 — 타입 수가 늘지 않고 오히려 둘 줄었다.

## 프로젝트 구조 (Project Structure)

### 문서 (이번 Feature)

```text
docs/specs/profile/
├── plan.md              # 이 파일 (/mino-plan 산출물)
├── research.md          # Phase 0 산출물
├── data-model.md        # Phase 1 산출물
├── quickstart.md        # Phase 1 산출물
├── contracts/           # Phase 1 산출물
│   ├── profile-screen-contract.md
│   ├── profile-launcher-contract.md
│   ├── profile-repository-contract.md
│   ├── profile-api-contract.md      # 4.0.0에서 되살아남 — 배포 OpenAPI 문서 기준으로 새로 씀
│   └── design-system-contract.md
└── tasks.md             # /mino-task 산출물 (이 실행이 만들지 않음)
```

### 소스 코드 (Repository Root 기준)

모바일(Android) 다중 모듈 구조를 그대로 따른다. **신규·변경 모듈만** 적는다.

```text
feature/profile/src/main/java/team/mino/feature/profile/
└── main/
    ├── model/ProfileAvatarMapping.kt                # 신설(ProfileAvatarId.kt 대체) — ProfileAvatar ↔ MinoProfileAvatar 매핑
    │                                                #        6.0.0: image 반환이 MinoProfileAvatar? — 기본 아바타만
    │                                                #        null. DefaultProfileAvatar 상수 삭제 (D53)
    ├── vm/ProfileUiState.kt                         # 변경(6.0.0) — 파생 displayedAvatar 삭제. 필드는 그대로
    ├── screen/ProfileScreen.kt                      # 변경(6.0.0) — 썸네일에 selectedAvatar를 그대로 전달,
    │                                                #        helperText를 오류 여부로 갈아 끼움 (D54)
    └── vm/ProfileViewModel.kt                       # 변경 — 진입 시 refreshProfile() · 저장 인자 타입
                                                     #        5.1.0: changeNickname()이 15자로 자른다 (D51)
                                                     #        6.0.0: save()가 ProfileAvatar.Default를 기본값으로
feature/profile/src/main/res/values/strings.xml      # 변경(6.0.0) — 오류 문구 1줄 추가 (D54)
feature/profile/src/test/java/team/mino/feature/profile/
├── fake/FakeProfileRepository.kt                    # 변경 — refreshProfile() 구현·갱신 호출 관측
└── main/vm/ProfileViewModelTest.kt                  # 변경 — 타입·갱신 호출·재프리필 가드
                                                     #        5.1.0: 상한 케이스 3건 추가 (TS-017·TS-020)
                                                     #        6.0.0: 기본 아바타 케이스 2건 (TS-023·프리필 null)
                                                     # Activity·Shell·NavHost·di/·component/(그리드)·
                                                     # Intent·SideEffect는 그대로

core/navigation/                                     # 손대지 않는다 — ProfileLauncher·ExtraTag 모두 그대로

core/domain/src/main/kotlin/team/mino/core/domain/
├── model/Profile.kt                                 # 변경 — avatarId: Int → avatar: ProfileAvatar
├── model/ProfileAvatar.kt                           # 신규 — 도메인 enum (D37)
│                                                    #        6.0.0: 13항목 — 선택 12 + 기본 아바타(마지막).
│                                                    #        Default가 기본 아바타를 가리킨다 (D53)
├── repository/ProfileRepository.kt                  # 변경 — refreshProfile() 추가
├── usecase/SaveProfileUseCase.kt                    # 변경 — 파라미터 타입
└── usecase/ValidateNicknameUseCase.kt               # 변경(5.1.0) — KDoc만. 판정 로직·시그니처는 그대로 (D51)
core/domain/src/test/kotlin/team/mino/core/domain/
├── usecase/ValidateNicknameUseCaseTest.kt           # 변경 — 중간 공백 케이스 1건 추가 (D48)
└── usecase/SaveProfileUseCaseTest.kt                # 변경 — 타입 + 파일 안 private FakeProfileRepository에
                                                     #        refreshProfile() 추가. 별도 fake 파일은 없다(D46)

core/data/src/main/java/team/mino/core/data/
├── network/dto/request/ProfileRequest.kt            # 신규 — nickname · AvatarRequest(color)
├── network/dto/response/ProfileResponse.kt          # 신규 — id · nickname · AvatarResponse? · createdAt
│                                                    # MinoResponse.kt(공용 봉투)는 이미 있다 — 만들지 않는다
├── network/dto/response/ErrorResponse.kt            # 신규 — 공용 { errorCode, message? } (ADR 2026-08-28)
├── network/service/UserApiService.kt                # 변경(splash 소유) — hasProfile()·getMe()·register()·
│                                                    # updateMe() · 봉투 해제 · 401 판정 헬퍼 (D49)
├── datasource/UserRemoteDataSource.kt(+Impl)        # 변경(splash 소유) — getMe()·register()·updateMe()
│                                                    # 추가. Impl은 수동 JSON 파싱을 버리고 위임만 (D49)
├── datasource/ProfileLocalDataSource.kt(+Impl)      # 변경 — ProfileEntry 반환 · clearProfile() (D42)
├── datasource/di/ProfileDataSourceModule.kt         # 변경 — 로컬 바인딩만. 원격은 UserDataSourceModule이
│                                                    # 이미 갖는다 — 이 모듈에 원격을 더하지 않는다 (D49)
├── repository/ProfileRepositoryImpl.kt              # 변경 — 원격이 앞, 로컬이 캐시. UserRemoteDataSource 주입
└── repository/mapper/ProfileMapper.kt               # 신규 — DTO ↔ 도메인 · 아바타 문자열 표 소유
                                                     #        6.0.0: 표 13행 — 기본 아바타 ↔ gray (D53)
core/data/src/test/java/team/mino/core/data/
├── network/UserApiServiceTest.kt                    # 신규 — MockEngine · 봉투 해제 · 401 분기 ·
│                                                    # hasProfile()이 본문 스키마에 안 기대는지 (D43·D49)
├── repository/mapper/ProfileMapperTest.kt           # 신규 — 아바타 색 왕복. 6.0.0: 13종 (D43·D53)
├── datasource/UserRemoteDataSourceImplTest.kt       # 변경(splash 소유) — 401 판정 케이스는 위로 이동,
│                                                    # 여기 남는 것은 위임 확인 (D49)
├── datasource/ProfileLocalDataSourceImplTest.kt     # 변경 — ProfileEntry · clearProfile()
└── repository/ProfileRepositoryImplTest.kt          # 변경 — 등록/수정 분기 · 캐시 불변
                                                     # network/di/NetworkModule · plugin/ · storage/ 는 손대지 않는다
                                                     # ProfileApiService·ProfileRemoteDataSource는 만들지 않는다 (D49)

core/design-system/src/main/java/.../component/profileavatar/
└── MinoProfileAvatarImage.kt                        # 변경(6.0.0) — avatar: MinoProfileAvatar? · null이면 기본
                                                     #        그림. 열거·Size·Defaults·토큰은 그대로 (D53)
core/design-system/src/main/res/drawable-{m,x,xx}hdpi/
└── (기본 아바타 WebP 1종 × 3벌)                      # 신규(6.0.0) — 원본 2314-95665
                                                     # 상단 바·아이콘·아바타 12종 에셋은 그대로
app/build.gradle.kts · settings.gradle.kts           # 손대지 않는다 — 모듈은 이미 등록돼 있다
```

**데이터 흐름 결정(5.0.0)**: 저장은 `ProfileViewModel → SaveProfileUseCase → ProfileRepository → UserRemoteDataSource(원천) → 성공하면 ProfileLocalDataSource(캐시)`이고, 읽기는 `observeProfile()` Flow 하나로 되돌아온다. **분기는 `ProfileRepositoryImpl` 안에 하나뿐이다** — 캐시가 비었으면 등록, 있으면 수정([D38](research.md#d38-등록수정-분기--서버에-직접-묻고-캐시가-그-답을-들고-있는다)). 화면도 UseCase도 그 분기를 알지 않는다.

**"원격 성공 → 캐시 갱신" 순서는 장식이 아니라 사용자에게 보이는 규칙이다**(FR-012·SC-006). 실패했는데 캐시가 바뀌면 화면을 다시 열었을 때 저장되지 않은 값이 프리필된다. 데이터 레이어의 불변식이므로 단위 테스트가 지킨다.

[research.md D24](research.md#d24-원격-연동이-붙을-때-바뀌는-지점을-지금-고정한다)가 plan 2.0.0에서 미리 고정한 "원격이 붙을 때 바뀌는 지점" 표는 이번 개정의 입력이 됐고, **일곱 줄 중 두 줄이 틀렸다.** 대조 결과는 그 항목에 표로 남겼다 — 이연 표의 정확도는 그것이 딛고 선 계약의 안정성을 넘지 못한다는 것이 이번에 배운 것이다.

**구조 결정**: 프로필 설정은 **진입형** feature다 — 탭 셸의 그래프에 편입되는 화면이 아니라 온보딩·마이페이지 양쪽이 Activity로 여는 독립 플로우이고, 바텀 네비게이션을 노출하지 않는다(UX-006). 따라서 `ProfileActivity`·`ProfileShell`·`ProfileNavHost`·`di/`(Launcher) 골격을 모두 갖는다([feature-module.md](../../architecture/feature-module.md) 1장). 화면이 하나여도 NavHost를 유지하는 이유는 인자 복원과 화면 조회 로깅이 거기 딸려 오기 때문이다(research.md D11). 온보딩·마이페이지 feature는 이번 범위 밖이며, 이 계획은 그들이 호출할 계약([profile-launcher-contract.md](contracts/profile-launcher-contract.md))까지만 확정한다.

**Phase 1 설계 후 재평가(3.0.0)**: 이번 개정은 설계가 아니라 **구현이 드러낸 사실**로 촉발됐다. 게이트 판정이 실제로 바뀐 곳은 셋이다.

| 원칙/기준 | 2.0.0 판정 | 3.0.0 판정 | 무엇이 바뀌었나 |
|---|---|---|---|
| III. 결정과 실패는 기록으로 남는다 | PASS, 승격 대상 2건 | **PASS** | D4가 [ADR](../../adr/2026-08-25-profile-avatar-assets-in-design-system.md)로 승격돼 승격 대상이 남지 않는다. 헌법 Governance의 "기록 없는 예외는 없다" 요건도 함께 충족됐다([D35](research.md#d35-아바타-12종의-소유-결정을-adr로-승격했다)) |
| IV. 명세가 구현에 선행한다 | PASS, 미충족 2건·어긋남 1건 | **PASS, 미충족 4건·어긋남 2건** | 원격 이연분(spec §4 서버 반영·FR-008 개인방)은 그대로다. 여기에 **FR-003의 칸 단위 선택 표시**([D28](research.md#d28-아바타-선택-상태의-시각-표시를-만들지-않는다))와 **FR-010의 "노출하되 비활성"**([D29](research.md#d29-온보딩-진입에서-뒤로가기를-노출하지-않는다))이 더해졌다. 둘 다 원본에 근거가 없거나 사용자가 다른 쪽을 확정한 것이며, 설계로 봉합하지 않고 [quickstart.md §5](quickstart.md)와 계약이 미충족을 든다 |
| V. 컨벤션은 게이트다 | PASS | **FAIL — MUST 위반 1건이 남는다** | 아래 참조 |

**원칙 V가 뒤집힌 이유 — 규약과 계약이 정면 충돌한다.** [`core:data` README](../../../core/data/README.md) §5·§2가 "DataSource는 DTO만 반환하고 변환하지 않는다"로 정하는데, [repository 계약](contracts/profile-repository-contract.md) §저장 계층이 "원격이 없어 DTO가 없으니 `ProfileLocalDataSourceImpl`이 `Preferences`에서 `Profile`을 직접 조립한다"를 명시 지시했다. **어느 쪽으로도 규약을 다 지킬 수 없다** — Preferences에는 자연적 DTO가 없고, README가 키 상수를 DataSource에 두게 해서 변환을 `RepositoryImpl`로 옮기면 키가 밖으로 샌다.

이 plan은 그 충돌을 **설계로 봉합하지 않는다.** 해소는 (a) README에 "DTO 없는 로컬 DataSource" 갈래를 보완하거나 (b) 계약을 바꿔 `ProfileEntry`를 도입하는 것이며, 규약 문서는 이 스킬이 고치지 않으므로 **판단을 사용자에게 남긴다.** 같은 성격의 규약 충돌 하나(아바타 에셋 배치)는 [ADR](../../adr/2026-08-25-profile-avatar-assets-in-design-system.md)로 예외 기록을 남겨 닫았고, 그 ADR이 §1 규약 정리를 후속 과제로 든다.

**판정이 바뀌지 않은 것**: 원칙 I·II와 기술 표준은 그대로다. 규범 감사가 레이어 경계·가시성·DI 소유·패키지 구조·M3 패턴·에러 처리 배선·원격 이연 범위(D24)·에셋 포맷·Compose Lint를 전수 확인해 위반 없음을 보고했다. `Complexity Tracking`에 올릴 항목은 없다.

**Phase 1 설계 후 재평가(4.0.0)**: 배포된 OpenAPI 문서와의 대조가 이번 개정을 이끌었다. 게이트 판정이 바뀐 곳은 셋이고, **두 곳이 FAIL·미충족에서 PASS로 회복한다.**

| 원칙/기준 | 3.0.0 판정 | 4.0.0 판정 | 무엇이 바뀌었나 |
|---|---|---|---|
| III. 결정과 실패는 기록으로 남는다 | PASS | **PASS, 승격 대상 1건** | D36~D43을 덧붙이고, 뒤집힌 D18·D22·D23은 지우지 않고 취소선으로 남겼다. 이미 취소선이던 D13~D16·D21에는 plan 4.0.0의 처분(되살림/종결)을 한 줄씩 적어, 다음 개정이 같은 후보를 다시 검토하지 않게 했다. **[D40](research.md#d40-응답-봉투와-에러-코드--공용-dto를-신설한다)의 `ApiEnvelope`·`ErrorResponse`는 승격 대상이다** — 서버를 소비하는 모든 feature를 구속한다 |
| IV. 명세가 구현에 선행한다 | PASS, 미충족 4건·어긋남 2건 | **PASS, 미충족 2건·어긋남 4건** | **닫힌 것 2건**: spec §4의 "저장은 서버 반영을 포함한다" 가정과 FR-008의 개인방 생성이 `POST /api/v1/users`로 충족된다([D36](research.md#d36-원격-연동-착수--원천은-서버-로컬-datastore는-캐시)·[D17](research.md#d17-개인방내-장소-생성--서버가-등록과-함께-처리한다확정) 보정). **남는 미충족 2건**: FR-003의 칸 단위 선택 표시([D28](research.md#d28-아바타-선택-상태의-시각-표시를-만들지-않는다))와 FR-010의 "노출하되 비활성"([D29](research.md#d29-온보딩-진입에서-뒤로가기를-노출하지-않는다)) — 이번 개정이 화면을 건드리지 않아 그대로다. **어긋남은 2건에서 4건으로 늘었다** — 닉네임 상한·공백에 더해 아바타 표현(`color`)과 미등록 신호(`401`)가 서버 문서와 어긋난다. 넷 다 설계로 봉합하지 않고 [API 계약 §2](contracts/profile-api-contract.md)가 서버팀 협의 항목으로 세운다. plan에만 있고 spec에 근거가 없는 요구사항은 없다 |
| V. 컨벤션은 게이트다 | **FAIL — MUST 위반 1건** | **PASS** | plan 3.0.0을 FAIL로 만든 `core:data` README §5·§2 충돌이 닫혔다. 충돌의 원인은 "원격이 없어 DTO가 없다"였는데 그 전제가 사라졌다 — 로컬 DataSource가 `ProfileEntry`를 반환하고 변환은 `ProfileRepositoryImpl`이 하므로 두 규칙을 모두 지킨다([D42](research.md#d42-로컬-캐시-datasource는-profileentry-dto를-반환한다)). 키 상수는 여전히 DataSource 구현체 안에 남아 README §5의 세 번째 규칙도 지켜진다. 사용자가 이 해소안을 골랐다 |

**판정이 바뀌지 않은 것**: 원칙 I·II와 기술 표준이다.

- **원칙 II** — 원격 계층이 들어와도 경계는 그대로다. `:feature:profile`은 여전히 `:core:domain`만 알고, DTO는 `ProfileRepositoryImpl` 밖으로 나가지 않는다. 새로 생긴 `ProfileAvatar`는 `:core:domain`에 있고 `:core:design-system`은 그것을 모른다 — 두 enum이 12항목으로 같지만 의존이 생기지 않는다([D37](research.md#d37-아바타-식별자--도메인-profileavatar-enum-서버-표현은-avatarcolor-문자열)). **서버 문자열은 `ProfileMapper` 한 파일 안에 갇힌다.**
- **기술 표준(디자인)** — 이번 개정은 화면·컴포넌트를 건드리지 않으므로 새로 대조할 Figma 값이 없다.
- **기술 표준(빌드 검증)** — 최소선은 그대로 `./gradlew :app:assembleQaDebug`이고, 여기에 서버가 필요 없는 `:core:data:test`가 실질적인 게이트로 더해진다([quickstart.md §1](quickstart.md)).

`Complexity Tracking`에 올릴 항목은 없다.

**Phase 1 설계 후 재평가(4.1.0)**: 설계 자체는 하나도 바뀌지 않았다 — 모듈 경계·인터페이스·상태 계약·데이터 흐름이 4.0.0 그대로다. 바뀐 것은 **근거**이며, 그 결과 게이트 하나의 부기가 개선된다.

| 원칙/기준 | 4.0.0 판정 | 4.1.0 판정 | 무엇이 바뀌었나 |
|---|---|---|---|
| IV. 명세가 구현에 선행한다 | PASS, 미충족 2건·어긋남 4건 | **PASS, 미충족 0건·어긋남 3건** | **미충족 2건이 모두 닫혔다** — [spec 2.0.0](spec.md)이 FR-003에서 칸 단위 선택 표시 요구를 걷어내고(대신 보조 수단 전달을 명시해 [D28](research.md#d28-아바타-선택-상태의-시각-표시를-만들지-않는다)의 접근성 시맨틱에 근거가 생겼다) FR-010을 "노출하지 않는다"로 정정했다([D29](research.md#d29-온보딩-진입에서-뒤로가기를-노출하지-않는다)). **spec을 구현에 맞춘 것이 아니라, 설계가 근거를 들어 보고한 것을 spec이 받아들인 결과다.** 어긋남은 4건에서 3건으로 준다 — 아바타 표현이 서버 `enum` 확정과 실측 대응표로 닫혔고([D44](research.md#d44-아바타-서버-문자열--12종이-방-팔레트-12색에-1대1로-대응한다)), 닉네임 2건은 spec §5가 "알고 받아들이는 어긋남"으로 확정해 협의 항목에서 내렸다([D19](research.md#d19-닉네임-규칙-불일치--클라이언트는-spec을-따르고-서버-거절은-저장-실패로-받는다) 보정). 남는 3건은 닉네임 거절 코드 미문서화·미등록이 `401`·응답 `avatar` nullable이며 모두 서버 문서의 빈틈이다 |

**판정이 바뀌지 않은 것**: 원칙 I·II·III·V와 기술 표준 전부다.

- **원칙 II** — 서버가 값 도메인을 통째로 바꿨는데 **고친 곳은 `ProfileMapper`의 표 하나**다. [D37](research.md#d37-아바타-식별자--도메인-profileavatar-enum-서버-표현은-avatarcolor-문자열)이 세운 경계(서버 문자열을 매퍼 한 파일에 가둔다)가 실제로 값어치를 했다는 증거이며, 도메인·화면·디자인 시스템·캐시는 이번 변경을 알지 못한다.
- **원칙 III** — [D44](research.md#d44-아바타-서버-문자열--12종이-방-팔레트-12색에-1대1로-대응한다)를 덧붙이고 D37·D19·D28·D29에 보정을 달았다. 뒤집힌 결정은 없어 새 취소선이 없다. [D40](research.md#d40-응답-봉투와-에러-코드--공용-dto를-신설한다)의 ADR 승격 대상은 **그대로 남아 있다**(T085).
- **기술 표준(디자인)** — 아바타 색 대응이 에셋 실측에 근거하므로 [figma-design-fidelity.md](../../conventions/figma-design-fidelity.md)의 대조 대상이 하나 늘었다. `Person10` ↔ `brown` 한 칸이 **미검증**이며 통과로 세지 않는다.

`Complexity Tracking`에 올릴 항목은 없다.

**Phase 1 설계 후 재평가(4.2.0)**: 이번 개정은 spec도 서버 문서도 아닌 **이미 반영된 코드**가 촉발했다. 게이트 판정이 뒤집힌 곳은 없고, 원칙 하나가 **위반 직전에서 회수됐다.**

| 원칙/기준 | 4.1.0 판정 | 4.2.0 판정 | 무엇이 바뀌었나 |
|---|---|---|---|
| I. 단일 출처 문서화 | PASS | **PASS (위반 1건 회수)** | 아바타 12행 값 표가 [API 계약 §2](contracts/profile-api-contract.md)·[data-model.md §4](data-model.md)·[research.md D44](research.md#d44-아바타-서버-문자열--12종이-방-팔레트-12색에-1대1로-대응한다) **세 곳에 복제돼 있었다.** 값의 소유자는 API 계약 §2 하나로 두고 data-model의 사본을 걷어냈다. research D44는 hex·토큰이라는 **근거**를 담아 성격이 다르므로 남긴다 |
| IV. 명세가 구현에 선행한다 | PASS, 미충족 0건·어긋남 3건 | **PASS, 미충족 0건·어긋남 3건** | 판정은 그대로다. 다만 **FR-006의 충족 근거가 실제로는 성립하지 않고 있었다** — data-model이 "갱신이 흐름을 통해 반영된다"고 적었지만 `prefill()`이 `.first()`라 반영되지 않는다. [D45](research.md#d45-프리필과-갱신의-순서--캐시로-먼저-채우고-갱신이-성공하면-조건부로-한-번-더)가 설계로 닫았다. **문서가 코드보다 앞서간 사례이며, 헌법이 경계하는 방향과 반대라 기록해 둔다** |

**판정이 바뀌지 않은 것**: 원칙 II·III·V와 기술 표준이다.

- **원칙 III** — [D45](research.md#d45-프리필과-갱신의-순서--캐시로-먼저-채우고-갱신이-성공하면-조건부로-한-번-더)·[D46](research.md#d46-develop-대조로-드러난-사실--도메인-테스트-fake는-별도-파일이-아니다)을 덧붙였다. 뒤집힌 결정이 없어 새 취소선이 없다. [D40](research.md#d40-응답-봉투와-에러-코드--공용-dto를-신설한다)의 ADR 승격 대상은 그대로 남아 있다(T085).
- **원칙 V** — 재프리필은 기존 `launchSafely` + `runCatchingDomain` 배선 안에서 돈다. 새 에러 경로도 새 리프도 만들지 않는다.

**이번 개정이 남기는 교훈**: [D24](research.md#d24-원격-연동이-붙을-때-바뀌는-지점을-지금-고정한다)의 예측 표가 "`:feature:profile` 전체 그대로"를 절반만 맞혔던 것과 같은 성격의 일이 또 나왔다. **문서가 코드의 현재 형태를 확인하지 않고 쓰이면, 성립하지 않는 서술이 게이트를 통과한다.** 구현 착수 전 `develop` 대조를 한 번 더 돌린 것이 이번에 값어치를 했다.

`Complexity Tracking`에 올릴 항목은 없다.

**Phase 1 설계 후 재평가(4.3.0)**: 설계 경계는 그대로이고, 원칙 하나가 **위반 직전에서 다시 회수됐다.**

| 원칙/기준 | 4.2.0 판정 | 4.3.0 판정 | 무엇이 바뀌었나 |
|---|---|---|---|
| I. 단일 출처 문서화 | PASS (위반 1건 회수) | **PASS (위반 1건 추가 회수)** | `ApiEnvelope<T>`를 신설했다면 **`MinoResponse<T>`와 같은 일을 하는 타입이 둘**이 됐다. 규칙의 출처는 [ADR 2026-08-27](../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)이고 프로필은 링크로 따르기만 한다. 엔드포인트 추가 절차도 [`core/data/README.md`](../../../core/data/README.md) §8을 지목만 한다 |
| III. 결정과 실패는 기록으로 남는다 | PASS, 승격 대상 2건 | **PASS, 승격 대상 1건(내용 축소)** | 승격 후보가 "봉투 + 에러 코드" 둘에서 **에러 코드 하나**로 줄었다. 봉투는 다른 feature가 먼저 승격해 두었다. [D47](research.md#d47-develop-재대조--420의-대조가-옛-트리를-근거로-했다)을 덧붙였고 뒤집힌 결정이 없어 새 취소선은 없다 |

**판정이 바뀌지 않은 것**: 원칙 II·IV·V와 기술 표준이다. `errorCode`를 읽는 `getMe()`의 지역 catch가 저장소에서 유일한 사례가 되지만, 이는 관례 위반이 아니라 **관례가 다루지 않는 경우**다 — 다른 곳은 실패를 가를 필요가 없고 프로필은 `401` 하나가 성격이 다른 두 상태를 겸한다([D47](research.md#d47-develop-재대조--420의-대조가-옛-트리를-근거로-했다)).

**이번 개정이 남기는 교훈**: [D24](research.md#d24-원격-연동이-붙을-때-바뀌는-지점을-지금-고정한다)의 예측 오차, [D45](research.md#d45-프리필과-갱신의-순서--캐시로-먼저-채우고-갱신이-성공하면-조건부로-한-번-더)의 문서 선행에 이어 **세 번째**다. 원인은 앞의 둘과 다르다 — 문서가 틀린 게 아니라 **읽은 시점이 낡았다.** 여러 feature가 `:core:data`를 동시에 넓히는 국면에서는 대조에 유효기간이 있다. [quickstart.md §2](quickstart.md)의 착수 직전 재조회 대상에 **API 문서뿐 아니라 `:core:data` 트리도** 포함된다.

`Complexity Tracking`에 올릴 항목은 없다.

**Phase 1 설계 후 재평가(4.4.0)**: 설계 산출물이 하나도 바뀌지 않았다 — 모듈 경계·인터페이스·상태 계약·데이터 흐름이 4.3.0 그대로다. 이번 개정은 **plan이 이미 정한 것을 plan 자신이 옮겨 적지 못한 자리**를 메운다. 게이트 판정이 바뀐 곳은 없고, 원칙 하나의 부기가 나아진다.

| 원칙/기준 | 4.3.0 판정 | 4.4.0 판정 | 무엇이 바뀌었나 |
|---|---|---|---|
| I. 단일 출처 문서화 | PASS (위반 1건 추가 회수) | **PASS** | 값의 소유자는 그대로 두고 **참조가 끊긴 자리 셋을 이었다.** `중간 공백`의 소유자는 [repository 계약 §테스트 계약](contracts/profile-repository-contract.md)이고 plan은 파일 표로 그것을 지목만 한다. 계약 본문을 plan으로 옮겨 적지 않았다 |
| IV. 명세가 구현에 선행한다 | PASS, 미충족 0건·어긋남 3건 | **PASS, 미충족 0건·어긋남 3건** | 판정도 개수도 그대로다. 다만 **계약이 요구한 검증 하나가 작업이 되지 못하고 있었다** — 요구사항이 빠진 것이 아니라 그것을 작업으로 옮기는 표가 빠져 있었다([D48](research.md#d48-분석이-드러낸-문서의-빈틈--계약이-요구한-검증이-작업이-되지-못했다)). spec 근거는 FR-002와 §4 가정(공백은 유효하지 않은 문자)에 이미 있다 |

**판정이 바뀌지 않은 것**: 원칙 II·III·V와 기술 표준 전부다. 새 모듈·새 의존·새 예외 리프가 없고, 새 컴포넌트가 없어 대조할 Figma 값도 없다.

**이번 개정이 남기는 교훈**: [D24](research.md#d24-원격-연동이-붙을-때-바뀌는-지점을-지금-고정한다)·[D45](research.md#d45-프리필과-갱신의-순서--캐시로-먼저-채우고-갱신이-성공하면-조건부로-한-번-더)·[D47](research.md#d47-develop-재대조--420의-대조가-옛-트리를-근거로-했다)은 모두 **plan이 코드나 서버를 잘못 읽어서** 생긴 일이었다. 이번은 다르다 — 읽은 것은 옳았고 **문서 안에서 근거와 표가 따로 놀았다.** 근거를 적는 곳(계약·기술 컨텍스트)과 작업을 낳는 곳(파일 표·구조 트리)이 나뉘어 있는 한 이 종류의 누락은 또 생기며, 그것을 잡는 것이 [`/mino-analyze`](tasks.md)의 몫이라는 것이 이번에 확인됐다.

`Complexity Tracking`에 올릴 항목은 없다.

**Phase 1 설계 후 재평가(5.1.0)**: 판정이 뒤집힌 게이트는 없고, **원칙 IV의 부기가 이 feature 들어 가장 좋아진다.**

| 원칙/기준 | 5.0.0 판정 | 5.1.0 판정 | 무엇이 바뀌었나 |
|---|---|---|---|
| IV. 명세가 구현에 선행한다 | PASS, 미충족 0건·어긋남 3건 | **PASS, 미충족 0건·어긋남 1건** | **닉네임 어긋남 2건이 서로 다른 쪽에서 닫혔다.** **상한**은 [spec 3.0.0](spec.md)이 PRD 10.0.0을 따라 15자를 채택해서(FR-014·UX-007 신설), **공백**은 **서버가 `pattern`에서 공백을 빼서**다([D52](research.md#d52-서버-문서-재조회2026-08-31--닉네임-pattern에서-공백이-빠졌다)). 남는 어긋남 1건은 응답 `avatar`가 nullable인 이유가 문서에 없는 것 하나다(미등록 `401`은 협의 항목으로 남지만 어긋남이 아니라 문서의 빈틈이다). **plan에만 있고 spec에 근거가 없는 요구사항은 없다** — FR-014·UX-007이 [D51](research.md#d51-닉네임-15자-상한의-강제-지점--viewmodel이-자른다)의 유일한 근거다 |

**판정이 바뀌지 않은 것**: 원칙 I·II·III·V와 기술 표준 전부다.

- **원칙 I(단일 출처)** — 상한 15라는 **값**의 소유자를 하나로 두었다. `ProfileViewModel`의 상수 하나이며, 계약·데이터 모델·quickstart는 그것을 **지목만** 한다. 세는 단위의 근거는 이 문서가 다시 쓰지 않고 [ADR 2026-08-25](../../adr/2026-08-25-grapheme-count-for-text-input.md)를 링크한다.
- **원칙 II(레이어 경계)** — 상한이 **화면 레이어 안에 머문다.** 도메인·데이터·디자인 시스템 어디에도 15가 새지 않고, `:core:design-system`을 넓히지 않는다. `ValidateNicknameUseCase`에 심었다면 도메인이 표현 계층의 제약을 알게 됐을 자리다.
- **원칙 III(기록)** — [D51](research.md#d51-닉네임-15자-상한의-강제-지점--viewmodel이-자른다)·[D52](research.md#d52-서버-문서-재조회2026-08-31--닉네임-pattern에서-공백이-빠졌다)를 덧붙이고 [D19](research.md#d19-닉네임-규칙-불일치--클라이언트는-spec을-따르고-서버-거절은-저장-실패로-받는다)에 보정을 달았다. **D19에 취소선을 긋지 않는다** — 결정이 틀린 것이 아니라 적용 대상이 없어졌다. **승격 후보 1건**: `MinoTextField`를 쓰는 필드의 상한은 ViewModel이 자른다는 규칙이 이제 **두 번째 사례**다(방 이름 15자, 닉네임 15자). 어느 문서도 적어 두지 않아 두 feature가 각자 발견했다.
- **원칙 V(컨벤션)** — 새 에러 경로도 새 예외 리프도 없다. 상한 초과는 실패가 아니므로 `runCatchingDomain` 배선에 닿지 않는다.
- **기술 표준(디자인)** — 새 컴포넌트가 없어 대조할 Figma 값이 없다. **카운터를 그리지 않는 것 자체가 디자인 근거를 갖는다** — 원본에 카운터 노드가 없다(UX-007).

**이번 개정이 남기는 교훈**: [D24](research.md#d24-원격-연동이-붙을-때-바뀌는-지점을-지금-고정한다)·[D45](research.md#d45-프리필과-갱신의-순서--캐시로-먼저-채우고-갱신이-성공하면-조건부로-한-번-더)·[D47](research.md#d47-develop-재대조--420의-대조가-옛-트리를-근거로-했다)·[D48](research.md#d48-분석이-드러낸-문서의-빈틈--계약이-요구한-검증이-작업이-되지-못했다)에 이어 다섯 번째이고, **원인이 또 다르다.** 앞의 넷은 이 저장소 안(코드·문서)의 문제였다. 이번은 **밖이 움직였다** — 서버가 스키마를 좁혔고 PRD가 규칙을 바꿨는데 둘 다 이 저장소에 통지가 오지 않는다. 알아채는 수단은 재조회와 기준 버전 대조뿐이며, 그것이 **spec 헤더의 기준 PRD 버전**과 **API 계약의 조회 시점**이 존재하는 이유다. 둘 다 이번에 값어치를 했다.

`Complexity Tracking`에 올릴 항목은 없다 — 타입도 파일도 늘지 않는다.

**Phase 1 설계 후 재평가(6.0.0)**: 판정이 뒤집힌 게이트는 없다. 이번 개정의 성격은 **저장소에 이미 있는 답을 찾아 쓴 것**이라, 원칙 I과 III가 그 사실로 강해진다.

| 원칙/기준 | 5.1.0 판정 | 6.0.0 판정 | 무엇이 바뀌었나 |
|---|---|---|---|
| I. 단일 출처 문서화 | PASS | **PASS (위반 1건 회수)** | 기본 아바타의 **값**을 두 곳에 두려던 자리를 하나로 모았다. plan 5.x의 설계에는 도메인의 `ProfileAvatar.Default`와 feature의 `DefaultProfileAvatar` 상수가 **둘 다** 있었고, 후자가 전자에서 파생하고는 있었지만 화면이 참조하는 이름이 따로 존재했다. 6.0.0은 `DefaultProfileAvatar`를 지우고 화면이 `ProfileAvatar.Default`를 직접 지목하게 한다 |
| IV. 명세가 구현에 선행한다 | PASS, 미충족 0건·어긋남 1건 | **PASS, 미충족 0건·어긋남 1건(성격 약화)** | 남은 어긋남은 응답 `avatar`의 nullable 하나인데, 앱이 "고르지 않음"을 `gray`로 표현하게 되면서 **`null`과 `gray`가 같은 상태를 뜻하는 두 표현**이 됐고 처리가 하나로 모인다([API 계약 §2](contracts/profile-api-contract.md) 6번). 여전히 서버 문서가 `null`의 발생 조건을 말하지 않으므로 협의 항목에는 남는다. **plan에만 있고 spec에 근거가 없는 요구사항은 없다** — FR-015가 [D53](research.md#d53-기본-아바타의-자리--도메인은-13항목-디자인-시스템-팔레트는-12종-그대로)의, FR-011이 [D54](research.md#d54-닉네임-안내-문구--평상시와-오류를-다른-문구로-가른다)의 유일한 근거다 |

**판정이 바뀌지 않은 것**: 원칙 II·III·V와 기술 표준이다.

- **원칙 II(레이어 경계)** — **경계가 시험대에 올랐고 통과했다.** 기본 아바타는 도메인 개념(FR-015)이지만 그림도 필요해, 두 모듈이 그것을 어떻게 나눠 갖느냐가 이번 설계의 전부였다. 답은 **도메인이 값을, 디자인 시스템이 그림을 갖되 열거는 넓히지 않는 것**이다 — `:core:design-system`은 여전히 `:core:domain`을 모르고, 서버 문자열은 `ProfileMapper` 한 파일에 갇힌 채다. 반대로 `MinoProfileAvatar`를 13항목으로 넓혔다면 디자인 시스템이 "기본값"이라는 도메인 개념을 들게 됐을 자리다.
- **원칙 III(기록)** — [D53](research.md#d53-기본-아바타의-자리--도메인은-13항목-디자인-시스템-팔레트는-12종-그대로)·[D54](research.md#d54-닉네임-안내-문구--평상시와-오류를-다른-문구로-가른다)·[D55](research.md#d55-서버-문서-재조회2026-08-31-1603--바뀐-것이-없다)를 덧붙였다. **뒤집힌 결정이 하나 있는데 취소선을 긋지 않는다** — [D37](research.md#d37-아바타-식별자--도메인-profileavatar-enum-서버-표현은-avatarcolor-문자열)의 "`gray`는 보내지 않는다"는 결정이 아니라 **그 시점의 사실 서술**이었고(프로필에 미선택 상태가 없다는 전제), spec 4.0.0이 그 전제를 바꿨다. 값 표의 소유자인 [API 계약 §2](contracts/profile-api-contract.md)가 경위를 든다. **승격 후보 2건은 그대로 유지된다**(에러 코드 취급, `MinoTextField` 상한을 ViewModel이 자르는 규칙) — 이번 개정이 새 후보를 만들지는 않았다. 다만 **"도메인 13 / 팔레트 12 / 미선택은 `null`"이 방 색에 이어 두 번째 사례가 됐다**는 사실은 세 번째가 나올 때 승격을 판단할 근거로 남긴다.
- **원칙 V(컨벤션)** — 새 에러 경로도 새 예외 리프도 없다. 새 에셋은 밀도별 WebP([ADR 2026-08-01](../../adr/2026-08-01-webp-for-raster-images.md))로 기존 12종과 같은 형식이고, 배치는 [ADR 2026-08-25](../../adr/2026-08-25-profile-avatar-assets-in-design-system.md)가 정한 모듈 그대로다. `MinoProfileAvatarImage`의 파라미터를 nullable로 바꾸는 것은 M3 패턴(`Defaults`·`token/`)의 구조를 건드리지 않는다.
- **기술 표준(디자인)** — **대조 대상이 둘 늘었다**: 기본 아바타 에셋(목록 첫 항목과 다른 그림인지 + 배경 hex로 `gray` 대응 근거를 굳힌다)과 안내 문구 자리(위치·크기·색). [quickstart §6](quickstart.md)이 절차로 담는다. **오류 문구의 글자가 원본과 다른 것은 위반이 아니라 spec 4.0.0이 확정한 의도다.**
- **기술 표준(빌드 검증)** — 최소선은 그대로 `./gradlew :app:assembleQaDebug`이고, 이번 개정에서는 **컴파일이 실질적인 안전망 노릇을 한다.** 도메인 enum에 항목이 하나 늘면 `ProfileAvatarMapping`의 전수 `when`이 깨져 매핑을 빠뜨릴 수 없다. **다만 `ProfileMapper`의 색 표는 `Map`이라 컴파일이 잡아 주지 못한다** — 13번째 줄을 빠뜨리면 `getValue`가 저장 시점에 던진다. [quickstart §4-6](quickstart.md)의 28번이 그것을 받는 확인이다.

**이번 개정이 남기는 교훈**: [D24](research.md#d24-원격-연동이-붙을-때-바뀌는-지점을-지금-고정한다)·[D45](research.md#d45-프리필과-갱신의-순서--캐시로-먼저-채우고-갱신이-성공하면-조건부로-한-번-더)·[D47](research.md#d47-develop-재대조--420의-대조가-옛-트리를-근거로-했다)·[D48](research.md#d48-분석이-드러낸-문서의-빈틈--계약이-요구한-검증이-작업이-되지-못했다)·[D52](research.md#d52-서버-문서-재조회2026-08-31--닉네임-pattern에서-공백이-빠졌다)에 이어 여섯 번째이고, **이번에도 원인이 다르다.** 앞의 다섯은 무언가가 어긋나 있었다. 이번에 드러난 것은 **저장소가 같은 문제를 이미 풀어 두었는데 이 plan이 그것을 찾지 않았다면 다르게 풀었으리라는 것**이다 — `MinoRoomColor`를 읽지 않았다면 `MinoProfileAvatar`를 13항목으로 넓히는 쪽이 자연스러워 보였고, 그리드 필터 누락이라는 조용한 버그를 예정했을 것이다. **설계에 착수하기 전에 같은 형태의 선례를 찾는 것이 새 결정을 내리는 것보다 먼저다.**

또 하나, [D54](research.md#d54-닉네임-안내-문구--평상시와-오류를-다른-문구로-가른다)가 드러낸 **"코드가 문서보다 앞서간"** 첫 사례가 나왔다. [D45](research.md#d45-프리필과-갱신의-순서--캐시로-먼저-채우고-갱신이-성공하면-조건부로-한-번-더)의 반대 방향이며, 둘을 함께 잡는 수단은 같다 — 착수 전 대조다.

`Complexity Tracking`에 올릴 항목은 없다 — 새 타입이 0개이고, 늘어난 것은 열거 항목 하나와 에셋 한 장이다.

## 복잡도 추적 (Complexity Tracking)

해당 없음 — Constitution Check에서 정당화가 필요한 위반이 발견되지 않았다.
