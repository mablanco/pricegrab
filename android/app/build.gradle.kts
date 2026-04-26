plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.detekt)
    jacoco
}

android {
    namespace = "com.mablanco.pricegrab"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.mablanco.pricegrab"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 4
        versionName = "0.1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resourceConfigurations += listOf("en", "es")
    }

    signingConfigs {
        create("release") {
            // All four values come from environment variables populated by CI
            // (see .github/workflows/android-ci.yml). Locally, developers can
            // export the same variables before running :app:assembleRelease.
            val keystorePath = System.getenv("PRICEGRAB_KEYSTORE")
            val keystorePassword = System.getenv("PRICEGRAB_KEYSTORE_PASSWORD")
            val releaseKeyAlias = System.getenv("PRICEGRAB_KEY_ALIAS")
            val releaseKeyPassword = System.getenv("PRICEGRAB_KEY_PASSWORD")

            if (!keystorePath.isNullOrBlank()) {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Required for F-Droid Reproducible Builds (Mode B in docs/fdroid.md).
            // AGP >= 8.3 embeds the local git revision and project path in
            // META-INF/version-control-info.textproto by default; that file would
            // differ between our CI runner and F-Droid's build server even when
            // the source tree is identical, breaking byte-for-byte verification.
            // We disable the embed so the upstream-signed APK shipped on GitHub
            // Releases is reproducible by F-Droid against our tagged source.
            vcsInfo.include = false
            // F-Droid's build server strips the entire `signingConfigs { create("release") { … } }`
            // block before invoking gradle, because F-Droid signs releases with its own key. We
            // therefore must use `findByName` (returns null if absent) instead of `getByName`
            // (throws). The null-check on storeFile additionally protects local debug builds
            // where the four PRICEGRAB_* env vars are not exported.
            val releaseSigningConfig = signingConfigs.findByName("release")
            if (releaseSigningConfig?.storeFile != null) {
                signingConfig = releaseSigningConfig
            }
        }
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
        }
        getByName("test") {
            java.srcDirs("src/test/kotlin")
        }
        getByName("androidTest") {
            java.srcDirs("src/androidTest/kotlin")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = false
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
        )
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    dependenciesInfo {
        // F-Droid does not allow the encrypted dependency metadata blob.
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.core)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(rootProject.file("detekt.yml"))
    buildUponDefaultConfig = true
    autoCorrect = false
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

// Aggregated JaCoCo report over the unit-test task.
val jacocoReportFileFilter = listOf(
    "**/R.class",
    "**/R\$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "android/**/*.*",
    "**/*\$Lambda\$*.*",
    "**/*\$inlined\$*.*",
    "**/ui/theme/**",
)

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    classDirectories.setFrom(
        fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
            exclude(jacocoReportFileFilter)
        },
    )
    sourceDirectories.setFrom(files("src/main/kotlin"))
    executionData.setFrom(
        fileTree(layout.buildDirectory.get()) {
            include("jacoco/testDebugUnitTest.exec")
        },
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// Enforce Constitution V: the core calculation package keeps >= 90 % line
// coverage under unit tests. Scoped narrowly so UI code (tested via
// instrumented tests) does not dilute the gate.
tasks.register<JacocoCoverageVerification>("jacocoCoreCoverageVerification") {
    dependsOn("jacocoTestReport")

    classDirectories.setFrom(
        fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
            include("com/mablanco/pricegrab/core/calc/**")
            exclude(jacocoReportFileFilter)
        },
    )
    sourceDirectories.setFrom(files("src/main/kotlin"))
    executionData.setFrom(
        fileTree(layout.buildDirectory.get()) {
            include("jacoco/testDebugUnitTest.exec")
        },
    )

    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.90".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.85".toBigDecimal()
            }
        }
    }
}

tasks.named("check").configure {
    dependsOn("jacocoCoreCoverageVerification")
}
