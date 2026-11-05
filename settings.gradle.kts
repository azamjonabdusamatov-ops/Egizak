pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral(); maven("https://www.jitpack.io") }
}
rootProject.name = "Egizak"
include(":app")

// Virtualization engine: NewBlackbox (Apache-2.0), git submodule in engine/
include(":Bcore", ":black-reflection", ":compiler")
project(":Bcore").projectDir = file("engine/Bcore")
project(":black-reflection").projectDir = file("engine/black-reflection")
project(":compiler").projectDir = file("engine/compiler")
