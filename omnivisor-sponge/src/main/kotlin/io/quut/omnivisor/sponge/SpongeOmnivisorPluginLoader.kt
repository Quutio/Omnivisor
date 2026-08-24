package io.quut.omnivisor.sponge

import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.PrivateModule
import com.google.inject.Scopes
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.Names
import io.quut.omnivisor.api.universe.IUniverseArchetype
import io.quut.omnivisor.api.universe.IUniverseManager
import io.quut.omnivisor.api.world.IWorldManager
import io.quut.omnivisor.sponge.listeners.ConnectionListener
import io.quut.omnivisor.sponge.listeners.IListener
import io.quut.omnivisor.sponge.universe.UniverseEventManager
import io.quut.omnivisor.sponge.universe.UniverseManager
import io.quut.omnivisor.sponge.user.SpongeUserManager
import io.quut.omnivisor.sponge.utils.Const
import io.quut.omnivisor.sponge.utils.MainThreadExecutor
import io.quut.omnivisor.sponge.utils.Registries
import io.quut.omnivisor.sponge.utils.typeLiteral
import io.quut.omnivisor.sponge.world.TransientWorldManager
import io.quut.omnivisor.sponge.world.WorldManager
import org.spongepowered.api.Server
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.lifecycle.RegisterChannelEvent
import org.spongepowered.api.event.lifecycle.RegisterRegistryEvent
import org.spongepowered.api.event.lifecycle.StartedEngineEvent
import org.spongepowered.api.event.lifecycle.StartingEngineEvent
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent
import org.spongepowered.api.network.channel.raw.RawDataChannel
import org.spongepowered.api.registry.DefaultedRegistryType
import org.spongepowered.api.scheduler.TaskExecutorService
import org.spongepowered.plugin.PluginContainer
import org.spongepowered.plugin.builtin.jvm.Plugin

@Plugin(Const.NAMESPACE)
class SpongeOmnivisorPluginLoader @Inject internal constructor(
	private val injector: Injector,
	private val container: PluginContainer,
	private val universeArchetypeRegistry: DefaultedRegistryType<IUniverseArchetype<*, *>>)
{
	lateinit var handshakeChannel: RawDataChannel

	private var plugin: SpongeOmnivisorPlugin? = null

	@Listener
	private fun registerRegistryServer(event: RegisterRegistryEvent.EngineScoped<Server>)
	{
		event.register<IUniverseArchetype<*, *>>(this.universeArchetypeRegistry.location(), true)
	}

	@Listener
	private fun onRegisterChannel(event: RegisterChannelEvent)
	{
		this.handshakeChannel = event.register(Const.HANDSHAKE_CHANNEL_KEY, RawDataChannel::class.java)
	}

	@Listener
	private fun onStartingEngineServer(event: StartingEngineEvent<Server>)
	{
		val injector: Injector = this.injector.createChildInjector(ServerModule(this.container, event.engine(), this.handshakeChannel))

		val plugin: SpongeOmnivisorPlugin = injector.getInstance(SpongeOmnivisorPlugin::class.java)
		plugin.load()

		this.plugin = plugin
	}

	@Listener
	private fun onStartedEngineServer(event: StartedEngineEvent<Server>)
	{
		this.plugin?.enable()
	}

	@Listener
	private fun onStoppingEngineServer(event: StoppingEngineEvent<Server>)
	{
		this.plugin?.end()
		this.plugin = null
	}

	class Module : PrivateModule()
	{
		override fun configure()
		{
			this.bind(SpongeOmnivisorPluginInfo::class.java).`in`(Scopes.SINGLETON)
			this.bind(UniverseManager::class.java).`in`(Scopes.SINGLETON)
			this.bind(SpongeUserManager::class.java).`in`(Scopes.SINGLETON)
			this.bind(WorldManager::class.java).`in`(Scopes.SINGLETON)
			this.bind(UniverseEventManager::class.java).`in`(Scopes.SINGLETON)
			this.bind(TransientWorldManager::class.java).`in`(Scopes.SINGLETON)

			// Public
			this.bind(IUniverseManager::class.java).to(UniverseManager::class.java)
			this.bind(IWorldManager::class.java).to(WorldManager::class.java)

			this.bind(typeLiteral<DefaultedRegistryType<IUniverseArchetype<*, *>>>()).toInstance(Registries.UNIVERSE_ARCHETYPE_REGISTRY)

			this.expose(IUniverseManager::class.java)
			this.expose(IWorldManager::class.java)

			this.expose(typeLiteral<DefaultedRegistryType<IUniverseArchetype<*, *>>>())
		}
	}

	private class ServerModule(private val container: PluginContainer, private val server: Server, private val handshakeChannel: RawDataChannel) : AbstractModule()
	{
		override fun configure()
		{
			this.bind(SpongeOmnivisorPlugin::class.java).`in`(Scopes.SINGLETON)

			val executor: TaskExecutorService = this.server.scheduler().executor(this.container)

			this.bind(TaskExecutorService::class.java).toInstance(executor)
			this.bind(MainThreadExecutor::class.java).toInstance(MainThreadExecutor(this.server, executor))

			this.bind(RawDataChannel::class.java)
				.annotatedWith(Names.named(Const.HANDSHAKE_CHANNEL))
				.toInstance(this.handshakeChannel)

			val listeners: Multibinder<IListener> = Multibinder.newSetBinder(this.binder(), IListener::class.java)
			listeners.addBinding().to(ConnectionListener::class.java)
		}
	}
}
