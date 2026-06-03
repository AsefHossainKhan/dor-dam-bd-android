@file:OptIn(ExperimentalMaterial3Api::class)

package com.asef.dordambdandroid.ui.screens.pricescreen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.asef.dordambdandroid.R
import com.asef.dordambdandroid.ui.components.AddFAB
import com.asef.dordambdandroid.ui.components.EditBottomSheet
import com.asef.dordambdandroid.ui.components.PullToRefreshLazyColumn
import com.asef.dordambdandroid.ui.theme.LocalExtendedTypography
import com.asef.dordambdandroid.util.formatDateTime
import com.asef.dordambdandroid.util.formatPrice

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PriceScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    itemId: Int,
    itemName: String,
) {
    val priceViewModel: PriceViewModel = hiltViewModel()
    val priceList by priceViewModel.priceList.collectAsState()
    val isLoading by priceViewModel.isLoading.collectAsState()
    LaunchedEffect(Unit) {
        priceViewModel.getPrices(itemId)
        priceViewModel.setItemId(itemId)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = itemName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            AddPriceFab(
                priceViewModel = priceViewModel,
                itemId = itemId
            )
        }) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PullToRefreshLazyColumn(
                items = priceList,
                isRefreshing = isLoading,
                onRefresh = {
                    priceViewModel.getPrices(itemId)
                },
                key = { item -> item.id },
                extraContent = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.price),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = stringResource(R.string.date_time),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                    }
                }
            ) { item ->
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, label = "scale")

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .scale(scale),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = {},
                                onLongClick = {
                                    priceViewModel.setPriceId(item.id)
                                    priceViewModel.setPrice(item.price)
                                    priceViewModel.openEditBottomSheet(true)
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val priceStyle = LocalExtendedTypography.current.price
                                Text(
                                    text = "৳",
                                    style = priceStyle,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 1.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = item.price.formatPrice(),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = priceStyle
                                )
                            }
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = formatDateTime(item.createdAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
            EditPriceBottomSheet(priceViewModel = priceViewModel)
        }
    }
}

@Composable
fun EditPriceBottomSheet(
    priceViewModel: PriceViewModel
) {
    val modalSheetVisibility by priceViewModel.editBottomSheetVisibility.collectAsState()
    val focusManager = LocalFocusManager.current
    val price by priceViewModel.price.collectAsState()
    val priceId by priceViewModel.priceId.collectAsState()
    EditBottomSheet(
        modalSheetVisibility,
        openSheet = { priceViewModel.openEditBottomSheet(true) },
        closeSheet = { priceViewModel.openEditBottomSheet(false) }
    ) {
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
        var priceText by rememberSaveable { mutableStateOf(price.formatPrice()) }
        val isValid = priceText.toFloatOrNull()?.let { it > 0f } == true
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.edit_price),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.update_the_price_entry),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    if (isValid) {
                        priceViewModel.editItem(priceId, priceText.toFloat())
                        priceViewModel.openEditBottomSheet(false)
                        focusManager.clearFocus()
                    }
                }),
                label = { Text(text = stringResource(R.string.price_bdt)) },
                prefix = { Text("৳ ") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (isValid) {
                        priceViewModel.editItem(priceId, priceText.toFloat())
                        priceViewModel.openEditBottomSheet(false)
                        focusManager.clearFocus()
                    }
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.update_price))
            }
        }
    }
}

@Composable
fun AddPriceFab(
    priceViewModel: PriceViewModel,
    itemId: Int
) {
    var modalSheetVisibility by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    AddFAB(
        modalSheetVisibility,
        openSheet = { modalSheetVisibility = true },
        closeSheet = { modalSheetVisibility = false }
    ) {
        var price by rememberSaveable { mutableStateOf("") }
        val isValid = price.toFloatOrNull()?.let { it > 0f } == true
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.add_price),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.enter_today_s_price),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    if (isValid) {
                        priceViewModel.addPrice(itemId = itemId, price = price.toFloat())
                        modalSheetVisibility = false
                        focusManager.clearFocus()
                    }
                }),
                label = { Text(text = stringResource(R.string.price_bdt)) },
                prefix = { Text("৳ ") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (isValid) {
                        priceViewModel.addPrice(itemId = itemId, price = price.toFloat())
                        modalSheetVisibility = false
                        focusManager.clearFocus()
                    }
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.add_price))
            }
        }
    }
}