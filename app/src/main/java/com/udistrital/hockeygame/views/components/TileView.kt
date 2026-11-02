package com.udistrital.hockeygame.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udistrital.hockeygame.domain.models.Tile
import com.udistrital.hockeygame.views.theme.*

@Composable
fun TileView(
    tile: Tile,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    isVertical: Boolean = true,
    width: Dp = 50.dp,
    height: Dp = 90.dp
) {
    val tileShape = RoundedCornerShape(10.dp)
    val borderColor = if (isSelected) CasinoGold else Color(0xFF333333)
    val borderWidth = if (isSelected) 3.dp else 1.dp
    val bgColor = if (isSelected) CasinoGoldLight else Color(0xFFFAFAFA)

    Surface(
        modifier = Modifier
            .size(width = width, height = height)
            .shadow(if (isSelected) 10.dp else 4.dp, tileShape)
            .clip(tileShape)
            .border(borderWidth, borderColor, tileShape)
            .background(bgColor)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        color = bgColor
    ) {
        if (isVertical) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PipGrid(number = tile.left, dotSize = width / 6)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color(0xFF444444))
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PipGrid(number = tile.right, dotSize = width / 6)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    PipGrid(number = tile.left, dotSize = height / 6)
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(Color(0xFF444444))
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    PipGrid(number = tile.right, dotSize = height / 6)
                }
            }
        }
    }
}

@Composable
private fun PipGrid(number: Int, dotSize: Dp) {
    val dotColor = Color(0xFF111111)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        when (number) {
            0 -> {}
            1 -> Pip(dotSize, dotColor)
            2 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { Pip(dotSize, dotColor) }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) { Pip(dotSize, dotColor) }
                }
            }
            3 -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Pip(dotSize, dotColor)
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { Pip(dotSize, dotColor) }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) { Pip(dotSize, dotColor) }
                    }
                }
            }
            4 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Pip(dotSize, dotColor); Pip(dotSize, dotColor)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Pip(dotSize, dotColor); Pip(dotSize, dotColor)
                    }
                }
            }
            5 -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Pip(dotSize, dotColor)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Pip(dotSize, dotColor); Pip(dotSize, dotColor)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Pip(dotSize, dotColor); Pip(dotSize, dotColor)
                        }
                    }
                }
            }
            6 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Pip(dotSize, dotColor); Pip(dotSize, dotColor)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Pip(dotSize, dotColor); Pip(dotSize, dotColor)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Pip(dotSize, dotColor); Pip(dotSize, dotColor)
                    }
                }
            }
            else -> {
                Text(text = number.toString(), color = dotColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun Pip(size: Dp, color: Color) {
    Box(
        modifier = Modifier
            .size(size)
            .background(color, CircleShape)
    )
}
