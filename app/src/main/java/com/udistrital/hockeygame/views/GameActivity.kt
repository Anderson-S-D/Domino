package com.udistrital.hockeygame.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.udistrital.hockeygame.data.enums.TypeScreen
import com.udistrital.hockeygame.viewmodels.GameViewModel
import com.udistrital.hockeygame.viewmodels.RoomViewModel
import com.udistrital.hockeygame.views.screens.GameScreen
import com.udistrital.hockeygame.views.screens.HomeScreen
import com.udistrital.hockeygame.views.screens.LobbyScreen
import com.udistrital.hockeygame.views.screens.RoomSelectionScreen
import com.udistrital.hockeygame.views.theme.HockeyGameTheme

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HockeyGameTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var currentScreen by remember { mutableStateOf(TypeScreen.HOME) }

                    val roomViewModel: RoomViewModel by viewModels()
                    val gameViewModel: GameViewModel by viewModels()

                    val roomModel by roomViewModel.model.collectAsState()
                    val roomError by roomViewModel.error.collectAsState()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            TypeScreen.HOME -> {
                                HomeScreen(
                                    onClickGame = {
                                        gameViewModel.startNewGame()
                                        currentScreen = TypeScreen.GAME
                                    },
                                    onClickMultiplayer = {
                                        roomViewModel.clearError()
                                        currentScreen = TypeScreen.ROOM_SELECTION
                                    }
                                )
                            }

                            TypeScreen.ROOM_SELECTION -> {
                                RoomSelectionScreen(
                                    onCreateRoomClick = {
                                        roomViewModel.createRoom()
                                        currentScreen = TypeScreen.LOBBY
                                    },
                                    onJoinByCodeClick = { code ->
                                        roomViewModel.joinRoomByCode(code) { success ->
                                            if (success) {
                                                currentScreen = TypeScreen.LOBBY
                                            }
                                        }
                                    },
                                    onJoinOpenRoomClick = {
                                        roomViewModel.joinOpenRoom { success ->
                                            if (success) {
                                                currentScreen = TypeScreen.LOBBY
                                            }
                                        }
                                    },
                                    errorMessage = roomError
                                )
                            }

                            TypeScreen.LOBBY -> {
                                LobbyScreen(
                                    roomCode = roomModel?.code ?: roomModel?.id ?: "A7K3Q9",
                                    hostName = roomModel?.player1?.getVisibleName() ?: "Anfitrión",
                                    guestName = roomModel?.player2?.getVisibleName(),
                                    onCancelClick = {
                                        currentScreen = TypeScreen.ROOM_SELECTION
                                    },
                                    onStartGameClick = {
                                        val p1 = roomModel?.player1
                                        val p2 = roomModel?.player2
                                        gameViewModel.startNewGame(
                                            roomId = roomModel?.code ?: "room-1",
                                            p1Id = p1?.uid ?: "player1",
                                            p1Name = p1?.getVisibleName() ?: "Jugador 1",
                                            p2Id = p2?.uid ?: "player2",
                                            p2Name = p2?.getVisibleName() ?: "Jugador 2"
                                        )
                                        currentScreen = TypeScreen.GAME
                                    }
                                )
                            }

                            TypeScreen.GAME -> {
                                GameScreen(
                                    gameViewModel = gameViewModel,
                                    onBackToHome = {
                                        currentScreen = TypeScreen.HOME
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
