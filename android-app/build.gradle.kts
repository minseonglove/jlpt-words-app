import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.minseonglove.jlptwords.androidapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.minseonglove.jlptwords"
        minSdk = 29
        targetSdk = 36
        versionCode = 16
        versionName = "1.1.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val signingPropsFile = rootProject.file(".gradle/signing.properties")
    if (signingPropsFile.exists()) {
        val signingProps =
            Properties().apply {
                load(signingPropsFile.inputStream())
            }
        signingConfigs {
            create("release") {
                storeFile = file(signingProps.getProperty("RELEASE_STORE_FILE"))
                storePassword = signingProps.getProperty("RELEASE_STORE_PASSWORD")
                keyAlias = signingProps.getProperty("RELEASE_KEY_ALIAS")
                keyPassword = signingProps.getProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        /**
         * 스토어에서 받은 앱과 나란히 깔아 확인하기 위한 개발 빌드.
         *
         * 같은 applicationId 로 덮어쓰면 DB 가 다음 버전으로 마이그레이션되는데, 다운그레이드
         * 경로가 없어 스토어 버전으로 되돌아갈 수 없다. 패키지를 나눠 데이터까지 분리한다.
         * `.dev` 는 Firebase 에 등록돼 있어 원격 콘텐츠도 그대로 받는다.
         */
        create("dev") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            matchingFallbacks += "debug"
        }

        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            isShrinkResources = true
            isDebuggable = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
    }

    sourceSets {
        getByName("androidTest") {
            assets.directories.add(rootProject.file("data-kmp/schemas").absolutePath)
        }
    }
}

dependencies {
    // KMP 모듈 의존성
    implementation(project(":presentation-kmp"))
    implementation(project(":domain-kmp"))
    implementation(project(":data-kmp"))

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // AdMob
    implementation(libs.play.services.ads)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.common.ktx)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(project(":presentation-kmp"))
    androidTestImplementation(project(":data-kmp"))
    androidTestImplementation(project(":domain-kmp"))
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.sqlite.bundled)
    androidTestImplementation(platform(libs.firebase.bom))
    androidTestImplementation(libs.firebase.firestore)
    androidTestImplementation(libs.wire.runtime)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
