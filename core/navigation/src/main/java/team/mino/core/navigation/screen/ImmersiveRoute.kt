package team.mino.core.navigation.screen

/**
 * 바텀 네비게이션을 숨겨야 하는 몰입 화면임을 표시하는 빈 마커 인터페이스.
 *
 * 탭 셸(`:feature:main`의 `MainShell`)이 현재 목적지가 이 마커를 구현하는지만 검사해
 * `bottomBar` 슬롯을 조건부로 그린다. 셸이 구체 Route 타입이나 feature 이름을 알 필요가
 * 없도록, 몰입 화면을 만드는 feature는 자신의 Route에 이 마커를 함께 구현한다.
 */
interface ImmersiveRoute
