package io.quut.omnivisor.api.universe

import io.quut.omnivisor.api.multiverse.IMultiverse
import io.quut.omnivisor.api.multiverse.physical.IPhysicalMultiverseContainer
import io.quut.omnivisor.api.multiverse.physical.IPhysicalMultiverseContext
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverse
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverseContainer
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverseContext
import io.quut.omnivisor.api.universe.physical.IPhysicalUniverseContainer
import io.quut.omnivisor.api.universe.physical.IPhysicalUniverseContext
import java.util.concurrent.CompletableFuture
import java.util.function.Function

interface IUniverseFactory<TInstance, TConfig>
{
	fun createUniverse(config: TConfig, function: ICreateFunction<TInstance, IUniverse<TInstance>, IPhysicalUniverseContext<TConfig, IPhysicalUniverseContainer>>): CompletableFuture<out IUniverse<TInstance>>
	fun createMultiverse(node: Boolean, config: TConfig, function: ICreateFunction<TInstance, IMultiverse<TInstance>, IPhysicalMultiverseContext<TConfig, IPhysicalMultiverseContainer>>): CompletableFuture<out IMultiverse<TInstance>>
	fun createVirtualMultiverse(node: Boolean, config: TConfig, function: ICreateFunction<TInstance, IVirtualMultiverse<TInstance>, IVirtualMultiverseContext<TConfig, IVirtualMultiverseContainer>>): CompletableFuture<out IVirtualMultiverse<TInstance>>

	interface ICreateFunction<TInstance, TUniverse : IUniverse<TInstance>, TContext>
	{
		fun <T : TUniverse> create(context: TContext, function: Function<TInstance, CompletableFuture<T>>): CompletableFuture<T>
	}
}
