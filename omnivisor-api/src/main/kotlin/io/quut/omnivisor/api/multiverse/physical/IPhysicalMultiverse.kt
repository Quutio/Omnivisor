package io.quut.omnivisor.api.multiverse.physical

import io.quut.omnivisor.api.multiverse.IMultiverse
import io.quut.omnivisor.api.universe.physical.IPhysicalUniverse

interface IPhysicalMultiverse<T> : IMultiverse<T>, IPhysicalUniverse<T>
