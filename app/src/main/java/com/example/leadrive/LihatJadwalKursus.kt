package com.example.leadrive
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
//import io.github.jan.supabase.postgrest.from
//import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
//import io.github.jan.supabase.postgrest.filter.eq
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import android.content.Context

// =======================
// DATA CLASS (HASIL JOIN)
// =======================

@Serializable
data class JadwalKursusResponse(
    val tanggal: String,
    val jam_mulai: String,
    val pemesanan: PemesananNested? // ✅ nullable
)

@Serializable
data class PemesananNested(
    val status_pemesanan: String,
    val paket_kursus: PaketKursusNested?
)


@Serializable
data class PaketKursusNested(
    val kursus: KursusNested?
)

@Serializable
data class KursusNested(
    val nama_kursus: String
)

// =======================
// SUPABASE QUERY
// =======================

suspend fun fetchJadwalKursusPeserta(
    userId: Long
): List<JadwalKursusResponse> {

    val supabase = SupabaseClient.client

    return supabase
        .from("jadwal_kursus")
        .select(
            columns = Columns.raw(
                """
                tanggal,
                jam_mulai,
                pemesanan!inner (
                    status_pemesanan,
                    id_user,
                    paket_kursus!inner (
                        kursus!inner (
                            nama_kursus
                        )
                    )
                )
                """
            )
        ) {
            filter {
                eq("pemesanan.id_user", userId)
//                neq("pemesanan.status_pemesanan", "selesai")
            }

            order("tanggal", Order.ASCENDING)
            order("jam_mulai", Order.ASCENDING)
        }
        .decodeList()
}


// =======================
// SCREEN
// =======================

@Composable
fun LihatJadwalKursusScreen(navController: NavController) {
    val context = LocalContext.current

    val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", -1L)

    var loading by remember { mutableStateOf(true) }
    var jadwalList by remember { mutableStateOf<List<JadwalKursusResponse>>(emptyList()) }

//    LaunchedEffect(userId) {
//        if (userId == -1L) {
//            Log.e("LihatJadwal", "User belum login")
//            Log.d("DEBUG_JADWAL", jadwalList.toString())
//
//            loading = false
//            return@LaunchedEffect
//        }
//
//        loading = true
//        jadwalList = fetchJadwalKursusPeserta(userId)
//        loading = false
//    }
    LaunchedEffect(userId) {
        if (userId == -1L) {
            loading = false
            return@LaunchedEffect
        }

        loading = true

        val data = fetchJadwalKursusPeserta(userId)

        // 🔥 FILTER FINAL & AMAN
        jadwalList = data.filter {
            it.pemesanan
                ?.status_pemesanan
                ?.lowercase()
                ?.trim() != "selesai"
        }

        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "List Jadwal Kursus",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(jadwalList) { item ->
                    JadwalKursusCard(item)
                }
            }
        }
    }
}

// =======================
// CARD UI (SESUAI FIGMA)
// =======================


@Composable
fun JadwalKursusCard(item: JadwalKursusResponse) {

    val namaKursus =
        item.pemesanan
            ?.paket_kursus
            ?.kursus
            ?.nama_kursus
            ?: "Kursus tidak tersedia"

    val status = item.pemesanan?.status_pemesanan ?: "-"

    Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Nama kursus
            Text(
                text = namaKursus,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Tanggal & jam
            Text(
                text = "${item.tanggal} • ${item.jam_mulai}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status pemesanan
            Text(
                text = status.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = when (status.lowercase()) {
                    "pending" -> Color(0xFFFF9800)
                    "diambil" -> Color(0xFF2196F3)
                    "progres" -> Color(0xFF9C27B0)
                    "selesai" -> Color(0xFF4CAF50)
                    else -> Color.Gray
                }
            )
        }
    }
}
