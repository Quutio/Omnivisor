package io.quut.omnivisor.api

import io.quut.fusion.api.connection.IConnectionRequestParameters
import net.kyori.adventure.key.Key

interface IOmnivisor
{
	fun connectionRequestParameters(id: Int): IConnectionRequestParameters
	fun connectionRequestParameters(universe: Key, vararg children: Key): IConnectionRequestParameters
}
