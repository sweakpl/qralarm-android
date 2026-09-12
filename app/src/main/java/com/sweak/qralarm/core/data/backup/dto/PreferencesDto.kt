package com.sweak.qralarm.core.data.backup.dto

import com.sweak.qralarm.core.domain.user.model.Theme
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement

/**
 * preferences.json - the few settings a backup carries. Everything is nullable: a setting that is
 * not in the file is one the restore leaves alone.
 */
@Serializable
data class PreferencesDto(
    val defaultAlarmCodeId: Long? = null,
    val emergencySliderRange: IntRangeDto? = null,
    val emergencyRequiredMatches: Int? = null,
    @Serializable(with = ThemeWithFallbackSerializer::class)
    val theme: Theme? = null
)

@Serializable
data class IntRangeDto(
    val first: Int = 0,
    val last: Int = 0
)

/**
 * Reads a theme this version of the app does not know as [Theme.Default], instead of failing the
 * whole backup on it.
 */
internal object ThemeWithFallbackSerializer : KSerializer<Theme> {

    override val descriptor: SerialDescriptor = JsonElement.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Theme) {
        encoder.encodeSerializableValue(Theme.serializer(), value)
    }

    override fun deserialize(decoder: Decoder): Theme {
        val jsonDecoder = decoder as JsonDecoder
        val themeJsonElement = jsonDecoder.decodeJsonElement()

        return try {
            jsonDecoder.json.decodeFromJsonElement(Theme.serializer(), themeJsonElement)
        } catch (_: SerializationException) {
            Theme.Default
        }
    }
}
