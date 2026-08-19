# 리서치: 마이페이지 & 환경설정

Phase 0 산출물. `plan.md` 1.0.0에서 발생한 결정을 모은다. 이 feature 안에서만 유효한 선택은 여기, 다른 feature에도 구속력을 갖는 결정은 [`docs/adr/`](../../adr/README.md) 승격 대상이다(완료 보고에서 표시).

---

## ~~D1. 프로필 저장소 — 로컬(DataStore) 단독, 원격 API 없음~~ 재검토됨(plan 3.0.0)

- ~~**Decision**: `Profile`(닉네임+아바타)은 원격 API 없이 `core:data`의 공유 DataStore(`storage/DataStoreModule`)에만 저장한다.~~
- ~~**Rationale**: 저장소 전반에 프로필 관련 네트워크 계약(DTO·엔드포인트)이 아직 없다. spec의 완료 조건(FR-003)도 "마이페이지·앱 전체 표기에 즉시 반영"만 요구할 뿐 서버 동기화를 요구하지 않는다. `ProfileRepository` 인터페이스로 감싸두면 추후 실제 프로필 API가 생겨도 `ProfileRepositoryImpl`만 교체하면 되므로 지금 서버 계약을 지어내지 않는다.~~
- ~~**Alternatives considered**: (a) `GithubRepository`처럼 임시 원격 API를 지어내 연결 — 존재하지 않는 계약을 임의로 만드는 것이라 기각. (b) 온보딩 모듈과 공유 DB 테이블 설계 — 온보딩 모듈 자체가 아직 없어 시기상조.~~
- **재검토 사유(plan 3.0.0)**: 이 결정이 "생기면 교체"라고 예고했던 실제 프로필 API가 백엔드 `swagger.yaml`(`GET/PATCH /api/v1/users/me`)로 문서화됐다. 존재하지 않는 계약을 지어내는 문제가 해소됐으므로 원격 우선으로 뒤집는다. 새 결정은 D9 참조.
- (plan 1.0.0에서 결정, plan 3.0.0에서 재검토)

## D2. 알림/위치 스위치의 "끄기" 동작 — 서로 다른 저장 계층

- **Decision**: 알림 스위치는 OS 권한(`POST_NOTIFICATIONS`)과 별개로 앱 자체 로컬 플래그(`notificationDeliveryEnabled`)를 두어 이 플래그만으로 끈다. 위치 스위치는 별도 로컬 플래그 없이 OS 권한 상태를 그대로 노출한다.
- **Rationale**: spec §5 TBD(2026-08-16 사용자 확정)가 이미 이 모델을 못박았다 — Android가 앱에 이미 부여된 런타임 권한을 스스로 취소하는 API를 제공하지 않기 때문에, 위치는 OS 설정으로 유도하는 것 외에 다른 선택지가 없다.
- **Alternatives considered**: 위치도 로컬 억제 플래그로 처리 — `core:map`이 아직 없어 "위치 조회를 코드에서 억제"할 소비 지점이 없고, 스펙이 요구하는 "실제 OS 권한 상태와 일치"(완료조건)에도 어긋나 기각.
- (plan 1.0.0에서 결정)

## D3. "영구 거부" 판정 — `shouldShowRequestPermissionRationale` + 로컬 "요청 이력" 플래그 조합

- **Decision**: 권한 거부 시 영구 거부 여부는 `Activity.shouldShowRequestPermissionRationale(permission)`(Route가 보유)과 `PermissionRepository.hasRequestedPermissionBefore(type)`(로컬 저장, ViewModel이 보유) 두 값을 함께 봐서 판정한다. 규칙: `!granted && !rationale && hasRequestedBefore` → 영구 거부(EC-003/EC-007 경로). `!granted && !hasRequestedBefore` → 최초 요청(시스템 팝업). `!granted && rationale` → 재요청 가능(시스템 팝업).
- **Rationale**: Android는 "한 번도 요청 안 함"과 "완전히 거부됨(다시 묻지 않음)" 둘 다에서 `shouldShowRequestPermissionRationale`이 `false`를 반환해 그 자체로는 구분이 안 된다. 로컬에 "요청한 적 있는가" 플래그를 별도로 남기는 것이 Android 공식 문서가 권장하는 표준 우회 방법이다 — spec §4 가정("OS가 제공하는 권한 거부 이력 판정 표준 동작을 그대로 따른다")이 지시하는 지점이 정확히 이 조합이다.
- **Alternatives considered**: (a) 로컬 플래그만으로 판정 — 이미 거부됐지만 시스템이 재요청을 허용하는 상태(첫 거부, "다시 묻지 않음" 미체크)까지 영구 거부로 오판해 매번 설정으로 보내버리는 UX 저하가 생겨 기각. (b) `rationale` API만으로 판정 — 최초 요청과 영구 거부를 구분하지 못해 기각.
- (plan 1.0.0에서 결정)

