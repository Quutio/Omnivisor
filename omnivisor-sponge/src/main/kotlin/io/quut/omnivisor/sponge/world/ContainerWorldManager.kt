package io.quut.omnivisor.sponge.world

import io.quut.omnivisor.api.domain.IUniverseDomain
import io.quut.omnivisor.api.universe.physical.IPhysicalUniverseContainer
import io.quut.omnivisor.api.world.IWorldContainer
import io.quut.omnivisor.api.world.IWorldManager
import io.quut.omnivisor.sponge.utils.DeferredCompletableFuture
import io.quut.omnivisor.sponge.utils.whenException
import net.kyori.adventure.key.Key
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Function

internal class ContainerWorldManager(private val container: IPhysicalUniverseContainer, private val worldManager: WorldManager) : IWorldManager
{
	private val prefix: String = java.lang.Long.toUnsignedString(ThreadLocalRandom.current().nextLong())

	private val worlds: MutableSet<CompletableFuture<out IWorldContainer<*>>> = hashSetOf()

	@Volatile
	private var closed: Boolean = false

	private fun createPrefix(prefix: String?): String =
		"${this.prefix}.${this.container.info.key.namespace()}.${this.container.info.key.value()}.${prefix ?: 'w'}"

	override fun <T> createTransientWorld(namespace: String, function: Function<Key, CompletableFuture<T>>, prefix: String?, suffix: String?): CompletableFuture<IWorldContainer<T>>
	{
		fun loadWorld(key: Key): CompletableFuture<T>
		{
			val area: IUniverseDomain = IUniverseDomain.world(key)

			this.container.registerArea(area)

			try
			{
				return function.apply(key).whenException { this.container.unregisterArea(area) }
			}
			catch (e: Throwable)
			{
				this.container.unregisterArea(area)

				throw e
			}
		}

		synchronized(this.worlds)
		{
			if (this.closed)
			{
				return CompletableFuture.failedFuture(IllegalStateException("Closed"))
			}

			val future = CompletableFuture<IWorldContainer<T>>()

			this.worlds.add(future)

			try
			{
				this.worldManager.createTransientWorld(namespace, ::loadWorld, this.createPrefix(prefix), suffix)
					.thenApply<IWorldContainer<T>> { c -> ContainerWorldContainer(future, c) }
					.whenComplete()
					{ container, throwable ->
						if (throwable == null)
						{
							future.complete(container)
						}
						else
						{
							this.worlds.remove(future)

							future.completeExceptionally(throwable)
						}
					}

				return future
			}
			catch (e: Throwable)
			{
				this.worlds.remove(future)

				throw e
			}
		}
	}

	internal fun close(): CompletableFuture<Void>
	{
		synchronized(this.worlds)
		{
			this.closed = true

			return CompletableFuture.allOf(*this.worlds.map { f -> f.thenCompose { c -> c.close() } }.toTypedArray())
		}
	}

	private inner class ContainerWorldContainer<T>(private val future: CompletableFuture<IWorldContainer<T>>, private val container: IWorldContainer<T>) : IWorldContainer<T>
	{
		private val closeFuture: DeferredCompletableFuture = DeferredCompletableFuture(this::close0)

		override val key: Key
			get() = this.container.key

		override val instance: T
			get() = this.container.instance

		override fun close(): CompletableFuture<Void> = this.closeFuture.get()

		private fun close0(): CompletableFuture<Void> =
			this.container.close().whenComplete()
			{ _, _ ->
				synchronized(this@ContainerWorldManager.worlds)
				{
					this@ContainerWorldManager.worlds.remove(this.future)
				}

				this@ContainerWorldManager.container.unregisterArea(IUniverseDomain.world(this.key))
			}
	}
}
