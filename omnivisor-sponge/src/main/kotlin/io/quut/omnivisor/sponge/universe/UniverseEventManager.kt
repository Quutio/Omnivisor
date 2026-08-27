package io.quut.omnivisor.sponge.universe

import com.google.inject.Inject
import io.quut.harmony.api.IHarmonyEventManager
import io.quut.harmony.api.IHarmonyEventManager.IBuilder.Companion.mapping
import io.quut.omnivisor.api.domain.IUniverseDomain
import io.quut.omnivisor.api.universe.IUniverseContainer
import io.quut.omnivisor.api.universe.IUniverseInfo
import io.quut.omnivisor.sponge.user.SpongeUserManager
import net.kyori.adventure.key.Key
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.event.action.FishingEvent
import org.spongepowered.api.event.action.LightningEvent
import org.spongepowered.api.event.action.SleepingEvent
import org.spongepowered.api.event.advancement.AdvancementEvent
import org.spongepowered.api.event.block.ChangeBlockEvent
import org.spongepowered.api.event.block.CollideBlockEvent
import org.spongepowered.api.event.block.InteractBlockEvent
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent
import org.spongepowered.api.event.block.ScheduleBlockUpdateEvent
import org.spongepowered.api.event.block.TickBlockEvent
import org.spongepowered.api.event.block.entity.BrewingEvent
import org.spongepowered.api.event.block.entity.ChangeSignEvent
import org.spongepowered.api.event.block.entity.CookingEvent
import org.spongepowered.api.event.command.ExecuteCommandEvent
import org.spongepowered.api.event.data.ChangeDataHolderEvent
import org.spongepowered.api.event.entity.AffectEntityEvent
import org.spongepowered.api.event.entity.BreedingEvent
import org.spongepowered.api.event.entity.ChangeEntityEquipmentEvent
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent
import org.spongepowered.api.event.entity.ConstructEntityEvent
import org.spongepowered.api.event.entity.DamageCalculationEvent
import org.spongepowered.api.event.entity.DestructEntityEvent
import org.spongepowered.api.event.entity.ExpireEntityEvent
import org.spongepowered.api.event.entity.HarvestEntityEvent
import org.spongepowered.api.event.entity.IgniteEntityEvent
import org.spongepowered.api.event.entity.InteractEntityEvent
import org.spongepowered.api.event.entity.InvokePortalEvent
import org.spongepowered.api.event.entity.ItemMergeWithItemEvent
import org.spongepowered.api.event.entity.LeashEntityEvent
import org.spongepowered.api.event.entity.MoveEntityEvent
import org.spongepowered.api.event.entity.RideEntityEvent
import org.spongepowered.api.event.entity.RotateEntityEvent
import org.spongepowered.api.event.entity.TameEntityEvent
import org.spongepowered.api.event.entity.UnleashEntityEvent
import org.spongepowered.api.event.entity.ai.SetAITargetEvent
import org.spongepowered.api.event.entity.ai.goal.GoalEvent
import org.spongepowered.api.event.entity.explosive.DefuseExplosiveEvent
import org.spongepowered.api.event.entity.explosive.DetonateExplosiveEvent
import org.spongepowered.api.event.entity.explosive.PrimeExplosiveEvent
import org.spongepowered.api.event.entity.living.AnimateHandEvent
import org.spongepowered.api.event.entity.living.player.CooldownEvent
import org.spongepowered.api.event.entity.living.player.KickPlayerEvent
import org.spongepowered.api.event.entity.living.player.PlayerChangeClientSettingsEvent
import org.spongepowered.api.event.entity.living.player.ResourcePackStatusEvent
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent
import org.spongepowered.api.event.item.inventory.DropItemEvent
import org.spongepowered.api.event.item.inventory.InteractItemEvent
import org.spongepowered.api.event.item.inventory.TransferInventoryEvent
import org.spongepowered.api.event.item.inventory.UpdateAnvilEvent
import org.spongepowered.api.event.item.inventory.UseItemStackEvent
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent
import org.spongepowered.api.event.message.PlayerChatEvent
import org.spongepowered.api.event.network.ServerSideConnectionEvent
import org.spongepowered.api.event.sound.PlaySoundEvent
import org.spongepowered.api.event.world.ChangeWeatherEvent
import org.spongepowered.api.event.world.ChangeWorldBorderEvent
import org.spongepowered.api.event.world.ExplosionEvent
import org.spongepowered.api.event.world.LoadWorldEvent
import org.spongepowered.api.event.world.SaveWorldEvent
import org.spongepowered.api.event.world.UnloadWorldEvent
import org.spongepowered.api.event.world.chunk.ChunkEvent
import org.spongepowered.api.world.Locatable
import org.spongepowered.api.world.server.ServerWorld
import org.spongepowered.plugin.PluginContainer
import java.lang.invoke.MethodHandles
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.jvm.optionals.getOrNull

