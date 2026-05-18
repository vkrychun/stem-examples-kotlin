pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://raw.githubusercontent.com/vkrychun/stem-runtime-kotlin/main") }
        google()
        mavenCentral()
    }
}

rootProject.name = "stem-examples-kotlin"

include(":StemJSON")
include(":StemQuickStart")
include(":StemCompose")
include(":StemHome")
