package io.quut.omnivisor.api.multiverse

import io.quut.omnivisor.api.universe.IUniverse
import io.quut.omnivisor.api.universe.IUniverseLike
import net.kyori.adventure.key.Key

interface IMultiverseLike : IUniverseLike
{
	fun child(key: Key): IUniverse<*>?
}
