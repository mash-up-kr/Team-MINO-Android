# 컴포넌트·에셋 배치 컨벤션

UI 컴포넌트와 이미지 에셋을 **어느 모듈에 만드는지**, 이미 만든 것을 **언제 공용 모듈로 올리는지**를 정한다. 사람이 짜든 에이전트가 짜든 같다.

이 문서는 **모듈 간 판정**만 소유한다. 모듈 안에서의 자리와 구조는 각 모듈 README가 소유한다.

| 무엇을 | 어디가 소유하는가 |
|---|---|
| 강제 규칙의 본문 (무엇이 위반인가) | [헌법 §기술 표준과 제약](../constitution.md) |
| 결정의 근거·배제한 대안 | [ADR — 방 색상 팔레트](../adr/2026-08-14-room-color-palette-in-design-system.md) · [ADR — Category 전용 칩](../adr/2026-08-03-category-item-dedicated-chip.md) · [ADR — 공통 셸 `MinoScaffold`](../adr/2026-07-31-common-shell-mino-scaffold.md) |
| **아이콘** — 판정 대상이 아니다. 항상 `:core:design-system`(`MinoIcons`) | [design-system README §5](../../core/design-system/README.md#5-아이콘) |
| **특정 모듈의 기능에 종속된 리소스** — `:app`의 런처·매니페스트 자산, `:core:*`가 자기 기능에 쓰는 리소스. 판정 대상이 아니다 | 그 리소스를 쓰는 모듈 |
| 디자인 토큰의 자리·추가 절차 | [design-system README §4](../../core/design-system/README.md#4-토큰-시스템) |
| `Modifier` 확장의 자리 | [design-system README §6.2](../../core/design-system/README.md#62-modifier-확장-구현-규칙) · [common:ui README §3](../../core/common/ui/README.md#3-디렉토리-구조) |
| 에셋을 어떻게 뽑는가 (Figma export) | [figma-design-fidelity §1.3](figma-design-fidelity.md#13-에셋-export--아이콘-svg이미지) |
| 모듈 **안에서의** 자리·패키지·구조 | [design-system README §3](../../core/design-system/README.md#3-디렉토리-구조) · [common:ui README §3](../../core/common/ui/README.md#3-디렉토리-구조) · [feature-module.md §2](../architecture/feature-module.md#2-패키지-구조) |
| 모듈 목록·의존 방향 | [modularization.md](../architecture/modularization.md) |

---

## 1. 판정 — 어느 모듈에 만드는가

| | feature | `:core:common:ui` | `:core:design-system` |
|---|---|---|---|
| **이미지 에셋** | 한 feature 전용 — **기본값** | 둘 이상의 feature가 쓸 때 | 해당 없음 |
| **컴포넌트** | 한 화면 전용 — **기본값** | 둘 이상의 feature가 공유하고 feature 도메인에 묶이지 않을 때 | Figma 디자인 시스템 컴포넌트일 때 (+ 예외 1건) |

**기본값은 언제나 그것을 쓰는 feature다.** 쓰일지 모를 자산을 선제적으로 공용 모듈에 두지 않는다 — 검증되지 않은 API가 공용 표면으로 굳는다. 언제 공용 모듈로 올리는지는 [§2.1 시점](#21-시점)이 소유한다.

**`:core:design-system`은 이미지 에셋을 받지 않는다.** 이 모듈의 표면은 Figma 디자인 시스템이 정하고, 화면용 사진·일러스트는 거기에 속하지 않는다. 그래서 여러 feature가 공유하는 이미지의 자리는 `:core:common:ui`다.

### 1.1 이미지 에셋

`ImageVector`로 변환할 수 없는 사진·일러스트다. 벡터 아이콘은 여기 해당하지 않는다(→ 위 경계표).

**벡터 drawable(`res/drawable/*.xml`)도 이미지 에셋이 아니다.** `MinoIcons`에 들어갈 아이콘이면 `:core:design-system`이 소유하고([README §5.2](../../core/design-system/README.md#52-아이콘-추가하기)), 그 밖의 벡터는 위 경계표대로 그것을 쓰는 모듈이 갖는다.

| 조건 | 위치 |
|---|---|
| 한 feature의 화면에서만 쓴다 | 그 feature의 `src/main/res/drawable-*` — **기본값** |
| 둘 이상의 feature가 쓴다 | `:core:common:ui`의 `src/main/res/drawable-*` |

- 사용처가 하나면 feature에 둔다. 다른 화면에도 쓸 것 같다는 예상은 근거가 아니다.
- Figma에서 내보내는 절차 → [figma-design-fidelity §1.3](figma-design-fidelity.md#13-에셋-export--아이콘-svg이미지)

**포맷과 밀도 — 어느 모듈에 두든 같다**

- **WebP로 저장한다.** PNG·JPEG 등 다른 래스터 포맷은 새로 추가하지 않는다. Figma export 결과가 PNG여도 `cwebp -lossless -q 100 <원본>.png -o <대상>.webp`로 무손실 변환한 뒤 커밋한다. 리소스 참조는 `R.drawable.<name>`이라 확장자와 무관해 코드 변경이 필요 없다.
- **밀도별로 `drawable-mdpi/xhdpi/xxhdpi/`에 나눠 배치한다.** 밀도를 구분하지 않는 `drawable/`은 `IconLocation` 린트 경고 대상이다.
- 배경 → [래스터 이미지 배치·포맷 ADR](../adr/2026-08-19-raster-image-placement-and-format.md)

### 1.2 컴포넌트

**`:core:design-system`에 만든다 — Figma 디자인 시스템에 컴포넌트로 존재할 때.** 디자인 시스템 파일에서 자기 속성 축(size·variant 등)을 갖는 컴포넌트셋으로 배포되는 것은 디자인 시스템의 자산이다. 사용처 개수와 무관하다.

> [!IMPORTANT]
> **거꾸로는 성립하지 않는다.** Figma 디자인 시스템 컴포넌트가 아닌 것은 아무리 여러 feature가 써도 `:core:design-system`에 두지 않는다. 그런 공유는 `:core:common:ui`가 받는다. "토큰만으로 그려진다" · "도메인을 모른다" · "재사용성이 높아 보인다"는 근거가 되지 않는다 — **디자인 시스템의 표면은 Figma가 정한다.** 예외는 바로 아래 하나뿐이다.

#### 예외 — `AtomicColorToken`을 직접 쓰는 컴포넌트

Figma 디자인 시스템 컴포넌트가 아니어도 **`:core:design-system` 안에서만 만들 수 있는 경우가 하나 있다. 컴포넌트가 `AtomicColorToken`의 원시 색을 직접 참조해야 할 때다.** `*Token` 오브젝트는 `internal`이라([design-system README §4.5](../../core/design-system/README.md#45-토큰-규칙)) 다른 모듈에서는 그 값이 아예 보이지 않는다. 선례는 [방 대표 색상 12종 팔레트 ADR](../adr/2026-08-14-room-color-palette-in-design-system.md) — 팔레트 24개 슬롯이 `AtomicColorToken`에 있고 대응하는 시맨틱 슬롯이 없어, 그 색을 쓰는 칩을 다른 모듈에서 만들 방법 자체가 없었다.

- **`AtomicColorToken`을 public으로 열어 예외를 회피하지 않는다.** 원시값이 feature로 새면 [design-system README §4.3](../../core/design-system/README.md#43-사용-패턴--모듈-안팎의-두-갈래)이 정한 "외부는 홀더 프로퍼티로만 접근한다"가 무너진다. 캡슐화를 깨는 비용이 컴포넌트 하나를 디자인 시스템에 두는 비용보다 크다.
- **"토큰을 쓴다"는 예외 사유가 아니다.** 색·타이포·그림자의 시맨틱 값은 `MinoAndroidTheme.*` 홀더 프로퍼티로 모듈 밖에서도 읽을 수 있다. 예외는 **홀더에 대응 슬롯이 없는 원시 색**을 써야 하는 경우로 한정된다.
- **예외로 들어와도 나머지 규칙은 같다.** M3 컴포넌트 패턴([design-system README §6.1](../../core/design-system/README.md#61-컴포넌트-구현-패턴--material3-관례))을 따르고, 도메인 개념(기본값 규칙·서버 식별자 매핑·화면 배치)은 갖지 않는다.

**`:core:common:ui`에 만든다 — 아래를 모두 만족할 때.**

- **둘 이상의 feature가 실제로 같은 컴포넌트를 필요로 한다.** "언젠가 쓸 것 같다"는 제외다([§2.1](#21-시점)).
- **특정 feature의 도메인·네비게이션에 묶여 있지 않다.** 화면 단위라도 feature 고유 ViewModel·Route·도메인 모델에 의존하면 대상이 아니다. 상태를 인자로 받고 콜백을 올리는 stateless 형태로 분리 가능해야 한다.
- **토큰이 아니라 동작·구조다.** 색·타이포·그림자 **값 자체**를 정의하는 것이라면 컴포넌트가 아니라 토큰이므로 `:core:design-system`의 토큰 체계로 간다.

**그 외에는 그 화면의 feature에 만든다.** 자리는 `<screen>/component/` → [feature-module.md §2](../architecture/feature-module.md#2-패키지-구조).

**공유되지만 stateless로 분리되지 않으면 각 feature가 각자 갖는다.** 여기서는 중복을 허용하는 것이 잘못된 공용화보다 싸다 — 공용 표면에 feature 도메인이 한 번 새면, 뒤에 올라오는 컴포넌트가 모두 그 모양에 맞춰진다.

> [!NOTE]
> **판정에 따라 만드는 주체가 갈린다.** feature와 `:core:common:ui`는 feature UI 담당이 그대로 소유해 직접 만들고 옮기지만, `:core:design-system`은 그 범위 밖이라 디자인 시스템 담당에게 넘긴다 → [`ui-developer`](../../.claude/agents/ui-developer.md) · [`design-system-builder`](../../.claude/agents/design-system-builder.md).

---

## 2. 승격 — 이미 만든 것을 공용 모듈로 옮길 때

승격은 **feature → `:core:common:ui`** 한 방향뿐이다. 컴포넌트든 이미지 에셋이든 공용화되는 자리는 이 모듈이다.

**`:core:design-system`으로 승격하는 경로는 없다.** 컴포넌트는 Figma 디자인 시스템 컴포넌트면 [§1.2](#12-컴포넌트)대로 처음부터 거기에 만들고 아니면 계속 feature나 `:core:common:ui`에 두며, 이미지 에셋은 아예 대상이 아니다.

### 2.1 시점

**두 번째 사용처가 생겼을 때 리팩토링한다.** 한 곳에서만 쓰는 동안은 그 feature에 두고, 중복이 실제로 발생한 뒤 공용화하면 잘못된 추상화를 피할 수 있다.

### 2.2 컴포넌트 — feature → `:core:common:ui`

1. 대상 컴포넌트에서 **feature 의존(ViewModel·Route·도메인 모델·feature 리소스)을 걷어내고** stateless로 다듬는다. 필요한 데이터는 파라미터로, 동작은 콜백으로 노출한다.
2. 성격에 맞는 패키지로 옮긴다 → [common:ui README §3](../../core/common/ui/README.md#3-디렉토리-구조).
3. 하드코딩된 색·치수·텍스트 스타일을 `:core:design-system` 토큰으로 교체한다. 토큰이냐 실측값이냐의 판정은 [figma-design-fidelity §2](figma-design-fidelity.md#2-판정--토큰이냐-실측값이냐).
4. 기존 사용처를 새 공용 컴포넌트 호출로 교체하고 중복 정의를 제거한다.
5. `@UiModePreviews` 프리뷰를 함께 둔다.

### 2.3 이미지 에셋 — feature → `:core:common:ui`

1. **밀도별 파일 전부**(`drawable-mdpi/xhdpi/xxhdpi`)를 함께 옮긴다. 일부만 옮기면 남은 밀도에서 조용히 다른 리소스를 집는다.
2. 파일명이 특정 feature를 가리키면 공용 이름으로 바꾼다.
3. 사용처의 `R` import를 `:core:common:ui`의 것으로 교체한다. 리소스 참조 자체는 `R.drawable.<name>`이라 확장자·경로에 영향받지 않는다.

### 2.4 승격하지 않는 것

- **`:core:design-system`으로 옮기는 것.** 사용처가 늘었다는 사실은 design-system으로 가는 사유가 아니다. 컴포넌트든 이미지든 공유는 `:core:common:ui`가 받는다.
  - 다만 **Figma 디자인 시스템 컴포넌트이거나 [§1.2의 예외](#예외--atomiccolortoken을-직접-쓰는-컴포넌트)에 해당하는 것을 feature에 만들어 둔 것**을 뒤늦게 발견했다면, 그것은 승격이 아니라 **처음 판정이 틀렸던 것**이다. §1.2대로 design-system에 만들고 feature 쪽 정의를 지운다.
- **feature의 도메인·Route·ViewModel에 묶인 것.** stateless로 분리되지 않으면 대상이 아니다.
- **화면(Screen) 단위.** 화면은 보통 feature의 Route·인자에 강하게 묶여 대상이 아니다. 드물게 승격하는 경우에만 상태·콜백만 받는 stateless Screen으로 분리하고, feature 쪽에는 ViewModel·Route를 잇는 Route 컴포저블만 남긴다.
- **사용처가 하나뿐인 것.** [§2.1](#21-시점).

---

## 3. 컴포넌트 신설 vs 기존 확장

이미 있는 공용 컴포넌트와 비슷한 것이 필요할 때, 기존 것을 확장할지 전용으로 새로 만들지의 판정이다. 근거는 [Category 전용 칩 ADR](../adr/2026-08-03-category-item-dedicated-chip.md).

- **Figma의 컴포넌트셋 경계를 따른다.** Figma가 둘을 별도 컴포넌트셋으로 두었다면 코드도 나눈다. 같은 컴포넌트셋의 variant면 기존 컴포넌트의 파라미터로 표현한다.
- **디자인에 없는 조합을 공개 API로 열지 않는다.** 기존 컴포넌트를 재사용하려고 상태별 색 슬롯을 일반화하면 Figma 컴포넌트셋에 없는 조합이 공개 API에 생긴다. 디자인 시스템의 SSOT가 Figma라는 전제가 깨진다.
- **나누더라도 치수는 공유한다.** 패딩·셰이프·폰트 같은 기하 값을 두 벌 정의하면 Figma가 값을 바꿀 때 두 곳이 갈라진다. 컴포넌트 토큰(`token/` 아래 `internal`)으로 공유하고 모듈 밖으로는 노출하지 않는다.
- **아직 동작하지 않는 기능의 파라미터를 미리 만들지 않는다.** 실제 호출부가 생길 때 디폴트 인자로 소스 호환 추가한다 → [M3 컴포넌트 패턴 ADR](../adr/2026-07-25-design-system-component-m3-pattern.md).
