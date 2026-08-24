package io.quut.omnivisor.api.world

import net.kyori.adventure.key.Key
import java.util.concurrent.CompletableFuture
import java.util.function.Function

interface IWorldManager
{
	fun <T> createTransientWorld(namespace: String, function: Function<Key, CompletableFuture<T>>, prefix: String? = null, suffix: String? = null): CompletableFuture<IWorldContainer<T>>
}
