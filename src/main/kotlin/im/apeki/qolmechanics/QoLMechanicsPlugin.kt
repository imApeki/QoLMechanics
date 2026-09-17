package im.apeki.qolmechanics

import im.apeki.qolmechanics.features.frames.FrameModule
import im.apeki.qolmechanics.features.parry.ParryModule
import org.bukkit.plugin.java.JavaPlugin

class QoLMechanicsPlugin : JavaPlugin() {

    override fun onEnable() {
        saveDefaultConfig()
        config.options().copyDefaults(true)
        saveConfig()

        ParryModule(this).init()
        FrameModule(this).init()
    }
}