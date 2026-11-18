import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

val keystorePropertiesFile: File = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.sweak.qralarm"
    compileSdk = 37
    ndkVersion = "28.2.13676358"

    signingConfigs {
        if (keystorePropertiesFile.exists()) create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }

    defaultConfig {
        applicationId = "com.sweak.qralarm"
        minSdk = 24
        targetSdk = 37
        versionCode = 91
        versionName = "2.11.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        // OpenCV ships large native libraries; only ARM phones are a real-world target.
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    buildTypes {
        debug {
            // Emulators for local testing.
            ndk {
                abiFilters += listOf("x86_64")
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    //noinspection WrongGradleMethod
    kotlin {
        compilerOptions {
            languageVersion = KotlinVersion.KOTLIN_2_3
        }
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    androidResources {
        noCompress += "tflite"
    }
    sourceSets.getByName("androidTest").assets.directories.add("$projectDir/schemas")
    testOptions {
        unitTests.all {
            it.maxHeapSize = "2g"
            // Ground-truth recognition data, see eval/README.md; the evaluation is skipped without it.
            System.getenv("QRALARM_EVAL_DIR")?.let { dir -> it.environment("QRALARM_EVAL_DIR", dir) }
        }
    }
    dependenciesInfo {
        // Do not include binary representation of app dependencies:
        includeInApk = false
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.ui.viewbinding)
    implementation(libs.androidx.media3.exoplayer)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.compose)
    implementation(libs.zxing.core)
    implementation(libs.mediapipe.tasks.vision)
    implementation(libs.opencv.android)

    testImplementation(libs.junit)
    testImplementation(libs.opencv.desktop)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.google.accompanist.permissions)
    implementation(libs.io.coil.compose)
    implementation(libs.io.coil.gif)
    implementation(libs.penfeizhou.frameanimation)
    implementation(libs.penfeizhou.awebp)
    implementation(libs.androidx.vectordrawable.animated)

    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
