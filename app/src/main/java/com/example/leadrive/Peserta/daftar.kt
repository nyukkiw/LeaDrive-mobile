package com.example.leadrive.Peserta

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.leadrive.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class NewUser(
    val name: String,
    val email: String,
    val password: String,
    val nomor_hp: String
)

@Composable
fun daftar(navController: NavController) { // Pastikan fungsi menerima NavController
    // State untuk setiap input field
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var nomorHp by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val supabase = SupabaseClient.client
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()), // Agar bisa di-scroll jika keyboard muncul
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Buat Akun Baru", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // --- Input Fields Sesuai Kebutuhan Anda ---
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = nomorHp, onValueChange = { nomorHp = it }, label = { Text("Nomor HP") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Konfirmasi Password") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            // Tombol Daftar
            Button(
                onClick = {
                    // Validasi input
                    if (name.isBlank() || email.isBlank() || nomorHp.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (password != confirmPassword) {
                        Toast.makeText(context, "Password tidak cocok", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    scope.launch {
                        try {
                            // Buat objek data yang akan dikirim ke Supabase
                            val newUser = NewUser(
                                name = name.trim(),
                                email = email.trim(),
                                password = password.trim(), // PERINGATAN: Password dikirim sebagai teks biasa
                                nomor_hp = nomorHp.trim()
                            )

                            // Masukkan data langsung ke tabel 'users' Anda
                            supabase.postgrest.from("users").insert(newUser)

                            // Jika berhasil
                            isLoading = false
                            Toast.makeText(context, "Pendaftaran berhasil! Silakan login.", Toast.LENGTH_LONG).show()
                            navController.popBackStack() // Kembali ke halaman login

                        } catch (e: Exception) {
                            isLoading = false
                            // Tangani kemungkinan error, misal email sudah ada (jika Anda set unique)
                            Toast.makeText(context, "Pendaftaran gagal: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Daftar")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tombol Batal
            OutlinedButton(
                onClick = {
                    navController.popBackStack() // Langsung kembali ke halaman login
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Batal")
            }
        }
    }
}
