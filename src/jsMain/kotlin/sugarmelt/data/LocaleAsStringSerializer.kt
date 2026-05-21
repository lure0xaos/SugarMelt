package sugarmelt.data

import de.comahe.i18n4k.Locale
import de.comahe.i18n4k.toTag
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object LocaleAsStringSerializer : KSerializer<Locale> {
    override val descriptor: SerialDescriptor = 
        PrimitiveSerialDescriptor("de.comahe.i18n4k.Locale", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Locale) {
        encoder.encodeString(value.toTag())
    }

    override fun deserialize(decoder: Decoder): Locale {
        return Locale(decoder.decodeString())
    }
}
