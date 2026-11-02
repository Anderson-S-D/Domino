package com.udistrital.hockeygame.views.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udistrital.hockeygame.views.theme.*

@Composable
fun RoomSelectionScreen(
    onCreateRoomClick: () -> Unit = {},
    onJoinByCodeClick: (String) -> Unit = {},
    onJoinOpenRoomClick: () -> Unit = {},
    errorMessage: String? = null
) {
    var roomCodeInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CasinoDarkFelt)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.MeetingRoom,
                contentDescription = null,
                tint = CasinoGold,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Salas de Juego",
                color = CasinoTextWhite,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Crea tu propia sala o ingresa un código",
                color = CasinoTextMuted,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CasinoEmerald),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (errorMessage != null) {
                        Surface(
                            color = CasinoRed,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                color = CasinoTextWhite,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(10.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = onCreateRoomClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CasinoGold,
                            contentColor = CasinoDarkFelt
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = CasinoDarkFelt,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "CREAR NUEVA SALA",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = CasinoTextMuted.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "O UNIRSE CON CÓDIGO",
                            color = CasinoGoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = CasinoTextMuted.copy(alpha = 0.3f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = roomCodeInput,
                        onValueChange = { roomCodeInput = it.uppercase() },
                        label = { Text("Código de sala (ej. A7K3Q9)", color = CasinoTextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = CasinoGold)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CasinoGold,
                            unfocusedBorderColor = CasinoTextMuted.copy(alpha = 0.5f),
                            focusedTextColor = CasinoTextWhite,
                            unfocusedTextColor = CasinoTextWhite
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onJoinByCodeClick(roomCodeInput) },
                        enabled = roomCodeInput.trim().length >= 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CasinoEmeraldLight,
                            contentColor = CasinoDarkFelt
                        )
                    ) {
                        Text(
                            text = "UNIRSE A SALA",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = onJoinOpenRoomClick) {
                        Text(
                            text = "Buscar cualquier sala pública disponible",
                            color = CasinoGoldLight,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RoomSelectionScreenPreview() {
    RoomSelectionScreen()
}
