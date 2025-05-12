pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { setUrl("https://jitpack.io") }
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
    }
}

rootProject.name = "MediaServiceCore"

// prepare for git submodules
val sharedDir = if (File(rootDir, "../SharedModules").exists())
    File(rootDir, "../SharedModules")
else
    File(rootDir, "./SharedModules")

include(":mediaserviceinterfaces", ":youtubeapi", ":googleapi", ":sharedtests", ":commons-io-2.8.0", ":sharedutils")
project(":mediaserviceinterfaces").projectDir = File(rootDir, "mediaserviceinterfaces")
project(":youtubeapi").projectDir = File(rootDir, "youtubeapi")
project(":googleapi").projectDir = File(rootDir, "googleapi")
project(":sharedtests").projectDir = File(sharedDir, "sharedtests")
project(":commons-io-2.8.0").projectDir = File(sharedDir, "commons-io-2.8.0")
project(":sharedutils").projectDir = File(sharedDir, "sharedutils")

