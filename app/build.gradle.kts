import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Properties
import java.io.FileInputStream

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin)
	alias(libs.plugins.ksp)
	id("org.jetbrains.kotlin.plugin.parcelize")
	alias(libs.plugins.hilt)
	alias(libs.plugins.room)
	alias(libs.plugins.kotlinx.serizliation)
	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.google.services)
}

android {
	compileSdk = 37
	buildToolsVersion = "35.0.0"
	namespace = "com.aman.vanish"

	packaging {
		jniLibs {
			useLegacyPackaging = true
		}
		resources {
			excludes += listOf(
				"META-INF/README.md",
				"META-INF/NOTICE.md"
			)
		}
	}

	dependenciesInfo {
		// Disables dependency metadata when building APKs.
		includeInApk = false
		// Disables dependency metadata when building Android App Bundles.
		includeInBundle = false
	}

	defaultConfig {
		applicationId = "com.aman.vanish"
		minSdk = 23
		targetSdk = 37
		versionCode = 2
		versionName = "1.0.1"
		testInstrumentationRunner = "com.aman.vanish.HiltTestRunner"
		ksp {
			arg("room.generateKotlin", "true")
		}
		androidResources {
			// https://issuetracker.google.com/issues/408030127
			generateLocaleConfig = false
		}
		
		val localProperties = Properties()
		val localPropertiesFile = rootProject.file("local.properties")
		if (localPropertiesFile.exists()) {
			localProperties.load(FileInputStream(localPropertiesFile))
		}
		resValue("string", "tg_backup_bot_token", localProperties.getProperty("tg_backup_bot_token", ""))
	}

	signingConfigs {
		register("release") {
			storeFile = file("../totoro-release.keystore")
			storePassword = "aman123"
			keyAlias = "totoro"
			keyPassword = "aman123"
		}
	}

	buildTypes {
		getByName("debug") {
			applicationIdSuffix = ".debug"
		}
		getByName("release") {
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
			signingConfig = signingConfigs.getByName("release")
		}
		create("nightly") {
			initWith(getByName("release"))
			applicationIdSuffix = ".nightly"
		}
	}

	buildFeatures {
		viewBinding = true
		buildConfig = true
		compose = true
	}

	sourceSets {
		getByName("androidTest") {
			assets.srcDirs(files("$projectDir/schemas"))
		}
		getByName("main") {
			java.srcDirs("src/main/kotlin/")
		}
	}

	compileOptions {
		isCoreLibraryDesugaringEnabled = true
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	room {
		schemaDirectory("$projectDir/schemas")
	}

	lint {
		abortOnError = true
		disable += listOf("MissingTranslation", "PrivateResource", "SetJavaScriptEnabled", "SimpleDateFormat")
	}

	testOptions {
		unitTests.isIncludeAndroidResources = true
		unitTests.isReturnDefaultValues = false
	}
}

// Global Kotlin compilation configuration using the compilerOptions DSL (replacing deprecated kotlinOptions)
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
	compilerOptions {
		jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
		freeCompilerArgs.addAll(
			"-opt-in=kotlin.ExperimentalStdlibApi",
			"-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
			"-opt-in=kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi",
			"-opt-in=kotlinx.coroutines.InternalForInheritanceCoroutinesApi",
			"-opt-in=kotlinx.coroutines.FlowPreview",
			"-opt-in=kotlin.contracts.ExperimentalContracts",
			"-opt-in=coil3.annotation.ExperimentalCoilApi",
			"-opt-in=coil3.annotation.InternalCoilApi",
			"-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
			"-opt-in=org.koitharu.kotatsu.parsers.InternalParsersApi",
			"-Xjspecify-annotations=strict",
			"-Xannotation-default-target=first-only",
			"-Xtype-enhancement-improvements-strict-mode"
		)
	}
}

