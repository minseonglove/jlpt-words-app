# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Koin 은 등록·조회 모두 KClass 를 키로 쓰므로 난독화된 이름이어도 짝이 맞는다.
# 다만 리플렉션 경로가 클래스 메타데이터를 읽으므로 kotlin.Metadata 는 남긴다.
-keep class kotlin.Metadata { *; }
