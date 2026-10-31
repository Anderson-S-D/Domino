# HockeyGame — Análisis técnico y guía para construir prompts

> Documento de ingeniería inversa del repositorio, elaborado el **2026-10-29**.
> Objetivo: extraer contexto verificable para generar prompts especializados que permitan **modificar, completar o amplificar** el proyecto sin romper lo existente.

---

## 1. Resumen ejecutivo

`HockeyGame` es una aplicación Android nativa en **Kotlin + Jetpack Compose + Firebase** que hoy implementa un prototipo de **aire hockey** (paleta controlada por acelerómetro, pelota con rebotes y un rival estático) envuelto en un flujo de autenticación y salas multijugador.

El repositorio se llama `domino-SAA` y el PDF del curso pide un **Dominó “Caos”** con turnos, fichas, poderes, eventos aleatorios, puntaje y sincronización en tiempo real. Por tanto, el proyecto actual es un **andamiaje válido de autenticación + salas + UI**, pero **no implementa el juego solicitado**.

Fortalezas aprovechables:

- Compila correctamente (verificado con `:app:compileDebugKotlin`).
- Ya tiene Firebase Auth con email/contraseña y sesión persistente.
- Ya tiene Firebase Realtime Database con creación/unión a salas.
- Ya existe una base visual coherente y reutilizable (paleta, tarjetas, botones, tipografías).
- La navegación por estados es simple y fácil de reemplazar por Navigation Compose.

Brechas críticas:

- No existe motor de dominó, fichas, tablero, validación de movimientos, poderes, eventos, puntaje ni fin de partida.
- La partida **no se sincroniza** en Firebase; solo se crea/une la sala.
- El lobby del anfitrión **no se actualiza en tiempo real** cuando entra el invitado.
- No hay registro de usuarios (el botón “Regístrate” no hace nada).
- No hay separación real de lógica de juego e interfaz; la física vive dentro de un `@Composable`.
- Los tests son plantillas vacías.

---

## 2. Contexto académico: qué se pide vs. qué existe

### 2.1 Requisitos del taller (PDF “Taller 2 301”)

Aplicación Android en Kotlin para jugar **Dominó “Caos”**:

- Cada jugador inicia con **7 fichas**.
- Partida **por turnos**; gana quien tenga más puntaje al final.
- Validación de movimientos según reglas del dominó.
- **Poderes** con usos limitados:
  - **Comodín:** colocar una ficha sin cumplir uno de los extremos.
  - **Salto:** el siguiente jugador pierde su turno.
  - **Intercambio:** intercambiar una ficha propia con una del oponente.
  - **Inversión:** cambiar el sentido de los turnos.
- **Eventos aleatorios** que alteran fichas, turno, puntaje o pozo.
- Mostrar permanentemente turno y puntaje.
- Si no hay movimiento válido: robar del pozo; si no quedan fichas, perder turno.
- Fin de partida: sin fichas, sin movimientos posibles o máximo de rondas.
- Desempate: gana quien usó menos poderes.
- **Sincronización en tiempo real con Firebase**.
- Gestión de usuario opcional con Google; login y creación de cuenta obligatorios en la métrica.
- Componentes reutilizables para tablero, fichas, jugadores, poderes y controles.
- **MVVM** y separación lógica/UI.
- Mínimo **1 poder** (aunque el mockup muestra los 4).
- Informe IEEE, pruebas unitarias y código limpio.

### 2.2 Pantallas esperadas según los mockups (texto extraído por OCR)

| # | Pantalla | Elementos detectados |
|---|----------|----------------------|
| 1 | Inicio de sesión | Logo “Dominó Caos”, iniciar sesión, continuar con Google, crear cuenta |
| 2 | Registro | Nombre, correo, confirmar contraseña, registrarse, volver a login |
| 3 | Menú principal / Lobby | Nueva partida, unirse con código, partidas disponibles, historial/estadísticas, saldo (ej. $1,250), ronda 5/10 |
| 4 | Crear partida | Modo de juego, número de jugadores, número de rondas (10), crear partida |
| 5 | Sala de espera | Código de sala (ej. A7K3), cancelar, esperar jugador |
| 6 | Tablero | Ronda, saldo, historial, terminar turno, zona de poderes |
| 7 | Selección de poderes | Comodín, Salto, Intercambio, Inversión, descripción de cada uno |
| 8 | Evento aleatorio | “Tormenta de fichas”: todos roban una ficha del pozo; botón continuar |
| 9 | Turno con poder aplicado | Mensaje “Has intercambiado una ficha con tu oponente”, terminar turno |
| 10 | Resultado | Ganaste, fichas colocadas, poderes usados, dinero final, jugar de nuevo, volver al menú |
| 11 | Historial | Lista de partidas con fecha, rondas, resultado y ganancia |

> Nota: los mockups son imágenes dentro del PDF; se recuperaron sus etiquetas mediante OCR. La composición visual exacta no fue interpretada, solo el texto y la estructura de pantallas.

### 2.3 Brecha funcional actual

| Requisito | Estado | Evidencia |
|-----------|--------|-----------|
| Login email/contraseña | ✅ Implementado | `AuthActivity.kt` |
| Registro de cuenta | ❌ No implementado | `LoginScreen.onRegisterClick = {}` |
| Continuar con Google | ❌ No implementado | Sin `GoogleSignInClient` ni credenciales |
| Crear/unirse a sala | ⚠️ Parcial | `RoomRepository` crea y une, sin transacción |
| Lobby en tiempo real | ❌ No implementado | No hay `addValueEventListener` |
| Tablero de dominó | ❌ No existe | Solo `Canvas` de hockey |
| Fichas / pozo / mano | ❌ No existe | Sin modelos |
| Validación de movimientos | ❌ No existe | Sin dominio |
| Turnos | ❌ No aplicado | `turnPlayerUid` existe pero no se usa |
| Poderes | ❌ No existe | Sin modelos ni UI |
| Eventos aleatorios | ❌ No existe | Sin modelos ni UI |
| Puntaje | ❌ No existe | Sin campos en el modelo |
| Fin de partida / ganador | ❌ No existe | `winnerPlayerUid` y `finished` sin uso |
| Sincronización de partida | ❌ No existe | Solo se sincroniza la sala |
| MVVM | ⚠️ Parcial | Solo `RoomViewModel`; juego en el composable |
| Componentes reutilizables | ⚠️ Parcial | Pantallas completas, sin átomos/moléculas |
| Pruebas unitarias | ❌ Plantillas | `ExampleUnitTest`, `ExampleInstrumentedTest` |
| Informe IEEE | ❌ No existe | Sin carpeta de documentación |

---

## 3. Ficha técnica

| Aspecto | Valor |
|---------|-------|
| Repositorio | `https://github.com/stephannynayleth01-coder/domino-SAA.git` |
| Módulo Android | `HockeyGame/app` |
| `namespace` / `applicationId` | `com.udistrital.hockeygame` |
| Lenguaje | Kotlin `2.4.21` |
| UI | Jetpack Compose + Material 3 |
| Compose BOM | `2026.02.01` |
| AGP | `9.3.2` |
| Gradle | `9.5.0` |
| `compileSdk` / `targetSdk` | `37` |
| `minSdk` | `24` |
| Java source/target | `11` |
| Firebase BOM | `35.0.2` |
| Servicios Firebase | Auth (email/password) + Realtime Database |
| Proyecto Firebase | `hockeygame-86dfd` |
| Persistencia local | Ninguna |
| Navegación | `enum TypeScreen` + estado en `GameActivity` |
| DI | Ninguna (repositorio instanciado en el ViewModel) |
| Tests | JUnit4 + Espresso (solo plantillas) |
| Estado de compilación | ✅ `:app:compileDebugKotlin` exitoso |

### Dependencias declaradas

