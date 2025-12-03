package com.example.leadrive

import android.util.Log
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
import io.github.jan.supabase.postgrest.query.Columns

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusKursusScreen(navController: NavController, idInstruktur: Int) {

    val supabase = SupabaseClient.client
    val scope = rememberCoroutineScope()

    // GANTI TIPE STATE MENJADI LIST OF JADWAL
    var daftarJadwal by remember { mutableStateOf<List<Jadwal>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // --- FUNGSI FETCH (LOGIC SUDAH BENAR) ---
    fun fetchStatusKursus() {
        scope.launch {
            isLoading = true
            try {
                Log.d("DEBUG_UI", "Fetching data...")
                val result = supabase.from("jadwal_kursus") // Pastikan nama tabel benar
                    .select(Columns.list("*", "pemesanan!inner(*)")) {
                        filter {
                            eq("id_instruktur", idInstruktur)
                            isIn("pemesanan.status_pemesanan", listOf("Diambil", "Progres"))
                        }
                    }
                    .decodeList<Jadwal>()

                Log.d("DEBUG_UI", "Data didapat: ${result.size} item")

                // Cek apakah relasi pemesanan terbaca
                result.forEach {
                    Log.d("DEBUG_UI", "Jadwal ID: ${it.id_jadwal}, Pemesanan NULL? : ${it.pemesanan == null}")
                }

                daftarJadwal = result

            } catch (e: Exception) {
                Log.e("DEBUG_UI", "Error: ${e.message}")
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // --- FUNGSI UPDATE ---
    fun updateStatus(idPemesanan: Int, statusBaru: String) {
        scope.launch {
            try {
                supabase.from("pemesanan")
                    .update(mapOf("status_pemesanan" to statusBaru)) {
                        filter { eq("id_pemesanan", idPemesanan) }
                    }
                fetchStatusKursus() // Refresh
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchStatusKursus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Status Kursus") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                }
            )
        }
    ) { inner ->
        Box(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> Text("Memuat data...")

                daftarJadwal.isEmpty() -> Text("Tidak ada jadwal aktif.")

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    // LOOPING DAFTAR JADWAL
                    items(daftarJadwal) { jadwal ->

                        val p = jadwal.pemesanan

                        // DEBUG VISUAL: Jika p null, tampilkan Text Merah
                        if (p == null) {
                            Text(
                                "Error: Data Pemesanan Null untuk Jadwal ID ${jadwal.id_jadwal}",
                                color = androidx.compose.ui.graphics.Color.Red
                            )
                        } else {
                            // JIKA DATA ADA, TAMPILKAN CARD
                            var expanded by remember { mutableStateOf(false) }
                            val daftarPilihan = listOf("Diambil", "Progres", "Selesai")

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Jadwal ID: ${jadwal.id_jadwal}", style = MaterialTheme.typography.titleMedium)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                    Text("ID Pemesanan: ${p.id_pemesanan}")
                                    Text("Tanggal: ${p.tanggal_pemesanan}")
                                    Text("Status: ${p.status_pemesanan}", style = MaterialTheme.typography.bodyLarge)

                                    Spacer(Modifier.height(16.dp))

                                    // Dropdown
                                    ExposedDropdownMenuBox(
                                        expanded = expanded,
                                        onExpandedChange = { expanded = !expanded }
                                    ) {
                                        TextField(
                                            value = p.status_pemesanan ?: "",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Ubah Status") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                                            colors = TextFieldDefaults.colors(
                                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                            )
                                        )

                                        ExposedDropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            daftarPilihan.forEach { opsi ->
                                                DropdownMenuItem(
                                                    text = { Text(opsi) },
                                                    onClick = {
                                                        updateStatus(p.id_pemesanan, opsi)
                                                        expanded = false
                                                    }
                                                )
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
}

