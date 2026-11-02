package com.udistrital.hockeygame.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.udistrital.hockeygame.views.screens.LoginScreen
import com.udistrital.hockeygame.views.screens.RegisterScreen
import com.udistrital.hockeygame.views.theme.HockeyGameTheme

class AuthActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val TAG = "AuthActivity"

    private val isLoadingState = mutableStateOf(false)
    private val errorMessageState = mutableStateOf<String?>(null)

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoadingState.value = true
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                firebaseAuthWithGoogle(idToken)
            } else {
                errorMessageState.value = "Google Sign-In requiere configurar Web Client ID en Firebase Console."
                isLoadingState.value = false
            }
        } catch (e: Exception) {
            Log.w(TAG, "Google sign in failed", e)
            errorMessageState.value = "Inicia sesión con correo/contraseña o configura Google Client ID en Firebase."
            isLoadingState.value = false
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            goToGame()
        }
    }

    fun goToGame() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            HockeyGameTheme {
                var currentAuthScreen by remember { mutableStateOf(AuthScreen.LOGIN) }

                when (currentAuthScreen) {
                    AuthScreen.LOGIN -> {
                        LoginScreen(
                            onLoginClick = { email, password ->
                                performLogin(email, password)
                            },
                            onGoogleSignInClick = {
                                performGoogleSignIn()
                            },
                            onRegisterClick = {
                                errorMessageState.value = null
                                currentAuthScreen = AuthScreen.REGISTER
                            },
                            isLoading = isLoadingState.value,
                            errorMessage = errorMessageState.value
                        )
                    }

                    AuthScreen.REGISTER -> {
                        RegisterScreen(
                            onRegisterClick = { name, email, password ->
                                performRegister(name, email, password)
                            },
                            onBackToLoginClick = {
                                errorMessageState.value = null
                                currentAuthScreen = AuthScreen.LOGIN
                            },
                            isLoading = isLoadingState.value,
                            errorMessage = errorMessageState.value
                        )
                    }
                }
            }
        }
    }

    private fun performLogin(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            errorMessageState.value = "Por favor ingresa correo y contraseña"
            return
        }
        isLoadingState.value = true
        errorMessageState.value = null

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                isLoadingState.value = false
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    goToGame()
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    errorMessageState.value = task.exception?.localizedMessage
                        ?: "Error de autenticación. Verifica tus credenciales."
                }
            }
    }

    private fun performRegister(name: String, email: String, pass: String) {
        isLoadingState.value = true
        errorMessageState.value = null

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                        isLoadingState.value = false
                        Toast.makeText(this, "¡Cuenta creada con éxito!", Toast.LENGTH_SHORT).show()
                        goToGame()
                    } ?: run {
                        isLoadingState.value = false
                        goToGame()
                    }
                } else {
                    isLoadingState.value = false
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    errorMessageState.value = task.exception?.localizedMessage
                        ?: "No se pudo crear la cuenta. Intenta con otro correo."
                }
            }
    }

    private fun performGoogleSignIn() {
        errorMessageState.value = null
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                isLoadingState.value = false
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    goToGame()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    errorMessageState.value = "Error al autenticar con Google en Firebase."
                }
            }
    }

    enum class AuthScreen { LOGIN, REGISTER }
}
