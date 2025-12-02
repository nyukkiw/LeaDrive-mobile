package com.example.leadrive

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.clickable
// ====== DATA CLASS UNTUK UI ======
data class PaketKursusUi(
    val idPaket: Long,
    val namaPaket: String,
    val harga: Long,
    val durasiJam: Int,
    val deskripsi: String?,
    val jenisKendaraan: String?
)

// Holder sederhana untuk menyimpan paket yang dipilih
object SelectedPaketHolder {
    var selected: PaketKursusUi? = null
}


// ====== FETCH DARI SUPABASE (paket_kursus) ======
suspend fun fetchPaketForKursus(kursusId: String): List<PaketKursusUi> {
    return try {
        withContext(Dispatchers.IO) {
            // filter berdasarkan id_kursus
            val base = SupabaseClient.SUPABASE_URL.trimEnd('/')
            val urlString =
                "$base/rest/v1/paket_kursus" +
                        "?select=id_paket,nama_paket,harga,durasi_jam,deskripsi,jenis_kendaraan,id_kursus" +
                        "&id_kursus=eq.$kursusId"

            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("apikey", SupabaseClient.SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer ${SupabaseClient.SUPABASE_KEY}")
                setRequestProperty("Accept", "application/json")
                connectTimeout = 10_000
                readTimeout = 10_000
            }

            val code = conn.responseCode
            val body = if (code in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }
            conn.disconnect()

            if (code !in 200..299) return@withContext emptyList<PaketKursusUi>()

            val jsonArray = JSONArray(body)
            val list = mutableListOf<PaketKursusUi>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)

                val id = obj.optLong("id_paket")
                val namaPaket = obj.optString("nama_paket", "Tanpa nama")
                val harga = obj.optLong("harga", 0L)
                val durasiJam = obj.optInt("durasi_jam", 0)
                val deskripsi = obj.optString("deskripsi", null)
                val jenisKendaraan = obj.optString("jenis_kendaraan", null)

                list.add(
                    PaketKursusUi(
                        idPaket = id,
                        namaPaket = namaPaket,
                        harga = harga,
                        durasiJam = durasiJam,
                        deskripsi = deskripsi,
                        jenisKendaraan = jenisKendaraan
                    )
                )
            }

            // optional: urutkan paket dari harga termurah
            list.sortedBy { it.harga }
        }
    } catch (e: Exception) {
        android.util.Log.e("FetchPaket", "Error fetchPaketForKursus: ${e.message}", e)
        emptyList()
    }
}

// ====== SCREEN: LIST PAKET UNTUK 1 KURSUS ======
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PilihPaketScreen(navController: NavController) {
    val context = LocalContext.current
    val kursus = SelectedKursusHolder.selected

    // kalau user nyasar ke sini tanpa pilih kursus dulu
    if (kursus == null) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Data kursus tidak ditemukan", Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
        return
    }

    var loading by remember { mutableStateOf(true) }
    var paketList by remember { mutableStateOf<List<PaketKursusUi>>(emptyList()) }

    // load paket ketika screen dibuka
    LaunchedEffect(kursus.id) {
        loading = true
        val result = fetchPaketForKursus(kursus.id)
        paketList = result
        if (result.isEmpty()) {
            Toast.makeText(context, "Tidak ada paket untuk kursus ini", Toast.LENGTH_LONG).show()
        }
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pilih Paket - ${kursus.name}") },
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
                .fillMaxSize()
        ) {

            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (paketList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada paket tersedia.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(paketList) { paket ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // simpan paket yang dipilih
                                    SelectedPaketHolder.selected = paket
                                    // navigasi ke layar pesan jadwal
                                    navController.navigate("pesan_jadwal")
                                }
                        ){
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = paket.namaPaket,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(Modifier.height(4.dp))
                                Text("Harga: Rp ${paket.harga}")
                                Text("Durasi: ${paket.durasiJam} jam")
                                Text("Jenis kendaraan: ${paket.jenisKendaraan ?: "-"}")
                                Spacer(Modifier.height(4.dp))
                                Text(paket.deskripsi ?: "-", style = MaterialTheme.typography.bodySmall)

                                // nanti kalau mau lanjut ke step pilih jadwal, tombolnya bisa di sini
                                /*
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        // TODO: navigate ke screen jadwal dengan paket.id
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Pilih paket ini")
                                }
                                */
                            }
                        }
                    }
                }
            }
        }
    }
}
