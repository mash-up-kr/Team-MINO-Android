# 검증 가이드: 프로필 설정 및 수정

Phase 1 산출물. 이 기능이 엔드투엔드로 동작함을 확인하는 절차만 담는다. 상태·계약의 정의는 [`data-model.md`](data-model.md)·[`contracts/`](contracts/)에 있다.

> **이번 범위의 "엔드투엔드"**: 화면 → ViewModel → UseCase → Repository → **꾹 서버**까지다. 로컬 DataStore는 그 응답의 캐시로 남는다([research.md D36](research.md#d36-원격-연동-착수--원천은-서버-로컬-datastore는-캐시)). 여전히 확인할 수 없는 항목은 §5에 모았다.

## 선행 조건

- 이 저장소의 표준 빌드 환경(JDK·Android SDK)이 구성되어 있다.
- **기기·에뮬레이터가 네트워크에 닿고 `https://api.gguk.org/`가 응답한다.** qa·prod가 같은 주소를 쓰므로 flavor는 아무거나 좋다.
- **Firebase 익명 세션이 확보돼 있다.** 세 요청 모두 Bearer 토큰을 요구하고, 토큰이 없으면 `MinoIdentityProofPlugin`이 `IllegalStateException`으로 요청을 막는다([identity-proof-attachment.md](../anonymous-auth-session/contracts/identity-proof-attachment.md)). 앱이 스플래시 없이 `ProfileActivity`로 바로 열리는 §4의 방식에서는 세션 확보가 먼저 돌았는지 확인해야 한다.
- 온보딩·마이페이지 feature는 아직 없다. 두 진입점은 §4의 방법으로 흉내 낸다.

## 1. 빌드·단위 테스트

```bash
./gradlew :core:domain:test :core:data:test          # 검증·매핑·분기·캐시 순서 ([repository 계약](contracts/profile-repository-contract.md) §테스트 계약)
./gradlew :feature:splash:testDebugUnitTest           # 회귀 — 이번 개정이 스플래시의 데이터 계층을 건드린다
./gradlew :feature:profile:testDebugUnitTest          # ViewModel 상태 전이
./gradlew :app:assembleQaDebug                        # 빌드 최소선 (헌법 §품질 게이트)
```

- `:core:data:test`가 이번 개정에서 가장 많이 늘어난다 — `MockEngine`으로 봉투 해제·DTO 매핑·`401 USER_NOT_REGISTERED` 처리·등록/수정 분기·**원격 실패 시 캐시 불변**을 덮는다([D43](research.md#d43-테스트-범위--mockengine-기반-데이터-레이어-테스트를-더한다)). 이 다섯은 네트워크 없이 도는 테스트이므로 서버 상태와 무관하게 항상 실행할 수 있다.
- **`:feature:splash`와 `UserRemoteDataSourceImplTest`는 회귀 확인이다.** 이번 개정은 이 feature가 처음으로 **이미 머지된 다른 feature의 데이터 계층을 고친다** — `UserApiService`·`UserRemoteDataSource`(+`Impl`)가 그것이다([D49](research.md#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다)). 진입 판정이 깨지면 사용자가 온보딩에 갇히거나 이미 가입한 채로 온보딩을 다시 본다. **`hasProfile()`이 성공 본문 스키마에 의존하지 않는다**는 사실이 특히 중요하다 — develop의 `{"data":{"id":1}}` 픽스처가 지키던 것이고, 그 느슨함은 실수가 아니라 계약이다.
- 로컬 `lintDebug`는 JBR 이슈로 데몬이 죽을 수 있다. 실패해도 코드 문제로 단정하지 않고, 검증이 수행된 것으로도 보지 않는다(헌법 §검증 장치의 한계).

## 2. 서버 계약 재확인

서버 문서는 언제든 바뀔 수 있다. 계약을 의심할 일이 생기면 [API 계약](contracts/profile-api-contract.md)의 스키마를 다시 조회해 대조한다.

```bash
S=".claude/skills/mino-plan/scripts/openapi_digest.py"
DOC=$(mktemp -t openapi)
python3 "$S" fetch "$DOC"
python3 "$S" show "$DOC" /api/v1/users:post /api/v1/users/me:get /api/v1/users/me:patch
```

계약이 달라져 있으면 이 문서가 아니라 **API 계약과 plan을 먼저 고친다.**

**같은 시점에 `:core:data` 트리도 훑는다** — 여러 feature가 이 모듈을 동시에 넓히고 있어, 만들려던 타입이 이미 있을 수 있다. `MinoResponse<T>`가 실제로 그랬고([D47](research.md#d47-develop-재대조--420의-대조가-옛-트리를-근거로-했다)), 그다음엔 **엔드포인트 자체**가 그랬다 — splash-screen이 `UserApiService`로 `GET /api/v1/users/me`를 먼저 쓰고 있었다([D49](research.md#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다)).

```bash
python3 "$S" index "$DOC" --tag user     # 이 feature가 쓰는 오퍼레이션의 태그
grep -rn "api/v1/users" core/data/src/main --include=*.kt   # 그 경로를 이미 부르는 코드
```

**두 번째 명령이 뭔가를 찾으면 새 `ApiService`를 만들지 않는다.** `ApiService`의 단위는 feature가 아니라 서버 태그이고, 소유자는 하나다.

> **이 절차는 형식이 아니다.** plan 4.0.0과 4.1.0 사이 **하루 만에** `avatar.color`가 자유 문자열에서 13개 `enum`으로 좁혀졌다. 그 사이에 구현했다면 서버가 거절할 값을 내보내고 있었을 것이다. 저장이 이유 없이 실패하면 **코드를 의심하기 전에 이 조회부터 돌린다.**

## 3. 화면 단독 확인 (Preview)

`ProfileScreen`은 stateless이므로 `@UiModePreviews`로 상태별 렌더를 확인한다.

| 프리뷰 | 상태 | 대응 시나리오 |
|---|---|---|
| 진입 직후 | 빈 닉네임, 미선택, 두 버튼 비활성 | TS-001 |
| 입력 완료 | 유효 닉네임 + 아바타 선택 | TS-002·TS-003 |
| 입력 오류 | `민` 입력, 오류 문구 노출 | TS-012 |
| 저장 중 | `isSaving = true` | UX-003, EC-004 |

- 마이페이지 진입(뒤로가기 **노출**)과 온보딩 진입(뒤로가기 **숨김**)을 각각 한 장씩 둔다 — FR-010의 차이가 프리뷰만으로 드러나야 한다([D29](research.md#d29-온보딩-진입에서-뒤로가기를-노출하지-않는다)).

## 4. 기기·에뮬레이터 실전 확인

온보딩·마이페이지가 없으므로 `ProfileActivity`를 `adb`로 직접 연다.

```bash
# 온보딩 진입(뒤로가기 차단) — 서버에 아직 등록되지 않은 상태를 전제한다
adb shell am start -n com.mino.gguk.qa/team.mino.feature.profile.ProfileActivity \
  --es profile_entry_point onboarding

# 마이페이지 진입(뒤로가기 허용·프리필)
adb shell am start -n com.mino.gguk.qa/team.mino.feature.profile.ProfileActivity \
  --es profile_entry_point edit
```

> **API 34 이하 에뮬레이터를 쓴다.** `ProfileActivity`는 `exported="false"`인데, API 33에서는 `am start`가 그대로 통과하지만 **API 35에서는 `SecurityException: Permission Denial`로 막히고 `adb root`도 안 된다.**
>
> 한글은 `adb shell input text`로 입력되지 않는다(`NullPointerException`). 닉네임 규칙이 영문 2자 이상도 허용하므로 `minho` 같은 영문으로 대체해 검증한다.
>
> **"미등록 상태"를 다시 만들려면 앱 데이터를 지운다.** 익명 세션이 함께 사라져 새 uid로 재발급되므로 서버에서도 미등록이 된다. 서버의 유저를 지우는 수단은 앱에 없다.

### 4-1. 등록 경로 (온보딩)

| # | 조작 | 기대 | 근거 |
|---|---|---|---|
| 1 | 앱 데이터를 지우고 `onboarding` 진입 | 프리필 없이 빈 상태. `저장`·`지우기` 비활성, **상단 바에 뒤로가기 버튼이 없다** | TS-001, [D29](research.md#d29-온보딩-진입에서-뒤로가기를-노출하지-않는다) |
| 2 | 진입 직후 로그캣 | `GET /api/v1/users/me`가 `401`(`USER_NOT_REGISTERED`)로 끝나고 **오류 스낵바가 뜨지 않는다** | [D38](research.md#d38-등록수정-분기--서버에-직접-묻고-캐시가-그-답을-들고-있는다)·[D39](research.md#d39-repository-표면--observeprofile--refreshprofile--saveprofile-세-멤버) |
| 3 | 시스템 back 제스처 | 화면이 닫히지 않는다 | EC-001 |
| 4 | `민` 입력 | 필드 오류 + `한글·영문 2글자 이상을 입력해주세요.`, `저장` 비활성 | TS-012 |
| 5 | `minho`로 수정 후 3번째 아바타 탭 | 오류가 사라지고 두 버튼 활성. 상단 썸네일이 3번으로 바뀐다 | TS-014·TS-016·TS-003 |
| 6 | `저장` 탭 | **`POST /api/v1/users`가 `201`로 끝나고** 화면이 닫힌다. 요청 본문의 `avatar.color`가 **`"orange"`**(3번째 아바타의 색)다 | FR-007, [API 계약 §2 아바타 값 표](contracts/profile-api-contract.md) |
| 7 | 저장 직후 `edit` 재진입 | 저장한 닉네임·아바타가 프리필된다 | TS-008, FR-006 |
| 8 | 개인방 확인 | `POST /api/v1/users`가 개인방(`내 장소`)을 함께 만들었다. **프로필 화면은 방 API를 호출하지 않지만**, 방 목록이 실서버로 붙어 있어(`GET /api/v1/rooms`) **`:feature:sharereceiver`의 방 선택 시트를 열면 `내 장소`가 보인다** — 그것으로 확인한다([D47](research.md#d47-develop-재대조--420의-대조가-옛-트리를-근거로-했다)) | FR-008, [D17](research.md#d17-개인방내-장소-생성--서버가-등록과-함께-처리한다확정) |

### 4-2. 수정 경로 (마이페이지)

| # | 조작 | 기대 | 근거 |
|---|---|---|---|
| 9 | `edit` 진입 | 캐시 값이 즉시 프리필되고, `GET /api/v1/users/me`가 `200`으로 끝나면 서버 값으로 한 번 더 정렬된다([D45](research.md#d45-프리필과-갱신의-순서--캐시로-먼저-채우고-갱신이-성공하면-조건부로-한-번-더)). **진입 직후 닉네임을 타이핑하면 갱신이 그것을 덮어쓰지 않는다** | FR-006 |
| 10 | 7번째 아바타 탭 | 썸네일이 7번으로 바뀐다. `checked`가 7번만 참이고 3번은 거짓(`uiautomator dump`) | TS-004, TS-019 |
| 11 | `지우기` 탭 | 닉네임 비고 아바타 해제, 썸네일이 기본 아바타로, 두 버튼 비활성 | TS-015 |
| 12 | 닉네임만 바꿔 저장 | **`PATCH /api/v1/users/me`가 `200`**. 이전 아바타가 유지되고 `avatar.color`가 그 아바타의 색으로 함께 나간다(부분 전송을 하지 않는다) | FR-007, FR-009 |
| 13 | `  minho  `로 고쳐 저장 후 재진입 | 프리필 값이 `minho`다(앞뒤 공백 없음). 요청 본문에도 공백이 없다 | EC-008 |
| 14 | 아바타 선택 없이 첫 저장(앱 데이터 삭제 후) | 요청의 `avatar.color`가 **`"red"`**(기본 아바타 = 첫 항목의 색). 재진입 시 기본 아바타가 선택 상태 | EC-002 |
| 15 | 값 수정 후 뒤로가기 | 확인 없이 닫히고 서버 값은 그대로다 | TS-009, EC-005 |
| 16 | 아무것도 바꾸지 않고 저장 | 같은 값으로 `PATCH`가 나가고 정상 복귀한다 | EC-006 |
| 17 | 앱을 강제 종료한 뒤 `edit` 재진입 | 캐시 값이 즉시 프리필되고, 갱신이 끝나면 서버 값으로 정렬된다 | FR-006, SC-003 |

### 4-3. 실패 경로 — **이번 개정에서 처음 기기로 확인된다**

| # | 조작 | 기대 | 근거 |
|---|---|---|---|
| 18 | 비행기 모드에서 `저장` 탭 | 스낵바로 실패를 알리고 **화면이 닫히지 않으며 입력값이 그대로 남는다** | FR-012, TS-006, EC-003·EC-007, SC-006 |
| 19 | 비행기 모드를 끄고 다시 `저장` | 다시 채워 넣지 않고 그대로 성공한다 | SC-006 |
| 20 | 16자 이상 닉네임으로 저장 | 클라이언트는 통과시키고 **서버가 거절해 저장 실패 스낵바가 뜬다**. 상태 코드를 로그캣에서 확인해 [API 계약 §2](contracts/profile-api-contract.md) 4번에 기록한다 | [D19](research.md#d19-닉네임-규칙-불일치--클라이언트는-spec을-따르고-서버-거절은-저장-실패로-받는다) |
| 21 | 느린 네트워크에서 `저장`을 연타 | 요청이 **한 번만** 나간다. `isSaving` 동안 두 번째 인텐트가 무시된다 | UX-003, EC-004 |
| 22 | 비행기 모드에서 `edit` 진입 | 캐시 값이 프리필되고 갱신 실패 스낵바가 뜬다. 화면은 그대로 쓸 수 있다 | [D39](research.md#d39-repository-표면--observeprofile--refreshprofile--saveprofile-세-멤버) |

- 20번은 **결과를 문서에 되먹이는 항목이다.** 서버가 어떤 코드로 거절하는지가 문서에 없어 실측이 유일한 근거다.

### 4-4. 스플래시 회귀 — **이번 개정이 남의 feature를 고치기 때문에 필요하다**

`UserApiService`·`UserRemoteDataSource`는 이미 머지된 splash-screen의 진입 게이트가 쓰는 코드다([D49](research.md#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다)). 프로필이 잘 돌아도 이 넷이 깨지면 **앱을 켜는 모든 사용자가 영향을 받는다.**

| # | 조작 | 기대 | 근거 |
|---|---|---|---|
| 23 | 앱 데이터 삭제 후 콜드 스타트 | 스플래시가 **온보딩**으로 보낸다(미등록 = `401` + `USER_NOT_REGISTERED`) | splash SC-002 |
| 24 | 23에서 프로필을 저장한 뒤 앱 재시작 | 스플래시가 **메인**으로 보낸다. 온보딩으로 다시 떨어지지 않는다 | splash SC-002 |
| 25 | 비행기 모드에서 콜드 스타트 | 스플래시가 오류 안내를 내고 **온보딩으로 보내지 않는다**(네트워크 실패를 미등록으로 뭉개지 않는다) | splash — `d783e03` 커밋 메시지의 판정 |

- **25번이 이 개정의 핵심 회귀 지점이다.** `401`이 아닌 실패를 `false`로 뭉개면 세션이 깨진 기존 사용자가 온보딩으로 떨어진다. 판정 코드가 `UserRemoteDataSourceImpl`에서 `UserApiService`로 옮겨가므로, 옮기는 과정에서 이 성질이 보존됐는지를 기기로 한 번 본다.
- 단위 테스트 쪽 대응은 §1의 `:feature:splash:testDebugUnitTest`와 `UserApiServiceTest`다.

## 5. 이번 범위에서도 확인할 수 없는 것

| 항목 | 이유 | 언제 |
|---|---|---|
| 앱 전체 반영(TS-011·SC-003)의 육안 확인 | 프로필을 표기하는 다른 화면이 아직 없다 | 마이페이지·코멘트 화면이 생긴 뒤. 지금은 `observeProfile()`의 방출을 단위 테스트로 본다 |
| 두 진입점의 실제 호출(FR-008·FR-009의 이동 부분)과 `RESULT_OK` 값의 관측 | 온보딩·마이페이지 feature가 없어 결과를 받을 호출자가 없다. `am start`는 결과 코드를 돌려주지 않는다 | 각 feature가 생길 때 |
| 진입점별 분기(FR-010)의 ViewModel 단위 검증 | JVM 테스트에서 `toRoute`가 항상 `null`을 돌려준다([D31](research.md#d31-viewmodel-단위-테스트는-isreturndefaultvalues로-열고-진입점은-통제하지-않는다)) | 대체 수단이 있다 — `ProfileUiState(entryPoint = ...)`를 직접 세워 검증하고, 실제 동작은 §4가 받는다 |
| `409 USER_ALREADY_REGISTERED`의 기기 재현 | 캐시가 비었는데 서버에는 유저가 있는 상태를 앱 조작만으로 만들 수 없다(앱 데이터를 지우면 익명 세션도 함께 사라진다) | 서버 쪽에서 상태를 만들어 주거나, `MockEngine` 테스트로 본다([D43](research.md#d43-테스트-범위--mockengine-기반-데이터-레이어-테스트를-더한다)) |
| `Person10` ↔ `brown` 대응이 **옳은지** | 나머지 11종은 배경 원 색이 디자인 시스템 토큰과 hex 단위로 일치하지만, `Person10`만 대응하는 토큰이 없어 남은 색으로 소거해 배정했다 | 디자인 확인([API 계약 §2](contracts/profile-api-contract.md) 디자인 확인 항목) |

**닫힌 항목** — plan 3.0.0의 §4가 들고 있던 것 중 저장 실패(FR-012 등), 중복 저장 차단의 눈 확인, 개인방 생성, 서버 반영, 닉네임 서버 거절은 모두 §4-1·4-3으로 옮겨 갔다.

## 6. 디자인 대조

[figma-design-fidelity.md](../../conventions/figma-design-fidelity.md)의 절차로 010-1(`2314-95662`)·010-2(`2314-95709`)·010-3(`2314-95754`) 세 노드와 대조한다. **이번 개정은 화면을 바꾸지 않으므로 새로 대조할 대상이 없다** — plan 3.0.0에서 끝난 대조가 그대로 유효하다.
