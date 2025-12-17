package com.example.leadrive.Peserta



import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.withContext

import androidx.compose.foundation.clickable

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

import android.widget.Toast



import android.util.Log







import android.Manifest

import android.content.Context

import android.content.pm.PackageManager

import android.location.Location

import androidx.core.content.ContextCompat

import com.google.android.gms.location.LocationServices

import com.google.android.gms.location.Priority

import kotlinx.coroutines.suspendCancellableCoroutine

import kotlin.coroutines.resume

import kotlin.coroutines.resumeWithException

import kotlin.math.*



import android.content.Intent

import android.net.Uri
import com.example.leadrive.SupabaseClient


import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL


// --- DATA CLASS ---

// ⬇️ INI DI listKursus.kt (JANGAN DI detailKursus.kt)

data class Kursus(

    val id: String,

    val name: String,

    val latitude: Double,

    val longitude: Double,

    val lokasi: String? = null,

    val jamBuka: String? = null,

    val jamTutup: String? = null

)







//@Serializable

//data class KursusRow(

// val id_kursus: Long,

// val nama_kursus: String?,

// val latitude: String?,

// val longitude: String?

//)



suspend fun getLastLocationSuspend(context: Context): Location? =

    suspendCancellableCoroutine { cont ->

        try {

            val hasPermission = ContextCompat.checkSelfPermission(

                context,

                Manifest.permission.ACCESS_FINE_LOCATION

            ) == PackageManager.PERMISSION_GRANTED



            if (!hasPermission) {

                cont.resume(null)

                return@suspendCancellableCoroutine

            }



            val client = LocationServices.getFusedLocationProviderClient(context)

            val task = client.lastLocation

            task.addOnSuccessListener { loc -> cont.resume(loc) }

                .addOnFailureListener { exc -> cont.resumeWithException(exc) }

            cont.invokeOnCancellation { /* nothing to clean up */ }

        } catch (e: Exception) {

            cont.resumeWithException(e)

        }

    }



suspend fun getCurrentLocationSuspend(context: Context): Location? =

    suspendCancellableCoroutine { cont ->

        try {

            val hasPermission = ContextCompat.checkSelfPermission(

                context,

                Manifest.permission.ACCESS_FINE_LOCATION

            ) == PackageManager.PERMISSION_GRANTED



            if (!hasPermission) {

                cont.resume(null)

                return@suspendCancellableCoroutine

            }



            val client = LocationServices.getFusedLocationProviderClient(context)

            val task = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)

            task.addOnSuccessListener { loc -> cont.resume(loc) }

                .addOnFailureListener { exc -> cont.resumeWithException(exc) }

            cont.invokeOnCancellation { /* nothing to clean up */ }

        } catch (e: Exception) {

            cont.resumeWithException(e)

        }

    }



// --- Haversine ---

fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {

    val R = 6371.0

    val dLat = Math.toRadians(lat2 - lat1)

    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2) * sin(dLat / 2) +

            cos(Math.toRadians(lat1)) *

            cos(Math.toRadians(lat2)) *

            sin(dLon / 2) * sin(dLon / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return R * c

}



fun openMapsNavigation(context: Context, lat: Double, lng: Double, label: String? = null) {

// pakai google.navigation supaya langsung mode arah

    val encodedLabel = label?.let { Uri.encode(it) } ?: ""

    val uri = Uri.parse("google.navigation:q=$lat,$lng($encodedLabel)")

    val intent = Intent(Intent.ACTION_VIEW, uri).apply {

        setPackage("com.google.android.apps.maps")

    }



// kalau aplikasi Google Maps tidak ada, buka lewat browser

    if (intent.resolveActivity(context.packageManager) != null) {

        context.startActivity(intent)

    } else {

        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")

        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))

    }

}









suspend fun fetchKursusFromServer(): List<Kursus> {

    return try {

// pindahkan seluruh I/O ke dispatcher IO

        withContext(Dispatchers.IO) {

            val url = "${SupabaseClient.SUPABASE_URL.trimEnd('/')}/rest/v1/kursus" +

                    "?select=id_kursus,nama_kursus,latitude,longitude,lokasi,jam_buka,jam_tutup"



            val conn = URL(url).openConnection() as HttpURLConnection

            conn.requestMethod = "GET"

            conn.setRequestProperty("apikey", SupabaseClient.SUPABASE_KEY)

            conn.setRequestProperty("Authorization", "Bearer ${SupabaseClient.SUPABASE_KEY}")

            conn.setRequestProperty("Accept", "application/json")

            conn.connectTimeout = 10_000

            conn.readTimeout = 10_000



            val code = conn.responseCode

            val body = if (code in 200..299) {

                conn.inputStream.bufferedReader().use { it.readText() }

            } else {

                conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""

            }

            Log.d("FetchKursus", "HTTP $code -> ${body.take(2000)}")

            conn.disconnect()



            if (code !in 200..299) return@withContext emptyList<Kursus>()



            val jsonArray = JSONArray(body)

            val result = mutableListOf<Kursus>()

            for (i in 0 until jsonArray.length()) {

                val obj = jsonArray.getJSONObject(i)

                val id = when {

                    obj.has("id_kursus") && !obj.isNull("id_kursus") -> obj.getString("id_kursus")

                    obj.has("id") && !obj.isNull("id") -> obj.getString("id")

                    else -> ""

                }

                val name = if (obj.has("nama_kursus") && !obj.isNull("nama_kursus")) obj.getString("nama_kursus") else obj.optString("name", "Tanpa nama")

                val lat = obj.optString("latitude").toDoubleOrNull() ?: obj.optDouble("latitude", Double.NaN).takeIf { !it.isNaN() } ?: 0.0

                val lon = obj.optString("longitude").toDoubleOrNull() ?: obj.optDouble("longitude", Double.NaN).takeIf { !it.isNaN() } ?: 0.0

                val lokasi = obj.optString("lokasi", "")

                val jamBuka = obj.optString("jam_buka", "")

                val jamTutup = obj.optString("jam_tutup", "")

                result.add(

                    Kursus(

                        id = id,

                        name = name,

                        latitude = lat,

                        longitude = lon,

                        lokasi = lokasi,

                        jamBuka = jamBuka,

                        jamTutup = jamTutup

                    )

                )

            }

            result

        }

    } catch (e: Exception) {

        Log.e("FetchKursus", "exception: ${e.message}", e)

        emptyList()

    }

}











