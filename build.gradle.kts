import org.ajoberstar.grgit.Grgit
import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    id("org.enginehub.codecov")
    jacoco
}

logger.lifecycle("""
*******************************************
 You are building WorldEdit!

 If you encounter trouble:
 1) Read COMPILING.md if you haven't yet
 2) Try running 'build' in a separate Gradle run
 3) Use gradlew and not gradle
 4) If you still need help, ask on Discord! https://discord.gg/enginehub

 Output files will be in [subproject]/build/libs
*******************************************
""")

applyCommonConfiguration()
applyRootArtifactoryConfig()

val totalReport = tasks.register<JacocoReport>("jacocoTotalReport") {
    for (proj in subprojects) {
        proj.apply(plugin = "jacoco")
        proj.plugins.withId("java") {
            executionData(
                    fileTree(proj.buildDir.absolutePath).include("**/jacoco/*.exec")
            )
            sourceSets(proj.the<JavaPluginConvention>().sourceSets["main"])
            reports {
                xml.isEnabled = true
                xml.destination = rootProject.buildDir.resolve("reports/jacoco/report.xml")
                html.isEnabled = true
            }
            dependsOn(proj.tasks.named("test"))
        }
    }
}
afterEvaluate {
    totalReport.configure {
        classDirectories.setFrom(classDirectories.files.map {
            fileTree(it).apply {
                exclude("**/*AutoValue_*")
                exclude("**/*Registration.*")
            }
        })

    }
}

codecov {
    reportTask.set(totalReport)
}

if (!project.hasProperty("gitCommitHash")) {
    apply(plugin = "org.ajoberstar.grgit")
    ext["gitCommitHash"] = try {
        extensions.getByName<Grgit>("grgit").head()?.abbreviatedId
    } catch (e: Exception) {
        logger.warn("Error getting commit hash", e)

        "no.git.id"
    }
}

val updateMinecraft = tasks.register("updateMinecraft") {
    group = "My Tasks"
    description = "Moves jar into minecraft directory (fabric)"

    dependsOn(subprojects.map { it.tasks.named("build") })

    doLast {
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            project.exec {
                commandLine("cmd","/c","updateMinecraft.bat")
            }
        }
        else if (Os.isFamily(Os.FAMILY_MAC) || Os.isFamily(Os.FAMILY_UNIX)) {
            project.exec {
                commandLine("./updateMinecraft.sh")
            }
        }
    }
}
