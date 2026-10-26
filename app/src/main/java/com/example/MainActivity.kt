package com.example

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.DownloadEntity
import com.example.ui.DownloadViewModel
import com.example.ui.SortOption
import com.example.ui.components.AboutDialog
import com.example.ui.components.CloudFilesDialog
import com.example.ui.components.DownloadDetailDialog
import com.example.ui.components.LogsDialog
import com.example.ui.components.NewDownloadDialog
import com.example.ui.components.TelegramLinkDialog
import com.example.ui.components.UdyatDrawerContent
import com.example.ui.screens.ActiveDownloadsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AccentSwatches
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextDarkGray
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.theme.UdyatDarkBackground
import com.example.ui.theme.UdyatDarkCardBorder
import com.example.ui.theme.UdyatDarkSurface
import com.example.ui.theme.UdyatDarkSurfaceVariant
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: DownloadViewModel by viewModels()
    private var sharedUrlFromIntent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val accentColor = AccentSwatches.getOrElse(settings.accentColorIndex) { AccentSwatches[0] }

            MyApplicationTheme(accentColor = accentColor) {
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
                    "Activa \"Instalar apps desconocidas\" para JK Downloader y vuelve a tocar Abrir",
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
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val allDownloads by viewModel.allDownloads.collectAsStateWithLifecycle()
    val activeDownloads by viewModel.activeDownloads.collectAsStateWithLifecycle()
    val completedDownloads by viewModel.completedDownloads.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isAddDialogOpen by viewModel.isAddDialogOpen.collectAsStateWithLifecycle()
    val detailItem by viewModel.detailItem.collectAsStateWithLifecycle()

    val queueBannerCount by viewModel.queuedNotificationCount.collectAsStateWithLifecycle()
    val settingsSavedBannerVisible by viewModel.settingsSavedBanner.collectAsStateWithLifecycle()

    // Navigation state
    var currentScreen by remember { mutableStateOf("home") } // "home" or "settings"

    // Search and sort
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(SortOption.NEWEST) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    // Filter tab: 0 = Todas, 1 = Activas, 2 = Completadas
    var selectedFilterTab by remember { mutableIntStateOf(0) }

    // Multi-selection
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }

    // Dialogs
    var isTelegramLinkDialogOpen by remember { mutableStateOf(false) }
    var isCloudFilesDialogOpen by remember { mutableStateOf(false) }
    var isLogsDialogOpen by remember { mutableStateOf(false) }
    var isAboutDialogOpen by remember { mutableStateOf(false) }

    // Clipboard detection
    var detectedClipboardUrl by remember { mutableStateOf<String?>(null) }
    var prefillUrl by remember { mutableStateOf("") }

    val accentColor = MaterialTheme.colorScheme.primary

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

    // Clipboard URL sniffer on app resume
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                try {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = cm.primaryClip
                    if (clip != null && clip.itemCount > 0) {
                        val text = clip.getItemAt(0)?.text?.toString()?.trim()
                        if (!text.isNullOrBlank() && (text.startsWith("http://") || text.startsWith("https://"))) {
                            // Check if not already in active downloads
                            val alreadyExists = allDownloads.any { it.url == text }
                            if (!alreadyExists && text != detectedClipboardUrl) {
                                detectedClipboardUrl = text
                            }
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler(enabled = currentScreen != "home" || isSearchActive || isSelectionMode) {
        when {
            currentScreen != "home" -> currentScreen = "home"
            isSearchActive -> {
                isSearchActive = false
                searchQuery = ""
            }
            isSelectionMode -> {
                isSelectionMode = false
                selectedIds = emptySet()
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            UdyatDrawerContent(
                userProfile = userProfile,
                onOpenProfileLink = { isTelegramLinkDialogOpen = true },
                onOpenCloudFiles = { isCloudFilesDialogOpen = true },
                onOpenLogs = { isLogsDialogOpen = true },
                onOpenAbout = { isAboutDialogOpen = true },
                onNavigateToSettings = { currentScreen = "settings" },
                onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
            )
        }
    ) {
        if (currentScreen == "settings") {
            SettingsScreen(
                settings = settings,
                onSaveSettings = { viewModel.updateSettings(it) },
                onBack = { currentScreen = "home" }
            )
        } else {
            Scaffold(
                containerColor = UdyatDarkBackground,
                topBar = {
                    TopAppBar(
                        title = {
                            if (isSelectionMode) {
                                Text(
                                    text = "Seleccionar archivos",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            } else if (isSearchActive) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = {
                                        Text("Buscar en descargas...", fontSize = 13.sp, color = TextDarkGray)
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
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("search_text_field")
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_jk_brand),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, accentColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "JK",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Downloader",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            if (isSelectionMode) {
                                IconButton(
                                    onClick = {
                                        isSelectionMode = false
                                        selectedIds = emptySet()
                                    },
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(UdyatDarkSurfaceVariant)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = TextWhite, modifier = Modifier.size(18.dp))
                                }
                            } else if (isSearchActive) {
                                IconButton(
                                    onClick = {
                                        isSearchActive = false
                                        searchQuery = ""
                                    },
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(UdyatDarkSurfaceVariant)
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = TextWhite, modifier = Modifier.size(18.dp))
                                }
                            } else {
                                IconButton(
                                    onClick = { coroutineScope.launch { drawerState.open() } },
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(UdyatDarkSurfaceVariant)
                                        .testTag("hamburger_menu_button")
                                ) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menú", tint = TextWhite, modifier = Modifier.size(20.dp))
                                }
                            }
                        },
                        actions = {
                            if (isSelectionMode) {
                                IconButton(
                                    onClick = {
                                        if (selectedIds.size == allDownloads.size) {
                                            selectedIds = emptySet()
                                        } else {
                                            selectedIds = allDownloads.map { it.id }.toSet()
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(UdyatDarkSurfaceVariant)
                                ) {
                                    Icon(
                                        imageVector = if (selectedIds.size == allDownloads.size) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                        contentDescription = "Seleccionar todo",
                                        tint = TextWhite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else if (isSearchActive) {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(UdyatDarkSurfaceVariant)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = TextWhite, modifier = Modifier.size(18.dp))
                                    }
                                }
                            } else {
                                IconButton(
                                    onClick = { isSearchActive = true },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(UdyatDarkSurfaceVariant)
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = "Buscar", tint = TextWhite, modifier = Modifier.size(20.dp))
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Box(modifier = Modifier.padding(end = 8.dp)) {
                                    IconButton(
                                        onClick = { sortMenuExpanded = true },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(UdyatDarkSurfaceVariant)
                                            .testTag("sort_button")
                                    ) {
                                        Icon(Icons.Default.SwapVert, contentDescription = "Ordenar", tint = TextWhite, modifier = Modifier.size(20.dp))
                                    }

                                    // Sort Menu (Screenshot 2)
                                    DropdownMenu(
                                        expanded = sortMenuExpanded,
                                        onDismissRequest = { sortMenuExpanded = false }
                                    ) {
                                        val options = listOf(
                                            SortOption.NEWEST to "Más nuevas",
                                            SortOption.OLDEST to "Más antiguas",
                                            SortOption.NAME_AZ to "Nombre A-Z",
                                            SortOption.NAME_ZA to "Nombre Z-A",
                                            SortOption.SIZE_LARGEST to "Tamaño: mayor",
                                            SortOption.SIZE_SMALLEST to "Tamaño: menor"
                                        )

                                        options.forEach { (opt, label) ->
                                            val isSelected = sortOption == opt
                                            DropdownMenuItem(
                                                text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        if (isSelected) {
                                                            Icon(Icons.Default.Check, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                        }
                                                        Text(
                                                            text = label,
                                                            color = if (isSelected) accentColor else TextWhite,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    sortOption = opt
                                                    sortMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = UdyatDarkBackground)
                    )
                },
                floatingActionButton = {
                    // Floating Action Button + (Screenshots 1, 2, 3, 5, 8, 12, 13)
                    FloatingActionButton(
                        onClick = {
                            prefillUrl = ""
                            viewModel.openAddDialog()
                        },
                        containerColor = accentColor,
                        contentColor = TextWhite,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("fab_add_download")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nueva descarga",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            ) { paddingValues ->
                ActiveDownloadsScreen(
                    downloads = allDownloads,
                    activeCount = activeDownloads.size,
                    completedCount = completedDownloads.size,
                    selectedFilter = selectedFilterTab,
                    onSelectFilter = { selectedFilterTab = it },
                    searchQuery = searchQuery,
                    sortOption = sortOption,
                    clipboardDetectedUrl = detectedClipboardUrl,
                    onAddClipboardUrl = { url ->
                        prefillUrl = url
                        detectedClipboardUrl = null
                        viewModel.openAddDialog()
                    },
                    onDismissClipboardBanner = { detectedClipboardUrl = null },
                    queueBannerCount = queueBannerCount,
                    onDismissQueueBanner = { viewModel.dismissQueuedBanner() },
                    settingsSavedBannerVisible = settingsSavedBannerVisible,
                    onDismissSettingsSavedBanner = { viewModel.dismissSettingsSavedBanner() },
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
                    onCopyUrl = { viewModel.copyUrl(context, it) },
                    onAddNew = {
                        prefillUrl = ""
                        viewModel.openAddDialog()
                    },
                    isSelectionMode = isSelectionMode,
                    selectedIds = selectedIds,
                    onToggleSelect = { id ->
                        selectedIds = if (selectedIds.contains(id)) selectedIds - id else selectedIds + id
                    },
                    onEnterSelectionMode = { isSelectionMode = true },
                    onBatchDelete = {
                        viewModel.batchDelete(selectedIds)
                        selectedIds = emptySet()
                        isSelectionMode = false
                    },
                    onExitSelectionMode = {
                        isSelectionMode = false
                        selectedIds = emptySet()
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }

    // Dialog: Añadir Descarga (Screenshot 9)
    if (isAddDialogOpen) {
        NewDownloadDialog(
            initialUrl = prefillUrl,
            defaultThreads = settings.maxThreads,
            onDismiss = { viewModel.closeAddDialog() },
            onConfirm = { url, threads, name ->
                viewModel.addDownload(url, threads, name)
                viewModel.closeAddDialog()
            }
        )
    }

    // Dialog: Telegram Linking
    if (isTelegramLinkDialogOpen) {
        TelegramLinkDialog(
            userProfile = userProfile,
            onLink = { fullName, telegramId, balance, quotaPlan ->
                viewModel.linkTelegram(fullName, telegramId, balance, quotaPlan)
            },
            onUnlink = { viewModel.unlinkTelegram() },
            onDismiss = { isTelegramLinkDialogOpen = false }
        )
    }

    // Dialog: Archivos en la Nube
    if (isCloudFilesDialogOpen) {
        CloudFilesDialog(
            completedList = completedDownloads,
            onOpen = {
                if (it.isApk) onOpenApkRequested()
                viewModel.openFile(context, it)
            },
            onShare = { viewModel.shareFile(context, it) },
            onDismiss = { isCloudFilesDialogOpen = false }
        )
    }

    // Dialog: Registros de la App (Logs)
    if (isLogsDialogOpen) {
        LogsDialog(onDismiss = { isLogsDialogOpen = false })
    }

    // Dialog: Acerca de (Screenshot 6 with ACEPTAR only and mod credit)
    if (isAboutDialogOpen) {
        AboutDialog(onDismiss = { isAboutDialogOpen = false })
    }

    // Dialog: Detalle de archivo
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
