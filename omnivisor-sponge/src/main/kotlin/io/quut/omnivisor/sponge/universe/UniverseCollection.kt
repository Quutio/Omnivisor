package io.quut.omnivisor.sponge.universe

import io.quut.omnivisor.api.universe.IUniverse
import io.quut.omnivisor.api.universe.IUniverseContainer
import io.quut.omnivisor.api.universe.IUniverseInfo
import io.quut.omnivisor.api.universe.IUniverseOptions
import io.quut.omnivisor.api.universe.event.IUniverseKilledEvent
import io.quut.omnivisor.sponge.universe.event.UniverseKilledEvent
import io.quut.omnivisor.sponge.universe.event.UniverseStartedEvent
import io.quut.omnivisor.sponge.universe.event.UniverseStartingEvent
import io.quut.omnivisor.sponge.universe.event.UniverseStoppedEvent
import io.quut.omnivisor.sponge.universe.event.UniverseStoppingEvent
import io.quut.omnivisor.sponge.universe.provider.ContainerProviderCollection
import io.quut.omnivisor.sponge.utils.DeferredCompletableFuture
import net.kyori.adventure.key.Key
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

internal abstract class UniverseCollection(protected val cluster: IUniverseClusterBase, private val providers: CompletableFuture<ContainerProviderCollection>)
{
	private val universes: ConcurrentMap<Key, UniverseHolder<*>> = ConcurrentHashMap()

	private val closedLock: ReentrantReadWriteLock = ReentrantReadWriteLock()

	@Volatile
	private var closed: Boolean = false

	internal val values: Collection<UniverseHolder<*>>
		get() = this.universes.values

	operator fun get(key: Key): IUniverse<*>? = this.universes[key]?.instance

	protected abstract fun universeInfo(key: Key, kind: Key): IUniverseInfo

	protected open fun registerUniverse(info: IUniverseInfo, holder: UniverseHolder<*>)
	{
		if (this.universes.putIfAbsent(info.key, holder) != null)
		{
			throw IllegalArgumentException("Already exists")
		}
	}

	protected open fun unregisterUniverse(info: IUniverseInfo, holder: UniverseHolder<*>)
	{
		this.universes.remove(info.key, holder)
	}

	internal fun <TInstance, TConfig> create(key: Key, options: IUniverseOptions<TInstance, TConfig>): CompletableFuture<out IUniverse<TInstance>>
	{
		if (this.closed)
		{
			return CompletableFuture.failedFuture(IllegalStateException("Closed"))
		}

		val info: IUniverseInfo = this.universeInfo(key, options.kind)
		val future = CompletableFuture<IUniverseBase<TInstance>>()
		val holder = Holder(info, future)

		this.closedLock.read()
		{
			if (this.closed)
			{
				return CompletableFuture.failedFuture(IllegalStateException("Closed"))
			}

			this.registerUniverse(info, holder)
		}

		try
		{
			return options.archetype.create(options.config, this.cluster.createFactory(info, this.providers, holder))
		}
		catch (e: Throwable)
		{
			this.unregisterUniverse(info, holder)

			return CompletableFuture.failedFuture(e)
		}
	}

	private fun remove(info: IUniverseInfo, holder: Holder<*>, removedCallback: (IUniverseKilledEvent) -> CompletableFuture<Void>): CompletableFuture<Void>
	{
		this.unregisterUniverse(info, holder)

		if (!this.closed)
		{
			return removedCallback(UniverseKilledEvent.TEARDOWN_FALSE)
		}

		return removedCallback(UniverseKilledEvent.TEARDOWN_TRUE)
	}

	internal fun close(): CompletableFuture<Void>
	{
		this.closedLock.write { this.closed = true }

		return CompletableFuture.allOf(*this.universes.values.map()
		{ h ->
			if (h.instance != null)
			{
				return@map h.close()
			}
			else
			{
				// Avoid getting indefinitely stuck due to starting instances
				// getting stuck due shutdown.
				return@map h.close().orTimeout(15, TimeUnit.SECONDS)
			}
		}.toTypedArray())
	}

	internal abstract fun child(providers: CompletableFuture<ContainerProviderCollection>): UniverseCollection

	private inner class Holder<T>(private val info: IUniverseInfo, private val future: CompletableFuture<IUniverseBase<T>>) : UniverseHolder<T>()
	{
		private val closeFuture: DeferredCompletableFuture = DeferredCompletableFuture { this.future.thenCompose(this::close0) }

		override val instance: IUniverseBase<T>?
			get() = this.future.getNow(null)

		override fun start(universe: IUniverseBase<T>)
		{
			universe.fireEvent(UniverseStartingEvent)

			this.future.complete(universe)

			universe.fireEvent(UniverseStartedEvent)
		}

		override fun stop(container: IUniverseContainer, eventSink: (Any) -> CompletableFuture<Void>, throwable: Throwable?): CompletableFuture<Void>
		{
			var future: CompletableFuture<Void> = eventSink(UniverseStoppingEvent)
				.thenCompose { eventSink(UniverseStoppedEvent) }
				.thenCompose { this@UniverseCollection.remove(this.info, this) { e -> eventSink(e) } }
				.whenComplete { _, _ -> this@UniverseCollection.cluster.unregister(container) }

			if (throwable != null)
			{
				future = future.whenComplete { _, e -> this.future.completeExceptionally(throwable.apply { e?.let(this::addSuppressed) }) }
			}

			return future
		}

		override fun close(): CompletableFuture<Void> = this.closeFuture.get()

		private fun close0(universe: IUniverseBase<T>): CompletableFuture<Void> =
			universe.close().thenCompose { this.stop(universe.container, universe::fireEventCatching, null) }
	}

	internal class Root(cluster: IUniverseClusterBase, providers: CompletableFuture<ContainerProviderCollection>) : UniverseCollection(cluster, providers)
	{
		private val universes: ConcurrentMap<Int, UniverseHolder<*>> = ConcurrentHashMap()
		private val universeIdCounter: AtomicInteger = AtomicInteger()

		operator fun get(id: Int): IUniverse<*>? = this.universes[id]?.instance

		override fun universeInfo(key: Key, kind: Key): IUniverseInfo =
			UniverseInfo(this.universeIdCounter.incrementAndGet(), key, kind)

		override fun registerUniverse(info: IUniverseInfo, holder: UniverseHolder<*>)
		{
			super.registerUniverse(info, holder)

			this.universes[info.id] = holder
		}

		override fun unregisterUniverse(info: IUniverseInfo, holder: UniverseHolder<*>)
		{
			super.unregisterUniverse(info, holder)

			this.universes.remove(info.id, holder)
		}

		override fun child(providers: CompletableFuture<ContainerProviderCollection>): UniverseCollection = Child(providers)

		private inner class Child(providers: CompletableFuture<ContainerProviderCollection>) : UniverseCollection(this@Root.cluster, providers)
		{
			override fun universeInfo(key: Key, kind: Key): IUniverseInfo =
				this@Root.universeInfo(key, kind)

			override fun registerUniverse(info: IUniverseInfo, holder: UniverseHolder<*>)
			{
				super.registerUniverse(info, holder)

				this@Root.universes[info.id] = holder
			}

			override fun unregisterUniverse(info: IUniverseInfo, holder: UniverseHolder<*>)
			{
				super.unregisterUniverse(info, holder)

				this@Root.universes.remove(info.id, holder)
			}

			override fun child(providers: CompletableFuture<ContainerProviderCollection>): Child = Child(providers)
		}
	}
}
