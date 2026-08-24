plugins {
	`maven-publish`
	`java-library`

	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.spotless)
}

allprojects {
	group = "io.quut"
	version = "1.0-SNAPSHOT"

	apply(plugin = "com.diffplug.spotless")

	spotless {
		kotlin {
			ktlint()
			leadingSpacesToTabs()
			endWithNewline()
			trimTrailingWhitespace()
		}
		kotlinGradle {
			ktlint()
			leadingSpacesToTabs()
			endWithNewline()
			trimTrailingWhitespace()
		}
	}

	repositories {
		mavenCentral()
		mavenLocal()

		val gprUser: String? by project
		val gprPassword: String? by project

		maven {
			name = "github-fusion"
			url = uri("https://maven.pkg.github.com/Quutio/Fusion")
			credentials {
				username = gprUser ?: System.getenv("GITHUB_ACTOR")
				password = gprPassword ?: System.getenv("GITHUB_TOKEN")
			}
		}

		maven {
			name = "github-harmony"
			url = uri("https://maven.pkg.github.com/Quutio/Harmony")
			credentials {
				username = gprUser ?: System.getenv("GITHUB_ACTOR")
				password = gprPassword ?: System.getenv("GITHUB_TOKEN")
			}
		}
	}
}

subprojects {
	apply(plugin = "maven-publish")
	apply(plugin = "java-library")
	apply(plugin = "kotlin")

	java {
		withSourcesJar()
	}

	kotlin {
		jvmToolchain(21)
	}

	publishing {
		publications {
			register("omnivisor", MavenPublication::class) {
				from(components["java"])

				this.artifactId = project.name.lowercase()

				pom {
					this.name.set(project.name)
					this.description.set(project.description)
				}
			}
		}
	}
}
