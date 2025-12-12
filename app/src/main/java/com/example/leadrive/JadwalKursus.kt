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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JadwalKursusScreen(navController: NavController, idInstruktur: Int) {

    val supabase = SupabaseClient.client
    val scope = rememberCoroutineScope()

    var daftarPemesanan by remember { mutableStateOf<List<Pemesanan>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var userNames by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }

    // -----------------------
    // FETCH PEMESANAN PENDING
    // -----------------------
    fun fetchPemesanan() {
        scope.launch {
            isLoading = true
            try {
                val result = supabase.from("pemesanan")
                    .select()
                    .decodeList<Pemesanan>()

                daftarPemesanan = result.filter {
                    it.status_pemesanan.equals("Pending", ignoreCase = true)
                }

            } catch (e: Exception) {
                println("=== ERROR FETCH PEMESANAN ===")
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // -------------------------------------
    // UPDATE id_instruktur di jadwal_kursus
    // -------------------------------------
    fun updateJadwalInstruktur(idPemesanan: Int, idInstruktur: Int) {
        scope.launch {
            try {
                supabase.from("jadwal_kursus")
                    .update(
                        mapOf("id_instruktur" to idInstruktur)
                    ) {
                        filter {
                            eq("id_pemesanan", idPemesanan)
                        }
                    }

                println("=== UPDATE JADWAL KURSUS BERHASIL ===")

            } catch (e: Exception) {
                println("=== ERROR UPDATE JADWAL KURSUS ===")
                e.printStackTrace()
            }
        }
    }

    // FETCH DATA SAAT MASUK HALAMAN
    LaunchedEffect(Unit) {
        fetchPemesanan()
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jadwal Kursus") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { inner ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentAlignment = Alignment.Center
        ) {

            when {
                isLoading -> Text("Memuat data...", fontSize = 18.sp)

                daftarPemesanan.isEmpty() -> Text("Tidak ada pemesanan pending", fontSize = 20.sp)

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize()
                    ) {
                        items(daftarPemesanan) { p ->

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("ID Pemesanan: ${p.id_pemesanan}")
                                    Text("ID Paket: ${p.id_paket}")
                                    Text("Tanggal: ${p.tanggal_pemesanan ?: "-"}")
                                    Text("ID User: ${p.id_user}")
                                    Text("Status: ${p.status_pemesanan}")
                                    Spacer(Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    // UPDATE STATUS PEMESANAN
                                                    supabase.from("pemesanan")
                                                        .update(
                                                            mapOf("status_pemesanan" to "Diambil")
                                                        ) {
                                                            filter {
                                                                eq("id_pemesanan", p.id_pemesanan)
                                                            }
                                                        }
                                                    // UPDATE TABEL jadwal_kursus
                                                    updateJadwalInstruktur(
                                                        idPemesanan = p.id_pemesanan,
                                                        idInstruktur = idInstruktur
                                                    )
                                                    fetchPemesanan()
                                                } catch (e: Exception) {
                                                    println("=== ERROR UPDATE PEMESANAN ===")
                                                    e.printStackTrace()
                                                }
                                            }
                                        }
                                    )
                                    {
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
