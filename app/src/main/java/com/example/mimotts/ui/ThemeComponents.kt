package com.example.mimotts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mimotts.data.AppColors

fun Modifier.noRippleClick(onClick: () -> Unit) = this.then(
    Modifier.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
    )
)

@Composable
fun Chip(label: String, selected: Boolean = false, onClick: () -> Unit = {}) {
    Text(
        text = label,
        fontSize = 12.sp,
        color = if (selected) AppColors.Bg else AppColors.TextMuted,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) AppColors.Accent else AppColors.Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    )
}

@Composable
fun SectionTitle(text: String, extra: @Composable RowScope.() -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppColors.Text)
        extra()
    }
}

@Composable
fun AccentLine() {
    Box(
        Modifier.padding(horizontal = 20.dp).fillMaxWidth().height(1.dp)
            .background(Brush.horizontalGradient(listOf(AppColors.Accent.copy(alpha = 0.4f), Color.Transparent)))
    )
}
