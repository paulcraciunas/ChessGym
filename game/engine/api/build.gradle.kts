import plugins.extensions.testFixturesImplementation

plugins {
    id("conventions.library")
}

library {
    testFixtures = true
}

dependencies {
    api(project(":game:logic:api"))

    testFixturesImplementation(project(":game:logic:api"))
}
