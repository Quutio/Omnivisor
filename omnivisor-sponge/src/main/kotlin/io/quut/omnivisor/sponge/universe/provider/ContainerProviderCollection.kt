package io.quut.omnivisor.sponge.universe.provider

import io.quut.omnivisor.api.universe.IUniverseContext
import io.quut.omnivisor.sponge.universe.UniverseContext
import java.util.function.Function

internal class ContainerProviderCollection(private val providers: Map<Class<*>, Function<IUniverseContext<*, *>, *>>)
{
	@Suppress("UNCHECKED_CAST")
	fun <D> require(dependency: Class<D>, context: UniverseContext<*, *>): D
	{
		val function: Function<IUniverseContext<*, *>, *> = this.providers[dependency] ?: throw RuntimeException("Missing dependency $dependency")

		return function.apply(context) as D
	}

	fun append(providers: MutableMap<Class<*>, Function<IUniverseContext<*, *>, *>>): ContainerProviderCollection =
		ContainerProviderCollection(this.providers + providers)
}
