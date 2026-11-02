package com.udistrital.hockeygame.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.udistrital.hockeygame.data.models.PlayerModel
import com.udistrital.hockeygame.data.models.RoomModel
import com.udistrital.hockeygame.repositories.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RoomViewModel : ViewModel() {
    private val repository = RoomRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _model = MutableStateFlow<RoomModel?>(null)
    val model: StateFlow<RoomModel?> = _model.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun createRoom() {
        val currentUser = auth.currentUser
        val host = PlayerModel(
            uid = currentUser?.uid ?: "local-host-id",
            email = currentUser?.email.orEmpty(),
            name = currentUser?.displayName.orEmpty()
        )
        val room = repository.createRoom(host)
        _model.value = room
        _error.value = null

        repository.listenToRoom(room.code) { updated ->
            _model.value = updated
        }
    }

    fun joinRoomByCode(code: String, onComplete: (Boolean) -> Unit) {
        if (code.isBlank()) {
            _error.value = "Ingresa el código de la sala"
            onComplete(false)
            return
        }

        _isLoading.value = true
        _error.value = null

        val currentUser = auth.currentUser
        val guest = PlayerModel(
            uid = currentUser?.uid ?: "local-guest-id",
            email = currentUser?.email.orEmpty(),
            name = currentUser?.displayName.orEmpty()
        )

        repository.joinRoomByCode(code, guest) { room, errorMsg ->
            _isLoading.value = false
            if (room != null) {
                _model.value = room
                repository.listenToRoom(room.code) { updated ->
                    _model.value = updated
                }
                onComplete(true)
            } else {
                _error.value = errorMsg ?: "No se pudo ingresar a la sala"
                onComplete(false)
            }
        }
    }

    fun joinOpenRoom(onComplete: (Boolean) -> Unit = {}) {
        _isLoading.value = true
        _error.value = null

        val currentUser = auth.currentUser
        val guest = PlayerModel(
            uid = currentUser?.uid ?: "local-guest-id",
            email = currentUser?.email.orEmpty(),
            name = currentUser?.displayName.orEmpty()
        )

        repository.joinOpenRoom(guest) { room, errorMsg ->
            _isLoading.value = false
            if (room != null) {
                _model.value = room
                repository.listenToRoom(room.code) { updated ->
                    _model.value = updated
                }
                onComplete(true)
            } else {
                _error.value = errorMsg ?: "No hay salas disponibles"
                onComplete(false)
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
