import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension
import kotlin.io.path.div
import kotlin.io.path.relativeTo

version = (properties["version"]).toString()
group = "sugarmelt"

plugins {
    val kotlinVersion: String by System.getProperties()
    val i18n4kVersion: String by System.getProperties()
    kotlin("multiplatform") version kotlinVersion
    id("de.comahe.i18n4k") version i18n4kVersion
}

val javaVersion: String by System.getProperties()
val kotlinVersion: String by System.getProperties()
val kotlinWrappersVersion: String by System.getProperties()
val i18n4kVersion: String by System.getProperties()
val kotlinCssGeneratorVersion: String by System.getProperties()


repositories {
    mavenCentral()
}

dependencies {
    constraints {
    }
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
            webpackTask {
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
        val commonMain by getting {
            dependencies {
                api(project.dependencies.platform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:$kotlinWrappersVersion"))
                api("nl.astraeus:kotlin-css-generator:$kotlinCssGeneratorVersion")
                api("de.comahe.i18n4k:i18n4k-core-js:$i18n4kVersion")
            }
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(project.dependencies.platform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:$kotlinWrappersVersion"))
                api("nl.astraeus:kotlin-css-generator:$kotlinCssGeneratorVersion")
                implementation("de.comahe.i18n4k:i18n4k-core-js:$i18n4kVersion")
            }
        }

        val jsTest by getting {
            dependencies {
                api(kotlin("test"))
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
                project.properties[result.groups[1]!!.value]?.toString() ?: result.value
            }
        }
    }
}

tasks.register<Copy>("copyRootResources") {
    dependsOn("jsProcessResources")
    group = "resources"
    from(layout.projectDirectory)
    into(tasks.getByName<KotlinWebpack>("jsBrowserProductionWebpack").outputDirectory)
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

tasks.getByName<KotlinWebpack>("jsBrowserDevelopmentWebpack") {
    dependsOn(tasks.getByName<Copy>("copyRootResources"))
    doLast {
        insertMeta(outputDirectory.get().file("panel.html").asFile, "mode", "development")
        println("$name output is: ${this@getByName.outputDirectory.get().asFile.toLinkedString()}")
    }
}

tasks.getByName<KotlinWebpack>("jsBrowserProductionWebpack") {
    dependsOn(tasks.getByName<Copy>("copyRootResources"))
    doLast {
        println("$name output is: ${this@getByName.outputDirectory.get().asFile.toLinkedString()}")
    }
}

tasks.register<Zip>("package") {
    group = "package"
    archiveFileName.set("${project.name}.zip")
    val webpack = tasks.getByName<KotlinWebpack>("jsBrowserProductionWebpack")
    dependsOn(webpack)
    from(tasks.getByName("jsProcessResources").outputs)
    from(webpack.outputDirectory)
    val path = layout.buildDirectory.get().asFile.toPath() / "package"
    val directory = path.toFile()
    destinationDirectory.set(directory)
    val file = path / archiveFileName.get()
    exclude("*.zip")
    doLast {
        println("$name output is: ${file.toLinkedString()} in ${path.toLinkedString()}")
    }
}

tasks.register<Zip>("zipSource") {
    dependsOn("clean", "build")
    group = "package"
    archiveFileName.set("${project.name}-source.zip")
    from(layout.projectDirectory)
    val buildPath = layout.buildDirectory.get().asFile.toPath()
    val packagePath = buildPath / "package"
    val packageFile = packagePath / archiveFileName.get()
    exclude(".*", "*.zip", "gradle/wrapper")
    exclude(listOf(buildPath, packagePath, packageFile)
        .map { it.relativeTo(layout.projectDirectory.asFile.toPath()).toString() })
    destinationDirectory.set(packagePath.toFile())
    doLast {
        println("$name output is: ${packageFile.toLinkedString()} in ${packagePath.toLinkedString()}")
    }
}

tasks.named("build") {
    finalizedBy(tasks.named("package"))
}

rootProject.plugins.withType(YarnPlugin::class.java) {
    rootProject.the<YarnRootExtension>().yarnLockMismatchReport = YarnLockMismatchReport.WARNING
    rootProject.the<YarnRootExtension>().reportNewYarnLock = true
    rootProject.the<YarnRootExtension>().yarnLockAutoReplace = true
}