## ~~D4. 다크모드 전역 적용 지점 — `core:design-system`은 파라미터만, 읽기·주입은 `:feature:main`~~ 재검토됨(plan 2.0.0)

- ~~**Decision**: `MinoAndroidAppTheme`에 `darkTheme: Boolean? = null` 파라미터를 추가한다(`null` = 시스템 추종, `true`/`false` = 강제). 실제로 `AppSettingsRepository.observeAppTheme()`를 읽어 `Boolean?`으로 변환해 넘기는 책임은 `:feature:main`의 `MainActivity`가 진다(현재 `MinoAndroidAppTheme` 호출부).~~
- ~~**Rationale**: `core:design-system`은 domain·data를 의존하지 않는 순수 UI 모듈이다(모듈 그래프 §의존성 흐름). 저장소 읽기를 그 안으로 들이면 레이어 경계(헌법 원칙 II)를 깬다. `:feature:main`은 이미 모든 feature 공통으로 `:core:domain`을 의존하므로(`AndroidFeatureConventionPlugin`), 새 의존 추가 없이 `AppSettingsRepository`를 주입받을 수 있다.~~
- ~~**Alternatives considered**: (a) 각 feature가 개별적으로 다크모드를 읽어 자기 화면에만 적용 — 화면마다 값이 어긋날 수 있어 "앱 전체 즉시 반영"(spec 완료조건) 요건을 못 만족해 기각. (b) `core:design-system`이 `DataStore`를 직접 읽기 — 모듈이 저장 방식을 알게 돼 교체 시(D1 뒤집힐 때) design-system까지 건드려야 해 기각.~~
- **재검토 사유(plan 2.0.0)**: spec 3.0.0에서 PRD 4.1.0의 다크모드 비목표 확정을 반영해 다크모드 요구사항(유저 플로우 2, FR-005·FR-006, UX-001, SC-002)이 전부 삭제됐다. 이 결정이 다루던 문제 자체가 사라져 대체 결정 없음 — `MinoAndroidAppTheme`는 손대지 않는다.
- (plan 1.0.0에서 결정, plan 2.0.0에서 재검토)

## D5. 외부 URL·앱 설정 이동 — `core:common:android`의 `Context` 확장 함수

- **Decision**: 노션 링크·Play 스토어·OS 앱 설정 화면 이동은 새 라이브러리 없이 `Intent(ACTION_VIEW, ...)` / `Intent(ACTION_APPLICATION_DETAILS_SETTINGS, ...)`를 쓰는 `Context` 확장 함수(`Context.openUrl`·`Context.openPlayStoreListing`·`Context.openAppSettings`)로 구현하고, `core:common:android/extension/Context.kt`에 둔다.
- **Rationale**: 순수 플랫폼 Intent로 충분한 기능이라 새 의존성이 필요 없다(YAGNI). 리시버가 있는 `Context` 확장이라 기존 `extension/` 패키지 규칙(README §4)에 정확히 들어맞고, 다른 feature도 "URL 열기" 같은 동작을 재사용할 수 있다.
- **Alternatives considered**: Custom Tabs(`androidx.browser`) 도입 — 인앱 브라우저 UX가 나아지지만 새 의존성이 필요하고 spec은 "웹 브라우저에서 연다"고만 요구해 굳이 필요하지 않아 기각. 두 번째 사용처가 생기면 재검토한다.
- (plan 1.0.0에서 결정)

## ~~D6. 다크모드 선택 UI — `core:design-system`에 `MinoBottomSheet` 신설~~ 재검토됨(plan 2.0.0)

