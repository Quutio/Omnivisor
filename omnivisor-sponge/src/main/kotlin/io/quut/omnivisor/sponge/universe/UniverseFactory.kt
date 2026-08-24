package io.quut.omnivisor.sponge.universe

import io.quut.omnivisor.api.multiverse.IMultiverse
import io.quut.omnivisor.api.multiverse.physical.IPhysicalMultiverseContainer
import io.quut.omnivisor.api.multiverse.physical.IPhysicalMultiverseContext
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverse
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverseContainer
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverseContext
import io.quut.omnivisor.api.universe.IUniverse
import io.quut.omnivisor.api.universe.IUniverseFactory
import io.quut.omnivisor.api.universe.IUniverseInfo
import io.quut.omnivisor.api.universe.IUniverseLike
import io.quut.omnivisor.api.universe.physical.IPhysicalUniverseContainer
import io.quut.omnivisor.api.universe.physical.IPhysicalUniverseContext
import io.quut.omnivisor.sponge.multiverse.PhysicalMultiverse
import io.quut.omnivisor.sponge.multiverse.VirtualMultiverse
import io.quut.omnivisor.sponge.universe.event.collection.ContainerEventCollection
import io.quut.omnivisor.sponge.universe.provider.ContainerProviderCollection
import io.quut.omnivisor.sponge.utils.CompletableFutureUtils
import io.quut.omnivisor.sponge.utils.MainThreadExecutor
import io.quut.omnivisor.sponge.utils.whenException
import io.quut.omnivisor.sponge.utils.whenExceptionallyComposeAsync
import io.quut.omnivisor.sponge.utils.whenSuccessAsync
import org.apache.logging.log4j.Logger
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture

internal class UniverseFactory<TInstance, TConfig>(
	private val logger: Logger,
	private val executor: MainThreadExecutor,
	private val universes: UniverseCollection,
	private val eventManager: UniverseEventManager,
	private val providers: CompletableFuture<ContainerProviderCollection>,
	private val info: IUniverseInfo,
	private val parent: CompletableFuture<IUniverseLike?>,
	private val holder: UniverseHolder<TInstance>) : IUniverseFactory<TInstance, TConfig>
{
	override fun createUniverse(config: TConfig, function: IUniverseFactory.ICreateFunction<TInstance, IUniverse<TInstance>, IPhysicalUniverseContext<TConfig, IPhysicalUniverseContainer>>): CompletableFuture<out IUniverse<TInstance>>
	{
		val container = UniverseContainer.Universe(this.info, this.eventManager, this.holder::close)
		val context = UniverseContext.Universe(this.logger, this.executor, config, container, this.providers)

		val future: CompletableFuture<Universe<TInstance>> = function.create(context)
			{ instance -> this.parent.thenApply { parent -> Universe(container, instance, parent, context) } }

		return this.initialize(future, container, context)
	}

	override fun createMultiverse(node: Boolean, config: TConfig, function: IUniverseFactory.ICreateFunction<TInstance, IMultiverse<TInstance>, IPhysicalMultiverseContext<TConfig, IPhysicalMultiverseContainer>>): CompletableFuture<out IMultiverse<TInstance>>
	{
		val self: CompletableFuture<PhysicalMultiverse<TInstance>> = CompletableFuture()
		val universes: UniverseCollection = this.universes.child(self.thenApply{ self -> self.providers })
		val container = UniverseContainer.Multiverse(info, this.eventManager, universes, this.holder::close)
		val context = UniverseContext.PhysicalMultiverse(this.logger, this.executor, config, container, this.providers)

		val future: CompletableFuture<PhysicalMultiverse<TInstance>> = function.create(context)
		{ instance ->
			this.parent.thenCompose()
			{ parent ->
				self.also()
				{ self ->
					if (!node)
					{
						self.complete(PhysicalMultiverse(universes, this.executor, container, instance, parent, context))
					}
					else
					{
						self.complete(PhysicalMultiverse.Node(universes, this.executor, container, instance, parent, context))
					}
				}
			}
		}.whenException { e -> self.completeExceptionally(e) }

		return this.initialize(future, container, context)
	}

	override fun createVirtualMultiverse(node: Boolean, config: TConfig, function: IUniverseFactory.ICreateFunction<TInstance, IVirtualMultiverse<TInstance>, IVirtualMultiverseContext<TConfig, IVirtualMultiverseContainer>>): CompletableFuture<out IVirtualMultiverse<TInstance>>
	{
		val self: CompletableFuture<VirtualMultiverse<TInstance>> = CompletableFuture()
		val universes: UniverseCollection = this.universes.child(self.thenApply{ self -> self.providers })
		val container = UniverseContainer.VirtualMultiverse(info, universes, this.holder::close)
		val context = UniverseContext.VirtualMultiverse(this.logger, this.executor, config, container, this.providers)

		val future: CompletableFuture<VirtualMultiverse<TInstance>> = function.create(context)
		{ instance ->
			this.parent.thenCompose()
			{ parent ->
				self.also()
				{ self ->
					if (!node)
					{
						self.complete(VirtualMultiverse(universes, this.executor, container, instance, parent, context))
					}
					else
					{
						self.complete(VirtualMultiverse.Node(universes, this.executor, container, instance, parent, context))
					}
				}
			}
		}

		return this.initialize(future, container, context)
	}

	private fun <T : IUniverseBase<TInstance>> initialize(intermediaryFuture: CompletableFuture<T>, container: UniverseContainer, context: UniverseContext<*, *>, universes: UniverseCollection? = null): CompletableFuture<out T> =
		intermediaryFuture.whenSuccessAsync(this.executor) { universe -> this.holder.start(universe) }
			.whenExceptionallyComposeAsync(this.executor)
			{ e ->
				if (!CompletableFutureUtils.unwrapCompletionExceptionMatch(e, CancellationException::class.java))
				{
					this.logger.error("Failed to create a new universe {}!", this.info.key, e)
				}

				val eventManager: ContainerEventCollection = context.collectEvents()

				return@whenExceptionallyComposeAsync (universes?.close() ?: CompletableFuture.completedFuture(null))
					.thenCompose { this.holder.stop(container, eventManager::fireEventCatching, e) }
			}
}
