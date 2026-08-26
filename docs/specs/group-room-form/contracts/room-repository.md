# 계약: 도메인 (`RoomRepository` · UseCase)

**대상 스펙 경로**: `docs/specs/group-room-form` · **부속 문서**: [plan.md](../plan.md)

`:core:domain`이 밖으로 여는 표면이다. ViewModel은 이 계약만 알고 `:core:data`의 존재를 모른다.

> 작성 규칙(반환 타입·`operator fun invoke`·UseCase 생성 기준)은 [`core/domain/README.md`](../../../../core/domain/README.md)가 소유한다.

---

## 1. `RoomRepository`

`:core:domain/repository/RoomRepository.kt`

```
interface RoomRepository {
    suspend fun getRoom(roomId: String): Room
    suspend fun createRoom(draft: RoomDraft): Room
    suspend fun updateRoom(roomId: String, draft: RoomDraft): Room
}
```

| 함수 | swagger 대응 | 성공 | 실패 | 근거 |
|---|---|---|---|---|
| `getRoom` | `GET /api/v1/rooms/{roomId}` | 편집 폼을 채울 `Room` | `MinoDomainException` throw | FR-013 · [research.md](../research.md) R-005 |
| `createRoom` | `POST /api/v1/rooms` | 만들어진 `Room`(`id`·`ownerId` 포함) | 〃 | FR-010 |
| `updateRoom` | `PATCH /api/v1/rooms/{roomId}` | 수정된 `Room` | 〃 | FR-015 |

- **`Result`를 반환하지 않는다.** 실패는 throw다 — [`error_handling.md`](../../../conventions/error_handling.md) §3·§4.
- **`Flow`를 반환하지 않는다.** 세 함수 모두 1회성 요청이고, 방 목록 관찰은 이 feature의 범위가 아니다.
- 반환 타입은 도메인 모델뿐이다. DTO가 이 경계를 넘지 않는다.

---

## 2. `ValidateRoomNameUseCase`

`:core:domain/usecase/ValidateRoomNameUseCase.kt`

```
class ValidateRoomNameUseCase @Inject constructor() {
    operator fun invoke(rawName: String): RoomNameValidation
}
```

| 입력 | 결과 | 근거 |
|---|---|---|
| `""` · `"   "` | `Blank` | FR-002·EC-001 |
| `"민호야 잘하자"` | `Valid` | TS-002 |
| `"민호야 잘하자^^"` · `"팀🎉"` | `InvalidCharacter` | TS-008·EC-005 |
| `" 야호 "` | `Valid` — 판정은 앞뒤 공백을 제거한 값으로 한다 | EC-001 |
| `"ㄱㄱㄱ"` · `"민호ㅇ"`(완성형과 자모 혼재) | `Valid` | EC-025 |

- 허용 문자는 **한글(완성형·자모)·영문·숫자·공백**이다. 자모 단독은 오류가 아니다 — FR-004·EC-025, 이유는 spec §5 답변.
- **길이를 판정하지 않는다.** 상한은 입력 차단이라 이 함수에 도달하는 값이 이미 15자 이하다([research.md](../research.md) R-009).
- `suspend`가 아니다. 글자 단위 입력마다 동기로 불린다(SC-002).

**단위 테스트 대상**: 위 표의 다섯 줄 + 숫자만·영문만·경계값(1자·15자).

---

## 3. `CreateRoomUseCase`

`:core:domain/usecase/CreateRoomUseCase.kt`

```
class CreateRoomUseCase @Inject constructor(
    private val roomRepository: RoomRepository,
) {
    suspend operator fun invoke(draft: RoomDraft): Room
}
```

**하는 일은 하나다** — `draft.color`가 `null`이면 `RoomColor.GRAY`로 확정한 뒤 `roomRepository.createRoom`을 호출한다(FR-006·TS-007). 회색 기본값은 도메인 규칙이므로 ViewModel이나 Mapper가 아니라 여기 있다([research.md](../research.md) R-010).

**편집에는 대응 UseCase를 두지 않는다.** `updateRoom`은 단일 API·단순 전달·재사용 없음·비즈니스 규칙 없음 네 조건을 모두 만족해 ViewModel이 Repository를 직접 호출한다([`core/domain/README.md`](../../../../core/domain/README.md) §4). 편집 경로에는 회색 기본값 규칙이 적용되지 않는다 — 폼에 색 해제 수단이 없어 `color`가 `null`로 들어올 일이 없다.

---

## 4. 실패의 성격

| 상황 | 통로 | UI | 근거 |
|---|---|---|---|
| 편집 진입 조회 실패 | `UiState.loadError` | 에러 화면 + 재시도 | error_handling §5 (주 데이터 로드) |
| 생성 요청 실패 | `DomainErrorEmitter` | Route가 수집 → 스낵바. **저장 확인 모달은 닫힌 채**로 폼에 머무르고 입력값은 그대로 남는다 | UX-003·**EC-009** |
| 편집 요청 실패 | `DomainErrorEmitter` | 〃 (편집 경로에는 모달이 없다) | UX-003·**EC-014** |

리프는 `MinoDomainException.Network` · `Http(code)` 둘뿐이다. 문구 매핑의 소유자는 [`error_handling.md`](../../../conventions/error_handling.md) §5·§8이 정한다.
