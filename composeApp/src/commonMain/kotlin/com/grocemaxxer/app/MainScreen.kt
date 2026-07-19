package com.grocemaxxer.app

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grocemaxxer.shared.CatalogItem
import com.grocemaxxer.shared.GroceryInputParser
import com.grocemaxxer.shared.GroceryItem
import com.grocemaxxer.shared.GroceryListOrganizer
import com.grocemaxxer.shared.ItemCategorizer
import com.grocemaxxer.shared.SectionGroup
import com.grocemaxxer.shared.ShareTextCodec
import com.grocemaxxer.shared.Store
import com.grocemaxxer.shared.StoreSection
import com.grocemaxxer.shared.fullDateLabel
import com.grocemaxxer.shared.shortDateLabel
import com.grocemaxxer.shared.toDedupeKey
import com.grocemaxxer.shared.toTitleCase
import grocemaxxer.composeapp.generated.resources.Res
import grocemaxxer.composeapp.generated.resources.grocemaxxer_title_no_background
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun MainScreen(
    repository: GroceryRepository,
    catalog: CatalogRepository,
    scope: CoroutineScope,
    items: List<GroceryItem>,
    settings: AppSettings,
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showImportSheet by remember { mutableStateOf(false) }
    var archiveExpanded by remember { mutableStateOf(false) }
    var showCompletion by remember { mutableStateOf(false) }

    val visibleItems = if (searchQuery.isBlank()) {
        items
    } else {
        items.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    }
    val groups = remember(visibleItems, settings.store) {
        GroceryListOrganizer.organize(visibleItems, settings.store)
    }
    val activeGroups = groups.filter { group -> group.items.any { !it.isChecked } }
    val archivedGroups = groups.filter { group -> group.items.all { it.isChecked } }
    val checkedCount = items.count { it.isChecked }
    val allDone = items.isNotEmpty() && checkedCount == items.size

    // Show the "you got everything" celebration only when the list *becomes*
    // complete during use, not when opening an already-finished list.
    var wasDone by remember { mutableStateOf(true) }
    LaunchedEffect(allDone) {
        if (allDone && !wasDone) showCompletion = true
        wasDone = allDone
    }

    val listState = rememberLazyListState()
    val archiveHeaderIndex = activeGroups.size

    // Re-stack the archive when the user scrolls back up to the unchecked
    // sections (the stack header leaves the bottom of the viewport).
    LaunchedEffect(archiveExpanded, archiveHeaderIndex) {
        if (!archiveExpanded) return@LaunchedEffect
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: Int.MAX_VALUE }
            .collect { lastVisible ->
                if (lastVisible < archiveHeaderIndex) archiveExpanded = false
            }
    }

    val blurRadius by animateDpAsState(if (showCompletion) 10.dp else 0.dp)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.blur(blurRadius),
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
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // weight(1f) keeps the wordmark from consuming the whole
                    // row and pushing the share/settings buttons off-screen.
                    Column(modifier = Modifier.weight(1f)) {
                        Image(
                            painter = painterResource(Res.drawable.grocemaxxer_title_no_background),
                            contentDescription = "grocemaxxer",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.CenterStart,
                        )
                        Text(
                            text = fullDateLabel(repository.todayIso),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(
                        onClick = {
                            shareListText(
                                ShareTextCodec.encode(items, shortDateLabel(repository.todayIso)),
                            )
                        },
                        enabled = items.isNotEmpty(),
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share list",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    IconButton(onClick = { showImportSheet = true }) {
                        Icon(
                            Icons.Default.MailOutline,
                            contentDescription = "Import a received list",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))

                StoreSelectorRow(
                    selected = settings.store,
                    onSelect = { store -> scope.launch { repository.setStore(store) } },
                )
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
                    colors = themedTextFieldColors(),
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
                            TextButton(onClick = { scope.launch { repository.setAllChecked(false) } }) {
                                Text("Uncheck all")
                            }
                            TextButton(onClick = { scope.launch { repository.setAllChecked(true) } }) {
                                Text("Check all")
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
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
                    ) {
                        items(activeGroups, key = { it.section.name }) { group ->
                            SectionCard(
                                group = group,
                                darkMode = settings.darkMode,
                                muted = false,
                                onToggle = { id -> scope.launch { repository.toggleChecked(id) } },
                                onRemove = { id -> scope.launch { repository.removeItem(id) } },
                                modifier = Modifier.animateItem(),
                            )
                        }
                        if (archivedGroups.isNotEmpty()) {
                            item(key = "archive-stack-header") {
                                ArchiveStackHeader(
                                    count = archivedGroups.size,
                                    expanded = archiveExpanded,
                                    onClick = { archiveExpanded = !archiveExpanded },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                            if (archiveExpanded) {
                                items(archivedGroups, key = { it.section.name }) { group ->
                                    SectionCard(
                                        group = group,
                                        darkMode = settings.darkMode,
                                        muted = true,
                                        onToggle = { id -> scope.launch { repository.toggleChecked(id) } },
                                        onRemove = { id -> scope.launch { repository.removeItem(id) } },
                                        modifier = Modifier.animateItem(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showCompletion) {
            CompletionOverlay(
                onAddMore = {
                    showCompletion = false
                    showAddSheet = true
                },
                onDismiss = { showCompletion = false },
            )
        }
    }

    if (showAddSheet) {
        AddItemSheet(
            currentItems = items,
            catalog = catalog,
            onDismiss = { showAddSheet = false },
            onAdd = { text, section ->
                scope.launch {
                    repository.addItems(text, section)
                    // Remember typed items so they're suggested next time.
                    GroceryInputParser.parse(text).forEach { raw ->
                        val name = raw.toTitleCase()
                        catalog.recordCustom(name, section ?: ItemCategorizer.categorize(name))
                    }
                }
                showAddSheet = false
            },
            onQuickAdd = { name, section ->
                scope.launch { repository.addItems(name, section) }
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

    if (showImportSheet) {
        ImportSheet(
            items = items,
            onDismiss = { showImportSheet = false },
            onOverwrite = { received ->
                scope.launch { repository.replaceTodayList(received) }
                showImportSheet = false
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StoreSelectorRow(
    selected: Store,
    onSelect: (Store) -> Unit,
) {
    val rowState = rememberLazyListState()
    // Selected store leads the row; the rest keep their canonical order.
    // Keyed items + animateItem make the promotion a slide, not a jump.
    val orderedStores = listOf(selected) + Store.entries.filter { it != selected }

    LaunchedEffect(selected) {
        rowState.animateScrollToItem(0)
    }

    LazyRow(
        state = rowState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(orderedStores, key = { it.name }) { store ->
            val isSelected = store == selected
            Surface(
                onClick = { onSelect(store) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                border = if (isSelected) {
                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                } else {
                    null
                },
                modifier = Modifier.animateItem(),
            ) {
                Text(
                    text = "${store.emoji} ${store.displayName}",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
private fun EmptyListPlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BasketLogo(size = 120.dp)
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
    muted: Boolean,
    onToggle: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = sectionStyle(group.section)
    Surface(
        modifier = modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        color = style.container(darkMode).let { if (muted) it.copy(alpha = it.alpha * 0.6f) else it },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${style.emoji}  ${group.section.displayName.uppercase()}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = if (muted) style.accent.copy(alpha = 0.7f) else style.accent,
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

@Composable
private fun ArchiveStackHeader(
    count: Int,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val chevronRotation by animateFloatAsState(if (expanded) 180f else 0f)
    val stackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(modifier = modifier.fillMaxWidth().padding(bottom = if (expanded) 0.dp else 10.dp)) {
        // Ghost cards behind the header give the collapsed "stack" look.
        if (!expanded) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp)
                    .offset(y = 10.dp),
                shape = RoundedCornerShape(24.dp),
                color = stackColor.copy(alpha = 0.35f),
            ) {}
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .height(52.dp)
                    .offset(y = 5.dp),
                shape = RoundedCornerShape(24.dp),
                color = stackColor.copy(alpha = 0.6f),
            ) {}
        }
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(24.dp),
            color = stackColor,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "✅  Done sections ($count)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(chevronRotation),
                )
            }
        }
    }
}

@Composable
private fun CompletionOverlay(
    onAddMore: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.padding(32.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "🎉", fontSize = 56.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "You got everything!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Anything else you want to grab?\nOtherwise, head to checkout!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp),
                ) {
                    Text("Head to checkout 🧺", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onAddMore, modifier = Modifier.fillMaxWidth()) {
                    Text("Add more items")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddItemSheet(
    currentItems: List<GroceryItem>,
    catalog: CatalogRepository,
    onDismiss: () -> Unit,
    onAdd: (String, StoreSection?) -> Unit,
    onQuickAdd: (String, StoreSection) -> Unit,
) {
    var itemText by remember { mutableStateOf("") }
    var selectedSection by remember { mutableStateOf<StoreSection?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<CatalogItem>>(emptyList()) }
    var browseSection by remember { mutableStateOf<StoreSection?>(null) }
    var browseItems by remember { mutableStateOf<List<CatalogItem>>(emptyList()) }

    LaunchedEffect(itemText, currentItems) {
        suggestions = catalog.suggestions(itemText, currentItems.map { it.name })
    }
    LaunchedEffect(browseSection, currentItems) {
        val section = browseSection
        browseItems = if (section == null) {
            emptyList()
        } else {
            catalog.itemsFor(section).filter { preset ->
                currentItems.none { it.name.toDedupeKey() == preset.name.toDedupeKey() }
            }
        }
    }

    fun submit() {
        if (itemText.isNotBlank()) onAdd(itemText, selectedSection)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 40.dp),
        ) {
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
                colors = themedTextFieldColors(),
            )
            if (suggestions.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    suggestions.forEach { suggestion ->
                        Surface(
                            onClick = {
                                onQuickAdd(suggestion.name, suggestion.section)
                                itemText = ""
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Text(
                                text = "${sectionStyle(suggestion.section).emoji} ${suggestion.name}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
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
                    colors = themedTextFieldColors(),
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
                text = "Tip: separate multiple items with commas · duplicates are skipped automatically",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )

            Spacer(Modifier.height(20.dp))
            Text("Or browse saved items", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(
                    StoreSection.entries.filter { it != StoreSection.OTHER },
                    key = { it.name },
                ) { section ->
                    val isSelected = section == browseSection
                    Surface(
                        onClick = { browseSection = if (isSelected) null else section },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) {
                            sectionStyle(section).lightContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = if (isSelected) {
                            BorderStroke(1.5.dp, sectionStyle(section).accent)
                        } else {
                            null
                        },
                    ) {
                        Text(
                            text = "${sectionStyle(section).emoji} ${section.displayName}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
            if (browseSection != null) {
                Spacer(Modifier.height(10.dp))
                if (browseItems.isEmpty()) {
                    Text(
                        text = "Everything from this aisle is already on your list!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        browseItems.forEach { preset ->
                            Surface(
                                onClick = { onQuickAdd(preset.name, preset.section) },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                            ) {
                                Text(
                                    text = "+ ${preset.name}",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Text-field colors that follow the active theme: filled with the
 * palette's pastel container, borderless until focused.
 */
@Composable
private fun themedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = Color.Transparent,
    focusedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    focusedTrailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
    unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
    cursorColor = MaterialTheme.colorScheme.primary,
)

/** State of the paste-a-received-list flow inside the share sheet. */
private sealed interface ImportState {
    data object Idle : ImportState
    data object NotAList : ImportState
    data object Identical : ImportState
    data class Different(
        val received: List<GroceryItem>,
        val diff: ShareTextCodec.ListDiff,
    ) : ImportState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportSheet(
    items: List<GroceryItem>,
    onDismiss: () -> Unit,
    onOverwrite: (List<GroceryItem>) -> Unit,
) {
    var pasteText by remember { mutableStateOf("") }
    var importState by remember { mutableStateOf<ImportState>(ImportState.Idle) }

    fun check(text: String): ImportState {
        val received = ShareTextCodec.parse(text) ?: return ImportState.NotAList
        val diff = ShareTextCodec.diff(items, received)
        return if (diff.isIdentical) ImportState.Identical else ImportState.Different(received, diff)
    }

    // If the clipboard already holds a shared list (the expected flow:
    // copy the message, then tap Import), prefill and check it instantly.
    val clipboard = LocalClipboardManager.current
    LaunchedEffect(Unit) {
        val clip = clipboard.getText()?.text ?: return@LaunchedEffect
        if (clip.lowercase().contains("grocemaxxer list")) {
            pasteText = clip
            importState = check(clip)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 40.dp)) {
            Text(
                text = "Import a List",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Copy the whole message you received, then open this sheet — it's picked up automatically. Or paste it below.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = pasteText,
                onValueChange = {
                    pasteText = it
                    importState = ImportState.Idle
                },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = { Text("Paste the whole message here…") },
                shape = RoundedCornerShape(16.dp),
                colors = themedTextFieldColors(),
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { importState = check(pasteText) },
                enabled = pasteText.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
            ) {
                Text("Check received list")
            }

            when (val state = importState) {
                ImportState.Idle -> {}
                ImportState.NotAList -> Text(
                    text = "That doesn't look like a grocemaxxer list — paste the whole message, including the 🧺 header line.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 10.dp),
                )
                ImportState.Identical -> Text(
                    text = "✓ That list matches yours exactly — nothing to update.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 10.dp),
                )
                is ImportState.Different -> {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "This list is different from yours",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = state.diff.summary() +
                                    " · ${state.received.size} items total",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (state.diff.added.isNotEmpty()) {
                                Text(
                                    text = "New: " + state.diff.added.joinToString(", "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                            if (state.diff.removed.isNotEmpty()) {
                                Text(
                                    text = "Not in theirs: " + state.diff.removed.joinToString(", "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { onOverwrite(state.received) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            ) {
                                Text("Overwrite my list with theirs", fontWeight = FontWeight.Bold)
                            }
                            TextButton(
                                onClick = { importState = ImportState.Idle },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Keep mine")
                            }
                        }
                    }
                }
            }
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
