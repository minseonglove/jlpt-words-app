import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

// Top-level build file where you can add configuration options common to all sub-projects.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.detekt) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.7.1")
        android.set(true)
        outputToConsole.set(true)
        enableExperimentalRules.set(true)
    }

    apply(plugin = "io.gitlab.arturbosch.detekt")

    configure<DetektExtension> {
        // 복잡도 계측만 하므로 기본 룰셋을 얹지 않고 detekt.yml 에 켠 룰만 실행한다
        buildUponDefaultConfig = false
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        // KMP 모듈은 main/test 관례를 따르지 않으므로 소스 루트를 직접 지정한다
        // (commonMain·androidMain·iosMain·*Test 가 모두 src 아래에 있음)
        source.setFrom(files("src"))
        basePath = rootProject.projectDir.absolutePath
        // 복잡도 계측이 목적이므로 위반이 있어도 빌드를 세우지 않는다
        ignoreFailures = true
        parallel = true
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "17"
        reports {
            xml.required.set(true)
            html.required.set(true)
            txt.required.set(false)
            sarif.required.set(false)
            md.required.set(false)
        }
    }
}

// detekt 계측 -> 집계까지 한 번에. 결과 해석은 scripts/complexity/report.py 참고.
tasks.register<Exec>("complexityReport") {
    group = "verification"
    description = "전 모듈의 순환복잡도를 계측하고 분포·리팩토링 우선순위를 출력한다"
    dependsOn(subprojects.map { "${it.path}:detekt" })
    workingDir = rootDir
    commandLine("python3", "scripts/complexity/report.py", "--hotspot")
}
