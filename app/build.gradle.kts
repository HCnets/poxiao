plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

fun projectSecret(name: String, default: String): String {
    return (findProperty(name) as String?)?.takeIf { it.isNotBlank() }
        ?: System.getenv(name)?.takeIf { it.isNotBlank() }
        ?: default
}

fun String.asBuildConfigValue(): String {
    return "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""
}

android {
    namespace = "com.poxiao.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.poxiao.app"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "SPARK_ASSISTANT_APP_ID", projectSecret("SPARK_ASSISTANT_APP_ID", "").asBuildConfigValue())
        buildConfigField(
            "String",
            "ASSISTANT_PROVIDER",
            projectSecret("ASSISTANT_PROVIDER", "SPARK").asBuildConfigValue(),
        )
        buildConfigField(
            "String",
            "SPARK_ASSISTANT_URL",
            projectSecret(
                "SPARK_ASSISTANT_URL",
                "wss://spark-openapi.cn-huabei-1.xf-yun.com/v1/assistants/asgz28mpmxpo_v1",
            ).asBuildConfigValue(),
        )
        buildConfigField(
            "String",
            "SPARK_ASSISTANT_API_KEY",
            projectSecret("SPARK_ASSISTANT_API_KEY", "").asBuildConfigValue(),
        )
        buildConfigField(
            "String",
            "SPARK_ASSISTANT_API_SECRET",
            projectSecret("SPARK_ASSISTANT_API_SECRET", "").asBuildConfigValue(),
        )
        buildConfigField(
            "String",
            "SPARK_ASSISTANT_DOMAIN",
            projectSecret("SPARK_ASSISTANT_DOMAIN", "generalv3").asBuildConfigValue(),
        )
        buildConfigField(
            "String",
            "DEEPSEEK_BASE_URL",
            projectSecret("DEEPSEEK_BASE_URL", "").asBuildConfigValue(),
        )
        buildConfigField(
            "String",
            "DEEPSEEK_API_KEY",
            projectSecret("DEEPSEEK_API_KEY", "").asBuildConfigValue(),
        )
        buildConfigField(
            "String",
            "DEEPSEEK_MODEL",
            projectSecret("DEEPSEEK_MODEL", "").asBuildConfigValue(),
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.02.01")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-compose:1.12.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.github.kyant0:backdrop:1.0.6")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    debugImplementation(composeBom)
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
