plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.blindmode.vm"
    di = true
    compose = true
}

dependencies {
    implementation(project(":domain:api"))
    implementation(project(":game:logic:api"))
    implementation(project(":game:logic:builders"))
    implementation(project(":game:engine:api"))
    implementation(project(":global:qualifiers"))
    implementation(project(":screens:data"))
    implementation(project(":settings:application:api"))
    implementation(project(":user:api"))

    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(project(":game:logic:impl"))
    testImplementation(testFixtures(project(":game:engine:api")))
    testImplementation(testFixtures(project(":domain:api")))
    testImplementation(project(":domain:impl"))
    testImplementation(project(":game:serializer:impl"))
    testImplementation(testFixtures(project(":user:api")))
    testImplementation(testFixtures(project(":settings:application:api")))
}
