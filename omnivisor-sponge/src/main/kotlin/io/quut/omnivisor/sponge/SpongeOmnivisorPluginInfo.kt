package io.quut.omnivisor.sponge

import com.google.inject.Inject
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.config.DefaultConfig
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.loader.ConfigurationLoader
import java.lang.invoke.MethodHandles
import java.nio.file.Path

internal class SpongeOmnivisorPluginInfo @Inject constructor(
	@param: ConfigDir(sharedRoot = false) val configDirectory: Path,
	@param: DefaultConfig(sharedRoot = false) val configLoader: ConfigurationLoader<CommentedConfigurationNode>)
{
	val lookup: MethodHandles.Lookup = MethodHandles.lookup()
}
