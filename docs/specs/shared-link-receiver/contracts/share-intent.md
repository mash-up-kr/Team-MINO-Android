# 계약: OS 공유 인텐트 수신

**대상 스펙 경로**: `docs/specs/shared-link-receiver`

**명세서**: [spec.md](../spec.md) · **계획**: [plan.md](../plan.md)

이 feature가 **외부 세계에 노출하는 유일한 표면**이다. 다른 앱과 OS가 이 계약을 통해 꾹을 호출한다.

---

## 1. 노출 표면

```xml
<!-- :feature:sharereceiver — AndroidManifest.xml -->
<activity
    android:name=".ShareReceiverActivity"
    android:exported="true"
    android:theme="@style/Theme.Mino.Transparent"
    android:excludeFromRecents="true"
    android:launchMode="singleTask"
    android:taskAffinity="">
    <intent-filter>
        <action android:name="android.intent.action.SEND" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="text/plain" />
    </intent-filter>
</activity>
```

| 속성 | 값 | 근거 |
|---|---|---|
| `exported` | `true` | OS 공유 시트에 노출되어야 한다 (FR-001) |
| `theme` | 투명 배경 | 앱의 다른 화면을 그리지 않고 딤 위에 시트만 띄운다 (FR-003, UX-001) |
| `excludeFromRecents` | `true` | 저장 후 물러나면 흔적을 남기지 않는다 (FR-011). **`taskAffinity`가 함께 있어야 의미를 갖는다** — 앱 태스크에 얹혀 있으면 걷어낼 자기 태스크가 없다 |
| `taskAffinity` | `""` (빈 문자열) | **앱의 태스크와 섞이지 않는다.** 선언하지 않으면 기본값이 `applicationId`라 `MainActivity`와 같은 affinity가 되고, 앱이 실행 중일 때 공유가 들어오면 꾹의 태스크가 통째로 전면으로 올라온다 (FR-003, TS-027, TS-028 — [research.md R-023](../research.md)) |
| `launchMode` | `singleTask` | 공유가 연달아 들어와도 시트가 겹치지 않는다 (EC-013). 새 인텐트는 새 인스턴스가 아니라 §2.3의 `onNewIntent`로 온다 |

- **런처 Activity가 아니다.** 스플래시(`[SCR-001]`)를 거치지 않는 별도 진입점이다 (FR-019, UX-010).
- **앱의 태스크에 들어가지 않는다.** 이 Activity는 항상 자기 태스크에서 뜨고, 그 아래에는 공유를 보낸 외부 앱이 남는다. 앱이 실행 중이든 종료돼 있든 동작이 같다 (FR-003 · [research.md R-023](../research.md)).
- `mimeType`은 `text/plain` 하나다. 이미지·동영상 파일 공유는 받지 않는다 — spec §4 가정이 "이미지·동영상 파일 자체가 공유된 경우는 URL 없음(EC-002)과 같게 다룬다"로 정했고, 필터에 넣지 않으면 애초에 공유 시트에 꾹이 뜨지 않아 같은 결과가 된다.

---

## 2. 입력

| 항목 | 값 |
|---|---|
| `Intent.action` | `ACTION_SEND` |
| `Intent.type` | `text/plain` |
| `Intent.getStringExtra(Intent.EXTRA_TEXT)` | 공유된 텍스트. URL이 본문 문구와 섞여 있을 수 있다 |

### 2.1 URL 추출 규칙

`ExtractSharedUrlUseCase`가 `EXTRA_TEXT`에서 URL을 뽑는다.

| 입력 | 결과 | 근거 |
|---|---|---|
| 문구 + URL 1개 | 그 URL | FR-002, TS-007 |
| URL 여러 개 | **가장 앞에 등장하는 하나** | FR-002, EC-003 |
| URL 없음 | `null` | EC-002 |
| `EXTRA_TEXT` 자체가 없음 | `null` | EC-002 |

