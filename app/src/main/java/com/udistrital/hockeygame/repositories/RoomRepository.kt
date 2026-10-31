package com.udistrital.hockeygame.repositories

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.udistrital.hockeygame.data.models.PlayerModel
import com.udistrital.hockeygame.data.models.RoomModel

class RoomRepository {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val roomsRef: DatabaseReference = database.getReference("rooms")

    fun createRoom(hostPlayer: PlayerModel): RoomModel? {
        val newRoomRef = roomsRef.push()
        val roomId = newRoomRef.key ?: return null

        val room = RoomModel(
            id = roomId,
            player1 = hostPlayer,
            player2 = null,
            startAt = System.currentTimeMillis().toString(),
            winnerPlayerUid = null,
            turnPlayerUid = hostPlayer.uid,
            finished = false
        )

        newRoomRef.setValue(room)
        return room
    }

    fun joinOpenRoom(guestPlayer: PlayerModel, onResult: (RoomModel?) -> Unit) {
        roomsRef.orderByChild("startAt").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var targetRoom: Pair<String, RoomModel>? = null

                for (child in snapshot.children) {
                    val room = child.getValue(RoomModel::class.java)
                    if (room != null) {
                        if (room.player2 == null && !room.finished) {
                            targetRoom = Pair(child.key ?: room.id ?: "", room)
                        }
                    }
                }

                if (targetRoom == null) {
                    onResult(null)
                    return
                }

                val roomId = targetRoom.first
                val room = targetRoom.second
                val updatedRoom = room.copy(
                    id = roomId,
                    player2 = guestPlayer,
                    turnPlayerUid = room.player1.uid
                )

                roomsRef.child(roomId).child("player2").setValue(guestPlayer)
                roomsRef.child(roomId).child("turnPlayerUid").setValue(room.player1.uid)
                onResult(updatedRoom)
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(null)
            }
        })
    }
}