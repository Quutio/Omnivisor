package io.quut.omnivisor.velocity.util

import net.kyori.adventure.key.Key

internal object Const
{
	internal const val NAMESPACE: String = "omnivisor"

	internal val HANDSHAKE_PLUGIN_CHANNEL_KEY: Key = this.key("handshake")

	fun key(value: String): Key = Key.key(this.NAMESPACE, value)
}
