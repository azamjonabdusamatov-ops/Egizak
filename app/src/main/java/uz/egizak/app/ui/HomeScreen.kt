package uz.egizak.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uz.egizak.app.data.Clone
import uz.egizak.app.data.CloneRepository
import uz.egizak.app.data.InstalledApp
import uz.egizak.app.data.Prefs
import uz.egizak.app.data.appIcon
import uz.egizak.app.data.loadInstalledApps

private enum class Tab { CLONES, APPS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(repo: CloneRepository, toast: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(Tab.CLONES) }
    var clones by remember { mutableStateOf(emptyList<Clone>()) }
    var apps by remember { mutableStateOf(emptyList<InstalledApp>()) }
    var query by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }
    val iconCache = remember { mutableStateMapOf<String, ImageBitmap?>() }

    if (showSettings) SettingsDialog(onDismiss = { showSettings = false })

    fun iconFor(pkg: String): ImageBitmap? = iconCache.getOrPut(pkg) { appIcon(context, pkg) }

    fun run(action: () -> Result<*>, done: String? = null) {
        scope.launch {
            busy = true
            val result = withContext(Dispatchers.IO) { action() }
            result.onFailure { toast(it.message ?: "Хато") }.onSuccess { done?.let(toast) }
            clones = withContext(Dispatchers.IO) { repo.all() }
            busy = false
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            apps = loadInstalledApps(context)
            clones = runCatching { repo.all() }.getOrDefault(emptyList())
        }
        busy = false
    }

    val filteredApps = apps.filter { it.label.contains(query, ignoreCase = true) }
    val filteredClones = clones.filter { it.label.contains(query, ignoreCase = true) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Egizak", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                actions = {
                    IconButton({ showSettings = true }) {
                        Icon(Icons.Default.Settings, "Созламалар")
                    }
                },
            )
        },
        floatingActionButton = {
            if (tab == Tab.CLONES) {
                ExtendedFloatingActionButton(
                    onClick = { tab = Tab.APPS; query = "" },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Клон қўшиш") },
                )
            }
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            SegmentedTabs(tab, clones.size) { tab = it; query = "" }
            SearchField(query, { query = it }) {
                if (tab == Tab.CLONES) "Клонлардан қидириш" else "Программа қидириш"
            }
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())

            when (tab) {
                Tab.CLONES -> CloneList(filteredClones, busy, ::iconFor, hasAny = clones.isNotEmpty(),
                    onOpen = { run({ repo.launch(it) }) },
                    onDelete = { run({ repo.removeClone(it) }) })
                Tab.APPS -> AppList(filteredApps, busy, clones,
                    onClone = { run({ repo.addClone(it.packageName) }, "«${it.label}» клони яратилди") })
            }
        }
    }
}

@Composable
private fun SegmentedTabs(selected: Tab, cloneCount: Int, onSelect: (Tab) -> Unit) {
    TabRow(selectedTabIndex = if (selected == Tab.CLONES) 0 else 1, modifier = Modifier.padding(horizontal = 8.dp)) {
        Tab(selected == Tab.CLONES, { onSelect(Tab.CLONES) },
            text = { Text(if (cloneCount > 0) "Клонлар ($cloneCount)" else "Клонлар") })
        Tab(selected == Tab.APPS, { onSelect(Tab.APPS) }, text = { Text("Программалар") })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchField(query: String, onChange: (String) -> Unit, placeholder: () -> String) {
    OutlinedTextField(
        value = query,
        onValueChange = onChange,
        singleLine = true,
        leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = {
            if (query.isNotEmpty()) IconButton({ onChange("") }) { Icon(Icons.Default.Close, "Тозалаш") }
        },
        placeholder = { Text(placeholder()) },
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun CloneList(
    clones: List<Clone>,
    busy: Boolean,
    iconFor: (String) -> ImageBitmap?,
    hasAny: Boolean,
    onOpen: (Clone) -> Unit,
    onDelete: (Clone) -> Unit,
) {
    if (clones.isEmpty() && !busy) {
        EmptyState(if (hasAny) "Ҳеч нарса топилмади" else "Ҳали клон йўқ.\n«Клон қўшиш» тугмасини босинг.")
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 96.dp)) {
        items(clones, key = { "${it.userId}/${it.packageName}" }) { c ->
            Card(
                Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(enabled = !busy) { onOpen(c) },
                shape = RoundedCornerShape(20.dp),
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    AppBadge(iconFor(c.packageName), c.userId + 1)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(c.label, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Очиш учун босинг", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onDelete(c) }, enabled = !busy) {
                        Icon(Icons.Outlined.Delete, "Ўчириш", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppList(
    apps: List<InstalledApp>,
    busy: Boolean,
    clones: List<Clone>,
    onClone: (InstalledApp) -> Unit,
) {
    if (apps.isEmpty() && !busy) {
        EmptyState("Программа топилмади")
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 24.dp)) {
        items(apps, key = { it.packageName }) { app ->
            val count = clones.count { it.packageName == app.packageName }
            Card(Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = RoundedCornerShape(20.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    AppBadge(app.icon, if (count > 0) count else null)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(app.label, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(if (count > 0) "$count та клон" else "Клон йўқ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    FilledTonalButton(onClick = { onClone(app) }, enabled = !busy) {
                        Icon(Icons.Outlined.ContentCopy, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Клон")
                    }
                }
            }
        }
    }
}

@Composable
private fun AppBadge(icon: ImageBitmap?, badge: Int?) {
    Box(contentAlignment = Alignment.BottomEnd) {
        if (icon != null) {
            Image(icon, null, Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Fit)
        } else {
            Surface(
                Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)),
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {}
        }
        if (badge != null) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                Text(
                    badge.toString(),
                    Modifier.sizeIn(minWidth = 18.dp, minHeight = 18.dp).padding(horizontal = 5.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun SettingsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var hideRoot by remember { mutableStateOf(Prefs.hideRoot(context)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onDismiss) { Text("Ёпиш") } },
        title = { Text("Созламалар") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Қурилмани яшириш", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Клон иловалардан root ва хавфсизлик изларини яширади. " +
                                "Баъзи иловалар клонда ишлаши мумкин. Жиддий банк иловаларида одатда кам ёрдам беради.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = hideRoot,
                        onCheckedChange = { hideRoot = it; Prefs.setHideRoot(context, it) },
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Ўзгариш кучга кириши учун клонни бутунлай ёпиб, қайта очинг.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )
}

@Composable
private fun EmptyState(text: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge)
    }
}
