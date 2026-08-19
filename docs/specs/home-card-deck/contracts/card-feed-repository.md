# 계약: `CardFeedRepository`

**대상 스펙 경로**: `docs/specs/home-card-deck` · **계획서**: [plan.md](../plan.md)

`:core:domain`이 소유하는 인터페이스. **이번 설계는 인터페이스까지이며 `:core:data` 구현은 포함하지 않는다**([research.md](../research.md) D5).

---

## 1. 인터페이스

```
interface CardFeedRepository {
    suspend fun getCards(roomId: String): List<PlaceCard>
    suspend fun recordAccess(pinId: String)
}
```

배치·바인딩 규칙은 [`core/domain/README.md`](../../../../core/domain/README.md)와 [`conventions/dependency-injection.md`](../../../conventions/dependency-injection.md)를 따른다 — 인터페이스는 `:core:domain`, 구현과 그 `@Binds`는 `:core:data`가 소유한다.

## 2. 오퍼레이션

### `getCards(roomId): List<PlaceCard>`

- **대응 API**: `GET /api/v1/rooms/{roomId}/cards`
- **반환**: 최대 10개. 서버가 개인별 큐레이션으로 이미 본 카드를 제외해서 준다.
- **호출 시점**: 홈 진입, `장소 더 보기` 클릭, 방·정렬 전환(홈 셸 소관).
- **실패**: [`conventions/error_handling.md`](../../../conventions/error_handling.md)의 도메인 예외 매핑을 따른다. 소비는 호출자 ViewModel.

### `recordAccess(pinId)`

- **대응 API**: `POST /api/v1/pins/{pinId}/accesses`
- **의미**: 사용자가 그 카드를 확인했음을 기록. 서버 큐레이션의 제외 조건이자 `친구들이 많이 본 곳` 집계 원천.
- **호출 시점**: `CardDeck`의 `onCardConfirmed` 콜백을 받은 홈 셸이 호출.
- **실패 정책**: 실패해도 **사용자 흐름을 막지 않는다.** 카드 넘김은 이미 일어났고 되돌릴 수 없다. 기록 실패는 다음 덱의 중복 노출로 이어질 뿐이므로 조용히 흘린다.

## 3. 미해결 사항

| # | 내용 | 영향 |
|---|---|---|
| **[TBD-1]** | `Card` 응답에 **장소분류 라벨 필드가 없다.** spec FR-009를 충족할 수 없다 | `PlaceCard.label`을 채울 수단이 없어 **`:core:data` 구현 착수 불가**. 백엔드 필드 추가 필요 ([research.md](../research.md) D6) |
| **[TBD-2]** | `GET /rooms/{roomId}/cards`가 **`[TBD]` 상태**다. "큐레이션 기획 변경 진행 중 … 파라미터·응답 구성은 잠정" | 확정 전 DTO·Mapper를 만들면 재작업. 카드덱 UI는 이 확정을 기다리지 않아도 된다 |
| **[TBD-3]** | 정렬 기준 파라미터가 없다 | 홈 셸이 정렬 칩·자동 전환을 구현할 때 시그니처 확장이 필요할 수 있다. **카드덱은 영향 없음** |
