import java.text.SimpleDateFormat
import java.util.Date

apply(from = "../../gradle_include/compose.gradle")
apply(from = "../../gradle_include/circuit.gradle")
apply(from = "../../gradle_include/flipper.gradle")

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
    kotlin("android")
    kotlin("kapt")

    id("io.sentry.android.gradle") version "4.9.0"
}


android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    namespace = "io.newm"
    testNamespace = "io.newm.test"
    defaultConfig {
        applicationId = "io.newm"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = getCurrentDateTimeVersionCode()
        versionName = getCustomVersionName(major = 1)
        testInstrumentationRunner = "io.newm.NewmAndroidJUnitRunner"
        testApplicationId = "io.newm.test"
    }

    packaging {
        resources {
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
        }
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = false
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isMinifyEnabled = false
        }
    }
    flavorDimensions += "version"

    productFlavors {
        create("production") {
            namespace = "io.newm"
            applicationId = "io.newm"
            dimension = "version"
        }
        create("development") {
            namespace = "io.newm"
            applicationId = "io.newm"
            applicationIdSuffix = ".dev"
            dimension = "version"
        }
        all {
            resValue(
                "string",
                "account_type",
                "$applicationId${applicationIdSuffix.orEmpty()}.account"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    kapt {
        correctErrorTypes = true
    }
}

dependencies {

    implementation(libs.process.phoenix)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.material)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.launchdarkly.client)
    implementation(libs.play.services.auth)
    implementation(libs.recaptcha)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.koin.android)
    implementation(libs.kotlin.reflect)
    implementation(platform(libs.firebase.bom))
    implementation(project(Modules.barcodeScanner))
    implementation(project(Modules.coreResources))
    implementation(project(Modules.coreTheme))
    implementation(project(Modules.coreUiUtils))
    implementation(project(Modules.login))
    implementation(project(Modules.musicPlayer))
    implementation(project(Modules.shared))

    testImplementation(libs.junit)
    testImplementation(libs.mockk)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.mockk.android)
}


sentry {
    org.set("project-newm")
    projectName.set("android")

    // this will upload your source code to Sentry to show it as part of the stack traces
    // disable if you don't want to expose your sources
    includeSourceContext.set(true)
    telemetry.set(true)
}


/**
 * Generates a version code based on the current date and time in the format `yyMMddHH`.
 *
 * The version code is an integer composed of:
 * - `yy`: The last two digits of the current year.
 * - `MM`: The current month.
 * - `dd`: The current day of the month.
 * - `HH`: The current hour (24-hour format).
 *
 * The function formats the current date and time using `SimpleDateFormat`,
 * converts it into a string, and then parses it as an integer.
 *
 * @return An integer representing the current date and time in the format `yyMMddHH`.
 */
fun getCurrentDateTimeVersionCode(): Int {
    val dateFormat = SimpleDateFormat("yyMMddHH")
    return dateFormat.format(Date()).toInt()
}

/**
 * Generates a custom version name based on the provided major version and the current date and time.
 *
 * The version name follows the format: `major.yyMMdd.HHmm`, where:
 * - `major`: The major version number passed as a parameter.
 * - `yy`: The current two-digit year.
 * - `MMdd`: The current month and day.
 * - `HH`: The current hour in 24-hour format.
 * - `mm`: The current minute.
 *
 * The function retrieves the current date and time using `SimpleDateFormat` to format each component.
 *
 * Example output for `major = 1` on October 1st, 2024 at 13:45 would be: `1.241001.1345`.
 *
 * @param major The major version number to be used as the first part of the version name.
 * @return A custom version name string in the format: `major.yyMMdd.HHmm`.
 */
fun getCustomVersionName(major: Int): String {
    val yearFormat = SimpleDateFormat("yy")
    val monthDayFormat = SimpleDateFormat("MMdd")
    val hourFormat = SimpleDateFormat("HH")
    val minuteFormat = SimpleDateFormat("mm")

    val year = yearFormat.format(Date())
    val monthDay = monthDayFormat.format(Date())
    val hour = hourFormat.format(Date())
    val minute = minuteFormat.format(Date())

    return "$major.$year$monthDay.$hour$minute"
}