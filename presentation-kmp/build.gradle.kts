import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.aboutlibraries)
}

kotlin {
    android {
        namespace = "com.minseonglove.jlptwords.presentation"
        compileSdk = 36
        minSdk = 28

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }

        androidResources {
            enable = true
        }

        // host-side(JVM) 유닛 테스트 활성화 — commonTest 를 JVM 에서 실행한다.
        withHostTest {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "PresentationKmp"
            isStatic = true
        }
    }

    // Google Mobile Ads SDK 를 SwiftPM 으로 받아 Kotlin cinterop 바인딩을 생성한다 (CocoaPods 대체).
    swiftPMDependencies {
        iosMinimumDeploymentTarget.set("15.0")
        swiftPackage(
            url = url("https://github.com/googleads/swift-package-manager-google-mobile-ads.git"),
            version = exact("12.12.0"),
            products = listOf(product("GoogleMobileAds")),
            importedClangModules = listOf("GoogleMobileAds"),
        )
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":domain-kmp"))
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.ui.tooling.preview)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.orbit.core)
                implementation(libs.orbit.compose)
                implementation(libs.kotlinx.collections.immutable)
                implementation(libs.navigation.compose)
                implementation(libs.navigationevent.compose)
                implementation(libs.aboutlibraries.core)
                implementation(libs.aboutlibraries.compose)
                implementation(libs.compose.ui.backhandler)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.koin.android)
                implementation(libs.androidx.activity.compose)
                implementation(libs.play.services.ads)
            }
        }
        val iosMain by creating {
            dependsOn(commonMain)
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
    }
}

composeCompiler {
    // domain-kmp 엔티티를 stable 로 인식시켜 State 비교가 인스턴스 동일성이 아닌 equals 로 이뤄지게 한다.
    stabilityConfigurationFiles.add(layout.projectDirectory.file("compose_stability.conf"))
}

compose.resources {
    publicResClass = true
    packageOfResClass = "jlptwords.presentation_kmp.generated.resources"
    generateResClass = always
}

aboutLibraries {
    export {
        outputFile = file("src/commonMain/composeResources/files/aboutlibraries.json")
        prettyPrint = true
    }
}
