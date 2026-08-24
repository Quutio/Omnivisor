package io.quut.omnivisor.api.universe

import net.kyori.adventure.key.Key
import java.util.concurrent.CompletableFuture

interface IUniverseCluster
{
	fun <TInstance, TConfig> create(key: Key, options: IUniverseOptions<TInstance, TConfig>): CompletableFuture<out IUniverse<TInstance>>
}
