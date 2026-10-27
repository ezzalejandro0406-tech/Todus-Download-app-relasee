package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.DownloadEntity
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextDarkGray
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.theme.UdyatDarkCardBorder
import com.example.ui.theme.UdyatDarkSurface
import com.example.ui.theme.UdyatDarkSurfaceVariant
import org.json.JSONObject

@Composable
fun NewDownloadDialog(
    initialUrl: String = "",
    defaultThreads: Int = 4,
    onDismiss: () -> Unit,
    onConfirm: (urlOrJson: String, threads: Int, customName: String?) -> Unit
) {
    var urlText by remember { mutableStateOf(initialUrl) }
    var customName by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = URL / Texto, 1 = Archivo (.txt/.udyat)

    // File import state
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var fileContentPreview by remember { mutableStateOf("") }
    var importedFileName by remember { mutableStateOf("") }
    var detectedChunksCount by remember { mutableIntStateOf(0) }
    var detectedFileSize by remember { mutableStateOf<Long?>(null) }

    val context = LocalContext.current
    val accentColor = MaterialTheme.colorScheme.primary

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val content = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                fileContentPreview = content

                // Check if .udyat JSON
                if (content.trim().startsWith("{") && (content.contains("\"chunks\"") || content.contains("\"c\""))) {
                    val obj = JSONObject(content.trim())
                    val name = obj.optString("name", obj.optString("filename", "descarga.bin"))
                    val size = obj.optLong("size", obj.optLong("totalSize", 0L))
                    val chunksArr = obj.optJSONArray("chunks") ?: obj.optJSONArray("c")
                    importedFileName = name
                    detectedFileSize = if (size > 0) size else null
                    detectedChunksCount = chunksArr?.length() ?: 0
                } else {
                    val lines = content.lines().filter { it.isNotBlank() && !it.startsWith("#") }
                    importedFileName = "Lista de ${lines.size} enlaces"
                    detectedChunksCount = lines.size
                    detectedFileSize = null
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error leyendo archivo: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
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
                // Header (Screenshots 6, 7)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF142B42)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = Color(0xFF29B6F6),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Añadir Descarga",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Selector (Screenshots 6, 7)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(UdyatDarkSurfaceVariant)
                        .padding(3.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedTab == 0) accentColor.copy(alpha = 0.25f) else Color.Transparent)
                                .border(
                                    width = if (selectedTab == 0) 1.dp else 0.dp,
                                    color = if (selectedTab == 0) accentColor else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedTab = 0 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "URL / Texto",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) TextWhite else TextGray
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedTab == 1) accentColor.copy(alpha = 0.25f) else Color.Transparent)
                                .border(
                                    width = if (selectedTab == 1) 1.dp else 0.dp,
                                    color = if (selectedTab == 1) accentColor else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedTab = 1 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Archivo (.txt/.udyat)",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) TextWhite else TextGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // TAB 0: URL / TEXTO (Screenshot 6)
                    Text(
                        text = "PEGA LA URL O ENLACES (UNO POR LÍNEA)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = urlText,
                        onValueChange = { urlText = it },
                        placeholder = {
                            Text(
                                text = "https://ejemplo.com/archivo1.zip\nhttps://s3.todus.cu/...\no contenido JSON .udyat",
                                fontSize = 12.sp,
                                color = TextDarkGray
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = UdyatDarkCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            cursorColor = accentColor,
                            focusedContainerColor = UdyatDarkSurfaceVariant,
                            unfocusedContainerColor = UdyatDarkSurfaceVariant
                        ),
                        minLines = 4,
                        maxLines = 6,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("url_input_field")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "NOMBRE PERSONALIZADO (OPCIONAL)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        placeholder = {
                            Text(
                                text = "ejemplo.zip",
                                fontSize = 12.sp,
                                color = TextDarkGray
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = TextDarkGray,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = UdyatDarkCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            cursorColor = accentColor,
                            focusedContainerColor = UdyatDarkSurfaceVariant,
                            unfocusedContainerColor = UdyatDarkSurfaceVariant
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_name_input")
                    )
                } else {
                    // TAB 1: ARCHIVO (.txt/.udyat) (Screenshot 7)
                    Text(
                        text = "SELECCIONAR ARCHIVO DE ENLACES O FRAGMENTOS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextWhite
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedFileUri == null) "ELEGIR ARCHIVO .TXT O .UDYAT" else "CAMBIAR ARCHIVO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (selectedFileUri != null && fileContentPreview.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = UdyatDarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, UdyatDarkCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = StatusGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = importedFileName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                if (detectedFileSize != null) {
                                    Text(
                                        text = "Tamaño: ${DownloadEntity.formatBytes(detectedFileSize!!)}",
                                        fontSize = 11.sp,
                                        color = TextGray
                                    )
                                }
                                Text(
                                    text = "Fragmentos / Enlaces: $detectedChunksCount",
                                    fontSize = 11.sp,
                                    color = StatusGreen
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons: CANCELAR & AÑADIR A COLA (Screenshots 6, 7)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UdyatDarkSurfaceVariant,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(
                            text = "CANCELAR",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextWhite
                        )
                    }

                    val canConfirm = if (selectedTab == 0) urlText.isNotBlank() else fileContentPreview.isNotBlank()

                    Button(
                        onClick = {
                            if (selectedTab == 0 && urlText.isNotBlank()) {
                                onConfirm(
                                    urlText.trim(),
                                    defaultThreads,
                                    customName.trim().ifBlank { null }
                                )
                            } else if (selectedTab == 1 && fileContentPreview.isNotBlank()) {
                                onConfirm(
                                    fileContentPreview.trim(),
                                    defaultThreads,
                                    importedFileName.ifBlank { null }
                                )
                            }
                        },
                        enabled = canConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp)
                            .testTag("confirm_download_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AÑADIR A COLA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
