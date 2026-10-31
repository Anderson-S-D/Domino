package com.udistrital.hockeygame.domain.models

/**
 * Ficha de dominó inmutable.
 *
 * Se usa un [IntArray] para almacenar los dos lados. Como los arreglos no
 * implementan equals/hashCode por contenido, se sobrescriben explícitamente.
 */
class Tile(numbers: IntArray) {

    /*
    * Validacion de que la ficha tenga 2 lados
     */
    init {
        require(numbers.size == TILE_SIDES) {
            "A domino tile must have exactly $TILE_SIDES numbers"
        }
    }

    private val values: IntArray = numbers.copyOf()

    /**
     * Copia defensiva de los lados de la ficha.
     */
    val numbers: IntArray
        get() = values.copyOf()

    val left: Int
        get() = values[0]

    val right: Int
        get() = values[1]

    val score: Int
        get() = left + right

    /**
     * Retorna una ficha nueva con sus lados invertidos.

    * Sobrescribir para asegurar comparaciones por contenido del arreglo.
     */
    fun reversed(): Tile = Tile(intArrayOf(right, left))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tile) return false
        return values.contentEquals(other.values)
    }

    override fun hashCode(): Int = values.contentHashCode()

    override fun toString(): String = "[$left|$right]"

    private companion object {
        const val TILE_SIDES = 2
    }
}
