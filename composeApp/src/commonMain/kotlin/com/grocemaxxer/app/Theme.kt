package com.grocemaxxer.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.grocemaxxer.shared.StoreSection

/** User-selectable accent palette, in the pastel style of the reference design. */
enum class ThemePalette(
    val displayName: String,
    val primary: Color,
    val container: Color,
    val onContainer: Color,
    val backgroundLight: Color,
) {
    MINT("Mint", Color(0xFF7FB77E), Color(0xFFDCEEDC), Color(0xFF2F5233), Color(0xFFF6FBF4)),
    PEACH("Peach", Color(0xFFE8A26D), Color(0xFFFFE3C7), Color(0xFF6B3F1D), Color(0xFFFFF8F0)),
    LAVENDER("Lavender", Color(0xFF9C8ACB), Color(0xFFE7DFF7), Color(0xFF3F3260), Color(0xFFFAF8FE)),
    SKY("Sky Blue", Color(0xFF6FA8DC), Color(0xFFD8EAF8), Color(0xFF1E3A5C), Color(0xFFF4F9FD)),
    ;

    companion object {
        fun fromName(name: String?): ThemePalette = entries.firstOrNull { it.name == name } ?: MINT
    }
}

@Composable
fun GrocemaxxerTheme(
    palette: ThemePalette,
    darkMode: Boolean,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkMode) {
        darkColorScheme(
            primary = palette.primary,
            onPrimary = Color(0xFF1B1B1E),
            primaryContainer = palette.primary.copy(alpha = 0.28f),
            onPrimaryContainer = Color(0xFFEDE8EE),
            background = Color(0xFF17151A),
            onBackground = Color(0xFFE9E4EA),
            surface = Color(0xFF211F24),
            onSurface = Color(0xFFE9E4EA),
            surfaceVariant = Color(0xFF2A272E),
            onSurfaceVariant = Color(0xFFB9B2BC),
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            onPrimary = Color.White,
            primaryContainer = palette.container,
            onPrimaryContainer = palette.onContainer,
            background = palette.backgroundLight,
            onBackground = Color(0xFF3A342F),
            surface = Color.White,
            onSurface = Color(0xFF3A342F),
            surfaceVariant = palette.container.copy(alpha = 0.55f),
            onSurfaceVariant = Color(0xFF7A736E),
        )
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}

/** Cosmetic styling for a store section: emoji badge + pastel card colors. */
data class SectionStyle(
    val emoji: String,
    val lightContainer: Color,
    val accent: Color,
) {
    fun container(darkMode: Boolean): Color =
        if (darkMode) accent.copy(alpha = 0.16f) else lightContainer
}

fun sectionStyle(section: StoreSection): SectionStyle = when (section) {
    StoreSection.FLORAL -> SectionStyle("💐", Color(0xFFFCE4EC), Color(0xFFD2698F))
    StoreSection.PRODUCE -> SectionStyle("🥬", Color(0xFFE8F5E9), Color(0xFF5F9E63))
    StoreSection.BAKERY -> SectionStyle("🥐", Color(0xFFFFE0B2), Color(0xFFC77F2E))
    StoreSection.DELI -> SectionStyle("🥪", Color(0xFFFFF3E0), Color(0xFFB07344))
    StoreSection.MEAT_SEAFOOD -> SectionStyle("🥩", Color(0xFFFFEBEE), Color(0xFFC05B5B))
    StoreSection.DAIRY_EGGS -> SectionStyle("🥛", Color(0xFFE3F2FD), Color(0xFF5187B8))
    StoreSection.FROZEN -> SectionStyle("🧊", Color(0xFFE0F7FA), Color(0xFF3E97A8))
    StoreSection.PANTRY_CANNED -> SectionStyle("🥫", Color(0xFFFFF9C4), Color(0xFFA69327))
    StoreSection.BREAKFAST_CEREAL -> SectionStyle("🥣", Color(0xFFFFECB3), Color(0xFFB08A25))
    StoreSection.BAKING -> SectionStyle("🧁", Color(0xFFF3E5F5), Color(0xFFA264AE))
    StoreSection.SNACKS -> SectionStyle("🍿", Color(0xFFFFF8E1), Color(0xFFB99A3B))
    StoreSection.BEVERAGES -> SectionStyle("🧃", Color(0xFFE1F5FE), Color(0xFF4A9BC4))
    StoreSection.CONDIMENTS_SAUCES -> SectionStyle("🍯", Color(0xFFF0F4C3), Color(0xFF8A9A2F))
    StoreSection.INTERNATIONAL -> SectionStyle("🌮", Color(0xFFFBE9E7), Color(0xFFC36F4F))
    StoreSection.PERSONAL_CARE -> SectionStyle("🧴", Color(0xFFEDE7F6), Color(0xFF7E6BB5))
    StoreSection.PHARMACY -> SectionStyle("💊", Color(0xFFE8EAF6), Color(0xFF5C6BC0))
    StoreSection.HOUSEHOLD -> SectionStyle("🧻", Color(0xFFECEFF1), Color(0xFF607D8B))
    StoreSection.PET -> SectionStyle("🐾", Color(0xFFEFEBE9), Color(0xFF8D6E63))
    StoreSection.OTHER -> SectionStyle("🛒", Color(0xFFF5F5F5), Color(0xFF757575))
}
