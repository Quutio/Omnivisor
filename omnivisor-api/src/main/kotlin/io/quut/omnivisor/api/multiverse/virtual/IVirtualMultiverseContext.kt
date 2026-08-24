package io.quut.omnivisor.api.multiverse.virtual

import io.quut.omnivisor.api.multiverse.IMultiverseContext

interface IVirtualMultiverseContext<TConfig, TContainer : IVirtualMultiverseContainer> : IMultiverseContext<TConfig, TContainer>
