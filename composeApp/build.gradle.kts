import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val isProductionBuild: Boolean = providers.gradleProperty("production").isPresent

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.androidApplication)
}

pluginManager.apply("com.android.application")
pluginManager.apply("com.google.gms.google-services")

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
    
    jvm()
    
    js {
        browser()
        binaries.executable()
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.android)
            implementation(libs.koin.core)
            implementation(libs.kmpauth.google)
            implementation(libs.kmpauth.firebase)
            implementation(libs.kmpauth.uihelper)
            implementation(libs.slf4jAndroid)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(projects.shared)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation.common)
            implementation(libs.ktor.serialization.kotlinx.json.client)
            implementation(libs.kotlinx.datetime)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.multiplatform.markdown.renderer)
            implementation(libs.multiplatform.markdown.renderer.m3)
            implementation(libs.kmpauth.google)
            implementation(libs.kmpauth.firebase)
            implementation(libs.kmpauth.uihelper)
            implementation(libs.multiplatformSettings)
            implementation(libs.multiplatformSettingsNoArg)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.cio)
            implementation(libs.koin.core)
            implementation(libs.logback)
        }
        jvmTest.dependencies {
            implementation(libs.kotlinx.datetime)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.koin.core)
        }
        jsMain.dependencies {
            implementation(libs.ktor.client.js)
            implementation(libs.koin.core)
            implementation(npm("copy-webpack-plugin", "12.0.2"))
        }
        jsTest.dependencies {
            implementation(libs.kotlinx.datetime)
        }
    }
}

android {
    namespace = "com.marcinmoskala.albert"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.marcinmoskala.albert"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            val serverUrl: String = System.getenv("SERVER_URL") ?: ""
            buildConfigField("String", "SERVER_URL", "\"$serverUrl\"")
        }
        getByName("debug") {
            buildConfigField("String", "SERVER_URL", "\"http://10.0.2.2:8080\"")
        }
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
}

tasks.register<JavaExec>("jvmRun") {
    group = "application"
    mainClass.set("com.marcinmoskala.albert.MainKt")
    val jvmTarget = kotlin.targets.getByName("jvm")
    val compilation = jvmTarget.compilations.getByName("main")
    classpath = files(compilation.output.allOutputs, compilation.runtimeDependencyFiles)
    dependsOn(compilation.compileTaskProvider)
}

compose.desktop {
    application {
        mainClass = "com.marcinmoskala.albert.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.marcinmoskala.albert"
            packageVersion = "1.0.0"
        }
    }
}
