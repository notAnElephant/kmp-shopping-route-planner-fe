import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
//    alias(libs.plugins.kotlinJvm)
   alias(libs.plugins.kotlinSerialization)

    id("org.openapi.generator") version "7.9.0"
    //newly added
//    application
//    kotlin("jvm") version "1.4.21"
//    kotlin("plugin.serialization") version "1.4.21"
}

openApiGenerate{
    inputSpec.set("C:/Users/roiol/Downloads/com_example_ktor_db_app-openapi_1.yaml")
    generatorName.set("kotlin")
    library.set("multiplatform")
    configOptions.put("dateLibrary", "kotlinx-datetime")
}

openApiValidate{
    inputSpec.set("C:/Users/roiol/Downloads/com_example_ktor_db_app-openapi_1.yaml")
}
kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosX64(),
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

        }
        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)

                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.serialization)
                implementation(libs.kotlinx.serialization.json) // Ensure this is included
                //TODO    implementation(kotlin("stdlib-common"))
                //                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationVersion")
                //should it be added?
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kotlintest.runner.junit5)

                implementation(compose.materialIconsExtended)
                implementation(libs.core)
            }

//            kotlin.srcDir("${layout.buildDirectory.get()}/generate-resources/main/src")
            kotlin.srcDir("C:\\Users\\roiol\\source\\repos\\Kotlin\\kmp-shopping-route-planner-fe\\composeApp\\build\\generate-resources\\main\\src")
        }
    }
}

android {
    namespace = "org.example.srp_fe"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.example.srp_fe"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
	        excludes += "META-INF/LICENSE.md"
	        excludes += "META-INF/LICENSE-notice.md"
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
//    implementation(project(":composeApp"))
    debugImplementation(compose.uiTooling)
}

