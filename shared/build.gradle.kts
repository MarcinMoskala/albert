import com.android.build.gradle.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import app.cash.sqldelight.gradle.VerifyMigrationTask

val isProductionBuild: Boolean = providers.gradleProperty("production").isPresent
val enableAndroidTargets: Boolean = !isProductionBuild

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
    // Apply Android plugin only when Android targets enabled
    alias(libs.plugins.androidLibrary) apply false
}

if (enableAndroidTargets) {
    pluginManager.apply("com.android.library")
}

kotlin {
    if (enableAndroidTargets) {
        androidTarget {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }
    }
    
    iosArm64()
    iosSimulatorArm64()
    
    jvm()
    
    js {
        browser()
    }
    
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation.common)
            implementation(libs.ktor.serialization.kotlinx.json.client)
            implementation(libs.koin.core)
            implementation(libs.sqldelight.runtime)
            implementation(libs.kotlinx.datetime)
            implementation(libs.sqldelight.async.extensions)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        if (enableAndroidTargets) {
            androidMain.dependencies {
                implementation(libs.ktor.client.android)
                implementation(libs.sqldelight.android.driver)
            }
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
            implementation(libs.sqldelight.sqlite.driver)
            implementation(libs.logback)
        }
        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        jvmTest.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
        jsTest.dependencies {
        }
        if (enableAndroidTargets) {
            androidUnitTest.dependencies {
                implementation(libs.sqldelight.sqlite.driver)
            }
        }
    }
}

if (enableAndroidTargets) {
    extensions.configure<LibraryExtension> {
        namespace = "com.marcinmoskala.albert"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        buildFeatures {
            buildConfig = true
        }
        buildTypes {
            getByName("debug") {
                buildConfigField("String", "SERVER_URL", "\"\"")
            }
            getByName("release") {
                val serverUrl: String = System.getenv("SERVER_URL") ?: ""
                buildConfigField("String", "SERVER_URL", "\"$serverUrl\"")
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
        defaultConfig {
            minSdk = libs.versions.android.minSdk.get().toInt()
        }
    }
}

sqldelight {
    databases {
        create("AlbertDatabase") {
            packageName.set("com.marcinmoskala.database")
            generateAsync.set(true)
            verifyMigrations.set(false)
        }
    }
}

tasks.withType<VerifyMigrationTask>().configureEach {
    enabled = false
}

tasks.configureEach {
    if (name == "jsBrowserTest") {
        enabled = false
    }
}
