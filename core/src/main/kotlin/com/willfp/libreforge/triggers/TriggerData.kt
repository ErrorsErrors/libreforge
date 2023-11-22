package com.willfp.libreforge.triggers

import com.willfp.eco.core.items.HashedItem
import com.willfp.libreforge.Dispatcher
import com.willfp.libreforge.EmptyProvidedHolder
import com.willfp.libreforge.GlobalDispatcher
import com.willfp.libreforge.ProvidedHolder
import com.willfp.libreforge.getProvider
import com.willfp.libreforge.toDispatcher
import com.willfp.libreforge.triggers.impl.TriggerBlank
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.Objects

data class TriggerData(
    /*
    In order to get the holder and dispatcher from the trigger data without
    having to pass them around everywhere, we just pass them in
    Trigger#dispatch by copying over the trigger data.
     */
    val holder: ProvidedHolder = EmptyProvidedHolder,
    val dispatcher: Dispatcher<*> = GlobalDispatcher,

    val player: Player? = null,
    val victim: LivingEntity? = null,
    val block: Block? = null,
    val event: Event? = null,
    val location: Location? = victim?.location ?: player?.location,
    val projectile: Projectile? = null,
    val velocity: Vector? = player?.velocity ?: victim?.velocity,
    val item: ItemStack? = player?.inventory?.itemInMainHand ?: victim?.equipment?.itemInMainHand,
    val text: String? = null,
    val value: Double = 1.0,

    /*
    This is a bodge inherited from v3, but it's the only real way to do this.
    Essentially, the player can get messed up by mutators, and that causes
    placeholders to parse incorrectly when doing Config#get<x>FromExpression.

    It's really not very nice, but it's good enough. Just don't think about it.
     */
    internal val _originalPlayer: Player? = player,
) {
    val foundItem: ItemStack?
        get() = holder.getProvider() ?: item

    /**
     * Turn into a dispatched trigger for a [player].
     */
    @Deprecated(
        "Use dispatch(dispatcher) instead",
        ReplaceWith("dispatch(player.toDispatcher())"),
        DeprecationLevel.ERROR
    )
    fun dispatch(player: Player) =
        dispatch(player.toDispatcher())

    /**
     * Turn into a dispatched trigger for a new [dispatcher].
     */
    fun dispatch(dispatcher: Dispatcher<*>) = DispatchedTrigger(
        dispatcher,
        TriggerBlank,
        this
    )

    override fun hashCode(): Int {
        return Objects.hash(
            holder,
            player,
            victim,
            block,
            event,
            location,
            projectile,
            velocity,
            item?.let { HashedItem.of(it) },
            text,
            value
        )
    }

    override fun equals(other: Any?): Boolean {
        return other is TriggerData && other.hashCode() == this.hashCode()
    }


    /*
    This is *horrible*, but it's the only way to make trigger data work nicely with
    versions pre-4.45.0. It's a bodge, but it works.

    "Why not just use @JvmOverloads?" That's because kotlin adds two extra parameters
    to the constructor, (named internal1 and internal2 here), and of course DefaultConstructorMarker
    isn't there in secondary constructors (like the ones generated by @JvmOverloads), so instead
    I just have to copy the constructor and add the extra parameters.
     */
    @Suppress("UNUSED_PARAMETER")
    @Deprecated(
        "This constructor is only here for backwards compatibility. Please use the primary constructor instead.",
        ReplaceWith("TriggerData(holder, player, victim, block, event, location, projectile, velocity, item, text, value, _originalPlayer, GlobalDispatcher)"),
        DeprecationLevel.ERROR
    )
    constructor(
        holder: ProvidedHolder?,
        player: Player?,
        victim: LivingEntity?,
        block: Block?,
        event: Event?,
        location: Location?,
        projectile: Projectile?,
        velocity: Vector?,
        item: ItemStack?,
        text: String?,
        value: Double,
        _originalPlayer: Player?,
        internal1: Int,
        internal2: kotlin.jvm.internal.DefaultConstructorMarker?
    ) : this(
        holder ?: EmptyProvidedHolder,
        GlobalDispatcher,
        player,
        victim,
        block,
        event,
        location,
        projectile,
        velocity,
        item,
        text,
        value,
        _originalPlayer
    )
}

enum class TriggerParameter(
    vararg val inheritsFrom: TriggerParameter
) {
    PLAYER,
    VICTIM,
    BLOCK,
    EVENT,
    LOCATION(VICTIM, PLAYER),
    PROJECTILE,
    VELOCITY(PLAYER, VICTIM),
    ITEM(PLAYER, VICTIM),
    TEXT,
    VALUE
}
