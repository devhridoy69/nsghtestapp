package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiagnosticTest
import com.example.ui.theme.HospitalNavy
import com.example.ui.theme.HospitalTeal

@Composable
fun TestItemCard(
    test: DiagnosticTest,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 1. Tactile spring scale physics on tap/press
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "press_scale"
    )

    // 2. Fluid color & border transitions
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) HospitalNavy else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
        animationSpec = tween(durationMillis = 200),
        label = "border_color"
    )

    val cardBgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFF0F7FF) else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 200),
        label = "card_bg"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 1.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "border_width"
    )

    val cardElevation by animateDpAsState(
        targetValue = if (isSelected) 4.dp else 1.dp,
        animationSpec = tween(180),
        label = "elevation"
    )

    // 3. Checkmark pop bounce
    val buttonScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_scale"
    )

    val buttonBgColor by animateColorAsState(
        targetValue = if (isSelected) HospitalNavy else HospitalNavy.copy(alpha = 0.08f),
        animationSpec = tween(180),
        label = "btn_bg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(pressScale)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleSelect()
            }
            .testTag("test_card_${test.effectiveId}"),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Test Name
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = test.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 21.sp
                )
            }

            // Test Price & Select Toggle Button
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "৳",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = HospitalNavy,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${test.price.toInt()}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = HospitalNavy
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Select / Selected Tactile Button with Spring Bounce
                Box(
                    modifier = Modifier
                        .scale(buttonScale)
                        .clip(RoundedCornerShape(8.dp))
                        .background(buttonBgColor)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("select_toggle_${test.effectiveId}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = if (isSelected) "Selected" else "Add",
                            tint = if (isSelected) Color.White else HospitalNavy,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSelected) "SELECTED" else "SELECT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else HospitalNavy,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
