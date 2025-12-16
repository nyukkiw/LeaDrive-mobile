package com.example.leadrive.Peserta

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay


/**
 * Bikin 1 record di tabel `pemesanan`, lalu 1 record di `jadwal_kursus`
 * Return: id_pemesanan kalau sukses, null kalau gagal
 */
//suspend fun createPemesananDanJadwal(
//    context: Context,
//    paketId: Long,
//    userId: Long,
//    tanggalKursus: String,   // format "YYYY-MM-DD"
//    jamMulai: String,        // format "HH:MM:SS"
//    idInstruktur: Long? = null,
//    latitude: String? = null,
//    longitude: String? = null
//): Long? = withContext(Dispatchers.IO) {
//    try {
//        val supabase = SupabaseClient.client
//
//        // 1) Insert ke tabel pemesanan
//        val today = LocalDate.now().toString()  // tanggal_pemesanan = hari ini
//
//        val pemesananRes = supabase.from("pemesanan").insert(
//            mapOf(
//                "id_paket" to paketId,
//                "tanggal_pemesanan" to today,
//                "status_pemesanan" to "Progres",
//                "id_user" to userId,
//                "latitude" to latitude,
//                "longitude" to longitude
//            )
//        ) {
//            select()
//            single()
//        }
//
//        val pemesanan = pemesananRes.decodeSingle<PemesananRow>()
//        val idPemesanan = pemesanan.id_pemesanan
//
//        // 2) Insert ke tabel jadwal_kursus
//        supabase.from("jadwal_kursus").insert(
//            mapOf(
//                "id_pemesanan" to idPemesanan,
//                "tanggal" to tanggalKursus,
//                "jam_mulai" to jamMulai,
//                "id_instruktur" to idInstruktur
//            )
//        )
//
//        idPemesanan
//    } catch (e: Exception) {
//        android.util.Log.e("CreatePemesanan", "Gagal buat pemesanan/jadwal: ${e.message}", e)
//        null
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusPembayaranScreen(navController: NavController) {
    val context = LocalContext.current

    val paket = SelectedPaketHolder.selected
    val tanggal = TempPembayaranHolder.tanggal
    val jamMulai = TempPembayaranHolder.jamMulai
    val jamSelesai = TempPembayaranHolder.jamSelesai
    val orderId = TempPembayaranHolder.orderId

    if (paket == null || tanggal == null || jamMulai == null || jamSelesai == null || orderId == null) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Data pembayaran tidak ditemukan", Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
        return
    }

    var status by remember { mutableStateOf("belum bayar") }
    var remainingSeconds by remember { mutableStateOf(15 * 60L) }

    // 1. Polling ke Midtrans
    LaunchedEffect(orderId) {
        while (true) {
            val midStatus = checkMidtransStatus(orderId)
            if (midStatus != null) {
                when (midStatus) {
                    "settlement" -> {
                        status = "sudah bayar"
                        updatePembayaranStatus(orderId, "sudah_bayar")

                        if (!TempPembayaranHolder.pemesananSudahDibuat) {
                            val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
                            val currentUserId = prefs.getLong("user_id", 0L)

                            val lat = prefs.getString("user_lat", null)?.takeUnless { it == "0" }
                            val lng = prefs.getString("user_lng", null)?.takeUnless { it == "0" }

                            val tanggalDb = TempPembayaranHolder.tanggal      // "YYYY-MM-DD"
                            val jamMulaiRaw = TempPembayaranHolder.jamMulai ?: "00:00"
                            val jamMulaiDb = jamMulaiRaw.take(5)

                            if (tanggalDb != null) {
                                val sukses = createPemesananDanJadwal(
                                    orderId = orderId,
                                    idUser = currentUserId,
                                    idPaket = paket.idPaket,
                                    tanggalKursus = tanggalDb,
                                    jamMulai = jamMulaiDb,
                                    latitude = lat,
                                    longitude = lng
                                )

                                if (sukses) {
                                    TempPembayaranHolder.pemesananSudahDibuat = true
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Gagal membuat pemesanan/jadwal di server",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }


                        break
                    }




                    "pending" -> {
                        status = "belum bayar"
                        updatePembayaranStatus(orderId, "belum_bayar")
                    }
                    "expire" -> {
                        status = "kedaluwarsa"
                        updatePembayaranStatus(orderId, "kedaluwarsa")
                        break
                    }
                    "cancel", "deny" -> {
                        status = "dibatalkan"
                        updatePembayaranStatus(orderId, "dibatalkan")
                        break
                    }
                }
            }
            delay(5000)
        }
    }

    // 2. Countdown lokal
    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
        if (status == "belum bayar") {
            status = "kedaluwarsa"
            updatePembayaranStatus(orderId, "kedaluwarsa")
        }
    }

    // 3. Kalau status sudah bayar -> buat pemesanan & jadwal SEKALI
//    LaunchedEffect(status) {
//        if (status == "sudah bayar" && !TempPembayaranHolder.pemesananSudahDibuat) {
//
//            val prefsUser = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
//            val userId = prefsUser.getLong("user_id", 0L)
//
//            // AMBIL LAT & LONG DARI PREFS KALAU ADA
//            val lat = prefsUser.getString("latitude", "0")
//            val lon = prefsUser.getString("longitude", "0")
//
//            if (userId == 0L) {
//                Toast.makeText(
//                    context,
//                    "User tidak dikenal, pemesanan tidak bisa dibuat",
//                    Toast.LENGTH_LONG
//                ).show()
//                return@LaunchedEffect
//            }
//
//            val ok = createPemesananDanJadwal(
//                orderId = orderId,
//                idUser = userId,
//                idPaket = paket.idPaket,   // SESUAIKAN nama field id paket
//                tanggalKursus = tanggal,    // "2025-12-03"
//                jamMulai = jamMulai,        // "14:47" / "14:47:00"
//                latitude = lat,
//                longitude = lon
//            )
//
//            if (ok) {
//                TempPembayaranHolder.pemesananSudahDibuat = true
//                Toast.makeText(
//                    context,
//                    "Pemesanan & jadwal berhasil dibuat.",
//                    Toast.LENGTH_LONG
//                ).show()
//            } else {
//                Toast.makeText(
//                    context,
//                    "Gagal membuat pemesanan/jadwal di server",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        }
//    }


    // COUNTDOWN LOKAL
    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
        if (status == "belum bayar") {
            status = "kedaluwarsa"
            updatePembayaranStatus(orderId, "kedaluwarsa")
        }
    }

    val menit = remainingSeconds / 60
    val detik = remainingSeconds % 60

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Status Pembayaran") },
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text("Paket: ${paket.namaPaket}", style = MaterialTheme.typography.titleLarge)
            Text("Tanggal kursus: $tanggal")
            Text("Jam: $jamMulai - $jamSelesai")
            Text("Total bayar: Rp ${paket.harga}")
            Text("Invoice: $orderId")
            Text("Status pembayaran: $status")

            Spacer(Modifier.height(8.dp))

            if (status == "belum bayar") {
                Text(
                    "Sisa waktu pembayaran: ${menit} menit ${detik} detik",
                    style = MaterialTheme.typography.titleMedium
                )
            } else if (status == "kedaluwarsa") {
                Text(
                    "Pembayaran kedaluwarsa, silakan buat pesanan baru.",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(16.dp))

            // TOMBOL BAYAR ULANG (PAKAI LINK YANG SAMA)
            Button(
                enabled = status == "belum bayar" && remainingSeconds > 0,
                onClick = {
                    val redirect = TempPembayaranHolder.redirectUrl
                    if (redirect.isNullOrEmpty()) {
                        Toast.makeText(
                            context,
                            "Link pembayaran tidak ditemukan. Silakan kembali dan buat ulang pesanan.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(redirect)
                        )
                        context.startActivity(intent)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Bayar")
            }

            Button(
                onClick = { navController.navigate("beranda_peserta") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Beranda")
            }

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kembali ke paket")
            }
        }
    }
}
