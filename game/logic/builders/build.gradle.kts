plugins {
    id("conventions.library")
}

dependencies {
    api(project(":game:logic:api"))
    implementation(project(":game:logic:impl"))
}
