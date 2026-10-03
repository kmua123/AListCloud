package com.kmuaz.alistcloud

import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaPlayer
import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kmuaz.alistcloud.data.datastore.DataStoreManager
import com.kmuaz.alistcloud.data.network.RetrofitClient
import com.kmuaz.alistcloud.data.network.model.*
import com.kmuaz.alistcloud.data.repository.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Opt-in integration test. Creates only a new uniquely named fixture folder, never modifies existing user files. */
@RunWith(AndroidJUnit4::class)
class CloudFeatureDeviceTest {
    @Test fun uploadPreviewAndArchiveRoundTrip() = runBlocking {
        val args = InstrumentationRegistry.getArguments()
        assumeTrue(args.getString("runLiveCloudTests") == "true")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val settings = DataStoreManager(context)
        val config = settings.serverConfig.first(); val token = settings.token.first()
        check(config.server.isNotBlank() && token.isNotBlank()) { "请先在手机登录" }
        val root = args.getString("cloudRoot") ?: error("必须提供 cloudRoot 测试目录")
        val folder = "$root/客户端功能验证-${System.currentTimeMillis()}"
        stage("LIVE_CLOUD_VALIDATION_FOLDER=$folder")
        val api = RetrofitClient.create(config.server); val repository = CloudRepository()
        assertEquals(200, api.mkdir(token, mapOf("path" to folder)).code)
        val cache = File(context.cacheDir, "documents").apply { mkdirs() }
        val png = File(cache, "测试图片.png")
        val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.BLUE) }
        png.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle()
        val wav = File(cache, "测试音乐.wav")
        val pcm = ByteArray(44100 * 2 * 4)
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray()); putInt(36 + pcm.size); put("WAVEfmt ".toByteArray()); putInt(16)
            putShort(1.toShort()); putShort(1.toShort()); putInt(44100); putInt(88200); putShort(2.toShort()); putShort(16.toShort())
            put("data".toByteArray()); putInt(pcm.size)
        }.array()
        wav.outputStream().use { it.write(header); it.write(pcm) }
        val mp4 = File(cache, "测试视频.mp4"); createVideo(mp4)
        val txt = File(cache, "测试文档.txt").apply { writeText("AList 客户端上传与解压验证\n") }
        val zip = File(cache, "测试压缩包.zip")
        ZipOutputStream(zip.outputStream()).use { out ->
            out.putNextEntry(ZipEntry("nested/hello.txt")); out.write("archive round trip".toByteArray()); out.closeEntry()
        }
        var progressSeen = false
        for (file in listOf(png, wav, mp4, txt, zip)) {
            stage("UPLOADING ${file.name}")
            val uri = FileProvider.getUriForFile(context, context.packageName + ".files", file)
            repository.upload(config.server, token, folder, LocalUpload(uri, file.name, file.length()), context.contentResolver, coroutineContext) { sent, _ -> if (sent > 0) progressSeen = true }
        }
        stage("UPLOAD_5_FILES_OK")
        assertTrue(progressSeen)
        val listing = repository.listAll(config.server, token, folder, true)
        assertEquals(5, listing.size)
        val http = OkHttpClient()
        for (file in listOf(png, wav, mp4, txt, zip)) {
            val response = api.getFile(token, FileGetRequest(cloudPath(folder, file.name)))
            assertEquals(200, response.code)
            val url = response.data!!.raw_url
            http.newCall(Request.Builder().url(url).build()).execute().use { result ->
                assertTrue(result.isSuccessful); assertArrayEquals(file.readBytes(), result.body!!.bytes())
            }
            if (file == wav || file == mp4) {
                val player = MediaPlayer()
                try { player.setDataSource(url); player.prepare(); assertTrue(player.duration > 0); player.start(); Thread.sleep(300); assertTrue(player.isPlaying) }
                finally { player.release() }
            }
        }
        stage("BYTE_ROUND_TRIP_AND_AUDIO_VIDEO_PLAYBACK_OK")
        val archivePath = cloudPath(folder, zip.name)
        val tools = com.kmuaz.alistcloud.viewmodel.CloudToolsViewModel(context.applicationContext as android.app.Application)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val localUri = FileProvider.getUriForFile(context, context.packageName + ".files", txt)
        assertEquals(txt.name, context.contentResolver.describeUpload(localUri).name)
        val taskFile = LocalUpload(localUri, "任务上传验证.txt", txt.length())
        instrumentation.runOnMainSync { tools.upload(listOf(taskFile), folder) }
        val uploadedTask = withTimeout(30000) { tools.uploads.first { it.lastOrNull()?.state in setOf("上传完成", "上传失败") }.last() }
        assertEquals(uploadedTask.error, "上传完成", uploadedTask.state)
        instrumentation.runOnMainSync { tools.upload(listOf(taskFile), folder) }
        val duplicateTask = withTimeout(30000) { tools.uploads.first { it.size == 2 && it.last().state == "上传失败" }.last() }
        assertTrue(duplicateTask.error.contains("同名文件"))
        val browser = com.kmuaz.alistcloud.viewmodel.HomeViewModel(context.applicationContext as android.app.Application)
        instrumentation.runOnMainSync { browser.selectCategory(com.kmuaz.alistcloud.ui.home.FileCategory.IMAGE) }
        withTimeout(60000) { browser.files.first { entries -> entries.any { it.fullPath("/") == "$folder/${png.name}" } } }
        instrumentation.runOnMainSync { browser.stopCategoryScan() }
        stage("UPLOAD_TASK_DUPLICATE_PROTECTION_AND_GLOBAL_CLASSIFICATION_OK")
        instrumentation.runOnMainSync { tools.listArchive(archivePath, "/", "") }
        withTimeout(60000) { tools.archiveBusy.first { !it } }
        assertTrue("压缩包浏览：${tools.archiveMessage.value}", tools.archiveFiles.value.any { it.name == "nested" && it.is_dir })
        instrumentation.runOnMainSync { tools.listArchive(archivePath, "/nested", "") }
        withTimeout(60000) { tools.archiveBusy.first { !it } }
        assertTrue(tools.archiveFiles.value.any { it.name == "hello.txt" })
        val destination = "$folder/解压结果"
        instrumentation.runOnMainSync { tools.decompress(archivePath, destination, "") }
        withTimeout(60000) { tools.archiveBusy.first { !it } }
        assertTrue("解压状态：${tools.archiveMessage.value}", tools.archiveMessage.value.startsWith("解压完成"))
        assertTrue(repository.listAll(config.server, token, "$destination/nested", true).any { it.name == "hello.txt" })
        instrumentation.runOnMainSync { tools.closeArchive() }
        stage("ZIP_BROWSE_AND_DECOMPRESS_OK")
    }
    private fun stage(message: String) {
        InstrumentationRegistry.getInstrumentation().sendStatus(0, android.os.Bundle().apply { putString("stream", "\n$message\n") })
    }
    private fun createVideo(file: File) {
        val codec = MediaCodec.createEncoderByType("video/avc")
        val format = MediaFormat.createVideoFormat("video/avc", 320, 240).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
            setInteger(MediaFormat.KEY_BIT_RATE, 64000); setInteger(MediaFormat.KEY_FRAME_RATE, 10); setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }
        val muxer = MediaMuxer(file.path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var started = false; var track = -1; var frame = 0; var ended = false
        try {
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE); codec.start()
            val info = MediaCodec.BufferInfo(); val deadline = System.currentTimeMillis() + 20000
            while (!ended && System.currentTimeMillis() < deadline) {
                if (frame <= 30) {
                    val input = codec.dequeueInputBuffer(10000)
                    if (input >= 0) {
                        val buffer = codec.getInputBuffer(input)!!; buffer.clear()
                        if (frame == 30) codec.queueInputBuffer(input, 0, 0, frame * 100000L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        else {
                            val bytes = ByteArray(320 * 240 * 3 / 2) { if (it < 320 * 240) (32 + frame * 4).toByte() else 128.toByte() }
                            buffer.put(bytes); codec.queueInputBuffer(input, 0, bytes.size, frame * 100000L, 0)
                        }
                        frame++
                    }
                }
                val output = codec.dequeueOutputBuffer(info, 10000)
                if (output == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) { track = muxer.addTrack(codec.outputFormat); muxer.start(); started = true }
                if (output >= 0) {
                    if (info.size > 0 && info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == 0) muxer.writeSampleData(track, codec.getOutputBuffer(output)!!, info)
                    ended = info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0; codec.releaseOutputBuffer(output, false)
                }
            }
            check(ended) { "视频测试样本编码超时" }
        } finally { runCatching { codec.stop() }; codec.release(); if (started) muxer.stop(); muxer.release() }
    }
}
