plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.ctetin.expandabletextviewlibrary"
    compileSdk = 37
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
// Explicitly disable the connectedAndroidTest task for this module
androidComponents {
    beforeVariants { variant ->
        variant.enableAndroidTest = false
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
}