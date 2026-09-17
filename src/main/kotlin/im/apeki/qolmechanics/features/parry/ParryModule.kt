package im.apeki.qolmechanics.features.parry

import im.apeki.qolmechanics.QoLMechanicsPlugin

class ParryModule(private val plugin: QoLMechanicsPlugin) {

    fun init() {
        val cfg = plugin.config

        val parrySection = cfg.getConfigurationSection("parry") ?: run {
            return
        }

        val config = ParryConfig(
            enabled = parrySection.getBoolean("enabled", true),
            windowMs = parrySection.getLong("window-ms", 250L),
            knockbackPower = parrySection.getDouble("knockback-power", 1.2),
            stunDurationTicks = parrySection.getInt("stun-duration-ticks", 60),
            soundEnabled = parrySection.getBoolean("sound.enabled", true),
            soundType = parrySection.getString("sound.type", "ITEM_SHIELD_BLOCK")!!,
            soundVolume = parrySection.getDouble("sound.volume", 1.0).toFloat(),
            soundPitch = parrySection.getDouble("sound.pitch", 0.8).toFloat(),
            particleEnabled = parrySection.getBoolean("particle.enabled", true),
            particleType = parrySection.getString("particle.type", "DUST_PILLAR")!!.uppercase(),
            particleCount = parrySection.getInt("particle.count", 15)
        )

        if (!config.enabled) return

        val service = ParryService(config, plugin)
        val listener = ParryListener(service)

        plugin.server.pluginManager.registerEvents(listener, plugin)
    }
}