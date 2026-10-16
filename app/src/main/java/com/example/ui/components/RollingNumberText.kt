package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * 60fps Rolling number digit ticker for smooth financial calculations.
 */
@Composable
fun RollingNumberText(
    number: Int,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: TextUnit = 24.sp,
    fontWeight: FontWeight = FontWeight.Black
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        val numString = number.toString()
        for (i in numString.indices) {
            val char = numString[i]
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInVertically { height -> height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> -height } + fadeOut()
                        )
                    } else {
                        (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> height } + fadeOut()
                        )
                    }.using(
                        SizeTransform(clip = false)
                    )
                },
                label = "digit_$i"
            ) { targetChar ->
                Text(
                    text = targetChar.toString(),
                    color = color,
                    fontSize = fontSize,
                    fontWeight = fontWeight
                )
            }
        }
    }
}
