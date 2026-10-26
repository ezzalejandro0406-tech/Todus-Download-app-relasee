package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextDarkGray
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.theme.UdyatDarkCardBorder
import com.example.ui.theme.UdyatDarkSurface
import com.example.ui.theme.UdyatDarkSurfaceVariant

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val accentColor = MaterialTheme.colorScheme.primary

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = UdyatDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, UdyatDarkCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Official Brand Circular Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_jk_brand),
                    contentDescription = "JK Downloader Logo Oficial",
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .border(2.dp, accentColor, CircleShape)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "JK Downloader",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Version Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.2f))
                        .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "v1.1 (Engine Turbo 2026)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Gestor de descargas de alta velocidad para toDus y enlaces directos.\nCompatible con Android, Windows, Linux y macOS.",
                    fontSize = 12.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Mod author
                Text(
                    text = "ᴍᴏᴅ ʙʏ ᴊᴀᴋᴜᴅᴏѕʜʏꫂꤪꤨᴼᶠᶜ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Telegram Links Row
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(UdyatDarkSurfaceVariant)
                        .clickable {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/gg_no_rot")))
                            } catch (_: Exception) {
                                Toast.makeText(context, "Telegram: t.me/gg_no_rot", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = StatusGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Canal: t.me/gg_no_rot",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ONLY ACEPTAR button as requested by the user
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text(
                        text = "ACEPTAR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
