package io.quut.omnivisor.sponge.universe

import io.quut.omnivisor.api.universe.IUniverseContainer
import java.util.concurrent.CompletableFuture

internal abstract class UniverseHolder<T>
{
	abstract val instance: IUniverseBase<T>?

	internal abstract fun start(universe: IUniverseBase<T>)
	internal abstract fun stop(container: IUniverseContainer, eventSink: (Any) -> CompletableFuture<Void>, throwable: Throwable?): CompletableFuture<Void>
	internal abstract fun close(): CompletableFuture<Void>
}
