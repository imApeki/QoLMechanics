package im.apeki.qolmechanics.features.transfer

import im.apeki.qolmechanics.QoLMechanicsPlugin

class TransferModule(private val plugin: QoLMechanicsPlugin) {

    fun init() {
        val cfg = plugin.config

        val transferSection = cfg.getConfigurationSection("transfer") ?: run {
            return
        }

        val config = TransferConfig(
            enabled = transferSection.getBoolean("enabled", true),
            maxDistance = transferSection.getDouble("max-distance", 3.0),
            messageSenderEnabled = transferSection.getBoolean("messages.sender.enabled", true),
            messageSender = transferSection.getString("messages.sender.text", "<green>Вы передали <item> x<amount> игроку <receiver>")!!,
            messageReceiverEnabled = transferSection.getBoolean("messages.receiver.enabled", true),
            messageReceiver = transferSection.getString("messages.receiver.text", "<green>Игрок <sender> передал вам <item> x<amount>")!!,
            soundEnabled = transferSection.getBoolean("sound.enabled", true)
        )

        if (!config.enabled) return

        val service = TransferService(config)
        val listener = TransferListener(service)

        plugin.server.pluginManager.registerEvents(listener, plugin)
    }
}
