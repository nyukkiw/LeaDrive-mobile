package com.example.leadrive

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class Pemesanan(
    val id_pemesanan: Int,
    val id_paket: Int,
    val tanggal_pemesanan: String,
    val status_pemesanan: String,
    val id_user: Int,
    val latitude: String? = null,
    val longitude: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JadwalKursusScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    var daftarPemesanan by remember { mutableStateOf<List<Pemesanan>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val supabase = SupabaseClient.client // ✅ pakai client dari file lain

    fun fetchPemesanan() {
        scope.launch {
            isLoading = true
            try {
                val result = supabase.from("pemesanan").select().decodeList<Pemesanan>()
                daftarPemesanan = result.filter { it.status_pemesanan == "Tersedia" }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchPemesanan()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jadwal Kursus") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> Text("Memuat data...", fontSize = 18.sp)
                daftarPemesanan.isEmpty() -> Text("Pesanan Masih Kosong", fontSize = 20.sp)
                else -> {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Daftar Pemesanan", fontSize = 22.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn {
                            items(daftarPemesanan) { pemesanan ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("ID: ${pemesanan.id_pemesanan}")
                                            Text("Status: ${pemesanan.status_pemesanan}")
                                        }
                                        Button(onClick = {
                                            scope.launch {
                                                try {
                                                    supabase.from("pemesanan")
                                                        .update(mapOf("status_pemesanan" to "Diambil")) {
                                                            filter {
                                                                eq("id_pemesanan", pemesanan.id_pemesanan)
                                                            }
                                                        }
                                                    fetchPemesanan() // Refresh list
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        }) {
                                            Text("Ambil")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}