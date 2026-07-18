package com.appresenze.presenze.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Dashed rounded-rect border, used for the "no events yet" placeholder card. */
fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp = 16.dp,
    strokeWidth: Dp = 1.dp,
): Modifier = this.drawBehind {
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
    )
}
