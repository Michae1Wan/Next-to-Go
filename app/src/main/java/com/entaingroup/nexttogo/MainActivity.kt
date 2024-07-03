package com.entaingroup.nexttogo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.entaingroup.nexttogo.ui.compose.races.RacesScreen
import com.entaingroup.nexttogo.ui.theme.NextToGoTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NextToGoTheme {
                Scaffold { paddingValues ->
                    Box(modifier = Modifier
                        .padding(paddingValues)
                    ){
                        RacesScreen()
                    }
                }
            }
        }
    }
}