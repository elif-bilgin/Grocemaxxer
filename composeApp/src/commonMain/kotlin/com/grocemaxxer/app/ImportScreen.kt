package com.grocemaxxer.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.grocemaxxer.shared.GroceryItem
import com.grocemaxxer.shared.ShareTextCodec

/** State of the paste-a-received-list flow on the import screen. */
private sealed interface ImportState {
    data object Idle : ImportState
    data object NotAList : ImportState
    data object Identical : ImportState
    data class Different(
        val received: List<GroceryItem>,
        val diff: ShareTextCodec.ListDiff,
    ) : ImportState
}

private fun Set<String>.toggled(name: String): Set<String> =
    if (name in this) this - name else this + name

/**
 * Full-screen import flow: paste (or auto-pick-up from the clipboard) a
 * shared list, see how it differs from yours, then overwrite, merge
 * manually with per-item choices, or cancel.
 */
@Composable
internal fun ImportScreen(
    items: List<GroceryItem>,
    onBack: () -> Unit,
    onApply: (List<GroceryItem>) -> Unit,
) {
    var pasteText by remember { mutableStateOf("") }
    var importState by remember { mutableStateOf<ImportState>(ImportState.Idle) }
    var mergeMode by remember { mutableStateOf(false) }
    var addSelections by remember { mutableStateOf(setOf<String>()) }
    var keepSelections by remember { mutableStateOf(setOf<String>()) }
    var checkSelections by remember { mutableStateOf(setOf<String>()) }

    fun check(text: String): ImportState {
        mergeMode = false
        val received = ShareTextCodec.parse(text) ?: return ImportState.NotAList
        val diff = ShareTextCodec.diff(items, received)
        return if (diff.isIdentical) ImportState.Identical else ImportState.Different(received, diff)
    }

    // If the clipboard already holds a shared list (the expected flow:
    // copy the message, then open Import), prefill and check it instantly.
    val clipboard = LocalClipboardManager.current
    LaunchedEffect(Unit) {
        val clip = clipboard.getText()?.text ?: return@LaunchedEffect
        if (clip.lowercase().contains("grocemaxxer list")) {
            pasteText = clip
            importState = check(clip)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to list",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = "Import a List",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Copy the whole message you received, then open this screen — it's picked up automatically. Or paste it below.",
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
            modifier = Modifier.fillMaxWidth().height(150.dp),
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
                            text = state.diff.summary() + " · ${state.received.size} items total",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (!mergeMode) {
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
                                onClick = { onApply(state.received) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            ) {
                                Text("Overwrite my list with theirs", fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    addSelections = state.diff.added.toSet()
                                    keepSelections = state.diff.removed.toSet()
                                    checkSelections = state.diff.checkChanged.toSet()
                                    mergeMode = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                            ) {
                                Text("Merge manually…", fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                                Text("Cancel import")
                            }
                        } else {
                            Spacer(Modifier.height(10.dp))
                            if (state.diff.added.isNotEmpty()) {
                                Text(
                                    text = "Add from their list",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                state.diff.added.forEach { name ->
                                    MergeChoiceRow(
                                        label = name,
                                        checked = name in addSelections,
                                        onToggle = { addSelections = addSelections.toggled(name) },
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                            if (state.diff.removed.isNotEmpty()) {
                                Text(
                                    text = "Keep items they don't have",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "Unchecked items will be removed from your list",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                state.diff.removed.forEach { name ->
                                    MergeChoiceRow(
                                        label = name,
                                        checked = name in keepSelections,
                                        onToggle = { keepSelections = keepSelections.toggled(name) },
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                            if (state.diff.checkChanged.isNotEmpty()) {
                                Text(
                                    text = "Take their check-off status",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                state.diff.checkChanged.forEach { name ->
                                    MergeChoiceRow(
                                        label = name,
                                        checked = name in checkSelections,
                                        onToggle = { checkSelections = checkSelections.toggled(name) },
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    onApply(
                                        ShareTextCodec.merge(
                                            current = items,
                                            received = state.received,
                                            acceptAdds = addSelections,
                                            acceptRemovals = state.diff.removed.toSet() - keepSelections,
                                            acceptChecks = checkSelections,
                                        ),
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            ) {
                                Text("Apply merge", fontWeight = FontWeight.Bold)
                            }
                            TextButton(
                                onClick = { mergeMode = false },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Back")
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun MergeChoiceRow(
    label: String,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }
}
