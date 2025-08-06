plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.puzzles.rated.vm"
    consumerProguardFile("consumer-rules.pro")
    proguardFile("proguard-rules.pro")
    di = true
}

dependencies {
    implementation(project(":user:api"))
    implementation(project(":game:logic:api"))
    implementation(project(":game:puzzles:api"))
    implementation(project(":game:logic:impl")) // TODO Paul: remove this. We shouldn't depend on impls
    implementation(project(":screens:common"))

    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(project(":user:impl"))
}
