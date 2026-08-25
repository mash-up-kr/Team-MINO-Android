# 디자인 시스템 텍스트 입력의 글자 수는 grapheme cluster로 센다

- **상태**: Accepted
- **작성일**: 2026-08-25
- **작성자**: Jaesung Lee

## 컨텍스트

`MinoTextArea`는 상한과 카운터를 `InputTransformation.maxLength`와 `state.text.length`로 처리했다. 둘 다 **UTF-16 코드 유닛**을 센다. 반면 화면에 `n/30`으로 보이는 숫자와 사용자가 세는 글자는 다르다.

| 입력 | 화면 글자 | 코드 유닛 | 고치기 전 카운터 |
|---|---|---|---|
| `팀 회식 🍺🍻` | 7 | 9 | `9/30` |
| `🍺` × 15 | 15 | 30 | `30/30` — 여기서 막힘 |
| `👨‍👩‍👧` × 3 | 3 | 24 | `24/30` |

경계 동작이 더 나빴다. `InputTransformation.maxLength`는 초과를 만드는 변경을 **통째로 되돌린다.** 29/30에서 2코드 유닛짜리 이모지를 넣으려 하면 잘려 들어가는 것이 아니라 **아무 일도 일어나지 않았다** — 키를 눌렀는데 화면이 반응하지 않는 것으로 보인다.

