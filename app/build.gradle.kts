import ir.mehdiyari.krypt.Versions

plugins {
    id("krypt.android.application")
    id("krypt.android.application.flavor")
    id("krypt.android.application.compose")
    alias(libs.plugins.kspPlugin)
    id("dagger.hilt.android.plugin")
    id("krypt.android.hilt")
}

android {

    defaultConfig {

        applicationId = "ir.mehdiyari.krypt"
        versionCode = Versions.VERSION_CODE
        versionName = Versions.VERSION_NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(libs.coreKtx)
    implementation(libs.appCompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.testExt)
    androidTestImplementation(libs.espressoCore)

    api(libs.composeActivity)

    implementation(libs.lifecycleViewModeKtx)
    implementation(libs.lifecycleViewModelCompose)
    ksp(libs.lifecycleCompiler)

    implementation(libs.room)
    ksp(libs.roomCompiler)

    implementation(libs.hiltNavigationCompose)

    implementation(libs.lifecycleViewModelCompose)
    implementation(libs.navigationCompose)

    implementation(libs.coroutinesAndroid)
    implementation(libs.coroutinesCore)

    implementation(libs.fallery)

    implementation(libs.glide)
    ksp(libs.glideCompiler)
    implementation(libs.landscapistGlide)

    implementation(libs.moshiKotlin)
    implementation(libs.processPhoenix)
    implementation(libs.exoplayer)
    implementation(libs.media3UI)

    testImplementation(libs.mockk)
    testImplementation(libs.coroutinesTest)
    testImplementation(libs.turbine)

    implementation(libs.lifecycleRuntimeCompose)
    implementation(libs.splashscreen)

    implementation(project(":shared:dispatchers"))
    implementation(project(":shared:cryptography"))
    implementation(project(":shared:accounts:account"))
    implementation(project(":shared:database"))
    implementation(project(":shared:files:files-data"))
    implementation(project(":shared:files:backup-data"))
    implementation(project(":shared:accounts:account-data"))
    implementation(project(":shared:designsystem:theme"))
}