import kotlin.io.path.div
import kotlin.io.path.relativeTo

version = properties["version"].toString()
group = "sugarmelt"

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
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
    dependsOn(tasks.getByName("generateI18n4kFiles"))
    filesMatching("manifest.json") {
        filter {
            it.replace(Regex("\\$\\{([^}]+)\\}")) { result ->
                properties[result.groups[1]!!.value]?.toString() ?: result.value
            }
        }
    }
}

tasks.register<Copy>("copyRootResources") {
    dependsOn("jsProcessResources")
    group = "resources"
    from(layout.projectDirectory)
    into(tasks.getByName<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("jsBrowserProductionWebpack").outputDirectory)
    include("README.md", "LICENSE.md")
}

private fun File.toLinkedString(): String =
    toURI().toURL().toExternalForm().replace("file:/", "file:///")

private fun java.nio.file.Path.toLinkedString(): String =
    toFile().toURI().toURL().toExternalForm().replace("file:/", "file:///")

private fun insertMeta(file: File, name: String, content: String) {
    val meta = "<meta name=\"$name\" content=\"$content\"/>"
    file.apply {
        writeText(readText().replace("<title>", "$meta\n    <title>"))
    }
}

tasks.getByName<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("jsBrowserDevelopmentWebpack") {
    dependsOn(tasks.getByName<Copy>("copyRootResources"))
    doLast {
        insertMeta(outputDirectory.get().file("panel.html").asFile, "mode", "development")
        println("$name output is: ${outputDirectory.get().asFile.toLinkedString()}")
    }
}

tasks.getByName<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("jsBrowserProductionWebpack") {
    dependsOn(tasks.getByName<Copy>("copyRootResources"))
    doLast {
        println("$name output is: ${outputDirectory.get().asFile.toLinkedString()}")
    }
}

tasks.register<Copy>("unpacked") {
    description = "package all files"
    group = "package"
    from(tasks.getByName("jsBrowserDistribution"))
    destinationDir = layout.buildDirectory.dir("dist/package").get().asFile
    include("**/*")
    filesMatching("manifest.json") {
        filter {
            it.replace(Regex("\\$\\{([^}]+)\\}")) { result ->
                properties[result.groups[1]!!.value]?.toString() ?: result.value
            }
        }
    }
    doLast {
        println("$name output is: ${destinationDir.toLinkedString()}")
    }
}

tasks.register<Zip>("package") {
    description = "package all files to zip"
    group = "package"
    archiveFileName.set("${project.name}.zip")
    val webpack =
        tasks.getByName<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("jsBrowserProductionWebpack")
    dependsOn(webpack)
    from(tasks.getByName("jsProcessResources").outputs)
    from(webpack.outputDirectory)
    val path = layout.buildDirectory.get().asFile.toPath().resolve("package")
    val directory = path.toFile()
    destinationDirectory.set(directory)
    val file = path.resolve(archiveFileName.get())
    exclude("*.zip")
    doLast {
        println("$name output is: ${file.toLinkedString()} in ${path.toLinkedString()}")
    }
}
tasks.register<Zip>("zipSource") {
    description = "prepare source zip"
    dependsOn("clean", "build")
    group = "package"
    archiveFileName.set("${project.name}-source.zip")
    from(layout.projectDirectory)
    val buildPath = layout.buildDirectory.get().asFile.toPath()
    val packagePath = buildPath / "package"
    val packageFile = packagePath / archiveFileName.get()
    exclude(".*", "*.zip", "gradle/wrapper")
    exclude(
        listOf(buildPath, packagePath, packageFile)
            .map { it.relativeTo(layout.projectDirectory.asFile.toPath()).toString() })
    destinationDirectory.set(packagePath.toFile())
    doLast {
        println("$name output is: ${packageFile.toLinkedString()} in ${packagePath.toLinkedString()}")
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
