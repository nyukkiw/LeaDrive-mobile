package com.example.leadrive

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardInstruktur(idInstruktur: Int, nama: String, photoUrl: String?, navController: NavController) {

    var showDialog by remember { mutableStateOf(false) }

    val supabase = SupabaseClient.client
    val scope = rememberCoroutineScope()

    var daftarJadwal by remember { mutableStateOf<List<Jadwal>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    //------------------------------------------------------
    // FETCH DATA JADWAL UNTUK INSTRUKTUR
    //------------------------------------------------------
    fun fetchJadwalInstruktur() {
        scope.launch {
            isLoading = true
            try {
                // LOG DEBUG 1
                println("DEBUG_DASHBOARD: Memulai fetch untuk ID Instruktur $idInstruktur")

                val result = supabase.from("jadwal_kursus")
                    .select(
                        // UBAH DISINI:
                        // Gunakan "*" untuk ambil semua kolom jadwal
                        // Gunakan "pemesanan(*)" untuk ambil SEMUA kolom pemesanan (agar tidak error konversi)
                        columns = Columns.list("*", "pemesanan(*)")
                    ) {
                        filter {
                            eq("id_instruktur", idInstruktur)
                        }
                    }
                    .decodeList<Jadwal>()

                // LOG DEBUG 2
                println("DEBUG_DASHBOARD: Berhasil dapat ${result.size} data")
                result.forEach {
                    println(" - Jadwal ID: ${it.id_jadwal}, Status: ${it.pemesanan?.status_pemesanan}")
                }

                daftarJadwal = result

            } catch (e: Exception) {
                // LOG ERROR PENTING
                println("=== ERROR FETCH JADWAL DASHBOARD ===")
                e.printStackTrace() // Cek Logcat bagian System.err
            } finally {
                isLoading = false
            }
        }
    }

    // Load saat masuk halaman
    LaunchedEffect(idInstruktur) {
        fetchJadwalInstruktur()
    }

    //------------------------------------------------------
    // ALERT LOGOUT
    //------------------------------------------------------
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Konfirmasi Logout") },
            text = { Text("Apakah anda ingin logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                ) {
                    Text("Ya")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Tidak")
                }
            }
        )
    }

    //------------------------------------------------------
    // UI
    //------------------------------------------------------
    Scaffold(
        topBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = photoUrl,
                            error = painterResource(id = R.drawable.ic_launcher_background),
                            placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
                        ),
                        contentDescription = "Foto Profil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(nama, fontSize = 20.sp, style = MaterialTheme.typography.titleLarge)
                        Text("Instruktur", fontSize = 14.sp)
                    }

                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            }
        },

        bottomBar = {
            BottomAppBar(containerColor = Color(0xFFFF9800)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("jadwalKursus/$idInstruktur") },
                        icon = { Icon(Icons.Filled.DateRange, contentDescription = "Jadwal") },
                        label = { Text("Jadwal") },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = Color.White,
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = Color(0xFFFF9800)
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("statusKursus/$idInstruktur") },
                        icon = { Icon(Icons.Filled.List, contentDescription = "Status") },
                        label = { Text("Status") },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = Color.White,
                            selectedIconColor = Color.White,
                            indicatorColor = Color(0xFFFF9800)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            when {
                isLoading -> Text(
                    "Memuat jadwal...",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 20.sp
                )

                daftarJadwal.isEmpty() -> Text(
                    "Belum ada kursus yang diambil",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 20.sp
                )

                else -> LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(daftarJadwal) { j ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {

                                Text("ID Jadwal: ${j.id_jadwal}")
                                Text("ID Pemesanan: ${j.id_pemesanan}")
                                Text("Tanggal: ${j.tanggal}")
                                Text("Jam Mulai: ${j.jam_mulai ?: "-"}")
                                Text("Instruktur ID: ${j.id_instruktur ?: "-"}")

                                Spacer(modifier = Modifier.height(4.dp))

                                val status = j.pemesanan?.status_pemesanan

                                Text(
                                    "Status: ${status ?: "-"}",
                                    color = warnaStatus(status),
                                    fontSize = 14.sp
                                )

                                if (status == "Diambil") {
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            navController.navigate("mapPeserta/${j.id_pemesanan}")
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Buka Map Peserta")
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

fun warnaStatus(status: String?): Color {
    return when (status) {
        "Diambil" -> Color(0xFFFFC107) // Kuning
        "Progres" -> Color(0xFFD32F2F) // Merah
        else -> Color.Black            // Default
    }
}

