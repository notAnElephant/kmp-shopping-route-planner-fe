import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    alias(libs.plugins.ktlint)
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
}

val libsCatalog = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<KtlintExtension> {
        version.set("1.8.0")
    }

    dependencies {
        add(
            "ktlintRuleset",
            "io.nlopez.compose.rules:ktlint:${libsCatalog.findVersion("composeRulesKtlint").get().requiredVersion}",
        )
    }
}
