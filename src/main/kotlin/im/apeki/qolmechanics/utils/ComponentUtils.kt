package im.apeki.qolmechanics.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

fun String.toComponent(): Component {
    return if (this.contains("&") || this.contains("§")) {
        LegacyComponentSerializer.legacyAmpersand().deserialize(this)
    } else {
        MiniMessage.miniMessage().deserialize(this)
    }
}
