package com.udistrital.hockeygame.views.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udistrital.hockeygame.views.theme.*

enum class PowerType(val displayName: String, val icon: ImageVector) {
    WILDCARD("Comodín", Icons.Default.AutoAwesome),
    SKIP("Salto", Icons.Default.SkipNext),
    SWAP("Intercambio", Icons.AutoMirrored.Filled.CompareArrows),
    INVERT("Inversión", Icons.AutoMirrored.Filled.Redo)
}

@Composable
fun PowerBar(
    activePower: PowerType?,
    onPowerSelected: (PowerType) -> Unit,
    remainingSkipPowers: Int = 1,
    isMyTurn: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CasinoEmerald),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PODERES ESPECIALES",
                color = CasinoGoldLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PowerType.entries.forEach { power ->
                    val isSelected = activePower == power
                    val isEnabled = isMyTurn && (power != PowerType.SKIP || remainingSkipPowers > 0)
                    val uses = if (power == PowerType.SKIP) remainingSkipPowers else 1

                    PowerCard(
                        power = power,
                        isSelected = isSelected,
                        isEnabled = isEnabled,
                        usesLeft = uses,
                        onClick = { if (isEnabled) onPowerSelected(power) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PowerCard(
    power: PowerType,
    isSelected: Boolean,
    isEnabled: Boolean,
    usesLeft: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val bgColor = when {
        isSelected -> CasinoGold
        isEnabled -> CasinoDarkFelt
        else -> CasinoDarkFelt.copy(alpha = 0.4f)
    }
    val contentColor = when {
        isSelected -> CasinoDarkFelt
        isEnabled -> CasinoGold
        else -> CasinoTextMuted.copy(alpha = 0.4f)
    }

    Surface(
        modifier = modifier
            .padding(horizontal = 3.dp)
            .height(56.dp)
            .clip(shape)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) CasinoGoldLight else CasinoGold.copy(alpha = 0.3f),
                shape = shape
            )
            .then(if (isEnabled) Modifier.clickable { onClick() } else Modifier),
        color = bgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = power.icon,
                contentDescription = power.displayName,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = power.displayName,
                color = contentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "($usesLeft)",
                color = contentColor.copy(alpha = 0.8f),
                fontSize = 9.sp
            )
        }
    }
}
