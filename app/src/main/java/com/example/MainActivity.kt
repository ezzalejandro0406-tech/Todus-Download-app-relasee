package com.example

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.DownloadEntity
import com.example.ui.DownloadViewModel
import com.example.ui.components.DownloadDetailDialog
import com.example.ui.components.NewDownloadDialog
import com.example.ui.screens.ActiveDownloadsScreen
import com.example.ui.screens.CompletedDownloadsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentRedContainer
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextDarkGray
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite

class MainActivity : ComponentActivity() {
    private val viewModel: DownloadViewModel by viewModels()
    private var sharedUrlFromIntent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            MyApplicationTheme {
                MainAppScreen(
                    viewModel = viewModel,
                    initialSharedUrl = sharedUrlFromIntent,
                    onClearSharedUrl = { sharedUrlFromIntent = null },
                    onOpenApkRequested = { checkUnknownAppSourcesPermission() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!text.isNullOrBlank()) {
                val extracted = extractUrl(text)
                sharedUrlFromIntent = extracted
            }
        }
    }

    private fun extractUrl(text: String): String {
        val parts = text.split("\\s+".toRegex())
        return parts.firstOrNull { it.startsWith("http://") || it.startsWith("https://") } ?: text
    }

    private fun checkUnknownAppSourcesPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                Toast.makeText(
                    this,
                    "Activa \"Instalar apps desconocidas\" para toDus Download y vuelve a tocar Abrir",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
                return false
            }
        }
        return true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: DownloadViewModel,
    initialSharedUrl: String?,
    onClearSharedUrl: () -> Unit,
    onOpenApkRequested: () -> Boolean
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val activeDownloads by viewModel.activeDownloads.collectAsStateWithLifecycle()
    val completedDownloads by viewModel.completedDownloads.collectAsStateWithLifecycle()
    val totalSpeedBps by viewModel.totalSpeedBps.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isAddDialogOpen by viewModel.isAddDialogOpen.collectAsStateWithLifecycle()
    val detailItem by viewModel.detailItem.collectAsStateWithLifecycle()

    var prefillUrl by remember { mutableStateOf("") }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(initialSharedUrl) {
        if (!initialSharedUrl.isNullOrBlank()) {
            prefillUrl = initialSharedUrl
            viewModel.openAddDialog()
            onClearSharedUrl()
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentRedContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = AccentRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "toDus",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Download",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentRed
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            prefillUrl = ""
                            viewModel.openAddDialog()
                        },
                        modifier = Modifier.testTag("top_add_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir descarga",
                            tint = AccentRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = TextWhite
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (activeDownloads.isNotEmpty()) {
                                    Badge(
                                        containerColor = AccentRed,
                                        contentColor = TextWhite
                                    ) {
                                        Text("${activeDownloads.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Descargas")
                        }
                    },
                    label = { Text("Descargas", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TextWhite,
                        selectedTextColor = AccentRed,
                        indicatorColor = AccentRed,
                        unselectedIconColor = TextDarkGray,
                        unselectedTextColor = TextDarkGray
                    ),
                    modifier = Modifier.testTag("tab_active")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (completedDownloads.isNotEmpty()) {
                                    Badge(
                                        containerColor = DarkCardBorder,
                                        contentColor = TextWhite
                                    ) {
                                        Text("${completedDownloads.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.History, contentDescription = "Historial")
                        }
                    },
                    label = { Text("Historial", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TextWhite,
                        selectedTextColor = AccentRed,
                        indicatorColor = AccentRed,
                        unselectedIconColor = TextDarkGray,
                        unselectedTextColor = TextDarkGray
                    ),
                    modifier = Modifier.testTag("tab_completed")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes")
                    },
                    label = { Text("Ajustes", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TextWhite,
                        selectedTextColor = AccentRed,
                        indicatorColor = AccentRed,
                        unselectedIconColor = TextDarkGray,
                        unselectedTextColor = TextDarkGray
                    ),
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        prefillUrl = ""
                        viewModel.openAddDialog()
                    },
                    containerColor = AccentRed,
                    contentColor = TextWhite,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_add_download")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nueva descarga",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> ActiveDownloadsScreen(
                    activeDownloads = activeDownloads,
                    totalSpeedBps = totalSpeedBps,
                    onPause = { viewModel.pauseDownload(it) },
                    onResume = { viewModel.resumeDownload(it) },
                    onCancel = { viewModel.cancelDownload(it) },
                    onRetry = { viewModel.retryDownload(it) },
                    onDelete = { viewModel.deleteDownload(it, true) },
                    onOpen = {
                        if (it.isApk) onOpenApkRequested()
                        viewModel.openFile(context, it)
                    },
                    onShare = { viewModel.shareFile(context, it) },
                    onItemClick = { viewModel.showDetail(it) },
                    onPauseAll = { viewModel.pauseAll() },
                    onResumeAll = { viewModel.resumeAll() },
                    onAddNew = {
                        prefillUrl = ""
                        viewModel.openAddDialog()
                    }
                )
                1 -> CompletedDownloadsScreen(
                    completedDownloads = completedDownloads,
                    onOpen = {
                        if (it.isApk) onOpenApkRequested()
                        viewModel.openFile(context, it)
                    },
                    onShare = { viewModel.shareFile(context, it) },
                    onDelete = { viewModel.deleteDownload(it, true) },
                    onItemClick = { viewModel.showDetail(it) },
                    onClearAll = { viewModel.clearCompleted() }
                )
                2 -> SettingsScreen(
                    settings = settings,
                    onSaveSettings = { viewModel.updateSettings(it) }
                )
            }
        }
    }

    if (isAddDialogOpen) {
        NewDownloadDialog(
            initialUrl = prefillUrl,
            defaultThreads = settings.maxThreads,
            onDismiss = { viewModel.closeAddDialog() },
            onConfirm = { url, threads ->
                viewModel.addDownload(url, threads)
                viewModel.closeAddDialog()
                selectedTab = 0
            }
        )
    }

    detailItem?.let { item ->
        DownloadDetailDialog(
            item = item,
            onDismiss = { viewModel.closeDetail() },
            onOpen = {
                if (it.isApk) onOpenApkRequested()
                viewModel.openFile(context, it)
            },
            onShare = { viewModel.shareFile(context, it) }
        )
    }
}
