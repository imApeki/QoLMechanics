package im.apeki.qolmechanics.commands

import im.apeki.qolmechanics.QoLMechanicsPlugin
import im.apeki.qolmechanics.utils.toComponent
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class QolCommand(private val plugin: QoLMechanicsPlugin) : CommandExecutor, TabCompleter {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (!sender.hasPermission("qolmechanics.use")) {
            sender.sendMessage("<red>У вас нет прав для использования этой команды".toComponent())
            return true
        }

        if (args.isEmpty() || args[0].lowercase() != "reload") {
            sender.sendMessage("<red>Использование: /qol reload - перезагрузить конфиг".toComponent())
            return true
        }

        plugin.reloadConfig()
        plugin.reloadModules()

        sender.sendMessage("<green>Плагин успешно перезагружен!".toComponent())
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        if (!sender.hasPermission("qolmechanics.use")) return emptyList()

        if (args.size == 1) {
            val completions = listOf("reload")
            return completions.filter { it.startsWith(args[0], ignoreCase = true) }
        }

        return emptyList()
    }
}