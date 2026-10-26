package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
    onLink: (fullName: String, telegramId: String, balance: String, quotaPlan: String) -> Unit,
    onUnlink: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val accentColor = MaterialTheme.colorScheme.primary

    var customName by remember {
        mutableStateOf(if (userProfile.isLinked) userProfile.fullName else "ᴊᴀᴋᴜᴅᴏѕʜʏꫂꤪꤨᴼᶠᶜ")
    }
    var customTelegramId by remember {
        mutableStateOf(if (userProfile.isLinked) userProfile.telegramId.removePrefix("ID: ").trim() else "618492041")
    }
    var customBalance by remember {
        mutableStateOf(if (userProfile.isLinked) userProfile.balance else "$ 150.00 CUP")
    }
    var customQuota by remember {
        mutableStateOf(if (userProfile.isLinked) userProfile.quotaPlan else "Plan Premium Ultra (Sin Límite)")
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = UdyatDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, if (userProfile.isLinked) StatusGreen.copy(alpha = 0.5f) else accentColor.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Official Brand Avatar with Verified Badge
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_jk_brand),
                        contentDescription = "Logo JK Downloader",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(2.dp, if (userProfile.isLinked) StatusGreen else accentColor, CircleShape)
                    )
                    if (userProfile.isLinked) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(StatusGreen)
                                .border(1.5.dp, UdyatDarkSurface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verificado",
                                tint = Color.Black,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (userProfile.isLinked) "Perfil Verificado JK" else "Vincular Cuenta de Telegram",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (userProfile.isLinked) {
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
                                text = "VERIFICADO VIP · ACTIVO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Verified user data table
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(UdyatDarkSurfaceVariant)
                            .border(1.dp, UdyatDarkCardBorder, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        ProfileDataRow("Nombre Completo:", userProfile.fullName, TextWhite, FontWeight.Bold)
                        HorizontalDivider(color = UdyatDarkCardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ProfileDataRow("ID de Telegram:", userProfile.telegramId, StatusCyan, FontWeight.SemiBold)
                        HorizontalDivider(color = UdyatDarkCardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ProfileDataRow("Estado de Cuenta:", userProfile.accountStatus, StatusGreen, FontWeight.SemiBold)
                        HorizontalDivider(color = UdyatDarkCardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ProfileDataRow("Saldo Disponible:", userProfile.balance, accentColor, FontWeight.Bold)
                        HorizontalDivider(color = UdyatDarkCardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ProfileDataRow("Plan de Descargas:", userProfile.quotaPlan, TextWhite, FontWeight.Medium)
                    }
                } else {
                    Text(
                        text = "Conéctate al bot oficial @downfree_jk_bot para sincronizar tu cuenta, datos verificados y tu historial de descargas.",
                        fontSize = 12.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Link Code Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(UdyatDarkSurfaceVariant)
                            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Código", "/start ${userProfile.linkCode}")
                                cm.setPrimaryClip(clip)
                                Toast.makeText(context, "Código copiado: /start ${userProfile.linkCode}", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "/start ${userProfile.linkCode}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar",
                                tint = TextGray,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Inputs for verified data
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Nombre Completo", fontSize = 11.sp, color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = UdyatDarkCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = UdyatDarkSurfaceVariant,
                            unfocusedContainerColor = UdyatDarkSurfaceVariant
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customTelegramId,
                        onValueChange = { customTelegramId = it },
                        label = { Text("ID de Telegram", fontSize = 11.sp, color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = UdyatDarkCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = UdyatDarkSurfaceVariant,
                            unfocusedContainerColor = UdyatDarkSurfaceVariant
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Button: Open @downfree_jk_bot
                Button(
                    onClick = {
                        try {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://t.me/downfree_jk_bot?start=${userProfile.linkCode}")
                            )
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Telegram: @downfree_jk_bot", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF229ED9),
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ABRIR @downfree_jk_bot",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Link / Confirm button
                Button(
                    onClick = {
                        onLink(
                            customName.ifBlank { "ᴊᴀᴋᴜᴅᴏѕʜʏꫂꤪꤨᴼᶠᶜ" },
                            customTelegramId.ifBlank { "618492041" },
                            customBalance,
                            customQuota
                        )
                        Toast.makeText(context, "¡Datos verificados y cuenta vinculada!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (userProfile.isLinked) accentColor else StatusGreen,
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = if (userProfile.isLinked) "ACTUALIZAR DATOS" else "VINCULAR Y VERIFICAR DATOS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                if (userProfile.isLinked) {
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(
                        onClick = {
                            onUnlink()
                            Toast.makeText(context, "Modo Invitado reactivado", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    ) {
                        Text(
                            text = "Desvincular (Volver a Modo Invitado)",
                            color = StatusRed,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Continuar como Invitado",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileDataRow(label: String, value: String, valueColor: Color, fontWeight: FontWeight) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            fontWeight = fontWeight,
            color = valueColor,
            textAlign = TextAlign.End
        )
    }
}
