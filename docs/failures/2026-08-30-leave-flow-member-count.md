# 방장 나가기 모달이 방 멤버 수를 사전에 세지 않아 실제 공유방에도 "혼자라 삭제된다" 문구를 보였다

- **상태**: Resolved
- **발생일자**: 2026-08-30
- **작성자**: Chea-yunzi
- **관련 ADR**: 없음 — 문서화되지 않은 결정(`docs/specs/room-detail/research.md` D15에 plan 문서로만 기록됨, 별도 ADR은 없었음)
- **관련 커밋/PR**: 원본 구현 `95aedf2`(방 상세 화면 구현 및 room-list 연동) — `RoomDetailViewModel.onLeaveClick`이 이 커밋에서 `research.md` D15("클라이언트는 방 멤버 수를 사전에 세지 않는다")를 그대로 코드로 옮겼다. 이번 수정은 같은 브랜치(`feature/154-room-list/room-detail-task`)의 커밋 예정 변경.

## 무엇을 시도했는가

`research.md` D15는 PRD [SYS-007](나가기/방장 위임)의 분기를 서버 에러 코드로 그대로 표현할 수 있다고 판단했다 — `DELETE /rooms/{roomId}/members/me`가 방장+다른 멤버 존재 시 `409 OWNER_TRANSFER_REQUIRED`를 돌려주므로, 클라이언트가 멤버 수를 미리 세지 않고 그 `409` 하나로 `LeaveDialogState.DelegateOwner`(위임 대상 선택)로 전이하면 된다는 것이었다. "서버가 SSOT이므로 같은 판정 로직을 클라이언트가 중복 구현할 이유가 없다"는 게 근거였다.

구현은 이 판단 그대로: `onLeaveClick()`이 `isOwner`만 보고 항상 `LeaveDialogState.ConfirmOwnerSingle`("나가면 방이 삭제돼요" 문구)을 먼저 띄우고, 사용자가 [나가기]를 눌러 `leaveRoom` 호출이 실제로 `409`를 받아야만 그때 `DelegateOwner`로 바뀌었다.

## 무엇이 잘못됐는가

실기기 확인 결과, 멤버가 2명 이상인 **공유방의 방장**이 [방 나가기]를 누르면 실제로는 위임이 필요한 상황인데도 화면엔 "방을 나가면 방이 삭제돼요 / 나 혼자 있는 방이라, 나가면 방과 저장된 모든 장소가 함께 삭제돼요"라는 **사실과 다른 문구**가 먼저 보였다. 서버가 결국 `409`로 옳게 판정해 다음 화면(위임 대상 선택)으로 넘어가긴 하지만, 그 직전에 사용자에게 거짓 경고가 노출되는 것 자체가 문제였다 — "서버가 최종적으로 옳게 판정한다"는 것과 "그 사이 화면이 정확한 정보를 보여준다"는 것은 별개였다.

## 어떻게 발견했는가

실기기(무선 디버깅 연결) 데모 중 방장 계정으로 멤버 2명짜리 공유방에서 [방 편집 / 방 나가기] 메뉴를 열어 확인 — `GET /api/v1/rooms`로 `type: "shared"`, `memberCount: 2`인 방임을 API 응답으로도 재확인했다.

## 무엇으로 대체했는가

`RoomDetailUiState.room.memberSummary`는 화면 진입 시 `loadRoomMembers()`가 이미 `GET /rooms/{roomId}/members` 실측으로 채워 둔다 — 별도 왕복 없이 `visibleAvatars.size + overflowCount`로 정확한 멤버 수를 즉시 알 수 있다. `onLeaveClick()`을 이 값으로 바로 분기하도록 바꿨다: `isOwner`이고 멤버가 2명 이상이면 곧장 `LeaveDialogState.DelegateOwner`로 전이하고 멤버 목록을 조회한다. `onLeaveConfirm()`의 `409` 처리는 판단 이후 경합(다른 멤버가 그 사이 탈퇴 등)에 대비한 방어선으로 그대로 남겼다.

멤버 선택 목록의 스크롤 영역 높이도 Figma 재확인(`node 3276-208669`)으로 240dp → 288dp로 함께 바로잡았다.

변경 파일: [`RoomDetailViewModel.kt`](../../feature/room/src/main/java/team/mino/feature/room/detail/vm/RoomDetailViewModel.kt)(`onLeaveClick`), [`RoomOwnerLeaveDialog.kt`](../../feature/room/src/main/java/team/mino/feature/room/detail/component/RoomOwnerLeaveDialog.kt)(`OwnerDelegateTokens.MemberListMaxHeight`), [`room-detail-main-contract.md`](../specs/room-detail/contracts/room-detail-main-contract.md)("분기 규칙 — 나가기 플로우").
