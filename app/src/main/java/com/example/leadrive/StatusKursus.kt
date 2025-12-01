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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusKursusScreen(navController: NavController, idInstruktur: Int) {

    val supabase = SupabaseClient.client
    val scope = rememberCoroutineScope()

    var daftarStatus by remember { mutableStateOf<List<Pemesanan>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // -------------------------------------------------
    // FETCH PEMESANAN DENGAN STATUS (Diambil / Progres)
    // -------------------------------------------------
    fun fetchStatusKursus() {
        scope.launch {
            isLoading = true
            try {
                val result = supabase.from("pemesanan")
                    .select()
                    .decodeList<Pemesanan>()

                val filterData = result.filter {
                    it.status_pemesanan.equals("Diambil", ignoreCase = true) ||
                            it.status_pemesanan.equals("Progres", ignoreCase = true)
                }

                daftarStatus = filterData

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // -------------------------------------------------
    // UPDATE STATUS
    // -------------------------------------------------
    fun updateStatus(idPemesanan: Int, statusBaru: String) {
        scope.launch {
            try {
                supabase.from("pemesanan")
                    .update(mapOf("status_pemesanan" to statusBaru)) {
                        filter { eq("id_pemesanan", idPemesanan) }
                    }

                fetchStatusKursus()

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
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentAlignment = Alignment.Center
        ) {

            when {
                isLoading -> Text("Memuat data...", fontSize = 18.sp)

                daftarStatus.isEmpty() ->
                    Text("Tidak ada kursus dalam status Diambil / Progres", fontSize = 20.sp)

                else -> LazyColumn(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {

                    items(daftarStatus) { p ->

                        var expanded by remember { mutableStateOf(false) }
                        val daftarPilihan = listOf("Diambil", "Progres", "Selesai")

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {

                            Column(modifier = Modifier.padding(16.dp)) {

                                Text("ID Pemesanan: ${p.id_pemesanan}")
                                Text("Tanggal: ${p.tanggal_pemesanan ?: "-"}")
                                Text("Status Saat Ini: ${p.status_pemesanan}")

                                Spacer(Modifier.height(10.dp))

                                // --------------------------
                                // DROPDOWN PILIHAN STATUS
                                // --------------------------
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = !expanded }
                                ) {
                                    TextField(
                                        value = p.status_pemesanan ?: "-",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Ubah Status") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                        },
                                        modifier = Modifier.menuAnchor()
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

