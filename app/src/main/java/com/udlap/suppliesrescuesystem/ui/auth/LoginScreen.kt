package com.udlap.suppliesrescuesystem.ui.auth

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.udlap.suppliesrescuesystem.R
import kotlinx.coroutines.launch

/**
 * Screen for users to authenticate using email/password or Google Sign-In.
 *
 * @param onLoginSuccess Callback triggered when the user logs in successfully, providing their role.
 * @param onNavigateToRegister Callback to navigate to the registration screen.
 * @param onIncompleteProfile Callback triggered when the user logs in but hasn't finished setting up their profile.
 * @param viewModel The [AuthViewModel] managing the authentication state.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onIncompleteProfile: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }
    
    val authState by viewModel.authState.collectAsState()
    val savedEmailValue by viewModel.savedEmail.collectAsState()
    val rememberMeValue by viewModel.rememberMe.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)
    val webClientId = stringResource(id = R.string.default_web_client_id)

    LaunchedEffect(savedEmailValue) {
        if (savedEmailValue != null && email.isEmpty()) {
            email = savedEmailValue!!
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess((authState as AuthState.Success).user.role)
            viewModel.resetState()
        } else if (authState is AuthState.IncompleteProfile) {
            onIncompleteProfile()
            viewModel.resetState()
        } else if (authState is AuthState.Error) {
            loginError = (authState as AuthState.Error).message
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "SUPPLIES RESCUE", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Log in to continue", fontSize = 16.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(48.dp))

            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TextField(
                        value = email,
                        onValueChange = { email = it; loginError = null },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = password,
                        onValueChange = { password = it; loginError = null },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberMeValue,
                    onCheckedChange = { viewModel.setRememberMe(it) },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4CAF50))
                )
                Text(text = "Remember me", fontSize = 14.sp, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (loginError != null) {
                Text(text = loginError!!, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
            }

            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                enabled = authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("LOGIN", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onNavigateToRegister) {
                Text("Don't have an account? Register", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    loginError = null
                    // Configuramos la opción de Google
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false) // Permitir cualquier cuenta
                        .setServerClientId(webClientId)
                        .setAutoSelectEnabled(false) // Desactivar para forzar el selector manual si hay dudas
                        .build()

                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    scope.launch {
                        try {
                            val result = credentialManager.getCredential(request = request, context = context)
                            
                            // Extraer el token según el tipo de credencial recibida
                            val credential = result.credential
                            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                viewModel.signInWithGoogle(googleIdTokenCredential.idToken)
                            } else {
                                Log.e("GoogleSignIn", "Tipo de credencial no esperado: ${credential.type}")
                                loginError = "Error al obtener cuenta de Google"
                            }
                        } catch (e: GetCredentialException) {
                            Log.e("GoogleSignIn", "Error de Credenciales [${e.type}]: ${e.message}")
                            loginError = "No se encontraron cuentas de Google disponibles"
                        } catch (e: Exception) {
                            Log.e("GoogleSignIn", "Error inesperado: ${e.message}", e)
                            loginError = "Error al conectar con Google"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.Black),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
            ) {
                Text("SIGN IN WITH GOOGLE", fontWeight = FontWeight.Bold)
            }
        }
    }
}