internal class UniverseEventManager @Inject constructor(
	private val container: PluginContainer,
	private val userManager: SpongeUserManager)
{
	private val worlds: ConcurrentMap<Key, IUniverseInfo> = ConcurrentHashMap()

	private lateinit var eventManager: IHarmonyEventManager<IUniverseInfo>

	internal fun init()
	{
		this.eventManager = IHarmonyEventManager.builder<IUniverseInfo>(this.container)
			.mapping { e: AdvancementEvent -> this.worlds[e.player().serverLocation().worldKey()] }
			.mapping { e: AffectEntityEvent -> this.worlds[e.entities().first().serverLocation().worldKey()] }
			.mapping { e: AnimateHandEvent -> this.worlds[e.humanoid().serverLocation().worldKey()] }
			.mapping { e: BreedingEvent.Breed -> this.worlds[e.offspringEntity().serverLocation().worldKey()] }
			.mapping { e: BreedingEvent.FindMate -> this.worlds[e.matingEntity().serverLocation().worldKey()] }
			.mapping { e: BreedingEvent.ReadyToMate -> this.worlds[e.entity().serverLocation().worldKey()] }
			.mapping { e: BrewingEvent -> this.worlds[e.brewingStand().serverLocation().worldKey()] }
			.mapping { e: ChangeBlockEvent -> this.worlds[e.world().key()] }
			.mapping { e: ChangeDataHolderEvent -> (e.targetHolder() as? Entity)?.let { e -> this.worlds[e.serverLocation().worldKey()] } }
			.mapping { e: ChangeEntityEquipmentEvent -> this.worlds[e.entity().serverLocation().worldKey()] }
			.mapping { e: ChangeEntityWorldEvent -> this.worlds[e.originalWorld().key()] }
			.mapping { e: ChangeInventoryEvent -> (e.cause().first(Entity::class.java)).getOrNull()?.let { e -> this.worlds[e.serverLocation().worldKey()] } }
			.mapping { e: ChangeSignEvent -> this.worlds[e.sign().serverLocation().worldKey()] }
			.mapping { e: ChangeWeatherEvent -> (e.universe() as? ServerWorld)?.let { e -> this.worlds[e.key()] } }
			.mapping { e: ChangeWorldBorderEvent.Player -> this.worlds[e.player().serverLocation().worldKey()] }
			.mapping { e: ChangeWorldBorderEvent.World -> this.worlds[e.world().key()] }
			.mapping { e: ChunkEvent.WorldScoped -> this.worlds[e.worldKey()] }
			.mapping { e: CollideBlockEvent -> this.worlds[e.targetLocation().worldKey()] }
			.mapping { e: ConstructEntityEvent -> this.worlds[e.location().worldKey()] }
			.mapping { e: CookingEvent -> this.worlds[e.blockEntity().serverLocation().worldKey()] }
			.mapping { e: CooldownEvent -> this.worlds[e.player().serverLocation().worldKey()] }
			.mapping { e: DamageCalculationEvent -> this.worlds[e.entity().serverLocation().worldKey()] }
			.mapping { e: DefuseExplosiveEvent -> this.worlds[e.fusedExplosive().serverLocation().worldKey()] }
			.mapping { e: DestructEntityEvent -> this.worlds[e.entity().serverLocation().worldKey()] }
			.mapping { e: DetonateExplosiveEvent -> this.worlds[e.explosive().serverLocation().worldKey()] }
			.mapping { e: DropItemEvent -> (e.cause().first(Entity::class.java)).getOrNull()?.let { e -> this.worlds[e.serverLocation().worldKey()] } }
			.mapping { e: ExecuteCommandEvent -> e.commandCause().location().getOrNull()?.let { l -> this.worlds[l.worldKey()] } }
			.mapping { e: ExpireEntityEvent -> this.worlds[e.entity().serverLocation().worldKey()] }
			.mapping { e: ExplosionEvent -> this.worlds[e.explosion().serverLocation().worldKey()] }
			.mapping { e: FishingEvent -> this.worlds[e.fishHook().serverLocation().worldKey()] }
			.mapping { e: GoalEvent -> this.worlds[e.agent().serverLocation().worldKey()] }
			.mapping { e: HarvestEntityEvent -> this.worlds[e.entity().serverLocation().worldKey()] }
			.mapping { e: IgniteEntityEvent -> this.worlds[e.entity().serverLocation().worldKey()] }
			.mapping { e: InteractBlockEvent -> this.worlds[e.block().world()] }
			.mapping { e: InteractContainerEvent -> this.worlds[e.container().viewer().serverLocation().worldKey()] }
			.mapping { e: InteractEntityEvent -> this.worlds[e.entity().serverLocation().worldKey()] }
			.mapping { e: InteractItemEvent -> (e.cause().first(Entity::class.java)).getOrNull()?.let { e -> this.worlds[e.serverLocation().worldKey()] } }
			.mapping { e: InvokePortalEvent -> this.worlds[e.entity().serverLocation().worldKey()] }
			.mapping { e: ItemMergeWithItemEvent -> this.worlds[e.item().serverLocation().worldKey()] }
			.mapping { e: KickPlayerEvent -> this.worlds[e.player().serverLocation().worldKey()] }
			.mapping { e: LeashEntityEvent -> this.worlds[e.entity().serverLocation().worldKey()] }
			.mapping { e: LightningEvent -> (e.cause().first(Entity::class.java)).getOrNull()?.let { e -> this.worlds[e.serverLocation().worldKey()] } }
			.mapping { e: LoadWorldEvent -> this.worlds[e.world().key()] }
			.mapping { e: MoveEntityEvent -> this.worlds[e.entity().serverLocation().worldKey()] }
			.mapping { e: NotifyNeighborBlockEvent -> this.worlds[e.tickets().first().target().world()] }
			.mapping { e: PlaySoundEvent -> this.worlds[e.location().worldKey()] }
			.mapping { e: PlayerChangeClientSettingsEvent -> this.worlds[e.player().serverLocation().worldKey()] }
			.mapping { e: PlayerChatEvent -> e.player().getOrNull()?.let { p -> this.worlds[p.serverLocation().worldKey()] } }
			.mapping { e: PrimeExplosiveEvent -> this.worlds[e.fusedExplosive().serverLocation().worldKey()] }
			.mapping { e: ResourcePackStatusEvent -> this.userManager.get(e.connection()) }
			.mapping { e: RideEntityEvent -> this.worlds[e.entity().serverLocation().worldKey()] }
			.mapping { e: RotateEntityEvent -> this.worlds[e.entity().serverLocation().worldKey()] }
			.mapping { e: SaveWorldEvent -> this.worlds[e.world().key()] }
			.mapping { e: ScheduleBlockUpdateEvent<*> -> this.worlds[e.tickets().first().block().serverLocation().worldKey()] }
			.mapping { e: ServerSideConnectionEvent -> this.userManager.get(e.connection()) }
			.mapping { e: SetAITargetEvent -> this.worlds[e.agent().serverLocation().worldKey()] }
			.mapping { e: SleepingEvent -> this.worlds[e.bed().world()] }
			.mapping { e: TameEntityEvent -> this.worlds[e.entity().serverLocation().worldKey()] }
			.mapping { e: TickBlockEvent -> this.worlds[e.block().world()] }
			.mapping { e: TransferInventoryEvent -> this.worlds[((e.sourceInventory() as Locatable).world() as ServerWorld).key()] }
			.mapping { e: UnleashEntityEvent -> this.worlds[e.entity().serverLocation().worldKey()] }
			.mapping { e: UnloadWorldEvent -> this.worlds[e.world().key()] }
			.mapping { e: UpdateAnvilEvent -> this.worlds[((e.inventory() as Locatable).world() as ServerWorld).key()] }
			.mapping { e: UseItemStackEvent -> (e.cause().first(Entity::class.java)).getOrNull()?.let { e -> this.worlds[e.serverLocation().worldKey()] } }
			.build()
	}

	internal fun registerListeners(container: IUniverseContainer, plugin: PluginContainer, listener: Any, lookup: MethodHandles.Lookup?)
	{
		this.eventManager.registerListeners(container.info, plugin, listener, lookup)
	}

	internal fun registerArea(container: IUniverseContainer, area: IUniverseDomain)
	{
		if (area is IUniverseDomain.IWorld)
		{
			this.worlds[area.worldKey] = container.info
		}
		else if (area is IUniverseDomain.ICompound)
		{
			area.scopes.forEach { area -> this.registerArea(container, area) }
		}
	}

	internal fun unregisterListeners(container: IUniverseContainer)
	{
		this.eventManager.unregisterListeners(container.info)
	}

	internal fun unregisterListeners(container: IUniverseContainer, plugin: PluginContainer)
	{
		this.eventManager.unregisterListeners(container.info, plugin)
	}

	internal fun unregisterListeners(container: IUniverseContainer, plugin: PluginContainer, listener: Any)
	{
		this.eventManager.unregisterListeners(container.info, plugin, listener)
	}

	internal fun unregisterArea(container: IUniverseContainer, area: IUniverseDomain)
	{
		if (area is IUniverseDomain.IWorld)
		{
			this.worlds.remove(area.worldKey)
		}
		else if (area is IUniverseDomain.ICompound)
		{
			area.scopes.forEach { area -> this.unregisterArea(container, area) }
		}
	}
}
