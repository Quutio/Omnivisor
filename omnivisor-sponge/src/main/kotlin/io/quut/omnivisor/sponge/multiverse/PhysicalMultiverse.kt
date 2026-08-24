package io.quut.omnivisor.sponge.multiverse

import io.quut.omnivisor.api.multiverse.physical.IPhysicalMultiverse
import io.quut.omnivisor.api.multiverse.physical.IPhysicalMultiverseNode
import io.quut.omnivisor.api.universe.IUniverseLike
import io.quut.omnivisor.sponge.universe.UniverseCollection
import io.quut.omnivisor.sponge.universe.UniverseContainer
import io.quut.omnivisor.sponge.universe.UniverseContext
import io.quut.omnivisor.sponge.utils.MainThreadExecutor

internal open class PhysicalMultiverse<T>(
	universes: UniverseCollection,
	mainThreadExecutor: MainThreadExecutor,
	container: UniverseContainer,
	instance: T,
	parent: IUniverseLike?,
	context: UniverseContext<*, *>) : Multiverse<T>(universes, mainThreadExecutor, container, instance, parent, context), IPhysicalMultiverse<T>
{
	internal class Node<T>(
		universes: UniverseCollection,
		mainThreadExecutor: MainThreadExecutor,
		container: UniverseContainer,
		instance: T,
		parent: IUniverseLike?,
		context: UniverseContext<*, *>) : PhysicalMultiverse<T>(universes, mainThreadExecutor, container, instance, parent, context), IPhysicalMultiverseNode<T>
}
