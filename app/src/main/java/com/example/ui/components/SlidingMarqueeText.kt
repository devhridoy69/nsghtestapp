package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberLight
import com.example.ui.theme.MedicalRed

@Composable
fun SlidingMarqueeText(
    text: String = "Please Confirm The Final Price From The Counter.. 01711902062",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AccentAmberLight.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = AccentAmber.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Notice Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MedicalRed)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Notice",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = " NOTICE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Marquee Container
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
                    .clipToBounds()
            ) {
                val containerWidth = maxWidth
                val density = LocalDensity.current
                val containerWidthPx = with(density) { containerWidth.toPx() }
                
                // Text width estimation or full traversal offset
                val travelDistance = containerWidthPx + 900f

                val infiniteTransition = rememberInfiniteTransition(label = "marquee")
                val animatedOffset by infiniteTransition.animateFloat(
                    initialValue = containerWidthPx,
                    targetValue = -900f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 11000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "offset"
                )

                Text(
                    text = "📢  $text    •    $text",
                    color = Color(0xFF78350F),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.offset { IntOffset(animatedOffset.toInt(), 0) }
                )
            }
        }
    }
}
