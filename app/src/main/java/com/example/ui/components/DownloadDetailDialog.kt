package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.DownloadEntity
import com.example.model.DownloadStatus
import com.example.ui.theme.NeonRedBright
import com.example.ui.theme.NeonRedCardBorder
import com.example.ui.theme.NeonRedContainer
import com.example.ui.theme.NeonRedDarkSurface
import com.example.ui.theme.NeonRedDarkSurfaceVariant
import com.example.ui.theme.NeonRedPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DownloadDetailDialog(
    item: DownloadEntity,
    onDismiss: () -> Unit,
    onOpen: (DownloadEntity) -> Unit,
    onShare: (DownloadEntity) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = NeonRedDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonRedPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonRedContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = NeonRedBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Detalles de Descarga",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                DetailItem(label = "Archivo", value = item.fileName)
                DetailItem(label = "Estado", value = item.status.name)
                DetailItem(
                    label = "Progreso",
                    value = "${item.formattedDownloaded} / ${item.formattedSize} (${item.progressPercent}%)"
                )
                DetailItem(label = "Hilos activos", value = "${item.threadsCount} hilos simultáneos")
                DetailItem(label = "Reintentos de red", value = "${item.reconnectAttempt} intentos")

                if (item.filePath.isNotEmpty()) {
                    DetailItem(label = "Ruta local", value = item.filePath)
                }

                if (!item.errorMsg.isNullOrEmpty()) {
                    DetailItem(label = "Último error", value = item.errorMsg, isError = true)
                }

                // Copyable URL section
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enlace de origen:",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonRedDarkSurfaceVariant)
                        .clickable {
                            val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clip.setPrimaryClip(ClipData.newPlainText("URL", item.url))
                            Toast.makeText(context, "Enlace copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.url,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar enlace",
                        tint = NeonRedBright,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.status == DownloadStatus.COMPLETED) {
                        OutlinedButton(
                            onClick = { onShare(item) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Compartir", fontSize = 13.sp, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onOpen(item) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonRedPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Abrir archivo", fontSize = 13.sp, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    TextButton(onClick = onDismiss) {
                        Text("Cerrar", fontSize = 14.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String, isError: Boolean = false) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 11.sp, color = TextMuted)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isError) NeonRedBright else TextPrimary
        )
    }
}
