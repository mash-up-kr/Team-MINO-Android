# Quickstart 검증: 방 상세 (Room Detail)

**대상 spec**: [spec.md](./spec.md) 2.1.3 · **대상 plan**: [plan.md](./plan.md)

구현 완료 후 이 기능이 end-to-end로 동작하는지 손으로 확인하는 절차. 계약·데이터 모델 세부는 복제하지 않고 참조만 한다.

## 선행 조건

- `:feature:room`에 `RoomDetailMain`이 nested Route로 등록돼 있고, `RoomListMain`에서 방 카드 선택 시 `navController.navigate(RoomDetailMain(roomId))`로 진입한다([research.md D1·D2](./research.md)).
- `:core:navigation`에 `ImmersiveRoute`가 정의돼 있고 `:feature:main`의 `MainShell`이 이를 참조하도록 배선돼 있다(그렇지 않으면 방 상세 진입 시 바텀 네비게이션이 계속 보인다 — [research.md D3](./research.md)).
- `RoomFormLauncher`([room-list/contracts/navigation-launchers.md](../room-list/contracts/navigation-launchers.md))가 최소 스텁으로라도 바인딩돼 있다.
- 테스트 계정에 장소 0개 / 장소 1개 이상(카테고리 혼합) / 여러 방에 동시 소속된 사용자, 방장 계정 / 일반 멤버 계정, 개인방 픽스처가 준비돼 있다.

## 셋업

```bash
./gradlew :app:assembleQaDebug
```

- 빌드 성공이 최소 게이트다(`docs/constitution.md` 「검증 장치의 한계」).

## 검증 시나리오

각 시나리오는 [spec.md](./spec.md)의 테스트 시나리오 ID를 그대로 재현한다 — 상세 Given/When/Then은 spec을 단일 출처로 한다.

1. **진입 & 기본 상태, 몰입 화면 확인** (TS-001~003)
   - 방 리스트에서 방 카드 선택 → 그 방 장소만 표시된 지도 + `Half`(256dp) 시트, 상단 방 제목·설명·장소 수 인디케이터·멤버 아바타 확인.
   - 진입 즉시 바텀 네비게이션이 사라지는지 확인(`ImmersiveRoute` 배선 검증).
   - 장소 0개 방으로 진입 → 마커 없음, 시트에 빈 상태 확인(EC-001).

2. **바텀시트 3단 전환 & `[X]` 복귀 상태 유지** (FR-002·FR-004, UX-003)
   - `Half` → 위로 드래그 → `Full`(장소 목록 전체). → 아래로 드래그 → 직전 `Half`로.
   - `Full`에서 `[X]` → 방 리스트가 **`Full`로** 복귀하는지 확인(TS-002). 방 리스트 쪽은 room-list [quickstart.md 시나리오 7](../room-list/quickstart.md)과 같은 검증이다 — `RoomListMain`이 백스택에 남아 상태를 보존한 결과([research.md D2](./research.md)).
   - 더보기[⋮] 위치가 `Peek`에서는 상단, `Half`/`Full`에서는 하단인지 확인(TS-010·TS-011).

3. **정렬·필터·뷰 전환** (TS-004~006)
   - 정렬 드롭다운 `최신순` 선택 → 장소 목록 재정렬(room-list와 같은 `MinoMenu` 컴포넌트, [research.md D4·D13](./research.md)).
   - 카테고리 칩 `카페` 선택 → 카페만 필터링(해당 카테고리 장소가 없으면 빈 목록, EC-003).
   - 뷰 토글 우측 아이콘 클릭 → 카드형 전환(TS-006).

4. **장소 액션 — 다른 방에 공유 / 삭제** (TS-007~009)
   - 장소 카드 [...] → [다른 방에 공유] → 방 선택 시트(`Full` 676dp 고정) → 방 선택 → [공유하기] → `공유가 완료되었습니다.` 3초 토스트, 화면 유지 확인(UX-002). 이미 저장된 방은 체크+비활성 확인(EC-004).
   - [...] → [장소 삭제] → 확인 모달(`UX-001` 문구 그대로) → [삭제] → 목록에서 즉시 사라지는지 확인(SC-003). [취소] → 모달만 닫히고 목록 불변 확인(TS-009).
   - 더보기 메뉴 항목이 "다른 방에 공유"·"장소 삭제" 2개뿐인지 확인("장소 이동" 미노출, EC-007).

5. **멤버 & 방 관리** (TS-010~012, EC-005·EC-006)
   - [친구 +] → 424dp 초대 시트, 참여자 목록 288dp 스크롤 확인. **[SYS-006] 실제 링크 생성·복사·공유 동작은 [research.md D11](./research.md)의 `[TBD]`(데이터 계약 미정)라 이 시나리오는 시트가 뜨는지까지만 검증한다.**
   - 방장 계정으로 더보기 → [방 편집] → `RoomFormLauncher` 편집 모드 호출 확인(값 채움 여부는 `[TBD]`, [research.md D9](./research.md)) → 완료 → 방 상세 복귀 + `방 편집이 완료되었어요` 스낵바 확인(TS-012).
   - 일반 멤버 계정으로 더보기 확인 → [방 편집] 메뉴 자체가 없는지 확인(EC-006).
   - 개인방 진입 → 더보기에 [나가기] 자체가 없는지 확인(EC-002·EC-005).
   - 일반 멤버로 [나가기] → 확인 모달 → [나가기] → [SCR-004] 방 리스트로 이동 확인. **실제 나가기 API 응답 처리는 `[TBD]`(research.md D12)라 이 시나리오는 화면 전환까지만 검증한다.**
   - 방장(N인 공동방)으로 [나가기] → 위임 모달 → 멤버 선택 → [다음] → 위임 후 탈퇴 처리까지의 실제 동작은 `[TBD]`, 모달 흐름(확인 → 위임 대상 선택)까지만 검증.

## 기대 결과

- 위 1~4번, 5번의 화면 전환·모달/시트 노출 여부는 `:feature:room` 단독 구현(`RoomFormLauncher`는 스텁)만으로 검증 가능하다.
- 5번 중 초대 링크 생성·나가기/위임 API의 **실제 데이터 처리**는 [SYS-006]·[SYS-007] 전용 spec이 이 저장소에 아직 없어 검증할 수 없다 — UI 골격(시트·모달이 뜨고 닫히는 것)까지만 확인한다.
- 방 편집([SYS-001])의 실제 값 채움·저장은 `:feature:roomform` 구현이 선행돼야 검증 가능하다.
