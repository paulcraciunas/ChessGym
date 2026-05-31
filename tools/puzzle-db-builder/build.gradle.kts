plugins {
    id("conventions.library")
    application
}

application {
    mainClass.set("com.paulcraciunas.tools.dbbuilder.PuzzleDbBuilderKt")
}

dependencies {
    implementation(project(":game:logic:api"))
    implementation(project(":game:logic:builders"))
    implementation(project(":game:puzzles:api"))
    implementation(project(":game:serializer:api"))
    implementation(project(":game:serializer:impl"))

    implementation(libs.public.zstd)
    implementation(libs.public.sqlite.jdbc)
}
