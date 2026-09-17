package im.apeki.qolmechanics.features.transfer

import im.apeki.qolmechanics.utils.toComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class TransferService(private val config: TransferConfig) {

    fun handleDrop(sender: Player, item: ItemStack): Boolean {
        if (!sender.isSneaking) return false

        val rayTraceResult = sender.world.rayTraceEntities(
            sender.eyeLocation,
            sender.eyeLocation.direction,
            config.maxDistance,
            { it is Player && it != sender }
        )

        val receiver = rayTraceResult?.hitEntity as? Player ?: return false

        val leftover = receiver.inventory.addItem(item)
        
        if (leftover.isNotEmpty()) {
            val amountAdded = item.amount - leftover.values.sumOf { it.amount }
            if (amountAdded <= 0) return false
            
            val clonedItem = item.clone()
            clonedItem.amount = amountAdded
            sendMessagesAndSound(sender, receiver, clonedItem)
            
            item.amount = leftover.values.first().amount
            return false 
        }

        sendMessagesAndSound(sender, receiver, item)
        return true
    }

    private fun sendMessagesAndSound(sender: Player, receiver: Player, item: ItemStack) {
        val amount = item.amount.toString()
        val itemComponent = if (item.itemMeta?.hasDisplayName() == true) {
            item.itemMeta!!.displayName() ?: Component.translatable(item.translationKey())
        } else {
            Component.translatable(item.translationKey())
        }

        if (config.messageSenderEnabled) {
            val msg = config.messageSender
                .replace("<receiver>", receiver.name)
                .replace("<amount>", amount)
            
            val comp = msg.toComponent().replaceText(
                TextReplacementConfig.builder()
                    .matchLiteral("<item>")
                    .replacement(itemComponent)
                    .build()
            )
            sender.sendMessage(comp)
        }

        if (config.messageReceiverEnabled) {
            val msg = config.messageReceiver
                .replace("<sender>", sender.name)
                .replace("<amount>", amount)
            
            val comp = msg.toComponent().replaceText(
                TextReplacementConfig.builder()
                    .matchLiteral("<item>")
                    .replacement(itemComponent)
                    .build()
            )
            receiver.sendMessage(comp)
        }

        if (config.soundEnabled) {
            sender.playSound(sender.location, Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f)
            receiver.playSound(receiver.location, Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f)
        }
    }
}
