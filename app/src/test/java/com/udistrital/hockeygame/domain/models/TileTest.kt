package com.udistrital.hockeygame.domain.models

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TileTest {

    @Test
    fun `exposes both sides and the score`() {
        val tile = Tile(intArrayOf(3, 5))

        assertEquals(3, tile.left)
        assertEquals(5, tile.right)
        assertEquals(8, tile.score)
        assertArrayEquals(intArrayOf(3, 5), tile.numbers)
    }

    @Test
    fun `reversed returns a new tile with swapped sides`() {
        val tile = Tile(intArrayOf(3, 5))
        val reversed = tile.reversed()

        assertEquals(5, reversed.left)
        assertEquals(3, reversed.right)
        assertEquals(8, reversed.score)
        assertArrayEquals(intArrayOf(5, 3), reversed.numbers)
    }

    @Test
    fun `equals and hashCode use the array content`() {
        val first = Tile(intArrayOf(3, 5))
        val second = Tile(intArrayOf(3, 5))
        val different = Tile(intArrayOf(5, 3))

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
        assertNotEquals(first, different)
    }

    @Test
    fun `defensive copy protects the internal state`() {
        val source = intArrayOf(1, 2)
        val tile = Tile(source)

        source[0] = 6
        assertArrayEquals(intArrayOf(1, 2), tile.numbers)

        val exposed = tile.numbers
        exposed[0] = 6
        assertArrayEquals(intArrayOf(1, 2), tile.numbers)
        assertEquals(1, tile.left)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `requires exactly two numbers`() {
        Tile(intArrayOf(1, 2, 3))
    }
}
