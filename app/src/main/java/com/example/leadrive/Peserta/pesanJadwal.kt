package com.example.leadrive.Peserta

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.ui.graphics.Color

// ================= HOLDER SESSION PEMBAYARAN =================
object TempPembayaranHolder {
    var orderId: String? = null
    var tanggal: String? = null
    var jamMulai: String? = null
    var jamSelesai: String? = null
    var redirectUrl: String? = null

    // flag untuk mencegah insert pembayaran berkali-kali
    var draftSudahDisimpan: Boolean = false
    var pemesananSudahDibuat: Boolean = false
}

// simpan ke SharedPreferences (biar kalau app ditutup, data masih ada)
fun savePaymentSession(context: Context) {
    val prefs = context.getSharedPreferences("payment_session", Context.MODE_PRIVATE)
    prefs.edit()
        .putString("order_id", TempPembayaranHolder.orderId)
        .putString("redirect_url", TempPembayaranHolder.redirectUrl)
        .putString("tanggal", TempPembayaranHolder.tanggal)
        .putString("jam_mulai", TempPembayaranHolder.jamMulai)
        .putString("jam_selesai", TempPembayaranHolder.jamSelesai)
        .apply()
}

fun loadPaymentSession(context: Context) {
    val prefs = context.getSharedPreferences("payment_session", Context.MODE_PRIVATE)
    TempPembayaranHolder.orderId = prefs.getString("order_id", null)
    TempPembayaranHolder.redirectUrl = prefs.getString("redirect_url", null)
    TempPembayaranHolder.tanggal = prefs.getString("tanggal", null)
    TempPembayaranHolder.jamMulai = prefs.getString("jam_mulai", null)
    TempPembayaranHolder.jamSelesai = prefs.getString("jam_selesai", null)
}

// ================= SCREEN PESAN JADWAL =================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesanJadwalScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val kursus = SelectedKursusHolder.selected
    val paket = SelectedPaketHolder.selected
    if (paket == null) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Paket tidak ditemukan", Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
        return
    }

    val jamBuka = kursus?.jamBuka?.let { LocalTime.parse(it) }
    val jamTutup = kursus?.jamTutup?.let { LocalTime.parse(it) }


    // load session lama kalau ada
    LaunchedEffect(Unit) {
        loadPaymentSession(context)
    }

    // AMBIL DATA USER DARI SHARED PREFERENCES
    val prefs = remember {
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    }
    LaunchedEffect(Unit) {
        getUserLocation(context) { lat, lng ->
            prefs.edit()
                .putString("user_lat", lat.toString())
                .putString("user_lng", lng.toString())
                .apply()
        }
    }
    val userName = prefs.getString("user_name", "") ?: ""
    val userEmail = prefs.getString("user_email", "") ?: ""
    val currentUserId = prefs.getLong("user_id", 0L)

    val today = remember { LocalDate.now() }
    val earliestDate = remember { today.plusDays(1) } // minimal besok
    var sudahBayar by remember { mutableStateOf(false) }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedStartTime by remember { mutableStateOf<LocalTime?>(null) }
//    var selectedEndTime by remember { mutableStateOf<LocalTime?>(null) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

    var loading by remember { mutableStateOf(false) }

    fun openDatePicker() {
        val initial = earliestDate
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val picked = LocalDate.of(year, month + 1, dayOfMonth)
                if (picked.isBefore(earliestDate)) {
                    Toast.makeText(
                        context,
                        "Pilih minimal tanggal besok.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    selectedDate = picked
                }
            },
            initial.year,
            initial.monthValue - 1,
            initial.dayOfMonth
        ).apply {
            datePicker.minDate = earliestDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.show()
    }

    fun openTimePicker() {
        val now = LocalTime.now()
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val picked = LocalTime.of(hourOfDay, minute)

                if (jamBuka != null && jamTutup != null) {
                    if (picked.isBefore(jamBuka)) {
                        Toast.makeText(
                            context,
                            "Jam mulai tidak boleh sebelum ${jamBuka}",
                            Toast.LENGTH_LONG
                        ).show()
                        return@TimePickerDialog
                    }

                    if (picked.isAfter(jamTutup)) {
                        Toast.makeText(
                            context,
                            "Jam mulai tidak boleh lewat ${jamTutup}",
                            Toast.LENGTH_LONG
                        ).show()
                        return@TimePickerDialog
                    }
                }

                selectedStartTime = picked
            },
            now.hour,
            now.minute,
            true
        ).show()
    }


