package io.quut.omnivisor.sponge.options

import net.kyori.option.Option
import net.kyori.option.value.ValueSource
import org.spongepowered.configurate.ConfigurationNode

internal class NodeValueSource(private val node: ConfigurationNode) : ValueSource
{
	override fun <T : Any> value(option: Option<T>): T? = this.node.node(option.id())[option.valueType().type()]
}
