package com.udistrital.hockeygame.domain.engine

import com.udistrital.hockeygame.domain.models.Board
import com.udistrital.hockeygame.domain.models.GameState
import com.udistrital.hockeygame.domain.models.PlayerState
import com.udistrital.hockeygame.domain.models.Tile
import kotlin.random.Random

/**
 * Motor de reglas del dominó para partidas 1v1.
 *
 * Todas las funciones son puras: reciben un [GameState] y devuelven uno nuevo
 * sin mutar el anterior. No depende de Android ni de Firebase.
 */
class DominoEngine {

    /**
     * Crea una partida nueva, baraja las 28 fichas y reparte 7 a cada jugador.
     *
     * @param seed semilla opcional para obtener repartos deterministas en tests.
     */
    fun createInitialGame(
        roomId: String,
        player1Id: String,
        player1Name: String,
        player2Id: String,
        player2Name: String,
        seed: Long? = null
    ): GameState {
        require(player1Id != player2Id) { "Players must have different ids" }

        val random = seed?.let { Random(it) } ?: Random.Default
        val deck = buildDeck().shuffled(random).toMutableList()

        val player1Hand = mutableListOf<Tile>()
        val player2Hand = mutableListOf<Tile>()

        /*
        Repartir del mazo sin reoetir fichas.
         */
        repeat(INITIAL_HAND_SIZE) {
            player1Hand += deck.removeAt(0)
            player2Hand += deck.removeAt(0)
        }

        return GameState(
            roomId = roomId,
            players = listOf(
                PlayerState(id = player1Id, name = player1Name, hand = player1Hand),
                PlayerState(id = player2Id, name = player2Name, hand = player2Hand)
            ),
            board = Board(),
            deck = deck,
            turnPlayerId = player1Id
        )
    }

    /**
     * Coloca una ficha de la mano de [playerId] en el extremo indicado.
     *
     * La ficha se invierte automáticamente si es necesario para que coincida
     * con el extremo elegido. El turno avanza al oponente, salvo que el jugador
     * tenga un Salto de Turno activo.
     */
    fun playTile(
        state: GameState,
        playerId: String,
        tile: Tile,
        onLeftSide: Boolean
    ): Result<GameState> {
        if (state.isFinished) {
            return Result.failure(IllegalStateException("The game is already finished"))
        }
        if (state.turnPlayerId != playerId) {
            return Result.failure(IllegalStateException("It is not $playerId's turn"))
        }

        val playerIndex = state.players.indexOfFirst { it.id == playerId }
        if (playerIndex == -1) {
            return Result.failure(IllegalArgumentException("Player $playerId was not found"))
        }

        val player = state.players[playerIndex]
        val tileIndex = player.hand.indexOf(tile)
        if (tileIndex == -1) {
            return Result.failure(IllegalArgumentException("Player $playerId does not own tile $tile"))
        }

        val updatedBoard = when {
            state.board.isEmpty -> Board(listOf(tile))
            onLeftSide -> state.board.placeLeft(tile)
                ?: return Result.failure(
                    IllegalArgumentException("Tile $tile cannot be placed on the left end")
                )

            else -> state.board.placeRight(tile)
                ?: return Result.failure(
                    IllegalArgumentException("Tile $tile cannot be placed on the right end")
                )
        }

        val updatedHand = player.hand.toMutableList().apply { removeAt(tileIndex) }
        val updatedPlayers = state.players.toMutableList().apply {
            this[playerIndex] = player.copy(hand = updatedHand)
        }

        val nextTurnPlayerId = when {
            state.skipNextTurnFor == playerId -> playerId
            else -> state.players.firstOrNull { it.id != playerId }?.id ?: playerId
        }

        val nextState = state.copy(
            players = updatedPlayers,
            board = updatedBoard,
            turnPlayerId = nextTurnPlayerId,
            skipNextTurnFor = null
        )

        return Result.success(checkGameEnd(nextState))
    }

