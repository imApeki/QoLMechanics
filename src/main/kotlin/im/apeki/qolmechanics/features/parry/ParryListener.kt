package im.apeki.qolmechanics.features.parry

import org.bukkit.Tag
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class ParryListener(private val service: ParryService) : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerHit(event: EntityDamageByEntityEvent) {
        val defender = event.entity as? Player ?: return
        val attacker = event.damager as? LivingEntity ?: return
        
        if (!defender.isBlocking) return

        val type = attacker.equipment?.itemInMainHand?.type

        if (type != null && Tag.ITEMS_AXES.isTagged(type)) {
            return
        }

        if (service.tryParry(defender, attacker, event)) {
            event.isCancelled = true
        }
    }
}