```kotlin
implementation(platform(libs.firebase.bom))
implementation(platform(libs.androidx.compose.bom))
implementation(libs.androidx.activity.ktx)
implementation(libs.androidx.appcompat)
implementation(libs.firebase.auth)
implementation(libs.firebase.database)
implementation(libs.androidx.activity.compose)
implementation(libs.androidx.compose.material3)
implementation(libs.androidx.compose.material.icons.core)
implementation(libs.androidx.compose.material.icons.extended)
implementation(libs.androidx.compose.ui)
implementation(libs.androidx.compose.ui.graphics)
implementation(libs.androidx.compose.ui.text)
implementation(libs.androidx.compose.ui.tooling.preview)
implementation(libs.androidx.core.ktx)
implementation(libs.androidx.lifecycle.runtime.ktx)
implementation(libs.material)
testImplementation(libs.junit)
androidTestImplementation(libs.androidx.compose.ui.test.junit4)
androidTestImplementation(libs.androidx.espresso.core)
androidTestImplementation(libs.androidx.junit)
```

No hay: Navigation Compose, Hilt/Koin, Room, DataStore, coroutines-test, MockK, Turbine, Robolectric, Firebase UI, Google Play Services Auth.

---

## 4. Estructura del proyecto

```text
HockeyGame/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
└── app/
    ├── build.gradle.kts
    ├── google-services.json
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── keepRules/rules.keep
        │   ├── res/values/{strings,colors,themes}.xml
        │   └── java/com/udistrital/hockeygame/
        │       ├── data/
        │       │   ├── enums/TypeScreen.kt
        │       │   └── models/{PlayerModel,RoomModel}.kt
        │       ├── repositories/RoomRepository.kt
        │       ├── viewmodels/RoomViewModel.kt
        │       └── views/
        │           ├── AuthActivity.kt
        │           ├── GameActivity.kt
        │           ├── screens/
        │           │   ├── HomeScreen.kt
        │           │   ├── LoginScreen.kt
        │           │   ├── LobbyScreen.kt
        │           │   ├── RoomSelectionScreen.kt
        │           │   └── GameScreen.kt
        │           └── theme/{Color,Theme,Type}.kt
        ├── test/.../ExampleUnitTest.kt
        └── androidTest/.../ExampleInstrumentedTest.kt
```

Tamaño aproximado: **~1,400 líneas Kotlin** (sin contar recursos ni wrapper). Es un proyecto pequeño, lo que permite refactorizaciones profundas sin conflicto.

---

## 5. Arquitectura actual

```text
┌──────────────────────────────┐
│ AuthActivity (launcher)      │
│  FirebaseAuth.signIn...      │
└──────────────┬───────────────┘
               │ Intent
               ▼
┌──────────────────────────────┐
│ GameActivity                 │
│  Scaffold + HockeyGameTheme  │
│  TypeScreen (estado local)   │
│  RoomViewModel (activity)    │
└───────┬──────────────────────┘
        │ when(currentScreen)
        ├── HOME ──────────────► HomeScreen
        ├── ROOM_SELECTION ────► RoomSelectionScreen
        ├── LOBBY ─────────────► LobbyScreen
        └── GAME ──────────────► GameScreen
                                   │
                                   └── física + Canvas + sensores

RoomViewModel ──► RoomRepository ──► Firebase Realtime Database
```

### Lectura arquitectónica

- **Capa de presentación:** composables en `views/screens`, con estado interno en varios de ellos (`LoginScreen`, `GameScreen`). No hay `UiState` por pantalla.
- **Capa de estado:** solo `RoomViewModel`; expone `StateFlow<RoomModel?>`. No hay ViewModel para autenticación, lobby, partida ni historial.
- **Capa de datos:** `RoomRepository` accede directamente a Firebase. No hay interfaces ni fuentes de datos alternativas.
- **Capa de dominio:** **inexistente**. No hay entidades de juego, reglas, casos de uso ni validadores.
- **Navegación:** máquina de estados manual con `enum TypeScreen`, sin back stack, sin argumentos y sin `rememberSaveable`.
- **Inyección de dependencias:** manual; `RoomViewModel` crea `RoomRepository()` y obtiene `FirebaseAuth` como singleton.

Esto cumple solo parcialmente el requisito MVVM: hay ViewModel para salas, pero el juego y la autenticación están acoplados a Activities/Composables.

---

## 6. Inventario archivo por archivo

### 6.1 `views/AuthActivity.kt` (57 líneas)

**Rol:** pantalla de entrada y autenticación.

**Comportamiento:**

- `auth = Firebase.auth` en `onCreate`.
- `onStart()` comprueba `auth.currentUser`; si existe, llama `goToGame()`.
- `LoginScreen(onLoginClick = { email, password -> auth.signInWithEmailAndPassword(...) })`.
- En éxito: log + `goToGame()`.
- En error: log + `Toast` “Authentication failed.”.
- `goToGame()` lanza `GameActivity` sin `finish()` ni flags.

**Problemas:**

- No hay registro ni recuperación de contraseña.
- No se valida email/contraseña antes de llamar a Firebase.
- `Toast` en inglés y sin detalle del error.
- Al volver atrás desde `GameActivity`, se regresa a `AuthActivity` (no se hace `finish()`).
- `onStart` puede lanzar la actividad repetidamente si el usuario ya está autenticado y la actividad se reanuda.
- No hay cierre de sesión.

### 6.2 `views/GameActivity.kt` (91 líneas)

**Rol:** contenedor principal y router de pantallas.

**Comportamiento:**

- `enableEdgeToEdge()`.
- `HockeyGameTheme { Scaffold { ... } }`.
- `var currentScreen by remember { mutableStateOf(TypeScreen.HOME) }`.
- `val roomViewModel: RoomViewModel by viewModels()` (scope de Activity).
- `val model by roomViewModel.model.collectAsState()`.
- `when (currentScreen)` renderiza `HomeScreen`, `RoomSelectionScreen`, `LobbyScreen` o `GameScreen`.

**Problemas:**

- El estado de navegación se pierde al rotar (no `rememberSaveable`).
- El `RoomViewModel` sobrevive a la rotación, pero la pantalla vuelve a `HOME`: inconsistencia.
- No hay back stack ni manejo del botón atrás.
- No hay pantalla de registro, historial, resultado ni partida.
- `collectAsState()` no es lifecycle-aware; debería ser `collectAsStateWithLifecycle()`.
- `LobbyScreen` recibe `roomCode`, `hostName` y `guestName` derivados del modelo, pero el modelo no se actualiza desde Firebase.

### 6.3 `views/screens/LoginScreen.kt` (184 líneas)

**Rol:** formulario de login Compose.

**API:**

```kotlin
@Composable
fun LoginScreen(
    onLoginClick: (email: String, password: String) -> Unit,
    onRegisterClick: () -> Unit = {}
)
```

**Estado local:** `email`, `password`, `isPasswordVisible`.

**UI:**

- Fondo `#0F172A`, icono `SportsHockey`, título “Bienvenido”.
- Card translúcida con dos `OutlinedTextField`.
- Botón “INGRESAR” azul `#00AEFF`.
- `TextButton` “¿No tienes cuenta? Regístrate aquí”.

**Problemas:**

- Sin validación, sin estado de carga, sin mensaje de error inline.
- `onRegisterClick` por defecto vacío; en `AuthActivity` no se pasa, así que el botón no hace nada.
- Estado no guardado con `rememberSaveable`.
- Textos hardcodeados; no usa `strings.xml`.

### 6.4 `views/screens/HomeScreen.kt` (180 líneas)

**Rol:** menú principal.

**API:**

```kotlin
@Composable
fun HomeScreen(
    onClickGame: () -> Unit,
    onClickMultiplayer: () -> Unit = {}
)
```

**UI:**

- Logo, título `Welcome to Hockey Game`, subtítulo en inglés.
- Card “ESTADÍSTICAS” con `home_score` (“Your last score is”), sin valor real.
- Botón “Play” (azul) y “MULTIPLAYER” (naranja).
- Footer “Components 301 • 2026 • Hockey Game”.

**Problemas:**

- No muestra estadísticas reales ni historial.
- El botón Play inicia directamente el hockey; no hay configuración de partida.
- Mezcla inglés/español.
- No hay saldo, rondas ni perfil, a diferencia del mockup.

### 6.5 `views/screens/RoomSelectionScreen.kt` (155 líneas)

**Rol:** elegir crear sala o unirse a una abierta.

**API:**

