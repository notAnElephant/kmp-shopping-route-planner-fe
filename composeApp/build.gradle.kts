import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

abstract class PatchOpenApiGeneratedSourcesTask : DefaultTask() {
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val authFile: RegularFileProperty

    @TaskAction
    fun patchGeneratedSources() {
        val file = authFile.asFile.get()
        if (!file.exists()) {
            return
        }

        val patched =
            file
                .readText()
                .replace("import io.ktor.util.InternalAPI\n", "")
                .replace("    @OptIn(InternalAPI::class)\n", "")

        if (patched != file.readText()) {
            file.writeText(patched)
        }
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinSerialization)

    id("org.openapi.generator") version "7.22.0"
}

openApiGenerate {
    inputSpec.set(
        rootProject.layout.projectDirectory
            .file("documentation.yaml")
            .asFile
            .toURI()
            .toString(),
    )
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
    inputSpec.set(
        rootProject.layout.projectDirectory
            .file("documentation.yaml")
            .asFile
            .toURI()
            .toString(),
    )
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

val cleanOpenApiGeneratedSources by tasks.registering(Delete::class) {
    group = "openapi tools"
    description = "Deletes all OpenAPI generated sources before regeneration."

    delete(layout.buildDirectory.dir("generate-resources/main"))
}

val patchOpenApiGeneratedSources by tasks.registering(PatchOpenApiGeneratedSourcesTask::class) {
    group = "openapi tools"
    description = "Patches generated OpenAPI sources for current Ktor compatibility."
    authFile.set(
        layout.buildDirectory.file(
            "generate-resources/main/src/commonMain/kotlin/org/openapitools/client/auth/HttpBasicAuth.kt",
        ),
    )
}

tasks.named("openApiGenerate") {
    dependsOn(cleanOpenApiGeneratedSources)

    // Workaround: the kotlin multiplatform generator may still emit test sources.
    finalizedBy(cleanupOpenApiGeneratedTests)
    finalizedBy(patchOpenApiGeneratedSources)
}

kotlin {
    android {
        namespace = "org.example.srpfe"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        androidResources {
            enable = true
        }
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
            implementation(project.dependencies.platform(libs.firebase.android.bom))
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.android)
            implementation(libs.ktor.client.content.negotiation.v111)
            implementation(libs.ktor.serialization.kotlinx.json.v111)
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

                implementation(libs.compose.icons.feather)
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
