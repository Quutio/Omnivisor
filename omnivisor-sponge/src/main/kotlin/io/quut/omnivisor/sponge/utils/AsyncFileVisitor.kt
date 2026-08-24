package io.quut.omnivisor.sponge.utils

import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.CompletableFuture

internal class AsyncFileVisitor<T : Any>(private val callback: (T) -> CompletableFuture<Void>) : SimpleFileVisitor<T>()
{
	private val stack: ArrayDeque<MutableList<CompletableFuture<Void>>> = ArrayDeque(listOf(mutableListOf()))

	val result: CompletableFuture<Void>
		get() = CompletableFuture.allOf(*this.stack.last().toTypedArray())

	override fun preVisitDirectory(directory: T, attrs: BasicFileAttributes): FileVisitResult
	{
		this.stack.add(mutableListOf())

		return FileVisitResult.CONTINUE
	}

	override fun visitFile(file: T, attrs: BasicFileAttributes): FileVisitResult
	{
		this.stack.last().add(this.callback(file))

		return FileVisitResult.CONTINUE
	}

	override fun postVisitDirectory(directory: T, e: IOException?): FileVisitResult
	{
		val combined: CompletableFuture<Void> = CompletableFuture.allOf(*this.stack.removeLast().toTypedArray())

		this.stack.last().add(combined.thenCompose { this.callback(directory) })

		return FileVisitResult.CONTINUE
	}
}
