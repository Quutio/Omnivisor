package io.quut.omnivisor.api.universe

import net.kyori.option.OptionSchema
import net.kyori.option.OptionState
import java.util.ServiceLoader
import java.util.function.Function

interface IUniverseArchetype<TInstance, TConfig> : IUniverseTemplate<TInstance, TConfig>
{
	val options: OptionSchema

	fun loadConfig(state: OptionState): TConfig

	companion object
	{
		@JvmStatic
		fun <TInstance, TConfig> builder(): IBuilder<TInstance, TConfig>
		{
			val factory: IFactory = ServiceLoader.load(IFactory::class.java).findFirst().orElseThrow() as IFactory
			return factory.builder()
		}
	}

	interface IBuilder<TInstance, TConfig> : IUniverseTemplate.IBuilder<TInstance, TConfig, IUniverseArchetype<TInstance, TConfig>>
	{
		fun options(schema: OptionSchema): IBuilder<TInstance, TConfig>

		fun configLoader(function: Function<OptionState, TConfig>): IBuilder<TInstance, TConfig>

		fun dynamic(function: Function<TConfig, IUniverseTemplate<TInstance, TConfig>>): IUniverseArchetype<TInstance, TConfig>
	}

	interface IFactory
	{
		fun <TInstance, TConfig> builder(): IBuilder<TInstance, TConfig>
	}
}
