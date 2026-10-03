package com.kmuaz.alistcloud.ui.home

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

fun String.isAudioName() = substringAfterLast('.', "").lowercase() in setOf("mp3", "flac", "wav", "aac", "m4a", "ogg", "opus", "wma")
fun String.isVideoName() = substringAfterLast('.', "").lowercase() in setOf("mp4", "mkv", "avi", "mov", "webm", "flv", "m4v", "3gp", "ts")
fun String.isArchiveName() = substringAfterLast('.', "").lowercase() in setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "iso")

@Composable
fun MediaPreviewDialog(url: String, name: String, onDismiss: () -> Unit, onExternal: () -> Unit) {
    var video by remember { mutableStateOf<VideoView?>(null) }
    var prepared by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(false) }
    var position by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf("") }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) { video?.pause(); playing = false }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); video?.stopPlayback() }
    }
    LaunchedEffect(prepared) {
        while (prepared) {
            position = (video?.currentPosition ?: 0).toFloat(); playing = video?.isPlaying == true
            delay(500)
        }
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(color = Color(0xFF101722), contentColor = Color.White, modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "关闭播放器") }
                    Text(name, modifier = Modifier.weight(1f), maxLines = 2)
                }
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AndroidView(factory = { context -> VideoView(context).also { view ->
                        video = view
                        view.setOnPreparedListener {
                            prepared = true; duration = view.duration.coerceAtLeast(0); view.start(); playing = true
                        }
                        view.setOnCompletionListener { playing = false }
                        view.setOnErrorListener { _, _, _ -> error = "无法播放此文件，请检查连接或使用其他播放器"; prepared = false; playing = false; true }
                        view.setVideoURI(Uri.parse(url))
                    } }, modifier = if (name.isAudioName()) Modifier.size(1.dp) else Modifier.fillMaxSize())
                    if (name.isAudioName()) Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(100.dp), tint = Color(0xFF9CBDFF))
                    if (!prepared && error.isEmpty()) CircularProgressIndicator()
                }
                if (error.isNotEmpty()) Text(error, color = Color(0xFFFFB4AB))
                Slider(value = position.coerceIn(0f, duration.coerceAtLeast(1).toFloat()),
                    onValueChange = { position = it; video?.seekTo(it.toInt()) }, enabled = prepared && duration > 0,
                    valueRange = 0f..duration.coerceAtLeast(1).toFloat())
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${mediaTime(position.toInt())} / ${mediaTime(duration)}")
                    IconButton(enabled = prepared, onClick = { if (playing) video?.pause() else video?.start(); playing = !playing }) {
                        Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, if (playing) "暂停播放" else "播放")
                    }
                    TextButton(onClick = { video?.pause(); playing = false; onExternal() }) { Text("其他应用打开") }
                }
            }
        }
    }
}
private fun mediaTime(ms: Int): String = "%02d:%02d".format(ms / 60000, ms / 1000 % 60)
