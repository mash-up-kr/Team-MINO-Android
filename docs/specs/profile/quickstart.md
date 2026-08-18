# 검증 가이드: 프로필 설정 및 수정

Phase 1 산출물. 이 기능이 엔드투엔드로 동작함을 확인하는 절차만 담는다. 상태·계약의 정의는 [`data-model.md`](data-model.md)·[`contracts/`](contracts/)에 있다.

## 선행 조건

- 이 저장소의 표준 빌드 환경(JDK·Android SDK)이 구성되어 있다.
- `:feature:profile`이 `settings.gradle.kts`와 `:app`에 등록되어 있다.
- **qa flavor로 빌드한다.** qa는 Ktor `MockEngine`을, prod는 실서버용 `OkHttp`를 쓴다([API 계약 §4](contracts/profile-api-contract.md)). 실서버·인증이 아직 없으므로 이번 검증은 전부 qa에서 한다.
- 온보딩·마이페이지 feature는 아직 없다. 두 진입점은 §3의 방법으로 흉내 낸다.

## 1. 빌드·단위 테스트

```bash
./gradlew :core:domain:test :core:data:test          # 검증·등록/수정 분기·원격 매핑 (contracts/profile-repository-contract.md §테스트 계약)
./gradlew :app:assembleQaDebug                        # 빌드 최소선 (헌법 §품질 게이트)
```

- 로컬 `lintDebug`는 JBR 이슈로 데몬이 죽을 수 있다. 실패해도 코드 문제로 단정하지 않고, 검증이 수행된 것으로도 보지 않는다(헌법 §검증 장치의 한계).
- prod 소스셋에 목이 새지 않았는지는 `./gradlew :app:assembleProdDebug`가 컴파일되는 것으로 확인한다.

## 2. 화면 단독 확인 (Preview)

`ProfileScreen`은 stateless이므로 `@UiModePreviews`로 상태별 렌더를 확인한다.

| 프리뷰 | 상태 | 대응 시나리오 |
|---|---|---|
| 진입 직후 | 빈 닉네임, 미선택, 두 버튼 비활성 | TS-001 |
| 입력 완료 | 유효 닉네임 + 아바타 선택 | TS-002·TS-003 |
| 입력 오류 | `민` 입력, 오류 문구 노출 | TS-012 |
| 저장 중 | `isSaving = true` | UX-003, EC-004 |

## 3. 기기·에뮬레이터 실전 확인 (목 기반)

온보딩·마이페이지가 없으므로 `ProfileActivity`를 `adb`로 직접 연다.

```bash
# 온보딩 진입(뒤로가기 차단) — 목 서버가 비어 있으면 POST /api/v1/users 로 나간다
adb shell am start -n team.mino.qa/team.mino.feature.profile.ProfileActivity \
  --es profile_entry_point onboarding

# 마이페이지 진입(뒤로가기 허용·프리필) — 캐시에 프로필이 있으면 PATCH /api/v1/users/me 로 나간다
adb shell am start -n team.mino.qa/team.mino.feature.profile.ProfileActivity \
  --es profile_entry_point edit
```

> 컴포넌트 이름·applicationId는 구현 시점의 flavor 설정에 맞춰 조정한다. `ProfileActivity`가 `exported=false`라면 확인용으로만 임시 노출하고 커밋하지 않는다.

목 요청·응답은 qa 빌드의 Ktor `Logging`(`LogLevel.BODY`)으로 Logcat에 찍힌다. 어떤 엔드포인트가 나갔는지는 여기서 확인한다.

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
| 9 | `  민호  ` 입력 후 저장 | 요청 본문의 `nickname`이 `민호`다(Logcat) | EC-008 |
| 10 | 아바타 없이 저장 | 요청 본문의 `avatar.id`가 기본 아바타 id다 | EC-002 |
| 11 | 저장 완료 | `POST /api/v1/users` 201 후 화면이 닫히고 결과가 `RESULT_OK`다 | TS-005, FR-008 |
| 12 | `edit`으로 재진입 | 저장된 닉네임·아바타가 프리필된다 | TS-008, FR-006 |
| 13 | 값 수정 후 저장 | `PATCH /api/v1/users/me` 200이 나가고 화면이 닫힌다 | TS-010, FR-009 |
| 14 | `edit`에서 값 수정 후 뒤로가기 | 확인 없이 닫히고 저장 값은 그대로다 | TS-009, EC-005 |
| 15 | 저장 중 `저장` 연타 | 요청이 한 번만 나간다(Logcat) | UX-003, EC-004 |
| 16 | 아무것도 바꾸지 않고 저장 | 같은 값으로 `PATCH`가 나가고 정상 복귀한다 | EC-006 |
| 17 | 앱 종료 후 `edit` 재진입 | 목 서버는 비었지만 캐시 값으로 프리필된다 | FR-006, research.md D16 |

### 저장 실패 재현

목의 **강제 실패 스위치**를 켜고 저장한다([API 계약 §4](contracts/profile-api-contract.md)).

| # | 기대 | 근거 |
|---|---|---|
| 18 | 화면이 닫히지 않고 입력한 닉네임·아바타가 그대로 남는다 | FR-012, TS-006, EC-003·EC-007 |
| 19 | 스낵바로 실패가 안내된다 | FR-012 |
| 20 | 다시 `저장`을 누를 수 있다(`isSaving`이 풀려 있다) | SC-006 |
| 21 | 실패 후 `edit` 재진입 시 이전 저장 값이 유지된다(캐시 미갱신) | contracts/profile-repository-contract.md |

## 4. 아직 확인할 수 없는 것

| 항목 | 이유 | 언제 |
|---|---|---|
| 실서버 연동·인증 헤더 | 인증 계약 미확정(research.md D20) | 인증 설계 문서 확정 후 qa 엔진을 `OkHttp`로 전환할 때 |
| 개인방(`내 장소`) 실제 생성 | 서버가 등록과 함께 처리하며 응답에 포함되지 않는다(research.md D17) | 방 목록 화면이 생긴 뒤 |
| 앱 전체 반영(TS-011·SC-003) | 프로필을 표기하는 다른 화면이 아직 없다 | 마이페이지·코멘트 화면이 생긴 뒤. 지금은 `observeProfile()`의 방출을 단위 테스트로 본다 |
| 16자 이상·공백 포함 닉네임의 서버 거절 | 목은 닉네임을 검증하지 않는다(research.md D19) | 실서버 연결 후 |

## 5. 디자인 대조

구현이 끝나면 [figma-design-fidelity.md](../../conventions/figma-design-fidelity.md)의 절차로 010-1·010-2·010-3 세 노드와 대조한다. 새로 만든 `MinoProfileAvatarImage`·`MinoTopNavigation`의 값 판정(토큰 / 실측)이 이 대조의 대상이다.
