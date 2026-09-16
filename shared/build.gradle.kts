import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    android {
        namespace = "com.minseonglove.jlptwords.shared"
        compileSdk = 36
        minSdk = 28

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }

        androidResources {
            enable = true
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(project(":presentation-kmp"))
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // export(":presentation-kmp") 대상은 api 여야 한다 (Kotlin 2.4 부터 링크 시 강제)
                api(project(":presentation-kmp"))
                implementation(project(":domain-kmp"))
                implementation(project(":data-kmp"))
                implementation(libs.koin.core)
            }
        }
        val androidMain by getting {
            dependencies {
            }
        }
        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
            }
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
    }
}
