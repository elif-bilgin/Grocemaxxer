package com.grocemaxxer.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grocemaxxer.shared.pickerDateLabel
import grocemaxxer.composeapp.generated.resources.Res
import grocemaxxer.composeapp.generated.resources.grocemaxxer_title_no_background
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

private enum class AppScreen { Welcome, Main, Import }

@Composable
fun App() {
    val repository = remember { GroceryRepository(groceryDataStore) }
    val catalog = remember { CatalogRepository() }
    val scope = rememberCoroutineScope()
    val settings by repository.settings.collectAsState(initial = AppSettings())
    val items by repository.items.collectAsState(initial = emptyList())
    val storedDates by repository.storedDates.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) { catalog.ensureSeeded() }

    var screen by remember { mutableStateOf(AppScreen.Welcome) }
    var showDatePicker by remember { mutableStateOf(false) }
    var pendingAdoptDate by remember { mutableStateOf<String?>(null) }

    fun adoptOrAsk(isoDate: String) {
        scope.launch {
            val list = repository.peekList(isoDate)
            if (list.any { it.isChecked }) {
                pendingAdoptDate = isoDate
            } else {
                repository.adoptList(isoDate, clearChecked = false)
                screen = AppScreen.Main
            }
        }
    }

    // Back gesture: Import -> Main -> Welcome -> exit app.
    PlatformBackHandler(enabled = screen != AppScreen.Welcome) {
        screen = when (screen) {
            AppScreen.Import -> AppScreen.Main
            else -> AppScreen.Welcome
        }
    }

    GrocemaxxerTheme(settings.palette, settings.darkMode) {
        SystemBarAppearance(settings.darkMode)
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            // The activity draws edge-to-edge, so the background colour fills
            // the whole screen while content is inset once, here. The padding
            // consumes the insets, so the nested Scaffold in MainScreen (and
            // anything else reading WindowInsets below this point) does not
            // add them a second time. safeDrawing also covers the keyboard,
            // which edge-to-edge windows no longer get from adjustResize.
            Box(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
                when (screen) {
                    AppScreen.Welcome -> WelcomeScreen(
                        hasStoredLists = storedDates.isNotEmpty(),
                        onUsePrevious = {
                            val latest = storedDates.firstOrNull()
                            if (latest == null) {
                                scope.launch {
                                    repository.startNewList()
                                    screen = AppScreen.Main
                                }
                            } else {
                                adoptOrAsk(latest)
                            }
                        },
                        onStartNew = {
                            scope.launch {
                                repository.startNewList()
                                screen = AppScreen.Main
                            }
                        },
                        onPickFromDate = { showDatePicker = true },
                    )
                    AppScreen.Main -> MainScreen(
                        repository = repository,
                        catalog = catalog,
                        scope = scope,
                        items = items,
                        settings = settings,
                        onOpenImport = { screen = AppScreen.Import },
                    )
                    AppScreen.Import -> ImportScreen(
                        items = items,
                        onBack = { screen = AppScreen.Main },
                        onApply = { received ->
                            scope.launch { repository.replaceTodayList(received) }
                            screen = AppScreen.Main
                        },
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        DateWheelSheet(
            dates = storedDates,
            onDismiss = { showDatePicker = false },
            onLoad = { isoDate ->
                showDatePicker = false
                adoptOrAsk(isoDate)
            },
        )
    }

    pendingAdoptDate?.let { isoDate ->
        AlertDialog(
            onDismissRequest = { pendingAdoptDate = null },
            title = { Text("Keep your progress?") },
            text = {
                Text(
                    "This list has checked-off items. Keep them checked (picking up " +
                        "where you left off), or clear them for a fresh shopping run?",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingAdoptDate = null
                        scope.launch {
                            repository.adoptList(isoDate, clearChecked = true)
                            screen = AppScreen.Main
                        }
                    },
                ) { Text("Clear checked") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingAdoptDate = null
                        scope.launch {
                            repository.adoptList(isoDate, clearChecked = false)
                            screen = AppScreen.Main
                        }
                    },
                ) { Text("Keep progress") }
            },
        )
    }
}

@Composable
private fun WelcomeScreen(
    hasStoredLists: Boolean,
    onUsePrevious: () -> Unit,
    onStartNew: () -> Unit,
    onPickFromDate: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(28.dp))
        BasketLogo(size = 160.dp)
        Spacer(Modifier.height(28.dp))
        Image(
            painter = painterResource(Res.drawable.grocemaxxer_title_no_background),
            contentDescription = "GroceMaxxer",
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Your grocery run, optimized.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))

        Button(
            onClick = onUsePrevious,
            enabled = hasStoredLists,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Text("USE PREVIOUS LIST", fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onStartNew,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Text("START NEW LIST", fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        }
        Spacer(Modifier.height(12.dp))
        TextButton(
            onClick = onPickFromDate,
            enabled = hasStoredLists,
        ) {
            Text("Use list from a date…")
        }
        Spacer(Modifier.height(28.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateWheelSheet(
    dates: List<String>,
    onDismiss: () -> Unit,
    onLoad: (String) -> Unit,
) {
    val listState = rememberLazyListState()
    val rowHeight = 44.dp
    val wheelHeight = 220.dp

    val centeredIndex by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2
            info.visibleItemsInfo
                .minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - viewportCenter) }
                ?.index ?: 0
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Load a list from…",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "It'll be copied to today so your history stays intact.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(wheelHeight),
                contentAlignment = Alignment.Center,
            ) {
                Column(modifier = Modifier.fillMaxWidth().height(rowHeight)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    Spacer(Modifier.height(rowHeight - 2.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = (wheelHeight - rowHeight) / 2),
                ) {
                    itemsIndexed(dates) { index, isoDate ->
                        val isCentered = index == centeredIndex
                        Box(
                            modifier = Modifier.fillMaxWidth().height(rowHeight),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = pickerDateLabel(isoDate),
                                style = if (isCentered) {
                                    MaterialTheme.typography.titleMedium
                                } else {
                                    MaterialTheme.typography.bodyMedium
                                },
                                fontWeight = if (isCentered) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCentered) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { dates.getOrNull(centeredIndex)?.let(onLoad) },
                enabled = dates.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                Text("Load this list", fontWeight = FontWeight.Bold)
            }
        }
    }
}
