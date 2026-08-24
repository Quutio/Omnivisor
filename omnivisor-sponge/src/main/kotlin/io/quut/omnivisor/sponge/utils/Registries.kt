package io.quut.omnivisor.sponge.utils

import io.quut.omnivisor.api.universe.IUniverseArchetype
import org.spongepowered.api.ResourceKey
import org.spongepowered.api.Sponge
import org.spongepowered.api.registry.DefaultedRegistryType
import org.spongepowered.api.registry.RegistryRoots
import org.spongepowered.api.registry.RegistryType

internal object Registries
{
	internal val UNIVERSE_ARCHETYPE_REGISTRY: DefaultedRegistryType<IUniverseArchetype<*, *>> = this.server(Const.UNIVERSE_ARCHETYPE_REGISTRY_KEY)

	private fun <T> server(key: ResourceKey): DefaultedRegistryType<T> =
		RegistryType.of<T>(RegistryRoots.SPONGE, key).asDefaultedType(Sponge::server)
}
