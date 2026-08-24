package io.quut.omnivisor.sponge.universe.event.collection

import io.quut.omnivisor.sponge.utils.exceptionallyVoid
import org.apache.logging.log4j.Logger
import java.util.concurrent.CompletableFuture

internal class ContainerEventCollection(private val logger: Logger, private val events: Map<Class<*>, ContainerEventHandler<*>>)
{
	internal fun fireEvent(event: Any): CompletableFuture<Void> =
		listener(event.javaClass)?.let { e -> e.forEach(event) { it } } ?: CompletableFuture.completedFuture(null)

	internal fun fireEventCatching(event: Any): CompletableFuture<Void> =
		listener(event.javaClass)?.let { e -> e.forEach(event) { f -> f.exceptionallyVoid { e -> this.logger.error("Failure during event dispatch", e) } } }
			?: CompletableFuture.completedFuture(null)

	@Suppress("UNCHECKED_CAST")
	private fun listener(event: Class<*>): ContainerEventHandler<Any>?
	{
		var handler: ContainerEventHandler<*>? = null
		for ((key: Class<*>, value: ContainerEventHandler<*>) in this.events)
		{
			if (key.isAssignableFrom(event))
			{
				if (handler != null)
				{
					throw UnsupportedOperationException("Event hierarchy is not yet supported. $event")
				}

				handler = value
			}
		}

		return handler as ContainerEventHandler<Any>?
	}
}
