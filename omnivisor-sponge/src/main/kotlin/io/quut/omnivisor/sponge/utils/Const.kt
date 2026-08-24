package io.quut.omnivisor.sponge.utils

import org.spongepowered.api.ResourceKey

internal object Const
{
	internal const val NAMESPACE: String = "omnivisor"

	internal const val HANDSHAKE_CHANNEL: String = "handshake"

	internal val UNIVERSE_ARCHETYPE_REGISTRY_KEY: ResourceKey = this.key("universe_archetype")

	internal val HANDSHAKE_CHANNEL_KEY: ResourceKey = this.key(this.HANDSHAKE_CHANNEL)

	internal fun key(value: String): ResourceKey = ResourceKey.of(this.NAMESPACE, value)
}
