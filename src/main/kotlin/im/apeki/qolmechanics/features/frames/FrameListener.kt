package im.apeki.qolmechanics.features.frames

import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent

class FrameListener(private val service: FrameService) : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onRightClickFrame(event: PlayerInteractEntityEvent) {
        val frame = event.rightClicked as? ItemFrame ?: return
        val player = event.player
        val tool = player.inventory.itemInMainHand

        if (service.handleTool(player, frame, tool)) {
            event.isCancelled = true
            return
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onLeftClickFrame(event: EntityDamageByEntityEvent) {
        val frame = event.entity as? ItemFrame ?: return
        val player = event.damager as? Player ?: return

        if (service.handleQuickExtract(player, frame)) {
            event.isCancelled = true
        }
    }
}