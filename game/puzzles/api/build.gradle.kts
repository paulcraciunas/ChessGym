import plugins.extensions.testFixturesImplementation

plugins {
    id("conventions.library")
}

library {
    testFixtures = true
}

dependencies {
    implementation(project(":game:logic:api"))
    implementation(libs.kotlinx.coroutines.core)

    testFixturesImplementation(project(":game:logic:impl"))
    testFixturesImplementation(project(":game:serializer:impl"))
}
