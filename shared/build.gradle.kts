
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
	kotlin("multiplatform")
	kotlin("plugin.serialization")
	id("kotlinx-serialization")
	id("com.android.library")
	id("com.squareup.sqldelight")
	id("com.google.devtools.ksp") version "2.0.0-1.0.22"
	id("com.github.gmazzo.buildconfig") version "5.3.5"
}

android {
	compileSdk = libs.versions.android.compileSdk.get().toInt()

	sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
	namespace = "io.newm.shared"

	defaultConfig {
		minSdk = libs.versions.android.minSdk.get().toInt()
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
}

kotlin {
	androidTarget()

	val xcf = XCFramework()
	listOf(
		iosArm64(),
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
				implementation(libs.kotlinx.coroutines.core)
				implementation(libs.kotlin.stdlib)
				implementation(libs.runtime)
				implementation(libs.coroutines.extensions)
				api(libs.koin.core)
				implementation(libs.ktor.client.logging)
				implementation(libs.ktor.client.core)
				implementation(libs.ktor.client.cio)
				implementation(libs.ktor.client.content.negotiation)
				implementation(libs.ktor.serialization.kotlinx.json)
				implementation(libs.ktor.client.auth)
				implementation(libs.androidx.datastore.preferences)
				implementation(libs.store5)
				implementation(libs.kvault)
			}
		}
		val commonTest by getting {
			dependencies {
				implementation(libs.koin.test)
				implementation(libs.kotlinx.coroutines.test)
				implementation(kotlin("test-common"))
				implementation(kotlin("test-annotations-common"))
			}
		}
		val androidMain by getting {
			dependencies {
				implementation(libs.android.driver)
				implementation(libs.ktor.client.android)
			}
		}

		named("androidUnitTest") {
			dependencies {
				implementation(kotlin("test-junit"))
				implementation(libs.junit)
			}
		}

		val iosArm64Main by getting
		val iosSimulatorArm64Main by getting
		val iosMain by creating {
			dependsOn(commonMain)
			iosArm64Main.dependsOn(this)
			iosSimulatorArm64Main.dependsOn(this)
			dependencies {
				implementation(libs.ktor.client.darwin)
				implementation(libs.native.driver)
			}
		}
		val iosSimulatorArm64Test by getting
		val iosTest by creating {
			dependsOn(commonTest)
			iosSimulatorArm64Test.dependsOn(this)
		}

		all {
			languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
		}
	}
}
dependencies {
	implementation(project(":shared"))
}

buildConfig {
	packageName("io.newm.shared.generated")

	val properties = Properties().apply { load(FileInputStream(File(rootProject.rootDir, "local.properties"))) }

	buildConfigField<String>( "STAGING_URL", properties.getProperty("STAGING_URL").replace("\"", ""))
	buildConfigField<String>("PRODUCTION_URL", properties.getProperty("PRODUCTION_URL").replace("\"", ""))
	buildConfigField<String>("GOOGLE_AUTH_CLIENT_ID", properties.getProperty("GOOGLE_AUTH_CLIENT_ID").replace("\"", ""))
	buildConfigField<String>("RECAPTCHA_SITE_KEY", properties.getProperty("RECAPTCHA_SITE_KEY").replace("\"", ""))
	buildConfigField<String>("SENTRY_AUTH_TOKEN", properties.getProperty("SENTRY_AUTH_TOKEN").replace("\"", ""))
	buildConfigField<String>("ANDROID_SENTRY_DSN", properties.getProperty("ANDROID_SENTRY_DSN").replace("\"", ""))
	buildConfigField<String>("LAUNCHDARKLY_MOBILE_KEY", properties.getProperty("LAUNCHDARKLY_MOBILE_KEY").replace("\"", ""))
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	kotlinOptions {
		jvmTarget = "11"
	}
}

sqldelight {
	database("NewmDatabase") {
		packageName = "io.newm.shared.db.cache"
		sourceFolders = listOf("sqldelight")
		version = 4
	}
}

kotlin.sourceSets.all {
	languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
}
