package io.quut.omnivisor.sponge.universe.event

import io.quut.omnivisor.api.universe.event.IUniverseKilledEvent

internal class UniverseKilledEvent(override val teardown: Boolean) : IUniverseKilledEvent
{
	companion object
	{
		internal val TEARDOWN_FALSE: UniverseKilledEvent = UniverseKilledEvent(false)
		internal val TEARDOWN_TRUE: UniverseKilledEvent = UniverseKilledEvent(true)
	}
}
