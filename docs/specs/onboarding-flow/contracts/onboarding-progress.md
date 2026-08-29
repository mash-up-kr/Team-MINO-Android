# 계약: 온보딩 진행 상태 (`OnboardingProgressRepository` · `ResolveOnboardingStepUseCase`)

**대상 스펙 경로**: `docs/specs/onboarding-flow` · **부속 문서**: [plan.md](../plan.md)

`:core:domain`이 밖으로 여는 표면이다. **온보딩 feature만 쓰는 계약이 아니다** — 완료 표시는 스플래시가 읽는다([research.md R-008](../research.md)).

> 작성 규칙(반환 타입·`operator fun invoke`·UseCase 생성 기준)은 [`core/domain/README.md`](../../../../core/domain/README.md)가 소유한다. 모델의 필드·기본값은 [data-model.md §2](../data-model.md)가 소유한다.

---

## 1. `OnboardingProgressRepository`

`:core:domain/repository/OnboardingProgressRepository.kt`

```
interface OnboardingProgressRepository {
    suspend fun getProgress(): OnboardingProgress
    suspend fun setCurrentStep(step: OnboardingStep)
    suspend fun setCreatedRoomId(roomId: String)
    suspend fun markCompleted()
}
```

| 함수 | 저장 위치 | 성공 | 실패 | 근거 |
|---|---|---|---|---|
| `getProgress` | `DataStore<Preferences>` 3개 키 | 저장된 값. 키가 없으면 기본값 | throw | FR-022·FR-023 |
| `setCurrentStep` | `onboarding_last_step` | — | throw | FR-024 |
| `setCreatedRoomId` | `onboarding_created_room_id` | — | throw | FR-008·EC-021 |
| `markCompleted` | `onboarding_completed` | — | throw | FR-021·FR-024 |

- **`Flow`를 반환하지 않는다.** 진행 상태를 관찰해야 하는 화면이 없다 — 읽는 시점은 온보딩 진입과 스플래시 분기 두 번뿐이다.
- **`Result`를 반환하지 않는다.** 실패는 throw다 — [`error_handling.md`](../../../conventions/error_handling.md) §3.
- **쓰기를 한 함수로 합치지 않는다.** 세 쓰기는 서로 다른 사건이고, 합치면 호출자가 "바뀌지 않은 나머지 필드"를 매번 실어 보내야 해서 덮어쓰기 사고가 생긴다.
- `setCurrentStep`은 **새 스텝을 열기 전에** 불린다. 호출 순서와 그 이유는 [onboarding-flow-ui.md §2.4](onboarding-flow-ui.md)가 소유한다.

### 실패의 성격

로컬 저장 실패는 `MinoDomainException`으로 매핑되는 예상 가능한 실패가 아니라 **버그**다(디스크 I/O 오류·직렬화 오류). [`error_handling.md`](../../../conventions/error_handling.md) §3의 화이트리스트에 DataStore 예외가 없으므로 rethrow되어 CEH로 간다. 이 계약은 그것을 도메인 예외로 위장하지 않는다.

---

## 2. 구현 (`:core:data`)

| 파일 | 역할 |
|---|---|
| `datasource/OnboardingProgressLocalDataSource.kt`(+`Impl`) | 공유 `DataStore<Preferences>`에서 3개 키를 읽고 쓴다. 키 이름과 기본값의 소유자는 [data-model.md §4.1](../data-model.md) |
| `repository/OnboardingProgressRepositoryImpl.kt` | DataSource 값을 `OnboardingProgress`로 조립한다 |
| `datasource/di/`·`repository/di/` | 각 `@Binds` |

