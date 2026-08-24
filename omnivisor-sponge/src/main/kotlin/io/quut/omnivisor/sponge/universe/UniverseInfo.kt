package io.quut.omnivisor.sponge.universe

import io.quut.omnivisor.api.universe.IUniverseInfo
import net.kyori.adventure.key.Key

internal class UniverseInfo(override val id: Int, override val key: Key, override val kind: Key) : IUniverseInfo
{
	override fun hashCode(): Int = this.id
	override fun equals(other: Any?): Boolean = other is UniverseInfo && this.id == other.id
}
