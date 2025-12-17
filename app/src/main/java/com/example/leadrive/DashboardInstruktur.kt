package com.example.leadrive

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
    var pendingCount by remember { mutableIntStateOf(0) }

    fun fetchPendingCount() {
        scope.launch {
            try {
                // Kita ambil semua data pemesanan yang statusnya Pending
                // Catatan: Jika data sangat banyak, gunakan .count() / head=true lebih efisien.
                // Tapi cara ini paling aman agar tidak error import.
                val result = supabase.from("pemesanan")
                    .select {
                        filter {
                            eq("status_pemesanan", "pending")
                        }
                    }
                    .decodeList<Pemesanan>()

                pendingCount = result.size

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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
                        columns = Columns.list("*", "pemesanan(*, users(*))")
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
        fetchPendingCount()
    }

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
            // Gunakan Column agar bisa menumpuk Profil dan Notifikasi
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                // --- KARTU PROFIL ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = photoUrl,
                                error = painterResource(id = R.drawable.ic_launcher_background),
                                placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
                            ),
                            contentDescription = "Foto Profil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(64.dp).clip(CircleShape)
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

                // --- KARTU NOTIFIKASI KECIL (Hanya muncul jika ada pending) ---
                if (pendingCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp)) // Jarak dari profil

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Saat diklik, langsung arahkan ke halaman Jadwal Kursus
                                navController.navigate("jadwalKursus/$idInstruktur")
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF9C4) // Warna Kuning Muda (Alert)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color(0xFFF57C00), // Ikon Oranye
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Notifikasi : Ada $pendingCount jadwal yang belum diambil",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black,
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Medium
                            )

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            BottomAppBar(containerColor = Color(0xFFFF9800)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("jadwalKursus/$idInstruktur") },
                        icon = { Icon(Icons.Filled.DateRange, contentDescription = "Jadwal") },
                        label = { Text("Jadwal") },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = Color.White,
                            selectedIconColor = Color.White,
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

        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            when {
                isLoading -> Text(
                    "Memuat jadwal...",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 20.sp
                )

                daftarJadwal.isEmpty() -> Text(
                    "Belum ada kursus diambil",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 20.sp
                )

                else -> LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(daftarJadwal) { j ->

                        // Safe call deep join username
                        val namaPeserta = j.pemesanan?.users?.name ?: "Tidak diketahui"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {

                                Text(
                                    text = "Nama  ${namaPeserta}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = Color.White.copy(alpha = 0.3f)
                                )
                                RowDetail("ID Pemesanan", j.id_pemesanan.toString())
                                RowDetail("Tanggal", j.tanggal)
                                RowDetail("Jam Mulai", j.jam_mulai ?: "-")

                                Spacer(modifier = Modifier.height(8.dp))

                                val status = j.pemesanan?.status_pemesanan
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Status: ", color = Color.White, fontSize = 14.sp)
                                    Text(
                                        text = status ?: "-",
                                        color = warnaStatusDarkBg(status),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }

                                if (status == "Diambil") {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { navController.navigate("mapPeserta/${j.id_pemesanan}") },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White,
                                            contentColor = Color(0xFF1565C0)
                                        )
                                    ) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Buka Map Peserta", fontWeight = FontWeight.Bold)
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

@Composable
fun RowDetail(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f), // Putih agak redup untuk Label
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color.White, // Putih terang untuk Nilai
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

// Warna Status khusus untuk Background Gelap (Lebih Terang/Neon)
fun warnaStatusDarkBg(status: String?): Color {
    return when (status) {
        "Diambil" -> Color(0xFFFFD54F) // Kuning Emas Terang (Kontras di Biru)
        "Progres" -> Color(0xFFFF6E40) // Oranye Terang (Lebih mudah dibaca daripada Merah Tua)
        "Selesai" -> Color(0xFF69F0AE) // Hijau Neon
        else -> Color.White
    }
}

