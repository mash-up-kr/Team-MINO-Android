package team.mino.core.common.android.extension

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * [url]을 기본 브라우저(등 [Intent.ACTION_VIEW]를 처리할 수 있는 앱)로 연다.
 */
fun Context.openUrl(url: String) {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

/**
 * 이 앱의 Play 스토어 상세 페이지를 연다.
 *
 * Play 스토어 앱이 설치돼 있으면 `market://details` Intent로 앱 안에서 열고,
 * 설치돼 있지 않으면([ActivityNotFoundException]) 웹 URL로 폴백한다.
 */
fun Context.openPlayStoreListing() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
    } catch (_: ActivityNotFoundException) {
        openUrl("https://play.google.com/store/apps/details?id=$packageName")
    }
}

/**
 * 이 앱의 시스템 설정 상세 화면(권한 등)을 연다. 권한이 영구 거부돼 시스템 다이얼로그로
 * 재요청할 수 없을 때(EC-003·EC-007) 사용자를 앱 설정으로 안내하는 용도다.
 */
fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)),
    )
}