```kotlin
@Composable
fun RoomSelectionScreen(
    onCreateRoomClick: () -> Unit = {},
    onJoinOpenRoomClick: () -> Unit = {}
)
```

**UI:** icono `MeetingRoom`, título “Salas de Juego”, card con “CREAR SALA” y “BUSCAR Y UNIRSE”.

**Problemas:**

- No hay campo para ingresar un código de sala (el mockup sí lo pide).
- “Buscar y unirse” no muestra carga, error ni lista de salas.
- No hay concepto de sala propia vs. ajena.

### 6.6 `views/screens/LobbyScreen.kt` (241 líneas)

**Rol:** sala de espera.

**API:**

```kotlin
@Composable
fun LobbyScreen(
    roomCode: String? = "HK89A2",
    hostName: String = "Jugador 1",
    guestName: String? = null,
    onCancelClick: () -> Unit = {},
    onStartGameClick: () -> Unit = {}
)
```

**UI:**

- Código de sala copiable (`LocalClipboardManager`).
- Card de jugadores: anfitrión + invitado (spinner si `guestName == null`).
- Botón “INICIAR PARTIDA” habilitado solo si hay invitado.
- “Cancelar y salir”.

**Problemas:**

- **No escucha cambios de Firebase**: el anfitrión nunca ve entrar al invitado.
- El código por defecto de preview `HK89A2` no refleja el código real (push key de 20 caracteres).
- `hostName` usa el email, no un nombre visible.
- No hay estado de “sala llena”, “sala cancelada” ni expiración.
- No hay listener que limpiar porque no existe.

### 6.7 `views/screens/GameScreen.kt` (378 líneas)

**Rol:** juego de aire hockey con sensores y `Canvas`.

**API:**

```kotlin
@Composable
fun GameScreen(onBack: () -> Unit)
```

**Estado:**

- `gyroX/Y/Z`, `accelX/Y/Z` (`mutableFloatStateOf`).
- `playerX`.
- `ballX`, `ballY`, `ballVelocityX`, `ballVelocityY`.
- `canvasWidth`, `canvasHeight`, `ballInitialized`.

**Sensores:**

- `SensorManager` + `DisposableEffect`.
- Registra `TYPE_GYROSCOPE` y `TYPE_ACCELEROMETER` con `SENSOR_DELAY_GAME`.
- `onDispose` desregistra el listener.
- El giroscopio solo se muestra en pantalla; no se usa para jugar.
- El acelerómetro mueve la paleta: `playerX += -accelX * 5f` dentro de `LaunchedEffect(accelX)`.

**Loop:**

```kotlin
LaunchedEffect(Unit) {
    while (isActive) {
        if (canvasWidth > 0f && canvasHeight > 0f) {
            if (!ballInitialized) { center + velocityY = 7f }
            ballX += ballVelocityX
            ballY += ballVelocityY
            // rebotes laterales, colisión jugador, colisión enemigo, fin si sale arriba/abajo
        }
        delay(5L)
    }
}
```

**Constantes físicas:**

| Elemento | Valor |
|----------|-------|
| Radio pelota | `60f` px |
| Radio paleta | `80f` px |
| Y jugador | `canvasHeight - 180f` |
| Y enemigo | `180f` |
| X enemigo | `canvasWidth / 2f` |
| Límite jugador | `100f .. canvasWidth - 100f` |
| Velocidad inicial pelota | `(0, 7f)` |
| Rebote jugador | invierte `velocityY`; `velocityX = (dx * 0.08f).coerceIn(-8f, 8f)` |
| Loop | `delay(5L)` (~200 FPS nominales, no ligado al frame real) |

**Problemas:**

- No hay puntaje, rondas, turnos, fichas, poderes ni eventos.
- El enemigo es estático y no tiene IA.
- La pelota sale por arriba o abajo y llama `onBack()`: no hay game over ni resultado.
- No hay pausa/reanudación al pasar a segundo plano.
- No hay `deltaTime`; la velocidad depende de la tasa de refresco real.
- Se usan píxeles crudos, no `dp`; se ve distinto según densidad.
- `registerListener` no valida que el sensor exista (`getDefaultSensor` puede devolver `null`).
- `LaunchedEffect(accelX)` se ejecuta en cada cambio del acelerómetro y **acumula** desplazamiento; la gravedad del sensor provoca deriva constante.
- `coerceIn(100f, canvasWidth - 100f)` puede lanzar `IllegalArgumentException` si el ancho es menor a 200 px (preview o pantalla muy pequeña).
- Escribir `canvasWidth`/`canvasHeight` dentro del `Canvas` es un anti-patrón (estado mutado en fase de dibujo).
- El texto de sensores y el botón “Back” comparten `Alignment.TopStart` y se superponen.
- No hay soporte para orientación landscape/portrait más allá del tamaño del canvas.
- No hay sonido, vibración ni feedback.

### 6.8 `repositories/RoomRepository.kt` (69 líneas)

**API:**

```kotlin
class RoomRepository {
    fun createRoom(hostPlayer: PlayerModel): RoomModel?
    fun joinOpenRoom(guestPlayer: PlayerModel, onResult: (RoomModel?) -> Unit)
}
```

**Implementación:**

- `database = FirebaseDatabase.getInstance()`.
- `roomsRef = database.getReference("rooms")`.
- `createRoom`: `push()` para obtener key; construye `RoomModel`; `setValue(room)`; retorna el objeto local.
- `joinOpenRoom`: `orderByChild("startAt").addListenerForSingleValueEvent(...)`; recorre hijos; elige el último con `player2 == null && !finished`; actualiza `player2` y `turnPlayerUid` con dos `setValue` separados.

**Problemas:**

- No usa transacción: dos invitados pueden unirse a la misma sala.
- El `setValue` de `createRoom` no tiene callback de error/completado; se retorna la sala antes de confirmar la escritura.
- `startAt` es `String` de milisegundos; ordenar por texto funciona por longitud fija, pero es frágil. Debería ser `Long` + `ServerValue.TIMESTAMP`.
- `orderByChild("startAt")` sin índice en reglas de Firebase.
- `targetRoom` se sobrescribe en cada iteración: se queda con la **última** sala abierta, no con la primera.
- No se evita que el anfitrión se una a su propia sala.
- No hay limpieza de salas viejas/abandonadas.
- No hay listener en tiempo real; solo lectura puntual.
- No hay `removeEventListener` porque no hay listeners persistentes.
- No hay manejo de errores de escritura ni de desconexión.

### 6.9 `viewmodels/RoomViewModel.kt` (38 líneas)

**API:**

```kotlin
class RoomViewModel : ViewModel() {
    val model: StateFlow<RoomModel?>
    fun createRoom()
    fun joinOpenRoom()
}
```

**Comportamiento:**

- Obtiene `auth.currentUser`; si es null, retorna sin feedback.
- Construye `PlayerModel(uid, email)`.
- `createRoom`: asigna el resultado a `_model`.
- `joinOpenRoom`: callback asigna `_model`.

**Problemas:**

- No hay `UiState` con loading/error.
- No hay `viewModelScope` ni operaciones suspendidas.
- El callback de Firebase puede llegar después de que el ViewModel se destruya.
- No hay métodos para escuchar la sala, cancelar, iniciar partida, salir o actualizar.
- No hay separación de casos de uso.

### 6.10 `data/models/RoomModel.kt` (11 líneas)

```kotlin
data class RoomModel(
    var id: String? = "",
    val player1: PlayerModel,
    val player2: PlayerModel? = null,
    val startAt: String,
    val winnerPlayerUid: String? = "",
    val turnPlayerUid: String = "",
    val finished: Boolean = false
)
```

**Observaciones:**

- Mezcla `var`/`val`; `id` es mutable, el resto inmutable.
- No tiene campos de juego: fichas, tablero, pozo, puntaje, poderes, eventos, rondas.
- `startAt` como `String`; `winnerPlayerUid` y `finished` existen pero no se usan.
- No hay constructor sin argumentos explícito; la deserialización de Firebase con clases Kotlin sin `no-arg constructor` es un riesgo (puede depender de `UnsafeAllocator` y de campos finales). Conviene añadir valores por defecto o mapear manualmente.

