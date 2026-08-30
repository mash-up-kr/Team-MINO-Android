# `ApiService`의 단위는 feature가 아니라 서버 리소스(OpenAPI 태그)다

- **상태**: Accepted
- **작성일**: 2026-08-28
- **작성자**: Jaesung Lee

## 컨텍스트

[`core/data/README.md`](../../core/data/README.md) §4는 `ApiService`를 **어떻게** 쓰는지를 정한다 — `internal`, `@Inject constructor`, 상대 경로, 예외를 잡지 않음, `body<MinoResponse<T>>().data`로 봉투 해제. 그러나 **`XxxApiService`의 `Xxx`가 무엇인지**는 어디에도 없었다. 명명 규칙 표에 `XxxApiService`라고만 적혀 있고, 그 `Xxx`를 feature 이름으로 읽을지 서버 리소스 이름으로 읽을지가 열려 있었다.

실제 코드는 처음부터 한 방향으로 수렴해 있었다. `RoomApiService` 하나가 OpenAPI `room` 태그의 `listRooms`·`getRoom`·`createRoom`·`updateRoom`을 전부 갖고, 방을 쓰는 feature가 여럿(`:feature:home`·`:feature:sharereceiver`·group-room-form)인데도 서비스는 하나다. `PinApiService`도 `pin` 쪽을 갖는다. **관례는 있었지만 적혀 있지 않았다.**

적혀 있지 않은 관례는 지켜지지 않는다. [profile](../specs/profile/plan.md)이 그것을 보여줬다.

splash-screen이 `d783e03`으로 `UserApiService`를 만들어 `GET /api/v1/users/me`를 진입 판정에 쓰고 있었다. 같은 시기에 profile은 **같은 세 엔드포인트**(`POST /api/v1/users` · `GET /api/v1/users/me` · `PATCH /api/v1/users/me`, 전부 `user` 태그)를 소비하는 `ProfileApiService`를 따로 설계하고 있었다. 두 feature가 각자 자기 이름을 서버 리소스 경계에 덧씌운 것이다. 브랜치에 develop을 반영하고 `:core:data`를 전수 확인하기 전까지 아무도 몰랐다.

그 시점의 중복은 서비스 하나가 아니었다.

| 갈라진 것 | splash-screen | profile |
|---|---|---|
| `GET /api/v1/users/me` 호출 | `UserApiService.getMe()` | `ProfileApiService.getMe()` |
| 경로 문자열 | `"api/v1/users/me"` | `"api/v1/users/me"` |
| `401` 미등록 판정 | `UserRemoteDataSourceImpl` | `ProfileApiService` |
| `USER_NOT_REGISTERED` 상수 | `UserRemoteDataSourceImpl`의 `private const` | `ProfileApiService`의 `companion` |

`ApiService` 층에서 갈라지면 **그 아래 DataSource·상수·에러 판정이 따라서 갈라진다.** 중복이 하나가 아니라 층으로 번지는 것이 이 문제의 성질이다.

## 결정

**`ApiService`의 단위는 서버 리소스다.** 구체적으로는 소비하는 오퍼레이션이 속한 **OpenAPI 태그**를 단위로 하고, 한 태그의 소유자는 **하나**다.

- 새 엔드포인트를 붙일 때는 그 오퍼레이션의 태그를 먼저 확인하고, **그 태그의 `ApiService`가 이미 있으면 그것을 넓힌다.** 없을 때만 새로 만든다.
- **`ApiService` 이름에 feature를 담지 않는다.** `ProfileApiService`·`SplashApiService`가 아니라 `UserApiService`다. 여러 feature가 한 서비스를 공유하는 것이 정상이다.
- 한 서비스가 **같은 경로에 함수를 둘 이상 두는 것은 허용된다.** 같은 엔드포인트라도 호출자가 요구하는 것이 다르면 함수를 나눈다(아래 §결과의 첫 항목).
- 경로 문자열·엔드포인트별 지역 정책·서버 코드 상수는 그 서비스 **안에** 둔다. 밖으로 새면 태그 단위 소유가 무의미해진다.

