package io.quut.omnivisor.velocity.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import io.quut.fusion.velocity.api.IVelocityFusion
import io.quut.omnivisor.velocity.VelocityOmnivisorPlugin
import io.quut.omnivisor.velocity.VelocityOmnivisorPluginLoader
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import java.util.Optional

internal object ServerCommand
{
	private const val SERVER_ARGUMENT = "server"
	private const val UNIVERSE_ARGUMENT = "universe"
	private const val CHILDREN_ARGUMENT = "children"

	internal fun register(proxy: ProxyServer, loader: VelocityOmnivisorPluginLoader, plugin: VelocityOmnivisorPlugin, fusion: IVelocityFusion)
	{
		val command: BrigadierCommand = this.command(proxy, plugin, fusion)

		proxy.commandManager.register(proxy.commandManager.metaBuilder(command).plugin(loader).build(), command)
	}

	internal fun command(proxy: ProxyServer, plugin: VelocityOmnivisorPlugin, fusion: IVelocityFusion): BrigadierCommand
	{
		fun execute(context: CommandContext<CommandSource>, player: Player): Int
		{
			val server: Optional<RegisteredServer> = proxy.getServer(StringArgumentType.getString(context, this.SERVER_ARGUMENT))
			if (server.isEmpty)
			{
				player.sendMessage(Component.text("Server not found."))

				return -1
			}

			val universe: Key = Key.key(StringArgumentType.getString(context, this.UNIVERSE_ARGUMENT))

			val children: Array<Key> = if (context.arguments.contains(this.CHILDREN_ARGUMENT))
			{
				StringArgumentType.getString(context, this.CHILDREN_ARGUMENT)
					.split(' ')
					.map(Key::key)
					.toTypedArray()
			}
			else
			{
				emptyArray()
			}

			fusion.connectionRequestTemplate(server.get()).connect(player, plugin.connectionRequestParameters(universe, *children))

			return Command.SINGLE_SUCCESS
		}

		return BrigadierCommand(
			BrigadierCommand.literalArgumentBuilder("omnivisor:server")
				.requires { source -> source is Player && source.hasPermission("omnivisor.command.server") }
				.then(BrigadierCommand.requiredArgumentBuilder(this.SERVER_ARGUMENT, StringArgumentType.string())
					.then(BrigadierCommand.requiredArgumentBuilder(this.UNIVERSE_ARGUMENT, StringArgumentType.string())
						.executes { context -> execute(context, context.source as Player) }
						.then(BrigadierCommand.requiredArgumentBuilder(this.CHILDREN_ARGUMENT, StringArgumentType.greedyString())
							.executes { context -> execute(context, context.source as Player) }))))
	}
}
