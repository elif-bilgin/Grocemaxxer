package com.grocemaxxer.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grocemaxxer.shared.GroceryItem
import com.grocemaxxer.shared.GroceryListOrganizer
import com.grocemaxxer.shared.SectionGroup
import com.grocemaxxer.shared.StoreSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun App() {
    val repository = remember { GroceryRepository(groceryDataStore) }
    val scope = rememberCoroutineScope()
    val settings by repository.settings.collectAsState(initial = AppSettings())
    val items by repository.items.collectAsState(initial = emptyList())
    var showWelcome by remember { mutableStateOf(true) }

    GrocemaxxerTheme(settings.palette, settings.darkMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            if (showWelcome) {
                WelcomeScreen(onStart = { showWelcome = false })
            } else {
                MainScreen(
                    repository = repository,
                    scope = scope,
                    items = items,
                    settings = settings,
                )
            }
        }
    }
}

@Composable
private fun WelcomeScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Welcome",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(24.dp))
        Text(text = "🧺", fontSize = 96.sp)
        Spacer(Modifier.height(24.dp))
        Text(
            text = "grocemaxxer",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "a checklist that walks the store the smart way",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onStart,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            contentPadding = PaddingValues(horizontal = 36.dp, vertical = 14.dp),
        ) {
            Text("START MY LIST", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        }
    }
}

@Composable
private fun MainScreen(
    repository: GroceryRepository,
    scope: CoroutineScope,
    items: List<GroceryItem>,
    settings: AppSettings,
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    val visibleItems = if (searchQuery.isBlank()) {
        items
    } else {
        items.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    }
    val groups = remember(visibleItems) { GroceryListOrganizer.organize(visibleItems) }
    val checkedCount = items.count { it.isChecked }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(20.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add item")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "*grocemaxxer*",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                IconButton(onClick = { showSettingsSheet = true }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search your list...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
            )
            Spacer(Modifier.height(10.dp))

            if (items.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "$checkedCount of ${items.size} collected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row {
                        TextButton(onClick = { scope.launch { repository.clearChecked() } }) {
                            Text("Clear checked")
                        }
                        TextButton(onClick = { scope.launch { repository.clearAll() } }) {
                            Text("Clear all")
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            when {
                items.isEmpty() -> EmptyListPlaceholder()
                groups.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Nothing on your list matches \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
                ) {
                    items(groups, key = { it.section.name }) { group ->
                        SectionCard(
                            group = group,
                            darkMode = settings.darkMode,
                            onToggle = { id -> scope.launch { repository.toggleChecked(id) } },
                            onRemove = { id -> scope.launch { repository.removeItem(id) } },
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddItemSheet(
            onDismiss = { showAddSheet = false },
            onAdd = { text, section ->
                scope.launch { repository.addItems(text, section) }
                showAddSheet = false
            },
        )
    }

    if (showSettingsSheet) {
        SettingsSheet(
            settings = settings,
            onDismiss = { showSettingsSheet = false },
            onPaletteSelected = { scope.launch { repository.setPalette(it) } },
            onDarkModeChanged = { scope.launch { repository.setDarkMode(it) } },
        )
    }
}

@Composable
private fun EmptyListPlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "🧺", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Your basket is empty",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Tap + to add what you need — it'll be sorted\ninto the fastest walk through the store.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SectionCard(
    group: SectionGroup,
    darkMode: Boolean,
    onToggle: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    val style = sectionStyle(group.section)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = style.container(darkMode),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${style.emoji}  ${group.section.displayName.uppercase()}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = style.accent,
            )
            Spacer(Modifier.height(10.dp))
            group.items.forEach { item ->
                ItemRow(item = item, onToggle = onToggle, onRemove = onRemove)
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun ItemRow(
    item: GroceryItem,
    onToggle: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggle(item.id) },
            )
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                color = if (item.isChecked) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { onRemove(item.id) }) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove ${item.name}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemSheet(
    onDismiss: () -> Unit,
    onAdd: (String, StoreSection?) -> Unit,
) {
    var itemText by remember { mutableStateOf("") }
    var selectedSection by remember { mutableStateOf<StoreSection?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    fun submit() {
        if (itemText.isNotBlank()) onAdd(itemText, selectedSection)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 40.dp)) {
            Text(
                text = "Add New Item",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = itemText,
                onValueChange = { itemText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. milk, eggs, tulips") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
            )
            Spacer(Modifier.height(12.dp))
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it },
            ) {
                OutlinedTextField(
                    value = selectedSection?.let { "${sectionStyle(it).emoji} ${it.displayName}" }
                        ?: "Auto-detect category",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("✨ Auto-detect category") },
                        onClick = {
                            selectedSection = null
                            dropdownExpanded = false
                        },
                    )
                    StoreSection.entries.forEach { section ->
                        DropdownMenuItem(
                            text = { Text("${sectionStyle(section).emoji} ${section.displayName}") },
                            onClick = {
                                selectedSection = section
                                dropdownExpanded = false
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = { submit() },
                enabled = itemText.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                Text("Add to list", fontWeight = FontWeight.Bold)
            }
            Text(
                text = "Tip: separate multiple items with commas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SettingsSheet(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onPaletteSelected: (ThemePalette) -> Unit,
    onDarkModeChanged: (Boolean) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 40.dp)) {
            Text(
                text = "Settings & Customize",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(16.dp))
            Text("Color", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ThemePalette.entries.forEach { palette ->
                    val selected = palette == settings.palette
                    Surface(
                        onClick = { onPaletteSelected(palette) },
                        shape = RoundedCornerShape(16.dp),
                        color = palette.container,
                        border = if (selected) {
                            BorderStroke(2.dp, palette.primary)
                        } else {
                            null
                        },
                    ) {
                        Text(
                            text = palette.displayName,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                            color = palette.onContainer,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Light/Dark Mode", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = settings.darkMode,
                    onCheckedChange = onDarkModeChanged,
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
