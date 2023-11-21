package com.willfp.libreforge.integrations.paper.impl

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent
import com.willfp.libreforge.effects.templates.ChanceMultiplierEffect
import com.willfp.libreforge.EntityDispatcher
import org.bukkit.event.EventHandler

object EffectElytraBoostSaveChance : ChanceMultiplierEffect("elytra_boost_save_chance") {
    @EventHandler(ignoreCancelled = true)
    fun handle(event: PlayerElytraBoostEvent) {
        val player = event.player

        if (passesChance(EntityDispatcher(player))) {
            event.setShouldConsume(false)
        }
    }
}
