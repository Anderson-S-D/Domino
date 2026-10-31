package com.udistrital.hockeygame.domain.models

/**
 * Estado de un jugador dentro de una partida.
 *
 * [score] se usa para almacenar el puntaje final (puntos que quedaron en la
 * mano) cuando la partida termina. [remainingSkipPowers] y [skipPowersUsed]
 * controlan el poder de Salto de Turno y sirven como criterio de desempate.
 */
data class PlayerState(
    val id: String,
    val name: String,
    val hand: List<Tile> = emptyList(),
    val score: Int = 0,
    val remainingSkipPowers: Int = 1,
    val skipPowersUsed: Int = 0
) {

    val handScore: Int
        get() = hand.sumOf { it.score }

    /**
     * Indica si el jugador tiene al menos una ficha jugable en [board].
     */
    fun canPlay(board: Board): Boolean {
        if (hand.isEmpty()) return false
        if (board.isEmpty) return true

        val leftEnd = board.leftEnd ?: return false
        val rightEnd = board.rightEnd ?: return false

        return hand.any { tile ->
            tile.left == leftEnd || tile.right == leftEnd || tile.left == rightEnd || tile.right == rightEnd
        }
    }
}
