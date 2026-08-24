package io.quut.omnivisor.sponge.universe.event.collection

import io.quut.omnivisor.api.universe.event.UniverseEventPriority
import io.quut.omnivisor.sponge.utils.CompletableFutureUtils
import io.quut.omnivisor.sponge.utils.thenComposeAsync
import java.util.SortedMap
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.Function

internal class ContainerEventHandler<T>(private val executor: Executor, private val listeners: Iterable<Function<T, CompletableFuture<Void>>>)
{
	internal fun forEach(event: T, wrapper: (CompletableFuture<Void>) -> CompletableFuture<Void>): CompletableFuture<Void>
	{
		val iterator: Iterator<Function<T, CompletableFuture<Void>>> = this.listeners.iterator()
		if (!iterator.hasNext())
		{
			return CompletableFuture.completedFuture(null)
		}

		val first: Function<T, CompletableFuture<Void>> = iterator.next()

		var future: CompletableFuture<Void> = CompletableFutureUtils.supplyAsync(this.executor) { wrapper(first.apply(event)) }.thenCompose { f -> f }
		while (iterator.hasNext())
		{
			val next: Function<T, CompletableFuture<Void>> = iterator.next()

			future = future.thenComposeAsync(this.executor) { wrapper(next.apply(event)) }
		}

		return future
	}

	internal class Builder<T>
	{
		private val listeners: SortedMap<UniverseEventPriority, MutableList<Function<T, CompletableFuture<Void>>>> = sortedMapOf()

		fun add(priority: UniverseEventPriority, function: Function<T, CompletableFuture<Void>>)
		{
			this.listeners.computeIfAbsent(priority) { _ -> mutableListOf() }.add(function)
		}

		fun build(executor: Executor): ContainerEventHandler<T> =
			ContainerEventHandler(executor, this.listeners.values.flatten())
	}
}
