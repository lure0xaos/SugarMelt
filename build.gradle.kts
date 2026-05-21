import utils.toLinkedString

version = properties["version"].toString()
group = "sugarmelt"

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.de.comahe.i18n4k)
}

val javaVersion: String = libs.versions.javaVersion.get()

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
    js {
        browser {
            commonWebpackConfig {
                sourceMaps = true
                devtool = "inline-cheap-module-source-map"
                cssSupport {
                    enabled.set(true)
                }
            }
            testTask {
                useKarma {
                    useFirefoxDeveloper()
                    useChrome()
                }
            }
            binaries.executable()
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(dependencies.platform(libs.org.jetbrains.kotlin.wrappers.kotlin.wrappers.bom))
                implementation(libs.org.jetbrains.kotlinx.kotlinx.serialization.json)
                implementation(libs.nl.astraeus.kotlin.css.generator)
                implementation(libs.de.comahe.i18n4k.i18n4k.core)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.org.jetbrains.kotlin.kotlin.test)
            }
        }
        jsMain {
            dependencies {
                implementation(libs.org.jetbrains.kotlin.wrappers.kotlin.browser)
                implementation(libs.org.jetbrains.kotlin.wrappers.kotlin.web)
            }
        }
    }
}

tasks.named<Copy>("jsProcessResources") {
    duplicatesStrategy = DuplicatesStrategy.WARN
    dependsOn("generateI18n4kFiles")
    val map = properties
        .filterValues { it is String || it is Number || it is Boolean }
        .mapValues { (_, value) -> value.toString() }
    filesMatching("manifest.json") {
        filter {
            it.replace(Regex("\\$\\{([^}]+)\\}")) { result ->
                map[result.groups[1]!!.value] ?: result.value
            }
        }
    }
}

tasks.register<Copy>("copyRootResources") {
    dependsOn("jsProcessResources")
    group = "resources"
    description = "copy root resources"
    from(layout.projectDirectory)
    val taskProvider =
        tasks.named<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("jsBrowserProductionWebpack")
    into(taskProvider.flatMap { it.outputDirectory })
    include("README.md", "LICENSE.md")
}

private fun insertMeta(file: File, name: String, content: String) {
    val meta = "<meta name=\"$name\" content=\"$content\"/>"
    file.apply {
        writeText(readText().replace("<title>", "$meta\n    <title>"))
    }
}

tasks.named<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("jsBrowserDevelopmentWebpack") {
    dependsOn("copyRootResources")
    val directoryProperty = outputDirectory
    val taskName = name
    doLast {
        insertMeta(directoryProperty.file("panel.html").get().asFile, "mode", "development")
        println("$taskName output is: ${directoryProperty.asFile.get().toLinkedString()}")
    }
}

tasks.named<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("jsBrowserProductionWebpack") {
    dependsOn("copyRootResources")
    val directoryProperty = outputDirectory
    val taskName = name
    doLast {
        println("$taskName output is: ${directoryProperty.asFile.get().toLinkedString()}")
    }
}

tasks.register<Copy>("unpacked") {
    description = "package all files"
    group = "package"
    val taskName = name
    from(tasks.named("jsBrowserDistribution"))
    val destDir = layout.buildDirectory.dir("dist/package")
    into(destDir)
    include("**/*")
    val map = properties
        .filterValues { it is String || it is Number || it is Boolean }
        .mapValues { (_, value) -> value.toString() }
    filesMatching("manifest.json") {
        filter {
            it.replace(Regex("\\$\\{([^}]+)\\}")) { result ->
                map[result.groups[1]!!.value] ?: result.value
            }
        }
    }
    doLast {
        println("$taskName output is: ${destDir.get().asFile.toLinkedString()}")
    }
}

tasks.register<Zip>("package") {
    description = "package all files to zip"
    group = "package"
    val archive = "${project.name}.zip"
    val taskName = name
    archiveFileName.set(archive)
    val webpack =
        tasks.named<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("jsBrowserProductionWebpack")
    dependsOn(webpack)
    from(tasks.named("jsProcessResources").map { it.outputs.files }, webpack.flatMap { it.outputDirectory })
    val path = layout.buildDirectory.dir("package")
    destinationDirectory.set(path)
    val file = layout.buildDirectory.file("package/$archive")
    exclude("*.zip")
    doLast {
        println("$taskName output is: ${file.get().asFile.toLinkedString()} in ${path.get().asFile.toLinkedString()}")
    }
}

tasks.register<Zip>("zipSource") {
    description = "prepare source zip"
    dependsOn("clean", "build")
    group = "package"
    val archive = "${project.name}-source.zip"
    val taskName = name
    archiveFileName.set(archive)
    from(layout.projectDirectory)
    val buildPath = layout.buildDirectory
    val packagePath = buildPath.dir("package")
    val packageFile = buildPath.file("package/$archive")
    include("**/*")
    exclude("build", ".gradle", "buildSrc/.gradle", ".idea", ".kotlin")
    destinationDirectory.set(packagePath)
    doLast {
        println("$taskName output is: ${packageFile.get().asFile.toLinkedString()} in ${packagePath.get().asFile.toLinkedString()}")
    }
}

tasks.named("build") {
    finalizedBy(tasks.named("package"), tasks.named("unpacked"))
}

plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
    the<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension>().apply {
        yarnLockMismatchReport = org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport.WARNING
        reportNewYarnLock = true
        yarnLockAutoReplace = true
    }
}
