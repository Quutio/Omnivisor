package io.quut.omnivisor.api.universe

import io.quut.omnivisor.api.universe.event.UniverseEventPriority
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.Function

interface IUniverseContext<TConfig, TContainer : IUniverseContainer>
{
	val config: TConfig
	val container: TContainer
	val executor: Executor

	fun <TDependency> globalProvider(dependency: Class<TDependency>): CompletableFuture<TDependency>

	fun <TEvent> event(event: Class<TEvent>, priority: UniverseEventPriority, function: Function<TEvent, CompletableFuture<Void>>)
}
