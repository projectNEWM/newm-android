plugins {
    id("com.android.library")
    kotlin("android")
}

apply(from = "../../../gradle_include/compose.gradle")

android {
    namespace = "io.newm.feature.musicplayer"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        resourcePrefix = "musicplayer"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.workmanager)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.koin.android)
    implementation(libs.play.services.auth)
    implementation(project(Modules.coreResources))
    implementation(project(Modules.coreTheme))
    implementation(project(Modules.coreUiUtils))
    implementation(project(Modules.shared))

    testImplementation(project(Modules.testUtils))
}
