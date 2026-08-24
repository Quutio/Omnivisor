package io.quut.omnivisor.sponge.universe

import io.quut.omnivisor.api.universe.IUniverse
import io.quut.omnivisor.api.universe.IUniverseContainer
import io.quut.omnivisor.api.universe.IUniverseInfo

internal interface IUniverseBase<T> : IUniverse<T>, IUniverseLikeBase
{
	val container: IUniverseContainer

	override val info: IUniverseInfo
		get() = this.container.info
}
