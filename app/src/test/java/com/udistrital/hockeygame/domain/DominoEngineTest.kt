package com.udistrital.hockeygame.domain

import com.udistrital.hockeygame.domain.engine.DominoEngine
import com.udistrital.hockeygame.domain.models.Board
import com.udistrital.hockeygame.domain.models.GameState
import com.udistrital.hockeygame.domain.models.PlayerState
import com.udistrital.hockeygame.domain.models.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DominoEngineTest {

    private val engine = DominoEngine()

    @Test
    fun `createInitialGame generates 28 unique tiles and deals 7 per player`() {
        val state = engine.createInitialGame(
            roomId = "room-1",
            player1Id = PLAYER_1,
            player1Name = "Player 1",
            player2Id = PLAYER_2,
            player2Name = "Player 2",
            seed = 2026L
        )

        val allTiles = state.players.flatMap { it.hand } + state.deck

        assertEquals(28, allTiles.size)
        assertEquals(7, state.players[0].hand.size)
        assertEquals(7, state.players[1].hand.size)
        assertEquals(14, state.deck.size)
        assertEquals(28, allTiles.map { it.left to it.right }.toSet().size)
        assertEquals(PLAYER_1, state.turnPlayerId)
        assertFalse(state.isFinished)
        assertNull(state.winnerId)
    }

    @Test
    fun `createInitialGame is deterministic when a seed is provided`() {
        val first = engine.createInitialGame(
            roomId = "room-1",
            player1Id = PLAYER_1,
            player1Name = "Player 1",
            player2Id = PLAYER_2,
            player2Name = "Player 2",
            seed = 7L
        )
        val second = engine.createInitialGame(
            roomId = "room-1",
            player1Id = PLAYER_1,
            player1Name = "Player 1",
            player2Id = PLAYER_2,
            player2Name = "Player 2",
            seed = 7L
        )

        assertEquals(first.players[0].hand, second.players[0].hand)
        assertEquals(first.players[1].hand, second.players[1].hand)
        assertEquals(first.deck, second.deck)
    }

    @Test
    fun `playTile places and reverses a tile on the left end`() {
        val state = gameState(
            board = Board(listOf(tile(2, 5))),
            player1Hand = listOf(tile(2, 4), tile(5, 6), tile(1, 1)),
            player2Hand = listOf(tile(0, 0))
        )

        val result = engine.playTile(state, PLAYER_1, tile(2, 4), onLeftSide = true)

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals(listOf(tile(4, 2), tile(2, 5)), updated.board.tiles)
        assertEquals(4, updated.board.leftEnd)
        assertEquals(5, updated.board.rightEnd)
        assertEquals(2, updated.players[0].hand.size)
        assertFalse(updated.players[0].hand.contains(tile(2, 4)))
        assertEquals(PLAYER_2, updated.turnPlayerId)
    }

    @Test
    fun `playTile places a tile on the right end`() {
        val state = gameState(
            board = Board(listOf(tile(2, 5))),
            player1Hand = listOf(tile(5, 6), tile(1, 1)),
            player2Hand = listOf(tile(0, 0))
        )

        val result = engine.playTile(state, PLAYER_1, tile(5, 6), onLeftSide = false)

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals(listOf(tile(2, 5), tile(5, 6)), updated.board.tiles)
        assertEquals(2, updated.board.leftEnd)
        assertEquals(6, updated.board.rightEnd)
        assertEquals(PLAYER_2, updated.turnPlayerId)
    }

    @Test
    fun `playTile reverses a tile on the right end when needed`() {
        val state = gameState(
            board = Board(listOf(tile(2, 5))),
            player1Hand = listOf(tile(6, 5), tile(1, 1)),
            player2Hand = listOf(tile(0, 0))
        )

        val result = engine.playTile(state, PLAYER_1, tile(6, 5), onLeftSide = false)

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals(listOf(tile(2, 5), tile(5, 6)), updated.board.tiles)
        assertEquals(6, updated.board.rightEnd)
    }

    @Test
    fun `playTile accepts any tile when the board is empty`() {
        val state = gameState(
            board = Board(),
            player1Hand = listOf(tile(3, 4), tile(1, 1)),
            player2Hand = listOf(tile(0, 0))
        )

        val result = engine.playTile(state, PLAYER_1, tile(3, 4), onLeftSide = true)

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals(listOf(tile(3, 4)), updated.board.tiles)
        assertEquals(3, updated.board.leftEnd)
        assertEquals(4, updated.board.rightEnd)
        assertEquals(PLAYER_2, updated.turnPlayerId)
    }

    @Test
    fun `playTile rejects a tile that does not match either end`() {
        val state = gameState(
            board = Board(listOf(tile(2, 5))),
            player1Hand = listOf(tile(1, 1)),
            player2Hand = listOf(tile(0, 0))
        )

        val leftResult = engine.playTile(state, PLAYER_1, tile(1, 1), onLeftSide = true)
        val rightResult = engine.playTile(state, PLAYER_1, tile(1, 1), onLeftSide = false)

        assertTrue(leftResult.isFailure)
        assertTrue(rightResult.isFailure)
    }

    @Test
    fun `playTile rejects a tile placed on the wrong requested side`() {
        val state = gameState(
            board = Board(listOf(tile(2, 5))),
            player1Hand = listOf(tile(2, 4)),
            player2Hand = listOf(tile(0, 0))
        )

        val result = engine.playTile(state, PLAYER_1, tile(2, 4), onLeftSide = false)

        assertTrue(result.isFailure)
    }

    @Test
    fun `playTile rejects a tile that is not in the player hand`() {
        val state = gameState(
            board = Board(listOf(tile(2, 5))),
            player1Hand = listOf(tile(1, 1)),
            player2Hand = listOf(tile(0, 0))
        )

        val result = engine.playTile(state, PLAYER_1, tile(4, 4), onLeftSide = true)

        assertTrue(result.isFailure)
    }

    @Test
    fun `playTile rejects a move when it is not the player turn`() {
        val state = gameState(
            board = Board(listOf(tile(2, 5))),
            player1Hand = listOf(tile(2, 4)),
            player2Hand = listOf(tile(5, 6)),
            turnPlayerId = PLAYER_2
        )

        val result = engine.playTile(state, PLAYER_1, tile(2, 4), onLeftSide = true)

        assertTrue(result.isFailure)
    }

    @Test
    fun `useSkipPower grants two consecutive plays in a 1v1 game`() {
        val state = gameState(
            board = Board(),
            deck = listOf(tile(6, 6)),
            player1Hand = listOf(tile(0, 0), tile(0, 1), tile(1, 1)),
            player2Hand = listOf(tile(2, 2))
        )

        val afterPower = engine.useSkipPower(state, PLAYER_1).getOrThrow()

        assertEquals(PLAYER_1, afterPower.turnPlayerId)
        assertEquals(0, afterPower.players[0].remainingSkipPowers)
        assertEquals(1, afterPower.players[0].skipPowersUsed)
        assertEquals(PLAYER_1, afterPower.skipNextTurnFor)

        val afterFirstPlay = engine
            .playTile(afterPower, PLAYER_1, tile(0, 0), onLeftSide = false)
            .getOrThrow()

        assertEquals(PLAYER_1, afterFirstPlay.turnPlayerId)
        assertNull(afterFirstPlay.skipNextTurnFor)
        assertEquals(2, afterFirstPlay.players[0].hand.size)

        val afterSecondPlay = engine
            .playTile(afterFirstPlay, PLAYER_1, tile(0, 1), onLeftSide = false)
            .getOrThrow()

        assertEquals(PLAYER_2, afterSecondPlay.turnPlayerId)
        assertEquals(1, afterSecondPlay.players[0].hand.size)
    }

    @Test
    fun `useSkipPower fails when the player has no uses left`() {
        val state = gameState(
            player1RemainingSkip = 0,
            player1SkipUsed = 1
        )

        val result = engine.useSkipPower(state, PLAYER_1)

        assertTrue(result.isFailure)
    }

    @Test
    fun `useSkipPower fails when it is not the player turn`() {
        val state = gameState(turnPlayerId = PLAYER_2)

        val result = engine.useSkipPower(state, PLAYER_1)

        assertTrue(result.isFailure)
    }

    @Test
    fun `drawTile takes the top deck tile and keeps the turn`() {
        val state = gameState(
            deck = listOf(tile(6, 6), tile(6, 5)),
            player1Hand = listOf(tile(0, 0)),
            player2Hand = listOf(tile(1, 1))
        )

        val result = engine.drawTile(state, PLAYER_1)

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals(listOf(tile(0, 0), tile(6, 6)), updated.players[0].hand)
        assertEquals(listOf(tile(6, 5)), updated.deck)
        assertEquals(PLAYER_1, updated.turnPlayerId)
        assertFalse(updated.isFinished)
    }

    @Test
    fun `drawTile fails when the deck is empty`() {
        val state = gameState(
            deck = emptyList(),
            board = Board(listOf(tile(0, 0))),
            player1Hand = listOf(tile(1, 1)),
            player2Hand = listOf(tile(2, 2))
        )

        val result = engine.drawTile(state, PLAYER_1)

        assertTrue(result.isFailure)
    }

    @Test
    fun `checkGameEnd uses fewer skip powers as tiebreaker`() {
        val state = gameState(
            board = Board(listOf(tile(0, 0))),
            deck = emptyList(),
            player1Hand = listOf(tile(3, 3)),
            player2Hand = listOf(tile(2, 4)),
            player1SkipUsed = 1,
            player2SkipUsed = 0
        )

        val result = engine.checkGameEnd(state)

        assertTrue(result.isFinished)
        assertEquals(6, result.players[0].score)
        assertEquals(6, result.players[1].score)
        assertEquals(PLAYER_2, result.winnerId)
    }

    @Test
    fun `checkGameEnd declares a draw when points and powers are tied`() {
        val state = gameState(
            board = Board(listOf(tile(0, 0))),
            deck = emptyList(),
            player1Hand = listOf(tile(3, 3)),
            player2Hand = listOf(tile(2, 4)),
            player1SkipUsed = 1,
            player2SkipUsed = 1
        )

        val result = engine.checkGameEnd(state)

        assertTrue(result.isFinished)
        assertNull(result.winnerId)
    }

    @Test
    fun `checkGameEnd prioritizes fewer points over fewer powers used`() {
        val state = gameState(
            board = Board(listOf(tile(0, 0))),
            deck = emptyList(),
            player1Hand = listOf(tile(3, 3)),
            player2Hand = listOf(tile(2, 2)),
            player1SkipUsed = 0,
            player2SkipUsed = 1
        )

        val result = engine.checkGameEnd(state)

        assertTrue(result.isFinished)
        assertEquals(PLAYER_2, result.winnerId)
    }

    @Test
    fun `checkGameEnd finishes when a player has no tiles left`() {
        val state = gameState(
            board = Board(listOf(tile(0, 0))),
            deck = emptyList(),
            player1Hand = emptyList(),
            player2Hand = listOf(tile(6, 6))
        )

        val result = engine.checkGameEnd(state)

        assertTrue(result.isFinished)
        assertEquals(PLAYER_1, result.winnerId)
        assertEquals(0, result.players[0].score)
        assertEquals(12, result.players[1].score)
    }

    @Test
    fun `checkGameEnd does not finish while the deck still has tiles`() {
        val state = gameState(
            board = Board(listOf(tile(0, 0))),
            deck = listOf(tile(6, 6)),
            player1Hand = listOf(tile(1, 1)),
            player2Hand = listOf(tile(2, 2))
        )

        val result = engine.checkGameEnd(state)

        assertFalse(result.isFinished)
        assertNull(result.winnerId)
    }

    private fun gameState(
        board: Board = Board(),
        deck: List<Tile> = listOf(tile(6, 6), tile(6, 5)),
        player1Hand: List<Tile> = listOf(tile(0, 0), tile(0, 1)),
        player2Hand: List<Tile> = listOf(tile(1, 1), tile(1, 2)),
        turnPlayerId: String = PLAYER_1,
        player1RemainingSkip: Int = 1,
        player1SkipUsed: Int = 0,
        player2RemainingSkip: Int = 1,
        player2SkipUsed: Int = 0
    ): GameState = GameState(
        roomId = "room-1",
        players = listOf(
            PlayerState(
                id = PLAYER_1,
                name = "Player 1",
                hand = player1Hand,
                remainingSkipPowers = player1RemainingSkip,
                skipPowersUsed = player1SkipUsed
            ),
            PlayerState(
                id = PLAYER_2,
                name = "Player 2",
                hand = player2Hand,
                remainingSkipPowers = player2RemainingSkip,
                skipPowersUsed = player2SkipUsed
            )
        ),
        board = board,
        deck = deck,
        turnPlayerId = turnPlayerId
    )

    private fun tile(left: Int, right: Int): Tile = Tile(intArrayOf(left, right))

    private companion object {
        const val PLAYER_1 = "player-1"
        const val PLAYER_2 = "player-2"
    }
}