- DataStore 인스턴스는 `core/data/storage/DataStoreModule`이 이미 제공한다. **새로 만들지 않는다** — `preferencesDataStore` delegate는 파일당 하나여야 한다([research.md R-007](../research.md)).
- 알 수 없는 `lastStep` 문자열은 `PROFILE`로 떨어뜨린다. 예외를 던지지 않는다 — 저장된 값이 낡은 것일 수 있고, 온보딩을 처음부터 태우는 편이 홈으로 튕기는 것보다 안전하다(SC-002).
- Mapper를 두지 않는다 — DTO가 없다(로컬 전용).

**단위 테스트 대상**: 키가 전부 비어 있을 때의 기본값 · 세 쓰기 각각의 왕복 · 알 수 없는 `lastStep` 문자열의 떨어짐 · `markCompleted` 후 `getProgress().isCompleted`.

---

## 3. `ResolveOnboardingStepUseCase`

`:core:domain/usecase/ResolveOnboardingStepUseCase.kt`

```
class ResolveOnboardingStepUseCase @Inject constructor() {
    operator fun invoke(progress: OnboardingProgress): OnboardingStep
}
```

저장된 진행 상태에서 **열어야 할 스텝**을 계산한다. Repository를 주입받지 않는 순수 함수다 — 조회는 호출자가 하고 이 함수는 판정만 한다. `suspend`가 아니다.

| # | 입력 | 결과 | 근거 |
|---|---|---|---|
| 1 | `lastStep = PROFILE` | `PROFILE` | FR-001 |
| 2 | `lastStep = ROOM_FORM` | `ROOM_FORM` | FR-023·TS-037 |
| 3 | `lastStep = INVITE`, `createdRoomId != null` | `INVITE` | FR-023·EC-021 |
| 4 | `lastStep = INVITE`, `createdRoomId == null` | `TUTORIAL` | FR-004·SC-004 — 방이 없으면 초대할 대상도 없다 |
| 5 | `lastStep = TUTORIAL` | `TUTORIAL` | FR-023·EC-022 — 튜토리얼 내부 위치는 복원하지 않으므로 항상 스텝 1부터다 |

- **`isCompleted`를 보지 않는다.** 완료된 설치는 온보딩을 열지 않으므로 이 함수에 도달하지 않는다. 판정 주체는 스플래시이고, 그 판정의 형태는 §4가 소유한다([R-008](../research.md)·[R-024](../research.md)).
- **#4가 이 함수를 UseCase로 만든 이유다.** 방어 규칙이 없다면 `progress.lastStep`을 그대로 읽으면 되지만, 그 조합이 실제로 만들어질 수 있다 — 공동방 스텝을 건너뛰고 `INVITE`를 기록하는 코드 경로는 없어야 하지만, 저장 값이 손상되거나 이후 스텝 구성이 바뀌면 생긴다.

**단위 테스트 대상**: 위 다섯 줄 전부. Android 의존이 없으므로 JVM 테스트로 덮는다.

---

## 4. 완료 표시를 밖에서 읽는 법 — `ResolveSplashEntryUseCase` 확장

> **plan 2.0.0에서 확정됐고, 2.0.1에서 ADR로 승격됐다.** 1.0.1은 두 안을 적고 "스플래시 계획의 개정이 정한다"로 미뤘는데, spec 1.2.0 §3.2가 **"온보딩을 띄울지 메인 탭을 띄울지 가르는 판정 기준은 이 문서(FR-021·FR-022)가 소유한다"** 로 소유자를 못박아 이 계획이 가져왔다([research.md R-024](../research.md)).
>
> **소유권 규칙과 호출 순서 제약의 근거는 [앱 진입 화면 판정 ADR](../../../adr/2026-08-29-onboarding-entry-decision-owned-by-onboarding.md)이 갖는다.** 아래는 이 feature에서의 구체적인 계약이다.

### 4.1 지금 구현

`:core:domain/usecase/ResolveSplashEntryUseCase.kt`는 근거를 하나만 본다.

```
suspend operator fun invoke(): SplashEntry =
    if (profileRegistrationRepository.isRegistered()) SplashEntry.Main else SplashEntry.Onboarding
```

