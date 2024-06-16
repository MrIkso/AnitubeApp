import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.navigation.safeargs)
}

val keystorePropertiesFile: File = File(rootProject.rootDir, "keystore.properties")
//println("keystore file: ${keystorePropertiesFile.path}") // Optional debug output

android {
    namespace = "com.mrikso.anitube.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mrikso.anitube.app"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    if (keystorePropertiesFile.exists()) {
        val keystoreProperties = Properties()
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
        signingConfigs {
            create("release")
            {
                keyAlias = keystoreProperties["keyAlias"].toString()
                keyPassword = keystoreProperties["keyPassword"].toString()
                storeFile = file(keystoreProperties["storeFile"]!!)
                storePassword = keystoreProperties["storePassword"].toString()
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    applicationVariants.configureEach {
        // rename the output APK file
        outputs.configureEach {
            (this as? ApkVariantOutputImpl)?.outputFileName =
                "${rootProject.name}_${versionName}(${versionCode})_${buildType.name}.apk"
        }

        // rename the output AAB file
        tasks.named(
            "sign${flavorName.uppercaseFirstChar()}${buildType.name.uppercaseFirstChar()}Bundle",
            com.android.build.gradle.internal.tasks.FinalizeBundleTask::class.java
        ) {
            val file = finalBundleFile.asFile.get()
            val finalFile =
                File(
                    file.parentFile,
                    "${rootProject.name}_$versionName($versionCode)_${buildType.name}.aab"
                )
            finalBundleFile.set(finalFile)
        }
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    lint {
        abortOnError = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    /*androidResources {
        generateLocaleConfig = true
    }*/
    lint {
        disable += "ContentDescription"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs.useLegacyPackaging = true
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.swiperefreshlayout)
    // navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.core.splashscreen)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.preference.ktx)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.paging)
    implementation(libs.androidx.room.rxjava3)
    ksp(libs.androidx.room.compiler)

    implementation(libs.expandabletextview)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.scalars)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    // jsoup
    implementation(libs.jsoup)

    // Gson
    implementation(libs.gson)

    // RxJava
    implementation(libs.rxandroid)
    implementation(libs.rxjava)
    implementation(libs.rxjava3.retrofit.adapter)

    // Glide
    implementation(libs.glide)
    ksp(libs.glide.compiler)

    implementation(libs.androidx.paging.runtime.ktx)
    // RxJava3 support for paging library
    implementation(libs.androidx.paging.rxjava3)

    // For media playback using ExoPlayer
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.datasource)
    implementation(libs.androidx.media3.decoder)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.exoplayer.smoothstreaming)
    implementation(libs.androidx.media3.exoplayer.rtsp)
    // For building media playback UIs
    implementation(libs.androidx.media3.ui)

    // Dagger - Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Utility
    implementation(libs.utilcodex)
    implementation(libs.m3u8.parser)
    implementation(libs.nextlib)
    implementation(libs.tagsoup)

    // newpipe yt taken from https://github.com/TeamNewPipe/NewPipe/blob/dev/app/build.gradle#L204
    implementation(libs.newpipeextractor)

    // TouchImageView
    implementation(libs.touchimageview)

    implementation(libs.circleimageview)

    implementation(project(":treeview"))
    implementation(project(":bottomsheetMenu"))
    implementation(project(":doubletapplayerview"))
}

// Delete large build log files from ~/.gradle/daemon/X.X/daemon-XXX.out.log
// Source: https://discuss.gradle.org/t/gradle-daemon-produces-a-lot-of-logs/9905
File("${project.gradle.gradleUserHomeDir.absolutePath}/daemon/${project.gradle.gradleVersion}").listFiles()
    ?.forEach {
        if (it.name.endsWith(".out.log")) {
            // println("Deleting gradle log file: $it") // Optional debug output
            it.delete()
        }
    }