# feature를 `api`/`impl` 두 모듈로 분리한 규약이 탭 feature에서 성립하지 않았다

- **상태**: Resolved
- **발생일자**: 2026-07-30
- **작성자**: Jaesung Lee
- **관련 ADR**: 없음 — 문서화되지 않은 결정(컨벤션 문서로만 존재). 대체 결정은 [feature 모듈은 `api`/`impl`로 나누지 않고 단일 모듈로 둔다](../adr/2026-07-30-single-feature-module.md)
- **관련 커밋/PR**: `d2fe203` chore: feature api/impl 컨벤션 플러그인 분리 (2026-06-09), `ff41b8d` feat: 샘플·홈 feature에 네비게이션 패턴 적용 (2026-06-09) — PR #29 (이슈 #27, 머지 2026-06-11)

## 무엇을 시도했는가

화면 네비게이션 기반 구조를 잡으면서 feature를 **두 모듈로 나누는 규약**을 도입했다.

- `:feature:x:api` — 전환 계약만 노출한다. `interface XLauncher : ActivityLauncher`와 `EXTRA_*` 상수. compose·hilt를 적용하지 않는 경량 모듈로 두고, 전용 컨벤션 플러그인(`mino.android.feature.api`)을 만들었다.
- `:feature:x:impl` — Activity·화면·ViewModel·Launcher 구현. 자신의 `api`와 **상대 feature의 `api`에만** 의존하고, 다른 feature의 `impl` 의존은 금지했다.

목적은 feature 간 결합을 전환 계약 한 겹으로 좁히는 것이었다. Hilt가 `XLauncherImpl`을 상대 `api`의 `XLauncher`로 주입해주므로 `impl`끼리 서로를 알 필요가 없다는 설계였다.

## 무엇이 잘못됐는가

**규약이 Activity로 진입하는 feature만 전제하고 있었다.** 바텀 네비게이션 탭처럼 셸의 그래프에 중첩 Route로 편입되는 화면 묶음에는 적용되지 않는다.

- 탭 화면은 Activity로 진입하지 않으므로 Launcher도 `EXTRA_*`도 없다. 즉 `api`에 넣을 것이 하나도 없는데, 규약("feature는 두 모듈로 만든다")이 빈 모듈을 강제한다.
- 셸이 탭 화면을 `screen<T>`로 등록하려면 화면 컴포저블에 닿아야 한다. `api`는 compose 미적용이고 `api`→`impl` 의존은 방향이 거꾸로라 등록 함수를 `api`에 둘 수 없다. 결국 Route와 등록 함수를 `impl`에서 public으로 열어야 하는데, 그러면 **`api`가 담당했던 공개 계약 역할이 `impl`로 옮겨가 `api`의 존재 이유가 사라진다.**
- 분리가 보장한다고 여겼던 경계가 실제로는 검증되지 않는다. `lint.xml`에 룰이 38개 있지만 **모듈 의존 방향을 검사하는 룰이 없고**, 경계는 문서와 리뷰에만 의존한다. 같은 수준의 보장은 단일 모듈 + 문서 규칙으로도 얻는다.

남은 것은 비용뿐이다 — feature마다 모듈 2개, 컨벤션 플러그인 2종, `settings.gradle.kts`·`:app` 등록 2줄, `api`에 compose·hilt를 넣지 않는 규칙 유지.

## 어떻게 발견했는가

이슈 #95에서 BottomNavigation 탭 셸(`:feature:main`)을 구현하면서, 탭 화면을 앞으로 탭별 모듈로 분리한다는 전제를 세운 것이 계기였다. 셸이 다른 모듈의 화면을 그래프에 등록할 방법을 찾다가 후보 세 가지를 검토했다.

- 등록 함수를 `impl`에 public으로 두고 셸이 상대 `impl`을 의존한다(레퍼런스 ppac `FarmemeNavHost`가 쓰는 형태)
- `:core:navigation`에 등록 계약을 두고 각 `impl`이 Hilt `@IntoSet`으로 바인딩한다
- `:app`이 탭 그래프를 조립한다

첫 번째가 유력하다고 보고 그 제약을 하나씩 짚는 과정에서, 어떤 후보를 골라도 **탭 feature의 `api`가 빈 껍데기가 되는 문제**와 **공개 표면이 `impl`로 이동하는 문제**가 남는다는 것이 드러났다. 문제는 후보 선택이 아니라 api/impl 분리 자체라는 결론에 도달했다.

## 무엇으로 대체했는가

**feature 모듈을 단일 모듈로 둔다.** 상세와 근거는 [대체 ADR](../adr/2026-07-30-single-feature-module.md)에 있다. 요지만 적으면:

- `api`/`impl` 분리와 `impl`→`impl` 의존 금지 규약을 폐기한다.
- feature를 **진입형**(Activity로 독립 진입 — 재사용 플로우, 온보딩·로그인 등)과 **탭 feature**(중첩 navigation screen 단위로 셸 그래프에 편입)로 구분하고, `:feature:main`이 탭 feature를 직접 의존하는 것을 예외로 허용한다.
- feature 간 순환 참조는 계속 금지한다. 탭 간 전환은 `:feature:main`이 콜백으로 배선한다.

마이그레이션은 이슈 #104에서 완료했다. `:feature:sample`·`:feature:home`·`:feature:main`이 각각 단일 모듈이 됐고, 컨벤션 플러그인도 하나로 합쳐졌다. ADR이 "별도 작업으로 정한다"로 남겼던 전환 구조는 [feature 간 전환 계약은 `:core:navigation`에 두고, 탭 feature는 등록 함수로 셸 그래프에 편입한다](../adr/2026-08-01-single-module-navigation-contract.md)로 확정했고, `:feature:home`을 첫 탭 feature로 전환해 등록 경로를 컴파일로 검증했다.

재발 방지로 두 가지를 남긴다. 모듈 분리 규약을 만들 때 **그 규약이 전제하는 화면 진입 방식을 문서에 명시한다** — 이번 규약은 "Activity로 진입하는 feature"만 전제했는데 그것이 적혀 있지 않아 탭 화면이 등장할 때까지 드러나지 않았다. 그리고 경계를 규약으로 강제하려면 **검증 장치를 함께 넣는다** — 문서로만 선언된 의존 규칙은 지켜지는지 확인할 수 없고 안전감만 남는다. 모듈 의존 검증은 분리 여부와 독립적으로 도입할 수 있다.
