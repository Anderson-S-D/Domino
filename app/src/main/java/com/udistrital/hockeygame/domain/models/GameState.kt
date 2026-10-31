package com.udistrital.hockeygame.domain.models

/**
 * Estado completo e inmutable de una partida de dominó.
 *
 * [skipNextTurnFor] guarda al jugador que activó el poder de Salto y que debe
 * conservar el turno después de colocar su siguiente ficha. Se limpia cuando
 * el salto se consume o cuando la partida termina.
 */
data class GameState(
    val roomId: String,
    val players: List<PlayerState>,
    val board: Board = Board(),
    val deck: List<Tile> = emptyList(),
    val turnPlayerId: String,
    val isFinished: Boolean = false,
    val winnerId: String? = null,
    val skipNextTurnFor: String? = null
)
