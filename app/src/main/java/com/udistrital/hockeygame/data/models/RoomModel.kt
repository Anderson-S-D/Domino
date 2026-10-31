package com.udistrital.hockeygame.data.models

data class RoomModel(
    var id: String? = "",
    val player1: PlayerModel,
    val player2: PlayerModel? = null,
    val startAt: String,
    val winnerPlayerUid: String? = "",
    val turnPlayerUid: String = "",
    val finished: Boolean = false
)