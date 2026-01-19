plugins {
    id("conventions.library")
}

dependencies {
    api(project(":user:api"))

    implementation(libs.javax.inject)
}
