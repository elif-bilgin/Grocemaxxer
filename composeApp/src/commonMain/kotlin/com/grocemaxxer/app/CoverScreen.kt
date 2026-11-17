package com.grocemaxxer.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grocemaxxer.shared.GroceryItem
import com.grocemaxxer.shared.GroceryListOrganizer

/**
 * The checklist as it appears on a cover display -- the Galaxy Z Flip's outer
 * screen and anything else of that size.
 *
 * Not the main screen scaled down. A window roughly 260-330dp square, used at
 * arm's length with one thumb while pushing a trolley, wants a different
 * thing entirely:
 *
 * - **Only what is left to buy.** Checked items disappear rather than sinking,
 *   because every row costs a meaningful share of the screen.
 * - **No chrome.** No wordmark, no search, no store selector, no sharing, no
 *   settings. Typing on a cover display is miserable, so the add sheet is
 *   gone too; this screen checks things off and nothing else.
 * - **Rows sized for a thumb**, with the whole row as the target rather than
 *   a checkbox.
 * - **Progress in the header**, since the list no longer shows what is done.
 *
 * Opening the phone returns to the full app with the same list, because both
 * read the same stored list and write through the same repository.
 */
@Composable
internal fun CoverScreen(
    items: List<GroceryItem>,
    settings: AppSettings,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val remaining = remember(items, settings.store) {
        GroceryListOrganizer.organize(items.filterNot { it.isChecked }, settings.store)
    }
    val checkedCount = items.count { it.isChecked }

    Column(modifier = modifier.fillMaxSize()) {
        // A cover display reports no window insets, so without an explicit
        // top margin the header sits flush against the physical edge and the
        // first line reads as clipped.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when {
                    items.isEmpty() -> "No list"
                    remaining.isEmpty() -> "All done"
                    else -> "${items.size - checkedCount} to go"
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            if (items.isNotEmpty()) {
                Text(
                    text = "$checkedCount/${items.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

        when {
            items.isEmpty() -> CoverMessage("Open GroceMaxxer on the main screen to start a list.")
            remaining.isEmpty() -> CoverMessage("You got everything! 🎉")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                remaining.forEach { group ->
                    val style = sectionStyle(group.section)
                    item(key = "header-${group.section.name}") {
                        Text(
                            text = "${style.emoji} ${group.section.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = style.accent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 2.dp, top = 6.dp, bottom = 1.dp),
                        )
                    }
                    items(group.items, key = { it.id }) { groceryItem ->
                        CoverItemRow(
                            name = groceryItem.name,
                            style = style,
                            darkMode = settings.darkMode,
                            onClick = { onToggle(groceryItem.id) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Carries the section's own colour, the same idea as the aisle cards on the
 * main screen -- a section is recognisable by its colour before you read it.
 * Here the colour is on each row rather than on a card around the group,
 * because a card's padding and corners cost height this screen cannot spare.
 */
@Composable
private fun CoverItemRow(
    name: String,
    style: SectionStyle,
    darkMode: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = style.container(darkMode),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 9.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(15.dp)
                    .border(2.dp, style.accent, CircleShape),
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CoverMessage(text: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
