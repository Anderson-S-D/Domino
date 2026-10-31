package com.udistrital.hockeygame.domain.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardTest {

    @Test
    fun `empty board has no ends`() {
        val board = Board()

        assertTrue(board.isEmpty)
        assertNull(board.leftEnd)
        assertNull(board.rightEnd)
    }

    @Test
    fun `ends follow the first and last tiles`() {
        val board = Board(listOf(tile(2, 5), tile(5, 6)))

        assertEquals(2, board.leftEnd)
        assertEquals(6, board.rightEnd)
    }

    @Test
    fun `placeLeft prepends a rotated tile when necessary`() {
        val board = Board(listOf(tile(2, 5)))

        val updated = board.placeLeft(tile(2, 4))

        assertEquals(listOf(tile(4, 2), tile(2, 5)), updated?.tiles)
        assertEquals(4, updated?.leftEnd)
        assertEquals(5, updated?.rightEnd)
    }

    @Test
    fun `placeRight appends a rotated tile when necessary`() {
        val board = Board(listOf(tile(2, 5)))

        val updated = board.placeRight(tile(6, 5))

        assertEquals(listOf(tile(2, 5), tile(5, 6)), updated?.tiles)
        assertEquals(2, updated?.leftEnd)
        assertEquals(6, updated?.rightEnd)
    }

    @Test
    fun `placeLeft and placeRight accept any tile on an empty board`() {
        val board = Board()

        assertEquals(listOf(tile(3, 4)), board.placeLeft(tile(3, 4))?.tiles)
        assertEquals(listOf(tile(3, 4)), board.placeRight(tile(3, 4))?.tiles)
    }

    @Test
    fun `placement returns null when the tile does not match`() {
        val board = Board(listOf(tile(2, 5)))

        assertNull(board.placeLeft(tile(1, 1)))
        assertNull(board.placeRight(tile(1, 1)))
    }

    private fun tile(left: Int, right: Int): Tile = Tile(intArrayOf(left, right))
}
