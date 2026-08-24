package io.quut.omnivisor.sponge.universe.template

import io.quut.omnivisor.api.multiverse.IMultiverse
import io.quut.omnivisor.api.multiverse.physical.IPhysicalMultiverseContainer
import io.quut.omnivisor.api.multiverse.physical.IPhysicalMultiverseContext
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverse
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverseContainer
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverseContext
import io.quut.omnivisor.api.universe.IUniverse
import io.quut.omnivisor.api.universe.IUniverseTemplate
import io.quut.omnivisor.api.universe.physical.IPhysicalUniverseContainer
import io.quut.omnivisor.api.universe.physical.IPhysicalUniverseContext
import java.util.concurrent.CompletableFuture

internal class UniverseTemplateBuilder<TInstance, TConfig> : IUniverseTemplate.IBuilder<TInstance, TConfig, IUniverseTemplate<TInstance, TConfig>>
{
	override fun universe(): IUniverseTemplate.IStepBuilder<TInstance, IUniverse<TInstance>, IPhysicalUniverseContainer, IPhysicalUniverseContext<TConfig, IPhysicalUniverseContainer>, TConfig, TConfig, IUniverseTemplate<TInstance, TConfig>> =
		UniverseTemplateStepBuilder({ context -> CompletableFuture.completedFuture(context.config) }) { function -> UniverseTemplate.universe(function) }

	override fun multiverse(node: Boolean): IUniverseTemplate.IStepBuilder<TInstance, IMultiverse<TInstance>, IPhysicalMultiverseContainer, IPhysicalMultiverseContext<TConfig, IPhysicalMultiverseContainer>, TConfig, TConfig, IUniverseTemplate<TInstance, TConfig>> =
		UniverseTemplateStepBuilder({ context -> CompletableFuture.completedFuture(context.config) }) { function -> UniverseTemplate.multiverse(node, function) }

	override fun virtualMultiverse(node: Boolean): IUniverseTemplate.IStepBuilder<TInstance, IVirtualMultiverse<TInstance>, IVirtualMultiverseContainer, IVirtualMultiverseContext<TConfig, IVirtualMultiverseContainer>, TConfig, TConfig, IUniverseTemplate<TInstance, TConfig>> =
		UniverseTemplateStepBuilder({ context -> CompletableFuture.completedFuture(context.config) }) { function -> UniverseTemplate.virtualMultiverse(node, function) }
}
