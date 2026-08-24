package io.quut.omnivisor.sponge.universe

import io.quut.omnivisor.sponge.universe.event.collection.ContainerEventCollection

internal abstract class UniverseLikeBase(private val events: ContainerEventCollection) : IUniverseLikeBase
{
	override fun fireEvent(event: Any) = this.events.fireEvent(event)
	override fun fireEventCatching(event: Any) = this.events.fireEventCatching(event)
}
