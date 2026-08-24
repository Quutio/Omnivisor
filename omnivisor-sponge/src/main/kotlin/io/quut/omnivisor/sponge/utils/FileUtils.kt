package io.quut.omnivisor.sponge.utils

import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.useDirectoryEntries

internal object FileUtils
{
	fun deleteAndCleanupDirectories(root: Path, file: Path)
	{
		file.deleteIfExists()

		var target: Path = file.parent ?: return
		while (target.startsWith(root) && target != root && target.useDirectoryEntries { s -> s.none() })
		{
			target.deleteIfExists()
			target = target.parent ?: return
		}
	}
}
