plugins {
	alias(libs.plugins.kotlin.jvm)
	id(libs.plugins.kotlin.kapt.get().pluginId)
	alias(libs.plugins.shadow)
}

repositories {
	maven {
		name = "velocity"
		url = uri("https://repo.papermc.io/repository/maven-public/")
	}
}

dependencies {
	implementation(project(":omnivisor-api"))

	compileOnly(libs.velocity)
	kapt(libs.velocity)

	compileOnly(libs.fusion.velocity)
}

kotlin {
	jvmToolchain(25)
}