이 규칙은 `DataSource`에도 그대로 내려간다 — `UserApiService`를 감싸는 것은 `UserRemoteDataSource` 하나이고, feature마다 원격 DataSource를 새로 만들지 않는다. 반면 **도메인 `Repository`는 이 규칙의 대상이 아니다.** 도메인 계약의 단위는 서버 리소스가 아니라 관심사다.

## 근거

**서버 리소스는 feature보다 오래 살고, feature보다 적다.** 화면은 합쳐지고 갈라지고 사라지지만 `POST /api/v1/users`는 그대로다. 수명이 긴 쪽을 경계로 삼아야 경계가 자주 흔들리지 않는다. feature를 단위로 삼으면 화면이 하나 늘 때마다 같은 엔드포인트의 소유자가 하나씩 는다.

**엔드포인트 지식이 두 곳에 있으면 반드시 갈라진다.** 서버가 미등록을 `401`에서 `404`로 바꾸는 날, 한쪽만 고쳐도 **컴파일은 통과한다.** 타입 시스템이 잡아주지 않는 종류의 중복이다. profile의 경우 갈라진 쪽이 어디냐에 따라 사용자는 온보딩에 갇히거나(진입 판정이 낡음) 빈 폼을 본다(프리필이 낡음). 둘 다 앱을 켜는 모든 사용자가 겪는다.

**중복이 층으로 번진다.** `ApiService`가 둘이면 그것을 감싸는 `DataSource`가 둘이 되고, 엔드포인트별 지역 정책(`401` 판정)과 서버 코드 상수도 각자 복제된다. 위 표가 그 결과다 — 서비스 하나를 나눈 대가로 네 가지가 갈라졌다.

**이미 그렇게 하고 있었다.** `RoomApiService`가 `room` 태그 넷을 다 갖는 것은 우연이 아니라 이 규칙의 실천이다. 새로 만드는 규칙이 아니라 **코드에 있으나 문서에 없던 규칙을 적는 것**이며, 그래서 기존 코드를 되돌릴 필요가 없다.

**태그를 단위로 고른 이유는 그것이 서버가 스스로 선언한 경계이기 때문이다.** 경로 접두어(`/api/v1/users`)로 가를 수도 있지만, 경로는 리소스 중첩(`/api/v1/rooms/{roomId}/members`가 `room`과 `invitation` 양쪽에 걸린다)에서 흔들린다. 태그는 서버팀이 "이것들은 한 묶음"이라고 명시한 값이므로 판정이 흔들리지 않고, [`openapi_digest.py`](../../.claude/skills/mino-plan/scripts/openapi_digest.py)의 `index --tag`가 그 판정을 기계적으로 내준다.

**적혀 있지 않은 관례는 지켜지지 않는다는 것이 이 ADR을 쓰는 직접적인 이유다.** profile은 규약을 어긴 것이 아니라 **규약이 다루지 않는 자리에서 다르게 읽은 것**이다. 세 문서(`core/data/README.md`·기존 ADR 둘)를 다 읽고도 `ProfileApiService`가 틀렸다고 판단할 근거가 없었다. 다음 feature도 같은 자리에 선다.

## 결과

