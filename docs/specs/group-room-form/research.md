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

**Decision**: **DTO·Mapper·Repository·DataSource 인터페이스까지 실제 계약대로 만들고, `DataSource` 구현 하나만 인메모리 mock으로 채운다.** `RoomMockRemoteDataSourceImpl`이 `RoomMockStore`(`@Singleton` 인메모리 맵)를 읽고 써서 swagger 스키마와 같은 모양의 DTO를 돌려준다. Ktor `ApiService`는 **이번에 만들지 않는다.**

**Rationale**: [`core/data/README.md`](../../../core/data/README.md) §8이 정한 10단계 절차 중 mock으로 대체되는 것은 2단계(ApiService)뿐이다. 나머지를 실제와 같게 두면 서버가 붙을 때 바뀌는 곳이 `RoomDataSourceModule`의 `@Binds` 한 줄과 새 `RoomApiService` 한 파일로 좁혀진다. 반대로 Repository까지 통째로 fake로 두면, 실서버 전환 때 Mapper·DTO·에러 매핑이 그때 처음 작성되어 이번 검증이 아무것도 보증하지 못한다.

**쓰지 않을 `ApiService`를 미리 만들지 않는 이유**는 [M3 컴포넌트 패턴 ADR](../../adr/2026-07-25-design-system-component-m3-pattern.md)이 API 표면에 대해 정한 것과 같다 — 호출부 없는 코드는 검증되지 않은 채 굳는다. `expectSuccess`·`convertDomainException`은 이미 `NetworkModule`에 전역으로 있어 서버 연결 시 새로 설계할 것이 없다.

**mock에 실패 주입 스위치를 두지 않는다.** EC-014(편집 실패 시 입력 유지)·UX-003의 검증은 Fake `RoomRepository`를 주입한 `RoomFormViewModel` 단위 테스트가 소유한다. 프로덕션 코드에 테스트 전용 분기를 남기지 않는다.

**이 결정은 다른 feature를 구속한다** — 서버가 붙기 전에 만들어지는 모든 화면이 같은 갈래에 선다. ADR 승격 후보다.

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

**Rationale**: 1.0.0에서 "서버 확정 전"을 이유로 미확정으로 남겼으나, mock 구간에서는 서버 확정을 기다릴 이유가 없다. 표기가 바뀔 때 고칠 곳이 `RoomMapper` 한 파일이라 되돌리는 비용이 사실상 없고, 그동안 이 값이 도메인·UI로 새어 나가지 않는다([contracts/room-api-mock.md](./contracts/room-api-mock.md) §2).

`GRAY`의 식별자를 `"gray"`로 두는 것은 R-017과 별개다 — 에셋 이름은 `my room`이지만 그것은 Figma variant 이름이지 서버 계약이 아니다. 도메인 값 이름을 따른다.

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
