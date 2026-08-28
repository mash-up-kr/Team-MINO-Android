# 검증 가이드: 외부 공유 수신 방 선택 바텀시트

**대상 스펙 경로**: `docs/specs/shared-link-receiver`

**명세서**: [spec.md](./spec.md) · **계획**: [plan.md](./plan.md)

이 문서는 기능이 엔드투엔드로 동작함을 증명하는 **실행 가능한 검증 시나리오**다. 계약과 데이터 모델의 세부는 복제하지 않고 링크로 지목한다.

---

## 1. 선행 조건

| 항목 | 값 |
|---|---|
| 기기 | Android 실기기 또는 에뮬레이터. **인스타그램이 설치돼 있을 필요는 없다**(§3의 adb 경로를 쓴다) |
| 빌드 | `./gradlew :app:assembleQaDebug` — 헌법 §품질 게이트의 빌드 확인 최소선 |
| 계정 상태 | 온보딩을 마쳐 개인방(`내 장소`)을 보유한 상태. 공동방 5개 이상을 만들어 두면 `Full` 644dp 구간까지 검증할 수 있다 |
| 서버 | **실서버로 검증한다.** qa flavor의 `API_BASE_URL`(`https://api.gguk.org/`)에 두 엔드포인트가 모두 배포돼 있다(2026-08-28 확인) — [contracts/room-list-api.md](./contracts/room-list-api.md) · [contracts/shared-place-save-api.md](./contracts/shared-place-save-api.md) |
| 등록 상태 | 온보딩을 마쳐 `POST /api/v1/users` 등록이 끝난 상태. 미등록이면 두 요청이 모두 `401 USER_NOT_REGISTERED`를 받아 §4.5의 빈 목록 경로로 수렴한다 |

```sh
./gradlew :app:assembleQaDebug
adb install -r app/build/outputs/apk/qa/debug/app-qa-debug.apk
```

---

## 2. Lint

로컬 `lintDebug`는 JBR JIT 이슈로 데몬이 죽을 수 있다. 회피 플래그를 붙여 실행한다.

```sh
./gradlew lintDebug -Dorg.gradle.jvmargs="-XX:-TieredCompilation"
```

Compose Lint 위반 처리 규칙은 [`compose-lint.md`](../../conventions/compose-lint.md)를 따른다.

---

## 3. 공유 인텐트 주입

공유 시트를 실제로 거치지 않고 `ACTION_SEND`를 직접 던져 시트를 띄운다. 계약은 [contracts/share-intent.md](./contracts/share-intent.md)가 소유한다.

```sh
# 정상 — 문구 + URL
adb shell am start -a android.intent.action.SEND -t text/plain \
  --es android.intent.extra.TEXT "여기 진짜 맛있음 https://www.instagram.com/p/ABCDEFG/"

# URL 여러 개 (EC-003 — 첫 번째만 쓴다)
adb shell am start -a android.intent.action.SEND -t text/plain \
  --es android.intent.extra.TEXT "https://www.instagram.com/p/FIRST/ 그리고 https://www.instagram.com/p/SECOND/"

# URL 없음 (EC-002)
adb shell am start -a android.intent.action.SEND -t text/plain \
  --es android.intent.extra.TEXT "여기 진짜 맛있음"
```

실제 공유 시트 경로까지 확인하려면 인스타그램 게시물에서 [공유] → 꾹을 고른다(TS-001).

---

## 4. 시나리오별 검증

spec의 테스트 시나리오 ID와 1:1로 대응한다. 각 행의 판정은 spec의 `Then` 문장을 그대로 쓴다.

### 4.1 진입과 표출

