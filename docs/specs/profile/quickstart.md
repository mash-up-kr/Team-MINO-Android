# 검증 가이드: 프로필 설정 및 수정

Phase 1 산출물. 이 기능이 엔드투엔드로 동작함을 확인하는 절차만 담는다. 상태·계약의 정의는 [`data-model.md`](data-model.md)·[`contracts/`](contracts/)에 있다.

> **이번 범위의 "엔드투엔드"**: 화면 → ViewModel → UseCase → Repository → 로컬 DataStore까지다. 원격 API는 연결하지 않으므로([research.md D22](research.md#d22-이번-범위의-저장소--로컬-datastore-단독-원격-연동은-후속-작업)) 서버 왕복은 이 가이드의 검증 대상이 아니다. 확인할 수 없는 항목은 §4에 모았다.

## 선행 조건

- 이 저장소의 표준 빌드 환경(JDK·Android SDK)이 구성되어 있다.
- `:feature:profile`이 `settings.gradle.kts`와 `:app`에 등록되어 있다.
- flavor는 아무거나 좋다. 네트워크를 쓰지 않으므로 qa·prod가 같게 동작한다.
- 온보딩·마이페이지 feature는 아직 없다. 두 진입점은 §3의 방법으로 흉내 낸다.

## 1. 빌드·단위 테스트

```bash
./gradlew :core:domain:test :core:data:test          # 검증·저장 왕복 ([repository 계약](contracts/profile-repository-contract.md) §테스트 계약)
./gradlew :app:assembleQaDebug                        # 빌드 최소선 (헌법 §품질 게이트)
```

- 로컬 `lintDebug`는 JBR 이슈로 데몬이 죽을 수 있다. 실패해도 코드 문제로 단정하지 않고, 검증이 수행된 것으로도 보지 않는다(헌법 §검증 장치의 한계).
- `:feature:profile`의 ViewModel 테스트는 `./gradlew :feature:profile:testDebugUnitTest`로 돈다.

## 2. 화면 단독 확인 (Preview)

`ProfileScreen`은 stateless이므로 `@UiModePreviews`로 상태별 렌더를 확인한다.

| 프리뷰 | 상태 | 대응 시나리오 |
|---|---|---|
| 진입 직후 | 빈 닉네임, 미선택, 두 버튼 비활성 | TS-001 |
| 입력 완료 | 유효 닉네임 + 아바타 선택 | TS-002·TS-003 |
| 입력 오류 | `민` 입력, 오류 문구 노출 | TS-012 |
| 저장 중 | `isSaving = true` | UX-003, EC-004 |

- 마이페이지 진입(뒤로가기 활성)과 온보딩 진입(뒤로가기 비활성)을 각각 한 장씩 둔다 — FR-010의 차이가 프리뷰만으로 드러나야 한다.

## 3. 기기·에뮬레이터 실전 확인

온보딩·마이페이지가 없으므로 `ProfileActivity`를 `adb`로 직접 연다.

```bash
# 온보딩 진입(뒤로가기 차단)
adb shell am start -n team.mino.qa/team.mino.feature.profile.ProfileActivity \
  --es profile_entry_point onboarding

# 마이페이지 진입(뒤로가기 허용·프리필)
adb shell am start -n team.mino.qa/team.mino.feature.profile.ProfileActivity \
  --es profile_entry_point edit
```

> 컴포넌트 이름·applicationId는 구현 시점의 flavor 설정에 맞춰 조정한다. `ProfileActivity`가 `exported=false`라면 확인용으로만 임시 노출하고 커밋하지 않는다.

### 확인 항목

| # | 조작 | 기대 | 근거 |
|---|---|---|---|
| 1 | 온보딩 진입 직후 아무것도 하지 않는다 | `저장`·`지우기` 비활성, 뒤로가기 비활성 | TS-001 |
| 2 | 시스템 back 제스처 | 화면이 닫히지 않는다 | EC-001 |
| 3 | 닉네임에 `민` 입력 | 필드 오류 상태 + `한글·영문 2글자 이상을 입력해주세요.`, `저장` 비활성 | TS-012 |
| 4 | `abc1` 입력 | 오류 상태 유지 | TS-013 |
| 5 | `민호`로 수정 | 오류 사라지고 `저장` 활성, `지우기`는 비활성 | TS-014·TS-016 |
| 6 | 3번째 아바타 탭 | 그 아바타만 선택되고 상단 썸네일이 바뀐다. `지우기` 활성 | TS-003, FR-005 |
| 7 | 7번째 아바타 탭 | 7번째만 선택, 3번째 해제 | TS-004 |
| 8 | `지우기` 탭 | 닉네임 비고 아바타 해제, 썸네일이 기본 아바타로, 두 버튼 비활성 | TS-015 |
| 9 | 한글 30자 입력 | 오류 없이 `저장` 활성 | TS-017 |
| 10 | `저장` 탭 | 화면이 닫히고 결과가 `RESULT_OK`다 | TS-005, FR-008(이동 부분) |
| 11 | `edit`으로 재진입 | 저장된 닉네임·아바타가 프리필된다 | TS-008, FR-006 |
| 12 | `  민호  `로 고쳐 저장 후 `edit` 재진입 | 프리필된 값이 `민호`다(앞뒤 공백 없음) | EC-008 |
| 13 | 아바타를 지운 적 없이 닉네임만 바꿔 저장 | 이전 아바타가 유지된다 | FR-007 |
| 14 | 아바타 선택 없이 첫 저장 후 `edit` 재진입 | 기본 아바타(첫 항목)가 선택 상태다 | EC-002 |
| 15 | `edit`에서 값 수정 후 뒤로가기 | 확인 없이 닫히고 저장 값은 그대로다 | TS-009, EC-005 |
| 16 | 아무것도 바꾸지 않고 저장 | 같은 값으로 저장되고 정상 복귀한다 | EC-006 |
| 17 | 앱을 강제 종료한 뒤 `edit` 재진입 | 저장 값이 그대로 프리필된다 | FR-006 |
| 18 | 앱 데이터를 지우고 `onboarding` 진입 | 프리필 없이 빈 상태다 | spec §4(세션은 앱 설치에 묶인다) |

## 4. 이번 범위에서 확인할 수 없는 것

원격을 연결하지 않아 생기는 공백이다. 설계로 메우지 않고 여기에 드러낸다.

| 항목 | 이유 | 언제 |
|---|---|---|
| 저장 실패 시 화면·입력값 유지(FR-012, TS-006, EC-003·EC-007, SC-006) | 로컬 저장에는 실패 원천이 사실상 없다 | 원격 연동 작업. 그전까지는 Fake Repository가 예외를 던지는 단위 테스트로만 본다([research.md D25](research.md#d25-저장-실패-경로--통로는-지금-배선하고-발화-원천은-후속-작업에-남긴다)) |
| 중복 저장 차단(UX-003, EC-004)의 눈 확인 | 로컬 저장이 즉시 끝나 `isSaving`이 보이는 시간이 거의 없다 | 같은 시점. 지금은 ViewModel 단위 테스트로 본다 |
| 개인방(`내 장소`) 생성(FR-008의 트리거 부분) | 서버가 등록 요청과 함께 만드는데 그 요청이 없다 | 원격 연동 작업([research.md D17](research.md#d17-개인방내-장소-생성--서버가-등록과-함께-처리한다확정)) |
| 서버 반영(spec §4 가정) | 원격 미연결 | 원격 연동 작업 |
| 16자 이상·공백 포함 닉네임의 서버 거절 | 거절할 서버가 없어 그대로 저장된다 | 원격 연동 작업. 그전에 spec 정리 권장([research.md D19](research.md#d19-닉네임-규칙-불일치--클라이언트는-spec을-따르고-서버-거절은-저장-실패로-받는다)) |
| 앱 전체 반영(TS-011·SC-003) | 프로필을 표기하는 다른 화면이 아직 없다 | 마이페이지·코멘트 화면이 생긴 뒤. 지금은 `observeProfile()`의 방출을 단위 테스트로 본다 |
| 두 진입점의 실제 호출(FR-008·FR-009의 이동 부분) | 온보딩·마이페이지 feature가 없다 | 각 feature가 생길 때. 지금은 `RESULT_OK` 반환까지만 본다 |

## 5. 디자인 대조

구현이 끝나면 [figma-design-fidelity.md](../../conventions/figma-design-fidelity.md)의 절차로 010-1·010-2·010-3 세 노드와 대조한다. 새로 만든 `MinoProfileAvatarImage`·`MinoTopNavigation`의 값 판정(토큰 / 실측)이 이 대조의 대상이다.
