plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.common"
    compose = true
}

android {
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        buildConfigField("boolean", "ENABLE_TEST_TAGS", "false")
    }
    buildTypes {
        create("benchmark") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            buildConfigField("boolean", "ENABLE_TEST_TAGS", "true")
        }
    }
}

dependencies {
    implementation(project(":global:resources"))
    implementation(project(":game:logic:api"))
    implementation(project(":game:logic:builders"))
    implementation(project(":domain:api"))
    api(project(":settings:application:api"))
    api(libs.kotlinx.collections.immutable)

    testImplementation(project(":game:logic:impl"))
}
