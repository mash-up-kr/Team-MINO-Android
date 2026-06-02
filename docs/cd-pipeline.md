# CD 파이프라인

GitHub Actions + Fastlane 기반 Play Store 배포 자동화.

## 배포 모델

Play Console 앱은 `team.mino` **단일 앱**. `prodRelease` 빌드를 내부테스트 트랙에 올려 검증하고, 같은 빌드를 production으로 **승급(promote)**한다. 재빌드하지 않으므로 검증한 바이너리가 그대로 출시된다.

`qaRelease`(=`team.mino.qa`, qa-api)는 Play에 올리지 않고 **APK를 GitHub Release(prerelease)에 첨부**해 QA가 직접 다운로드한다.

배포는 `release/*` 브랜치에서 일어나고, `main` 병합은 출시 기록(마무리)이다. 브랜치 전략은 [conventions/branch-naming.md](conventions/branch-naming.md) 참조.

## 단계와 트리거

| 단계 | 워크플로 | 트리거 | 게이트 |
|---|---|---|---|
| ① QA 배포 | `cd-qa.yml` | `push: release/**` | — |
| ② 내부테스트 | `cd-internal.yml` | 수동 `workflow_dispatch` | QA 통과 판정 |
| ③ production 승급 | `cd-promote.yml` | 수동 `workflow_dispatch` | 내부테스트 통과 판정 |
| ④ main 병합·태그 | (배포 아님) | git PR/태그 | ③ 제출 성공 후 |

`versionCode`는 `github.run_number`로 자동 주입된다. ③은 ②가 올린 빌드를 그대로 승급하며 `version_code` 입력을 비우면 internal 최신 빌드를 승급한다.

## 필요한 GitHub Secrets

| 이름 | 내용 |
|---|---|
| `KEYSTORE_PROPERTIES_B64` | `keystore.properties`의 base64 |
| `KEYSTORE_QA_B64` | `keystore/qa.jks`의 base64 |
| `KEYSTORE_PROD_B64` | `keystore/prod.jks`의 base64 |
| `PLAY_SERVICE_ACCOUNT_JSON` | Play 업로드용 서비스계정 JSON 키 (원문) |
| `DISCORD_WEBHOOK_URL` | 배포 알림 webhook |

base64 인코딩 예: `base64 -i keystore/prod.jks | pbcopy`

## 필요한 GitHub Variables

| 이름 | 내용 |
|---|---|
| `PLAY_INTERNAL_OPT_IN_URL` | 내부테스트 옵트인/설치 링크 (디스코드 알림에 첨부, 없으면 링크 생략) |

## Play Console 사전 준비 (1회)

1. `team.mino` 앱 생성 및 최초 설정(데이터 보안·콘텐츠 등급·개인정보처리방침 등) 완료
2. Google Cloud 서비스계정 생성 → Play Console 권한 부여 → JSON 키 발급 → `PLAY_SERVICE_ACCOUNT_JSON`
3. **첫 AAB는 콘솔에서 수동 업로드 1회** (fastlane supply는 앱을 생성하지 못함)
4. 내부테스트 테스터 목록(이메일/구글그룹) 등록

## 실행 방법

- ① `release/x.y.z` 브랜치에 push → 자동으로 qaRelease APK 빌드 → `qa-x.y.z` prerelease에 첨부. QA는 Release 페이지/디스코드 링크에서 APK 다운로드
- ② Actions → **CD - Internal Testing** → Run workflow → 대상 `release/x.y.z` 선택, `version_name` 입력
- ③ Actions → **CD - Promote to Production** → Run workflow → (선택) `version_code`·`version_name` 입력

## 후속 과제

- 디스코드 알림 카드 정보 구성 확정
- 배포 단계 스킬화 (`/release-*`)
