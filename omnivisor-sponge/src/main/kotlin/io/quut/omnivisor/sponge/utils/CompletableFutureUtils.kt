package io.quut.omnivisor.sponge.utils

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.Executor

internal object CompletableFutureUtils
{
	internal fun <T> supplyAsync(executor: Executor, supplier: () -> T): CompletableFuture<T> = CompletableFuture.supplyAsync(supplier, executor)

	internal fun unwrapCompletionExceptionMatch(e: Throwable, vararg matches: Class<out Exception>): Boolean =
		(e is CompletionException && matches.any { c -> c.isInstance(e.cause) }) || matches.any { c -> c.isInstance(e) }
}
