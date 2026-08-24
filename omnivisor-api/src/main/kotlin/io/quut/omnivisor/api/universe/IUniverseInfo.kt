package io.quut.omnivisor.api.universe

import net.kyori.adventure.key.Key

interface IUniverseInfo
{
	val id: Int
	val key: Key
	val kind: Key
}