//    val canSubmit = selectedDate != null &&
//            selectedStartTime != null &&
//            selectedEndTime != null
    val canSubmit = selectedDate != null && selectedStartTime != null


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pesan Jadwal") },
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

            Text(text = paket.namaPaket, style = MaterialTheme.typography.titleLarge)
            Text("Harga: Rp ${paket.harga}")
            Text("Durasi: ${paket.durasiJam} jam")
            Text("Jenis kendaraan: ${paket.jenisKendaraan ?: "-"}")
            Text("Waktu buka: ${kursus?.jamBuka ?: "-"}")
            Text("Waktu tutup: ${kursus?.jamTutup ?: "-"}")



            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openDatePicker() }
            ) {
                OutlinedTextField(
                    value = selectedDate?.format(dateFormatter) ?: "",
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Tanggal kursus (minimal besok)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable { openTimePicker(isStart = true) }
//            ) {
//                OutlinedTextField(
//                    value = selectedStartTime?.toString() ?: "",
//                    onValueChange = {},
//                    enabled = false,
//                    label = { Text("Jam mulai") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openTimePicker() }
            ) {
                OutlinedTextField(
                    value = selectedStartTime?.toString() ?: "",
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Jam mulai") },
                    modifier = Modifier.fillMaxWidth()
                )
            }


//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable { openTimePicker(isStart = false) }
//            ) {
//                OutlinedTextField(
//                    value = selectedEndTime?.toString() ?: "",
//                    onValueChange = {},
//                    enabled = false,
//                    label = { Text("Jam selesai") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }

            Spacer(Modifier.height(16.dp))

            if (loading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            // ================== TOMBOL BAYAR ==================
            Button(
                enabled = canSubmit && !loading,
                onClick = {
                    scope.launch {
                        loading = true

                        // SELALU buat orderId baru -> transaksi baru
                        val newOrderId = "ORD-${System.currentTimeMillis()}"
                        TempPembayaranHolder.orderId = newOrderId
                        TempPembayaranHolder.tanggal = selectedDate?.toString()
                        TempPembayaranHolder.jamMulai = selectedStartTime?.toString()
//                        TempPembayaranHolder.jamSelesai = selectedEndTime?.toString()
                        TempPembayaranHolder.redirectUrl = null
                        TempPembayaranHolder.pemesananSudahDibuat = false

                        // ambil user dari shared prefs
                        val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
                        val userName = prefs.getString("user_name", "") ?: ""
                        val userEmail = prefs.getString("user_email", "") ?: ""
                        val currentUserId = prefs.getLong("user_id", 0L)


                        // 1) simpan draft pembayaran
                        val okDraft = insertPembayaranDraft(
                            orderId = newOrderId,
                            amount = paket.harga,     // pastikan tipe Long
                            userId = currentUserId
                        )

                        if (!okDraft) {
                            loading = false
                            Toast.makeText(
                                context,
                                "Gagal menyimpan data pembayaran ke server",
                                Toast.LENGTH_LONG
                            ).show()
                            return@launch
                        }

                        // 2) bikin transaksi Snap
                        val snap = createSnapTransaction(
                            orderId = newOrderId,
                            amount = paket.harga,
                            customerName = userName.ifBlank { "User" },
                            customerEmail = userEmail.ifBlank { "demo@example.com" }
                        )

                        loading = false

                        if (snap == null) {
                            Toast.makeText(
                                context,
                                "Gagal membuat transaksi Midtrans",
                                Toast.LENGTH_LONG
                            ).show()
                            return@launch
                        }

                        TempPembayaranHolder.redirectUrl = snap.redirectUrl

                        // 3) buka halaman Midtrans
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(snap.redirectUrl)
                        )
                        context.startActivity(intent)
                        sudahBayar=true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Bayar")
            }


            Spacer(Modifier.height(8.dp))

            // --- tombol CEK STATUS PEMBAYARAN ---
            Button(
                enabled = sudahBayar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (sudahBayar) MaterialTheme.colorScheme.primary else Color.Gray
                ),
                onClick = {
                    if (TempPembayaranHolder.orderId == null) {
                        Toast.makeText(
                            context,
                            "Belum ada transaksi yang dibuat.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        navController.navigate("status_pembayaran")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cek status pembayaran")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Batal")
            }
        }
    }
}
