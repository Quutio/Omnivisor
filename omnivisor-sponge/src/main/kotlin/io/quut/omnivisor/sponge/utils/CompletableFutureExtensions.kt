package io.quut.omnivisor.sponge.utils

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor
import java.util.function.Function

inline fun <T> CompletableFuture<T>.thenAcceptAsync(executor: Executor, crossinline action: (T) -> Unit): CompletableFuture<Void> =
	this.thenAcceptAsync({ v -> action(v) }, executor)

inline fun <T, U> CompletableFuture<T>.thenApplyAsync(executor: Executor, crossinline action: (T) -> U): CompletableFuture<U> =
	this.thenApplyAsync({ v -> action(v) }, executor)

inline fun <T, U> CompletableFuture<T>.thenComposeAsync(executor: Executor, crossinline target: (T) -> CompletionStage<U>): CompletableFuture<U> =
	this.thenComposeAsync({ value -> target(value) }, executor)

inline fun <T> CompletableFuture<T>.exceptionallyComposeAsync(executor: Executor, crossinline target: (Throwable) -> CompletionStage<T>): CompletableFuture<T> =
	this.exceptionallyComposeAsync({ throwable -> target(throwable) }, executor)

inline fun CompletableFuture<Void>.exceptionallyVoid(crossinline action: (Throwable) -> Unit): CompletableFuture<Void> =
	this.exceptionally()
	{ throwable ->
		action(throwable)

		return@exceptionally null
	}

inline fun CompletableFuture<Void>.exceptionallyAsyncVoid(executor: Executor, crossinline action: (Throwable) -> Unit): CompletableFuture<Void> =
	this.exceptionallyAsync(
	{ throwable ->
		action(throwable)

		return@exceptionallyAsync null
	}, executor)

inline fun <T> CompletableFuture<T>.whenException(crossinline action: (Throwable) -> Unit): CompletableFuture<T> =
	this.whenComplete()
	{ _, throwable ->
		if (throwable != null)
		{
			action(throwable)
		}
	}

inline fun <T> CompletableFuture<T>.whenExceptionallyCompose(crossinline action: (Throwable) -> CompletableFuture<Void>): CompletableFuture<T> =
	this.exceptionallyCompose { throwable ->
		action(throwable)
		.whenCompleteCompose { _, t -> CompletableFuture.failedFuture(throwable.apply { t?.let(this::addSuppressed) }) }
	}

inline fun <T> CompletableFuture<T>.whenExceptionallyComposeAsync(executor: Executor, crossinline action: (Throwable) -> CompletableFuture<Void>): CompletableFuture<T> =
	this.exceptionallyComposeAsync(executor) { throwable ->
		action(throwable)
		.whenCompleteCompose { _, t -> CompletableFuture.failedFuture(throwable.apply { t?.let(this::addSuppressed) }) }
	}

inline fun <T, U> CompletableFuture<T>.whenCompleteCompose(crossinline action: (T?, Throwable?) -> CompletableFuture<U>): CompletableFuture<U> =
	this.handle { value, throwable -> action(value, throwable) }.thenCompose(Function.identity())

inline fun <T> CompletableFuture<T>.whenSuccess(crossinline target: (T) -> Unit): CompletableFuture<T> =
	this.thenApply()
	{ v ->
		target(v)

		return@thenApply v
	}

inline fun <T> CompletableFuture<T>.whenSuccessAsync(executor: Executor, crossinline target: (T) -> Unit): CompletableFuture<T> =
	this.thenApplyAsync(
	{ v ->
		target(v)

		return@thenApplyAsync v
	}, executor)
