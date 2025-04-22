import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.awt.SystemColor.desktop

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.google.services)

    id("org.openapi.generator") version "7.9.0"
}

openApiGenerate{
    inputSpec.set("C:/Users/roiol/Downloads/documentation.yaml")
    generatorName.set("kotlin")
    library.set("multiplatform")
    configOptions.put("dateLibrary", "kotlinx-datetime")
}

openApiValidate{
    inputSpec.set("C:/Users/roiol/Downloads/documentation.yaml")
}
kotlin {


    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop") { // This adds the JVM target for desktop
        compilations.all {
            kotlinOptions.jvmTarget = "11" // Adjust JVM target version if necessary
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

            implementation(project.dependencies.platform(libs.firebase.android.bom))
            implementation(libs.firebase.android.auth.ktx)
            implementation(libs.firebase.android.firestore.ktx)
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
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kotlintest.runner.junit5)

                implementation(compose.materialIconsExtended)
                implementation(libs.core)

                implementation(libs.logging)
                implementation(libs.navigation)

                implementation("io.github.kashif-mehmood-km:camerak:+")
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

//compose.desktop {
//    application {
//        mainClass = "org.example.srp_fe.MainKt"
//        nativeDistributions {
//            targetFormats(TargetFormat.Exe)
//            packageName = "ComposeApp"
//            packageVersion = "1.0.0"
//        }
//    }
//}

