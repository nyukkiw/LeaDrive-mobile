package com.example.leadrive

import android.util.Base64
import android.util.Log
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.OffsetDateTime
import io.github.jan.supabase.postgrest.query.Order
//import com.example.leadrive.BuildConfig

object MidtransConfig {
    val SERVER_KEY: String = BuildConfig.MIDTRANS_SERVER_KEY

}



data class SnapResponse(
    val token: String,
    val redirectUrl: String
)

// ================== MIDTRANS ==================

suspend fun createSnapTransaction(
    orderId: String,
    amount: Long,
    customerName: String,
    customerEmail: String
): SnapResponse? = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://app.sandbox.midtrans.com/snap/v1/transactions")

        val auth = Base64.encodeToString(
            "${MidtransConfig.SERVER_KEY}:".toByteArray(),
            Base64.NO_WRAP
        )

        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Authorization", "Basic $auth")
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 10000
            readTimeout = 10000
        }

        val bodyJson = JSONObject().apply {
            put("transaction_details", JSONObject().apply {
                put("order_id", orderId)
                put("gross_amount", amount)
            })
            put("customer_details", JSONObject().apply {
                put("first_name", customerName)
                put("email", customerEmail)
            })
            // ⬇️ Tambahan: bikin invoice expire 15 menit
            put("expiry", JSONObject().apply {
                // start_time boleh dikosongin → Midtrans pakai waktu sekarang
                put("unit", "minutes")   // "minutes" (pakai s)
                put("duration", 15)      // 15 menit
            })
        }.toString()

        conn.outputStream.use { it.write(bodyJson.toByteArray()) }

        val code = conn.responseCode
        val body = if (code in 200..299) {
            conn.inputStream.bufferedReader().use { it.readText() }
        } else {
            conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
        }
        conn.disconnect()

        if (code !in 200..299) {
            Log.e("Midtrans", "createSnapTransaction failed: HTTP $code, body=$body")
            return@withContext null
        }

        val json = JSONObject(body)
        SnapResponse(
            token = json.getString("token"),
            redirectUrl = json.getString("redirect_url")
        )
    } catch (e: Exception) {
        Log.e("Midtrans", "Error createSnapTransaction: ${e.message}", e)
        null
    }
}

suspend fun checkMidtransStatus(orderId: String): String? = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://api.sandbox.midtrans.com/v2/$orderId/status")

        val auth = Base64.encodeToString(
            "${MidtransConfig.SERVER_KEY}:".toByteArray(),
            Base64.NO_WRAP
        )

        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Basic $auth")
            connectTimeout = 10000
            readTimeout = 10000
        }

        val code = conn.responseCode
        val body = if (code in 200..299) {
            conn.inputStream.bufferedReader().use { it.readText() }
        } else {
            conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
        }
        conn.disconnect()

        if (code !in 200..299) {
            Log.e("Midtrans", "checkMidtransStatus failed: HTTP $code, body=$body")
            return@withContext null
        }

        val json = JSONObject(body)
        json.optString("transaction_status", null)
    } catch (e: Exception) {
        Log.e("Midtrans", "Error checkMidtransStatus: ${e.message}", e)
        null
    }
}

// ================== SUPABASE: PEMBAYARAN ==================

@Serializable
data class PembayaranDraft(
    val metode: String,
    val jumlah: Double,
    val status: String,
    val invoice: String,
    val id_user: Long
)

suspend fun insertPembayaranDraft(
    orderId: String,
    amount: Long,
    userId: Long
): Boolean = withContext(Dispatchers.IO) {
    val supabase = SupabaseClient.client
    try {
        val body = PembayaranDraft(
            metode = "midtrans",
            jumlah = amount.toDouble(),
            status = "belum_bayar",
            invoice = orderId,
            id_user = userId
        )

        supabase.from("pembayaran").insert(body)
        true
    } catch (e: Exception) {
        Log.e("PembayaranInsert", "Gagal insert draft: $e", e)
        false
    }
}

suspend fun updatePembayaranStatus(
    orderId: String,
    status: String
): Boolean = withContext(Dispatchers.IO) {
    try {
        val base = SupabaseClient.SUPABASE_URL.trimEnd('/')
        val url = URL("$base/rest/v1/pembayaran?invoice=eq.$orderId")

        val bodyJson = JSONObject().apply {
            put("status", status)
            if (status == "sudah_bayar") {
                put("tanggal_jam_pembayaran", OffsetDateTime.now().toString())
            }
        }.toString()

        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "PATCH"
            setRequestProperty("apikey", SupabaseClient.SUPABASE_KEY)
            setRequestProperty("Authorization", "Bearer ${SupabaseClient.SUPABASE_KEY}")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Prefer", "return=minimal")
            doOutput = true
            connectTimeout = 10000
            readTimeout = 10000
        }

        conn.outputStream.use { it.write(bodyJson.toByteArray()) }

        val code = conn.responseCode
        Log.d("PembayaranUpdate", "HTTP $code")
        conn.disconnect()

        code in 200..299
    } catch (e: Exception) {
        Log.e("PembayaranUpdate", "Error: ${e.message}", e)
        false
    }
}

