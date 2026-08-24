package io.quut.omnivisor.sponge.universe

import io.quut.omnivisor.api.universe.IUniverse
import io.quut.omnivisor.api.universe.IUniverseLike
import java.util.concurrent.CompletableFuture

internal class Universe<T>(
	override val container: UniverseContainer,
	override val instance: T,
	override val parent: IUniverseLike?,
	context: UniverseContext<*, *>) : UniverseLikeBase(context.collectEvents()), IUniverse<T>, IUniverseBase<T>
