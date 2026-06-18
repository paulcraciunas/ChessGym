plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.common"
    compose = true
    benchmark = true
}

android {
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        buildConfigField("boolean", "ENABLE_TEST_TAGS", "false")
    }
    buildTypes {
        getByName("benchmark") {
            buildConfigField("boolean", "ENABLE_TEST_TAGS", "true")
        }
    }
}

dependencies {
    implementation(project(":global:resources"))
    implementation(project(":game:logic:api"))
    implementation(project(":domain:api"))
    api(project(":screens:data"))
    api(project(":settings:application:api"))

    "benchmarkApi"(libs.androidx.compose.runtime.tracing)

    testImplementation(project(":game:logic:impl"))
}
