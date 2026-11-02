package com.udistrital.hockeygame.repositories

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.udistrital.hockeygame.data.models.PlayerModel
import com.udistrital.hockeygame.data.models.RoomCodeGenerator
import com.udistrital.hockeygame.data.models.RoomModel

class RoomRepository {
    private val TAG = "RoomRepository"
    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private val roomsRef: DatabaseReference by lazy { database.getReference("rooms") }

    // En memoria fallback si Firebase aún no está vinculado en la consola
    private val activeRoomsLocal = mutableMapOf<String, RoomModel>()

    fun createRoom(hostPlayer: PlayerModel): RoomModel {
        val roomCode = RoomCodeGenerator.generate(6)
        val room = RoomModel(
            id = roomCode,
            code = roomCode,
            player1 = hostPlayer,
            player2 = null,
            startAt = System.currentTimeMillis().toString(),
            winnerPlayerUid = null,
            turnPlayerUid = hostPlayer.uid,
            status = "WAITING",
            finished = false
        )

        activeRoomsLocal[roomCode] = room

        try {
            roomsRef.child(roomCode).setValue(room)
                .addOnSuccessListener { Log.d(TAG, "Room created on Firebase: $roomCode") }
                .addOnFailureListener { Log.w(TAG, "Firebase room write failed, using local mode", it) }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase unavailable, room created locally: $roomCode", e)
        }

        return room
    }

    fun joinRoomByCode(code: String, guestPlayer: PlayerModel, onResult: (RoomModel?, String?) -> Unit) {
        val cleanCode = code.trim().uppercase()

        // 1. Verificar localmente
        val localRoom = activeRoomsLocal[cleanCode]
        if (localRoom != null && localRoom.player2 == null) {
            val updated = localRoom.copy(
                player2 = guestPlayer,
                turnPlayerUid = localRoom.player1.uid,
                status = "PLAYING"
            )
            activeRoomsLocal[cleanCode] = updated
            onResult(updated, null)
            return
        }

        // 2. Intentar en Firebase
        try {
            roomsRef.child(cleanCode).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val room = snapshot.getValue(RoomModel::class.java)
                    if (room == null) {
                        onResult(null, "No se encontró la sala con código $cleanCode")
                        return
                    }

                    if (room.player2 != null && room.player2.uid != guestPlayer.uid) {
                        onResult(null, "La sala $cleanCode ya está llena")
                        return
                    }

                    val updatedRoom = room.copy(
                        id = cleanCode,
                        code = cleanCode,
                        player2 = guestPlayer,
                        turnPlayerUid = room.player1.uid,
                        status = "PLAYING"
                    )

                    activeRoomsLocal[cleanCode] = updatedRoom
                    roomsRef.child(cleanCode).setValue(updatedRoom)
                    onResult(updatedRoom, null)
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(null, "Error al conectar con la sala: ${error.message}")
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "Firebase check failed", e)
            onResult(null, "No se pudo conectar a Firebase para unirse a la sala")
        }
    }

    fun joinOpenRoom(guestPlayer: PlayerModel, onResult: (RoomModel?, String?) -> Unit) {
        // Buscar cualquier sala local esperando
        val openLocal = activeRoomsLocal.values.firstOrNull { it.player2 == null && !it.finished }
        if (openLocal != null) {
            joinRoomByCode(openLocal.code, guestPlayer, onResult)
            return
        }

        try {
            roomsRef.orderByChild("status").equalTo("WAITING")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val firstChild = snapshot.children.firstOrNull()
                        if (firstChild == null) {
                            onResult(null, "No hay salas abiertas disponibles. ¡Crea una nueva!")
                            return
                        }
                        val roomCode = firstChild.key ?: return
                        joinRoomByCode(roomCode, guestPlayer, onResult)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        onResult(null, error.message)
                    }
                })
        } catch (e: Exception) {
            onResult(null, "No hay salas disponibles")
        }
    }

    fun listenToRoom(roomCode: String, onUpdate: (RoomModel) -> Unit) {
        try {
            roomsRef.child(roomCode).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val room = snapshot.getValue(RoomModel::class.java)
                    if (room != null) {
                        activeRoomsLocal[roomCode] = room
                        onUpdate(room)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Listener cancelled for $roomCode", error.toException())
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "Realtime listener offline, returning local state", e)
            activeRoomsLocal[roomCode]?.let(onUpdate)
        }
    }
}
