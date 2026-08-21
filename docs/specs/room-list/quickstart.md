# Quickstart 검증: 방 리스트 탭 (Room List Tab)

**대상 spec**: [spec.md](./spec.md) 2.1.0 · **대상 plan**: [plan.md](./plan.md)

구현 완료 후 이 기능이 end-to-end로 동작하는지 손으로 확인하는 절차. 계약·데이터 모델 세부는 복제하지 않고 참조만 한다.

## 선행 조건

- `:feature:room`이 `:feature:main`에 탭으로 등록돼 있다.
- `RoomDetailLauncher`·`RoomFormLauncher`([contracts/navigation-launchers.md](./contracts/navigation-launchers.md))가 최소 스텁으로라도 바인딩돼 있다(그렇지 않으면 Hilt 그래프 조립이 실패해 앱이 뜨지 않는다 — [research.md D5·D6](./research.md)).
- 테스트 계정에 공동방 0개(신규 유저) / 1개 / 2개 이상, 세 가지 픽스처가 준비돼 있다.

## 셋업

```bash
./gradlew :app:assembleQaDebug
```

- 빌드 성공이 최소 게이트다(`docs/constitution.md` 「검증 장치의 한계」).

## 검증 시나리오

각 시나리오는 [spec.md](./spec.md)의 테스트 시나리오 ID를 그대로 재현한다 — 상세 Given/When/Then은 spec을 단일 출처로 한다.

1. **진입 & 기본 상태** (TS-001~004, TS-016)
   - 공동방 0개 계정으로 `저장` 탭 진입 → 지도가 뜨고 시트가 `Half`(256dp)로 표출, Nudge 바텀시트 자동 표출(TS-010) 확인.
   - 공동방 1개 → `Half` 360dp. 공동방 2개 이상 → `Half` 380dp + 3번째 카드 스크롤 어포던스.
   - 마이페이지에서 위치 권한을 미리 허용한 계정으로 재진입 → 권한 팝업 없이 즉시 지도 초기화(TS-016).

2. **시트 드래그** (TS-005, TS-015)
   - `Half` → 아래로 드래그 → `Peek`(88dp, 헤더만). → 위로 두 번 드래그 → `Full`(바텀 네비게이션 숨김 확인).
   - `Full`에서 아래로 드래그 → 직전 `Half` 높이로 복귀.

3. **지도 필터** (TS-013, EC-006)
   - `Peek`/`Half`에서 정렬 드롭다운 `최신순` 선택 → 마커가 최근 14일 저장 장소로 좁혀짐.
   - 카테고리 칩 `카페` 선택 후 카페가 없는 계정이면 마커 0개.

4. **`Full` 방 목록** (TS-006~008, EC-003)
   - `Full` 승격 → 개인방(`내 장소`)이 최상단 고정, 공동방이 이어짐.
   - 정렬 칩 `최근 저장 순` → 개인방 고정 유지한 채 공동방만 재정렬.
   - 방 카드 선택 → `RoomDetailLauncher` 호출 확인(방 상세로 전환).
   - 공동방 0개면 Ghost Card만 목록에 남는지 확인.

5. **공동방 생성** (TS-009, EC-004)
   - 시트 `[+]` → `RoomFormLauncher` 호출 확인. 방 이름 없이 완료 시도 → CTA 비활성 유지(방 생성 폼 자체는 [SYS-001] spec 소관, 이 화면은 호출만 검증).
   - 생성 완료 → 방 상세로 직행하는지 확인(`RoomFormLauncher` 결과 → `RoomDetailLauncher` 체이닝).

6. **Nudge & Ghost Card** (TS-010~012, TS-014, EC-005)
   - 공동방 0개 상태에서 `[나중에 만들래요]` → 시트 닫힘, Ghost Card는 하단에 유지.
   - 탭을 벗어났다가 재진입 → Nudge가 다시 뜨는지 확인(로컬 상태 저장 없이 매번 판정 — [research.md D9](./research.md)).
   - 공동방을 1개 생성한 뒤 탭 재진입 → Nudge·Ghost Card 모두 사라짐.

7. **방 상세 복귀 상태 유지** (EC-007) — `room-detail`([issue #161](../room-detail/spec.md))이 구현된 이후에만 검증 가능
   - 방 상세를 `Full`로 본 뒤 `[X]` → 방 리스트도 `Full`로 복귀하는지 확인.

## 기대 결과

- 위 1~6번은 `:feature:room` 단독 구현만으로 검증 가능하다(런처는 스텁이어도 호출 여부까지만 확인).
- 7번은 room-detail 구현이 선행돼야 실제 값 검증이 가능하다 — 그 전까지는 room-list 쪽 시작 인자(`sheetLevelOverride`)가 올바르게 소비되는지만 단위 테스트로 확인한다.
