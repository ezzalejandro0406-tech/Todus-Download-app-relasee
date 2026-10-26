package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusCyan
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextDarkGray
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.theme.UdyatDarkBackground
import com.example.ui.theme.UdyatDarkCardBorder
import com.example.ui.theme.UdyatDarkSurface
import com.example.ui.theme.UdyatDarkSurfaceVariant

@Composable
fun UdyatDrawerContent(
    userProfile: UserProfile,
    onOpenProfileLink: () -> Unit,
    onOpenCloudFiles: () -> Unit,
    onOpenLogs: () -> Unit,
    onOpenAbout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val context = LocalContext.current
    val accentColor = MaterialTheme.colorScheme.primary

    ModalDrawerSheet(
        drawerContainerColor = UdyatDarkBackground,
        drawerContentColor = TextWhite,
        modifier = Modifier.width(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            // Profile Card (Screenshot 14 with guest/linked states)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(UdyatDarkSurface)
                    .border(1.dp, UdyatDarkCardBorder, RoundedCornerShape(16.dp))
                    .clickable {
                        onCloseDrawer()
                        onOpenProfileLink()
                    }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .border(2.dp, if (userProfile.isLinked) StatusGreen else TextDarkGray, CircleShape)
                            .background(UdyatDarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (userProfile.isLinked) userProfile.name.take(1).uppercase() else "I",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (userProfile.isLinked) StatusGreen else TextGray
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = userProfile.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = if (userProfile.isLinked) userProfile.username else "Toca para vincular bot",
                            fontSize = 12.sp,
                            color = if (userProfile.isLinked) TextGray else accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Menu Items (Screenshot 14)
            DrawerMenuItem(
                icon = Icons.Default.Cloud,
                iconBg = Color(0xFF102845),
                iconTint = Color(0xFF29B6F6),
                title = "Archivos en la Nube",
                onClick = {
                    onCloseDrawer()
                    onOpenCloudFiles()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.Send,
                iconBg = Color(0xFF132F20),
                iconTint = StatusGreen,
                title = "Canal de Telegram",
                onClick = {
                    onCloseDrawer()
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/gg_no_rot")))
                    } catch (_: Exception) {
                        Toast.makeText(context, "Telegram: t.me/gg_no_rot", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.Link,
                iconBg = Color(0xFF332612),
                iconTint = StatusAmber,
                title = "Grupo de Soporte",
                onClick = {
                    onCloseDrawer()
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/gg_no_rot")))
                    } catch (_: Exception) {
                        Toast.makeText(context, "Telegram: t.me/gg_no_rot", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.Code,
                iconBg = Color(0xFF10333E),
                iconTint = StatusCyan,
                title = "Registros de la App (Logs)",
                subtitle = "Diagnóstico y registro de errores",
                onClick = {
                    onCloseDrawer()
                    onOpenLogs()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.Info,
                iconBg = UdyatDarkSurfaceVariant,
                iconTint = TextGray,
                title = "Acerca de",
                onClick = {
                    onCloseDrawer()
                    onOpenAbout()
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(color = UdyatDarkCardBorder, thickness = 0.5.dp)

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Settings Item (Screenshot 14)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onCloseDrawer()
                        onNavigateToSettings()
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Ajustes",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite
                )
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite
            )
            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextDarkGray
                )
            }
        }
    }
}