### 6.11 `data/models/PlayerModel.kt` (6 líneas)

```kotlin
data class PlayerModel(
    val uid: String,
    val email: String
)
```

- Solo identidad; no hay nombre visible, avatar, saldo, estadísticas ni mano de fichas.
- `email` se usa como nombre en el lobby.

### 6.12 `data/enums/TypeScreen.kt` (8 líneas)

```kotlin
enum class TypeScreen { HOME, ROOM_SELECTION, LOBBY, GAME }
```

- Router manual. Faltan `LOGIN`, `REGISTER`, `CREATE_ROOM`, `RESULT`, `HISTORY`, `PROFILE`.

### 6.13 Tema (`views/theme/`)

- `Color.kt`: colores por defecto de plantilla (`Purple80`, `PurpleGrey80`, `Pink80`, etc.) que **no se usan** en las pantallas.
- `Theme.kt`: `HockeyGameTheme` con `dynamicColor = true` por defecto; en Android 12+ usa colores dinámicos del sistema, pero las pantallas hardcodean su propia paleta, así que el tema casi no tiene efecto.
- `Type.kt`: solo `bodyLarge`; el resto es tipografía por defecto.

### 6.14 Tests

- `ExampleUnitTest.kt`: `assertEquals(4, 2 + 2)`.
- `ExampleInstrumentedTest.kt`: verifica el nombre del paquete.
- No hay tests de ViewModel, repositorio, reglas ni UI.

### 6.15 Recursos

- `strings.xml`: solo `app_name`, `home_title`, `home_btn_play`, `home_score`, `home_btn_back`.
- `themes.xml`: `Theme.HockeyGame` hereda de `android:Theme.Material.Light.NoActionBar` (tema claro) mientras las pantallas son oscuras.
- `colors.xml`: colores de plantilla.
- `AndroidManifest.xml`: `AuthActivity` es launcher; `GameActivity` también `exported=true` sin intent-filter. No se declara `INTERNET` explícitamente (Firebase la aporta por manifest fusionado).

---

## 7. Flujo de navegación actual

```text
[AuthActivity]  (launcher)
   │ currentUser != null
   ├──────────────────────────────► [GameActivity]
   │
   └─ LoginScreen
        └─ signInWithEmailAndPassword
             ├─ éxito ────────────► [GameActivity]
             └─ error ─────────────► Toast

[GameActivity]
   HOME
    ├─ Play ─────────────────────► GAME
    └─ Multiplayer ──────────────► ROOM_SELECTION
                                      ├─ Crear sala ──► LOBBY
                                      └─ Buscar/unirse ► LOBBY
   LOBBY
    ├─ Iniciar partida (solo con invitado) ► GAME
    └─ Cancelar ─────────────────► ROOM_SELECTION
   GAME
    ├─ Back ─────────────────────► HOME
    └─ pelota fuera ─────────────► HOME
```

**Limitaciones del flujo:**

- No hay pantalla de registro.
- No hay pantalla de creación configurable (rondas, jugadores, modo).
- No hay pantalla de resultado ni historial.
- No hay navegación profunda ni deep link por código de sala.
- No hay control del botón atrás del sistema.
- No hay restauración de estado tras rotación o muerte del proceso.

---

## 8. Firebase: modelo de datos y operaciones

### 8.1 Esquema actual

```json
{
  "rooms": {
    "-Nxxxxxxxxxxxxxxxxxxx": {
      "id": "-Nxxxxxxxxxxxxxxxxxxx",
      "player1": {
        "uid": "firebase_uid_del_anfitrion",
        "email": "host@example.com"
      },
      "player2": {
        "uid": "firebase_uid_del_invitado",
        "email": "guest@example.com"
      },
      "startAt": "1760000000000",
      "winnerPlayerUid": "",
      "turnPlayerUid": "firebase_uid_del_anfitrion",
      "finished": false
    }
  }
}
```

- Ruta: `rooms/{roomId}`.
- `roomId` = key autogenerada por `push()` (20 caracteres), no un código corto.
- `player2` se omite o queda `null` cuando la sala está vacía.
- No hay nodo de partida, tablero, fichas, turnos, poderes, eventos ni puntajes.

### 8.2 Operaciones actuales

| Operación | Ruta | Implementación |
|-----------|------|----------------|
| Crear sala | `rooms/{pushId}` | `setValue(RoomModel)` |
| Buscar salas abiertas | `rooms` ordenado por `startAt` | `addListenerForSingleValueEvent` |
| Unirse | `rooms/{id}/player2` y `turnPlayerUid` | dos `setValue` |
| Escuchar lobby | — | ❌ no existe |
| Iniciar partida | — | ❌ no existe |
| Actualizar tablero | — | ❌ no existe |
| Declarar ganador | — | ❌ no existe |

### 8.3 Riesgos de Firebase

- **Sin reglas de seguridad** versionadas: cualquiera con la API key podría escribir si las reglas están en modo prueba.
- **Sin App Check**.
- **Sin transacciones** para unirse: condición de carrera.
- **Sin `ServerValue.TIMESTAMP`**: reloj del cliente.
- **Sin índices**: `orderByChild("startAt")` puede advertir y ser ineficiente.
- **Sin listeners lifecycle-aware**: el lobby no reacciona.
- **Sin `onDisconnect`**: no hay presencia ni limpieza de salas.
- **`google-services.json` versionado**: es lo normal en Android, pero la seguridad real depende de las reglas; la API key no es un secreto, pero debe restringirse.

---

## 9. Lógica de juego actual (aire hockey)

### 9.1 Variables de estado

| Variable | Tipo | Uso |
|----------|------|-----|
| `gyroX/Y/Z` | `Float` | Solo visualización |
| `accelX/Y/Z` | `Float` | `accelX` mueve la paleta |
| `playerX` | `Float` | Posición horizontal de la paleta |
| `ballX`, `ballY` | `Float` | Posición de la pelota |
| `ballVelocityX/Y` | `Float` | Velocidad por frame |
| `canvasWidth/Height` | `Float` | Dimensiones reales del Canvas |
| `ballInitialized` | `Boolean` | Inicialización única |

### 9.2 Loop y colisiones

```text
cada 5 ms:
  si canvas medido:
    si pelota no inicializada:
      ball = centro
      velocity = (0, 7)
    ball += velocity

    si ball toca borde izquierdo/derecho:
      reposicionar y invertir velocityX

    si ball toca borde superior o inferior:
      onBack()   // fin sin puntaje

    si distancia(ball, player) <= ballRadius + playerRadius && velocityY > 0:
      ballY = playerY - playerRadius - ballRadius
      velocityY = -velocityY
      velocityX = clamp((ballX - playerX) * 0.08, -8, 8)

    si distancia(ball, enemy) <= ballRadius + playerRadius && velocityY < 0:
      ballY = enemyY + playerRadius + ballRadius
      velocityY = -velocityY
      velocityX = clamp((ballX - enemyX) * 0.08, -8, 8)
```

### 9.3 Problemas de diseño del loop

- **No hay `deltaTime`:** el movimiento es por frame, no por segundo; en pantallas de 120 Hz la pelota va al doble de velocidad que en 60 Hz.
- **Acelerómetro integrado:** `playerX += -accelX * 5f` en cada evento acumula; no es una velocidad ni una posición absoluta. La gravedad y el ruido del sensor producen deriva.
- **Sin zona muerta:** cualquier vibración mueve la paleta.
- **Sin calibración:** no se resta la gravedad ni se usa la orientación.
- **Sin pausa:** el loop sigue activo si la app va a segundo plano (hasta que se destruye la composición).
- **Sin colisión continua:** a velocidades altas podría atravesar, aunque los radios grandes lo hacen improbable.
- **Sin estado de juego:** no hay `GameState`, `Score`, `Round`, `Paused`, `GameOver`.
- **Sin separación lógica/UI:** todo está en el composable, imposible de testear sin UI.

---

## 10. Sistema de diseño observado

### 10.1 Paleta

