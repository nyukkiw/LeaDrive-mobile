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

@kotlinx.serialization.Serializable
data class User(
    val id: Int? = null,
    val name: String,
    val email: String,
    val email_verified_at: String? = null,
    val password: String,
    val remember_token: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val nomor_hp: String? = null,
    val status: String? = null

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
                    val person = supabase.from("users").select(columns = Columns.list("name","email" )){
                        filter{
                            eq("email",email)

                        }
                    }
                    println(person)
                }


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
