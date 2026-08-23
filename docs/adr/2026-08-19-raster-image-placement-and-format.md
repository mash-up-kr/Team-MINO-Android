# 공유 래스터 이미지는 `:core:common:ui`에 두고, WebP·밀도 규칙은 배치 규약이 소유한다

- **상태**: Accepted
- **작성일**: 2026-08-19
- **작성자**: Jaesung Lee

## 컨텍스트

[래스터 이미지 WebP ADR](2026-08-01-webp-for-raster-images.md)이 포맷을 결정하면서 그 규칙의 반영처를 `core/design-system/README.md` 5.3절로 정했다. 당시 전제는 공유 래스터 이미지가 `:core:design-system`에 있거나 갈 수 있다는 것이었다.

이후 컴포넌트·이미지의 소속 모듈 판정을 [`conventions/component-asset-placement.md`](../conventions/component-asset-placement.md)로 모으는 과정에서 두 가지가 확정됐다.

- **`:core:design-system`의 표면은 Figma 디자인 시스템이 정한다.** 디자인 시스템 파일에 컴포넌트로 존재하지 않는 것은 이 모듈에 두지 않는다. 화면용 사진·일러스트는 거기에 속하지 않는다.
- **그래서 여러 feature가 공유하는 이미지의 자리는 `:core:common:ui`다.** 공용화되는 UI 자산이 모이는 모듈이 거기 하나이기 때문이다.

그 결과 WebP 규칙이 **그 규칙의 적용 대상이 하나도 살지 않는 모듈의 README**에 남았다. 실제로 저장소의 래스터 이미지는 `feature/sample`의 봉투 일러스트뿐이고, `:core:design-system`의 `res/drawable`에는 벡터 XML만 있다. 규칙을 지켜야 하는 사람(feature에 이미지를 넣는 사람)이 design-system README를 열 이유가 없다는 것이 문제의 핵심이다.

## 결정

1. **공유 래스터 이미지는 `:core:common:ui`가 갖는다.** `:core:design-system`은 이미지 에셋을 받지 않는다.
2. **WebP 포맷·밀도별 배치 규칙의 소유 문서를 [`conventions/component-asset-placement.md`](../conventions/component-asset-placement.md) §1.1로 옮긴다.** `core/design-system/README.md` 5.3절은 삭제하고, 그 절이 속한 5장은 아이콘만 다룬다.
3. **포맷 결정 자체는 그대로 승계한다** — 래스터는 WebP로 저장하고, PNG·JPEG 등 다른 래스터 포맷은 새로 추가하지 않는다. 변환 명령과 밀도별 배치를 포함한 본문은 배치 규약 §1.1이 소유한다.

## 근거

- **규칙은 그것을 지켜야 하는 사람이 도달하는 자리에 둔다.** 헌법 2.0.1이 Figma 판정 절차를 모듈 README가 아닌 규약 문서로 옮긴 것과 같은 논리다. 이미지 규칙의 적용 주체는 대부분 feature에 있다.
- **래스터 규칙은 모든 모듈에 걸린다.** 특정 모듈 README가 소유하면, 정작 그 모듈에 존재하지 않는 것을 규정하는 상태가 된다. 지금이 정확히 그 상태였다.
- **디자인 시스템 모듈의 표면은 Figma가 정한다.** 사용처가 늘었다는 이유로 화면 일러스트가 디자인 시스템 자산이 되지는 않는다. 이 원칙을 이미지에만 예외로 두면 컴포넌트 쪽 판정도 함께 흔들린다.

## 결과

- [WebP ADR](2026-08-01-webp-for-raster-images.md)은 `Superseded`가 된다. 포맷 결정은 이 ADR이 승계하므로 유효한 규칙에는 변화가 없다.
- `core/design-system/README.md` 5.3절이 사라지고, 포맷·밀도 규칙은 배치 규약 §1.1이 소유한다.
- `feature/sample`의 봉투 일러스트는 그대로 둔다. 사용처가 하나라 배치 규약 §1.1의 기본값에 이미 맞다.
- `:core:common:ui`에 `res/drawable-*`가 생기는 것은 두 번째 사용처가 실제로 나타났을 때다. 지금 미리 만들지 않는다.
- 기존 PNG 리소스의 일괄 전환은 여전히 범위 밖이다(승계한 결정과 동일).

## 고려한 대안

- **`core/design-system/README.md` 5.3절을 그대로 둔다** — 변경 폭이 0이지만, 적용 대상이 없는 모듈이 규칙을 소유하는 상태가 남는다. 이미지를 넣는 사람이 그 문서에 도달하지 못하는 문제도 그대로다.
- **`core/common/ui/README.md`로 옮긴다** — 공유 이미지의 새 집이라는 점에서 자연스럽지만, 실제 이미지의 대다수인 feature 로컬 이미지를 남의 모듈 README가 규정하게 되어 같은 문제가 반복된다.
- **에셋 전용 규약 문서를 새로 만든다** — 배치 규약과 소유 축(어느 모듈에, 어떤 모양으로)이 같아 문서만 하나 더 늘어난다.
