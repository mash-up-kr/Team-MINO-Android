# 계약: 프로필 서버 API (`:core:data` ↔ 꾹 서버)

**대상 스펙 경로**: `docs/specs/profile` · **부속 문서**: [plan.md](../plan.md)

이 feature가 소비하는 서버 엔드포인트의 계약이다. 레이어 구성·작성 규칙(`ApiService`·`DataSource`·`RepositoryImpl`·Mapper)은 [`core/data/README.md`](../../../../core/data/README.md)가 소유하고, 도메인 표면은 [repository 계약](profile-repository-contract.md)이 소유한다.

> **출처**: `https://api.gguk.org/api-docs-json` (Team MINO API 1.0.0) · **조회 시점**: 2026-08-31T16:03:55+09:00
>
> **plan 6.0.0의 재조회에서는 바뀐 것이 없다** — 세 오퍼레이션을 원문으로 펼쳐 같은 날 12:44 조회본과 대조했고 `nickname` 제약·`avatar.color`의 13개 `enum`·`required`·`errorCode` 열거까지 전부 동일했다([D55](../research.md#d55-서버-문서-재조회2026-08-31-1603--바뀐-것이-없다)). **이번에 바뀐 것은 서버가 아니라 이 앱이다** — 13번째 값 `gray`를 이제 실제로 보낸다(§2 아바타 값 표).
>
> **plan 5.1.0의 재조회에서 `nickname.pattern`이 바뀌었다** — 문자 클래스 끝의 공백이 빠져 `^[\uAC00-\uD7A3A-Za-z ]+$` → **`^[\uAC00-\uD7A3A-Za-z]+$`** 가 됐고, `PATCH`의 description도 `공백 포함`에서 **`공백·숫자 불가`** 로 바뀌었다. 나머지는 2026-08-28 조회본과 동일했다. **이 변화가 §2의 어긋남 하나를 지운다**([D52](../research.md#d52-서버-문서-재조회2026-08-31--닉네임-pattern에서-공백이-빠졌다)).
>
> 그 이전에는 하루 만에 `avatar.color`가 자유 문자열(`minLength 1`·`maxLength 20`)에서 **13개 `enum`**으로 좁혀진 적이 있다(2026-08-27T22:15:17 → 2026-08-28T01:12:44). **재조회가 사실을 뒤집은 것이 이번으로 세 번째다.** 이 문서를 근거로 삼기 전에 위 URL을 다시 조회해 대조하는 습관이 필요하다.
>
> 이 문서의 스키마는 위 조회본에서 **원문 그대로** 옮긴 것이다. 요약하거나 표로 바꾸지 않는다 — 이 대조에서 값어치 있는 정보가 `maxLength`·`pattern`·`required` 같은 제약인데, 변환에서 가장 먼저 뭉개지는 것이 그 제약이다. 서버 문서는 언제든 바뀔 수 있으므로 계약을 다시 판단할 때는 위 URL을 다시 조회한다.

---

## 1. 쓰는 엔드포인트

| 메서드 · 경로 | 쓰는 곳 | spec 근거 |
|---|---|---|
| `GET /api/v1/users/me` | `refreshProfile()` — 프리필과 등록 여부 판정 | FR-006 · [D38](../research.md#d38-등록수정-분기--서버에-직접-묻고-캐시가-그-답을-들고-있는다) |
| `POST /api/v1/users` | `saveProfile()` — 미등록일 때. 개인방(`내 장소`)이 같은 흐름에서 생긴다 | FR-007 · FR-008 · [D17](../research.md#d17-개인방내-장소-생성--서버가-등록과-함께-처리한다확정) |
| `PATCH /api/v1/users/me` | `saveProfile()` — 등록돼 있을 때 | FR-007 · FR-009 |

세 오퍼레이션은 모두 OpenAPI 태그 `user`에 속하며, **소유 서비스는 [`UserApiService`](../../../../core/data/src/main/java/team/mino/core/data/network/service/UserApiService.kt) 하나다** — 이 feature는 그것을 넓히고 `ProfileApiService`를 따로 만들지 않는다([D49](../research.md#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다)). 같은 태그의 `GET /api/v1/users/me`를 splash-screen이 먼저 쓰고 있다.

세 요청 모두 `security: [{ bearer: [] }]`이며, `Authorization: Bearer <Firebase ID 토큰>`은 [`MinoIdentityProofPlugin`](../../../../core/data/src/main/java/team/mino/core/data/network/plugin/MinoIdentityProofPlugin.kt)이 `HttpClient` 전역에서 붙인다. **이 feature는 헤더에 아무것도 더하지 않는다**([D20](../research.md#d20-인증-헤더--이번-범위에서-배선하지-않는다) 보정). 첨부 계약은 [`identity-proof-attachment.md`](../../anonymous-auth-session/contracts/identity-proof-attachment.md)가 소유한다.

성공 응답은 모두 `{ "data": ... }`로 감싸이고 실패 응답은 `{ "errorCode", "message" }`다.

- **봉투는 이미 있는 `MinoResponse<T>`를 쓴다.** 새로 만들지 않는다 — `ApiService`가 `body<MinoResponse<T>>().data`로 벗기고 `DataSource` 위로는 알맹이만 올린다. 규칙의 출처는 [ADR 2026-08-27](../../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)이며, 엔드포인트를 붙이는 절차는 [`core/data/README.md`](../../../../core/data/README.md) §8이 소유한다.
- **에러 본문 `ErrorResponse`는 신설이다.** 위 ADR이 "에러 본문은 이 봉투가 아니며 이 타입이 다루지 않는다"로 범위 밖에 두었다([D40](../research.md#d40-응답-봉투와-에러-코드--공용-dto를-신설한다) 보정).

### `POST /api/v1/users` — 유저 등록 (+ 개인방 자동 생성)

> 익명 인증 토큰의 uid로 등록한다. 개인방(내 장소) 생성이 같은 흐름에서 처리되며 응답에는 포함하지 않는다.

요청 (`required: true`):

```json
{
 "type": "object",
 "properties": {
  "nickname": { "type": "string", "minLength": 2, "maxLength": 15, "pattern": "^[\\uAC00-\\uD7A3A-Za-z]+$" },
  "avatar": {
   "type": "object",
   "properties": {
    "color": {
     "enum": ["red","red_orange","orange","lime","green","cyan","violet","pink","blue","brown","light_blue","purple","gray"],
     "type": "string"
    }
   },
   "required": ["color"]
  }
 },
 "required": ["nickname", "avatar"]
}
```

응답 `201`:

```json
{
 "type": "object",
 "properties": {
  "data": {
   "type": "object",
   "properties": {
    "id": { "type": "string", "format": "uuid" },
    "nickname": { "type": "string", "example": "꾹이" },
    "avatar": {
     "type": "object",
     "nullable": true,
     "properties": {
      "color": {
       "enum": ["red","red_orange","orange","lime","green","cyan","violet","pink","blue","brown","light_blue","purple","gray"],
       "type": "string",
       "example": "red"
      }
     }
    },
    "createdAt": { "type": "string", "format": "date-time" }
   }
  }
 }
}
```

실패: `401` (`errorCode`: `UNAUTHORIZED` | `TOKEN_EXPIRED`) · `409` (`errorCode`: `USER_ALREADY_REGISTERED`).

### `GET /api/v1/users/me` — 내 프로필 조회

요청 본문 없음. 응답 `200`은 위 `201`과 같은 형태다.

실패 `401` — **미등록이 인증 실패와 같은 상태 코드로 온다**:

```json
{
 "errorCode": { "type": "string", "enum": ["UNAUTHORIZED", "TOKEN_EXPIRED", "USER_NOT_REGISTERED"], "example": "UNAUTHORIZED" },
 "message": { "type": "string", "example": "인증 정보가 없습니다." }
}
```

### `PATCH /api/v1/users/me` — 프로필 수정

> 닉네임(한글/영문 2~15자, 공백·숫자 불가)·아바타 수정

요청은 `POST`와 같은 속성(같은 13개 `enum`과 같은 `nickname` 제약 포함)이되 `"required": []`다 — 모든 필드가 선택이다. **이 앱은 언제나 두 값을 함께 보낸다.** 화면이 닉네임과 아바타를 한 폼으로 다루고(FR-001), 부분 전송은 "아바타를 안 골랐다"와 "아바타를 건드리지 않았다"를 구분하지 못하기 때문이다. 응답 `200`·실패 `401`은 `GET`과 같다.

---

## 2. spec과 어긋나는 지점

**조용히 넘기지 않는다.** 6건 중 **4건이 닫혔고 2건이 남는다.** 4번은 plan 5.0.0 구현 뒤 T083의 기기 실측으로 닫혔고, **닉네임 2건(2·3번)은 plan 5.1.0에서 성격이 바뀌었다** — "알고 받아들이는 어긋남"이 아니라 **어긋남이 아니게 됐다.** 아래 별도 항으로 옮겼다.

### 남은 협의 항목 (2건)

| # | 서버 문서 | 이 구현 | 잠정 처리 | 근거 |
|---|---|---|---|---|
| 5 | `GET /api/v1/users/me`의 **미등록이 `401`** — 인증 실패와 같은 코드 | — | 본문의 `errorCode == "USER_NOT_REGISTERED"`만 `null`(미등록)로 지역 처리하고 나머지 401은 전파한다. `404`였다면 지역 catch가 필요 없다. **이 저장소에서 `errorCode`를 읽는 유일한 엔드포인트다** — `PinApiService`·`SharedPlaceSaveWorker`는 "실패 판정은 상태 코드만 본다"를 명시한다([D47](../research.md#d47-develop-재대조--420의-대조가-옛-트리를-근거로-했다)). 그 선례가 이 요청의 근거를 강화한다. **읽는 코드 지점은 `UserApiService`의 판정 헬퍼 하나**이고 `hasProfile()`·`getMe()`가 그것을 공유한다 — splash-screen도 같은 판정을 쓰므로 소비자는 이 feature 하나가 아니다([D49](../research.md#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다)) | [D38](../research.md#d38-등록수정-분기--서버에-직접-묻고-캐시가-그-답을-들고-있는다) |
| 6 | 응답의 `avatar`가 `nullable: true`인데 요청에서는 `required` | 앱은 언제나 아바타를 보낸다(EC-002의 기본 아바타 = `gray`) | `null`이면 기본 아바타로 읽는다 — **`gray`와 같은 곳으로 모은다.** plan 6.0.0에서 성격이 약해졌다: 앱이 `gray`로 "고르지 않음"을 표현하게 되면서 `null`과 `gray`가 같은 상태를 뜻하는 두 표현이 됐고, 어느 쪽이 오든 처리가 하나다. **언제 `null`이 되는지는 여전히 문서에 없다** | [D37](../research.md#d37-아바타-식별자--도메인-profileavatar-enum-서버-표현은-avatarcolor-문자열)·[D53](../research.md#d53-기본-아바타의-자리--도메인은-13항목-디자인-시스템-팔레트는-12종-그대로) |

### 디자인 확인 항목 (0건 — 닫힘)

| 대상 | 어떻게 닫혔나 |
|---|---|
| `Person10` → `brown` | **디자인 확인 완료(2026-08-28, T086). 소거법으로 배정한 대응이 맞았다.** 나머지 11종은 배경 원 색이 디자인 시스템 토큰과 hex 단위로 일치해 확정적이었고, `Person10`만 배경 `#FBE9DA`에 대응 토큰이 없어 남은 색으로 배정했던 유일한 추정이었다. 이것으로 **아바타 12종의 서버 문자열 대응이 전부 근거를 갖는다**([D44](../research.md#d44-아바타-서버-문자열--12종이-방-팔레트-12색에-1대1로-대응한다)) |

### T083 실측으로 닫힌 항목 (1건)

| # | 무엇이었나 | 어떻게 닫혔나 |
|---|---|---|
| 4 | 닉네임 검증 실패의 **상태 코드가 문서에 없었다** — `POST`는 401·409만, `PATCH`는 401만 문서화 | **기기 실측 결과 `400 Bad Request`다**(2026-08-28, [quickstart §4-3](../quickstart.md) 20번). 서버 문서에 없는 값이라 실측이 유일한 근거였다. `expectSuccess = true`가 이 400을 `MinoDomainException.Http(400)`으로 바꾸고 화면은 저장 실패 스낵바를 띄우므로, **[spec](../spec.md) §5·EC-014의 "16자 이상은 서버가 거절해 저장 실패로 보인다"가 실측으로 뒷받침됐다**([D19](../research.md#d19-닉네임-규칙-불일치--클라이언트는-spec을-따르고-서버-거절은-저장-실패로-받는다)). **plan 5.1.0 보정: 클라이언트에 상한을 더하지 않는다는 판단은 뒤집혔다** — spec 3.0.0이 상한을 채택했고 입력 차단이 16자를 서버에 보내지 않으므로, 이 실측 경로는 닉네임 길이로 재현되지 않는다. 실측 자체(닉네임 검증 실패 = `400`)는 다른 사유에 여전히 유효한 기록이다 |

### plan 4.1.0에서 닫힌 항목 (3건)

| # | 무엇이었나 | 어떻게 닫혔나 |
|---|---|---|
| 1 | 아바타를 `{ color }`로 모델링하는데 값의 열거가 없어 `"person_01"`~`"person_12"`를 잠정으로 실었다 | **서버가 13개 `enum`으로 값 도메인을 확정했다**(2026-08-28 조회). 그 목록이 방 팔레트와 같고, 아바타 12종의 배경 원 색을 실측하니 12색에 1대1로 대응했다. 잠정 문자열을 폐기하고 [D44](../research.md#d44-아바타-서버-문자열--12종이-방-팔레트-12색에-1대1로-대응한다)의 표로 확정했다 |
| 2 | 닉네임 `maxLength: 15` vs spec의 상한 없음 | **plan 5.1.0에서 어긋남 자체가 사라졌다** — 아래 항 참고. (4.1.0 시점의 처리: [spec 2.0.0](../spec.md) §5가 "상한을 두지 않는다"로 확정해 알고 받아들이는 어긋남으로 두었다) |
| 3 | 닉네임 `pattern`이 공백 허용 vs spec의 공백 불가 | **plan 5.1.0에서 어긋남 자체가 사라졌다** — 아래 항 참고. (4.1.0 시점의 처리: 클라이언트가 더 좁아 서버까지 가지 않으므로 실패가 없다고 보았다) |

### plan 5.1.0에서 어긋남이 소멸한 항목 (2건 — 닉네임)

**두 지점이 서로 다른 쪽에서 해소돼, 클라이언트 규칙과 서버 스키마가 이제 완전히 같다.**

| 지점 | 서버(2026-08-31 조회) | 클라이언트 | 어떻게 같아졌나 |
|---|---|---|---|
| 길이 | `minLength 2` · `maxLength 15` | 하한 2자는 `ValidateNicknameUseCase`, 상한 15자는 입력 차단 | **spec이 움직였다.** [spec 3.0.0](../spec.md)이 PRD 10.0.0을 따라 15자 상한을 채택하고 FR-014로 입력 차단을 세웠다([D51](../research.md#d51-닉네임-15자-상한의-강제-지점--viewmodel이-자른다)) |
| 문자 | `pattern ^[\uAC00-\uD7A3A-Za-z]+$` — 한글 음절·영문만 | 같음(공백·숫자·특수문자 무효) | **서버가 움직였다.** `pattern`에서 공백이 빠졌다([D52](../research.md#d52-서버-문서-재조회2026-08-31--닉네임-pattern에서-공백이-빠졌다)) |

**결과: 클라이언트가 통과시킨 닉네임을 서버가 길이·문자로 거절하는 경로가 없다.** 4번이 실측한 `400`은 관측 기록으로 남지만 **닉네임 길이로는 재현되지 않는다.** [spec](../spec.md) EC-014가 3.0.0에서 "금칙어 등 클라이언트가 판정하지 않는 사유"로 좁혀진 것이 이 사실과 맞물린다.

**대응 API가 없는 요구사항은 없다.** spec의 FR-006·FR-007·FR-008·FR-009가 모두 위 세 엔드포인트로 덮인다.

### 아바타 값 표

**13행이다** — 선택 12종과 기본 아바타 1종. plan 5.x까지 12행이었고 `gray`를 배제했던 것이 plan 6.0.0에서 뒤집혔다([D53](../research.md#d53-기본-아바타의-자리--도메인은-13항목-디자인-시스템-팔레트는-12종-그대로)).

| `ProfileAvatar` | `avatar.color` | | `ProfileAvatar` | `avatar.color` |
|---|---|---|---|---|
| `Person1` | `red` | | `Person7` | `cyan` |
| `Person2` | `red_orange` | | `Person8` | `pink` |
| `Person3` | `orange` | | `Person9` | `blue` |
| `Person4` | `green` | | `Person10` | `brown` |
| `Person5` | `purple` | | `Person11` | `light_blue` |
| `Person6` | `lime` | | `Person12` | `violet` |
| | | | **기본 아바타** | **`gray`** |

- 표의 소유자는 `ProfileMapper` 한 파일이다. **선언 순서에서 파생하지 않는다** — 위 대응은 `RoomColor`의 선언 순서와 어긋나므로 `ordinal`로 이으면 조용히 틀린 값이 나간다([D44](../research.md#d44-아바타-서버-문자열--12종이-방-팔레트-12색에-1대1로-대응한다)).
- **`gray`를 보낸다.** 사용자가 아바타를 고르지 않고 저장하면 이 값이 나간다(spec FR-015·EC-002). 방에서 `gray`가 "색을 고르지 않은 방"의 값인 것과 같은 뜻이며, [`RoomMapper`](../../../../core/data/src/main/java/team/mino/core/data/repository/mapper/RoomMapper.kt)가 미선택 방을 `gray`로 확정해 보내는 것과 같은 규칙이다. **`enum`의 13개 값이 이제 전부 쓰인다.**
- 받는 쪽은 모르는 문자열과 `null` 아바타를 기본 아바타로 읽는다. `gray`와 `null`이 같은 곳으로 모이므로 서버가 둘 중 무엇을 주든 화면은 같게 선다.

---

## 3. 레이어 구성

**이 feature는 `user` 태그의 기존 소유자를 넓힌다.** `ProfileApiService`·`ProfileRemoteDataSource`를 만들지 않는다 — 근거는 [D49](../research.md#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다)이고, 레이어 작성 규칙 자체는 [`core/data/README.md`](../../../../core/data/README.md) §4·§5가 소유한다.

```text
:core:data/
├── network/
│   ├── dto/request/ProfileRequest.kt      # 신규 — nickname · AvatarRequest(color)
│   ├── dto/response/ProfileResponse.kt    # 신규 — id · nickname · AvatarResponse?(color) · createdAt
│   ├── dto/response/ErrorResponse.kt      # 신규 · 공용 — { errorCode, message? }
│   ├── dto/response/MinoResponse.kt       # 이미 있다 — 만들지 않는다
│   └── service/UserApiService.kt          # 변경(develop 커밋) — 세 오퍼레이션 · 봉투 해제 · 401 판정
├── datasource/
│   ├── UserRemoteDataSource.kt(+Impl)     # 변경(develop 커밋) — DTO만 반환. Impl은 순수 위임
│   ├── ProfileLocalDataSource.kt(+Impl)   # 변경 — ProfileEntry 반환 (캐시)
│   └── di/ProfileDataSourceModule.kt      # 변경 — 로컬 바인딩만. 원격은 UserDataSourceModule이 이미 갖는다
└── repository/
    ├── ProfileRepositoryImpl.kt           # 변경 — UserRemoteDataSource 주입. 변환의 경계
    └── mapper/ProfileMapper.kt            # 신규 — DTO ↔ 도메인 · 아바타 문자열 표 소유
```

`UserApiService`의 표면:

```kotlin
internal class UserApiService @Inject constructor(private val client: HttpClient) {
    /**
     * 등록 여부만 판정한다. **성공 본문을 역직렬화하지 않는다** — splash-screen의 진입 게이트가 쓴다.
     * 미등록(`401` + `USER_NOT_REGISTERED`)이면 `false`, 다른 401은 그대로 전파된다.
     */
    suspend fun hasProfile(): Boolean

    /** 프로필 값을 읽는다. 미등록이면 `null`. 다른 401은 그대로 전파된다. */
    suspend fun getMe(): ProfileResponse?

    suspend fun register(request: ProfileRequest): ProfileResponse

    suspend fun updateMe(request: ProfileRequest): ProfileResponse

    /** `401` 본문의 `errorCode == "USER_NOT_REGISTERED"` 판정. 위 두 함수가 공유한다. */
    private suspend fun MinoDomainException.Http.isUserNotRegistered(): Boolean
}
```

- **같은 경로에 함수가 둘인 것은 의도다.** `hasProfile()`과 `getMe()`는 같은 `GET /api/v1/users/me`를 부르지만 **실패 허용치가 다르다** — 진입 게이트는 본문 스키마가 어긋나도 통과해야 하고, 프리필은 본문이 필요하다. 하나로 합치면 스플래시 진입이 프로필 본문 스키마에 의존하게 된다([D49](../research.md#d49-develop-통합-재대조--user-태그-엔드포인트의-소유자는-userapiservice-하나다)).
- 봉투 해제는 이 클래스 안에서 끝난다 — `body<MinoResponse<ProfileResponse>>().data` 형태이며 `DataSource` 위쪽은 봉투를 모른다([ADR 2026-08-27](../../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)). 반환 타입에 `MinoResponse`가 드러나서는 안 된다.
- **`401` 지역 catch는 이 파일에만 있다.** `MinoDomainException.Http(401)`의 `cause`가 들고 있는 `ResponseException`에서 본문을 `ErrorResponse`로 읽어 판정하고, `USER_NOT_REGISTERED`가 아니면 다시 던진다. 배치 근거는 [ADR 2026-08-28](../../../adr/2026-08-28-error-body-type-and-no-error-code-leaf.md)이다.
  - **develop의 현재 코드는 이것을 `UserRemoteDataSourceImpl`에 두고 `Json.parseToJsonElement`로 손수 파싱한다.** 이 개정이 그것을 여기로 옮기고 `ErrorResponse`로 통일한다. 옮기면 Ktor 타입(`ResponseException`·`bodyAsText()`)이 `network/` 밖으로 새지 않아 [`core/data/README.md`](../../../../core/data/README.md) §5를 지킨다.
- 그 밖의 실패는 잡지 않는다. 매핑은 `HttpClient`의 `convertDomainException`이 전역 수행한다([`core/data/README.md`](../../../../core/data/README.md) §4).

`UserRemoteDataSource`의 표면 — 위 네 함수와 1:1이고, `Impl`은 위임만 한다(README §5):

```kotlin
internal interface UserRemoteDataSource {
    suspend fun isRegistered(): Boolean            // 기존 — splash-screen이 쓴다
    suspend fun getMe(): ProfileResponse?          // 신규
    suspend fun register(request: ProfileRequest): ProfileResponse   // 신규
    suspend fun updateMe(request: ProfileRequest): ProfileResponse   // 신규
}
```

`isRegistered()`의 이름과 도메인 계약([`ProfileRegistrationRepository`](../../../../core/domain/src/main/kotlin/team/mino/core/domain/repository/ProfileRegistrationRepository.kt))은 **바뀌지 않는다** — splash-screen이 이미 머지된 채로 그것을 쓰고 있고, 이 개정은 그 아래 구현만 정리한다.

## 4. 서버가 표현을 바꾸면 고칠 곳

| 바뀌는 것 | 고칠 파일 |
|---|---|
| 아바타 색 대응(디자인 확인 항목) | `ProfileMapper`의 표 하나 |
| 미등록 신호(협의 항목 5가 `404`로 확정되면) | `UserApiService`의 `401` 판정 헬퍼 하나 — `hasProfile()`·`getMe()`가 함께 따라온다. `ErrorResponse`의 소비자가 0이 되므로 [ADR 2026-08-28](../../../adr/2026-08-28-error-body-type-and-no-error-code-leaf.md)도 함께 본다 |
| 아바타 `enum`이 넓어지거나 좁아지면 | `ProfileMapper`의 표. 모르는 값은 이미 기본 아바타로 읽으므로 조회가 깨지지는 않는다 |
| 닉네임 규칙(spec 3.0.0에서 확정, 서버와 일치) | 다시 논의된다면 **spec부터** 고친다. 판정은 `ValidateNicknameUseCase`(하한·문자), 상한은 `ProfileViewModel`의 입력 차단([D51](../research.md#d51-닉네임-15자-상한의-강제-지점--viewmodel이-자른다)) — **두 곳이며 역할이 다르다** |
| 응답 봉투 | `MinoResponse`와 이를 쓰는 `ApiService`들 — 프로필만의 문제가 아니므로 [ADR 2026-08-27](../../../adr/2026-08-27-response-envelope-unwrapped-in-apiservice.md)을 먼저 고친다 |

도메인 모델·화면·디자인 시스템은 어느 경우에도 바뀌지 않는다.
