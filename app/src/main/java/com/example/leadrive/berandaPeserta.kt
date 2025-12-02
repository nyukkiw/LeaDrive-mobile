package com.example.leadrive

// Compose + UI
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Navigation
import androidx.navigation.NavController
import androidx.navigation.compose.*

// Coroutines
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import androidx.compose.runtime.rememberCoroutineScope

// Android platform APIs
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast

// Activity result / permission helpers
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

// Play services location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

// Math
import kotlin.math.*













// helper: open app settings (when permission permanently denied)
fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.fromParts("package", context.packageName, null)
    context.startActivity(intent)
}

// ---------------- Composables (Beranda UI) ----------------

@Composable
fun BerandaPeserta(navControllerUtama: NavController) { // NavController from MainActivity
    val navControllerBawah = rememberNavController()

    val items = listOf(
        BottomNavItem("Beranda", Icons.Default.Home, "beranda_content"),
        BottomNavItem("Setting", Icons.Default.Settings, "setting")
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navControllerBawah.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (item.route == "setting") {
                                navControllerUtama.navigate("setting_peserta") {}
                            }else if(item.route == "beranda_content"){
                                navControllerUtama.navigate("beranda_peserta") {}
                            } else {
                                navControllerBawah.navigate(item.route) {
                                    popUpTo(navControllerBawah.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navControllerBawah,
            startDestination = "beranda_content",
            modifier = Modifier.padding(innerPadding)
        ) {
            // <-- penting: BerandaContent harus menerima navControllerBawah
            composable("beranda_content") { BerandaContent(navControllerBawah) }
            composable("list_kursus") { ListKursusScreen(navControllerUtama) }
            composable("detail_kursus") { DetailKursusScreen(navControllerUtama) }
        }
    }
}


@Composable
fun BerandaContent(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // <= pastikan deklarasi loading ada
    var loading by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        TopBanner()
        Spacer(modifier = Modifier.height(12.dp))

        KursusCard(
            paket = "Paket: (nama paket)",
            mobil = "Mobil: (Nama dan jenis)",
            onDetailClick = {}
        )

        Spacer(modifier = Modifier.height(18.dp))

        ActionRow(
            onNearbyClick = {

                    // cukup navigasi ke screen list; logic lokasi & fetch dipindah ke ListKursusScreen
                    navController.navigate("list_kursus")
                },
            onScheduleClick = { /* ... */ },
            onHistoryClick = { /* ... */ },
            onProfileClick = { /* ... */ }
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (loading) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp))
            }

        }
    }
}

// --- UI building blocks (TopBanner, KursusCard, ActionRow, ActionItem, SettingScreen) ---
data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)

@Composable
fun TopBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF43A2FF), Color(0xFF7BD1FF))
                )
            )
            .padding(12.dp)
    ) {
        Column {
            Text("KURSUS MENGEMUDI", color = Color.White, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(40.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hinted search text", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun KursusCard(paket: String, mobil: String, onDetailClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("Kursus sekarang", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(paket)
            Text(mobil)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDetailClick) {
                Text("Lihat detail kursus")
            }
        }
    }
}

@Composable
fun ActionRow(
    onNearbyClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ActionItem(Icons.Default.Place, "Kursus\nterdekat", onNearbyClick)
        ActionItem(Icons.Default.CalendarMonth, "Lihat\njadwal", onScheduleClick)
        ActionItem(Icons.Default.History, "Riwayat\nkursus", onHistoryClick)
        ActionItem(Icons.Default.Person, "Profil", onProfileClick)
    }
}

@Composable
fun ActionItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable { onClick() }
    ) {
        Surface(
            shape = CircleShape,
            tonalElevation = 3.dp,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 12.sp)
    }
}

@Composable
fun SettingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ini Halaman Setting", style = MaterialTheme.typography.headlineMedium)
    }
}
