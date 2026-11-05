pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral(); maven("https://jitpack.io") }
}
rootProject.name = "Egizak"
include(":app")
// Stage 1: NewBlackbox engine (Apache-2.0) is added as a git submodule in engine/
// and its core module is included here, e.g.:
// include(":Bcore"); project(":Bcore").projectDir = file("engine/Bcore")
