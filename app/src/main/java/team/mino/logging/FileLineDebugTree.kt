package team.mino.logging

import timber.log.Timber

/**
 * 태그를 `(파일명.kt:줄번호)` 형식으로 남기는 디버그 트리.
 * Android Studio Logcat에서 태그 클릭 시 호출 위치로 바로 이동할 수 있다.
 */
class FileLineDebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String =
        "(${element.fileName}:${element.lineNumber})"
}
