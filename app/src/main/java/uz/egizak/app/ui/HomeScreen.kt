package uz.egizak.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uz.egizak.app.R
import uz.egizak.app.data.Clone
import uz.egizak.app.data.CloneRepository
import uz.egizak.app.data.InstalledApp
import uz.egizak.app.data.loadInstalledApps

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(repo: CloneRepository, toast: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tab by remember { mutableIntStateOf(0) }
    var clones by remember { mutableStateOf(emptyList<Clone>()) }
    var apps by remember { mutableStateOf(emptyList<InstalledApp>()) }
    var busy by remember { mutableStateOf(true) }

    // Engine calls touch disk and IPC, so keep them off the main thread.
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

    val created = stringResource(R.string.clone_created)
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }) { pad ->
        Column(Modifier.padding(pad)) {
            TabRow(selectedTabIndex = tab) {
                Tab(tab == 0, { tab = 0 }, text = { Text(stringResource(R.string.tab_clones)) })
                Tab(tab == 1, { tab = 1 }, text = { Text(stringResource(R.string.tab_apps)) })
            }
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (tab == 0) {
                if (clones.isEmpty() && !busy) {
                    Text(stringResource(R.string.no_clones), Modifier.padding(16.dp))
                }
                LazyColumn {
                    items(clones, key = { "${it.userId}/${it.packageName}" }) { c ->
                        ListItem(
                            headlineContent = { Text(c.label) },
                            supportingContent = { Text(c.packageName) },
                            trailingContent = {
                                IconButton({ run({ repo.removeClone(c) }) }) { Icon(Icons.Default.Delete, null) }
                            },
                            modifier = Modifier.clickable(enabled = !busy) { run({ repo.launch(c) }) },
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(apps, key = { it.packageName }) { app ->
                        val count = clones.count { it.packageName == app.packageName }
                        ListItem(
                            headlineContent = { Text(app.label) },
                            supportingContent = { Text(if (count > 0) "Клонлар: $count" else app.packageName) },
                            trailingContent = {
                                IconButton({ run({ repo.addClone(app.packageName) }, created) }, enabled = !busy) {
                                    Icon(Icons.Default.Add, stringResource(R.string.add_clone))
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
