package com.example.leadrive

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.leadrive.ui.theme.LeaDriveTheme
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import androidx.compose.foundation.lazy.items

val supabase = createSupabaseClient(
    supabaseUrl = "https://xyzcompany.supabase.co",
    supabaseKey = "publishable-or-anon-key"
) {
//    install(Auth)
    install(Postgrest)
    //install other modules
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge() // Bisa diaktifkan atau tidak, Compose menangani insets dengan baik
        setContent {
            LeaDriveTheme {
                // Panggil Composable utama kita di sini
                MainScreen()
            }
        }
    }
}





@Composable
fun MainScreen() {
    // Box digunakan untuk menumpuk elemen, mirip seperti FrameLayout atau ConstraintLayout
    // Kita buat Box ini memenuhi seluruh layar dan menjadi latar belakang abu-abu
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray), // Latar belakang utama
        contentAlignment = Alignment.Center // Konten di dalamnya akan diposisikan di tengah
    ) {
        // Ini adalah View putih di belakang tombol
        Box(
            modifier = Modifier
                .width(220.dp) // Lebar kotak
                .height(270.dp) // Tinggi kotak
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp)) // Memberi bayangan
                .clip(RoundedCornerShape(20.dp)) // Memberi sudut tumpul
                .background(Color.White), // Memberi warna latar belakang putih
            contentAlignment = Alignment.Center // Konten di dalam kotak putih ini juga akan di tengah
        ) {
            // Ini adalah Tombol "Peserta"
            Button(
                onClick = { /* Aksi saat tombol diklik, misalnya pindah halaman */ },
                shape = RoundedCornerShape(20.dp), // Memberi sudut tumpul pada tombol
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFA500) // Warna oranye (0xFFFFA500)
                )
            ) {
                Text(text = "Peserta")
            }
        }
    }
}


@Serializable
data class Note(
    val id: Int,
    val body: String
)



@Composable
fun Notelist(){
    val notes = remember { mutableStateListOf<Note>() }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                // Selalu bagus untuk membungkus network call dengan try-catch
                val results = supabase.from("mobil").select().decodeList<Note>()
                notes.addAll(results)
            } catch (e: Exception) {
                // Tangani error, misalnya tampilkan log
                Log.e("NoteList", "Gagal mengambil data: ${e.message}")
            }
        }


        }
    LazyColumn {
        items(items = notes) { note->
            ListItem(headlineContent = {Text(text = note.body)})
        }
    }



    }








@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    LeaDriveTheme {
        MainScreen()
    }
}
