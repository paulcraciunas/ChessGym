plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.serializer.di"
    di = true
}

dependencies {
    implementation(project(":game:logic:api"))
    implementation(project(":game:serializer:impl"))
}
