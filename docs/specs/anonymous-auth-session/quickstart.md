# Phase 1 검증 가이드: 비회원 익명 인증 세션

**대상 스펙 경로**: `docs/specs/anonymous-auth-session`

**소속 문서**: [plan.md](./plan.md) — 부속 산출물이며 독자 버전을 갖지 않는다.

구현이 계약대로 동작함을 증명하는 절차다. 계약 본문은 [contracts/](./contracts/), 모델은 [data-model.md](./data-model.md)가 소유하며 여기서 복제하지 않는다.

> **이번 범위의 판정선**: plan 1.1.0에서 검증용 임시 배선을 만들지 않기로 했으므로, `ensureSession()`을 부르는 프로덕션 코드가 없다. 따라서 이번 범위에서 실행할 수 있는 것은 §2뿐이고, 앱을 띄워야 판정되는 절차는 §3으로 분리해 실행 조건과 함께 남긴다. 근거는 [plan.md](./plan.md) §세션 확보의 호출자·§전제와 이연 항목.

---

## 1. 선행 조건

| # | 조건 | 확인 방법 |
|---|---|---|
| P-1 | Firebase 콘솔에서 이 프로젝트의 **익명 인증 제공자가 사용 설정**되어 있다 | 콘솔 → Authentication → Sign-in method. 꺼져 있으면 세션 확보가 항상 "그 밖의 실패"로 떨어진다 |
| P-2 | `app/google-services.json`이 그 프로젝트의 것이다 | 이미 저장소에 있다. flavor별 applicationId(`com.mino.gguk.qa`·`com.mino.gguk`)가 콘솔에 등록되어 있어야 한다 |

P-1·P-2는 §3을 실행할 때 필요하다. §2의 자동 검증은 Firebase를 띄우지 않으므로(R-015) 이 조건 없이도 돌아간다.

---

## 2. 이번 범위에서 실행하는 검증

```bash
# 빌드 게이트 (헌법 §품질 게이트)
./gradlew :app:assembleQaDebug

# 단위 테스트 (Repository 멱등성·예외 매핑·헤더 첨부) — 전부 :core:data
./gradlew :core:data:testQaDebugUnitTest

# Lint (가능한 환경에서). :core:data·:app은 flavor가 붙어 있어 lintDebug가 아니라 lintQaDebug다
./gradlew :core:data:lintQaDebug :app:lintQaDebug
```

> 로컬 Lint는 JBR JIT 이슈로 데몬이 죽을 수 있다 — 그 경우의 판정은 헌법 §검증 장치의 한계를 따른다.
> `:core:error-handling`은 Kotlin JVM 모듈이라 Lint 태스크 자체가 없다.

### 자동 검증이 덮는 시나리오

| 테스트 대상 | 기대 결과 | 시나리오 |
|---|---|---|
| `AnonymousAuthRepositoryImpl` — 최초 확보 | Fake가 익명 로그인을 1회 호출하고 `userId`가 반환된다 | TS-001 |
| `AnonymousAuthRepositoryImpl` — 재호출 | 두 번째 호출에서 로그인 호출 수가 늘지 않는다 | TS-004 · SC-001 |
| `AnonymousAuthRepositoryImpl` — 동시 호출 | N개 코루틴이 동시에 호출해도 로그인 호출은 1회 | TS-003 · SC-004 |
| 예외 매핑 — 연결 실패 | `MinoDomainException.Network`가 던져진다 | TS-005 · FR-018 |
| 예외 매핑 — 발급 실패 | `MinoDomainException.Auth`가 던져진다 | TS-020 · FR-018 |
| 예외 매핑 — 열거 밖 예외 | 매핑되지 않고 원본이 그대로 전파된다 | [`error_handling.md`](../../conventions/error_handling.md) §3 |
| 예외 매핑 — 취소 | `CancellationException`이 원본 그대로 전파된다 | 같은 문서 §3 |
| Ktor 플러그인 — Mino host | `Authorization: Bearer` 헤더가 실린다 | TS-013 |
| Ktor 플러그인 — 외부 host | 헤더가 실리지 않는다 | TS-016 · FR-011 |
| Ktor 플러그인 — 요청 내용 | 앱이 만든 사용자 식별자가 헤더·본문·쿼리에 없다 | TS-014 · FR-009 |

`ktor-client-mock`으로 요청을 가로채 헤더를 확인한다. 기존 `DomainExceptionMappingTest`가 같은 도구를 쓴다.

### V-10. 기기 식별자 경로 소멸 — TS-007 · FR-015

앱을 띄우지 않고 판정할 수 있어 이번 범위에 포함한다.

```bash
# 결과가 비어 있어야 한다 (문서·기록은 제외)
grep -ril "ANDROID_ID\|ensureDeviceId\|DeviceRepository\|DeviceIdLocalDataSource\|DeviceInfoProvider" \
  --include="*.kt" --include="*.kts" --include="*.xml" . | grep -v "/build/"
```

**기대**: 출력 없음. 사용자 구분의 경로가 익명 세션 하나만 남는다.

---

## 3. 진입 화면 구현 이후로 이연된 검증

아래는 **앱이 세션 확보를 실제로 호출해야** 판정되는 절차다. 이번 범위에는 그 호출자가 없으므로 실행하지 않는다.

**실행 조건**: 진입 화면(PRD [SCR-001])이 [contracts/anonymous-auth-repository.md](./contracts/anonymous-auth-repository.md) §4의 C-1~C-8을 구현한 시점. 절차는 그때 그대로 쓰이므로 지우지 않고 남긴다. QA 빌드(`assembleQaDebug`) 기준이며, `userId` 확인은 그 화면이 남기는 로그나 디버거로 한다.

