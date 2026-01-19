plugins {
    id("conventions.library")
}

dependencies {
    implementation(project(":game:logic:api"))
    implementation(project(":game:puzzles:api"))
    implementation(project(":settings:application:api"))
}
