package com.example.leadrive

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
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
                        // TODO: Tambahkan logika untuk menyimpan 'inputNama' ke database nanti
                        try {
                            scope.launch {

                            }
                        }catch(e: Exception){
                            isLoading = false
                            Toast.makeText(context, "gagal update: ${e.message}", Toast.LENGTH_LONG).show()
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
