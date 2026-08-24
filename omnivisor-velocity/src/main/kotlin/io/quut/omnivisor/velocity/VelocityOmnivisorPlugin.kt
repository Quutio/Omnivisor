package io.quut.omnivisor.velocity

import com.google.common.io.ByteArrayDataOutput
import com.google.common.io.ByteStreams
import io.quut.fusion.api.connection.IConnectionRequestParameters
import io.quut.omnivisor.api.IOmnivisor
import io.quut.omnivisor.velocity.util.Const
import net.kyori.adventure.key.Key

internal class VelocityOmnivisorPlugin : IOmnivisor
{
	override fun connectionRequestParameters(id: Int): IConnectionRequestParameters =
		IConnectionRequestParameters()
		{ r ->
			r.loginPluginMessageHandler(Const.HANDSHAKE_PLUGIN_CHANNEL_KEY)
			{ _ ->
				val output: ByteArrayDataOutput = ByteStreams.newDataOutput(5)
				output.writeByte(0)
				output.writeInt(id)

				return@loginPluginMessageHandler output.toByteArray()
			}
		}

	override fun connectionRequestParameters(universe: Key, vararg children: Key): IConnectionRequestParameters =
		IConnectionRequestParameters()
		{ r ->
			r.loginPluginMessageHandler(Const.HANDSHAKE_PLUGIN_CHANNEL_KEY)
			{ _ ->
				val output: ByteArrayDataOutput = ByteStreams.newDataOutput()
				output.writeByte(1)
				output.writeUTF(universe.asString())
				output.writeInt(children.size)
				children.forEach { key -> output.writeUTF(key.asString()) }

				return@loginPluginMessageHandler output.toByteArray()
			}
		}
}
