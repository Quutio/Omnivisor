package io.quut.omnivisor.velocity

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import io.quut.fusion.velocity.api.IVelocityFusionAPI
import io.quut.omnivisor.api.IOmnivisor
import io.quut.omnivisor.api.IOmnivisorAPI
import io.quut.omnivisor.velocity.command.ServerCommand
import io.quut.omnivisor.velocity.util.Const

@Plugin(id = Const.NAMESPACE, name = "Omnivisor", version = "1.0")
class VelocityOmnivisorPluginLoader @Inject internal constructor(private val proxy: ProxyServer)
{
	private val plugin: VelocityOmnivisorPlugin = VelocityOmnivisorPlugin()
	private val api: API = API()

	@Subscribe
	fun onProxyInitialize(event: ProxyInitializeEvent)
	{
		ServerCommand.register(this.proxy, this, this.plugin, IVelocityFusionAPI.get().fusion)

		IOmnivisorAPI.register(this.api)
	}

	@Subscribe
	fun onProxyShutdown(event: ProxyShutdownEvent)
	{
		IOmnivisorAPI.unregister(this.api)
	}

	private inner class API : IOmnivisorAPI
	{
		override val omnivisor: IOmnivisor
			get() = this@VelocityOmnivisorPluginLoader.plugin
	}
}
