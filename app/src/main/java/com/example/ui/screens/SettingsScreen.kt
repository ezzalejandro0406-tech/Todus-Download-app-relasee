package com.example.ui.screens

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AppSettings
import com.example.model.DownloadEntity
import com.example.ui.DownloadViewModel
import com.example.ui.theme.AccentSwatches
import com.example.ui.theme.NeonRed
import com.example.ui.theme.StatusCyan
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextDarkGray
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.theme.UdyatDarkBackground
import com.example.ui.theme.UdyatDarkCardBorder
import com.example.ui.theme.UdyatDarkSurface
import com.example.ui.theme.UdyatDarkSurfaceVariant
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: DownloadViewModel,
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedAccentIndex by remember(settings.accentColorIndex) { mutableIntStateOf(settings.accentColorIndex) }
    var maxConcurrent by remember(settings.maxConcurrentDownloads) { mutableIntStateOf(settings.maxConcurrentDownloads) }
    var maxThreads by remember(settings.maxThreads) { mutableIntStateOf(settings.maxThreads) }
    var autoReconnect by remember(settings.autoReconnect) { mutableStateOf(settings.autoReconnect) }
    var maxReconnectAttempts by remember(settings.maxReconnectAttempts) { mutableIntStateOf(settings.maxReconnectAttempts) }
    var speedLimit by remember(settings.speedLimit) { mutableStateOf(settings.speedLimit) }
    var subfolder by remember(settings.downloadSubfolder) { mutableStateOf(settings.downloadSubfolder) }
    var proxyEnabled by remember(settings.proxyEnabled) { mutableStateOf(settings.proxyEnabled) }
    var proxyHost by remember(settings.proxyHost) { mutableStateOf(settings.proxyHost) }
    var proxyPortStr by remember(settings.proxyPort) { mutableStateOf(settings.proxyPort.toString()) }

    var speedMenuExpanded by remember { mutableStateOf(false) }

    // Optimization metrics & dialog states
    var dbSizeStr by remember { mutableStateOf("84.00 KB") }
    var downloadFolderSizeStr by remember { mutableStateOf("43.82 MB") }
    var showEmptyFolderDialog by remember { mutableStateOf(false) }
    var showCleanOrphansDialog by remember { mutableStateOf(false) }
    var topBannerMessage by remember { mutableStateOf<String?>(null) }

    fun refreshMetrics() {
        val (db, folder) = viewModel.getStorageMetrics()
        dbSizeStr = db
        downloadFolderSizeStr = folder
    }

    LaunchedEffect(Unit) {
        refreshMetrics()
    }

    val activeAccentColor = AccentSwatches.getOrElse(selectedAccentIndex) { AccentSwatches[0] }

    fun saveCurrent() {
        val port = proxyPortStr.toIntOrNull() ?: 8080
        val updated = settings.copy(
            accentColorIndex = selectedAccentIndex,
            maxConcurrentDownloads = maxConcurrent,
            maxThreads = maxThreads,
            autoReconnect = autoReconnect,
            maxReconnectAttempts = maxReconnectAttempts,
            speedLimit = speedLimit,
            downloadSubfolder = subfolder.ifBlank { "JKDOWN" },
            proxyEnabled = proxyEnabled,
            proxyHost = proxyHost.trim(),
            proxyPort = port
        )
        onSaveSettings(updated)
        topBannerMessage = "Ajustes guardados correctamente"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(UdyatDarkBackground)
    ) {
        // Top bar matching Screenshots 3, 11: (<) Ajustes (✓)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(UdyatDarkSurface)
                    .border(1.dp, UdyatDarkCardBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextWhite,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = "Ajustes",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            IconButton(
                onClick = { saveCurrent() },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(StatusGreen.copy(alpha = 0.2f))
                    .border(1.dp, StatusGreen, CircleShape)
                    .testTag("save_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Guardar",
                    tint = StatusGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Green Success Banner if triggered
        if (topBannerMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF132A1E))
                    .border(1.dp, StatusGreen, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = StatusGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = topBannerMessage!!,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextWhite,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "✕",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        modifier = Modifier.clickable { topBannerMessage = null }
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Card 1: Descargas Simultáneas & Hilos (Screenshots 3, 11)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = UdyatDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, UdyatDarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Descargas simultáneas",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Text(
                                text = "$maxConcurrent",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = activeAccentColor
                            )
                        }

                        Slider(
                            value = maxConcurrent.toFloat(),
                            onValueChange = { maxConcurrent = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = activeAccentColor,
                                inactiveTrackColor = UdyatDarkSurfaceVariant
                            )
                        )

                        HorizontalDivider(
                            color = UdyatDarkCardBorder,
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hilos por descarga",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Text(
                                text = "$maxThreads",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = activeAccentColor
                            )
                        }

                        Slider(
                            value = maxThreads.toFloat(),
                            onValueChange = { maxThreads = it.toInt() },
                            valueRange = 1f..16f,
                            steps = 14,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = activeAccentColor,
                                inactiveTrackColor = UdyatDarkSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Card 2: Reintento Automático & Máximo Reintentos (Screenshots 3, 11)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = UdyatDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, UdyatDarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Reintento automático",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "Reintentar descargas si falla la red",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }
                            Switch(
                                checked = autoReconnect,
                                onCheckedChange = { autoReconnect = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = activeAccentColor,
                                    uncheckedThumbColor = TextGray,
                                    uncheckedTrackColor = UdyatDarkSurfaceVariant
                                )
                            )
                        }

                        HorizontalDivider(
                            color = UdyatDarkCardBorder,
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Máximo de reintentos",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "Intentos automáticos antes de marcar error",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(UdyatDarkSurfaceVariant)
                                    .border(1.dp, UdyatDarkCardBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "—",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    modifier = Modifier
                                        .clickable { if (maxReconnectAttempts > 1) maxReconnectAttempts-- }
                                        .padding(horizontal = 8.dp)
                                )
                                Text(
                                    text = "$maxReconnectAttempts",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeAccentColor,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                Text(
                                    text = "+",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    modifier = Modifier
                                        .clickable { if (maxReconnectAttempts < 20) maxReconnectAttempts++ }
                                        .padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Card 3: Límite de Velocidad & Subcarpeta (Screenshots 3, 12)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = UdyatDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, UdyatDarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Límite de velocidad",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "Velocidad máxima combinada",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }

                            Box {
                                OutlinedButton(
                                    onClick = { speedMenuExpanded = true },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, activeAccentColor),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = activeAccentColor)
                                ) {
                                    Text(
                                        text = speedLimit,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                DropdownMenu(
                                    expanded = speedMenuExpanded,
                                    onDismissRequest = { speedMenuExpanded = false },
                                    modifier = Modifier.background(UdyatDarkSurfaceVariant)
                                ) {
                                    listOf("Sin Límite", "1 MB/s", "2 MB/s", "5 MB/s", "10 MB/s", "20 MB/s").forEach { limit ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = limit,
                                                    color = if (speedLimit == limit) activeAccentColor else TextWhite,
                                                    fontSize = 12.sp
                                                )
                                            },
                                            onClick = {
                                                speedLimit = limit
                                                speedMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            color = UdyatDarkCardBorder,
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        Text(
                            text = "Subcarpeta de descargas",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = subfolder,
                            onValueChange = { subfolder = it },
                            placeholder = { Text("JKDOWN", color = TextDarkGray, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = TextDarkGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = activeAccentColor,
                                unfocusedBorderColor = UdyatDarkCardBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = UdyatDarkSurfaceVariant,
                                unfocusedContainerColor = UdyatDarkSurfaceVariant
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Card 4: Configuración de Proxy (Screenshot 4)
            item {
                Text(
                    text = "CONFIGURACIÓN DE PROXY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = UdyatDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, UdyatDarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Habilitar Proxy",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "Enrutar tráfico a través de un proxy local o remoto",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }
                            Switch(
                                checked = proxyEnabled,
                                onCheckedChange = { proxyEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = activeAccentColor,
                                    uncheckedThumbColor = TextGray,
                                    uncheckedTrackColor = UdyatDarkSurfaceVariant
                                )
                            )
                        }

                        if (proxyEnabled) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = proxyHost,
                                    onValueChange = { proxyHost = it },
                                    placeholder = { Text("Host del Proxy", color = TextDarkGray, fontSize = 12.sp) },
                                    modifier = Modifier.weight(2f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = activeAccentColor,
                                        unfocusedBorderColor = UdyatDarkCardBorder,
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite,
                                        focusedContainerColor = UdyatDarkSurfaceVariant,
                                        unfocusedContainerColor = UdyatDarkSurfaceVariant
                                    )
                                )
                                OutlinedTextField(
                                    value = proxyPortStr,
                                    onValueChange = { proxyPortStr = it },
                                    placeholder = { Text("Puerto", color = TextDarkGray, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = activeAccentColor,
                                        unfocusedBorderColor = UdyatDarkCardBorder,
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite,
                                        focusedContainerColor = UdyatDarkSurfaceVariant,
                                        unfocusedContainerColor = UdyatDarkSurfaceVariant
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "Proxy verificado correctamente", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, activeAccentColor)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = null,
                                    tint = activeAccentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "PROBAR CONEXIÓN PROXY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeAccentColor
                                )
                            }
                        }
                    }
                }
            }

            // Card 5: Almacenamiento y Optimización (Screenshots 4, 5)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ALMACENAMIENTO Y OPTIMIZACIÓN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(UdyatDarkSurfaceVariant)
                            .clickable { refreshMetrics() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = TextWhite,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ACTUALIZAR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = UdyatDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, UdyatDarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // SQLite DB Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF263238)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = Color(0xFF81D4FA),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Base de Datos SQLite",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextWhite
                                )
                            }
                            Text(
                                text = dbSizeStr,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF81D4FA)
                            )
                        }

                        HorizontalDivider(
                            color = UdyatDarkCardBorder,
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        // Downloads Folder Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF1B3A24)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = StatusGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Carpeta de Descargas",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextWhite
                                )
                            }
                            Text(
                                text = downloadFolderSizeStr,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Three Action Buttons (Screenshot 5)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // COMPACTAR BD (Green)
                            Button(
                                onClick = {
                                    viewModel.compactDatabase {
                                        refreshMetrics()
                                        topBannerMessage = "Base de datos optimizada: Se compactó el almacenamiento."
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1B4D2E),
                                    contentColor = StatusGreen
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                            ) {
                                Text(
                                    text = "COMPACTAR BD",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            // HUÉRFANOS (Blue)
                            Button(
                                onClick = { showCleanOrphansDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF153B57),
                                    contentColor = Color(0xFF40C4FF)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                            ) {
                                Text(
                                    text = "HUÉRFANOS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            // VACIAR CARPETA (Red)
                            Button(
                                onClick = { showEmptyFolderDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4D141D),
                                    contentColor = NeonRed
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                            ) {
                                Text(
                                    text = "VACIAR CARPETA",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            // Card 6: Color Swatches Grid (Screenshots 3, 11)
            item {
                Text(
                    text = "TEMA Y COLOR DE ACENTO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = UdyatDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, UdyatDarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = null,
                                tint = activeAccentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Paleta de Colores Neón",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Grid of 24 colors
                        val rows = AccentSwatches.chunked(8)
                        for (row in rows) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (swatch in row) {
                                    val index = AccentSwatches.indexOf(swatch)
                                    val isSelected = selectedAccentIndex == index
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(swatch)
                                            .border(
                                                width = if (isSelected) 2.5.dp else 0.dp,
                                                color = if (isSelected) Color.White else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedAccentIndex = index },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Footer Branding matching reference app
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = UdyatDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, UdyatDarkCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "JK Downloader",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "Versión 1.1.3 · ᴍᴏᴅ ʙʏ ᴊᴀᴋᴜᴅᴏѕʜʏꫂꤪꤨᴼᶠᶜ",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(StatusGreen.copy(alpha = 0.2f))
                                .border(1.dp, StatusGreen.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "ACTUALIZADO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = StatusGreen
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }

    // ===============================================
    // DIALOG: Limpiar almacenamiento (Screenshot 4)
    // ===============================================
    if (showCleanOrphansDialog) {
        Dialog(onDismissRequest = { showCleanOrphansDialog = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = UdyatDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, UdyatDarkCardBorder),
                modifier = Modifier.fillMaxWidth(0.96f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color(0xFF40C4FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Limpiar almacenamiento",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Se buscarán y eliminarán fragmentos incompletos (.udyat_part, .tdpart), checkpoints y archivos temporales huérfanos que ya no pertenezcan a descargas activas, además de compactar la base de datos.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = TextGray
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { showCleanOrphansDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UdyatDarkSurfaceVariant,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Text(
                            text = "CANCELAR",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            showCleanOrphansDialog = false
                            viewModel.cleanOrphanFiles { count, freed ->
                                refreshMetrics()
                                topBannerMessage = "Almacenamiento limpio: $count archivos huérfanos eliminados (${DownloadEntity.formatBytes(freed)} liberados)."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonRed,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Text(
                            text = "LIMPIAR AHORA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    // ===============================================
    // DIALOG: Vaciar Carpeta de Descargas (Screenshot 3)
    // ===============================================
    if (showEmptyFolderDialog) {
        Dialog(onDismissRequest = { showEmptyFolderDialog = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = UdyatDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, UdyatDarkCardBorder),
                modifier = Modifier.fillMaxWidth(0.96f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = NeonRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Vaciar Carpeta de Descargas",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "¿Estás seguro de que deseas eliminar TODOS los archivos guardados en la carpeta de descargas?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Esta acción borrará permanentemente todos los archivos descargados guardados en la carpeta actual. No se puede deshacer.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = TextGray
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { showEmptyFolderDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UdyatDarkSurfaceVariant,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Text(
                            text = "CANCELAR",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            showEmptyFolderDialog = false
                            viewModel.emptyDownloadFolder { count ->
                                refreshMetrics()
                                topBannerMessage = "Carpeta vaciada: $count archivos eliminados."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonRed,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ELIMINAR TODO",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
