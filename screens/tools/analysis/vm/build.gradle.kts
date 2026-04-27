plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.tools.analysis.vm"
    consumerProguardFile("consumer-rules.pro")
    proguardFile("proguard-rules.pro")
    di = true
}

dependencies {
    implementation(project(":domain:api"))
    implementation(project(":game:logic:api"))
    implementation(project(":game:engine:api"))
    implementation(project(":game:serializer:api"))
    implementation(project(":game:serializer:di"))
    implementation(project(":screens:common"))
    implementation(project(":settings:application:api"))

    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(project(":game:logic:impl"))
    testImplementation(project(":game:serializer:impl"))
    testImplementation(testFixtures(project(":game:engine:api")))
}
