package io.quut.omnivisor.api.universe

import java.util.concurrent.CompletableFuture

interface IUniverseContainer
{
	val info: IUniverseInfo

	fun close(): CompletableFuture<Void>
}
