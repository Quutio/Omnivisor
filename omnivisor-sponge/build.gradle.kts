import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.sun.beans.introspect.PropertyInfo
import org.gradle.kotlin.dsl.withType
import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.sponge)
	alias(libs.plugins.shadow)
}

dependencies {
	implementation(project(":omnivisor-api"))

	implementation(libs.harmony.sponge)
}

sponge {
	apiVersion("17.0.0")
	license("MIT")
	loader {
		name(PluginLoaders.JAVA_PLAIN)
		version("1.0")
	}
	plugin("omnivisor") {
		entrypoint("io.quut.omnivisor.sponge.SpongeOmnivisorPluginLoader")
		guiceModule($$"io.quut.omnivisor.sponge.SpongeOmnivisorPluginLoader$Module")
		displayName("Omnivisor")
		description("Its just a simulation!")
		contributor("Joni Aromaa (isokissa3)") {
			description("Lead Developer")
		}
		dependency("spongeapi") {
			loadOrder(PluginDependency.LoadOrder.AFTER)
			optional(false)
		}
	}
}

tasks.withType<ShadowJar> {
	relocate("kotlin", "io.quut.omnivisor.libs.kotlin")
	relocate("org.intellij.lang.annotations", "io.quut.omnivisor.libs.org.intellij.lang.annotations")
	relocate("org.jetbrains.annotations", "io.quut.omnivisor.libs.org.jetbrains.annotations")
}
