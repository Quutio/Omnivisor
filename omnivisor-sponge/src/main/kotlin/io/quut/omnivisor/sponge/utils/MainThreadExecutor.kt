package io.quut.omnivisor.sponge.utils

import org.spongepowered.api.Server
import org.spongepowered.api.scheduler.TaskExecutorService
import org.spongepowered.api.scheduler.TaskFuture
import java.util.concurrent.Executor
import java.util.function.BooleanSupplier

internal class MainThreadExecutor(private val server: Server, private val executor: TaskExecutorService): Executor
{
	private val tasks: LinkedHashSet<Task> = LinkedHashSet()

	override fun execute(command: Runnable)
	{
		if (this.server.onMainThread())
		{
			return command.run()
		}

		synchronized(this.tasks)
		{
			val task = Task(command)

			this.tasks.add(task)

			task.future = this.executor.submit(task)
		}
	}

	internal fun poll(done: BooleanSupplier)
	{
		while (!done.asBoolean)
		{
			val task: Task? = synchronized(this.tasks) { if (this.tasks.isNotEmpty()) this.tasks.removeFirst() else null }
			if (task != null)
			{
				task.poll()
			}
			else
			{
				Thread.sleep(1)
			}
		}
	}

	private inner class Task(private val command: Runnable): Runnable
	{
		lateinit var future: TaskFuture<*>

		override fun run()
		{
			synchronized(this@MainThreadExecutor.tasks)
			{
				this@MainThreadExecutor.tasks.remove(this)
			}

			this.command.run()
		}

		fun poll()
		{
			if (this.future.cancel(false))
			{
				this.command.run()
			}
		}
	}
}
