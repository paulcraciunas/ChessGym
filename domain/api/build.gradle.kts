import plugins.extensions.testFixturesImplementation

plugins {
    id("conventions.library")
}

library {
    testFixtures = true
}

dependencies {
    implementation(project(":game:logic:api"))
    implementation(project(":game:engine:api"))
    implementation(project(":game:puzzles:api"))
    implementation(project(":game:serializer:api"))
    implementation(project(":settings:application:api"))

    testFixturesImplementation(project(":game:logic:api"))
    testFixturesImplementation(project(":game:logic:impl"))
    testFixturesImplementation(project(":game:engine:api"))
}
