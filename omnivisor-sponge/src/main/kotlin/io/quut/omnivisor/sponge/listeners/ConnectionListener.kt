package io.quut.omnivisor.sponge.listeners

import com.google.inject.Inject
import com.google.inject.name.Named
import io.quut.omnivisor.api.multiverse.IMultiverseLike
import io.quut.omnivisor.api.universe.IUniverse
import io.quut.omnivisor.sponge.universe.UniverseFallbackResult
import io.quut.omnivisor.sponge.universe.UniverseManager
import io.quut.omnivisor.sponge.user.SpongeUserManager
import io.quut.omnivisor.sponge.utils.Const
import org.spongepowered.api.ResourceKey
import org.spongepowered.api.Server
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.Order
import org.spongepowered.api.event.network.ServerSideConnectionEvent
import org.spongepowered.api.network.channel.raw.RawDataChannel
import org.spongepowered.api.scheduler.Task
import org.spongepowered.plugin.PluginContainer

internal class ConnectionListener @Inject internal constructor(
	private val container: PluginContainer,
	@param: Named(Const.HANDSHAKE_CHANNEL) private val handshakeChannel: RawDataChannel,
	private val server: Server,
	private val userManager: SpongeUserManager,
	private val universeManager: UniverseManager) : IListener
{
	@Listener(order = Order.POST)
	private fun onServerSideConnectionIntent(event: ServerSideConnectionEvent.Intent)
	{
		this.handshakeChannel.handshake().sendTo(event.connection()) { }.whenComplete()
		{ buffer, ex ->
			var universe: IUniverse<*>? = null
			if (ex == null)
			{
				val method: Int = buffer.readByte().toInt()
				if (method == 0)
				{
					universe = this.universeManager.universe(buffer.readInt())
				}
				else if (method == 1)
				{
					universe = this.universeManager.child(ResourceKey.resolve(buffer.readUTF()))

					val count: Int = buffer.readInt()
					for (i in 0..<count)
					{
						if (universe is IMultiverseLike)
						{
							universe = universe.child(ResourceKey.resolve(buffer.readUTF()))
						}
						else
						{
							universe = null

							break
						}
					}
				}
				else
				{
					throw UnsupportedOperationException("Unsupported method type")
				}
			}

			if (universe == null)
			{
				val fallback: UniverseFallbackResult = this.universeManager.fallback()
				if (fallback.reject)
				{
					return@whenComplete event.connection().close()
				}

				universe = fallback.universe
			}

			if (universe != null)
			{
				this.userManager.connectionEstablished(event.connection(), universe)
			}
		}
	}

	@Listener(order = Order.POST)
	private fun onServerSideConnectionDisconnect(event: ServerSideConnectionEvent.Disconnect)
	{
		// Remove the user after the event has been processed to allow
		// Harmony listeners to work in the POST order.
		this.server.scheduler().submit(Task.builder()
			.plugin(this.container)
			.execute { _ -> this.userManager.connectionDisconnected(event.connection()) }
			.build())
	}
}
