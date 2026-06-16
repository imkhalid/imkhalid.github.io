package com.khalid.vyntra.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Filter bar: two compact chips ("Filters", "Sort") in a row. Pattern shamelessly
 * borrowed from Linear/Shopify — scales to lots of filter dimensions without
 * cluttering the screen. The filter sheet is owned by the caller; this just
 * exposes the click callbacks and the values to render on the chips.
 *
 * @param activeFilterCount When > 0 the "Filters" chip shows a small badge.
 * @param sortLabel         Current sort label rendered after "Sort · ". Pass
 *                          null to hide the sort chip.
 */
@Composable
fun VyntraFilterBar(
    activeFilterCount: Int,
    onFiltersClick: () -> Unit,
    modifier: Modifier = Modifier,
    sortLabel: String? = null,
    onSortClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChipPill(
            icon = Icons.Filled.Tune,
            label = "Filters",
            badgeCount = activeFilterCount,
            highlighted = activeFilterCount > 0,
            onClick = onFiltersClick
        )
        if (sortLabel != null && onSortClick != null) {
            FilterChipPill(
                icon = Icons.Filled.Sort,
                label = "Sort · $sortLabel",
                highlighted = false,
                onClick = onSortClick
            )
        }
    }
}

@Composable
private fun FilterChipPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    highlighted: Boolean,
    badgeCount: Int = 0
) {
    val container = if (highlighted) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerHigh
    val foreground = if (highlighted) MaterialTheme.colorScheme.onPrimaryContainer
                     else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier
            .heightIn(min = 36.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = container
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = foreground,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = foreground,
                fontWeight = FontWeight.Medium
            )
            if (badgeCount > 0) {
                Spacer(Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
