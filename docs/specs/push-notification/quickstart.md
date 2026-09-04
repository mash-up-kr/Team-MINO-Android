# Quickstart: 푸시 알림 검증

이 문서는 구현이 끝났음을 실기기에서 확인하는 절차다. 계약·데이터 모델의 세부는 [contracts/](contracts/)·[data-model.md](data-model.md)를 참고한다.

## 0. 선행 조건

- `google-services.json`이 `:app`에 배치돼 있다(`AndroidFirebaseConventionPlugin`이 이미 적용 중).
- 실기기 또는 Google Play 서비스가 있는 에뮬레이터. FCM은 순정 에뮬레이터(Play 서비스 없음)에서 토큰이 발급되지 않는다.
- 테스트 계정의 알림 권한이 허용돼 있다(`POST_NOTIFICATIONS`, [SCR-008] 마이페이지 스위치 — 이 feature 밖 경로). 그 스위치(#152)가 아직 없으면 수동으로 켠다 — 기기 설정 → 앱 → 알림, 또는 `adb shell pm grant <applicationId> android.permission.POST_NOTIFICATIONS`(`applicationId`는 flavor 접미어까지 포함한 실제 패키지명). 매니페스트 선언은 SDK 병합이 맡는다([research.md D12](research.md#d12-post_notifications-선언은-fcm-sdk의-매니페스트-병합에-맡긴다)).

---

## 1. 토큰 등록 확인 (FR-001~FR-005, TS-001~TS-005)

1. 앱을 새로 설치하고 첫 실행 → 온보딩을 완료해 프로필을 등록한다([contracts/push-token-api.md](contracts/push-token-api.md) §1 — 등록 전엔 `USER_NOT_REGISTERED` 401을 받을 수 있다).
2. 앱을 종료하고 다시 실행한다 — 이 두 번째 실행이 성공적인 등록 시점이다.
3. `Logcat`에서 `PUT /api/v1/users/me/push-token` 요청·200 응답을 확인한다(Ktor 로깅 플러그인, 기존 `HttpClientConfig`).
4. 서버 로그 또는 DB에서 이 설치의 유저에게 등록된 토큰 값이 있는지 확인한다(서버팀 협조 필요 — 이 앱은 조회 API를 갖지 않는다).
5. (선택) Firebase Console → Cloud Messaging → 이 토큰으로 테스트 메시지를 보내 도달을 확인한다. 이것은 토큰이 유효한지만 증명한다 — 콘솔 메시지는 `notification` 페이로드라 시스템이 알림을 그리며, 이 feature의 표시·탭 경로(§2)는 지나지 않는다.

**실패 주입**: 기기를 비행기 모드로 두고 앱을 시작한다 → 오류 화면·토스트가 뜨지 않고 앱이 정상 이용 가능함을 확인한다(FR-004, UX-002).

---

## 2. 알림 표시·탭 진입 확인 (FR-006~FR-014, 유저 플로우 2~4)

Firebase Console의 테스트 메시지는 `notification` 페이로드만 보낼 수 있어 이 feature가 요구하는 data-only 메시지([research.md D1](research.md#d1-fcm-메시지는-data-only-페이로드를-전제한다))를 재현하지 못하고, 서버 OpenAPI에도 발송 오퍼레이션이 없다. 그래서 **실사용 시나리오를 1차 수단**으로 쓴다 — 인스타그램 공유 → 저장은 서버가 비동기로 확정하고 그 결과(중복·실패)를 실제 푸시로 보내므로(`docs/specs/shared-link-receiver/spec.md` FR-014·FR-015·FR-017), 공유 한 번이 곧 실제 발송이다. 실사용으로 일으킬 수 없는 세 갈래만 §2.0의 합성 발송으로 보낸다.

### 2.0 발송 수단

| 수단 | 일으키는 `type` | 방법 |
|---|---|---|
| **실사용 — 인스타그램 공유** | `PIN_DUPLICATED` | 이미 저장된 장소의 게시물을 같은 방으로 다시 공유해 [저장하기] |
| | `SAVE_FAILED` | 장소 정보가 없는 게시물이나 한국 밖 장소 링크를 공유(shared-link-receiver EC-007·EC-008) |
| **실사용 — 초대 링크** | `ROOM_MEMBER_JOINED` · `ROOM_JOINED_SELF` | 두 번째 기기(또는 앱 데이터를 지운 재설치)가 초대 링크로 방에 참가 |
| **합성 — Firebase Admin SDK** | `NEARBY_PLACE` · `NEARBY_PLACE_SUMMARY` · `TOP_COMMENTED_PLACE` · 모르는 `type` | 서버가 위치·코멘트 집계로 보내는 유형이라 앱이 유발할 수 없다. 아래 스크립트로 보낸다. 서비스 계정 키(JSON)가 로컬에 필요하며 저장소에 넣지 않는다. 키를 확보하지 못하면 이 갈래는 "서버 발송 조건 미확인으로 실기기 미검증"으로 기록하고 넘어간다 — 파싱·라우팅 표는 도메인 단위 테스트(tasks T008·T010)가 전 행을 덮는다 |

앱 상태는 실사용 경로에서 이렇게 만든다 — 공유 후 인스타그램으로 돌아가면 **백그라운드**, [저장하기] 직후 최근 앱에서 꾹을 스와이프하면 **종료 상태**, 저장 후 바로 꾹을 열어 임의 화면을 보고 있으면 **사용 중**이다.

합성 발송 스크립트(`pip install firebase-admin`). `data`는 [contracts/push-payload-contract.md §2](contracts/push-payload-contract.md#2-예시-사용자-제공-캡처-그대로)를 그대로 옮기고 `notification` 필드를 넣지 않는다(D1). 등록 토큰은 §1 절차 3의 요청 본문(Ktor 로깅)에서 읽는다.

```python
import firebase_admin
from firebase_admin import credentials, messaging

firebase_admin.initialize_app(credentials.Certificate("<service-account.json>"))
messaging.send(messaging.Message(
    token="<등록 토큰>",
    data={"type": "NEARBY_PLACE_SUMMARY", "title": "근처에 저장한 곳 3개가 있어요", "body": "반경 3km"},
))
```

### 2.1 시나리오

| 시나리오 | 사전 상태 | 보낼 `type` | 재현 수단(§2.0) | 기대 결과 | 관련 TS |
|---|---|---|---|---|---|
| 백그라운드 수신 | 앱을 백그라운드로 보낸다 | `PIN_DUPLICATED` | 실사용 | 알림 영역에 유형 문구가 뜬다 | TS-006 |
| 종료 상태 수신 | 앱을 완전히 종료(최근 앱 목록에서 스와이프) | `PIN_DUPLICATED` | 실사용 | 알림 영역에 뜬다 | TS-007 |
| 장소 알림 탭 | `PIN_DUPLICATED` 도착 | `PIN_DUPLICATED` | 실사용 | 탭 → 그 `pinId`의 장소 상세가 보인다. **실제 payload에 `pinId`가 실려 오는지 함께 확인한다**(계약 재확인) | TS-008 |
| 공동방 알림 탭 | `ROOM_MEMBER_JOINED` 도착 | `ROOM_MEMBER_JOINED` | 실사용(기기 2대) | 탭 → 그 `roomId`의 방 상세가 보인다 | TS-009 |
| 저장 오류 알림 탭 | `SAVE_FAILED` 도착 | `SAVE_FAILED` | 실사용 | 탭 → 알림 탭 목록(placeholder)이 보인다. 안내 화면으로 바로 들어가지 않는다 | TS-010 |
| 종료 상태에서 탭 | 앱 완전 종료 후 알림 탭 | `PIN_DUPLICATED` | 실사용 | 스플래시 → 세션 확보 → 장소 상세 | TS-011 |
| 탭한 알림 소거 | 알림 탭 | 아무거나 | 실사용 | 알림 영역에서 사라진다(OS 표준 동작) | TS-012 |
| 권한 없음 | 알림 권한 거부 상태 | 아무거나 | 실사용 | 알림이 뜨지 않는다. 권한 요청·대체 UI 없음 | TS-013 |
| 채널 단일 확인 | 알림을 한 번 이상 표시 | — | — | 기기 설정 → 앱 알림에 채널 하나만 보인다 | TS-014 |
| 사용 중 수신 | 임의 화면을 보는 중 | 아무거나 | 실사용 | 알림이 뜨고 화면 전환은 없다 | TS-015, TS-016 |
| 실행 중 탭 | 앱을 보는 중 또는 백그라운드(프로세스 살아 있음) | `PIN_DUPLICATED` | 실사용 | **스플래시 없이** 보고 있던 화면 위에서 저장 탭으로 바뀌고 장소 상세가 열린다. 뒤로가기로 이전 탭 상태가 살아 있다(D13·D15) | TS-008, SC-004 |
| 스플래시 진행 중 탭 | 앱을 완전히 종료한 뒤 런처로 막 켜서 스플래시가 보이는 동안 | `PIN_DUPLICATED`(미리 도착시켜 둔 알림) | 실사용 | 스플래시가 **한 번만** 보이고(겹치지 않음) 그대로 장소 상세에 도달한다. Logcat에 세션 확보 요청이 한 번만 나간다(D16) | TS-011 |
| 폼을 열어 둔 채 탭 | 방 만들기 폼(`RoomFormActivity`)을 열어 둔 상태 | 아무거나 | 실사용 | 폼이 닫히고 Main 위에서 도착지가 열린다. 입력 중이던 폼 내용은 사라진다 — 의도된 동작(D14) | — |
| 대표 알림 | 반경 안 저장 장소 3개 | `NEARBY_PLACE_SUMMARY` | 합성 | 알림 1건. 탭 → 알림 탭 목록 | TS-018, TS-019 |
| 모르는 유형 | — | 미등록 문자열(예: `"UNKNOWN_TYPE"`) | 합성 | 알림이 뜨지 않는다 | EC-008 |

**백스택 확인** (D13·D14): (1) 실행 중 탭 — 뒤로가기를 눌러도 중복된 `MainActivity`나 `SplashActivity`가 스택에 없다(`singleTask` 재사용). (2) 종료 상태 탭 — 스플래시가 뜨기 전에 Main의 빈 화면이 눈에 띄게 보이지 않고(`setContent` 전 우회), 도착지에서 뒤로가기를 눌러도 우회에 쓰인 첫 `MainActivity`가 스택에 남아 있지 않다(`finish()` 확인).

---

## 3. 자동 테스트로 옮기지 않는 것

- 알림 영역에 실제로 표시되는지는 `NotificationManager` 상태 조회가 계측 테스트에서 불안정해 수동 확인으로 남긴다.
- `FirebaseMessagingService.onMessageReceived`는 시스템이 호출하는 콜백이라 유닛 테스트는 `ParsePushMessageUseCase`·`ResolvePushDestinationUseCase`(순수 함수)까지만 커버한다 — 알림 빌드·`PendingIntent` 조립은 계측/수동 검증이다.
