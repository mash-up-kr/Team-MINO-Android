# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Crashlytics 스택 트레이스 복원용: 줄 번호는 유지하고 원본 파일명은 마스킹한다.
# 실제 위치는 빌드 시 자동 업로드되는 매핑 파일로 Crashlytics 콘솔에서 복원된다.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile