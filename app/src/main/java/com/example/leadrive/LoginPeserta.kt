package com.example.leadrive


import android.util.Log
//import io.github.jan.supabase.postgrest.query.postgrestFilter

import io.github.jan.supabase.postgrest.query.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import android.widget.Toast
import android.content.Context
import androidx.compose.ui.platform.LocalContext

@kotlinx.serialization.Serializable
data class User(
    val email: String,
    val password: String,
    val name: String

)

@Composable
fun LoginPeserta(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) } // Tambahkan state untuk loading

    val scope = rememberCoroutineScope()
    val supabase = SupabaseClient.client
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login Peserta", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // Input untuk Username
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Input untuk Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Input untuk Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))






        // Tombol Login
        Button(
            onClick = {

                scope.launch {
                    val inputNama=username
                    val inputEmail=email
                    val inputPassword=password

                    try {
//                        val cekagain = supabase.postgrest["users"].select(columns = Columns.list("name", "email", "password")){
//                            filter{
//                                eq("email", "anyu@gmail.com")
//                            }
//                        }
//                        Log.d("cekagain", "isi dari response: ${cekagain.data}")
//
//                        val allUsers = supabase.from("users").select().decodeList<User>()
//                        Log.d("allUserCek","isi: $allUsers")

                        val ambilData = supabase.from("users").select().decodeList<User>()
                        val cariData = ambilData.find { it.email.equals(email, ignoreCase = true) }
                        Log.d("dataEmailInput,","data data yang diinput: ${inputEmail},${email}")
                        Log.d("test email","ini data yang di dapat: ${cariData?.email},${cariData?.password},${cariData?.name}")
                        Log.d("alo","halo")
                        if((cariData?.email == inputEmail && cariData.password == inputPassword) && cariData.name == inputNama){

                            // SIMPAN USER DI SHARED PREFERENCES
                            val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
                            prefs.edit()
                                .putString("user_email", cariData.email)
                                .putString("user_name", cariData.name)
                                .apply()

                            Toast.makeText(context,"Login Berhasil", Toast.LENGTH_SHORT).show()
                            navController.navigate("beranda_Peserta")
                        }else{
                            Toast.makeText(context,"Login Gagal, periksa kembali email dan password", Toast.LENGTH_LONG).show()
                        }

                        Log.d("cekResponse", "isi dari response: ${ambilData.size}")
                    }catch (e :Exception){
                        Log.d("catchResponse", "isi dari response: $e")
                    }



                }





                // Prevent multiple clicks while loading
//                if (isLoading) return@Button
//
//                // Validate input fields
//                if (email.isBlank() || password.isBlank()) {
//                    message = "Email and password cannot be empty."
//                    return@Button
//                }
//
//                isLoading = true
//                message = ""
//
//                scope.launch {
//                   try {
//                       // 1. Log input pengguna untuk memastikan tidak ada spasi yang tidak diinginkan
//                       Log.d("LoginPeserta", "Attempting login with email: [${email.trim()}]")
//
//
//                       Log.d("LoginDebug", "Email yang dicari: '$email'")
//
//                       // Fetch the user data including the password for comparison
//                       val response = supabase.from("users").select (columns = Columns.list("email", "password")){
//                           filter{
//                               eq("email",email)
//                           }
//                       }
//
//                       Log.d("LoginResponse", "isi dari response: $response")
//                        val person = response.decodeList<User>()
//                       Log.d("LoginDebug", "Jumlah user ditemukan: ${person.size}")
//                       Log.d("LoginDebug", "Users: $person")
//                       Log.d("usersRespon", "isi: ${person.size}")
//                       if (person != null) {
//                           val pers=person[0]
//                           // User found, now check the password
//                           // NOTE: This is a basic check. For production, use a secure hashing method.
//                           if (pers.password == password) {
//                               // Password matches
//                               message = "Login successful! Welcome, ${pers.name}."
//                               Toast.makeText(context, "Login Berhasil", Toast.LENGTH_SHORT).show()
//                               // Navigate to the home screen upon successful login
//                               navController.navigate("berandaPeserta") {
//                                   // Clear the back stack to prevent going back to the login screen
//                                   popUpTo("login") { inclusive = true }
//                               }
//                           } else {
//                               // Password does not match
//                               message = "Invalid password."
//                           }
//                       } else {
//                           // No user found with that email
//                           message = "Invalid email "
//                       }
//                   }catch (e: Exception){
//                       // Handle exceptions, e.g., network errors
//                       message = "An error occurred: ${e.message}"
//                       Log.e("LoginPeserta", "Error during login", e)
//                   }finally {
//                       isLoading = false // Reset loading state
//                   }
//
//
//
//                }


            }, modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        //tombol register
        Button(
            onClick = {

                navController.navigate("daftar")
            }, modifier = Modifier.fillMaxWidth()

                ){
                Text("Register")
            }


        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message)
    }
}
