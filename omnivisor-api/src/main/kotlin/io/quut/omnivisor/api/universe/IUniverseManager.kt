package io.quut.omnivisor.api.universe

import io.quut.omnivisor.api.multiverse.IMultiverseLike

interface IUniverseManager : IMultiverseLike, IUniverseCluster
{
	fun universe(id: Int): IUniverse<*>?
}
