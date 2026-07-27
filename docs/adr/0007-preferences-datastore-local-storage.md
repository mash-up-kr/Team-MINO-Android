# 0007. 로컬 키-값 저장소로 Preferences DataStore를 채택한다

- **상태**: Accepted
- **작성일**: 2026-07-27
- **작성자**: Jaesung Lee

> [!NOTE]
> 번호 충돌 주의: 미머지 브랜치 `feature/72-error-handling-convention`에 별도의 ADR 0006·0007이 존재한다. 두 브랜치 중 나중에 develop에 머지되는 쪽이 번호를 재조정해야 한다.

## 컨텍스트

이슈 #89에서 디바이스 ID를 로컬에 저장·조회할 필요가 생겼는데, 레포에는 로컬 저장 인프라(SharedPreferences·DataStore·Room)가 전혀 없었다. 첫 도입이므로 이 선택이 이후 레포의 다른 로컬 키-값 저장(디바이스 토큰 등)의 기본 방식이 된다.

저장 대상인 디바이스 ID·디바이스 토큰은 세션 인증 토큰이 아니라 기기 식별용 값이다.

## 결정

- 로컬 키-값 저장소로 **Preferences DataStore**를 채택하고 **평문으로 저장**한다 (암호화 래핑 없음).
- `DataStore<Preferences>` 인스턴스는 단일 파일(`mino_preferences`) 하나만 만들어 DI(`core:data`의 `storage/DataStoreModule`)로 제공하고, 저장 항목들은 키로 구분해 공유한다.
- 수명주기·변경 이유가 다른 데이터는 같은 인스턴스를 쓰되 **DataSource를 분리**한다 (예: 불변 신원 캐시인 디바이스 ID와 발급·만료 수명주기를 가질 디바이스 토큰은 별도 DataSource).

## 근거

- **Jetpack 표준·코루틴 친화**: suspend/Flow 기반이라 기존 데이터 레이어(suspend DataSource)와 자연스럽게 결합하고, SharedPreferences의 동기 IO 문제가 없다.
- **평문 저장 충분**: 저장 값이 기기 식별용이라 유출 시 세션 탈취로 이어지는 자격 증명이 아니다. 암호화 래핑은 초기 구축·유지 비용 대비 이득이 없다고 판단했다.
- **인스턴스 단일화**: DataStore는 파일당 인스턴스가 강제 단일이어야 하는 프레임워크 제약이 있어, 파일을 늘리기보다 단일 파일·키 분리가 관리가 단순하다. 책임 분리는 인스턴스가 아니라 DataSource 계층에서 한다.

## 결과

- 이후 로컬 키-값 저장이 필요한 작업은 `DataStoreModule`이 제공하는 단일 `DataStore<Preferences>`를 주입받고, 데이터 성격별로 DataSource를 분리해 추가한다 (`core/data/README.md` 로컬 저장 규칙 참조).
- 관계형·대용량 데이터는 이 결정의 범위 밖이다 — 필요 시점에 Room 도입을 별도 검토한다.
- 민감도가 다른 값(실제 자격 증명 등)을 저장하게 되면 평문 결정을 재검토한다.

## 고려한 대안

- **DataStore + 암호화 래핑(Tink 등)**: 토큰 유출 리스크를 민감하게 볼 경우의 선택지. 저장 값이 기기 식별용이라 과잉으로 판단, 구축·유지 비용을 이유로 기각.
- **EncryptedSharedPreferences**: Jetpack Security가 deprecated 수순이라 신규 도입 대상에서 제외.
- **SharedPreferences**: 가장 단순하지만 동기 IO·코루틴 비친화적이라 신규 도입 기준으로 기각.