// ================== SUPABASE: PEMESANAN + JADWAL ==================
@Serializable
data class PembayaranRow(
    val id_pembayaran: Long,
    val invoice: String,
    val id_pemesanan: Long? = null
)
@Serializable
data class PemesananRow(
    val id_pemesanan: Long,
    val id_paket: Long,
    val tanggal_pemesanan: String,
    val status_pemesanan: String,
    val id_user: Long,

    val latitude: String? = null,
    val longitude: String? = null
)
//data class PemesananRow(
//    val id_pemesanan: Long
//)

@kotlinx.serialization.Serializable
data class PemesananInsert(
    val id_paket: Long,
    val tanggal_pemesanan: String,
    val status_pemesanan: String,
    val id_user: Long,
    val latitude: String? = null,
    val longitude: String? = null
)

@kotlinx.serialization.Serializable
data class JadwalKursusInsert(
    val id_pemesanan: Long,
    val tanggal: String,
    val jam_mulai: String,
    val id_instruktur: Long? = null
)

@kotlinx.serialization.Serializable
data class PembayaranUpdateBody(
    val id_pemesanan: Long,
    val tanggal_jam_pembayaran: String
)


@Serializable
data class JadwalInsert(
    val id_pemesanan: Long,
    val tanggal: String,
    val jam_mulai: String,
    val id_instruktur: Long? = null
)

// ================== FUNGSI BARU ==================
/**
 * Dipanggil sekali ketika status pembayaran sudah "settlement".
 * - Tidak akan insert dua kali (cek id_pemesanan di tabel pembayaran).
 * - Simpan pemesanan + jadwal_kursus.
 * - Update pembayaran.id_pemesanan dan tanggal_jam_pembayaran.
 */
suspend fun createPemesananDanJadwal(
    orderId: String,
    idUser: Long,
    idPaket: Long,
    tanggalKursus: String,   // "YYYY-MM-DD"
    jamMulai: String,        // "HH:MM" atau "HH:MM:SS"
    latitude: String?,
    longitude: String?
): Boolean {
    val supabase = SupabaseClient.client

    return try {
        // 0. Ambil pembayaran dari invoice
        val pembayaranList = supabase.from("pembayaran").select {
            filter { eq("invoice", orderId) }
        }.decodeList<PembayaranRow>()

        if (pembayaranList.isEmpty()) {
            Log.e("PemesananCreate", "Tidak ada pembayaran dengan invoice $orderId")
            return false
        }

        val pembayaran = pembayaranList.first()

        // Kalau sudah punya id_pemesanan, jangan buat lagi
        if (pembayaran.id_pemesanan != null) {
            Log.d("PemesananCreate", "Sudah pernah buat pemesanan utk $orderId")
            return true
        }

        val today = LocalDate.now().toString()

        // 1) INSERT PEMESANAN
        val respPemesanan = supabase.from("pemesanan").insert(
            PemesananInsert(
                id_paket = idPaket,
                tanggal_pemesanan = today,
                status_pemesanan = "pending",   // default
                id_user = idUser,
                latitude = latitude,
                longitude = longitude
            )
        ) {
            // PENTING: cuma select(), TANPA single()
            select()
        }

        // Supabase balikin ARRAY [ { ... } ], jadi decodeList().first()
        val pemesananBaru = respPemesanan.decodeList<PemesananRow>().first()
        val idPemesanan = pemesananBaru.id_pemesanan

        // 2) INSERT JADWAL_KURSUS
        supabase.from("jadwal_kursus").insert(
            JadwalKursusInsert(
                id_pemesanan = idPemesanan,
                tanggal = tanggalKursus,
                jam_mulai = if (jamMulai.length == 5) "$jamMulai:00" else jamMulai,
                id_instruktur = null
            )
        )

        // 3) UPDATE PEMBAYARAN: isi id_pemesanan
        supabase.from("pembayaran").update(
            PembayaranUpdateBody(
                id_pemesanan = idPemesanan,
                tanggal_jam_pembayaran = today
            )
        ) {
            filter { eq("invoice", orderId) }
        }

        Log.d("PemesananCreate", "BERHASIL buat pemesanan & jadwal utk $orderId")
        true
    } catch (e: Exception) {
        Log.e("PemesananCreate", "Gagal: $e")
        false
    }
}
