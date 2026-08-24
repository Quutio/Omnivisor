package io.quut.omnivisor.api.multiverse.physical

import io.quut.omnivisor.api.multiverse.IMultiverseContext
import io.quut.omnivisor.api.universe.physical.IPhysicalUniverseContext

interface IPhysicalMultiverseContext<TConfig, TContainer : IPhysicalMultiverseContainer> : IMultiverseContext<TConfig, TContainer>, IPhysicalUniverseContext<TConfig, TContainer>
