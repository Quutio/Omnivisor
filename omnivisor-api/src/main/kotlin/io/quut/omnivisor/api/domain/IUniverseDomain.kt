package io.quut.omnivisor.api.domain

import net.kyori.adventure.key.Key
import java.util.Collections

interface IUniverseDomain
{
	companion object
	{
		@JvmStatic
		fun world(worldKey: Key): IWorld = IWorld.of(worldKey)

		@JvmStatic
		fun compound(vararg area: IUniverseDomain): ICompound = ICompound.of(area.toHashSet())
	}

	interface IWorld : IUniverseDomain
	{
		val worldKey: Key

		companion object
		{
			@JvmStatic
			fun of(worldKey: Key): IWorld = Impl(worldKey)
		}

		private class Impl(override val worldKey: Key): IWorld
	}

	interface ICompound : IUniverseDomain
	{
		val scopes: Collection<IUniverseDomain>

		companion object
		{
			@JvmStatic
			fun of(scopes: Set<IUniverseDomain>): ICompound = Impl(Collections.unmodifiableSet(scopes))
		}

		private class Impl(override val scopes: Set<IUniverseDomain>): ICompound
	}
}
