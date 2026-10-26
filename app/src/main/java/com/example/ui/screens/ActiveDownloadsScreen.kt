package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DownloadEntity
import com.example.model.DownloadStatus
import com.example.ui.components.DownloadCard
import com.example.ui.components.SpeedometerHeader
import com.example.ui.components.VpnTipBanner
import com.example.ui.theme.AccentRed
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextDarkGray
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite

@Composable
fun ActiveDownloadsScreen(
    activeDownloads: List<DownloadEntity>,
    totalSpeedBps: Long,
    onPause: (Long) -> Unit,
    onResume: (Long) -> Unit,
    onCancel: (Long) -> Unit,
    onRetry: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onOpen: (DownloadEntity) -> Unit,
    onShare: (DownloadEntity) -> Unit,
    onItemClick: (DownloadEntity) -> Unit,
    onPauseAll: () -> Unit,
    onResumeAll: () -> Unit,
    onAddNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isReconnectingAny = activeDownloads.any { it.status == DownloadStatus.RECONNECTING }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("active_downloads_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Speedometer Header
        item {
            SpeedometerHeader(
                totalSpeedBps = totalSpeedBps,
                activeCount = activeDownloads.size,
                isReconnectingAny = isReconnectingAny,
                onPauseAll = onPauseAll,
                onResumeAll = onResumeAll
            )
        }

        // VPN Tip Banner
        item {
            VpnTipBanner()
        }

        if (activeDownloads.isEmpty()) {
            item {
                EmptyActiveState(onAddNew = onAddNew)
            }
        } else {
            items(activeDownloads, key = { it.id }) { item ->
                DownloadCard(
                    item = item,
                    onPause = onPause,
                    onResume = onResume,
                    onCancel = onCancel,
                    onRetry = onRetry,
                    onDelete = onDelete,
                    onOpen = onOpen,
                    onShare = onShare,
                    onClick = onItemClick
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun EmptyActiveState(onAddNew: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(DarkSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = null,
                tint = TextDarkGray,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "No hay descargas",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Pega un enlace de toDus.",
            fontSize = 13.sp,
            color = TextDarkGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onAddNew,
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
            shape = CircleShape,
            modifier = Modifier.testTag("empty_state_add_button")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Pega un enlace de toDus", fontWeight = FontWeight.Bold, color = TextWhite)
        }
    }
}