**프로필을 저장한 뒤 온보딩을 중단한 사용자가 다음 실행에서 홈으로 밀려난다.** TS-037·TS-038이 실패하고, 그 사용자는 남은 스텝을 영영 보지 못한다.

### 4.2 이 계획이 요구하는 형태

```
class ResolveSplashEntryUseCase @Inject constructor(
    private val profileRegistrationRepository: ProfileRegistrationRepository,
    private val onboardingProgressRepository: OnboardingProgressRepository,
) {
    suspend operator fun invoke(): SplashEntry
}
```

| # | 프로필 등록 | 완료 표시 | 결과 | 근거 |
|---|---|---|---|---|
| 1 | 없음 | 무관 | `Onboarding` | FR-021 · 스플래시 spec FR-003 |
| 2 | 있음 | `false` | `Onboarding` | **FR-022·TS-038** — 이번에 더해지는 갈래 |
| 3 | 있음 | `true` | `Main` | FR-021·TS-036·TS-039 |

- **호출 순서가 있다.** `isRegistered()`를 먼저 부른다 — 그 판정이 미등록일 때 프로필 캐시를 비우는 부수 효과를 갖고, 온보딩 화면이 "캐시는 비어 있다"에 기대기 때문이다(`ProfileRegistrationRepository` 문서 주석 · `ProfileEntryPoint.needsRefresh`). 완료 표시를 먼저 읽어 단축 평가로 이 호출을 건너뛰면 그 보장이 깨진다.
- **#1에서 완료 표시를 읽지 않아도 된다.** 프로필이 없으면 온보딩이 확정이다. 다만 위 순서 제약 때문에 성능상 얻는 것이 없으므로 단축 여부는 구현 재량이다.
- **`isCompleted`의 원천은 이 설치의 DataStore 하나다.** 서버에 묻지 않는다(spec §4 가정).

### 4.3 기각한 안

| 안 | 기각 사유 |
|---|---|
| `IsOnboardingCompletedUseCase`를 온보딩 쪽에 두고 스플래시 ViewModel이 조합한다 | 판정 규칙이 ViewModel로 새어 나가 두 조건의 결합을 한 테스트로 덮을 수 없다. TS-038·TS-039가 갈라진다 |
| 온보딩이 진입 직후 스스로 홈으로 튕겨 낸다 | 완료한 사용자가 온보딩 화면을 한 프레임 본다. SC-003 위반 |

### 4.4 이 변경이 스플래시에 남기는 것

- **`:feature:splash`는 손대지 않는다.** `SplashViewModel`은 UseCase를 그대로 부르고 반환 타입 `SplashEntry`도 그대로다.
- **`ResolveSplashEntryUseCaseTest`가 회귀 대상이다.** 기존 2갈래 테스트에 #2가 더해진다.
- 스플래시 spec·plan은 이 판정을 **소비하는 쪽**으로 개정되어야 한다. 이 계획이 그 문서를 고치지 않는다 — 완료 보고에 싣는다.

---

## 5. 이 계약이 지켜지는지 보는 법

| 확인 | 방법 |
|---|---|
| 도메인이 Android를 모른다 | `OnboardingProgress`·`OnboardingStep`·`ResolveOnboardingStepUseCase`에 `android.*` import가 없다 |
| 저장 시점이 전환 앞이다 | 스텝 전이 코드에서 `setCurrentStep` 호출이 `navigate`/`launch`보다 앞선다 |
| DataStore 인스턴스가 하나다 | `preferencesDataStore(` 호출 지점이 `DataStoreModule.kt` 한 곳뿐이다 |
| 진입 판정이 두 근거를 본다 | `ResolveSplashEntryUseCase`가 Repository 둘을 주입받고, `isRegistered()`가 먼저 불린다 |
| 완료 표시를 서버에 묻지 않는다 | `OnboardingProgressRepositoryImpl`에 원격 DataSource 주입이 없다 |
