package com.northwindinteractive.penntestapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.northwindinteractive.penntestapp.ui.composables.VenueCheckInScreen
import com.northwindinteractive.penntestapp.ui.theme.PennTestAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivityCheck", "onCreate called, savedInstanceState=$savedInstanceState")

        enableEdgeToEdge()
        setContent {
            PennTestAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    VenueCheckInScreen()
                }
            }
        }
    }
}