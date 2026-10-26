package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DownloadEntity
import com.example.model.DownloadStatus
import com.example.ui.SortOption
import com.example.ui.components.ClipboardDetectedBanner
import com.example.ui.components.DownloadCard
import com.example.ui.components.QueueNotificationBanner
import com.example.ui.components.SettingsSavedBanner
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TextDarkGray
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.theme.UdyatDarkBackground
import com.example.ui.theme.UdyatDarkCardBorder
import com.example.ui.theme.UdyatDarkSurface
import com.example.ui.theme.UdyatDarkSurfaceVariant

@Composable
fun ActiveDownloadsScreen(
    downloads: List<DownloadEntity>,
    activeCount: Int,
    completedCount: Int,
    selectedFilter: Int, // 0 = Todas, 1 = Activas, 2 = Completadas
    onSelectFilter: (Int) -> Unit,
    searchQuery: String,
    sortOption: SortOption,
    clipboardDetectedUrl: String?,
    onAddClipboardUrl: (String) -> Unit,
    onDismissClipboardBanner: () -> Unit,
    queueBannerCount: Int?,
    onDismissQueueBanner: () -> Unit,
    settingsSavedBannerVisible: Boolean,
    onDismissSettingsSavedBanner: () -> Unit,
    onPause: (Long) -> Unit,
    onResume: (Long) -> Unit,
    onCancel: (Long) -> Unit,
    onRetry: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onOpen: (DownloadEntity) -> Unit,
    onShare: (DownloadEntity) -> Unit,
    onItemClick: (DownloadEntity) -> Unit,
    onCopyUrl: (String) -> Unit,
    onAddNew: () -> Unit,
    isSelectionMode: Boolean,
    selectedIds: Set<Long>,
    onToggleSelect: (Long) -> Unit,
    onEnterSelectionMode: () -> Unit,
    onBatchDelete: () -> Unit,
    onExitSelectionMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = MaterialTheme.colorScheme.primary

    // Apply Filter
    val filteredByTab = when (selectedFilter) {
        1 -> downloads.filter { it.status != DownloadStatus.COMPLETED }
        2 -> downloads.filter { it.status == DownloadStatus.COMPLETED }
        else -> downloads
    }

    // Apply Search
    val searchFiltered = if (searchQuery.isBlank()) {
        filteredByTab
    } else {
        filteredByTab.filter {
            it.fileName.contains(searchQuery, ignoreCase = true) ||
                    it.url.contains(searchQuery, ignoreCase = true)
        }
    }

    // Apply Sorting (Screenshot 2)
    val sortedList = when (sortOption) {
        SortOption.NEWEST -> searchFiltered.sortedByDescending { it.createdAt }
        SortOption.OLDEST -> searchFiltered.sortedBy { it.createdAt }
        SortOption.NAME_AZ -> searchFiltered.sortedBy { it.fileName.lowercase() }
        SortOption.NAME_ZA -> searchFiltered.sortedByDescending { it.fileName.lowercase() }
        SortOption.SIZE_LARGEST -> searchFiltered.sortedByDescending { it.fileSize }
        SortOption.SIZE_SMALLEST -> searchFiltered.sortedBy { it.fileSize }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(UdyatDarkBackground)
    ) {
        // Filter Chips Row: [# Todas] [↓ Activas] [✓ Completadas] (Screenshots 1, 2, 8, etc.)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChipItem(
                text = "Todas",
                prefix = "# ",
                isSelected = selectedFilter == 0,
                accentColor = accentColor,
                onClick = { onSelectFilter(0) }
            )

            FilterChipItem(
                text = "Activas",
                prefix = "↓ ",
                isSelected = selectedFilter == 1,
                accentColor = accentColor,
                onClick = { onSelectFilter(1) }
            )

            FilterChipItem(
                text = "Completadas",
                prefix = "✓ ",
                isSelected = selectedFilter == 2,
                accentColor = accentColor,
                onClick = { onSelectFilter(2) }
            )
        }

        // Banners Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Clipboard detected banner (Screenshot 13)
            ClipboardDetectedBanner(
                visible = !clipboardDetectedUrl.isNullOrBlank(),
                onAdd = {
                    clipboardDetectedUrl?.let { onAddClipboardUrl(it) }
                },
                onDismiss = onDismissClipboardBanner
            )

            // Queue notification banner (Screenshot 5)
            QueueNotificationBanner(
                message = "Descargas encoladas",
                count = queueBannerCount ?: 1,
                visible = queueBannerCount != null,
                onDismiss = onDismissQueueBanner
            )

            // Settings saved banner (Screenshot 3)
            SettingsSavedBanner(
                visible = settingsSavedBannerVisible,
                onDismiss = onDismissSettingsSavedBanner
            )
        }

        // Selection Action Bar
        if (isSelectionMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(UdyatDarkSurface)
                    .border(1.dp, UdyatDarkCardBorder)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedIds.size} seleccionados",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onBatchDelete,
                        enabled = selectedIds.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusRed,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onExitSelectionMode,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UdyatDarkSurfaceVariant,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Cancelar", fontSize = 12.sp)
                    }
                }
            }
        }

        // Main List or Empty State
        if (sortedList.isEmpty()) {
            EmptyDownloadsState(onAddNew = onAddNew, accentColor = accentColor)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("downloads_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sortedList, key = { it.id }) { item ->
                    DownloadCard(
                        item = item,
                        onPause = onPause,
                        onResume = onResume,
                        onCancel = onCancel,
                        onRetry = onRetry,
                        onDelete = onDelete,
                        onOpen = onOpen,
                        onShare = onShare,
                        onClick = onItemClick,
                        onCopyUrl = onCopyUrl,
                        isSelectionMode = isSelectionMode,
                        isSelected = selectedIds.contains(item.id),
                        onToggleSelect = onToggleSelect,
                        onEnterSelectionMode = onEnterSelectionMode
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    text: String,
    prefix: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) accentColor else UdyatDarkSurfaceVariant)
            .border(
                width = 1.dp,
                color = if (isSelected) accentColor else UdyatDarkCardBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = prefix,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) TextWhite else TextGray
            )
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) TextWhite else TextGray
            )
        }
    }
}

@Composable
private fun EmptyDownloadsState(
    onAddNew: () -> Unit,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Rounded dark cloud download icon (Screenshot 12)
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(UdyatDarkSurfaceVariant)
                .border(1.dp, UdyatDarkCardBorder, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = null,
                tint = TextDarkGray,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sin descargas",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Pega un enlace o importa un archivo .jk para empezar",
            fontSize = 13.sp,
            color = TextDarkGray,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // + NUEVA DESCARGA pill button (Screenshot 12)
        Button(
            onClick = onAddNew,
            colors = ButtonDefaults.buttonColors(
                containerColor = UdyatDarkSurfaceVariant,
                contentColor = TextWhite
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .height(44.dp)
                .border(1.dp, UdyatDarkCardBorder, RoundedCornerShape(24.dp))
                .testTag("empty_state_add_button")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = TextWhite,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "NUEVA DESCARGA",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextWhite
            )
        }
    }
}
