import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import app.cash.sqldelight.gradle.SqlDelightExtension
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.sqldelight.android.driver)
            // Lottie 动画
            implementation(libs.lottie.compose)
            // ExoPlayer 视频播放
            implementation(libs.exoplayer.exoplayer)
            implementation(libs.exoplayer.ui)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.preview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // KMM 共享模块依赖
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.sqldelight.coroutines.extensions)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// SQLDelight 配置
configure<SqlDelightExtension> {
    databases {
        create("TigerFireDatabase") {
            packageName.set("com.cryallen.tigerfire.database")
        }
    }
}

android {
    namespace = "com.cryallen.tigerfire"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    // 启用 BuildConfig 功能
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.cryallen.tigerfire"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 只打包需要的语言资源 (目前只需要中文)
        resourceConfigurations += setOf("zh", "zh-rCN")

        // 启用矢量图支持
        vectorDrawables.useSupportLibrary = true

        // BuildConfig 字段：是否使用优化后的 UI
        buildConfigField("boolean", "IS_USE_OPTIMIZED_UI", "false")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // 签名配置
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val useKeystore = keystorePropertiesFile.exists()

    signingConfigs {
        if (useKeystore) {
            val keystoreProperties = Properties()
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))

            create("release") {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
        }
        getByName("release") {
            // 启用代码混淆和压缩
            isMinifyEnabled = true
            // 启用资源压缩
            isShrinkResources = true
            // ProGuard 规则文件
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 配置签名
            if (useKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Lint 配置
    lint {
        abortOnError = false
        checkReleaseBuilds = false
        disable += listOf(
            "ObsoleteLintCustomCheck",
            "MissingTranslation",
            "InvalidPackage"
        )
    }

    // APK 分割配置 (可选，用于生成针对不同架构的APK)
    splits {
        abi {
            isEnable = false  // 设为 true 可生成针对不同架构的APK
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = false
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)

    // 内存泄露检测 - LeakCanary (仅在 debug 构建中启用)
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.13")

    // UI自动化测试依赖
    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.uiautomator)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
