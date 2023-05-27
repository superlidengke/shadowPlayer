package com.example.simplemusic

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Environment
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import me.rosuh.libmpg123.MPG123
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class RawAudioDataTest {
    val sdCardBase = Environment.getExternalStorageDirectory().absolutePath

    @Test
    fun getInfo() {
        val extractor = MediaExtractor()
        // Context of the app under test.
        val appContext =
            InstrumentationRegistry.getInstrumentation().targetContext
        try {
            val audioFd = appContext.assets.openFd("320.mp3")
            extractor.setDataSource(audioFd)
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

        val mf = extractor.getTrackFormat(0)

        val mime = mf.getString(MediaFormat.KEY_MIME)
        val bitRate = mf.getInteger(MediaFormat.KEY_BIT_RATE)
        val sampleRate = mf.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val duration = mf.getLong(MediaFormat.KEY_DURATION)
        val channelNum = mf.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        println("mime: $mime, bitRate: $bitRate, sampleRate: $sampleRate, duration: $duration, channel: $channelNum")
//        mex.readSampleData()/
//        val inputBuffer = ByteBuffer.allocate(300)
//        inputBuffer.short
        val codec = MediaCodec.createDecoderByType(mime!!);
        codec.configure(mf, null, null, 0)
        codec.start()
        val codecInputBuffers = codec.getInputBuffers();
        val codecOutputBuffers = codec.getOutputBuffers()

        //https://github.com/rosuH/MPG123-Android
        val testMp3Path = "${sdCardBase}/Documents/audio1/320.mp3"
        appContext.assets.open("320.mp3").use { input ->
            Files.copy(
                input,
                Paths.get(testMp3Path),
                StandardCopyOption.REPLACE_EXISTING
            )
        }
        val decoder = MPG123(testMp3Path)
        println("Mpg123: ${decoder.duration}, ${decoder.rate}, ${decoder.numChannels}")
        var frame = decoder.readFrame()
        println("frame length: ${frame.size}")
        val samplePoints = mutableListOf<Int>()
        while (!(frame == null || frame.isEmpty())) {
            samplePoints.add(convertToWaveForm(frame))
            frame = decoder.readFrame()
        }
        println("count: ${samplePoints.size}")
        println(samplePoints)
// duration * sample rate = frame size * frame count
    }

    /**
     * Just convert frame data to wave form by sqrt the sum of (ele * ele)
     */
    private fun calculateRealVolume(array: ShortArray): Int {
        var sum = 0.0
        array.forEachIndexed { index, sh ->
            if (index != array.size - 1) {
                sum += (sh.toInt() * sh.toInt()).toDouble()
            }
        }
        return kotlin.math.sqrt(sum).toInt()
    }

    /**
     * Get the average abs volume in this frame
     */
    private fun convertToWaveForm(array: ShortArray): Int {
        return array.map { abs(it.toInt()) }.average().toInt()
    }


}

