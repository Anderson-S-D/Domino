package com.udistrital.hockeygame.data.models

data class RoomModel(
    var id: String = "",
    val code: String = "",
    val player1: PlayerModel = PlayerModel(),
    val player2: PlayerModel? = null,
    val startAt: String = "",
    val winnerPlayerUid: String? = null,
    val turnPlayerUid: String = "",
    val status: String = "WAITING", // WAITING, PLAYING, FINISHED, CANCELLED
    val finished: Boolean = false
)
