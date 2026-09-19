package im.apeki.qolmechanics.features.frames

import im.apeki.qolmechanics.QoLMechanicsPlugin
import org.bukkit.Material

class FrameModule(private val plugin: QoLMechanicsPlugin) {

    fun init() {
        val cfg = plugin.config

        val framesSection = cfg.getConfigurationSection("frames") ?: run {
            plugin.logger.warning("Секция 'frames' не найдена в config.yml!")
            return
        }

        val config = FrameConfig(
            enabled = framesSection.getBoolean("enabled", true),
            requireShift = framesSection.getBoolean("require-shift", true),
            invisible = framesSection.getString("tools.invisible", "SHEARS")!!.uppercase(),
            visible = framesSection.getString("tools.visible", "LEATHER")!!.uppercase(),
            fix = framesSection.getString("tools.fix", ".*GLASS_PANE")!!.uppercase(),
            unfix = framesSection.getString("tools.unfix", "SHEARS")!!.uppercase(),
            soundEnabled = framesSection.getBoolean("sound.enabled", true)
        )

        if (!config.enabled) return

        val cachedInvisible = cacheMaterials(config.invisible)
        val cachedVisible = cacheMaterials(config.visible)
        val cachedFix = cacheMaterials(config.fix)
        val cachedUnfix = cacheMaterials(config.unfix)

        val service = FrameService(config, cachedInvisible, cachedVisible, cachedFix, cachedUnfix)
        val listener = FrameListener(service)

        plugin.server.pluginManager.registerEvents(listener, plugin)
    }

    private fun cacheMaterials(regexString: String): Set<Material> {
        val regex = regexString.toRegex()
        return Material.entries.filter { it.name.matches(regex) }.toSet()
    }
}