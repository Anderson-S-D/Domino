package com.udistrital.hockeygame.views.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.udistrital.hockeygame.viewmodels.GameViewModel
import com.udistrital.hockeygame.views.components.*
import com.udistrital.hockeygame.views.theme.*

@Composable
fun GameScreen(
    gameViewModel: GameViewModel = viewModel(),
    onBackToHome: () -> Unit
) {
    val uiState by gameViewModel.uiState.collectAsState()
    var showQuitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (uiState.gameState == null) {
            gameViewModel.startNewGame()
        }
    }

    val state = uiState.gameState ?: return

    val myPlayer = state.players.firstOrNull() ?: return
    val opponentPlayer = state.players.getOrNull(1)
    val isMyTurn = state.turnPlayerId == myPlayer.id
    val canPlay = myPlayer.canPlay(state.board)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CasinoDarkFelt)
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Marcador y Turnos
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CasinoEmerald),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showQuitDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Salir",
                            tint = CasinoGold
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isMyTurn) "¡ES TU TURNO!" else "TURNO DE ${opponentPlayer?.name ?: "OPONENTE"}",
                            color = if (isMyTurn) CasinoGold else CasinoTextMuted,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Pozo: ${state.deck.size} fichas",
                            color = CasinoGoldLight,
                            fontSize = 11.sp
                        )
                    }

                    Surface(
                        color = CasinoDarkFelt,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "${myPlayer.score} - ${opponentPlayer?.score ?: 0}",
                            color = CasinoGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Banner de Mensaje / Eventos Aleatorios
            if (uiState.activeEventMessage != null) {
                Surface(
                    color = CasinoGold,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.activeEventMessage!!,
                            color = CasinoDarkFelt,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { gameViewModel.dismissEventMessage() }) {
                            Text("OK", color = CasinoDarkFelt, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            if (uiState.errorMessage != null) {
                Surface(
                    color = CasinoRed,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = CasinoTextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Tablero Principal
            BoardView(
                board = state.board,
                selectedTile = uiState.selectedTile,
                onPlayLeft = { gameViewModel.playTile(onLeftSide = true) },
                onPlayRight = { gameViewModel.playTile(onLeftSide = false) }
            )

            // Zona de Poderes
            PowerBar(
                activePower = uiState.activePower,
                onPowerSelected = { power -> gameViewModel.activatePower(power) },
                remainingSkipPowers = myPlayer.remainingSkipPowers,
                isMyTurn = isMyTurn
            )

            // Mano del Jugador
            HandView(
                hand = myPlayer.hand,
                selectedTile = uiState.selectedTile,
                onTileSelected = { tile -> gameViewModel.selectTile(tile) },
                onDrawTile = { gameViewModel.drawTile() },
                onPassTurn = { gameViewModel.passTurn() },
                deckSize = state.deck.size,
                canPlay = canPlay,
                isMyTurn = isMyTurn
            )
        }
    }

    // Modal de Confirmación para Salir
    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = { Text("¿Abandonar Partida?", color = CasinoGold, fontWeight = FontWeight.Bold) },
            text = { Text("Si sales ahora perderás tu progreso en la partida actual.", color = CasinoTextWhite) },
            confirmButton = {
                Button(
                    onClick = {
                        showQuitDialog = false
                        onBackToHome()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CasinoRed)
                ) {
                    Text("Abandonar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) {
                    Text("Cancelar", color = CasinoTextMuted)
                }
            },
            containerColor = CasinoEmerald
        )
    }

    // Modal de Resultado (Game Over)
    if (state.isFinished) {
        val winner = state.players.firstOrNull { it.id == state.winnerId }
        val isWinner = winner?.id == myPlayer.id

        AlertDialog(
            onDismissRequest = {},
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(CasinoGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = CasinoDarkFelt,
                        modifier = Modifier.size(36.dp)
                    )
                }
            },
            title = {
                Text(
                    text = if (isWinner) "¡HAS GANADO!" else if (winner != null) "¡GANÓ ${winner.name.uppercase()}!" else "¡EMPATE!",
                    color = CasinoGold,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Resultado final de la partida de Dominó Caos:",
                        color = CasinoTextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    state.players.forEach { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(p.name, color = CasinoTextWhite, fontWeight = FontWeight.Bold)
                            Text("${p.handScore} pts (Poderes: ${p.skipPowersUsed})", color = CasinoGoldLight)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { gameViewModel.startNewGame() },
                    colors = ButtonDefaults.buttonColors(containerColor = CasinoGold, contentColor = CasinoDarkFelt),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Jugar de nuevo", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onBackToHome) {
                    Text("Volver al Menú", color = CasinoTextMuted)
                }
            },
            containerColor = CasinoEmerald
        )
    }
}
