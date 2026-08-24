package io.quut.omnivisor.sponge.universe.archetype

import io.quut.omnivisor.api.multiverse.IMultiverse
import io.quut.omnivisor.api.multiverse.physical.IPhysicalMultiverseContainer
import io.quut.omnivisor.api.multiverse.physical.IPhysicalMultiverseContext
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverse
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverseContainer
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverseContext
import io.quut.omnivisor.api.universe.IUniverse
import io.quut.omnivisor.api.universe.IUniverseArchetype
import io.quut.omnivisor.api.universe.IUniverseTemplate
import io.quut.omnivisor.api.universe.IUniverseTemplate.IStepBuilder
import io.quut.omnivisor.api.universe.physical.IPhysicalUniverseContainer
import io.quut.omnivisor.api.universe.physical.IPhysicalUniverseContext
import io.quut.omnivisor.sponge.universe.template.UniverseTemplate
import io.quut.omnivisor.sponge.universe.template.UniverseTemplateStepBuilder
import net.kyori.option.OptionSchema
import net.kyori.option.OptionState
import java.util.concurrent.CompletableFuture
import java.util.function.Function

internal class UniverseArchetypeBuilder<TInstance, TConfig> : IUniverseArchetype.IBuilder<TInstance, TConfig>
{
	private var options: OptionSchema? = null
	private var configLoader: Function<OptionState, TConfig>? = null

	override fun options(schema: OptionSchema): IUniverseArchetype.IBuilder<TInstance, TConfig>
	{
		this.options = schema

		return this
	}

	override fun configLoader(function: Function<OptionState, TConfig>): IUniverseArchetype.IBuilder<TInstance, TConfig>
	{
		this.configLoader = function

		return this
	}

	override fun universe(): IStepBuilder<TInstance, IUniverse<TInstance>, IPhysicalUniverseContainer, IPhysicalUniverseContext<TConfig, IPhysicalUniverseContainer>, TConfig, TConfig, IUniverseArchetype<TInstance, TConfig>> =
		UniverseTemplateStepBuilder({ context -> CompletableFuture.completedFuture(context.config) }) { function -> UniverseArchetype(this.options!!, this.configLoader!!, UniverseTemplate.universe(function)) }

	override fun multiverse(node: Boolean): IStepBuilder<TInstance, IMultiverse<TInstance>, IPhysicalMultiverseContainer, IPhysicalMultiverseContext<TConfig, IPhysicalMultiverseContainer>, TConfig, TConfig, IUniverseArchetype<TInstance, TConfig>> =
		UniverseTemplateStepBuilder({ context -> CompletableFuture.completedFuture(context.config) }) { function -> UniverseArchetype(this.options!!, this.configLoader!!, UniverseTemplate.multiverse(node, function)) }

	override fun virtualMultiverse(node: Boolean): IStepBuilder<TInstance, IVirtualMultiverse<TInstance>, IVirtualMultiverseContainer, IVirtualMultiverseContext<TConfig, IVirtualMultiverseContainer>, TConfig, TConfig, IUniverseArchetype<TInstance, TConfig>> =
		UniverseTemplateStepBuilder({ context -> CompletableFuture.completedFuture(context.config) }) { function -> UniverseArchetype(this.options!!, this.configLoader!!, UniverseTemplate.virtualMultiverse(node, function)) }

	override fun dynamic(function: Function<TConfig, IUniverseTemplate<TInstance, TConfig>>): IUniverseArchetype<TInstance, TConfig> =
		UniverseArchetype(this.options!!, this.configLoader!!, UniverseTemplate.Dynamic(function))
}
