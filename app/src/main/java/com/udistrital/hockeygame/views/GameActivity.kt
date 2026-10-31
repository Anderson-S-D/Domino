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
import com.udistrital.hockeygame.views.screens.GameScreen
import com.udistrital.hockeygame.views.screens.HomeScreen
import com.udistrital.hockeygame.data.enums.TypeScreen
import com.udistrital.hockeygame.viewmodels.RoomViewModel
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
                    val model by roomViewModel.model.collectAsState()

                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        when (currentScreen) {
                            TypeScreen.HOME -> {
                                HomeScreen(
                                    onClickGame = {
                                        currentScreen = TypeScreen.GAME
                                    },
                                    onClickMultiplayer = {
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
                                    onJoinOpenRoomClick = {
                                        roomViewModel.joinOpenRoom()
                                        currentScreen = TypeScreen.LOBBY
                                    }

                                )
                            }

                            TypeScreen.LOBBY -> {
                                LobbyScreen(
                                    roomCode = model?.id,
                                    hostName = model?.player1?.email ?: "",
                                    guestName = model?.player2?.email,
                                    onCancelClick = {
                                        currentScreen = TypeScreen.ROOM_SELECTION
                                    },
                                    onStartGameClick = {
                                        currentScreen = TypeScreen.GAME
                                    }
                                )
                            }

                            TypeScreen.GAME -> {
                                GameScreen {
                                    currentScreen = TypeScreen.HOME
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}