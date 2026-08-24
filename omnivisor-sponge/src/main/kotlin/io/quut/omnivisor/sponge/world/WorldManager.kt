package io.quut.omnivisor.sponge.world

import com.google.inject.Inject
import io.quut.omnivisor.api.world.IWorldContainer
import io.quut.omnivisor.api.world.IWorldManager
import io.quut.omnivisor.sponge.utils.MainThreadExecutor
import net.kyori.adventure.key.Key
import java.util.concurrent.CompletableFuture
import java.util.function.Function

internal class WorldManager @Inject constructor(private val transient: TransientWorldManager) : IWorldManager
{
	fun init(mainThreadExecutor: MainThreadExecutor)
	{
		this.transient.init(mainThreadExecutor)
	}

	override fun <T> createTransientWorld(namespace: String, function: Function<Key, CompletableFuture<T>>, prefix: String?, suffix: String?): CompletableFuture<IWorldContainer<T>> =
		this.transient.create(namespace, function, prefix, suffix)
}