| TS | 확인 방법 | 통과 판정 |
|---|---|---|
| TS-001 | §3의 정상 인텐트 | 딤 배경 위 `Peek` 시트. 헤더 두 줄이 보인다 |
| TS-009 | 에뮬레이터 망을 느리게 만든 뒤(`adb emu network delay gprs`) §3 | 시트가 즉시 뜨고 로딩·스피너가 보이지 않는다. 카드는 나중에 채워진다 |
| TS-022 | 앱을 완전히 종료(`adb shell am force-stop`) 후 §3 | 스플래시 없이 시트가 곧바로 뜬다 |
| TS-023 | 비행기 모드 + force-stop 후 §3 | 세션 대기·오류 화면 없이 시트가 뜬다. 방 목록 자리는 빈 목록 안내로 수렴한다([research.md R-006](./research.md)) |
| TS-027 | **force-stop 없이** 꾹을 실행해 아무 화면이나 띄운 뒤 홈으로 나가고, 다른 앱을 전면에 둔 채 §3 | 꾹의 화면이 전면으로 나오지 않는다. 시트 뒤에 보이는 것은 직전에 보고 있던 그 앱이다 |

```sh
adb shell am force-stop team.mino.android          # 콜드 스타트 재현
adb shell svc wifi disable && adb shell svc data disable   # 오프라인 재현
adb emu network delay gprs                          # 느린 망 재현 (에뮬레이터 한정)
```

**태스크 분리 확인 (TS-027 — [research.md R-023](./research.md))**

TS-027은 눈으로도 판정되지만, 통과 여부가 태스크 소속 하나에 달려 있으므로 직접 들여다본다.

```sh
adb shell am force-stop team.mino.android
adb shell monkey -p team.mino.android -c android.intent.category.LAUNCHER 1   # 앱을 띄워 태스크를 만든다
adb shell input keyevent KEYCODE_HOME
# → §3의 정상 인텐트 주입
adb shell dumpsys activity activities | grep -E "Task\{|realActivity"
```

`ShareReceiverActivity`가 **`MainActivity`와 다른 태스크의 루트**로 잡혀야 한다. 같은 태스크에 두 Activity가 쌓여 있으면 `taskAffinity=""`가 빠진 것이고, TS-027·TS-028이 모두 깨진다.

**전환 애니메이션** — 태스크가 분리되면 태스크 전환 애니메이션이 사라지지만, `windowAnimationStyle=@null`은 일부 OEM·버전에서 기본값으로 폴백한다. 시트가 뜨고 걷힐 때 화면이 밀리거나 페이드되는 흔적이 보이면 `windowEnterAnimation`·`windowExitAnimation`을 명시적으로 지정해야 한다.

qa 빌드는 `LogLevel.BODY`라 요청·응답이 로그캣에 그대로 찍힌다([`core/data/README.md`](../../../core/data/README.md) §4). 시트 표출 시각과 `GET /api/v1/rooms` 응답 시각을 비교해 UX-009·TS-009를 판정한다.

**SC-001 판정 — 1초 이내 조작 가능**

TS-009가 "대기 표현이 없다"를 보는 것과 달리, SC-001은 **시간을 잰다.** 인텐트 수신부터 시트가 조작 가능해질 때까지가 1초 이내여야 한다.

```sh
adb shell am force-stop team.mino.android      # 콜드 스타트로 재현 — 가장 불리한 조건이다
adb logcat -c
# → §3의 정상 인텐트 주입
adb logcat -v time | grep -E "ActivityTaskManager: START|Displayed team.mino"
```

`START` 로그와 `Displayed`(첫 프레임) 로그의 시각 차가 판정값이다. 세션 확인·방 목록 조회는 시트 표출을 붙잡지 않으므로([data-model.md §6](./data-model.md)), 이 값에 네트워크 시간이 섞이면 UX-009도 함께 깨진 것이다. 3회 측정해 모두 1초 이내여야 통과로 본다.

### 4.2 목록과 선택

