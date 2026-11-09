package com.aetnox.cooking.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage

object MediaViewerUtils {

    fun isVideoUri(uriStr: String, context: Context): Boolean {
        if (uriStr.isBlank()) return false
        val lower = uriStr.lowercase()
        if (lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".mov") ||
            lower.endsWith(".3gp") || lower.endsWith(".webm") || lower.endsWith(".avi")
        ) {
            return true
        }
        return try {
            val type = context.contentResolver.getType(Uri.parse(uriStr))
            type?.startsWith("video") == true
        } catch (e: Exception) {
            false
        }
    }

    fun openMediaInSystemApp(context: Context, uriStr: String) {
        try {
            val uri = Uri.parse(uriStr)
            val isVideo = isVideoUri(uriStr, context)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, if (isVideo) "video/*" else "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, if (isVideo) "Воспроизвести видео" else "Просмотр фото"))
        } catch (e: Exception) {
            // handle error
        }
    }

    fun saveUriToInternalStorage(context: Context, uri: Uri): String {
        val uriStr = uri.toString()
        if (uriStr.isBlank() || uriStr.startsWith("/") || uriStr.startsWith("file://")) {
            return uriStr
        }
        return try {
            val storageDir = java.io.File(context.filesDir, "recipe_media").apply {
                if (!exists()) mkdirs()
            }
            val isVideo = isVideoUri(uriStr, context)
            val ext = if (isVideo) ".mp4" else ".jpg"
            val destFile = java.io.File(storageDir, "media_${System.currentTimeMillis()}_${(1000..9999).random()}$ext")

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                destFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            uriStr
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun FullscreenMediaDialog(
    uriStr: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isVideo = MediaViewerUtils.isVideoUri(uriStr, context)

    val exoPlayer = remember(uriStr, isVideo) {
        if (isVideo) {
            ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(Uri.parse(uriStr))
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }
        } else {
            null
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer?.release()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (isVideo && exoPlayer != null) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = true
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AsyncImage(
                    model = uriStr,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрыть",
                    tint = Color.White
                )
            }
        }
    }
}
