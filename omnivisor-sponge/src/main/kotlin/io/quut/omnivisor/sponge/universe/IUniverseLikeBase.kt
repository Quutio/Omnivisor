package io.quut.omnivisor.sponge.universe

import io.quut.omnivisor.api.universe.IUniverseLike
import io.quut.omnivisor.sponge.universe.event.UniverseTickEvent
import java.util.concurrent.CompletableFuture

internal interface IUniverseLikeBase : IUniverseLike
{
	fun fireEvent(event: Any): CompletableFuture<Void>
	fun fireEventCatching(event: Any): CompletableFuture<Void>

	fun tick()
	{
		this.fireEvent(UniverseTickEvent)
	}

	fun close(): CompletableFuture<Void> = CompletableFuture.completedFuture(null)
}
