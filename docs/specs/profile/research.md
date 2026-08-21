# 리서치: 프로필 설정 및 수정

Phase 0 산출물. `plan.md`에서 내린 설계 결정을 누적한다. 항목은 지우지 않으며, 뒤집힌 결정은 취소선과 함께 남기고 새 항목을 덧붙인다. 이 feature 안에서만 유효한 선택은 여기에, 다른 feature에도 구속력을 갖는 결정은 [`docs/adr/`](../../adr/README.md) 승격 대상이다(완료 보고에서 표시).

---

## D1. 화면의 소유 모듈 — 진입형 `:feature:profile` 신설

- **Decision**: 프로필 설정 화면을 진입형 feature 모듈 `:feature:profile`로 신설하고, 전환 계약 `ProfileLauncher`와 Intent extra 키를 `:core:navigation`에 둔다. 온보딩과 마이페이지는 이 Launcher로 같은 Activity를 연다.
- **Rationale**: spec FR-001은 두 진입점이 **같은 화면**을 쓸 것을 요구한다. 두 호출자(온보딩·마이페이지)는 서로 다른 feature 모듈이 될 것이고, feature 간 직접 의존은 금지되어 있어([modularization.md](../../architecture/modularization.md) 금지 규칙) 화면을 어느 한쪽 안에 두면 나머지가 닿을 수 없다. 전환 계약을 `:core:navigation`에 모으는 형태가 이 저장소의 유일한 feature 간 결합 수단이다([feature-navigation.md](../../architecture/feature-navigation.md) 1장).
- **Alternatives considered**:
  - **`:feature:mypage` 내부 Route로 두기** — 미머지 브랜치 `feature/152-mypage-settings-screen/plan`의 계획이 택한 형태다. 그 계획은 온보딩 재사용을 자기 spec의 비목표로 두어 성립했지만, 이번 spec은 두 진입점을 모두 범위에 넣으므로 성립하지 않는다. 기각.
  - **온보딩 feature 내부 Route로 두기** — 마이페이지가 온보딩 모듈을 의존해야 해 같은 이유로 기각.
  - **`:core:common:ui`에 공용 화면으로 두기** — 화면이 ViewModel·도메인 계약을 갖게 되어 공용 UI 모듈의 책임(재사용 컴포넌트·Composable 유틸)을 넘는다. 기각.
- **주의**: 위 미머지 브랜치의 `contracts/profile-setup-contract.md`와 이 결정은 배치가 충돌한다. 두 브랜치가 합쳐지는 시점에 마이페이지 쪽 프로필 Route를 제거하고 Launcher 호출로 바꿔야 한다(완료 보고에 실음).
- (plan 1.0.0에서 결정)

## D2. 진입점 구분과 저장 결과 반환 — Intent extra + Activity Result

- **Decision**: 호출자가 `EXTRA_PROFILE_ENTRY_POINT`로 진입점(`onboarding` / `edit`)을 넘긴다. 프로필 저장이 끝나면 화면은 `setResult(RESULT_OK)` 후 자신을 닫고, **다음 화면은 호출자가 정한다.** 온보딩 진입에서는 뒤로가기(상단 버튼·시스템 back)를 모두 막고, 마이페이지 진입에서는 연다.
- **Rationale**: FR-008·FR-009는 진입점마다 저장 후 목적지가 다르다고 말하는데, 목적지(온보딩 공동방 생성 스텝·마이페이지)는 모두 이 모듈 **밖**에 있다. 결과만 돌려주고 다음 목적지를 호출자가 고르면 프로필 모듈이 온보딩 그래프도 마이페이지 탭도 모르는 상태로 남는다. 결과 전달은 `ActivityLauncher`가 이미 지원하는 `resultLauncher` 경로를 쓴다([core:navigation README](../../../core/navigation/README.md)).
- **Alternatives considered**:
  - **저장된 프로필의 존재 여부로 모드를 추론** — 저장 실패 후 재진입이나 프로필이 있는 상태에서의 온보딩 재실행 같은 경우에 모드가 흔들린다. 진입점은 호출자가 아는 사실이므로 추론하지 않는다. 기각.
  - **프로필 모듈이 다음 화면을 직접 연다** — 온보딩·방 생성 Launcher를 이 모듈이 알아야 해, 호출자가 늘 때마다 이 모듈이 바뀐다. 기각.
  - **Activity 두 개(생성용·수정용)** — 화면 구성이 같다는 FR-001을 코드 중복으로 갚는다. 기각.
- (plan 1.0.0에서 결정)

## ~~D3. 프로필 저장소 — 로컬 DataStore 단독, 원격은 인터페이스 뒤로 이연~~ — 재검토됨(plan 1.1.0)

