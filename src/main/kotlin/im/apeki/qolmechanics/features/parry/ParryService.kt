package im.apeki.qolmechanics.features.parry

import im.apeki.qolmechanics.QoLMechanicsPlugin
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID

class ParryService(
    private val config: ParryConfig,
    private val plugin: QoLMechanicsPlugin
) {
    private val shieldRaiseTimes = mutableMapOf<UUID, Long>()

    fun markShieldRaised(player: Player) {
        shieldRaiseTimes[player.uniqueId] = System.currentTimeMillis()
    }

    fun tryParry(defender: Player, attacker: LivingEntity): Boolean {
        val raiseTime = shieldRaiseTimes[defender.uniqueId] ?: return false
        val timePassed = System.currentTimeMillis() - raiseTime

        if (timePassed <= config.windowMs) {
            applyParryEffects(defender, attacker)
            return true
        }
        return false
    }

    private fun applyParryEffects(defender: Player, attacker: LivingEntity) {
        val knockbackDir = attacker.location.toVector().subtract(defender.location.toVector()).normalize()
        knockbackDir.setY(0.4)
        knockbackDir.multiply(config.knockbackPower)
        attacker.velocity = knockbackDir

        attacker.addPotionEffect(PotionEffect(PotionEffectType.SLOW, config.stunDurationTicks, 2))
        attacker.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, config.stunDurationTicks, 0))

        if (config.soundEnabled) {
            try {
                val sound = Sound.valueOf(config.soundType.uppercase())
                defender.world.playSound(defender.location, sound, config.soundVolume, config.soundPitch)
            } catch (_: IllegalArgumentException) {
                plugin.logger.warning("Неверное название звука в конфиге: ${config.soundType}")
            }
        }
        
        if (config.particleEnabled) {
            try {
                val particle = Particle.valueOf(config.particleType)
                val particleLocation = defender.eyeLocation.add(defender.eyeLocation.direction.multiply(1.5))

                when (particle.dataType) {
                    BlockData::class.java -> {
                        defender.world.spawnParticle(
                            particle,
                            particleLocation,
                            config.particleCount,
                            0.5, 0.5, 0.5,
                            0.0,
                            org.bukkit.Material.IRON_BLOCK.createBlockData()
                        )
                    }
                    Particle.DustOptions::class.java -> {
                        defender.world.spawnParticle(
                            particle,
                            particleLocation,
                            config.particleCount,
                            0.5, 0.5, 0.5,
                            0.0,
                            Particle.DustOptions(Color.GRAY, 1.0f)
                        )
                    }
                    else -> {
                        defender.world.spawnParticle(
                            particle,
                            particleLocation,
                            config.particleCount,
                            0.5, 0.5, 0.5,
                            0.0
                        )
                    }
                }
            } catch (_: IllegalArgumentException) {
                plugin.logger.warning("Неверное название частицы в конфиге: ${config.particleType}")
            }
        }

        shieldRaiseTimes.remove(defender.uniqueId)
    }
}