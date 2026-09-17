package im.apeki.qolmechanics.features.frames

data class FrameConfig(
    val enabled: Boolean,
    val requireShift: Boolean,
    val toolInvisible: String,
    val toolVisible: String,
    val fixTools: List<String>,
    val toolUnfix: String,
    val soundEnabled: Boolean
)