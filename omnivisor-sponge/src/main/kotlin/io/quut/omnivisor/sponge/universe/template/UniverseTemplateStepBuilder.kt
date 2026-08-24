package io.quut.omnivisor.sponge.universe.template

import io.quut.omnivisor.api.universe.IUniverse
import io.quut.omnivisor.api.universe.IUniverseContainer
import io.quut.omnivisor.api.universe.IUniverseContext
import io.quut.omnivisor.api.universe.IUniverseTemplate
import io.quut.omnivisor.api.universe.event.UniverseEventPriority
import io.quut.omnivisor.sponge.utils.thenApplyAsync
import io.quut.omnivisor.sponge.utils.thenComposeAsync
import io.quut.omnivisor.sponge.utils.whenSuccessAsync
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function

internal class UniverseTemplateStepBuilder<TInstance, TUniverse : IUniverse<TInstance>, TContainer : IUniverseContainer, TContext : IUniverseContext<TConfig, TContainer>, TConfig, TState, TOut>(
	private val function: Function<TContext, CompletableFuture<TState>>,
	private val completeFunction: (BiFunction<TContext, Function<TInstance, CompletableFuture<TUniverse>>, CompletableFuture<TUniverse>>) -> TOut): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>
{
	override fun action(consumer: Consumer<TState>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
		this.chain { _, state -> consumer.accept(state) }

	override fun <TNewState> step(function: Function<TState, TNewState>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut> =
		this.chain<TNewState> { _, state -> CompletableFuture.completedFuture(function.apply(state)) }

	override fun <TNewState> stepAsync(function: Function<TState, CompletableFuture<TNewState>>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut> =
		this.chain<TNewState> { _, state -> function.apply(state) }

	override fun <D> dependency(dependency: Class<D>, consumer: Consumer<D>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
		this.chain { context, _ -> this.function.apply(context).thenCompose { context.globalProvider(dependency).whenSuccessAsync(context.executor) { dependency -> consumer.accept(dependency) } } }

	override fun <D, TNewState> dependencyStep(dependency: Class<D>, function: Function<D, TNewState>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut> =
		this.chain<TNewState> { context, _ -> this.function.apply(context).thenCompose { context.globalProvider(dependency).thenApplyAsync(context.executor) { dependency -> function.apply(dependency) } } }

	override fun <D, TNewState> dependencyStep(dependency: Class<D>, function: BiFunction<D, TState, TNewState>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut> =
		this.chain<TNewState> { context, state -> this.function.apply(context).thenCompose { context.globalProvider(dependency).thenApplyAsync(context.executor) { dependency -> function.apply(dependency, state) } } }

	override fun <D, TNewState> dependencyStepAsync(dependency: Class<D>, function: Function<D, CompletableFuture<TNewState>>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut> =
		this.chain<TNewState> { context, _ -> this.function.apply(context).thenCompose { context.globalProvider(dependency).thenComposeAsync(context.executor) { dependency -> function.apply(dependency) } } }

	override fun <D, TNewState> dependencyStepAsync(dependency: Class<D>, function: BiFunction<D, TState, CompletableFuture<TNewState>>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut> =
		this.chain<TNewState> { context, state -> this.function.apply(context).thenCompose { context.globalProvider(dependency).thenComposeAsync(context.executor) { dependency -> function.apply(dependency, state) } } }

	override fun context(consumer: Consumer<TContext>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
		this.chain { context, _ -> consumer.accept(context) }

	override fun context(consumer: BiConsumer<TContext, TState>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
		this.chain { context, state -> consumer.accept(context, state) }

	override fun <TNewState> contextStep(function: Function<TContext, TNewState>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut> =
		this.chain<TNewState> { context, _ -> CompletableFuture.completedFuture(function.apply(context)) }

	override fun <TNewState> contextStep(function: BiFunction<TContext, TState, TNewState>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut> =
		this.chain<TNewState> { context, state -> CompletableFuture.completedFuture(function.apply(context, state)) }

	override fun <TNewState> contextStepAsync(function: Function<TContext, CompletableFuture<TNewState>>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut> =
		this.chain<TNewState> { context, _ -> function.apply(context) }

	override fun <TNewState> contextStepAsync(function: BiFunction<TContext, TState, CompletableFuture<TNewState>>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut> =
		this.chain<TNewState> { context, state -> function.apply(context, state) }

	override fun container(consumer: Consumer<TContainer>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
		this.chain { context, _ -> CompletableFuture.completedFuture(consumer.accept(context.container)) }

	override fun container(consumer: BiConsumer<TContainer, TState>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
		this.chain { context, state -> CompletableFuture.completedFuture(consumer.accept(context.container, state)) }

	override fun <TNewState> containerStep(function: Function<TContainer, TNewState>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut> =
		this.chain<TNewState> { context, _ -> CompletableFuture.completedFuture(function.apply(context.container)) }

	override fun <TNewState> containerStep(function: BiFunction<TContainer, TState, TNewState>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut> =
		this.chain<TNewState> { context, state -> CompletableFuture.completedFuture(function.apply(context.container, state)) }

	override fun <TNewState> containerStepAsync(function: Function<TContainer, CompletableFuture<TNewState>>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut> =
		this.chain<TNewState> { context, _ -> function.apply(context.container) }

	override fun <TNewState> containerStepAsync(function: BiFunction<TContainer, TState, CompletableFuture<TNewState>>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut> =
		this.chain<TNewState> { context, state -> function.apply(context.container, state) }

	override fun <TEvent> event(event: Class<TEvent>, consumer: BiConsumer<TEvent, TState>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
		this.event(event, UniverseEventPriority.DEFAULT, consumer)

	override fun <TEvent> event(event: Class<TEvent>, priority: UniverseEventPriority, consumer: BiConsumer<TEvent, TState>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
		this.chain()
		{ context, state ->
			context.event(event, priority)
			{ event ->
				consumer.accept(event, state)

				return@event CompletableFuture.completedFuture(null)
			}
		}

	override fun <TEvent> eventAsync(event: Class<TEvent>, function: BiFunction<TEvent, TState, CompletableFuture<Void>>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
		this.eventAsync(event, UniverseEventPriority.DEFAULT, function)

	override fun <TEvent> eventAsync(event: Class<TEvent>, priority: UniverseEventPriority, function: BiFunction<TEvent, TState, CompletableFuture<Void>>): IUniverseTemplate.IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
		this.chain { context, state -> context.event(event, priority) { event -> function.apply(event, state) } }

	override fun complete(function: Function<TContainer, TInstance>): IUniverseTemplate.ICompleteBuilder<TUniverse, TState, TOut> =
		this.complete0 { context, state -> CompletableFuture.completedFuture(Pair(state, function.apply(context.container))) }

	override fun complete(function: BiFunction<TContainer, TState, TInstance>): IUniverseTemplate.ICompleteBuilder<TUniverse, TState, TOut> =
		this.complete0 { context, state -> CompletableFuture.completedFuture(Pair(state, function.apply(context.container, state))) }

	override fun completeAsync(function: BiFunction<TContainer, TState, CompletableFuture<TInstance>>): IUniverseTemplate.ICompleteBuilder<TUniverse, TState, TOut> =
		this.complete0 { context, state -> function.apply(context.container, state).thenApply { i -> Pair(state, i) } }

	override fun completeAsync(function: Function<TContainer, CompletableFuture<TInstance>>): IUniverseTemplate.ICompleteBuilder<TUniverse, TState, TOut> =
		this.complete0 { context, state -> function.apply(context.container).thenApply { i -> Pair(state, i) } }

	private fun complete0(function: BiFunction<TContext, TState, CompletableFuture<Pair<TState, TInstance>>>): IUniverseTemplate.ICompleteBuilder<TUniverse, TState, TOut> =
		this.complete0 { context -> this.function.apply(context).thenComposeAsync(context.executor) { state -> function.apply(context, state) } }

	private fun chain(consumer: BiConsumer<TContext, TState>): UniverseTemplateStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
		this.chain { context -> this.function.apply(context).whenSuccessAsync(context.executor) { state -> consumer.accept(context, state) } }

	private fun <TNewState> chain(function: BiFunction<TContext, TState, CompletableFuture<TNewState>>): UniverseTemplateStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut> =
		this.chain { context -> this.function.apply(context).thenComposeAsync(context.executor) { state -> function.apply(context, state) } }

	private fun <TNewState> chain(function: Function<TContext, CompletableFuture<TNewState>>): UniverseTemplateStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut> =
		UniverseTemplateStepBuilder(function, this.completeFunction)

	private fun complete0(function: Function<TContext, CompletableFuture<Pair<TState, TInstance>>>): IUniverseTemplate.ICompleteBuilder<TUniverse, TState, TOut> =
		UniverseTemplateCompleteBuilder.Generic({ context, universeFunction ->
			function.apply(context)
			.thenCompose { (state, instance) ->
				universeFunction.apply(instance)
				.thenApply { universe -> Pair(universe, state) }
			}
		}, this.completeFunction)
}