| TS | 확인 방법 | 통과 판정 |
|---|---|---|
| TS-002 | 개인방 1 + 공동방 3 상태로 진입 | `내 장소`가 최상단. 카드 4개. 각 카드에 썸네일·이름·장소 수·체크박스 |
| TS-003 | 공동방 2개 체크 → `[저장하기]` | 시트가 닫히고 `저장이 완료됐습니다.` 토스트 |
| TS-004 | 아무것도 고르지 않고 `[저장하기]` 탭 | 반응 없음. 시트 유지 |
| TS-005 | 1개 체크 → 해제 | `[저장하기]`가 다시 비활성 |
| TS-015 | 방 6개 상태에서 끝까지 스크롤 후 마지막 방 체크 | 선택되고 `[저장하기]` 활성 유지 |
| TS-016 | `Full`에서 2개 체크 → 아래로 드래그해 `Peek` | 선택 상태 유지 |
| TS-026 | 이미 그 장소가 저장된 방을 포함해 진입 | 다른 카드와 시각적으로 구분되지 않고 정상 선택된다 |

**카드 탭 영역** — 체크박스뿐 아니라 카드 어디를 눌러도 토글되는지 확인한다(UX-003).

### 4.3 높이 단계

`Peek`·`Full` 높이는 고정 dp다. 계약은 [contracts/room-picker-sheet-ui.md §3.1](./contracts/room-picker-sheet-ui.md)이 소유한다.

| TS | 방 개수 | 조작 | 통과 판정 |
|---|---|---|---|
| TS-011 | 3개 이상 | 진입 직후 | 436dp. 카드 2개 온전 + 3번째 잘림 |
| TS-012 | 정확히 4개 | 위로 드래그 | 612dp. 카드 4개 모두 온전 |
| TS-013 | 5개 이상 | 위로 드래그 | 644dp. 카드 4개 온전 + 5번째 잘림 |
| TS-014 | 5개 이상 | `Full`에서 목록 스크롤 | 헤더·액션 영역 고정. 시트 높이 불변 |
| TS-020 | 1개 | 위로 드래그 | 방이 많을 때와 같이 `Full`로 승격. 아래 공간은 빔 |
| TS-021 | 3개 | `Peek` 스크롤 또는 `Full` 승격 | 두 경로 모두에서 3번째 카드 선택 가능 |

높이는 Layout Inspector로 실측한다. 디자인 대조 절차는 [`figma-design-fidelity.md`](../../conventions/figma-design-fidelity.md)를 따른다.

### 4.4 종료와 복귀

| TS | 확인 방법 | 통과 판정 |
|---|---|---|
| TS-006 | 저장 후 토스트가 사라질 때까지 대기 | 꾹의 화면이 남지 않고 직전 앱으로 복귀 |
| TS-008 | 1개 체크 후 뒤로가기 | 저장되지 않고 토스트 없이 복귀 |
| EC-001 | 딤 영역 탭 / 아래로 드래그 | 위와 같음 |
| TS-028 | §4.1의 TS-027 상태에서 저장하고 토스트가 사라질 때까지 대기 | 백그라운드에 있던 꾹의 화면이 아니라 공유를 시작한 앱으로 돌아간다 |
| EC-013 | 시트가 떠 있는 상태에서 §3의 정상 인텐트를 **다른 URL로 한 번 더** 주입 | 시트가 겹쳐 뜨지 않는다. 링크가 새 URL로 바뀌고 방 선택이 비워진다. 저장하면 나중 URL이 실린다 |
| EC-013 (URL 없음) | 시트가 떠 있는 상태에서 §3의 **URL 없는** 인텐트를 주입 | 떠 있는 시트가 그대로 유지된다. 닫히지 않는다 |

EC-013의 저장 URL 판정은 §5의 로그캣으로 한다 — 요청 본문에 실린 것이 **나중에 주입한 URL**이어야 한다.

`excludeFromRecents` 확인 — 종료 후 최근 앱 목록에 꾹의 공유 화면이 남지 않아야 한다. 앱을 실행 중인 상태에서 공유한 경우 **꾹의 원래 태스크는 최근 앱에 그대로 남아 있어야 한다** — 공유 흐름이 그것을 건드리지 않는다(spec §4 가정).

### 4.5 빈 목록 (FR-013)

