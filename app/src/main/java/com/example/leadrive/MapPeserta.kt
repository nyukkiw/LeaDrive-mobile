package com.example.leadrive

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@SuppressLint("ClickableViewAccessibility")
@Composable
fun MapPesertaScreen(idPemesanan: Int, navController: NavController) {

    val context = LocalContext.current
    val supabase = SupabaseClient.client

    // State untuk Lokasi Peserta (Target)
    var targetLat by remember { mutableStateOf<Double?>(null) }
    var targetLon by remember { mutableStateOf<Double?>(null) }

    // State untuk Lokasi SAYA (Driver) - Realtime
    var myLocation by remember { mutableStateOf<GeoPoint?>(null) }

    // Client untuk akses GPS
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // --- BAGIAN 1: Request Izin & Ambil Lokasi GPS Saya ---
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            // Jika izin diberikan, ambil lokasi terakhir
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        myLocation = GeoPoint(location.latitude, location.longitude)
                        Log.d("GPS_SAYA", "Lokasi ditemukan: ${location.latitude}, ${location.longitude}")
                    } else {
                        Log.e("GPS_SAYA", "Lokasi null (Pastikan GPS aktif)")
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    // Jalankan Request Izin saat layar dibuka
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Sudah punya izin, langsung ambil
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    myLocation = GeoPoint(location.latitude, location.longitude)
                }
            }
        } else {
            // Belum punya izin, minta dulu
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // --- BAGIAN 2: Fetch Lokasi Peserta dari Supabase ---
    LaunchedEffect(idPemesanan) {
        try {
            val result = supabase.from("pemesanan")
                .select { filter { eq("id_pemesanan", idPemesanan) } }
                .decodeSingleOrNull<Pemesanan>()

            if (result != null) {
                val cleanLat = result.latitude?.replace(",", ".")
                val cleanLon = result.longitude?.replace(",", ".")
                targetLat = cleanLat?.toDoubleOrNull()
                targetLon = cleanLon?.toDoubleOrNull()
            }
        } catch (e: Exception) {
            Log.e("DEBUG_MAP", "Error fetch supabase: ${e.message}")
        }
    }

    // --- BAGIAN 3: Tampilkan UI ---
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        // Hanya tampilkan peta jika KEDUA lokasi (Saya & Peserta) sudah didapat
        if (targetLat != null && targetLon != null && myLocation != null) {
            OSMRoutingMapView(
                startPoint = myLocation!!, // Lokasi Realtime
                endPoint = GeoPoint(targetLat!!, targetLon!!) // Lokasi Peserta
            )
        } else {
            // Tampilan Loading
            CircularProgressIndicator()
            Text(
                text = if (myLocation == null) "Mencari GPS Anda..." else "Mengambil data peserta...",
                // PERBAIKAN DISINI:
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 50.dp)
            )
        }
        SmallFloatingActionButton(
            onClick = { navController.popBackStack() }, // Fungsi kembali
            modifier = Modifier
                .align(Alignment.TopStart) // Pojok Kiri Atas
                .padding(16.dp), // Jarak dari tepi layar
            containerColor = androidx.compose.ui.graphics.Color.White, // Agar kontras dengan peta
            contentColor = androidx.compose.ui.graphics.Color.Black
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali"
            )
        }
    }
}

@Composable
fun OSMRoutingMapView(startPoint: GeoPoint, endPoint: GeoPoint) {
    val context = LocalContext.current
    var routeOverlay by remember { mutableStateOf<Polyline?>(null) }
    var routeDistance by remember { mutableStateOf("Menghitung...") }

    // --- Hitung Rute (Realtime Start ke End) ---
    LaunchedEffect(startPoint, endPoint) {
        withContext(Dispatchers.IO) {
            try {
                val roadManager = OSRMRoadManager(context, "MyUserAgent/1.0")
                // Opsi: Gunakan MEAN_BY_CAR agar rute lewat jalan raya mobil
                // roadManager.setMean(OSRMRoadManager.MEAN_BY_CAR)

                val waypoints = arrayListOf(startPoint, endPoint)
                val road = roadManager.getRoad(waypoints)

                if (road.mStatus == org.osmdroid.bonuspack.routing.Road.STATUS_OK) {
                    val polyline = RoadManager.buildRoadOverlay(road)
                    polyline.outlinePaint.color = Color.BLUE
                    polyline.outlinePaint.strokeWidth = 15f

                    routeOverlay = polyline
                    routeDistance = "${"%.2f".format(road.mLength)} km"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Render Peta ---
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osm_prefs", 0))
            val map = MapView(ctx)
            map.setMultiTouchControls(true)
            map.controller.setZoom(16.0)
            map.controller.setCenter(startPoint) // Fokus awal ke driver
            map
        },
        update = { map ->
            map.overlays.clear()

            // Marker SAYA (Driver)
            val startMarker = Marker(map)
            startMarker.position = startPoint
            startMarker.title = "Posisi Anda"
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            // startMarker.icon = ContextCompat.getDrawable(context, R.drawable.ic_car) // Jika punya icon mobil
            map.overlays.add(startMarker)

            // Marker PESERTA
            val endMarker = Marker(map)
            endMarker.position = endPoint
            endMarker.title = "Lokasi Peserta ($routeDistance)"
            endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            map.overlays.add(endMarker)

            // Garis Rute
            routeOverlay?.let { poly ->
                map.overlays.add(poly)
            }

            // Agar peta otomatis zoom mencakup kedua titik (start & end)
            map.zoomToBoundingBox(BoundingBox.fromGeoPoints(listOf(startPoint, endPoint)), true)

            map.invalidate()
        }
    )
}