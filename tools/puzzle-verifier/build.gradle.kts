plugins {
    id("conventions.library")
    application
}

application {
    mainClass.set("com.paulcraciunas.tools.verifier.PuzzleVerifierKt")
}

dependencies {
    implementation(project(":game:logic:api"))
    implementation(project(":game:logic:impl"))
    implementation(project(":game:serializer:api"))
    implementation(project(":game:serializer:impl"))

    implementation(libs.public.zstd)
}