| 재현 | 통과 판정 |
|---|---|
| TS-024 — 앱을 설치만 하고 실행하지 않은 기기에 §3 | 온보딩으로 전환되지 않고 시트가 뜬다. 안내 + `[저장하기]` 비활성 |
| TS-025 — 위 상태에서 뒤로가기 | 아무것도 저장되지 않고 복귀. 링크가 보관되지 않는다 |
| EC-011 — 앱 삭제 후 재설치, 첫 공유 | TS-024와 같은 화면 |
| 조회 실패 — §4.1의 오프라인 재현 | TS-024와 같은 화면 |

네 경우가 **같은 화면**이어야 한다. 구분해 보이면 [research.md R-006](./research.md) 위반이다.

---

## 5. 저장 요청 생존 검증

이 feature에서 가장 깨지기 쉬운 지점이다. spec §4 가정("앱을 떠나도 저장 요청은 취소되지 않는다")을 직접 확인한다.

### 5.1 Activity 종료 후에도 요청이 나가는가

```sh
adb shell dumpsys jobscheduler | grep -A5 "team.mino.android"
```

1. 방을 고르고 `[저장하기]`를 누른다.
2. 토스트가 사라지고 Activity가 종료된다.
3. 위 명령으로 예약된 작업이 남아 있는지 확인한다.

### 5.2 오프라인에서 예약되고 연결 시 실행되는가 (EC-009)

```sh
adb shell svc wifi disable && adb shell svc data disable
# → 공유 → 방 선택 → [저장하기]
adb shell dumpsys jobscheduler | grep -A5 "team.mino.android"   # 대기 중 확인
adb shell svc wifi enable
# → 로그에서 요청 전송 확인
```

`NetworkType.CONNECTED` 제약 덕에 오프라인에서는 실행되지 않고 대기해야 한다([research.md R-005](./research.md)).

### 5.3 프로세스가 죽어도 살아남는가

```sh
# [저장하기] 직후, 토스트가 뜬 상태에서
adb shell am force-stop team.mino.android
adb shell dumpsys jobscheduler | grep -A5 "team.mino.android"   # 작업이 남아 있어야 한다
```

WorkManager를 도입한 이유가 이 시나리오다. 작업이 사라지면 R-004의 선택이 무의미해진다.

### 5.4 방을 여러 개 골라도 요청은 하나인가 (TS-019)

방 2개를 체크하고 `[저장하기]`를 누른 뒤, 로그캣에서 `POST /api/v1/rooms/pins`가 **정확히 한 번** 나가고 그 본문의 `roomIds`에 **고른 방 두 개가 모두** 실렸는지 확인한다.

```sh
adb logcat | grep -A5 "rooms/pins"
```

방마다 요청이 따로 나가면 [research.md R-021](./research.md) 위반이다 — 서버가 배열을 받으므로 분해는 서버 몫이다.

> **부분 실패(TS-019)는 여기서 관측되지 않는다.** `202`는 요청 전체의 접수만 알리고, 어느 방이 성공·중복·실패인지는 서버가 저장을 확정한 뒤 알림함으로 전달한다([contracts/shared-place-save-api.md §2](./contracts/shared-place-save-api.md)). 알림함은 spec §3.2가 [SCR-007]로 넘긴 비목표다.

### 5.5 4xx는 재시도하지 않는가

**실서버로는 결정적으로 재현할 수 없다.** 시트에는 내가 속한 방만 뜨므로 `403`을 만들 수 없고, `400`을 유도할 입력도 서버 판정에 달려 있다. 이 판정은 실기기가 아니라 단위 테스트가 소유한다.

| 검증 | 수단 |
|---|---|
| `4xx` → 재시도 없이 `failure()` | `androidx.work:work-testing`의 `TestListenableWorkerBuilder` + `ktor-client-mock`. `:core:data`의 `DomainExceptionMappingTest`·`IdentityProofAttachmentTest`가 같은 구성을 쓴다 |
| `5xx`·네트워크 오류 → `retry()` | 위와 같음 |
| 도메인 예외가 아닌 예외 → 전파 | 위와 같음 ([research.md R-016](./research.md)) |

