# 계약: 폼 진입과 결과 (`RoomFormLauncher`)

**대상 스펙 경로**: `docs/specs/group-room-form` · **부속 문서**: [plan.md](../plan.md)

폼을 여는 쪽과 폼 사이의 유일한 계약이다. 이 계약이 지켜지는 한 두 feature 모듈은 서로를 컴파일 타임에 모른다.

> 계약이 놓이는 자리와 작성 규칙은 [`feature-navigation.md`](../../../architecture/feature-navigation.md) 1장, API는 [`core:navigation` README](../../../../core/navigation/README.md) §2.1이 소유한다. 여기서는 **이 feature의 계약 내용**만 정한다.

---

## 1. 계약 인터페이스

`:core:navigation` — `activity/launcher/RoomFormLauncher.kt`

```
interface RoomFormLauncher : ActivityLauncher
```

구현은 `:feature:roomform`의 `di/RoomFormLauncherImpl.kt`가 갖고, `createIntent`에서 `RoomFormActivity`만 지목한다.

---

## 2. 진입 인자 (Intent extra)

`:core:navigation` — `activity/launcher/ExtraTag.kt`에 상수를 추가한다. 키 네이밍 규칙은 [`feature-navigation.md`](../../../architecture/feature-navigation.md) §1이 소유한다.

| 상수 | 값 | 타입 | 의미 | 근거 |
|---|---|---|---|---|
| `EXTRA_ROOM_FORM_ROOM_ID` | `"room_form_room_id"` | `String?` | **있으면 편집, 없으면 생성.** 모드를 가르는 유일한 값 | FR-009·FR-013 |
| `EXTRA_ROOM_FORM_ONBOARDING` | `"room_form_onboarding"` | `Boolean` (기본 `false`) | 온보딩 진입 여부. `true`면 [건너뛰기]를 노출하고 뒤로가기를 노출하지 않는다 | FR-017·FR-022 |

**진입점을 식별하는 인자를 두지 않는다.** 온보딩을 뺀 나머지 진입점 6개는 폼에게 구분되지 않으며, 도착점은 호출자가 정한다 — [research.md](../research.md) R-004. 온보딩만 예외인 이유는 그것이 도착점이 아니라 **폼 자신의 chrome**을 바꾸기 때문이다.

**호출 예**

```
// 생성 (온보딩이 아닌 진입점)
roomFormLauncher.launch(activity, resultLauncher = roomFormResultLauncher)

// 생성 (온보딩 첫 공동방 생성 스텝)
roomFormLauncher.launch(activity, resultLauncher = …) { putExtra(EXTRA_ROOM_FORM_ONBOARDING, true) }

// 편집 (방 상세 더보기 [편집])
roomFormLauncher.launch(activity, resultLauncher = …) { putExtra(EXTRA_ROOM_FORM_ROOM_ID, roomId) }
```

`withFinish`는 쓰지 않는다 — 결과를 받아야 하므로 호출 Activity가 살아 있어야 한다([`core:navigation` README](../../../../core/navigation/README.md) §2.1).

---

## 3. 결과 계약

| 결과 코드 | 추가 extra | 언제 | 호출자가 할 일 | 근거 |
|---|---|---|---|---|
| `RESULT_OK` | `EXTRA_ROOM_FORM_RESULT_OUTCOME` = `ROOM_FORM_OUTCOME_CREATED` · `EXTRA_ROOM_FORM_RESULT_ROOM_ID` | 저장 확인 모달의 [저장하기]로 방이 만들어졌다 | 진입점별 도착점으로 이동 + `방 생성 완료!` | FR-010·FR-011·FR-012 |
| `RESULT_OK` | `EXTRA_ROOM_FORM_RESULT_OUTCOME` = `ROOM_FORM_OUTCOME_UPDATED` · `EXTRA_ROOM_FORM_RESULT_ROOM_ID` | 편집 CTA로 방 정보가 수정됐다 | 방 상세 갱신 + `방 편집이 완료되었어요` | FR-015·FR-016 |
| `RESULT_OK` | `EXTRA_ROOM_FORM_RESULT_OUTCOME` = `ROOM_FORM_OUTCOME_SKIPPED` | 온보딩에서 [건너뛰기]를 눌렀다 | 공유 방법 튜토리얼 스텝으로 이동 | FR-017·TS-024 |
| `RESULT_CANCELED` | 없음 | 이탈이 확정됐다(모달 [나가기] 또는 잃을 것 없는 뒤로가기) | 아무것도 하지 않고 원래 화면을 유지한다 | FR-018·FR-021·FR-024 |

**상수** (`ExtraTag.kt`) — 값의 소유자는 이 표다. 위 결과 표는 상수 **이름**으로만 지목한다.

| 상수 | 값 |
|---|---|
| `EXTRA_ROOM_FORM_RESULT_OUTCOME` | `"room_form_result_outcome"` |
| `EXTRA_ROOM_FORM_RESULT_ROOM_ID` | `"room_form_result_room_id"` |
| `ROOM_FORM_OUTCOME_CREATED` | `"created"` |
| `ROOM_FORM_OUTCOME_UPDATED` | `"updated"` |
| `ROOM_FORM_OUTCOME_SKIPPED` | `"skipped"` |

`ROOM_FORM_OUTCOME_SKIPPED`를 `RESULT_CANCELED`가 아니라 `RESULT_OK`로 두는 이유: 건너뛰기는 **다음 스텝으로 나아가는 조작**이라 되돌아가는 이탈과 도착점이 다르다(TS-024는 튜토리얼 스텝, TS-035는 원래 화면). 두 경우를 한 코드로 합치면 호출자가 구분할 방법이 없다.

`created`가 `roomId`를 함께 싣는 이유: FR-011의 도착점 두 갈래(방 상세로 이동 / 복제 시트에서 새 방 선택 가능)가 모두 새 방의 식별자를 필요로 한다. FR-019의 "개인방 바로 아래 배치"도 이 값으로 이뤄진다.

---

## 4. DI 배선

| 무엇 | 어디 | 스코프 |
|---|---|---|
| `RoomFormLauncherImpl` → `RoomFormLauncher` `@Binds` | `:feature:roomform`의 `di/RoomFormNavigationModule.kt` | `ActivityRetainedComponent` + `@ActivityRetainedScoped` |

구현을 가진 모듈이 바인딩을 소유한다 — [`dependency-injection.md`](../../../conventions/dependency-injection.md). `:app`은 `implementation(project(":feature:roomform"))`으로 그래프에 넣기만 한다.

---

## 5. 이 계약이 지켜지는지 보는 법

| 확인 | 방법 |
|---|---|
| feature 간 의존이 없다 | `feature/roomform/build.gradle.kts`에 다른 `:feature:*`가 없고, 호출자 쪽에도 `:feature:roomform`이 없다 |
| 도착점을 폼이 모른다 | `:feature:roomform` 어디에도 다른 feature의 `Launcher` 주입이 없다 |
| 결과가 네 갈래로 닫힌다 | Activity의 `setResult` 호출 지점이 `RoomFormSideEffect.Finish` 처리 한 곳뿐이다 |

> `[TBD]` **결과를 읽는 쪽의 표면을 이 계약이 갖는가.** 지금은 진입점 feature마다 `EXTRA_ROOM_FORM_RESULT_OUTCOME` 문자열을 손으로 `when` 하게 된다. 판독 헬퍼를 `:core:navigation`에 둘지 각자 쓸지는 **두 번째 호출자가 생길 때** 정한다 — 지금은 임시 검증 진입점 하나뿐이라 근거가 없다.
