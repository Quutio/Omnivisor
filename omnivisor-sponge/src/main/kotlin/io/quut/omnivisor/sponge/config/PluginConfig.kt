package io.quut.omnivisor.sponge.config

import org.spongepowered.api.ResourceKey
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
internal class PluginConfig
{
	lateinit var universes: Map<ResourceKey, UniverseConfig>
}