    /**
     * Activa el poder de Salto de Turno.
     *
     * En 1v1 el jugador conserva el turno y, después de su siguiente jugada,
     * el turno no pasa al oponente: se consumen dos jugadas consecutivas.
     */
    fun useSkipPower(state: GameState, playerId: String): Result<GameState> {
        if (state.isFinished) {
            return Result.failure(IllegalStateException("The game is already finished"))
        }
        if (state.turnPlayerId != playerId) {
            return Result.failure(IllegalStateException("It is not $playerId's turn"))
        }
        if (state.players.none { it.id != playerId }) {
            return Result.failure(IllegalStateException("Skip power requires an opponent"))
        }
        if (state.skipNextTurnFor == playerId) {
            return Result.failure(IllegalStateException("Skip power is already active"))
        }

        val playerIndex = state.players.indexOfFirst { it.id == playerId }
        if (playerIndex == -1) {
            return Result.failure(IllegalArgumentException("Player $playerId was not found"))
        }

        val player = state.players[playerIndex]
        if (player.remainingSkipPowers <= 0) {
            return Result.failure(IllegalStateException("Player $playerId has no skip powers left"))
        }

        val updatedPlayer = player.copy(
            remainingSkipPowers = player.remainingSkipPowers - 1,
            skipPowersUsed = player.skipPowersUsed + 1
        )
        val updatedPlayers = state.players.toMutableList().apply {
            this[playerIndex] = updatedPlayer
        }

        return Result.success(
            state.copy(
                players = updatedPlayers,
                skipNextTurnFor = playerId
            )
        )
    }

    /**
     * Roba la ficha superior del pozo y la agrega a la mano de [playerId].
     *
     * El turno se mantiene para que el jugador pueda colocar la ficha robada.
     */
    fun drawTile(state: GameState, playerId: String): Result<GameState> {
        if (state.isFinished) {
            return Result.failure(IllegalStateException("The game is already finished"))
        }
        if (state.turnPlayerId != playerId) {
            return Result.failure(IllegalStateException("It is not $playerId's turn"))
        }
        if (state.deck.isEmpty()) {
            return Result.failure(IllegalStateException("The deck is empty"))
        }

        val playerIndex = state.players.indexOfFirst { it.id == playerId }
        if (playerIndex == -1) {
            return Result.failure(IllegalArgumentException("Player $playerId was not found"))
        }

        val drawnTile = state.deck.first()
        val player = state.players[playerIndex]
        val updatedPlayers = state.players.toMutableList().apply {
            this[playerIndex] = player.copy(hand = player.hand + drawnTile)
        }

        val nextState = state.copy(
            players = updatedPlayers,
            deck = state.deck.drop(1)
        )

        return Result.success(checkGameEnd(nextState))
    }

    /**
     * Detecta y aplica el final de la partida.
     *
     * La partida termina si un jugador se queda sin fichas o si el pozo está
     * vacío y ningún jugador puede jugar. El puntaje final es la suma de los
     * puntos que quedaron en cada mano; gana el menor puntaje. En caso de
     * empate gana quien haya usado menos veces el poder de Salto. Si persiste
     * el empate, [GameState.winnerId] queda en `null`.
     */
    fun checkGameEnd(state: GameState): GameState {
        if (state.isFinished) return state

        val someoneEmptyHand = state.players.any { it.hand.isEmpty() }
        val blocked = state.deck.isEmpty() && state.players.none { it.canPlay(state.board) }

        if (!someoneEmptyHand && !blocked) return state

        val scoredPlayers = state.players.map { player ->
            player.copy(score = player.handScore)
        }

        val lowestScore = scoredPlayers.minOfOrNull { it.score } ?: 0
        val lowestScorePlayers = scoredPlayers.filter { it.score == lowestScore }
        val fewestPowersUsed = lowestScorePlayers.minOfOrNull { it.skipPowersUsed } ?: 0
        val finalists = lowestScorePlayers.filter { it.skipPowersUsed == fewestPowersUsed }

        return state.copy(
            players = scoredPlayers,
            isFinished = true,
            winnerId = finalists.singleOrNull()?.id,
            skipNextTurnFor = null
        )
    }

    private fun buildDeck(): List<Tile> = buildList {
        for (left in MIN_TILE_VALUE..MAX_TILE_VALUE) {
            for (right in left..MAX_TILE_VALUE) {
                add(Tile(intArrayOf(left, right)))
            }
        }
    }

    companion object {
        const val MIN_TILE_VALUE = 0
        const val MAX_TILE_VALUE = 6
        const val INITIAL_HAND_SIZE = 7
    }
}
