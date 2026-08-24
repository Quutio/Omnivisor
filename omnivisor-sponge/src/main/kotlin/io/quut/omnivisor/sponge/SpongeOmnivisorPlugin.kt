package io.quut.omnivisor.sponge

import com.google.inject.Inject
import io.quut.omnivisor.api.universe.IUniverse
import io.quut.omnivisor.api.universe.IUniverseArchetype
import io.quut.omnivisor.api.universe.IUniverseCluster
import io.quut.omnivisor.api.universe.IUniverseOptions
import io.quut.omnivisor.sponge.config.PluginConfig
import io.quut.omnivisor.sponge.config.UniverseConfig
import io.quut.omnivisor.sponge.listeners.IListener
import io.quut.omnivisor.sponge.options.NodeValueSource
import io.quut.omnivisor.sponge.universe.UniverseEventManager
import io.quut.omnivisor.sponge.universe.UniverseManager
import io.quut.omnivisor.sponge.utils.MainThreadExecutor
import io.quut.omnivisor.sponge.utils.Registries
import io.quut.omnivisor.sponge.world.WorldManager
import net.kyori.option.OptionState
import org.apache.logging.log4j.Logger
import org.spongepowered.api.ResourceKey
import org.spongepowered.api.event.EventManager
import org.spongepowered.api.registry.Registry
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.plugin.PluginContainer
import java.util.concurrent.CompletableFuture

internal class SpongeOmnivisorPlugin @Inject constructor(
	private val logger: Logger,
	private val container: PluginContainer,
	private val pluginConfig: SpongeOmnivisorPluginInfo,
	private val mainThreadExecutor: MainThreadExecutor,
	private val worldManager: WorldManager,
	private val universeManager: UniverseManager,
	private val eventManager: EventManager,
	private val universeEventManager: UniverseEventManager,
	private val listeners: Set<IListener>)
{
	private lateinit var config: PluginConfig

	internal fun load()
	{
		val node: CommentedConfigurationNode = this.pluginConfig.configLoader.load(this.pluginConfig.configLoader.defaultOptions().shouldCopyDefaults(true))

		this.config = node.require(PluginConfig::class.java)

		this.pluginConfig.configLoader.save(node)

		this.listeners.forEach { listener -> this.eventManager.registerListeners(this.container, listener, this.pluginConfig.lookup) }

		this.universeEventManager.init()

		this.worldManager.init(this.mainThreadExecutor)
		this.universeManager.init(this.mainThreadExecutor)
	}

	internal fun enable()
	{
		val registry: Registry<IUniverseArchetype<*, *>> = Registries.UNIVERSE_ARCHETYPE_REGISTRY.get()

		this.walkConfig(this.universeManager, registry, this.config.universes)
	}

	internal fun end()
	{
		this.mainThreadExecutor.poll(this.universeManager.close()::isDone)
	}

	private fun walkConfig(parent: IUniverseCluster, registry: Registry<IUniverseArchetype<*, *>>, universes: Map<ResourceKey, UniverseConfig>)
	{
		for ((id: ResourceKey, config: UniverseConfig) in universes)
		{
			this.logger.info("Creating config specified universe $id of type ${config.kind} in $parent.")

			this.create(parent, registry, id, config).thenApply()
			{ u ->
				if (config.children.isEmpty())
				{
					return@thenApply
				}

				if (u !is IUniverseCluster)
				{
					this.logger.warn("Universe $id was configured with children but it does not support them! Skipping...")

					return@thenApply
				}

				this.walkConfig(u, registry, config.children)
			}
		}
	}

	private fun create(parent: IUniverseCluster, registry: Registry<IUniverseArchetype<*, *>>, key: ResourceKey, config: UniverseConfig): CompletableFuture<out IUniverse<*>>
	{
		val archetype = registry.value<IUniverseArchetype<*, *>>(config.archetype)

		val state: OptionState = archetype.options
			.stateBuilder()
			.values(NodeValueSource(config.options))
			.build()

		return this.create(parent, key, config.kind, archetype, state)
	}

	private fun <T, C> create(parent: IUniverseCluster, key: ResourceKey, type: ResourceKey, archetype: IUniverseArchetype<T, C>, state: OptionState): CompletableFuture<out IUniverse<T>> =
		parent.create(key, IUniverseOptions.of(type, archetype, archetype.loadConfig(state)))
}
