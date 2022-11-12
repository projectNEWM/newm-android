import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin(Plugins.multiplatform)
    id(Plugins.kotlinxSerialization)
    id(Plugins.androidLibrary)
    id(Plugins.sqlDelight)
}

android {
    compileSdk = Versions.androidCompileSdk
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = Versions.androidMinSdk
        targetSdk = Versions.androidTargetSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    namespace = "io.newm.common"
}

kotlin {
    android()

    val xcf = XCFramework()
    listOf(
//        iosX64(),
//        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            xcf.add(this)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                with(Ktor) {
                    implementation(clientCore)
                    implementation(clientJson)
                    implementation(clientLogging)
                    implementation(contentNegotiation)
                    implementation(json)
                }
                with(SqlDelight) {
                    implementation(runtime)
                }
                with(Koin) {
                    api(core)
                }
                with(Kotlin ) {
                    implementation(coroutinesCore)
                    implementation(serializationCore)
                }
            }
        }
//        val commonTest by getting {
//            dependencies {
//                implementation(Koin.test)
//                implementation(Kotlin.coroutinesTest)
//                implementation(kotlin("test-common"))
//                implementation(kotlin("test-annotations-common"))
//            }
//        }
//        val androidMain by getting {
//            dependencies {
//                with(Ktor) {
//                    implementation(clientOkhttp)
//                    implementation(clientAndroid)
//                }
//                implementation(SqlDelight.androidDriver)
//            }
//        }
//        val androidTest by getting {
//            dependencies {
//                implementation(kotlin("test-junit"))
//                implementation("junit:junit:4.13.2")
//            }
//        }
//        val iosX64Main by getting
//        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
//            iosX64Main.dependsOn(this)
//            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                with(Ktor) {
                    implementation(ios)
                    implementation(iosDarwin)
                }
                implementation(SqlDelight.nativeDriver)
            }
        }
//        val iosX64Test by getting
//        val iosArm64Test by getting
//        val iosSimulatorArm64Test by getting
//        val iosTest by creating {
//            dependsOn(commonTest)
//            iosX64Test.dependsOn(this)
//            iosArm64Test.dependsOn(this)
//            iosSimulatorArm64Test.dependsOn(this)
//        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

sqldelight {
    database("NewmDatabase") {
        packageName = "io.newm.common.db"
        sourceFolders = listOf("sqldelight")
    }
}