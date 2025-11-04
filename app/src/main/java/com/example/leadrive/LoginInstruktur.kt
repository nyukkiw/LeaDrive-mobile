package com.example.leadrive

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import io.github.jan.supabase.postgrest.from

@kotlinx.serialization.Serializable
data class Instruktur(
        val id_instruktur: Int,
        val nama: String,
        val email: String,
        val password: String,
        val nomor_sim: String? = null,
        val status_aktif: Boolean? = null,
        val id_kursus: Int? = null,
        val foto_profil: String? = null
)

@Composable
fun LoginInstruktur() {
    var nama by remember { mutableStateOf("") }
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
        Text("Login Instruktur", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(24.dp))

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
                        // Ambil semua data instruktur dari tabel Supabase
                        val result = supabase.from("instruktur").select()
                        val data = result.decodeList<Instruktur>()

                        // Cocokkan username dan password secara lokal
                        val user = data.find {
                            it.nama.trim().equals(nama.trim(), ignoreCase = true) &&
                                    it.password.trim() == password.trim()
                        }

                        if (user != null) {
                            message = "✅ Berhasil login sebagai ${user.nama}"
                        } else {
                            message = "❌ Username atau Password salah"
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

        Spacer(modifier = Modifier.height(16.dp))
        Text(message)
    }
}