> 이 항목이 전제한 "프로필 API 계약이 저장소 어디에도 없다"가 깨졌다. [꾹 API 초안](https://raw.githubusercontent.com/mash-up-kr/Team-MINO-Node/refs/heads/KKardy/GM-111-outline-prd/docs/swagger.yaml)이 유저 등록·조회·수정 엔드포인트를 정의하고 있어, 원격 연동을 이연할 근거가 사라졌다. 새 결정은 [D13](#d13-프로필-저장-경로--원격-api가-원천-로컬-datastore는-캐시)이며, 아래 본문은 기각 이력으로 남긴다.


- **Decision**: `Profile`은 `core:data`의 공유 Preferences DataStore(`storage/DataStoreModule`)에 저장한다. `core:domain`의 `ProfileRepository` 인터페이스로 감싸 두고, 원격 API가 확정되면 `ProfileRepositoryImpl`만 교체한다. 저장 실패는 `MinoDomainException`으로 전파되는 계약을 지금 확정해 두되, 로컬 단독 구간에서는 그 실패 원천이 사실상 발생하지 않는다.
- **Rationale**: 저장소 어디에도 프로필 관련 네트워크 계약(엔드포인트·DTO)이 없다. 없는 서버 계약을 지어내면 명세에 근거 없는 설계가 된다(헌법 원칙 IV). DataStore 채택은 이미 [ADR](../../adr/2026-07-27-preferences-datastore-local-storage.md)로 서 있고, 같은 프로필 데이터를 다루는 미머지 브랜치의 마이페이지 계획도 같은 판단(로컬 단독)을 했다 — 두 feature가 갈라지지 않게 같은 결정을 따른다.
- **spec과의 어긋남**: spec §4는 "프로필 저장은 서버 반영을 포함한다"를 가정으로 적고 있다. 이 결정은 그 가정을 **구현 시점으로 미루는 것**이며 부정하지 않는다. 서버 계약이 확정되면 원격 DataSource를 붙이는 후속 작업이 필요하고, 그때 FR-012(저장 실패)의 실패 원천이 실제로 생긴다. 완료 보고에 남긴다.
- **Alternatives considered**: (a) 임시 원격 API를 만들어 연결 — 존재하지 않는 계약을 임의로 만드는 것이라 기각. (b) 서버 계약이 확정될 때까지 plan을 멈춤 — 화면·상태·모듈 경계는 서버와 무관하게 결정 가능하므로 전체를 막을 이유가 없어 기각.
- **ADR 승격 후보**: 프로필이라는 앱 전역 데이터의 저장 위치 결정이라 다른 feature(마이페이지·코멘트 작성자 표기·방 멤버)도 구속한다.
- (plan 1.0.0에서 결정)

## D4. 아바타 12종의 소유 — `:core:design-system`

- **Decision**: 아바타 12종을 `:core:design-system`이 소유한다. `component/profileavatar/`에 12항목 `enum class MinoProfileAvatar`와 그 에셋을 두고, 서버·저장 식별자(문자열) ↔ enum 매핑은 이 enum이 아니라 소비 feature가 갖는다.
- **Rationale**: [방 대표 색상 12종 팔레트 ADR](../../adr/2026-08-14-room-color-palette-in-design-system.md)이 같은 모양의 문제를 이미 정리했다 — 값이 `internal` 토큰에 걸려 있고 소비자가 여러 feature에 걸치면 디자인 시스템이 소유한다. 프로필 아바타도 마이페이지 헤더·코멘트 작성자·방 멤버 아바타에서 함께 쓰이고(spec SC-003), spec §3.2가 "아바타 12종의 이미지 에셋과 시각 규격은 디자인 시스템이 정의한다"고 못박았다.
- **디자인 관찰**: Figma [010-1](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8?node-id=2314-95662&m=dev)의 그리드는 4열 × 3행이고, 각 칸은 색 배경 원 위에 캐릭터 글리프가 얹힌 형태다. 상단 썸네일의 기본 아바타는 그리드 첫 칸과 같은 그림으로 보이므로 **기본값은 enum의 첫 항목**으로 두고, 정확한 대응은 구현 단계에서 Figma 대조로 확정한다([figma-design-fidelity.md](../../conventions/figma-design-fidelity.md)).
- **에셋 포맷**: 벡터로 변환 가능하면 `ImageVector`, 아니면 WebP다([ADR](../../adr/2026-08-01-webp-for-raster-images.md), [design-system README §5.3](../../../core/design-system/README.md)). 판정은 에셋을 실제로 받아보는 구현 단계에서 한다.
- **Alternatives considered**: (a) `:feature:profile`이 에셋과 목록을 소유 — 프로필 표기 지점이 여러 feature에 생기는 순간 에셋이 복제된다. 기각. (b) `:core:domain`에 아바타 enum을 두고 디자인 시스템이 참조 — 레이어 역행이다(design-system → domain 금지). 기각.
- **ADR 승격 후보**: 방 색상 팔레트와 같은 성격의 소유 결정이라 승격 대상이다.
- (plan 1.0.0에서 결정)

## D5. 아바타를 그리는 컴포넌트 — 기존 `MinoAvatar`를 확장하지 않고 전용 컴포넌트 신설

- **Decision**: `MinoProfileAvatarImage`(가칭, `component/profileavatar/`)를 신설한다. 표시할 아바타·크기(`Dp`)·선택 상태·클릭을 받고, 선택 표시는 컴포넌트가 그린다. 기존 `MinoAvatar`와 `MinoAvatarSize`(24·32·40·48·56dp)는 건드리지 않는다.
- **Rationale**: `MinoAvatar`는 URL 이미지와 사람·회사·학교 placeholder 글리프를 다루는 컴포넌트이고, `MinoAvatarSize`는 Figma `Avatar/Avatar` 컴포넌트셋의 Size 축을 그대로 옮긴 것이다. 프로필 화면이 쓰는 크기(그리드 70dp·상단 썸네일 120dp)는 그 축 밖의 값이고, 그림도 앱이 번들한 고정 12종이라 성격이 다르다. 축에 없는 크기를 넣으려고 열거값을 늘리면 다른 화면이 쓰지 않는 값이 축에 남는다.
- **Alternatives considered**: (a) `MinoAvatarSize`에 70·120dp를 추가하고 `MinoAvatar`에 로컬 아바타 파라미터를 더한다 — 하나의 컴포넌트가 URL 아바타와 번들 캐릭터 두 축을 갖게 되어 API가 갈라진다. 기각. (b) feature 안에서 `Image` + `Modifier.clip`으로 직접 조립 — 에셋이 디자인 시스템에 있는데 그리는 방법만 feature에 흩어져, 두 번째 소비 화면에서 복제된다. 기각.
- (plan 1.0.0에서 결정)

## D6. 상단 내비게이션 — `:core:design-system`에 `MinoTopNavigation` 신설

- **Decision**: 뒤로가기 버튼과 가운데 제목으로 이루어진 상단 바를 `:core:design-system`에 `MinoTopNavigation`으로 신설한다. 이번 범위에서는 이 화면이 쓰는 구성(뒤로가기 + 제목, 뒤로가기 비활성 가능)만 만든다.
- **Rationale**: Figma 010-1이 이 자리에 `Top Navigation/Top Navigation` **컴포넌트 인스턴스**를 쓰고 있어 디자이너가 이미 공용 컴포넌트로 배포한 자산이다. 코드에는 대응물이 없다. 화면 고유 chrome은 화면이 직접 배치한다는 규약([feature-module.md](../../architecture/feature-module.md) 4장)은 **어디에 두는가**가 아니라 **셸이 아닌 화면이 배치한다**는 규칙이므로, 컴포넌트 자체를 디자인 시스템이 소유하는 것과 충돌하지 않는다.
- **Alternatives considered**: feature 안에 `component/`로 조립 — 마이페이지·방 폼 등 같은 상단 바를 쓰는 화면이 이미 예정돼 있어 곧 복제된다. 기각.
- **범위 주의**: Figma 컴포넌트셋의 나머지 variant(액션 아이콘·검색 등)는 이번에 만들지 않는다. 필요해지는 화면이 자기 작업에서 축을 넓힌다.
- (plan 1.0.0에서 결정)

## D7. 닉네임 검증의 위치 — `core:domain`의 UseCase

- **Decision**: 닉네임 유효 판정을 `ValidateNicknameUseCase`(`core:domain/usecase/`)에 둔다. 판정 규칙은 "앞뒤 공백을 제거한 값이 한글 음절·영문 알파벳으로만 이루어지고 길이가 2 이상"이며 상한은 두지 않는다. 저장은 `SaveProfileUseCase`가 같은 판정을 다시 통과시킨 뒤 앞뒤 공백을 제거한 값으로 `ProfileRepository.saveProfile`을 호출한다.
- **Rationale**: 헌법 원칙 II와 [core:domain README §4](../../../core/domain/README.md)가 비즈니스 규칙을 ViewModel에 두지 못하게 한다. 닉네임 규칙은 UI 표현이 아니라 저장 값의 불변식이고(spec SC-004 "유효하지 않은 닉네임으로 저장이 시도되는 경우 0건"), 화면이 실시간 판정과 저장 직전 판정 두 곳에서 같은 규칙을 쓴다.
- **자모 처리**: `ㄱ`·`ㅏ` 같은 낱자는 유효하지 않은 문자로 본다. spec §4가 "한글 음절과 영문 알파벳만"으로 확정했다.
- **plan 1.1.0 보정**: 저장이 나가는 Repository 멤버가 `saveProfile` 하나에서 `registerProfile`·`updateProfile` 둘로 갈렸다([D14](#d14-등록과-수정의-분기--진입점이-아니라-저장된-프로필의-유무로-가른다)). 검증의 위치에 대한 이 결정 자체는 그대로다.
- **plan 2.0.0 보정**: 그 분기가 철회되어 호출 대상이 다시 `saveProfile` 하나로 돌아왔다([D23](#d23-repository-표면--observeprofile--saveprofile-두-멤버)). 위 Decision 본문이 그대로 유효하다.
- **Alternatives considered**: (a) ViewModel의 계산 프로퍼티로 두기 — 규칙이 presentation에 갇혀 저장 경로가 그것을 재사용하지 못하고, 헌법이 금지한다. 기각. (b) `Nickname` 값 객체(VO)로 도메인 모델화 — 유효하지 않은 값을 표현할 수 없는 타입이 되어, 입력 중인 무효 문자열을 담아야 하는 UiState가 결국 원시 `String`을 따로 든다. 타입 하나가 늘고 얻는 것이 없어 기각.
- (plan 1.0.0에서 결정)

## D8. 저장 실패·중복 저장 — `DomainErrorEmitter` + `isSaving` 플래그

- **Decision**: 저장 실패는 [에러 처리 규약](../../conventions/error_handling.md) §5의 "사용자 액션의 일회성 실패" 통로를 쓴다 — ViewModel이 `runCatchingDomain` 결과를 `emitDomainError`로 흘리고, `ProfileRoute`가 `CollectDomainError`로 받아 셸의 스낵바 호스트에 띄운다. 화면과 입력값은 그대로 둔다. 저장 진행 상태는 `ProfileUiState.isSaving: Boolean`으로 두고, `true`인 동안 저장 인텐트를 무시한다.
- **Rationale**: FR-012·EC-003·EC-007은 "화면 유지 + 입력값 보존 + 실패 알림"을 요구하므로 주 데이터 로드 실패(State에 담아 에러 화면으로 전환)가 아니라 일회성 실패다. UX-003·EC-004의 중복 저장 방지는 [UiState isLoading 분리형 ADR](../../adr/2026-07-25-uistate-isloading-over-sealed-status.md)의 분리형 필드로 표현한다 — sealed 단일 상태로 만들면 실패 시 되돌릴 목적지가 없다.
- **문구 매핑**: 리프별 사용자 문구는 `ProfileRoute`가 자기 파일의 `when`으로 매핑한다. 공통 매퍼는 만들지 않는다(에러 처리 규약 §8).
- (plan 1.0.0에서 결정)

## D9. 앱 전체 즉시 반영 — `observeProfile(): Flow<Profile?>`

- **Decision**: `ProfileRepository`가 단발 조회(`getProfile`) 대신 `observeProfile(): Flow<Profile?>`을 노출하고, 프로필을 표기하는 화면들이 이것을 구독한다. 저장은 `suspend fun saveProfile(...)`이다.
- **Rationale**: SC-003·TS-011이 "앱을 다시 켜지 않고" 모든 표기 지점이 새 값을 보여줄 것을 요구한다. DataStore가 `Flow`를 원천으로 내주므로, 화면마다 재조회 시점을 배선하는 것보다 구독이 단순하고 누락이 없다.
- **Alternatives considered**: 화면 재진입(RESUMED) 시 재조회 — 마이페이지처럼 되돌아오는 화면에는 통하지만, 동시에 살아 있는 다른 화면(코멘트 목록 등)은 갱신되지 않는다. 기각.
- (plan 1.0.0에서 결정)

## D10. 개인방(`내 장소`) 생성 — 이 모듈이 트리거하지 않는다

- **Decision**: `:feature:profile`은 개인방 생성을 호출하지 않는다. 온보딩 진입에서 저장이 성공하면 `RESULT_OK`로 그 사실만 알리고, 개인방을 만드는 주체는 서버 또는 온보딩 feature가 소유한다.
- **Rationale**: spec §3.2가 개인방의 생성 규칙을 방 스펙의 몫으로 남겼고, PRD [SCR-002]는 "프로필 생성 및 개인방(`내 장소`) **자동 생성**"이라고 적어 클라이언트의 별도 호출을 지시하지 않는다. 방 도메인 계약이 아직 없는 상태에서 이 모듈이 그것을 의존하면, 방 스펙이 확정될 때 프로필 모듈이 함께 흔들린다.
- **후속 확인 필요**: FR-008이 말하는 "트리거"의 실제 주체(서버 자동 생성 / 온보딩의 명시 호출)는 방·온보딩 스펙과 서버 계약이 확정될 때 정해진다. 완료 보고에 교차 확인 항목으로 싣는다.
- (plan 1.0.0에서 결정)

## D11. 화면 그래프 — 화면이 하나여도 `ProfileNavHost`를 유지한다

- **Decision**: `:feature:profile`은 화면이 하나지만 `ProfileShell` + `ProfileNavHost` 골격을 그대로 둔다.
- **Rationale**: [feature-module.md](../../architecture/feature-module.md) 4장이 "인자 복원(`toRoute`)·화면 조회 로깅이 NavHost에 딸려 오므로 NavHost 유지가 기본"이라고 정했고, 이 화면은 진입 인자(진입점)와 화면 로깅을 모두 갖는다.
- (plan 1.0.0에서 결정)

## D12. 테스트 범위 — JVM 단위 테스트만

- **Decision**: `ValidateNicknameUseCase`·`SaveProfileUseCase`·`ProfileViewModel`(Fake `ProfileRepository`)을 JVM 단위 테스트로 덮는다. Compose UI 테스트는 도입하지 않는다.
- **Rationale**: 저장소에 Compose UI 테스트 선례가 없고([core:data](../../../core/data/README.md)의 Fake 기반 JVM 테스트가 유일한 패턴), 테스트 인프라 도입은 이 기능의 범위 밖이다. spec의 화면 시나리오(TS-001~TS-017)는 [quickstart.md](quickstart.md)의 수동 검증으로 받는다.
- **plan 2.0.0 보정**: [D21](#d21-테스트-범위-확장--목-엔진-기반-데이터-레이어-테스트를-더한다)이 물러나면서 이 항목이 다시 테스트 범위의 전부다. 여기에 `ProfileLocalDataSourceImpl`(DataStore 왕복)과 `ProfileRepositoryImpl`(도메인 ↔ 저장 형태 변환)의 JVM 테스트가 더해진다 — 원격이 없으므로 `MockEngine`은 쓰지 않는다.
- (plan 1.0.0에서 결정)

---

## ~~D13. 프로필 저장 경로 — 원격 API가 원천, 로컬 DataStore는 캐시~~ — 재검토됨(plan 2.0.0)

> 계약이 존재한다는 사실은 그대로지만, **이번 구현 범위에서 원격을 연결하지 않기로** 정해졌다(사용자 지시: "UI 및 도메인 모델 설계 위주로 진행하고, API 연결은 추후 진행한다"). 원천을 서버로 두는 설계는 원격 연동이 실제로 붙는 후속 작업에서 되살아난다. 이번 범위의 결정은 [D22](#d22-이번-범위의-저장소--로컬-datastore-단독-원격-연동은-후속-작업)이며, 아래 본문은 후속 작업이 되돌아올 지점으로 남긴다.

- **Decision**: 프로필의 원천은 서버다. `:core:data`에 `ProfileApiService`·`ProfileRemoteDataSource`를 두고 꾹 API 초안(아래 **원본 계약** 참조)의 유저 엔드포인트 3종을 소비한다. 로컬 Preferences DataStore는 **원격 응답의 캐시**로 남겨, 앱 재시작 후 프리필과 `observeProfile()` 방출을 담당한다. 저장은 "원격 성공 → 캐시 갱신" 순서이며, 원격이 실패하면 캐시를 건드리지 않는다.
- **Rationale**: [D3](#d3-프로필-저장소--로컬-datastore-단독-원격은-인터페이스-뒤로-이연--재검토됨plan-110)이 로컬 단독을 택한 유일한 근거는 계약 부재였고, 그 근거가 사라졌다. spec §4의 가정("프로필 저장은 서버 반영을 포함하며, 저장 시점에 네트워크에 연결되어 있다고 가정한다")이 그대로 성립하고, FR-012(저장 실패 시 화면 유지)의 실패 원천도 실재하게 된다. 캐시를 남기는 이유는 SC-003("앱을 다시 켜지 않고도" 모든 표기 지점 갱신)을 Flow 하나로 만족시키는 [D9](#d9-앱-전체-즉시-반영--observeprofile-flowprofile)의 구조를 유지하기 위해서다.
- **Alternatives considered**:
  - **캐시 없이 매번 `GET /users/me`** — 프로필을 표기하는 모든 화면이 각자 네트워크를 때리고, 오프라인·지연 중에는 닉네임이 비어 보인다. spec은 오프라인을 다루지 않지만 표기 지점이 여럿이라(SC-003) 비용이 크다. 기각.
  - **로컬 우선 저장 후 백그라운드 동기화** — spec §4가 "오프라인 저장·나중에 동기화는 다루지 않는다"고 명시적으로 제외했다. 기각.
- **원본 계약**: `https://raw.githubusercontent.com/mash-up-kr/Team-MINO-Node/refs/heads/KKardy/GM-111-outline-prd/docs/swagger.yaml` (브랜치 문서라 이동·변경될 수 있다. 소비 형태는 `contracts/profile-api-contract.md`가 스냅숏으로 보관했다 — 이 파일은 plan 2.0.0에서 삭제됐고 git 이력 `e1ac7a0`에 남아 있다)
- (plan 1.1.0에서 결정)

## ~~D14. 등록과 수정의 분기 — 진입점이 아니라 저장된 프로필의 유무로 가른다~~ — 재검토됨(plan 2.0.0)

> 등록·수정 두 엔드포인트가 이번 범위에서 사라져 가를 대상 자체가 없다([D22](#d22-이번-범위의-저장소--로컬-datastore-단독-원격-연동은-후속-작업)). 로컬 단독 저장은 덮어쓰기 한 갈래뿐이며, 도메인 표면은 [D23](#d23-repository-표면--observeprofile--saveprofile-두-멤버)이 정한다. **분기 기준을 "저장된 프로필의 유무"로 두고 진입점을 쓰지 않는다**는 판단 자체는 후속 작업에서 그대로 되살릴 값이라 본문을 남긴다. `DeviceRepository.ensureDeviceId()`의 반환 타입 확대도 함께 철회한다 — 등록 요청이 없어져 `deviceId`를 쓰는 곳이 없다.

- **Decision**: 저장 시 호출할 엔드포인트를 **캐시에 프로필이 있는지**로 고른다. 없으면 `POST /api/v1/users`(등록), 있으면 `PATCH /api/v1/users/me`(수정)다. 진입점(`ProfileEntryPoint`)은 뒤로가기·저장 후 목적지에만 쓰고 API 분기에는 쓰지 않는다.
- **Rationale**: 두 값이 대개 함께 움직이지만 어긋나는 경우가 실재한다 — 온보딩에서 등록이 성공한 뒤 다음 스텝에서 되돌아오거나, 마이페이지 진입인데 캐시가 비어 있는 복구 상황이다. 무엇을 호출할지는 "서버에 내 유저가 있는가"의 문제이므로 그 사실에 직접 묻는 편이 맞다.
- **`deviceId`**: 등록 요청은 `deviceId`를 요구한다. 이미 있는 `DeviceRepository`가 확보를 담당하지만 현재 시그니처(`suspend fun ensureDeviceId()`)가 값을 돌려주지 않아, **반환 타입을 `String`으로 넓힌다**(멱등 동작은 그대로). 프로덕션 호출자가 아직 없어 영향은 테스트뿐이다.
- **재등록(409)**: 캐시가 비어 있는데 서버에 이미 유저가 있으면 `409 Conflict`가 온다. 이번 설계는 이것을 **저장 실패로 다룬다**(FR-012 경로). 자동으로 `PATCH`로 갈아타는 폴백은 두지 않는다 — API 문서가 "등록·인증 계약(재등록 처리 등)은 별도 인증 설계 문서에서 확정한다"고 남겨 둔 지점이라, 지금 정하면 근거 없는 동작을 만든다.
- (plan 1.1.0에서 결정)

## ~~D15. 목(mock) 구현 — flavor 소스셋으로 가른 Ktor `MockEngine`~~ — 재검토됨(plan 2.0.0)

> 목은 **원격 경로를 서버 없이 돌려보기 위한 장치**였다. 이번 범위가 원격을 연결하지 않으므로([D22](#d22-이번-범위의-저장소--로컬-datastore-단독-원격-연동은-후속-작업)) 목이 대신할 대상이 없다. `NetworkModule`·flavor 소스셋·`qaImplementation(libs.ktor.client.mock)`은 이번 설계에서 손대지 않는다. 원격 연동 작업이 시작될 때 이 항목을 먼저 읽는다 — 엔진만 가르는 방식과 기각된 세 대안이 그대로 유효하다.

- **Decision**: `HttpClient`의 **엔진만** 갈아 끼운다. `:core:data`의 flavor 소스셋(`src/qa/` · `src/prod/`)에 각각 엔진을 제공하는 Hilt 모듈을 두고, qa는 `MockEngine`, prod는 `OkHttp`를 제공한다. `NetworkModule`은 엔진을 주입받는 형태로 바꾸고 나머지 설정(`expectSuccess`·`convertDomainException`·`ContentNegotiation`·`Logging`)은 한 곳에 그대로 둔다. `ktor-client-mock`은 `qaImplementation`으로만 건다.
- **Rationale**: 목의 목적은 서버 없이 화면을 끝까지 돌려보는 것이고, 그러려면 **직렬화·응답 봉투 해제·에러 매핑·Repository 변환까지 실제 경로**가 돌아야 한다. DataSource나 Repository를 Fake로 바꾸면 딱 그 구간이 검증에서 빠진다. 엔진 교체는 그 구간을 모두 남기면서 네트워크 홉만 없앤다. 소스셋으로 가르면 prod 산출물에 목 코드도 `ktor-client-mock` 의존도 들어가지 않는다.
- **Alternatives considered**:
  - **`ProfileRemoteDataSource`의 Fake를 DI로 바인딩** — 가장 손이 적지만 DTO·매핑·에러 매핑 경로가 죽는다. 실서버를 붙이는 순간 처음 검증하는 코드가 된다. 기각.
  - **`BuildConfig.USE_MOCK_API` 런타임 분기** — 두 엔진 의존이 모든 빌드에 들어가고, prod APK에 목 응답 문자열이 남는다. 기각.
  - **로컬 목 서버(WireMock 등) 별도 실행** — 개발자가 서버를 띄워야 앱이 도는 상태가 되어 "임시 구축"의 취지에 어긋난다. 기각.
- **종료 조건**: 실서버와 인증 계약이 확정되면 qa 소스셋의 엔진을 `OkHttp`로 바꾸고 `Flavor.apiBaseUrl`을 실제 값으로 채운다. 이 전환이 목 구축의 끝이며, 그때까지 qa 빌드는 서버에 나가지 않는다.
- (plan 1.1.0에서 결정)

## ~~D16. 목 응답의 성질 — 프로세스 내 상태 유지 + 지연·실패 주입~~ — 재검토됨(plan 2.0.0)

> [D15](#d15-목mock-구현--flavor-소스셋으로-가른-ktor-mockengine)와 함께 물러난다. 이번 범위에서 저장 실패(FR-012)를 재현하는 방법은 [D25](#d25-저장-실패-경로--통로는-지금-배선하고-발화-원천은-후속-작업에-남긴다)가 따로 정한다.

- **Decision**: 목 핸들러는 프로세스가 사는 동안 등록된 유저를 메모리에 들고 있어 `POST` → `GET`/`PATCH`가 이어진다. 응답에 짧은 인위적 지연을 넣고, 실패 응답을 강제하는 스위치를 하나 둔다. 프로필 외 경로 요청은 `501`로 답해 "아직 목이 없다"를 드러낸다.
- **Rationale**: 지연이 0이면 `isSaving`이 만드는 중복 저장 차단(UX-003·EC-004)을 손으로 확인할 방법이 없고, 실패를 낼 수 없으면 FR-012·TS-006·EC-003·EC-007을 기기에서 재현할 수 없다. 다른 경로를 조용히 성공시키면 아직 붙지 않은 기능이 붙은 것처럼 보인다.
- **범위**: 목이 답하는 것은 유저 엔드포인트 3종뿐이다. 방·핀·코멘트·알림 엔드포인트는 각 기능의 계획이 자기 몫을 더한다.
- (plan 1.1.0에서 결정)

## D17. 개인방(`내 장소`) 생성 — 서버가 등록과 함께 처리한다(확정)

- **Decision**: [D10](#d10-개인방내-장소-생성--이-모듈이-트리거하지-않는다)의 결정(이 모듈이 트리거하지 않는다)을 유지하고, 그 근거를 가정에서 사실로 바꾼다. `POST /api/v1/users`가 "유저 등록 (+ 개인방 자동 생성)"으로 정의되어 있고 개인방은 응답에 포함되지 않는다.
- **결과**: FR-008의 "개인방 생성을 트리거한다"는 등록 요청 성공으로 충족된다. 클라이언트가 방 API를 호출하지 않으며, D10에 남겨 둔 "생성 주체 미확정" 항목은 닫힌다.
- **plan 2.0.0 보정**: 생성 주체가 서버라는 **사실**은 그대로지만, 그 트리거가 되는 등록 요청이 이번 범위에 없다([D22](#d22-이번-범위의-저장소--로컬-datastore-단독-원격-연동은-후속-작업)). FR-008의 개인방 생성 부분은 원격 연동 작업에서 충족되며, 이번 범위가 만족시키는 것은 FR-008의 나머지(저장 완료 후 온보딩 다음 스텝으로 이동)뿐이다. 이 미충족은 [plan.md](plan.md) Constitution Check의 원칙 IV 항목에 명시한다.
- (plan 1.1.0에서 결정)

## D18. 아바타 식별자 — 서버 계약을 따라 `Int`

- **Decision**: 도메인 `Profile.avatarId`의 타입을 `Int`로 둔다(서버 `Avatar { id: integer }`). `MinoProfileAvatar`(디자인 시스템 enum) ↔ `Int` 매핑은 [D4](#d4-아바타-12종의-소유--coredesign-system)가 정한 대로 `:feature:profile`이 소유한다.
- **Rationale**: 식별자의 정의권은 서버에 있다. 클라이언트가 문자열 키를 따로 만들면 매핑이 두 겹(enum ↔ 문자열 ↔ 서버 int)이 되고, 서버가 아바타를 추가할 때 어느 쪽이 원천인지 흐려진다.
- **미확정**: 12종과 서버 `id` 값의 대응표는 API 문서에 없다(스키마가 `integer`라고만 말한다). 목 단계에서는 **enum 선언 순서를 1부터 매긴 값**으로 두고, 서버 대응표가 나오면 매핑 한 곳만 고친다. 이 임시 대응은 매핑을 소유한 파일에 남긴다.
- **Alternatives considered**: 기존 결정(`String`)을 유지하고 데이터 레이어에서 변환 — 도메인이 서버와 다른 식별 체계를 갖게 되어 이득 없이 층이 는다. 기각.
- **plan 2.0.0 보정**: 원격이 이연돼도 이 결정은 **유지한다**. 식별자 타입을 로컬 사정에 맞춰 바꿨다가 원격 연동 때 되돌리면 저장된 캐시 값의 형식까지 함께 흔들린다. 서버 대응표가 여전히 없으므로 "enum 선언 순서를 1부터"라는 임시 매핑도 그대로이며, 매핑을 소유한 파일에 그 사실을 남기는 것도 그대로다.
- (plan 1.1.0에서 결정)

## D19. 닉네임 규칙 불일치 — 클라이언트는 spec을 따르고, 서버 거절은 저장 실패로 받는다

- **Decision**: 클라이언트 검증은 spec 그대로 둔다(앞뒤 공백 제외, 한글 음절·영문만, 2자 이상, **상한 없음**). 서버 스키마(`공백 포함 한글/영문 2~15자`)와 어긋나는 입력이 서버에서 거절되면 FR-012의 저장 실패 경로로 사용자에게 알린다.
- **Rationale**: plan은 spec에 없는 요구사항을 만들지 않는다(헌법 원칙 IV). 상한 15자를 클라이언트에 심으면 spec §5가 "상한을 두지 않는다"로 확정한 결정을 설계가 뒤집는 꼴이 된다. 반대로 서버 규칙을 무시할 수도 없으므로, 어긋남을 삼키지 않고 실패로 드러낸다.
- **후속 필요**: 두 규칙 중 무엇이 옳은지는 기획·서버와 맞춰 spec을 개정해야 한다. 어긋나는 지점은 두 곳이다 — **상한**(spec 없음 / 서버 15자)과 **공백**(spec 불가 / 서버 허용). 완료 보고에서 `/mino-spec` 개정을 제안한다.
- **plan 2.0.0 보정**: 이번 범위에는 서버 거절 경로가 없으므로 어긋남이 **드러나지 않는다** — 15자를 넘는 닉네임이 로컬에 그대로 저장되고 아무 실패도 나지 않는다. 어긋남이 사용자에게 처음 보이는 시점은 원격 연동 작업이며, 그전에 spec을 정리하는 편이 낫다. 클라이언트가 spec을 따른다는 결정 자체는 그대로다.
- (plan 1.1.0에서 결정)

## D20. 인증 헤더 — 이번 범위에서 배선하지 않는다

- **Decision**: `GET`·`PATCH /api/v1/users/me`가 요구하는 Bearer 토큰을 이번 설계에서 붙이지 않는다. 목 엔진은 인증을 검사하지 않으므로 화면 흐름은 완성되고, 실서버 전환 시 토큰 주입 지점(`defaultRequest`의 헤더 또는 플러그인)을 그때 정한다.
- **Rationale**: API 문서가 "인증은 별도 인증 설계 문서에서 확정한다(잠정: Bearer 토큰)"로 미뤄 둔 상태다. 토큰 발급 자체도 이 저장소에서 별도 이슈로 분리되어 있어, 지금 헤더 규약을 정하면 확정될 계약과 어긋날 가능성이 높다.
- **경계**: 이 결정은 목 구축이 끝나는 시점(D15 종료 조건)과 같은 지점에서 닫힌다. 그전까지 `:feature:profile`은 인증 상태를 알지 못한다.
- **plan 2.0.0 보정**: 원격 자체가 이연되면서 이 결정은 더 넓은 이연([D22](#d22-이번-범위의-저장소--로컬-datastore-단독-원격-연동은-후속-작업))에 흡수된다. 판단은 뒤집히지 않았고, 닫히는 시점만 원격 연동 작업으로 옮겨 갔다.
- (plan 1.1.0에서 결정)

## ~~D21. 테스트 범위 확장 — 목 엔진 기반 데이터 레이어 테스트를 더한다~~ — 재검토됨(plan 2.0.0)

> 덮으려던 대상(응답 봉투 해제·DTO 매핑·비2xx 변환·캐시 갱신 순서)이 이번 범위에 존재하지 않는다([D22](#d22-이번-범위의-저장소--로컬-datastore-단독-원격-연동은-후속-작업)). 테스트 범위는 [D12](#d12-테스트-범위--jvm-단위-테스트만)로 되돌아간다.

- **Decision**: [D12](#d12-테스트-범위--jvm-단위-테스트만)의 범위에 `ProfileRepositoryImpl`의 원격 경로 테스트를 더한다. `ktor-client-mock`은 이미 `:core:data`의 테스트 의존이므로, 기존 `DomainExceptionMappingTest` 방식으로 응답 봉투 해제·DTO 매핑·비2xx의 도메인 예외 변환·캐시 갱신 순서를 덮는다.
- **Rationale**: 원격이 원천이 되면서 "성공 시에만 캐시를 갱신한다"(D13)가 사용자에게 보이는 규칙(FR-012·SC-006)이 됐다. 이 순서는 화면이 아니라 데이터 레이어의 불변식이라 단위 테스트가 지킬 자리다.
- (plan 1.1.0에서 결정)

---

## D22. 이번 범위의 저장소 — 로컬 DataStore 단독, 원격 연동은 후속 작업

- **Decision**: 이번 구현 범위에서 프로필의 저장소는 `:core:data`의 공유 Preferences DataStore(`storage/DataStoreModule`) **하나뿐**이다. `:core:data`에 프로필 관련 DTO·`ApiService`·`RemoteDataSource`·목 엔진·flavor 소스셋을 만들지 않고, `NetworkModule`도 손대지 않는다. 원격 연동은 별도 후속 작업으로 분리하며, 그때 바뀌는 지점은 [D24](#d24-원격-연동이-붙을-때-바뀌는-지점을-지금-고정한다)가 미리 고정한다.
- **Rationale**: 사용자가 이번 계획의 범위를 "UI 및 도메인 모델 설계 위주, API 연결은 추후"로 지정했다. [D13](#d13-프로필-저장-경로--원격-api가-원천-로컬-datastore는-캐시)이 원격을 원천으로 삼은 근거(계약이 존재한다)는 사라지지 않았지만, 그 계약을 **언제 소비할지**는 계약의 존재와 별개의 판단이고 그 판단권은 사용자에게 있다. 화면·상태·도메인 경계는 저장소의 위치와 무관하게 결정되므로([D3](#d3-프로필-저장소--로컬-datastore-단독-원격은-인터페이스-뒤로-이연--재검토됨plan-110)의 기각 사유 (b)가 이번에도 그대로 성립한다) 이번 범위만으로도 화면이 끝까지 도는 완결된 산출물이 나온다.
- **[D3](#d3-프로필-저장소--로컬-datastore-단독-원격은-인터페이스-뒤로-이연--재검토됨plan-110)과의 관계**: 결과물의 **모양**은 D3과 같지만 **근거는 다르다.** D3은 "계약이 없어서" 로컬 단독이었고 계약이 생긴 순간 무너졌다. 이 결정은 "계약은 있으나 이번 범위가 아니어서" 로컬 단독이므로, 계약이 갱신되거나 서버가 뜨더라도 스스로 무너지지 않는다. 닫히는 조건은 하나 — 원격 연동 작업이 착수되는 것이다.
- **spec과의 어긋남**: spec §4는 "프로필 저장은 서버 반영을 포함하며, 사용자는 저장 시점에 네트워크에 연결되어 있다고 가정한다"를 가정으로 적고 있다. 이 결정은 그 가정을 **부정하지 않고 충족 시점을 후속 작업으로 미룬다.** 이번 범위의 산출물만으로는 spec의 그 가정과 FR-008의 개인방 생성([D17](#d17-개인방내-장소-생성--서버가-등록과-함께-처리한다확정))이 충족되지 않으며, 이 사실을 [plan.md](plan.md) Constitution Check와 [quickstart.md](quickstart.md) §4가 명시적으로 든다. 설계로 봉합하지 않는다.
- **Alternatives considered**:
  - **원격 계층을 만들되 목 엔진으로만 돌린다**([D13](#d13-프로필-저장-경로--원격-api가-원천-로컬-datastore는-캐시)+[D15](#d15-목mock-구현--flavor-소스셋으로-가른-ktor-mockengine) 유지) — 사용자가 지정한 범위를 넘는다. 또한 목만 있는 원격 계층은 실서버가 뜰 때까지 **검증되지 않은 코드**로 남고, 그 사이 서버 계약이 초안 단계에서 바뀌면 만든 만큼 다시 고쳐야 한다. 기각.
  - **저장 자체를 하지 않고 화면만 만든다(인메모리)** — FR-006(마이페이지 진입 프리필)과 SC-003(앱 전역 즉시 반영)이 저장된 값을 전제하므로 spec 요구사항이 통째로 검증 불가가 된다. 로컬 저장은 "API 연결"이 아니라 도메인 모델이 실재하기 위한 최소치다. 기각.
- **ADR 승격 후보 아님**: 이것은 이 feature의 **작업 순서**에 대한 결정이지 다른 feature를 구속하는 구조 결정이 아니다. 반면 [D13](#d13-프로필-저장-경로--원격-api가-원천-로컬-datastore는-캐시)이 정한 "원격이 원천, 로컬은 캐시"는 되살아날 때 승격 후보로 남는다.
- (plan 2.0.0에서 결정)

## D23. Repository 표면 — `observeProfile()` + `saveProfile()` 두 멤버

- **Decision**: `ProfileRepository`를 두 멤버로 둔다 — `fun observeProfile(): Flow<Profile?>`과 `suspend fun saveProfile(profile: Profile)`. [D14](#d14-등록과-수정의-분기--진입점이-아니라-저장된-프로필의-유무로-가른다)가 나눴던 `registerProfile`·`updateProfile`과 [D13](#d13-프로필-저장-경로--원격-api가-원천-로컬-datastore는-캐시)이 열었던 `refreshProfile`을 모두 거둔다. `SaveProfileUseCase`는 `DeviceRepository`를 알지 않는다.
- **Rationale**: 로컬 저장에서 등록과 수정은 같은 쓰기다 — 키가 있으면 덮어쓰고 없으면 만든다. 갈래가 하나뿐인데 멤버를 둘로 두면 구현체가 두 이름으로 같은 코드를 부르는 껍데기가 된다. `refreshProfile`은 원천이 로컬일 때 새로 받아올 곳이 없어 의미가 없다. **쓰지 않을 계약을 미리 열어 두지 않는다** — 이번 범위에서 근거 없는 표면이 되고([헌법 원칙 IV](../../constitution.md)), 원격이 붙을 때 어차피 다시 판단할 자리다([D24](#d24-원격-연동이-붙을-때-바뀌는-지점을-지금-고정한다)).
- **`saveProfile`의 반환**: `Profile`을 돌려주지 않고 `Unit`이다. 저장된 값은 `observeProfile()`이 곧바로 흘리므로([D9](#d9-앱-전체-즉시-반영--observeprofile-flowprofile)) 반환값을 쓸 곳이 없고, 반환하면 호출자가 Flow와 반환값 중 어느 쪽을 원천으로 볼지 갈린다.
- **Alternatives considered**:
  - **원격 시절의 4멤버 표면을 유지하고 구현만 로컬로** — 원격이 붙을 때 인터페이스를 안 고쳐도 된다는 이점이 있지만, 그 대가로 이번 범위에 아무도 부르지 않는 멤버 두 개(`refreshProfile`·둘로 갈린 저장)와 그것을 채우는 빈 구현이 남는다. 표면을 나중에 넓히는 비용은 파일 하나이고, 지금 지어낸 계약이 원격 확정 시점의 실제 계약과 어긋날 위험이 그보다 크다. 기각.
  - **`saveProfile(nickname: String, avatarId: Int)`로 원시 값을 받는다** — 도메인 모델이 있는데 그 필드를 풀어 넘기는 것이라 `Profile`이 저장 경로에서만 없는 모양이 된다. 기각.
- (plan 2.0.0에서 결정)

## D24. 원격 연동이 붙을 때 바뀌는 지점을 지금 고정한다

- **Decision**: 이번 설계는 원격 연동 작업이 **무엇을 고치고 무엇을 고치지 않는지**를 미리 못박는다.

  | 원격이 붙을 때 | 판정 |
  |---|---|
  | `:core:domain`의 `Profile` 모델 | **그대로** — 필드(`nickname`·`avatarId: Int`)가 서버 스키마에서 도출됐다([D18](#d18-아바타-식별자--서버-계약을-따라-int)) |
  | `ProfileRepository` 인터페이스 | **넓어진다** — `refreshProfile()` 추가, 저장이 등록/수정으로 갈릴지는 그때 [D14](#d14-등록과-수정의-분기--진입점이-아니라-저장된-프로필의-유무로-가른다)를 다시 읽고 판단한다 |
  | `ProfileRepositoryImpl` (`:core:data`) | **바뀐다** — 원격 호출을 앞에 두고 로컬을 캐시로 내린다([D13](#d13-프로필-저장-경로--원격-api가-원천-로컬-datastore는-캐시)) |
  | `ProfileLocalDataSource`·DataStore 키 | **그대로** — 저장 형태가 캐시로 역할만 바뀐다 |
  | `:feature:profile` 전체 | **그대로** — 화면·ViewModel·Intent·SideEffect가 저장 경로를 모른다 |
  | `:core:design-system` 신설 컴포넌트 | **그대로** — 도메인·데이터를 모른다 |
  | `NetworkModule`·flavor 소스셋 | **그때 처음 손댄다** — 이번 범위에서 건드리지 않는다 |

- **Rationale**: "추후 진행"은 그 추후가 무엇을 부수는지 적혀 있을 때만 안전한 이연이다. 이 표가 없으면 원격 작업이 화면까지 되짚어 고치게 되고, 이연이 절약한 만큼을 재작업으로 돌려주게 된다. 표의 내용 자체는 새 판단이 아니라 [D9](#d9-앱-전체-즉시-반영--observeprofile-flowprofile)(Flow 구독)·[D18](#d18-아바타-식별자--서버-계약을-따라-int)(식별자 타입)·[D7](#d7-닉네임-검증의-위치--coredomain의-usecase)(검증 위치)이 이미 만들어 둔 경계를 읽어낸 것이다.
- **후속 작업의 입력**: 원격 연동을 설계할 때 되살릴 항목은 [D13](#d13-프로필-저장-경로--원격-api가-원천-로컬-datastore는-캐시)·[D14](#d14-등록과-수정의-분기--진입점이-아니라-저장된-프로필의-유무로-가른다)·[D15](#d15-목mock-구현--flavor-소스셋으로-가른-ktor-mockengine)·[D16](#d16-목-응답의-성질--프로세스-내-상태-유지--지연실패-주입)·[D20](#d20-인증-헤더--이번-범위에서-배선하지-않는다)·[D21](#d21-테스트-범위-확장--목-엔진-기반-데이터-레이어-테스트를-더한다)이다. 서버 계약의 스냅숏은 plan 1.1.0의 `contracts/profile-api-contract.md`에 있었고, 이번 개정에서 지웠으므로 git 이력(`e1ac7a0`)에서 되살린다. 원본은 [D13](#d13-프로필-저장-경로--원격-api가-원천-로컬-datastore는-캐시)이 적어 둔 URL이다.
- (plan 2.0.0에서 결정)

## D25. 저장 실패 경로 — 통로는 지금 배선하고, 발화 원천은 후속 작업에 남긴다

- **Decision**: [D8](#d8-저장-실패중복-저장--domainerroremitter--issaving-플래그)이 정한 실패 통로(`runCatchingDomain` → `emitDomainError` → `CollectDomainError` → 스낵바)와 `isSaving` 플래그를 이번 범위에서 **그대로 만든다.** 다만 로컬 저장에서는 이 통로를 발화시킬 원천이 사실상 없다 — DataStore 쓰기 실패는 디스크 이상 같은 예외 상황이다. 실패 경로의 **검증**은 `SaveProfileUseCase`·`ProfileViewModel`의 Fake 기반 단위 테스트(Fake Repository가 예외를 던지는 경우)로 받고, 기기에서의 수동 재현은 후속 작업으로 미룬다.
- **Rationale**: FR-012·EC-003·EC-007·SC-006은 spec의 요구사항이고, 통로를 나중에 뚫으면 ViewModel·Route·Activity를 다시 열어야 한다([D24](#d24-원격-연동이-붙을-때-바뀌는-지점을-지금-고정한다)가 `:feature:profile`을 "그대로"로 둔 근거가 이 배선이다). 반대로 발화 원천을 억지로 만들려고 로컬에 인위적 실패 스위치를 넣으면, 프로덕션 코드에 이번 범위 밖의 장치가 남는다.
- **`isSaving`의 성격**: 로컬 저장은 사실상 즉시 끝나므로 이번 범위에서 `isSaving`이 눈에 보이는 시간은 거의 0이다. UX-003·EC-004의 중복 저장 차단은 눈이 아니라 ViewModel 단위 테스트(저장 중 두 번째 인텐트가 무시되는지)로 확인한다.
- (plan 2.0.0에서 결정)

## D26. 아바타 그리드의 배치 — 화면이 소유하고 `LazyVerticalGrid`를 쓰지 않는다

- **Decision**: 아바타 12칸의 배치(4열 × 3행)를 `:feature:profile`의 `component/`에 두고, `LazyVerticalGrid`가 아니라 고정 `Column` + `Row` 조합으로 그린다. 컴포넌트 한 칸은 `MinoProfileAvatarImage`([D5](#d5-아바타를-그리는-컴포넌트--기존-minoavatar를-확장하지-않고-전용-컴포넌트-신설))이고, 그리드는 자기 칸을 어떻게 그리는지 모른다.
- **Rationale**: 항목이 12개로 고정이고 서버에서 내려받지 않으므로(spec §4) 지연 로딩이 얻을 것이 없다. 반대로 잃는 것은 있다 — 화면 전체가 세로 스크롤 하나로 흐르는 구조(상단 바 → 안내 문구 → 썸네일 → 입력 필드 → 그리드 → 액션)에서 `LazyVerticalGrid`를 중첩하면 높이 제약이 무한대가 되어 크래시하거나 고정 높이를 억지로 지정해야 한다.
- **Alternatives considered**: (a) `FlowRow`로 열 수를 폭에 맡긴다 — 디자인이 4열을 고정하고 있어 폭에 따라 3열·5열이 되는 것은 원본과의 대조를 깨뜨린다. 기각. (b) `LazyVerticalGrid`를 화면 최상위로 올리고 나머지 요소를 `span`으로 얹는다 — 12칸 때문에 화면 전체 구조를 그리드에 맞추는 역전이다. 기각.
- (plan 2.0.0에서 결정)
