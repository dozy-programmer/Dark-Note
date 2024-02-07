package com.akapps.instantapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akapps.instantapp.ui.theme.DailyNoteTheme
import com.google.android.gms.instantapps.InstantApps

class InstantActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DailyNoteTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    InstantMessage()
                }
            }
        }
    }


    @Composable
    private fun InstantMessage() {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.app_info),
                        textAlign = TextAlign.Center
                    )

                    Button(
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp),
                        onClick = { showInstallPrompt() }
                    ) {
                        Text(stringResource(R.string.button_download))
                    }
                }
            }
        }
    }

    private fun showInstallPrompt() {
        val postInstall = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_DEFAULT)
            .setPackage(packageName)

        InstantApps.showInstallPrompt(this, postInstall, 0, null)
    }

}