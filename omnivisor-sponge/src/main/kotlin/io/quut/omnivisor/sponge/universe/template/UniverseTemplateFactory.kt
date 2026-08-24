package io.quut.omnivisor.sponge.universe.template

import io.quut.omnivisor.api.universe.IUniverseTemplate

internal class UniverseTemplateFactory : IUniverseTemplate.IFactory
{
	override fun <TInstance, TConfig> builder(): IUniverseTemplate.IBuilder<TInstance, TConfig, IUniverseTemplate<TInstance, TConfig>> =
		UniverseTemplateBuilder()
}
