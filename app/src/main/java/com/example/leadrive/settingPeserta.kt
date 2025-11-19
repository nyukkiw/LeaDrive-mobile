package com.example.leadrive

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingPesertaScreen(navController: NavController) {

    // 1. State untuk menampung teks yang diketik di kolom input
    var inputNama by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val supabase = SupabaseClient.client
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Akun") },
                navigationIcon = {
                    // Tombol untuk kembali ke halaman sebelumnya (beranda)
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp), // Beri padding di sekeliling kolom
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Beri jarak antar elemen
        ) {
            // 2. Kolom input untuk nama
            OutlinedTextField(
                value = inputNama,
                onValueChange = { inputNama = it },
                label = { Text("Nama Pengguna") },
                modifier = Modifier.fillMaxWidth(), // Buat lebarnya penuh
                singleLine = true
            )

            // 3. Baris untuk menampung tombol Simpan dan Batal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp) // Jarak antar tombol
            ) {
                // Tombol Batal
                OutlinedButton(
                    onClick = {
                        // Mengosongkan kolom input saat tombol batal diklik
                        inputNama = ""
                    },
                    modifier = Modifier.weight(1f) // Bagi lebar layar sama rata
                ) {
                    Text("Batal")
                }

                // Tombol Simpan
                Button(
                    onClick = {

                        val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
                        val savedEmail = prefs.getString("user_email", null)
                        if (savedEmail.isNullOrEmpty()) {
                            Toast.makeText(context, "Tidak ada user yang login", Toast.LENGTH_LONG)
                                .show()
                            return@Button
                        }
                        scope.launch {
                            try {


                                // URL PATCH Supabase REST
                                val url =
                                    "https://djgznqnnekcjoafbzfcr.supabase.co/rest/v1/users?email=eq.$savedEmail"

                                val response = supabase.httpClient.patch(url) {
                                    header("apikey", SupabaseClient.SUPABASE_KEY)
                                    header("Authorization", "Bearer ${SupabaseClient.SUPABASE_KEY}")
                                    header("Content-Type", "application/json")

                                    setBody(
                                        """{"name": "$inputNama"}"""
                                    )
                                }

                                // update session user
                                prefs.edit().putString("user_name", inputNama).apply()

                                Toast.makeText(context, "Berhasil update", Toast.LENGTH_LONG).show()
                                navController.popBackStack()


                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Gagal update: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                    },
                    modifier = Modifier.weight(1f) // Bagi lebar layar sama rata
                ) {
                    Text("Simpan")
                }
            }
        }
    }
}

// Composable untuk melihat preview tampilan tanpa harus menjalankan aplikasi
@Preview(showBackground = true)
@Composable
fun SettingPesertaScreenPreview() {
    // Kita menggunakan NavController palsu untuk preview
    SettingPesertaScreen(navController = rememberNavController())
}
