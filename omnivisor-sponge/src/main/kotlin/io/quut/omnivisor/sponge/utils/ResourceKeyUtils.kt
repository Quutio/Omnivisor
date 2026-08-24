package io.quut.omnivisor.sponge.utils

import org.spongepowered.api.ResourceKey
import java.util.concurrent.ThreadLocalRandom

internal object ResourceKeyUtils
{
	fun randomResourceKey(namespace: String, prefix: String?, suffix: String?): ResourceKey
	{
		val random: String = java.lang.Long.toUnsignedString(ThreadLocalRandom.current().nextLong())

		val stringBuilder = StringBuilder(random.length + (prefix?.length?.plus(1) ?: 0) + (suffix?.length?.plus(1) ?: 0))
		if (prefix != null)
		{
			stringBuilder.append(prefix)
			stringBuilder.append('-')
		}

		stringBuilder.append(random)

		if (suffix != null)
		{
			stringBuilder.append('-')
			stringBuilder.append(suffix)
		}

		return ResourceKey.of(namespace, stringBuilder.toString())
	}
}
