package io.quut.omnivisor.sponge.world

import com.google.inject.Inject
import io.quut.omnivisor.api.world.IWorldContainer
import io.quut.omnivisor.sponge.SpongeOmnivisorPluginInfo
import io.quut.omnivisor.sponge.utils.AsyncFileVisitor
import io.quut.omnivisor.sponge.utils.CompletableFutureUtils
import io.quut.omnivisor.sponge.utils.DeferredCompletableFuture
import io.quut.omnivisor.sponge.utils.FileUtils
import io.quut.omnivisor.sponge.utils.MainThreadExecutor
import io.quut.omnivisor.sponge.utils.ResourceKeyUtils
import io.quut.omnivisor.sponge.utils.thenAcceptAsync
import io.quut.omnivisor.sponge.utils.thenApplyAsync
import io.quut.omnivisor.sponge.utils.thenComposeAsync
import io.quut.omnivisor.sponge.utils.whenCompleteCompose
import io.quut.omnivisor.sponge.utils.whenExceptionallyCompose
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.spongepowered.api.Game
import org.spongepowered.api.ResourceKey
import org.spongepowered.api.world.server.ServerWorld
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Function
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.jvm.optionals.getOrNull

internal class TransientWorldManager @Inject constructor(
	private val pluginInfo: SpongeOmnivisorPluginInfo,
	private val game: Game)
{
	private val worlds: MutableMap<ResourceKey, TransientWorld> = hashMapOf()

	private lateinit var mainThreadExecutor: MainThreadExecutor
	private lateinit var ioWorker: ExecutorService
	private lateinit var worldLocksDirectory: Path

	internal fun init(mainThreadExecutor: MainThreadExecutor)
	{
		this.mainThreadExecutor = mainThreadExecutor

		this.ioWorker = Executors.newSingleThreadExecutor { r -> Thread(r, "Omnivisor-TransientWorld-IOWorker").apply { this.isDaemon = true } }

		this.worldLocksDirectory = this.pluginInfo.configDirectory.resolve("transient-worlds").resolve("locks")
		this.worldLocksDirectory.createDirectories()

		this.deleteOldWorlds()
	}

	private fun deleteOldWorlds()
	{
		fun iterateNamespace(root: Path)
		{
			val namespace: String = root.name

			val visitor = AsyncFileVisitor<Path>()
			{ p ->
				if (!p.isDirectory())
				{
					val value: String = root.relativize(p).invariantSeparatorsPathString.substringBeforeLast(".")
					val key: ResourceKey = ResourceKey.of(namespace, value)
					val world = TransientWorld(key, p)

					this.worlds[key] = world

					return@AsyncFileVisitor CompletableFuture.runAsync({ world.close() }, this.ioWorker)
				}

				return@AsyncFileVisitor CompletableFuture.runAsync({ p.deleteIfExists() }, this.ioWorker)
			}

			Files.walkFileTree(root, visitor)
		}

		Files.newDirectoryStream(this.worldLocksDirectory, Path::isDirectory)
			.use { stream -> stream.forEach(::iterateNamespace) }
	}

	private fun worldLockDirectory(worldKey: ResourceKey) = this.worldLocksDirectory.resolve(worldKey.namespace())
	private fun worldLockFile(worldKey: ResourceKey) = this.worldLockDirectory(worldKey).resolve("${worldKey.value()}.lock")

	internal fun <T> create(namespace: String, function: Function<Key, CompletableFuture<T>>, prefix: String?, suffix: String?): CompletableFuture<IWorldContainer<T>>
	{
		if (!Key.parseableNamespace(namespace))
		{
			return CompletableFuture.failedFuture(IllegalArgumentException("Invalid namespace"))
		}

		if (prefix != null && !Key.parseableValue(prefix))
		{
			return CompletableFuture.failedFuture(IllegalArgumentException("Invalid prefix"))
		}

		if (suffix != null && !Key.parseableValue(suffix))
		{
			return CompletableFuture.failedFuture(IllegalArgumentException("Invalid suffix"))
		}

		fun generateRandomKey(): ResourceKey
		{
			while (true)
			{
				val key: ResourceKey = ResourceKeyUtils.randomResourceKey(namespace, prefix, suffix)
				if (!this.game.server().worldManager().worldExists(key))
				{
					return key
				}
			}
		}

		fun loadWorld(world: TransientWorld): CompletableFuture<IWorldContainer<T>>
		{
			try
			{
				return function.apply(world.key)
					.thenApply<IWorldContainer<T>> { v -> WorldContainer(v, world) }
					.whenExceptionallyCompose { world.close() }
			}
			catch (e: Throwable)
			{
				return world.close().whenCompleteCompose { _, t -> CompletableFuture.failedFuture(e.apply { t?.let(this::addSuppressed) }) }
			}
		}

		fun acquireLock(failures: Int = 0, lastException: Throwable? = null): CompletableFuture<TransientWorld> =
			CompletableFutureUtils.supplyAsync(this.mainThreadExecutor) { generateRandomKey() }
				.thenApplyAsync(this.ioWorker, this::acquireWorldLock)
				.thenCompose { v -> v?.let(CompletableFuture<*>::completedFuture) ?: TransientWorldManager.retry { acquireLock(failures, lastException) } }
				.exceptionallyCompose()
				{ e ->
					val newException: Throwable = lastException ?: IOException("Failure to acquire the world lock file")
					newException.addSuppressed(e)

					if (failures < 10)
					{
						return@exceptionallyCompose TransientWorldManager.retry { acquireLock(failures + 1, newException) }
					}
					else
					{
						return@exceptionallyCompose CompletableFuture.failedFuture(newException)
					}
				}

		return acquireLock().thenComposeAsync(this.mainThreadExecutor, ::loadWorld)
	}

	private fun acquireWorldLock(key: ResourceKey): TransientWorld?
	{
		val lockFile: Path = this.worldLockFile(key)

		val world = TransientWorld(key, lockFile)
		if (this.worlds.putIfAbsent(key, world) == null)
		{
			try
			{
				lockFile.createParentDirectories().createFile()

				return world
			}
			catch (e: Throwable)
			{
				this.worlds.remove(key, world)

				try
				{
					FileUtils.deleteAndCleanupDirectories(this.worldLocksDirectory, lockFile)
				}
				catch (ex: Throwable)
				{
					e.addSuppressed(ex)
				}

				throw e
			}
		}

		return null
	}

	companion object
	{
		private val DELAY_EXECUTOR: Executor = CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS)

		private fun <T> retry(function: () -> CompletableFuture<T>): CompletableFuture<T> =
			CompletableFutureUtils.supplyAsync(this.DELAY_EXECUTOR) { function() }.thenCompose(Function.identity())
	}

	private inner class TransientWorld(val key: ResourceKey, private val lockFile: Path)
	{
		private val closeFuture: DeferredCompletableFuture = DeferredCompletableFuture(this::close0)

		fun close(): CompletableFuture<Void> = this.closeFuture.get()

		private fun close0(): CompletableFuture<Void> =
			this.deleteWorld().thenAcceptAsync(this@TransientWorldManager.ioWorker) { this.end() }

		private fun deleteWorld(): CompletableFuture<Boolean> =
			CompletableFutureUtils.supplyAsync(this@TransientWorldManager.mainThreadExecutor)
			{
				val world: ServerWorld? = this@TransientWorldManager.game.server().worldManager().world(this.key).getOrNull()
				if (world != null && world.players().isNotEmpty())
				{
					world.players().forEach { p -> p.kick(Component.text("World deletion")) }

					return@supplyAsync TransientWorldManager.retry { deleteWorld() }
				}

				return@supplyAsync this@TransientWorldManager.game.server().worldManager().deleteWorld(this.key)
			}
			.thenCompose(Function.identity())
			.exceptionallyCompose { TransientWorldManager.retry { deleteWorld() } }

		private fun end()
		{
			FileUtils.deleteAndCleanupDirectories(this@TransientWorldManager.worldLocksDirectory, this.lockFile)

			this@TransientWorldManager.worlds.remove(this.key, this)
		}
	}

	private class WorldContainer<T>(override val instance: T, private val world: TransientWorld): IWorldContainer<T>
	{
		override val key: Key
			get() = this.world.key

		override fun close(): CompletableFuture<Void> = this.world.close()
	}
}
