package io.quut.omnivisor.sponge.universe.template

import io.quut.omnivisor.api.multiverse.IMultiverse
import io.quut.omnivisor.api.multiverse.physical.IPhysicalMultiverseContainer
import io.quut.omnivisor.api.multiverse.physical.IPhysicalMultiverseContext
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverse
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverseContainer
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverseContext
import io.quut.omnivisor.api.universe.IUniverse
import io.quut.omnivisor.api.universe.IUniverseFactory
import io.quut.omnivisor.api.universe.IUniverseTemplate
import io.quut.omnivisor.api.universe.physical.IPhysicalUniverseContainer
import io.quut.omnivisor.api.universe.physical.IPhysicalUniverseContext
import java.util.concurrent.CompletableFuture
import java.util.function.BiFunction
import java.util.function.Function

internal abstract class UniverseTemplate<TInstance, TConfig> : IUniverseTemplate<TInstance, TConfig>
{
	internal class Static<TInstance, TUniverse : IUniverse<TInstance>, TConfig>(private val function: BiFunction<TConfig, IUniverseFactory<TInstance, TConfig>, CompletableFuture<out TUniverse>>): UniverseTemplate<TInstance, TConfig>()
	{
		override fun create(config: TConfig, factory: IUniverseFactory<TInstance, TConfig>): CompletableFuture<out IUniverse<TInstance>> =
			this.function.apply(config, factory)
	}

	internal class Dynamic<TInstance, TConfig>(private val function: Function<TConfig, IUniverseTemplate<TInstance, TConfig>>): UniverseTemplate<TInstance, TConfig>()
	{
		override fun create(config: TConfig, factory: IUniverseFactory<TInstance, TConfig>): CompletableFuture<out IUniverse<TInstance>> =
			this.function.apply(config).create(config, factory)
	}

	companion object
	{
		internal fun <TInstance, TConfig> universe(function: BiFunction<IPhysicalUniverseContext<TConfig, IPhysicalUniverseContainer>, Function<TInstance, CompletableFuture<IUniverse<TInstance>>>, CompletableFuture<IUniverse<TInstance>>>): Static<TInstance, IUniverse<TInstance>, TConfig> =
			Static { config, factory -> factory.createUniverse(config, CreateFunction(function)) }

		internal fun <TInstance, TConfig> multiverse(node: Boolean, function: BiFunction<IPhysicalMultiverseContext<TConfig, IPhysicalMultiverseContainer>, Function<TInstance, CompletableFuture<IMultiverse<TInstance>>>, CompletableFuture<IMultiverse<TInstance>>>): Static<TInstance, IMultiverse<TInstance>, TConfig> =
			Static { config, factory -> factory.createMultiverse(node, config, CreateFunction(function)) }

		internal fun <TInstance, TConfig> virtualMultiverse(node: Boolean, function: BiFunction<IVirtualMultiverseContext<TConfig, IVirtualMultiverseContainer>, Function<TInstance, CompletableFuture<IVirtualMultiverse<TInstance>>>, CompletableFuture<IVirtualMultiverse<TInstance>>>): Static<TInstance, IVirtualMultiverse<TInstance>, TConfig> =
			Static { config, factory -> factory.createVirtualMultiverse(node, config, CreateFunction(function)) }
	}

	private class CreateFunction<TInstance, TUniverse : IUniverse<TInstance>, TContext>(
		private val function: BiFunction<TContext, Function<TInstance, CompletableFuture<TUniverse>>, CompletableFuture<TUniverse>>) : IUniverseFactory.ICreateFunction<TInstance, TUniverse, TContext>
	{
		@Suppress("UNCHECKED_CAST")
		override fun <T : TUniverse> create(context: TContext, function: Function<TInstance, CompletableFuture<T>>): CompletableFuture<T> =
			this.function.apply(context) { instance -> function.apply(instance).thenApply { universe -> universe } }.thenApply { universe -> universe as T }
	}
}
