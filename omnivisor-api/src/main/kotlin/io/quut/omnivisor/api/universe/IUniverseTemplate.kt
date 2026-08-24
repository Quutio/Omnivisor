package io.quut.omnivisor.api.universe

import io.quut.omnivisor.api.domain.IUniverseDomain
import io.quut.omnivisor.api.multiverse.IMultiverse
import io.quut.omnivisor.api.multiverse.IMultiverseContainer
import io.quut.omnivisor.api.multiverse.IMultiverseContext
import io.quut.omnivisor.api.multiverse.physical.IPhysicalMultiverseContainer
import io.quut.omnivisor.api.multiverse.physical.IPhysicalMultiverseContext
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverse
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverseContainer
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverseContext
import io.quut.omnivisor.api.universe.event.UniverseEventPriority
import io.quut.omnivisor.api.universe.physical.IPhysicalUniverseContainer
import io.quut.omnivisor.api.universe.physical.IPhysicalUniverseContext
import java.lang.invoke.MethodHandles
import java.util.ServiceLoader
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function

interface IUniverseTemplate<TInstance, TConfig>
{
	fun create(config: TConfig, factory: IUniverseFactory<TInstance, TConfig>): CompletableFuture<out IUniverse<TInstance>>

	companion object
	{
		@JvmStatic
		fun <TInstance, TConfig> builder(): IBuilder<TInstance, TConfig, IUniverseTemplate<TInstance, TConfig>>
		{
			val factory: IFactory = ServiceLoader.load(IFactory::class.java).findFirst().orElseThrow() as IFactory
			return factory.builder()
		}
	}

	interface IBuilder<TInstance, TConfig, TOut>
	{
		fun universe(): IStepBuilder<TInstance, IUniverse<TInstance>, IPhysicalUniverseContainer, IPhysicalUniverseContext<TConfig, IPhysicalUniverseContainer>, TConfig, TConfig, TOut>
		fun multiverse(node: Boolean = false): IStepBuilder<TInstance, IMultiverse<TInstance>, IPhysicalMultiverseContainer, IPhysicalMultiverseContext<TConfig, IPhysicalMultiverseContainer>, TConfig, TConfig, TOut>
		fun virtualMultiverse(node: Boolean = false): IStepBuilder<TInstance, IVirtualMultiverse<TInstance>, IVirtualMultiverseContainer, IVirtualMultiverseContext<TConfig, IVirtualMultiverseContainer>, TConfig, TConfig, TOut>
	}

