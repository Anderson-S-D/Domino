package com.udistrital.hockeygame.views.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udistrital.hockeygame.domain.models.Tile
import com.udistrital.hockeygame.views.theme.*

@Composable
fun HandView(
    hand: List<Tile>,
    selectedTile: Tile?,
    onTileSelected: (Tile) -> Unit,
    onDrawTile: () -> Unit,
    onPassTurn: () -> Unit,
    deckSize: Int,
    canPlay: Boolean,
    isMyTurn: Boolean
) {
    val scrollState = rememberScrollState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CasinoEmerald),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TU MANO (${hand.size} fichas)",
                    color = CasinoTextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                if (isMyTurn) {
                    if (!canPlay && deckSize > 0) {
                        Button(
                            onClick = onDrawTile,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CasinoGold,
                                contentColor = CasinoDarkFelt
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Robar del pozo ($deckSize)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (!canPlay && deckSize == 0) {
                        Button(
                            onClick = onPassTurn,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CasinoRed,
                                contentColor = CasinoTextWhite
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Pasar Turno", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (hand.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "¡Sin fichas en la mano!",
                        color = CasinoGoldLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    hand.forEach { tile ->
                        TileView(
                            tile = tile,
                            isSelected = selectedTile == tile,
                            onClick = { if (isMyTurn) onTileSelected(tile) },
                            isVertical = true,
                            width = 46.dp,
                            height = 84.dp
                        )
                    }
                }
            }
        }
    }
}
