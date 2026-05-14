package com.sweak.qralarm.features.qralarm_pro.components.util

import coil.ImageLoader
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.decode.ImageSource
import coil.fetch.SourceResult
import coil.request.Options
import com.github.penfeizhou.animation.loader.ByteBufferLoader
import com.github.penfeizhou.animation.webp.WebPDrawable
import okio.BufferedSource
import okio.ByteString.Companion.encodeUtf8
import java.nio.ByteBuffer

class AnimatedWebPDecoder(private val source: ImageSource) : Decoder {

    override suspend fun decode(): DecodeResult {
        val bytes = source.source().use { it.readByteArray() }
        val loader = object : ByteBufferLoader() {
            override fun getByteBuffer(): ByteBuffer = ByteBuffer.wrap(bytes)
        }
        val drawable = WebPDrawable(loader).apply { setAutoPlay(false) }
        return DecodeResult(drawable = drawable, isSampled = false)
    }

    class Factory : Decoder.Factory {
        override fun create(
            result: SourceResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? = if (isAnimatedWebP(result.source.source())) {
            AnimatedWebPDecoder(result.source)
        } else null

        private fun isAnimatedWebP(source: BufferedSource): Boolean {
            if (!source.rangeEquals(0, RIFF)) return false
            if (!source.rangeEquals(8, WEBP)) return false
            if (!source.rangeEquals(12, VP8X)) return false
            if (!source.request(21)) return false
            val flags = source.peek().apply { skip(20) }.readByte().toInt()
            return flags and ANIM_FLAG_BIT != 0
        }

        private companion object {
            val RIFF = "RIFF".encodeUtf8()
            val WEBP = "WEBP".encodeUtf8()
            val VP8X = "VP8X".encodeUtf8()
            const val ANIM_FLAG_BIT = 0x02
        }
    }
}