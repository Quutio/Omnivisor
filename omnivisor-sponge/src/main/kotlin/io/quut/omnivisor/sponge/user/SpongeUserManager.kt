package io.quut.omnivisor.sponge.user

import io.quut.omnivisor.api.universe.IUniverse
import io.quut.omnivisor.api.universe.IUniverseInfo
import org.spongepowered.api.network.ServerSideConnection
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal class SpongeUserManager
{
	private val connections: ConcurrentMap<ServerSideConnection, UserData> = ConcurrentHashMap()

	internal fun get(connection: ServerSideConnection): IUniverseInfo? = this.connections[connection]?.universeInfo

	internal fun connectionEstablished(connection: ServerSideConnection, universe: IUniverse<*>)
	{
		this.connections[connection] = UserData(universe.info)
	}

	internal fun connectionDisconnected(connection: ServerSideConnection)
	{
		this.connections.remove(connection)
	}

	private class UserData(val universeInfo: IUniverseInfo)
}
