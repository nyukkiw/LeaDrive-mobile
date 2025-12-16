package com.example.leadrive.Peserta

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// ==== FUNGSI MAPS ====
fun openMapsNavigation(context: Context, lat: Double, lng: Double, label: String) {
    val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        val webUri =
            Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailKursusScreen(navController: NavController) {
    val context = LocalContext.current

    // ⬇️ ambil data yang dipilih dari holder DI listKursus.kt
    val kursus = SelectedKursusHolder.selected

    if (kursus == null) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Data kursus tidak ditemukan", Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(kursus.name) },   // ⬅️ SELALU pakai kursus.name
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

            Text(
                "Nama kursus: ${kursus.name}",
                style = MaterialTheme.typography.titleMedium
            )
            Text("Alamat: ${kursus.lokasi ?: "-"}")
            Text("Jam buka: ${kursus.jamBuka ?: "-"}")
            Text("Jam tutup: ${kursus.jamTutup ?: "-"}")

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    openMapsNavigation(
                        context,
                        kursus.latitude,
                        kursus.longitude,
                        kursus.name
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Buka lokasi di Maps")
            }

            Button(
                onClick = {
                            navController.navigate("pilih_paket")
                          },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lihat / Pesan Jadwal")
            }
        }
    }
}
