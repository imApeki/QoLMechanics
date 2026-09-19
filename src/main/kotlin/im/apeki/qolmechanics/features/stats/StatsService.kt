package im.apeki.qolmechanics.features.stats

import com.google.gson.Gson
import im.apeki.qolmechanics.QoLMechanicsPlugin
import org.bukkit.scheduler.BukkitTask
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.StandardOpenOption.*
import java.time.Duration
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

class StatsService(
    private val plugin: QoLMechanicsPlugin,
) {
    private val gson = Gson()
    private val sessionId: String = UUID.randomUUID().toString()

    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5L))
        .build()

    val serverId = loadServerId()
    private var task: BukkitTask? = null

    private val staticEnvironment by lazy { StaticEnvironment() }

    fun start() {
        stop()
        submitSnapshot()
        task = plugin.server.scheduler.runTaskTimer(
            plugin,
            Runnable { submitSnapshot() },
            24000L, // 20 минут
            24000L,
        )
    }

    fun stop() {
        task?.cancel()
        task = null
    }

    private fun submitSnapshot() {
        val payload = StatsPayload(
            productId = PRODUCT_ID,
            serverId = serverId,
            sessionId = sessionId,
            serverVersion = staticEnvironment.serverVersion,
            productVersion = plugin.description.version,
            jvmVersion = staticEnvironment.jvmVersion,
            jvmVendor = staticEnvironment.jvmVendor,
            serverSoftware = staticEnvironment.serverSoftware,
            onlineMode = plugin.server.onlineMode,
            currentOnline = plugin.server.onlinePlayers.size,
            operatingSystem = staticEnvironment.operatingSystem,
            cpuArchitecture = staticEnvironment.cpuArchitecture,
            cpuCores = Runtime.getRuntime().availableProcessors(),
            ramMb = Runtime.getRuntime().maxMemory().toMegabytes(),
        )

        val jsonPayload = gson.toJson(payload)

        plugin.server.scheduler.runTaskAsynchronously(
            plugin,
            Runnable { send(jsonPayload) },
        )
    }

    private fun send(jsonPayload: String) {
        val request = HttpRequest.newBuilder(URI.create(URL))
            .timeout(Duration.ofSeconds(10))
            .header("Content-Type", "application/json; charset=utf-8")
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .build()

        runCatching {
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .orTimeout(10, TimeUnit.SECONDS)
                .exceptionally { null }
        }
    }

    private fun loadServerId(): String {
        val path = plugin.dataFolder.toPath().resolve(".storage")
        Files.createDirectories(path.parent)

        if (Files.isRegularFile(path)) {
            Files.newBufferedReader(path, UTF_8).use { reader ->
                val value = reader.readLine()?.trim().orEmpty()
                if (runCatching { UUID.fromString(value) }.isSuccess) {
                    return value
                }
            }
        }

        val value = UUID.randomUUID().toString()
        Files.newBufferedWriter(path, UTF_8, CREATE, TRUNCATE_EXISTING, WRITE).use { writer ->
            writer.write(value)
            writer.newLine()
        }

        if (System.getProperty("os.name").orEmpty().startsWith("Windows", ignoreCase = true)) {
            runCatching { Files.setAttribute(path, "dos:hidden", true) }
        }

        return value
    }

    private fun Long.toMegabytes(): Long = (this.toDouble() / 1048576).roundToLong()

    private inner class StaticEnvironment {
        val operatingSystem: String = System.getProperty("os.name").orEmpty()
        val cpuArchitecture: String = normalizeArchitecture(System.getProperty("os.arch").orEmpty())
        val jvmVersion: String = extractJvmMajorVersion(System.getProperty("java.version").orEmpty())
        val jvmVendor: String = resolveJvmVendor()
        val serverSoftware: String = resolveServerSoftware()
        val serverVersion: String = resolveServerVersion()


        private fun resolveJvmVendor(): String {
            val vendor = System.getProperty("java.vendor").orEmpty()
            val vmName = System.getProperty("java.vm.name").orEmpty()
            val vendorVersion = System.getProperty("java.vendor.version").orEmpty()

            val combined = "$vendor $vmName $vendorVersion".lowercase(Locale.ROOT)

            return when {
                "graalvm" in combined -> "GraalVM"
                "temurin" in combined || "adoptium" in combined -> "Temurin (Adoptium)"
                "corretto" in combined || "amazon" in combined -> "Amazon Corretto"
                "zulu" in combined || "azul" in combined -> "Azul Zulu"
                "liberica" in combined || "bellsoft" in combined -> "Liberica"
                "microsoft" in combined -> "Microsoft"
                "ibm" in combined || "red hat" in combined -> "Red Hat"
                "oracle" in combined -> "Oracle"
                else -> vendor.ifBlank { "Unknown" }
            }
        }

        private fun resolveServerVersion(): String {
            val candidates = listOf(plugin.server.bukkitVersion, plugin.server.version)
            candidates.forEach { extractVersionNumber(it)?.let { version -> return version } }
            return candidates.firstOrNull { it.isNotBlank() }?.trim().orEmpty()
        }

        private fun resolveServerSoftware(): String {
            return when {
                hasClass("catserver.server.CatServer") -> "catserver"
                hasClass("io.izzel.arclight.server.Launcher") -> "arclight"
                hasClass("com.mohistmc.MohistMCStart") -> "mohist"
                hasClass("org.magmafoundation.magma.remapper.proxy.ProxyClass") -> "magma"
                hasClass("org.leavesmc.leaves.LeavesConfig") -> "leaves"
                hasClass("org.dreeam.leaf.LeafBootstrap") -> "leaf"
                hasClass("org.galemc.gale.version.AbstractPaperVersionFetcher") -> "gale"
                hasClass("io.papermc.paper.threadedregions.RegionizedServer") -> "folia"
                hasClass("gg.pufferfish.pufferfish.PufferfishConfig") -> "pufferfish"
                hasClass("org.purpurmc.purpur.PurpurConfig") -> "purpur"
                hasClass("io.papermc.paper.configuration.Configuration") -> "paper"
                hasClass("com.destroystokyo.paper.PaperConfig") -> "paper"
                hasClass("org.spigotmc.SpigotConfig") -> "spigot"
                else -> {
                    val rawName = "${plugin.server.name} ${plugin.server.version}".trim().lowercase(Locale.ROOT)
                    when {
                        "shieldspigot" in rawName -> "shieldspigot"
                        "craftbukkit" in rawName -> "craftbukkit"
                        else -> plugin.server.name.trim().lowercase(Locale.ROOT)
                    }
                }
            }
        }

        private fun normalizeArchitecture(rawArchitecture: String): String {
            return when (rawArchitecture.lowercase(Locale.ROOT)) {
                "amd64", "x86_64", "x64" -> "x64"
                "x86", "i386", "i486", "i586", "i686" -> "x86"
                "aarch64", "arm64" -> "arm64"
                "arm", "arm32" -> "arm"
                else -> rawArchitecture
            }
        }

        private fun extractVersionNumber(rawValue: String?): String? {
            val source = rawValue?.trim().orEmpty()
            if (source.isEmpty()) return null
            return Regex("\\b\\d+\\.\\d+(?:\\.\\d+)?\\b").find(source)?.value
        }

        private fun extractJvmMajorVersion(rawVersion: String): String {
            val value = rawVersion.trim()
            if (value.isEmpty()) return value
            Regex("^1\\.(\\d+)").find(value)?.groupValues?.getOrNull(1)?.let { return it }
            Regex("^(\\d+)").find(value)?.groupValues?.getOrNull(1)?.let { return it }
            return value
        }

        private fun hasClass(name: String): Boolean = try {
            Class.forName(name, false, plugin::class.java.classLoader)
            true
        } catch (_: Throwable) {
            false
        }
    }

    private data class StatsPayload(
        val productId: String,
        val serverId: String,
        val sessionId: String,
        val serverVersion: String,
        val productVersion: String,
        val jvmVersion: String,
        val jvmVendor: String,
        val serverSoftware: String,
        val onlineMode: Boolean,
        val currentOnline: Int,
        val operatingSystem: String,
        val cpuArchitecture: String,
        val cpuCores: Int,
        val ramMb: Long,
    )

    companion object {
        private const val URL = "https://leetiumlabs.com/api/stats"
        private const val PRODUCT_ID = "8350de3d-2019-47b4-8241-78bcbffa64ad"
    }
}