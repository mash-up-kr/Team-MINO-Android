# 래스터 이미지 리소스는 WebP 확장자를 사용한다

- **상태**: Accepted
- **작성일**: 2026-08-01
- **작성자**: full_avocado

## 컨텍스트

`core:design-system`/feature 모듈에 `ImageVector`로 변환할 수 없는 사진·일러스트 성격의 래스터 이미지를 추가할 때, 지금까지는 Figma export 결과인 PNG를 그대로 커밋해왔다. PR #110(초대장 카드) 작업에서 봉투 일러스트 PNG 6개(`drawable-mdpi/xhdpi/xxhdpi` × back/front)를 `cwebp -lossless -q 100`으로 WebP로 변환해본 결과, 픽셀 동일성을 유지하면서 파일 크기만 줄어드는 것을 확인했다.

## 결정

프로젝트 전반의 래스터 이미지 리소스는 **WebP 확장자로 저장**한다. Figma에서 export한 원본이 PNG여도 커밋 전 무손실(`-lossless -q 100`) 변환을 거친다. PNG·JPEG 등 다른 래스터 포맷은 새로 추가하지 않는다.

## 근거

- Android 리소스 참조는 `R.drawable.<name>`으로 확장자 비의존적이라, 포맷 전환에 코드 변경이 필요 없다.
- `-lossless` 옵션으로 변환하면 픽셀 단위로 원본과 동일해 시각적 손실이 없다.
- 같은 시각 품질에서 파일 크기가 줄어 APK 크기에 유리하다.

## 결과

- `core/design-system/README.md` 5.3절에 이 규칙을 반영.
- 앞으로 래스터 이미지를 추가할 때는 Figma export(PNG) → `cwebp -lossless -q 100`으로 변환 → 밀도별(`drawable-mdpi/xhdpi/xxhdpi`) 폴더에 WebP로 커밋하는 흐름을 따른다.
- 기존에 이미 커밋된 PNG 리소스를 일괄 전환하는 마이그레이션은 이 결정의 범위 밖이며, 건드릴 일이 있을 때(리팩터·기능 변경 등) 함께 전환한다.

## 고려한 대안

- **PNG 유지**: 별도 변환 단계 없이 Figma export 결과를 그대로 쓸 수 있지만, 동일 시각 품질에서 파일 크기가 더 크다. 무손실 WebP 변환이 코드 변경 없이 적용 가능하다는 점이 확인되어 채택하지 않음.
