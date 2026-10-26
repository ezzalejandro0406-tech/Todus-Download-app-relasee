package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.theme.NeonOrangeWarning
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
fun SettingsScreen(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var maxThreads by remember(settings.maxThreads) { mutableFloatStateOf(settings.maxThreads.toFloat()) }
    var autoReconnect by remember(settings.autoReconnect) { mutableStateOf(settings.autoReconnect) }
    var sslSniBypass by remember(settings.sslSniBypass) { mutableStateOf(settings.sslSniBypass) }
    var proxyEnabled by remember(settings.proxyEnabled) { mutableStateOf(settings.proxyEnabled) }
    var proxyHost by remember(settings.proxyHost) { mutableStateOf(settings.proxyHost) }
    var proxyPort by remember(settings.proxyPort) { mutableStateOf(settings.proxyPort.toString()) }
    var notifyOnFinish by remember(settings.notifyOnFinish) { mutableStateOf(settings.notifyOnFinish) }

    fun commit() {
        onSaveSettings(
            settings.copy(
                maxThreads = maxThreads.toInt(),
                autoReconnect = autoReconnect,
                sslSniBypass = sslSniBypass,
                proxyEnabled = proxyEnabled,
                proxyHost = proxyHost.trim(),
                proxyPort = proxyPort.toIntOrNull() ?: 8080,
                notifyOnFinish = notifyOnFinish
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Motor de descarga
        item {
            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        icon = Icons.Default.Bolt,
                        title = "MOTOR Y VELOCIDAD MULTIHILO",
                        subtitle = "Ajusta la cantidad de hilos paralelos para descarga rápida"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hilos por archivo:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${maxThreads.toInt()} Hilos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonRedBright
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
                            thumbColor = NeonRedBright,
                            activeTrackColor = NeonRedPrimary,
                            inactiveTrackColor = NeonRedDarkSurfaceVariant
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(4, 8, 16, 32).forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (maxThreads.toInt() == preset) NeonRedPrimary else NeonRedDarkSurfaceVariant
                                    )
                                    .clickable {
                                        maxThreads = preset.toFloat()
                                        commit()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$preset",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Reconexión inteligente
        item {
            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        icon = Icons.Default.Sync,
                        title = "RECONEXIÓN AUTOMÁTICA",
                        subtitle = "Reanuda sin perder partes ante cortes de datos móviles"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-reconexión activa",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Reintenta automáticamente al detectar pérdida de conexión",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Switch(
                            checked = autoReconnect,
                            onCheckedChange = {
                                autoReconnect = it
                                commit()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextPrimary,
                                checkedTrackColor = NeonRedPrimary,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = NeonRedDarkSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Intentos máximos: 10 reintentos automáticos con espera progresiva.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Section: Red y SSL
        item {
            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        icon = Icons.Default.Security,
                        title = "RED NACIONAL Y SSL/SNI",
                        subtitle = "Compatibilidad con servidores y proxies de Cuba"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bypass SSL/SNI toDus",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Permite certificados locales en red toDus y Cuba",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Switch(
                            checked = sslSniBypass,
                            onCheckedChange = {
                                sslSniBypass = it
                                commit()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextPrimary,
                                checkedTrackColor = NeonRedPrimary,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = NeonRedDarkSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Proxy Settings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Servidor Proxy HTTP",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Opcional para túneles o APN específicos",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Switch(
                            checked = proxyEnabled,
                            onCheckedChange = {
                                proxyEnabled = it
                                commit()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextPrimary,
                                checkedTrackColor = NeonRedPrimary,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = NeonRedDarkSurfaceVariant
                            )
                        )
                    }

                    if (proxyEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = proxyHost,
                                onValueChange = {
                                    proxyHost = it
                                    commit()
                                },
                                placeholder = { Text("127.0.0.1", color = TextMuted, fontSize = 12.sp) },
                                label = { Text("Host Proxy", fontSize = 11.sp) },
                                modifier = Modifier.weight(2f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonRedPrimary,
                                    unfocusedBorderColor = NeonRedCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            OutlinedTextField(
                                value = proxyPort,
                                onValueChange = {
                                    proxyPort = it
                                    commit()
                                },
                                placeholder = { Text("8080", color = TextMuted, fontSize = 12.sp) },
                                label = { Text("Puerto", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonRedPrimary,
                                    unfocusedBorderColor = NeonRedCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section: Batería y Sistema
        item {
            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        icon = Icons.Default.BatteryAlert,
                        title = "OPTIMIZACIÓN DE BATERÍA",
                        subtitle = "Evita que Android corte las descargas con la pantalla apagada"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Android puede pausar la conexión si detecta ahorro de batería activado. Toca abajo para permitir descargas en segundo plano ininterrumpidas.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                                    if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
                                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(context, "La app ya tiene permiso sin restricciones", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (_: Exception) {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Abre Ajustes > Batería en tu dispositivo", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRedDarkSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryAlert,
                            contentDescription = null,
                            tint = NeonOrangeWarning,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Excluir de Ahorro de Batería",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Storage path card
        item {
            NeonCard(modifier = Modifier.fillMaxWidth()) {
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
                            .background(NeonRedContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = NeonRedBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Carpeta de destino",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Descargas/toDus Download",
                            fontSize = 12.sp,
                            color = TextSecondary
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

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(NeonRedContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonRedBright,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = NeonRedBright
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}
