package io.quut.omnivisor.sponge.universe

import io.quut.omnivisor.api.multiverse.IMultiverseContainer
import io.quut.omnivisor.api.multiverse.IMultiverseContext
import io.quut.omnivisor.api.multiverse.physical.IPhysicalMultiverseContainer
import io.quut.omnivisor.api.multiverse.physical.IPhysicalMultiverseContext
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverseContainer
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverseContext
import io.quut.omnivisor.api.universe.IUniverseContainer
import io.quut.omnivisor.api.universe.IUniverseContext
import io.quut.omnivisor.api.universe.event.UniverseEventPriority
import io.quut.omnivisor.api.universe.physical.IPhysicalUniverseContainer
import io.quut.omnivisor.api.universe.physical.IPhysicalUniverseContext
import io.quut.omnivisor.sponge.universe.event.collection.ContainerEventCollection
import io.quut.omnivisor.sponge.universe.event.collection.ContainerEventHandler
import io.quut.omnivisor.sponge.universe.provider.ContainerProviderCollection
import org.apache.logging.log4j.Logger
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.Function

internal abstract class UniverseContext<TConfig, TContainer : IUniverseContainer>(
	private val logger: Logger,
	override val executor: Executor,
	override val config: TConfig,
	override val container: TContainer,
	private val globalProviders: CompletableFuture<ContainerProviderCollection>) : IUniverseContext<TConfig, TContainer>
{
	protected val localProviders: MutableMap<Class<*>, Function<IUniverseContext<*, *>, *>> = mutableMapOf()
	private val events: MutableMap<Class<*>, ContainerEventHandler.Builder<*>> = mutableMapOf()

	override fun <TDependency> globalProvider(dependency: Class<TDependency>): CompletableFuture<TDependency> =
		this.globalProviders.thenApply { providers -> providers.require(dependency, this) }

	@Suppress("UNCHECKED_CAST")
	override fun <TEvent> event(event: Class<TEvent>, priority: UniverseEventPriority, function: Function<TEvent, CompletableFuture<Void>>)
	{
		val builder: ContainerEventHandler.Builder<TEvent> = this.events.computeIfAbsent(event)
			{ _ -> ContainerEventHandler.Builder<TEvent>() } as ContainerEventHandler.Builder<TEvent>

		builder.add(priority, function)
	}

	internal fun collectProviders(): ContainerProviderCollection = this.globalProviders.get().append(this.localProviders)

	internal fun collectEvents(): ContainerEventCollection =
		ContainerEventCollection(this.logger, this.events.asSequence().associate()
			{ e -> e.key to e.value.build(this.executor) })

	internal class Universe<TConfig>(
		logger: Logger,
		executor: Executor,
		config: TConfig,
		container: IPhysicalUniverseContainer,
		globalProviders: CompletableFuture<ContainerProviderCollection>)
		: UniverseContext<TConfig, IPhysicalUniverseContainer>(logger, executor, config, container, globalProviders), IPhysicalUniverseContext<TConfig, IPhysicalUniverseContainer>

	internal abstract class Multiverse<TConfig, TContainer : IMultiverseContainer>(
		logger: Logger,
		executor: Executor,
		config: TConfig,
		container: TContainer,
		globalProviders: CompletableFuture<ContainerProviderCollection>)
		: UniverseContext<TConfig, TContainer>(logger, executor, config, container, globalProviders), IMultiverseContext<TConfig, TContainer>
	{
		override fun <TDependency> offerLocalProvider(dependency: Class<TDependency>, function: Function<IUniverseContext<*, *>, TDependency>)
		{
			this.localProviders[dependency] = function
		}
	}

	internal class PhysicalMultiverse<TConfig>(
		logger: Logger,
		executor: Executor,
		config: TConfig,
		container: IPhysicalMultiverseContainer,
		globalProviders: CompletableFuture<ContainerProviderCollection>)
		: Multiverse<TConfig, IPhysicalMultiverseContainer>(logger, executor, config, container, globalProviders), IPhysicalMultiverseContext<TConfig, IPhysicalMultiverseContainer>

	internal class VirtualMultiverse<TConfig>(
		logger: Logger,
		executor: Executor,
		config: TConfig,
		container: IVirtualMultiverseContainer,
		globalProviders: CompletableFuture<ContainerProviderCollection>)
		: Multiverse<TConfig, IVirtualMultiverseContainer>(logger, executor, config, container, globalProviders), IVirtualMultiverseContext<TConfig, IVirtualMultiverseContainer>
}
