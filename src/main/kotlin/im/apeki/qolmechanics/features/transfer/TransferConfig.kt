package im.apeki.qolmechanics.features.transfer

data class TransferConfig(
    val enabled: Boolean,
    val maxDistance: Double,
    val messageSenderEnabled: Boolean,
    val messageSender: String,
    val messageReceiverEnabled: Boolean,
    val messageReceiver: String,
    val soundEnabled: Boolean
)
