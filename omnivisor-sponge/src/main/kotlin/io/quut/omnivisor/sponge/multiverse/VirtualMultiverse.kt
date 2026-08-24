package io.quut.omnivisor.sponge.multiverse

import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverse
import io.quut.omnivisor.api.multiverse.virtual.IVirtualMultiverseNode
import io.quut.omnivisor.api.universe.IUniverseLike
import io.quut.omnivisor.sponge.universe.UniverseCollection
import io.quut.omnivisor.sponge.universe.UniverseContainer
import io.quut.omnivisor.sponge.universe.UniverseContext
import io.quut.omnivisor.sponge.utils.MainThreadExecutor

internal open class VirtualMultiverse<T>(
	universes: UniverseCollection,
	mainThreadExecutor: MainThreadExecutor,
	container: UniverseContainer,
	instance: T,
	parent: IUniverseLike?,
	context: UniverseContext<*, *>) : Multiverse<T>(universes, mainThreadExecutor, container, instance, parent, context), IVirtualMultiverse<T>
{
	internal class Node<T>(
		universes: UniverseCollection,
		mainThreadExecutor: MainThreadExecutor,
		container: UniverseContainer,
		instance: T,
		parent: IUniverseLike?,
		context: UniverseContext<*, *>) : VirtualMultiverse<T>(universes, mainThreadExecutor, container, instance, parent, context), IVirtualMultiverseNode<T>
}