이 지점은 공동방 생성·편집 폼(이슈 #146) 구현 중 드러났다. 방 설명은 30자 상한에 글자 수 제한만 있고 **문자 종류 제한이 없어**, 이모지가 흔히 들어오는 필드다.

## 결정

`MinoTextArea`의 상한 판정과 카운터를 **UAX #29 grapheme cluster** 단위로 바꾼다. `android.icu.text.BreakIterator.getCharacterInstance()`가 그 단위를 준다(`minSdk` 29라 사용 가능).

- `util/text/GraphemeLength.kt` — `CharSequence.graphemeLength()`
- `component/textinput/MaxGraphemeLengthTransformation.kt` — 상류 `InputTransformation.maxLength`를 대체

둘 다 `internal`이고 **API 표면은 바뀌지 않았다.** `maxLength`·`showCounter` 파라미터, `TextFieldState` 기반 상태 모델, `MinoTextInputDefaults`·`TextInputTokens` 모두 그대로다.

**방 이름 쪽은 바꾸지 않는다.** 허용 문자(한글 완성형·자모·영문·숫자·공백)가 전부 BMP 안이라 코드 유닛과 화면 글자가 항상 1:1이다. 우연이 아니라 허용 문자 집합이 보장하는 성질이므로 `length`로 세도 된다. 허용 문자가 넓어지면 그때 재검토한다.

## 근거

**코드포인트로는 부족하다.** 서로게이트 쌍은 잡히지만 `👨‍👩‍👧`는 이모지 3개를 ZWJ 2개가 잇는 시퀀스라 코드포인트로 5다. 화면과 맞추려면 grapheme cluster여야 한다.

**상태 모델과 세는 단위는 다른 축이다.** `MinoTextArea`를 `MinoTextField`처럼 `value: String` 기반으로 바꾸는 것으로는 풀리지 않는다 — Kotlin의 `String.length`도 코드 유닛이기 때문이다. 그래서 상태 모델은 그대로 두고 세는 단위만 고쳤다.

**접근성 시맨틱을 상류와 같게 유지했다.** `InputTransformation.maxLength`는 `applySemantics`로 `maxTextLength`를 함께 노출한다. 걷어내면서 빠뜨리면 TalkBack·자동입력이 상한을 읽지 못하므로 `MaxGraphemeLengthTransformation`이 같은 시맨틱을 다시 붙인다.

**이미 상한을 넘은 값이 들어와도 필드가 잠기지 않게 좁혔다.** 상류 `maxLength`는 "결과가 초과인가"만 보므로, 서버가 30 grapheme을 넘는 값을 돌려주면 **지우는 편집까지 되돌아가** 편집이 불가능해진다. 서버 상한이 확정되지 않은 동안 실제로 열려 있는 경로라, 길이가 **늘어난** 경우에만 되돌리도록 했다.

**다른 화면도 함께 나아진다.** PRD가 코멘트 입력에 `N/200` 카운터를 요구하면서 특수문자를 허용하므로, 그 화면이 `MinoTextArea`를 쓰면 같은 문제를 그대로 겪었을 것이다.

## 결과

**`MinoTextArea`를 쓰는 모든 화면이 이 단위를 따른다.** 호출부는 바뀌지 않지만 렌더되는 숫자와 상한에 걸리는 시점이 달라진다.

**이 변경은 소비자가 컴파일로 알 수 없다.** 시그니처가 그대로인 채 동작만 달라지는 종류다. 지금 소비자가 카탈로그 프리뷰뿐이라 영향이 없었을 뿐이며, 소비처가 늘어난 뒤 같은 성격의 변경을 할 때는 그 사실을 명시적으로 알려야 한다.

**이런 변경을 규율하는 문서가 이 저장소에 없다.** [M3 컴포넌트 패턴 ADR](2026-07-25-design-system-component-m3-pattern.md)과 [design-system README §6.1](../../core/design-system/README.md#61-컴포넌트-구현-패턴--material3-관례)이 다루는 축(`Defaults`·`Colors`·컴포넌트 토큰 구성, 파라미터를 언제 늘리는가)을 하나도 건드리지 않기 때문에, 통과했다기보다 **해당이 없다.** "표면은 그대로인데 렌더 값이 달라지는 동작 변경"을 어떻게 다룰지는 아직 정해진 규칙이 없다.

**이 동작을 검증하는 자동 장치가 없다.** JVM 단위 테스트는 `android.icu`가 `android.jar` 스텁이라 불가능하고(이 저장소에 Robolectric이 없다), `:core:design-system`에는 `androidTest` 소스셋 자체가 없다. **계측 테스트가 올바른 자리라고 말할 수는 있어도 그 자리가 아직 존재하지 않는다.** 세는 값이 실제로 맞는지는 실행해 봐야 확인되며, 계측 테스트 신설이 후속 과제로 남는다.

**`graphemeLength` 헬퍼의 자리는 잠정적이다.** `:core:design-system`의 `util/text/`에 `internal`로 두었다. 방 이름은 위 이유로 승격이 필요 없고 두 번째 사용처도 아직 코드로 없다. 두 번째 사용처가 생기면 그때 승격 여부를 판정한다.

**`BreakIterator`는 스레드 안전하지 않다.** 입력 컴포넌트가 타자마다 호출하므로 `ThreadLocal`로 인스턴스를 재사용한다.

## 고려한 대안

- **코드포인트 단위로 센다** — 기각. 서로게이트 쌍은 잡히지만 ZWJ 시퀀스(`👨‍👩‍👧`)가 5로 세져 화면과 여전히 어긋난다.
- **`MinoTextArea`를 `value: String` 기반으로 바꾼다** — 기각. `String.length`도 코드 유닛이라 세는 단위 문제가 그대로 남는다. 상태 모델 축과 세는 단위 축은 독립이다.
- **편차를 안고 넘어간다(방 설명에 한해 화면 글자 단위 요구를 완화)** — 기각. 30자짜리 짧은 필드라 피해가 작다고 볼 수 있으나, 방 설명에 이모지를 넣는 것은 흔한 사용 패턴이고 **경계에서 입력이 통째로 씹히는 동작은 사용자에게 설명하기 어렵다.**
- **상한 판정만 고치고 카운터는 그대로 둔다** — 기각. 카운터가 상한과 다른 수를 세면 `29/30`인데 더 못 넣는 상태가 생겨 오히려 나빠진다.
