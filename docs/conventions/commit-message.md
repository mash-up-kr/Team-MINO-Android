# 커밋 메시지 컨벤션

**Conventional Commits** 스타일 기반, 메시지는 **한국어**. scope는 사용하지 않고, `Co-Authored-By` 꼬리표도 붙이지 않는다 (작성 보조와 무관하게 커밋 작성자는 개발자).

## 포맷

```
<type>: <한국어 제목>

<선택: 본문 — "왜" 바꿨는지 중심>
<선택: 푸터 — BREAKING CHANGE, 이슈 참조 등>
```

제목은 한 줄, 마침표 없음, 완료형/요약형. 본문·푸터는 필요할 때만.

## type 목록

| type | 쓰임 |
|---|---|
| `feat` | 신규 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 동작 변화 없는 코드 구조 개선 |
| `docs` | 문서·주석·KDoc 수정 |
| `chore` | 빌드·설정·의존성·CI 등 부수 작업 |
| `test` | 테스트 추가/수정 |
| `style` | 포맷·세미콜론·공백 등 동작 무관 스타일 변경 |
| `perf` | 성능 개선 |

scope는 사용하지 않는다 (`feat(auth):` 같은 표기 금지).

## 예시

```
feat: 로그인 API 연동
```

```
docs: 디자인 시스템 KDoc 작성
```

본문이 필요한 경우:

```
fix: 홈 화면 진입 시 크래시 해결

탭 전환 시 Fragment 재생성 타이밍과 ViewModel 초기화가
충돌. onViewCreated에서 상태 복원 순서를 조정함.
```

## BREAKING CHANGE

하위 호환을 깨는 변경이면 푸터에 명시:

```
BREAKING CHANGE: SharedPreferences → EncryptedSharedPreferences 이전. 기존 설치본 재로그인 필요.
```

## 쪼개기 원칙

커밋 하나에 하나의 의도. 여러 논리 단위(기능 ↔ 리팩토링, 프로덕션 ↔ 테스트, 모듈 간, 의존성 ↔ 기능)가 섞이면 분리한다. 섞이면 리뷰·리버트·히스토리 추적이 어렵다.
