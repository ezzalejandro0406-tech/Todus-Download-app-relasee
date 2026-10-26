package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DoneAll
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
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonRedBright
import com.example.ui.theme.NeonRedCardBorder
import com.example.ui.theme.NeonRedContainer
import com.example.ui.theme.NeonRedDarkBackground
import com.example.ui.theme.NeonRedDarkSurface
import com.example.ui.theme.NeonRedPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

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
                    onClearSharedUrl = { sharedUrlFromIntent = null }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: DownloadViewModel,
    initialSharedUrl: String?,
    onClearSharedUrl: () -> Unit
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

    // Request Notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Auto-open add dialog when URL is shared via SEND intent
    LaunchedEffect(initialSharedUrl) {
        if (!initialSharedUrl.isNullOrBlank()) {
            prefillUrl = initialSharedUrl
            viewModel.openAddDialog()
            onClearSharedUrl()
        }
    }

    Scaffold(
        containerColor = NeonRedDarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonRedContainer)
                                .border(1.dp, NeonRedPrimary, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = NeonRedBright,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "toDus",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Download",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonRedBright
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NeonRedPrimary)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "TURBO",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextPrimary
                                    )
                                }
                            }
                            Text(
                                text = "Multihilo & Reconexión automática",
                                fontSize = 11.sp,
                                color = TextMuted
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
                            tint = NeonRedBright
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeonRedDarkBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = NeonRedDarkSurface,
                contentColor = TextPrimary
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (activeDownloads.isNotEmpty()) {
                                    Badge(
                                        containerColor = NeonRedPrimary,
                                        contentColor = TextPrimary
                                    ) {
                                        Text("${activeDownloads.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = "Activas")
                        }
                    },
                    label = { Text("Activas", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TextPrimary,
                        selectedTextColor = NeonRedBright,
                        indicatorColor = NeonRedPrimary,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
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
                                        containerColor = NeonRedContainer,
                                        contentColor = NeonRedBright
                                    ) {
                                        Text("${completedDownloads.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Finalizadas")
                        }
                    },
                    label = { Text("Finalizadas", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TextPrimary,
                        selectedTextColor = NeonRedBright,
                        indicatorColor = NeonRedPrimary,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("tab_completed")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes")
                    },
                    label = { Text("Ajustes", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TextPrimary,
                        selectedTextColor = NeonRedBright,
                        indicatorColor = NeonRedPrimary,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
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
                    containerColor = NeonRedPrimary,
                    contentColor = TextPrimary,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_add_download")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nueva descarga",
                        modifier = Modifier.size(26.dp)
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
                    onOpen = { viewModel.openFile(context, it) },
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
                    onOpen = { viewModel.openFile(context, it) },
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

    // Add New Download Dialog
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

    // Download Detail Dialog
    detailItem?.let { item ->
        DownloadDetailDialog(
            item = item,
            onDismiss = { viewModel.closeDetail() },
            onOpen = { viewModel.openFile(context, it) },
            onShare = { viewModel.shareFile(context, it) }
        )
    }
}
