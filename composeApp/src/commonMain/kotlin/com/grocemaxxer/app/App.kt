package com.grocemaxxer.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grocemaxxer.shared.GroceryItem
import com.grocemaxxer.shared.pickerDateLabel
import grocemaxxer.composeapp.generated.resources.Res
import grocemaxxer.composeapp.generated.resources.grocemaxxer_title_no_background
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

/**
 * Top-level destinations. [Loading] is the brief moment before the stored
 * lists have been read and the startup destination is known; declaration
 * order doubles as navigation depth, which drives the slide direction of the
 * screen transition.
 */
private enum class AppScreen { Loading, Welcome, Main, Import }

private const val SCREEN_TRANSITION_MS = 320

@Composable
fun App() {
    val repository = remember { GroceryRepository(groceryDataStore) }
    val catalog = remember { CatalogRepository() }
    val scope = rememberCoroutineScope()
    val settings by repository.settings.collectAsState(initial = AppSettings())
    val items by repository.items.collectAsState(initial = emptyList())
    // null until the first read lands, so startup can tell "no lists yet"
    // apart from "not loaded yet" and not flash the welcome screen.
    val storedDates by repository.storedDates.collectAsState(initial = null)

    LaunchedEffect(Unit) { catalog.ensureSeeded() }

    var screen by remember { mutableStateOf(AppScreen.Loading) }
    var showDatePicker by remember { mutableStateOf(false) }
    var pendingAdoptDate by remember { mutableStateOf<String?>(null) }

    // When the newest stored list predates today, the app opens straight onto
    // it -- blurred, behind the welcome buttons -- so you can see what you are
    // being asked about before choosing. Null once a choice has been made.
    var resumeDate by remember { mutableStateOf<String?>(null) }
    var resumePreview by remember { mutableStateOf<List<GroceryItem>>(emptyList()) }

    fun finishStartup() {
        resumeDate = null
        resumePreview = emptyList()
        screen = AppScreen.Main
    }

    // Startup routing, run once the stored dates are known: go straight to the
    // most recent list rather than to a menu, and only fall back to the
    // welcome screen when there is genuinely nothing to open.
    var startupRouted by remember { mutableStateOf(false) }
    LaunchedEffect(storedDates) {
        val dates = storedDates ?: return@LaunchedEffect
        if (startupRouted) return@LaunchedEffect
        startupRouted = true
        val latest = dates.firstOrNull()
        when {
            latest == null -> screen = AppScreen.Welcome
            latest == repository.todayIso -> screen = AppScreen.Main
            else -> {
                resumePreview = repository.peekList(latest)
                resumeDate = latest
                screen = AppScreen.Main
            }
        }
    }

    fun adoptOrAsk(isoDate: String) {
        scope.launch {
            val list = repository.peekList(isoDate)
            if (list.any { it.isChecked }) {
                pendingAdoptDate = isoDate
            } else {
                repository.adoptList(isoDate, clearChecked = false)
                finishStartup()
            }
        }
    }

    fun startFresh() {
        scope.launch {
            repository.startNewList()
            finishStartup()
        }
    }

    // Back: Import -> Main -> Welcome -> exit. Disabled while the resume
    // prompt is up, because dismissing it would leave a list on screen that
    // has not been adopted; backing out of the app is the honest outcome.
    PlatformBackHandler(enabled = screen == AppScreen.Import || (screen == AppScreen.Main && resumeDate == null)) {
        screen = when (screen) {
            AppScreen.Import -> AppScreen.Main
            else -> AppScreen.Welcome
        }
    }

    val resuming = resumeDate != null
    val previewBlur by animateDpAsState(
        targetValue = if (resuming) 14.dp else 0.dp,
        animationSpec = tween(SCREEN_TRANSITION_MS),
        label = "resumeBlur",
    )

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
                AnimatedContent(
                    targetState = screen,
                    transitionSpec = {
                        // Fade the first screen in; slide afterwards, in the
                        // direction of travel through the destinations.
                        val transform = if (initialState == AppScreen.Loading) {
                            fadeIn(tween(SCREEN_TRANSITION_MS)) togetherWith
                                fadeOut(tween(SCREEN_TRANSITION_MS))
                        } else {
                            val direction =
                                if (targetState.ordinal > initialState.ordinal) 1 else -1
                            (
                                slideInHorizontally(tween(SCREEN_TRANSITION_MS)) { width ->
                                    direction * width / 3
                                } + fadeIn(tween(SCREEN_TRANSITION_MS))
                                ) togetherWith (
                                slideOutHorizontally(tween(SCREEN_TRANSITION_MS)) { width ->
                                    -direction * width / 3
                                } + fadeOut(tween(SCREEN_TRANSITION_MS / 2))
                                )
                        }
                        transform.using(SizeTransform(clip = false))
                    },
                    label = "screen",
                ) { current ->
                    when (current) {
                        AppScreen.Loading -> Box(modifier = Modifier.fillMaxSize())
                        AppScreen.Welcome -> WelcomeContent(
                            hasStoredLists = !storedDates.isNullOrEmpty(),
                            onUsePrevious = {
                                val latest = storedDates?.firstOrNull()
                                if (latest == null) startFresh() else adoptOrAsk(latest)
                            },
                            onStartNew = ::startFresh,
                            onPickFromDate = { showDatePicker = true },
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(32.dp),
                        )
                        AppScreen.Main -> Box(modifier = Modifier.fillMaxSize().blur(previewBlur)) {
                            MainScreen(
                                repository = repository,
                                catalog = catalog,
                                scope = scope,
                                items = if (resuming) resumePreview else items,
                                dateIso = resumeDate ?: repository.todayIso,
                                settings = settings,
                                onOpenImport = { screen = AppScreen.Import },
                            )
                        }
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

                // The resume prompt: the welcome screen's own content, over
                // the blurred list it is asking about.
                AnimatedVisibility(
                    visible = resuming,
                    enter = fadeIn(tween(SCREEN_TRANSITION_MS)) +
                        scaleIn(tween(SCREEN_TRANSITION_MS), initialScale = 0.94f),
                    exit = fadeOut(tween(SCREEN_TRANSITION_MS)) +
                        scaleOut(tween(SCREEN_TRANSITION_MS), targetScale = 0.94f),
                ) {
                    val interactionSource = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.72f))
                            // Swallows taps and drags so nothing reaches the
                            // list behind, which is only a preview until one
                            // of these buttons is pressed.
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = {},
                            ),
                    ) {
                        WelcomeContent(
                            hasStoredLists = true,
                            onUsePrevious = { resumeDate?.let(::adoptOrAsk) },
                            onStartNew = ::startFresh,
                            onPickFromDate = { showDatePicker = true },
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(32.dp),
                        )
                    }
                }
            }
        }

        if (showDatePicker) {
            DateWheelSheet(
                dates = storedDates.orEmpty(),
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
                                finishStartup()
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
                                finishStartup()
                            }
                        },
                    ) { Text("Keep progress") }
                },
            )
        }
    }
}

/**
 * The start-screen body: logo, wordmark and the three list choices.
 *
 * Shared verbatim by the welcome screen and by the resume prompt that appears
 * over a blurred previous list, so the two can never drift apart.
 */
@Composable
private fun WelcomeContent(
    hasStoredLists: Boolean,
    onUsePrevious: () -> Unit,
    onStartNew: () -> Unit,
    onPickFromDate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
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