| Token implícito | Hex | Uso |
|-----------------|-----|-----|
| Fondo | `#0F172A` | Fondo de todas las pantallas |
| Primario | `#00AEFF` | Botones principales, iconos, acentos |
| Acento | `#FF9800` | Multiplayer, crear sala, códigos |
| Superficie | `#FFFFFF` al 8% | Cards |
| Texto principal | `#FFFFFF` | Títulos y cuerpo |
| Texto secundario | `#FFFFFF` al 40–80% | Subtítulos, labels |
| Pista | `#3498DB` | Fondo del Canvas |
| Jugador | `#E74C3C` / borde `#C0392B` | Paleta inferior |
| Rival | `#2ECC71` / borde `#27AE60` | Paleta superior |
| Pelota | `#000000` / `#212121` | Círculos |
| Éxito/estado | `#FF9800` | “Listo”, códigos |
| Error (no tokenizado) | `#E74C3C` | Solo Canvas |

### 10.2 Tipografía

- Títulos: `32.sp`, `FontWeight.Bold`.
- Código de sala: `28.sp`, `ExtraBold`, `letterSpacing = 2.sp`.
- Botones: `18.sp`, `Bold`.
- Cuerpo/labels: `16.sp`, `SemiBold`.
- Subtítulos: `15.sp`.
- Secundarios: `11–14.sp`.
- Footer: `bodySmall`.

### 10.3 Formas y espaciado

- Radios: `16.dp` (botones/inputs), `18.dp` (botones home), `20.dp` (cards), `24.dp` (cards grandes/logo).
- Alturas de botón: `48.dp`, `54.dp`, `56.dp`.
- Padding de pantalla: `24.dp`.
- Separaciones: `6, 8, 12, 16, 20, 24, 28, 32.dp`.
- Sombras: `8.dp` y `10.dp`.
- Cards: `CardDefaults.cardColors(containerColor = White.copy(alpha = 0.08f))`, elevación `8.dp`.

### 10.4 Patrones de UI reutilizables

- Fondo oscuro + card translúcida.
- Botón primario azul a ancho completo.
- Botón secundario naranja.
- Icono + texto en botones.
- `OutlinedTextField` con borde azul al enfocar y texto blanco.
- `CircularProgressIndicator` naranja para estados de espera.
- `HorizontalDivider` blanco al 10–20%.

**Oportunidad:** extraer `HockeyBackground`, `PrimaryButton`, `SecondaryButton`, `GameCard`, `SectionTitle`, `PlayerRow`, `RoomCodeCard`, `PowerCard`, `GameButton` a un paquete `ui/components`.

---

## 11. Build y configuración

### 11.1 Hallazgos

- `app/build.gradle.kts` usa el DSL nuevo `compileSdk { version = release(37) }`.
- `release` tiene `optimization { enable = false }`: no se aplica R8/obfuscación.
- No hay `buildConfigField` ni `flavor` para dev/prod.
- No hay `signingConfig` de release.
- No hay `kotlinOptions`/`compilerOptions` para fijar `jvmTarget`; conviene alinear con Java 11 o subir a 17.
- `uiText = 1.12.0` está fijado fuera del BOM; puede generar conflicto de versiones. Es innecesario.
- `material-icons-extended` es muy pesado; solo se usan unos pocos iconos.
- `google-services` se declara con versión hardcodeada en el build raíz (`4.5.0`), fuera del catálogo.
- No hay `local.properties` versionado (correcto).
- `google-services.json` está versionado (normal para Android, pero conviene restringir la API key en Google Cloud).

### 11.2 Comandos útiles

```bash
# Compilar
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:compileDebugKotlin

# Tests unitarios
./gradlew :app:testDebugUnitTest

# Lint
./gradlew :app:lintDebug

# Instalar en dispositivo
./gradlew :app:installDebug
```

---

## 12. Checklist de requisitos vs. estado

| # | Requisito | Estado | Acción para el prompt |
|---|-----------|--------|------------------------|
| 1 | Login | ✅ | Mantener, mejorar errores/carga |
| 2 | Registro | ❌ | Crear `RegisterScreen` + `createUserWithEmailAndPassword` |
| 3 | Google Sign-In | ❌ | Opcional; requiere `google-services` + `GoogleSignInClient` |
| 4 | Crear sala | ⚠️ | Añadir código corto, configuración y transacción |
| 5 | Unirse por código | ❌ | Campo de código + consulta por `code` |
| 6 | Lista de salas | ❌ | Query en tiempo real |
| 7 | Lobby realtime | ❌ | `addValueEventListener` + `callbackFlow` |
| 8 | Iniciar partida | ❌ | Cambiar `state` de sala + repartir fichas |
| 9 | Tablero | ❌ | Modelo `Board`, `Tile`, `BoardSide` |
| 10 | Fichas/mano/pozo | ❌ | `Hand`, `Deck`, `Tile` |
| 11 | Validación de movimientos | ❌ | `MoveValidator` puro + tests |
| 12 | Turnos | ❌ | `TurnState` + `turnPlayerUid` real |
| 13 | Poderes | ❌ | `Power` sealed class + `PowerInventory` |
| 14 | Eventos | ❌ | `GameEvent` sealed class + RNG con seed |
| 15 | Puntaje | ❌ | `Scoreboard` + reglas de puntuación |
| 16 | Fin de partida | ❌ | `GameStatus.Finished` + ganador/desempate |
| 17 | Sincronización realtime | ❌ | `GameRepository.observeGame()` |
| 18 | Abandonar/reiniciar | ❌ | Acciones `Forfeit`/`Restart` |
| 19 | Componentes reutilizables | ⚠️ | Extraer `ui/components` |
| 20 | MVVM | ⚠️ | ViewModels + casos de uso + dominio |
| 21 | Tests unitarios | ❌ | JUnit + coroutines-test + Turbine |
| 22 | Informe IEEE | ❌ | Documento con arquitectura y diagramas |

---

## 13. Hallazgos priorizados

### P0 — Bloqueantes para la entrega

| Hallazgo | Archivo:línea | Impacto |
|----------|---------------|---------|
| No existe el juego de dominó | `GameScreen.kt` completo | No cumple el objetivo |
| No hay sincronización de partida | `RoomRepository.kt` | No cumple Firebase realtime |
| El lobby no se actualiza | `GameActivity.kt:66-76`, `LobbyScreen.kt` | El anfitrión nunca ve al invitado |
| No hay registro | `LoginScreen.kt:169-176` | Métrica de login/registro incompleta |
| No hay modelo de partida | `RoomModel.kt` | Imposible persistir tablero/fichas/poderes |
| No hay tests reales | `ExampleUnitTest.kt` | Métrica de pruebas en 0 |

### P1 — Correctitud y robustez

| Hallazgo | Archivo:línea | Impacto |
|----------|---------------|---------|
| Unirse a sala sin transacción | `RoomRepository.kt:33-68` | Dos invitados en la misma sala |
| `createRoom` retorna antes de confirmar escritura | `RoomRepository.kt:15-31` | Sala fantasma si falla la red |
| `startAt` como String | `RoomModel.kt:7` | Orden frágil; usar Long/ServerValue |
| `orderByChild` sin índice | `RoomRepository.kt:34` | Advertencia/ineficiencia |
| Sensores sin null-check | `GameScreen.kt:79-93` | Crash/no-op en dispositivos sin sensor |
| Acelerómetro integrado | `GameScreen.kt:103-105` | Deriva constante de la paleta |
| Loop sin delta time | `GameScreen.kt:135-229` | Velocidad depende del dispositivo |
| `onBack()` al perder pelota | `GameScreen.kt:172-174`, `222-224` | No hay game over real |
| Navegación no saveable | `GameActivity.kt:33` | Se pierde pantalla al rotar |
| `AuthActivity` no hace `finish()` | `AuthActivity.kt:26-29` | Back vuelve al login estando autenticado |
| Sin `collectAsStateWithLifecycle` | `GameActivity.kt:36` | Consumo innecesario en background |
| Modelos Kotlin sin no-arg | `RoomModel.kt`, `PlayerModel.kt` | Riesgo de deserialización Firebase |

### P2 — Calidad, UI y mantenibilidad

