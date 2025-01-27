plugins {
    id ("krypt.android.library")
    id ("krypt.android.hilt")
}

android {
    namespace = "ir.mehdiyari.krypt.dispatchers"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(libs.coroutinesAndroid)
    implementation(libs.coroutinesCore)

    testImplementation(libs.junit)
    androidTestImplementation(libs.testExt)
}