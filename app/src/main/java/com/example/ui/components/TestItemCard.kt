package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) HospitalNavy else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        label = "border_color"
    )

    val cardBgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFF0F7FF) else MaterialTheme.colorScheme.surface,
        label = "card_bg"
    )

    val checkScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.92f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onToggleSelect() }
            .testTag("test_card_${test.effectiveId}"),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Test Name (Left column)
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
                    lineHeight = 20.sp
                )
            }

            // Test Price & Select Toggle (Right column)
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

                // Select / Selected Button
                Box(
                    modifier = Modifier
                        .scale(checkScale)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) HospitalNavy else HospitalNavy.copy(alpha = 0.08f))
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
