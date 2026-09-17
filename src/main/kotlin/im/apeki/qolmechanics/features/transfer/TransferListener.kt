package im.apeki.qolmechanics.features.transfer

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent

class TransferListener(private val service: TransferService) : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onDrop(event: PlayerDropItemEvent) {
        val player = event.player
        val itemStack = event.itemDrop.itemStack

        if (service.handleDrop(player, itemStack)) {
            event.itemDrop.remove()
        }
    }
}
