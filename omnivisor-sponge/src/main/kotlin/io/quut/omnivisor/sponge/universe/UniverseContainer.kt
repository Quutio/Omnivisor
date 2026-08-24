package io.quut.omnivisor.sponge.universe

import io.quut.omnivisor.api.domain.IUniverseDomain
import io.quut.omnivisor.api.multiverse.physical.IPhysicalMultiverseContainer
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverseContainer
import io.quut.omnivisor.api.universe.IUniverse
import io.quut.omnivisor.api.universe.IUniverseContainer
import io.quut.omnivisor.api.universe.IUniverseInfo
import io.quut.omnivisor.api.universe.IUniverseOptions
import io.quut.omnivisor.api.universe.physical.IPhysicalUniverseContainer
import net.kyori.adventure.key.Key
import org.spongepowered.plugin.PluginContainer
import java.lang.invoke.MethodHandles
import java.util.concurrent.CompletableFuture

internal abstract class UniverseContainer(
	override val info: IUniverseInfo,
	private val closeCallback: () -> CompletableFuture<Void>) : IUniverseContainer
{
	override fun close(): CompletableFuture<Void> = this.closeCallback()

	abstract class Physical(
		private val eventManager: UniverseEventManager,
		info: IUniverseInfo,
		closeCallback: () -> CompletableFuture<Void>) : UniverseContainer(info, closeCallback), IPhysicalUniverseContainer
	{
		override fun registerListeners(plugin: Any, listener: Any, lookup: MethodHandles.Lookup?)
		{
			this.eventManager.registerListeners(this, plugin as PluginContainer, listener, lookup)
		}

		override fun registerArea(area: IUniverseDomain)
		{
			this.eventManager.registerArea(this, area)
		}

		override fun unregisterListeners(plugin: Any)
		{
			this.eventManager.unregisterListeners(this, plugin as PluginContainer)
		}

		override fun unregisterListeners(plugin: Any, listener: Any)
		{
			this.eventManager.unregisterListeners(this, plugin as PluginContainer, listener)
		}

		override fun unregisterArea(area: IUniverseDomain)
		{
			this.eventManager.unregisterArea(this, area)
		}
	}

	class Universe(info: IUniverseInfo, eventManager: UniverseEventManager, closeCallback: () -> CompletableFuture<Void>)
		: Physical(eventManager, info, closeCallback)

	class Multiverse(info: IUniverseInfo, eventManager: UniverseEventManager, val universeCollection: UniverseCollection, closeCallback: () -> CompletableFuture<Void>)
		: Physical(eventManager, info, closeCallback), IPhysicalMultiverseContainer
	{
		override fun <TInstance, TConfig> create(key: Key, options: IUniverseOptions<TInstance, TConfig>): CompletableFuture<out IUniverse<TInstance>> =
			this.universeCollection.create(key, options)
	}

	class VirtualMultiverse(info: IUniverseInfo, val universeCollection: UniverseCollection, closeCallback: () -> CompletableFuture<Void>) : UniverseContainer(info, closeCallback), IVirtualMultiverseContainer
	{
		override fun <TInstance, TConfig> create(key: Key, options: IUniverseOptions<TInstance, TConfig>): CompletableFuture<out IUniverse<TInstance>> =
			this.universeCollection.create(key, options)
	}
}
