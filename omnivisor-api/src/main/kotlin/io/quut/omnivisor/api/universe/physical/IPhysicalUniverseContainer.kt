package io.quut.omnivisor.api.universe.physical

import io.quut.omnivisor.api.domain.IUniverseDomain
import io.quut.omnivisor.api.universe.IUniverseContainer
import java.lang.invoke.MethodHandles

interface IPhysicalUniverseContainer : IUniverseContainer
{
	fun registerListeners(plugin: Any, listener: Any, lookup: MethodHandles.Lookup? = null)
	fun registerArea(area: IUniverseDomain)

	fun unregisterListeners(plugin: Any)
	fun unregisterListeners(plugin: Any, listener: Any)
	fun unregisterArea(area: IUniverseDomain)
}
