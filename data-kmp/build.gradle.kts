import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.wire)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        namespace = "com.minseonglove.jlptwords.data"
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
            baseName = "DataKmp"
            isStatic = true
        }
        iosTarget.compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-linker-option")
                    freeCompilerArgs.add("-lsqlite3")
                }
            }
        }
    }

    // Firebase iOS SDK 를 SwiftPM 으로 받아 Kotlin cinterop 바인딩을 생성한다 (CocoaPods 대체).
    // Firestore 의 FIR* 심볼은 FirebaseFirestoreInternal 모듈에 들어 있어 그쪽을 바인딩한다.
    // Crashlytics 는 Kotlin 에서 호출하지 않고 FirebaseApp.configure() 가 런타임에 자동
    // 등록하므로, 앱에 링크만 되도록 product 로만 넣고 cinterop 바인딩은 만들지 않는다.
    swiftPMDependencies {
        iosMinimumDeploymentTarget.set("15.0")
        swiftPackage(
            url = url("https://github.com/firebase/firebase-ios-sdk.git"),
            version = exact("12.4.0"),
            products =
                listOf(
                    product("FirebaseCore"),
                    product("FirebaseFirestore"),
                    product("FirebaseAnalytics"),
                    product("FirebaseCrashlytics"),
                ),
            importedClangModules = listOf("FirebaseFirestoreInternal", "FirebaseAnalytics"),
        )
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":domain-kmp"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.room.runtime)
                implementation(libs.sqlite.bundled)
                implementation(libs.koin.core)
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.androidx.datastore.core.okio)
                implementation(libs.wire.runtime)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.network)
                implementation(libs.ktor.http)
                implementation(libs.kotlinx.datetime)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.firebase.analytics)
                implementation(libs.firebase.firestore)
                implementation(libs.koin.android)
                implementation(libs.ktor.client.cio)
            }
        }
        val androidHostTest by getting {
            dependencies {
                // Robolectric 이 SQLite(AndroidSQLiteDriver)를 제공해 host 에서 마이그레이션 테스트를 돌린다.
                implementation(libs.robolectric)
                implementation(libs.androidx.test.core)
            }
        }
        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
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

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}

wire {
    kotlin {
        rpcRole = "none"
        rpcCallStyle = "blocking"
    }
    sourcePath {
        srcDir("src/commonMain/proto")
    }
}

// 스냅샷 생성기(PrepopulatedDbGenerator)의 산출물 쓰기는 -PwriteSnapshot=true 로 명시했을 때만 —
// 전체 테스트 실행이 매번 번들 DB 를 다시 써서 워킹트리를 더럽히지 않도록 한다.
tasks.withType<Test>().configureEach {
    systemProperty("jlptwords.writeSnapshot", providers.gradleProperty("writeSnapshot").getOrElse("false"))
}