실기기에서 우연히 `4xx`가 관측되면 로그캣에서 같은 `roomId`로 재요청이 나가지 않는지만 확인한다.

---

## 6. 이관·승격 검증

컴포넌트와 에셋이 두 방향으로 움직인다. 어느 쪽도 남은 참조를 만들지 않아야 한다.

### 6.1 방 카드 이관 (`:feature:sample` → `:core:design-system`)

[research.md R-010](./research.md)의 이관이 무엇도 깨뜨리지 않았는지 확인한다.

```sh
# sample에 방 카드 잔재가 없어야 한다
grep -rn "MinoRoomCard\|MinoRoomCheckBoxCard\|RoomCardContent\|RoomCardTokens" feature/sample/

# design-system에서 참조가 닫혀야 한다
grep -rn "team.mino.feature.sample" core/design-system/

./gradlew :app:assembleQaDebug
```

- 첫 두 명령의 결과가 **모두 비어 있어야** 한다.
- `:core:design-system`의 Preview에서 `MinoRoomCheckBoxCard`가 체크/미체크·메모 있음/없음 네 조합으로 렌더되는지 Android Studio Preview로 확인한다.

### 6.2 썸네일 폴백 승격 (`:feature:roomform` → `:core:common:ui`)

[research.md R-019](./research.md)의 승격을 확인한다.

```sh
# roomform에 캐릭터 에셋과 매핑이 남아 있지 않아야 한다
find feature/roomform/src/main/res -name "room_thumbnail_*"
grep -rn "thumbnailRes" feature/roomform/

# 에셋이 밀도 3벌 모두 옮겨졌어야 한다 (각 13개)
ls core/common/ui/src/main/res/drawable-mdpi/room_thumbnail_* | wc -l
ls core/common/ui/src/main/res/drawable-xhdpi/room_thumbnail_* | wc -l
ls core/common/ui/src/main/res/drawable-xxhdpi/room_thumbnail_* | wc -l

# design-system이 도메인도 래스터 에셋도 갖지 않아야 한다
grep -rn "core:domain" core/design-system/build.gradle.kts
find core/design-system/src/main/res -name "*.webp"
```

- 첫 두 명령과 마지막 두 명령의 결과가 **모두 비어 있어야** 한다. 밀도별 개수는 셋 다 13이다.
- 방 생성 폼(`:feature:roomform`)에서 색을 바꿔가며 미리보기 썸네일이 그대로 바뀌는지 확인한다 — 승격이 기존 화면을 건드리지 않았음을 보는 것이 이 확인의 목적이다. 색을 고르지 않은 상태의 회색 썸네일도 함께 본다.

---

## 7. 완료 판정

| 항목 | 기준 |
|---|---|
| 빌드 | `./gradlew :app:assembleQaDebug` 성공 |
| Lint | 위반 0건 또는 문서화된 예외 |
| 시나리오 | §4의 TS 전 항목 통과 |
| 태스크 분리 | §4.1의 `dumpsys` 확인 — `ShareReceiverActivity`가 `MainActivity`와 다른 태스크의 루트 |
| 성능 | §4.1의 SC-001 측정 3회 모두 1초 이내 |
| 요청 생존 | §5.1~5.3 전 항목 통과 |
| 요청 단위 | §5.4 — 방을 여러 개 골라도 요청 1건에 roomIds가 모두 실린다 |
| 재시도 정책 | §5.5의 단위 테스트 통과 |
| 이관·승격 | §6.1·§6.2의 grep 결과 비어 있음 |
| 디자인 | Figma `013-1` 실측과 높이·문구·카드 구성 일치 |

미검증으로 남는 것 — **저장 결과의 성공·중복·실패**는 서버가 비동기로 확정하고 알림함으로 전달하므로(FR-014·FR-015) 이 feature의 검증 범위 밖이다. 알림함 화면은 spec §3.2가 [SCR-007]로 넘겼다.
