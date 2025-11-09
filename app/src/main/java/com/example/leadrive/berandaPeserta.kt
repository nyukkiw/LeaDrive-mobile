package com.example.leadrive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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
        BottomNavItem("Chat", Icons.Default.Chat, "chat"),
        BottomNavItem("Setting", Icons.Default.Settings, "setting")
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                // TODO: Ganti logic ini dengan state yang lebih baik nanti
                // Untuk sekarang, kita anggap item pertama (Beranda) selalu aktif
                val currentRoute = "beranda_content"

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
            composable("beranda_content") { BerandaContent() }
            composable("chat") { ChatScreen() }

        }
    }
}

/**
 * Ini adalah konten spesifik untuk halaman Beranda saja.
 */
@Composable
fun BerandaContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Selamat Datang di Beranda Peserta!", style = MaterialTheme.typography.headlineMedium)
        // Nanti semua konten beranda seperti daftar kursus, dll, akan ada di sini
    }
}

// --- Halaman Dummy (dari Langkah 1) ---

@Composable
fun ChatScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ini Halaman Chat", style = MaterialTheme.typography.headlineMedium)
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

