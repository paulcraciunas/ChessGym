plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.tools.importgame.vm"
    di = true
    compose = true
}

dependencies {
    implementation(project(":domain:api"))
    implementation(project(":global:qualifiers"))
    implementation(project(":game:engine:api"))
    implementation(project(":game:logic:api"))
    implementation(project(":game:serializer:api"))
    implementation(project(":game:serializer:di"))
    implementation(project(":screens:data"))

    implementation(libs.public.timber)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(project(":game:logic:impl"))
    testImplementation(project(":game:serializer:impl"))
    testImplementation(testFixtures(project(":domain:api")))
}
