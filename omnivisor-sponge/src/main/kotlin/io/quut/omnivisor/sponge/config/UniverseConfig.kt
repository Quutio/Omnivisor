package io.quut.omnivisor.sponge.config

import org.spongepowered.api.ResourceKey
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
internal class UniverseConfig
{
	lateinit var kind: ResourceKey
	lateinit var archetype: ResourceKey
	lateinit var options: ConfigurationNode

	lateinit var children: Map<ResourceKey, UniverseConfig>
}
