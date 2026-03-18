package com.guardian.app.ui.screens

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import com.guardian.app.data.api.VirusTotalResult
import com.guardian.app.ui.theme.*
import com.guardian.app.viewmodel.GuardianViewModel
import kotlinx.coroutines.launch

// Data class for app scan result
data class AppScanResult(
    val packageName: String,
    val appName: String,
    val isThreat: Boolean,
    val threatType: String,
    val isSystemApp: Boolean,
    val virusTotalResult: VirusTotalResult? = null
)

@Composable
fun ScanScreen(viewModel: GuardianViewModel) {
    val context = LocalContext.current
    val stats by viewModel.stats.collectAsState()
    val blacklist by viewModel.blacklist.collectAsState()
    val isVirusTotalScanning by viewModel.isVirusTotalScanning.collectAsState()
    val virusTotalProgress by viewModel.virusTotalProgress.collectAsState()
    val virusTotalResults by viewModel.virusTotalResults.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    
    // Use theme-aware colors
    val backgroundColor = if (isDarkTheme) GuardianBackground else GuardianBackgroundLight
    val surfaceColor = if (isDarkTheme) GuardianSurface else GuardianSurfaceLight
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1E293B)
    val grayText = if (isDarkTheme) Color.Gray else Color(0xFF64748B)
    
    var scanResults by remember { mutableStateOf<List<AppScanResult>>(emptyList()) }
    var showVirusTotalDialog by remember { mutableStateOf(false) }
    var showVirusTotalAppPicker by remember { mutableStateOf(false) }
    var showApkDialog by remember { mutableStateOf(false) }
    var apkResult by remember { mutableStateOf<String?>(null) }
    var showAllApps by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Сканер",
            style = MaterialTheme.typography.headlineLarge,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Проверка установленных приложений",
            style = MaterialTheme.typography.bodyMedium,
            color = grayText
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Scan Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Full VirusTotal Scan Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clickable(enabled = !isVirusTotalScanning) {
                        showVirusTotalDialog = true
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GuardianSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(GuardianBlue.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isVirusTotalScanning) Icons.Default.Sync else Icons.Default.BugReport,
                            contentDescription = "VirusTotal",
                            modifier = Modifier
                                .size(28.dp)
                                .then(if (isVirusTotalScanning) Modifier.rotate(rotation) else Modifier),
                            tint = if (isVirusTotalScanning) GuardianYellow else GuardianBlue
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Полное сканирование VirusTotal",
                            style = MaterialTheme.typography.titleMedium,
                            color = textColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Проверить все установленные приложения",
                            style = MaterialTheme.typography.bodySmall,
                            color = grayText
                        )
                    }
                }
            }
            
            // Scan Specific App Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clickable(enabled = !isVirusTotalScanning) {
                        showVirusTotalAppPicker = true
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GuardianSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(GuardianGreen.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Проверить приложение",
                            modifier = Modifier.size(28.dp),
                            tint = GuardianGreen
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Проверить приложение",
                            style = MaterialTheme.typography.titleMedium,
                            color = textColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Выбрать конкретное приложение для сканирования",
                            style = MaterialTheme.typography.bodySmall,
                            color = grayText
                        )
                    }
                }
            }
            
            // APK Scan Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clickable(enabled = !isVirusTotalScanning) {
                        showApkDialog = true
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GuardianSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(GuardianYellow.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = "Сканировать APK",
                            modifier = Modifier.size(28.dp),
                            tint = GuardianYellow
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Сканировать APK файл",
                            style = MaterialTheme.typography.titleMedium,
                            color = textColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Проверить загруженный APK файл",
                            style = MaterialTheme.typography.bodySmall,
                            color = grayText
                        )
                    }
                }
            }
        }
        
        // Scan Status
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when {
                isVirusTotalScanning -> "VirusTotal: ${virusTotalProgress.first}/${virusTotalProgress.second}"
                else -> "Выберите тип сканирования"
            },
            style = MaterialTheme.typography.titleMedium,
            color = when {
                isVirusTotalScanning -> GuardianYellow
                else -> grayText
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Stats Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.CheckCircle,
                iconTint = GuardianGreen,
                title = "Проверено",
                value = stats.appsScanned.toString()
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Block,
                iconTint = GuardianRed,
                title = "Угрозы",
                value = stats.threatsBlocked.toString()
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Warning,
                iconTint = GuardianYellow,
                title = "В списке",
                value = blacklist.size.toString()
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Toggle for showing all apps or only threats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (showAllApps) "Все приложения" else "Обнаруженные угрозы",
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
            
            TextButton(onClick = { showAllApps = !showAllApps }) {
                Text(
                    text = if (showAllApps) "Только угрозы" else "Показать все",
                    color = GuardianBlue
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Apps List
        val displayedApps = if (showAllApps) {
            scanResults
        } else {
            scanResults.filter { it.isThreat }
        }
        
        if (displayedApps.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(displayedApps) { app ->
                    AppListItem(
                        app = app,
                        isBlacklisted = blacklist.any { it.packageName == app.packageName },
                        onAddToBlacklist = { 
                            viewModel.addToBlacklist(app.appName, app.packageName)
                        },
                        onRemoveFromBlacklist = {
                            val blacklistedApp = blacklist.find { it.packageName == app.packageName }
                            blacklistedApp?.let { viewModel.removeFromBlacklist(it.id) }
                        },
                        onUninstall = {
                            val intent = viewModel.getUninstallIntent(app.packageName)
                            context.startActivity(intent)
                        }
                    )
                }
            }
        } else if (!isVirusTotalScanning && scanResults.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = GuardianGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (showAllApps) "Нет приложений" else "Угроз не обнаружено",
                        style = MaterialTheme.typography.titleMedium,
                        color = GuardianGreen
                    )
                    if (stats.lastScanTime > 0) {
                        Text(
                            text = "Последнее сканирование: ${formatTime(stats.lastScanTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = grayText
                        )
                    }
                }
            }
        } else if (!isVirusTotalScanning && scanResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = grayText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Нажмите на кнопку для сканирования",
                        style = MaterialTheme.typography.bodyMedium,
                        color = grayText
                    )
                }
            }
        }
    }
    
    // VirusTotal Dialog
    if (showVirusTotalDialog) {
        AlertDialog(
            onDismissRequest = { showVirusTotalDialog = false },
            containerColor = surfaceColor,
            title = { 
                Text("VirusTotal", color = textColor, fontWeight = FontWeight.Bold) 
            },
            text = {
                Column {
                    Text(
                        text = "Сканирование через базу VirusTotal использует облачную проверку файлов по SHA-256 хэшу.",
                        color = grayText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "⚠️ Лимит бесплатного API: 4 запроса в минуту. Полное сканирование может занять много времени.",
                        color = GuardianYellow,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (!viewModel.isVirusTotalApiKeyConfigured()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠️ API ключ не настроен. Добавьте его в Настройках.",
                            color = GuardianRed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showVirusTotalDialog = false
                        viewModel.startVirusTotalScan()
                    }
                ) {
                    Text("Запустить", color = GuardianBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showVirusTotalDialog = false }) {
                    Text("Отмена", color = grayText)
                }
            }
        )
    }
    
    // VirusTotal App Picker Dialog
    if (showVirusTotalAppPicker) {
        val pm = context.packageManager
        val allApps = remember {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
                .sortedBy { pm.getApplicationLabel(it).toString() }
        }
        var searchQuery by remember { mutableStateOf("") }
        val filteredApps = allApps.filter {
            pm.getApplicationLabel(it).toString().lowercase().contains(searchQuery.lowercase()) ||
            it.packageName.lowercase().contains(searchQuery.lowercase())
        }
        
        AlertDialog(
            onDismissRequest = { showVirusTotalAppPicker = false },
            containerColor = surfaceColor,
            title = { 
                Text("Выберите приложение", color = textColor, fontWeight = FontWeight.Bold) 
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Поиск...", color = grayText) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedPlaceholderColor = grayText,
                            unfocusedPlaceholderColor = grayText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.height(300.dp)
                    ) {
                        items(filteredApps) { app ->
                            val appName = pm.getApplicationLabel(app).toString()
                            val packageName = app.packageName
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        showVirusTotalAppPicker = false
                                        viewModel.scanSingleAppWithVirusTotal(packageName, appName)
                                    },
                                colors = CardDefaults.cardColors(containerColor = GuardianSurface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = null,
                                        tint = GuardianBlue,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = appName,
                                            color = textColor,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = packageName,
                                            color = grayText,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = grayText
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showVirusTotalAppPicker = false }) {
                    Text("Отмена", color = grayText)
                }
            }
        )
    }
    
    // APK Scan Dialog
    if (showApkDialog) {
        AlertDialog(
            onDismissRequest = { 
                showApkDialog = false
                apkResult = null
            },
            containerColor = surfaceColor,
            title = { 
                Text("Сканирование APK", color = textColor, fontWeight = FontWeight.Bold) 
            },
            text = {
                Column {
                    Text(
                        text = "Выберите APK файл для проверки на вирусы.",
                        color = grayText
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_GET_CONTENT).apply {
                                type = "application/vnd.android.package-archive"
                                addCategory(android.content.Intent.CATEGORY_OPENABLE)
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GuardianPrimary)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Выбрать файл")
                    }
                    if (apkResult != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = apkResult!!,
                            color = if (apkResult!!.startsWith("⚠️")) GuardianRed else GuardianGreen,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showApkDialog = false }) {
                    Text("Закрыть", color = grayText)
                }
            }
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GuardianSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSystemInDarkTheme()) Color.White else Color(0xFF1E293B),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSystemInDarkTheme()) Color.Gray else Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun AppListItem(
    app: AppScanResult,
    isBlacklisted: Boolean,
    onAddToBlacklist: () -> Unit,
    onRemoveFromBlacklist: () -> Unit,
    onUninstall: () -> Unit
) {
    val textColor = if (isSystemInDarkTheme()) Color.White else Color(0xFF1E293B)
    val grayText = if (isSystemInDarkTheme()) Color.Gray else Color(0xFF64748B)
    
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (app.isThreat) GuardianSurface else GuardianSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon placeholder
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when {
                            app.isThreat -> GuardianRed.copy(alpha = 0.2f)
                            app.isSystemApp -> GuardianBlue.copy(alpha = 0.1f)
                            else -> GuardianGreen.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        app.isThreat -> Icons.Default.Warning
                        app.virusTotalResult != null -> Icons.Default.BugReport
                        app.isSystemApp -> Icons.Default.Android
                        else -> Icons.Default.CheckCircle
                    },
                    contentDescription = null,
                    tint = when {
                        app.isThreat -> GuardianRed
                        app.virusTotalResult != null -> GuardianYellow
                        app.isSystemApp -> GuardianBlue
                        else -> GuardianGreen
                    },
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleSmall,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = grayText
                )
                if (app.isThreat && app.threatType.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = app.threatType,
                        style = MaterialTheme.typography.bodySmall,
                        color = GuardianRed
                    )
                }
                if (app.virusTotalResult != null && !app.virusTotalResult.isInfected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Безопасно (${app.virusTotalResult.detectedBy}/${app.virusTotalResult.totalScanners})",
                        style = MaterialTheme.typography.bodySmall,
                        color = GuardianGreen
                    )
                }
            }
            
            // System app badge
            if (app.isSystemApp && !app.isThreat) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = GuardianBlue.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Системное",
                        style = MaterialTheme.typography.labelSmall,
                        color = GuardianBlue,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            // Add/Remove from blacklist button for threats
            if (app.isThreat || isBlacklisted) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = if (isBlacklisted) onRemoveFromBlacklist else onAddToBlacklist,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isBlacklisted) Icons.Default.CheckCircle else Icons.Default.Block,
                        contentDescription = if (isBlacklisted) "Удалить из черного списка" else "Добавить в черный список",
                        tint = if (isBlacklisted) GuardianGreen else GuardianRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Uninstall button for threats (non-system apps only)
            if (app.isThreat && !app.isSystemApp) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onUninstall,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить приложение",
                        tint = GuardianRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MMM HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
