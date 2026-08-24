package io.quut.omnivisor.sponge.multiverse

import io.quut.omnivisor.api.multiverse.IMultiverseLike
import io.quut.omnivisor.api.universe.IUniverse
import io.quut.omnivisor.api.universe.IUniverseOptions
import io.quut.omnivisor.sponge.universe.UniverseCollection
import io.quut.omnivisor.sponge.universe.UniverseLikeBase
import io.quut.omnivisor.sponge.universe.event.collection.ContainerEventCollection
import io.quut.omnivisor.sponge.universe.provider.ContainerProviderCollection
import io.quut.omnivisor.sponge.utils.MainThreadExecutor
import net.kyori.adventure.key.Key
import java.util.concurrent.CompletableFuture

internal abstract class MultiverseLikeBase(
	internal val providers: ContainerProviderCollection,
	events: ContainerEventCollection) : UniverseLikeBase(events), IMultiverseLike
{
	protected abstract val universes: UniverseCollection
	protected abstract val mainThreadExecutor: MainThreadExecutor

	override fun child(key: Key): IUniverse<*>? = this.universes[key]

	fun <TInstance, TConfig> create(key: Key, options: IUniverseOptions<TInstance, TConfig>): CompletableFuture<out IUniverse<TInstance>> =
		this.universes.create(key, options)

	override fun tick()
	{
		this.universes.values.forEach { h -> h.instance?.tick() }
	}

	override fun close(): CompletableFuture<Void> = this.universes.close()
}
