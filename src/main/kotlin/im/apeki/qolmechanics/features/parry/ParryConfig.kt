package im.apeki.qolmechanics.features.parry

data class ParryConfig(
    val enabled: Boolean,
    val windowMs: Long,
    val knockbackPower: Double,
    val stunDurationTicks: Int,
    val soundEnabled: Boolean,
    val soundType: String,
    val soundVolume: Float,
    val soundPitch: Float,
    val particleEnabled: Boolean,
    val particleType: String,
    val particleCount: Int
)