| Hallazgo | Archivo:línea | Impacto |
|----------|---------------|---------|
| Textos hardcodeados y mezcla de idiomas | Varios | Dificulta i18n y consistencia |
| Tema dinámico ignorado por colores hardcodeados | `Theme.kt` | Deuda visual |
| `uiText` fuera del BOM | `libs.versions.toml:12` | Posible conflicto |
| `material-icons-extended` innecesario | `app/build.gradle.kts:49` | Tamaño de APK |
| `optimization.enable = false` | `app/build.gradle.kts:25-28` | Release sin optimizar |
| `GameActivity` exportada sin filtro | `AndroidManifest.xml:14-21` | Superficie de ataque menor |
| Sin `INTERNET` explícito | `AndroidManifest.xml` | Depende de manifest fusionado |
| Sin accesibilidad completa | `contentDescription = null` | Mala experiencia con TalkBack |
| Sin estados de carga/error | Varios | UI poco clara |
| Sin manejo de back del sistema | `GameActivity.kt` | Salidas inesperadas |

---

## 14. Roadmap de amplificación

### Fase 1 — Cimientos (1–2 días)

1. Introducir Navigation Compose o mantener router pero con `rememberSaveable` y argumentos.
2. Crear `domain/model` con `GameState`, `Player`, `Tile`, `Board`, `Power`, `GameEvent`, `Move`.
3. Crear `domain/engine` puro: `DominoEngine`, `MoveValidator`, `ScoreCalculator`, `TurnManager`, `PowerResolver`, `EventResolver`.
4. Añadir tests unitarios del motor antes de tocar UI.
5. Completar autenticación: registro, errores, carga, logout, `finish()`.

### Fase 2 — Firebase realtime (1–2 días)

1. Rediseñar esquema: `rooms/{code}` con `state`, `config`, `players`, `game`, `updatedAt`.
2. Usar transacciones para unirse y para cada movimiento.
3. Implementar `observeRoom(code): Flow<Room>` con `callbackFlow` y `removeEventListener`.
4. Reglas de seguridad + índices + `ServerValue.TIMESTAMP`.
5. Presencia con `onDisconnect`.

### Fase 3 — Juego (3–5 días)

1. Reparto de 7 fichas, pozo, turnos.
2. Validación de jugadas en ambos extremos.
3. Poderes (comodín, salto, intercambio, inversión) con usos limitados.
4. Eventos aleatorios con RNG inyectable/seed.
5. Puntaje, rondas, fin de partida, desempate.
6. UI del tablero, fichas, mano, poderes, historial y resultado.

### Fase 4 — Calidad y entrega (1–2 días)

1. Extraer componentes reutilizables.
2. Tests de ViewModel y integración.
3. Informe IEEE con diagrama de clases, arquitectura y componentes.
4. Pulido visual y accesibilidad.

---

## 15. Mapa de migración: hockey → dominó

| Concepto actual | Reemplazo propuesto |
|-----------------|---------------------|
| `GameScreen` con Canvas y física | `GameScreen` con tablero de dominó y manos |
| `ballX/ballY/velocity` | `GameState.board`, `GameState.hand`, `GameState.deck` |
| `playerX` controlado por acelerómetro | Selección de ficha + extremo del tablero |
| Enemigo estático | Oponente remoto sincronizado por Firebase |
| `onBack()` al perder pelota | `GameStatus.Finished` + pantalla de resultado |
| `RoomModel` | `RoomModel` + `GameModel` anidado |
| `turnPlayerUid` sin uso | `TurnState.currentPlayerId` + validación |
| `winnerPlayerUid` sin uso | `GameResult.winnerId` + desempate por poderes |
| `startAt` | `createdAt` + `updatedAt` con `ServerValue.TIMESTAMP` |
| `TypeScreen` | `NavHost` con rutas y argumentos (`room/{code}`) |
| Sensores | Gestos/taps; los sensores no son necesarios para dominó |

---

## 16. Cómo construir prompts especializados

### 16.1 Bloque de contexto base (prepend a cualquier prompt)

Copia este bloque al inicio de cada encargo:

```text
CONTEXTO DEL PROYECTO
- App Android nativa en Kotlin + Jetpack Compose + Material 3.
- Repositorio: domino-SAA / módulo HockeyGame.
- Paquete base: com.udistrital.hockeygame.
- applicationId: com.udistrital.hockeygame (NO cambiar).
- Stack: Kotlin 2.4.21, AGP 9.3.2, Gradle 9.5.0, Compose BOM 2026.02.01,
  Firebase BOM 35.0.2, compileSdk/targetSdk 37, minSdk 24, Java 11.
- Firebase: Auth email/password + Realtime Database (proyecto hockeygame-86dfd).
- Arquitectura actual: data/models, data/enums, repositories, viewmodels,
  views/screens, views/theme; navegación manual con enum TypeScreen.
- Estado actual: login funciona; crear/unirse a sala funciona parcialmente;
  lobby NO es realtime; GameScreen es un aire hockey con Canvas y sensores;
  no existe dominó, fichas, poderes, eventos, puntaje ni fin de partida.
- Estilo visual: fondo #0F172A, primario #00AEFF, acento #FF9800,
  cards blancas al 8%, radios 16–24 dp, botones 54–56 dp, texto blanco.
- Restricciones: mantener minSdk 24, no romper login ni creación de salas,
  no cambiar applicationId, no introducir dependencias sin justificarlas.
- Entrega esperada: código compilable, imports completos, sin TODO,
  con nombres en inglés para código y textos de UI en español.
```

### 16.2 Reglas para redactar el encargo

1. **Delimita el alcance:** indica archivos exactos o capas (`domain/`, `data/`, `views/`). Evita “haz todo el juego”.
2. **Define el contrato:** firmas de funciones, modelos, estados y callbacks esperados.
3. **Fija criterios de aceptación:** “compila”, “tests X pasan”, “no cambia la UI de login”, “sala se actualiza en <1 s”.
4. **Pide el “por qué”:** obliga a explicar decisiones de diseño y trade-offs.
5. **Exige tests:** para lógica pura, tests unitarios; para Firebase, fake repository.
6. **Prohíbe atajos:** nada de lógica en composables, nada de `GlobalScope`, nada de `!!` injustificado.
7. **Pide migración incremental:** un prompt por capa; no reescribas todo el proyecto de golpe.
8. **Incluye ejemplos de entrada/salida:** especialmente para validación de jugadas y poderes.
9. **Pide diff o archivos completos:** según la herramienta; si es un agente, pide lista de archivos tocados.
10. **Añade verificación:** “al final ejecuta `./gradlew :app:compileDebugKotlin` y reporta”.

### 16.3 Variables reutilizables

| Variable | Descripción |
|----------|-------------|
| `{{OBJETIVO}}` | Resultado funcional buscado |
| `{{ARCHIVOS}}` | Rutas que se pueden tocar |
| `{{CONTRATO}}` | Firmas/modelos esperados |
| `{{CRITERIOS}}` | Checklist de aceptación |
| `{{NO_TOCAR}}` | Archivos o comportamientos prohibidos |
| `{{TESTS}}` | Casos de prueba obligatorios |
| `{{ESTILO}}` | Tokens visuales a respetar |

---

## 17. Prompts especializados listos para usar

> Encadena los prompts en el orden propuesto. Cada uno asume que el anterior ya se aplicó.

### Prompt 0 — Auditoría y plan de migración (solo lectura)

```text
Actúa como arquitecto Android senior. Analiza el proyecto HockeyGame
(paquete com.udistrital.hockeygame) y produce un plan de migración a
Dominó "Caos" en 4 fases.

Revisa especialmente:
- app/src/main/java/com/udistrital/hockeygame/views/GameActivity.kt
- app/src/main/java/com/udistrital/hockeygame/views/screens/GameScreen.kt
- app/src/main/java/com/udistrital/hockeygame/repositories/RoomRepository.kt
- app/src/main/java/com/udistrital/hockeygame/viewmodels/RoomViewModel.kt
- app/src/main/java/com/udistrital/hockeygame/data/models/RoomModel.kt
- app/build.gradle.kts y gradle/libs.versions.toml

Entrega:
1. Diagrama de arquitectura objetivo (texto).
2. Lista de archivos a crear/modificar/eliminar, con propósito.
3. Orden de implementación y dependencias entre tareas.
4. Riesgos técnicos y mitigaciones.
5. Criterios de aceptación por fase.

No escribas código todavía. No propongas cambiar applicationId ni minSdk.
```

