package com.appresenze.presenze.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appresenze.presenze.ui.theme.Accent
import com.appresenze.presenze.ui.theme.DashedBorder
import com.appresenze.presenze.ui.theme.ScrimColor
import com.appresenze.presenze.ui.theme.TextPrimary
import com.appresenze.presenze.ui.theme.TextSecondary55

/**
 * Bottom-anchored confirmation prompt, equivalent to the `isConfirming` overlay
 * in the design (tap the scrim to cancel, sheet itself absorbs taps).
 */
@Composable
fun ConfirmSheet(
    actionLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ScrimColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCancel,
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(Color.White)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 28.dp)
        ) {
            Text(
                text = "Confermi la $actionLabel?",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Text(
                text = "Verrà registrato l'orario attuale.",
                fontSize = 13.sp,
                color = TextSecondary55,
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.dp, DashedBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                ) {
                    Text("Annulla", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                ) {
                    Text("Conferma", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}
