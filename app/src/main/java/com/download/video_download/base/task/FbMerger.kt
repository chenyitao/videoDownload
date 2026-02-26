package com.download.video_download.base.task

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaMuxer
import java.io.IOException
import java.nio.ByteBuffer

object FbMerger {
    private const val TAG = "FbMerger"
    private const val BUFFER_SIZE = 256 * 1024
    fun merge(audioFile: String, videoFile: String, outputFile: String) {
        val audioExtractor = initMediaExtractor(audioFile) ?: return
        val videoExtractor = initMediaExtractor(videoFile) ?: return

        try {
            val audioFormat = getTrackFormat(audioExtractor) ?: return
            val videoFormat = getTrackFormat(videoExtractor) ?: return

            val muxer = MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val audioTrackIndex = muxer.addTrack(audioFormat)
            val videoTrackIndex = muxer.addTrack(videoFormat)
            muxer.start()

            writeMediaDataToMuxer(audioExtractor, videoExtractor, muxer, audioTrackIndex, videoTrackIndex)

            muxer.stop()
            muxer.release()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            audioExtractor.release()
            videoExtractor.release()
        }
    }
    private fun initMediaExtractor(filePath: String): MediaExtractor? {
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(filePath)
            extractor
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    private fun writeMediaDataToMuxer(
        audioExtractor: MediaExtractor,
        videoExtractor: MediaExtractor,
        muxer: MediaMuxer,
        audioTrackIndex: Int,
        videoTrackIndex: Int
    ) {
        val bufferInfo = MediaCodec.BufferInfo()
        val buffer = ByteBuffer.allocate(BUFFER_SIZE)
        var isAudioDone = false
        var isVideoDone = false

        while (!isAudioDone || !isVideoDone) {
            if (!isAudioDone) {
                isAudioDone = writeSampleData(audioExtractor, muxer, audioTrackIndex, buffer, bufferInfo)
            }
            if (!isVideoDone) {
                isVideoDone = writeSampleData(videoExtractor, muxer, videoTrackIndex, buffer, bufferInfo)
            }
        }
    }
    private fun getTrackFormat(extractor: MediaExtractor): android.media.MediaFormat? {
        val trackCount = extractor.trackCount
        if (trackCount <= 0) {
            println("No track found in the media file")
            return null
        }
        extractor.selectTrack(0)
        return extractor.getTrackFormat(0)
    }
    private fun writeSampleData(
        extractor: MediaExtractor,
        muxer: MediaMuxer,
        trackIndex: Int,
        buffer: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo
    ): Boolean {
        val sampleSize = extractor.readSampleData(buffer, 0)
        if (sampleSize < 0) {
            return true
        }

        bufferInfo.size = sampleSize
        bufferInfo.offset = 0
        bufferInfo.presentationTimeUs = extractor.sampleTime
        bufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME

        muxer.writeSampleData(trackIndex, buffer, bufferInfo)
        extractor.advance()
        return false
    }
}