package io.quut.omnivisor.sponge.universe.archetype

import io.quut.omnivisor.api.universe.IUniverseArchetype

internal class UniverseArchetypeFactory : IUniverseArchetype.IFactory
{
	override fun <TInstance, TConfig> builder(): IUniverseArchetype.IBuilder<TInstance, TConfig> =
		UniverseArchetypeBuilder()
}
