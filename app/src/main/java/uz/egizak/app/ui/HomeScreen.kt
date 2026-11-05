package uz.egizak.app.ui

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
import uz.egizak.app.R
import uz.egizak.app.data.CloneRepository
import uz.egizak.app.data.loadInstalledApps

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(repo: CloneRepository, toast: (String) -> Unit) {
    val context = LocalContext.current
    var tab by remember { mutableIntStateOf(0) }
    var clones by remember { mutableStateOf(repo.all()) }
    val apps = remember { loadInstalledApps(context) }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }) { pad ->
        Column(Modifier.padding(pad)) {
            TabRow(selectedTabIndex = tab) {
                Tab(tab == 0, { tab = 0 }, text = { Text(stringResource(R.string.tab_clones)) })
                Tab(tab == 1, { tab = 1 }, text = { Text(stringResource(R.string.tab_apps)) })
            }
            if (tab == 0) {
                if (clones.isEmpty()) {
                    Text(stringResource(R.string.no_clones), Modifier.padding(16.dp))
                }
                LazyColumn {
                    items(clones) { c ->
                        ListItem(
                            headlineContent = { Text(c.label) },
                            supportingContent = { Text(c.packageName) },
                            trailingContent = {
                                IconButton({
                                    repo.removeClone(c).onFailure { toast(it.message ?: "") }
                                    clones = repo.all()
                                }) { Icon(Icons.Default.Delete, null) }
                            },
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(apps) { app ->
                        val count = clones.count { it.packageName == app.packageName }
                        ListItem(
                            headlineContent = { Text(app.label) },
                            supportingContent = { Text(if (count > 0) "Клонлар: $count" else app.packageName) },
                            trailingContent = {
                                IconButton({
                                    repo.addClone(app.packageName, app.label)
                                        .onFailure { toast(it.message ?: "") }
                                    clones = repo.all()
                                }) { Icon(Icons.Default.Add, stringResource(R.string.add_clone)) }
                            },
                        )
                    }
                }
            }
        }
    }
}
