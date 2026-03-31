package com.seizurewatch.prototype.camera

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlin.math.roundToInt

class HeartRateAnalyzer(
    private val onHeartRateCalculated: (Int) -> Unit
) : ImageAnalysis.Analyzer {

    private val samples = mutableListOf<Int>()
    private var windowStart = System.currentTimeMillis()

    override fun analyze(image: ImageProxy) {
        if (image.format != ImageFormat.YUV_420_888) {
            image.close()
            return
        }

        val plane = image.planes[0]
        val buffer = plane.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val avg = bytes.map { it.toInt() and 0xFF }.average().toInt()
        samples.add(avg)

        val now = System.currentTimeMillis()
        if (samples.size >= 90) {
            val bpm = estimateBpm(samples, now - windowStart)
            if (bpm in 35..220) {
                onHeartRateCalculated(bpm)
            }
            samples.clear()
            windowStart = now
        }

        image.close()
    }

    private fun estimateBpm(values: List<Int>, durationMs: Long): Int {
        if (values.size < 3 || durationMs <= 0) return 0

        var peaks = 0
        for (i in 1 until values.lastIndex) {
            if (values[i] > values[i - 1] && values[i] > values[i + 1]) {
                peaks++
            }
        }

        val seconds = durationMs / 1000.0
        if (seconds <= 0.0) return 0

        return ((peaks / seconds) * 60.0).roundToInt()
    }
}
