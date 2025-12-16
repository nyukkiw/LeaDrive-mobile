package com.example.leadrive.Peserta

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.Serializable
import android.content.Context
import androidx.compose.foundation.clickable
import com.example.leadrive.SupabaseClient

// =======================
// DATA CLASS (JOIN RESULT)
// =======================

@Serializable
data class RiwayatKursusResponse(
    val tanggal: String,
    val jam_mulai: String,
    val pemesanan: PemesananRiwayat
)

@Serializable
data class PemesananRiwayat(
    val status_pemesanan: String,
    val paket_kursus: PaketKursusRiwayat
)

@Serializable
data class PaketKursusRiwayat(
    val id_kursus: Long,
    val kursus: KursusRiwayat
)



@Serializable
data class KursusRiwayat(
    val nama_kursus: String
)



// =======================
// SUPABASE QUERY
// =======================

suspend fun fetchRiwayatKursus(
    userId: Long
): List<RiwayatKursusResponse> {
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
                    paket_kursus!inner (
                    id_kursus,
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
                eq("pemesanan.status_pemesanan", "Selesai")
            }
            order("tanggal", Order.DESCENDING)
        }
        .decodeList()
}

// =======================
// SCREEN
// =======================

@Composable
fun RiwayatKursusScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", -1L)

    var loading by remember { mutableStateOf(true) }
    var riwayatList by remember { mutableStateOf<List<RiwayatKursusResponse>>(emptyList()) }

    LaunchedEffect(userId) {
        if (userId != -1L) {
            riwayatList = fetchRiwayatKursus(userId)
        }
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Riwayat Kursus",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            riwayatList.isEmpty() -> {
                Text(
                    text = "Belum ada riwayat kursus",
                    color = Color.Gray
                )
            }

            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(riwayatList) { item ->
                        RiwayatKursusCard(
                            item = item,
                            navController = navController
                        )
                    }

                }
            }
        }
    }
}

// =======================
// CARD UI
// =======================

@Composable
fun RiwayatKursusCard(
    item: RiwayatKursusResponse,
    navController: NavController
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(
                    "rating_kursus/${item.pemesanan.paket_kursus.id_kursus}/${item.pemesanan.paket_kursus.kursus.nama_kursus}"
                )
            },
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = item.pemesanan.paket_kursus.kursus.nama_kursus,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "${item.tanggal} • ${item.jam_mulai}",
                color = Color.Gray
            )

            Text(
                text = "SELESAI",
                color = Color(0xFF4CAF50)
            )
        }
    }
}

