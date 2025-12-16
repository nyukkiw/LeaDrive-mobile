package com.example.leadrive.Peserta
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.leadrive.ui.theme.LeaDriveTheme
import io.github.jan.supabase.SupabaseClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.Image // <-- TAMBAHKAN IMPORT INI
import androidx.compose.ui.res.painterResource // <-- TAMBAHKAN IMPORT INI
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.leadrive.R

class MainActivity : ComponentActivity() {

    private lateinit var supabase: SupabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supabase = com.example.leadrive.SupabaseClient.client
        enableEdgeToEdge()
        setContent {

            LeaDriveTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") { LoginScreen(navController) }
                        composable("loginPeserta") { LoginPeserta(navController=navController) }

                        composable("Daftar") {
                            daftar(navController = navController)
                        }
                        composable("beranda_peserta") {
                            // Pastikan Anda memanggil Composable yang benar
                            BerandaPeserta(navControllerUtama = navController)
                        }

                        composable("setting_peserta"){
                            SettingPesertaScreen(navController = navController)
                        }
                        composable("detail_kursus") {
                            DetailKursusScreen(navController)
                        }

                        composable("pilih_paket"){
                            PilihPaketScreen(navController)
                        }

                        composable("pesan_jadwal"){
                            PesanJadwalScreen(navController)
                        }

                        composable("status_pembayaran") {
                            StatusPembayaranScreen(navController)
                        }

                        composable(
                            route = "rating_kursus/{idKursus}/{namaKursus}",
                            arguments = listOf(
                                navArgument("idKursus") { type = NavType.LongType },
                                navArgument("namaKursus") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val idKursus = backStackEntry.arguments!!.getLong("idKursus")
                            val namaKursus = backStackEntry.arguments!!.getString("namaKursus")!!

                            RatingKursusScreen(
                                navController = navController,
                                idKursus = idKursus,
                                namaKursus = namaKursus
                            )
                        }


                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavController, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.mipmap.lea_drive_foreground),
            // GANTI 'leadrive' dengan nama file logo Anda
            contentDescription = "Logo Aplikasi", // Deskripsi untuk aksesibilitas
            modifier = Modifier
                .fillMaxWidth(0.5f) // Ukuran logo 50% dari lebar layar
                .padding(bottom = 48.dp) // Jarak dari logo ke teks di bawahnya
        )
        Text(
            text = "Masuk Sebagai",
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.navigate("loginPeserta") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(text = "Peserta", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { "" },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(text = "Instruktur", fontSize = 18.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LeaDriveTheme {
        val navController = rememberNavController()
        LoginScreen(navController)
    }
}



