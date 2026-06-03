@file:OptIn(ExperimentalMaterial3Api::class)

package com.asef.dordambdandroid.ui.screens.homescreen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.asef.dordambdandroid.R
import com.asef.dordambdandroid.data.remote.models.items.createitem.CreateItem
import com.asef.dordambdandroid.ui.components.AddFAB
import com.asef.dordambdandroid.ui.components.EditBottomSheet
import com.asef.dordambdandroid.ui.components.PullToRefreshLazyColumn
import com.asef.dordambdandroid.ui.screens.Screen
import com.asef.dordambdandroid.ui.theme.LocalExtendedColors
import com.asef.dordambdandroid.ui.theme.LocalExtendedTypography
import com.asef.dordambdandroid.util.formatPrice
import com.asef.dordambdandroid.util.relativeTime

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController, modifier: Modifier = Modifier
) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val itemList by homeViewModel.itemList.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val searchText by homeViewModel.searchText.collectAsState()
    val itemPriceInfo by homeViewModel.itemPriceInfo.collectAsState()

    LaunchedEffect(key1 = Unit) {
        homeViewModel.getItems()
    }

    Scaffold(
        floatingActionButton = { AddItemFab(homeViewModel) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Dor Dam BD",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = Color.Unspecified
                )
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PullToRefreshLazyColumn(items = itemList,
                isRefreshing = isLoading,
                onRefresh = {
                    homeViewModel.getItems()
                },
                key = { it.id },
                extraContent = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextField(
                            value = searchText,
                            onValueChange = { homeViewModel.changeSearchText(it) },
                            placeholder = {
                                Text(
                                    stringResource(R.string.search_items),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                )
                        )
                    }
                }
            ) { item ->
                val info = itemPriceInfo[item.id]
                val latestPrice = info?.latestPrice
                val trend = info?.trend ?: PriceTrend.UNKNOWN
                val timeAgo = remember(latestPrice?.createdAt) {
                    latestPrice?.createdAt?.let { relativeTime(it) } ?: ""
                }

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
                                onClick = {
                                    navController.navigate(
                                        Screen.PriceScreen.withArgs(
                                            item.id.toString(), item.name
                                        )
                                    )
                                },
                                onLongClick = {
                                    homeViewModel.setItemText(item.name)
                                    homeViewModel.setItemId(item.id)
                                    homeViewModel.openEditBottomSheet(true)
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: item name + last updated time
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            if (timeAgo.isNotEmpty()) {
                                Text(
                                    text = timeAgo,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Right: Trend Arrow + Price Column
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val extendedColors = LocalExtendedColors.current
                            when (trend) {
                                PriceTrend.UP -> Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = stringResource(R.string.price_increased),
                                    tint = extendedColors.trendUp,
                                    modifier = Modifier.size(20.dp)
                                )
                                PriceTrend.DOWN -> Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = stringResource(R.string.price_decreased),
                                    tint = extendedColors.trendDown,
                                    modifier = Modifier.size(20.dp)
                                )
                                else -> Spacer(modifier = Modifier.size(20.dp))
                            }

                            val priceInteractionSource = remember { MutableInteractionSource() }
                            val isPricePressed by priceInteractionSource.collectIsPressedAsState()
                            val priceScale by animateFloatAsState(if (isPricePressed) 0.95f else 1f, label = "priceScale")

                            Box(
                                modifier = Modifier
                                    .width(75.dp)
                                    .scale(priceScale)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable(
                                        interactionSource = priceInteractionSource,
                                        indication = null
                                    ) {
                                        homeViewModel.openQuickAddSheet(item.id, item.name)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                val priceStyle = LocalExtendedTypography.current.price
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Text(
                                        text = "৳",
                                        style = priceStyle,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 2.dp) // Fine-tune baseline
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Text(
                                        text = latestPrice?.price?.formatPrice() ?: stringResource(R.string.add),
                                        style = priceStyle,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
            EditItemBottomSheet(homeViewModel = homeViewModel)
            QuickAddPriceFromHome(homeViewModel = homeViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddPriceFromHome(homeViewModel: HomeViewModel) {
    val visible by homeViewModel.quickAddSheetVisible.collectAsState()
    val itemId by homeViewModel.quickAddItemId.collectAsState()
    val itemName by homeViewModel.quickAddItemName.collectAsState()
    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (visible) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { homeViewModel.closeQuickAddSheet() },
            modifier = Modifier.imePadding()
        ) {
            var priceText by rememberSaveable { mutableStateOf("") }
            val isValid = priceText.toFloatOrNull()?.let { it > 0f } == true
            val focusRequester = remember { FocusRequester() }

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = itemName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.enter_today_s_price),
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
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isValid) {
                                homeViewModel.addPrice(itemId, priceText.toFloat())
                                homeViewModel.closeQuickAddSheet()
                                focusManager.clearFocus()
                            }
                        }
                    ),
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
                            homeViewModel.addPrice(itemId, priceText.toFloat())
                            homeViewModel.closeQuickAddSheet()
                            focusManager.clearFocus()
                        }
                    },
                    enabled = isValid,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.submit_price))
                }
            }
        }
    }
}

@Composable
fun EditItemBottomSheet(
    homeViewModel: HomeViewModel
) {
    val modalSheetVisibility by homeViewModel.editBottomSheetVisibility.collectAsState()
    val focusManager = LocalFocusManager.current
    val itemName by homeViewModel.itemText.collectAsState()
    val itemId by homeViewModel.itemId.collectAsState()
    EditBottomSheet(
        modalSheetVisibility,
        openSheet = { homeViewModel.openEditBottomSheet(true) },
        closeSheet = { homeViewModel.openEditBottomSheet(false) }
    ) {
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
                text = stringResource(R.string.edit_item),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.update_the_item_name),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = itemName,
                onValueChange = { homeViewModel.setItemText(it) },
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    homeViewModel.editItem(itemId, itemName)
                    homeViewModel.openEditBottomSheet(false)
                    focusManager.clearFocus()
                }),
                label = { Text(text = stringResource(R.string.item_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    homeViewModel.editItem(itemId, itemName)
                    homeViewModel.openEditBottomSheet(false)
                    focusManager.clearFocus()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.update_item))
            }
        }
    }
}

@Composable
fun AddItemFab(
    homeViewModel: HomeViewModel
) {
    var modalSheetVisibility by rememberSaveable {
        mutableStateOf(false)
    }
    val focusManager = LocalFocusManager.current
    AddFAB(
        modalSheetVisibility,
        openSheet = { modalSheetVisibility = true },
        closeSheet = { modalSheetVisibility = false }
    ) {
        var itemName by rememberSaveable { mutableStateOf("") }
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
                text = stringResource(R.string.add_item),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.enter_a_new_item_name),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = itemName,
                onValueChange = { itemName = it },
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (itemName.isNotBlank()) {
                        homeViewModel.postItem(CreateItem(name = itemName))
                        modalSheetVisibility = false
                        focusManager.clearFocus()
                    }
                }),
                label = { Text(text = stringResource(R.string.item_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    homeViewModel.postItem(CreateItem(name = itemName))
                    modalSheetVisibility = false
                    focusManager.clearFocus()
                },
                enabled = itemName.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.create_item))
            }
        }
    }
}
