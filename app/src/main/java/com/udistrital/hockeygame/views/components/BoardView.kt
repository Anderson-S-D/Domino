package com.udistrital.hockeygame.views.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udistrital.hockeygame.domain.models.Board
import com.udistrital.hockeygame.domain.models.Tile
import com.udistrital.hockeygame.views.theme.*

@Composable
fun BoardView(
    board: Board,
    selectedTile: Tile?,
    onPlayLeft: () -> Unit,
    onPlayRight: () -> Unit
) {
    val scrollState = rememberScrollState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        colors = CardDefaults.cardColors(containerColor = CasinoEmerald),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = CasinoDarkFelt,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Izquierda: ${board.leftEnd ?: '-'}",
                        color = CasinoGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = "TABLERO (${board.tiles.size} fichas)",
                    color = CasinoTextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Surface(
                    color = CasinoDarkFelt,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Derecha: ${board.rightEnd ?: '-'}",
                        color = CasinoGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            if (board.isEmpty) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "El tablero está vacío.\n¡Coloca la primera ficha!",
                        color = CasinoTextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .horizontalScroll(scrollState)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    board.tiles.forEachIndexed { index, tile ->
                        TileView(
                            tile = tile,
                            isVertical = false,
                            width = 76.dp,
                            height = 42.dp
                        )
                        if (index < board.tiles.size - 1) {
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }
            }

            if (selectedTile != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPlayLeft,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CasinoGold,
                            contentColor = CasinoDarkFelt
                        )
                    ) {
                        Text("◄ Izquierda", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = onPlayRight,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CasinoGold,
                            contentColor = CasinoDarkFelt
                        )
                    ) {
                        Text("Derecha ►", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