	interface IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>
	{
		fun action(consumer: Consumer<TState>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>

		fun <TNewState> step(function: Function<TState, TNewState>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut>
		fun <TNewState> stepAsync(function: Function<TState, CompletableFuture<TNewState>>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut>

		fun <TDependency> dependency(dependency: Class<TDependency>, consumer: Consumer<TDependency>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>

		fun <TDependency, TNewState> dependencyStep(dependency: Class<TDependency>, function: Function<TDependency, TNewState>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut>
		fun <TDependency, TNewState> dependencyStep(dependency: Class<TDependency>, function: BiFunction<TDependency, TState, TNewState>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut>
		fun <TDependency, TNewState> dependencyStepAsync(dependency: Class<TDependency>, function: Function<TDependency, CompletableFuture<TNewState>>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut>
		fun <TDependency, TNewState> dependencyStepAsync(dependency: Class<TDependency>, function: BiFunction<TDependency, TState, CompletableFuture<TNewState>>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut>

		fun context(consumer: Consumer<TContext>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>
		fun context(consumer: BiConsumer<TContext, TState>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>

		fun <TNewState> contextStep(function: Function<TContext, TNewState>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut>
		fun <TNewState> contextStep(function: BiFunction<TContext, TState, TNewState>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut>
		fun <TNewState> contextStepAsync(function: Function<TContext, CompletableFuture<TNewState>>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut>
		fun <TNewState> contextStepAsync(function: BiFunction<TContext, TState, CompletableFuture<TNewState>>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut>

		fun container(consumer: Consumer<TContainer>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>
		fun container(consumer: BiConsumer<TContainer, TState>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>

		fun <TNewState> containerStep(function: Function<TContainer, TNewState>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut>
		fun <TNewState> containerStep(function: BiFunction<TContainer, TState, TNewState>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut>
		fun <TNewState> containerStepAsync(function: Function<TContainer, CompletableFuture<TNewState>>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut>
		fun <TNewState> containerStepAsync(function: BiFunction<TContainer, TState, CompletableFuture<TNewState>>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut>

		fun <TEvent> event(event: Class<TEvent>, consumer: BiConsumer<TEvent, TState>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>
		fun <TEvent> event(event: Class<TEvent>, priority: UniverseEventPriority, consumer: BiConsumer<TEvent, TState>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>
		fun <TEvent> eventAsync(event: Class<TEvent>, function: BiFunction<TEvent, TState, CompletableFuture<Void>>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>
		fun <TEvent> eventAsync(event: Class<TEvent>, priority: UniverseEventPriority, function: BiFunction<TEvent, TState, CompletableFuture<Void>>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>

		fun complete(function: Function<TContainer, TInstance>): ICompleteBuilder<TUniverse, TState, TOut>
		fun complete(function: BiFunction<TContainer, TState, TInstance>): ICompleteBuilder<TUniverse, TState, TOut>

		fun completeAsync(function: Function<TContainer, CompletableFuture<TInstance>>): ICompleteBuilder<TUniverse, TState, TOut>
		fun completeAsync(function: BiFunction<TContainer, TState, CompletableFuture<TInstance>>): ICompleteBuilder<TUniverse, TState, TOut>

		companion object
		{
			inline fun <TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut, reified TDependency> IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>.dependencyAction(consumer: Consumer<TDependency>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
				this.dependency(TDependency::class.java, consumer)

			inline fun <TInstance, TUniverse, TContainer, TContext, TConfig, TState, TNewState, TOut, reified TDependency> IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>.dependencyStep(function: Function<TDependency, TNewState>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut> =
				this.dependencyStep(TDependency::class.java, function)

			inline fun <TInstance, TUniverse, TContainer, TContext, TConfig, TState, TNewState, TOut, reified TDependency> IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>.dependencyStep(function: BiFunction<TDependency, TState, TNewState>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut> =
				this.dependencyStep(TDependency::class.java, function)

			inline fun <TInstance, TUniverse, TContainer, TContext, TConfig, TState, TNewState, TOut, reified TDependency> IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>.dependencyStepAsync(function: Function<TDependency, CompletableFuture<TNewState>>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut> =
				this.dependencyStepAsync(TDependency::class.java, function)

			inline fun <TInstance, TUniverse, TContainer, TContext, TConfig, TState, TNewState, TOut, reified TDependency> IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>.dependencyStepAsync(function: BiFunction<TDependency, TState, CompletableFuture<TNewState>>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TNewState, TOut> =
				this.dependencyStepAsync(TDependency::class.java, function)

			fun <TInstance, TUniverse, TContainer : IMultiverseContainer, TContext : IMultiverseContext<TConfig, TContainer>, TConfig, TState, TOut, TDependency> IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>.provide(dependency: Class<TDependency>, function: Function<TState, TDependency>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
				this.context { context, state -> context.offerLocalProvider(dependency) { _ -> function.apply(state) } }

			fun <TInstance, TUniverse, TContainer : IMultiverseContainer, TContext : IMultiverseContext<TConfig, TContainer>, TConfig, TState, TOut, TDependency> IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>.provideFactory(dependency: Class<TDependency>, function: Function<TState, Function<IUniverseContext<*, *>, TDependency>>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
				this.context { context, state -> context.offerLocalProvider(dependency) { c -> function.apply(state).apply(c) } }

			inline fun <TInstance, TUniverse, TContainer : IMultiverseContainer, TContext : IMultiverseContext<TConfig, TContainer>, TConfig, TState, TOut, reified TDependency> IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>.provide(function: Function<TState, TDependency>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
				this.provide(TDependency::class.java, function)

			inline fun <TInstance, TUniverse, TContainer : IMultiverseContainer, TContext : IMultiverseContext<TConfig, TContainer>, TConfig, TState, TOut, reified TDependency> IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>.provideFactory(function: Function<TState, Function<IUniverseContext<*, *>, TDependency>>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
				this.provideFactory(TDependency::class.java, function)

			fun <TInstance, TUniverse, TContainer : IPhysicalUniverseContainer, TContext : IPhysicalUniverseContext<TConfig, TContainer>, TConfig, TState, TOut> IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>.listener(plugin: Any, listener: Any, lookup: MethodHandles.Lookup? = null): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
				this.context { context -> context.container.registerListeners(plugin, listener, lookup) }

			fun <TInstance, TUniverse, TContainer : IPhysicalUniverseContainer, TContext : IPhysicalUniverseContext<TConfig, TContainer>, TConfig, TState, TOut> IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>.listenerFactory(plugin: Any, function: Function<TState, Any>, lookup: MethodHandles.Lookup? = null): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
				this.context { context, state -> context.container.registerListeners(plugin, function.apply(state), lookup) }

			fun <TInstance, TUniverse, TContainer : IPhysicalUniverseContainer, TContext : IPhysicalUniverseContext<TConfig, TContainer>, TConfig, TState, TOut> IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>.area(area: IUniverseDomain): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
				this.context { context -> context.container.registerArea(area) }

			fun <TInstance, TUniverse, TContainer : IPhysicalUniverseContainer, TContext : IPhysicalUniverseContext<TConfig, TContainer>, TConfig, TState, TOut> IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>.area(function: Function<TState, IUniverseDomain>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
				this.context { context, state -> context.container.registerArea(function.apply(state)) }

			inline fun <TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut, reified TEvent> IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>.event(consumer: BiConsumer<TEvent, TState>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
				this.event(TEvent::class.java, consumer)

			inline fun <TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut, reified TEvent> IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>.event(priority: UniverseEventPriority, consumer: BiConsumer<TEvent, TState>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
				this.event(TEvent::class.java, priority, consumer)

			inline fun <TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut, reified TEvent> IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>.eventAsync(function: BiFunction<TEvent, TState, CompletableFuture<Void>>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
				this.eventAsync(TEvent::class.java, function)

			inline fun <TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut, reified TEvent> IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut>.eventAsync(priority: UniverseEventPriority, function: BiFunction<TEvent, TState, CompletableFuture<Void>>): IStepBuilder<TInstance, TUniverse, TContainer, TContext, TConfig, TState, TOut> =
				this.eventAsync(TEvent::class.java, priority, function)

			fun <TInstance, TUniverse, TConfig, TState : TInstance, TOut> IStepBuilder<TInstance, TUniverse, *, *, TConfig, TState, TOut>.complete(): ICompleteBuilder<TUniverse, TState, TOut> =
				this.complete { _, s -> s }
		}
	}

	interface ICompleteBuilder<TUniverse, TState, TOut>
	{
		fun <TNewState> universeStep(function: Function<TUniverse, TNewState>): ICompleteBuilder<TUniverse, TNewState, TOut>
		fun <TNewState> universeStep(function: BiFunction<TUniverse, TState, TNewState>): ICompleteBuilder<TUniverse, TNewState, TOut>

		fun <TNewState> universeStepAsync(function: Function<TUniverse, CompletableFuture<TNewState>>): ICompleteBuilder<TUniverse, TNewState, TOut>
		fun <TNewState> universeStepAsync(function: BiFunction<TUniverse, TState, CompletableFuture<TNewState>>): ICompleteBuilder<TUniverse, TNewState, TOut>

		fun build(): TOut
	}

	interface IFactory
	{
		fun <TInstance, TConfig> builder(): IBuilder<TInstance, TConfig, IUniverseTemplate<TInstance, TConfig>>
	}
}
