import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.application)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.mokoResources)
    alias(libs.plugins.ksp)
}

kotlin {
    kotlin.applyDefaultHierarchyTemplate()

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    val iosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget =
        when {
            System.getenv("SDK_NAME")?.startsWith("iphoneos") == true -> ::iosArm64
            System.getenv("NATIVE_ARCH")?.startsWith("arm") == true -> ::iosSimulatorArm64
            else -> ::iosX64
        }
    iosTarget("ios") {}

    sourceSets {
        all {
            languageSettings {
                optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
            }
        }
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")

            dependencies{
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class) implementation(compose.components.resources)

                implementation(libs.ktor.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                implementation(libs.koin.compose)

                implementation(libs.koin.annotations)

                implementation(libs.kamel)

                implementation(libs.voyager.navigator)
                implementation(libs.voyager.koin)

                implementation(libs.composeImageLoader)
                implementation(libs.composeIcons.featherIcons)

                implementation(libs.napier)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)

                implementation(libs.multiplatformSettings)

                implementation(libs.kstore)

                implementation(libs.moko.mvvm)
                implementation(libs.moko.resources.compose)
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain {
            dependencies{
                implementation(project.dependencies.platform(libs.compose.bom))
                implementation(libs.compose.ui)
                implementation(libs.compose.ui.tooling.preview)
                implementation(libs.compose.uitooling)
                implementation(libs.androidx.activityCompose)
                implementation(libs.androidx.appcompat)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.sqlDelight.driver.android)

                //Koin
                implementation(libs.koin.android)
                implementation(libs.koin.androidx.compose)
            }

            // Required for moko-resources to work
            dependsOn(commonMain.get())
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqlDelight.driver.native)
        }

    }
}

dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
}

// Ensure all Kotlin compile tasks depend on kspCommonMainKotlinMetadata for prior code processing.
tasks.withType<KotlinCompile<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

// Make all SourcesJar tasks depend on kspCommonMainKotlinMetadata.
afterEvaluate {
    tasks.filter { task: Task ->
        task.name.contains("SourcesJar", true)
    }.forEach {
        it.dependsOn("kspCommonMainKotlinMetadata")
    }
}

android {
    namespace = "org.company.template"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34

        applicationId = "org.company.template.androidApp"
        versionCode = 1
        versionName = "1.0.0"
    }
    sourceSets["main"].apply {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
        res.srcDirs("src/androidMain/resources")
        resources.srcDirs("src/commonMain/resources")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    dependencies {
//        add("kspAndroid", libs.koin.ksp.compiler)
        debugImplementation(libs.compose.uitooling)
    }
}

buildConfig {
    // BuildConfig configuration here.
    // https://github.com/gmazzo/gradle-buildconfig-plugin#usage-in-kts
}

sqldelight {
    databases {
        create("MyDatabase") {
            // Database configuration here.
            // https://cashapp.github.io/sqldelight
            packageName.set("org.company.template.db")
        }
    }
}