- **같은 경로에 함수가 둘 이상 있을 수 있고, 그것은 냄새가 아니다.** `UserApiService`는 `GET /api/v1/users/me`에 대해 `hasProfile(): Boolean`(성공 본문을 역직렬화하지 않는다 — 진입 게이트용)과 `getMe(): ProfileResponse?`(본문을 읽는다 — 프리필용) 둘을 갖는다. 같은 엔드포인트라도 **실패 허용치가 다르면** 함수를 나눈다. 하나로 합치면 진입 게이트가 프로필 본문 스키마에 의존하게 되고, 그 성질을 지키던 테스트 픽스처가 함께 무너진다. 나누는 판단의 근거는 호출자의 요구이지 코드 줄 수가 아니다.
- **새 엔드포인트를 붙이는 절차에 확인 한 줄이 늘어난다.** 태그를 확인하고, 그 태그를 이미 부르는 코드가 있는지 본다. `python3 openapi_digest.py index "$DOC" --tag <태그>`와 `grep -rn "<경로>" core/data/src/main`이면 된다. **두 번째가 뭔가를 찾으면 새 서비스를 만들지 않는다.**
- **여러 feature가 한 `ApiService`를 공유하는 것이 정상 상태다.** 그 결과로 한 feature의 작업이 **다른 feature가 쓰는 파일을 고치게 된다.** 이때는 회귀 확인이 따라야 한다 — 고친 쪽의 테스트만이 아니라 그 서비스를 쓰는 **모든** feature의 테스트를 돌린다. profile이 `UserApiService`를 넓히면서 `:feature:splash`의 진입 판정을 회귀 대상으로 세운 것이 그 예다.
- **`DataSource`도 같은 단위를 따른다.** `UserRemoteDataSource` 하나가 `UserApiService`를 감싼다. 그 인터페이스에는 여러 feature가 쓰는 함수가 섞이며(`isRegistered()`는 splash, `getMe()`·`register()`·`updateMe()`는 profile), 자기가 안 쓰는 함수의 계약을 건드리지 않는다.
- **도메인 `Repository`는 계속 관심사 단위다.** `ProfileRegistrationRepository`(등록 **여부**)와 `ProfileRepository`(프로필 **값**)가 같은 `UserRemoteDataSource`를 쓰면서 따로 있는 것은 중복이 아니다. 데이터 레이어에서 합쳐진 것이 도메인에서도 합쳐져야 하는 것은 아니다.
- **`ApiService`가 커진다.** 한 태그의 오퍼레이션이 많으면 파일 하나가 길어진다. 그것을 이유로 feature별로 쪼개지 않는다 — 쪼개는 기준은 파일 길이가 아니라 서버 리소스 경계다. 서버가 태그를 나누면 그때 나눈다.

## 고려한 대안

**feature 단위로 둔다(`ProfileApiService`·`SplashApiService`).** 각 feature가 자기 서비스를 온전히 소유해 남의 파일을 고칠 일이 없고, 회귀 범위가 자기 feature에 갇힌다. 그러나 같은 엔드포인트의 소유자가 feature 수만큼 늘고, 경로·지역 정책·서버 코드 상수가 함께 복제되며, 그 복제를 **타입 시스템이 잡아주지 못한다.** 서버가 계약을 바꾸는 날 조용히 갈라진다. 격리의 이득보다 갈라짐의 대가가 크다. 기각.

**경로 접두어를 단위로 삼는다(`/api/v1/users` → `UserApiService`).** 태그를 몰라도 판정할 수 있고 대개 태그와 일치한다. 그러나 리소스가 중첩되는 경로에서 흔들린다 — `POST /api/v1/rooms/{roomId}/members`는 경로상 `rooms` 밑이지만 태그는 `invitation`이고, `GET /api/v1/rooms/{roomId}/members`는 같은 경로에 태그가 `room`이다. 경로로 가르면 이 둘이 한 서비스에 묶여 서버가 선언한 경계와 어긋난다. 기각.

**규칙을 세우지 않고 사례별로 판단한다.** 유연하고 문서가 늘지 않는다. 그러나 **적혀 있지 않아서 갈라진 것이 이 ADR의 발단이다.** profile은 세 문서를 다 읽고도 `ProfileApiService`가 틀렸다고 볼 근거가 없었다. 사례별 판단은 대조를 빠뜨린 사람에게는 판단할 기회조차 주지 않는다. 기각.

**`ApiService` 층을 없애고 `DataSource`가 Ktor를 직접 부른다.** 소유 단위 문제 자체가 사라진다. 그러나 봉투 해제 지점([응답 봉투 ADR](2026-08-27-response-envelope-unwrapped-in-apiservice.md))과 HTTP 세부의 경계([`core/data/README.md`](../../core/data/README.md) §5)가 함께 무너지고, Ktor 타입이 `network/` 밖으로 샌다. 범위를 훨씬 넘는 변경이고 이 문제를 풀자고 치를 대가가 아니다. 기각.
