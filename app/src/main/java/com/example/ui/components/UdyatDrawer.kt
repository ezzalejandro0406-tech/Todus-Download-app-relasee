package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
        modifier = Modifier.width(310.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 20.dp, horizontal = 16.dp)
        ) {
            // Profile Card with Official Brand Logo & Verified Data
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                UdyatDarkSurfaceVariant,
                                UdyatDarkSurface
                            )
                        )
                    )
                    .border(
                        1.dp,
                        if (userProfile.isLinked) StatusGreen.copy(alpha = 0.6f) else accentColor.copy(alpha = 0.4f),
                        RoundedCornerShape(18.dp)
                    )
                    .clickable {
                        onCloseDrawer()
                        onOpenProfileLink()
                    }
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(54.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_jk_brand),
                                contentDescription = "Avatar JK",
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(
                                        2.dp,
                                        if (userProfile.isLinked) StatusGreen else accentColor,
                                        CircleShape
                                    )
                            )
                            if (userProfile.isLinked) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(StatusGreen)
                                        .border(1.dp, UdyatDarkSurface, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verificado",
                                        tint = Color.Black,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userProfile.fullName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            if (userProfile.isLinked) {
                                Text(
                                    text = userProfile.telegramId,
                                    fontSize = 11.sp,
                                    color = StatusCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Text(
                                    text = "Toca para vincular con @downfree_jk_bot",
                                    fontSize = 10.sp,
                                    color = accentColor,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }

                    if (userProfile.isLinked) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(UdyatDarkBackground.copy(alpha = 0.7f))
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(StatusGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "VERIFICADO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusGreen
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Saldo: ${userProfile.balance}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Menu Items
            DrawerMenuItem(
                icon = Icons.Default.Cloud,
                iconBg = Color(0xFF102845),
                iconTint = Color(0xFF29B6F6),
                title = "Archivos en la Nube",
                subtitle = "Historial sincronizado del mes",
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
                subtitle = "t.me/gg_no_rot",
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
                iconBg = Color(0xFF22111E),
                iconTint = accentColor,
                title = "Bot Oficial",
                subtitle = "@downfree_jk_bot",
                onClick = {
                    onCloseDrawer()
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/downfree_jk_bot")))
                    } catch (_: Exception) {
                        Toast.makeText(context, "Telegram: @downfree_jk_bot", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.Link,
                iconBg = Color(0xFF332612),
                iconTint = StatusAmber,
                title = "Grupo de Soporte",
                subtitle = "Comunidad oficial de soporte",
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
                subtitle = "Diagnóstico y conexiones activas",
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
                subtitle = "JK Downloader · ᴍᴏᴅ ʙʏ ᴊᴀᴋᴜᴅᴏѕʜʏꫂꤪꤨᴼᶠᶜ",
                onClick = {
                    onCloseDrawer()
                    onOpenAbout()
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(color = UdyatDarkCardBorder, thickness = 0.5.dp)

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Settings Item
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
            .padding(vertical = 9.dp, horizontal = 8.dp),
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
                fontSize = 13.sp,
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
