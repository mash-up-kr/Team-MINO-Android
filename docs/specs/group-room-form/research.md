# 리서치: 공동방 생성 및 편집 폼

**대상 스펙 경로**: `docs/specs/group-room-form`

**부속 문서**: [plan.md](./plan.md)에 종속된다. 이 문서는 독자 버전을 갖지 않으며, 각 항목이 어느 plan 버전에서 결정되었는지를 적는다.

> 결정을 지우지 않는다. 뒤집힌 결정은 취소선과 `재검토됨(plan X.Y.Z)` 표시를 남기고 새 항목을 덧붙인다.

> **Figma 노드 표기**: 이 문서의 `NNNN-NNNNN`은 [MU_디자인](https://www.figma.com/design/5P3HE7q8MGc6yAr4rTOSZn/MU_%EB%94%94%EC%9E%90%EC%9D%B8) 파일의 노드 ID다. 디자인 시스템 라이브러리 노드는 [MU_Wanted Design System](https://www.figma.com/design/hkSOCt4kOfyaVWdxybTicF/MU_Wanted-Design-System--Community-) 파일 소속임을 그 자리에 밝힌다. 표기 규칙은 [`figma-design-fidelity.md`](../../conventions/figma-design-fidelity.md) §5.

---

## R-001. 폼을 어떤 종류의 feature로 두는가 *(plan 1.0.0)*

**Decision**: **진입형 feature 모듈 `:feature:roomform`.** Activity(`RoomFormActivity`)를 진입점으로 갖고, 다른 feature는 `:core:navigation`의 `RoomFormLauncher` 계약으로 연다.

**Rationale**: [`feature-module.md`](../../architecture/feature-module.md) 1장이 구분 기준을 "재사용 여부가 아니라 Activity로 독립 진입하는지, 탭 셸의 그래프에 편입되는지"로 못박았다. 폼의 호출자는 온보딩·방 리스트 탭·홈 탭·장소 상세 시트·방 상세로 흩어져 있고(FR-001의 진입점 8개), 그중 온보딩은 탭 셸과 생애주기가 아예 분리된다. 탭 feature로 두면 셸 그래프에 편입되어 온보딩에서 열 수 없고, 탭 feature로 만든 화면을 다른 feature가 여는 계약 자체가 없다. 또 FR-011·FR-019가 **호출자에게 결과를 돌려주기**를 요구하는데, 그 수단인 `resultLauncher`+`setResult`는 Activity 전환 축에만 있다([`feature-navigation.md`](../../architecture/feature-navigation.md) 1장).

모듈 이름은 새로 고르지 않았다 — [방 색상 팔레트 ADR](../../adr/2026-08-14-room-color-palette-in-design-system.md) §결과가 "첫 적용은 `:feature:roomform`이다"라고 이미 적어 두었다.

**Alternatives considered**:
- **`:feature:main`의 탭 그래프에 편입되는 화면으로 둔다** — 기각. 온보딩·장소 상세 시트에서 열 수 없다. 탭 feature는 셸만이 자기 그래프에 넣을 수 있다.
- **각 진입점 feature가 자기 안에 폼 화면을 갖는다** — 기각. FR-001이 "진입점과 무관하게 하나로 유지한다"를 명시적으로 요구한다. 복제하면 SC-003의 입력 규칙 보장이 진입점 수만큼 갈라진다.

---

## R-002. 서버가 없는 상태에서 데이터 레이어를 어디까지 진짜로 만드는가 *(plan 1.0.0)*

> ~~**재검토됨(plan 2.0.0)**~~ — 실서버가 배포되어 아래 결정의 전제("서버가 없다")가 사라졌다. `DataSource` 구현을 mock으로 채운다는 부분은 [R-024](#r-024-실서버가-붙었다--mock-데이터-레이어를-걷어낸다-plan-200)로 대체됐고, ADR 승격 후보에서도 내렸다. **나머지(DTO·Mapper·Repository·인터페이스를 실제 계약대로 세운다)는 유효하며 실제로 그대로 쓰였다.**

**Decision**: **DTO·Mapper·Repository·DataSource 인터페이스까지 실제 계약대로 만들고, ~~`DataSource` 구현 하나만 인메모리 mock으로 채운다~~.** `RoomMockRemoteDataSourceImpl`이 `RoomMockStore`(`@Singleton` 인메모리 맵)를 읽고 써서 swagger 스키마와 같은 모양의 DTO를 돌려준다. Ktor `ApiService`는 **이번에 만들지 않는다.**

**Rationale**: [`core/data/README.md`](../../../core/data/README.md) §8이 정한 10단계 절차 중 mock으로 대체되는 것은 2단계(ApiService)뿐이다. 나머지를 실제와 같게 두면 서버가 붙을 때 바뀌는 곳이 `RoomDataSourceModule`의 `@Binds` 한 줄과 새 `RoomApiService` 한 파일로 좁혀진다. 반대로 Repository까지 통째로 fake로 두면, 실서버 전환 때 Mapper·DTO·에러 매핑이 그때 처음 작성되어 이번 검증이 아무것도 보증하지 못한다.

**쓰지 않을 `ApiService`를 미리 만들지 않는 이유**는 [M3 컴포넌트 패턴 ADR](../../adr/2026-07-25-design-system-component-m3-pattern.md)이 API 표면에 대해 정한 것과 같다 — 호출부 없는 코드는 검증되지 않은 채 굳는다. `expectSuccess`·`convertDomainException`은 이미 `NetworkModule`에 전역으로 있어 서버 연결 시 새로 설계할 것이 없다.

**mock에 실패 주입 스위치를 두지 않는다.** EC-014(편집 실패 시 입력 유지)·UX-003의 검증은 Fake `RoomRepository`를 주입한 `RoomFormViewModel` 단위 테스트가 소유한다. 프로덕션 코드에 테스트 전용 분기를 남기지 않는다.

~~**이 결정은 다른 feature를 구속한다** — 서버가 붙기 전에 만들어지는 모든 화면이 같은 갈래에 선다. ADR 승격 후보다.~~

**Alternatives considered**:
- **Ktor `MockEngine`으로 HTTP 레이어까지 흉내낸다**(`libs.ktor.client.mock`이 카탈로그에 이미 있다) — 기각. JSON 문자열을 손으로 쓰고 라우팅을 흉내내는 비용이 인메모리 맵보다 크고, 검증되는 것은 우리가 쓴 mock JSON뿐이다. 단일 `HttpClient`에 baseUrl이 GitHub 임시값으로 잡혀 있어(`NetworkModule`) 엔진만 바꿔서는 깨끗하게 분리되지도 않는다.
- **`:core:domain`의 `RoomRepository`를 feature에서 fake로 구현한다** — 기각. feature가 Repository 구현을 가지면 [`dependency-injection.md`](../../conventions/dependency-injection.md)의 바인딩 소유 규칙이 뒤집히고, `:core:data`가 나중에 같은 인터페이스를 구현할 때 바인딩이 충돌한다.

> **사실 정정 (plan 1.2.0)**: 위 본문이 근거로 든 "baseUrl이 GitHub 임시값으로 잡혀 있다"는 더 이상 사실이 아니다. `core/data`가 그 사이에 `BuildConfig.API_BASE_URL`(flavor별)로 교체했다. 결정은 바뀌지 않는다 — MockEngine을 기각한 주된 이유는 JSON을 손으로 쓰는 비용이었고 baseUrl은 보조 근거였다.

---

## R-003. swagger 계약이 spec과 어긋나는 세 지점 *(plan 1.0.0)*

**Decision**: **세 지점 모두 spec을 따른다.** 어긋남은 서버팀에 제기할 계약 이슈로 보고한다.

| # | swagger 초안 | spec | 따를 쪽 |
|---|---|---|---|
| 1 | `description.maxLength: 20` | FR-005 · PRD 모두 30자 | **spec (30자)** |
| 2 | `color: string` — "팔레트 **5색** 중 하나의 hex 값 (예: `#FF6B6B`)" | FR-006 · PRD 데이터 용어 모두 **12종** | **spec (12종)** |
| 3 | `Room.color`는 hex 문자열인데 같은 문서의 `InvitationPreview.color`는 `{ id: integer }` 객체 | — | **식별자 문자열**(아래) |

**Rationale**: [헌법 원칙 IV](../../constitution.md)가 명세를 구현에 선행시킨다. swagger 자체가 `version: 0.1.0-draft`이고 상단에 "요청/응답 스키마·에러 코드 값은 각 피쳐 PR에서 확정한다"고 적어 두어, 이 초안은 확정 계약이 아니다. 게다가 3번은 **같은 문서 안에서 두 표현이 충돌**하므로 어느 쪽도 확정으로 볼 수 없다.

**색상을 hex로 주고받지 않는 것은 취향이 아니라 구조적 제약이다.** 12색의 실제 hex는 `AtomicColorToken`에 있고 그 오브젝트는 `internal`이다([`core/design-system/README.md`](../../../core/design-system/README.md) §4.5). `:core:data`는 `:core:design-system`을 의존하지도, 의존해서도 안 되므로(레이어 역행) **data 레이어가 hex 값을 알 방법 자체가 없다.** hex를 계약으로 두면 팔레트 hex 사본이 data 레이어에 생기고, 디자이너가 색을 바꿀 때 서버·앱·디자인 시스템 세 곳이 갈라진다. 그래서 mock DTO의 `color`는 `"red"` · `"red_orange"` · … · `"gray"` 식별자 문자열을 쓴다. 서버가 다른 표현으로 확정하면 고칠 곳은 `RoomMapper` 한 곳이다.

**Alternatives considered**:
- **swagger를 따르고 spec을 개정 대상으로 올린다** — 기각. spec 2.1.1이 Figma·PRD 두 산출물의 교차 근거로 30자·12색을 확정했고, 어긋난 쪽은 스스로 draft를 선언한 문서다. 앱이 5색만 그리면 디자인에 있는 칩 7개를 지워야 한다.
- **`color`를 hex로 주고받고 feature가 hex↔`MinoRoomColor`를 매핑한다** — 기각. 매핑 테이블이 feature에 생기는데 그 테이블의 값(hex)을 feature에서 읽을 수 없다. 읽으려면 `AtomicColorToken`을 public으로 열어야 하고, 그것은 [방 색상 팔레트 ADR](../../adr/2026-08-14-room-color-palette-in-design-system.md)이 이미 기각한 선택지다.

---

## R-004. 도착점 이동과 완료 스낵바를 누가 하는가 *(plan 1.0.0)*

**Decision**: **폼은 도착점을 모른다.** 폼은 `created` · `updated` · `skipped` · (취소) 중 무엇이 일어났는지와 `roomId`만 Activity 결과로 돌려주고, 어디로 갈지(FR-011)와 완료 스낵바(FR-012·FR-015)는 폼을 연 진입점 feature가 결정한다.

**Rationale**: FR-011의 도착점 세 갈래는 전부 다른 feature의 화면이다 — 온보딩 친구 초대 스텝(PRD [SCR-002]) · 방 상세(PRD [SCR-005]) · 장소 복제 시트(PRD [SYS-003]). 폼이 이들로 직접 이동하려면 그 feature들의 `Launcher`를 주입받아야 하고, 그 순간 `:feature:roomform`이 진입점 feature 다섯 개를 컴파일 타임에 알게 되어 [헌법 원칙 II](../../constitution.md)의 "feature 모듈은 다른 feature 모듈을 의존하지 않는다"가 무너진다. 진입점이 앞으로 더 늘면(PRD [SYS-009]가 이미 세 개를 더했다) 폼의 `when` 분기와 의존이 함께 늘어난다.

스낵바도 같은 이유로 폼의 몫이 아니다. UX-006이 "**이동한 화면 위에** 얹혀 표출된다"고 못박았으므로 표출 주체는 도착 화면이고, 폼은 그 시점에 이미 `finish()`된 상태다.

**대신 온보딩 여부는 인자로 받아야 한다.** FR-017(건너뛰기 노출)·FR-022(뒤로가기 비노출)가 폼 **자체의 chrome**을 바꾸기 때문이다. 이것이 폼이 진입 맥락에 대해 아는 유일한 것이며, 나머지 진입점 6개는 폼에게 구분되지 않는다.

**Alternatives considered**:
- **`RoomFormOrigin` enum을 `:core:navigation`에 두고 폼이 도착점을 분기한다** — 기각. 계약이 키 상수를 넘어 도메인 열거를 갖게 되고([`feature-navigation.md`](../../architecture/feature-navigation.md) 1장 "타입 계약 대신 키 상수"), 그래도 이동 자체는 다른 feature의 `Launcher` 없이는 못 한다. 분기만 늘고 문제는 그대로다.
- **폼이 스낵바를 띄운 뒤 이동한다** — 기각. UX-006과 어긋나고, 이동 후 표출이므로 폼의 생애주기를 벗어난다.

---

## R-005. 편집 진입 시 기존 값을 어떻게 채우는가 *(plan 1.0.0)*

**Decision**: **`roomId`만 넘겨받고 폼이 `RoomRepository.getRoom(roomId)`으로 조회한다.** 이름·설명·색상을 Intent extra로 실어 보내지 않는다.

**Rationale**: 값을 extra로 실으면 호출자(방 상세)가 자기가 들고 있는 방 정보를 그대로 넘기게 되는데, 그 정보가 낡았을 가능성을 폼이 알 방법이 없다. 또 FR-024가 요구하는 "폼 진입 시점의 값"이 **호출자가 화면에 그리고 있던 값**이 되어, 사용자가 보고 있던 것과 실제 저장된 것이 다르면 이탈 모달 판정이 어긋난다. 프로세스 사망 후 복원에서도 `roomId` 하나만 `SavedStateHandle`에 살아남으면 되므로 복원 경로가 단순하다.

조회 실패는 **주 데이터 로드 실패**이므로 State에 리프를 담고 에러 화면 + 재시도로 처리한다([`error_handling.md`](../../conventions/error_handling.md) §5). 생성 진입은 조회가 없어 이 경로를 타지 않는다.

**Alternatives considered**:
- **이름·설명·색상을 extra 3개로 넘긴다** — 기각. 위 정합성 문제에 더해, `EXTRA_*` 상수가 4개로 늘고 색상은 문자열로 다시 인코딩해야 한다. 로드 스피너 한 번을 아끼려고 계약을 넓히는 거래다.
- **호출자가 `Room` 도메인 모델을 직렬화해 넘긴다** — 기각. Intent extra의 타입 계약을 만드는 것이라 위 §R-004의 기각 사유와 같고, 도메인 모델이 `:core:navigation`으로 새어 나간다.

---

## R-006. 확인 모달 3종을 어느 모듈에 만드는가 *(plan 1.0.0)*

**Decision**: **`:feature:roomform`이 갖는다.** `form/component/RoomFormConfirmDialog.kt` 하나가 저장·생성 이탈·편집 이탈 세 모달을 제목과 버튼 라벨만 바꿔 그린다.

**Rationale**: [`component-asset-placement.md`](../../conventions/component-asset-placement.md) §1.2의 판정은 "Figma 디자인 시스템에 컴포넌트로 존재하는가"다. Figma 노드 `3798-167701`(`001-1-4 공동방_저장 여부 묻기`)를 열어 확인한 결과, 모달 본체(`3798-167740`)는 **로컬 프레임**이다 — 둥근 사각형과 텍스트, 그리고 버튼 인스턴스 2개(`Menu/Resource/Action Area/Trailing Content/Button`)로 조립돼 있을 뿐 컴포넌트 인스턴스가 아니다. 딤 오버레이(`3798-167739`)도 로컬 사각형이다. 즉 디자인 시스템이 배포하는 컴포넌트셋이 아니므로 `:core:design-system`에 두지 않는다.

같은 문서가 "그 밖에는 그 화면의 feature에 만든다"를 기본값으로 두고, `:core:common:ui` 승격은 §2.1의 "두 번째 사용처가 생겼을 때"다. 지금 사용처는 이 화면 하나다.

**세 모달을 한 컴포저블로 합치는 근거는 UX-008이다** — spec이 "세 모달은 본문 문구 없이 제목 한 줄과 버튼 2개로 같은 구성을 쓴다"고 명시했다. 어느 모달인지는 `RoomFormDialog` sealed 타입이 들고, 컴포저블은 제목·확인 라벨·확인 콜백만 받는다.

**Alternatives considered**:
- **`:core:design-system`에 `MinoDialog`를 만든다** — 기각. Figma 컴포넌트셋이 아니므로 §1.2의 `[!IMPORTANT]`("거꾸로는 성립하지 않는다")에 정면으로 걸린다. "재사용성이 높아 보인다"는 근거가 되지 않는다고 같은 절이 못박았다.
- **`:core:common:ui`에 만든다** — 기각. 둘 이상의 feature가 **실제로** 쓸 때가 조건인데, 지금 다른 feature는 존재하지도 않는다. §2.1이 예상을 근거로 삼지 말라고 정했다.
- **M3 `AlertDialog`를 화면에서 직접 쓴다** — 보류가 아니라 구현 상세다. 컴포저블 내부에서 M3 `Dialog`를 쓸지 직접 오버레이를 그릴지는 Figma 대조 시 정한다. UX-009(바깥 탭·뒤로가기 = [취소])는 `Dialog`의 `dismissOnBackPress`·`dismissOnClickOutside` 기본 동작과 일치한다.

---

## R-007. 상단 내비게이션은 어디에 만드는가 *(plan 1.0.0)*

**Decision**: **`:core:design-system`에 `MinoTopNavigation`을 신설한다.** 이번 작업이 그 첫 사용처다.

**Rationale**: R-006과 정확히 같은 판정을 반대 결과로 통과한다. 폼 화면의 상단 바(`3798-167736`)는 `Top Navigation/Top Navigation` **인스턴스**이고, `search_design_system` 조회 결과 그 이름은 `MU_Wanted Design System (Community)` 라이브러리의 **`component_set`**이다(`componentKey: 3c17c279…`, 설명 "화면 상단 내비게이션으로 사용합니다"). [`component-asset-placement.md`](../../conventions/component-asset-placement.md) §1.2에 따라 **사용처 개수와 무관하게** 디자인 시스템이 소유한다.

지금 `:core:design-system`에 대응 컴포넌트가 없다는 사실이 이 판정을 바꾸지 않는다 — 판정 대상이 "코드에 있는가"가 아니라 "Figma가 컴포넌트로 배포하는가"이기 때문이다. 이번 폼이 필요로 하는 축(뒤로가기 유무·타이틀·우측 텍스트 액션)만 구현하고, 쓰지 않는 variant는 열지 않는다([M3 컴포넌트 패턴 ADR](../../adr/2026-07-25-design-system-component-m3-pattern.md)).

**Alternatives considered**:
- **폼이 자기 topBar를 직접 조립한다**(`feature-module.md` 4장의 "화면 고유 chrome은 그 화면이 직접 배치") — 기각. 그 조항은 **배치**를 말하지 컴포넌트 소유를 말하지 않는다. 상단 바를 feature에서 조립하면 다음 화면이 같은 것을 다시 조립하고, Figma 컴포넌트셋이 코드에서 N벌로 갈라진다.

---

## R-008. 미리보기 카드와 방 썸네일(색상 + 캐릭터)은 어디에 만드는가 *(plan 1.0.0)*

**Decision**: **`:feature:roomform`이 갖는다.** 미리보기 카드는 `form/component/RoomPreviewCard.kt`가 그리고, 썸네일 13종(12색 + 회색)은 이 모듈의 `res/drawable-{mdpi,xhdpi,xxhdpi}/`에 **WebP**로 둔다.

**Rationale**: `Room Thumbnail_Empty`는 Figma 디자인 시스템의 `component_set`이 맞지만(`componentKey: 4039a656…`), 노드 `3798-167705`를 열어 본 실체는 **둥근 사각형 하나에 래스터 이미지 fill이 채워진 것**이다(`radius` 14, 80×80, variant 축은 방 종류·색상). 즉 이 자산의 내용물은 컴포넌트 구조가 아니라 **이미지 에셋**이고, [`component-asset-placement.md`](../../conventions/component-asset-placement.md) §1.1이 이미지 에셋의 기본 위치를 "그 feature의 `res/drawable-*`"로, §1.2가 "`:core:design-system`은 이미지 에셋을 받지 않는다"를 못박았다. 두 조항을 함께 만족하는 조합은 feature 배치 하나뿐이다.

미리보기 카드 본체(`3798-167703`~`3798-167709`)도 로컬 프레임이라 컴포넌트셋이 아니다. FR-008의 실시간 반영과 안내 문구 대체(`방 이름을 입력해 주세요.` / `어떤 장소들을 모으는 방인가요?`)는 이 화면의 상태에 붙는 동작이므로 feature가 갖는 것이 자연스럽다.

**승격은 예상이 아니라 예정이다.** FR-016이 방 목록 카드·지도 마커·방 뱃지에 같은 대표 색상과 캐릭터를 요구하므로, "두 번째 사용처"는 이미 spec에 적혀 있다. 다만 그 화면들이 아직 없어 §2.1의 시점 기준("두 번째 사용처가 **생겼을 때**")을 만족하지 않는다. 옮길 때의 절차는 [`component-asset-placement.md`](../../conventions/component-asset-placement.md) §2.3이 소유한다 — 이동 대상이 13종×3밀도라 누락이 쉬운 작업이다.

**충돌로 보였던 것은 이미 닫혀 있다.** "Figma 컴포넌트셋은 디자인 시스템이 소유한다"와 "디자인 시스템은 이미지 에셋을 받지 않는다"가 이 자산 하나에서 반대 방향을 가리키는 듯하지만, [래스터 이미지 배치·포맷 ADR](../../adr/2026-08-19-raster-image-placement-and-format.md)이 이미지 에셋의 자리를 이미 정해 두었다. 규약 갱신 대상이 아니라 그 ADR을 근거로 지목하면 되는 문제다.

**Alternatives considered**:
- **`:core:design-system`에 `MinoRoomThumbnail(painter: Painter)`를 만들고 에셋은 호출부가 넘긴다** — 기각. 남는 것이 `Image` + `clip(RoundedCornerShape)` 두 줄이라 컴포넌트로서 얻는 것이 없고, 정작 SSOT여야 할 "색↔캐릭터 대응"은 여전히 호출부에 남는다.
- **캐릭터를 `ImageVector`로 변환해 `MinoIcons`에 넣는다** — 기각. `MinoIcons`는 아이콘 세트이고(README §5), 대상은 일러스트 이미지 fill이라 벡터 변환 대상이 아니다.

---

## R-009. 방 이름 검증을 어디에 두는가 *(plan 1.0.0)*

**Decision**: **`:core:domain`의 `ValidateRoomNameUseCase`.** 결과는 `RoomNameValidation` sealed 타입(`Valid` · `Blank` · `InvalidCharacter`)이고, 길이 상한(15자)은 검증이 아니라 **입력 차단**으로 처리해 UseCase가 아니라 텍스트 필드가 막는다.

**Rationale**: [`core/domain/README.md`](../../../core/domain/README.md) §4의 판단 흐름이 "비즈니스 규칙이 있으면 UseCase"로 갈린다. 허용 문자 판정(FR-004)과 공백만 입력의 취급(EC-001)은 화면이 아니라 **공동방이라는 개념의 규칙**이고, 생성·편집 두 경로가 같은 규칙을 쓴다(FR-013). ViewModel에 두면 같은 문서 §7의 "ViewModel 안에 비즈니스 규칙 작성" 금지에 걸린다.

**길이를 UseCase에 넣지 않는 이유**는 spec이 그것을 오류가 아니라 **입력되지 않음**으로 규정했기 때문이다 — TS-003은 "16번째 글자는 입력되지 않고 카운터는 `15/15`를 유지한다"이고 EC-002는 붙여넣기 초과분을 "상한까지만 반영하고 초과분은 버린다"이다. 오류 상태로 가는 길이 아예 없으므로 판정 결과 타입에 자리가 없다.

`RoomNameValidation`을 `Boolean`이 아니라 sealed로 두는 이유는 UX-002가 오류 **문구**를 필드 아래에 요구하고, 문구 매핑은 presentation의 몫이라 도메인이 리프를 그대로 넘겨야 하기 때문이다.

> **인용 정정 (plan 1.1.1)**: 위 본문이 인용한 TS-003의 `"카운터는 15/15를 유지한다"`는 **spec 3.0.0에서 삭제됐다**(현재 문구는 `"…화면의 방 이름은 15자 그대로다"`). 결정 자체 — 길이를 판정이 아니라 입력 차단으로 다룬다 — 는 그대로 유효하며, 근거가 되는 절반(16번째 글자가 입력되지 않는다)도 살아 있다. 경위는 R-015.

**Alternatives considered**:
- **Repository가 저장 시점에 검증한다** — 기각. FR-004가 입력 즉시(CTA를 누르기 전) 오류를 드러내라고 요구한다(spec §4 가정).
- **정규식을 ViewModel 상수로 둔다** — 기각. 위 §7 금지 조항. 편집 경로가 같은 상수를 다시 참조해야 한다.

---

## R-010. 회색 기본값을 어느 레이어가 적용하는가 *(plan 1.0.0)*

**Decision**: **`:core:domain`의 `CreateRoomUseCase`가 적용한다.** 도메인 모델 `RoomColor`는 12색에 `GRAY`를 더한 13항목 enum이고, 폼의 "미선택"은 `RoomColor?`의 `null`로 표현되며, UseCase가 `color ?: RoomColor.GRAY`로 확정한다.

**Rationale**: [방 색상 팔레트 ADR](../../adr/2026-08-14-room-color-palette-in-design-system.md)이 "회색 기본값은 방을 만드는 시점의 **도메인 규칙**"이라고 명시하고, 디자인 시스템의 `MinoRoomColor`에는 넣지 말라고 정했다. 그 결정을 그대로 따르되 ADR이 열어 둔 자리("회색 기본값이 필요한 자리는 도메인 레이어가 채운다")를 UseCase로 채운다.

`GRAY`가 도메인 enum에는 있고 디자인 시스템 enum에는 없는 비대칭은 의도된 것이다 — 사용자가 **고를 수 있는** 색은 12개이고(FR-006), 방이 **가질 수 있는** 색은 13개다(EC 없음, spec §2.3 "선택하지 않은 방은 회색을 갖는다"). feature의 매핑 함수가 `RoomColor.GRAY → null`로 옮겨 칩 그리드에서 회색이 선택 후보로 나타나지 않게 한다.

**편집 경로에는 이 규칙을 적용하지 않는다.** 편집은 이미 색을 가진 방을 고치는 것이고, 폼에서 색을 해제하는 수단이 없다(spec §4 가정: "이미 선택된 칩을 다시 눌러도 해제되지 않는다"). 그래서 `UpdateRoom`은 UseCase 없이 ViewModel이 Repository를 직접 호출한다 — §4의 4개 조건(단일 API·단순 표시·재사용 없음·비즈니스 규칙 없음)을 모두 만족한다.

**Alternatives considered**:
- **Mapper가 `null → "gray"`로 변환한다** — 기각. 도메인 규칙이 data 레이어에 숨어 도메인 테스트로 검증되지 않는다.
- **`RoomColor`를 12항목으로 두고 방의 색을 `RoomColor?`로 표현한다** — 기각. 회색이 "값이 없음"이 아니라 실제로 마커·뱃지에 칠해지는 색이므로(spec §2.3), 표현할 수 없는 상태가 된다.

---

## R-011. 확인 모달 3종의 표출 상태를 어떻게 모델링하는가 *(plan 1.0.0)*

**Decision**: **`RoomFormUiState.dialog: RoomFormDialog?` 단일 슬롯.** `RoomFormDialog`는 `Save` · `ExitCreate` · `ExitEdit` 세 값을 갖는 sealed 타입이다.

**Rationale**: UX-008이 "둘 이상이 동시에 표출되지 않는다"를 요구한다. 모달마다 `Boolean` 플래그 세 개를 두면 동시 표출이 **표현 가능한 상태**로 남아 규칙을 코드가 아니라 규율로 지켜야 한다. 단일 nullable 슬롯이면 동시 표출이 타입 수준에서 불가능하다.

`SideEffect`로 모달을 띄우지 않는 이유는 모달이 **일회성 이벤트가 아니라 지속 상태**이기 때문이다. 떠 있는 동안 폼 입력을 막고(UX-008) 뒤로가기의 의미를 바꾸므로(UX-009·EC-017), 화면이 매 프레임 읽어야 하는 값이다.

> **근거 정정 (plan 1.1.1)**: 1.0.0은 여기에 "화면 회전·프로세스 사망을 넘어 살아남아야 한다"를 함께 적었으나 **철회한다.** spec에 회전·복원 요구가 없고, 이 설계도 `UiState`를 `SavedStateHandle`에 얹지 않는다(복원되는 것은 Route의 `roomId`뿐). 결론은 바뀌지 않는다 — 위의 "표현 불가능한 상태를 없앤다"만으로 충분하다.

**Alternatives considered**:
- **모달을 Route로 등록해 백스택에 올린다** — 기각. UX-009가 뒤로가기를 [취소]와 같게 처리하라고 요구하는데, 백스택 pop과 [취소]의 결과가 같아 이득이 없고 spec 유저 플로우 3의 완료 조건("폼 화면과 저장 확인 모달은 뒤로가기 대상으로 남지 않는다")과 어긋날 여지가 생긴다.

---

## R-012. 중복 제출 차단(UX-001)을 어떻게 보장하는가 *(plan 1.0.0)*

**Decision**: **`RoomFormUiState.isSubmitting: Boolean`으로 막는다.** `true`인 동안 제출 계열 intent를 ViewModel이 무시하고, 확정 버튼도 비활성으로 그린다. intent 이름의 소유자는 [contracts/room-form-ui.md](./contracts/room-form-ui.md) §2다.

**Rationale**: SC-005가 "중복 생성 0건"을 성과로 뒀다. 상태 플래그를 UiState에 두면 **막는 것과 그리는 것이 같은 값**을 보므로, 버튼이 눌리는 것처럼 보이는데 요청은 안 가는(또는 그 반대의) 어긋남이 생기지 않는다. 디바운스 유틸(`rippleSingleClickable`의 연타 차단)은 빠른 두 번 탭은 막아도 느린 두 번 탭은 막지 못해 SC-005의 기준을 만족하지 못한다.

로딩 표현을 `sealed` 상태가 아니라 `Boolean` 필드로 두는 것은 [UiState isLoading 분리형 ADR](../../adr/2026-07-25-uistate-isloading-over-sealed-status.md)의 방향을 따른 것이다(상태 `Proposed`).

**Alternatives considered**:
- **`Job`을 들고 `isActive`로 판단한다** — 기각. UI가 그 사실을 읽을 수 없어 버튼 상태와 갈라진다.

---

## R-013. 이탈 판정(FR-021 · FR-024)의 기준값을 어디에 두는가 *(plan 1.0.0)*

**Decision**: **생성은 "세 항목이 모두 비어 있는가", 편집은 "진입 시점 스냅샷과 같은가"로 갈린다.** `RoomFormUiState`가 `initial: RoomFormValues?`를 들고, 편집 진입 시 조회 결과로 한 번 채운 뒤 바꾸지 않는다.

**Rationale**: spec §4 가정이 두 판정이 다를 수밖에 없는 이유를 이미 적었다 — 편집 폼은 열릴 때부터 값이 차 있어 생성의 "무언가 입력했는가"를 쓸 수 없다. TS-043("고쳤다가 되돌리면 모달이 뜨지 않는다")이 **비교 대상이 스냅샷**임을 못박는다. 되돌림을 감지하려면 변경 여부 플래그가 아니라 값 비교여야 한다.

`initial`을 nullable로 두는 이유는 생성 경로에 스냅샷이 없기 때문이고, 그것이 곧 두 판정을 가르는 분기 조건이 된다.

**Alternatives considered**:
- **`isDirty: Boolean`을 입력마다 `true`로 세운다** — 기각. TS-043을 만족하지 못한다.
- **생성도 빈 값 스냅샷과 비교한다** — 통합은 가능하나 기각. EC-021("모두 지워 빈 폼으로 되돌린 뒤 뒤로가기 → 즉시 이탈")은 스냅샷 비교가 아니라 **현재 값이 비었는가**로 판정된다. 억지로 합치면 두 규칙이 한 식에 섞여 읽히지 않는다.

---

## R-014. 임시 검증 진입점을 어디에 두는가 *(plan 1.0.0)*

**Decision**: **`:feature:main`에 둔다.** placeholder 탭 화면에서 폼을 열고 결과를 받아 스낵바를 띄우는 배선을 임시로 추가하며, 실제 진입점 feature가 생기면 걷어낸다.

**Rationale**: 진입점 8개와 도착점이 모두 아직 없어, 폼만 만들면 **눌러 볼 수 있는 경로가 하나도 없다.** `:feature:main`은 이미 같은 성격의 배선(`onNavigateToSample`·`onRequestSampleResult`)과 placeholder 탭을 갖고 있어(`MainNavHost.kt`의 주석이 그 임시성을 명시한다) 같은 자리에 붙이는 것이 이 저장소의 기존 관행이다. 또 진입점 7개 중 5개가 결국 방 리스트 탭·홈 탭에 생기므로, 나중에 실제 배선이 들어설 자리와 같다.

`:feature:sample`을 쓰지 않는 이유는 [`feature-module.md`](../../architecture/feature-module.md)가 그 모듈을 "데모용이라 추후 제거될 수 있다"고 적었기 때문이다 — 제거 예정 모듈에 새 의존을 더하지 않는다.

**Alternatives considered**:
- **검증 진입점을 만들지 않고 단위 테스트로만 검증한다** — 기각. FR-009(CTA 라벨)·UX-005(CTA 고정)·UX-008(딤)처럼 눈으로만 확인되는 요구가 남는다. [quickstart.md](./quickstart.md)가 실행 가능한 시나리오를 요구한다.
- **`:app`에 임시 Activity를 만든다** — 기각. `:app`은 그래프 조립만 한다([`dependency-injection.md`](../../conventions/dependency-injection.md)).

---

## R-015. 방 이름 필드에 글자 수 카운터를 두는가 *(plan 1.1.0)*

**Decision**: **두지 않는다.** `MinoTextField`를 확장하지 않고 현재 API 그대로 쓴다. 방 설명은 `MinoTextArea`의 기본 카운터(`showCounter = true`, `maxLength = 30`)를 그대로 쓴다.

**Rationale**: 노드 `2314-95310`(`001-1-1 공동방_기본`의 `Textinput/Textfield` 인스턴스)를 열어 확인한 결과, 구조가 세 단이다 — Heading(`방 이름` + 필수 표기 `*`) · Input(placeholder `방 이름을 입력해 주세요.`) · helper text(`한글·영문·숫자만 입력 가능해요. (공백 포함 15자 이내)`). **카운터 노드가 없다.** 상한 안내는 카운터가 아니라 helper 문구 안에 문장으로 들어 있다.

이 구조는 현재 `MinoTextField`의 `label` · `required` · `placeholder` · `helperText` · `status` 파라미터로 그대로 그려진다. 확장이 필요 없다.

**spec과 어긋난다는 사실을 계획이 삼키지 않는다.** FR-003은 "입력한 글자 수를 `n/15` 형태로 표시한다"를, TS-003은 "카운터는 `15/15`를 유지한다"를 요구한다. 디자인에 그 카운터가 없으므로 **FR-003·TS-003이 갱신 대상**이며, 이 계획은 디자인을 따른다(2026-08-21 사용자 확정). 상한 자체(15자 초과 입력이 반영되지 않음)는 카운터와 무관하게 그대로 지킨다 — TS-003의 나머지 절반이다.

**Alternatives considered**:
- **FR-003대로 카운터를 구현한다** — 기각(사용자 확정). 구현하면 Figma 대조에서 "요소 추가"로 잡히고, [figma-design-fidelity §4](../../conventions/figma-design-fidelity.md)가 요소 추가를 불일치로 판정한다.
- **`MinoTextField`에 카운터를 옵션으로 열어 두되 이 화면은 끈다** — 기각. 호출부 없는 파라미터를 미리 만들지 않는다([M3 컴포넌트 패턴 ADR](../../adr/2026-07-25-design-system-component-m3-pattern.md)).

> **추인 (plan 1.1.1)**: spec 3.0.0이 이 결정을 받아들여 FR-003에서 카운터 요구를 걷어냈다. 위 본문의 "FR-003·TS-003이 갱신 대상"은 **당시 상태의 기록**이며, 지금은 spec과 이 결정이 일치한다. 함께 TS-003·TS-018·UX-007·SC-002가 정정되고 TS-045(방 이름에는 카운터가 없다)가 신설됐다. spec은 PRD [SYS-001] Flow A가 카운터를 방 설명에만 붙였다는 점도 근거로 실었다.

---

## R-016. 생성·편집 폼의 상단 타이틀 문구 *(plan 1.1.0)*

**Decision**: 생성은 **`공동방 만들기`**, 편집은 **`방 편집`**.

**Rationale**: 두 노드를 각각 열어 확인했다.

| 화면 | 노드 | 좌측 | 타이틀 | 우측 |
|---|---|---|---|---|
| 생성 | `2314-95336` | 뒤로가기 `<` | `공동방 만들기` | `건너뛰기` |
| 편집 | `2542-125957` | 뒤로가기 `<` | `방 편집` | 비어 있음 |

편집 화면의 우측 슬롯이 **자리는 차지하되 내용이 비어 있는** 것이 FR-017(건너뛰기는 온보딩 전용)과 맞는다. 생성 화면 프레임에 뒤로가기와 건너뛰기가 함께 그려져 있는 것은 명세 보드가 두 조건을 한 장에 표기한 것이며, 노출 조건은 spec 유저 플로우 5가 이미 확정했다(FR-017·FR-022).

**spec §4의 가정이 낡았다.** "편집 화면의 상단 타이틀은 생성과 동일하게 `공동방 만들기`로 그려져 있으나 … 이 스펙은 타이틀 문구를 확정하지 않는다"는 서술은 그 뒤의 디자인 개정으로 무효가 됐다. 갱신 대상으로 보고한다.

**Alternatives considered**: 없다. 디자인이 확정한 문자열이라 선택지가 없다.

> **추인 (plan 1.1.1)**: spec 3.0.0이 이 결정을 **FR-025**("생성 진입 시 상단 타이틀을 `공동방 만들기`로, 편집 진입 시 `방 편집`으로 표시한다")로 승격하고 TS-044를 신설했다. §4 가정에서는 제거됐다. 타이틀은 이제 가정이 아니라 요구사항이므로, 이 결정을 구현하는 작업은 FR-025를 근거로 지목한다.

---

## R-017. 회색(미선택) 방의 썸네일 *(plan 1.1.0)*

**Decision**: **`Room Thumbnail_Empty`의 `my room` variant가 회색 썸네일이다.** 별도 에셋을 만들지 않는다. export 대상은 이 컴포넌트셋의 **13개 variant 전부**다.

**Rationale**: 디자인 시스템 라이브러리 파일(`hkSOCt4kOfyaVWdxybTicF`)의 컴포넌트셋 `16765-22588`을 열었다. variant는 13개이고, 12개는 팔레트 색 이름(`red` · `red orange` · `orange` · `lime` · `green` · `cyan` · `violet` · `pink` · `blue` · `brown` · `light blue` · `purple`)이며 나머지 하나가 `my room`이다. 렌더 결과를 확인하니 **`my room`이 회색 배경에 회색 캐릭터**다.

이름은 개인방(`내 장소`) 관점에서 붙었지만 실체는 회색이므로, spec §2.3의 "선택하지 않은 방은 회색을 갖는다"와 §4 가정의 "회색이 적용된 방에도 캐릭터는 붙는다"가 이 variant로 함께 충족된다. 1.0.0이 "회색에 대응하는 캐릭터가 무엇인지 확인되지 않았다"고 남긴 자리가 이것으로 닫힌다.

`RoomColor.GRAY` → `my room` 에셋 매핑은 feature가 소유한다 — 도메인 값과 UI 에셋을 잇는 자리는 [방 색상 팔레트 ADR](../../adr/2026-08-14-room-color-palette-in-design-system.md)이 정한 대로 feature다.

**Alternatives considered**:
- **회색 썸네일을 코드에서 합성한다**(회색 배경 + 공용 캐릭터) — 기각. 13번째 variant가 이미 있다. 합성하면 디자인이 회색을 바꿀 때 12색과 다른 경로로 갈라진다.
- **미선택 상태에서 썸네일을 비운다** — 기각. spec §4 가정이 회색 방에도 캐릭터가 붙는다고 확정했다.

---

## R-018. mock의 색상 식별자 표기 *(plan 1.1.0)*

**Decision**: [R-003](#r-003-swagger-계약이-spec과-어긋나는-세-지점-plan-100)이 제안한 소문자 스네이크 식별자를 그대로 확정한다 — `"red"` · `"red_orange"` · … · `"gray"`.

**Rationale**: 1.0.0에서 "서버 확정 전"을 이유로 미확정으로 남겼으나, mock 구간에서는 서버 확정을 기다릴 이유가 없다. 표기가 바뀔 때 고칠 곳이 `RoomMapper` 한 파일이라 되돌리는 비용이 사실상 없고, 그동안 이 값이 도메인·UI로 새어 나가지 않는다([contracts/room-api.md](./contracts/room-api.md) §2 — 링크는 plan 2.0.0에서 갱신됐다).

`GRAY`의 식별자를 `"gray"`로 두는 것은 R-017과 별개다 — 에셋 이름은 `my room`이지만 그것은 Figma variant 이름이지 서버 계약이 아니다. 도메인 값 이름을 따른다.

> **후속 (plan 2.1.0 → ~~정정~~ plan 2.2.0)**: 2.1.0은 `GRAY`의 서버 어휘가 `"grey"`(영국식)로 확정됐다고 적었으나 **틀렸다.** 서버가 배포한 `enum`은 `"gray"`이며, **이 항목의 원래 판정이 13색 전부에서 그대로 맞았다** → [R-030](#r-030-서버-enum-배포로-색-어휘가-확정됐다--grey-판정을-되돌린다-plan-220). 위 문단의 "도메인 값 이름을 따른다"도 그대로 성립한다.

**Alternatives considered**: R-003의 기각 이력을 참조한다. 이번 항목은 그 결정의 확정일 뿐 새 후보를 검토하지 않았다.

---

## R-019. 방 설명 필드의 편집 상태를 누가 소유하는가 *(plan 1.2.0)*

**Decision**: **`RoomFormRoute`가 `TextFieldState`를 소유한다.** 텍스트 변화를 `DescriptionChanged`로 ViewModel에 전달하고, 편집 진입 초기값은 `initial`이 처음 채워질 때 한 번만 주입한다. `RoomFormScreen`은 그것을 파라미터로 받아 stateless를 유지한다. 전달 수단과 생성 호출은 구현이 고르며, 계약은 [contracts/room-form-ui.md](./contracts/room-form-ui.md) §4가 소유한다.

**Rationale**: 1.1.1까지 이 지점이 비어 있었다. `MinoTextArea`는 `state: TextFieldState`를 받는데 `RoomFormUiState.values.description`은 `String`이라, 둘을 잇는 주인이 정해지지 않은 채 계약이 "`maxLength = 30`으로 그대로 받는다"로 덮여 있었다.

Route가 드는 것이 세 후보 중 가장 적게 잃는다. **디자인 시스템을 건드리지 않고**, 상한(`InputTransformation.maxLength`)과 카운터가 컴포넌트 계약에 그대로 남는다. `TextFieldState`는 편집 버퍼, `UiState`는 도메인 값이라는 역할 분담은 Compose가 의도한 형태이기도 하다.

**대가는 두 가지이고 감추지 않는다.**

- **초기값 주입에 타이밍 결합이 생긴다.** 편집 폼은 값을 서버에서 읽어 오므로([R-005](#r-005-편집-진입-시-기존-값을-어떻게-채우는가-plan-100)) 조회 완료 시점에 한 번 주입해야 한다. 여러 번 주입하면 사용자가 고치던 내용을 덮어쓴다. 재시도와 프로세스 사망 복원에서의 가드는 계약이 못박았다.
- **두 입력 필드가 비대칭이 된다.** 방 이름은 ViewModel이 15자로 자르고, 방 설명은 컴포넌트가 30자로 자른다. 원인은 `MinoTextField`에 `maxLength`가 없고 `MinoTextArea`에만 있다는 디자인 시스템 API의 차이다. 계약에 그 사실을 적어 두어 구현이 양쪽 다 자르거나 한쪽도 안 자르는 일이 없게 한다.

FR-024(진입 시점 대비 변경 판정)와 FR-008(미리보기 실시간 반영)은 둘 다 `UiState.values`를 읽으므로 이 결정에 영향을 받지 않는다.

> **근거 갱신 (plan 1.3.0)**: 위 Rationale이 이 선택의 장점으로 든 **"디자인 시스템을 건드리지 않고"**는 [R-022](#r-022-방-설명의-글자-수를-어느-단위로-세는가-plan-130)로 더 이상 사실이 아니다. 상한은 이제 `InputTransformation.maxLength`가 아니라 `MaxGraphemeLengthTransformation`이 건다. **결론(Route 소유)은 그대로 선다** — 나머지 두 근거(컴포넌트 계약 유지, Compose가 의도한 역할 분담)가 남아 있고, 아래 기각한 대안들의 비용도 변하지 않았다. 다만 "`InputTransformation.maxLength`를 못 써 다시 구현해야 한다"는 기각 근거는 이제 성립하지 않는다 — 어차피 직접 구현했다.

**Alternatives considered**:
- **`MinoTextArea`에 `value: String` 오버로드를 추가한다** — 기각. 두 필드가 대칭이 되고 `UiState`가 유일한 원천이 되는 이점이 있으나, `BasicTextField`의 state 기반 이점(IME 조합 처리·undo)을 잃고 `InputTransformation.maxLength`를 못 써 상한과 카운터를 오버로드에서 다시 구현해야 한다. Compose가 state 기반으로 가는 방향과도 역행한다.
- **ViewModel이 `TextFieldState`를 들고 `UiState`에 넣는다** — 기각. 가변 Compose 타입이 `data class`에 들어가 `copy`·`equals`의 의미가 깨지고, `MviContainer`의 `updateState { copy(...) }` 모델과 충돌한다([`core/common/android/README.md`](../../../core/common/android/README.md) §2).

---

## R-020. `Room.type`을 도메인 모델에 두는가 *(plan 1.2.0)*

**Decision**: **두지 않는다.** `Room`에서 `type` 필드를, `model/`에서 `RoomType` enum을, `RoomResponse`에서 대응 필드를 함께 뺀다.

**Rationale**: 1.0.0이 `type`을 넣으며 근거로 든 FR-014·EC-013(개인방은 편집 대상이 아니다)은 **방 상세 더보기 메뉴의 요구**이고 [§범위 경계](./plan.md)가 이번 범위 밖으로 둔 항목이다. 그 결과 이 feature의 `UiState`·`Intent`·`RoomFormViewModel` 어디도 `type`을 읽지 않는다. [`core/domain/README.md`](../../../core/domain/README.md) §5가 "서버 스펙에만 존재하는 필드는 도메인 모델에 포함하지 않는다"를 정했으므로 빼는 것이 규약대로다.

**DTO에서도 뺀다.** `ignoreUnknownKeys = true`가 걸려 있어 서버가 `type`을 보내도 파싱이 깨지지 않는다([`core/data/README.md`](../../../core/data/README.md) §4). 매핑되지 않을 필드를 DTO에만 남기면 "왜 안 쓰는가"라는 질문이 코드에 남는다.

**필요해지는 시점은 예측 가능하다** — 방 상세가 더보기 메뉴를 그릴 때다. 그 feature가 그때 `Room`에 필드를 더하면 되고, 도메인 모델에 필드를 추가하는 것은 되돌리기 어려운 결정이 아니다.

**Alternatives considered**:
- **남기고 개인방 `roomId`로 편집 폼이 열렸을 때의 방어를 더한다** — 기각. spec은 그 경로를 진입점 없음(FR-014)으로 닫아 두었으므로, 방어를 넣는 것은 spec에 없는 요구사항 신설이다([헌법 원칙 IV](../../constitution.md)).
- **필드는 남기고 근거만 "DTO 매핑 완전성"으로 바꾼다** — 기각. 쓰이지 않는 필드를 도메인에 두는 근거로는 약하고, 위 README §5와 정면으로 어긋난다.

---

## R-021. 방 이름의 허용 문자에 자모 단독이 드는가 *(plan 1.2.0)*

**Decision**: **든다.** `ValidateRoomNameUseCase`는 자모 단독(`ㄱ`·`ㅏ`)을 한글로 보아 `Valid`로 판정한다.

**Rationale**: 1.1.1이 이 판단을 `[TBD]`로 남겼던 이유는 **저장 허용값을 넓히는 결정을 설계가 임의로 내릴 수 없기 때문**이었다. spec 3.1.0이 FR-004를 `한글(완성형·자모)`로 고치고 EC-025를 신설해 그 자리를 채웠으므로 이제 설계는 spec을 따르기만 하면 된다.

근거의 핵심은 FR-004가 오류 판정을 **입력 즉시** 하도록 정했다는 점이다. 조합이 끝나기를 기다릴 수 없으므로, 자모를 배제하면 IME로 한글을 치는 동안 매 글자 오류가 번쩍인다. `value: String` API로는 조합 구간을 알 수 없어 억제할 방법도 없다.

**Alternatives considered**: 1.1.1의 `[TBD]`가 적어 둔 두 갈래(불허 + 조합 중 억제 / 불허 + 판정 시점 이연)는 spec 3.1.0의 §5 답변이 기각했다. 이 항목은 그 결정의 설계 반영일 뿐 새 후보를 검토하지 않았다.

---

## R-022. 방 설명의 글자 수를 어느 단위로 세는가 *(plan 1.3.0)*

**Decision**: **`MinoTextArea`의 상한과 카운터를 grapheme 단위로 고쳤다.** `:core:design-system`의 실제 코드 변경이며, 이 계획이 문서로만 남긴 것이 아니다.

**Rationale**: 1.2.0이 이 지점을 `[TBD]`로 남겼다 — `MinoTextArea`가 `InputTransformation.maxLength`와 `state.text.length`로, 즉 **UTF-16 코드 유닛**으로 세는데 spec §4 가정은 "사용자가 보는 문자 단위"를 요구했기 때문이다.

**방 이름은 이 문제가 구조적으로 없다.** FR-004의 허용 문자(한글 완성형·자모·영문·숫자·공백)가 전부 BMP 안이라 코드 유닛과 화면 글자가 항상 1:1이다. 우연이 아니라 허용 문자 집합이 보장하는 성질이므로, 방 이름을 자르는 ViewModel은 `length`로 세도 된다 — FR-004가 넓어지면 그때 재검토한다(열린 항목 G). 문제는 **문자 종류 제한이 없는**(EC-006) 방 설명뿐이다.

| 입력 | 화면 글자 | 코드 유닛 | 고치기 전 카운터 |
|---|---|---|---|
| `팀 회식 🍺🍻` | 7 | 9 | `9/30` |
| `🍺` × 15 | 15 | 30 | `30/30` — 여기서 막힘 |
| `👨‍👩‍👧` × 3 | 3 | 24 | `24/30` |

경계 동작이 더 나빴다. `InputTransformation.maxLength`는 초과를 만드는 변경을 **통째로 되돌리므로**, 29/30에서 2코드 유닛짜리 이모지를 넣으려 하면 입력이 잘려 들어가는 게 아니라 **아무 일도 일어나지 않았다.**

**상태 모델과 세는 단위는 다른 축이다.** `MinoTextArea`를 `MinoTextField`처럼 `value: String` 기반으로 바꾸는 것으로는 이 문제가 풀리지 않는다 — Kotlin의 `String.length`도 코드 유닛이기 때문이다. 그래서 R-019(Route 소유)를 유지한 채 세는 단위만 고쳤다.

**코드포인트로는 부족하다.** 서로게이트 쌍은 잡히지만 `👨‍👩‍👧`는 이모지 3개를 ZWJ 2개가 잇는 시퀀스라 코드포인트로 5다. UAX #29의 grapheme cluster여야 하며, `android.icu.text.BreakIterator.getCharacterInstance()`가 그 단위를 준다(`minSdk` 29라 사용 가능).

**API 표면은 바뀌지 않았다.** `maxLength`·`showCounter` 파라미터도, `TextFieldState`도, `MinoTextInputDefaults`·`TextInputTokens`도 그대로다. 신규 심볼은 둘 다 `internal`이고 기존 호출부(카탈로그 프리뷰)도 손대지 않았다.

**규약을 어디까지 통과했는지 정확히 적는다.** [M3 컴포넌트 패턴 ADR](../../adr/2026-07-25-design-system-component-m3-pattern.md)과 [design-system README §6.1](../../../core/design-system/README.md#61-컴포넌트-구현-패턴--material3-관례)은 이 변경을 **규율하지 않는다** — 그 조항들이 다루는 축(`Defaults`·`Colors`·컴포넌트 토큰 구성, 파라미터를 언제 늘리는가)을 하나도 건드리지 않았기 때문이다. 통과했다기보다 해당이 없다. 이 변경이 실제로 걸리는 축은 **표면은 그대로인데 렌더 값이 달라지는 동작 변경**이고, 그것을 규정한 문서는 이 저장소에 없다. 소비자가 컴파일로 알 수 없는 종류의 변경이라는 뜻이며, 지금은 소비자가 카탈로그 프리뷰뿐이라 영향이 없다.

**접근성 시맨틱은 상류와 같게 유지했다.** `InputTransformation.maxLength`는 `applySemantics`로 `maxTextLength`를 함께 노출한다. 그것을 걷어내면서 빠뜨리면 TalkBack·자동입력이 상한을 읽지 못하므로, `MaxGraphemeLengthTransformation`이 같은 시맨틱을 다시 붙인다.

**이미 상한을 넘은 값이 들어와도 필드가 잠기지 않는다.** 상류 `maxLength`는 "결과가 초과인가"만 보므로 서버가 30 grapheme을 넘는 설명을 돌려주면 **지우는 편집까지 되돌아가** 편집이 불가능해진다. 열린 항목 D(서버 상한 미확정)가 살아 있는 동안 실제로 열려 있는 경로라, 길이가 **늘어난** 경우에만 되돌리도록 좁혔다.

**다른 화면도 함께 나아진다.** PRD가 코멘트 입력에 `N/200` 카운터를 요구하면서 특수문자를 허용하므로, 그 화면이 `MinoTextArea`를 쓰면 같은 문제를 그대로 겪었을 것이다.

**남는 것 — 헬퍼의 자리.** `graphemeLength`는 `:core:design-system`의 `util/text/`에 `internal`로 두었다. 방 이름은 위 이유로 승격이 필요 없고, 두 번째 사용처도 아직 코드로 없다. 승격 조건과 그때 판정할 것은 열린 항목 G가 추적한다.

**검증**: `:app:assembleQaDebug`·`ktlintCheck` 통과.

**디자인 대조는 대상이 아니다.** [figma-design-fidelity §4](../../conventions/figma-design-fidelity.md)가 대조를 요구하는 세 축(변수 전수·미바인딩 치수와 색·요소 구조) 중 걸리는 것이 없다 — 토큰·치수·색·Composable 트리가 모두 무변경이고, 카운터가 그리는 **숫자**는 런타임 상태이지 디자인 축이 아니다. 같은 문서 §6이 "게이트 통과를 디자인 일치의 근거로 삼지 않는다"고 못박았으므로, 위 빌드 통과를 그 근거로 적지 않는다.

**이 동작을 검증하는 자동 장치가 없다.** JVM 단위 테스트는 `android.icu`가 `android.jar` 스텁이라 불가능하고(이 저장소에 Robolectric이 없다), `:core:design-system`에는 `androidTest` 소스셋 자체가 없다. 즉 "계측 테스트가 올바른 자리"라고 말할 수는 있어도 **그 자리는 아직 존재하지 않는다.** 세는 값이 실제로 맞는지는 실행해 봐야 확인되며, 계측 테스트 신설을 후속 작업으로 남긴다.

**Alternatives considered**:
- **편차를 안고 spec §4 가정을 방 설명에 한해 완화한다** — 기각. 30자짜리 짧은 필드라 피해가 작다고 볼 수 있으나, 방 설명에 이모지를 넣는 것은 흔한 사용 패턴이고 경계에서 입력이 통째로 씹히는 동작은 설명하기 어렵다.
- **`MinoTextArea`를 `value: String` 기반으로 바꾼다** — 기각. 세는 단위 문제가 그대로 남고(위 참조), R-019가 이미 기각한 선택지다.
- **코드포인트로 센다** — 기각. ZWJ 시퀀스를 놓친다.
- **`java.text.BreakIterator`를 써서 `:core:common:kotlin`에 둔다** — 기각(보류). JVM 단위 테스트가 가능해지는 것이 큰 이점이나 **함정이 있다**: Android의 `java.text.BreakIterator`는 ICU가 뒤를 받치지만 **데스크톱 JVM은 JDK 자체 규칙이라 ZWJ 이모지 시퀀스를 한 클러스터로 묶지 않는다.** 그대로 옮기면 이 변경을 유발한 바로 그 케이스에서 JVM 테스트가 Android와 다른 값을 기대값으로 굳힌다. 승격할 때 이 차이를 먼저 판정해야 한다 — 조건은 [plan.md](./plan.md) §복잡도 추적 G가 추적한다.

---

## R-023. 편집 보드의 방 이름 필드 불일치를 재대조한다 *(plan 1.4.0)*

**Decision**: **불일치가 해소됐음을 확인하고, 이 계획의 `MinoTextField` 결정을 그대로 유지한다.** R-015의 결정은 바뀌지 않고 근거만 두 보드로 넓어진다. 사실 서술의 소유자는 [contracts/design-system-additions.md](./contracts/design-system-additions.md) §3 말미이며, 열린 항목 C가 닫힌다.

**Rationale**: 1.1.1이 남긴 열린 항목 C는 "편집 보드가 방 이름을 생성 보드와 다른 컴포넌트로 그렸다"였고, 닫는 조건은 **디자이너 확인 후 어느 보드를 갱신할지 갈리는 것**이었다. 2026-08-24 재대조 결과 **편집 명세 보드가 생성 보드에 맞춰 갱신됐다** — C의 두 갈래 중 "생성 보드가 의도"인 쪽이다.

| 노드 | 방 이름 | 방 설명 | 상단 타이틀 | CTA |
|---|---|---|---|---|
| `2314-95301` 생성 명세 보드 | `Textinput/Textfield`(`2314-95310`) · 카운터 없음 | `Textinput/Textarea`(`2314-95311`) · `0/30` | `공동방 만들기` | `방 생성하기`(비활성) |
| `2542-125922` 편집 명세 보드 | **`Textinput/Textfield`**(`4170-140432`) · 카운터 없음 | `Textinput/Textarea`(`4170-140433`) · `0/30` | `방 편집` | `방 편집 완료` |
| `2792-151339` User Flow 보드 | `Textinput/Textarea` · `2/15` 카운터 | `Textinput/Textarea` | `공동방 만들기` | `방 편집 완료` |

편집 명세 보드의 두 필드는 **새 인스턴스로 교체됐다**(`4170-*` 대역). 좌표(y=158·288)와 크기(335×100·335×108)가 생성 보드와 같고, `4170-140432`의 세 단 구조와 문자열(`방 이름` + `*` · placeholder `방 이름을 입력해 주세요.` · helper `한글·영문·숫자만 입력 가능해요. (공백 포함 15자 이내)`)이 R-015가 기록한 `2314-95310`과 일치한다. **카운터 노드는 여전히 없다.**

**남은 불일치는 User Flow 보드 하나이고, 그 보드가 낡았다는 판정은 이미 있다.** `2792-151339`는 편집 화면인데 상단 타이틀이 `공동방 만들기`라 FR-025와도 어긋난다. [spec.md](./spec.md) §5가 R-016 확정 당시 "`2792-150324`는 그 뒤의 디자인 개정으로 낡았다"고 적어 둔 것의 두 번째 증거이므로, 이 보드를 근거로 결정을 뒤집지 않는다.

**설계는 한 줄도 바뀌지 않는다.** 방 이름 = `MinoTextField`(카운터 없음) · 방 설명 = `MinoTextArea`(`maxLength = 30` · `showCounter = true`)이라는 [contracts/room-form-ui.md](./contracts/room-form-ui.md) §1의 트리, 상한을 자르는 주체가 갈린다는 R-019, grapheme 단위라는 R-022가 모두 그대로다. 프로덕션 코드도 손대지 않는다 — 1.3.0이 낸 `MinoTextArea` 델타로 이미 충분하다.

**대조하면서 함께 드러난 것 — FR-013의 Figma 근거가 낡은 보드에만 남는다.** 갱신된 `2542-125922`는 이제 **빈 폼**을 그린다(placeholder · `0/30` · 대표 색상 `red` 선택 · CTA 활성). 컴포넌트를 갈아 끼우며 새 인스턴스의 기본 콘텐츠가 남은 것으로 보이나 그것은 추론이고, **관찰된 사실은 편집 명세 보드에 기존 값이 채워져 있지 않다는 것**이다. 그 결과 두 가지가 생긴다.

- FR-013("기존 값으로 채운 상태로 열고")과 TS-018(`3/30`)의 Figma 근거는 `2792-151339` 하나뿐이 되었다. PRD [SYS-001] Flow D가 같은 요구를 문장으로 받치고 있어 요구사항이 흔들리지는 않는다.
- 빈 이름에 CTA가 활성으로 그려져 FR-007·TS-005("이름을 지우면 CTA 비활성")와 어긋난다. 폼 상태 하나를 정지 화면으로 그린 것이라 요구사항 판정에는 쓰이지 않는다.

**둘 다 spec의 근거 정리 문제이고 이 계획의 설계 판단이 아니다.** 계획이 삼키지 않고 여기 적어 두며, 처리는 `/mino-spec`이 판정한다.

> **추인 (plan 1.4.1)**: spec 3.2.0이 두 관찰을 §4 가정 3건으로 받았다 — ① FR-013의 근거를 PRD [SYS-001] Flow D로 명시하고 기존 값 채움을 **유지**한다, ② 명세 보드와 User Flow 보드가 어긋나면 명세 보드를 따른다, ③ 빈 이름에 CTA가 활성으로 그려진 표기는 FR-007을 뒤집지 않는다. 함께 FR-009의 근거가 `2792-151339`에서 명세 보드 `2542-125922`로 교체됐다. **세 가정이 모두 이 계획이 이미 전제한 것을 추인하므로 설계 변경은 없다** — 초기값 주입은 [contracts/room-form-ui.md](./contracts/room-form-ui.md) §4, CTA 조건은 [data-model.md](./data-model.md) §CTA 활성 조건이 그대로 유효하다. 남은 것은 spec 체크리스트의 디자인 확인 대상 #4·#5(값이 채워진 편집 화면 표기 · `2792-151339` 갱신)이며, 둘 다 디자이너 요청 사항이라 이 계획을 막지 않는다.

**Alternatives considered**:
- **`2792-151339`가 남았으니 C를 계속 열어 둔다** — 기각. C가 걸었던 것은 **어느 컴포넌트가 의도인가**이고 그 답이 나왔다. 낡은 User Flow 보드의 정리는 이 계획이 기다릴 대상이 아니며, 기다리면 근거 없이 열린 항목이 무기한 남는다.
- **편집 보드가 빈 폼을 그리므로 FR-013을 재검토한다** — 기각. 위 이유로 spec의 몫이고, PRD가 문장으로 받치고 있어 설계가 흔들리지 않는다.
- **양쪽이 같아졌으니 R-015를 고쳐 쓴다** — 기각. 기존 결정을 덮어쓰지 않는다는 이 문서의 누적 규칙에 따라 새 항목으로 남긴다.

---

## R-024. 실서버가 붙었다 — mock 데이터 레이어를 걷어낸다 *(plan 2.0.0)*

**Decision**: **[R-002](#r-002-서버가-없는-상태에서-데이터-레이어를-어디까지-진짜로-만드는가-plan-100)를 뒤집는다.** `RoomApiService`를 만들고 `RoomRemoteDataSourceImpl`이 그것을 위임하며, `RoomMockRemoteDataSourceImpl`과 `RoomMockStore`를 지운다. 남는 것은 DTO·Mapper·`RoomRepositoryImpl`·`RoomRemoteDataSource` 인터페이스로, **넷 다 한 글자도 바뀌지 않는다.**

**Rationale**: [Team MINO API `1.0.0`](https://api.gguk.org/api-docs-json)이 배포되어 있고(조회: 2026-08-27T21:45:27+09:00) `POST /api/v1/rooms` · `PATCH /api/v1/rooms/{roomId}` · `GET /api/v1/rooms/{roomId}` 셋이 모두 있다. R-002가 mock을 택한 전제("서버가 없다")가 사라졌으므로 결정도 따라 사라진다.

**R-002가 값을 했다는 것이 이 항목의 근거이기도 하다.** 그 결정이 노린 것은 "서버가 붙을 때 바뀌는 곳을 `@Binds` 한 줄과 새 파일 한 개로 좁힌다"였고, 실제로 걷어내는 비용이 **파일 2개 삭제 · 파일 2개 신규 · 바인딩 1줄**이었다. DTO·Mapper·Repository를 fake로 두었다면 이번에 처음 작성됐을 것이다.

**mock을 남겨 둘 스위치를 두지 않는다.** 프로덕션 코드에 출처를 고르는 분기를 남기면 그 분기가 검증되지 않은 채 굳는다 — R-002가 실패 주입 스위치를 거부한 것과 같은 이유다. 실패 경로 검증은 Fake `RoomRepository`를 쓰는 ViewModel 테스트가 계속 소유한다.

**Alternatives considered**:
- **mock을 flavor로 남기고 qa에서만 실서버를 쓴다** — 기각. `RoomMockStore`가 프로세스 수명 저장소라 어차피 재실행마다 사라지고, 두 출처를 유지하는 비용이 실서버 하나를 쓰는 것보다 크다. 서버가 닿지 않을 때의 대비는 mock이 아니라 실패 처리(UX-003)가 이미 갖고 있다.
- **`RoomRemoteDataSource` 인터페이스를 걷어내고 `RepositoryImpl`이 `ApiService`를 직접 쓴다** — 기각. [`core/data/README.md`](../../../core/data/README.md) §5가 인터페이스·구현체 쌍을 규칙으로 못박았다. 지금 지우면 로컬 캐시가 붙을 때 되살려야 한다.

> **[R-002](#r-002-서버가-없는-상태에서-데이터-레이어를-어디까지-진짜로-만드는가-plan-100)는 ~~재검토됨(plan 2.0.0)~~** — `DataSource` 구현을 mock으로 채운다는 부분이 이 항목으로 대체됐다. **ADR 승격 후보에서도 내린다**: 그 결정이 다른 feature를 구속하려면 "서버가 없는 동안"이라는 전제가 유지돼야 하는데, 전제가 사라졌다.

---

## R-025. `{ data }` 봉투를 어느 레이어가 벗기는가 *(plan 2.0.0)*

**Decision**: **`ApiService`가 벗긴다.** 제네릭 `MinoResponse<T>`(`network/dto/response/`)로 `client.post(...).body<MinoResponse<RoomResponse>>().data`로 즉시 푼다. `DataSource`·`RepositoryImpl`·Mapper·도메인은 봉투의 존재를 모른다.

> **사실 정정 (plan 2.1.0)**: 이 항목을 쓸 때 **같은 결정이 이미 ADR로 존재한다는 사실을 놓쳤다.** `shared-link-receiver`가 먼저 `GET /api/v1/rooms`를 붙이며 [응답 봉투 ADR](../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)(2026-08-27, Accepted)로 정했고, 그 ADR의 §결과가 "`group-room-form`이 mock을 걷어내고 실서버로 전환할 때 같은 타입을 쓴다"고 이 feature를 직접 지목한다. **결론과 근거는 아래와 같지만 두 가지가 바뀐다** — 타입 이름은 `ApiEnvelope<T>`가 아니라 **`MinoResponse<T>`**이고(ADR이 `MinoDomainException`·`MinoIdentityProofPlugin`의 접두어를 따랐다), 이 계획은 그 타입을 **만들지 않고 쓰기만 한다.** 아래의 ADR 승격 제안도 철회한다 — 승격된 결정이 이미 있다.

**Rationale**: [`core/data/README.md`](../../../core/data/README.md) §2의 레이어 흐름이 `ApiService`를 "엔드포인트를 호출하는" 자리로, `DataSource`를 "출처를 추상화하는" 자리로 갈라 두었다. 봉투는 **HTTP 전송 형식**이지 데이터가 아니므로 전송을 아는 가장 안쪽 레이어에서 끝나야 한다. 같은 문서 §5가 DataSource의 책임을 "데이터 출처 호출만. 변환·비즈니스 로직 없음"으로 못박은 것도 같은 방향이다.

제네릭 하나로 두는 것은 이 feature만의 판단이 아니다 — 서버의 성공 응답 **전부**가 같은 봉투를 쓴다(`api-docs-json` 25개 오퍼레이션). 엔드포인트마다 `RoomEnvelope`·`PinEnvelope`를 만들면 같은 모양의 클래스가 API 수만큼 늘어난다.

~~**이 결정은 다른 feature를 구속한다** — 앞으로 이 서버를 부르는 모든 `ApiService`가 같은 자리에서 같은 타입으로 봉투를 푼다. ADR 승격 후보다.~~ → 위 정정대로 ADR이 이미 있다.

**Alternatives considered**:
- **Ktor `ResponseObserver`/커스텀 `ContentConverter`로 전역에서 봉투를 벗긴다** — 기각. 봉투가 없는 응답(있을 수 있다)이나 봉투 자체를 읽어야 하는 경우가 생기면 전역 변환을 되돌릴 수 없고, 무엇보다 `body<RoomResponse>()`라고 쓰인 코드가 실제로는 다른 JSON을 파싱한다는 사실이 호출부에서 보이지 않는다.
- **`RoomResponse`를 봉투째 선언한다(`data class RoomEnvelope(val data: RoomResponse)`)** — 기각. DTO가 25번 복제된다.
- **`DataSourceImpl`이 벗긴다** — 기각. DataSource가 전송 형식을 알게 되어 §5의 책임 규칙을 어긴다.

---

## R-026. 색·설명 계약을 실제 API로 재대조한다 *(plan 2.0.0)*

**Decision**: **[R-003](#r-003-swagger-계약이-spec과-어긋나는-세-지점-plan-100)의 판정(spec을 따르고 어긋남은 서버팀에 제기)을 유지한다.** [R-018](#r-018-mock의-색상-식별자-표기-plan-110)의 소문자 스네이크 식별자도 유지한다. 어긋나는 항목만 실제 문서로 갱신했고, 소유자는 [contracts/room-api.md](./contracts/room-api.md) §2다.

**Rationale**: 초안에서 확정 문서로 바뀌면서 셋 중 하나(`color`가 5색 hex)는 해소 방향으로 움직였다 — 이제 `Room.color`의 예시가 `"black"`이라 **문자열 색 이름**을 받는 쪽으로 읽힌다. 그런데 같은 문서가 `color.maxLength: 7`을 걸어 두었고, 하필 그 값이 `#RRGGBB`의 길이와 같으며, 팔레트에서 두 단어인 색 둘(`red_orange`·`light_blue`)만 상한을 넘는다. `InvitationPreview.room.color`의 예시는 여전히 hex(`"#FF6B6B"`)다.

**즉 서버가 무엇을 받는지가 문서에서 갈리지 않는다.** `color`에 `enum`이 없어 어휘를 읽을 수도 없다. 이 상태에서 표현을 바꾸는 것은 추정이므로, R-003이 세운 원칙 — **명세를 따르고 어긋남은 제기한다**([헌법 원칙 IV](../../constitution.md)) — 을 그대로 적용한다. 되돌리는 비용이 `RoomMapper` 한 파일이라는 사정도 초안 때와 같다.

**상한에 맞춰 식별자를 줄이지 않는다.** `"red_orange"`를 7자 이하로 만드는 방법은 전부 우리가 지어내는 것이고, 서버가 실제로 무엇을 받는지 모르는 채 지어낸 값은 맞을 확률이 오히려 낮다. 잘못 지어낸 식별자는 `Http(400)`이 아니라 **조용히 저장된 뒤 다시 읽을 때 `GRAY`로 떨어져**(Mapper의 미지 식별자 처리) 색이 사라진 것처럼 보인다.

**드러나는 방식**: 2번은 팔레트 12색 중 2색을 고른 사용자에게만, 1번(설명 20자)은 21~30자 설명에만 나타난다. 둘 다 **부분 실패**라 빌드·테스트로는 잡히지 않고 실기기 검증에서만 보인다 → [quickstart.md](./quickstart.md) S-9.

**Alternatives considered**: R-003의 기각 이력을 참조한다. 이번 항목이 새로 검토한 후보는 "상한에 맞춰 식별자를 줄인다" 하나이며 위에서 기각했다.

> **후속 (plan 2.1.0)**: 서버팀 협의로 네 지점이 모두 닫혔다 → [R-029](#r-029-색-어휘와-어긋남-네-건의-협의-결과-plan-210). **"기다리고 제기한다"는 판단이 옳았다** — 상한에 맞춰 식별자를 지어냈다면 확정 어휘(`red_orange`·`light_blue`)와 어긋나 되돌려야 했을 것이다.

---

## R-027. PATCH에서 지운 설명이 서버에 전달되는가 *(plan 2.0.0)*

**Decision**: **`RoomRequest.description`에서 기본값을 없앤다.** `val description: String?`로 선언해 설명이 없을 때도 `"description": null`이 본문에 실리게 한다.

**Rationale**: `NetworkModule`의 `Json { ignoreUnknownKeys = true }`는 `encodeDefaults`를 건드리지 않으므로 kotlinx-serialization의 기본값 `false`가 적용된다 — **기본값과 같은 값을 가진 프로퍼티는 인코딩되지 않는다.** 지금의 `description: String? = null`은 설명이 없을 때 필드째 빠진다.

`POST`에서는 무해하다(`required`가 `name`·`color`뿐). **문제는 `PATCH`다.** 서버 스키마의 `required`가 `[]`이므로 빠진 필드는 "건드리지 않았다"로 읽히는 것이 PATCH의 통상 의미이며, 그러면 **편집에서 설명을 전부 지운 사용자의 변경이 요청에 담기지 않아 조용히 사라진다.** 폼은 성공으로 닫히고 방 설명은 그대로 남는다 — 실패를 알리는 신호가 어디에도 없다.

이 지점은 mock에서 드러날 수 없었다. mock은 DTO 객체를 그대로 맵에 넣어 **직렬화를 한 번도 거치지 않았기** 때문이다. 실서버 전환이 처음 만드는 결함이다.

**`encodeDefaults = true`를 전역으로 켜지 않는다.** 그 설정은 이 앱의 모든 요청 DTO의 직렬화 결과를 바꾸며, "기본값은 보내지 않는다"에 기대는 다른 DTO가 나중에 생기면 조용히 깨진다. 고칠 곳을 한 프로퍼티로 좁히는 편이 영향 범위를 읽을 수 있다.

**Alternatives considered**:
- **`Update` 전용 요청 타입을 나눠 `description`을 `String`(비-nullable)으로 둔다** — 기각. 서버가 `anyOf [string, null]`을 받으므로 빈 문자열과 `null` 중 무엇이 "설명 없음"인지 다시 정해야 하고, [data-model.md](./data-model.md) §5가 "빈 문자열은 `null`로 보낸다"를 이미 정했다. 타입을 나누는 조건(두 요청이 갈라짐)도 아직 아니다.
- **`@EncodeDefault` 애노테이션을 붙인다** — 실질적으로 같은 결과지만 기각. 기본값을 선언한 뒤 그 기본값을 무시하라고 지시하는 형태라, 기본값을 없애는 쪽이 읽는 사람에게 의도가 곧바로 보인다.

---

## R-028. 세션·유저 등록 선행 조건을 이 feature가 떠안는가 *(plan 2.0.0)*

**Decision**: **떠안지 않는다.** 익명 세션 확보 배선과 유저 등록(`POST /api/v1/users`) 호출은 이 계획의 범위 밖이며, 각각 `docs/specs/anonymous-auth-session`과 `docs/specs/profile`이 소유한다. 이 feature는 **선행 조건이 충족되지 않은 상태를 명시하고 검증 가이드에 우회를 적는 데까지** 한다.

**Rationale**: 세 엔드포인트가 모두 `401`에 `USER_NOT_REGISTERED`를 두고 있어, 신원 증명이 실려도 서버에 유저가 없으면 거절된다. 그런데 저장소의 현재 상태가 둘 다 비어 있다 — `EnsureAnonymousSessionUseCase`는 `:core:domain`에 있으나 **호출하는 코드가 없고**, `POST /api/v1/users`를 부르는 코드는 아예 없다(`ProfileRepositoryImpl`은 로컬 저장뿐).

여기서 세션 부트스트랩을 배선하면 **앱 시작 시점의 인증 생애주기**를 이 폼의 계획이 정하게 된다. 그것은 진입점 8개 전부와 나머지 모든 화면에 걸리는 결정이라 폼 하나가 정할 수 있는 범위가 아니고, 이미 자기 spec을 가진 남의 결정이다([헌법 원칙 II](../../constitution.md)의 경계 원칙이 문서 소유에도 그대로 적용된다).

**막히는 것을 숨기지 않는다.** 배선 전까지 실기기에서 S-1~S-9가 전부 401로 막히며, 그 사실과 우회는 [quickstart.md](./quickstart.md) §1이 소유하고 [plan.md](./plan.md) §열린 항목 H가 추적한다. 이 계획이 만든 코드가 틀려서 막히는 것이 아니라는 구분이 남아야 다음 단계가 엉뚱한 곳을 고치지 않는다.

**Alternatives considered**:
- **임시 검증 진입점(`:feature:main`)에서 세션·등록을 부른다** — 기각. 걷어낼 임시 배선에 인증 부트스트랩을 넣으면, 걷어내는 순간 실제 진입점이 그 책임을 물려받았는지 아무도 확인하지 않는다. 게다가 `:feature:main`이 인증 UseCase를 알게 되어 되돌리기 어려운 의존이 생긴다.
- **실서버 전환을 세션 배선 이후로 미룬다** — 기각. 데이터 레이어 교체와 인증 배선은 서로를 기다릴 이유가 없고, 미루면 mock 코드가 그동안 계속 유지 대상으로 남는다. 앞의 것을 먼저 마치고 뒤의 것이 붙는 순간 전체가 켜지는 편이 대기 비용이 없다.


---

## R-029. 색 어휘와 어긋남 네 건의 협의 결과 *(plan 2.1.0)*

> ~~**일부 재검토됨(plan 2.2.0)**~~ — 아래 `"grey"` 판정이 **틀렸다.** 서버가 배포한 `enum`은 `"gray"`다 → [R-030](#r-030-서버-enum-배포로-색-어휘가-확정됐다--grey-판정을-되돌린다-plan-220). **협의 결과 표(어긋남 1~4)와 나머지 판단은 유효하며, 그중 2·4는 실제로 반영됐다.**

**Decision**: **어긋남 네 건이 모두 spec 쪽으로 닫혔다.** 이 구현이 고치는 것은 ~~**`RoomColor.GRAY`의 서버 식별자 `"gray"` → `"grey"`**~~ **하나뿐이다 → 실제로는 고칠 것이 없었다(R-030).** 나머지 12색의 식별자는 [R-018](#r-018-mock의-색상-식별자-표기-plan-110)이 확정한 소문자 스네이크 표기 그대로 맞았다. 확정 어휘 표의 소유자는 [contracts/room-api.md](./contracts/room-api.md) §2다.

**Rationale**: 2026-08-27 서버팀 협의 결과다.

| # | 어긋남 | 결과 |
|---|---|---|
| 1 | `description.maxLength: 20` vs FR-005의 30자 | **30자가 맞다.** 서버가 상한을 고친다 |
| 2 | `color.maxLength: 7`이 `red_orange`·`light_blue`를 자른다 | **상한을 늘린다.** 서버가 고친다 |
| 3 | `Room.color` 예시(`"black"`)와 `InvitationPreview.room.color` 예시(`"#FF6B6B"`)의 형식이 갈린다 | **스키마의 모순이 아니라 낡은 예시였다.** 4번이 닫히면서 함께 닫힌다 — 확정 어휘에 `"black"`도 hex도 없다 |
| 4 | `color`에 `enum`이 없어 어휘를 읽을 수 없다 | **12색으로 확정.** 공백은 밑줄이 되고(`red orange` → `"red_orange"`), 미선택 기본값은 ~~**`"grey"`**~~ → **`"gray"`**(R-030) |

~~**`grey`가 이 항목의 유일한 구현 변경이다.**~~ **이 문단의 전제가 틀렸다(R-030).** 서버 어휘가 `"gray"`라 흡수할 철자 차이 자체가 없었다. 다만 **"도메인 이름을 서버 철자에 맞춰 바꾸지 않는다"는 판단은 그대로 유효하다** — R-018이 "열거 상수 이름에서 식별자를 파생하지 않는다"고 정한 원칙이며, 철자가 갈렸다면 그 원칙이 값을 했을 자리다.

**서버 `enum`의 순서를 칩 그리드에 반영하지 않는다.** `RoomColor`의 선언 순서는 [`RoomColor.kt`](../../../core/domain/src/main/kotlin/team/mino/core/domain/model/RoomColor.kt)의 KDoc대로 **칩 그리드 배치 순서**이고 그 순서는 Figma가 정한다. 두 순서는 소유자가 다르므로 서로를 따르지 않는다 — 우연히 같아졌다가 한쪽이 바뀌면 조용히 어긋난다.

**서버 수정 반영 전까지 1·2는 실측으로 확인한다.** 협의는 끝났지만 배포는 아직이므로, 21~30자 설명과 두 단어 색이 실제로 저장되는지는 서버가 고친 뒤에 확인해야 한다 → [quickstart.md](./quickstart.md) S-9 · [plan.md](./plan.md) §열린 항목 D.

**Alternatives considered**: 없다. 확정된 서버 어휘를 받아 적은 항목이다. 이 결정 이전에 검토·기각된 후보들(hex 계약 · 식별자 단축 · spec 개정)은 [R-003](#r-003-swagger-계약이-spec과-어긋나는-세-지점-plan-100)·[R-026](#r-026-색설명-계약을-실제-api로-재대조한다-plan-200)의 이력을 참조한다.

---

## R-030. 서버 enum 배포로 색 어휘가 확정됐다 — `grey` 판정을 되돌린다 *(plan 2.2.0)*

**Decision**: **서버가 `color`에 `enum` 13개를 배포했고, 회색은 `"gray"`다.** [R-029](#r-029-색-어휘와-어긋남-네-건의-협의-결과-plan-210)가 확정한 `"grey"`(영국식)를 되돌린다. [R-018](#r-018-mock의-색상-식별자-표기-plan-110)이 정한 소문자 스네이크 표기가 **13색 전부에서 그대로 맞았으므로 `RoomMapper`는 고칠 것이 없다.**

**Rationale**: 2026-08-28T00:55:30+09:00 조회. `POST /api/v1/rooms` · `PATCH /api/v1/rooms/{roomId}` · `GET /api/v1/rooms/{roomId}`의 `color`가 모두 같은 `enum`을 갖는다.

```
red · red_orange · orange · lime · green · cyan · violet · pink · blue · brown · light_blue · purple · gray
```

응답 쪽에는 설명까지 붙었다 — **"팔레트 색상 키(13색, snake_case). 실제 색 매핑은 클라이언트 담당, 개인방 기본은 gray."** 색값을 클라이언트가 소유한다는 [R-003](#r-003-swagger-계약이-spec과-어긋나는-세-지점-plan-100)의 구조적 판단을 서버가 계약 문서에 명시한 것이다.

**어긋남 네 건의 최종 상태**

| # | 어긋남 | 상태 |
|---|---|---|
| 1 | `description.maxLength: 20` vs FR-005의 30자 | **미해소.** 협의대로 고쳐지지 않았다. 21~30자 설명은 여전히 거절된다 |
| 2 | `color.maxLength: 7`이 두 단어 색을 자른다 | **해소.** 상한이 제거되고 `enum`으로 대체됐다 |
| 3 | 색 표현의 자기모순 | **부분 해소.** 방 응답의 예시가 `"black"` → `"red"` + `enum`으로 정리됐으나, `InvitationPreview.room.color`는 여전히 hex 예시(`"#FF6B6B"`)다. **이 feature는 초대 미리보기를 쓰지 않아 영향이 없다** |
| 4 | `color`에 `enum`이 없다 | **해소.** 13색이 명시됐다 |

**왜 틀렸는가 — 구두 확정을 스키마 확정으로 취급했다.** 2.1.0은 서버팀이 구두로 전한 어휘를 받아 `research.md`에 확정 결정으로 박고 작업(`tasks.md` T069)까지 만들었다. 그런데 [R-026](#r-026-색설명-계약을-실제-api로-재대조한다-plan-200)이 세운 원칙은 **"문서에서 읽을 수 없으면 추정하지 않고 기다린다"**였고, 2.1.0은 그 원칙을 자기가 어겼다. 대가는 하루짜리였지만 — 그 사이에 T069가 수행됐다면 **서버가 `enum` 위반으로 거절하는 코드**가 들어갔을 것이다.

**앞으로도 같게 다룬다**: 구두 합의는 협의 결과로 기록하되 **계약 값은 배포된 스키마가 낼 때까지 확정하지 않는다.** 그때까지 코드는 기존 값을 유지하고, 검증 시나리오가 차이를 드러내게 둔다(이번에 [quickstart.md](./quickstart.md) S-9가 맡았던 역할이다).

**서버 `enum` 순서는 계약이 아니다.** 배포된 순서가 `RoomColor`의 선언 순서와 우연히 일치하지만, 그 선언 순서는 Figma 칩 그리드가 소유한다([R-018](#r-018-mock의-색상-식별자-표기-plan-110)). 두 순서를 서로 맞추지 않는다 — 한쪽이 바뀔 때 조용히 어긋난다.

**Alternatives considered**: 없다. 배포된 `enum`은 확정 계약이므로 받아 적는 것 외의 선택지가 없다. `"grey"`를 고집하면 요청이 거절된다.

---

## R-031. `RoomApiService`가 이미 `develop`에 있다 — 신규가 아니라 확장이다 *(plan 3.0.0)*

**Decision**: `core/data/.../network/service/RoomApiService.kt`를 **새로 만들지 않는다.** 이미 있는 클래스에 `getRoom`·`createRoom`·`updateRoom` 세 함수를 **더한다.** 테스트도 마찬가지로 이미 있는 `core/data/src/test/java/team/mino/core/data/network/RoomApiServiceTest.kt`에 케이스를 더한다 — 새 파일을 만들지 않는다.

**Rationale**: 2026-08-28 `develop` 재확인. [R-024](#r-024-실서버가-붙었다--mock-데이터-레이어를-걷어낸다-plan-200)~[R-030](#r-030-서버-enum-배포로-색-어휘가-확정됐다--grey-판정을-되돌린다-plan-220)이 쓰인 시점에는 이 파일이 없었으나, 그 사이 `shared-link-receiver`가 `GET /api/v1/rooms`를 붙이면서 같은 이름의 클래스를 세웠다([그 feature의 research.md R-015](../shared-link-receiver/research.md)). 현재 형태는 `listRooms()` 하나다.

이 계획이 같은 이름의 파일을 "신규"로 다루면 **작업자가 기존 파일을 덮어써 `listRooms()`를 지운다.** 그 함수는 방 선택 시트(`:feature:sharereceiver`)가 무는 유일한 데이터 경로다.

> **정정(plan 3.0.1) — "조용히 깨진다"는 틀렸다.** 이 문단은 원래 덮어쓰기가 다른 feature를 조용히 망가뜨린다고 적었다. 실측하면 그렇지 않다. `listRooms()`를 참조하는 곳이 프로덕션 [`RoomListRemoteDataSourceImpl`](../../../core/data/src/main/java/team/mino/core/data/datasource/RoomListRemoteDataSourceImpl.kt)과 [`RoomApiServiceTest`](../../../core/data/src/test/java/team/mino/core/data/network/RoomApiServiceTest.kt)의 케이스 3건이라, 함수를 지우면 **`:core:data`가 컴파일되지 않는다.** 실패는 은밀한 런타임 회귀가 아니라 즉시 드러나는 빌드 실패다.
>
> **결정은 그대로다** — 확장으로 다루는 이유는 "조용히 깨져서"가 아니라 **덮어쓸 이유가 없어서**다. 바뀌는 것은 위험의 등급이다.
>
> **가드가 비는 구간은 없다.** 합병이 프로덕션 참조를 옮기지만 순서가 `T072`(네 함수 구현) → `T073` → `T082`(`RoomList*` 삭제)라, 참조가 사라지기 전에 새 참조가 먼저 선다. 다만 그 사이 **테스트 3건이 유일한 가드가 되는 순간이 있으므로**, T080이 "기존 케이스를 고치지 않는다"고 못박은 것은 스타일 규칙이 아니라 **가드 유지 장치**다.

**확장이 따라야 할 기존 형태** — 세 함수는 이미 자리잡은 관례를 그대로 잇는다.

| 항목 | 기존 `listRooms()`가 정한 형태 |
|---|---|
| 경로 | `client.get("api/v1/rooms")` — `defaultRequest.url` 기준 상대 경로, 앞에 `/`를 붙이지 않는다 |
| 봉투 | `.body<MinoResponse<T>>().data` — 그 줄에서 끝난다 |
| 예외 | 잡지 않는다. KDoc이 `convertDomainException`·`MinoIdentityProofPlugin`을 지목한다 |
| 가시성 | `internal class ... @Inject constructor(private val client: HttpClient)` |

**함께 확인된 것 — [R-025](#r-025--data-봉투를-어느-레이어가-벗기는가-plan-200)의 선행 조건이 닫혔다.** `MinoResponse<T>`(`network/dto/response/MinoResponse.kt`)와 [응답 봉투 ADR](../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)이 **둘 다 `develop`에 있다.** R-025가 "이 브랜치에서 ADR 링크가 열리지 않는다"고 적었던 상태는 끝났다. 결정은 그대로이고, 기다리던 것이 도착했을 뿐이다.

**Alternatives considered**:
- *`RoomFormApiService`를 따로 만든다* — 이름 충돌을 피하고 이 계획의 소유 범위가 또렷해지나, 같은 리소스·같은 `HttpClient`를 쓰는 서비스가 둘이 된다. [`core/data/README.md`](../../../core/data/README.md) §4의 리소스 단위 명명과 어긋나고, [R-032](#r-032-방-리소스의-두-datasource를-하나로-합친다-plan-300)가 DataSource를 합치는 방향과도 반대다. 기각.

---

## R-032. 방 리소스의 두 DataSource를 하나로 합친다 *(plan 3.0.0)*

**Decision**: `RoomRemoteDataSource`에 `listRooms(): List<RoomSummaryResponse>`를 **흡수하고**, `RoomListRemoteDataSource`·`RoomListRemoteDataSourceImpl`·`di/RoomListDataSourceModule`을 **지운다.** `RoomRepositoryImpl`은 두 DataSource가 아니라 하나만 주입받는다.

이 결정은 [R-024](#r-024-실서버가-붙었다--mock-데이터-레이어를-걷어낸다-plan-200)가 "`RoomRemoteDataSource` 인터페이스는 한 글자도 바뀌지 않는다"고 적었던 것을 **뒤집는다.** 그 판단은 mock을 실구현으로 갈아 끼우는 것만 보았고, 그 사이에 같은 리소스의 DataSource가 하나 더 생겼다는 사실을 알 수 없었다.

**Rationale**: 이 합병을 이 계획에 배정한 것은 이 계획이 아니라 [`shared-link-receiver`의 research.md R-015](../shared-link-receiver/research.md)다 — *"같은 `room` 리소스에 DataSource가 둘인 상태는 `group-room-form`이 실서버로 전환하는 시점에 하나로 합쳐지며, 그때 지워지는 것은 `RoomListRemoteDataSource`다."* 같은 문장이 코드에도 남아 있다([`RoomListRemoteDataSource.kt`](../../../core/data/src/main/java/team/mino/core/data/datasource/RoomListRemoteDataSource.kt)의 KDoc).

**둘로 갈라 놓았던 유일한 근거가 이번에 사라진다.** 그 KDoc이 밝힌 분리 사유는 *"바인딩 대상이 다르기 때문 — 그쪽은 mock, 이쪽은 실서버"*다. R-024가 mock을 걷어내면 두 바인딩이 같은 실구현을 가리키게 되어, 같은 리소스·같은 `ApiService`·같은 스코프를 무는 인터페이스가 **아무 차이 없이 둘** 남는다. [`core/data/README.md`](../../../core/data/README.md) §5는 DataSource를 데이터 출처 단위의 인터페이스·구현체 **쌍**으로 정하지, 한 출처에 둘을 두는 형태를 두지 않는다.

`RoomRepositoryImpl`의 KDoc이 스스로 이 상태를 **"출처가 함수마다 갈리는 과도기"**라 부르고 있다. 그 과도기를 끝내는 것이 이 계획의 몫이다.

**합병을 미루면 무엇이 남는가**: mock이 사라진 뒤에도 `RoomListRemoteDataSource`가 근거 문장이 거짓이 된 KDoc(`그쪽은 mock`)을 달고 남는다. 소유자 지목이 끊긴 문서 링크는 [헌법 원칙 I](../../constitution.md)이 금지하는 상태이고, 다음에 방 API를 건드리는 사람이 어느 DataSource에 함수를 더할지 판단할 근거가 없어진다.

**바뀌는 경계** — 이것이 이번 개정을 MAJOR로 만든다.

| 대상 | 이전 판정 | 3.0.0 |
|---|---|---|
| `RoomRemoteDataSource` 인터페이스 | 무변경 | **`listRooms()` 추가** |
| `RoomRepositoryImpl` | 무변경 | **생성자 인자 2개 → 1개**, `getRooms()`가 같은 DataSource를 문다 |
| `RoomList*` 3파일 | 이 계획 밖 | **삭제** |

**`RoomSummaryResponse`·`RoomSummaryMapper`는 건드리지 않는다.** 옮기는 것은 함수 한 개의 선언 자리뿐이고, 목록 응답의 DTO·변환·도메인 모델(`RoomSummary`)은 `shared-link-receiver`가 소유한 채로 남는다. 이 계획은 그 계약을 읽지도 바꾸지도 않는다.

**Alternatives considered**:
- *합병을 미루고 열린 항목으로 남긴다* — 이번 개정이 MINOR에 머물러 `tasks.md`의 기존 작업 의미가 보존된다. 그러나 위의 "거짓이 된 KDoc"이 그대로 남고, 합병을 배정한 문서가 지목한 **바로 그 시점**을 이 계획이 그냥 지나치게 된다. 다음에 이 상태를 정리할 계기가 어느 spec에도 없다. 기각(2026-08-28 사용자 확인).
- *`RoomListRemoteDataSource`를 남기고 `RoomRemoteDataSource` 쪽을 지운다* — 지우는 쪽이 반대가 된다. 이 계획이 소유한 세 함수가 더 많고, 배정 문서가 지목한 삭제 대상이 명시적으로 `RoomListRemoteDataSource`다. 기각.

---

## R-033. 어긋남 한 건은 여전히 열려 있다 — 2026-08-28 재조회 *(plan 3.0.0)*

> **상태 갱신(plan 3.1.0)** — 이 항목이 "여전히 열려 있다"고 적은 `description.maxLength: 20`은 **2026-08-31T12:51 재조회에서 사라졌다.** 아래 결정(30자 유지)은 그대로 유효하고, 바뀐 것은 그것을 막고 있던 서버 제약이다 → [R-035](#r-035-어긋남이-0건이-됐다--서버가-descriptionname의-상한을-걷어냈다-plan-310).

**Decision**: `description`의 상한은 **30자를 유지한다**([R-029](#r-029-색-어휘와-어긋남-네-건의-협의-결과-plan-210)의 판정 그대로). 이번 조회에서도 서버는 `20`이다. 코드는 바뀌지 않는다.

**Rationale**: 2026-08-28T11:39:53+09:00 재조회. 세 오퍼레이션의 요청 스키마가 여전히 `"description": { "anyOf": [{ "type": "string", "maxLength": 20 }, { "type": "null" }] }`다. 색 `enum` 13색은 [R-030](#r-030-서버-enum-배포로-색-어휘가-확정됐다--grey-판정을-되돌린다-plan-220)이 확인한 그대로이고 변동이 없다.

`enum` 배포(08-28T00:55) 이후 약 11시간이 지난 조회에서도 설명 상한이 그대로라는 것은, 서버팀의 수정이 색만 먼저 나갔다는 뜻이다. [plan.md](./plan.md) §열린 항목 D의 닫는 조건은 그대로 유효하다.

**Alternatives considered**: 없다. [R-030](#r-030-서버-enum-배포로-색-어휘가-확정됐다--grey-판정을-되돌린다-plan-220)이 세운 "배포된 스키마가 낼 때까지 확정하지 않는다"를 따르면 조회 결과를 받아 적는 것이 전부다.

---

## R-034. 방 썸네일 13종과 폴백 컴포넌트가 `:core:common:ui`로 올라갔다 *(plan 3.0.0)*

**Decision**: 방 썸네일 WebP 13종(12색 + 회색)과 그것을 그리는 `RoomThumbnailFallback`은 **`:feature:roomform`이 아니라 [`:core:common:ui`](../../../core/common/ui/README.md)에 있다.** 이 계획의 소스 트리에서 `feature/roomform/src/main/res/drawable-*/`를 걷어낸다.

**Rationale**: 2026-08-28 `develop` 실측. 커밋 `2e4c5a2`가 밀도 3벌(mdpi·xhdpi·xxhdpi)을 통째로 옮기고 `RoomThumbnailFallback(color: MinoRoomColor?)`을 **public**으로 세웠다. 승격 사유는 그 커밋이 적었다 — *"폴백은 도메인 색상값과 래스터 에셋을 함께 요구해 디자인 시스템에 둘 수 없다."*

두 번째 사용처가 실제로 생긴 것이 승격의 근거다. `shared-link-receiver`의 방 선택 시트가 같은 폴백을 그려야 했고, [`component-asset-placement.md`](../../conventions/component-asset-placement.md) §2.3(승격은 feature → `:core:common:ui` 한 방향)이 정한 조건이 그 시점에 충족됐다.

**이 승격은 기존 ADR을 어기지 않는다 — 오히려 두 ADR이 명령한 그대로다.** [래스터 이미지 ADR](../../adr/2026-08-19-raster-image-placement-and-format.md) §결정 1이 *"공유 래스터 이미지는 `:core:common:ui`가 갖는다"*고 정했고, [방 색상 팔레트 ADR](../../adr/2026-08-14-room-color-palette-in-design-system.md) §적용 범위가 *"홀더 프로퍼티만 쓰는 공용 합성 컴포넌트는 여러 feature가 쓰더라도 이 ADR을 근거로 `:core:design-system`에 두지 않는다"*고 이 경우를 미리 갈라 두었다. `RoomThumbnailFallback`은 `internal` 토큰에 닿지 않으므로 그 갈래에 정확히 해당한다.

**팔레트 ADR §결과의 "매핑은 feature가 소유한다"와도 어긋나지 않는다.** 그 문장이 가리키는 것은 **서버 색 식별자 ↔ `MinoRoomColor`** 변환이고, 그 매핑은 지금도 feature에 있다 — [`RoomColorUiModel.chip`](../../../feature/roomform/src/main/java/team/mino/feature/roomform/form/model/RoomColorUiModel.kt)(`RoomColor` → `MinoRoomColor`, KDoc이 그 ADR을 근거로 인용한다)과 [`RoomMapper`](../../../core/data/src/main/java/team/mino/core/data/repository/mapper/RoomMapper.kt)(서버 문자열 ↔ `RoomColor`). 승격된 것은 **`MinoRoomColor` → drawable**이라는 별개의 매핑으로, ADR이 다루지 않은 축이다. **ADR 개정이 필요 없다.** [R-008](#r-008-미리보기-카드와-확인-모달의-소유-모듈-plan-100)이 *"미리보기 카드는 디자인 시스템 컴포넌트가 아니라서 `:feature:roomform`이 갖는다"*고 정한 것은 **그대로 유효하다** — 옮겨 간 것은 카드가 아니라 카드 **안쪽의 썸네일 한 장**이고, [`RoomPreviewCard`](../../../feature/roomform/src/main/java/team/mino/feature/roomform/form/component/RoomPreviewCard.kt)는 여전히 이 feature가 소유한 채 그 폴백을 호출한다.

**이 계획이 더 할 일은 없다.** 에셋도 컴포넌트도 이미 서 있고 폼이 이미 쓰고 있다. 트리 표기만 실측과 맞춘다.

**Alternatives considered**: 없다. 이미 머지되어 두 feature가 함께 쓰는 상태이고, 되돌리면 방 선택 시트가 에셋을 잃는다.

---

## R-035. 어긋남이 0건이 됐다 — 서버가 `description`·`name`의 상한을 걷어냈다 *(plan 3.1.0)*

**Decision**: **방 설명 30자를 그대로 두고, 방 이름 15자는 이제 클라이언트만 강제한다.** 코드는 한 줄도 바뀌지 않으나 **[plan.md](./plan.md) §열린 항목 D가 닫힌다.**

**Rationale**: 2026-08-31T12:51:29+09:00 재조회. 두 요청 스키마가 바뀌었다.

| 필드 | 2026-08-28T11:39 | 2026-08-31T12:51 |
|---|---|---|
| `name` | `minLength: 1`, **`maxLength: 15`** | `minLength: 1`, **`pattern: "^[\uAC00-\uD7A3\u3131-\u314E\u314F-\u3163A-Za-z0-9 ]+$"`** (상한 제거) |
| `description` | `anyOf: [{ string, **maxLength: 20** }, null]` | `anyOf: [{ string }, null]` (상한 제거) |

[R-029](#r-029-색-어휘와-어긋남-네-건의-협의-결과-plan-210)가 협의로 확정하고 [R-033](#r-033-어긋남-한-건은-여전히-열려-있다--2026-08-28-재조회-plan-300)이 두 번 미반영으로 확인한 **어긋남 1번이 닫혔다.** 서버가 협의한 `30`을 넣는 대신 상한 자체를 걷어냈으므로, spec의 30자와 부딪힐 값이 남아 있지 않다. 이로써 이 계약의 어긋남은 **0건**이다.

**`name`의 `pattern`은 FR-004와 정확히 같다.** 유니코드 구간이 완성형(`AC00-D7A3`)·자음(`3131-314E`)·모음(`314F-3163`)·영문·숫자·공백이며, **자모 단독을 허용한다** — spec 3.1.0이 [R-021](#r-021-방-이름의-자모-단독-허용-plan-120)로 확정한 그 판정을 서버가 독립적으로 같게 내렸다. 클라이언트 검증을 느슨하게 할 이유도, 조일 이유도 없다.

**15자 상한이 이제 클라이언트에만 있다.** 서버가 길이를 보지 않으므로 16자 이름이 요청으로 나가면 그대로 저장된다. 이 폼은 `NameChanged`에서 잘라 16번째 글자를 만들지 않으므로([data-model.md](./data-model.md) §입력 규칙) 실제로 그런 요청이 나갈 경로가 없고, **그래서 코드를 더하지 않는다.** 다만 상한의 유일한 수문장이 클라이언트라는 사실은 [contracts/room-api.md](./contracts/room-api.md) §1이 기록한다 — 다른 클라이언트가 붙으면 그쪽에도 같은 규칙이 필요하다.

**Alternatives considered**:

- *서버가 상한을 안 보니 클라이언트 상한도 푼다* — FR-003·TS-003이 15자를 요구한다. 서버 제약이 느슨해진 것은 spec을 바꿀 근거가 아니다.
- *길이를 서버에서도 강제해 달라고 다시 요청한다* — 상한 제거가 협의(30으로 늘린다)의 결과일 가능성이 높고, 지금 어긋나는 것이 없어 협의를 다시 열 근거가 없다. 실제로 문제가 되는 것은 **다른 클라이언트가 붙을 때**이며 그때 열면 된다.

---

## R-036. 도착점 feature 지형이 바뀌었다 — `:feature:home`이 홈 방 시트를 갖고 있다 *(plan 3.1.0)*

**Decision**: **이 계획의 범위는 그대로다.** FR-011의 도착점 이동은 여전히 이 계획이 만들지 않는다. 바뀌는 것은 [plan.md](./plan.md) §범위 경계가 그 사실을 설명하는 **근거**다 — "도착점 feature가 하나도 없다"에서 "도착점 feature는 다른 spec이 소유한다"로 옮긴다.

**Rationale**: plan 2.2.1이 2026-08-28에 적은 *"`:feature:home`은 아직 Sample 버튼만 있는 골격"*이 낡았다. 실측하면 그 모듈은 [`HomeRoomSheet.kt`](../../../feature/home/src/main/java/team/mino/feature/home/main/component/HomeRoomSheet.kt)로 **홈 방 시트를 이미 그리고 있고**, 첫 칸 `방 만들기`가 `HomeSideEffect.NavigateToRoomForm`으로 나가며, 그 신호를 어디로 배선할지는 [`HomeNavigation.kt`](../../../feature/home/src/main/java/team/mino/feature/home/HomeNavigation.kt)의 `onNavigateToRoomForm` 콜백을 받는 셸이 정한다.

**이것이 `RoomFormLauncher` 계약을 흔들지 않는다.** 오히려 계약이 노린 모양 그대로다 — 홈은 폼을 **열 신호만** 내고 도착점을 스스로 정하며, `:feature:home`의 `build.gradle.kts` 어디에도 `:feature:roomform`이 없다.

**spec 4.0.0이 바꾼 홈 분기(방 상세 → 홈 덱 전환)가 실제로 닿는 곳은 그 모듈이다.** 지금 `onNavigateToRoomForm: () -> Unit`은 **결과를 되받는 표면이 없어**, 폼이 돌려주는 `created` + `roomId`로 보는 방을 바꾸려면 그 콜백이 결과를 받는 형태로 넓어져야 한다. 그것은 [`docs/specs/home-deck-exploration`](../home-deck-exploration/spec.md)의 몫이고, 이 계획이 대신 설계하지 않는다 — 폼 쪽 계약([contracts/room-form-launcher.md](./contracts/room-form-launcher.md) §3)은 이미 그 값을 싣고 있어 더할 것이 없다.

**Alternatives considered**:

- *이 계획이 홈 쪽 배선까지 맡는다* — `:feature:home`은 다른 spec이 소유하는 모듈이라 이 계획이 손대면 두 spec이 같은 파일을 두고 갈린다. 계약이 이미 필요한 값을 싣고 있으므로 넘길 것도 없다.
- *`RoomFormLauncher`에 도착점 힌트를 싣는다* — [R-004](#r-004-폼은-도착점을-모른다-plan-100)가 기각한 것과 같은 안이다. 홈 분기가 바뀌었다는 사실은 그 기각을 흔들기는커녕, 도착점을 폼 밖에 둔 덕에 **폼을 한 글자도 안 고치고 넘어간** 근거가 된다.
