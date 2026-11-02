package com.udistrital.hockeygame.data.models

import kotlin.random.Random

object RoomCodeGenerator {
    private val CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray()

    fun generate(length: Int = 6): String {
        return (1..length)
            .map { CHARS[Random.nextInt(CHARS.size)] }
            .joinToString("")
    }
}