### Prompt 1 — Capa de dominio del dominó

```text
Implementa la capa de dominio pura (sin Android, sin Firebase) para Dominó
"Caos" en:
app/src/main/java/com/udistrital/hockeygame/domain/

Modelos mínimos:
- Tile(id, left: Int, right: Int, isWildcard: Boolean = false)
- BoardTile(tile, leftEnd, rightEnd, position)
- Board(tiles: List<BoardTile>, leftEnd: Int?, rightEnd: Int?)
- PowerType { WILDCARD, SKIP, SWAP, INVERT }
- Power(type, remainingUses)
- GameEvent sealed class (p. ej. TileStorm, PointBonus, ForcedDraw)
- PlayerState(id, name, hand: List<Tile>, powers: List<Power>, score: Int, passes: Int)
- TurnState(currentPlayerId, direction: Int, round: Int, maxRounds: Int)
- GameStatus { WAITING, PLAYING, FINISHED }
- GameState(roomId, players, board, deck, turn, status, events, winnerId, tiebreakByPowers)
- Move sealed class (PlayTile, DrawTile, UsePower, Pass)

Reglas:
- 7 fichas por jugador al iniciar.
- Dominó estándar 28 fichas (0-0 a 6-6).
- Validación en extremos izquierdo/derecho.
- Poderes con usos limitados y validación previa.
- Robo del pozo si no hay jugada válida.
- Fin por mano vacía, sin movimientos o máximo de rondas.
- Desempate por menos poderes usados.

Requisitos:
- Kotlin puro, inmutable, determinista con RNG inyectable (seed).
- Sin dependencias de Android/Firebase.
- Tests unitarios JUnit4 para: reparto, validación, poderes, eventos,
  fin de partida y desempate.
- No toques UI ni Firebase en este prompt.

Al final ejecuta ./gradlew :app:testDebugUnitTest y reporta.
```

### Prompt 2 — Motor de partida y reducer

```text
Sobre la capa domain existente, implementa un GameEngine con reducer puro:

fun reduce(state: GameState, action: GameAction): GameState

GameAction:
- StartGame(roomId, players, seed)
- PlayTile(playerId, tileId, side: BoardSide)
- DrawTile(playerId)
- UsePower(playerId, power: PowerType, targetTileId: String? = null)
- PassTurn(playerId)
- ResolveEvent(event: GameEvent)
- Forfeit(playerId)
- Restart

Requisitos:
- El reducer no muta el estado; devuelve copia.
- Valida turno, propiedad de la ficha y legalidad del movimiento.
- Emite errores de dominio tipados (sealed class GameError).
- Aplica poderes y eventos en el orden correcto.
- Actualiza puntaje, ronda y estado de fin.
- Mantén determinismo con seed.
- Añade tests de integración del reducer con secuencias completas.

No implementes UI. No accedas a Firebase. Explica el orden de resolución
de un turno con poder + evento en un comentario breve.
```

### Prompt 3 — Poderes y eventos aleatorios

```text
Implementa el sistema de poderes y eventos de Dominó "Caos" sobre el
dominio existente.

Poderes:
1. Comodín: permite colocar una ficha ignorando un extremo.
2. Salto: el siguiente jugador pierde turno.
3. Intercambio: intercambia una ficha propia por una aleatoria del rival.
4. Inversión: cambia el sentido de los turnos.

Eventos (mínimo 3):
- Tormenta de fichas: todos roban una ficha del pozo.
- Bonificación: jugador actual +N puntos.
- Bloqueo: el siguiente jugador no puede usar poderes por un turno.

Requisitos:
- Usos limitados por poder y por partida (configurable).
- Validar que el poder se pueda usar antes de consumirlo.
- RNG inyectable y seed para tests.
- Efectos expresados como cambios de estado, no como side effects.
- Tests para cada poder y cada evento, incluyendo casos límite
  (pozo vacío, rival sin fichas, último turno, empate).

No toques la UI todavía. Documenta el contrato de cada poder.
```

### Prompt 4 — Repositorio Firebase y sincronización realtime

```text
Rediseña la capa de datos Firebase para soportar una partida de dominó
sincronizada en tiempo real.

Esquema propuesto:
rooms/{code}
  code: String
  hostId: String
  guestId: String?
  status: "WAITING" | "PLAYING" | "FINISHED"
  createdAt: Long
  updatedAt: Long
  config: { maxRounds: Int, maxPlayers: Int }
  game: { ...estado serializable del dominio... }

Requisitos:
- Usar transacciones para unirse a sala y para aplicar movimientos.
- Evitar que dos invitados ocupen la misma sala.
- Usar ServerValue.TIMESTAMP para createdAt/updatedAt.
- Implementar observeRoom(code): Flow<RoomDto> con callbackFlow y
  removeEventListener en awaitClose.
- Implementar createRoom, joinRoom(code), startGame, sendMove,
  forfeit, restart, leaveRoom.
- Manejar errores con un sealed class Result.
- No exponer Firebase directamente al ViewModel.
- Añadir reglas de seguridad e índices en un archivo .json documentado.
- Mantener compatibilidad con las pantallas actuales mientras migras.

Entrega: repositorio, DTOs, mapeadores y tests con fake de Firebase.
No cambies la UI en este paso.
```

### Prompt 5 — Autenticación completa

```text
Completa el flujo de autenticación de HockeyGame.

Requisitos:
- Añadir RegisterScreen con nombre, email, contraseña y confirmación.
- Crear usuario con FirebaseAuth.createUserWithEmailAndPassword.
- Guardar nombre visible en el perfil (UserProfile) o en Realtime Database.
- Validar email, longitud de contraseña y coincidencia.
- Mostrar loading, errores inline y éxito.
- Añadir cierre de sesión y limpiar el back stack.
- En AuthActivity, si hay usuario, navegar a GameActivity y hacer finish().
- Manejar onStart sin relanzar actividades duplicadas.
- No romper el login actual.
- Textos de UI en español; código e identificadores en inglés.

Entrega:
1. Archivos nuevos/modificados.
2. Firmas de las funciones del ViewModel de autenticación.
3. Tests unitarios de validación (sin Firebase).
4. Explicación breve del flujo.

No implementes Google Sign-In en este prompt.
```

### Prompt 6 — Lobby en tiempo real

```text
Haz que el lobby sea realtime y use códigos cortos.

Requisitos:
- Generar código de 6 caracteres alfanuméricos único (ej. A7K3Q9).
- Crear la sala en rooms/{code} con transacción para evitar colisiones.
- El anfitrión debe ver al invitado aparecer sin recargar.
- El invitado debe ver el código y el estado.
- Mostrar estados: esperando, listo, sala llena, cancelada, error.
- Permitir cancelar sala (borrar o marcar como cancelada) y salir.
- Escuchar con addValueEventListener y limpiar en onDispose/awaitClose.
- Habilitar "Iniciar partida" solo cuando haya 2 jugadores.
- Al iniciar, cambiar status a PLAYING y crear el estado inicial del juego.
- Usar el estilo visual actual (fondo #0F172A, cards al 8%, azul #00AEFF,
  naranja #FF9800, radios 16–24 dp).

No implementes el tablero todavía. Incluye tests del generador de códigos
y del mapeo de estados.
```

### Prompt 7 — UI del juego y componentes reutilizables

