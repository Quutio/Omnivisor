package io.quut.omnivisor.sponge.universe

import com.google.inject.Inject
import io.quut.omnivisor.api.universe.IUniverse
import io.quut.omnivisor.api.universe.IUniverseContainer
import io.quut.omnivisor.api.universe.IUniverseContext
import io.quut.omnivisor.api.universe.IUniverseInfo
import io.quut.omnivisor.api.universe.IUniverseLike
import io.quut.omnivisor.api.universe.IUniverseManager
import io.quut.omnivisor.api.universe.event.IUniverseStoppedEvent
import io.quut.omnivisor.api.universe.event.UniverseEventPriority
import io.quut.omnivisor.api.universe.physical.IPhysicalUniverseContext
import io.quut.omnivisor.api.world.IWorldManager
import io.quut.omnivisor.sponge.multiverse.MultiverseLikeBase
import io.quut.omnivisor.sponge.universe.event.collection.ContainerEventCollection
import io.quut.omnivisor.sponge.universe.provider.ContainerProviderCollection
import io.quut.omnivisor.sponge.utils.MainThreadExecutor
import io.quut.omnivisor.sponge.world.ContainerWorldManager
import io.quut.omnivisor.sponge.world.WorldManager
import org.apache.logging.log4j.Logger
import org.spongepowered.api.Game
import org.spongepowered.api.scheduler.Task
import org.spongepowered.api.util.Ticks
import org.spongepowered.plugin.PluginContainer
import java.util.concurrent.CompletableFuture
import java.util.function.Function

internal class UniverseManager @Inject constructor(
	private val logger: Logger,
	private val pluginContainer: PluginContainer,
	private val eventManager: UniverseEventManager,
	worldManager: WorldManager,
	private val game: Game): MultiverseLikeBase(this.buildProviders(worldManager), ContainerEventCollection(logger, mapOf())), IUniverseManager
{
	override val universes: UniverseCollection.Root = UniverseCollection.Root(object : IUniverseClusterBase
	{
		override fun <TInstance, TConfig> createFactory(info: IUniverseInfo, providers: CompletableFuture<ContainerProviderCollection>, holder: UniverseHolder<TInstance>): UniverseFactory<TInstance, TConfig> =
		UniverseFactory(this@UniverseManager.logger, this@UniverseManager.mainThreadExecutor, this@UniverseManager.universes, this@UniverseManager.eventManager, providers, info, CompletableFuture.completedFuture(null), holder)

		override fun unregister(container: IUniverseContainer)
		{
			this@UniverseManager.eventManager.unregisterListeners(container)
		}
	}, CompletableFuture.completedFuture(this.providers))

	override lateinit var mainThreadExecutor: MainThreadExecutor

	override val parent: IUniverseLike?
		get() = null

	internal fun init(mainThreadExecutor: MainThreadExecutor)
	{
		this.mainThreadExecutor = mainThreadExecutor

		this.game.server().scheduler().submit(Task.builder()
			.plugin(this.pluginContainer)
			.interval(Ticks.single())
			.execute(this::tick)
			.build())
	}

	override fun universe(id: Int): IUniverse<*>? = this.universes[id]

	internal fun fallback(): UniverseFallbackResult =
		UniverseFallbackResult(null, false)

	companion object
	{
		private fun buildProviders(worldManager: WorldManager): ContainerProviderCollection
		{
			fun worldManager(): Function<IUniverseContext<*, *>, *> =
				{ context ->
					if (context is IPhysicalUniverseContext<*, *>)
					{
						ContainerWorldManager(context.container, worldManager)
							.apply { context.event(IUniverseStoppedEvent::class.java, UniverseEventPriority.DEFAULT) { _ -> this.close() } }
					}
					else
					{
						null
					}
				}

			return ContainerProviderCollection(buildMap()
			{
				this[IWorldManager::class.java] = worldManager()
			})
		}
	}
}
