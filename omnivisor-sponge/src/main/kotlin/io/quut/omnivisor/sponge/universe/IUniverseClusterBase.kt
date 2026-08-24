package io.quut.omnivisor.sponge.universe

import io.quut.omnivisor.api.universe.IUniverseContainer
import io.quut.omnivisor.api.universe.IUniverseInfo
import io.quut.omnivisor.sponge.universe.provider.ContainerProviderCollection
import java.util.concurrent.CompletableFuture

internal interface IUniverseClusterBase
{
	fun <TInstance, TConfig> createFactory(info: IUniverseInfo, providers: CompletableFuture<ContainerProviderCollection>, holder: UniverseHolder<TInstance>): UniverseFactory<TInstance, TConfig>

	fun unregister(container: IUniverseContainer)
}
