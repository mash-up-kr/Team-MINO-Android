package team.mino.feature.sharereceiver.fake

import android.content.res.Resources

/**
 * `RoomSummary.toRoomPickerItem`이 요구하는 [Resources]의 최소 대역.
 *
 * 이 더블이 필요한 이유는 변환이 문구 포맷을 소유하기 때문이다 — 도메인 모델을 카드가 그릴 형태로 옮기는 자리에
 * `장소 N개` 포맷이 붙어 있어(`data-model.md` §5.2) ViewModel이 목록을 만들려면 리소스에 닿아야 한다.
 *
 * **문구는 판정 대상이 아니다.** 여기서 돌려주는 값은 변환이 터지지 않게 하는 자리 채움일 뿐이며, 실제 문구는
 * `res/values/strings.xml`이 소유한다. 이 테스트가 보는 것은 목록의 **구성과 선택 상태**다.
 *
 * 스텁 `android.jar`의 [Resources]를 상속하는 것이 성립하는 것은 이 모듈이 리소스를 통해 하는 일이 문자열 하나뿐이라
 * 나머지 표면에 닿지 않기 때문이다. 리소스 사용이 늘면 이 더블이 아니라 Robolectric 같은 다른 수단이 필요해진다.
 */
@Suppress("DEPRECATION")
internal class FakeResources : Resources(null, null, null) {
    override fun getString(
        id: Int,
        vararg formatArgs: Any?,
    ): String = formatArgs.joinToString(prefix = "string-$id(", postfix = ")")
}
