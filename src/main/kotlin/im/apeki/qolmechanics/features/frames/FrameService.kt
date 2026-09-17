package im.apeki.qolmechanics.features.frames

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

class FrameService(private val config: FrameConfig) {

    fun handleTool(player: Player, frame: ItemFrame, tool: ItemStack): Boolean {
        if (config.requireShift && !player.isSneaking) return false

        val toolName = tool.type.name

        if (toolName == config.toolUnfix && frame.isFixed) {
            frame.isFixed = false
            consumeItem(tool)
            playSound(player)
            return true
        }

        val isFixTool = config.fixTools.any { pattern ->
            if (pattern.startsWith("*")) {
                toolName.endsWith(pattern.drop(1))
            } else {
                toolName == pattern
            }
        }
        if (isFixTool && !frame.isFixed) {
            frame.isFixed = true
            consumeItem(tool)
            playSound(player)
            return true
        }

        if (toolName == config.toolVisible && !frame.isVisible) {
            frame.isVisible = true
            consumeItem(tool)
            playSound(player)
            return true
        }

        if (toolName == config.toolInvisible && frame.isVisible) {
            frame.isVisible = false
            consumeItem(tool)
            playSound(player)
            return true
        }

        return false
    }

    fun handleQuickExtract(player: Player, frame: ItemFrame): Boolean {
        if (config.requireShift && !player.isSneaking) return false
        if (player.inventory.itemInMainHand.type != Material.AIR) return false

        val itemInFrame = frame.item
        if (itemInFrame.type == Material.AIR) return false

        frame.setItem(null)
        player.inventory.setItemInMainHand(itemInFrame)
        playSound(player)
        return true
    }

    private fun consumeItem(item: ItemStack) {
        if (item.type.maxDurability > 0) {
            val meta = item.itemMeta
            if (meta is Damageable) {
                meta.damage += 1
                if (meta.damage >= item.type.maxDurability) {
                    item.amount = 0
                } else {
                    item.itemMeta = meta
                }
            }
        } else {
            item.amount -= 1
        }
    }

    private fun playSound(player: Player) {
        if (config.soundEnabled) {
            player.playSound(player.location, Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 0.8f, 1.2f)
        }
    }
}