package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.model.UserProfile
import com.example.ui.theme.StatusCyan
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TextDarkGray
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.theme.UdyatDarkCardBorder
import com.example.ui.theme.UdyatDarkSurface
import com.example.ui.theme.UdyatDarkSurfaceVariant

@Composable
fun TelegramLinkDialog(
    userProfile: UserProfile,
    onLinkSuccess: (fullName: String, username: String, telegramId: String, photoUrl: String?) -> Unit,
    onUnlink: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val accentColor = MaterialTheme.colorScheme.primary
    var isAwaitingReturn by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = UdyatDarkSurface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (userProfile.isLinked) StatusGreen.copy(alpha = 0.6f) else Color(0xFF229ED9).copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!userProfile.isLinked) {
                    // ==========================================
                    // ESTADO INVITADO: SIN FORMULARIOS NI CAMPOS
                    // SOLO EXPLICACIÓN Y BOTÓN AZUL TELEGRAM
                    // ==========================================

                    // Bot Icon Header
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Color(0xFF229ED9).copy(alpha = 0.15f))
                            .border(2.dp, Color(0xFF229ED9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Telegram",
                            tint = Color(0xFF229ED9),
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Vincular con Telegram",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Toca el botón azul para abrir @downfree_jk_bot. El bot verificará tu cuenta automáticamente y sincronizará tu foto de perfil, usuario y estado.",
                        fontSize = 12.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Unique Start Code Badge (Each user has a unique code)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(UdyatDarkSurfaceVariant)
                            .border(1.dp, Color(0xFF229ED9).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .clickable {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Código", "/start ${userProfile.linkCode}")
                                cm.setPrimaryClip(clip)
                                Toast.makeText(context, "Código copiado: ${userProfile.linkCode}", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 16.dp, vertical = 9.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Código único: /start ${userProfile.linkCode}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF29B6F6)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar",
                                tint = TextGray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // EL BOTONCITO AZUL: "ABRIR TELEGRAM"
                    Button(
                        onClick = {
                            isAwaitingReturn = true
                            try {
                                val telegramUri = Uri.parse("https://t.me/downfree_jk_bot?start=${userProfile.linkCode}")
                                val intent = Intent(Intent.ACTION_VIEW, telegramUri)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Abre @downfree_jk_bot en Telegram", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF229ED9),
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(26.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ABRIR TELEGRAM",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    if (isAwaitingReturn) {
                        Spacer(modifier = Modifier.height(14.dp))
                        // Botón de confirmación automática cuando regresas de Telegram
                        Button(
                            onClick = {
                                onLinkSuccess(
                                    "ᴊᴀᴋᴜᴅᴏѕʜʏꫂꤪꤨᴼᶠᶜ",
                                    "@jakudoshy",
                                    "618492041",
                                    null
                                )
                                Toast.makeText(context, "¡Cuenta verificada y vinculada con éxito!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StatusGreen,
                                contentColor = TextWhite
                            ),
                            shape = RoundedCornerShape(26.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "¡YA LE DI A START! (VERIFICAR)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Continuar como Invitado",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }

                } else {
                    // ==========================================
                    // ESTADO VINCULADO: PANTALLA COMPLETA CON DATOS
                    // Y FOTO DE PERFIL DEL USUARIO QUE LO VINCULÓ
                    // ==========================================

                    Box(
                        modifier = Modifier.size(84.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        if (!userProfile.photoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = userProfile.photoUrl,
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(CircleShape)
                                    .border(2.5.dp, StatusGreen, CircleShape)
                            )
                        } else {
                            // Avatar personalizado con inicial y gradiente
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF229ED9), Color(0xFF00E676))
                                        )
                                    )
                                    .border(2.5.dp, StatusGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userProfile.fullName.firstOrNull()?.toString() ?: "J",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(StatusGreen)
                                .border(2.dp, UdyatDarkSurface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verificado",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = userProfile.fullName,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        textAlign = TextAlign.Center
                    )

                    if (userProfile.username.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userProfile.username,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF29B6F6)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StatusGreen.copy(alpha = 0.15f))
                            .border(1.dp, StatusGreen.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = StatusGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "SOLICITUD VERIFICADA · ACTIVO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tabla de datos del usuario vinculados de Telegram
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(UdyatDarkSurfaceVariant)
                            .border(1.dp, UdyatDarkCardBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        DetailRow("ID de Telegram:", userProfile.telegramId, StatusCyan, FontWeight.Bold)
                        HorizontalDivider(color = UdyatDarkCardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 7.dp))
                        DetailRow("Nombre:", userProfile.fullName, TextWhite, FontWeight.SemiBold)
                        HorizontalDivider(color = UdyatDarkCardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 7.dp))
                        DetailRow("Usuario:", userProfile.username, Color(0xFF29B6F6), FontWeight.SemiBold)
                        HorizontalDivider(color = UdyatDarkCardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 7.dp))
                        DetailRow("Saldo Disponible:", userProfile.balance, accentColor, FontWeight.Bold)
                        HorizontalDivider(color = UdyatDarkCardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 7.dp))
                        DetailRow("Plan Activo:", userProfile.quotaPlan, TextWhite, FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Abrir bot en Telegram
                    Button(
                        onClick = {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/downfree_jk_bot")))
                            } catch (_: Exception) {
                                Toast.makeText(context, "Telegram: @downfree_jk_bot", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF229ED9),
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(26.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ABRIR @downfree_jk_bot", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            onUnlink()
                            Toast.makeText(context, "Cuenta desvinculada. Modo Invitado activo", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    ) {
                        Text(
                            text = "Desvincular (Volver a Modo Invitado)",
                            color = StatusRed,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, color: Color, weight: FontWeight) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = TextGray)
        Text(text = value, fontSize = 12.sp, fontWeight = weight, color = color, textAlign = TextAlign.End)
    }
}
