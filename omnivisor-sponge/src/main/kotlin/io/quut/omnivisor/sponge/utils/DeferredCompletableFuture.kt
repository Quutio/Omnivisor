package io.quut.omnivisor.sponge.utils

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference

internal class DeferredCompletableFuture(private val startCallback: () -> CompletableFuture<Void>)
{
	private val future: AtomicReference<CompletableFuture<Void>> = AtomicReference()

	fun get(): CompletableFuture<Void> = this.future.get() ?: this.get0()

	private fun get0(): CompletableFuture<Void>
	{
		val future = CompletableFuture<Void>()

		this.future.compareAndExchange(null, future)?.let { return it }

		this.startCallback().whenComplete()
		{ value, throwable ->
			if (throwable == null)
			{
				future.complete(value)
			}
			else
			{
				future.completeExceptionally(throwable)
			}
		}

		return future
	}
}
