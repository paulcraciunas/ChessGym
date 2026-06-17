plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.global.resources"
}

abstract class GeneratePreWarmDrawableListTask : DefaultTask() {
    @get:InputDirectory
    abstract val drawableDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val drawableDir = drawableDirectory.get().asFile

        val outDir = outputDirectory.get().asFile
        val outputFile = File(outDir, "PreWarmDrawables.kt")

        val files = drawableDir.listFiles { _, name ->
            name.endsWith(".xml") || name.endsWith(".png") || name.endsWith(".webp")
        } ?: emptyArray()

        val resourceNames = files.map { it.nameWithoutExtension }

        outputFile.parentFile.mkdirs()
        outputFile.writeText(
            """
            package com.paulcraciunas.global.resources

            object PreWarmDrawables {
                val list = intArrayOf(
                    ${resourceNames.joinToString(",\n                    ") { "R.drawable.$it" }}
                )
            }
        """.trimIndent()
        )
    }
}

// Register task provider
val generatePreWarmDrawableList = tasks.register<GeneratePreWarmDrawableListTask>("generatePreWarmDrawableList") {
    drawableDirectory.set(project.layout.projectDirectory.dir("src/main/res/drawable"))
    outputDirectory.set(project.layout.buildDirectory.dir("generated/source/prewarm/main/kotlin"))
}

// Hook it directly into the Components API
androidComponents {
    onVariants { variant ->
        variant.sources.kotlin?.addGeneratedSourceDirectory(
            taskProvider = generatePreWarmDrawableList,
            wiredWith = GeneratePreWarmDrawableListTask::outputDirectory
        )
    }
}
