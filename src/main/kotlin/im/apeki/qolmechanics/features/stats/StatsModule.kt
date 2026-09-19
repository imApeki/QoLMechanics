package im.apeki.qolmechanics.features.stats

import im.apeki.qolmechanics.QoLMechanicsPlugin

class StatsModule(private val plugin: QoLMechanicsPlugin) {
    fun init() {
        StatsService(plugin).start()
    }
}