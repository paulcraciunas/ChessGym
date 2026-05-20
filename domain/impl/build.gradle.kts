plugins {
    id("conventions.library")
}

dependencies {
    implementation(project(":domain:api"))
    implementation(project(":game:logic:api"))
    implementation(project(":game:engine:api"))
    implementation(project(":game:puzzles:api"))
    implementation(project(":game:serializer:api"))
    implementation(project(":settings:application:api"))
    implementation(project(":user:api"))
    implementation(project(":global:device:api"))
    implementation(project(":global:qualifiers"))
    implementation(libs.javax.inject)

    testImplementation(project(":game:logic:impl"))
    testImplementation(project(":game:serializer:impl"))
    testImplementation(testFixtures(project(":settings:application:api")))
    testImplementation(testFixtures(project(":game:engine:api")))
    testImplementation(testFixtures(project(":game:puzzles:api")))
    testImplementation(testFixtures(project(":domain:api")))
    testImplementation(testFixtures(project(":user:api")))
    testImplementation(testFixtures(project(":global:device:api")))
}
