package io.quut.omnivisor.sponge.universe.template

import io.quut.omnivisor.api.universe.IUniverse
import io.quut.omnivisor.api.universe.IUniverseContext
import io.quut.omnivisor.api.universe.IUniverseTemplate
import io.quut.omnivisor.sponge.utils.thenComposeAsync
import java.util.concurrent.CompletableFuture
import java.util.function.BiFunction
import java.util.function.Function

internal abstract class UniverseTemplateCompleteBuilder<TInstance, TUniverse : IUniverse<TInstance>, TContext : IUniverseContext<*, *>, TState, TOut>(
	protected val function: BiFunction<TContext, Function<TInstance, CompletableFuture<TUniverse>>, CompletableFuture<Pair<TUniverse, TState>>>)
	: IUniverseTemplate.ICompleteBuilder<TUniverse, TState, TOut>
{
	protected abstract fun <TNewState> chain0(function: BiFunction<TContext, Function<TInstance, CompletableFuture<TUniverse>>, CompletableFuture<Pair<TUniverse, TNewState>>>): UniverseTemplateCompleteBuilder<TInstance, TUniverse, TContext, TNewState, TOut>

	override fun <TNewState> universeStep(function: Function<TUniverse, TNewState>): IUniverseTemplate.ICompleteBuilder<TUniverse, TNewState, TOut> =
		this.chain { _, (universe, _) -> CompletableFuture.completedFuture(function.apply(universe)) }

	override fun <TNewState> universeStep(function: BiFunction<TUniverse, TState, TNewState>): IUniverseTemplate.ICompleteBuilder<TUniverse, TNewState, TOut> =
		this.chain { _, (universe, state) -> CompletableFuture.completedFuture(function.apply(universe, state)) }

	override fun <TNewState> universeStepAsync(function: Function<TUniverse, CompletableFuture<TNewState>>): IUniverseTemplate.ICompleteBuilder<TUniverse, TNewState, TOut> =
		this.chain { _, (universe, _) -> function.apply(universe) }

	override fun <TNewState> universeStepAsync(function: BiFunction<TUniverse, TState, CompletableFuture<TNewState>>): IUniverseTemplate.ICompleteBuilder<TUniverse, TNewState, TOut> =
		this.chain { _, (universe, state) -> function.apply(universe, state) }

	private fun <TNewState> chain(function: BiFunction<TContext, Pair<TUniverse, TState>, CompletableFuture<TNewState>>): UniverseTemplateCompleteBuilder<TInstance, TUniverse, TContext, TNewState, TOut> =
		this.chain0 { context, universeFunction -> this.function.apply(context, universeFunction).thenComposeAsync(context.executor) { (universe, state) -> function.apply(context, Pair(universe, state)).thenApply { state -> Pair(universe, state) } } }

	internal abstract class Universe<TInstance, TContext : IUniverseContext<*, *>, TState, TOut>(function: BiFunction<TContext, Function<TInstance, CompletableFuture<IUniverse<TInstance>>>, CompletableFuture<Pair<IUniverse<TInstance>, TState>>>)
		: UniverseTemplateCompleteBuilder<TInstance, IUniverse<TInstance>, TContext, TState, TOut>(function)

	internal class Generic<TInstance, TUniverse : IUniverse<TInstance>, TContext : IUniverseContext<TConfig, *>, TConfig, TState, TOut>(
		function: BiFunction<TContext, Function<TInstance, CompletableFuture<TUniverse>>, CompletableFuture<Pair<TUniverse, TState>>>,
		val function2: (BiFunction<TContext, Function<TInstance, CompletableFuture<TUniverse>>, CompletableFuture<TUniverse>>) -> TOut)
		: UniverseTemplateCompleteBuilder<TInstance, TUniverse, TContext, TState, TOut>(function)
	{
		override fun <TNewState> chain0(function: BiFunction<TContext, Function<TInstance, CompletableFuture<TUniverse>>, CompletableFuture<Pair<TUniverse, TNewState>>>): UniverseTemplateCompleteBuilder<TInstance, TUniverse, TContext, TNewState, TOut> =
			Generic(function, function2)

		override fun build(): TOut = this.function2 { context, universeFunction -> this.function.apply(context, universeFunction).thenApply { (universe, _) -> universe } }
	}
}
