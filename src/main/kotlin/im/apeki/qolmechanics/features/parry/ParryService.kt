package im.apeki.qolmechanics.features.parry

import im.apeki.qolmechanics.QoLMechanicsPlugin
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class ParryService(
    private val config: ParryConfig,
    private val plugin: QoLMechanicsPlugin
) {

    fun tryParry(defender: Player, attacker: LivingEntity, event: EntityDamageByEntityEvent): Boolean {
        if (event.cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK && event.cause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) return false

        val attackerDir = attacker.location.direction.normalize()
        val defenderDir = defender.location.direction.normalize()
        
        if (attackerDir.dot(defenderDir) > -0.5) return false

        val windowTicks = (config.windowMs / 50.0).toInt()
        val timePassedTicks = defender.activeItemUsedTime

        if (timePassedTicks <= windowTicks) {
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

        if (attacker is Player) {
            attacker.addPotionEffect(PotionEffect(PotionEffectType.SLOW_DIGGING, config.stunDurationTicks, 3, false, false, true))
            attacker.addPotionEffect(PotionEffect(PotionEffectType.SLOW, config.stunDurationTicks, 2, false, false, true))
        } else {
            attacker.addPotionEffect(PotionEffect(PotionEffectType.SLOW, config.stunDurationTicks, 2))
            attacker.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, config.stunDurationTicks, 0))
        }

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
    }
}