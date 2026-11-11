package com.asef.dordambdandroid.ui.components.location

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.asef.dordambdandroid.R
import com.asef.dordambdandroid.data.remote.models.places.PlaceView

/**
 * "Type a place": searches shared places as the user types; when none is
 * the typed name, offers to add it as a new shared place.
 *
 * SPEC: spec-14240b
 */
@Composable
fun PlaceSearchDialog(
    query: String,
    results: List<PlaceView>,
    loading: Boolean,
    error: String?,
    onQueryChange: (String) -> Unit,
    onPick: (PlaceView) -> Unit,
    onCreate: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.imePadding()) {
                val focusRequester = remember { FocusRequester() }
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
                val trimmed = query.trim()
                val canCreate = trimmed.isNotEmpty() && !loading &&
                    results.none { it.name.equals(trimmed, ignoreCase = true) }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                    OutlinedTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.search_places_hint)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            results.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
                                ?.let(onPick)
                        }),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                    )
                }
                if (loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                LazyColumn {
                    items(results, key = { it.id }) { place ->
                        ListItem(
                            headlineContent = { Text(place.name) },
                            supportingContent = place.label
                                ?.takeIf { it != place.name }
                                ?.let { label -> { Text(label) } },
                            leadingContent = { Icon(Icons.Default.Place, contentDescription = null) },
                            modifier = Modifier.clickable { onPick(place) }
                        )
                        HorizontalDivider()
                    }
                    if (canCreate) {
                        item(key = "create") {
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.add_new_place, trimmed)) },
                                supportingContent = { Text(stringResource(R.string.add_new_place_hint)) },
                                leadingContent = {
                                    Icon(Icons.Default.AddLocationAlt, contentDescription = null)
                                },
                                modifier = Modifier.clickable { onCreate(trimmed) }
                            )
                        }
                    }
                }
            }
        }
    }
}