- 한 번의 공유로 저장되는 장소는 1개다. URL이 여러 개여도 장소를 여러 개 만들지 않는다(spec §4 가정).

### 2.2 URL이 없을 때

시트를 띄우지 않고 **저장 오류 알림 경로로 넘긴다**(EC-002 — "저장을 진행하지 않고, 알림함에 `장소를 저장하지 못했어요.` 오류 알림을 남긴다").

> [!NOTE]
> 알림 생성은 서버가 맡는다([contracts/shared-place-save-api.md](./shared-place-save-api.md) §3). 클라이언트에는 URL 없이 호출할 저장 엔드포인트가 없으므로, **이 경우 클라이언트는 아무 요청도 보내지 않고 조용히 종료한다.** EC-002가 요구하는 알림이 실제로 남으려면 서버가 이 사례를 인지할 방법이 필요하며, 이는 [contracts/shared-place-save-api.md](./shared-place-save-api.md)의 서버 협의에 함께 올린다.

### 2.3 이미 떠 있는 시트에 새 공유가 도착할 때

`launchMode=singleTask`이므로 두 번째 `ACTION_SEND`는 새 인스턴스가 아니라 **기존 인스턴스의 `onNewIntent`로 온다.** URL을 읽는 곳이 `onCreate` 하나뿐이면 시트가 옛 링크를 쥔 채 남아, 사용자가 방금 공유한 것과 다른 링크가 저장된다.

| 새 인텐트 | 동작 | 근거 |
|---|---|---|
| URL이 있다 | 시트의 링크를 새 URL로 교체하고 방 선택을 비운다. 방 목록은 다시 조회하지 않는다 | EC-013, SC-001, UX-009 |
| URL이 없다 | 무시하고 떠 있는 시트를 유지한다 | EC-002는 "시트를 띄우지 않는다"이지 "떠 있는 시트를 걷는다"가 아니다. 사용자 조작 없이 시트가 사라지는 경로를 FR-012가 두지 않는다 |
| 저장 완료 토스트 중에 도착한다 | 토스트 단계를 되돌리고 새 시트를 띄운다. 앞선 저장은 이미 워커로 넘어가 취소되지 않는다 | EC-013, FR-010 |

`setIntent(intent)`로 태스크 레코드의 인텐트를 갈아끼우고, `SavedStateHandle`의 URL도 함께 덮는다 — 전자는 프로세스가 통째로 재생성될 때를, 후자는 ViewModel만 복원될 때를 덮는다. 배선은 [research.md R-024](../research.md)가 소유한다.

---

## 3. 출력

이 Activity는 호출자에게 결과를 돌려주지 않는다.

| 종료 경로 | 동작 | 근거 |
|---|---|---|
| 저장 실행 | 워커 예약 → 토스트 → `finish()` | FR-010, FR-011 |
| 사용자 이탈 | 즉시 `finish()` | FR-012, EC-001 |
| URL 없음 | 즉시 `finish()` | EC-002 |

- `setResult`를 호출하지 않는다. 공유 시트를 띄운 외부 앱은 결과를 기대하지 않는다.
- `finish()` 이후 사용자는 공유를 시작한 외부 앱으로 돌아간다 — 이 Activity의 태스크가 사라지면 그 아래 태스크, 곧 공유를 보낸 외부 앱이 드러난다. **앱이 실행 중이었더라도 꾹의 태스크로 돌아가지 않는다**(FR-011, TS-006, TS-028).

---

## 4. `:core:navigation`에 계약을 두지 않는 이유

진입형 feature의 표준 골격은 `:core:navigation`에 `XLauncher`와 `EXTRA_*`를 두지만, 이 feature는 두지 않는다. 근거는 [research.md R-008](../research.md) — 이 화면의 유일한 진입은 OS 공유 인텐트이고, 앱 안에서 이 화면을 여는 feature가 없다. [SCR-002] 온보딩의 공유 방법 튜토리얼은 연습용 가상 화면이라 이 시트를 호출하지 않는다(spec §3.2 비목표).
