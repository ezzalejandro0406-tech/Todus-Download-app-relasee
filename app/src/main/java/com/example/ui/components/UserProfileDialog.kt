package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.UserProfile
import com.example.ui.theme.NeonRed
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextDarkGray
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.theme.UdyatDarkBackground
import com.example.ui.theme.UdyatDarkCardBorder
import com.example.ui.theme.UdyatDarkSurface
import com.example.ui.theme.UdyatDarkSurfaceVariant
import java.util.Locale

@Composable
fun UserProfileDialog(
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    onUnlink: () -> Unit
) {
    val accentColor = MaterialTheme.colorScheme.primary

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = UdyatDarkBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, UdyatDarkCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header: (<) Perfil de Usuario
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Perfil de Usuario",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Avatar Card (Screenshots 8, 9)
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.BottomEnd) {
                                if (!userProfile.photoUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = userProfile.photoUrl,
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, StatusGreen, CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF229ED9), Color(0xFF00E676))
                                                )
                                            )
                                            .border(2.dp, StatusGreen, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = userProfile.fullName.firstOrNull()?.toString() ?: "J",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(StatusGreen)
                                        .border(1.dp, UdyatDarkSurface, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = if (userProfile.username.isNotBlank()) userProfile.username else "@Jakudoshy",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(StatusGreen.copy(alpha = 0.2f))
                                        .border(1.dp, StatusGreen.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = userProfile.planName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = StatusGreen
                                    )
                                }
                            }
                        }
                    }

                    // Card 1: Información de Identidad (Screenshot 8)
                    ProfileSectionCard(
                        title = "Información de Identidad",
                        icon = Icons.Default.Person,
                        iconTint = Color(0xFF29B6F6)
                    ) {
                        ProfileInfoRow("Nombre", userProfile.fullName)
                        HorizontalDivider(color = UdyatDarkCardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                        ProfileInfoRow("Alias Telegram", if (userProfile.username.isNotBlank()) userProfile.username else "@Jakudoshy")
                        HorizontalDivider(color = UdyatDarkCardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                        ProfileInfoRow("ID Telegram", if (userProfile.telegramId.isNotBlank()) userProfile.telegramId else "7935796405")
                        HorizontalDivider(color = UdyatDarkCardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                        ProfileInfoRow("Teléfono ToDus", userProfile.phone)
                        HorizontalDivider(color = UdyatDarkCardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                        ProfileInfoRow("Rol", userProfile.role)
                        HorizontalDivider(color = UdyatDarkCardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                        ProfileInfoRow("Última actividad", userProfile.lastActivity)
                    }

                    // Card 2: Suscripción y Plan (Screenshot 8)
                    ProfileSectionCard(
                        title = "Suscripción y Plan",
                        icon = Icons.Default.Star,
                        iconTint = Color(0xFFFFB300)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Estado del Plan", fontSize = 12.sp, color = TextGray)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(StatusGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    userProfile.planStatus,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = StatusGreen
                                )
                            }
                        }
                        HorizontalDivider(color = UdyatDarkCardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                        ProfileInfoRow("Vencimiento", userProfile.planExpiration)
                    }

                    // Card 3: Consumo de Tráfico (Screenshot 9)
                    ProfileSectionCard(
                        title = "Consumo de Tráfico",
                        icon = Icons.Default.DataUsage,
                        iconTint = Color(0xFF00E5FF)
                    ) {
                        val progressFraction = (userProfile.trafficUsedGb / userProfile.trafficTotalGb).coerceIn(0.0, 1.0).toFloat()
                        val percentStr = String.format(Locale.US, "%.1f%%", progressFraction * 100)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Consumo del mes", fontSize = 12.sp, color = TextGray)
                            Text(percentStr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = accentColor,
                            trackColor = UdyatDarkSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Consumido: ${String.format(Locale.US, "%.2f", userProfile.trafficUsedGb)} GB / ${String.format(Locale.US, "%.2f", userProfile.trafficTotalGb)} GB",
                                fontSize = 11.sp,
                                color = TextDarkGray
                            )
                            val remaining = (userProfile.trafficTotalGb - userProfile.trafficUsedGb).coerceAtLeast(0.0)
                            Text(
                                text = "Restante: ${String.format(Locale.US, "%.2f", remaining)} GB",
                                fontSize = 11.sp,
                                color = TextDarkGray
                            )
                        }
                    }

                    // Card 4: Almacenamiento en la Nube (Screenshot 9)
                    ProfileSectionCard(
                        title = "Almacenamiento en la Nube",
                        icon = Icons.Default.Cloud,
                        iconTint = Color(0xFF42A5F5)
                    ) {
                        ProfileInfoRow(
                            "Espacio ocupado",
                            "${String.format(Locale.US, "%.2f", userProfile.cloudUsedGb)} GB / ${String.format(Locale.US, "%.2f", userProfile.cloudTotalGb)} GB"
                        )
                        HorizontalDivider(color = UdyatDarkCardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                        ProfileInfoRow("Archivos guardados", "${userProfile.cloudFilesCount} archivos")
                    }

                    // Card 5: Dispositivos Vinculados (Screenshot 10)
                    ProfileSectionCard(
                        title = "Dispositivos Vinculados",
                        icon = Icons.Default.Devices,
                        iconTint = Color(0xFFAB47BC)
                    ) {
                        ProfileInfoRow("Dispositivos activos", "${userProfile.activeDevices} de ${userProfile.maxDevices} permitidos")
                    }

                    // Card 6: Configuración del Bot (Screenshot 10)
                    ProfileSectionCard(
                        title = "Configuración del Bot",
                        icon = Icons.Default.Settings,
                        iconTint = Color(0xFFFF7043)
                    ) {
                        ProfileInfoRow("Estrategia de Subida", userProfile.uploadStrategy)
                        HorizontalDivider(color = UdyatDarkCardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                        ProfileInfoRow("Tamaño de Partes", "${userProfile.partSizeMb} MB")
                        HorizontalDivider(color = UdyatDarkCardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                        ProfileInfoRow("Subida Automática", if (userProfile.autoUpload) "Habilitada" else "Deshabilitada")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bottom Action: Desvincular Cuenta (Screenshot 10)
                    OutlinedButton(
                        onClick = {
                            onUnlink()
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NeonRed
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = NeonRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DESVINCULAR CUENTA DE TELEGRAM",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonRed
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = UdyatDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, UdyatDarkCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite
                )
            }
            content()
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextGray
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextWhite
        )
    }
}