- ~~**Decision**: Material3 `ModalBottomSheet`를 디자인 토큰으로 감싼 `MinoBottomSheet` 컴포넌트를 `core:design-system/component/bottomsheet/`에 새로 만들고, 다크모드 선택 목록(라이트/다크/시스템 기본값)을 그 안에 배치한다.~~
- ~~**Rationale**: UX-001이 요구하는 "Android 관용 BottomSheet"는 이 저장소의 컴포넌트 M3 패턴(Defaults·Colors·토큰 계층)을 그대로 따라야 다른 화면에도 일관되게 재사용된다. Figma 디자인 시스템 라이브러리에도 대응 컴포넌트가 있을 가능성이 높아(다른 M3 컴포넌트들처럼) 처음부터 feature 로컬이 아니라 `core:design-system`에 둔다 — Button·TextInput·Menu 등 기존 컴포넌트가 이미 이 경로를 따른다.~~
- ~~**Alternatives considered**: `:feature:mypage` 안에 로컬로 만들고 두 번째 사용처가 생기면 승격 — `core:common:ui`의 "승격" 정책(§5)은 동작/구조 컴포넌트에 적용되는 기준이고, 바텀시트는 디자인 토큰이 필요한 시각적 컴포넌트라 성격상 design-system에 더 가깝다고 판단해 기각. 구현 시 Figma에 실제 대응 컴포넌트가 없다고 확인되면 이 결정을 재검토한다.~~
- **재검토 사유(plan 2.0.0)**: 다크모드 선택 UX-001 자체가 spec 3.0.0에서 삭제됐다(D4와 같은 사유). `MinoBottomSheet`를 만들 화면이 없어져 신설을 보류한다 — 다른 화면에서 바텀시트가 필요해지면 그때 새 결정으로 다시 연다.
- (plan 1.0.0에서 결정, plan 2.0.0에서 재검토)

## D7. 권한 재요청 불가 안내 — `core:design-system`에 `MinoDialog` 신설

- **Decision**: EC-003·EC-007의 확인 다이얼로그(§5 TBD 질문 1 답변)는 Material3 `AlertDialog`를 감싼 `MinoDialog` 컴포넌트로 `core:design-system/component/dialog/`에 새로 만든다.
- **Rationale**: D6과 같은 논리 — 확인 다이얼로그는 이 화면 전용이 아니라 향후 다른 확인 동작(예: 방 나가기 등)에도 재사용될 범용 컴포넌트다.
- **Alternatives considered**: Compose 기본 `AlertDialog`를 그때그때 직접 호출 — 디자인 토큰이 적용되지 않고 화면마다 스타일이 갈릴 위험이 있어 기각.
- (plan 1.0.0에서 결정)

## D8. 프로필 아바타 12종 — 새 WebP 래스터 에셋

- **Decision**: 프로필 설정의 12개 제공 아바타는 `design-system` README §5.3 규칙대로 WebP로 추가하고, `Profile.avatarId`는 그 12개 중 하나를 가리키는 불투명 문자열 키로 둔다. 정확한 ID·네이밍은 Figma 노드(151449)를 열어 구현 단계에서 확정한다.
- **Rationale**: 기존 `MinoAvatar` 컴포넌트(사진 기반 프로필 이미지, placeholder 폴백)와는 성격이 다른 자산(디자이너가 그린 12색 캐릭터 일러스트)이라 재사용 대상이 아니다. `ImageVector` 변환 조건(§5.2 — 단색 fill만)을 만족하지 못할 가능성이 높아 래스터(WebP) 경로를 기본으로 잡는다.
- **Alternatives considered**: `MinoAvatar`의 `imageUrl` 파라미터로 원격 이미지처럼 취급 — 로컬 번들 자산을 URL처럼 다루는 것은 부자연스럽고 오프라인에서도 항상 보여야 하므로 기각.
- (plan 1.0.0에서 결정)

## D9. 프로필 저장소를 원격 API로 전환 — `avatarId`는 `Int`

