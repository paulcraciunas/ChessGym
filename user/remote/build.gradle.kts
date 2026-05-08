plugins {
    id("conventions.library")
}

library {
    serialization = true
}

dependencies {
    api(project(":user:api"))

    implementation(project(":global:qualifiers"))

    implementation(libs.javax.inject)
    implementation(libs.bundles.ktor.client)

    testImplementation(libs.ktor.client.mock)
}
