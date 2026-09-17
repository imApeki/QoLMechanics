package im.apeki.qolmechanics.features.parry

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

class ParryListener(private val service: ParryService) : Listener {

    @EventHandler
    fun onShieldRaise(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            val player = event.player

            val mainHand = player.inventory.itemInMainHand.type
            val offHand = player.inventory.itemInOffHand.type

            if (mainHand == Material.SHIELD || offHand == Material.SHIELD) {
                service.markShieldRaised(player)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerHit(event: EntityDamageByEntityEvent) {
        val defender = event.entity as? Player ?: return
        val attacker = event.damager as? LivingEntity ?: return
        val type = attacker.equipment?.itemInMainHand?.type

        if (type != null && Tag.ITEMS_AXES.isTagged(type)) {
            return
        }

        if (service.tryParry(defender, attacker)) {
            event.isCancelled = true
        }
    }
}