data class KursusWithDistance(

    val kursus: Kursus,

    val distanceKm: Double

)

//

// Menyimpan kursus yang dipilih untuk dikirim ke halaman detail

object SelectedKursusHolder {

    var selected: Kursus? = null

}







@Composable

fun ListKursusScreen(navController: NavController) {

    val context = LocalContext.current

    val scope = rememberCoroutineScope()



    var kursusList by remember { mutableStateOf<List<Kursus>>(emptyList()) }

    var loading by remember { mutableStateOf(true) }



    var kursusWithDistance by remember { mutableStateOf<List<KursusWithDistance>>(emptyList()) }

// Load data saat screen muncul

    LaunchedEffect(Unit) {

        loading = true

        try {



            val fetched = fetchKursusFromServer()

            Log.d("ListKursus", "fetched ${fetched.size} kursus")



            val loc = getLastLocationSuspend(context) ?: getCurrentLocationSuspend(context)



            val mapped: List<KursusWithDistance> = if (loc != null) {

                fetched.map { k ->

// proteksi: jika lat/lon 0.0 then penalize dengan large distance

                    val validLat = k.latitude != 0.0

                    val validLon = k.longitude != 0.0

                    val distance = if (validLat && validLon) {

                        haversineKm(loc.latitude, loc.longitude, k.latitude, k.longitude)

                    } else {

// beri jarak besar agar muncul di bawah

                        Double.MAX_VALUE

                    }

                    KursusWithDistance(k, distance)

                }.sortedBy { it.distanceKm }

            } else {

// tidak ada lokasi -> beri distance = infinity (tampil tanpa urutan)

                fetched.map { KursusWithDistance(it, Double.MAX_VALUE / 4) }

            }



// simpan ke state untuk ditampilkan

            kursusWithDistance = mapped

            kursusList = mapped.map { it.kursus }







            Log.d("ListKursus", "first distances: ${mapped.take(5).map { Pair(it.kursus.name, it.distanceKm) }}")



            if (fetched.isEmpty()) {

                Toast.makeText(context, "Fetched 0 kursus (cek RLS/API)", Toast.LENGTH_LONG).show()

            } else if (loc == null) {

                Toast.makeText(context, "Menampilkan kursus tanpa pengurutan (lokasi tidak tersedia)", Toast.LENGTH_SHORT).show()

            }

        } catch (e: Exception) {

            Log.e("ListKursus", "exception in LaunchedEffect: ${e.message}", e)

            Toast.makeText(context, "Error mengambil kursus", Toast.LENGTH_SHORT).show()

            kursusList = emptyList()

        } finally {

            loading = false

        }

    }







    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {



        Text(

            text = "List Kursus Terdekat",

            style = MaterialTheme.typography.titleLarge,

            modifier = Modifier.align(Alignment.CenterHorizontally)

        )



        Spacer(modifier = Modifier.height(16.dp))



        if (loading) {

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

                CircularProgressIndicator()

            }

        } else {

            if (kursusWithDistance.isEmpty()) {

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

                    Text("Tidak ada data kursus", style = MaterialTheme.typography.bodyLarge)

                }

            } else {

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    items(kursusWithDistance) { item ->

                        val k = item.kursus

                        Log.d("dalam k","tampilkan hasil k: ${k.name}, ${k.lokasi},${k.jamBuka},${k.jamTutup} ")

                        Card(

                            modifier = Modifier

                                .fillMaxWidth()

                                .clickable {

                                    SelectedKursusHolder.selected = k

                                    navController.navigate("detail_kursus")

                                }

                        ) {

                            Column(modifier = Modifier.padding(16.dp)) {

                                Text(text = k.name, style = MaterialTheme.typography.titleMedium)

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(text = "Lat: ${k.latitude}, Lon: ${k.longitude}", style = MaterialTheme.typography.bodySmall)

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Jarak: ${
                                        if (!item.distanceKm.isFinite() || item.distanceKm == Double.MAX_VALUE) "—"
                                        else String.format("%.2f km", item.distanceKm)
                                    }",
                                    style = MaterialTheme.typography.bodySmall
                                )


                            }

                        }

                    }

                }

            }

        }



    }

}