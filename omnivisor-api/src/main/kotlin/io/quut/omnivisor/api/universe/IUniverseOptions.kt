package io.quut.omnivisor.api.universe

import net.kyori.adventure.key.Key

interface IUniverseOptions<TInstance, TConfig>
{
	val kind: Key
	val archetype: IUniverseArchetype<TInstance, TConfig>
	val config: TConfig

	companion object
	{
		@JvmStatic
		fun <TInstance, TConfig> of(kind: Key, archetype: IUniverseArchetype<TInstance, TConfig>, config: TConfig): IUniverseOptions<TInstance, TConfig> =
			Impl(kind, archetype, config)
	}

	private class Impl<TInstance, TConfig>(
		override val kind: Key,
		override val archetype: IUniverseArchetype<TInstance, TConfig>,
		override val config: TConfig) : IUniverseOptions<TInstance, TConfig>
}
