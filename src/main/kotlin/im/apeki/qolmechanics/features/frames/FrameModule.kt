package im.apeki.qolmechanics.features.frames

import im.apeki.qolmechanics.QoLMechanicsPlugin

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
            toolInvisible = framesSection.getString("tools.invisible", "SHEARS")!!.uppercase(),
            toolVisible = framesSection.getString("tools.visible", "LEATHER")!!.uppercase(),
            fixTools = framesSection.getStringList("tools.fix").map { it.uppercase() }.takeIf { it.isNotEmpty() } ?: listOf("*_GLASS_PANE"),
            toolUnfix = framesSection.getString("tools.unfix", "SHEARS")!!.uppercase(),
            soundEnabled = framesSection.getBoolean("sound.enabled", true)
        )

        if (!config.enabled) return

        val service = FrameService(config)
        val listener = FrameListener(service)

        plugin.server.pluginManager.registerEvents(listener, plugin)
    }
}