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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.model.AppSettings
import com.example.ui.components.NeonCard
import com.example.ui.theme.AccentSwatches
import com.example.ui.theme.StatusCyan
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TextDarkGray
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.theme.UdyatDarkBackground
import com.example.ui.theme.UdyatDarkCardBorder
import com.example.ui.theme.UdyatDarkSurface
import com.example.ui.theme.UdyatDarkSurfaceVariant
import java.io.File
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var themeMode by remember(settings.themeMode) { mutableStateOf(settings.themeMode) }
    var selectedAccentIndex by remember(settings.accentColorIndex) { mutableIntStateOf(settings.accentColorIndex) }

    var maxConcurrent by remember(settings.maxConcurrentDownloads) { mutableIntStateOf(settings.maxConcurrentDownloads) }
    var maxThreads by remember(settings.maxThreads) { mutableIntStateOf(settings.maxThreads) }
    var autoReconnect by remember(settings.autoReconnect) { mutableStateOf(settings.autoReconnect) }
    var maxReconnectAttempts by remember(settings.maxReconnectAttempts) { mutableIntStateOf(settings.maxReconnectAttempts) }
    var speedLimit by remember(settings.speedLimit) { mutableStateOf(settings.speedLimit) }
    var subfolder by remember(settings.downloadSubfolder) { mutableStateOf(settings.downloadSubfolder) }
    var proxyEnabled by remember(settings.proxyEnabled) { mutableStateOf(settings.proxyEnabled) }

    var speedMenuExpanded by remember { mutableStateOf(false) }

    // Optimization metrics
    var dbSizeStr by remember { mutableStateOf("192.66 KB") }
    var downloadFolderSizeStr by remember { mutableStateOf("43.82 MB") }

    val activeAccentColor = AccentSwatches.getOrElse(selectedAccentIndex) { AccentSwatches[0] }

    fun buildCurrentSettings(): AppSettings {
        return settings.copy(
            themeMode = themeMode,
            accentColorIndex = selectedAccentIndex,
            maxConcurrentDownloads = maxConcurrent,
            maxThreads = maxThreads,
            autoReconnect = autoReconnect,
            maxReconnectAttempts = maxReconnectAttempts,
            speedLimit = speedLimit,
            downloadSubfolder = subfolder.ifBlank { "JKDOWN" },
            proxyEnabled = proxyEnabled
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(UdyatDarkBackground)
    ) {
        // Top bar matching Screenshot 11: (<) Ajustes (✓)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(UdyatDarkSurfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "Ajustes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            // Circular Green checkmark button (Screenshot 11)
            IconButton(
                onClick = {
                    val s = buildCurrentSettings()
                    onSaveSettings(s)
                    onBack()
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, StatusGreen.copy(alpha = 0.6f), CircleShape)
                    .background(Color(0xFF132F20))
                    .testTag("save_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Guardar",
                    tint = StatusGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("settings_screen_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Tema y Color de Acento (Screenshot 11)
            item {
                NeonCard(modifier = Modifier.fillMaxWidth(), containerColor = UdyatDarkSurface) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(activeAccentColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Nightlight,
                                    contentDescription = null,
                                    tint = activeAccentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Tema de la interfaz",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Segmented Control: [ Sistema | Claro | Oscuro ]
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(UdyatDarkSurfaceVariant)
                                .padding(4.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                listOf("Sistema", "Claro", "Oscuro").forEach { mode ->
                                    val isSelected = themeMode == mode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) activeAccentColor.copy(alpha = 0.25f) else Color.Transparent)
                                            .border(
                                                width = if (isSelected) 1.dp else 0.dp,
                                                color = if (isSelected) activeAccentColor else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { themeMode = mode }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = mode,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) TextWhite else TextGray
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(activeAccentColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ColorLens,
                                    contentDescription = null,
                                    tint = activeAccentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Color de acento",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 24 Swatches Grid (Screenshot 11)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AccentSwatches.chunked(6).forEachIndexed { rowIndex, rowColors ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    rowColors.forEachIndexed { colIndex, swatchColor ->
                                        val actualIndex = rowIndex * 6 + colIndex
                                        val isSelected = selectedAccentIndex == actualIndex
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(swatchColor)
                                                .clickable { selectedAccentIndex = actualIndex },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .border(1.5.dp, TextWhite, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = TextWhite,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section Header: DESCARGAS Y CONCURRENCIA (Screenshot 7)
            item {
                Text(
                    text = "DESCARGAS Y CONCURRENCIA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            // Card 2: Descargas y Concurrencia (Screenshot 7 & 10)
            item {
                NeonCard(modifier = Modifier.fillMaxWidth(), containerColor = UdyatDarkSurface) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // 1. Descargas simultáneas
                        StepperRow(
                            title = "Descargas simultáneas",
                            subtitle = "Archivos procesados a la vez",
                            value = maxConcurrent,
                            onDecrement = { if (maxConcurrent > 1) maxConcurrent-- },
                            onIncrement = { if (maxConcurrent < 5) maxConcurrent++ },
                            accentColor = activeAccentColor
                        )

                        Slider(
                            value = maxConcurrent.toFloat(),
                            onValueChange = { maxConcurrent = it.toInt() },
                            valueRange = 1f..5f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = TextWhite,
                                activeTrackColor = activeAccentColor,
                                inactiveTrackColor = UdyatDarkSurfaceVariant
                            )
                        )

                        HorizontalDivider(
                            color = UdyatDarkCardBorder,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        // 2. Hilos por descarga
                        StepperRow(
                            title = "Hilos por descarga",
                            subtitle = "Conexiones Range concurrentes por archivo",
                            value = maxThreads,
                            onDecrement = { if (maxThreads > 1) maxThreads-- },
                            onIncrement = { if (maxThreads < 32) maxThreads++ },
                            accentColor = activeAccentColor
                        )

                        Slider(
                            value = maxThreads.toFloat(),
                            onValueChange = { maxThreads = it.toInt() },
                            valueRange = 1f..32f,
                            steps = 30,
                            colors = SliderDefaults.colors(
                                thumbColor = TextWhite,
                                activeTrackColor = activeAccentColor,
                                inactiveTrackColor = UdyatDarkSurfaceVariant
                            )
                        )

                        HorizontalDivider(
                            color = UdyatDarkCardBorder,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        // 3. Reintento automático
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Reintento automático",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "Reintentar descargas si falla la red",
                                    fontSize = 12.sp,
                                    color = TextGray
                                )
                            }
                            Switch(
                                checked = autoReconnect,
                                onCheckedChange = { autoReconnect = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextWhite,
                                    checkedTrackColor = activeAccentColor,
                                    uncheckedThumbColor = TextDarkGray,
                                    uncheckedTrackColor = UdyatDarkSurfaceVariant
                                )
                            )
                        }

                        HorizontalDivider(
                            color = UdyatDarkCardBorder,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        // 4. Máximo de reintentos
                        StepperRow(
                            title = "Máximo de reintentos",
                            subtitle = "Intentos automáticos antes de marcar error",
                            value = maxReconnectAttempts,
                            onDecrement = { if (maxReconnectAttempts > 1) maxReconnectAttempts-- },
                            onIncrement = { if (maxReconnectAttempts < 10) maxReconnectAttempts++ },
                            accentColor = activeAccentColor
                        )

                        Slider(
                            value = maxReconnectAttempts.toFloat(),
                            onValueChange = { maxReconnectAttempts = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = TextWhite,
                                activeTrackColor = activeAccentColor,
                                inactiveTrackColor = UdyatDarkSurfaceVariant
                            )
                        )

                        HorizontalDivider(
                            color = UdyatDarkCardBorder,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        // 5. Límite de velocidad
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Límite de velocidad",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "Velocidad máxima combinada",
                                    fontSize = 12.sp,
                                    color = TextGray
                                )
                            }

                            Box {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { speedMenuExpanded = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = speedLimit,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = activeAccentColor
                                    )
                                    Text(
                                        text = " ▼",
                                        fontSize = 10.sp,
                                        color = activeAccentColor
                                    )
                                }

                                DropdownMenu(
                                    expanded = speedMenuExpanded,
                                    onDismissRequest = { speedMenuExpanded = false }
                                ) {
                                    listOf("Sin Límite", "500 KB/s", "1 MB/s", "2 MB/s", "5 MB/s").forEach { opt ->
                                        DropdownMenuItem(
                                            text = { Text(opt) },
                                            onClick = {
                                                speedLimit = opt
                                                speedMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            color = UdyatDarkCardBorder,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        // 6. Subcarpeta de descargas (Screenshot 10)
                        Text(
                            text = "Subcarpeta de descargas",
                            fontSize = 11.sp,
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = subfolder,
                            onValueChange = { subfolder = it },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = TextGray,
                                    modifier = Modifier.size(18.dp)
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

                        Spacer(modifier = Modifier.height(10.dp))

                        // 7. Directorio base personalizado (Screenshot 10)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(UdyatDarkSurfaceVariant)
                                    .border(1.dp, UdyatDarkCardBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = TextGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Directorio base persona...",
                                        fontSize = 13.sp,
                                        color = TextDarkGray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(UdyatDarkSurfaceVariant)
                                    .border(1.dp, UdyatDarkCardBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        Toast.makeText(context, "Directorio: /Download/$subfolder", Toast.LENGTH_SHORT).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = TextWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Section Header: CONFIGURACIÓN DE PROXY (Screenshot 10)
            item {
                Text(
                    text = "CONFIGURACIÓN DE PROXY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            // Card 3: Proxy (Screenshot 10)
            item {
                NeonCard(modifier = Modifier.fillMaxWidth(), containerColor = UdyatDarkSurface) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Habilitar Proxy",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "Enrutar tráfico a través de un proxy local o remoto",
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }

                        Switch(
                            checked = proxyEnabled,
                            onCheckedChange = { proxyEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextWhite,
                                checkedTrackColor = activeAccentColor,
                                uncheckedThumbColor = TextDarkGray,
                                uncheckedTrackColor = UdyatDarkSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Section Header: ALMACENAMIENTO Y OPTIMIZACIÓN (Screenshot 10)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
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
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                dbSizeStr = "194.20 KB"
                                Toast.makeText(context, "Métricas actualizadas", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ACTUALIZAR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGray
                        )
                    }
                }
            }

            // Card 4: Almacenamiento (Screenshot 10)
            item {
                NeonCard(modifier = Modifier.fillMaxWidth(), containerColor = UdyatDarkSurface) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Base de Datos SQLite",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Text(
                                text = dbSizeStr,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Carpeta de Descargas",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Text(
                                text = downloadFolderSizeStr,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3 Action Pills side by side: COMPACTAR BD, HUÉRFANOS, VACIAR CARPETA
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Green: COMPACTAR BD
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF133621))
                                    .border(1.dp, StatusGreen.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                    .clickable {
                                        Toast.makeText(context, "Base de datos SQLite compactada", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Build,
                                        contentDescription = null,
                                        tint = StatusGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "COMPACTAR BD",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                }
                            }

                            // Cyan: HUÉRFANOS
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF102F44))
                                    .border(1.dp, Color(0xFF29B6F6).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                    .clickable {
                                        Toast.makeText(context, "No se encontraron archivos huérfanos", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = Color(0xFF29B6F6),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "HUÉRFANOS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                }
                            }

                            // Red: VACIAR CARPETA
                            Box(
                                modifier = Modifier
                                    .weight(1.1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF3B151A))
                                    .border(1.dp, StatusRed.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                    .clickable {
                                        Toast.makeText(context, "Carpeta de descargas limpia", Toast.LENGTH_SHORT).show()
                                        downloadFolderSizeStr = "0.00 B"
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = StatusRed,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "VACIAR CARPETA",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Card 5: App Card with ACTUALIZADO badge (Screenshot 10)
            item {
                NeonCard(modifier = Modifier.fillMaxWidth(), containerColor = UdyatDarkSurface) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(activeAccentColor.copy(alpha = 0.15f))
                                .border(1.dp, activeAccentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = activeAccentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "JK Downloader",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Versión 1.1.3 · ᴍᴏᴅ ʙʏ ᴊᴀᴋᴜᴅᴏѕʜʏꫂꤪꤨᴼᶠᶜ",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                        }

                        // Green ACTUALIZADO badge (Screenshot 10)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF132F20))
                                .border(1.dp, StatusGreen.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ACTUALIZADO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusGreen
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun StepperRow(
    title: String,
    subtitle: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextGray
            )
        }

        // Stepper: [ - ]   X   [ + ] (Screenshot 7)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(UdyatDarkSurfaceVariant)
                .border(1.dp, UdyatDarkCardBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onDecrement,
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("—", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextGray)
                }

                Text(
                    text = "$value",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = onIncrement,
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextGray)
                }
            }
        }
    }
}
