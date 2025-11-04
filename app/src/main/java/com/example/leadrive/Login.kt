package com.example.leadrive

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.leadrive.ui.theme.LeaDriveTheme

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent{
            LeaDriveTheme {
                MainScreen()
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun LoginScreenPreview() {
        LeaDriveTheme {
            MainScreen()
        }
    }


}