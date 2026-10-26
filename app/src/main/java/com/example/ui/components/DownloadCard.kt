package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DownloadEntity
import com.example.model.DownloadStatus
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusCyan
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TextDarkGray
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.theme.UdyatDarkCardBorder
import com.example.ui.theme.UdyatDarkSurface
import com.example.ui.theme.UdyatDarkSurfaceVariant

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
    onCopyUrl: (String) -> Unit = {},
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: (Long) -> Unit = {},
    onEnterSelectionMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val accentColor = MaterialTheme.colorScheme.primary
    val isDownloading = item.status == DownloadStatus.DOWNLOADING
    val isReconnecting = item.status == DownloadStatus.RECONNECTING
    val isCompleted = item.status == DownloadStatus.COMPLETED
    val isFailed = item.status == DownloadStatus.FAILED

    NeonCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("download_card_${item.id}")
            .clickable {
                if (isSelectionMode) {
                    onToggleSelect(item.id)
                } else {
                    onClick(item)
                }
            },
        borderColor = if (isSelected) accentColor else UdyatDarkCardBorder,
        borderWidth = if (isSelected) 1.5.dp else 1.dp,
        containerColor = UdyatDarkSurface
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
                // Checkbox when in selection mode (Screenshot 1)
                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (isSelected) accentColor else UdyatDarkSurfaceVariant)
                            .border(1.dp, if (isSelected) accentColor else UdyatDarkCardBorder, RoundedCornerShape(5.dp))
                            .clickable { onToggleSelect(item.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = TextWhite,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                // File icon in rounded colored box (Screenshots 1, 2, 3)
                val (fileIcon, iconBg, iconTint) = when {
                    item.isApk -> Triple(Icons.Default.Android, Color(0xFF1B432E), StatusGreen)
                    item.isVideo -> Triple(Icons.Default.VideoFile, Color(0xFF102A45), Color(0xFF29B6F6))
                    item.isAudio -> Triple(Icons.Default.AudioFile, Color(0xFF3E2D12), StatusAmber)
                    item.isArchive -> Triple(Icons.Default.FolderZip, Color(0xFF3E1C1A), Color(0xFFFF7043))
                    else -> Triple(Icons.Default.InsertDriveFile, UdyatDarkSurfaceVariant, TextGray)
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = fileIcon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title + Status badge + Size
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.fileName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        // Status Badge (Screenshots 1, 3)
                        val (statusText, statusColor) = when (item.status) {
                            DownloadStatus.DOWNLOADING -> "DOWNLOADING" to StatusCyan
                            DownloadStatus.RECONNECTING -> "RECONNECTING" to StatusAmber
                            DownloadStatus.PAUSED -> "PAUSED" to TextGray
                            DownloadStatus.COMPLETED -> "COMPLETED" to StatusGreen
                            DownloadStatus.FAILED -> "ERROR" to StatusRed
                            DownloadStatus.QUEUED -> "QUEUED" to TextDarkGray
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .background(statusColor.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = statusText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    val sizeText = if (item.fileSize > 0) {
                        "${item.formattedDownloaded} de ${item.formattedSize}"
                    } else {
                        "${item.formattedDownloaded} de —"
                    }

                    Text(
                        text = sizeText,
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }

                // Action buttons: Pause || (amber circular button) / Retry / Play + More ...
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isDownloading || isReconnecting) {
                        IconButton(
                            onClick = { onPause(item.id) },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .border(1.dp, StatusAmber.copy(alpha = 0.6f), CircleShape)
                                .background(StatusAmber.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pausar",
                                tint = StatusAmber,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else if (item.status == DownloadStatus.PAUSED) {
                        IconButton(
                            onClick = { onResume(item.id) },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .border(1.dp, StatusGreen.copy(alpha = 0.6f), CircleShape)
                                .background(StatusGreen.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Reanudar",
                                tint = StatusGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else if (isFailed) {
                        // Circular red retry button (Screenshot 3)
                        IconButton(
                            onClick = { onRetry(item.id) },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .border(1.dp, StatusRed.copy(alpha = 0.6f), CircleShape)
                                .background(StatusRed.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reintentar",
                                tint = StatusRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else if (isCompleted) {
                        IconButton(
                            onClick = { onOpen(item) },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .border(1.dp, StatusGreen.copy(alpha = 0.6f), CircleShape)
                                .background(StatusGreen.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Abrir",
                                tint = StatusGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(UdyatDarkSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "Opciones",
                                tint = TextGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Context Menu (Screenshot 4)
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Copiar enlace") },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onCopyUrl(item.url)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Limpiar de lista") },
                                leadingIcon = { Icon(Icons.Default.Clear, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onDelete(item.id)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Seleccionar") },
                                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onEnterSelectionMode()
                                    onToggleSelect(item.id)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Borrar archivo", color = StatusRed) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = StatusRed) },
                                onClick = {
                                    showMenu = false
                                    if (isCompleted) onDelete(item.id) else onCancel(item.id)
                                }
                            )
                        }
                    }
                }
            }

            // Error Message Banner (Screenshots 2 & 3)
            if (isFailed && !item.errorMsg.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusRed.copy(alpha = 0.12f))
                        .border(1.dp, StatusRed.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = StatusRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.errorMsg ?: "Falló tras varios reintentos",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StatusRed,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Progress Bar (Screenshots 1, 3)
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { if (item.fileSize > 0) item.progress else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = when {
                    isCompleted -> StatusGreen
                    isFailed -> StatusRed
                    isReconnecting -> StatusAmber
                    else -> StatusCyan
                },
                trackColor = UdyatDarkSurfaceVariant
            )

            // Percent on left, Speed on right (Screenshots 1, 3)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.progressPercent}.0%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                val speedStr = when {
                    isCompleted -> "Completada"
                    isFailed -> "Error"
                    isReconnecting -> "Reconectando…"
                    item.speedBytesPerSec > 0 -> item.formattedSpeed
                    isDownloading -> "0 KB/s"
                    else -> "En pausa"
                }

                Text(
                    text = speedStr,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isCompleted -> StatusGreen
                        isFailed -> StatusRed
                        else -> StatusCyan
                    }
                )
            }
        }
    }
}
