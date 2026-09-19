package im.apeki.qolmechanics.features.frames

data class FrameConfig(
    val enabled: Boolean,
    val requireShift: Boolean,
    val invisible: String,
    val visible: String,
    val fix: String,
    val unfix: String,
    val soundEnabled: Boolean
)