package io.quut.omnivisor.sponge.multiverse

import io.quut.omnivisor.api.multiverse.IMultiverse
import io.quut.omnivisor.api.universe.IUniverseContainer
import io.quut.omnivisor.api.universe.IUniverseLike
import io.quut.omnivisor.sponge.universe.IUniverseBase
import io.quut.omnivisor.sponge.universe.UniverseCollection
import io.quut.omnivisor.sponge.universe.UniverseContext
import io.quut.omnivisor.sponge.utils.MainThreadExecutor

internal abstract class Multiverse<T>(
	override val universes: UniverseCollection,
	override val mainThreadExecutor: MainThreadExecutor,
	override val container: IUniverseContainer,
	override val instance: T,
	override val parent: IUniverseLike?,
	context: UniverseContext<*, *>) : MultiverseLikeBase(context.collectProviders(), context.collectEvents()), IMultiverse<T>, IUniverseBase<T>
