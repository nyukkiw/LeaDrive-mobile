package com.example.leadrive

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.favre.lib.crypto.bcrypt.BCrypt
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@kotlinx.serialization.Serializable
data class Instruktur(
    val id_instruktur: Int,
    val nama: String,
    val email: String,
    val password: String,
    val nomor_sim: String? = null,
    val status_aktif: Boolean? = null,
    val id_kursus: Int? = null,
    val foto_profil: String? = null,
)

@Composable
fun LoginInstruktur(navController: NavController) {
    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val supabase = SupabaseClient.client  // ambil instance client dari file SupabaseClient.kt

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logoleaddrive),
            contentDescription = "Logo LeaDrive",
            modifier = Modifier.size(150.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Login Instruktur", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = nama,
            onValueChange = { nama = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        if (email.isBlank() && nama.isBlank()) {
                            message = "Masukkan email atau username"
                            return@launch
                        }

                        val result = supabase.from("instruktur").select()
                        val data = result.decodeList<Instruktur>()

                        val userCandidate = data.find {
                            (nama.isNotBlank() && it.nama.trim().equals(nama.trim(), ignoreCase = true)) ||
                                    (email.isNotBlank() && it.email.trim().equals(email.trim(), ignoreCase = true))
                        }

                        when {
                            userCandidate == null -> {
                                message = "Username atau Email tidak ditemukan"
                            }
                            !BCrypt.verifyer().verify(password.trim().toCharArray(), userCandidate.password.trim()).verified -> {
                                message = "Password salah"
                            }
                            else -> {
                                val nameMatches = nama.isBlank() || userCandidate.nama.trim().equals(nama.trim(), ignoreCase = true)
                                val emailMatches = email.isBlank() || userCandidate.email.trim().equals(email.trim(), ignoreCase = true)

                                if (nameMatches && emailMatches) {
                                    val photoUrl = userCandidate.foto_profil?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
                                    navController.navigate("dashboardInstruktur/${userCandidate.nama}?photoUrl=$photoUrl")
                                } else {
                                    message = "Username dan Email tidak cocok satu sama lain"
                                }
                            }
                        }
                    } catch (e: Exception) {
                        message = "Terjadi kesalahan: ${e.message}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = androidx.compose.ui.graphics.Color(0xFFFF9800)
            )
        ) {
            Text("Login", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kembali", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (message.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
