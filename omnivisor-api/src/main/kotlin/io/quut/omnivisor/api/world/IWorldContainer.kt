package io.quut.omnivisor.api.world

import net.kyori.adventure.key.Key
import java.util.concurrent.CompletableFuture

interface IWorldContainer<T>
{
	val key: Key
	val instance: T

	fun close(): CompletableFuture<Void>
}
