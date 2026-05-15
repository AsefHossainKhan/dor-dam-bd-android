package com.asef.dordambdandroid.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> PullToRefreshLazyColumn(
    items: List<T>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    lazyListState: LazyListState = rememberLazyListState(),
    extraContent: (@Composable () -> Unit) = {},
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    key: (T) -> Any,
    content: @Composable (T) -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
    ) {
        Column {
            extraContent()
            LazyColumn(
                state = lazyListState,
                contentPadding = contentPadding,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = verticalArrangement
            ) {
                items(
                    items = items,
                    key = key
                ) {
                    content(it)
                }
            }
        }
    }
}
