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

> 이 항목이 전제한 "프로필 API 계약이 저장소 어디에도 없다"가 깨졌다. [꾹 API 초안](https://raw.githubusercontent.com/mash-up-kr/Team-MINO-Node/refs/heads/KKardy/GM-111-outline-prd/docs/swagger.yaml)이 유저 등록·조회·수정 엔드포인트를 정의하고 있어, 원격 연동을 이연할 근거가 사라졌다. 새 결정은 [D13](#d13-프로필-저장-경로--원격-api가-원천-로컬-datastore는-캐시--재검토됨plan-200)이며, 아래 본문은 기각 이력으로 남긴다.


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

## ~~D6. 상단 내비게이션 — `:core:design-system`에 `MinoTopNavigation` 신설~~ — 부분 재검토됨(plan 3.0.0)

> **컴포넌트를 `:core:design-system`에 신설한다는 결정은 유효하다.** 뒤집힌 것은 두 가지다 — (a) "나머지 variant(액션 아이콘·검색 등)"라는 서술이 사실과 다르다. 컴포넌트셋 `16215-20432`의 축은 `Platform`(iOS·Android·Web) 하나뿐이다. (b) 어느 variant를 구현하는가가 [D27](#d27-상단-바는-화면-목업이-쓰는-ios-variant를-따른다)로 확정됐다. 본문의 나머지는 그대로 유효하다.

- **Decision**: 뒤로가기 버튼과 가운데 제목으로 이루어진 상단 바를 `:core:design-system`에 `MinoTopNavigation`으로 신설한다. 이번 범위에서는 이 화면이 쓰는 구성(뒤로가기 + 제목, 뒤로가기 비활성 가능)만 만든다.
- **Rationale**: Figma 010-1이 이 자리에 `Top Navigation/Top Navigation` **컴포넌트 인스턴스**를 쓰고 있어 디자이너가 이미 공용 컴포넌트로 배포한 자산이다. 코드에는 대응물이 없다. 화면 고유 chrome은 화면이 직접 배치한다는 규약([feature-module.md](../../architecture/feature-module.md) 4장)은 **어디에 두는가**가 아니라 **셸이 아닌 화면이 배치한다**는 규칙이므로, 컴포넌트 자체를 디자인 시스템이 소유하는 것과 충돌하지 않는다.
- **Alternatives considered**: feature 안에 `component/`로 조립 — 마이페이지·방 폼 등 같은 상단 바를 쓰는 화면이 이미 예정돼 있어 곧 복제된다. 기각.
- **범위 주의**: Figma 컴포넌트셋의 나머지 variant(액션 아이콘·검색 등)는 이번에 만들지 않는다. 필요해지는 화면이 자기 작업에서 축을 넓힌다.
- (plan 1.0.0에서 결정)

## D7. 닉네임 검증의 위치 — `core:domain`의 UseCase

- **Decision**: 닉네임 유효 판정을 `ValidateNicknameUseCase`(`core:domain/usecase/`)에 둔다. 판정 규칙은 "앞뒤 공백을 제거한 값이 한글 음절·영문 알파벳으로만 이루어지고 길이가 2 이상"이며 상한은 두지 않는다. 저장은 `SaveProfileUseCase`가 같은 판정을 다시 통과시킨 뒤 앞뒤 공백을 제거한 값으로 `ProfileRepository.saveProfile`을 호출한다.
- **Rationale**: 헌법 원칙 II와 [core:domain README §4](../../../core/domain/README.md)가 비즈니스 규칙을 ViewModel에 두지 못하게 한다. 닉네임 규칙은 UI 표현이 아니라 저장 값의 불변식이고(spec SC-004 "유효하지 않은 닉네임으로 저장이 시도되는 경우 0건"), 화면이 실시간 판정과 저장 직전 판정 두 곳에서 같은 규칙을 쓴다.
- **자모 처리**: `ㄱ`·`ㅏ` 같은 낱자는 유효하지 않은 문자로 본다. spec §4가 "한글 음절과 영문 알파벳만"으로 확정했다.
- **plan 1.1.0 보정**: 저장이 나가는 Repository 멤버가 `saveProfile` 하나에서 `registerProfile`·`updateProfile` 둘로 갈렸다([D14](#d14-등록과-수정의-분기--진입점이-아니라-저장된-프로필의-유무로-가른다--재검토됨plan-200)). 검증의 위치에 대한 이 결정 자체는 그대로다.
- **plan 2.0.0 보정**: 그 분기가 철회되어 호출 대상이 다시 `saveProfile` 하나로 돌아왔다([D23](#d23-repository-표면--observeprofile--saveprofile-두-멤버--재검토됨plan-400)). 위 Decision 본문이 그대로 유효하다.
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
- **plan 2.0.0 보정**: [D21](#d21-테스트-범위-확장--목-엔진-기반-데이터-레이어-테스트를-더한다--재검토됨plan-200)이 물러나면서 이 항목이 다시 테스트 범위의 전부다. 여기에 `ProfileLocalDataSourceImpl`(DataStore 왕복)과 `ProfileRepositoryImpl`(도메인 ↔ 저장 형태 변환)의 JVM 테스트가 더해진다 — 원격이 없으므로 `MockEngine`은 쓰지 않는다.
- **plan 4.0.0 보정**: "JVM 단위 테스트만"이라는 경계는 그대로다. `MockEngine`을 쓰지 않는다는 단서만 뒤집힌다 — [D43](#d43-테스트-범위--mockengine-기반-데이터-레이어-테스트를-더한다)이 원격 경로를 더한다. Compose UI 테스트를 도입하지 않는 판정은 유지한다.
- (plan 1.0.0에서 결정)

---

## ~~D13. 프로필 저장 경로 — 원격 API가 원천, 로컬 DataStore는 캐시~~ — 재검토됨(plan 2.0.0)

> 계약이 존재한다는 사실은 그대로지만, **이번 구현 범위에서 원격을 연결하지 않기로** 정해졌다(사용자 지시: "UI 및 도메인 모델 설계 위주로 진행하고, API 연결은 추후 진행한다"). 원천을 서버로 두는 설계는 원격 연동이 실제로 붙는 후속 작업에서 되살아난다. 이번 범위의 결정은 [D22](#d22-이번-범위의-저장소--로컬-datastore-단독-원격-연동은-후속-작업--재검토됨plan-400)이며, 아래 본문은 후속 작업이 되돌아올 지점으로 남긴다.

- **Decision**: 프로필의 원천은 서버다. `:core:data`에 `ProfileApiService`·`ProfileRemoteDataSource`를 두고 꾹 API 초안(아래 **원본 계약** 참조)의 유저 엔드포인트 3종을 소비한다. 로컬 Preferences DataStore는 **원격 응답의 캐시**로 남겨, 앱 재시작 후 프리필과 `observeProfile()` 방출을 담당한다. 저장은 "원격 성공 → 캐시 갱신" 순서이며, 원격이 실패하면 캐시를 건드리지 않는다.
- **Rationale**: [D3](#d3-프로필-저장소--로컬-datastore-단독-원격은-인터페이스-뒤로-이연--재검토됨plan-110)이 로컬 단독을 택한 유일한 근거는 계약 부재였고, 그 근거가 사라졌다. spec §4의 가정("프로필 저장은 서버 반영을 포함하며, 저장 시점에 네트워크에 연결되어 있다고 가정한다")이 그대로 성립하고, FR-012(저장 실패 시 화면 유지)의 실패 원천도 실재하게 된다. 캐시를 남기는 이유는 SC-003("앱을 다시 켜지 않고도" 모든 표기 지점 갱신)을 Flow 하나로 만족시키는 [D9](#d9-앱-전체-즉시-반영--observeprofile-flowprofile)의 구조를 유지하기 위해서다.
- **Alternatives considered**:
  - **캐시 없이 매번 `GET /users/me`** — 프로필을 표기하는 모든 화면이 각자 네트워크를 때리고, 오프라인·지연 중에는 닉네임이 비어 보인다. spec은 오프라인을 다루지 않지만 표기 지점이 여럿이라(SC-003) 비용이 크다. 기각.
  - **로컬 우선 저장 후 백그라운드 동기화** — spec §4가 "오프라인 저장·나중에 동기화는 다루지 않는다"고 명시적으로 제외했다. 기각.
- **plan 4.0.0 처분 — 되살아났다.** "원격이 원천, 로컬은 캐시"가 [D36](#d36-원격-연동-착수--원천은-서버-로컬-datastore는-캐시)으로 복귀했다. 다만 근거 문서는 아래 브랜치 swagger가 아니라 **배포된 OpenAPI 문서**이며, 그 차이가 [D37](#d37-아바타-식별자--도메인-profileavatar-enum-서버-표현은-avatarcolor-문자열)을 만들었다.
- **원본 계약**: `https://raw.githubusercontent.com/mash-up-kr/Team-MINO-Node/refs/heads/KKardy/GM-111-outline-prd/docs/swagger.yaml` (브랜치 문서라 이동·변경될 수 있다. 소비 형태는 `contracts/profile-api-contract.md`가 스냅숏으로 보관했다 — 이 파일은 plan 2.0.0에서 삭제됐고 git 이력 `e1ac7a0`에 남아 있다)
- (plan 1.1.0에서 결정)

## ~~D14. 등록과 수정의 분기 — 진입점이 아니라 저장된 프로필의 유무로 가른다~~ — 재검토됨(plan 2.0.0)

> 등록·수정 두 엔드포인트가 이번 범위에서 사라져 가를 대상 자체가 없다([D22](#d22-이번-범위의-저장소--로컬-datastore-단독-원격-연동은-후속-작업--재검토됨plan-400)). 로컬 단독 저장은 덮어쓰기 한 갈래뿐이며, 도메인 표면은 [D23](#d23-repository-표면--observeprofile--saveprofile-두-멤버--재검토됨plan-400)이 정한다. **분기 기준을 "저장된 프로필의 유무"로 두고 진입점을 쓰지 않는다**는 판단 자체는 후속 작업에서 그대로 되살릴 값이라 본문을 남긴다. `DeviceRepository.ensureDeviceId()`의 반환 타입 확대도 함께 철회한다 — 등록 요청이 없어져 `deviceId`를 쓰는 곳이 없다.

- **Decision**: 저장 시 호출할 엔드포인트를 **캐시에 프로필이 있는지**로 고른다. 없으면 `POST /api/v1/users`(등록), 있으면 `PATCH /api/v1/users/me`(수정)다. 진입점(`ProfileEntryPoint`)은 뒤로가기·저장 후 목적지에만 쓰고 API 분기에는 쓰지 않는다.
- **Rationale**: 두 값이 대개 함께 움직이지만 어긋나는 경우가 실재한다 — 온보딩에서 등록이 성공한 뒤 다음 스텝에서 되돌아오거나, 마이페이지 진입인데 캐시가 비어 있는 복구 상황이다. 무엇을 호출할지는 "서버에 내 유저가 있는가"의 문제이므로 그 사실에 직접 묻는 편이 맞다.
- **`deviceId`**: 등록 요청은 `deviceId`를 요구한다. 이미 있는 `DeviceRepository`가 확보를 담당하지만 현재 시그니처(`suspend fun ensureDeviceId()`)가 값을 돌려주지 않아, **반환 타입을 `String`으로 넓힌다**(멱등 동작은 그대로). 프로덕션 호출자가 아직 없어 영향은 테스트뿐이다.
- **plan 4.0.0 처분 — 갱신해 되살아났다.** 분기 자체는 [D38](#d38-등록수정-분기--서버에-직접-묻고-캐시가-그-답을-들고-있는다)이 이어받되, 기준을 캐시가 아니라 **서버 응답**으로 바꿨다. 배포 문서의 `GET /api/v1/users/me`가 미등록을 `401 USER_NOT_REGISTERED`로 알려 주므로 캐시를 대리 지표로 쓸 이유가 없어졌다. `deviceId` 철회는 그대로다 — 등록 요청이 되살아나도 `deviceId`를 요구하지 않는다(신원은 Bearer 토큰이 싣는다).
- **재등록(409)**: 캐시가 비어 있는데 서버에 이미 유저가 있으면 `409 Conflict`가 온다. 이번 설계는 이것을 **저장 실패로 다룬다**(FR-012 경로). 자동으로 `PATCH`로 갈아타는 폴백은 두지 않는다 — API 문서가 "등록·인증 계약(재등록 처리 등)은 별도 인증 설계 문서에서 확정한다"고 남겨 둔 지점이라, 지금 정하면 근거 없는 동작을 만든다.
- (plan 1.1.0에서 결정)

## ~~D15. 목(mock) 구현 — flavor 소스셋으로 가른 Ktor `MockEngine`~~ — 재검토됨(plan 2.0.0)

> 목은 **원격 경로를 서버 없이 돌려보기 위한 장치**였다. 이번 범위가 원격을 연결하지 않으므로([D22](#d22-이번-범위의-저장소--로컬-datastore-단독-원격-연동은-후속-작업--재검토됨plan-400)) 목이 대신할 대상이 없다. `NetworkModule`·flavor 소스셋·`qaImplementation(libs.ktor.client.mock)`은 이번 설계에서 손대지 않는다. 원격 연동 작업이 시작될 때 이 항목을 먼저 읽는다 — 엔진만 가르는 방식과 기각된 세 대안이 그대로 유효하다.

- **Decision**: `HttpClient`의 **엔진만** 갈아 끼운다. `:core:data`의 flavor 소스셋(`src/qa/` · `src/prod/`)에 각각 엔진을 제공하는 Hilt 모듈을 두고, qa는 `MockEngine`, prod는 `OkHttp`를 제공한다. `NetworkModule`은 엔진을 주입받는 형태로 바꾸고 나머지 설정(`expectSuccess`·`convertDomainException`·`ContentNegotiation`·`Logging`)은 한 곳에 그대로 둔다. `ktor-client-mock`은 `qaImplementation`으로만 건다.
- **Rationale**: 목의 목적은 서버 없이 화면을 끝까지 돌려보는 것이고, 그러려면 **직렬화·응답 봉투 해제·에러 매핑·Repository 변환까지 실제 경로**가 돌아야 한다. DataSource나 Repository를 Fake로 바꾸면 딱 그 구간이 검증에서 빠진다. 엔진 교체는 그 구간을 모두 남기면서 네트워크 홉만 없앤다. 소스셋으로 가르면 prod 산출물에 목 코드도 `ktor-client-mock` 의존도 들어가지 않는다.
- **Alternatives considered**:
  - **`ProfileRemoteDataSource`의 Fake를 DI로 바인딩** — 가장 손이 적지만 DTO·매핑·에러 매핑 경로가 죽는다. 실서버를 붙이는 순간 처음 검증하는 코드가 된다. 기각.
  - **`BuildConfig.USE_MOCK_API` 런타임 분기** — 두 엔진 의존이 모든 빌드에 들어가고, prod APK에 목 응답 문자열이 남는다. 기각.
  - **로컬 목 서버(WireMock 등) 별도 실행** — 개발자가 서버를 띄워야 앱이 도는 상태가 되어 "임시 구축"의 취지에 어긋난다. 기각.
- **plan 4.0.0 처분 — 되살리지 않고 종결한다.** 종료 조건이 이미 충족된 상태다: `Flavor.apiBaseUrl`이 qa·prod 모두 `https://api.gguk.org/`이고 실서버가 떠 있다. 목이 대신할 대상이 없으므로 flavor 소스셋도 `ktor-client-mock`의 `qaImplementation`도 만들지 않는다([D41](#d41-목-엔진을-만들지-않는다)).
- **종료 조건**: 실서버와 인증 계약이 확정되면 qa 소스셋의 엔진을 `OkHttp`로 바꾸고 `Flavor.apiBaseUrl`을 실제 값으로 채운다. 이 전환이 목 구축의 끝이며, 그때까지 qa 빌드는 서버에 나가지 않는다.
- (plan 1.1.0에서 결정)

## ~~D16. 목 응답의 성질 — 프로세스 내 상태 유지 + 지연·실패 주입~~ — 재검토됨(plan 2.0.0)

> [D15](#d15-목mock-구현--flavor-소스셋으로-가른-ktor-mockengine--재검토됨plan-200)와 함께 물러난다. 이번 범위에서 저장 실패(FR-012)를 재현하는 방법은 [D25](#d25-저장-실패-경로--통로는-지금-배선하고-발화-원천은-후속-작업에-남긴다)가 따로 정한다.

- **Decision**: 목 핸들러는 프로세스가 사는 동안 등록된 유저를 메모리에 들고 있어 `POST` → `GET`/`PATCH`가 이어진다. 응답에 짧은 인위적 지연을 넣고, 실패 응답을 강제하는 스위치를 하나 둔다. 프로필 외 경로 요청은 `501`로 답해 "아직 목이 없다"를 드러낸다.
- **Rationale**: 지연이 0이면 `isSaving`이 만드는 중복 저장 차단(UX-003·EC-004)을 손으로 확인할 방법이 없고, 실패를 낼 수 없으면 FR-012·TS-006·EC-003·EC-007을 기기에서 재현할 수 없다. 다른 경로를 조용히 성공시키면 아직 붙지 않은 기능이 붙은 것처럼 보인다.
- **plan 4.0.0 처분 — 되살리지 않고 종결한다.** [D41](#d41-목-엔진을-만들지-않는다)과 함께 닫힌다.
- **범위**: 목이 답하는 것은 유저 엔드포인트 3종뿐이다. 방·핀·코멘트·알림 엔드포인트는 각 기능의 계획이 자기 몫을 더한다.
- (plan 1.1.0에서 결정)

## D17. 개인방(`내 장소`) 생성 — 서버가 등록과 함께 처리한다(확정)

- **Decision**: [D10](#d10-개인방내-장소-생성--이-모듈이-트리거하지-않는다)의 결정(이 모듈이 트리거하지 않는다)을 유지하고, 그 근거를 가정에서 사실로 바꾼다. `POST /api/v1/users`가 "유저 등록 (+ 개인방 자동 생성)"으로 정의되어 있고 개인방은 응답에 포함되지 않는다.
- **결과**: FR-008의 "개인방 생성을 트리거한다"는 등록 요청 성공으로 충족된다. 클라이언트가 방 API를 호출하지 않으며, D10에 남겨 둔 "생성 주체 미확정" 항목은 닫힌다.
- **plan 4.0.0 보정 — 충족된다.** 배포된 OpenAPI 문서가 `POST /api/v1/users`를 "익명 인증 토큰의 uid로 등록한다. 개인방(내 장소) 생성이 같은 흐름에서 처리되며 응답에는 포함하지 않는다"로 확정했다(2026-08-27 조회). 등록 요청이 이번 범위에 들어오므로 FR-008의 개인방 생성 부분이 닫힌다. 클라이언트는 여전히 방 API를 호출하지 않는다.
- **plan 2.0.0 보정**: 생성 주체가 서버라는 **사실**은 그대로지만, 그 트리거가 되는 등록 요청이 이번 범위에 없다([D22](#d22-이번-범위의-저장소--로컬-datastore-단독-원격-연동은-후속-작업--재검토됨plan-400)). FR-008의 개인방 생성 부분은 원격 연동 작업에서 충족되며, 이번 범위가 만족시키는 것은 FR-008의 나머지(저장 완료 후 온보딩 다음 스텝으로 이동)뿐이다. 이 미충족은 [plan.md](plan.md) Constitution Check의 원칙 IV 항목에 명시한다.
- (plan 1.1.0에서 결정)

## ~~D18. 아바타 식별자 — 서버 계약을 따라 `Int`~~ — 재검토됨(plan 4.0.0)

> 이 결정의 유일한 근거는 서버 스키마가 `Avatar { id: integer }`라는 것이었는데, **배포된 OpenAPI 문서에 그 스키마가 없다.** 실제 계약은 `avatar: { color: string(1..20) }`이다(2026-08-27 조회). 근거가 사라졌으므로 타입을 재판정한다 — 새 결정은 [D37](#d37-아바타-식별자--도메인-profileavatar-enum-서버-표현은-avatarcolor-문자열)이다. 아래 본문은 초안 swagger를 근거로 삼았던 이력으로 남긴다.

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
- **T083 실측 보정(2026-08-28) — 거절 코드는 `400 Bad Request`다.** 4.0.0이 "문서에 없다"로 남긴 마지막 빈칸을 기기 확인이 채웠다([quickstart §4-3](quickstart.md) 20번). 16자 이상 닉네임을 저장하면 서버가 `400`으로 거절하고, `expectSuccess = true` → `convertDomainException`이 그것을 `MinoDomainException.Http(400)`으로 바꿔 화면이 저장 실패 스낵바를 띄운다. **이 결정이 예측한 경로가 그대로 관측됐다** — 클라이언트에 상한을 심지 않고 서버 거절을 저장 실패로 받는다는 판단이 실측으로 확인됐으므로 [spec](spec.md) §5·EC-014를 고칠 이유가 없다. [API 계약 §2](contracts/profile-api-contract.md)의 협의 항목 4가 이것으로 닫혔다.
- **plan 5.1.0 보정 — 어긋남이 양쪽에서 닫혀 이 결정의 전제가 사라졌다.** 두 지점이 **서로 다른 쪽에서** 해소됐다. **상한**은 [spec 3.0.0](spec.md)이 PRD 10.0.0을 따라 15자를 채택하면서 닫혔고(§5, FR-014 신설), **공백**은 **서버가 `pattern`에서 공백을 뺐다**(2026-08-31 재조회, [D52](#d52-서버-문서-재조회2026-08-31--닉네임-pattern에서-공백이-빠졌다)). 이제 클라이언트 규칙과 서버 스키마가 `2~15자 · 한글·영문 · 공백 불가`로 **완전히 같다.** 이 결정이 감수하기로 한 "어긋남을 저장 실패로 드러낸다"는 **더 이상 발화하지 않는다** — 길이 초과는 [D51](#d51-닉네임-15자-상한의-강제-지점--viewmodel이-자른다)의 입력 차단이, 공백은 `ValidateNicknameUseCase`가 서버에 닿기 전에 막는다. 결정 자체를 뒤집는 것이 아니라 **적용 대상이 없어진 것**이므로 취소선을 긋지 않는다. T083이 실측한 `400`은 관측 기록으로 남되, 닉네임 길이로는 재현되지 않는다.
- **plan 4.1.0 보정 — spec이 확정해 이 항목이 닫힌다.** [spec 2.0.0](spec.md) §5가 두 지점을 모두 확정했다 — **상한**은 두지 않고(16자 이상은 서버가 거절해 저장 실패로 보인다, 신설 EC-014), **공백**은 불가를 유지한다(클라이언트가 더 좁아 서버까지 가지 않으므로 실패가 없다). 즉 이 결정의 "클라이언트는 spec을 따른다"가 spec 자신의 확정으로 승격됐고, "후속 필요"로 남겨 둔 spec 개정도 끝났다. **협의 항목에서 내린다** — 어긋남은 남지만 알고 받아들이기로 한 어긋남이다.
- **plan 4.0.0 보정 — 어긋남이 드러난다.** 배포 문서가 `nickname`을 `minLength 2 · maxLength 15 · pattern ^[\uAC00-\uD7A3A-Za-z ]+$`로 확정했다(2026-08-27 조회). spec과 어긋나는 두 지점(**상한**: spec 없음 / 서버 15자, **공백**: spec 불가 / 서버 허용)이 그대로다. 클라이언트가 spec을 따른다는 결정은 유지하고, 16자 이상 닉네임은 서버가 거절해 FR-012의 저장 실패로 사용자에게 보인다. **거절 시의 상태 코드가 문서에 없다** — `POST`는 401·409만, `PATCH`는 401만 문서화되어 있다. `expectSuccess = true`가 어떤 비2xx든 `MinoDomainException.Http`로 바꾸므로 화면 동작은 성립하지만, 코드 확정은 서버팀 협의 항목이다([API 계약](contracts/profile-api-contract.md) §2).
- **plan 2.0.0 보정**: 이번 범위에는 서버 거절 경로가 없으므로 어긋남이 **드러나지 않는다** — 15자를 넘는 닉네임이 로컬에 그대로 저장되고 아무 실패도 나지 않는다. 어긋남이 사용자에게 처음 보이는 시점은 원격 연동 작업이며, 그전에 spec을 정리하는 편이 낫다. 클라이언트가 spec을 따른다는 결정 자체는 그대로다.
- (plan 1.1.0에서 결정)

## D20. 인증 헤더 — 이번 범위에서 배선하지 않는다

- **Decision**: `GET`·`PATCH /api/v1/users/me`가 요구하는 Bearer 토큰을 이번 설계에서 붙이지 않는다. 목 엔진은 인증을 검사하지 않으므로 화면 흐름은 완성되고, 실서버 전환 시 토큰 주입 지점(`defaultRequest`의 헤더 또는 플러그인)을 그때 정한다.
- **Rationale**: API 문서가 "인증은 별도 인증 설계 문서에서 확정한다(잠정: Bearer 토큰)"로 미뤄 둔 상태다. 토큰 발급 자체도 이 저장소에서 별도 이슈로 분리되어 있어, 지금 헤더 규약을 정하면 확정될 계약과 어긋날 가능성이 높다.
- **경계**: 이 결정은 목 구축이 끝나는 시점(D15 종료 조건)과 같은 지점에서 닫힌다. 그전까지 `:feature:profile`은 인증 상태를 알지 못한다.
- **plan 4.0.0 보정 — 닫혔다. 다만 이 범위가 배선하는 것이 아니다.** 이 결정이 쓰인 뒤 이슈 #176(익명 인증 세션)이 `MinoIdentityProofPlugin`을 `HttpClient` 전역에 설치해 두었다. Mino host로 나가는 모든 요청에 `Authorization: Bearer`가 이미 붙으므로, 프로필 원격 연동은 헤더에 **아무것도 더하지 않는다.** 계약은 `docs/specs/anonymous-auth-session/contracts/identity-proof-attachment.md`가 소유한다. `:feature:profile`이 인증 상태를 알지 못한다는 경계도 그대로다.
- **plan 2.0.0 보정**: 원격 자체가 이연되면서 이 결정은 더 넓은 이연([D22](#d22-이번-범위의-저장소--로컬-datastore-단독-원격-연동은-후속-작업--재검토됨plan-400))에 흡수된다. 판단은 뒤집히지 않았고, 닫히는 시점만 원격 연동 작업으로 옮겨 갔다.
- (plan 1.1.0에서 결정)

## ~~D21. 테스트 범위 확장 — 목 엔진 기반 데이터 레이어 테스트를 더한다~~ — 재검토됨(plan 2.0.0)

> 덮으려던 대상(응답 봉투 해제·DTO 매핑·비2xx 변환·캐시 갱신 순서)이 이번 범위에 존재하지 않는다([D22](#d22-이번-범위의-저장소--로컬-datastore-단독-원격-연동은-후속-작업--재검토됨plan-400)). 테스트 범위는 [D12](#d12-테스트-범위--jvm-단위-테스트만)로 되돌아간다.

- **Decision**: [D12](#d12-테스트-범위--jvm-단위-테스트만)의 범위에 `ProfileRepositoryImpl`의 원격 경로 테스트를 더한다. `ktor-client-mock`은 이미 `:core:data`의 테스트 의존이므로, 기존 `DomainExceptionMappingTest` 방식으로 응답 봉투 해제·DTO 매핑·비2xx의 도메인 예외 변환·캐시 갱신 순서를 덮는다.
- **Rationale**: 원격이 원천이 되면서 "성공 시에만 캐시를 갱신한다"(D13)가 사용자에게 보이는 규칙(FR-012·SC-006)이 됐다. 이 순서는 화면이 아니라 데이터 레이어의 불변식이라 단위 테스트가 지킬 자리다.
- **plan 4.0.0 처분 — 되살아났다.** 덮을 대상이 실재하게 됐다. 범위는 [D43](#d43-테스트-범위--mockengine-기반-데이터-레이어-테스트를-더한다)이 다시 적는다.
- (plan 1.1.0에서 결정)

---

## ~~D22. 이번 범위의 저장소 — 로컬 DataStore 단독, 원격 연동은 후속 작업~~ — 재검토됨(plan 4.0.0)

> 이 결정이 스스로 적어 둔 닫히는 조건("원격 연동 작업이 착수되는 것")이 충족됐다. 사용자가 실서버 연결을 지시했고, [D24](#d24-원격-연동이-붙을-때-바뀌는-지점을-지금-고정한다)가 미리 고정한 표가 이번 개정의 입력이 됐다. 새 결정은 [D36](#d36-원격-연동-착수--원천은-서버-로컬-datastore는-캐시)이다. 이연이 예정대로 닫힌 것이지 판단이 틀렸던 것이 아니므로 본문은 그대로 남긴다.

- **Decision**: 이번 구현 범위에서 프로필의 저장소는 `:core:data`의 공유 Preferences DataStore(`storage/DataStoreModule`) **하나뿐**이다. `:core:data`에 프로필 관련 DTO·`ApiService`·`RemoteDataSource`·목 엔진·flavor 소스셋을 만들지 않고, `NetworkModule`도 손대지 않는다. 원격 연동은 별도 후속 작업으로 분리하며, 그때 바뀌는 지점은 [D24](#d24-원격-연동이-붙을-때-바뀌는-지점을-지금-고정한다)가 미리 고정한다.
- **Rationale**: 사용자가 이번 계획의 범위를 "UI 및 도메인 모델 설계 위주, API 연결은 추후"로 지정했다. [D13](#d13-프로필-저장-경로--원격-api가-원천-로컬-datastore는-캐시--재검토됨plan-200)이 원격을 원천으로 삼은 근거(계약이 존재한다)는 사라지지 않았지만, 그 계약을 **언제 소비할지**는 계약의 존재와 별개의 판단이고 그 판단권은 사용자에게 있다. 화면·상태·도메인 경계는 저장소의 위치와 무관하게 결정되므로([D3](#d3-프로필-저장소--로컬-datastore-단독-원격은-인터페이스-뒤로-이연--재검토됨plan-110)의 기각 사유 (b)가 이번에도 그대로 성립한다) 이번 범위만으로도 화면이 끝까지 도는 완결된 산출물이 나온다.
- **[D3](#d3-프로필-저장소--로컬-datastore-단독-원격은-인터페이스-뒤로-이연--재검토됨plan-110)과의 관계**: 결과물의 **모양**은 D3과 같지만 **근거는 다르다.** D3은 "계약이 없어서" 로컬 단독이었고 계약이 생긴 순간 무너졌다. 이 결정은 "계약은 있으나 이번 범위가 아니어서" 로컬 단독이므로, 계약이 갱신되거나 서버가 뜨더라도 스스로 무너지지 않는다. 닫히는 조건은 하나 — 원격 연동 작업이 착수되는 것이다.
- **spec과의 어긋남**: spec §4는 "프로필 저장은 서버 반영을 포함하며, 사용자는 저장 시점에 네트워크에 연결되어 있다고 가정한다"를 가정으로 적고 있다. 이 결정은 그 가정을 **부정하지 않고 충족 시점을 후속 작업으로 미룬다.** 이번 범위의 산출물만으로는 spec의 그 가정과 FR-008의 개인방 생성([D17](#d17-개인방내-장소-생성--서버가-등록과-함께-처리한다확정))이 충족되지 않으며, 이 사실을 [plan.md](plan.md) Constitution Check와 [quickstart.md](quickstart.md) §4가 명시적으로 든다. 설계로 봉합하지 않는다.
- **Alternatives considered**:
  - **원격 계층을 만들되 목 엔진으로만 돌린다**([D13](#d13-프로필-저장-경로--원격-api가-원천-로컬-datastore는-캐시--재검토됨plan-200)+[D15](#d15-목mock-구현--flavor-소스셋으로-가른-ktor-mockengine--재검토됨plan-200) 유지) — 사용자가 지정한 범위를 넘는다. 또한 목만 있는 원격 계층은 실서버가 뜰 때까지 **검증되지 않은 코드**로 남고, 그 사이 서버 계약이 초안 단계에서 바뀌면 만든 만큼 다시 고쳐야 한다. 기각.
  - **저장 자체를 하지 않고 화면만 만든다(인메모리)** — FR-006(마이페이지 진입 프리필)과 SC-003(앱 전역 즉시 반영)이 저장된 값을 전제하므로 spec 요구사항이 통째로 검증 불가가 된다. 로컬 저장은 "API 연결"이 아니라 도메인 모델이 실재하기 위한 최소치다. 기각.
- **ADR 승격 후보 아님**: 이것은 이 feature의 **작업 순서**에 대한 결정이지 다른 feature를 구속하는 구조 결정이 아니다. 반면 [D13](#d13-프로필-저장-경로--원격-api가-원천-로컬-datastore는-캐시--재검토됨plan-200)이 정한 "원격이 원천, 로컬은 캐시"는 되살아날 때 승격 후보로 남는다.
- (plan 2.0.0에서 결정)

## ~~D23. Repository 표면 — `observeProfile()` + `saveProfile()` 두 멤버~~ — 재검토됨(plan 4.0.0)

> "쓰지 않을 계약을 미리 열어 두지 않는다"는 판단은 유효했고, 이제 `refreshProfile()`을 쓸 곳이 생겼다. 새 표면은 [D39](#d39-repository-표면--observeprofile--refreshprofile--saveprofile-세-멤버)가 정한다. 저장을 등록/수정으로 가르지 않고 `saveProfile` 하나로 두는 판단은 [D38](#d38-등록수정-분기--서버에-직접-묻고-캐시가-그-답을-들고-있는다)이 이어받는다.

- **Decision**: `ProfileRepository`를 두 멤버로 둔다 — `fun observeProfile(): Flow<Profile?>`과 `suspend fun saveProfile(profile: Profile)`. [D14](#d14-등록과-수정의-분기--진입점이-아니라-저장된-프로필의-유무로-가른다--재검토됨plan-200)가 나눴던 `registerProfile`·`updateProfile`과 [D13](#d13-프로필-저장-경로--원격-api가-원천-로컬-datastore는-캐시--재검토됨plan-200)이 열었던 `refreshProfile`을 모두 거둔다. `SaveProfileUseCase`는 `DeviceRepository`를 알지 않는다.
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
  | `:core:domain`의 `Profile` 모델 | **그대로** — 필드(`nickname`·`avatarId: Int`)가 서버 스키마에서 도출됐다([D18](#d18-아바타-식별자--서버-계약을-따라-int--재검토됨plan-400)) |
  | `ProfileRepository` 인터페이스 | **넓어진다** — `refreshProfile()` 추가, 저장이 등록/수정으로 갈릴지는 그때 [D14](#d14-등록과-수정의-분기--진입점이-아니라-저장된-프로필의-유무로-가른다--재검토됨plan-200)를 다시 읽고 판단한다 |
  | `ProfileRepositoryImpl` (`:core:data`) | **바뀐다** — 원격 호출을 앞에 두고 로컬을 캐시로 내린다([D13](#d13-프로필-저장-경로--원격-api가-원천-로컬-datastore는-캐시--재검토됨plan-200)) |
  | `ProfileLocalDataSource`·DataStore 키 | **그대로** — 저장 형태가 캐시로 역할만 바뀐다 |
  | `:feature:profile` 전체 | **그대로** — 화면·ViewModel·Intent·SideEffect가 저장 경로를 모른다 |
  | `:core:design-system` 신설 컴포넌트 | **그대로** — 도메인·데이터를 모른다 |
  | `NetworkModule`·flavor 소스셋 | **그때 처음 손댄다** — 이번 범위에서 건드리지 않는다 |

- **Rationale**: "추후 진행"은 그 추후가 무엇을 부수는지 적혀 있을 때만 안전한 이연이다. 이 표가 없으면 원격 작업이 화면까지 되짚어 고치게 되고, 이연이 절약한 만큼을 재작업으로 돌려주게 된다. 표의 내용 자체는 새 판단이 아니라 [D9](#d9-앱-전체-즉시-반영--observeprofile-flowprofile)(Flow 구독)·[D18](#d18-아바타-식별자--서버-계약을-따라-int--재검토됨plan-400)(식별자 타입)·[D7](#d7-닉네임-검증의-위치--coredomain의-usecase)(검증 위치)이 이미 만들어 둔 경계를 읽어낸 것이다.
- **plan 4.0.0 검증 — 표의 절반이 맞았고 두 줄이 틀렸다.** 이 표는 예측이었으므로 실행된 지금 대조해 둔다. 이연을 안전하게 만드는 장치의 값어치가 다음 이연에서 판단되려면 이 대조가 남아야 한다.

  | 예측 | 실제 | |
  |---|---|---|
  | `Profile` 모델 **그대로** | **틀렸다** — 아바타 필드의 타입이 바뀐다 | 예측의 근거였던 서버 스키마(`Avatar { id: integer }`)가 배포 문서에 없었다([D37](#d37-아바타-식별자--도메인-profileavatar-enum-서버-표현은-avatarcolor-문자열)) |
  | `ProfileRepository` **넓어진다** | 맞았다 | `refreshProfile()` 추가([D39](#d39-repository-표면--observeprofile--refreshprofile--saveprofile-세-멤버)) |
  | `ProfileRepositoryImpl` **바뀐다** | 맞았다 | 원격을 앞에 두고 로컬을 캐시로 내린다 |
  | `ProfileLocalDataSource`·키 **그대로** | **틀렸다** — 반환 타입과 아바타 키가 함께 바뀐다 | 아바타 타입 변경([D37](#d37-아바타-식별자--도메인-profileavatar-enum-서버-표현은-avatarcolor-문자열))과 `ProfileEntry` 도입([D42](#d42-로컬-캐시-datasource는-profileentry-dto를-반환한다))이 겹쳤다 |
  | `:feature:profile` **그대로** | 절반만 맞았다 | 실패 통로·`isSaving`·화면 구조는 손대지 않는다([D25](#d25-저장-실패-경로--통로는-지금-배선하고-발화-원천은-후속-작업에-남긴다) 보정). 아바타 매핑(`ProfileAvatarId.kt`)과 진입 시 갱신 호출은 바뀐다 |
  | `:core:design-system` **그대로** | 맞았다 | 12종 enum·컴포넌트를 건드리지 않는다 |
  | `NetworkModule`·flavor 소스셋 **그때 처음 손댄다** | **손대지 않는다** | 인증 플러그인이 이미 설치돼 있고 목을 만들지 않으므로([D41](#d41-목-엔진을-만들지-않는다)) 네트워크 인프라는 그대로다. 예측보다 나은 결과다 |

  **틀린 두 줄의 공통 원인은 하나다** — 표가 근거로 삼은 서버 계약이 브랜치 초안 swagger였고, 배포 문서와 달랐다. 이연 표의 정확도는 그것이 딛고 선 계약의 안정성을 넘지 못한다.
- **후속 작업의 입력**: 원격 연동을 설계할 때 되살릴 항목은 [D13](#d13-프로필-저장-경로--원격-api가-원천-로컬-datastore는-캐시--재검토됨plan-200)·[D14](#d14-등록과-수정의-분기--진입점이-아니라-저장된-프로필의-유무로-가른다--재검토됨plan-200)·[D15](#d15-목mock-구현--flavor-소스셋으로-가른-ktor-mockengine--재검토됨plan-200)·[D16](#d16-목-응답의-성질--프로세스-내-상태-유지--지연실패-주입--재검토됨plan-200)·[D20](#d20-인증-헤더--이번-범위에서-배선하지-않는다)·[D21](#d21-테스트-범위-확장--목-엔진-기반-데이터-레이어-테스트를-더한다--재검토됨plan-200)이다. 서버 계약의 스냅숏은 plan 1.1.0의 `contracts/profile-api-contract.md`에 있었고, 이번 개정에서 지웠으므로 git 이력(`e1ac7a0`)에서 되살린다. 원본은 [D13](#d13-프로필-저장-경로--원격-api가-원천-로컬-datastore는-캐시--재검토됨plan-200)이 적어 둔 URL이다.
- (plan 2.0.0에서 결정)

## D25. 저장 실패 경로 — 통로는 지금 배선하고, 발화 원천은 후속 작업에 남긴다

- **Decision**: [D8](#d8-저장-실패중복-저장--domainerroremitter--issaving-플래그)이 정한 실패 통로(`runCatchingDomain` → `emitDomainError` → `CollectDomainError` → 스낵바)와 `isSaving` 플래그를 이번 범위에서 **그대로 만든다.** 다만 로컬 저장에서는 이 통로를 발화시킬 원천이 사실상 없다 — DataStore 쓰기 실패는 디스크 이상 같은 예외 상황이다. 실패 경로의 **검증**은 `SaveProfileUseCase`·`ProfileViewModel`의 Fake 기반 단위 테스트(Fake Repository가 예외를 던지는 경우)로 받고, 기기에서의 수동 재현은 후속 작업으로 미룬다.
- **Rationale**: FR-012·EC-003·EC-007·SC-006은 spec의 요구사항이고, 통로를 나중에 뚫으면 ViewModel·Route·Activity를 다시 열어야 한다([D24](#d24-원격-연동이-붙을-때-바뀌는-지점을-지금-고정한다)가 `:feature:profile`을 "그대로"로 둔 근거가 이 배선이다). 반대로 발화 원천을 억지로 만들려고 로컬에 인위적 실패 스위치를 넣으면, 프로덕션 코드에 이번 범위 밖의 장치가 남는다.
- **plan 4.0.0 보정 — 발화 원천이 생겼다.** 이 결정이 "후속 작업에 남긴다"고 적은 것이 이번 범위다. HTTP 비2xx·네트워크 실패가 `convertDomainException`을 거쳐 `MinoDomainException`이 되므로, [D8](#d8-저장-실패중복-저장--domainerroremitter--issaving-플래그)의 통로가 실제로 발화한다. 통로를 미리 배선해 둔 덕에 `:feature:profile`에서 고칠 곳은 없다 — [D24](#d24-원격-연동이-붙을-때-바뀌는-지점을-지금-고정한다)가 이 배선을 근거로 feature를 "그대로"로 둔 예측이 여기서는 맞았다. `isSaving`도 네트워크 왕복만큼 눈에 보이게 된다.
- **`isSaving`의 성격**: 로컬 저장은 사실상 즉시 끝나므로 이번 범위에서 `isSaving`이 눈에 보이는 시간은 거의 0이다. UX-003·EC-004의 중복 저장 차단은 눈이 아니라 ViewModel 단위 테스트(저장 중 두 번째 인텐트가 무시되는지)로 확인한다.
- (plan 2.0.0에서 결정)

## D26. 아바타 그리드의 배치 — 화면이 소유하고 `LazyVerticalGrid`를 쓰지 않는다

- **Decision**: 아바타 12칸의 배치(4열 × 3행)를 `:feature:profile`의 `component/`에 두고, `LazyVerticalGrid`가 아니라 고정 `Column` + `Row` 조합으로 그린다. 컴포넌트 한 칸은 `MinoProfileAvatarImage`([D5](#d5-아바타를-그리는-컴포넌트--기존-minoavatar를-확장하지-않고-전용-컴포넌트-신설))이고, 그리드는 자기 칸을 어떻게 그리는지 모른다.
- **Rationale**: 항목이 12개로 고정이고 서버에서 내려받지 않으므로(spec §4) 지연 로딩이 얻을 것이 없다. 반대로 잃는 것은 있다 — 화면 전체가 세로 스크롤 하나로 흐르는 구조(상단 바 → 안내 문구 → 썸네일 → 입력 필드 → 그리드 → 액션)에서 `LazyVerticalGrid`를 중첩하면 높이 제약이 무한대가 되어 크래시하거나 고정 높이를 억지로 지정해야 한다.
- **Alternatives considered**: (a) `FlowRow`로 열 수를 폭에 맡긴다 — 디자인이 4열을 고정하고 있어 폭에 따라 3열·5열이 되는 것은 원본과의 대조를 깨뜨린다. 기각. (b) `LazyVerticalGrid`를 화면 최상위로 올리고 나머지 요소를 `span`으로 얹는다 — 12칸 때문에 화면 전체 구조를 그리드에 맞추는 역전이다. 기각.
- (plan 2.0.0에서 결정)

---

## D27. 상단 바는 화면 목업이 쓰는 iOS variant를 따른다

- **Decision**: `MinoTopNavigation`이 Figma 컴포넌트셋 `16215-20432`의 **`Platform=iOS` variant**(`16215-20433`)를 구현한다. 바 높이 44dp, 제목 가운데 정렬, 뒤로가기 아이콘은 셰브런(`MinoIcons.ChevronLeft`), 타이포는 `Headline 2/Bold`(17/24)다.
- **Rationale**: 프로필 화면 목업 010-1·010-2·010-3이 실제로 붙이고 있는 인스턴스(`2314-95704`)가 iOS variant다. 디자인 검수가 두 파일을 열어 확인했고, 화면 인스턴스와 DS iOS variant의 Bar 하위 구조·치수가 완전히 일치했다. **사용자가 "화면 목업을 따른다"로 확정했다** — 코드를 Android variant로 두고 디자이너에게 인스턴스 교체를 요청하는 갈래를 택하지 않았다.
- **상태 표시줄 인셋은 가져오지 않는다**: iOS variant의 `Spacing/Status`(54)는 `Bar`와 **형제 노드**이고 iOS 상태바 높이다. 이 컴포넌트는 인셋을 갖지 않으므로([design-system 계약](contracts/design-system-contract.md) §2) 44만 취한다.
- **결과로 생기는 위험**: iOS variant는 Title 양쪽 Filler 폭이 0이고 Leading·Trailing이 절대배치라, **제목이 바 전폭 기준 중앙이고 뒤로가기가 그 위에 겹친다.** 긴 제목은 뒤로가기 아래로 흘러 들어간 뒤 말줄임된다. 원본 구조 그대로이며 임의로 여백을 넣어 고치지 않았다. `프로필 설정` 넉 자에서는 드러나지 않지만 긴 제목을 쓰는 화면이 이 컴포넌트를 재사용하면 문제가 된다.
- **Alternatives considered**: Android variant를 유지하고 화면 목업의 인스턴스 교체를 요청한다 — plan 2.0.0 시점의 [D6](#d6-상단-내비게이션--coredesign-system에-minotopnavigation-신설--부분-재검토됨plan-300) 해석이자 사용자에게 먼저 제시한 안이다. 앱이 Android인데 iOS chrome을 쓰는 어색함과 DS가 Android variant를 따로 둔다는 점이 근거였으나, **사용자가 목업을 기준으로 삼기로 했다.** 기각 이력으로 남긴다.
- **ADR 승격 후보**: 아니다. 이 화면이 어느 variant를 쓰는가는 이 feature의 선택이다. 다만 **다른 화면이 상단 바를 쓸 때 같은 판단이 반복된다** — 앱 전체가 iOS variant를 쓰는지가 정해지면 그때 ADR 대상이다.
- (plan 3.0.0에서 결정)

## D28. 아바타 선택 상태의 시각 표시를 만들지 않는다

> **plan 4.1.0 보정 — spec이 이 결정을 받아들였다.** [spec 2.0.0](spec.md)이 FR-003에서 칸 단위 표시 요구를 걷어내고 "선택 상태는 상단 프로필 썸네일로 드러난다"로 고쳤다. 함께 **보조 수단(화면 낭독 등)에는 선택 여부를 전달한다**를 명시하고 TS-019를 신설했으므로, 이 결정이 남겨 둔 접근성 시맨틱이 이제 spec 근거를 갖는다. 원칙 IV의 미충족 1건이 닫힌다.

- **Decision**: `MinoProfileAvatarImage`의 `selected`는 접근성 시맨틱(`Modifier.rippleSingleSelectable`)만 싣고 **테두리·체크마크 등 시각 표시를 그리지 않는다.** 선택은 상단 썸네일이 바뀌는 것으로만 드러난다.
- **Rationale**: Figma 원본 어디에도 선택된 칸을 구별하는 표현이 없다. 디자인 검수가 010-1·010-2·그리드 노드를 독립적으로 열어 확인했고, 그리드 프레임의 변수 전수가 12칸 모두 같은 4개(`Label/Normal`·`Background/Normal/Normal`·`Static/White`·`Line/Normal/Alternative`)뿐이라 **선택 칸만 쓰는 색이 아예 없다.** 기기 검증에서도 선택 전후 그리드 영역의 픽셀 차이가 없음을 확인했다. **사용자가 "선택 표시를 만들지 않는다"로 확정했다.**
- **spec 미충족**: FR-003의 "그 아바타만 선택 상태로 표시"와 TS-003·TS-004의 육안 판정은 **충족되지 않는다.** 원본에 근거가 없는 것을 지어내지 않는다는 판단이며([헌법](../../constitution.md) 원칙 IV), 미충족 사실을 [quickstart.md §4](quickstart.md)가 든다.
- **Alternatives considered**: (a) Primary 토큰으로 선택 테두리를 임의 결정한다 — FR-003은 충족하지만 대조할 원본이 없어 검수가 미검증으로 남고, 디자이너 확인 시 재작업 위험이 있다. 기각. (b) 컴포넌트 정의 노드 링크를 받아 selected variant 유무를 확인한다 — 사용자에게 제시했으나 채택되지 않았다.
- (plan 3.0.0에서 결정)

## D29. 온보딩 진입에서 뒤로가기를 노출하지 않는다

> **plan 4.1.0 보정 — spec이 이 결정을 받아들였다.** [spec 2.0.0](spec.md)이 FR-010을 "뒤로가기를 노출하지 않아"로 고치고 TS-001·유저 플로우 1 진입 조건을 함께 정정했다. 원칙 IV의 미충족 1건이 닫힌다. **다만 PRD `[SYS-011]` Flow D는 아직 "비활성"이라 PRD 개정이 남아 있다** — spec 2.0.0 §5가 그 사실을 든다.

- **Decision**: 온보딩 진입(`ProfileEntryPoint.Onboarding`)에서 상단 바의 뒤로가기 버튼을 **아예 숨긴다** — `MinoTopNavigation(onBackClick = null)`이며 leading 슬롯 24dp 자리는 남는다. 시스템 back은 `BackHandler(enabled = true) {}`로 계속 삼킨다(EC-001).
- **Rationale**: spec FR-010은 "온보딩 진입에서는 뒤로가기를 **비활성으로 두어** 화면을 벗어나지 못하게" 한다고 적었으나, 그 비활성 상태의 시각을 담은 노드가 컴포넌트 정의 노드 제약으로 열리지 않아 **디자인 근거 없는 값을 그려야 하는 상황**이었다. **사용자가 "아예 숨긴다"로 확정했다.**
- **spec과의 어긋남**: FR-010의 "노출하되 비활성"과 다르다. 화면을 벗어나지 못하게 한다는 **목적은 그대로 충족**되며, 달라지는 것은 버튼이 보이느냐다.
- **결과**: [D34](#d34-minotopnavigation에-backenabled-파라미터를-두지-않는다)가 이 결정에서 파생된다 — 비활성 경로의 호출자가 사라졌다.
- (plan 3.0.0에서 결정)

## D30. 로컬 저장 실패용 도메인 예외 리프를 추가하지 않는다

- **Decision**: `MinoDomainException`에 로컬 저장 실패용 리프를 추가하지 않는다. `ProfileLocalDataSourceImpl`·`ProfileRepositoryImpl`은 DataStore 예외를 잡아 변환하지 않고 그대로 전파한다.
- **Rationale**: [`core:error-handling` README](../../../core/error-handling/README.md) §4가 "리프는 `:core:data`의 **매핑 지점 화이트리스트와 짝으로** 추가한다"·"**탈출구 리프 금지** — 매핑 규칙에 걸리지 않는 예외는 버그이며 CEH 소관"으로 정한다. 로컬 DataStore 쓰기 실패에는 매핑 지점이 없고, [에러 처리 규약](../../conventions/error_handling.md) §1의 분류에서 디스크 이상은 버그 갈래다. [D25](#d25-저장-실패-경로--통로는-지금-배선하고-발화-원천은-후속-작업에-남긴다)가 "통로는 지금 배선하고 발화 원천은 후속 작업에 남긴다"로 예고한 상태와 같다.
- **남는 어긋남**: `ProfileRepository.saveProfile`의 실패 계약은 `MinoDomainException`인데 이번 범위에 그것을 만드는 지점이 없다. 규범 감사가 SHOULD로 지적했고, **계약을 지우지 않고 "지금은 발화 원천이 없다"를 KDoc에 덧붙여** 닫았다. 원격 연동에서 매핑 지점이 생길 때 함께 닫힌다.
- **Alternatives considered**: `Storage` 리프를 추가한다 — 매핑 지점 없이 리프만 늘리면 README §4의 두 규칙을 동시에 어긴다. 기각.
- **plan 4.0.0 보정 — 결정은 유지되고 "남는 어긋남"은 닫힌다.** 원격이 붙어도 새 리프를 만들지 않는다. HTTP 원천의 매핑 지점(`convertDomainException`)이 이미 있고 `Http`·`Network` 리프로 충분하기 때문이다. `saveProfile`의 실패 계약을 만드는 지점이 그 매핑이므로, KDoc에 덧붙였던 "지금은 발화 원천이 없다"는 단서를 걷어낸다.
- (plan 3.0.0에서 결정)

## D31. ViewModel 단위 테스트는 `isReturnDefaultValues`로 열고, 진입점은 통제하지 않는다

- **Decision**: `feature/profile/build.gradle.kts`에 `testOptions { unitTests { isReturnDefaultValues = true } }`를 둔다. 그 결과 **JVM 테스트에서 `savedStateHandle.toRoute<ProfileMain>()`이 항상 `null`을 돌려주므로 `ProfileViewModel.entryPoint`는 늘 `MyPage`다.** 진입점별 분기는 ViewModel을 거치지 않고 `ProfileUiState(entryPoint = ...)`를 직접 세워 검증한다.
- **Rationale**: `androidx.lifecycle 2.10.0`·`savedstate 1.4.0`에서 `SavedState`가 `Bundle` 기반으로 바뀌어, `toRoute`가 `android.os.Bundle.putCharSequence`를 타고 스텁 `android.jar`에서 예외로 죽는다. **ViewModel을 생성조차 할 수 없었다.** `:core:data`에 같은 블록·같은 사유의 선례가 있고, 이 파일은 이번 범위에서 새로 만든 것이라 plan이 열거한 "기존 파일 변경"에도 해당하지 않는다.
- **덮이는 범위**: US1·US2·US3의 케이스는 어느 것도 진입점을 읽지 않아 전부 성립한다. 성립하지 않는 유일한 종류가 진입점별 분기이며, 그것은 파생 프로퍼티라 UiState 직접 구성으로 검증된다.
- **Alternatives considered**: (a) Robolectric 도입 — [D12](#d12-테스트-범위--jvm-단위-테스트만)의 "테스트 인프라 도입은 범위 밖"을 뒤집고 버전 카탈로그에 항목을 더해야 한다. 기각. (b) `toRoute` 대신 `savedStateHandle.get<String>()`으로 복원 — [feature-navigation.md](../../architecture/feature-navigation.md) 2장이 `toRoute` 복원을 명시 규정한다. 기각. (c) Route 인자에 기본값을 주어 `contains` 경로로 우회 — 메커니즘은 성립하지만 프로덕션 시그니처를 테스트 편의로 바꾸는 것이라 불필요해진 시점에 폐기.
- **후속 제안**: 이 블록은 `:feature:profile`만의 문제가 아니다. `toRoute`를 쓰는 모든 feature의 ViewModel 테스트가 같은 벽을 만나므로 `AndroidFeatureConventionPlugin`으로 올릴 후보다.
- (plan 3.0.0에서 결정)

## D32. 화면은 상단 바와 액션 영역을 고정하고 본문만 스크롤한다

- **Decision**: `ProfileScreen`은 바깥 `Column`에 `MinoTopNavigation`을 고정으로 두고, 본문(안내 문구 → 썸네일 → 입력 필드 → 그리드)만 `weight(1f)` + `verticalScroll`로 감싸며, `MinoActionArea`를 마지막에 둔다.
- **Rationale**: plan 2.0.0의 [screen 계약](contracts/profile-screen-contract.md)이 "화면 전체가 세로 스크롤 하나로 흐른다"고 적었으나, 그대로 만들면 원본과 두 곳이 어긋난다 — 콘텐츠가 넘칠 때 상단 바가 밀려 올라가고(원본 y=0 고정), 화면이 원본(812)보다 길 때 액션 영역이 바닥에 붙지 않는다(원본 bottom=0 고정). [figma-design-fidelity.md](../../conventions/figma-design-fidelity.md)가 산문과 노드가 어긋나면 노드를 따르도록 정한다. 계약의 문장은 **그리드가 중첩 스크롤을 만들지 않게 하려는 것**이었고, 이 구조가 그 조건도 함께 만족한다([D26](#d26-아바타-그리드의-배치--화면이-소유하고-lazyverticalgrid를-쓰지-않는다)).
- **하단 간격**: 원본 `Frame 102`가 자식 합계 630 > 높이 612로 **18px 오버플로**한 상태라 렌더 간격 22는 디자이너 의도가 아니다. **사용자가 20dp 유지를 확정했다** — 본문 하단 패딩 0 + 액션 영역 상단 패딩 20이다.
- (plan 3.0.0에서 결정)

## D33. 진입점 값 상수는 `ExtraTag.kt`가 아니라 전환 계약 파일이 갖는다

- **Decision**: `PROFILE_ENTRY_POINT_ONBOARDING`·`PROFILE_ENTRY_POINT_EDIT`를 `activity/launcher/ProfileLauncher.kt`에 둔다. 키 `EXTRA_PROFILE_ENTRY_POINT`는 `ExtraTag.kt`에 남는다.
- **Rationale**: [`core:navigation` README](../../../core/navigation/README.md) §3이 `ExtraTag.kt`를 **"Intent extra 키"** 전용으로 정의하고 §4 표가 `activity/launcher` 패키지를 "feature별 전환 계약 — `XLauncher` 인터페이스와 Intent extra 키"로 정의한다. **제약은 패키지가 아니라 파일 수준**이므로, 값 상수를 같은 패키지의 계약 파일로 옮기면 두 규칙을 다 지키면서 "호출자와 화면이 같은 문자열을 본다"는 원래 목적도 유지된다. Kotlin 최상위 상수의 import는 패키지 기준이라 소비처 수정도 필요 없다.
- **Alternatives considered**: feature 모듈 내부로 옮긴다 — 호출자 feature가 값을 실어야 하는데 대상 feature에 두면 [단일 모듈 전환 계약 ADR](../../adr/2026-08-01-single-module-navigation-contract.md)이 막는 feature 간 순환이 된다. 기각.
- (plan 3.0.0에서 결정)

## D34. `MinoTopNavigation`에 `backEnabled` 파라미터를 두지 않는다

- **Decision**: `backEnabled` 파라미터와 그에 딸린 `DisabledBackIconColor` 토큰·`disabledBackIconColor` Defaults를 만들지 않는다.
- **Rationale**: 이 파라미터의 유일한 근거였던 "온보딩 진입의 뒤로가기 비활성"이 [D29](#d29-온보딩-진입에서-뒤로가기를-노출하지-않는다)로 사라져 **호출부가 하나도 없다.** [component-asset-placement.md](../../conventions/component-asset-placement.md) §3이 "아직 동작하지 않는 기능의 파라미터를 미리 만들지 않는다. 실제 호출부가 생길 때 **디폴트 인자로 소스 호환 추가**한다"로 정하므로 되돌리기 비용도 낮다. 비활성 시각은 컴포넌트 정의 노드(`16215-20537`) 차단으로 **한 번도 대조된 적이 없는 잠정값**이었고, 제거로 이 컴포넌트의 미대조 표면이 0이 됐다.
- (plan 3.0.0에서 결정)

## D35. 아바타 12종의 소유 결정을 ADR로 승격했다

- **Decision**: [D4](#d4-아바타-12종의-소유--coredesign-system)를 [ADR — 프로필 아바타 12종의 에셋과 컴포넌트는 `:core:design-system`이 소유한다](../../adr/2026-08-25-profile-avatar-assets-in-design-system.md)로 승격했다. D4는 이 feature의 근거로 남고, 구속력을 갖는 결정은 ADR이 소유한다.
- **Rationale**: [헌법](../../constitution.md) Governance가 "예외를 ADR로 기록한다. 기록 없는 예외는 없다"로 정하는데, 이 배치는 [component-asset-placement.md](../../conventions/component-asset-placement.md) §1의 "design-system은 이미지 에셋을 받지 않는다"와 문면상 충돌하는 예외다. plan 2.0.0의 Constitution Check가 이미 원칙 III를 "승격 대상 1건"으로 표시해 두었다.
- **D4보다 세워진 근거**: D4는 "소비자가 여럿이고 spec이 지정했다"를 들었으나 그것만으로는 `:core:common:ui`도 만족한다. ADR이 1순위로 세운 것은 **컴포넌트와 에셋의 불가분성**이다 — 모듈 그래프가 `ui --> design` 방향이라 에셋만 `:core:common:ui`로 보내면 `:core:design-system`이 자기가 그릴 그림의 `R`을 참조하지 못해 **컴파일되지 않는다.**
- (plan 3.0.0에서 결정)

---

## D36. 원격 연동 착수 — 원천은 서버, 로컬 DataStore는 캐시

- **Decision**: 프로필의 원천을 서버로 옮긴다. `:core:data`에 `ProfileApiService`·`ProfileRemoteDataSource`를 두고 배포된 OpenAPI 문서의 유저 엔드포인트 3종을 소비한다. 로컬 Preferences DataStore는 **원격 응답의 캐시**로 역할이 바뀌어, 앱 재시작 후 프리필과 `observeProfile()` 방출을 담당한다. 저장은 **원격 성공 → 캐시 갱신** 순서이며, 원격이 실패하면 캐시를 건드리지 않는다.
- **Rationale**: [D22](#d22-이번-범위의-저장소--로컬-datastore-단독-원격-연동은-후속-작업--재검토됨plan-400)가 스스로 적어 둔 닫히는 조건("원격 연동 작업이 착수되는 것")이 충족됐다. spec §4의 가정("프로필 저장은 서버 반영을 포함한다")과 FR-008의 개인방 생성([D17](#d17-개인방내-장소-생성--서버가-등록과-함께-처리한다확정))이 이로써 실제로 성립하고, plan 2.0.0·3.0.0이 Constitution Check 원칙 IV에 미충족으로 들고 있던 2건이 닫힌다. 캐시를 남기는 이유는 SC-003("앱을 다시 켜지 않고도" 모든 표기 지점 갱신)을 Flow 하나로 만족시키는 [D9](#d9-앱-전체-즉시-반영--observeprofile-flowprofile)의 구조를 유지하기 위해서다.
- **캐시 갱신 순서가 규칙인 이유**: "성공 시에만 캐시를 갱신한다"는 FR-012·SC-006이 사용자에게 보이는 동작이다. 저장이 실패했는데 캐시가 바뀌면 화면을 다시 열었을 때 저장되지 않은 값이 프리필된다. 이 순서는 화면이 아니라 데이터 레이어의 불변식이며 단위 테스트가 지킨다([D43](#d43-테스트-범위--mockengine-기반-데이터-레이어-테스트를-더한다)).
- **오프라인**: 다루지 않는다(spec §4). 저장 시점에 네트워크가 없으면 `MinoDomainException.Network`가 FR-012의 저장 실패 경로로 나가고, 큐잉·나중 동기화는 두지 않는다.
- **기존 저장값 마이그레이션**: 두지 않는다. 앱이 배포된 적이 없어 기기에 남은 `profile_avatar_id`(Int) 값을 지킬 이유가 없다. 캐시는 첫 `refreshProfile()`이 서버 값으로 다시 채운다.
- **Alternatives considered**:
  - **캐시 없이 매번 `GET /users/me`** — 프로필을 표기하는 모든 화면이 각자 네트워크를 때리고, 지연 중에는 닉네임이 비어 보인다. 표기 지점이 여럿이라(SC-003) 비용이 크다. 기각.
  - **로컬 우선 저장 후 백그라운드 동기화** — spec §4가 "오프라인 저장·나중에 동기화는 다루지 않는다"로 명시 제외했다. 기각.
- **ADR 승격 후보**: "원격이 원천, 로컬은 캐시"는 다른 feature도 따를 패턴이다. 다만 이 저장소에서 원격을 실제로 소비하는 첫 사례이므로 사례가 하나 더 쌓인 뒤 승격을 판단한다.
- (plan 4.0.0에서 결정)

## D37. 아바타 식별자 — 도메인 `ProfileAvatar` enum, 서버 표현은 `avatar.color` 문자열

- **Decision**: `:core:domain`에 12항목 `ProfileAvatar` enum을 신설하고 `Profile`이 그것을 든다. `ProfileAvatar` ↔ 서버 문자열의 대응표는 `:core:data`의 `ProfileMapper` 한 곳이 소유하며, `ProfileAvatar` ↔ `MinoProfileAvatar`(그림)의 대응은 지금처럼 `:feature:profile`이 소유한다. [D18](#d18-아바타-식별자--서버-계약을-따라-int--재검토됨plan-400)의 `Int`는 근거가 사라져 폐기한다.
- **Rationale**: D18이 `Int`를 고른 유일한 근거는 서버 스키마가 `Avatar { id: integer }`라는 것이었는데, 배포된 OpenAPI 문서에는 그 스키마가 없다 — 실제 계약은 `avatar: { color: string(1..20) }`이다. 근거가 사라진 타입을 관성으로 남기면 `Int`가 어디서 왔는지 아무도 설명할 수 없게 된다. 대신 **같은 모양의 문제를 이 저장소가 이미 푼 방식**을 따른다: [`RoomColor`](../../../core/domain/src/main/kotlin/team/mino/core/domain/model/RoomColor.kt)가 도메인 enum이고, 서버 문자열 표는 `RoomMapper`가 들고, 팔레트는 `:core:design-system`이 갖고, 대응은 feature가 갖는다. 프로필 아바타는 그 구조와 정확히 동형이다.
- **도메인이 아바타를 아는 것이 정당한 근거**: spec §2.3이 아바타를 도메인 개념으로 정의한다 — "앱이 제공하는 고정 12종 캐릭터 이미지 중 하나. 프로필은 이 목록 중 하나를 가리킨다." 목록이 고정이고 서버에서 내려받지 않는다는 것도 spec §4가 확정했다.
- **`ProfileAvatar`와 `MinoProfileAvatar`가 둘 다 12항목인 것**: 중복이 아니라 소유의 분리다. 도메인 enum은 **무엇인지**(식별)를, 디자인 시스템 enum은 **어떻게 보이는지**(그림)를 안다. [아바타 ADR](../../adr/2026-08-25-profile-avatar-assets-in-design-system.md)이 후자를 `:core:design-system`에 둔 결정은 그대로 유효하고, `:core:design-system`이 도메인을 의존하지 않는다는 경계(헌법 원칙 II)도 지켜진다.
- **plan 4.1.0 보정 — 잠정 문자열이 확정으로 바뀌었다.** 아래 "잠정" 단락은 2026-08-27 조회본을 근거로 쓴 것이고, **2026-08-28 조회본에서 서버가 `avatar.color`를 13개 enum으로 확정**했다. `"person_01"`은 이제 서버가 거절할 값이다. 새 표와 그 근거는 [D44](#d44-아바타-서버-문자열--12종이-방-팔레트-12색에-1대1로-대응한다)가 갖는다. **이 결정이 만든 장치(표를 `ProfileMapper` 한 파일에 가두는 것)가 값어치를 했다** — 서버가 값 도메인을 통째로 바꿨는데 고칠 곳은 그 한 파일이다.
- ~~**서버로 보낼 문자열 — 잠정이다**~~(plan 4.1.0에서 [D44](#d44-아바타-서버-문자열--12종이-방-팔레트-12색에-1대1로-대응한다)로 대체): `avatar.color`에 `"person_01"`~`"person_12"`를 싣는다(`maxLength 20` 안에 들어간다). **필드 이름이 `color`인데 색이 아닌 값을 싣는 어긋남을 알고 하는 선택**이다 — 12종 캐릭터에 대응하는 색 팔레트가 정의된 적이 없고 문서에도 값의 열거가 없다. 서버팀 협의 항목이며([API 계약](contracts/profile-api-contract.md) §2), 확정되면 고칠 곳은 `ProfileMapper`의 표 하나다. 도메인·UI·화면 어디에도 이 문자열이 새어 나가지 않는다.
- **모르는 값을 받았을 때**: 목록에 없는 문자열은 기본 아바타(첫 항목)로 읽는다. 서버가 아바타를 넓혔다는 이유로 프로필 조회가 실패하면 안 된다 — `RoomMapper`가 모르는 색을 `GRAY`로 읽는 것과 같은 판단이다. 응답의 `avatar`가 `null`인 경우(문서상 nullable)도 같게 처리한다.
- **Alternatives considered**:
  - **`Profile.avatarId: String`** — 변경은 가장 작지만 서버 표현이 도메인과 feature까지 샌다. `RoomMapper`의 KDoc이 명시적으로 경계한 형태이며, 서버가 표현을 바꾸면 고칠 곳이 한 곳으로 모이지 않는다. 기각.
  - **`Int` 유지 + 매퍼가 `Int` ↔ 문자열 표 소유** — 구현 변경이 가장 적다. 그러나 `1`~`12`는 서버와도 도메인과도 무관한 임의 값이라 표의 왼쪽 열이 아무것도 설명하지 못하고, 항목이 끼어들면 이미 저장된 값의 의미가 조용히 어긋난다. 기각.
- (plan 4.0.0에서 결정)

## D38. 등록/수정 분기 — 서버에 직접 묻고, 캐시가 그 답을 들고 있는다

- **Decision**: 저장 시 호출할 엔드포인트를 **캐시에 프로필이 있는지**로 고른다 — 없으면 `POST /api/v1/users`(등록), 있으면 `PATCH /api/v1/users/me`(수정). 그리고 그 캐시는 `refreshProfile()`이 **서버에 직접 물어** 채운다: `GET /api/v1/users/me`가 성공하면 응답을 캐시에 쓰고, 미등록(`401` + `errorCode = USER_NOT_REGISTERED`)이면 캐시를 비운다. 진입점(`ProfileEntryPoint`)은 뒤로가기·저장 후 목적지에만 쓰고 API 분기에 쓰지 않는다.
- **Rationale**: [D14](#d14-등록과-수정의-분기--진입점이-아니라-저장된-프로필의-유무로-가른다--재검토됨plan-200)가 "무엇을 호출할지는 '서버에 내 유저가 있는가'의 문제"라고 판단한 것은 옳았지만, 그때는 물어볼 수단이 없어 캐시를 대리 지표로 썼다. 배포 문서가 미등록을 명시적 `errorCode`로 알려 주므로 대리 지표를 쓸 이유가 없어졌다. 캐시는 이제 그 답을 **보관**하는 자리이지 추측하는 자리가 아니다.
- **`401`을 정상 갈래로 읽는 것**: `GET /api/v1/users/me`는 인증 실패와 미등록을 **같은 401**로 낸다. 둘을 가르는 것은 본문의 `errorCode`뿐이므로, `ProfileApiService.getMe()`가 `401` + `USER_NOT_REGISTERED`만 `null`(미등록)로 지역 처리하고 나머지 401(`UNAUTHORIZED`·`TOKEN_EXPIRED`)은 그대로 전파한다. [`core:data` README](../../../core/data/README.md) §4가 허용한 "엔드포인트별 특수 정책이 필요한 지점만 해당 지역에서 catch를 병용한다"에 해당한다. 상태 코드가 `404`였다면 지역 catch가 필요 없었을 것이므로 **서버팀 협의 항목으로 세운다**([API 계약](contracts/profile-api-contract.md) §2).
- **재등록(409)**: 캐시가 비어 있는데 서버에 이미 유저가 있으면 `409 USER_ALREADY_REGISTERED`가 온다. 이것을 **저장 실패로 다룬다**(FR-012 경로). 자동으로 `PATCH`로 갈아타는 폴백은 두지 않는다 — 그 상황은 캐시 쓰기가 실패했거나 서버 상태가 앞서 나간 경우이고, 사용자가 화면을 다시 열면 `refreshProfile()`이 캐시를 복구해 다음 저장이 `PATCH`로 간다. 이 복구 경로가 있는 것이 [D14](#d14-등록과-수정의-분기--진입점이-아니라-저장된-프로필의-유무로-가른다--재검토됨plan-200) 시절과 달라진 점이다.
- **`saveProfile` 하나로 두는 것**: 분기가 생겼다고 도메인 표면을 `registerProfile`·`updateProfile`로 나누지 않는다. 화면이 아는 것은 "저장한다" 하나이고([화면 계약](contracts/profile-screen-contract.md)), 어느 엔드포인트로 나가는지는 데이터 레이어의 사정이다. 도메인을 갈라 두면 호출자가 등록 여부를 알아야 하는데, 그 판단의 원천이 데이터 레이어에 있으므로 순환이 된다.
- (plan 4.0.0에서 결정)

## D39. Repository 표면 — `observeProfile()` + `refreshProfile()` + `saveProfile()` 세 멤버

- **Decision**: [D23](#d23-repository-표면--observeprofile--saveprofile-두-멤버--재검토됨plan-400)의 두 멤버에 `suspend fun refreshProfile()`을 더한다. 저장은 `saveProfile(profile)` 하나를 유지한다([D38](#d38-등록수정-분기--서버에-직접-묻고-캐시가-그-답을-들고-있는다)).
- **Rationale**: D23이 `refreshProfile`을 거둔 이유는 "원천이 로컬일 때 새로 받아올 곳이 없어 의미가 없다"였고, 원천이 서버가 된 지금 그 이유가 사라졌다. 부를 곳도 둘이다 — ① 화면 진입 시 프리필의 원천을 서버로 맞춘다(FR-006) ② 등록 여부를 서버에 물어 캐시를 채운다([D38](#d38-등록수정-분기--서버에-직접-묻고-캐시가-그-답을-들고-있는다)). 두 번째가 없으면 `409` 막다른 길에서 빠져나올 수단이 없다.
- **`refreshProfile()`이 값을 돌려주지 않는 것**: 반환은 `Unit`이고 결과는 `observeProfile()`로 흘러나온다. 반환하면 호출자가 Flow와 반환값 중 어느 쪽을 원천으로 볼지 갈린다([D23](#d23-repository-표면--observeprofile--saveprofile-두-멤버--재검토됨plan-400)의 `saveProfile`과 같은 이유).
- **실패**: 네트워크·HTTP 실패는 `MinoDomainException`으로 던진다. 온보딩 진입의 미등록은 **실패가 아니다** — `null` 캐시로 정상 종료한다([D38](#d38-등록수정-분기--서버에-직접-묻고-캐시가-그-답을-들고-있는다)). 그렇지 않으면 프로필을 처음 만드는 사용자가 화면에 들어서자마자 오류 스낵바를 본다.
- (plan 4.0.0에서 결정)

## D40. 응답 봉투와 에러 코드 — 공용 DTO를 신설한다

> **plan 4.3.0 보정 — 봉투는 신설이 아니라 재사용이다.** 이 결정이 만들려던 `ApiEnvelope<T>`는 **이미 `MinoResponse<T>`로 존재한다**(`core/data/network/dto/response/MinoResponse.kt`, `internal @Serializable data class MinoResponse<T>(val data: T)`). [shared-link-receiver](../shared-link-receiver/plan.md)가 먼저 서버를 붙이면서 만들었고, [전용 ADR](../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)이 **Accepted** 상태로 지배한다. 그 ADR은 `ApiEnvelope<T>`라는 이름까지 검토한 뒤 `MinoResponse`로 확정했으므로, 프로필이 다른 이름을 새로 만들면 같은 일을 하는 타입이 둘이 된다. **`ErrorResponse`는 여전히 신설이다** — 같은 ADR이 "에러 본문은 이 봉투가 아니며 이 타입이 다루지 않는다"로 범위 밖에 두었다. 자세한 것은 [D47](#d47-develop-재대조--420의-대조가-옛-트리를-근거로-했다)에 있다.

- **Decision**: `:core:data`의 `network/dto/response/`에 `@Serializable data class ApiEnvelope<T>(val data: T)`와 `@Serializable data class ErrorResponse(val errorCode: String, val message: String? = null)`를 둔다. 봉투를 벗기는 것은 `ApiService`의 책임이며, `DataSource` 위쪽으로는 알맹이 DTO만 나간다.
- **Rationale**: 배포 문서의 모든 성공 응답이 `{ "data": ... }`로, 모든 실패 응답이 `{ "errorCode", "message" }`로 통일돼 있다. 프로필이 실서버를 소비하는 첫 사례이므로 이 두 타입을 프로필 전용으로 만들면 다음 feature가 같은 것을 다시 만든다. [group-room-form의 API 계약](../group-room-form/contracts/room-api-mock.md) §1이 "서버 연결 시 봉투 처리는 `ApiService`가 담당한다"로 미리 지목해 둔 자리이기도 하다.
- **`ErrorResponse`가 필요한 이유는 한 곳뿐이다**: [D38](#d38-등록수정-분기--서버에-직접-묻고-캐시가-그-답을-들고-있는다)의 `USER_NOT_REGISTERED` 판정이다. 그 밖의 실패는 `convertDomainException`이 상태 코드만으로 `MinoDomainException`을 만들므로 본문을 읽지 않는다. **`errorCode`를 도메인 예외로 승격하지 않는다** — `MinoDomainException`에 새 리프를 만들지 않는다는 [D30](#d30-로컬-저장-실패용-도메인-예외-리프를-추가하지-않는다)의 판단이 그대로 걸린다.
- ~~**ADR 승격 대상**: 두 타입은 이 feature가 아니라 **모든 서버 소비 feature를 구속한다.** 봉투를 어디서 벗기는지, 에러 코드를 도메인까지 올릴지 말지는 다음 feature가 다시 정할 문제가 아니다. 완료 보고에서 ADR 승격을 제안한다.~~
- **ADR 승격(plan 4.3.0 보정)**: **봉투 쪽은 이미 승격돼 있다** — [ADR 2026-08-27](../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)이 "어디서 벗기는지"를 확정했고 프로필은 그것을 따르기만 한다. 남는 승격 후보는 **에러 본문 쪽 하나**다 — `ErrorResponse`의 도입과 "`errorCode`를 도메인 예외로 올리지 않는다"는 판단이며, 그것은 아직 어느 ADR도 다루지 않는다.
- (plan 4.0.0에서 결정)

## D41. 목 엔진을 만들지 않는다

- **Decision**: [D15](#d15-목mock-구현--flavor-소스셋으로-가른-ktor-mockengine--재검토됨plan-200)·[D16](#d16-목-응답의-성질--프로세스-내-상태-유지--지연실패-주입--재검토됨plan-200)을 되살리지 않는다. flavor 소스셋(`src/qa/`·`src/prod/`)도, `qaImplementation(libs.ktor.client.mock)`도, `NetworkModule`의 엔진 분리도 하지 않는다. `NetworkModule`은 그대로 둔다.
- **Rationale**: D15의 종료 조건이 이미 충족돼 있다 — `Flavor.apiBaseUrl`이 qa·prod 모두 `https://api.gguk.org/`이고 서버가 응답한다. 목의 목적은 "서버 없이 화면을 끝까지 돌려보는 것"이었으므로 서버가 있으면 만들 이유가 없고, 만들면 검증되지 않은 두 번째 경로가 남는다.
- **경계**: 방(`RoomMockRemoteDataSourceImpl`)의 목은 이 결정이 건드리지 않는다. 그쪽은 [group-room-form](../group-room-form/contracts/room-api-mock.md)의 소관이며, 프로필이 실서버를 붙였다는 사실이 그 계획을 대신 개정하지 않는다.
- **테스트에서의 `MockEngine`은 다른 것이다**: [D43](#d43-테스트-범위--mockengine-기반-데이터-레이어-테스트를-더한다)이 쓰는 `MockEngine`은 `testImplementation`으로 이미 있는 것이고, 프로덕션 산출물에 들어가지 않는다.
- (plan 4.0.0에서 결정)

## D42. 로컬 캐시 DataSource는 `ProfileEntry` DTO를 반환한다

- **Decision**: `ProfileLocalDataSource`의 반환 타입을 도메인 `Profile`에서 `:core:data` 내부 DTO `ProfileEntry`로 바꾸고, `ProfileEntry` ↔ `Profile` 변환을 `ProfileMapper`가 맡는다. `ProfileRepositoryImpl`이 변환의 경계다.
- **Rationale**: plan 3.0.0의 Constitution Check 원칙 V가 **FAIL로 남아 있었다** — [`core:data` README](../../../core/data/README.md) §5("DataSource는 DTO만 반환, 변환 없음")·§2("변환은 `RepositoryImpl` 안에서 끝난다")와 [repository 계약](contracts/profile-repository-contract.md)이 정면 충돌했고, 당시엔 어느 쪽으로도 규약을 다 지킬 수 없었다. 충돌의 원인은 "원격이 없어 DTO가 없다"였는데 **그 전제가 이번 개정에서 사라졌다.** 원격 DTO와 매퍼가 어차피 생기므로 `ProfileEntry`를 두어도 층이 늘지 않고, README의 두 규칙을 모두 지킬 수 있다. 사용자가 이 해소안을 골랐다.
- **키가 새지 않는 것**: `profile_nickname`·`profile_avatar` 키 상수는 여전히 `ProfileLocalDataSourceImpl` 안에 남는다(README §5 "키 상수는 해당 DataSource 구현체 안에 둔다"). DataSource는 `Preferences` → `ProfileEntry`까지만 조립하고 도메인 타입을 모른다.
- **아바타 키가 바뀐다**: `profile_avatar_id`(Int) → `profile_avatar`(String, `ProfileAvatar`의 이름). [D37](#d37-아바타-식별자--도메인-profileavatar-enum-서버-표현은-avatarcolor-문자열)의 타입 변경을 캐시가 따라간다. **저장 문자열은 서버 표현이 아니라 enum 이름이다** — 캐시가 서버 문자열을 들면 서버 표현이 바뀔 때 고칠 곳이 매퍼 밖으로 하나 더 생긴다. 마이그레이션은 두지 않는다([D36](#d36-원격-연동-착수--원천은-서버-로컬-datastore는-캐시)).
- **Alternatives considered**: (a) 그대로 두고 미결로 남긴다 — 원칙 V FAIL이 유지되고, 원격 매퍼가 생긴 뒤에도 안 고치면 다음 개정에서 더 굳는다. 기각. (b) README에 "DTO 없는 로컬 DataSource" 갈래를 보완한다 — 규약 문서 개정은 이 스킬의 범위 밖이고, 예외를 열기 전에 예외 없이 되는지부터 봐야 한다. 기각.
- (plan 4.0.0에서 결정)

## D43. 테스트 범위 — `MockEngine` 기반 데이터 레이어 테스트를 더한다

- **Decision**: [D12](#d12-테스트-범위--jvm-단위-테스트만)의 JVM 단위 테스트 범위에 원격 경로를 더한다. `ktor-client-mock`은 이미 `:core:data`의 테스트 의존이므로 기존 [`DomainExceptionMappingTest`](../../../core/data/src/test/java/team/mino/core/data/network/DomainExceptionMappingTest.kt) 방식으로 ① 봉투(`{data}`) 해제 ② DTO ↔ 도메인 매핑(아바타 문자열 표 포함) ③ `401 USER_NOT_REGISTERED`의 `null` 처리와 다른 401의 전파 ④ 등록/수정 분기 ⑤ 캐시 갱신 순서(실패 시 캐시 불변)를 덮는다. Compose UI 테스트는 여전히 도입하지 않는다.
- **Rationale**: 위 다섯은 모두 화면이 아니라 데이터 레이어의 불변식이고, 그중 ③·⑤는 사용자에게 보이는 규칙(FR-006·FR-012·SC-006)이다. `MockEngine`은 HTTP 홉만 없애고 직렬화·봉투 해제·에러 매핑·변환 경로를 전부 실제로 돌리므로 이 다섯을 한 자리에서 지킬 수 있다.
- **`MinoIdentityProofPlugin`은 이 테스트가 덮지 않는다**: 첨부 계약은 [`IdentityProofAttachmentTest`](../../../core/data/src/test/java/team/mino/core/data/network/IdentityProofAttachmentTest.kt)가 이미 소유한다. 프로필 테스트가 같은 것을 다시 검사하지 않는다.
- (plan 4.0.0에서 결정)

## D44. 아바타 서버 문자열 — 12종이 방 팔레트 12색에 1대1로 대응한다

- **T086 디자인 확인 완료(2026-08-28) — `Person10` → `brown`이 맞았다.** 이 결정이 남긴 유일한 추정이 닫혔다. 11종은 배경 원 색이 토큰과 hex 단위로 일치해 확정적이었고 `Person10`만 대응 토큰이 없어 소거법으로 배정했는데, 디자인이 그 배정을 확인해 줬다. **이제 12종 전부가 근거를 갖는다** — `ProfileMapper`의 표를 고칠 이유가 없다.

- **Decision**: `ProfileMapper`가 소유하는 `ProfileAvatar` ↔ 서버 문자열 표를 아래로 확정한다. [D37](#d37-아바타-식별자--도메인-profileavatar-enum-서버-표현은-avatarcolor-문자열)이 잠정으로 쓰던 `"person_01"`~`"person_12"`를 폐기한다.

  | 아바타 | `avatar.color` | 배경 원 색 | 디자인 시스템 토큰 |
  |---|---|---|---|
  | `Person1` | `red` | `#FED5D5` | `Red90` |
  | `Person2` | `red_orange` | `#FED9C4` | `RedOrange90` |
  | `Person3` | `orange` | `#FEE6C6` | `Orange90` |
  | `Person4` | `green` | `#D9FFE6` | `Green95` |
  | `Person5` | `purple` | `#F2D6FF` | `Purple90` |
  | `Person6` | `lime` | `#E6FFD4` | `Lime95` |
  | `Person7` | `cyan` | `#B5F4FF` | `Cyan90` |
  | `Person8` | `pink` | `#FED3F7` | `Pink90` |
  | `Person9` | `blue` | `#C9DEFE` | `Blue90` |
  | `Person10` | `brown` | `#FBE9DA` | **없음 — 소거법** |
  | `Person11` | `light_blue` | `#C4ECFE` | `LightBlue90` |
  | `Person12` | `violet` | `#DBD3FE` | `Violet90` |

- **Rationale**: 2026-08-28 조회본에서 서버가 `avatar.color`를 **13개 enum**으로 확정했다(`red`·`red_orange`·`orange`·`lime`·`green`·`cyan`·`violet`·`pink`·`blue`·`brown`·`light_blue`·`purple`·`gray`). 그 목록은 [`RoomColor`](../../../core/domain/src/main/kotlin/team/mino/core/domain/model/RoomColor.kt)와 정확히 같다. 값 도메인이 닫히면서 임의 문자열을 고를 여지가 사라졌고, **남은 문제는 12종 아바타를 12색 중 무엇에 붙일지 하나**가 됐다.
- **근거는 추정이 아니라 에셋 실측이다**: 아바타 12종의 배경 원 색을 밀도 `xxhdpi` 원본에서 샘플링해 디자인 시스템의 아토믹 색 토큰과 대조했다. **11개가 서로 다른 색 계열의 토큰과 정확히(hex 일치) 맞았고**, 12개가 선택 가능한 12색을 중복 없이 빠짐없이 덮었다. 아바타가 색 정체성을 이미 갖고 있었다는 뜻이며, `MinoProfileAvatar`의 KDoc이 적어 둔 "그림에는 배경 원과 캐릭터가 함께 굽혀 있다"와도 맞는다.
- **`gray`를 쓰지 않는다**: 서버 enum 13개 중 `gray`만 남는데, 방에서 `gray`는 "색을 고르지 않은 방이 갖는 색"이다([`RoomColor`](../../../core/domain/src/main/kotlin/team/mino/core/domain/model/RoomColor.kt)의 `selectable`이 `GRAY`를 뺀다). 프로필 아바타에는 "고르지 않음"이 저장되지 않으므로(EC-002가 기본 아바타로 채운다) 보낼 일이 없다. 받는 쪽은 모르는 값과 같게 기본 아바타로 읽는다.
- **`Person10` → `brown`은 소거법이다**: 배경이 `#FBE9DA`인데 **디자인 시스템에 `brown` 계열 토큰이 없다.** 나머지 11색이 모두 배정된 뒤 남는 색이 `brown` 하나뿐이라 그것으로 둔다. 다른 11개와 달리 hex 일치 근거가 없으므로 **디자인 확인 항목으로 남긴다**([API 계약 §2](contracts/profile-api-contract.md)).
- **표를 선언 순서에서 파생하지 않는다**: 위 대응은 `RoomColor`의 선언 순서(`RED`·`RED_ORANGE`·`ORANGE`·`LIME`·`GREEN`·…)와 **일치하지 않는다** — 아바타 4·5·6이 `green`·`purple`·`lime`이라 순서가 어긋난다. `ordinal`로 이으면 조용히 틀린 값이 나가므로 표를 명시적으로 적는다. [D37](#d37-아바타-식별자--도메인-profileavatar-enum-서버-표현은-avatarcolor-문자열)이 "표를 enum 이름에서 파생하지 않는다"고 정해 둔 이유가 여기서 실증됐다.
- **Alternatives considered**:
  - **`"person_01"`~`"person_12"` 유지**([D37](#d37-아바타-식별자--도메인-profileavatar-enum-서버-표현은-avatarcolor-문자열)의 잠정) — 서버 enum에 없는 값이라 요청이 거절된다. 선택지가 아니다.
  - **`ordinal`로 `RoomColor.selectable`에 잇는다** — 코드가 짧지만 위에서 보였듯 실제 대응과 어긋난다. 기각.
  - **디자인·서버 답을 기다리고 `[TBD]`로 둔다** — 보낼 값이 없으면 저장 경로 전체가 돌지 않아 T067·T068·T082가 착수 불가가 된다. 실측 근거가 11/12에서 확정적이므로 채택하고 확인 항목으로 남기는 편이 낫다. 사용자가 이 갈래를 골랐다.
- **이 결정이 흔들리면 고칠 곳**: `ProfileMapper`의 표 하나다. 도메인·화면·디자인 시스템·캐시 어디에도 이 문자열이 새어 나가지 않는다([D42](#d42-로컬-캐시-datasource는-profileentry-dto를-반환한다)가 캐시에 enum 이름을 저장하게 한 이유이기도 하다).
- (plan 4.1.0에서 결정)

## D45. 프리필과 갱신의 순서 — 캐시로 먼저 채우고, 갱신이 성공하면 조건부로 한 번 더

- **Decision**: `ProfileViewModel`의 프리필을 두 번 돈다. ① 진입 즉시 `observeProfile().first()`로 **캐시** 값을 채운다(현행 그대로). ② `refreshProfile()`이 성공하면 **`isNicknameTouched == false && !isSaving`일 때만** 갱신된 캐시 값으로 한 번 더 채운다. 두 조건 중 하나라도 거짓이면 화면을 건드리지 않는다.
- **Rationale**: [D39](#d39-repository-표면--observeprofile--refreshprofile--saveprofile-세-멤버)가 `refreshProfile()`의 존재 이유로 든 두 가지 중 **①(프리필의 원천을 서버로 맞춘다)이 현재 코드 형태로는 성립하지 않는다.** 이미 develop에 들어간 `prefill()`은 `observeProfile().first()` 한 번이라 흐름을 계속 듣지 않는다 — 갱신이 캐시를 고쳐도 화면은 그대로다. [data-model.md](data-model.md)가 "그 흐름을 통해 다시 반영된다"고 적은 것은 **틀린 서술**이었고, 이번 개정이 그것을 코드 쪽에서 바로잡는다.
- **`.first()`를 계속 구독으로 바꾸지 않는 이유**: 구현이 `.first()`를 고른 것은 실수가 아니다. `ProfileViewModel.prefill()`의 KDoc이 그 근거를 적어 두었다 — **저장 직후 흘러나온 값이 사용자가 그 사이에 입력한 것을 덮어쓴다.** 계속 구독으로 바꾸면 그 문제가 되살아난다. 그래서 흐름을 계속 듣는 대신 **갱신이 성공한 그 시점에 한 번만** 다시 읽는다.
- **가드가 두 개인 이유**: `isNicknameTouched`는 사용자가 이미 타이핑을 시작한 경우를, `isSaving`은 갱신 응답이 저장 왕복 중에 도착한 경우를 막는다. 둘 다 사용자의 의도가 서버 값보다 우선하는 상황이다.
- **로딩 상태를 두지 않는다**: 갱신을 기다렸다가 프리필하면 네트워크 왕복 동안 화면이 빈 채로 선다. spec에 진입 로딩 표현이 없어 그 상태를 설명할 수단이 없으므로([data-model.md §5](data-model.md)), 캐시를 먼저 보여 주는 순서를 유지한다.
- **실질 노출은 좁다**: 단일 기기 전제에서 캐시와 서버는 거의 언제나 같다 — 앱 데이터를 지우면 익명 세션도 함께 사라져 서버에서도 미등록이 되기 때문이다. 이 결정이 실제로 값어치를 내는 곳은 **캐시 쓰기가 실패했거나 서버 상태가 앞서 나간 복구 경로** 하나이며, 그것은 [D38](#d38-등록수정-분기--서버에-직접-묻고-캐시가-그-답을-들고-있는다)이 `409`의 탈출구로 지목한 바로 그 경로다. 그 경로에서 화면이 비어 보이지 않게 하는 것이 이 결정의 값이다.
- **Alternatives considered**:
  - **현행 유지하고 [data-model.md](data-model.md)의 문구만 정정** — 코드를 건드리지 않는다. 그러나 `refreshProfile()`의 존재 이유가 "등록 여부 확정" 하나로 줄고, 복구 경로에서 프로필이 있는데 화면이 비는 상태가 남는다. 기각.
  - **`refreshProfile()`을 먼저 기다린 뒤 프리필 한 번** — 가장 단순하고 항상 서버 값이 보이지만, 네트워크 왕복 동안 빈 화면이 서고 spec에 그 상태를 설명할 표현이 없다. 기각.
  - **`.first()`를 지속 구독으로 교체** — 구현이 KDoc으로 근거를 남기며 기각해 둔 선택지다. 저장 직후 값이 사용자 입력을 덮어쓴다. 기각.
- (plan 4.2.0에서 결정)

## D46. develop 대조로 드러난 사실 — 도메인 테스트 fake는 별도 파일이 아니다

- **Decision**: `:core:domain`의 `FakeProfileRepository`는 **`SaveProfileUseCaseTest.kt` 안의 `private class`**이며, 앞으로도 그대로 둔다. plan 4.1.0까지의 문서가 가리킨 `core/domain/src/test/kotlin/team/mino/core/domain/repository/FakeProfileRepository.kt`는 **존재하지 않는다.**
- **Rationale**: develop에 반영된 코드를 대조해 확인한 사실이다. 쓰는 곳이 `SaveProfileUseCaseTest` 하나뿐이라 파일을 나눌 이유가 없고, 그 fake의 KDoc이 "왜 실패 타입을 평범한 `Throwable`로 두는가"를 테스트 옆에서 설명하고 있어 붙여 두는 편이 읽기 좋다.
- **파일을 나눈다면 경로가 다르다**: 같은 모듈에 이미 [`core/domain/src/test/kotlin/team/mino/core/domain/fake/FakeRoomRepository.kt`](../../../core/domain/src/test/kotlin/team/mino/core/domain/fake/FakeRoomRepository.kt) 선례가 있다. 나눌 일이 생기면 `repository/`가 아니라 **`fake/`** 로 간다.
- **문서에 남기는 이유**: 이 착오는 `tasks.md`의 T009(완료 표시)와 T063(미착수)이 **없는 경로를 가리키게** 만들었다. 착수자가 파일을 찾다 새로 만들면 fake가 둘이 된다. 경로는 [plan.md §프로젝트 구조](plan.md)가 정정했다.
- (plan 4.2.0에서 확인)

## D47. develop 재대조 — 4.2.0의 대조가 옛 트리를 근거로 했다

- **Decision**: [D46](#d46-develop-대조로-드러난-사실--도메인-테스트-fake는-별도-파일이-아니다)이 수행한 develop 대조 중 `:core:data` 쪽 판정을 **폐기하고 아래로 대체한다.** 4.2.0을 쓰는 동안 워크트리가 새 커밋(`e00563e`까지)으로 움직였는데, `:core:data` 트리는 그 이전에 읽은 것을 근거로 삼았다. 프로필 파일 자체의 판정(8개 프로덕션 + 5개 테스트, [D45](#d45-프리필과-갱신의-순서--캐시로-먼저-채우고-갱신이-성공하면-조건부로-한-번-더)의 `.first()` 발견)은 새 트리에서 재확인했으므로 **그대로 유효하다.**
- **바로잡는 사실 넷**:

  | 4.2.0이 적은 것 | 실제(2026-08-28 재대조) |
  |---|---|
  | `ApiEnvelope<T>`를 신설한다 | **`MinoResponse<T>`가 이미 있다.** [ADR 2026-08-27](../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)이 Accepted로 지배하며 이름 후보로 `ApiEnvelope<T>`를 검토한 뒤 기각했다 |
  | `ApiEnvelope`·`ErrorResponse`를 ADR로 승격 제안 | **봉투 쪽은 이미 승격 완료.** 남는 후보는 `ErrorResponse`와 에러 코드 취급 하나 |
  | 방은 여전히 mock | **절반만 맞다.** `RoomRepositoryImpl`이 이제 원천을 둘 쓴다 — `getRooms()`는 **실서버**(`RoomApiService.listRooms()`), `getRoom`·`createRoom`·`updateRoom`만 mock |
  | 서버가 만든 개인방이 앱에 보이지 않는다 | **더 이상 사실이 아니다.** 방 목록이 실서버이고 `:feature:sharereceiver`의 방 선택 시트가 그것을 소비하며 `RoomType.PERSONAL`을 안다 |

- **새로 드러난 관례 긴장 — `errorCode`에 분기하지 않는다**: `PinApiService`와 `SharedPlaceSaveWorker`가 나란히 "**실패 판정은 `errorCode`가 아니라 HTTP 상태 코드만 본다**"를 KDoc에 명시했다([shared-link-receiver 계약](../shared-link-receiver/contracts/shared-place-save-api.md) §1.2가 근거). [D38](#d38-등록수정-분기--서버에-직접-묻고-캐시가-그-답을-들고-있는다)의 `getMe()`는 **저장소에서 유일하게 `errorCode`를 읽는 지점**이 된다.
  - 그럼에도 D38을 뒤집지 않는다. 저쪽이 상태 코드만 보는 것은 **가를 필요가 없기 때문**이고(실패는 전부 같은 실패다), 프로필은 `401` 하나가 "인증 실패"와 "미등록"이라는 **성격이 전혀 다른 두 상태**를 겸하고 있어 가르지 않으면 온보딩이 성립하지 않는다. 관례를 어기는 것이 아니라 관례가 다루지 않는 경우다.
  - 다만 이 예외는 **서버가 미등록을 `404`로 바꾸면 사라진다.** [API 계약 §2](contracts/profile-api-contract.md)의 협의 항목 ⑤에 그 사실을 적어 두었고, 이 관례 선례 둘이 그 요청의 근거를 강화한다.
- **왜 기록하는가**: [D24](#d24-원격-연동이-붙을-때-바뀌는-지점을-지금-고정한다)의 예측 표가 두 줄 틀렸던 것, [D45](#d45-프리필과-갱신의-순서--캐시로-먼저-채우고-갱신이-성공하면-조건부로-한-번-더)에서 문서가 코드보다 앞서갔던 것에 이어 **세 번째로 같은 성격의 일**이 벌어졌다. 이번 원인은 앞의 둘과 다르다 — 문서가 틀린 게 아니라 **읽은 시점이 낡았다.** 여러 feature가 같은 모듈(`:core:data`)을 동시에 넓히는 국면에서는 대조 자체에 유효기간이 있다. 착수 직전 재대조를 [quickstart.md §2](quickstart.md)가 이미 요구하고 있으며, 그 대상에 **API 문서뿐 아니라 `:core:data` 트리도 포함**된다.
- (plan 4.3.0에서 확인)

## D48. 분석이 드러낸 문서의 빈틈 — 계약이 요구한 검증이 작업이 되지 못했다

- **Decision**: [repository 계약 §테스트 계약](contracts/profile-repository-contract.md)이 `ValidateNicknameUseCase`의 검증 대상에 더한 **`중간 공백`** 케이스를 [plan.md §규모/범위](plan.md)의 바뀌는 테스트 파일 표에 올린다. 표가 다섯 줄에서 여섯 줄이 되고, `tasks.md`가 그 한 건을 작업으로 만들 수 있게 된다.
- **Rationale**: `/mino-analyze`의 교차 대조가 찾았다. **요구사항이 빠진 것이 아니라 그것을 작업으로 옮기는 통로가 빠져 있었다** — spec 근거는 FR-002와 §4 가정("숫자·특수문자·이모지·공백은 유효하지 않은 문자")에 처음부터 있었고, 계약도 검증 대상으로 들었는데, plan의 파일 표가 그 파일을 들지 않아 `/mino-task`가 근거를 지목할 수 없었다. 그 결과 tasks.md는 이것을 **작업이 아니라 미결 9번**으로 남길 수밖에 없었다.
- **동작 결함이 아니다**: 구현([`ValidateNicknameUseCase`](../../../core/domain/src/main/kotlin/team/mino/core/domain/usecase/ValidateNicknameUseCase.kt))은 `Regex("[가-힣a-zA-Z]{2,}")`로 이미 공백을 배제하고 있고 KDoc도 "중간 공백은 무효"를 명시한다. 비어 있는 것은 **그 사실을 지키는 테스트**뿐이다. 그래서 이 항목은 버그 수정이 아니라 회귀 방지다.
- **왜 표 밖을 손대지 않았는가**: `tasks.md`가 작업을 만들지 않고 미결로 남긴 것은 **옳은 판단이었다.** Phase 7 머리말이 "plan의 표 밖을 고치면 설계에서 벗어난 신호"라는 게이트를 걸어 두었으므로, 작업 목록이 스스로 그 게이트를 넘으면 게이트가 무의미해진다. **표를 고칠 권한은 plan에 있고, 이 개정이 그 일을 한다.**
- **함께 메운 빈틈 둘**: ① §규모/범위의 `DTO 4`가 4.0.0의 `ApiEnvelope` 셈을 그대로 들고 있었다([D47](#d47-develop-재대조--420의-대조가-옛-트리를-근거로-했다)이 그 타입을 걷어냈으므로 3이 맞다). ② §프로젝트 구조의 `core/data` 테스트 트리가 `ProfileApiServiceTest.kt`·`ProfileMapperTest.kt`를 담지 않아, [D43](#d43-테스트-범위--mockengine-기반-데이터-레이어-테스트를-더한다)이 근거를 든 신규 테스트가 트리에서는 근거 없는 파일로 보였다. 셋 다 **근거는 있는데 그것을 실어 나르는 표·트리가 따라오지 못한** 같은 성격이다.
- **Alternatives considered**: `중간 공백`을 계약에서 도로 걷어내 어긋남 자체를 없애는 안. 취하지 않는다 — 계약이 그 케이스를 든 것은 spec §4 가정에 근거가 있어서이고, 검증을 줄이는 방향으로 문서를 맞추면 spec이 확정한 규칙이 테스트 없이 남는다.
- (plan 4.4.0에서 확인)

## D49. develop 통합 재대조 — `user` 태그 엔드포인트의 소유자는 `UserApiService` 하나다

- **Decision**: `ProfileApiService`·`ProfileRemoteDataSource`(+`Impl`)를 **두지 않는다.** 세 유저 엔드포인트(`POST /api/v1/users` · `GET /api/v1/users/me` · `PATCH /api/v1/users/me`)는 develop이 이미 만든 [`UserApiService`](../../../core/data/src/main/java/team/mino/core/data/network/service/UserApiService.kt)와 [`UserRemoteDataSource`](../../../core/data/src/main/java/team/mino/core/data/datasource/UserRemoteDataSource.kt)가 소유하고, 이 feature는 그 둘을 **넓힌다.** 도메인 표면([`ProfileRepository`](../../../core/domain/src/main/kotlin/team/mino/core/domain/repository/ProfileRepository.kt))은 바뀌지 않는다.
- **이 결정이 덮어쓰는 것**: [D36](#d36-원격-연동-착수--원천은-서버-로컬-datastore는-캐시)의 "`:core:data`에 `ProfileApiService`·`ProfileRemoteDataSource`를 두고"라는 **타입 배치 한 구절**과, [D38](#d38-등록수정-분기--서버에-직접-묻고-캐시가-그-답을-들고-있는다)·[D47](#d47-develop-재대조--420의-대조가-옛-트리를-근거로-했다)이 `ProfileApiService.getMe()`로 지목한 **이름**이다. 두 결정의 실질(원천은 서버·캐시는 로컬, 등록/수정은 캐시 유무로 가른다, `401`+`USER_NOT_REGISTERED`만 미등록으로 읽는다)은 **그대로 유효하다** — 바뀐 것은 그 일을 어느 타입이 하느냐뿐이다.

### 무엇이 드러났나

`feature/159-profile-setup/base`에 develop을 반영한 뒤 `:core:data`를 전수 확인하니, **같은 엔드포인트 그룹의 구현이 이미 커밋되어 있었다.** splash-screen이 `d783e03`(`feat: 프로필 등록 여부 조회 데이터 계층 추가`)로 넣은 것이다.

| 계층 | develop (커밋됨) | 이 feature가 만들던 것 (미커밋) |
|---|---|---|
| `ApiService` | `UserApiService.getMe()` → `Unit`, 본문 미역직렬화 | `ProfileApiService.getMe()`/`register()`/`updateMe()` |
| `errorCode` 파싱 | `UserRemoteDataSourceImpl` — `Json.parseToJsonElement` 수동 파싱 | `ProfileApiService` — `ErrorResponse` DTO |
| 상수 `USER_NOT_REGISTERED` | `UserRemoteDataSourceImpl`의 `private const` | `ProfileApiService`의 `companion` |
| `DataSource` | `UserRemoteDataSource.isRegistered()` | `ProfileRemoteDataSource.getMe()` |
| 도메인 Repository | `ProfileRegistrationRepository.isRegistered()` | `ProfileRepository.refreshProfile()` |

같은 경로에 서비스 둘, `errorCode` 파싱 둘, 상수 둘이다. **도메인 Repository 둘만이 정당한 중복이다** — `isRegistered()`는 스플래시의 진입 판정, `refreshProfile()`은 프로필 값의 프리필로 관심사가 다르다. 나머지 넷은 같은 일을 두 번 한다.

### Rationale

**`ApiService`의 단위는 feature가 아니라 서버 리소스(OpenAPI 태그)다.** 이것은 취향이 아니라 저장소가 이미 지키고 있는 규칙이다 — [`RoomApiService`](../../../core/data/src/main/java/team/mino/core/data/network/service/RoomApiService.kt)가 `room` 태그의 `listRooms`·`getRoom`·`createRoom`·`updateRoom`을 **전부** 갖고, `PinApiService`가 `pin` 쪽을 갖는다. 방을 쓰는 feature가 여럿인데도 `RoomApiService`는 하나다. 프로필 세 엔드포인트는 전부 `user` 태그이므로 소유자는 `UserApiService`다. `ProfileApiService`는 **feature 이름을 서버 리소스 경계에 덧씌운 두 번째 소유자**였다.

**엔드포인트 지식이 두 곳에 있으면 반드시 갈라진다.** 경로 문자열(`api/v1/users/me`)·미등록 판정 규칙·`USER_NOT_REGISTERED` 상수가 두 벌이면, 서버가 미등록을 `404`로 바꾸는 날 한쪽만 고쳐도 컴파일이 통과한다. 스플래시는 온보딩 진입을 판정하고 프로필은 프리필을 판정하므로, 갈라진 쪽이 어디든 **사용자는 온보딩에 갇히거나 빈 폼을 본다.**

**그런데도 스플래시의 본문 미역직렬화는 보존한다.** `UserApiService.getMe()`가 성공 본문을 읽지 않는 것은 develop의 의도적 선택이다("필요한 것은 상태 코드와 `errorCode`뿐이라 DTO도 Mapper도 두지 않았다"). 이것을 `getMe(): ProfileResponse?` 하나로 합치면 **스플래시의 진입 판정이 프로필 본문의 스키마 일치에 의존하게 된다** — 서버가 `200`과 함께 예상 밖 본문을 주면 지금은 통과하는 진입이 그때부터 실패한다. 실제로 커밋된 [`UserRemoteDataSourceImplTest`](../../../core/data/src/test/java/team/mino/core/data/datasource/UserRemoteDataSourceImplTest.kt)는 `{"data":{"id":1}}`로 성공을 확인하는데, 이 본문은 `ProfileResponse`를 만족하지 않는다. 그 픽스처가 느슨한 것이 아니라 **판정이 본문에 기대지 않는다는 사실 자체가 검증되고 있는 것**이다. 합치면 그 검증이 사라진다.
  - 그래서 `UserApiService`는 같은 경로에 **함수 둘**을 갖는다 — `hasProfile(): Boolean`(본문 안 읽음, 스플래시)과 `getMe(): ProfileResponse?`(본문 읽음, 프로필). 어색해 보이지만 두 질문의 **실패 허용치가 다르다**는 사실을 타입이 정직하게 드러내는 편이 낫다. 공유되는 것은 `401` 판정 헬퍼 하나다.

**`errorCode` 지역 파싱은 `ApiService`에 둔다.** develop은 이것을 `UserRemoteDataSourceImpl`에 두었는데, 그러려면 `ResponseException`·`bodyAsText()`를 만져야 해서 **Ktor 타입을 다루는 일이 `network/` 밖으로 나온다** — [`core:data` README](../../../core/data/README.md) §5의 "데이터 출처 호출만"을 넘어선다(§9는 DTO **노출**을 금지하는 조항이라 이 건과 무관하다). [ADR 2026-08-28](../../adr/2026-08-28-error-body-type-and-no-error-code-leaf.md)이 이미 그 배치를 검토하고 같은 이유로 기각해 두었다. 그 ADR을 따르고 **develop의 커밋 코드를 그쪽으로 맞춘다.** 옮기고 나면 `UserRemoteDataSourceImpl`은 위임 네 줄이 되어 README §5의 모범 형태에 오히려 가까워진다.
  - 파싱 방식도 `ErrorResponse` DTO로 통일한다. develop의 `Json.parseToJsonElement` + `JsonObject` 수동 파싱은 같은 ADR이 정한 타입을 쓰지 않고 손으로 다시 만든 것이다.
  - **규약과 커밋 코드가 서로 다른 답을 갖고 있었다.** README §4는 지역 catch를 "해당 DataSource에서" 병용하라고 적고 develop이 그대로 따랐는데, ADR 2026-08-28은 `ApiService`를 골랐다.
  - **2026-08-28에 사용자 승인으로 §4를 정리했다.** 한쪽으로 몰지 않고 **자리를 무엇을 보는지로 갈랐다** — 상태 코드만 보는 정책(§4의 원래 예시 "404를 빈 결과로")은 `DataSource`에, **실패 본문을 읽어야 하는 정책만** `ApiService`에 둔다. 두 경우가 요구하는 것이 다르기 때문이다. 이 결정에는 영향이 없다.

### 이 결정이 만드는 작업

| 파일 | 무엇이 |
|---|---|
| `network/service/UserApiService.kt` | **확장** — `hasProfile()`·`getMe()`·`register()`·`updateMe()`, `401` 판정 헬퍼 |
| `datasource/UserRemoteDataSource.kt`(+`Impl`) | **확장** — `getMe()`·`register()`·`updateMe()` 추가. `Impl`의 수동 JSON 파싱 제거, 순수 위임으로 |
| `datasource/UserRemoteDataSourceImplTest.kt` | 지역 catch가 옮겨간 만큼 `UserApiServiceTest`로 이동. `isRegistered()` 위임만 남는다 |
| `datasource/di/ProfileDataSourceModule.kt` | 원격 바인딩을 두지 않는다 — 로컬 하나만 |
| `repository/ProfileRepositoryImpl.kt` | 주입 대상이 `UserRemoteDataSource`로 |
| `docs/adr/2026-08-28-...md` | "현재 소비 지점은 `ProfileApiService.getMe()` 하나다" → `UserApiService`로. 소비 지점이 둘(`hasProfile`·`getMe`)이 된 사실 반영 |
| ~~`network/service/ProfileApiService.kt`~~ | **만들지 않는다** |
| ~~`datasource/ProfileRemoteDataSource.kt`(+`Impl`)~~ | **만들지 않는다** |

`ProfileRequest`·`ProfileResponse`·`ErrorResponse`·`ProfileMapper`·`ProfileEntry`·`ProfileAvatar`는 **그대로 유효하다** — 이 결정은 타입의 내용이 아니라 그것을 호출하는 서비스의 소유자만 바꾼다.

### Alternatives considered

- **`UserApiService.getMe(): ProfileResponse?` 하나로 완전 통합하고 `isRegistered() = getMe() != null`**. 코드 경로가 하나로 줄어 가장 깔끔하다. 그러나 스플래시 진입 판정이 프로필 본문 스키마에 의존하게 되고, 그 사실을 지키던 커밋된 테스트 픽스처를 고쳐야 한다. **진입 게이트의 실패 허용치를 좁히는 대가**가 중복 제거 이득보다 크다. 기각.
- **`ProfileApiService`를 그대로 두고 병존**. 커밋된 파일을 하나도 건드리지 않아 회귀 위험이 0이다. 그러나 같은 경로에 서비스 둘·`errorCode` 파싱 둘이 남고, ADR 2026-08-28의 "현재 소비 지점은 하나다"가 **거짓인 채로 남는다.** ADR이 사실과 어긋나면 그 다음 개정이 잘못된 전제 위에서 판단한다. 기각.
- **지역 파싱을 develop처럼 `DataSource`에 두고 ADR 2026-08-28을 개정**. 커밋 코드를 안 고쳐도 된다. 그러나 ADR이 그 배치를 이미 검토하고 Ktor 누출을 이유로 기각했으므로, 되살리려면 **기각 사유가 틀렸음을 보여야 하는데 누출은 실제로 일어난다**(`UserRemoteDataSourceImpl`이 `ResponseException`·`bodyAsText()`를 만진다). 기각.
- **`ProfileRegistrationRepository`를 없애고 `ProfileRepository`로 합친다**. 도메인 Repository 둘도 프로필을 다루니 합칠 수 있어 보인다. 그러나 스플래시가 필요한 것은 **값이 아니라 존재 여부**이고, 합치면 진입 판정이 `observeProfile()`의 캐시 의미까지 알아야 한다. 관심사가 다르므로 둘을 유지한다. 범위 밖이기도 하다 — 스플래시는 이미 머지된 feature다. 기각.

- (plan 5.0.0에서 확인)

## D50. 진입 시 갱신 — 마이페이지 진입에서만 건다

- **Decision**: `ProfileViewModel`의 진입 시 `refreshProfile()` 호출을 `ProfileEntryPoint.MyPage`로 제한한다. 온보딩 진입에서는 갱신하지 않고 캐시 프리필만 돈다. 판정은 `ProfileEntryPoint.needsRefresh`가 소유한다.
- **Rationale**: 온보딩 진입은 스플래시의 `ResolveSplashEntryUseCase`가 `hasProfile()`로 **같은 `GET /api/v1/users/me`를 쳐서 미등록(`401 USER_NOT_REGISTERED`)을 확정한 결과** 열린 화면이다([D49](#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다)). 그 직후 `getMe()`로 같은 경로를 한 번 더 치면 같은 401을 받고, 이미 비어 있는 캐시를 다시 비우는 것으로 끝난다. 앱 시작 경로에 **결과가 0인 왕복 1회**와 매 요청에 붙는 `IdTokenProvider.getIdToken()` 1회가 순수 낭비로 얹힌다.
- **마이페이지는 제외하지 않는 이유**: 스플래시는 콜드 스타트 때 **등록 여부만** 봤고 값을 캐시에 채우지 않았다. 마이페이지 진입은 그로부터 임의의 시간이 지난 시점이라 [D45](#d45-프리필과-갱신의-순서--캐시로-먼저-채우고-갱신이-성공하면-조건부로-한-번-더)가 든 복구 경로(캐시 쓰기 실패·서버 상태가 앞선 경우)가 그대로 살아 있다.
- **온보딩에서 캐시를 비우는 일은 스플래시 게이트로 옮긴다.** 갱신을 끄면 `refreshProfile()`이 하던 캐시 무효화도 함께 사라지는데, 그 정리는 `ProfileRepositoryImpl.saveProfile()`의 등록/수정 분기가 기대는 전제다(캐시 유무로 `POST`/`PATCH`를 가른다 — [D38](#d38-등록수정-분기--서버에-직접-묻고-캐시가-그-답을-들고-있는다)). 그래서 `ProfileRegistrationRepositoryImpl.isRegistered()`가 미등록으로 판정할 때 캐시를 비운다. 왕복이 늘지 않고 성공 본문을 역직렬화하지도 않으므로 [D49](#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다)의 성질도 그대로다. 이로써 온보딩의 "캐시는 비어 있다"가 UI의 추정이 아니라 데이터 레이어가 세운 사실이 된다.
- **조회 메서드에 쓰기가 딸리는 것을 감수한다**: `isRegistered()`는 이름상 질의지만, 그 판정 자체가 캐시를 무효로 만드는 사건이다. 판정과 무효화를 떼어 두면 둘 사이에 캐시가 낡은 채로 읽히는 창이 생기고, 그 창을 닫을 책임자가 없다. 부수효과는 도메인 인터페이스 KDoc에 계약으로 싣는다.
- **[D45]의 나머지는 그대로다**: 캐시 먼저 → 갱신 그다음의 순서, 두 가드(`isNicknameTouched`·`isSaving`), 로딩 상태를 두지 않는 것 모두 유효하다. 이 결정은 갱신을 *언제 거는가*만 좁힌다.
- **판정을 `ProfileEntryPoint`가 드는 이유**: ViewModel의 private 함수로 두면 유닛 테스트로 판정할 수 없다 — 이 모듈은 `isReturnDefaultValues = true`라 `savedStateHandle.toRoute<ProfileMain>()`가 스텁 `Bundle`을 읽어 **어떤 진입점을 넣어도 `MyPage`로 복원된다**(`RoomFormViewModelTest`가 같은 사실을 문서화하고 있다). 진입점 자체의 성질로 올리면 ViewModel을 거치지 않고 직접 고정할 수 있다.
- **Alternatives considered**:
  - **스플래시가 `hasProfile()` 대신 `refreshProfile()`을 써서 한 번의 왕복으로 등록 판정과 캐시 채움을 끝낸다.** 중복이 근본에서 사라지고 프로필 화면은 캐시만 읽으면 된다. 그러나 진입 게이트가 프로필 본문 스키마에 묶여, 서버가 예상 밖 본문을 주는 순간 앱을 켜는 모든 사용자가 진입에 실패한다 — [D49](#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다)가 `hasProfile()`이 성공 본문을 역직렬화하지 않도록 지킨 바로 그 성질을 깬다. 기각.
  - **현행 유지.** 동작은 옳고 낭비는 온보딩 1회뿐이다. 그러나 그 1회가 하필 **앱 시작 경로**에 있고, 없애는 비용이 조건 한 줄이다. 기각.
- (2026-08-28 확정)


## D51. 닉네임 15자 상한의 강제 지점 — ViewModel이 자른다

- **Decision**: [spec 3.0.0](spec.md) FR-014의 15자 상한을 **`ProfileViewModel.changeNickname()`이 `value.take(NICKNAME_MAX_LENGTH)`로 자른다.** `ValidateNicknameUseCase`에는 상한을 넣지 않고, `MinoTextField`에도 `maxLength` 파라미터를 더하지 않는다. 자른 값으로 판정을 다시 받는다.
- **Rationale**: 세 가지가 같은 곳을 가리킨다.
  - **spec이 상한을 판정에서 뺐다.** FR-002가 "길이 상한 15자는 이 판정에 넣지 않는다"로 명시하고, FR-014가 그것을 입력 차단으로 돌린다. 상한을 UseCase에 심으면 spec이 나눈 두 개념(오류가 되는 하한 / 오류가 아닌 상한)이 한 판정으로 뭉개지고, 디자인에 없는 "15자 초과" 오류 문구를 만들어야 한다.
  - **같은 저장소에 선례가 있다.** 방 이름 15자가 정확히 이 형태다 — [`RoomFormViewModel.changeName()`](../../../feature/roomform/src/main/java/team/mino/feature/roomform/form/vm/RoomFormViewModel.kt)이 `value.take(NAME_MAX_LENGTH)`로 자르고 `ValidateRoomNameUseCase`는 길이를 모른다. 원인도 같다 — **`MinoTextField`에는 `maxLength`가 없고 `MinoTextArea`에만 있다.** 두 번째 사례이므로 승격 후보로 든다(완료 보고).
  - **세는 단위는 이미 규율돼 있다.** [ADR 2026-08-25](../../adr/2026-08-25-grapheme-count-for-text-input.md)가 grapheme cluster를 원칙으로 두되 **"허용 문자가 전부 BMP 안이면 코드 유닛으로 세도 된다"**를 방 이름에 적용했다. 닉네임의 허용 문자(한글 음절 `가`–`힣` · 영문)는 그 방 이름 집합의 **부분집합**이므로 같은 판정이 그대로 성립한다. spec §4의 "사용자가 보는 문자 단위"는 유효한 값 전부에서 `String.take`와 일치한다.
- **자르고 나서 판정하는 순서가 중요하다**: 원본으로 판정하면 화면에 없는 16번째 글자가 오류를 만든다. `RoomFormViewModel`의 KDoc이 같은 이유를 든다.
- **감수하는 것**: 상한을 넘는 **무효** 입력(이모지 등)을 붙여넣으면 `take`가 서로게이트 쌍을 가를 수 있다. 그 값은 어차피 `ValidateNicknameUseCase`가 무효로 판정해 오류 상태가 되므로 저장 경로에 닿지 않는다. ADR이 방 이름에 대해 감수한 것과 같은 범위다.
- **Alternatives considered**:
  - **`MinoTextField`에 `maxLength`를 더한다.** 자르는 주체가 컴포넌트로 모여 두 필드의 비대칭이 사라진다. 그러나 `:core:design-system`을 넓히는 일이고, `MinoTextArea`의 `maxLength`는 **카운터의 분모**를 겸하는데 디자인에 닉네임 카운터가 없다(UX-007) — 카운터 없는 `maxLength`를 위해 파라미터 축을 하나 더 여는 셈이다. 이 plan의 범위 밖이기도 하다(design-system은 손대지 않는다). 기각하되, 세 번째 사례가 나오면 재검토 대상이다.
  - **`ValidateNicknameUseCase`에 상한을 넣는다.** 한 곳에서 다 판정해 단순하다. 그러나 spec FR-002가 명시적으로 배제했고, 오류 상태가 되면 디자인에 없는 문구가 필요해진다. 기각.
  - **자르지 않고 서버 거절에 맡긴다(5.0.0까지의 동작).** spec 3.0.0이 이 선택지를 닫았다. 기각.
- (plan 5.1.0에서 결정)

## D52. 서버 문서 재조회(2026-08-31) — 닉네임 `pattern`에서 공백이 빠졌다

- **Decision**: [API 계약](contracts/profile-api-contract.md)의 근거 조회본을 **2026-08-31T12:44:57+09:00**으로 갱신한다. 설계는 바꾸지 않는다 — 바뀐 것이 이미 클라이언트가 지키고 있던 방향이기 때문이다.
- **무엇이 바뀌었나**: `POST /api/v1/users`·`PATCH /api/v1/users/me`의 `nickname.pattern`이 `^[\uAC00-\uD7A3A-Za-z ]+$`에서 **`^[\uAC00-\uD7A3A-Za-z]+$`** 로 좁아졌고(문자 클래스 끝의 공백이 빠졌다), `PATCH`의 description이 `닉네임(공백 포함 한글/영문 2~15자)` → **`닉네임(한글/영문 2~15자, 공백·숫자 불가)`** 로 바뀌었다. 나머지(아바타 13종 `enum`, `id: uuid`, `401`·`409` 코드와 `errorCode` 열거, `PATCH`의 `required: []`)는 2026-08-28 조회본과 **동일**했다.
- **Rationale**: 이 변화는 클라이언트에 **아무 작업도 만들지 않는다.** `ValidateNicknameUseCase`가 이미 공백을 무효로 판정하고 있었고([D19](#d19-닉네임-규칙-불일치--클라이언트는-spec을-따르고-서버-거절은-저장-실패로-받는다)), 서버가 그 좁은 쪽으로 따라온 것이다. 바뀌는 것은 **문서의 어긋남 목록**뿐이며, [API 계약 §2](contracts/profile-api-contract.md)의 3번이 "spec이 알고 받아들인 어긋남"에서 **"어긋남이 아니다"** 로 내려간다.
- **이번 조회가 값어치를 한 지점**: [D51](#d51-닉네임-15자-상한의-강제-지점--viewmodel이-자른다)의 근거를 세우려고 상한을 확인하러 갔다가 **묻지 않은 것이 바뀐 것**을 발견했다. plan 4.0.0의 `avatar.color` enum 사건(3시간 만의 값 도메인 변경), 4.3.0의 옛 트리 대조에 이어 **세 번째로 재조회가 사실을 뒤집었다.** [quickstart §2](quickstart.md)의 착수 직전 재조회 규칙을 유지할 근거가 하나 더 쌓였다.
- **감수하는 것**: 서버 문서에 변경 이력이 없어 **언제 바뀌었는지 알 수 없다.** 8-28과 8-31 사이라는 것만 안다. 이 저장소가 조회 시점을 적는 것이 유일한 시간 축이다.
- (plan 5.1.0에서 결정)

## D53. 기본 아바타의 자리 — 도메인은 13항목, 디자인 시스템 팔레트는 12종 그대로

- **Decision**: [spec 4.0.0](spec.md) FR-015가 확정한 "선택 목록 12종 밖의 기본 아바타"를 **레이어마다 다른 형태로 든다.**
  - **`:core:domain`의 `ProfileAvatar`** — 13번째 항목을 **마지막에** 더하고 `Default`가 그것을 가리킨다. 프로필이 가리키는 값은 언제나 이 13종 중 하나다.
  - **`:core:design-system`의 `MinoProfileAvatar`** — **12종 그대로 둔다.** 기본 그림은 열거 항목이 아니라 `MinoProfileAvatarImage(avatar: MinoProfileAvatar?)`의 **`null` 갈래**가 그린다.
  - **`:feature:profile`의 매핑** — `ProfileAvatar.image`의 반환 타입이 `MinoProfileAvatar?`가 되고 기본 아바타만 `null`로 간다. 반대 방향은 12종 그대로다.
  - **`:core:data`의 `ProfileMapper`** — 색 표가 13행이 되고, 기본 아바타가 `enum`의 13번째 색에 대응한다.
- **Rationale**: **저장소가 같은 문제를 이미 풀어 두었고, 그 답을 그대로 쓴다.** 방 대표 색이 정확히 이 구조다.

  | 자리 | 방 대표 색 | 프로필 아바타(이 결정) |
  |---|---|---|
  | 도메인 | [`RoomColor`](../../../core/domain/src/main/kotlin/team/mino/core/domain/model/RoomColor.kt) — 13항목, `GRAY` 포함 | `ProfileAvatar` — 13항목 |
  | 디자인 시스템 | [`MinoRoomColor`](../../../core/design-system/src/main/java/team/mino/core/designsystem/component/roomcolorchip/MinoRoomColor.kt) — **12종.** KDoc이 "회색 기본값·표시 이름·서버 식별자·그리드 배치는 여기에 없다"로 못박는다 | `MinoProfileAvatar` — **12종 그대로** |
  | 미선택 표현 | 소비처의 `MinoRoomColor?`의 `null` | 소비처의 `MinoProfileAvatar?`의 `null` |
  | 기본 그림 | [`RoomThumbnailFallback`](../../../core/common/ui/src/main/java/team/mino/core/common/ui/component/RoomThumbnailFallback.kt)이 `MinoRoomColor?`를 받아 `null -> room_thumbnail_gray` | `MinoProfileAvatarImage`가 `MinoProfileAvatar?`를 받아 `null -> 기본 그림` |
  | 서버 표 | [`RoomMapper`](../../../core/data/src/main/java/team/mino/core/data/repository/mapper/RoomMapper.kt) — 13행. `null`은 `GRAY`로 확정해 보내고, 모르는 값은 `GRAY`로 읽는다 | `ProfileMapper` — 13행. 같은 규칙 |

  `RoomColor`의 KDoc이 든 이유가 spec §4의 문장과 같다 — **"`GRAY`는 '값 없음'이 아니라 색을 고르지 않은 방이 갖게 되는 색이다."** spec 4.0.0이 기본 아바타를 두고 "화면에만 있는 자리 표시가 아니라 어엿한 값"이라 적은 것과 같은 말이다.
- **이 형태가 실제로 사게 되는 것 셋**:
  - **`ProfileAvatarGrid`가 한 줄도 바뀌지 않는다.** `MinoProfileAvatar.entries`가 여전히 12개라 4열 × 3행이 그대로 서고, 기본 아바타를 걸러 내는 필터를 두지 않는다. 13항목으로 넓혔다면 `entries - 기본`이 필요하고, 그것을 빠뜨려도 컴파일은 통과해 **13칸짜리 그리드가 조용히 그려진다.**
  - **`MinoProfileAvatar`의 KDoc이 지켜진다** — "저장 식별자·'미선택'·그리드 배치는 갖지 않는다". 13번째 항목은 이름을 무엇으로 짓든 역할(기본값)을 담게 되고, 그 문장을 어긴다.
  - **`prefill()`이 저절로 맞는다.** 서버가 13번째 색을 준 프로필은 `image`가 `null`을 내므로 `selectedAvatar = null`, 즉 "고르지 않음"으로 복원된다. 실제로 사용자가 고르지 않은 상태이므로 `지우기` 활성 조건(FR-005)도 옳게 계산된다. 13항목으로 넓혔다면 기본 아바타가 **선택된 것처럼** 복원돼 `지우기`가 활성이 된다.
- **감수하는 것**: `MinoProfileAvatarImage`의 `avatar`가 nullable이 되면서 **"null이면 기본 그림"이라는 규칙이 `:core:design-system`에 들어간다.** 방 쪽은 그 규칙이 `:core:common:ui`에 있어 모듈이 다르다. 그러나 아바타는 [ADR 2026-08-25](../../adr/2026-08-25-profile-avatar-assets-in-design-system.md)가 에셋과 컴포넌트를 이 모듈에 두기로 이미 정했고, 기본 그림만 다른 모듈로 빼면 테두리·지름·선택 시맨틱을 복제해야 한다. 규칙 자체는 `RoomThumbnailFallback`이 든 것과 글자 그대로 같다.
- **호출부 파급은 없다**: `MinoProfileAvatarImage`를 쓰는 곳은 `:feature:profile`(썸네일·그리드)과 모듈 내부 Preview뿐이다. 그리드는 12종을 넘기므로 non-null이 그대로 통하고, 썸네일만 nullable을 받는다.
- **Alternatives considered**:
  - **`MinoProfileAvatar`를 13항목으로 넓힌다.** `MinoProfileAvatarImage`의 시그니처가 그대로여서 변경 폭이 가장 작아 보인다. 그러나 위 셋(그리드 필터·KDoc·프리필)을 모두 잃고, `MinoRoomColor`가 같은 상황에서 고르지 않은 쪽이다. 기각.
  - **기본 그림을 `:core:common:ui`에 둔다.** `RoomThumbnailFallback`과 모듈까지 같아진다. 그러나 썸네일의 기하(120dp·5dp 테두리)를 그 모듈이 다시 알아야 하고, ADR 2026-08-25의 에셋 소유 결정과 어긋난다. 기각.
  - **기본 아바타를 12종 중 하나로 둔다(현행).** 코드가 하나도 안 바뀐다. spec 4.0.0 FR-015가 정면으로 닫았다 — 디자인의 기본 썸네일이 목록 첫 항목과 다른 그림이다. 기각.
- (plan 6.0.0에서 결정)

## D54. 닉네임 안내 문구 — 평상시와 오류를 다른 문구로 가른다

- **Decision**: `MinoTextField`의 `helperText`를 상태에 따라 갈아 끼운다. 평상시에는 `최대 15자까지 입력할 수 있어요.`, `isNicknameErrorVisible`이면 `한글·영문 2글자 이상을 입력해주세요.`다. 문자열 둘 다 `:feature:profile`의 `strings.xml`이 갖고, **화면이 고른다** — `ProfileUiState`에 필드를 더하지 않는다.
- **Rationale**:
  - **spec 4.0.0 FR-011이 두 문구를 각각 지정했다.** 사용자가 확정한 것은 "디자인대로 한 문구를 색만 바꾸는" 쪽이 아니라 **사유별로 가르는** 쪽이다 — 1글자를 입력했을 때 상한을 말하는 문구가 빨갛게 뜨는 것이 무엇이 잘못됐는지 알려 주지 못하기 때문이다.
  - **화면 구조는 디자인 그대로다.** 안내 문구 자리가 하나뿐인 것도, 오류일 때 색이 바뀌는 것도 원본과 같다. 바뀌는 것은 그 자리에 놓이는 **글자**뿐이라 [figma-design-fidelity](../../conventions/figma-design-fidelity.md)의 대조에서 새로 재는 값이 없다.
  - **상태에 새 필드가 필요 없다.** 갈림의 조건이 이미 있는 파생 값 `isNicknameErrorVisible`(= `isNicknameTouched && !isNicknameValid`)과 정확히 같다. 문구 선택은 그리는 일이므로 화면이 한다.
- **상한용 오류 문구는 만들지 않는다**: 상한 초과는 [D51](#d51-닉네임-15자-상한의-강제-지점--viewmodel이-자른다)의 입력 차단이 막아 오류 상태 자체가 생기지 않는다(FR-014·UX-007). 문구는 평상시 자리에서 상한을 **미리** 알려 주는 역할만 한다.
- **이번 개정이 드러낸 것**: `profile_nickname_helper`가 이미 `최대 15자까지 입력할 수 있어요.`였다(`feature/273-onboarding-branding` 브랜치의 **미커밋 작업분** — 하드코딩돼 있던 화면 문자열을 리소스로 뽑으면서 디자인의 문구를 그대로 넣었다) — 즉 **구현이 디자인을 따랐고, spec 3.0.0 FR-011과 [화면 계약](contracts/profile-screen-contract.md)만 옛 문구(`한글·영문 2글자 이상을 입력해주세요.`)를 들고 있었다.** 어긋남이 화면에 드러나지 않은 이유는 그 문구가 오류일 때만 다르게 보였어야 하는데 오류 갈래가 아예 없었기 때문이다. [D45](#d45-프리필과-갱신의-순서--캐시로-먼저-채우고-갱신이-성공하면-조건부로-한-번-더)의 "문서가 코드보다 앞서간" 사례와 방향이 반대인 **"코드가 문서보다 앞서간"** 첫 사례다.
- **Alternatives considered**:
  - **디자인대로 한 문구만 두고 색만 바꾼다.** 원본과 글자까지 같아진다. 사용자가 spec 4.0.0에서 이 선택지를 검토하고 기각했다. 기각.
  - **`ProfileUiState`가 `@StringRes helperTextRes`를 든다.** 화면의 분기가 사라진다. 그러나 상태가 안드로이드 리소스 식별자를 알게 되고, 같은 상태를 쓰는 Preview·테스트가 리소스에 묶인다. 기각.
  - **`MinoTextField`에 `errorText`를 더한다.** 컴포넌트가 두 문구를 받아 상태로 고른다. `:core:design-system`을 넓히는 일이고, 다른 화면에 같은 요구가 아직 없다. 기각하되 두 번째 사례가 나오면 재검토한다.
- (plan 6.0.0에서 결정)

## D55. 서버 문서 재조회(2026-08-31 16:03) — 바뀐 것이 없다

- **Decision**: [API 계약](contracts/profile-api-contract.md)의 근거 조회본을 **2026-08-31T16:03:55+09:00**으로 갱신한다. 계약 본문은 아바타 값 표가 13행이 되는 것([D53](#d53-기본-아바타의-자리--도메인은-13항목-디자인-시스템-팔레트는-12종-그대로)) 말고는 바뀌지 않는다.
- **무엇을 확인했나**: 세 오퍼레이션을 원문으로 다시 펼쳐 [D52](#d52-서버-문서-재조회2026-08-31--닉네임-pattern에서-공백이-빠졌다)의 조회본(같은 날 12:44)과 대조했다. `nickname`의 `minLength 2`·`maxLength 15`·`pattern ^[가-힣A-Za-z]+$`, `avatar.color`의 13개 `enum`, `POST`의 `required: ["nickname","avatar"]`, `PATCH`의 `required: []`, `401`·`409`의 `errorCode` 열거까지 **전부 동일**했다.
- **이번 재조회가 값어치를 한 지점**: 사용자가 제시한 서버 닉네임 정책표가 문서와 같은지 확인하는 것이 이번 개정의 출발점이었다. **여섯 항목 중 다섯이 문서에 있고 정확히 일치했다** — 길이·허용 문자·불허 문자·앞뒤 공백 처리(`pattern`이 공백을 배제하므로 trim 없이는 통과할 수 없다는 사실로 뒷받침)·필수 여부. 확인 결과 spec 3.0.0과 이미 같아 **닉네임 쪽에는 아무 작업도 생기지 않았고**, 이번 개정의 실질은 전부 아바타와 문구 쪽이다.
- **문서에 없는 것 하나**: 정책표의 `수정 시 각각 optional (단 최소 한 필드 필요)` 중 **"최소 한 필드"** 는 `PATCH`의 스키마에 없다(`required: []`이므로 빈 객체도 스키마상 통과한다). 서버가 스키마 밖에서 거는 규칙이며, **이 앱은 언제나 두 값을 함께 보내므로 닿지 않는다**([API 계약 §1](contracts/profile-api-contract.md)). 협의 항목으로 세우지 않고 사실만 기록한다.
- (plan 6.0.0에서 결정)
