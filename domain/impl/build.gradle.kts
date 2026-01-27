plugins {
    id("conventions.library")
}

dependencies {
    implementation(project(":domain:api"))
    implementation(project(":game:logic:api"))
    implementation(project(":game:puzzles:api"))
    implementation(project(":settings:application:api"))
    implementation(project(":user:api"))
    implementation(libs.javax.inject)

    testImplementation(project(":game:logic:impl"))
    testImplementation(testFixtures(project(":settings:application:api")))
    testImplementation(testFixtures(project(":game:puzzles:api")))
    testImplementation(testFixtures(project(":domain:api")))
    testImplementation(testFixtures(project(":user:api")))
}
