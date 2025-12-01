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
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class Pemesanan(
    val id_pemesanan: Int,
    val id_paket: Int,
    val tanggal_pemesanan: String? = null,
    val status_pemesanan: String,
    val id_user: Int,
    val latitude: String? = null,
    val longitude: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JadwalKursusScreen(navController: NavController, idInstruktur: Int) {

    val supabase = SupabaseClient.client
    val scope = rememberCoroutineScope()

    var daftarPemesanan by remember { mutableStateOf<List<Pemesanan>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // -----------------------------------------------------
    // FETCH PEMESANAN PENDING
    // -----------------------------------------------------
    fun fetchPemesanan() {
        scope.launch {
            isLoading = true
            try {
                val result = supabase.from("pemesanan")
                    .select(
                        columns = Columns.list(
                            "id_pemesanan",
                            "id_paket",
                            "tanggal_pemesanan",
                            "status_pemesanan",
                            "id_user",
                            "latitude",
                            "longitude"
                        )
                    )
                    .decodeList<Pemesanan>()

                daftarPemesanan = result.filter { it.status_pemesanan == "pending" }

            } catch (e: Exception) {
                println("=== ERROR FETCH PEMESANAN ===")
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // -----------------------------------------------------
    // UPDATE id_instruktur di jadwal_kursus
    // -----------------------------------------------------
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
                                                    // 1. UPDATE PEMESANAN STATUS → DIAMBIL
                                                    supabase.from("pemesanan")
                                                        .update(
                                                            mapOf("status_pemesanan" to "Diambil")
                                                        ) {
                                                            filter {
                                                                eq("id_pemesanan", p.id_pemesanan)
                                                            }
                                                        }

                                                    // 2. UPDATE jadwal_kursus → isi id_instruktur
                                                    updateJadwalInstruktur(
                                                        idPemesanan = p.id_pemesanan,
                                                        idInstruktur = idInstruktur
                                                    )

                                                    // 3. REFRESH LIST
                                                    fetchPemesanan()

                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        }
                                    ) {
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
