package com.asef.dordambdandroid.ui.components.location

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.asef.dordambdandroid.R
import com.asef.dordambdandroid.data.remote.models.places.PlaceView
import com.asef.dordambdandroid.location.ChosenLocation

/**
 * The optional location chip under the price field.
 *
 * Empty: up to three suggestions (add sheet only) plus "Type a place" and "Pick on map".
 * Set: the path fine to coarse; tapping a coarser level trims to it, ✕ clears it.
 *
 * SPEC: spec-5f5a30, spec-14240b, spec-628f81
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LocationChip(
    location: ChosenLocation?,
    onTrim: (Int) -> Unit,
    onClear: () -> Unit,
    onTypePlace: () -> Unit,
    onPickOnMap: () -> Unit,
    modifier: Modifier = Modifier,
    suggestions: List<PlaceView> = emptyList(),
    loadingSuggestions: Boolean = false,
    onSuggestion: (PlaceView) -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.location_optional),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (loadingSuggestions && location == null) {
                Spacer(Modifier.width(8.dp))
                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
            }
        }
        Spacer(Modifier.padding(top = 4.dp))

        if (location != null) {
            SelectedLocation(location = location, onTrim = onTrim, onClear = onClear)
            Row {
                TextButton(onClick = onTypePlace) { Text(stringResource(R.string.type_a_place)) }
                TextButton(onClick = onPickOnMap) { Text(stringResource(R.string.pick_on_map)) }
            }
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                suggestions.forEach { place ->
                    SuggestionChip(
                        onClick = { onSuggestion(place) },
                        label = {
                            Text(
                                text = place.label ?: place.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        icon = { Icon(Icons.Default.Place, contentDescription = null, Modifier.size(16.dp)) }
                    )
                }
                AssistChip(
                    onClick = onTypePlace,
                    label = { Text(stringResource(R.string.type_a_place)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, Modifier.size(16.dp)) }
                )
                AssistChip(
                    onClick = onPickOnMap,
                    label = { Text(stringResource(R.string.pick_on_map)) },
                    leadingIcon = { Icon(Icons.Default.Map, contentDescription = null, Modifier.size(16.dp)) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectedLocation(
    location: ChosenLocation,
    onTrim: (Int) -> Unit,
    onClear: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Icon(Icons.Default.Place, contentDescription = null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            FlowRow(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                if (location.path.isEmpty()) {
                    Text(
                        text = stringResource(R.string.pinned_location),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                location.path.forEachIndexed { index, step ->
                    if (index > 0) {
                        Text(
                            text = " › ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = step.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (index == 0) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (index == 0) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.primary,
                        modifier = if (index == 0) Modifier
                        else Modifier.clickable(onClickLabel = stringResource(R.string.keep_only, step.name)) {
                            onTrim(index)
                        }
                    )
                }
            }
            IconButton(onClick = onClear) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_location))
            }
        }
    }
    if (location.path.size > 1) {
        Text(
            text = stringResource(R.string.tap_coarser_level_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
