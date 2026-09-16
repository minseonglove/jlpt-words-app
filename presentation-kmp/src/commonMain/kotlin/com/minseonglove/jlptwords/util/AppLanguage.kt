package com.minseonglove.jlptwords.util

/**
 * 앱 UI 언어. [languageTag] 는 Compose 리소스(values-<tag>)와 매칭되는 BCP 47 언어 코드다.
 * 한국어는 별도 values-ko 없이 기본 리소스(values)로 폴백된다.
 */
enum class AppLanguage(
    val languageTag: String,
) {
    KOREAN("ko"),
    JAPANESE("ja"),
}

/**
 * 앱 UI 언어를 [language] 로 즉시 전환한다.
 *
 * 플랫폼 로케일을 직접 바꾸는 것 외에 다른 방법이 없다. compose-resources 가 언어를 정하는 경로는
 * 두 갈래인데, 둘 다 앱이 주입할 수 없기 때문이다.
 *  - `stringResource` 등 컴포저블 경로는 `LocalComposeEnvironment` 를 거치는데 이 CompositionLocal 과
 *    `ComposeEnvironment` 인터페이스가 라이브러리 internal 이다.
 *  - `getString` 등 비컴포저블 경로는 `getSystemResourceEnvironment()` 가 플랫폼 로케일을 직접 읽는다.
 *    `getString(environment, resource)` 오버로드는 공개돼 있지만 `ResourceEnvironment` 의 생성자가
 *    internal 이라 원하는 언어의 환경 객체를 만들 수 없다.
 *
 * 그래서 CompositionLocal 만으로 바꾸면 스낵바·문의 메일 본문 같은 비컴포저블 문구가 시스템 언어로
 * 남아 화면과 어긋난다. 라이브러리가 환경 주입을 공개하면 그때 옮긴다.
 */
expect fun applyAppLanguage(language: AppLanguage)
