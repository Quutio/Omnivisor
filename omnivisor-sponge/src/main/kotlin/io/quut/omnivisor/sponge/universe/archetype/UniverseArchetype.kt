package io.quut.omnivisor.sponge.universe.archetype

import io.quut.omnivisor.api.universe.IUniverse
import io.quut.omnivisor.api.universe.IUniverseArchetype
import io.quut.omnivisor.api.universe.IUniverseFactory
import io.quut.omnivisor.sponge.universe.template.UniverseTemplate
import net.kyori.option.OptionSchema
import net.kyori.option.OptionState
import java.util.concurrent.CompletableFuture
import java.util.function.Function

internal class UniverseArchetype<TInstance, TConfig>(
	override val options: OptionSchema,
	private val configLoader: Function<OptionState, TConfig>,
	private val step: UniverseTemplate<TInstance, TConfig>) : IUniverseArchetype<TInstance, TConfig>
{
	override fun loadConfig(state: OptionState): TConfig = this.configLoader.apply(state)

	override fun create(config: TConfig, factory: IUniverseFactory<TInstance, TConfig>): CompletableFuture<out IUniverse<TInstance>> =
		this.step.create(config, factory)
}
