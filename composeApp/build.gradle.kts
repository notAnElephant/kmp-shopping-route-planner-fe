import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinSerialization)

    id("org.openapi.generator") version "7.20.0"
}

openApiGenerate {
    inputSpec.set("file:///${projectDir.absolutePath.replace('\\', '/')}/documentation.yaml")
    generatorName.set("kotlin")
    library.set("multiplatform")
    configOptions.put("dateLibrary", "kotlinx-datetime")

    // Disable generated test/doc stubs
    globalProperties.put("apiTests", "false")
    globalProperties.put("modelTests", "false")
    globalProperties.put("apiDocs", "false")
    globalProperties.put("modelDocs", "false")
}

openApiValidate {
    inputSpec.set("file:///${projectDir.absolutePath.replace('\\', '/')}/documentation.yaml")
}

val cleanupOpenApiGeneratedTests by tasks.registering(Delete::class) {
    group = "openapi tools"
    description = "Deletes unwanted OpenAPI generated test sources (workaround)."

    delete(
        layout.buildDirectory.dir("generate-resources/main/src/test"),
        layout.buildDirectory.dir("generate-resources/main/src/commonTest"),
        layout.buildDirectory.dir("generate-resources/main/src/jvmTest"),
        layout.buildDirectory.dir("generate-resources/main/src/androidTest"),
        layout.buildDirectory.dir("generate-resources/main/src/androidUnitTest"),
        layout.buildDirectory.dir("generate-resources/main/src/iosTest"),
    )
}

val patchOpenApiGeneratedSources by tasks.registering {
    group = "openapi tools"
    description = "Patches generated OpenAPI sources for current Ktor compatibility."

    doLast {
        val authFile =
            layout.buildDirectory
                .file("generate-resources/main/src/commonMain/kotlin/org/openapitools/client/auth/HttpBasicAuth.kt")
                .get()
                .asFile

        if (authFile.exists()) {
            val patched =
                authFile
                    .readText()
                    .replace("import io.ktor.util.InternalAPI\n", "")
                    .replace("    @OptIn(InternalAPI::class)\n", "")
            authFile.writeText(patched)
        }
    }
}

tasks.named("openApiGenerate") {
    // Workaround: the kotlin multiplatform generator may still emit test sources.
    finalizedBy(cleanupOpenApiGeneratedTests)
    finalizedBy(patchOpenApiGeneratedSources)
}

kotlin {

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
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

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }

        jvmMain {
            kotlin.srcDir("src/desktopMain/kotlin")
        }

        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
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

                implementation(compose.materialIconsExtended)
                implementation(libs.core)

                implementation(libs.logging)
                implementation(libs.navigation)

                implementation(libs.camerak)

                implementation(libs.filekit.core)
                implementation(libs.filekit.dialogs)
                implementation(libs.filekit.dialogs.compose)
                implementation(libs.kmpauth.google)
                implementation(libs.kmpauth.firebase)
                implementation(libs.kmpauth.uihelper)
            }

            kotlin.srcDir(buildDir.resolve("generate-resources/main/src"))
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "org.example.srpfe"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "org.example.srpfe"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/kotlinx-io.kotlin_module"
            excludes += "'META-INF/atomicfu.kotlin_module'"
            excludes += "META-INF/kotlinx-coroutines-io.kotlin_module"
            excludes += "META-INF/kotlinx-coroutines-core.kotlin_module"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
        getByName("debug") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation.v111)
    implementation(libs.ktor.serialization.kotlinx.json.v111)
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "org.example.srpfe.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ShoppingRoutePlannerFE"
            packageVersion = "1.0.0"
        }
    }
}
