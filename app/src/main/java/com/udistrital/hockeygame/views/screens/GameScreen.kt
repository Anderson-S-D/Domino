package com.udistrital.hockeygame.views.screens

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udistrital.hockeygame.R
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlin.math.sqrt

@Composable
fun GameScreen(
    onBack: () -> Unit
) {

    val context = LocalContext.current

    var gyroX by remember { mutableFloatStateOf(0f) }
    var gyroY by remember { mutableFloatStateOf(0f) }
    var gyroZ by remember { mutableFloatStateOf(0f) }

    var accelX by remember { mutableFloatStateOf(0f) }
    var accelY by remember { mutableFloatStateOf(0f) }
    var accelZ by remember { mutableFloatStateOf(0f) }

    val sensorManager = remember {
        context.getSystemService(SensorManager::class.java)
    }

    DisposableEffect(sensorManager) {

        val listener = object : SensorEventListener {

            override fun onSensorChanged(event: SensorEvent) {

                when (event.sensor.type) {

                    Sensor.TYPE_GYROSCOPE -> {
                        gyroX = event.values[0]
                        gyroY = event.values[1]
                        gyroZ = event.values[2]
                    }

                    Sensor.TYPE_ACCELEROMETER -> {
                        accelX = event.values[0]
                        accelY = event.values[1]
                        accelZ = event.values[2]
                    }
                }
            }

            override fun onAccuracyChanged(
                sensor: Sensor?,
                accuracy: Int
            ) {
            }
        }

        sensorManager.registerListener(
            listener,
            sensorManager.getDefaultSensor(
                Sensor.TYPE_GYROSCOPE
            ),
            SensorManager.SENSOR_DELAY_GAME
        )

        sensorManager.registerListener(
            listener,
            sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER
            ),
            SensorManager.SENSOR_DELAY_GAME
        )

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    var playerX by remember {
        mutableFloatStateOf(0f)
    }

    LaunchedEffect(accelX) {
        playerX += -accelX * 5f
    }

    var ballX by remember {
        mutableFloatStateOf(0f)
    }

    var ballY by remember {
        mutableFloatStateOf(0f)
    }

    var ballVelocityX by remember {
        mutableFloatStateOf(0f)
    }

    var ballVelocityY by remember {
        mutableFloatStateOf(0f)
    }

    var canvasWidth by remember {
        mutableFloatStateOf(0f)
    }

    var canvasHeight by remember {
        mutableFloatStateOf(0f)
    }

    var ballInitialized by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            if (canvasWidth > 0f && canvasHeight > 0f) {
                if (!ballInitialized) {

                    ballX = canvasWidth / 2f
                    ballY = canvasHeight / 2f

                    ballVelocityX = 0f
                    ballVelocityY = 7f

                    ballInitialized = true
                }

                ballX += ballVelocityX
                ballY += ballVelocityY

                val ballRadius = 60f
                val playerRadius = 80f

                val playerY = canvasHeight - 180f

                val playerXLimitado = playerX.coerceIn(
                    100f,
                    canvasWidth - 100f
                )

                if (ballX - ballRadius <= 0f) {
                    ballX = ballRadius
                    ballVelocityX = -ballVelocityX
                }

                if (ballX + ballRadius >= canvasWidth) {
                    ballX = canvasWidth - ballRadius
                    ballVelocityX = -ballVelocityX
                }

                if (ballY - ballRadius <= 0f) {
                    onBack()
                }

                val distanciaJugador = sqrt(
                    (ballX - playerXLimitado) *
                            (ballX - playerXLimitado) +
                            (ballY - playerY) *
                            (ballY - playerY)
                )

                if (distanciaJugador <= ballRadius + playerRadius && ballVelocityY > 0f) {
                    ballY =
                        playerY -
                                playerRadius -
                                ballRadius
                    ballVelocityY = -ballVelocityY

                    val diferenciaX =
                        ballX - playerXLimitado

                    ballVelocityX =
                        (diferenciaX * 0.08f)
                            .coerceIn(-8f, 8f)
                }

                val enemyY = 180f
                val enemyX = canvasWidth / 2f

                val distanciaEnemigo = sqrt(
                    (ballX - enemyX) *
                            (ballX - enemyX) +
                            (ballY - enemyY) *
                            (ballY - enemyY)
                )

                if (distanciaEnemigo <= ballRadius + playerRadius && ballVelocityY < 0f) {

                    ballY = enemyY + playerRadius + ballRadius

                    ballVelocityY = -ballVelocityY

                    val diferenciaX =
                        ballX - enemyX

                    ballVelocityX =
                        (diferenciaX * 0.08f)
                            .coerceIn(-8f, 8f)
                }

                if (ballY + ballRadius >= canvasHeight) {
                    onBack()
                }
            }

            delay(5L)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {

            canvasWidth = size.width
            canvasHeight = size.height

            val centroX = size.width / 2f

            val playerY = size.height - 180f

            val playerXLimitado = playerX.coerceIn(
                100f,
                size.width - 100f
            )

            drawRect(
                color = Color(0xFF3498DB),
                topLeft = Offset.Zero,
                size = size
            )

            drawCircle(
                color = Color(0xFFE74C3C),
                radius = 80f,
                center = Offset(
                    playerXLimitado,
                    playerY
                )
            )

            drawCircle(
                color = Color(0xFFC0392B),
                radius = 80f,
                center = Offset(
                    playerXLimitado,
                    playerY
                ),
                style = Stroke(width = 8f)
            )

            drawCircle(
                color = Color.Black,
                radius = 60f,
                center = Offset(
                    ballX,
                    ballY
                )
            )

            drawCircle(
                color = Color(0xFF212121),
                radius = 48f,
                center = Offset(
                    ballX,
                    ballY
                )
            )

            val enemyY = 180f

            drawCircle(
                color = Color(0xFF2ECC71),
                radius = 80f,
                center = Offset(
                    centroX,
                    enemyY
                )
            )

            drawCircle(
                color = Color(0xFF27AE60),
                radius = 80f,
                center = Offset(
                    centroX,
                    enemyY
                ),
                style = Stroke(width = 8f)
            )
        }

        Text(
            text = """
                GIROSCOPIO
                
                X: %.2f
                Y: %.2f
                Z: %.2f
                
                ACELERÓMETRO
                
                X: %.2f
                Y: %.2f
                Z: %.2f
            """.trimIndent().format(
                gyroX,
                gyroY,
                gyroZ,
                accelX,
                accelY,
                accelZ
            ),
            modifier = Modifier.align(
                Alignment.TopStart
            ),
            color = Color.White,
            fontSize = 16.sp
        )

        Button(
            onClick = {
                onBack()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .width(120.dp)
                .height(48.dp)
                .shadow(
                    10.dp,
                    RoundedCornerShape(18.dp)
                ),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00AEFF)
            )
        ) {

            Text(
                text = stringResource(
                    R.string.home_btn_back
                ),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Preview
@Composable
fun GameScreenPreview(){
    GameScreen {  }
}