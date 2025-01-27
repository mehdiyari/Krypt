plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {

    plugins {

        register("kryptAndroidApplication"){
            id = "krypt.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }

        register("kryptAndroidApplicationFlavor"){
            id = "krypt.android.application.flavor"
            implementationClass = "AndroidApplicationFlavorConventionPlugin"
        }

        register("kryptAndroidApplicationCompose"){
            id = "krypt.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }

        register("kryptAndroidLibrary"){
            id = "krypt.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }

        register("kryptAndroidLibraryCompose"){
            id = "krypt.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }

        register("kryptAndroidHilt"){
            id = "krypt.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }

        register("kryptAndroidFeature"){
            id = "krypt.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }

    }

}