package com.example.leadrive

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardInstruktur(idInstruktur: Int, nama: String, photoUrl: String?, navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Konfirmasi Logout") },
            text = { Text("Apakah anda ingin logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                ) {
                    Text("Ya")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Tidak")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = photoUrl,
                            error = painterResource(id = R.drawable.ic_launcher_background), // Ganti dengan gambar error default
                            placeholder = painterResource(id = R.drawable.ic_launcher_foreground) // Ganti dengan gambar placeholder default
                        ),
                        contentDescription = "Foto Profil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = nama, fontSize = 20.sp, style = MaterialTheme.typography.titleLarge)
                        Text(text = "Instruktur", fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium)
                    }
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color(0xFFFF9800),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tombol Jadwal Kursus
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("jadwalKursus/$idInstruktur") },
                        icon = { Icon(Icons.Filled.DateRange, contentDescription = "Jadwal Kursus") },
                        label = { Text("Jadwal") },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = Color.White,
                            unselectedTextColor = Color.White,
                            indicatorColor = Color(0xFFFF9800)
                        )
                    )
                    // Tombol Status Kursus
                    NavigationBarItem(
                        selected = false,
                        onClick = { /*navController.navigate("statusKursus")*/ },
                        icon = { Icon(Icons.Filled.List, contentDescription = "Status Kursus") },
                        label = { Text("Status") },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = Color.White,
                            unselectedTextColor = Color.White,
                            indicatorColor = Color(0xFFFF9800)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("Belum ada Kursus yang Diambil", fontSize = 24.sp)
        }
    }
}