```text
Implementa la UI del tablero de Dominó "Caos" siguiendo los mockups del
taller, reutilizando el estilo visual existente.

Componentes a extraer en ui/components:
- GameBackground
- GameCard
- PrimaryButton / SecondaryButton
- SectionTitle
- PlayerBadge (nombre, puntaje, turno)
- RoomCodeCard
- TileView (ficha de dominó con puntos)
- BoardView (extremos izquierdo/derecho)
- HandRow (fichas del jugador)
- PowerCard (icono, nombre, usos restantes, enabled)
- EventDialog (título, descripción, continuar)
- GameResultScreen (ganador, fichas colocadas, poderes usados, saldo)

Pantallas:
- GameScreen: tablero, mano, turno, puntajes, botón terminar turno,
  acceso a poderes, historial.
- ResultScreen: resultado y acciones jugar de nuevo / volver al menú.
- HistoryScreen: lista de partidas.

Requisitos:
- Sin lógica de juego en composables; solo render y callbacks.
- Estado hoisted desde un GameViewModel con UiState.
- Accesibilidad: contentDescription, tamaños táctiles >= 48 dp.
- Responsive: portrait y landscape; usar dp y pesos, no píxeles.
- Textos en español; código en inglés.
- No rompas HomeScreen ni LoginScreen.

Entrega los composables y una preview por componente.
```

### Prompt 8 — Reemplazar el GameScreen de hockey

```text
Reemplaza el GameScreen de aire hockey por la pantalla de partida de
Dominó "Caos", sin romper la navegación existente.

Contexto:
- El GameScreen actual usa Canvas, sensores y un loop de física.
- La nueva pantalla debe consumir GameViewModel.uiState y enviar acciones
  al motor de dominio.
- El botón "Back" actual debe convertirse en "Abandonar" con confirmación.
- Al terminar la partida, navegar a ResultScreen.
- Mantener la paleta y los tokens visuales actuales.

Requisitos:
- Eliminar el uso de sensores y la física del composable.
- No dejar código muerto (variables de pelota, colisiones, etc.).
- No usar píxeles crudos; usar dp.
- Manejar estados: cargando, esperando, jugando, evento, terminado, error.
- Añadir previews con estados simulados.
- Tests de UI básicos para seleccionar ficha y confirmar movimiento.

Entrega:
1. GameScreen.kt completo.
2. GameViewModel.kt con StateFlow<GameUiState>.
3. Lista de archivos eliminados/modificados.
4. Verificación de compilación.
```

### Prompt 9 — Tests y calidad

```text
Diseña e implementa la estrategia de pruebas de HockeyGame.

Cobertura mínima:
- Dominio: reparto, validación de movimientos, poderes, eventos,
  puntaje, fin de partida, desempate.
- ViewModels: estados de carga/error/éxito con fake repositories.
- Repositorios: mapeo DTO ↔ modelo, transacciones simuladas.
- UI: un test Compose por pantalla crítica (login, lobby, tablero).

Herramientas:
- JUnit4
- kotlinx-coroutines-test
- Turbine
- Fake repositories hechos a mano (sin MockK si no es necesario)
- Compose UI Test para los flujos críticos

Requisitos:
- Tests deterministas (RNG con seed, reloj inyectable).
- Nombres descriptivos en inglés.
- Sin tests que dependan de Firebase real.
- Añadir dependencias solo si están justificadas.

Entrega:
1. Archivos de test.
2. Comandos para ejecutarlos.
3. Reporte de cobertura conceptual por capa.
```

### Prompt 10 — Informe IEEE y documentación

```text
Redacta el informe IEEE del proyecto Dominó "Caos" a partir del código
real del repositorio.

Secciones mínimas:
1. Introducción y objetivos.
2. Especificaciones de diseño (requisitos funcionales y no funcionales).
3. Arquitectura MVVM: capas, responsabilidades y flujo de datos.
4. Diagrama de clases (en Mermaid o PlantUML) con modelos de dominio,
   ViewModels, repositorios y DTOs.
5. Arquitectura de componentes UI y navegación.
6. Sincronización en tiempo real con Firebase (esquema y reglas).
7. Poderes, eventos y reglas del juego.
8. Pruebas unitarias y estrategia de calidad.
9. Conclusiones y trabajo futuro.

Requisitos:
- Usa el formato IEEE (título, autores, resumen, índice, referencias).
- No inventes funcionalidades que no existan; marca el estado real.
- Incluye diagramas y tablas.
- Entrega en Markdown listo para exportar a PDF.
```

---

## 18. Checklist de aceptación por cambio

Antes de dar por bueno cualquier prompt aplicado:

- [ ] `./gradlew :app:compileDebugKotlin` termina sin errores.
- [ ] `./gradlew :app:testDebugUnitTest` pasa.
- [ ] El login sigue funcionando.
- [ ] Crear sala y unirse siguen funcionando.
- [ ] El lobby se actualiza sin recargar.
- [ ] La partida se sincroniza entre dos dispositivos/emuladores.
- [ ] Los poderes respetan usos y turnos.
- [ ] Los eventos no rompen el estado.
- [ ] El fin de partida calcula ganador y desempate.
- [ ] No hay lógica de juego en composables.
- [ ] No hay `GlobalScope`, `!!` injustificado ni `delay` para sincronizar.
- [ ] No se cambió `applicationId` ni `minSdk`.
- [ ] La UI mantiene la paleta y los tokens.
- [ ] Los textos nuevos están en `strings.xml` o centralizados.
- [ ] Se actualizó la documentación afectada.

---

## 19. Anexos

### 19.1 Firmas públicas actuales

```kotlin
// Activities
class AuthActivity : ComponentActivity()
class GameActivity : ComponentActivity()

// Screens
@Composable fun LoginScreen(onLoginClick: (String, String) -> Unit, onRegisterClick: () -> Unit = {})
@Composable fun HomeScreen(onClickGame: () -> Unit, onClickMultiplayer: () -> Unit = {})
@Composable fun RoomSelectionScreen(onCreateRoomClick: () -> Unit = {}, onJoinOpenRoomClick: () -> Unit = {})
@Composable fun LobbyScreen(roomCode: String? = "HK89A2", hostName: String = "Jugador 1", guestName: String? = null, onCancelClick: () -> Unit = {}, onStartGameClick: () -> Unit = {})
@Composable fun GameScreen(onBack: () -> Unit)

// ViewModel
class RoomViewModel : ViewModel() {
    val model: StateFlow<RoomModel?>
    fun createRoom()
    fun joinOpenRoom()
}

// Repository
class RoomRepository {
    fun createRoom(hostPlayer: PlayerModel): RoomModel?
    fun joinOpenRoom(guestPlayer: PlayerModel, onResult: (RoomModel?) -> Unit)
}

// Models
data class PlayerModel(val uid: String, val email: String)
data class RoomModel(
    var id: String? = "",
    val player1: PlayerModel,
    val player2: PlayerModel? = null,
    val startAt: String,
    val winnerPlayerUid: String? = "",
    val turnPlayerUid: String = "",
    val finished: Boolean = false
)

// Navigation
enum class TypeScreen { HOME, ROOM_SELECTION, LOBBY, GAME }
```

### 19.2 Glosario

| Término | Significado |
|---------|-------------|
| **Mano** | Fichas que tiene un jugador |
| **Pozo** | Fichas no repartidas |
| **Extremo** | Valor izquierdo o derecho visible del tablero |
| **Poder** | Habilidad especial con usos limitados |
| **Evento** | Alteración aleatoria del estado de partida |
| **Ronda** | Secuencia de turnos hasta que todos juegan |
| **Desempate** | Menos poderes usados gana |
| **DTO** | Objeto de transferencia para Firebase |
| **Reducer** | Función pura `(estado, acción) -> estado` |
| **CallbackFlow** | Puente entre listener de Firebase y `Flow` de Kotlin |

### 19.3 Recomendaciones de herramientas para los prompts

- Para código: indicar “Kotlin idiomático, sin Java, sin `!!`, sin `lateinit` innecesario”.
- Para Firebase: exigir “transacciones, `ServerValue.TIMESTAMP`, `callbackFlow`, `awaitClose`”.
- Para Compose: exigir “state hoisting, `collectAsStateWithLifecycle`, `rememberSaveable`, `LazyColumn` para listas”.
- Para tests: exigir “JUnit4 + coroutines-test + Turbine + fakes manuales”.
- Para documentación: exigir “Mermaid/PlantUML y estado real del código”.

---

**Fin del análisis.** Este documento puede usarse como contexto persistente: los bloques de la sección 16 y 17 están pensados para copiarse directamente en un agente de código.
