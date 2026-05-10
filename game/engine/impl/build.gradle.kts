plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.game.engine.impl"
    proguardFile("proguard-rules.pro")
    di = true
}

android {
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }
    defaultConfig {
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }
    }
}

dependencies {
    api(project(":game:engine:api"))
    implementation(project(":global:utils"))
    implementation(libs.public.timber)
}