// Modern AGP 8.x API to dynamically override version code and name for nightly variant
androidComponents {
	onVariants(selector().withBuildType("nightly")) { variant ->
		variant.outputs.forEach { output ->
			val now = LocalDateTime.now()
			output.versionCode.set(now.format(DateTimeFormatter.ofPattern("yyMMdd")).toInt())
			output.versionName.set("N" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
		}
	}
}

dependencies {
	var parsersVersion = libs.versions.parsers.get()
	if (System.getProperties().containsKey("parsersVersionOverride")) {
		parsersVersion = System.getProperty("parsersVersionOverride")
	}
	implementation("com.github.clquwu:kotatsu-parsers-redo:$parsersVersion") {
		exclude(group = "org.json", module = "json")
	}

	coreLibraryDesugaring(libs.desugar.jdk.libs)
	implementation(libs.kotlin.stdlib)
	implementation(libs.kotlinx.coroutines.android)
	implementation(libs.kotlinx.coroutines.guava)

	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.runtime)
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.foundation)
	implementation(libs.androidx.compose.material3)
	implementation(libs.androidx.compose.activity)

	implementation(libs.androidx.appcompat)
	implementation(libs.androidx.core)
	implementation(libs.androidx.activity)
	implementation(libs.androidx.fragment)
	implementation(libs.androidx.transition)
	implementation(libs.androidx.collection)
	implementation(libs.lifecycle.viewmodel)
	implementation(libs.lifecycle.service)
	implementation(libs.lifecycle.process)
	implementation(libs.androidx.constraintlayout)
	implementation(libs.androidx.documentfile)
	implementation(libs.androidx.swiperefreshlayout)
	implementation(libs.androidx.recyclerview)
	implementation(libs.androidx.viewpager2)
	implementation(libs.androidx.preference)
	implementation(libs.androidx.biometric)
	implementation(libs.material)
	implementation(libs.androidx.lifecycle.common.java8)
	implementation(libs.androidx.webkit)

	implementation(libs.androidx.work.runtime)
	implementation(libs.guava)

	// Foldable/Window layout
	implementation(libs.androidx.window)

	implementation(libs.androidx.room.runtime)
	implementation(libs.androidx.room.ktx)
	ksp(libs.androidx.room.compiler)

	implementation(libs.okhttp)
	implementation(libs.okhttp.tls)
	implementation(libs.okhttp.dnsoverhttps)
	implementation(libs.okio)
	implementation(libs.retrofit)
	implementation(libs.retrofit.gson)
	implementation(libs.okhttp.logging)
	implementation(libs.kotlinx.coroutines.core)
	implementation(libs.kotlinx.serialization.json)

	implementation(libs.adapterdelegates)
	implementation(libs.adapterdelegates.viewbinding)

	implementation(libs.hilt.android)
	ksp(libs.hilt.compiler)
	implementation(libs.androidx.hilt.work)
	ksp(libs.androidx.hilt.compiler)

	implementation(libs.coil.core)
	implementation(libs.coil.network)
	implementation(libs.coil.gif)
	implementation(libs.coil.svg)
	implementation(libs.avif.decoder)
	implementation(libs.ssiv)
	implementation(libs.disk.lru.cache)
	implementation(libs.markwon)
	implementation(libs.kizzyrpc)

	implementation(libs.acra.http)
	implementation(libs.acra.dialog)

	implementation(libs.conscrypt.android)

	debugImplementation(libs.leakcanary.android)
	"nightlyImplementation"(libs.leakcanary.android)
	debugImplementation(libs.workinspector)

	testImplementation(libs.junit)
	testImplementation(libs.json)
	testImplementation(libs.kotlinx.coroutines.test)

	androidTestImplementation(libs.androidx.runner)
	androidTestImplementation(libs.androidx.rules)
	androidTestImplementation(libs.androidx.test.core)
	androidTestImplementation(libs.androidx.junit)

	androidTestImplementation(libs.kotlinx.coroutines.test)

	androidTestImplementation(libs.androidx.room.testing)
	androidTestImplementation(libs.moshi.kotlin)

	androidTestImplementation(libs.hilt.android.testing)
	kspAndroidTest(libs.hilt.android.compiler)

	// Firebase BOM & Firebase Analytics (Google Analytics)
	implementation(platform(libs.firebase.bom))
	implementation(libs.firebase.analytics)
	implementation(libs.firebase.messaging)
}
