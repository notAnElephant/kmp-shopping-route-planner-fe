plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "org.example.srpfe"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.example.srpfe"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
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
}

dependencies {
    implementation(project(":composeApp"))
    implementation(platform(libs.firebase.android.bom))
    implementation("com.google.firebase:firebase-common")
    implementation(libs.androidx.activity.compose)
    debugImplementation(compose.uiTooling)
}
