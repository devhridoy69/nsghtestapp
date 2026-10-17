package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.SyncStatus
import com.example.ui.theme.HospitalNavy
import com.example.ui.theme.HospitalNavyLight
import com.example.ui.theme.HospitalTeal
import com.example.ui.theme.MedicalRed
import com.example.ui.viewmodel.AppScreen

@Composable
fun HospitalHeader(
    isOnline: Boolean,
    syncStatus: SyncStatus,
    currentScreen: AppScreen = AppScreen.TESTS,
    onTabSelected: (AppScreen) -> Unit = {},
    onSyncClick: () -> Unit,
    onOpenMoreMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSyncing = syncStatus is SyncStatus.Syncing

    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
        color = HospitalNavy,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            HospitalNavy,
                            HospitalNavyLight
                        )
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column {
                // Top Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Hospital Logo & Name Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Branding Logo
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            modifier = Modifier
                                .size(50.dp)
                                .shadow(4.dp, RoundedCornerShape(12.dp))
                                .border(1.5.dp, HospitalTeal.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.nsgh_logo),
                                contentDescription = "NSGH Logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .padding(3.dp)
                                    .testTag("hospital_logo_image")
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            // "NSGH" in Bold Vibrant Red, "HOSPITAL" deleted as requested!
                            Text(
                                text = "NSGH",
                                color = Color(0xFFFF2E4D),
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "New Shafipur General Hospital",
                                color = Color.White.copy(alpha = 0.95f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Shafipur, Kaliakair, Gazipur",
                                color = Color(0xFFB0D5FF),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Action Icons (Sync & Three Dot More Options)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Sync Button
                        IconButton(
                            onClick = onSyncClick,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .testTag("sync_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(if (isSyncing) angle else 0f)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Animated Three Dot More Options Button
                        val moreInteractionSource = remember { MutableInteractionSource() }
                        val isMorePressed by moreInteractionSource.collectIsPressedAsState()
                        val moreScale by animateFloatAsState(
                            targetValue = if (isMorePressed) 0.82f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                            label = "more_scale"
                        )
                        val moreRotation by animateFloatAsState(
                            targetValue = if (isMorePressed) 90f else 0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                            label = "more_rot"
                        )

                        IconButton(
                            onClick = onOpenMoreMenu,
                            interactionSource = moreInteractionSource,
                            modifier = Modifier
                                .size(38.dp)
                                .scale(moreScale)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f))
                                .testTag("more_options_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(22.dp)
                                    .rotate(moreRotation)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Modern Navigation Pill Tabs (Tests vs Doctors)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.28f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Tests Tab
                    HeaderTabItem(
                        title = "Diagnostic Tests",
                        icon = Icons.Default.Science,
                        isSelected = currentScreen == AppScreen.TESTS,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabSelected(AppScreen.TESTS) }
                    )

                    // Doctors Tab
                    HeaderTabItem(
                        title = "Doctor List",
                        icon = Icons.Default.People,
                        isSelected = currentScreen == AppScreen.DOCTORS,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabSelected(AppScreen.DOCTORS) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Network & Online/Offline Auto-Sync Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.20f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = if (isOnline) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }
                        ) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = if (isOnline) "Cloud Connected" else "Offline Mode",
                                tint = if (isOnline) Color(0xFF10B981) else Color(0xFFFCA5A5),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (isOnline) "Online • Auto Sync Active" else "Offline Mode • Local Cache",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    when (syncStatus) {
                        is SyncStatus.Syncing -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = HospitalTeal,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Updating...",
                                    fontSize = 11.sp,
                                    color = HospitalTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        is SyncStatus.Success -> {
                            Text(
                                text = "Synced",
                                fontSize = 11.sp,
                                color = Color(0xFF86EFAC),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        is SyncStatus.Error -> {
                            Text(
                                text = "Sync error",
                                fontSize = 11.sp,
                                color = Color(0xFFFCA5A5)
                            )
                        }
                        else -> {
                            Text(
                                text = "Ready",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderTabItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.Transparent,
        label = "tab_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) HospitalNavy else Color.White.copy(alpha = 0.85f),
        label = "tab_text"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}