### V-1. 최초 실행 세션 생성 — TS-001 · SC-003

1. 앱을 완전히 삭제한다 (`adb uninstall com.mino.gguk.qa`)
2. 네트워크 연결 상태로 설치·실행한다
3. 확보된 `userId`를 확인하고, Firebase 콘솔 → Authentication → Users에 익명 사용자가 1명 늘었는지 확인한다

**기대**: 새 사용자 1명. 진입 화면이 대기 없이 다음 화면으로 넘어간다(UX-003).

### V-2. 재실행 복원 — TS-008

1. V-1 직후 앱을 종료하고 다시 실행한다
2. `userId`가 V-1과 같은지 확인한다

**기대**: 동일 `userId`. 콘솔의 사용자 수가 늘지 않는다.

### V-3. 오프라인 재실행 — TS-009 · SC-001

1. V-1 이후 기내 모드를 켠다
2. 앱을 종료하고 다시 실행한다

**기대**: 세션 확보가 성공하고 진입 화면을 통과한다. 실패 안내가 뜨면 로컬 복원이 아니라 네트워크 발급을 시도했다는 뜻이다 — [research.md](./research.md) R-004의 빠른 경로가 동작하지 않은 것이다.

### V-4. 오프라인 최초 실행 — TS-005 · EC-001

1. 앱을 삭제한다
2. 기내 모드를 켠 상태로 설치·실행한다

**기대**: 진입 화면에 머무르고, **연결 문제**로 인한 실패 안내가 뜬다(`Network`). 데이터 조회 실패로 오인될 문구가 없다(UX-001). 재시도 버튼이 없다(UX-002).

### V-5. 조작 없는 자동 복구 — TS-021 · SC-011

1. V-4 상태에서 앱을 그대로 둔 채 기내 모드를 끈다

**기대**: 아무것도 누르지 않아도 세션이 확보되고 다음 화면으로 전환된다.

### V-6. 그 밖의 실패 구분 — TS-020

1. Firebase 콘솔에서 익명 인증 제공자를 **일시적으로 비활성화**한다
2. 앱을 삭제하고 네트워크 연결 상태로 실행한다

**기대**: V-4와 **다른** 안내(일시적 오류)가 뜬다(`Auth`). 확인 후 제공자를 반드시 다시 활성화한다.

### V-7. 진입 차단 — TS-018 · SC-009

1. V-4 상태에서 뒤로 가기·딥링크 등 가능한 모든 경로로 진행을 시도한다

**기대**: 진입 화면 밖으로 나가는 경로가 없다. 세션 없이 열리는 화면이 하나도 없다.

### V-8. 재설치는 새 사용자 — TS-010 · EC-004

1. V-1 이후 앱을 삭제하고 다시 설치·실행한다

**기대**: 이전과 다른 `userId`. 콘솔에 익명 사용자가 1명 더 생긴다. 이전 데이터가 조회되지 않는 것이 오류로 표현되지 않는다(UX-004).

### V-9. 백업에 세션이 실리지 않는다 — TS-011 · SC-008 · FR-007

백업 규칙 자체는 이번 범위에서 발효되지만(`res/xml/backup_rules.xml`·`res/xml/data_extraction_rules.xml`), 그것이 동작하는지 보려면 세션이 확보된 앱을 백업해야 한다.

1. 백업 매니저를 켜고 transport를 확인한다
   ```bash
   adb shell bmgr enable true
   adb shell bmgr list transports
   ```
2. 세션 확보 상태에서 백업을 수행한다
   ```bash
   adb shell bmgr backupnow com.mino.gguk.qa
   ```
3. 백업 세트 토큰을 확인한다
   ```bash
   adb shell bmgr list sets
   ```
4. 앱을 삭제하고 재설치한 뒤 복원한다
   ```bash
   adb shell bmgr restore <token> com.mino.gguk.qa
   ```
5. 앱을 실행해 `userId`를 확인한다

**기대**: 복원 후에도 이전 `userId`가 살아나지 않고 새 세션이 발급된다. `sharedpref` 제외가 동작한 것이다 — [research.md](./research.md) R-012.

---

## 4. 검증하지 않는 것

| 항목 | 이유 |
|---|---|
| 서버의 신원 증명 검증·데이터 소유권 판정 (TS-015) | spec §3.2 비목표 — 서버가 소유한다. 앱 측 검증은 §2의 헤더 첨부까지다 |
| 실제 Mino 서버 요청에 헤더가 실려 나가는 것 | `Flavor.apiBaseUrl`은 실서버 도메인으로 갱신돼 첨부 판정이 성립하지만, 그 host로 요청을 보내는 `Service`·호출부가 아직 없어 실행할 대상이 없다 — [tasks.md](./tasks.md) N-1 · N-3 |
| 장기 미사용 후 재실행 (TS-019 · FR-017) | 무효화 경로를 **두지 않는** 것이 요구사항이라 코드로 검증할 대상이 없다. V-2가 복원 경로를 대신 덮는다 |
| 지연 상태의 표현과 임계 시간 (TS-022 · FR-019) | 진입 화면 구현이 없어 검증할 대상이 없다. 지연이 임계를 넘었을 때의 합류 조건은 계약 C-7이 소유하고, 구체값은 spec §3.2에 따라 진입 화면 스펙 소관이다 |
