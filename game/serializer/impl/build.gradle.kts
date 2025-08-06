plugins {
    id("conventions.library")
}

dependencies {
    api(project(":game:serializer:api"))
    implementation(project(":game:logic:api"))

    testImplementation(project(":game:logic:impl"))
    testImplementation(testFixtures(project(":game:logic:impl")))
}
