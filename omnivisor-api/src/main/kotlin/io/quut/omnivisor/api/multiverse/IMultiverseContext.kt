package io.quut.omnivisor.api.multiverse

import io.quut.omnivisor.api.universe.IUniverseContext
import java.util.function.Function

interface IMultiverseContext<TConfig, TContainer : IMultiverseContainer> : IUniverseContext<TConfig, TContainer>
{
	fun <TDependency> offerLocalProvider(dependency: Class<TDependency>, function: Function<IUniverseContext<*, *>, TDependency>)
}
