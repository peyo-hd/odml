package com.example.mysoundclassification

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.marusys.app.GenieAudioRecord
import org.tensorflow.lite.DataType
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GAudioRecord(audioSource: Int, sampleRateInHz: Int, channelConfig: Int,
                   audioFormat: Int, bufferSizeInBytes: Int)
    : AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes) {
    companion object {
        fun create(bufferSize: Long): AudioRecord {
            val bufferSizeInBytes = bufferSize.toInt() * DataType.FLOAT32.byteSize() * 2
            return GAudioRecord(MediaRecorder.AudioSource.MIC, 16000,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT, bufferSizeInBytes)
        }
    }

    private val mRecord = GenieAudioRecord(audioSource, sampleRateInHz,
        AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes)
    private val mBuffer = ByteArray(bufferSizeInBytes)

    override fun read(audioData: FloatArray, offsetInFloats: Int,
        sizeInFloats: Int, readMode: Int): Int {
        val ret = mRecord.read(mBuffer, offsetInFloats, sizeInFloats, readMode)
        if (ret > 0) {
            var i = 0;
            while (i < ret) { // PCM_16BIT to PCM_FLOAT
                val s = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)
                    .put(mBuffer[i]).put(mBuffer[i+1]).getShort(0)
                audioData[i/4] = s.toFloat() / 32768.0F
                i += 4
            }
        }
        return ret / 4
    }

    override fun startRecording() { mRecord.startRecording() }
    override fun getBufferSizeInFrames(): Int { return mBuffer.size / 4 }
}
