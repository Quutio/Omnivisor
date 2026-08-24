pluginManagement {
	repositories {
		gradlePluginPortal()
		maven(uri("https://repo.spongepowered.org/repository/maven-public/"))
	}
	plugins {
		id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
		id("com.diffplug.spotless") version "8.9.0"
	}
}

rootProject.name = "Omnivisor"

include("omnivisor-sponge")
include("omnivisor-api")
include("omnivisor-velocity")
