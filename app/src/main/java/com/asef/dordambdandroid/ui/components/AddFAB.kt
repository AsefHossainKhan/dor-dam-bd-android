package com.asef.dordambdandroid.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFAB(
    modalSheetVisibility: Boolean,
    modifier: Modifier = Modifier,
    openSheet: () -> Unit,
    closeSheet: () -> Unit,
    content: @Composable () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    FloatingActionButton(
        modifier = modifier,
        onClick = {
            openSheet()
        }
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add a Record")
    }
    if (modalSheetVisibility) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                closeSheet()
            },
            modifier = Modifier.imePadding()
        ) {
            Column {
                content()
            }
        }
    }

}