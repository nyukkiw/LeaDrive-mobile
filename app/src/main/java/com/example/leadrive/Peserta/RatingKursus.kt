package com.example.leadrive.Peserta

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import java.time.LocalDate
import kotlinx.coroutines.launch
import android.widget.Toast
import android.util.Log
import com.example.leadrive.SupabaseClient

@Serializable
data class RatingInsert(
    val id_user: Long,
    val id_kursus: Long,
    val rating: Int,
    val komentar: String?,
    val tanggal: String,
    val id_instruktur: Long? = null
)


@Composable
fun RatingKursusScreen(
    navController: NavController,
    idKursus: Long,
    namaKursus: String
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val userId = prefs.getLong("user_id", -1L)

    var rating by remember { mutableStateOf(0) }
    var komentar by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }


    val supabase = SupabaseClient.client
    val scope = rememberCoroutineScope() // 🔥 INI PENTING

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Beri Rating Kursus", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text(namaKursus, style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(16.dp))

        // ⭐ STAR RATING
        Row {
            (1..5).forEach { star ->
                IconButton(onClick = { rating = star }) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (star <= rating) Color(0xFFFFC107) else Color.Gray
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = komentar,
            onValueChange = { komentar = it },
            label = { Text("Komentar") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                Log.d("RATING_DEBUG", "userId=$userId, idKursus=$idKursus, rating=$rating")

                Toast.makeText(
                    context,
                    "userId=$userId | rating=$rating",
                    Toast.LENGTH_SHORT
                ).show()

                scope.launch {
                    try {
                        SupabaseClient.client
                            .from("rating_ulasan")
                            .insert(
                                RatingInsert(
                                    id_user = userId,
                                    id_kursus = idKursus,
                                    rating = rating,
                                    komentar = komentar.ifBlank { null },
                                    tanggal = LocalDate.now().toString(),
                                    id_instruktur = null
                                )
                            )


                        // ✅ INI TANDA BERHASIL
                        Toast.makeText(
                            context,
                            "Rating berhasil dikirim",
                            Toast.LENGTH_SHORT
                        ).show()

                        // balik ke halaman sebelumnya
                        navController.popBackStack()

                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "Gagal mengirim rating",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        ) {
            Text("Kirim Rating")
        }

    }
}

