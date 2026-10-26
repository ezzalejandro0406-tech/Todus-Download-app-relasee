package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
import com.example.ui.components.NeonCard
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentRedContainer
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.TextDarkGray
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var maxThreads by remember(settings.maxThreads) { mutableFloatStateOf(settings.maxThreads.toFloat()) }
    var maxConcurrent by remember(settings.maxConcurrentDownloads) { mutableFloatStateOf(settings.maxConcurrentDownloads.toFloat()) }
    var autoReconnect by remember(settings.autoReconnect) { mutableStateOf(settings.autoReconnect) }
    var showNotifications by remember(settings.showNotifications) { mutableStateOf(settings.showNotifications) }

    fun commit() {
        onSaveSettings(
            settings.copy(
                maxThreads = maxThreads.toInt(),
                maxConcurrentDownloads = maxConcurrent.toInt(),
                autoReconnect = autoReconnect,
                showNotifications = showNotifications
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section: Descargas
        item {
            NeonCard(modifier = Modifier.fillMaxWidth(), containerColor = DarkSurface) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Descargas",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentRed
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Hilos por archivo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Hilos por archivo",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Text(
                                text = "Más hilos, más velocidad",
                                fontSize = 12.sp,
                                color = TextDarkGray
                            )
                        }
                        Text(
                            text = "${maxThreads.toInt()} hilos",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentRed
                        )
                    }

                    Slider(
                        value = maxThreads,
                        onValueChange = {
                            maxThreads = it
                            commit()
                        },
                        valueRange = 1f..32f,
                        steps = 30,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentRed,
                            activeTrackColor = AccentRed,
                            inactiveTrackColor = DarkSurfaceVariant
                        )
                    )

                    HorizontalDivider(
                        color = DarkCardBorder,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    // Descargas simultáneas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Descargas simultáneas",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Text(
                                text = if (maxConcurrent.toInt() == 1) {
                                    "Se descarga uno a la vez, los demás esperan."
                                } else {
                                    "Se descargan hasta ${maxConcurrent.toInt()} descargas a la vez."
                                },
                                fontSize = 12.sp,
                                color = TextDarkGray
                            )
                        }
                        Text(
                            text = "${maxConcurrent.toInt()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentRed
                        )
                    }

                    Slider(
                        value = maxConcurrent,
                        onValueChange = {
                            maxConcurrent = it
                            commit()
                        },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentRed,
                            activeTrackColor = AccentRed,
                            inactiveTrackColor = DarkSurfaceVariant
                        )
                    )

                    HorizontalDivider(
                        color = DarkCardBorder,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    // Reconexión automática
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reconexión automática",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Text(
                                text = "Reanuda sin perder progreso al reconectar datos móviles",
                                fontSize = 12.sp,
                                color = TextDarkGray
                            )
                        }
                        Switch(
                            checked = autoReconnect,
                            onCheckedChange = {
                                autoReconnect = it
                                commit()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextWhite,
                                checkedTrackColor = AccentRed,
                                uncheckedThumbColor = TextDarkGray,
                                uncheckedTrackColor = DarkSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        // Section: Almacenamiento y Notificaciones
        item {
            NeonCard(modifier = Modifier.fillMaxWidth(), containerColor = DarkSurface) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sistema y Almacenamiento",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentRed
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Notificaciones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notificaciones",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Text(
                                text = "Avisos cuando una descarga termine o falle",
                                fontSize = 12.sp,
                                color = TextDarkGray
                            )
                        }
                        Switch(
                            checked = showNotifications,
                            onCheckedChange = {
                                showNotifications = it
                                commit()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextWhite,
                                checkedTrackColor = AccentRed,
                                uncheckedThumbColor = TextDarkGray,
                                uncheckedTrackColor = DarkSurfaceVariant
                            )
                        )
                    }

                    HorizontalDivider(
                        color = DarkCardBorder,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Carpeta de descargas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = AccentRed, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Carpeta de descargas",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Text(
                                text = "Download/toDus Download",
                                fontSize = 12.sp,
                                color = TextDarkGray
                            )
                        }
                    }
                }
            }
        }

        // Section: Batería
        item {
            NeonCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                                if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
                                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(context, "Ya está excluido de optimización de batería", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (_: Exception) {
                            try {
                                context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                            } catch (_: Exception) {
                                Toast.makeText(context, "Abre Ajustes > Batería", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                containerColor = DarkSurface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.BatteryAlert, contentDescription = null, tint = StatusOrange, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Para que Android no corte las descargas al apagar la pantalla.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Toca para que Android no corte las descargas",
                            fontSize = 12.sp,
                            color = AccentRed
                        )
                    }
                }
            }
        }

        // Section: Creador
        item {
            NeonCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Eliel_21"))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Telegram: @Eliel_21", Toast.LENGTH_SHORT).show()
                        }
                    },
                containerColor = DarkSurface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentRedContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = AccentRed, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Creador de toDus Download",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                        Text(
                            text = "@Eliel_21 · Toca para abrir Telegram",
                            fontSize = 12.sp,
                            color = TextDarkGray
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
