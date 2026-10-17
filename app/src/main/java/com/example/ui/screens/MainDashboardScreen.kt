package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SyncStatus
import com.example.ui.components.AppSideDrawer
import com.example.ui.components.CreditsDialog
import com.example.ui.components.HospitalHeader
import com.example.ui.components.RollingNumberText
import com.example.ui.components.SelectedTestsBottomSheet
import com.example.ui.components.SlidingMarqueeText
import com.example.ui.components.TestItemCard
import com.example.ui.components.TestItemSkeleton
import com.example.ui.theme.HospitalNavy
import com.example.ui.theme.HospitalTeal
import com.example.ui.theme.MedicalRed
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun MainDashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val showSideDrawer by viewModel.showSideDrawer.collectAsState()

    val tests by viewModel.tests.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTestsMap by viewModel.selectedTests.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val noticeText by viewModel.noticeText.collectAsState()
    val showEstimateSheet by viewModel.showEstimateSheet.collectAsState()
    val showCreditsDialog by viewModel.showCreditsDialog.collectAsState()
    val discountPercentage by viewModel.discountPercentage.collectAsState()

    val haptic = LocalHapticFeedback.current
    var isSearchFocused by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 24 }
    }

    val headerElevation by animateDpAsState(
        targetValue = if (isScrolled) 8.dp else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "header_elevation"
    )

    val selectedCount = selectedTestsMap.size
    val subtotal = selectedTestsMap.values.sumOf { it.price }
    val discountAmount = subtotal * (discountPercentage / 100.0)
    val netTotal = subtotal - discountAmount

    // Back button handling
    BackHandler(enabled = showSideDrawer || currentScreen == AppScreen.DOCTORS) {
        if (showSideDrawer) {
            viewModel.setSideDrawerVisible(false)
        } else if (currentScreen == AppScreen.DOCTORS) {
            viewModel.setScreen(AppScreen.TESTS)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Fluid Tab & Screen Switching Animation
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (targetState == AppScreen.DOCTORS) {
                    (slideInHorizontally(
                        initialOffsetX = { width -> width },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(tween(220))).togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { width -> -width / 3 },
                            animationSpec = tween(220)
                        ) + fadeOut(tween(180))
                    )
                } else {
                    (slideInHorizontally(
                        initialOffsetX = { width -> -width / 3 },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(tween(220))).togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { width -> width },
                            animationSpec = tween(220)
                        ) + fadeOut(tween(180))
                    )
                }
            },
            label = "screen_switch_anim"
        ) { screen ->
            when (screen) {
                AppScreen.DOCTORS -> {
                    DoctorListScreen(viewModel = viewModel)
                }
                AppScreen.TESTS -> {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        topBar = {
                            Surface(
                                shadowElevation = headerElevation,
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                HospitalHeader(
                                    isOnline = isOnline,
                                    syncStatus = syncStatus,
                                    currentScreen = currentScreen,
                                    onTabSelected = { viewModel.setScreen(it) },
                                    onSyncClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.manualSync()
                                    },
                                    onOpenMoreMenu = { viewModel.setSideDrawerVisible(true) }
                                )
                            }
                        },
                        bottomBar = {
                            AnimatedVisibility(
                                visible = selectedCount > 0,
                                enter = slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) + fadeIn(),
                                exit = slideOutVertically(
                                    targetOffsetY = { it },
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)
                                ) + fadeOut()
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(20.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                        .testTag("floating_total_bar"),
                                    color = HospitalNavy,
                                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .navigationBarsPadding()
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        // Quick Special Discount Selector with tactile bounce
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Discount:",
                                                color = Color.White.copy(alpha = 0.85f),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            listOf(0.0, 10.0, 20.0, 25.0, 30.0).forEach { pct ->
                                                val isSelected = discountPercentage == pct
                                                val pillScale by animateFloatAsState(
                                                    targetValue = if (isSelected) 1.08f else 1f,
                                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                                    label = "pill_scale"
                                                )
                                                val pillBg by animateColorAsState(
                                                    targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.16f),
                                                    animationSpec = tween(150),
                                                    label = "pill_bg"
                                                )
                                                val pillTextColor by animateColorAsState(
                                                    targetValue = if (isSelected) HospitalNavy else Color.White,
                                                    animationSpec = tween(150),
                                                    label = "pill_text"
                                                )

                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .scale(pillScale)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(pillBg)
                                                        .clickable(
                                                            interactionSource = remember { MutableInteractionSource() },
                                                            indication = null
                                                        ) {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            viewModel.setDiscount(pct)
                                                        }
                                                        .padding(vertical = 5.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = if (pct == 0.0) "0%" else "${pct.toInt()}%",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = pillTextColor
                                                    )
                                                }
                                            }
                                        }

                                        // Total Price and Rolling Digits
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(CircleShape)
                                                            .background(MedicalRed)
                                                            .clickable {
                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                viewModel.clearAllSelections()
                                                            }
                                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = "$selectedCount Selected",
                                                                color = Color.White,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Icon(
                                                                imageVector = Icons.Default.Clear,
                                                                contentDescription = "Clear all",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Estimated Net:",
                                                        color = Color.White.copy(alpha = 0.85f),
                                                        fontSize = 12.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(verticalAlignment = Alignment.Bottom) {
                                                    Text(
                                                        text = "৳",
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(bottom = 2.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    RollingNumberText(
                                                        number = netTotal.toInt(),
                                                        color = Color.White,
                                                        fontSize = 24.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                    if (discountPercentage > 0) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "(${discountPercentage.toInt()}% off)",
                                                            fontSize = 11.sp,
                                                            color = Color(0xFF86EFAC)
                                                        )
                                                    }
                                                }
                                            }

                                            Button(
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    viewModel.setEstimateSheetVisible(true)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.testTag("view_estimate_button")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Receipt,
                                                    contentDescription = null,
                                                    tint = HospitalNavy,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "View Total",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = HospitalNavy
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    ) { paddingValues ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            // 1. Sliding Marquee Notice Banner (Auto-Synced from Supabase)
                            SlidingMarqueeText(
                                text = noticeText,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 10.dp, bottom = 4.dp)
                                    .testTag("dashboard_sliding_text")
                            )

                            // 2. Search & Sort Bar Row with fluid focus animation
                            val searchBorderColor by animateColorAsState(
                                targetValue = if (isSearchFocused) HospitalNavy else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                animationSpec = tween(200),
                                label = "search_border"
                            )
                            val searchElevation by animateDpAsState(
                                targetValue = if (isSearchFocused) 4.dp else 0.dp,
                                animationSpec = tween(200),
                                label = "search_elevation"
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    shadowElevation = searchElevation,
                                    color = Color.White
                                ) {
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { viewModel.onSearchQueryChange(it) },
                                        placeholder = { Text("Search test name...", fontSize = 13.sp) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Search",
                                                tint = if (isSearchFocused) HospitalNavy else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        trailingIcon = {
                                            AnimatedVisibility(
                                                visible = searchQuery.isNotEmpty(),
                                                enter = fadeIn() + scaleIn(),
                                                exit = fadeOut() + scaleOut()
                                            ) {
                                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Clear,
                                                        contentDescription = "Clear search",
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedBorderColor = searchBorderColor,
                                            unfocusedBorderColor = searchBorderColor
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onFocusChanged { isSearchFocused = it.isFocused }
                                            .testTag("search_text_input")
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Sort Dropdown Button
                                Box {
                                    IconButton(
                                        onClick = { showSortMenu = true },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color.White)
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                                RoundedCornerShape(14.dp)
                                            )
                                            .testTag("sort_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Sort,
                                            contentDescription = "Sort tests",
                                            tint = HospitalNavy,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showSortMenu,
                                        onDismissRequest = { showSortMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Name: A to Z") },
                                            onClick = {
                                                viewModel.onSortByChange("name_asc")
                                                showSortMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Price: Low to High") },
                                            onClick = {
                                                viewModel.onSortByChange("price_asc")
                                                showSortMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Price: High to Low") },
                                            onClick = {
                                                viewModel.onSortByChange("price_desc")
                                                showSortMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Results count
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${tests.size} Hospital Tests",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (selectedCount > 0) {
                                    Surface(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.clearAllSelections()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = MedicalRed.copy(alpha = 0.10f),
                                        border = BorderStroke(1.dp, MedicalRed.copy(alpha = 0.35f)),
                                        modifier = Modifier.testTag("unselect_all_header_button")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ClearAll,
                                                contentDescription = "Unselect All",
                                                tint = MedicalRed,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Unselect All ($selectedCount)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MedicalRed
                                            )
                                        }
                                    }
                                }
                            }

                            // Fluid LazyColumn with Shimmer Loading
                            if (tests.isEmpty()) {
                                if (syncStatus is SyncStatus.Syncing) {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp, vertical = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        contentPadding = PaddingValues(bottom = 140.dp)
                                    ) {
                                        items(8) {
                                            TestItemSkeleton()
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "No tests match your search.",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                                Text("Reset Search")
                                            }
                                        }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("tests_lazy_list"),
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 140.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(tests, key = { it.effectiveId }) { test ->
                                        // CRITICAL: Read isSelected directly from selectedTestsMap state so Compose immediately recomposes!
                                        val isSelected = test.effectiveId in selectedTestsMap
                                        Box(modifier = Modifier.animateItem()) {
                                            TestItemCard(
                                                test = test,
                                                isSelected = isSelected,
                                                onToggleSelect = { viewModel.toggleTestSelection(test) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Slide-in Side Drawer (opens from three-dot menu)
        AppSideDrawer(
            isOpen = showSideDrawer,
            currentScreen = currentScreen,
            onNavigate = { viewModel.setScreen(it) },
            onOpenCredits = { viewModel.setCreditsDialogVisible(true) },
            onClose = { viewModel.setSideDrawerVisible(false) }
        )
    }

    // Selected Tests Bottom Sheet
    if (showEstimateSheet) {
        SelectedTestsBottomSheet(
            selectedTests = selectedTestsMap.values.toList(),
            discountPercentage = discountPercentage,
            onDiscountChange = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.setDiscount(it)
            },
            onRemoveTest = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.removeTestFromSelection(it)
            },
            onClearAll = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.clearAllSelections()
            },
            onDismiss = { viewModel.setEstimateSheetVisible(false) },
            generateShareText = { viewModel.generateEstimateSummaryText() }
        )
    }

    // App & Hospital Credits Dialog
    if (showCreditsDialog) {
        CreditsDialog(
            onDismiss = { viewModel.setCreditsDialogVisible(false) }
        )
    }
}
