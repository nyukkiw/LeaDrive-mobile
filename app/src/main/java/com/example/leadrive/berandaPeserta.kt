package com.example.leadrive

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.compose.ui.draw.clip

// Data class untuk merepresentasikan setiap item di bottom navigation
data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

/**
 * Ini adalah Composable utama yang akan menjadi "rumah" bagi Navigasi Bawah.
 * Ia berisi Scaffold dan NavHost-nya sendiri.
 */
@Composable
fun BerandaPeserta(navControllerUtama: NavController) { // NavController dari MainActivity
    val navControllerBawah = rememberNavController() // NavController khusus untuk Bottom Nav

    // Daftar item untuk Bottom Navigation Bar
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
                          }else{
                              navControllerBawah.navigate(item.route){
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
        // NavHost untuk mengatur konten yang ditampilkan di atas bottom bar
        NavHost(
            navController = navControllerBawah,
            startDestination = "beranda_content",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("beranda_content") { BerandaContent(navControllerUtama) }


        }
    }
}

/**
 * Ini adalah konten spesifik untuk halaman Beranda saja.
 */
@Composable
fun BerandaContent(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp)
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
            onNearbyClick = {},
            onScheduleClick = {},
            onHistoryClick = {},
            onProfileClick = {}
        )

        Spacer(modifier = Modifier.height(18.dp))
    }
}
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
                modifier = Modifier.fillMaxWidth(0.85f).height(40.dp)
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
        modifier = Modifier.width(80.dp).clickable { onClick() }
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