- **Decision**: `ProfileRepositoryImpl`은 `core:data/network/service/UserApiService`(Ktor)로 `GET /api/v1/users/me`·`PATCH /api/v1/users/me`를 호출한다. 로컬 캐시는 두지 않는다(매번 원격 조회, 필요해지면 나중에 캐시 계층 추가). `Profile.avatarId`는 `String`이 아니라 `Int`로 바꾼다 — swagger `Avatar` 스키마가 `{ id: integer }`이기 때문이다(§design-system은 그 정수를 12종 아바타 카탈로그의 키로 매핑).
- **Rationale**: D1 재검토 사유와 동일 — 실제 계약이 생겼으니 그걸 따른다. `getProfile()`은 `Profile?`이 아니라 `Profile`(non-null)로 좁힌다: spec이 이미 "프로필이 생성된 사용자"를 진입 전제로 삼고(§1 진입 조건), swagger에도 "프로필 없음"을 나타내는 응답이 없다 — 인증은 됐는데 프로필이 없는 상태는 이 API 설계에서 존재하지 않는다.
- **Alternatives considered**: 로컬 캐시를 두고 원격과 동기화 — 오프라인 지원이 spec 요구사항에 없고(§4 가정에 오프라인 언급 없음), 캐시 무효화 규칙까지 설계하는 건 지금 필요 이상의 복잡도라 기각. 필요해지면 별도 결정으로 추가한다.
- **의존성(범위 밖)**: `core:data`의 `HttpClient`는 아직 `https://api.github.com/` 임시 baseUrl과 인증 헤더 주입이 없다(`core/data/README.md` §4 NOTE). 실서버(`https://api.gguk.org`) 연결과 Bearer 토큰 부착은 이 feature 고유 관심사가 아니라 여러 feature가 공유할 인프라라 D11에서 별도로 다룬다.
- (plan 3.0.0에서 결정)

## D10. 알림 발송 억제 — 서버 구독 해제 API가 없어 클라이언트 표시 단계에서 억제

- **Decision**: 알림 권한이 처음 허용되는 시점(FR-007 성공 콜백)에 FCM 토큰을 발급받아 `PUT /api/v1/users/me/push-token`으로 등록한다. "앱 자체 알림 발송 설정"(`notificationDeliveryEnabled`, D2) OFF는 서버의 푸시 발송 자체를 막지 못한다 — 대신 클라이언트의 FCM 수신 지점(`FirebaseMessagingService.onMessageReceived`)에서 이 로컬 플래그를 확인해 OFF면 시스템 알림으로 표시하지 않고 조용히 버린다.
- **Rationale**: swagger에 토큰 등록(`PUT`)만 있고 해제(`DELETE` 등)가 없다 — 서버에 "이 사용자에게 그만 보내라"고 알릴 방법이 API 계약에 없다. D2가 이미 "OS 권한은 유지, 앱 자체 플래그만 끈다"고 정했으므로, 그 플래그의 실제 효력을 수신 단계에서 만드는 것이 계약을 어기지 않는 유일한 방법이다.
- **Alternatives considered**: (a) 스위치 OFF 시 `PUT push-token`을 빈 토큰으로 호출해 사실상 해제 시도 — 계약에 없는 동작을 서버가 어떻게 처리할지 불명확해(빈 문자열이 유효한 요청인지 문서에 없음) 기각. (b) 백엔드에 해제 API 추가를 요청 — 이 feature의 plan이 결정할 수 있는 범위 밖이라 지금은 클라이언트 억제로 가고, API가 생기면 재검토.
- **신규 컴포넌트**: `core:domain/repository/PushNotificationRepository`(`suspend fun syncPushToken()`), `core:data/device/PushTokenProvider`(FCM SDK 래퍼, `firebase-messaging` 의존성 신규 추가 필요), `core:data/repository/PushNotificationRepositoryImpl`.
- (plan 3.0.0에서 결정)

## D11. 실서버 연결·인증 부착 — 이 feature의 범위 밖, 선행 의존성으로 기록만

- **Decision**: `HttpClient`의 baseUrl을 `https://api.gguk.org`로 바꾸는 것과 `Authorization: Bearer <token>` 헤더를 붙이는 인증 인프라는 이 plan이 설계하지 않는다. `ProfileRepositoryImpl`·`PushNotificationRepositoryImpl`은 그 인프라가 이미 갖춰져 있다고 가정하고 인터페이스 수준에서만 설계한다.
- **Rationale**: swagger 자체가 "인증 방식은 별도 인증 설계 문서에서 확정한다"고 명시해 백엔드에서도 미확정이다. baseUrl·인증은 프로필뿐 아니라 이후 모든 원격 API(방·핀·코멘트·알림)가 공유할 인프라라 이 feature 하나가 결정할 성격이 아니다. 지어내면 다른 feature가 나중에 다른 방식으로 다시 만들 위험이 크다.
- **Alternatives considered**: 이 feature 안에서 임시로 `NetworkModule`을 고쳐 baseUrl·인증을 직접 배선 — 다른 feature와 충돌할 임시방편을 스스로 만드는 것이라 기각. 완료 보고에서 이 의존성을 선행 과제로 명시한다.
- (plan 3.0.0에서 결정)
