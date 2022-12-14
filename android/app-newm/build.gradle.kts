plugins {
    id(Plugins.androidApplication)
    id(Plugins.crashlytics)
    id(Plugins.googleServices)
    id(Plugins.shot)
    kotlin(Plugins.android)
    kotlin(Plugins.kapt)
}

android {
    compileSdk = Versions.androidCompileSdk

    namespace = "io.projectnewm"
    testNamespace = "io.projectnewm.test"
    defaultConfig {
        applicationId = "io.newm"
        minSdk = Versions.androidMinSdk
        targetSdk = Versions.androidTargetSdk
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "io.projectnewm.NewmAndroidJUnitRunner"
        testApplicationId = "io.projectnewm.test"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.composeCompiler
    }

    kapt {
        correctErrorTypes = true
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    kapt(Airbnb.showkaseProcessor)

    implementation(platform(Google.firebase))
    implementation(project(Modules.coreTheme))
    implementation(project(Modules.coreResources))
    implementation(project(Modules.coreUiUtils))
    implementation(project(Modules.nowPlaying))
    implementation(project(Modules.login))
    implementation(project(Modules.shared))

    implementation(Airbnb.showkase)
    implementation(Google.activityCompose)
    implementation(Google.androidxCore)
    implementation(Google.appCompat)
    implementation(Google.composeMaterial)
    implementation(Google.composeUi)
    implementation(Google.composeUiToolingPreview)
    implementation(Google.constraintLayout)
    implementation(Google.firebaseAnalytics)
    implementation(Google.firebaseCrashlytics)
    implementation(Google.lifecycle)
    implementation(Google.material)
    implementation(Google.navigationCompose)
    implementation(Google.navigationUiKtx)
    implementation(Google.splashScreen)
    implementation(Koin.android)
    implementation(Koin.androidCompose)
    implementation(Kotlin.reflect)

    debugImplementation(Facebook.flipper)
    debugImplementation(Facebook.soloader)
    releaseImplementation(Facebook.flipperNoop)

    debugImplementation(Google.composeUiTooling)
    debugImplementation(Google.composeUiTestManifest)

    testImplementation(JUnit.jUnit)

    androidTestImplementation(Google.espressoTest)
    androidTestImplementation(Google.Test.composeUiTestJUnit)
    androidTestImplementation(JUnit.androidxComposeJUnit)
    androidTestImplementation(JUnit.androidxJUnit)
}