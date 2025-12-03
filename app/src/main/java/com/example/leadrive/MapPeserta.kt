package com.example.leadrive

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@SuppressLint("ClickableViewAccessibility")
@Composable
fun MapPesertaScreen(idPemesanan: Int, navController: NavController) {

    val supabase = SupabaseClient.client

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    // --- BAGIAN 1: Fetch Lokasi Tujuan (Peserta) dari Supabase ---
    LaunchedEffect(idPemesanan) {
        try {
            Log.d("DEBUG_MAP", "Memulai request ke Supabase untuk ID: $idPemesanan")

            val result = supabase.from("pemesanan")
                .select {
                    filter {
                        eq("id_pemesanan", idPemesanan)
                    }
                }
                .decodeSingleOrNull<Pemesanan>()

            if (result != null) {
                // Bersihkan format angka (koma ke titik)
                val cleanLat = result.latitude?.replace(",", ".")
                val cleanLon = result.longitude?.replace(",", ".")

                val latDouble = cleanLat?.toDoubleOrNull()
                val lonDouble = cleanLon?.toDoubleOrNull()

                if (latDouble != null && lonDouble != null) {
                    latitude = latDouble
                    longitude = lonDouble
                    Log.d("DEBUG_MAP", "Lokasi ditemukan: $latitude, $longitude")
                }
            } else {
                Log.e("DEBUG_MAP", "Data kosong untuk ID: $idPemesanan")
            }

        } catch (e: Exception) {
            Log.e("DEBUG_MAP", "Error: ${e.message}", e)
        }
    }

    // --- BAGIAN 2: Tampilkan Peta dengan Rute ---
    if (latitude != null && longitude != null) {
        // Kita panggil fungsi peta yang baru
        OSMRoutingMapView(
            targetLat = latitude!!,
            targetLon = longitude!!
        )
    } else {
        Text("Memuat lokasi peserta dan rute...")
    }
}

@Composable
fun OSMRoutingMapView(targetLat: Double, targetLon: Double) {
    val context = LocalContext.current

    // State untuk menyimpan garis rute
    var routeOverlay by remember { mutableStateOf<Polyline?>(null) }

    // --- BAGIAN 3: Tentukan Titik Awal & Akhir ---

    // TODO: Ganti ini dengan lokasi GPS Asli Driver (gunakan FusedLocationProvider)
    // Untuk tes sekarang, kita pakai koordinat dummy (misal: Alun-alun atau lokasi dekat target)
    // Saya set koordinat yang agak dekat dengan target agar rute terlihat masuk akal
    // (Contoh: saya geser sedikit dari targetLat/Lon)
    val startPoint = GeoPoint(targetLat - 0.01, targetLon - 0.01)

    val endPoint = GeoPoint(targetLat, targetLon)

    // --- BAGIAN 4: Hitung Rute (Background Process) ---
    LaunchedEffect(targetLat, targetLon) {
        withContext(Dispatchers.IO) { // Wajib IO thread untuk networking
            try {
                val roadManager = OSRMRoadManager(context, "MyUserAgent/1.0")
                // roadManager.setMean(OSRMRoadManager.MEAN_BY_CAR) // Default mobil

                val waypoints = arrayListOf(startPoint, endPoint)

                // Minta rute ke server OSRM
                val road = roadManager.getRoad(waypoints)

                if (road.mStatus == org.osmdroid.bonuspack.routing.Road.STATUS_OK) {
                    // Buat garis visual dari data jalan
                    val polyline = RoadManager.buildRoadOverlay(road)

                    // Styling garis rute
                    polyline.outlinePaint.color = Color.BLUE // Warna Biru
                    polyline.outlinePaint.strokeWidth = 15f  // Ketebalan garis

                    routeOverlay = polyline
                    Log.d("DEBUG_ROUTE", "Rute berhasil dibuat. Jarak: ${road.mLength} km")
                } else {
                    Log.e("DEBUG_ROUTE", "Gagal load rute. Status: ${road.mStatus}")
                }
            } catch (e: Exception) {
                Log.e("DEBUG_ROUTE", "Error routing: ${e.message}")
            }
        }
    }

    // --- BAGIAN 5: Render Peta ---
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osm_prefs", 0))

            val map = MapView(ctx)
            map.setMultiTouchControls(true)
            map.controller.setZoom(15.0)
            map.controller.setCenter(endPoint) // Fokus ke tujuan
            map
        },
        update = { map ->
            // Bersihkan overlay lama agar tidak menumpuk saat recompose
            map.overlays.clear()

            // 1. Marker DRIVER (Awal)
            val startMarker = Marker(map)
            startMarker.position = startPoint
            startMarker.title = "Posisi Driver"
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            // startMarker.icon = resources... (bisa custom icon mobil)
            map.overlays.add(startMarker)

            // 2. Marker PESERTA (Tujuan)
            val endMarker = Marker(map)
            endMarker.position = endPoint
            endMarker.title = "Lokasi Peserta"
            endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            map.overlays.add(endMarker)

            // 3. Tambahkan Garis Rute (Jika sudah selesai loading)
            routeOverlay?.let { poly ->
                map.overlays.add(poly)
            }

            map.invalidate() // Refresh tampilan peta
        }
    )
}
