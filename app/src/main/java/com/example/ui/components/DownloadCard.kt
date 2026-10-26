package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DownloadEntity
import com.example.model.DownloadStatus
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentRedContainer
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.TextDarkGray
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite

@Composable
fun DownloadCard(
    item: DownloadEntity,
    onPause: (Long) -> Unit,
    onResume: (Long) -> Unit,
    onCancel: (Long) -> Unit,
    onRetry: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onOpen: (DownloadEntity) -> Unit,
    onShare: (DownloadEntity) -> Unit,
    onClick: (DownloadEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCancelDialog by remember { mutableStateOf(false) }

    val isDownloading = item.status == DownloadStatus.DOWNLOADING
    val isReconnecting = item.status == DownloadStatus.RECONNECTING
    val isCompleted = item.status == DownloadStatus.COMPLETED

    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val spinRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing)
        ),
        label = "spin"
    )

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = {
                Text(
                    text = "¿Cancelar descarga?",
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            },
            text = {
                Text(
                    text = "Se quitan de la cola y se borra lo que ya se había descargado.",
                    color = TextGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        onCancel(item.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Cancelar descarga", color = TextWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Seguir", color = TextGray)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    NeonCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("download_card_${item.id}")
            .clickable { onClick(item) },
        borderColor = when {
            isDownloading -> AccentRed.copy(alpha = 0.5f)
            isReconnecting -> StatusOrange.copy(alpha = 0.5f)
            isCompleted -> StatusGreen.copy(alpha = 0.4f)
            else -> DarkCardBorder
        },
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // File Type Icon
                val fileIcon = when {
                    item.isApk -> Icons.Default.Android
                    item.isVideo -> Icons.Default.VideoFile
                    item.isAudio -> Icons.Default.AudioFile
                    item.isArchive -> Icons.Default.FolderZip
                    else -> Icons.Default.InsertDriveFile
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = fileIcon,
                        contentDescription = null,
                        tint = if (isCompleted) StatusGreen else AccentRed,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.fileName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    when (item.status) {
                        DownloadStatus.DOWNLOADING -> {
                            val speedText = if (item.speedBytesPerSec > 0) item.formattedSpeed else "Calculando velocidad…"
                            Text(
                                text = "Descargando · $speedText · ${item.threadsCount} hilos",
                                fontSize = 12.sp,
                                color = AccentRed
                            )
                        }
                        DownloadStatus.RECONNECTING -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = StatusOrange,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .rotate(spinRotation)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Reconectando (${item.reconnectAttempt})…",
                                    fontSize = 12.sp,
                                    color = StatusOrange
                                )
                            }
                        }
                        DownloadStatus.PAUSED -> {
                            Text(text = "En pausa", fontSize = 12.sp, color = TextDarkGray)
                        }
                        DownloadStatus.COMPLETED -> {
                            Text(text = "Descarga completada · ${item.formattedSize}", fontSize = 12.sp, color = StatusGreen)
                        }
                        DownloadStatus.FAILED -> {
                            Text(text = item.errorMsg ?: "La descarga falló", fontSize = 12.sp, color = AccentRed)
                        }
                        DownloadStatus.QUEUED -> {
                            Text(text = "En cola", fontSize = 12.sp, color = TextDarkGray)
                        }
                    }
                }

                // Control buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (item.status) {
                        DownloadStatus.DOWNLOADING, DownloadStatus.RECONNECTING -> {
                            IconButton(
                                onClick = { onPause(item.id) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceVariant)
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = "Pausar", tint = TextWhite, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { showCancelDialog = true },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceVariant)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = TextGray, modifier = Modifier.size(18.dp))
                            }
                        }
                        DownloadStatus.PAUSED -> {
                            IconButton(
                                onClick = { onResume(item.id) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(AccentRedContainer)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Reanudar", tint = AccentRed, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { showCancelDialog = true },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceVariant)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = TextGray, modifier = Modifier.size(18.dp))
                            }
                        }
                        DownloadStatus.FAILED -> {
                            IconButton(
                                onClick = { onRetry(item.id) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(AccentRedContainer)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reintentar", tint = AccentRed, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { onDelete(item.id) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceVariant)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = TextGray, modifier = Modifier.size(18.dp))
                            }
                        }
                        DownloadStatus.COMPLETED -> {
                            IconButton(
                                onClick = { onOpen(item) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(StatusGreen.copy(alpha = 0.15f))
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = "Abrir", tint = StatusGreen, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { onShare(item) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceVariant)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Compartir", tint = TextGray, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { onDelete(item.id) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceVariant)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = TextDarkGray, modifier = Modifier.size(18.dp))
                            }
                        }
                        DownloadStatus.QUEUED -> {
                            IconButton(
                                onClick = { showCancelDialog = true },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceVariant)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = TextGray, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            if (!isCompleted) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { if (item.fileSize > 0) item.progress else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isReconnecting) StatusOrange else AccentRed,
                    trackColor = DarkSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.formattedDownloaded} / ${item.formattedSize} (${item.progressPercent}%)",
                        fontSize = 11.sp,
                        color = TextGray
                    )

                    if (isDownloading && item.etaSeconds > 0) {
                        Text(
                            text = "quedan ${item.formattedEta}",
                            fontSize = 11.sp,
                            color = TextDarkGray
                        )
                    }
                }
            }
        }
    }
}
