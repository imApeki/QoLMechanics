package im.apeki.qolmechanics

import im.apeki.qolmechanics.commands.QolCommand
import im.apeki.qolmechanics.features.frames.FrameModule
import im.apeki.qolmechanics.features.stats.StatsModule
import im.apeki.qolmechanics.features.parry.ParryModule
import im.apeki.qolmechanics.features.transfer.TransferModule
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin

class QoLMechanicsPlugin : JavaPlugin() {

    override fun onEnable() {
        saveDefaultConfig()
        config.options().copyDefaults(true)
        saveConfig()

        initModules()
        StatsModule(this).init()

        getCommand("qol")?.apply {
            val qolCommand = QolCommand(this@QoLMechanicsPlugin)
            setExecutor(qolCommand)
            tabCompleter = qolCommand
        }
    }

    fun reloadModules() {
        HandlerList.unregisterAll(this)
        initModules()
    }

    private fun initModules() {
        ParryModule(this).init()
        FrameModule(this).init()
        TransferModule(this).init()
    }
}