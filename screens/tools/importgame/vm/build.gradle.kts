plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.tools.importgame.vm"
    di = true
}

dependencies {
    implementation(project(":game:logic:api"))
    implementation(project(":game:logic:builders"))
    implementation(project(":game:serializer:api"))
    implementation(project(":game:serializer:di"))
    implementation(project(":settings:application:api"))
    implementation(project(":screens:common"))

    implementation(libs.public.timber)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(project(":game:logic:impl"))
    testImplementation(project(":game:serializer:impl"))
    testImplementation(testFixtures(project(":settings:application:api")))
}
