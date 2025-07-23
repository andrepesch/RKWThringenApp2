package com.example.rkwthringenapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.rkwthringenapp.ui.AppNavigation
import com.example.rkwthringenapp.ui.theme.RKWThüringenAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RKWThüringenAppTheme {
                // Die AppNavigation steuert jetzt alles.
                // Die onSendClick-Logik wird jetzt innerhalb der Screens gehandhabt.
                AppNavigation()
            }
        }
    }
}