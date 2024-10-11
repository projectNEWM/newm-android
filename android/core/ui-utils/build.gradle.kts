plugins {
    id("com.android.library")
    kotlin("android")
}

apply(from = "../../../gradle_include/compose.gradle")

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    namespace = "io.newm.core.ui.utils"

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        resourcePrefix = "core_ui_utils"
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
    api(project(Modules.coreResources))

    implementation(libs.androidx.browser)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material.icons.extended)
    implementation(project(":shared"))
    implementation(project(Modules.coreTheme))
}
