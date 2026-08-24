package io.quut.omnivisor.api.universe

interface IUniverse<T> : IUniverseLike
{
	val info: IUniverseInfo

	val instance: T
}
