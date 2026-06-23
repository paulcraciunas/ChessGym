plugins {
    id("conventions.library")
    application
}

application {
    mainClass.set("com.paulcraciunas.tools.dbbuilder.PuzzleDbBuilderKt")
}

tasks.register<JavaExec>("runBenchmarkDbBuilder") {
    group = "tools"
    description = "Generates a deterministic puzzle database for benchmark/baseline-profile builds"
    mainClass.set("com.paulcraciunas.tools.dbbuilder.BenchmarkDbBuilderKt")
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf(
        rootProject.file("app/src/benchmark/assets/databases").absolutePath
    )
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
