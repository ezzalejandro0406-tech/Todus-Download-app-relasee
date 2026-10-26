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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DownloadEntity
import com.example.model.DownloadStatus
import com.example.ui.theme.NeonGreenSuccess
import com.example.ui.theme.NeonOrangeWarning
import com.example.ui.theme.NeonRedBright
import com.example.ui.theme.NeonRedCardBorder
import com.example.ui.theme.NeonRedContainer
import com.example.ui.theme.NeonRedDarkSurfaceVariant
import com.example.ui.theme.NeonRedPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

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
    val isDownloading = item.status == DownloadStatus.DOWNLOADING
    val isReconnecting = item.status == DownloadStatus.RECONNECTING
    val isCompleted = item.status == DownloadStatus.COMPLETED

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val spinRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing)
        ),
        label = "spin"
    )

    val borderColor = when {
        isDownloading -> NeonRedPrimary
        isReconnecting -> NeonOrangeWarning
        isCompleted -> NeonGreenSuccess.copy(alpha = 0.6f)
        else -> NeonRedCardBorder
    }

    NeonCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("download_card_${item.id}")
            .clickable { onClick(item) },
        borderColor = borderColor,
        borderWidth = if (isDownloading || isReconnecting) 1.5.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Icon + Filename + Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // File Type Icon
                val (fileIcon, iconColor) = when {
                    item.isApk -> Icons.Default.Android to NeonRedBright
                    item.isVideo -> Icons.Default.VideoFile to Color(0xFF00E5FF)
                    item.isAudio -> Icons.Default.AudioFile to Color(0xFFFFD54F)
                    item.isArchive -> Icons.Default.FolderZip to Color(0xFFFF7043)
                    else -> Icons.Default.InsertDriveFile to TextSecondary
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonRedDarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = fileIcon,
                        contentDescription = "Archivo",
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title + Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.fileName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Status badge
                        when (item.status) {
                            DownloadStatus.DOWNLOADING -> {
                                Text(
                                    text = "Descargando • ${item.formattedSpeed}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeonRedBright
                                )
                            }
                            DownloadStatus.RECONNECTING -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = null,
                                        tint = NeonOrangeWarning,
                                        modifier = Modifier
                                            .size(12.dp)
                                            .rotate(spinRotation)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Reconectando (${item.reconnectAttempt})…",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NeonOrangeWarning
                                    )
                                }
                            }
                            DownloadStatus.PAUSED -> {
                                Text(
                                    text = "En pausa",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                            DownloadStatus.COMPLETED -> {
                                Text(
                                    text = "Completado • ${item.formattedSize}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeonGreenSuccess
                                )
                            }
                            DownloadStatus.FAILED -> {
                                Text(
                                    text = item.errorMsg ?: "Error de red",
                                    fontSize = 12.sp,
                                    color = NeonRedBright,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            DownloadStatus.QUEUED -> {
                                Text(
                                    text = "En cola",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }

                // Action Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (item.status) {
                        DownloadStatus.DOWNLOADING, DownloadStatus.RECONNECTING -> {
                            IconButton(
                                onClick = { onPause(item.id) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonRedContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pause,
                                    contentDescription = "Pausar",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { onCancel(item.id) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonRedDarkSurfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancelar",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        DownloadStatus.PAUSED -> {
                            IconButton(
                                onClick = { onResume(item.id) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonRedContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Reanudar",
                                    tint = NeonRedBright,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { onCancel(item.id) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonRedDarkSurfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancelar",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        DownloadStatus.FAILED -> {
                            IconButton(
                                onClick = { onRetry(item.id) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonRedContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reintentar",
                                    tint = NeonRedBright,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { onDelete(item.id) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonRedDarkSurfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        DownloadStatus.COMPLETED -> {
                            IconButton(
                                onClick = { onOpen(item) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreenSuccess.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Abrir",
                                    tint = NeonGreenSuccess,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { onShare(item) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonRedDarkSurfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Compartir",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { onDelete(item.id) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonRedDarkSurfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        DownloadStatus.QUEUED -> {
                            IconButton(
                                onClick = { onCancel(item.id) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonRedDarkSurfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancelar",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Progress Bar (if not completed)
            if (!isCompleted) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { if (item.fileSize > 0) item.progress else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isReconnecting) NeonOrangeWarning else NeonRedPrimary,
                    trackColor = NeonRedDarkSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.formattedDownloaded} / ${item.formattedSize} (${item.progressPercent}%)",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.threadsCount > 0) {
                            Text(
                                text = "${item.threadsCount} HILOS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonRedBright,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NeonRedContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        if (isDownloading && item.etaSeconds > 0) {
                            Text(
                                text = "ETA: ${item.formattedEta}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
